package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Softpal PAC. Two variants (following msg-tool and GARbro):
 *  - Classic: NO magic. u32 count at offset 0, index at 0x3FE, entry = name[0x20 or 0x10] + u32 size + u32 offset.
 *    The first entry must start exactly at the end of the index (used for detection).
 *  - Amuse Craft: magic "PAC ", count at offset 8, index at 0x804, name[0x20].
 * File contents (script.src etc.; those prefixed with '$' are encrypted) are extracted as is. Pack writes the classic variant.
 */
object SoftpalFormat : ArchiveFormat {
    override val id = "softpal"
    override val displayName = "PAC (Softpal ADV System)"
    override val extensions = listOf("pac")

    private const val CLASSIC_INDEX = 0x3FEL
    private const val AMUSE_INDEX = 0x804L

    override fun matches(head: ByteArray) = false // classic has no magic; see detects()

    override fun detects(src: RandomSource, head: ByteArray): Boolean =
        try { parse(src, IoUtil.sjis()).isNotEmpty() } catch (e: Exception) { false }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val r = parse(src, IoUtil.cs(opts))
        if (r.isEmpty()) throw IOException("Index PAC tidak valid")
        return r
    }

    private fun parse(src: RandomSource, cs: java.nio.charset.Charset): List<Entry> {
        val len = src.length
        if (len < 0x40) throw IOException("File PAC terlalu kecil")
        val head = src.readFully(0, 12)
        val amuse = head[0] == 'P'.code.toByte() && head[1] == 'A'.code.toByte() &&
            head[2] == 'C'.code.toByte() && head[3] == ' '.code.toByte()
        val count = head.le().getInt(if (amuse) 8 else 0)
        if (count <= 0 || count > 500_000) throw IOException("Jumlah entri PAC tidak valid")
        val candidates = if (amuse) listOf(AMUSE_INDEX to 0x20) else listOf(CLASSIC_INDEX to 0x20, CLASSIC_INDEX to 0x10)
        var last: Exception? = null
        for ((indexOff, nameLen) in candidates) {
            try {
                val entrySize = nameLen + 8
                val tableEnd = indexOff + count.toLong() * entrySize
                if (tableEnd > len) throw IOException("Index PAC di luar arsip")
                // the first entry must be exactly at the end of the index
                val firstOff = src.readFully(indexOff + nameLen + 4, 4).le().int.toLong() and 0xffffffffL
                if (firstOff != tableEnd) throw IOException("Layout PAC tidak cocok")
                val idx = src.readFully(indexOff, count * entrySize)
                val bb = idx.le()
                val out = ArrayList<Entry>(count)
                for (i in 0 until count) {
                    val o = i * entrySize
                    val name = IoUtil.cstr(idx, o, nameLen, cs)
                    val size = bb.getInt(o + nameLen).toLong() and 0xffffffffL
                    val off = bb.getInt(o + nameLen + 4).toLong() and 0xffffffffL
                    if (!IoUtil.saneName(name) || off + size > len) throw IOException("Entri PAC di luar arsip: $name")
                    out += Entry(name, off, size)
                }
                return out
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index PAC tidak valid")
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override val packOptions = listOf(
        OptionSpec(
            key = "pac_var", kind = OptKind.CHOICE,
            en = "PAC variant", id = "Varian PAC",
            choices = listOf(
                Choice("classic", "Classic Softpal (no magic)", "Softpal klasik (tanpa magic)"),
                Choice("amuse", "Amuse Craft (\"PAC \" magic)", "Amuse Craft (magic \"PAC \")"),
            ),
            default = "classic",
            hintEn = "Choose Classic if you are not sure.", hintId = "Pilih Klasik jika tidak mengetahuinya.",
        ),
        OptionSpec(
            key = "pac_name", kind = OptKind.CHOICE,
            en = "Name field size", id = "Ukuran kolom nama",
            choices = listOf(Choice("32", "32 bytes (0x20)", "32 byte (0x20)"), Choice("16", "16 bytes (0x10)", "16 byte (0x10)")),
            default = "32",
            showIf = "pac_var" to "classic",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        if (n == 0) throw IOException("Tidak ada file")
        val amuse = opts.str("pac_var", "classic") == "amuse"
        val nameLen = if (amuse) 0x20 else opts.int("pac_name", 32).let { if (it == 16) 0x10 else 0x20 }
        val indexOff = if (amuse) AMUSE_INDEX else CLASSIC_INDEX
        val entry = nameLen + 8
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= nameLen) throw IOException("Nama terlalu panjang (maks ${nameLen - 1} byte): ${it.path}")
            b
        }
        val headLen = indexOff + n.toLong() * entry
        var pos = headLen
        val head = ByteBuffer.allocate(headLen.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        if (amuse) { head.put("PAC ".toByteArray(Charsets.US_ASCII)); head.position(8) }
        head.putInt(n)
        for (i in 0 until n) {
            val o = (indexOff + i.toLong() * entry).toInt()
            head.position(o)
            head.put(names[i])
            head.position(o + nameLen)
            head.putInt(inputs[i].size.toInt()).putInt(pos.toInt())
            pos += inputs[i].size
        }
        if (pos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(head.array())
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                IoUtil.appendInput(raf, f, buf)
            }
            progress.update(n, n, "selesai")
        }
    }
}
