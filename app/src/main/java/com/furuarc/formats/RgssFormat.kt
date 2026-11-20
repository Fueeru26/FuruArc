package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/** RGSS archive base (RPG Maker XP/VX/VX Ace): XOR encryption with a rotating key (k = k*7+3). */
private object Rgss {
    fun step(k: Int): Int = k * 7 + 3

    /** Names in the archive use "\" as the folder separator. UTF-8, or Shift-JIS for old archives. */
    fun decodeName(b: ByteArray): String {
        val s = try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(b)).toString()
        } catch (e: CharacterCodingException) {
            try {
                String(b, charset("windows-31j"))
            } catch (e2: Exception) {
                String(b, Charsets.ISO_8859_1)
            }
        }
        return s.replace('\\', '/')
    }

    fun encodeName(path: String): ByteArray = path.replace('/', '\\').toByteArray(Charsets.UTF_8)

    fun i32(src: RandomSource, pos: Long): Int = src.readFully(pos, 4).le().int

    /** XOR per 4-byte word; a final partial word uses the low bytes of the key. Returns the next key. */
    fun xorWords(buf: ByteArray, n: Int, keyIn: Int): Int {
        var key = keyIn
        var i = 0
        while (i < n) {
            var k = key
            var j = 0
            while (j < 4 && i + j < n) {
                buf[i + j] = (buf[i + j].toInt() xor (k and 0xFF)).toByte()
                k = k ushr 8
                j++
            }
            key = step(key)
            i += 4
        }
        return key
    }

    fun extract(src: RandomSource, e: Entry, out: OutputStream, dataKey: Int) {
        val buf = ByteArray(1024 * 1024) // multiple of 4 so the key sequence stays correct
        var key = dataKey
        var done = 0L
        while (done < e.size) {
            val want = minOf(buf.size.toLong(), e.size - done).toInt()
            var r = 0
            while (r < want) {
                val n = src.readAt(e.offset + done + r, buf, r, want - r)
                if (n <= 0) throw IOException("Arsip terpotong")
                r += n
            }
            key = xorWords(buf, want, key)
            out.write(buf, 0, want)
            done += want
        }
    }

    /** Copy and encrypt the contents of [ins] to [raf] at its current position. Returns the number of bytes. */
    fun copyEncrypted(ins: InputStream, raf: RandomAccessFile, dataKey: Int): Long {
        val buf = ByteArray(1024 * 1024)
        var key = dataKey
        var total = 0L
        while (true) {
            val n = IoUtil.fill(ins, buf)
            if (n <= 0) break
            key = xorWords(buf, n, key)
            raf.write(buf, 0, n)
            total += n
            if (n < buf.size) break
        }
        return total
    }

    fun le(v: Int): ByteArray = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(v).array()
}

/** RGSSAD version 1: XP (.rgssad) and VX (.rgss2a). */
object RgssV1Format : ArchiveFormat {
    override val id = "rgss1"
    override val displayName = "RGSSAD (XP/VX)"
    override val extensions = listOf("rgssad", "rgss2a")

    override fun matches(head: ByteArray): Boolean =
        head.size >= 8 && head[0] == 'R'.code.toByte() && head[1] == 'G'.code.toByte() && head[2] == 'S'.code.toByte() &&
            head[3] == 'S'.code.toByte() && head[4] == 'A'.code.toByte() && head[5] == 'D'.code.toByte() &&
            head[6] == 0.toByte() && head[7] == 1.toByte()

    private const val SEED = 0xDEADCAFE.toInt()

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val out = ArrayList<Entry>()
        var pos = 8L
        var key = SEED
        val len = src.length
        while (pos + 4 <= len) {
            val nameLen = Rgss.i32(src, pos) xor key
            key = Rgss.step(key)
            pos += 4
            if (nameLen < 0 || nameLen > 4096 || pos + nameLen + 4 > len) throw IOException("Arsip RGSSAD rusak")
            val nb = src.readFully(pos, nameLen)
            for (i in nb.indices) {
                nb[i] = (nb[i].toInt() xor (key and 0xFF)).toByte()
                key = Rgss.step(key)
            }
            pos += nameLen
            val size = Rgss.i32(src, pos) xor key
            key = Rgss.step(key)
            pos += 4
            if (size < 0 || pos + size > len) throw IOException("Arsip RGSSAD rusak")
            out.add(Entry(Rgss.decodeName(nb), pos, size.toLong(), meta = key))
            pos += size
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) =
        Rgss.extract(src, entry, out, entry.meta as Int)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(byteArrayOf('R'.code.toByte(), 'G'.code.toByte(), 'S'.code.toByte(), 'S'.code.toByte(), 'A'.code.toByte(), 'D'.code.toByte(), 0, 1))
            var key = SEED
            inputs.forEachIndexed { idx, inp ->
                progress.update(idx, inputs.size, inp.path)
                val nb = Rgss.encodeName(inp.path)
                raf.write(Rgss.le(nb.size xor key))
                key = Rgss.step(key)
                for (i in nb.indices) {
                    nb[i] = (nb[i].toInt() xor (key and 0xFF)).toByte()
                    key = Rgss.step(key)
                }
                raf.write(nb)
                // the real size is only known after the data is read: write first, fix afterwards
                val sizePos = raf.filePointer
                raf.write(ByteArray(4))
                val dataKey = Rgss.step(key).also { }
                // data key = key after writing the size
                val size = inp.open().use { Rgss.copyEncrypted(it, raf, dataKey) }
                if (size > Int.MAX_VALUE) throw IOException("File terlalu besar: ${inp.path}")
                val end = raf.filePointer
                raf.seek(sizePos)
                raf.write(Rgss.le(size.toInt() xor key))
                raf.seek(end)
                key = Rgss.step(key)
            }
            progress.update(inputs.size, inputs.size, "")
        }
    }
}

/** RGSS3A (VX Ace). */
object RgssV3Format : ArchiveFormat {
    override val id = "rgss3"
    override val displayName = "RGSS3A (VX Ace)"
    override val extensions = listOf("rgss3a")

    override fun matches(head: ByteArray): Boolean =
        head.size >= 8 && head[0] == 'R'.code.toByte() && head[1] == 'G'.code.toByte() && head[2] == 'S'.code.toByte() &&
            head[3] == 'S'.code.toByte() && head[4] == 'A'.code.toByte() && head[5] == 'D'.code.toByte() &&
            head[6] == 0.toByte() && head[7] == 3.toByte()

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val out = ArrayList<Entry>()
        val key = Rgss.i32(src, 8) * 9 + 3
        var pos = 12L
        val len = src.length
        while (true) {
            if (pos + 16 > len) throw IOException("Arsip RGSS3A rusak")
            val h = src.readFully(pos, 16).le()
            val offset = h.getInt(0) xor key
            if (offset == 0) break
            val size = h.getInt(4) xor key
            val entryKey = h.getInt(8) xor key
            val nameLen = h.getInt(12) xor key
            pos += 16
            if (nameLen < 0 || nameLen > 4096 || pos + nameLen > len) throw IOException("Arsip RGSS3A rusak")
            val nb = src.readFully(pos, nameLen)
            for (i in nb.indices) nb[i] = (nb[i].toInt() xor ((key ushr (8 * (i % 4))) and 0xFF)).toByte()
            pos += nameLen
            if (offset < 0 || size < 0 || offset.toLong() + size > len) throw IOException("Arsip RGSS3A rusak")
            out.add(Entry(Rgss.decodeName(nb), offset.toLong(), size.toLong(), meta = entryKey))
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) =
        Rgss.extract(src, entry, out, entry.meta as Int)

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val baseKey = 0x2B3C4D5E
        val key = baseKey * 9 + 3
        val names = inputs.map { Rgss.encodeName(it.path) }
        var tableLen = 4L // end marker
        for (n in names) tableLen += 16 + n.size
        val dataStart = 12 + tableLen
        if (dataStart > Int.MAX_VALUE) throw IOException("Terlalu banyak file")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(byteArrayOf('R'.code.toByte(), 'G'.code.toByte(), 'S'.code.toByte(), 'S'.code.toByte(), 'A'.code.toByte(), 'D'.code.toByte(), 0, 3))
            raf.write(Rgss.le(baseKey))
            raf.write(ByteArray(tableLen.toInt())) // table is filled in after all data is written
            val offs = IntArray(inputs.size)
            val sizes = IntArray(inputs.size)
            val keys = IntArray(inputs.size)
            inputs.forEachIndexed { idx, inp ->
                progress.update(idx, inputs.size, inp.path)
                keys[idx] = (inp.path.hashCode() * 31) xor 0x5A5A1234
                val start = raf.filePointer
                if (start > Int.MAX_VALUE) throw IOException("Arsip melebihi 2 GB")
                val size = inp.open().use { Rgss.copyEncrypted(it, raf, keys[idx]) }
                if (start + size > Int.MAX_VALUE) throw IOException("Arsip melebihi 2 GB")
                offs[idx] = start.toInt()
                sizes[idx] = size.toInt()
            }
            raf.seek(12)
            val tb = java.io.ByteArrayOutputStream()
            for (i in inputs.indices) {
                tb.write(Rgss.le(offs[i] xor key))
                tb.write(Rgss.le(sizes[i] xor key))
                tb.write(Rgss.le(keys[i] xor key))
                tb.write(Rgss.le(names[i].size xor key))
                val nb = names[i].copyOf()
                for (j in nb.indices) nb[j] = (nb[j].toInt() xor ((key ushr (8 * (j % 4))) and 0xFF)).toByte()
                tb.write(nb)
            }
            tb.write(Rgss.le(0 xor key))
            raf.write(tb.toByteArray())
            progress.update(inputs.size, inputs.size, "")
        }
    }
}
