package com.furuarc.core

import java.io.ByteArrayOutputStream
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * RPG Maker MV/MZ save file. Contents are JSON wrapped in several layers; the layer order ("recipe")
 * is detected automatically when reading and reused when writing:
 *  - 'Z' = zlib (binary)
 *  - 'B' = base64 (text)
 *  - 'L' = LZString base64 (text)
 *  - 'U' = UTF-8 text in which each character (0-255) is one data byte (pako binary string)
 * Example: MV = "L"; MZ = "UZ" (zlib, then its bytes written as UTF-8 text). The recipe is written from the outermost layer.
 */
object RpgmSave {
    const val MV = 1
    const val MZ = 2
    const val MZ_DEFAULT = "UZ"

    class Decoded(val recipe: String, val json: String) {
        val kind: Int get() = if (recipe == "L") MV else MZ
    }

    fun recipeFor(kind: Int, mzRecipe: String?): String =
        if (kind == MV) "L" else (mzRecipe?.takeIf { it.isNotEmpty() && it != "L" } ?: MZ_DEFAULT)

    fun decode(file: ByteArray): Decoded? {
        var cur = file
        val ops = StringBuilder()
        repeat(6) {
            if (cur.size > 2 && cur[0] == 0x78.toByte()) {
                val z = inflate(cur)
                if (z != null) {
                    cur = z
                    ops.append('Z')
                    return@repeat
                }
            }
            val bin = unBinaryString(cur)
            if (bin != null) {
                cur = bin
                ops.append('U')
                return@repeat
            }
            var txt = String(cur, Charsets.UTF_8)
            if (txt.startsWith("﻿")) txt = txt.substring(1)
            txt = txt.trim()
            if (txt.isEmpty()) return null
            if (txt[0] == '{' || txt[0] == '[') return Decoded(ops.toString(), txt)
            val raw = Base64Lite.decode(txt)
            if (raw != null && raw.size > 2 && raw[0] == 0x78.toByte() && inflate(raw) != null) {
                cur = raw
                ops.append('B')
                return@repeat
            }
            val lz = try {
                LzString.decompressFromBase64(txt)
            } catch (e: Exception) {
                null
            }
            if (lz != null && lz.isNotEmpty() && (lz[0] == '{' || lz[0] == '[')) return Decoded(ops.toString() + "L", lz)
            return null
        }
        return null
    }

    fun encode(json: String, recipe: String): ByteArray {
        var data = json.toByteArray(Charsets.UTF_8)
        for (op in recipe.reversed()) {
            data = when (op) {
                'Z' -> deflate(data)
                'U' -> binaryString(data)
                'B' -> Base64Lite.encode(data).toByteArray(Charsets.US_ASCII)
                'L' -> LzString.compressToBase64(String(data, Charsets.UTF_8)).toByteArray(Charsets.US_ASCII)
                else -> data
            }
        }
        return data
    }

    /** UTF-8 text whose characters are all < 256 and whose bytes form a zlib stream -> the original bytes. */
    private fun unBinaryString(b: ByteArray): ByteArray? {
        if (b.size < 4 || b[0] != 0x78.toByte()) return null
        val t = try {
            Charsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(b)).toString()
        } catch (e: Exception) {
            return null
        }
        val out = ByteArray(t.length)
        for (i in t.indices) {
            val c = t[i].code
            if (c > 255) return null
            out[i] = c.toByte()
        }
        return if (inflate(out) != null) out else null
    }

    private fun binaryString(b: ByteArray): ByteArray {
        val sb = StringBuilder(b.size)
        for (x in b) sb.append((x.toInt() and 0xFF).toChar())
        return sb.toString().toByteArray(Charsets.UTF_8)
    }

    private fun inflate(b: ByteArray): ByteArray? = try {
        val inf = Inflater()
        inf.setInput(b)
        val bo = ByteArrayOutputStream()
        val buf = ByteArray(64 * 1024)
        while (!inf.finished()) {
            val n = inf.inflate(buf)
            if (n == 0 && (inf.needsInput() || inf.needsDictionary())) break
            bo.write(buf, 0, n)
        }
        val ok = inf.finished()
        inf.end()
        if (ok) bo.toByteArray() else null
    } catch (e: Exception) {
        null
    }

    private fun deflate(b: ByteArray): ByteArray {
        val d = Deflater(1)
        d.setInput(b)
        d.finish()
        val bo = ByteArrayOutputStream()
        val buf = ByteArray(64 * 1024)
        while (!d.finished()) bo.write(buf, 0, d.deflate(buf))
        d.end()
        return bo.toByteArray()
    }

    /** Save file extension by type. */
    fun ext(kind: Int) = if (kind == MZ) "rmmzsave" else "rpgsave"
}
