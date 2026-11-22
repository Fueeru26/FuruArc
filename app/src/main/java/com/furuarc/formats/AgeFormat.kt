package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream

/**
 * AGE (Eushully) ALF archive. An ALF has no table of its own: the file list lives in the shared index
 * SYS4INI.BIN / SYS5INI.BIN (or the per-archive .AAI). Layout follows GARbro ArcFormats/Eushully/ArcALF.cs (MIT).
 * Index: "S4IC"/"S3IC" magic, u32 packed size at 0x134, LZSS stream at 0x138 ("S4AC": 0x114/0x118; "S3IN": plain at 0x12C).
 * Unpacked index: i32 archive count, names (0x100 each), i32 file count, records of 0x50 bytes:
 * name[0x40], i32 archive id, i32 file number, u32 offset, u32 size (offset/size inside the ALF). "@" entries are skipped.
 * The index file is chosen with the "Index file" option; the ALF name is taken from the opened file.
 */
object AgeFormat : ArchiveFormat {
    override val id = "age"
    override val displayName = "AGE (Eushully ALF)"
    override val extensions = listOf("alf")
    override val canPack: Boolean get() = false
    override val explicitOnly: Boolean get() = true

    private class Meta

    val IDX_SPEC = OptionSpec(
        key = "ageidx", kind = OptKind.FILE,
        en = "Index file (SYS4INI.BIN / SYS5INI.BIN / .AAI)", id = "File indeks (SYS4INI.BIN / SYS5INI.BIN / .AAI)",
        default = "",
        hintEn = "Required: the ALF only holds data. Pick the index from the same game folder.",
        hintId = "Wajib: ALF hanya berisi data. Pilih file indeks dari folder game yang sama.",
    )
    private val ARC_SPEC = OptionSpec(
        key = "agearc", kind = OptKind.TEXT,
        en = "ALF name in the index (optional)", id = "Nama ALF di indeks (opsional)",
        default = "",
        hintEn = "Only if the file was renamed (for example DATA1.ALF).",
        hintId = "Hanya jika file diganti nama (contoh DATA1.ALF).",
    )

    override val unpackOptions = listOf(IDX_SPEC, ARC_SPEC, ENCODING_SPEC)
    override val packOptions: List<OptionSpec> = emptyList()

    override fun matches(head: ByteArray): Boolean = isIndexMagic(head)

    /** An ALF has no magic: it is accepted when the engine is chosen explicitly (explicitOnly). */
    override fun detects(src: RandomSource, head: ByteArray): Boolean = true

    private fun isIndexMagic(h: ByteArray): Boolean {
        if (h.size < 4 || h[0].toInt() != 'S'.code) return false
        val t = String(h, 1, 3, Charsets.US_ASCII)
        return t == "4IC" || t == "3IC" || t == "5IC" || t == "4AC" || t == "5AC" || t == "3IN"
    }

    /** LZSS: 4 KiB window, start position 0xFEE, window filled with 0. Flag bit 1 = literal. */
    private fun lzss(d: ByteArray, off: Int, n: Int): ByteArray {
        val out = java.io.ByteArrayOutputStream(n * 4)
        val fr = ByteArray(0x1000)
        var fp = 0xFEE
        var i = off
        val end = minOf(d.size, off + n)
        while (i < end) {
            val c = d[i++].toInt() and 0xFF
            for (b in 0 until 8) {
                if (i >= end) break
                if ((c shr b) and 1 != 0) {
                    val v = d[i++]
                    out.write(v.toInt()); fr[fp and 0xFFF] = v; fp++
                } else {
                    if (i + 2 > end) break
                    val lo = d[i].toInt() and 0xFF
                    val hi = d[i + 1].toInt() and 0xFF
                    i += 2
                    val o = lo or ((hi and 0xF0) shl 4)
                    val l = (hi and 0xF) + 3
                    for (k in 0 until l) {
                        val v = fr[(o + k) and 0xFFF]
                        out.write(v.toInt()); fr[fp and 0xFFF] = v; fp++
                    }
                }
            }
        }
        return out.toByteArray()
    }

    private fun readIndex(f: File): ByteArray {
        if (!f.isFile) throw IOException("File indeks tidak ditemukan — pilih lagi di opsi")
        val d = f.readBytes()
        if (!isIndexMagic(d)) throw IOException("Bukan file indeks Eushully (SYS4INI.BIN / SYS5INI.BIN / .AAI)")
        val t = String(d, 1, 3, Charsets.US_ASCII)
        if (t == "3IN") return d.copyOfRange(0x12C, d.size)
        val off = if (t == "4AC" || t == "5AC") 0x114 else 0x134
        if (d.size < off + 4) throw IOException("File indeks terpotong")
        val packed = d.le().getInt(off)
        if (packed <= 0 || off + 4 + packed.toLong() > d.size) throw IOException("Ukuran indeks tidak valid")
        return lzss(d, off + 4, packed)
    }

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val head = src.readFully(0, minOf(8L, src.length).toInt())
        if (isIndexMagic(head))
            throw IOException("Ini file indeks, bukan arsip. Buka file .ALF (mis. DATA1.ALF) dan pilih file indeks ini di opsi")
        val idxPath = opts.extra["ageidx"].orEmpty().trim()
        if (idxPath.isEmpty()) throw IOException("Pilih File indeks (SYS4INI.BIN / SYS5INI.BIN / .AAI) di opsi")
        val arcName = (opts.extra["agearc"]?.trim()?.takeIf { it.isNotEmpty() } ?: opts.extra["_arc"].orEmpty()).trim()
        val idx = readIndex(File(idxPath)).le()
        val cs = IoUtil.cs(opts)
        var p = 0
        if (idx.capacity() < 8) throw IOException("Indeks kosong")
        val arcCount = idx.getInt(p); p += 4
        if (arcCount <= 0 || arcCount > 4096 || p + arcCount * 0x100 + 4 > idx.capacity()) throw IOException("Indeks Eushully tidak valid")
        val names = ArrayList<String>(arcCount)
        for (i in 0 until arcCount) { names += IoUtil.cstr(idx.array(), p, 0x100, cs); p += 0x100 }
        var arcId = names.indexOfFirst { it.equals(arcName, ignoreCase = true) }
        if (arcId < 0) {
            if (arcCount == 1) arcId = 0
            else throw IOException("ALF \"$arcName\" tidak ada di indeks. Isi \"Nama ALF di indeks\" dengan salah satu: " + names.joinToString(", "))
        }
        val fc = idx.getInt(p); p += 4
        if (fc < 0 || fc > 5_000_000 || p.toLong() + fc.toLong() * 0x50 > idx.capacity()) throw IOException("Jumlah file indeks tidak valid")
        val len = src.length
        val out = ArrayList<Entry>()
        val seen = HashSet<String>()
        for (i in 0 until fc) {
            val name = IoUtil.cstr(idx.array(), p, 0x40, cs)
            val aid = idx.getInt(p + 0x40)
            val off = idx.getInt(p + 0x48).toLong() and 0xFFFFFFFFL
            val size = idx.getInt(p + 0x4C).toLong() and 0xFFFFFFFFL
            p += 0x50
            if (aid != arcId || name == "@" || !IoUtil.saneName(name)) continue
            if (off + size > len) continue   // beyond the end (truncated ALF)
            var nm = name
            if (!seen.add(nm.lowercase())) { nm = "${i}_$name"; seen.add(nm.lowercase()) }
            out += Entry(nm, off, size, meta = Meta())
        }
        if (out.isEmpty()) throw IOException("Tidak ada file yang cocok untuk \"${names[arcId]}\" — pastikan indeks dan ALF dari game yang sama")
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        IoUtil.copyRange(src, entry.offset, entry.size, out)
    }

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        throw IOException("Pengemasan ALF belum didukung")
    }
}
