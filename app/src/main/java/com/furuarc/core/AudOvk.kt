package com.furuarc.core

import java.io.IOException

/**
 * OVK container (Visual Arts / RealLive / Siglus): an entry table followed by a collection of Ogg Vorbis.
 *
 * Layout (GARbro RealLive/ArcOVK.cs, rlvm ReadVisualArtsTable, SiglusSceneScriptUtility sound.py):
 *   int32 count; then count x { uint32 size, uint32 offset, int32 number, int32 sampleCount } (16 bytes);
 *   Ogg data follows the table. No magic.
 * Covered variants: (1) plain Ogg; (2) single-byte XOR-ed Ogg (key detected from the first 4 bytes
 * 'OggS', like _xor_decrypt_ogg_auto in SiglusSceneScriptUtility).
 * Not covered: the NWK variant (12-byte entries containing NWA) and other OVK containers not documented in the reference sources.
 * No different OVK definition was found in Kirikiri/BGI sources; only the layout above is known.
 */
object OvkAudio {
    private const val ENTRY = 16
    private const val MAX_OUT = 600L * 1024 * 1024

    private class Ent(val size: Int, val offset: Int, val id: Int)

    private fun u32(b: ByteArray, o: Int): Long =
        (b[o].toLong() and 0xff) or ((b[o + 1].toLong() and 0xff) shl 8) or
            ((b[o + 2].toLong() and 0xff) shl 16) or ((b[o + 3].toLong() and 0xff) shl 24)

    private fun i32(b: ByteArray, o: Int): Int = u32(b, o).toInt()

    /** Single-byte XOR key that turns the 4 bytes at [o] into 'OggS' (0 if plain), or -1 if not Ogg. */
    private fun oggKey(b: ByteArray, o: Int): Int {
        if (o < 0 || o + 4 > b.size) return -1
        val k = (b[o].toInt() and 0xff) xor 'O'.code
        return if (((b[o + 1].toInt() and 0xff) xor k) == 'g'.code &&
            ((b[o + 2].toInt() and 0xff) xor k) == 'g'.code &&
            ((b[o + 3].toInt() and 0xff) xor k) == 'S'.code
        ) k else -1
    }

    /** Entry table if consistent with the file size, otherwise null. */
    private fun table(b: ByteArray): List<Ent>? {
        if (b.size < 4 + ENTRY) return null
        val count = u32(b, 0)
        if (count < 1 || count > (b.size - 4L) / ENTRY) return null
        val dataOff = 4L + count * ENTRY
        if (dataOff >= b.size) return null
        val out = ArrayList<Ent>(count.toInt())
        for (i in 0 until count.toInt()) {
            val p = 4 + i * ENTRY
            val size = u32(b, p)
            val off = u32(b, p + 4)
            if (size < 4 || off < dataOff || off + size > b.size) return null
            out.add(Ent(size.toInt(), off.toInt(), i32(b, p + 8)))
        }
        return out
    }

    /** OVK has no magic: valid if the entry table fits in the file and every entry starts with 'OggS' (plain or XOR-ed). */
    fun isOvk(b: ByteArray): Boolean {
        val t = table(b) ?: return false
        for (e in t) if (oggKey(b, e.offset) < 0) return false
        return true
    }

    /** Extract all entries; name '<5-digit number>.ogg', data = complete Ogg (XOR removed if needed). */
    fun extract(b: ByteArray): List<ExtractedFile> {
        val t = table(b) ?: throw IOException("Bukan arsip OVK yang valid: tabel entri tidak sesuai ukuran file")
        var total = 0L
        val names = HashSet<String>()
        val res = ArrayList<ExtractedFile>(t.size)
        for ((idx, e) in t.withIndex()) {
            total += e.size
            if (total > MAX_OUT) throw IOException("Hasil ekstraksi OVK melebihi 600 MB")
            val key = oggKey(b, e.offset)
            if (key < 0) throw IOException("Entri OVK #${e.id} bukan Ogg (tidak ada 'OggS' setelah percobaan XOR)")
            val data = ByteArray(e.size)
            System.arraycopy(b, e.offset, data, 0, e.size)
            if (key != 0) for (i in data.indices) data[i] = (data[i].toInt() xor key).toByte()
            var name = String.format("%05d.ogg", e.id)
            if (!names.add(name)) {
                name = String.format("%05d_%d.ogg", e.id, idx)
                names.add(name)
            }
            res.add(ExtractedFile(name, data))
        }
        return res
    }
}
