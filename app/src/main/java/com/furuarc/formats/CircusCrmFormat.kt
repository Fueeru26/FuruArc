package com.furuarc.formats

import com.furuarc.core.*
import java.io.IOException
import java.io.OutputStream

/**
 * Circus CRM (.crm), magic "CRXB" (port of msg-tool).
 * Layout: "CRXB", u32, u32 count (+8), u32; entries start at 0x10 @ 0x20 bytes: offset u32, u32, name[0x18].
 * Size = next offset difference (sorted by offset); the last entry runs to the end of the file.
 */
object CircusCrmFormat : ArchiveFormat {
    override val id = "circuscrm"
    override val displayName = "CRM (Adv32 / Circus)"
    override val extensions = listOf("crm")

    override val unpackOptions = listOf(ENCODING_SPEC)
    override val packOptions = listOf(ENCODING_SPEC)

    override fun matches(head: ByteArray) =
        head.size >= 4 && String(head, 0, 4, Charsets.US_ASCII) == "CRXB"

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x20) throw IOException("File terlalu kecil")
        val count = src.readFully(8, 4).le().int
        if (count <= 0 || count > 500_000 || 0x10L + count * 0x20L > len) throw IOException("Jumlah entri CRM tidak valid")
        val idx = src.readFully(0x10, count * 0x20)
        val bb = idx.le()
        val cs = IoUtil.cs(opts)
        val map = java.util.TreeMap<Long, String>()
        for (i in 0 until count) {
            val o = i * 0x20
            val off = bb.getInt(o).toLong() and 0xffffffffL
            val name = IoUtil.cstr(idx, o + 8, 0x18, cs)
            if (off > len || !IoUtil.saneName(name)) throw IOException("Index CRM tidak valid")
            map[off] = name
        }
        val keys = map.keys.toList()
        return keys.mapIndexed { i, off ->
            val end = if (i + 1 < keys.size) keys[i + 1] else len
            Entry(map[off]!!, off, end - off)
        }
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun pack(inputs: List<PackInput>, target: java.io.File, opts: Options, progress: Progress) {
        val n = inputs.size
        if (n < 1) throw IOException("Tidak ada file")
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size >= 0x18) throw IOException("Nama terlalu panjang (maks 23 byte): ${it.path}")
            b
        }
        var pos = 0x10L + n.toLong() * 0x20
        val head = java.nio.ByteBuffer.allocate(pos.toInt()).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        head.put("CRXB".toByteArray(Charsets.US_ASCII)).putInt(1).putInt(n).putInt(0x08000200)
        for (i in 0 until n) {
            val o = 0x10 + i * 0x20
            head.position(o)
            head.putInt(pos.toInt()).putInt(0x0E7A0000 + i * 128)
            head.put(names[i])
            pos += inputs[i].size
        }
        if (pos > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        java.io.RandomAccessFile(target, "rw").use { raf ->
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
