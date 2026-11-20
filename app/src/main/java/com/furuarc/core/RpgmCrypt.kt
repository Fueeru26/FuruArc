package com.furuarc.core

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * RPG Maker MV/MZ asset encryption: fake 16-byte header "RPGMV\0\0\0" + version, then the first 16 bytes of the
 * original contents are XOR-ed with a 16-byte key (encryptionKey in data/System.json).
 */
object RpgmCrypt {
    private val HEADER = byteArrayOf(
        0x52, 0x50, 0x47, 0x4D, 0x56, 0, 0, 0,
        0, 3, 1, 0, 0, 0, 0, 0,
    )
    const val LEN = 16

    /** The first 16 bytes of every PNG file. */
    private val PNG_HEAD = intArrayOf(
        0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
        0x00, 0x00, 0x00, 0x0D, 0x49, 0x48, 0x44, 0x52,
    )

    // encrypted extension -> original extension
    private val DEC_MAP = mapOf(
        "rpgmvp" to "png", "rpgmvm" to "m4a", "rpgmvo" to "ogg",
        "png_" to "png", "m4a_" to "m4a", "ogg_" to "ogg",
    )

    fun parseKey(hex: String): ByteArray? {
        val h = hex.trim().replace(" ", "")
        if (h.length != 32) return null
        val b = ByteArray(16)
        for (i in 0 until 16) b[i] = (h.substring(i * 2, i * 2 + 2).toIntOrNull(16) ?: return null).toByte()
        return b
    }

    fun keyToHex(b: ByteArray): String = b.joinToString("") { String.format("%02x", it.toInt() and 0xFF) }

    private fun ext(path: String): String = path.substringAfterLast('.', "").lowercase()

    /** Decrypted file name, or null if it is not an encrypted asset file. */
    fun decryptedPath(path: String): String? {
        val e = DEC_MAP[ext(path)] ?: return null
        return path.substring(0, path.length - ext(path).length) + e
    }

    private fun isUnder(path: String, dir: String): Boolean =
        path.replace('\\', '/').lowercase().split('/').dropLast(1).contains(dir)

    /** Asset type that can be encrypted: 1 = image (png in img/), 2 = audio (ogg/m4a in audio/), 0 = none. */
    fun encryptKind(path: String): Int = when (ext(path)) {
        "png" -> if (isUnder(path, "img")) 1 else 0
        "ogg", "m4a" -> if (isUnder(path, "audio")) 2 else 0
        else -> 0
    }

    fun encryptedPath(path: String, mz: Boolean): String? {
        if (encryptKind(path) == 0) return null
        val e = ext(path)
        val n = when (e) {
            "png" -> if (mz) "png_" else "rpgmvp"
            "ogg" -> if (mz) "ogg_" else "rpgmvo"
            else -> if (mz) "m4a_" else "rpgmvm"
        }
        return path.substring(0, path.length - e.length) + n
    }

    /** Guess the key from the first 32 bytes of an encrypted PNG file (the PNG header is known). */
    fun detectKey(head: ByteArray): ByteArray? {
        if (head.size < LEN + LEN || !isEncryptedHeader(head)) return null
        val k = ByteArray(LEN)
        for (i in 0 until LEN) k[i] = (head[LEN + i].toInt() xor PNG_HEAD[i]).toByte()
        return k
    }

    fun isEncryptedHeader(head: ByteArray): Boolean =
        head.size >= 5 && head[0] == 0x52.toByte() && head[1] == 0x50.toByte() && head[2] == 0x47.toByte() &&
            head[3] == 0x4D.toByte() && head[4] == 0x56.toByte()

    /** Get encryptionKey from the contents of System.json (null if absent / not 32 hex). */
    fun keyFromSystemJson(text: String): ByteArray? = try {
        val m = Json.parse(text) as? Map<*, *>
        (m?.get("encryptionKey") as? String)?.let { parseKey(it) }
    } catch (e: Exception) {
        null
    }

    fun decrypt(ins: InputStream, out: OutputStream, key: ByteArray) {
        val head = ByteArray(LEN)
        if (IoUtil.fill(ins, head) < LEN || !isEncryptedHeader(head)) throw IOException("Bukan file aset RPGM terenkripsi")
        val first = ByteArray(LEN)
        val n = IoUtil.fill(ins, first)
        for (i in 0 until n) first[i] = (first[i].toInt() xor key[i].toInt()).toByte()
        out.write(first, 0, n)
        ins.copyTo(out, 256 * 1024)
    }

    fun encrypt(ins: InputStream, out: OutputStream, key: ByteArray) {
        out.write(HEADER)
        val first = ByteArray(LEN)
        val n = IoUtil.fill(ins, first)
        for (i in 0 until n) first[i] = (first[i].toInt() xor key[i].toInt()).toByte()
        out.write(first, 0, n)
        ins.copyTo(out, 256 * 1024)
    }
}
