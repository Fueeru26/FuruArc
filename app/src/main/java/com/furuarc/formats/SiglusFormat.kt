package com.furuarc.formats

import com.furuarc.core.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

/** SiglusEngine exe key option (used by encrypted Scene.pck and Gameexe.dat). */
val SIGLUS_KEY_SPEC = OptionSpec(
    key = "skey", kind = OptKind.TEXT,
    en = "Exe key (only if the game needs it)", id = "Kunci exe (hanya jika game memerlukannya)",
    hintEn = "Exe key (only if the game needs it). Leave empty: for Scene.pck the key is found automatically. Gameexe.dat needs the key if it is encrypted (use Key Finder): 16 hex bytes (e.g. 0x12, 0x34, … or 32 hex digits) or the 暗号 text.",
    hintId = "Kunci exe (hanya jika game memerlukannya). Kosongkan: untuk Scene.pck kunci dicari otomatis. Gameexe.dat yang terenkripsi butuh kunci (cari dengan Pencari Kunci): 16 byte hex (mis. 0x12, 0x34, … atau 32 digit hex) atau teks 暗号.",
)

private fun keyOf(opts: Options): ByteArray? {
    val t = opts.str("skey")
    if (t.isBlank()) return null
    return try { SiglusCodec.parseKey(t) } catch (e: IOException) { throw e } catch (e: Exception) { throw IOException("Kunci tidak valid") }
}

// ========================= Scene.pck =========================

/**
 * SiglusEngine Scene.pck. Header of 23 int32 (92 bytes): global table (.inc), scene name list, scene data list.
 * Each scene = .dat (XOR with the exe key if used, XOR with the fixed key, LZSS). When unpacked, file #0 `_FuruArc_Siglus.meta`
 * stores the header + global table + scene order; this file MUST be included when packing so Scene.pck can be rebuilt.
 */
object SiglusPckFormat : ArchiveFormat {
    override val id = "siglus"
    override val displayName = "PCK (SiglusEngine)"
    override val extensions = listOf("pck")

    const val META = "_FuruArc_Siglus.meta"
    private const val META_OLD = "_GameArc_Siglus.meta"
    private const val HDR = 92
    private val META_MAGIC = "GASP".toByteArray(Charsets.US_ASCII)

    override val packOptions = listOf(SIGLUS_KEY_SPEC)
    override val unpackOptions = listOf(SIGLUS_KEY_SPEC)

    override fun matches(head: ByteArray) = head.size >= 4 && head.le().getInt(0) == HDR

    private class Hdr(val h: IntArray) {
        val size get() = h[0]
        val nameIdxOfs get() = h[13]
        val nameCnt get() = h[14]
        val nameOfs get() = h[15]
        val dataIdxOfs get() = h[17]
        val dataCnt get() = h[18]
        val dataOfs get() = h[19]
        val mod get() = h[21]
    }

    private fun readHdr(src: RandomSource): Hdr {
        if (src.length < HDR) throw IOException("File terlalu kecil")
        val bb = src.readFully(0, HDR).le()
        return Hdr(IntArray(23) { bb.getInt(it * 4) })
    }

    override fun detects(src: RandomSource, head: ByteArray): Boolean {
        if (!matches(head) || src.length < HDR) return false
        val h = try { readHdr(src) } catch (e: Exception) { return false }
        val len = src.length
        if (h.size < HDR || h.size.toLong() > len) return false
        if (h.mod != 0 && h.mod != 1) return false
        if (h.nameCnt < 0 || h.nameCnt != h.h[16] || h.dataCnt < 0 || h.nameCnt != h.dataCnt) return false
        val ok = longArrayOf(h.nameIdxOfs.toLong(), h.nameOfs.toLong(), h.dataIdxOfs.toLong(), h.dataOfs.toLong())
        if (ok.any { it < h.size || it > len }) return false
        if (!(ok[0] <= ok[1] && ok[1] <= ok[2] && ok[2] <= ok[3])) return false
        if (h.nameIdxOfs.toLong() + h.nameCnt.toLong() * 8 > len || h.dataIdxOfs.toLong() + h.dataCnt.toLong() * 8 > len) return false
        return true
    }

    private class Scn(val index: Int, val rawName: String)

    @Volatile private var keyCache: Pair<String, ByteArray?>? = null

    private fun rawBlobs(src: RandomSource, h: Hdr): List<ByteArray> {
        val idx = src.readFully(h.dataIdxOfs.toLong(), h.dataCnt * 8).le()
        val out = ArrayList<ByteArray>(h.dataCnt)
        for (i in 0 until h.dataCnt) {
            val o = idx.getInt(i * 8).toLong() and 0xffffffffL
            val l = idx.getInt(i * 8 + 4).toLong() and 0xffffffffL
            if (l < 32 || l > 64L * 1024 * 1024 || h.dataOfs.toLong() + o + l > src.length) continue
            out.add(src.readFully(h.dataOfs.toLong() + o, l.toInt()))
        }
        return out
    }

    /** Recover the exe key from Scene.pck itself (see [SiglusKeys]). null if not encrypted or on failure. */
    fun findKey(src: RandomSource, cancelled: () -> Boolean = { false }): SiglusKeys.Result? {
        val h = readHdr(src)
        if (h.mod == 0) return null
        return SiglusKeys.find(rawBlobs(src, h), cancelled)
    }

    private fun resolveKey(src: RandomSource, h: Hdr, opts: Options): ByteArray? {
        keyOf(opts)?.let { return it }
        if (h.mod == 0) return null
        val id = "${src.length}/${h.dataOfs}/${h.dataCnt}/" + src.readFully(h.dataOfs.toLong(), minOf(64L, src.length - h.dataOfs).toInt()).contentHashCode()
        keyCache?.let { if (it.first == id) return it.second }
        val r = try { SiglusKeys.find(rawBlobs(src, h)) } catch (e: Exception) { null }
        val k = if (r != null && r.ok) r.key else null
        keyCache = Pair(id, k)
        return k
    }

    private fun names(src: RandomSource, h: Hdr): List<String> {
        val n = h.nameCnt
        val idx = src.readFully(h.nameIdxOfs.toLong(), n * 8).le()
        val out = ArrayList<String>(n)
        for (i in 0 until n) {
            val o = idx.getInt(i * 8)
            val l = idx.getInt(i * 8 + 4)
            if (o < 0 || l < 0 || h.nameOfs.toLong() + (o.toLong() + l) * 2 > src.length) throw IOException("Daftar nama scene tidak valid")
            out.add(if (l == 0) "" else String(src.readFully(h.nameOfs.toLong() + o * 2L, l * 2), Charsets.UTF_16LE))
        }
        return out
    }

    private fun fileName(i: Int, raw: String) = (if (raw.isEmpty()) String.format("_noname_%04d", i) else raw) + ".dat"

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val h = readHdr(src)
        val key = resolveKey(src, h, opts)
        val nm = names(src, h)
        val idx = src.readFully(h.dataIdxOfs.toLong(), h.dataCnt * 8).le()
        val out = ArrayList<Entry>(nm.size + 1)
        out += Entry(META, 0, 0, 0, MetaMarker)
        val seen = HashSet<String>()
        for (i in nm.indices) {
            val o = idx.getInt(i * 8).toLong() and 0xffffffffL
            val l = idx.getInt(i * 8 + 4).toLong() and 0xffffffffL
            val abs = h.dataOfs.toLong() + o
            if (abs + l > src.length) throw IOException("Data scene di luar arsip")
            var size = l
            if (l >= 8 && (h.mod == 0 || key != null)) {
                val b = src.readFully(abs, 8)
                if (h.mod != 0) SiglusCodec.xorCycle(b, key!!)
                SiglusCodec.xorCycle(b, SiglusCodec.EASY)
                val packed = b.le().getInt(0).toLong() and 0xffffffffL
                val org = b.le().getInt(4).toLong() and 0xffffffffL
                if (packed == l && org in 1..0x40000000L) size = org
            }
            var name = fileName(i, nm[i])
            if (!seen.add(name.lowercase())) name = name.removeSuffix(".dat") + "_$i.dat"
            out += Entry(name, abs, size, l, Scn(i, nm[i]))
        }
        return out
    }

    private object MetaMarker

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val h = readHdr(src)
        if (entry.meta === MetaMarker) {
            out.write(buildMeta(src, h, resolveKey(src, h, opts)))
            return
        }
        val key = resolveKey(src, h, opts)
        val blob = src.readFully(entry.offset, entry.packedSize.toInt())
        out.write(SiglusCodec.decodeScene(blob, h.mod, key))
    }

    private fun buildMeta(src: RandomSource, h: Hdr, key: ByteArray?): ByteArray {
        val prefix = src.readFully(0, h.nameIdxOfs)
        val nm = names(src, h)
        val bo = ByteArrayOutputStream()
        val bb = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN)
        bo.write(META_MAGIC)
        bb.putInt(1).putInt(prefix.size)
        bo.write(bb.array())
        bo.write(prefix)
        bb.clear(); bb.putInt(nm.size); bo.write(bb.array(), 0, 4)
        for (n in nm) {
            val u = n.toByteArray(Charsets.UTF_8)
            bb.clear(); bb.putInt(u.size); bo.write(bb.array(), 0, 4)
            bo.write(u)
        }
        // v2: the exe key that was found (so packing does not need the key to be entered again)
        if (key != null && key.size == 16) { bo.write(1); bo.write(key) } else bo.write(0)
        return bo.toByteArray()
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        var key = keyOf(opts)
        val metaIn = inputs.firstOrNull { it.path.replace('\\', '/').substringAfterLast('/') == META }
            ?: inputs.firstOrNull { it.path.replace('\\', '/').substringAfterLast('/') == META_OLD }
            ?: throw IOException("File $META tidak ditemukan di folder. Bongkar Scene.pck asli dulu, lalu edit isi folder hasilnya.")
        val m = metaIn.open().use { it.readBytes() }.le()
        val mb = m.array()
        if (mb.size < 16 || !META_MAGIC.indices.all { mb[it] == META_MAGIC[it] }) throw IOException("$META tidak valid")
        val prefixLen = m.getInt(8)
        if (prefixLen < HDR || 12 + prefixLen + 4 > mb.size) throw IOException("$META rusak")
        val prefix = mb.copyOfRange(12, 12 + prefixLen)
        var p = 12 + prefixLen
        val cnt = m.getInt(p); p += 4
        val order = ArrayList<String>(cnt)
        repeat(cnt) {
            val l = m.getInt(p); p += 4
            if (l < 0 || p + l > mb.size) throw IOException("$META rusak")
            order.add(String(mb, p, l, Charsets.UTF_8)); p += l
        }
        if (key == null && p < mb.size && mb[p].toInt() == 1 && p + 17 <= mb.size) key = mb.copyOfRange(p + 1, p + 17)
        val mod = prefix.le().getInt(21 * 4)
        if (mod != 0 && key == null) throw IOException("Scene.pck ini memakai kunci exe, tetapi $META tidak memuat kunci. Isi opsi Kunci (hex 16 byte) — cari dengan Pencari Kunci.")

        val byName = LinkedHashMap<String, PackInput>()
        for (f in inputs) {
            val base = f.path.replace('\\', '/').substringAfterLast('/')
            if (base == META || base == META_OLD || !base.lowercase().endsWith(".dat")) continue
            byName[base.dropLast(4)] = f
        }
        // order: same as the original Scene.pck, then new scenes
        val finalNames = ArrayList<String>()
        val finalInputs = ArrayList<PackInput>()
        for ((i, raw) in order.withIndex()) {
            val fname = if (raw.isEmpty()) String.format("_noname_%04d", i) else raw
            val f = byName.remove(fname) ?: byName.remove(fname.removeSuffix("_$i")) ?: throw IOException("Scene \"$fname.dat\" tidak ada di folder")
            finalNames.add(raw); finalInputs.add(f)
        }
        for ((n, f) in byName) { finalNames.add(n); finalInputs.add(f) }

        val fkey = key
        val blobs = ArrayList<ByteArray>(finalInputs.size)
        for ((i, f) in finalInputs.withIndex()) {
            progress.update(i, finalInputs.size, f.path)
            val dat = f.open().use { it.readBytes() }
            blobs.add(SiglusCodec.encodeScene(dat, mod, fkey))
        }

        val nameBlob = ByteArrayOutputStream()
        val nameIdx = ByteBuffer.allocate(finalNames.size * 8).order(ByteOrder.LITTLE_ENDIAN)
        var units = 0
        for (n in finalNames) {
            nameIdx.putInt(units).putInt(n.length)
            nameBlob.write(n.toByteArray(Charsets.UTF_16LE))
            units += n.length
        }
        val dataIdx = ByteBuffer.allocate(blobs.size * 8).order(ByteOrder.LITTLE_ENDIAN)
        var pos = 0L
        for (b in blobs) {
            if (pos + b.size > 0x7fffffffL) throw IOException("Ukuran Scene.pck melebihi 2 GB")
            dataIdx.putInt(pos.toInt()).putInt(b.size)
            pos += b.size
        }
        val hb = prefix.le()
        val nameIdxOfs = prefix.size
        val nameOfs = nameIdxOfs + finalNames.size * 8
        val dataIdxOfs = nameOfs + nameBlob.size()
        val dataOfs = dataIdxOfs + blobs.size * 8
        hb.putInt(13 * 4, nameIdxOfs); hb.putInt(14 * 4, finalNames.size)
        hb.putInt(15 * 4, nameOfs); hb.putInt(16 * 4, finalNames.size)
        hb.putInt(17 * 4, dataIdxOfs); hb.putInt(18 * 4, blobs.size)
        hb.putInt(19 * 4, dataOfs); hb.putInt(20 * 4, blobs.size)
        hb.putInt(22 * 4, 0) // the original source is not packed
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(prefix)
            raf.write(nameIdx.array())
            raf.write(nameBlob.toByteArray())
            raf.write(dataIdx.array())
            for (b in blobs) raf.write(b)
        }
        progress.update(finalInputs.size, finalInputs.size, "")
    }
}

// ========================= Gameexe.dat =========================

/** SiglusEngine Gameexe.dat: game configuration; unpacked into Gameexe.ini (UTF-8) and packed back. */
object SiglusGameexeFormat : ArchiveFormat {
    override val id = "siglus_gameexe"
    override val displayName = "Gameexe.dat (SiglusEngine)"
    override val extensions = listOf("dat")

    const val INI = "Gameexe.ini"

    override val packOptions = listOf(SIGLUS_KEY_SPEC)
    override val unpackOptions = listOf(SIGLUS_KEY_SPEC)

    override fun matches(head: ByteArray): Boolean {
        if (head.size < 8) return false
        val bb = head.le()
        return bb.getInt(0) == 0 && (bb.getInt(4) == 0 || bb.getInt(4) == 1)
    }

    override fun detects(src: RandomSource, head: ByteArray): Boolean {
        if (!matches(head) || src.length < 16 || src.length > 64L * 1024 * 1024) return false
        val mode = head.le().getInt(4)
        if (mode != 0) return src.length in 24..(64L * 1024 * 1024)
        val b = try { src.readFully(8, 8) } catch (e: Exception) { return false }
        SiglusCodec.xorCycle(b, SiglusCodec.GEI)
        val packed = b.le().getInt(0).toLong() and 0xffffffffL
        val org = b.le().getInt(4).toLong() and 0xffffffffL
        if (mode == 0) return packed == src.length - 8 && org in 2..0x10000000L
        // mode 1: encrypted with the exe key, contents cannot be checked without the key
        return src.length in 24..(64L * 1024 * 1024)
    }

    private fun decode(src: RandomSource, opts: Options): ByteArray {
        val all = src.readFully(0, src.length.toInt())
        val mode = all.le().getInt(4)
        val payload = all.copyOfRange(8, all.size)
        SiglusCodec.xorCycle(payload, SiglusCodec.GEI)
        if (mode != 0) {
            val key = keyOf(opts) ?: throw IOException("Gameexe.dat ini terenkripsi dengan kunci exe. Isi opsi Kunci; cari dengan Pencari Kunci (di folder game yang punya Scene.pck).")
            SiglusCodec.xorCycle(payload, key)
        }
        if (!SiglusCodec.looksLikeLzss(payload)) throw IOException(if (mode != 0) "Kunci exe salah atau Gameexe.dat rusak" else "Gameexe.dat tidak valid")
        return SiglusCodec.lzssUnpack(payload)
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val mode = src.readFully(0, 8).le().getInt(4)
        val b = src.readFully(8, 8)
        SiglusCodec.xorCycle(b, SiglusCodec.GEI)
        var org = 0L
        if (mode == 0) org = (b.le().getInt(4).toLong() and 0xffffffffL) / 2
        else keyOf(opts)?.let { k -> SiglusCodec.xorCycle(b, k); org = (b.le().getInt(4).toLong() and 0xffffffffL) / 2 }
        return listOf(Entry(INI, 8, org, src.length - 8, mode))
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        var text = String(decode(src, opts), Charsets.UTF_16LE)
        if (text.startsWith("﻿")) text = text.substring(1)
        out.write(text.toByteArray(Charsets.UTF_8))
    }

    fun decodeText(bytes: ByteArray): String {
        if (bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte()) return String(bytes, 2, bytes.size - 2, Charsets.UTF_16LE)
        if (bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) return String(bytes, 2, bytes.size - 2, Charsets.UTF_16BE)
        val s = if (bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte()) 3 else 0
        val dec = Charsets.UTF_8.newDecoder()
        return try { dec.decode(ByteBuffer.wrap(bytes, s, bytes.size - s)).toString() }
        catch (e: java.nio.charset.CharacterCodingException) { String(bytes, s, bytes.size - s, Charset.forName("windows-31j")) }
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val inp = inputs.firstOrNull { it.path.replace('\\', '/').substringAfterLast('/').equals(INI, true) }
            ?: inputs.firstOrNull { it.path.lowercase().endsWith(".ini") }
            ?: throw IOException("Gameexe.ini tidak ditemukan di folder")
        val key = keyOf(opts)
        val text = decodeText(inp.open().use { it.readBytes() })
        val lz = SiglusCodec.lzssPack(text.toByteArray(Charsets.UTF_16LE))
        SiglusCodec.xorCycle(lz, SiglusCodec.GEI)
        if (key != null) SiglusCodec.xorCycle(lz, key)
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(0).putInt(if (key != null) 1 else 0).array())
            raf.write(lz)
        }
        progress.update(1, 1, INI)
    }
}

// ========================= OVK (suara) =========================

/** SiglusEngine OVK (voice, z####.ovk): u32 count, 16-byte table (size, offset, number, sample count), then OGG chunks (sometimes XOR-ed with 1 byte). */
object SiglusOvkFormat : ArchiveFormat {
    override val id = "siglus_ovk"
    override val displayName = "OVK (SiglusEngine voice)"
    override val extensions = listOf("ovk")

    override fun matches(head: ByteArray) = head.size >= 4 && head.le().getInt(0).let { it in 1..200000 }

    private class Item(val size: Long, val offset: Long, val no: Int, val samples: Int)

    private fun table(src: RandomSource): List<Item> {
        val cnt = src.readFully(0, 4).le().getInt(0)
        if (cnt <= 0 || cnt > 200000 || 4L + cnt * 16L > src.length) throw IOException("Tabel OVK tidak valid")
        val bb = src.readFully(4, cnt * 16).le()
        val out = ArrayList<Item>(cnt)
        for (i in 0 until cnt) {
            val s = bb.getInt(i * 16).toLong() and 0xffffffffL
            val o = bb.getInt(i * 16 + 4).toLong() and 0xffffffffL
            if (o < 4L + cnt * 16L || o + s > src.length) throw IOException("Entri OVK di luar arsip")
            out.add(Item(s, o, bb.getInt(i * 16 + 8), bb.getInt(i * 16 + 12)))
        }
        return out
    }

    private fun oggKey(first: ByteArray): Int? {
        if (first.size < 4) return null
        if (first[0] == 'O'.code.toByte() && first[1] == 'g'.code.toByte() && first[2] == 'g'.code.toByte() && first[3] == 'S'.code.toByte()) return 0
        val k = (first[0].toInt() xor 'O'.code) and 0xff
        if (((first[1].toInt() xor k) and 0xff) == 'g'.code && ((first[2].toInt() xor k) and 0xff) == 'g'.code && ((first[3].toInt() xor k) and 0xff) == 'S'.code) return k
        return null
    }

    override fun detects(src: RandomSource, head: ByteArray): Boolean {
        if (!matches(head) || src.length < 24) return false
        return try {
            val t = table(src)
            val f = t.first()
            f.size >= 4 && oggKey(src.readFully(f.offset, 4)) != null
        } catch (e: Exception) { false }
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val seen = HashSet<Int>()
        return table(src).mapIndexed { i, t ->
            val no = if (seen.add(t.no)) t.no else 100000 + i
            Entry(String.format("%05d.ogg", no), t.offset, t.size, t.size, t.samples)
        }
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val b = src.readFully(entry.offset, entry.size.toInt())
        val k = oggKey(b) ?: throw IOException("Entri OVK bukan OGG")
        if (k != 0) for (i in b.indices) b[i] = (b[i].toInt() xor k).toByte()
        out.write(b)
    }

    /** Samples = granule position of the last OGG page. */
    private fun sampleCount(b: ByteArray): Int {
        var i = 0
        var last = 0L
        while (i + 27 <= b.size && b[i] == 'O'.code.toByte() && b[i + 1] == 'g'.code.toByte() && b[i + 2] == 'g'.code.toByte() && b[i + 3] == 'S'.code.toByte()) {
            val seg = b[i + 26].toInt() and 0xff
            if (i + 27 + seg > b.size) break
            var body = 0
            for (k in 0 until seg) body += b[i + 27 + k].toInt() and 0xff
            val gp = b.le().getLong(i + 6)
            if (gp >= 0) last = gp
            i += 27 + seg + body
        }
        return if (last in 0..Int.MAX_VALUE.toLong()) last.toInt() else 0
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val numRe = Regex("(\\d+)(?=\\.ogg$)", RegexOption.IGNORE_CASE)
        val items = inputs.filter { it.path.lowercase().endsWith(".ogg") }
        if (items.isEmpty()) throw IOException("Tidak ada file .ogg di folder")
        val named = items.mapIndexed { i, f ->
            val base = f.path.replace('\\', '/').substringAfterLast('/')
            Pair(numRe.find(base)?.value?.toLongOrNull()?.toInt() ?: (i + 1), f)
        }.sortedBy { it.first }
        val datas = ArrayList<ByteArray>()
        for ((i, p) in named.withIndex()) {
            progress.update(i, named.size, p.second.path)
            datas.add(p.second.open().use { it.readBytes() })
        }
        val n = named.size
        val tbl = ByteBuffer.allocate(4 + n * 16).order(ByteOrder.LITTLE_ENDIAN)
        tbl.putInt(n)
        var off = 4L + n * 16L
        for (i in 0 until n) {
            tbl.putInt(datas[i].size).putInt(off.toInt()).putInt(named[i].first).putInt(sampleCount(datas[i]))
            off += datas[i].size
        }
        if (off > 0x7fffffffL) throw IOException("Ukuran OVK melebihi 2 GB")
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(tbl.array())
            for (d in datas) raf.write(d)
        }
        progress.update(n, n, "")
    }
}
