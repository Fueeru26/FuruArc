package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * NOA (EntisGLS): "Entis\x1a" / "VIST\x1a", u32 0x02000400 di +8; DirEntry di 0x40 (port GARbro ArcNOA + crass).
 * Entry: u64 size, attr, encode_type, u64 offset (relative to the start of DirEntry), 8-byte time, extra, name (NUL-terminated).
 * Data in "filedata": u64 length at +8, contents at +0x10 (contents + 4-byte XOR checksum).
 * Read: Raw, ERISA-N (0x80000010), BSHF (0x40000000), ERISA+BSHF (0xC0000010). Written: Raw or BSHF (password-protected).
 */
object NoaFormat : ArchiveFormat {
    override val id = "noa"
    override val displayName = "NOA (EntisGLS)"
    override val extensions = listOf("noa", "dat", "rsa", "arc", "emc")

    private const val FILE_ID = 0x02000400
    private const val ENC_RAW = 0
    private const val ENC_ERISA = 0x80000010.toInt()
    private const val ENC_BSHF = 0x40000000
    private const val ENC_ERISA_BSHF = 0xC0000010.toInt()

    private class Meta(val enc: Int)

    override fun matches(head: ByteArray): Boolean {
        if (head.size < 12) return false
        val ok = (head.startsWith("Entis") && head[5].toInt() == 0x1a) || (head.startsWith("VIST") && head[4].toInt() == 0x1a)
        if (!ok) return false
        return head.le().getInt(8) == FILE_ID
    }

    private fun ByteArray.startsWith(s: String): Boolean {
        if (size < s.length) return false
        for (i in s.indices) if (this[i].toInt() != s[i].code) return false
        return true
    }

    private val PASS_SPEC = OptionSpec(
        key = "noapass", kind = OptKind.TEXT,
        en = "Password (encrypted NOA)", id = "Password (NOA terenkripsi)",
        default = "",
        hintEn = "Only for BSHF-encrypted entries. Key Finder can read it from the game's .exe (IDR_COTOMI).",
        hintId = "Hanya untuk entri terenkripsi BSHF. Pencari Kunci bisa membacanya dari .exe game (IDR_COTOMI).",
    )

    override val unpackOptions = listOf(PASS_SPEC, ENCODING_SPEC)
    override val packOptions = listOf(
        OptionSpec(
            key = "noapass", kind = OptKind.TEXT,
            en = "Password (empty = no encryption)", id = "Password (kosong = tanpa enkripsi)",
            default = "",
            hintEn = "Filled: every file is BSHF-encrypted with this password. Compression (ERISA) is not available when packing.",
            hintId = "Diisi: semua file dienkripsi BSHF dengan password ini. Kompresi (ERISA) tidak tersedia saat kemas.",
        ),
        ENCODING_SPEC,
    )

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val len = src.length
        if (len < 0x50) throw IOException("File terlalu kecil")
        val head = src.readFully(0, 0x30)
        if (!matches(head)) throw IOException("Bukan arsip NOA")
        val old = String(head, 0x10, 20, Charsets.US_ASCII) == "EMSAC-Binary Archive"
        val cs = if (old) IoUtil.sjis() else IoUtil.cs(opts)
        val out = ArrayList<Entry>()
        parseDir(src, 0x40, "", cs, out, 0)
        if (out.isEmpty()) throw IOException("NOA kosong atau indeks tidak terbaca")
        return out
    }

    private fun parseDir(src: RandomSource, dirOffset: Long, cur: String, cs: java.nio.charset.Charset, out: MutableList<Entry>, depth: Int) {
        val len = src.length
        if (depth > 32 || dirOffset + 0x14 > len) throw IOException("Indeks NOA di luar arsip")
        val h = src.readFully(dirOffset, 0x14)
        if (String(h, 0, 8, Charsets.US_ASCII) != "DirEntry") throw IOException("Catatan DirEntry tidak ditemukan")
        val bb = h.le()
        val size = bb.getLong(8)
        if (size <= 0 || size > Int.MAX_VALUE || dirOffset + 0x10 + size > len + 8) throw IOException("Ukuran DirEntry tidak valid")
        val body = src.readFully(dirOffset + 0x10, minOf(size, len - dirOffset - 0x10).toInt()).le()
        var p = 0
        val count = body.getInt(p); p += 4
        if (count < 0 || count > 2_000_000) throw IOException("Jumlah entri NOA tidak valid")
        for (i in 0 until count) {
            if (p + 36 > body.capacity()) throw IOException("Indeks NOA terpotong")
            val esize = body.getLong(p)
            val attr = body.getInt(p + 8)
            val enc = body.getInt(p + 12)
            val off = dirOffset + body.getLong(p + 16)
            p += 32
            val extra = body.getInt(p); p += 4
            if (extra < 0 || p + extra + 4 > body.capacity()) throw IOException("Indeks NOA rusak")
            p += extra
            val nl = body.getInt(p); p += 4
            if (nl < 0 || p + nl > body.capacity()) throw IOException("Nama NOA rusak")
            val name = IoUtil.cstr(body.array(), p, nl, cs)
            p += nl
            val full = if (cur.isEmpty()) name else "$cur/$name"
            when (attr) {
                0x10 -> parseDir(src, off + 0x10, full, cs, out, depth + 1)
                0x20, 0x40 -> return
                else -> {
                    if (!IoUtil.saneName(full) || off < 0 || off + 0x10 > len) continue
                    out += Entry(full, off, esize, packedSize = esize, meta = Meta(enc))
                }
            }
        }
    }

    private fun payload(src: RandomSource, e: Entry): ByteArray {
        val h = src.readFully(e.offset, 0x10).le()
        val size = h.getLong(8)
        if (size < 0 || size > Int.MAX_VALUE || e.offset + 0x10 + size > src.length) throw IOException("Catatan filedata rusak: ${e.name}")
        return src.readFully(e.offset + 0x10, size.toInt())
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val enc = (entry.meta as? Meta)?.enc ?: 0
        val want = entry.size
        if (enc == ENC_RAW) {
            val size = src.readFully(entry.offset + 8, 8).le().getLong()
            IoUtil.copyRange(src, entry.offset + 0x10, minOf(want, size), out)
            return
        }
        if (enc != ENC_ERISA && enc != ENC_BSHF && enc != ENC_ERISA_BSHF)
            throw IOException("Skema enkripsi 0x${Integer.toHexString(enc)} belum didukung (${entry.name})")
        if (want > Int.MAX_VALUE) throw IOException("Entri terlalu besar")
        val data = payload(src, entry)
        if (data.size <= 4) return
        var cipherLen = data.size - 4
        val pw = opts.extra["noapass"]?.trim().orEmpty()
        val bshf = enc and ENC_BSHF != 0
        if (bshf) {
            if (pw.isEmpty())
                throw IOException("Entri terenkripsi: isi opsi Password (cari dengan Pencari Kunci dari .exe game)")
            cipherLen = EntisCodec.bshfDecrypt(data, cipherLen, pw)
        }
        val plain: ByteArray = if (enc and 0x80000000.toInt() != 0) {
            EntisCodec.nemesisDecode(data, 0, data.size - 4, want.toInt())
        } else data.copyOf(minOf(want.toInt(), cipherLen))
        if (bshf) {
            val sum = EntisCodec.xorSum(plain, 0, minOf(want.toInt(), plain.size))
            for (i in 0 until 4) if (sum[i] != data[data.size - 4 + i])
                throw IOException("Checksum tidak cocok untuk ${entry.name} — password kemungkinan salah")
        }
        if (plain.size < want) throw IOException("Dekompresi ERISA berakhir lebih awal: ${entry.name}")
        out.write(plain, 0, want.toInt())
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val n = inputs.size
        val cs = IoUtil.cs(opts)
        val pw = opts.extra["noapass"]?.trim().orEmpty()
        val enc = if (pw.isEmpty()) ENC_RAW else ENC_BSHF
        val names = inputs.map {
            val b = it.path.toByteArray(cs)
            b + byteArrayOf(0)
        }
        // filedata payload size per file
        val payloadLen = inputs.map {
            if (enc == ENC_BSHF) (it.size + 31) / 32 * 32 + 4 else it.size + 4
        }
        var bodyLen = 4L
        for (nm in names) bodyLen += 32 + 4 + 4 + nm.size
        var rel = 0x10 + bodyLen            // offset relative to the start of DirEntry
        val head = ByteBuffer.allocate(0x40 + 0x10 + bodyLen.toInt()).order(ByteOrder.LITTLE_ENDIAN)
        head.put(byteArrayOf('E'.code.toByte(), 'n'.code.toByte(), 't'.code.toByte(), 'i'.code.toByte(), 's'.code.toByte(), 0x1a, 0, 0))
        head.putInt(FILE_ID).putInt(0)
        val desc = "ERISA-Archive file".toByteArray(Charsets.US_ASCII)
        head.put(desc); head.position(0x38)
        var total = 0x10 + bodyLen
        for (l in payloadLen) total += 0x10 + l
        head.putLong(total)                // length of the records after the header
        head.position(0x40)
        head.put("DirEntry".toByteArray(Charsets.US_ASCII)).putLong(bodyLen)
        head.putInt(n)
        val now = java.util.Calendar.getInstance()
        for (i in 0 until n) {
            head.putLong(inputs[i].size).putInt(0).putInt(enc).putLong(rel)
            head.put(now.get(java.util.Calendar.SECOND).toByte()).put(now.get(java.util.Calendar.MINUTE).toByte())
                .put(now.get(java.util.Calendar.HOUR_OF_DAY).toByte())
                .put(((now.get(java.util.Calendar.DAY_OF_WEEK) - 1)).toByte())
                .put(now.get(java.util.Calendar.DAY_OF_MONTH).toByte())
                .put((now.get(java.util.Calendar.MONTH) + 1).toByte())
                .putShort(now.get(java.util.Calendar.YEAR).toShort())
            head.putInt(0).putInt(names[i].size).put(names[i])
            rel += 0x10 + payloadLen[i]
        }
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(head.array())
            val chunk = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, n, f.path)
                val rec = ByteBuffer.allocate(0x10).order(ByteOrder.LITTLE_ENDIAN)
                rec.put("filedata".toByteArray(Charsets.US_ASCII)).putLong(payloadLen[i])
                raf.write(rec.array())
                val sum = ByteArray(4)
                var done = 0L
                val bs = if (enc == ENC_BSHF) EntisCodec.Bshf(pw) else null
                f.open().use { ins ->
                    var fill = 0
                    while (true) {
                        val r = ins.read(chunk, fill, chunk.size - fill)
                        if (r < 0) break
                        for (k in 0 until r) sum[((done + k) and 3).toInt()] = (sum[((done + k) and 3).toInt()].toInt() xor chunk[fill + k].toInt()).toByte()
                        done += r
                        fill += r
                        if (bs == null) { raf.write(chunk, 0, fill); fill = 0 }
                        else if (fill == chunk.size) {
                            var o = 0
                            while (o < fill) { bs.encryptBlock(chunk, o); o += 32 }
                            raf.write(chunk, 0, fill); fill = 0
                        }
                    }
                    if (bs != null && fill > 0) {
                        val padded = (fill + 31) / 32 * 32
                        java.util.Arrays.fill(chunk, fill, padded, 0)
                        var o = 0
                        while (o < padded) { bs.encryptBlock(chunk, o); o += 32 }
                        raf.write(chunk, 0, padded)
                    }
                }
                if (done != f.size) throw IOException("Ukuran file berubah saat dikemas: ${f.path}")
                raf.write(sum)
            }
            progress.update(n, n, "selesai")
        }
    }
}
