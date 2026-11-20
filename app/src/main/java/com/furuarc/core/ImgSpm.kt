package com.furuarc.core

import java.io.IOException

/**
 * NeXAS SPM (.spm) -- NOT YET SUPPORTED as an image.
 *
 * Findings from public sources (Karaik/NeXAS_DX, SpmParser.java / Spm.java; crskycode/NeXAS_Tool,
 * pkuislm/NexasPackEdit, GARbro, awesome-game-file-format-reversing -- the last three do not cover SPM):
 * an SPM file is not a pixel image but a "sprite map": a version string "SPM VER-x.xx" (NUL-terminated),
 * a page list (chips: destination/source rects, image options, hit areas), a list of referenced image NAMES
 * (the actual images are in other files), and animation data. It contains no pixel data,
 * so nothing can be decoded to RGBA without making it up. [isSpm] only recognizes the file
 * so callers can show a clear message.
 */
object SpmImage {
    private val MAGIC = "SPM VER-".toByteArray(Charsets.US_ASCII)

    fun isSpm(head: ByteArray): Boolean {
        if (head.size < MAGIC.size) return false
        for (i in MAGIC.indices) if (head[i] != MAGIC[i]) return false
        return true
    }

    @Suppress("UNUSED_PARAMETER")
    fun decode(b: ByteArray): RgbaImage {
        throw IOException("SPM NeXAS belum didukung: berkas SPM berisi tata letak sprite, hitbox, animasi, dan nama gambar rujukan, bukan piksel")
    }
}
