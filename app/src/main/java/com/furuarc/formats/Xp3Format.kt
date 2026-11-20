package com.furuarc.formats

import com.furuarc.core.*
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.Adler32
import java.util.zip.InflaterInputStream

/**
 * KiriKiri XP3. Reads v1 and v2. There is NO per-game cipher yet:
 * only an optional 1-byte XOR as a starting point (see [Options.xorKey]).
 */
object Xp3Format : ArchiveFormat {
    override val id = "xp3"
    override val displayName = "XP3 (KiriKiri)"
    override val extensions = listOf("xp3")

    private val MAGIC = byteArrayOf(
        0x58, 0x50, 0x33, 0x0d, 0x0a, 0x20, 0x0a, 0x1a, 0x8b.toByte(), 0x67, 0x01,
    )
    private const val CHUNK = 8 * 1024 * 1024

    private fun tag(s: String) = ByteBuffer.wrap(s.toByteArray(Charsets.US_ASCII)).order(ByteOrder.LITTLE_ENDIAN).int
    private val T_FILE = tag("File")
    private val T_INFO = tag("info")
    private val T_SEGM = tag("segm")
    private val T_ADLR = tag("adlr")

    private class XpMeta(val segs: List<Seg>, val adler: Long?)

    private class Seg(val flags: Int, val offset: Long, val origSize: Long, val packedSize: Long)

    override fun matches(head: ByteArray) =
        head.size >= MAGIC.size && MAGIC.indices.all { head[it] == MAGIC[it] }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        var idxOff = src.readFully(11, 8).le().long
        if (idxOff < 0x13 || idxOff >= src.length) throw IOException("Offset index XP3 tidak valid")
        // XP3 v2: at that offset there is a 0x80 marker, the actual index offset is at +9
        if ((src.readFully(idxOff, 1)[0].toInt() and 0xff) == 0x80) idxOff = src.readFully(idxOff + 9, 8).le().long
        if (idxOff < 0x13 || idxOff >= src.length) throw IOException("Offset index XP3 tidak valid")

        val flag = src.readFully(idxOff, 1)[0].toInt() and 0xff
        val index: ByteArray = when (flag and 7) {
            0 -> {
                val sz = src.readFully(idxOff + 1, 8).le().long
                src.readFully(idxOff + 9, sz.toInt())
            }
            1 -> {
                val h = src.readFully(idxOff + 1, 16).le()
                val packed = h.long
                IoUtil.inflate(src.readFully(idxOff + 17, packed.toInt()))
            }
            else -> throw IOException("Flag index XP3 tidak didukung: $flag")
        }

        val bb = index.le()
        val out = ArrayList<Entry>()
        while (bb.remaining() >= 12) {
            val t = bb.int
            val size = bb.long
            val end = bb.position() + size.toInt()
            if (t == T_FILE) {
                var name = ""
                var orig = 0L
                var packed = 0L
                var adler: Long? = null
                val segs = ArrayList<Seg>()
                while (bb.position() + 12 <= end) {
                    val st = bb.int
                    val ss = bb.long
                    val se = bb.position() + ss.toInt()
                    if (st == T_INFO) {
                        bb.int // flags
                        orig = bb.long
                        packed = bb.long
                        val n = bb.short.toInt() and 0xffff
                        val chars = ByteArray(n * 2)
                        bb.get(chars)
                        name = String(chars, Charsets.UTF_16LE)
                    } else if (st == T_ADLR) {
                        adler = bb.int.toLong() and 0xffffffffL
                    } else if (st == T_SEGM) {
                        repeat((ss / 28).toInt()) {
                            segs += Seg(bb.int, bb.long, bb.long, bb.long)
                        }
                    }
                    bb.position(se)
                }
                out += Entry(name.replace('\\', '/'), segs.firstOrNull()?.offset ?: 0L, orig, packed, XpMeta(segs, adler))
            }
            bb.position(end)
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        @Suppress("UNCHECKED_CAST")
        val segs = (entry.meta as XpMeta).segs
        val xor = opts.xorKey
        for (sg in segs) {
            if ((sg.flags and 7) == 1) {
                if (sg.packedSize > Int.MAX_VALUE) throw IOException("Segmen XP3 terlalu besar")
                val data = IoUtil.inflate(src.readFully(sg.offset, sg.packedSize.toInt()))
                if (xor != null) for (i in data.indices) data[i] = (data[i].toInt() xor xor).toByte()
                out.write(data)
            } else {
                // no compression: copy incrementally (safe for segments > 2 GB)
                IoUtil.copyRange(src, sg.offset, sg.packedSize, out, xor?.let { byteArrayOf(it.toByte()) })
            }
        }
    }

    // ---------- automatic XOR key detection ----------

    private fun magic(vararg v: Int) = ByteArray(v.size) { v[it].toByte() }

    /** Known leading bytes of files; used to guess the 1-byte XOR key. */
    private val MAGICS: Map<String, ByteArray> = mapOf(
        "png" to magic(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A),
        "jpg" to magic(0xFF, 0xD8, 0xFF),
        "jpeg" to magic(0xFF, 0xD8, 0xFF),
        "ogg" to magic(0x4F, 0x67, 0x67, 0x53),
        "bmp" to magic(0x42, 0x4D),
        "gif" to magic(0x47, 0x49, 0x46, 0x38),
        "wav" to magic(0x52, 0x49, 0x46, 0x46),
        "webp" to magic(0x52, 0x49, 0x46, 0x46),
    )

    /** Read the first [n] bytes of a segment (after decompression, before XOR). null on failure. */
    private fun readHead(src: RandomSource, seg: Seg, n: Int): ByteArray? {
        try {
            if ((seg.flags and 7) == 1) {
                val raw = src.readFully(seg.offset, minOf(seg.packedSize, 65536L).toInt())
                val ins = InflaterInputStream(ByteArrayInputStream(raw))
                val out = ByteArray(n)
                var r = 0
                while (r < n) {
                    val c = ins.read(out, r, n - r)
                    if (c < 0) return null
                    r += c
                }
                return out
            }
            if (seg.origSize < n) return null
            return src.readFully(seg.offset, n)
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Match the start of image/audio files against their magic bytes: key = byte_read XOR magic[0], then
     * verified against the whole magic. The key with the most votes is chosen (at least 60% of samples).
     * A result of 0 means the archive is not XOR-ed.
     */
    override fun expectedChecksum(entry: Entry): Long? = (entry.meta as? XpMeta)?.adler

    override fun detectKey(src: RandomSource, entries: List<Entry>): Int? {
        val votes = IntArray(256)
        var total = 0
        for (e in entries) {
            if (total >= 40) break
            val ext = e.name.substringAfterLast('.', "").lowercase()
            val magic = MAGICS[ext] ?: continue
            val segs = (e.meta as? XpMeta)?.segs ?: continue
            val seg = segs.firstOrNull() ?: continue
            val head = readHead(src, seg, magic.size) ?: continue
            total++
            val k = (head[0].toInt() xor magic[0].toInt()) and 0xff
            var ok = true
            for (i in magic.indices) {
                if (((head[i].toInt() xor k) and 0xff) != (magic[i].toInt() and 0xff)) {
                    ok = false
                    break
                }
            }
            if (ok) votes[k]++
        }
        if (total == 0) return null
        var best = 0
        for (k in 1..255) if (votes[k] > votes[best]) best = k
        return if (votes[best] > 0 && votes[best] * 5 >= total * 3) best else null
    }

    override val packOptions = listOf(
        OptionSpec(key = "compress", kind = OptKind.BOOL, en = "Compress (zlib)", id = "Kompres (zlib)", default = "1"),
        OptionSpec(
            key = "level", kind = OptKind.CHOICE, en = "Compression level", id = "Level kompresi",
            choices = listOf(
                Choice("6", "Normal", "Normal"), Choice("1", "Fast (bigger file)", "Cepat (file lebih besar)"),
                Choice("9", "Best (slower)", "Terbaik (lebih lambat)"),
            ),
            default = "6", showIf = "compress" to "1",
        ),
        OptionSpec(
            key = "xor", kind = OptKind.NUMBER,
            en = "XOR key 0-255 (optional)", id = "Kunci XOR 0-255 (opsional)",
            hintEn = "Leave empty for no encryption. Must match the key the game expects.",
            hintId = "Kosongkan bila tidak terenkripsi. Harus sama dengan kunci yang dipakai game.",
        ),
    )

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(MAGIC)
            raf.write(ByteArray(8)) // index offset placeholder
            val idx = java.io.ByteArrayOutputStream()
            val buf = ByteArray(CHUNK)

            inputs.forEachIndexed { i, f ->
                progress.update(i, inputs.size, f.path)
                val adler = Adler32()
                val segs = ArrayList<Seg>()
                var orig = 0L
                var packedTotal = 0L
                f.open().use { ins ->
                    while (true) {
                        val n = IoUtil.fill(ins, buf)
                        if (n <= 0) break
                        val chunk = buf.copyOf(n)
                        adler.update(chunk, 0, n)
                        opts.xorKey?.let { k ->
                            for (j in chunk.indices) chunk[j] = (chunk[j].toInt() xor k).toByte()
                        }
                        val stored = if (opts.compress) IoUtil.deflate(chunk, opts.int("level", 6)) else chunk
                        val off = raf.filePointer
                        raf.write(stored)
                        segs += Seg(if (opts.compress) 1 else 0, off, n.toLong(), stored.size.toLong())
                        orig += n
                        packedTotal += stored.size
                    }
                }
                if (segs.isEmpty()) segs += Seg(0, raf.filePointer, 0, 0)

                val nameBytes = f.path.toByteArray(Charsets.UTF_16LE)
                val infoBody = 4 + 8 + 8 + 2 + nameBytes.size
                val segmBody = 28 * segs.size
                val fileBody = (12 + infoBody) + (12 + segmBody) + (12 + 4)
                val b = ByteBuffer.allocate(12 + fileBody).order(ByteOrder.LITTLE_ENDIAN)
                b.putInt(T_FILE).putLong(fileBody.toLong())
                b.putInt(T_INFO).putLong(infoBody.toLong())
                b.putInt(0).putLong(orig).putLong(packedTotal)
                b.putShort((nameBytes.size / 2).toShort()).put(nameBytes)
                b.putInt(T_SEGM).putLong(segmBody.toLong())
                for (s in segs) b.putInt(s.flags).putLong(s.offset).putLong(s.origSize).putLong(s.packedSize)
                b.putInt(T_ADLR).putLong(4).putInt(adler.value.toInt())
                idx.write(b.array())
            }

            val indexOffset = raf.filePointer
            val raw = idx.toByteArray()
            val comp = IoUtil.deflate(raw, opts.int("level", 6))
            val h = ByteBuffer.allocate(17).order(ByteOrder.LITTLE_ENDIAN)
            h.put(1).putLong(comp.size.toLong()).putLong(raw.size.toLong())
            raf.write(h.array())
            raf.write(comp)
            raf.seek(11)
            raf.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(indexOffset).array())
            progress.update(inputs.size, inputs.size, "selesai")
        }
    }
}
