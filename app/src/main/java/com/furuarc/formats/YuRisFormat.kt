package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile

/**
 * Yu-Ris (.ypf), magic "YPF\0". Follows msg-tool (versions 234..500).
 * Header 0x20: "YPF\0", u32 version, u32 count, u32 index length.
 * Entry: u32 name hash, u8 name length (encoded: table[x xor 0xFF]), name (NOT then XOR: v290=0x40, v500=0x36, otherwise 0),
 * u8 type, u8 packed, u32 original size, u32 compressed size, offset (u32; u64 if version >= 480), u32 data hash (if version >= 473).
 * The name length table differs for versions < 500 and >= 500. Packed file = zlib.
 * Pack: versions 480..499 (default 494, option ypf_ver) and 500; name & data hash = MurmurHash2 (seed 0), type by extension,
 * .ybn compressed with zlib, others stored plain. YPF attached to an .exe is not supported yet.
 */
object YuRisFormat : ArchiveFormat {
    override val id = "yuris"
    override val displayName = "YPF (Yu-Ris)"
    override val extensions = listOf("ypf")

    // pairs that swap with each other in the name length table (identity for other values)
    private val SWAP_DEFAULT = intArrayOf(
        3, 72, 6, 53, 9, 11, 12, 16, 13, 19, 17, 25, 21, 27, 28, 30, 32, 35, 38, 41, 44, 47, 46, 50,
    )
    private val SWAP_V500 = intArrayOf(
        3, 10, 6, 53, 9, 11, 12, 16, 13, 19, 17, 24, 20, 46, 21, 27, 28, 30, 32, 35, 38, 41, 44, 47,
    )

    private fun table(swaps: IntArray): IntArray {
        val t = IntArray(256) { it }
        var i = 0
        while (i < swaps.size) { t[swaps[i]] = swaps[i + 1]; t[swaps[i + 1]] = swaps[i]; i += 2 }
        return t
    }

    private val T_DEFAULT = table(SWAP_DEFAULT)
    private val T_V500 = table(SWAP_V500)

    private class Meta(val packed: Boolean)

    override fun matches(head: ByteArray) =
        head.size >= 4 && head[0] == 'Y'.code.toByte() && head[1] == 'P'.code.toByte() &&
            head[2] == 'F'.code.toByte() && head[3].toInt() == 0

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x20) throw IOException("File terlalu kecil")
        val h = src.readFully(0, 0x20).le()
        val version = h.getInt(4)
        val count = h.getInt(8)
        var idxLen = h.getInt(12).toLong() and 0xffffffffL
        if (version >= 300 && idxLen > 0x20 && idxLen <= len) idxLen -= 0x20 // v>=300: this value includes the 0x20 header
        if (count <= 0 || count > 500_000 || idxLen <= 0 || 0x20 + idxLen > len) throw IOException("Header YPF tidak valid")
        val idx = src.readFully(0x20, idxLen.toInt())
        val tbl = if (version < 500) T_DEFAULT else T_V500
        // width of the entry tail (after the name) by version; unknown version: try them all
        val tail = when {
            version >= 480 -> 22
            version >= 473 -> 18
            else -> 14
        }
        val xorByVersion = when (version) { 290 -> 0x40; 500 -> 0x36; else -> 0 }
        var last: Exception? = null
        val tries = if (version in 234..500) listOf(tail) else listOf(22, 18, 14)
        for (t in tries) {
            try {
                return parse(idx, count, t, tbl, xorByVersion, IoUtil.cs(opts))
            } catch (e: Exception) {
                last = e
            }
        }
        throw last ?: IOException("Index YPF tidak valid")
    }

    private fun parse(idx: ByteArray, count: Int, tail: Int, tbl: IntArray, xorKey: Int, cs: java.nio.charset.Charset): List<Entry> {
        val bb = idx.le()
        val out = ArrayList<Entry>(count)
        var p = 0
        for (i in 0 until count) {
            if (p + 5 > idx.size) throw IOException("Index terpotong")
            val l = tbl[(idx[p + 4].toInt() and 0xff) xor 0xff]
            val nameAt = p + 5
            if (nameAt + l + tail > idx.size) throw IOException("Index terpotong")
            val nameBytes = ByteArray(l) { ((idx[nameAt + it].toInt() and 0xff).inv() xor xorKey).toByte() }
            val name = String(nameBytes, cs).replace('\\', '/')
            if (!IoUtil.saneName(name)) throw IOException("Nama YPF tidak terbaca")
            val q = nameAt + l
            val packed = (idx[q + 1].toInt() and 0xff) != 0
            val size = bb.getInt(q + 2).toLong() and 0xffffffffL
            val psize = bb.getInt(q + 6).toLong() and 0xffffffffL
            val off = if (tail >= 22) bb.getLong(q + 10) else bb.getInt(q + 10).toLong() and 0xffffffffL
            out += Entry(name, off, size, if (packed) psize else size, Meta(packed))
            p = q + tail
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val m = entry.meta as? Meta
        if (m != null && m.packed) {
            out.write(IoUtil.inflate(src.readFully(entry.offset, entry.packedSize.toInt())))
        } else {
            IoUtil.copyRange(src, entry.offset, entry.size, out)
        }
    }

    private fun murmur2(d: ByteArray, off: Int = 0, len: Int = d.size): Int {
        val m = 0x5bd1e995
        var h = len
        var i = off
        var n = len
        while (n >= 4) {
            var k = (d[i].toInt() and 0xff) or ((d[i + 1].toInt() and 0xff) shl 8) or
                ((d[i + 2].toInt() and 0xff) shl 16) or ((d[i + 3].toInt() and 0xff) shl 24)
            k *= m; k = k xor (k ushr 24); k *= m
            h *= m; h = h xor k
            i += 4; n -= 4
        }
        if (n == 3) h = h xor ((d[i + 2].toInt() and 0xff) shl 16)
        if (n >= 2) h = h xor ((d[i + 1].toInt() and 0xff) shl 8)
        if (n >= 1) { h = h xor (d[i].toInt() and 0xff); h *= m }
        h = h xor (h ushr 13); h *= m; h = h xor (h ushr 15)
        return h
    }

    private fun typeOf(name: String): Int = when (name.substringAfterLast('.', "").lowercase()) {
        "ybn" -> 0; "bmp" -> 1; "png" -> 2; "jpg", "jpeg" -> 3; "gif" -> 4
        "wav" -> 5; "ogg" -> 6; "psd" -> 7; else -> 0
    }

    private val VER_SPEC = OptionSpec(
        key = "ypf_ver", kind = OptKind.TEXT,
        en = "YPF version (480–500)", id = "Versi YPF (480–500)",
        default = "494",
    )

    override val packOptions get() = listOf(VER_SPEC)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val version = opts.int("ypf_ver", 494)
        if (version !in 480..500) throw IOException("Versi YPF yang didukung untuk pack: 480–500")
        val tbl = if (version < 500) T_DEFAULT else T_V500
        val xk = if (version == 500) 0x36 else 0
        val cs = IoUtil.cs(opts)
        val n = inputs.size
        val names = inputs.map {
            val b = it.path.replace('/', '\\').toByteArray(cs)
            if (b.isEmpty() || b.size > 255) throw IOException("Nama tidak valid: ${it.path}")
            b
        }
        var idxSize = 0
        for (b in names) idxSize += 5 + b.size + 22
        val dataStart = 0x20L + idxSize
        val idx = java.nio.ByteBuffer.allocate(idxSize).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        var pos = dataStart
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.seek(dataStart)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                val raw = f.open().use { it.readBytes() }
                val type = typeOf(f.path)
                val packed = type == 0 && opts.compress
                val stored = if (packed) IoUtil.deflate(raw) else raw
                val nb = names[i]
                idx.putInt(murmur2(nb))
                idx.put((tbl[nb.size] xor 0xff).toByte())
                for (b in nb) idx.put(((b.toInt() and 0xff) xor xk).inv().toByte())
                idx.put(type.toByte()).put((if (packed) 1 else 0).toByte())
                idx.putInt(raw.size).putInt(stored.size).putLong(pos).putInt(murmur2(stored))
                raf.write(stored)
                pos += stored.size
            }
            val head = java.nio.ByteBuffer.allocate(0x20).order(java.nio.ByteOrder.LITTLE_ENDIAN)
            head.put("YPF".toByteArray(Charsets.US_ASCII)).put(0.toByte())
            head.putInt(version).putInt(n).putInt(dataStart.toInt())
            raf.seek(0)
            raf.write(head.array())
            raf.write(idx.array())
            progress.update(n, n, "selesai")
        }
    }
}
