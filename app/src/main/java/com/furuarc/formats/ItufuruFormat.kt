package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Yaneurao Itufuru (.scd), magic "SCR\0" (port from msg-tool).
 * Header 0x10: "SCR\0", u32 count, u32 first data offset (= 0x10 + count*16), u32 reserved.
 * All bytes after the header (index AND file contents) are XOR-ed with 0xA5. 16-byte entry: name[12], offset u32 (relative to data).
 * Size = difference to the next offset (last entry: until end of file).
 */
object ItufuruFormat : ArchiveFormat {
    override val id = "itufuru"
    override val displayName = "SCD (Yaneurao Itufuru)"
    override val extensions = listOf("scd")

    private val MAGIC = byteArrayOf('S'.code.toByte(), 'C'.code.toByte(), 'R'.code.toByte(), 0)
    private const val KEY = 0xA5
    private val KEYB = byteArrayOf(KEY.toByte())

    override val packOptions = listOf(ENCODING_SPEC)
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun matches(head: ByteArray) =
        head.size >= 4 && MAGIC.indices.all { head[it] == MAGIC[it] }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x10) throw IOException("File terlalu kecil")
        val h = src.readFully(0, 0x10).le()
        val count = h.getInt(4)
        val first = h.getInt(8).toLong() and 0xffffffffL
        if (count < 0 || count > 500_000 || first < 0x10L + count * 16L || first > len) throw IOException("Index Itufuru tidak valid")
        val idx = src.readFully(0x10, count * 16)
        for (i in idx.indices) idx[i] = (idx[i].toInt() xor KEY).toByte()
        val bb = idx.le()
        val cs = IoUtil.cs(opts)
        val names = ArrayList<String>(count)
        val offs = LongArray(count)
        for (i in 0 until count) {
            names += IoUtil.cstr(idx, i * 16, 12, cs).replace('\\', '/')
            offs[i] = bb.getInt(i * 16 + 12).toLong() and 0xffffffffL
        }
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val next = if (i + 1 < count) offs[i + 1] else len - first
            val size = next - offs[i]
            if (size < 0 || first + offs[i] + size > len || !IoUtil.saneName(names[i])) throw IOException("Entri Itufuru tidak valid")
            out += Entry(names[i], first + offs[i], size)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out, KEYB)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            if (b.size > 12) throw IOException("Nama terlalu panjang (maks 12 byte): ${it.path}")
            b
        }
        val first = 0x10L + n * 16L
        var total = 0L
        val idx = ByteBuffer.allocate(n * 16).order(ByteOrder.LITTLE_ENDIAN)
        for (i in 0 until n) {
            idx.position(i * 16)
            idx.put(names[i])
            idx.position(i * 16 + 12)
            idx.putInt(total.toInt())
            total += inputs[i].size
        }
        if (first + total > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
        val ib = idx.array()
        for (i in ib.indices) ib[i] = (ib[i].toInt() xor KEY).toByte()
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(MAGIC)
            raf.write(ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN).putInt(n).putInt(first.toInt()).putInt(0).array())
            raf.write(ib)
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                f.open().use { ins ->
                    while (true) {
                        val r = ins.read(buf)
                        if (r < 0) break
                        for (k in 0 until r) buf[k] = (buf[k].toInt() xor KEY).toByte()
                        raf.write(buf, 0, r)
                    }
                }
            }
            progress.update(n, n, "selesai")
        }
    }
}
