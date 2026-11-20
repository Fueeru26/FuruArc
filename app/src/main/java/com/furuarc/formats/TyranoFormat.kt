package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * TyranoScript (NW.js): package.nw is a plain ZIP file.
 * Read: ZIP (stored / deflate), no encryption and no ZIP64. Write: ZIP deflate.
 * Not supported yet: app.asar (Electron) and packages attached inside an .exe.
 */
object TyranoFormat : ArchiveFormat {
    override val id = "tyrano"
    override val displayName = "NW/ZIP (TyranoScript)"
    override val extensions = listOf("nw", "zip")

    override fun matches(head: ByteArray) =
        head.size >= 4 && head[0] == 'P'.code.toByte() && head[1] == 'K'.code.toByte() &&
            head[2].toInt() == 3 && head[3].toInt() == 4

    private class Meta(val method: Int)

    private fun u16(b: ByteArray, o: Int) = (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8)
    private fun u32(b: ByteArray, o: Int) = u16(b, o).toLong() or (u16(b, o + 2).toLong() shl 16)

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val tailLen = minOf(src.length, 65557L).toInt()
        if (tailLen < 22) throw IOException("File ZIP terlalu kecil")
        val tailPos = src.length - tailLen
        val tail = src.readFully(tailPos, tailLen)
        var e = tailLen - 22
        while (e >= 0 && !(tail[e].toInt() == 0x50 && tail[e + 1].toInt() == 0x4b &&
                tail[e + 2].toInt() == 5 && tail[e + 3].toInt() == 6)) e--
        if (e < 0) throw IOException("Akhir direktori ZIP tidak ditemukan")
        val count = u16(tail, e + 10)
        val cdSize = u32(tail, e + 12)
        val cdOff = u32(tail, e + 16)
        if (count == 0xffff || cdSize == 0xffffffffL || cdOff == 0xffffffffL)
            throw IOException("ZIP64 belum didukung")
        if (cdOff + cdSize > src.length) throw IOException("Direktori ZIP tidak valid")
        val cd = src.readFully(cdOff, cdSize.toInt())
        val out = ArrayList<Entry>(count)
        var p = 0
        for (i in 0 until count) {
            if (p + 46 > cd.size || u32(cd, p) != 0x02014b50L) throw IOException("Entri ZIP rusak")
            val flags = u16(cd, p + 8)
            val method = u16(cd, p + 10)
            val comp = u32(cd, p + 20)
            val orig = u32(cd, p + 24)
            val nl = u16(cd, p + 28)
            val xl = u16(cd, p + 30)
            val cl = u16(cd, p + 32)
            val local = u32(cd, p + 42)
            if (p + 46 + nl > cd.size) throw IOException("Entri ZIP rusak")
            val name = String(cd, p + 46, nl, Charsets.UTF_8)
            p += 46 + nl + xl + cl
            if (name.endsWith("/")) continue // folder
            if (flags and 1 != 0) throw IOException("ZIP terenkripsi belum didukung: $name")
            if (method != 0 && method != 8) throw IOException("Metode kompresi $method belum didukung: $name")
            out += Entry(name, local, orig, comp, Meta(method))
        }
        return out
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val method = (entry.meta as? Meta)?.method ?: 0
        val lh = src.readFully(entry.offset, 30)
        if (u32(lh, 0) != 0x04034b50L) throw IOException("Header lokal ZIP rusak")
        val dataOff = entry.offset + 30 + u16(lh, 26) + u16(lh, 28)
        if (method == 0) {
            IoUtil.copyRange(src, dataOff, entry.packedSize, out)
            return
        }
        val inf = Inflater(true)
        try {
            val inBuf = ByteArray(64 * 1024)
            val outBuf = ByteArray(64 * 1024)
            var pos = dataOff
            var left = entry.packedSize
            while (!inf.finished()) {
                if (inf.needsInput()) {
                    if (left <= 0) break
                    val r = src.readAt(pos, inBuf, 0, minOf(inBuf.size.toLong(), left).toInt())
                    if (r <= 0) throw IOException("Arsip terpotong")
                    inf.setInput(inBuf, 0, r)
                    pos += r; left -= r
                }
                val k = inf.inflate(outBuf)
                if (k > 0) out.write(outBuf, 0, k)
                else if (inf.needsDictionary()) throw IOException("Data deflate tidak valid")
                else if (!inf.needsInput() && !inf.finished()) break
            }
        } finally {
            inf.end()
        }
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
    )

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        ZipOutputStream(FileOutputStream(target).buffered(256 * 1024)).use { zo ->
            zo.setLevel(if (opts.compress) opts.int("level", 6) else Deflater.NO_COMPRESSION)
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, inputs.size, f.path)
                zo.putNextEntry(ZipEntry(f.path.replace('\\', '/')))
                f.open().use { ins ->
                    while (true) {
                        val n = ins.read(buf)
                        if (n < 0) break
                        zo.write(buf, 0, n)
                    }
                }
                zo.closeEntry()
            }
        }
        progress.update(inputs.size, inputs.size, "selesai")
    }
}
