package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Random

/**
 * Escu:de (.bin), magic "ESC-ARC2" (port from msg-tool).
 * Layout: "ESC-ARC2", u32 key @8; starting @0xC all u32 are XORed with a key stream (key ^= 0x65AC9365; key ^= ((key>>1 ^ key)>>3) ^ ((key<<1 ^ key)<<3)):
 * count, name table length, then 12-byte entries (relative name offset, absolute data offset, length).
 * Names (NUL-terminated) are at (name offset + count*12 + 0x14). Contents starting with "acp" = LZW (MSB-first), decompressed on extract.
 * Pack: each file is wrapped in "acp" with 9-bit literal tokens (no real compression, still valid for the game decoder).
 */
object EscudeFormat : ArchiveFormat {
    override val id = "escude"
    override val displayName = "BIN (Escu:de)"
    override val extensions = listOf("bin")

    private val MAGIC = "ESC-ARC2".toByteArray(Charsets.US_ASCII)

    private class Meta(val lzw: Boolean)

    override val packOptions = listOf(ENCODING_SPEC)
    override val unpackOptions = listOf(ENCODING_SPEC)

    override fun matches(head: ByteArray) =
        head.size >= 8 && MAGIC.indices.all { head[it] == MAGIC[it] }

    private class KeyStream(var key: Int) {
        fun next(): Int {
            key = key xor 0x65AC9365
            key = key xor ((((key ushr 1) xor key) ushr 3) xor (((key shl 1) xor key) shl 3))
            return key
        }
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x14) throw IOException("File terlalu kecil")
        val head = src.readFully(0, 0x10).le()
        val ks = KeyStream(head.getInt(8))
        val count = head.getInt(0xC) xor ks.next()
        if (count <= 0 || count > 500_000 || 0x14L + count.toLong() * 12 > len) throw IOException("Index Escu:de tidak valid")
        val tbl = src.readFully(0x10, 4 + count * 12).le()
        ks.next() // name table length (2nd key value), unused
        val cs = IoUtil.cs(opts)
        val out = ArrayList<Entry>(count)
        val namesBase = count.toLong() * 12 + 0x14
        for (i in 0 until count) {
            val nameOff = (tbl.getInt(4 + i * 12) xor ks.next()).toLong() and 0xffffffffL
            val dataOff = (tbl.getInt(8 + i * 12) xor ks.next()).toLong() and 0xffffffffL
            val length = (tbl.getInt(12 + i * 12) xor ks.next()).toLong() and 0xffffffffL
            val namePos = nameOff + namesBase
            if (namePos >= len || dataOff + length > len) throw IOException("Entri Escu:de di luar arsip")
            val raw = src.readFully(namePos, minOf(260L, len - namePos).toInt())
            val name = IoUtil.cstr(raw, 0, raw.size, cs).replace('\\', '/')
            if (!IoUtil.saneName(name)) throw IOException("Nama Escu:de tidak valid")
            var size = length
            var lzw = false
            if (length >= 8) {
                val h = src.readFully(dataOff, 8)
                if (h[0] == 'a'.code.toByte() && h[1] == 'c'.code.toByte() && h[2] == 'p'.code.toByte()) {
                    lzw = true
                    size = ((h[4].toLong() and 0xff) shl 24) or ((h[5].toLong() and 0xff) shl 16) or
                        ((h[6].toLong() and 0xff) shl 8) or (h[7].toLong() and 0xff)
                }
            }
            out += Entry(name, dataOff, size, length, Meta(lzw))
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val lzw = (entry.meta as? Meta)?.lzw ?: false
        if (!lzw) {
            IoUtil.copyRange(src, entry.offset, entry.packedSize, out)
            return
        }
        if (entry.packedSize > Int.MAX_VALUE || entry.size > Int.MAX_VALUE) throw IOException("File terlalu besar")
        out.write(lzwDecode(src.readFully(entry.offset, entry.packedSize.toInt())))
    }

    /** Escu:de LZW decoder: 9..24-bit tokens MSB-first; 0x100 = end, 0x101 = width+1, 0x102 = reset. */
    fun lzwDecode(data: ByteArray): ByteArray {
        if (data.size < 8) throw IOException("Data LZW terlalu pendek")
        val size = ((data[4].toLong() and 0xff) shl 24 or ((data[5].toLong() and 0xff) shl 16) or
            ((data[6].toLong() and 0xff) shl 8) or (data[7].toLong() and 0xff)).toInt()
        if (size < 0) throw IOException("Ukuran LZW tidak valid")
        val out = ByteArray(size)
        var bitPos = 8L * 8
        fun bits(n: Int): Int {
            var v = 0
            repeat(n) {
                val bi = (bitPos shr 3).toInt()
                if (bi >= data.size) throw IOException("Data LZW habis")
                v = (v shl 1) or ((data[bi].toInt() shr (7 - (bitPos and 7).toInt())) and 1)
                bitPos++
            }
            return v
        }
        val dict = IntArray(0x8900 + 2)
        var width = 9
        var dpos = 0
        var dst = 0
        while (dst < size) {
            var token = bits(width)
            when {
                token == 0x100 -> break
                token == 0x101 -> { width++; if (width > 24) throw IOException("Lebar token LZW melebihi batas") }
                token == 0x102 -> { width = 9; dpos = 0 }
                else -> {
                    if (dpos >= 0x8900) throw IOException("Kamus LZW penuh")
                    dict[dpos] = dst
                    dpos++
                    if (token < 0x100) {
                        out[dst++] = token.toByte()
                    } else {
                        token -= 0x103
                        if (token < 0 || token + 1 >= dpos) throw IOException("Token LZW di luar batas: $token")
                        val s = dict[token]
                        val count = minOf(size - dst, dict[token + 1] - s + 1)
                        if (count < 0) throw IOException("Token LZW rusak")
                        for (i in 0 until count) out[dst + i] = out[s + i]
                        dst += count
                    }
                }
            }
        }
        return out
    }

    /** 9-bit literal token writer (MSB-first), written incrementally to [raf]. */
    private class BitWriter(val raf: RandomAccessFile) {
        private val buf = ByteArray(64 * 1024)
        private var n = 0
        private var acc = 0L
        private var nb = 0
        var written = 0L
        fun put(v: Int, width: Int) {
            acc = (acc shl width) or (v.toLong() and ((1L shl width) - 1))
            nb += width
            while (nb >= 8) {
                buf[n++] = ((acc shr (nb - 8)) and 0xff).toByte()
                nb -= 8
                if (n == buf.size) flushBuf()
            }
            acc = acc and ((1L shl nb) - 1)
        }
        private fun flushBuf() { raf.write(buf, 0, n); written += n; n = 0 }
        fun finish() {
            if (nb > 0) { buf[n++] = ((acc shl (8 - nb)) and 0xff).toByte(); nb = 0; acc = 0 }
            flushBuf()
        }
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        if (n == 0) throw IOException("Tidak ada file")
        val cs = IoUtil.cs(opts)
        val names = inputs.map { it.path.toByteArray(cs) }
        val headerLen = 0xC + 0xC * n
        var namesTotal = 0
        val nameOffs = IntArray(n)
        for (i in 0 until n) { nameOffs[i] = namesTotal; namesTotal += names[i].size + 1 }
        val dataOffs = LongArray(n)
        val lens = LongArray(n)
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(MAGIC)
            raf.write(ByteArray(headerLen))
            for (i in 0 until n) { raf.write(names[i]); raf.write(0) }
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                dataOffs[i] = raf.filePointer
                raf.write(byteArrayOf('a'.code.toByte(), 'c'.code.toByte(), 'p'.code.toByte(), 0))
                raf.write(ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(f.size.toInt()).array())
                val bw = BitWriter(raf)
                var idx = 0L
                val buf = ByteArray(256 * 1024)
                f.open().use { ins ->
                    while (true) {
                        val r = ins.read(buf)
                        if (r < 0) break
                        for (k in 0 until r) {
                            if (idx > 0 && idx % 0x4000 == 0L) bw.put(0x102, 9)
                            bw.put(buf[k].toInt() and 0xff, 9)
                            idx++
                        }
                    }
                }
                bw.put(0x100, 9)
                bw.finish()
                lens[i] = raf.filePointer - dataOffs[i]
            }
            if (raf.filePointer > 0xffffffffL) throw IOException("Ukuran total melebihi 4 GB")
            // encrypted header
            val key0 = Random().nextInt()
            val ks = KeyStream(key0)
            val hb = ByteBuffer.allocate(4 + 8 + 12 * n).order(ByteOrder.LITTLE_ENDIAN)
            hb.putInt(key0)
            hb.putInt(n xor ks.next())
            hb.putInt(namesTotal xor ks.next())
            for (i in 0 until n) {
                hb.putInt(nameOffs[i] xor ks.next())
                hb.putInt(dataOffs[i].toInt() xor ks.next())
                hb.putInt(lens[i].toInt() xor ks.next())
            }
            raf.seek(8)
            raf.write(hb.array())
            progress.update(n, n, "selesai")
        }
    }
}
