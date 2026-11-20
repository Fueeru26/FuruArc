package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.CRC32

/**
 * Majiro (.arc), magic "MajiroArcV1/2/3.000" (port of GARbro).
 * Header: magic[16], u32 count @0x10, u32 name table offset @0x14, u32 data offset @0x18.
 * Table @0x1C: v1 = (hash u32, offset u32) x (count+1, last entry = end of data); v2 = (hash u32, offset u32, size u32);
 * v3 = (hash u64, offset u32, size u32). NUL-terminated names (Shift-JIS) in sequence in the name table.
 * Write: v1 (names without folders, sorted by name CRC32, like the GARbro writer).
 */
object MajiroFormat : ArchiveFormat {
    override val id = "majiro"
    override val displayName = "ARC (Majiro)"
    override val extensions = listOf("arc")

    private val SIG = "MajiroArcV".toByteArray(Charsets.US_ASCII)

    override val packOptions = listOf(ENCODING_SPEC)
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun matches(head: ByteArray) =
        head.size >= 11 && SIG.indices.all { head[it] == SIG[it] }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x1C) throw IOException("File terlalu kecil")
        val h = src.readFully(0, 0x1C)
        val ver = h[0xA].toInt() - '0'.code
        if (ver < 1 || ver > 3 || String(h, 0xB, 4, Charsets.US_ASCII) != ".000") throw IOException("Versi Majiro tidak didukung")
        val bb = h.le()
        val count = bb.getInt(16)
        val namesOff = bb.getInt(20).toLong() and 0xffffffffL
        val dataOff = bb.getInt(24).toLong() and 0xffffffffL
        if (count <= 0 || count > 500_000 || dataOff <= namesOff || dataOff > len) throw IOException("Index Majiro tidak valid")
        val entrySize = 4 * (ver + 1)
        val tableSize = (count + (if (ver == 1) 1 else 0)).toLong() * entrySize
        if (tableSize + 0x1C != namesOff) throw IOException("Tabel Majiro tidak cocok")
        val table = src.readFully(0x1C, tableSize.toInt()).le()
        val names = src.readFully(namesOff, (dataOff - namesOff).toInt())
        val hashSize = if (ver < 3) 4 else 8
        val cs = IoUtil.cs(opts)
        val out = ArrayList<Entry>(count)
        var np = 0
        for (i in 0 until count) {
            var z = np
            while (z < names.size && names[z].toInt() != 0) z++
            if (z >= names.size) break
            val name = String(names, np, z - np, cs)
            np = z + 1
            val off = table.getInt(i * entrySize + hashSize).toLong() and 0xffffffffL
            val size = if (ver == 1) {
                val nx = table.getInt((i + 1) * entrySize + hashSize).toLong() and 0xffffffffL
                if (nx >= off) nx - off else 0L
            } else table.getInt(i * entrySize + hashSize + 4).toLong() and 0xffffffffL
            if (off < dataOff || off + size > len || !IoUtil.saneName(name)) throw IOException("Entri Majiro tidak valid: $name")
            out += Entry(name, off, size)
        }
        if (out.isEmpty()) throw IOException("Majiro tidak berisi file")
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        if (n == 0) throw IOException("Tidak ada file")
        val cs = IoUtil.cs(opts)
        class Item(val input: PackInput, val name: ByteArray, val hash: Long)
        val items = inputs.map {
            val nb = it.path.substringAfterLast('/').toByteArray(cs)
            val c = CRC32(); c.update(nb)
            Item(it, nb, c.value)
        }.sortedBy { it.hash }
        val namesOff = 0x1C + (n + 1) * 8
        var namesLen = 0
        for (it in items) namesLen += it.name.size + 1
        val dataOff = namesOff + namesLen
        var pos = dataOff.toLong()
        val table = ByteBuffer.allocate((n + 1) * 8).order(ByteOrder.LITTLE_ENDIAN)
        for (it in items) { table.putInt(it.hash.toInt()).putInt(pos.toInt()); pos += it.input.size }
        if (pos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        table.putInt(0).putInt(pos.toInt())
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write("MajiroArcV1.000\u0000".toByteArray(Charsets.US_ASCII))
            raf.write(ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN).putInt(n).putInt(namesOff).putInt(dataOff).array())
            raf.write(table.array())
            for (it in items) { raf.write(it.name); raf.write(0) }
            val buf = ByteArray(256 * 1024)
            items.forEachIndexed { i, it ->
                progress.update(i, n, it.input.path)
                IoUtil.appendInput(raf, it.input, buf)
            }
            progress.update(n, n, "selesai")
        }
    }
}
