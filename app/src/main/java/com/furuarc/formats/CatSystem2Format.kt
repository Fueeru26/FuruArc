package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * CatSystem2 (.int), magic "KIF\0".
 * Layout: "KIF\0", u32 count; entries: name[0x40 or 0x20] (Shift-JIS, NUL-terminated), u32 offset (absolute), u32 size
 * (name size tried as 0x40 then 0x20, like msg-tool).
 * Only PLAIN indexes are supported. Many games encrypt the index with a per-game key;
 * if the names make no sense, listing files fails with a clear message.
 * File contents (.cct, .anm, .fes, etc.) are extracted as is.
 */
object CatSystem2Format : ArchiveFormat {
    override val id = "catsystem2"
    override val displayName = "INT (CatSystem2)"
    override val extensions = listOf("int")

    private val NAME_SIZES = intArrayOf(0x40, 0x20)

    override fun matches(head: ByteArray) =
        head.size >= 4 && head[0] == 'K'.code.toByte() && head[1] == 'I'.code.toByte() &&
            head[2] == 'F'.code.toByte() && head[3].toInt() == 0

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 8) throw IOException("File terlalu kecil")
        val count = src.readFully(4, 4).le().int
        if (count <= 0 || count > 500_000) throw IOException("Jumlah entri tidak valid")
        if (len >= 8 + 12 && IoUtil.cstr(src.readFully(8, 12), 0, 12, Charsets.US_ASCII) == "__key__.dat") {
            val pw = opts.extra["cs2pass"]?.trim().orEmpty()
            if (pw.isEmpty())
                throw IOException("Arsip INT terenkripsi (ada __key__.dat): isi opsi Password dengan password game (bisa dicari di Pencari Kunci dari file .exe game)")
            val idx = CatSystemCrypt.readIndex(src, count, pw, IoUtil.cs(opts))
                ?: throw IOException("Password salah atau indeks INT tidak cocok")
            return idx.entries.map { Entry(it.first, it.second, it.third, meta = "enc") }
        }
        var last: Exception? = null
        for (nameSize in NAME_SIZES) {
            try {
                return parse(src, count, nameSize, IoUtil.cs(opts))
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index INT tidak terbaca (kemungkinan terenkripsi, butuh kunci game)")
    }

    private fun parse(src: RandomSource, count: Int, nameSize: Int, cs: java.nio.charset.Charset): List<Entry> {
        val len = src.length
        val entry = nameSize + 8
        val dataStart = 8L + count.toLong() * entry
        if (dataStart > len) throw IOException("Index di luar arsip")
        val idx = src.readFully(8, count * entry)
        val bb = idx.le()
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val o = i * entry
            val name = IoUtil.cstr(idx, o, nameSize, cs)
            val off = bb.getInt(o + nameSize).toLong() and 0xffffffffL
            val size = bb.getInt(o + nameSize + 4).toLong() and 0xffffffffL
            if (!IoUtil.saneName(name) || off < dataStart || off + size > len)
                throw IOException("Index INT tidak terbaca (kemungkinan terenkripsi, butuh kunci game)")
            out += Entry(name, off, size)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        if (entry.meta == "enc") {
            val count = src.readFully(4, 4).le().int
            val idx = CatSystemCrypt.readIndex(src, count, opts.extra["cs2pass"]?.trim().orEmpty(), IoUtil.cs(opts))
                ?: throw IOException("Password salah")
            val buf = src.readFully(entry.offset, entry.size.toInt())
            idx.cipher.decryptBlocks(buf, 0, buf.size, true)
            out.write(buf)
            return
        }
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    private val PASS_SPEC = OptionSpec(
        key = "cs2pass", kind = OptKind.TEXT,
        en = "Password (encrypted INT)", id = "Password (INT terenkripsi)",
        default = "",
        hintEn = "Only for archives with __key__.dat. Use Key Finder on the game's .exe.",
        hintId = "Hanya untuk arsip ber-__key__.dat. Cari lewat Pencari Kunci pada .exe game.",
    )

    override val packOptions = listOf(
        OptionSpec(
            key = "int_name", kind = OptKind.CHOICE,
            en = "Name field size", id = "Ukuran kolom nama",
            choices = listOf(Choice("64", "64 bytes (0x40, most games)", "64 byte (0x40, kebanyakan game)"), Choice("32", "32 bytes (0x20, old games)", "32 byte (0x20, game lama)")),
            default = "64",
            hintEn = "Choose 64 if you are not sure.", hintId = "Pilih 64 jika tidak mengetahuinya.",
        ),
        ENCODING_SPEC,
    )
    override val unpackOptions get() = listOf(PASS_SPEC, ENCODING_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val NAME = if (opts.int("int_name", 64) == 32) 0x20 else 0x40
        val ENTRY = NAME + 8
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= NAME) throw IOException("Nama terlalu panjang (maks ${NAME - 1} byte): ${it.path}")
            b
        }
        var pos = 8L + n.toLong() * ENTRY
        val head = ByteBuffer.allocate(pos.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        head.put("KIF".toByteArray(Charsets.US_ASCII)).put(0.toByte()).putInt(n)
        for (i in 0 until n) {
            val o = 8 + i * ENTRY
            head.position(o)
            head.put(names[i])
            head.position(o + NAME)
            head.putInt(pos.toInt()).putInt(inputs[i].size.toInt())
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
