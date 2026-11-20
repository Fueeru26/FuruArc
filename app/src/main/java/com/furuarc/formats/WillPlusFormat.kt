package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * WillPlus / AdvHD (.arc), no magic bytes (detected via structure).
 * Layout: u32 extension count; each extension: ext[4], count u32, table offset u32;
 * each entry: name[N] (N = 8 or 12, without extension), size u32, offset u32 (absolute).
 * Full name = name + "." + extension. File contents (.wsc, .wip, etc.) are output as is.
 */
object WillPlusFormat : ArchiveFormat {
    override val id = "willplus"
    override val displayName = "ARC (WillPlus/AdvHD)"
    override val extensions = listOf("arc")

    override fun matches(head: ByteArray) = false // no magic; see detects()

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index ARC tidak valid")
        return r
    }

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset = IoUtil.sjis()): List<Entry> {
        var last: Exception? = null
        for (nameLen in intArrayOf(8, 12)) {
            try {
                return parseWith(src, nameLen, cs)
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index ARC tidak valid")
    }

    private fun parseWith(src: RandomSource, nameLen: Int, cs: java.nio.charset.Charset): List<Entry> {
        val len = src.length
        if (len < 16) throw IOException("File terlalu kecil")
        val extCount = src.readFully(0, 4).le().int
        if (extCount !in 1..64 || 4L + extCount * 12L > len) throw IOException("Header ARC tidak valid")
        val dirs = src.readFully(4, extCount * 12).le()
        val entrySize = nameLen + 8
        val out = ArrayList<Entry>()
        // data must lie after all entry tables (distinguishes name width 8 vs 12)
        var tablesEnd = 4L + extCount * 12L
        for (d in 0 until extCount) {
            val c = dirs.getInt(d * 12 + 4)
            val t = dirs.getInt(d * 12 + 8).toLong() and 0xffffffffL
            if (c > 0) tablesEnd = maxOf(tablesEnd, t + c.toLong() * entrySize)
        }
        for (d in 0 until extCount) {
            val o = d * 12
            val extBytes = ByteArray(4)
            for (k in 0 until 4) extBytes[k] = dirs.get(o + k)
            if (extBytes.any { it.toInt() != 0 && (it < 0x20 || it > 0x7e) }) throw IOException("Ekstensi tidak valid")
            val ext = IoUtil.cstr(extBytes, 0, 4, Charsets.US_ASCII)
            val count = dirs.getInt(o + 4)
            val tableOff = dirs.getInt(o + 8).toLong() and 0xffffffffL
            if (count < 0 || count > 500_000 || tableOff + count.toLong() * entrySize > len)
                throw IOException("Tabel ARC di luar arsip")
            if (count == 0) continue
            val tbl = src.readFully(tableOff, count * entrySize)
            val bb = tbl.le()
            for (i in 0 until count) {
                val e = i * entrySize
                val stem = IoUtil.cstr(tbl, e, nameLen, cs)
                val size = bb.getInt(e + nameLen).toLong() and 0xffffffffL
                val off = bb.getInt(e + nameLen + 4).toLong() and 0xffffffffL
                if (stem.isEmpty() || !IoUtil.saneName(stem) || off < tablesEnd || off + size > len)
                    throw IOException("Entri ARC tidak valid")
                out += Entry(if (ext.isEmpty()) stem else "$stem.$ext", off, size)
            }
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override val packOptions = listOf(
        OptionSpec(
            key = "wp_name", kind = OptKind.CHOICE,
            en = "Name field size", id = "Ukuran kolom nama",
            choices = listOf(
                Choice("0", "Auto (by longest name)", "Auto (sesuai nama terpanjang)"),
                Choice("8", "8 bytes", "8 byte"),
                Choice("12", "12 bytes", "12 byte"),
            ),
            default = "0",
            hintEn = "Choose Auto if you are not sure.", hintId = "Pilih Auto jika tidak mengetahuinya.",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val cs = IoUtil.cs(opts)
        class Item(val input: PackInput, val stem: ByteArray, val ext: String)
        val items = inputs.map {
            val fn = it.path.substringAfterLast('/')
            val dot = fn.lastIndexOf('.')
            val stem = if (dot > 0) fn.substring(0, dot) else fn
            val ext = if (dot > 0) fn.substring(dot + 1) else ""
            if (ext.length > 4 || ext.any { c -> c.code !in 0x20..0x7e })
                throw IOException("Ekstensi tidak didukung: ${it.path}")
            Item(it, stem.toByteArray(cs), ext)
        }
        val maxStem = items.maxOfOrNull { it.stem.size } ?: 0
        val forced = opts.int("wp_name", 0)
        val nameLen = when {
            forced == 12 -> if (maxStem <= 12) 12 else throw IOException("Nama file terlalu panjang (maks 12 byte tanpa ekstensi)")
            forced == 8 -> if (maxStem <= 8) 8 else throw IOException("Nama file terlalu panjang untuk 8 byte (maks 8 byte tanpa ekstensi)")
            maxStem <= 8 -> 8
            maxStem <= 12 -> 12
            else -> throw IOException("Nama file terlalu panjang (maks 12 byte tanpa ekstensi)")
        }
        val groups = items.groupBy { it.ext }.entries.toList()
        if (groups.size > 64) throw IOException("Terlalu banyak jenis ekstensi")
        val entrySize = nameLen + 8
        val headerSize = 4L + groups.size * 12L
        var tablePos = headerSize
        val tableOffs = LongArray(groups.size)
        groups.forEachIndexed { i, g -> tableOffs[i] = tablePos; tablePos += g.value.size.toLong() * entrySize }
        var dataPos = tablePos

        val head = ByteBuffer.allocate(tablePos.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        head.putInt(groups.size)
        groups.forEachIndexed { i, g ->
            val eb = ByteArray(4)
            g.key.toByteArray(Charsets.US_ASCII).copyInto(eb)
            head.put(eb).putInt(g.value.size).putInt(tableOffs[i].toInt())
        }
        val order = ArrayList<PackInput>(items.size)
        for (g in groups) for (item in g.value) {
            val nb = ByteArray(nameLen)
            item.stem.copyInto(nb)
            head.put(nb).putInt(item.input.size.toInt()).putInt(dataPos.toInt())
            dataPos += item.input.size
            order += item.input
        }
        if (dataPos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(head.array())
            val buf = ByteArray(256 * 1024)
            order.forEachIndexed { i, f ->
                progress.update(i, order.size, f.path)
                IoUtil.appendInput(raf, f, buf)
            }
            progress.update(order.size, order.size, "selesai")
        }
    }
}
