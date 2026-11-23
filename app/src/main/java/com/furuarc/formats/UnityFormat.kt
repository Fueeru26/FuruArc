package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Unity AssetBundle (UnityFS). Container level only: lists the nodes (CAB-*, *.resS, ...) and extracts them.
 * Header (big endian): "UnityFS\0", u32 version, cstring unity version, cstring revision, i64 size,
 * u32 compressed/uncompressed blocks-info size, u32 flags (0x3F = compression of the blocks info, 0x40 = info
 * follows the header, 0x80 = info at the end of the file, 0x200 = padding before the data).
 * Blocks info: 16-byte hash, i32 blocks (u32 size, u32 packed size, u16 flags), i32 nodes (i64 offset, i64 size,
 * u32 flags, cstring path). Block compression: 0 none, 1 LZMA, 2/3 LZ4/LZ4HC.
 * Not supported: encrypted or custom-modified bundles, legacy UnityWeb/UnityRaw, and the serialized objects inside.
 */
object UnityFormat : ArchiveFormat {
    override val id = "unity"
    override val displayName = "Unity (UnityFS AssetBundle)"
    override val extensions = listOf("unity3d", "bundle", "assetbundle", "ab", "bin", "data")

    private val MAGIC = "UnityFS".toByteArray(Charsets.US_ASCII)
    private const val BLOCK = 1 shl 20

    private class Block(val uOff: Long, val cOff: Long, val cSize: Int, val uSize: Int, val comp: Int)

    private class Bundle(val blocks: List<Block>, val dataStart: Long) {
        var lastIdx = -1
        var lastData: ByteArray? = null
    }

    private class Meta(val bundle: Bundle)

    private val VER_SPEC = OptionSpec(
        key = "ufsver", kind = OptKind.CHOICE,
        en = "Bundle format version", id = "Versi format bundle",
        choices = listOf(
            Choice("7", "7 (Unity 2019.4 and newer)", "7 (Unity 2019.4 dan lebih baru)"),
            Choice("6", "6 (Unity 5.6 - 2019.3)", "6 (Unity 5.6 - 2019.3)"),
        ),
        default = "7",
    )
    private val UVER_SPEC = OptionSpec(
        key = "ufsunity", kind = OptKind.TEXT,
        en = "Unity version string", id = "String versi Unity",
        default = "2019.4.40f1",
        hintEn = "Use the Unity version of the game (shown in the original bundle's header).",
        hintId = "Isi dengan versi Unity game (terlihat di header bundle asli).",
    )

    override val packOptions = listOf(VER_SPEC, UVER_SPEC)

    override fun matches(head: ByteArray): Boolean {
        if (head.size < 8) return false
        for (i in MAGIC.indices) if (head[i] != MAGIC[i]) return false
        return head[7].toInt() == 0
    }

    private class Rd(val b: ByteArray) {
        var p = 0
        fun u8() = b[p++].toInt() and 0xFF
        fun u16(): Int { val v = ((b[p].toInt() and 0xFF) shl 8) or (b[p + 1].toInt() and 0xFF); p += 2; return v }
        fun u32(): Long {
            if (p + 4 > b.size) throw IOException("Header Unity terpotong")
            val v = ByteBuffer.wrap(b, p, 4).order(ByteOrder.BIG_ENDIAN).int.toLong() and 0xFFFFFFFFL
            p += 4; return v
        }
        fun i64(): Long {
            if (p + 8 > b.size) throw IOException("Data Unity terpotong")
            val v = ByteBuffer.wrap(b, p, 8).order(ByteOrder.BIG_ENDIAN).long; p += 8; return v
        }
        fun cstr(): String {
            val s = p
            while (p < b.size && b[p].toInt() != 0) p++
            if (p >= b.size) throw IOException("String Unity tidak berakhir")
            val r = String(b, s, p - s, Charsets.UTF_8); p++; return r
        }
        fun align(n: Int) { while (p % n != 0) p++ }
    }

    private fun unpack(comp: Int, data: ByteArray, off: Int, len: Int, uSize: Int): ByteArray = when (comp and 0x3F) {
        0 -> {
            if (len < uSize) throw IOException("Blok Unity tidak lengkap")
            data.copyOfRange(off, off + uSize)
        }
        1 -> Lzma.decode(data, off, len, uSize)
        2, 3 -> Lz4.decode(data, off, len, uSize)
        else -> throw IOException("Kompresi Unity ${comp and 0x3F} tidak dikenal")
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x30) throw IOException("File terlalu kecil")
        val head = src.readFully(0, minOf(len, 0x400L).toInt())
        if (!matches(head)) throw IOException("Bukan bundle UnityFS")
        val h = Rd(head)
        h.p = 8
        val version = h.u32().toInt()
        val unityVer = h.cstr()
        h.cstr()
        val size = h.i64()
        val cLen = h.u32()
        val uLen = h.u32()
        val flags = h.u32().toInt()
        if (version < 6 || version > 8) throw IOException("Versi UnityFS $version belum didukung")
        if (cLen <= 0 || uLen <= 0 || cLen > 256L * 1024 * 1024 || uLen > 512L * 1024 * 1024) throw IOException("Ukuran info blok Unity tidak valid")
        if (flags and 0x3F > 3) throw IOException("Kompresi info blok tidak dikenal")
        if (size > len + 16) throw IOException("File terpotong: ukuran header ${size} > ${len}")
        var pos = h.p.toLong()
        val major = unityVer.substringBefore('.').toIntOrNull() ?: 0
        val minor = unityVer.split('.').getOrNull(1)?.toIntOrNull() ?: 0
        if (version >= 7 || (major == 2019 && minor >= 4)) pos = (pos + 15) / 16 * 16
        val infoAtEnd = flags and 0x80 != 0
        val infoPos = if (infoAtEnd) len - cLen else pos
        if (infoPos < 0 || infoPos + cLen > len) throw IOException("Info blok Unity di luar file")
        val raw = src.readFully(infoPos, cLen.toInt())
        val info = unpack(flags, raw, 0, raw.size, uLen.toInt())
        var dataStart = if (infoAtEnd) pos else pos + cLen
        if (!infoAtEnd && flags and 0x200 != 0) dataStart = (dataStart + 15) / 16 * 16

        val r = Rd(info)
        r.p = 16
        val bc = r.u32().toInt()
        if (bc < 0 || bc > 4_000_000) throw IOException("Jumlah blok Unity tidak valid")
        val blocks = ArrayList<Block>(bc)
        var uo = 0L
        var co = 0L
        for (i in 0 until bc) {
            if (r.p + 10 > info.size) throw IOException("Info blok terpotong")
            val us = r.u32()
            val cs = r.u32()
            val bf = r.u16()
            if (us > Int.MAX_VALUE || cs > Int.MAX_VALUE) throw IOException("Blok Unity terlalu besar")
            blocks += Block(uo, co, cs.toInt(), us.toInt(), bf)
            uo += us; co += cs
        }
        if (dataStart + co > len) throw IOException("Data blok Unity melewati akhir file")
        val bundle = Bundle(blocks, dataStart)
        val nc = r.u32().toInt()
        if (nc < 0 || nc > 2_000_000) throw IOException("Jumlah node Unity tidak valid")
        val out = ArrayList<Entry>(nc)
        for (i in 0 until nc) {
            val off = r.i64()
            val sz = r.i64()
            r.u32()
            val name = r.cstr()
            if (off < 0 || sz < 0 || off + sz > uo) throw IOException("Node Unity di luar data: $name")
            if (!IoUtil.saneName(name)) continue
            out += Entry(name, off, sz, packedSize = sz, meta = Meta(bundle))
        }
        if (out.isEmpty()) throw IOException("Bundle Unity tidak berisi file")
        return out
    }

    private fun blockData(src: RandomSource, b: Bundle, i: Int): ByteArray {
        if (b.lastIdx == i) b.lastData?.let { return it }
        val bl = b.blocks[i]
        val raw = src.readFully(b.dataStart + bl.cOff, bl.cSize)
        val d = try {
            unpack(bl.comp, raw, 0, raw.size, bl.uSize)
        } catch (e: OutOfMemoryError) {
            throw IOException("Memori tidak cukup untuk membuka blok ${bl.uSize / 1048576} MB")
        }
        b.lastIdx = i; b.lastData = d
        return d
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val b = (entry.meta as? Meta)?.bundle ?: throw IOException("Entri Unity tidak valid")
        var cur = entry.offset
        var left = entry.size
        // find the first block (linear search; bundles have at most a few thousand blocks)
        var i = 0
        while (i < b.blocks.size && b.blocks[i].uOff + b.blocks[i].uSize <= cur) i++
        while (left > 0) {
            if (i >= b.blocks.size) throw IOException("Data Unity berakhir lebih awal: ${entry.name}")
            val bl = b.blocks[i]
            val d = blockData(src, b, i)
            val s = (cur - bl.uOff).toInt()
            val n = minOf(left, (bl.uSize - s).toLong()).toInt()
            out.write(d, s, n)
            cur += n; left -= n; i++
        }
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val ver = opts.str("ufsver", "7").toIntOrNull()?.coerceIn(6, 7) ?: 7
        val unity = opts.str("ufsunity", "2019.4.40f1")
        val n = inputs.size
        if (n == 0) throw IOException("Tidak ada file untuk dikemas")
        var total = 0L
        for (f in inputs) total += f.size
        val blockCount = ((total + BLOCK - 1) / BLOCK).toInt()
        // blocks info (stored uncompressed, follows the header)
        val bi = java.io.ByteArrayOutputStream()
        fun be32(v: Long) { bi.write((v shr 24).toInt()); bi.write((v shr 16).toInt()); bi.write((v shr 8).toInt()); bi.write(v.toInt()) }
        fun be64(v: Long) { be32(v ushr 32); be32(v and 0xFFFFFFFFL) }
        bi.write(ByteArray(16))
        be32(blockCount.toLong())
        var rem = total
        for (i in 0 until blockCount) {
            val s = minOf(rem, BLOCK.toLong()); rem -= s
            be32(s); be32(s); bi.write(0); bi.write(0)
        }
        be32(n.toLong())
        var off = 0L
        for (f in inputs) {
            be64(off); be64(f.size)
            val low = f.path.lowercase()
            val flag = if (low.endsWith(".ress") || low.endsWith(".resource")) 0L else 4L
            be32(flag)
            bi.write(f.path.toByteArray(Charsets.UTF_8)); bi.write(0)
            off += f.size
        }
        val infoBytes = bi.toByteArray()
        val hd = java.io.ByteArrayOutputStream()
        hd.write(MAGIC); hd.write(0)
        fun hBe32(v: Long) { hd.write((v shr 24).toInt()); hd.write((v shr 16).toInt()); hd.write((v shr 8).toInt()); hd.write(v.toInt()) }
        hBe32(ver.toLong())
        hd.write(unity.toByteArray(Charsets.UTF_8)); hd.write(0)
        hd.write("0.0.0".toByteArray(Charsets.US_ASCII)); hd.write(0)
        var hlen = hd.size() + 8 + 12
        // same rule as the reader (list): UnityFS 7+ and Unity 2019.4+ align the header to 16 bytes
        val major = unity.substringBefore('.').toIntOrNull() ?: 0
        val minor = unity.split('.').getOrNull(1)?.toIntOrNull() ?: 0
        if (ver >= 7 || (major == 2019 && minor >= 4)) hlen = (hlen + 15) / 16 * 16
        val fileSize = hlen + infoBytes.size + total
        hBe32(fileSize ushr 32); hBe32(fileSize and 0xFFFFFFFFL)
        hBe32(infoBytes.size.toLong()); hBe32(infoBytes.size.toLong()); hBe32(0x40)
        while (hd.size() < hlen) hd.write(0)
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(hd.toByteArray())
            raf.write(infoBytes)
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                val w = IoUtil.appendInput(raf, f, buf)
                if (w != f.size) throw IOException("Ukuran file berubah saat dikemas: ${f.path}")
            }
            progress.update(n, n, "selesai")
        }
    }
}
