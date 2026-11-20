package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * NeXAS PAC: "PAC" + 1 byte, u32 count, u32 variant (0 raw, 1 LZSS, 4/5 zlib, 7 zstd); 76-byte entries:
 * name[64] (Shift-JIS), u32 offset, u32 original size, u32 stored size (differs from original size = compressed).
 * Plain index at +12 (if the first entry's offset = 12 + count*76), or at the end of the archive: u32 length in the last 4 bytes,
 * contents XOR-ed with 0xFF then Huffman ([NexasCodec]). Read: variants 0 and 4/5; LZSS and zstd are not yet supported.
 * Written: variant 0 or 4, Huffman index (default) or plain. Not yet tested on real game archives.
 */
object NexasFormat : ArchiveFormat {
    override val id = "nexas"
    override val displayName = "NeXAS PAC"
    override val extensions = listOf("pac")

    private const val REC = 76
    private const val NAME_LEN = 64

    private class Meta(val variant: Int)

    private fun headOk(h: ByteArray): Boolean {
        if (h.size < 12) return false
        if (h[0].toInt() != 'P'.code || h[1].toInt() != 'A'.code || h[2].toInt() != 'C'.code) return false
        if (h[3].toInt() != 0 && h[3].toInt() != 'K'.code) return false
        val bb = h.le()
        val n = bb.getInt(4); val v = bb.getInt(8)
        return n in 1..2_000_000 && (v == 0 || v == 1 || v == 4 || v == 5 || v == 7)
    }

    override fun matches(head: ByteArray) = headOk(head)

    override val unpackOptions = listOf(ENCODING_SPEC)
    override val packOptions = listOf(
        OptionSpec(
            key = "nxcomp", kind = OptKind.CHOICE, en = "Compression", id = "Kompresi", default = "zlib",
            choices = listOf(Choice("zlib", "zlib (variant 4)", "zlib (varian 4)"), Choice("none", "None (variant 0)", "Tanpa kompresi (varian 0)")),
            hintEn = "Files that do not shrink are stored raw.", hintId = "File yang tidak mengecil disimpan mentah.",
        ),
        OptionSpec(
            key = "nxindex", kind = OptKind.CHOICE, en = "Index", id = "Indeks", default = "huff",
            choices = listOf(Choice("huff", "Huffman at end (like the game)", "Huffman di akhir (seperti game)"), Choice("plain", "Plain after header", "Polos setelah header")),
        ),
        ENCODING_SPEC,
    )

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 16) throw IOException("File terlalu kecil")
        val h = src.readFully(0, 12)
        if (!headOk(h)) throw IOException("Bukan arsip NeXAS PAC")
        val bb = h.le()
        val count = bb.getInt(4); val variant = bb.getInt(8)
        val idxSize = count.toLong() * REC
        val cs = IoUtil.cs(opts)
        var idx: ByteArray? = null
        if (12 + idxSize <= len) {
            val plain = src.readFully(12, idxSize.toInt())
            if (plain.le().getInt(NAME_LEN).toLong() == 12 + idxSize) idx = plain
        }
        if (idx == null) {
            val enc = src.readFully(len - 4, 4).le().getInt().toLong()
            if (enc <= 0 || enc > len - 16) throw IOException("Panjang indeks NeXAS tidak valid")
            val data = NexasCodec.xorFF(src.readFully(len - 4 - enc, enc.toInt()))
            idx = NexasCodec.huffDecode(data, 0, data.size, idxSize.toInt())
        }
        val ib = idx.le()
        val out = ArrayList<Entry>(count)
        for (i in 0 until count) {
            val p = i * REC
            val name = IoUtil.cstr(idx, p, NAME_LEN, cs).replace('\\', '/')
            val off = ib.getInt(p + NAME_LEN).toLong() and 0xFFFFFFFFL
            val size = ib.getInt(p + NAME_LEN + 4).toLong() and 0xFFFFFFFFL
            val packed = ib.getInt(p + NAME_LEN + 8).toLong() and 0xFFFFFFFFL
            if (!IoUtil.saneName(name) || off + packed > len) continue
            out += Entry(name, off, size, packedSize = packed, meta = Meta(variant))
        }
        if (out.isEmpty()) throw IOException("Indeks NeXAS kosong atau rusak")
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val variant = (entry.meta as? Meta)?.variant ?: 0
        if (variant == 0 || entry.packedSize == entry.size) {
            IoUtil.copyRange(src, entry.offset, entry.size, out); return
        }
        if (variant != 4 && variant != 5)
            throw IOException("Varian kompresi $variant (${if (variant == 1) "LZSS" else if (variant == 7) "zstd" else "?"}) belum didukung: ${entry.name}")
        if (entry.packedSize > Int.MAX_VALUE) throw IOException("Entri terlalu besar")
        val raw = IoUtil.inflate(src.readFully(entry.offset, entry.packedSize.toInt()))
        if (raw.size.toLong() != entry.size) throw IOException("Ukuran hasil dekompresi tidak cocok: ${entry.name}")
        out.write(raw)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val zlib = opts.str("nxcomp", "zlib") == "zlib"
        val huff = opts.str("nxindex", "huff") == "huff"
        val names = inputs.map {
            val b = it.path.replace('/', '\\').toByteArray(cs)
            if (b.size >= NAME_LEN) throw IOException("Nama terlalu panjang (maks. 63 byte): ${it.path}")
            b
        }
        val idx = ByteBuffer.allocate(n * REC).order(ByteOrder.LITTLE_ENDIAN)
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            val head = ByteBuffer.allocate(12).order(ByteOrder.LITTLE_ENDIAN)
            head.put('P'.code.toByte()).put('A'.code.toByte()).put('C'.code.toByte()).put(0)
            head.putInt(n).putInt(if (zlib) 4 else 0)
            raf.write(head.array())
            if (!huff) raf.write(ByteArray(n * REC))
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                val off = raf.filePointer
                var size: Long; var packed: Long
                if (zlib) {
                    val data = f.open().use { it.readBytes() }
                    val z = IoUtil.deflate(data)
                    size = data.size.toLong()
                    if (z.size < data.size) { raf.write(z); packed = z.size.toLong() } else { raf.write(data); packed = size }
                } else {
                    size = IoUtil.appendInput(raf, f, buf); packed = size
                    if (size != f.size) throw IOException("Ukuran file berubah saat dikemas: ${f.path}")
                }
                if (off + packed > 0xFFFFFFFFL) throw IOException("Arsip melebihi 4 GB")
                val p = i * REC
                idx.position(p); idx.put(names[i]); idx.position(p + NAME_LEN)
                idx.putInt(off.toInt()).putInt(size.toInt()).putInt(packed.toInt())
            }
            if (huff) {
                val enc = NexasCodec.xorFF(NexasCodec.huffEncode(idx.array()))
                raf.write(enc)
                raf.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(enc.size).array())
            } else {
                raf.seek(12); raf.write(idx.array())
            }
            progress.update(n, n, "selesai")
        }
    }
}
