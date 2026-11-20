package com.furuarc.formats

import com.furuarc.core.*
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile

/** Ren'Py archive (.rpa): RPA-2.0, RPA-3.0, RPA-3.2 (baca); RPA-3.0 (tulis). */
object RpaFormat : ArchiveFormat {
    override val id = "rpa"
    override val displayName = "RPA (Ren'Py)"
    override val extensions = listOf("rpa")

    override fun matches(head: ByteArray) =
        head.size >= 8 && String(head, 0, 4, Charsets.US_ASCII) == "RPA-"

    private class Meta(val prefix: ByteArray)

    override fun list(src: RandomSource, opts: Options): List<Entry> {
        val head = src.readFully(0, minOf(80L, src.length).toInt())
        val nl = head.indexOf('\n'.code.toByte())
        if (nl < 0) throw IOException("Header RPA tidak valid")
        val parts = String(head, 0, nl, Charsets.US_ASCII).trim().split(" ")
        val version = parts[0]
        val indexOffset = parts[1].toLong(16)
        var key = 0L
        when (version) {
            "RPA-3.0" -> parts.drop(2).forEach { key = key xor it.toLong(16) }
            "RPA-3.2" -> parts.drop(3).forEach { key = key xor it.toLong(16) }
            "RPA-2.0" -> key = 0L
            else -> throw IOException("Versi tidak didukung: $version")
        }

        val comp = src.readFully(indexOffset, (src.length - indexOffset).toInt())
        val raw = IoUtil.inflate(comp)
        val map = PickleLite.parse(raw) as? Map<*, *> ?: throw IOException("Index RPA bukan dict")

        val out = ArrayList<Entry>(map.size)
        for ((k, v) in map) {
            val name = when (k) {
                is String -> k
                is PyBytes -> String(k.bytes, Charsets.UTF_8)
                else -> continue
            }
            val tuple = (v as List<*>).firstOrNull() as? List<*> ?: continue
            val off = (tuple[0] as Number).toLong() xor key
            val len = (tuple[1] as Number).toLong() xor key
            val prefix = when (val p = tuple.getOrNull(2)) {
                is PyBytes -> p.bytes
                is String -> p.toByteArray(Charsets.ISO_8859_1)
                else -> ByteArray(0)
            }
            out += Entry(name, off, len, len, Meta(prefix))
        }
        return out.sortedBy { it.name }
    }

    override fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options) {
        val prefix = (entry.meta as? Meta)?.prefix ?: ByteArray(0)
        out.write(prefix)
        IoUtil.copyRange(src, entry.offset, entry.size - prefix.size, out)
    }

    override val packOptions = listOf(
        OptionSpec(
            key = "rpa_ver", kind = OptKind.CHOICE,
            en = "RPA version", id = "Versi RPA",
            choices = listOf(
                Choice("3.0", "RPA-3.0 (Ren'Py 6.x - 8.x, recommended)", "RPA-3.0 (Ren'Py 6.x - 8.x, disarankan)"),
                Choice("2.0", "RPA-2.0 (old Ren'Py 6.x)", "RPA-2.0 (Ren'Py 6.x lama)"),
            ),
            default = "3.0",
            hintEn = "Choose RPA-3.0 if you are not sure.", hintId = "Pilih RPA-3.0 jika tidak mengetahuinya.",
        ),
        OptionSpec(
            key = "rpa_key", kind = OptKind.TEXT,
            en = "XOR key (hex, optional)", id = "Kunci XOR (hex, opsional)",
            default = "DEADBEEF",
            hintEn = "Up to 8 hex digits (Ren'Py default: DEADBEEF). Only used by RPA-3.0.", hintId = "Maks 8 digit hex (bawaan Ren'Py: DEADBEEF). Hanya untuk RPA-3.0.",
            showIf = "rpa_ver" to "3.0",
        ),
    )

    override fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress) {
        val v2 = opts.str("rpa_ver", "3.0") == "2.0"
        val key = if (v2) 0L else (opts.str("rpa_key", "42424242").trim().removePrefix("0x").toLongOrNull(16) ?: 0x42424242L) and 0xffffffffL
        val headerLen = if (v2) 25 else 34
        RandomAccessFile(target, "rw").use { raf ->
            raf.setLength(0)
            raf.write(ByteArray(headerLen)) // placeholder header
            val rec = ArrayList<Triple<String, Long, Long>>()
            val buf = ByteArray(256 * 1024)
            inputs.forEachIndexed { i, f ->
                progress.update(i, inputs.size, f.path)
                val start = raf.filePointer
                var total = 0L
                f.open().use { ins ->
                    while (true) {
                        val n = ins.read(buf)
                        if (n < 0) break
                        raf.write(buf, 0, n)
                        total += n
                    }
                }
                rec += Triple(f.path, start xor key, total xor key)
            }
            val indexOffset = raf.filePointer
            raf.write(IoUtil.deflate(PickleLite.buildRpaIndex(rec)))
            raf.seek(0)
            val hdr = if (v2) String.format("RPA-2.0 %016x\n", indexOffset)
            else String.format("RPA-3.0 %016x %08x\n", indexOffset, key)
            raf.write(hdr.toByteArray(Charsets.US_ASCII))
            progress.update(inputs.size, inputs.size, "selesai")
        }
    }
}
