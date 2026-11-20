package com.furuarc.core

import java.io.IOException

/**
 * EntisGLS ERI (lossless) image decoder. Port of GARbro ArcFormats/Entis/ImageERI.cs and EriReader.cs
 * (lossless path: RunlengthGamma, RunlengthHuffman, Nemesis/ERISA order-1; block prediction + color delta).
 *
 * Supported: RGB 24/32 bpp, RGBA 32 bpp, RGB 16 bpp (BGR555), Gray 8 bpp, paletted 8 bpp.
 * Not supported (IOException): lossy ERI (DCT/LOT), arithmetic code type 2/4 (GARbro does not either yet),
 * lossless EMI, and differential images ("reference-file").
 */
object EriImage {
    private const val MAX_DIM = 16384
    private const val MAX_PIXELS = 100_000_000L

    private const val CV_LOSSLESS_EMI = 0x03010000
    private const val CV_LOSSLESS_ERI = 0x03020000
    private const val CV_DCT_ERI = 0x00000001
    private const val CV_LOT_ERI = 0x00000005
    private const val CV_LOT_ERI_MSS = 0x00000105

    private const val ARCH_ARITHMETIC = 32
    private const val ARCH_GAMMA = -1
    private const val ARCH_HUFFMAN = -4
    private const val ARCH_NEMESIS = -16

    private const val TYPE_MASK = 0x0000FFFF
    private const val TYPE_RGB = 1
    private const val TYPE_GRAY = 2
    private const val TYPE_RGBA = 0x04000001
    private const val TYPE_WITH_ALPHA = 0x04000000

    fun isEri(head: ByteArray): Boolean {
        if (head.size < 0x28) return false
        val id = i32(head, 8)
        if (id != 0x03000100 && id != 0x02000100) return false
        return ascii(head, 0x10, "Entis Rasterized Image") || ascii(head, 0x10, "Moving Entis Image") ||
            ascii(head, 0x10, "EMSAC-Image")
    }

    private fun ascii(b: ByteArray, off: Int, s: String): Boolean {
        if (off + s.length > b.size) return false
        for (i in s.indices) if (b[off + i].toInt() != s[i].code) return false
        return true
    }

    private fun i32(b: ByteArray, o: Int): Int {
        if (o < 0 || o + 4 > b.size) throw IOException("Data ERI terpotong")
        return (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8) or
            ((b[o + 2].toInt() and 0xff) shl 16) or ((b[o + 3].toInt() and 0xff) shl 24)
    }

    private fun i64(b: ByteArray, o: Int): Long =
        (i32(b, o).toLong() and 0xffffffffL) or (i32(b, o + 4).toLong() shl 32)

    private class Info {
        var streamPos = 0
        var version = 0
        var transformation = 0
        var architecture = 0
        var formatType = 0
        var width = 0
        var height = 0
        var verticalFlip = false
        var bpp = 0
        var blockingDegree = 0
        var description: String? = null
    }

    fun decode(b: ByteArray): RgbaImage {
        try {
            return decodeImpl(b)
        } catch (e: IndexOutOfBoundsException) {
            throw IOException("Data ERI rusak atau terpotong")
        } catch (e: NegativeArraySizeException) {
            throw IOException("Data ERI rusak (ukuran tidak valid)")
        } catch (e: ArithmeticException) {
            throw IOException("Data ERI rusak (kesalahan aritmetika)")
        } catch (e: NullPointerException) {
            throw IOException("Data ERI rusak (struktur tidak konsisten)")
        }
    }

    private fun parseHeader(b: ByteArray): Info {
        if (b.size < 0x50 || !isEri(b.copyOf(minOf(b.size, 0x40)))) throw IOException("Bukan file ERI (header tidak cocok)")
        if (!ascii(b, 0x40, "Header  ")) throw IOException("Seksi Header ERI tidak ditemukan")
        val hl = i64(b, 0x48)
        if (hl <= 0 || hl > b.size) throw IOException("Ukuran seksi Header ERI tidak valid")
        var headerSize = hl.toInt()
        val streamPos = 0x50 + headerSize
        var pos = 0x50
        var info: Info? = null
        var desc: String? = null
        while (headerSize > 0x10) {
            val id = String(b, pos, 8, Charsets.ISO_8859_1)
            val len = i64(b, pos + 8)
            pos += 0x10
            headerSize -= 0x10
            if (len <= 0 || len > headerSize) break
            val l = len.toInt()
            if (id == "FileHdr ") {
                if (i32(b, pos) > 0x00020100) throw IOException("Versi file ERI tidak valid")
            } else if (id == "ImageInf") {
                val version = i32(b, pos)
                if (version != 0x00020100 && version != 0x00020200) throw IOException("Versi ImageInf ERI tidak didukung: 0x" + Integer.toHexString(version))
                val n = Info()
                n.streamPos = streamPos
                n.version = version
                n.transformation = i32(b, pos + 4)
                n.architecture = i32(b, pos + 8)
                n.formatType = i32(b, pos + 12)
                val w = i32(b, pos + 16)
                val h = i32(b, pos + 20)
                n.width = Math.abs(w)
                n.height = Math.abs(h)
                n.verticalFlip = h < 0
                n.bpp = i32(b, pos + 24)
                n.blockingDegree = i32(b, pos + 52 + 0)
                info = n
            } else if (id == "descript") {
                if (l >= 2 && (b[pos].toInt() and 0xff) == 0xFF && (b[pos + 1].toInt() and 0xff) == 0xFE) {
                    desc = String(b, pos + 2, (l / 2 - 1) * 2, Charsets.UTF_16LE)
                } else {
                    desc = String(b, pos, l, Charsets.UTF_8)
                }
            }
            headerSize -= l
            pos += l
        }
        if (info == null) throw IOException("Seksi ImageInf ERI tidak ditemukan")
        info.description = desc
        return info
    }

    // Offsets ImageInf: ver(0) trans(4) arch(8) fmt(12) w(16) h(20) bpp(24) clip(28) samp(32) quant(36,8) allot(44,8)
    // blocking(52) lapped(56) frameTransform(60) frameDegree(64)

    private fun hasReferenceFile(desc: String?): Boolean {
        if (desc.isNullOrEmpty()) return false
        if (desc[0] != '#') return false
        val lines = desc.split("\n").map { it.trimEnd('\r') }
        var i = 0
        val re = Regex("^\\s*#\\s*(\\S+)")
        while (i < lines.size) {
            val m = re.find(lines[i]) ?: break
            val tag = m.groupValues[1]
            val value = StringBuilder()
            i++
            while (i < lines.size) {
                var line = lines[i]
                if (line.startsWith("#")) {
                    if (line.length < 2 || line[1] != '#') break
                    line = line.substring(1)
                }
                value.append(line).append('\n')
                i++
            }
            if (tag == "reference-file" && value.toString().trim { it <= ' ' }.isNotEmpty()) return true
        }
        return false
    }

    private fun decodeImpl(b: ByteArray): RgbaImage {
        val info = parseHeader(b)
        val w = info.width
        val h = info.height
        if (w <= 0 || h <= 0) throw IOException("Dimensi ERI tidak valid")
        if (w > MAX_DIM || h > MAX_DIM || w.toLong() * h > MAX_PIXELS) throw IOException("Dimensi ERI terlalu besar: ${w}x$h")

        // Validate architecture/transformation (per the EriReader constructor)
        when (info.architecture) {
            ARCH_NEMESIS, ARCH_HUFFMAN, ARCH_GAMMA -> {
                if (info.transformation == CV_LOSSLESS_ERI && info.blockingDegree == 0)
                    throw IOException("ERI tidak valid: blocking degree 0")
            }
            ARCH_ARITHMETIC -> {
                if (info.transformation != CV_LOSSLESS_ERI) throw IOException("ERI tidak valid: arithmetic code hanya untuk lossless")
                throw IOException("ERI arithmetic code (tipe 2/4) belum didukung (GARbro juga belum)")
            }
            else -> throw IOException("Arsitektur ERI tidak dikenal: ${info.architecture}")
        }
        val channels: Int
        when (info.formatType and TYPE_MASK) {
            TYPE_RGB -> channels = if (info.bpp <= 8) 1 else if (info.formatType and TYPE_WITH_ALPHA == 0) 3 else 4
            TYPE_GRAY -> channels = 1
            else -> throw IOException("Tipe format ERI tidak didukung: 0x" + Integer.toHexString(info.formatType))
        }
        when (info.transformation) {
            CV_LOSSLESS_ERI -> {}
            CV_DCT_ERI, CV_LOT_ERI, CV_LOT_ERI_MSS -> throw IOException("ERI lossy (DCT/LOT) belum didukung")
            CV_LOSSLESS_EMI -> throw IOException("EMI lossless (gambar bergerak) tidak didukung")
            else -> throw IOException("Kompresi ERI tidak didukung: 0x" + Integer.toHexString(info.transformation))
        }
        val bpp = info.bpp
        when (bpp) {
            32 -> if (channels == 1) throw IOException("Kombinasi bpp/kanal ERI tidak valid")
            24, 16 -> if (channels != 3) throw IOException("Kombinasi bpp/kanal ERI tidak valid")
            8 -> if (channels != 1) throw IOException("Kombinasi bpp/kanal ERI tidak valid")
            else -> throw IOException("Kedalaman bit ERI tidak didukung: $bpp")
        }
        if (bpp == 32 && channels == 4 && info.formatType != TYPE_RGBA) throw IOException("Kombinasi format RGBA ERI tidak valid")
        if (info.blockingDegree < 1 || info.blockingDegree > 8) throw IOException("Blocking degree ERI tidak didukung: ${info.blockingDegree}")
        if (hasReferenceFile(info.description)) throw IOException("ERI diferensial (reference-file) tidak didukung")

        // Find the Stream/Palette/ImageFrm sections
        var pos = info.streamPos
        var palette: IntArray? = null
        while (true) {
            if (pos + 16 > b.size) throw IOException("Seksi ImageFrm ERI tidak ditemukan")
            val id = String(b, pos, 8, Charsets.ISO_8859_1)
            val len = i64(b, pos + 8)
            pos += 16
            if (id == "Stream  ") continue
            if (id == "ImageFrm") break
            if (id == "Palette " && bpp <= 8 && len <= 0x400) {
                val colors = (len / 4).toInt()
                if (colors <= 0 || colors > 0x100 || pos + colors * 4 > b.size) throw IOException("Palet ERI tidak valid")
                val pal = IntArray(colors)
                for (i in 0 until colors) {
                    val o = pos + i * 4
                    pal[i] = (0xff shl 24) or ((b[o + 2].toInt() and 0xff) shl 16) or ((b[o + 1].toInt() and 0xff) shl 8) or (b[o].toInt() and 0xff)
                }
                palette = pal
                pos += colors * 4
                continue
            }
            if (len < 0 || pos + len > b.size) throw IOException("Seksi ERI terpotong")
            pos += len.toInt()
        }

        val ctx = Ctx(b, pos, b.size, info.architecture)
        val bytesPerLineAbs = ((w.toLong() * bpp / 8 + 3) and 3L.inv())
        if (bytesPerLineAbs * h > 0x7fffffffL) throw IOException("Gambar ERI terlalu besar")
        val out = ByteArray((bytesPerLineAbs * h).toInt())
        val lineBytes = bytesPerLineAbs.toInt()
        val dstLineBytes = if (info.verticalFlip) lineBytes else -lineBytes
        val dstStart = if (info.verticalFlip) 0 else (h - 1) * lineBytes
        decodeLossless(ctx, info, channels, out, dstStart, dstLineBytes)
        return toRgba(info, out, lineBytes, palette)
    }

    private fun toRgba(info: Info, src: ByteArray, stride: Int, palette: IntArray?): RgbaImage {
        val w = info.width
        val h = info.height
        val rgba = ByteArray(w * h * 4)
        var d = 0
        for (y in 0 until h) {
            var s = y * stride
            for (x in 0 until w) {
                when (info.bpp) {
                    32 -> {
                        rgba[d] = src[s + 2]; rgba[d + 1] = src[s + 1]; rgba[d + 2] = src[s]
                        rgba[d + 3] = if (info.formatType == TYPE_RGBA) src[s + 3] else 0xff.toByte()
                        s += 4
                    }
                    24 -> {
                        rgba[d] = src[s + 2]; rgba[d + 1] = src[s + 1]; rgba[d + 2] = src[s]
                        rgba[d + 3] = 0xff.toByte()
                        s += 3
                    }
                    16 -> {
                        val v = (src[s].toInt() and 0xff) or ((src[s + 1].toInt() and 0xff) shl 8)
                        val bl = v and 0x1F
                        val g = (v shr 5) and 0x1F
                        val r = (v shr 10) and 0x1F
                        rgba[d] = ((r shl 3) or (r shr 2)).toByte()
                        rgba[d + 1] = ((g shl 3) or (g shr 2)).toByte()
                        rgba[d + 2] = ((bl shl 3) or (bl shr 2)).toByte()
                        rgba[d + 3] = 0xff.toByte()
                        s += 2
                    }
                    else -> {
                        val v = src[s].toInt() and 0xff
                        s++
                        if (palette != null) {
                            if (v < palette.size) {
                                val c = palette[v]
                                rgba[d] = (c shr 16).toByte(); rgba[d + 1] = (c shr 8).toByte(); rgba[d + 2] = c.toByte()
                            }
                            rgba[d + 3] = 0xff.toByte()
                        } else {
                            rgba[d] = v.toByte(); rgba[d + 1] = v.toByte(); rgba[d + 2] = v.toByte(); rgba[d + 3] = 0xff.toByte()
                        }
                    }
                }
                d += 4
            }
        }
        return RgbaImage(w, h, rgba)
    }

    // ------------------------------------------------------------------ lossless decoding

    private fun decodeLossless(
        ctx: Ctx, info: Info, channels: Int, out: ByteArray, dstStart: Int, dstLineBytes: Int
    ) {
        ctx.flushBuffer()
        val nVer = ctx.getNBits(8).toInt()
        val fOpTable = ctx.getNBits(8).toInt()
        val fEncodeType = ctx.getNBits(8).toInt()
        val nBitCount = ctx.getNBits(8).toInt()
        if (fOpTable != 0 || (fEncodeType and 0xFE) != 0) throw IOException("Header data ERI tidak valid")
        when (nVer) {
            1 -> if (nBitCount != 0) throw IOException("Header data ERI tidak valid")
            2 -> throw IOException("ERI arithmetic code (tipe 2) belum didukung (GARbro juga belum)")
            4 -> throw IOException("ERI arithmetic code (tipe 4) belum didukung (GARbro juga belum)")
            8 -> if (nBitCount != 8) throw IOException("Header data ERI tidak valid")
            16 -> if (nBitCount != 8 || fEncodeType != 0) throw IOException("Header data ERI tidak valid")
            else -> throw IOException("Versi data ERI tidak dikenal: $nVer")
        }
        val bpp = info.bpp
        val pixBytes = bpp shr 3
        val bs = 1 shl info.blockingDegree
        val blockArea = 1 shl (info.blockingDegree * 2)
        val blockSamples = blockArea * channels
        val wBlocks = (info.width + bs - 1) shr info.blockingDegree
        val hBlocks = (info.height + bs - 1) shr info.blockingDegree
        val ops = IntArray(wBlocks * hBlocks)
        val columnBuf = ByteArray(bs * channels)
        val lineBuf = ByteArray(channels * (wBlocks shl info.blockingDegree))
        val decodeBuf = ByteArray(blockSamples)
        val arrangeBuf = ByteArray(blockSamples)
        val table = arrangeTable(bs, blockArea, blockSamples, channels)
        val arch = info.architecture

        var huff: HTree? = null
        var prob: Prob? = null
        if (arch == ARCH_HUFFMAN) huff = HTree()
        if (arch == ARCH_NEMESIS) { prob = Prob(); prob.init() }

        var opIdx = 0
        if ((fEncodeType and 1) != 0 && channels >= 3) {
            if (arch == ARCH_NEMESIS) throw IOException("Kombinasi Nemesis dengan tabel operasi tidak valid")
            for (i in ops.indices) {
                ops[i] = if (arch == ARCH_GAMMA) (ctx.getNBits(4).toInt() or 0xC0) else opHuff(ctx, huff!!)
            }
        }
        if (ctx.getABit() != 0) throw IOException("Data ERI tidak valid (bit pembuka)")
        if (arch == ARCH_GAMMA) {
            if ((fEncodeType and 1) != 0) ctx.initGamma()
        } else if (arch == ARCH_HUFFMAN) {
            ctx.prepareHuffman()
        } else {
            ctx.prepareErisa()
        }

        val allBlockLines = bs * channels
        var leftHeight = info.height
        val gray = info.formatType == TYPE_GRAY
        for (posY in 0 until hBlocks) {
            java.util.Arrays.fill(columnBuf, 0, bs * channels, 0)
            val dstBlockRow = dstStart + posY * dstLineBytes * bs
            val dstH = minOf(bs, leftHeight)
            var leftWidth = info.width
            var lineIdx = 0
            for (posX in 0 until wBlocks) {
                val dstW = minOf(bs, leftWidth)
                val opCode: Int
                if (channels >= 3) {
                    if ((fEncodeType and 1) != 0) {
                        opCode = ops[opIdx++]
                    } else if (arch == ARCH_HUFFMAN) {
                        opCode = opHuff(ctx, huff!!)
                    } else if (arch == ARCH_NEMESIS) {
                        opCode = ctx.erisaCode(prob!!).also { if (it < 0) throw IOException("Data ERI terpotong atau rusak") } and 0xFF
                    } else {
                        opCode = ctx.getNBits(4).toInt() or 0xC0
                        ctx.initGamma()
                    }
                } else {
                    opCode = if (gray) 0xC0 else 0
                    if ((fEncodeType and 1) == 0 && arch == ARCH_GAMMA) ctx.initGamma()
                }
                if (ctx.decodeBytes(arrangeBuf, blockSamples) < blockSamples) throw IOException("Data ERI terpotong atau rusak")
                performOperation(
                    opCode, allBlockLines, lineBuf, lineIdx, bs, blockArea, blockSamples, channels,
                    arrangeBuf, decodeBuf, columnBuf, table
                )
                lineIdx += bs * channels
                restore(decodeBuf, out, dstBlockRow + posX * bs * pixBytes, dstLineBytes, dstW, dstH, bs, blockArea, bpp, channels, info.formatType)
                leftWidth -= bs
            }
            leftHeight -= bs
        }
    }

    private fun opHuff(ctx: Ctx, t: HTree): Int {
        val c = ctx.huffmanCode(t)
        if (c == H_ESCAPE) throw IOException("Data ERI terpotong atau rusak")
        return c and 0xFF
    }

    private fun arrangeTable(bs: Int, blockArea: Int, blockSamples: Int, channels: Int): IntArray {
        val t = IntArray(blockSamples * 4)
        var p = 0
        for (i in 0 until blockSamples) t[i] = i
        p = blockSamples
        var l = 0
        for (i in 0 until channels) {
            for (j in 0 until bs) {
                var m = l + j
                for (k in 0 until bs) {
                    t[p++] = m
                    m += bs
                }
            }
            l += blockArea
        }
        p = blockSamples * 2
        for (i in 0 until blockArea) {
            var k = i
            for (j in 0 until channels) {
                t[p++] = k
                k += blockArea
            }
        }
        p = blockSamples * 3
        for (i in 0 until bs) {
            var l2 = i
            for (j in 0 until bs) {
                var m = l2
                l2 += bs
                for (k in 0 until channels) {
                    t[p++] = m
                    m += blockArea
                }
            }
        }
        return t
    }

    private fun performOperation(
        opCode: Int, allBlockLines: Int, lineBuf: ByteArray, lineIdx0: Int, bs: Int, blockArea: Int,
        blockSamples: Int, channels: Int, arrangeBuf: ByteArray, decodeBuf: ByteArray, columnBuf: ByteArray,
        table: IntArray
    ) {
        val colorOp = opCode and 0x0F
        val arrangeCode = (opCode ushr 4) and 0x03
        val diffOp = (opCode ushr 6) and 0x03
        if (arrangeCode == 0) {
            System.arraycopy(arrangeBuf, 0, decodeBuf, 0, blockSamples)
            if (opCode == 0) return
        } else {
            val pa = arrangeCode * blockSamples
            for (i in 0 until blockSamples) decodeBuf[table[pa + i]] = arrangeBuf[i]
        }
        colorOperation(colorOp, decodeBuf, blockArea)

        var pb = 0
        var pc = 0
        if ((diffOp and 1) != 0) {
            for (i in 0 until allBlockLines) {
                var last = columnBuf[pc].toInt()
                for (j in 0 until bs) {
                    last = (last + decodeBuf[pb]).toByte().toInt()
                    decodeBuf[pb++] = last.toByte()
                }
                columnBuf[pc++] = last.toByte()
            }
        } else {
            for (i in 0 until allBlockLines) {
                columnBuf[pc++] = decodeBuf[pb + bs - 1]
                pb += bs
            }
        }
        var next = 0
        var lineIdx = lineIdx0
        for (k in 0 until channels) {
            var lastArr = lineBuf
            var lastIdx = lineIdx
            for (i in 0 until bs) {
                for (j in 0 until bs) {
                    decodeBuf[next + j] = (decodeBuf[next + j] + lastArr[lastIdx + j]).toByte()
                }
                lastArr = decodeBuf
                lastIdx = next
                next += bs
            }
            System.arraycopy(lastArr, lastIdx, lineBuf, lineIdx, bs)
            lineIdx += bs
        }
    }

    private fun addTo(buf: ByteArray, dstOff: Int, srcOff: Int, n: Int) {
        for (i in 0 until n) buf[dstOff + i] = (buf[dstOff + i] + buf[srcOff + i]).toByte()
    }

    private fun colorOperation(op: Int, buf: ByteArray, a: Int) {
        when (op) {
            0x5 -> addTo(buf, a, 0, a)
            0x6 -> addTo(buf, 2 * a, 0, a)
            0x7 -> { addTo(buf, a, 0, a); addTo(buf, 2 * a, 0, a) }
            0x9 -> addTo(buf, 0, a, a)
            0xA -> addTo(buf, 2 * a, a, a)
            0xB -> { addTo(buf, 0, a, a); addTo(buf, 2 * a, a, a) }
            0xD -> addTo(buf, 0, 2 * a, a)
            0xE -> addTo(buf, a, 2 * a, a)
            0xF -> { addTo(buf, 0, 2 * a, a); addTo(buf, a, 2 * a, a) }
            else -> {}
        }
    }

    private fun restore(
        dec: ByteArray, out: ByteArray, dstBlock: Int, lineBytes: Int, dstW: Int, dstH: Int, bs: Int,
        blockArea: Int, bpp: Int, channels: Int, formatType: Int
    ) {
        var dstLine = dstBlock
        var srcLine = 0
        for (y in 0 until dstH) {
            var d = dstLine
            var s = srcLine
            when (bpp) {
                32 -> if (formatType == TYPE_RGBA) {
                    for (x in 0 until dstW) {
                        out[d] = dec[s]; out[d + 1] = dec[s + blockArea]
                        out[d + 2] = dec[s + blockArea * 2]; out[d + 3] = dec[s + blockArea * 3]
                        s++; d += 4
                    }
                } else {
                    for (x in 0 until dstW) {
                        out[d] = dec[s]; out[d + 1] = dec[s + blockArea]; out[d + 2] = dec[s + blockArea * 2]
                        s++; d += 4
                    }
                }
                24 -> for (x in 0 until dstW) {
                    out[d] = dec[s]; out[d + 1] = dec[s + blockArea]; out[d + 2] = dec[s + blockArea * 2]
                    s++; d += 3
                }
                16 -> for (x in 0 until dstW) {
                    val word = (dec[s].toInt() and 0x1F) or ((dec[s + blockArea].toInt() and 0x1F) shl 5) or
                        ((dec[s + blockArea * 2].toInt() and 0x1F) shl 10)
                    out[d] = word.toByte(); out[d + 1] = (word shr 8).toByte()
                    s++; d += 2
                }
                else -> System.arraycopy(dec, srcLine, out, dstLine, dstW)
            }
            srcLine += bs
            dstLine += lineBytes
        }
    }

    // ------------------------------------------------------------------ ERISA probability model (order-1)

    private const val TOTAL_LIMIT = 0x2000
    private const val SYM_MAX = 0x101
    private const val ESC = -1

    private class Prob {
        var total = 0
        var sorts = 0
        val occ = IntArray(SYM_MAX)
        val sym = IntArray(SYM_MAX)

        fun init() {
            total = SYM_MAX; sorts = SYM_MAX
            for (i in 0 until 0x100) { occ[i] = 1; sym[i] = i }
            occ[0x100] = 1; sym[0x100] = ESC
        }

        fun half() {
            total = 0
            for (i in 0 until sorts) { occ[i] = (occ[i] + 1) shr 1; total += occ[i] }
        }

        fun increase(index0: Int): Int {
            var index = index0
            val o = ++occ[index]
            val s = sym[index]
            while (--index >= 0) {
                if (occ[index] >= o) break
                occ[index + 1] = occ[index]; sym[index + 1] = sym[index]
            }
            ++index
            occ[index] = o; sym[index] = s
            if (++total >= TOTAL_LIMIT) half()
            return index
        }
    }

    // ------------------------------------------------------------------ adaptive Huffman tree (ERINA)

    private const val H_CODE_FLAG = Int.MIN_VALUE
    private const val H_ESCAPE = 0x7FFFFFFF
    private const val H_NULL = 0x8000
    private const val H_EOF = -2
    private const val H_MAX = 0x4000
    private const val H_ROOT = 0x200

    private class HTree {
        val w = IntArray(0x201)
        val par = IntArray(0x201)
        val ch = IntArray(0x201)
        val symLookup = IntArray(0x100) { H_NULL }
        var escape = H_NULL
        var treePtr = H_ROOT

        init {
            w[H_ROOT] = 0
            par[H_ROOT] = H_NULL
            ch[H_ROOT] = H_NULL
        }

        fun increase(entry: Int) {
            w[entry] = (w[entry] + 1) and 0xFFFF
            normalize(entry)
            if (w[H_ROOT] >= H_MAX) halfAndRebuild()
        }

        private fun recount(p: Int) {
            val c = ch[p]
            w[p] = (w[c] + w[c + 1]) and 0xFFFF
        }

        private fun fixRef(code: Int, pos: Int) {
            val n = code and H_CODE_FLAG.inv()
            if (n != H_ESCAPE) symLookup[n and 0xFF] = pos else escape = pos
        }

        private fun normalize(entry0: Int) {
            var entry = entry0
            while (entry < H_ROOT) {
                var swap = entry + 1
                val weight = w[entry]
                while (swap < H_ROOT) {
                    if (w[swap] >= weight) break
                    ++swap
                }
                if (entry == --swap) {
                    entry = par[entry]
                    recount(entry)
                    continue
                }
                if ((ch[entry] and H_CODE_FLAG) == 0) {
                    val c = ch[entry]
                    par[c] = swap; par[c + 1] = swap
                } else fixRef(ch[entry], swap)
                if ((ch[swap] and H_CODE_FLAG) == 0) {
                    val c = ch[swap]
                    par[c] = entry; par[c + 1] = entry
                } else fixRef(ch[swap], entry)
                val tw = w[swap]
                val tc = ch[swap]
                val swapParent = par[swap]
                w[swap] = w[entry]; ch[swap] = ch[entry]
                w[entry] = tw; ch[entry] = tc
                recount(swapParent)
                entry = swapParent
            }
        }

        fun addNewEntry(newCode: Int) {
            if (treePtr > 0) {
                treePtr -= 2
                val i = treePtr
                w[i] = 1
                ch[i] = H_CODE_FLAG or newCode
                symLookup[newCode and 0xFF] = i
                if (ch[H_ROOT] != H_NULL) {
                    // child i+1 <- copy of i+2
                    w[i + 1] = w[i + 2]; par[i + 1] = par[i + 2]; ch[i + 1] = ch[i + 2]
                    if ((ch[i + 1] and H_CODE_FLAG) != 0) fixRef(ch[i + 1], i + 1)
                    w[i + 2] = (w[i] + w[i + 1]) and 0xFFFF
                    par[i + 2] = par[i + 1]
                    ch[i + 2] = i
                    par[i] = i + 2
                    par[i + 1] = i + 2
                    normalize(i + 2)
                } else {
                    par[i] = H_ROOT
                    escape = i + 1
                    w[escape] = 1
                    par[escape] = H_ROOT
                    ch[escape] = H_CODE_FLAG or H_ESCAPE
                    w[H_ROOT] = 2
                    ch[H_ROOT] = i
                }
            } else {
                var i = treePtr
                if (ch[i] == (H_CODE_FLAG or H_ESCAPE)) i += 1
                ch[i] = H_CODE_FLAG or newCode
            }
        }

        private fun copy(dst: Int, src: Int) {
            w[dst] = w[src]; par[dst] = par[src]; ch[dst] = ch[src]
        }

        private fun halfAndRebuild() {
            var nextEntry = H_ROOT
            var i = H_ROOT - 1
            while (i >= treePtr) {
                if ((ch[i] and H_CODE_FLAG) != 0) {
                    w[i] = ((w[i] + 1) shr 1) and 0xFFFF
                    copy(nextEntry--, i)
                }
                i--
            }
            ++nextEntry
            i = treePtr
            while (true) {
                copy(i, nextEntry)
                copy(i + 1, nextEntry + 1)
                nextEntry += 2
                if ((ch[i] and H_CODE_FLAG) == 0) {
                    val c = ch[i]
                    par[c] = i; par[c + 1] = i
                } else fixRef(ch[i], i)
                if ((ch[i + 1] and H_CODE_FLAG) == 0) {
                    val c = ch[i + 1]
                    par[c] = i + 1; par[c + 1] = i + 1
                } else fixRef(ch[i + 1], i + 1)
                val weight = (w[i] + w[i + 1]) and 0xFFFF
                if (nextEntry <= H_ROOT) {
                    var j = nextEntry
                    while (true) {
                        if (weight <= w[j]) {
                            w[j - 1] = weight
                            ch[j - 1] = i
                            break
                        }
                        copy(j - 1, j)
                        if (++j > H_ROOT) {
                            w[H_ROOT] = weight
                            ch[H_ROOT] = i
                            break
                        }
                    }
                    --nextEntry
                } else {
                    w[H_ROOT] = weight
                    par[H_ROOT] = H_NULL
                    ch[H_ROOT] = i
                    par[i] = H_ROOT
                    par[i + 1] = H_ROOT
                    break
                }
                i += 2
            }
        }
    }

    // ------------------------------------------------------------------ decoding context (bit reader + RLE/Huffman/ERISA)

    private class Ctx(private val d: ByteArray, private var pos: Int, private val end: Int, private val arch: Int) {
        private var intCount = 0
        private var intBuf = 0

        // RLE / gamma
        private var flgZero = 0
        private var nLength = 0

        // Huffman
        private var trees: Array<HTree?> = arrayOfNulls(0x101)
        private var lastTree: HTree? = null

        // ERISA
        private var codeReg = 0L
        private var augend = 0L
        private var postBits = 0
        private var table: Array<Prob?> = arrayOfNulls(0x101)
        private var lastProb: Prob? = null
        private val runLenProb = Prob()
        private val phraseLenProb = Prob()
        private val phraseIdxProb = Prob()

        private fun prefetch(): Boolean {
            if (intCount == 0) {
                if (pos >= end) return false
                var v = 0
                for (k in 0 until 4) v = (v shl 8) or (if (pos + k < end) d[pos + k].toInt() and 0xff else 0)
                pos += 4
                intBuf = v
                intCount = 32
            }
            return true
        }

        fun flushBuffer() { intCount = 0 }

        fun getABit(): Int {
            if (!prefetch()) return 1
            val v = intBuf shr 31
            intCount--
            intBuf = intBuf shl 1
            return v
        }

        fun getNBits(n0: Int): Long {
            var n = n0
            var code = 0L
            while (n != 0) {
                if (!prefetch()) break
                val c = minOf(n, intCount)
                code = ((code shl c) or ((intBuf.toLong() and 0xffffffffL) ushr (32 - c))) and 0xffffffffL
                n -= c
                intCount -= c
                intBuf = if (c >= 32) 0 else intBuf shl c
            }
            return code
        }

        fun initGamma() {
            flgZero = getABit()
            nLength = 0
        }

        private fun gamma(): Int {
            if (!prefetch()) return 0
            intCount--
            val first = intBuf
            intBuf = intBuf shl 1
            if (first and Int.MIN_VALUE == 0) return 1
            if (!prefetch()) return 0
            var code = 0
            var base = 2
            while (true) {
                if (!prefetch()) return 0
                val dbit = intBuf ushr 31
                intCount--; intBuf = intBuf shl 1
                code = (code shl 1) or dbit
                if (!prefetch()) return 0
                val cont = intBuf ushr 31
                intCount--; intBuf = intBuf shl 1
                if (cont == 0) return code + base
                base = base shl 1
            }
        }

        private fun gammaLen(): Int {
            val g = gamma()
            return if (g < 0) 0 else g
        }

        fun decodeBytes(dst: ByteArray, count: Int): Int = when (arch) {
            ARCH_GAMMA -> gammaBytes(dst, count)
            ARCH_HUFFMAN -> erinaBytes(dst, count)
            else -> erisaBytes(dst, count)
        }

        private fun gammaBytes(dst: ByteArray, count0: Int): Int {
            var count = count0
            var di = 0
            var decoded = 0
            if (nLength == 0) {
                nLength = gammaLen()
                if (nLength == 0) return decoded
            }
            while (true) {
                var nRepeat = minOf(nLength, count)
                nLength -= nRepeat
                count -= nRepeat
                if (flgZero == 0) {
                    decoded += nRepeat
                    while (nRepeat-- > 0) dst[di++] = 0
                } else {
                    while (nRepeat-- > 0) {
                        val sign = getABit().toByte().toInt()
                        val code = gamma().toByte().toInt()
                        if (code == 0) return decoded
                        decoded++
                        dst[di++] = ((code xor sign) - sign).toByte()
                    }
                }
                if (count == 0) {
                    if (nLength == 0) flgZero = flgZero.inv()
                    return decoded
                }
                flgZero = flgZero.inv()
                nLength = gammaLen()
                if (nLength == 0) return decoded
            }
        }

        // ---------------- Huffman

        fun prepareHuffman() {
            nLength = 0
            for (i in 0 until 0x101) trees[i] = HTree()
            lastTree = trees[0]
        }

        private fun huffmanWalk(tree: HTree): Int {
            // returns the symbol code, H_ESCAPE for escape, or H_EOF when data runs out
            var entry: Int
            var child = tree.ch[H_ROOT]
            do {
                if (!prefetch()) return H_EOF
                entry = child + (intBuf ushr 31)
                child = tree.ch[entry]
                intBuf = intBuf shl 1
                --intCount
            } while ((child and H_CODE_FLAG) == 0)
            tree.increase(entry)
            return child and H_CODE_FLAG.inv()
        }

        fun huffmanCode(tree: HTree): Int {
            if (tree.escape != H_NULL) {
                val c = huffmanWalk(tree)
                if (c == H_EOF) return H_ESCAPE
                if (c != H_ESCAPE) return c
                // real escape: keep reading 8 bits
            }
            val code = getNBits(8).toInt()
            tree.addNewEntry(code)
            return code
        }

        private fun huffmanLength(tree: HTree): Int {
            if (tree.escape != H_NULL) {
                val c = huffmanWalk(tree)
                if (c == H_EOF) return H_ESCAPE
                if (c != H_ESCAPE) return c
            }
            val code = gamma()
            if (code == -1) return H_ESCAPE
            tree.addNewEntry(code)
            return code
        }

        private fun erinaBytes(dst: ByteArray, count: Int): Int {
            var tree = lastTree!!
            var i = 0
            if (nLength > 0) {
                var length = minOf(nLength, count)
                nLength -= length
                while (length-- > 0) dst[i++] = 0
            }
            while (i < count) {
                val symbol = huffmanCode(tree)
                if (symbol == H_ESCAPE) break
                dst[i++] = symbol.toByte()
                if (symbol == 0) {
                    var length = huffmanLength(trees[0x100]!!)
                    if (length == H_ESCAPE) break
                    if (--length != 0) {
                        nLength = length
                        if (i + length > count) length = count - i
                        nLength -= length
                        while (length-- > 0) dst[i++] = 0
                    }
                }
                tree = trees[symbol and 0xFF]!!
            }
            lastTree = tree
            return i
        }

        // ---------------- ERISA (arithmetic)

        fun prepareErisa() {
            for (i in 0 until 0x101) {
                val p = Prob()
                p.init()
                table[i] = p
            }
            lastProb = table[0]
            runLenProb.init(); phraseLenProb.init(); phraseIdxProb.init()
            nLength = 0
            codeReg = getNBits(32)
            augend = 0xFFFF
            postBits = 0
        }

        private fun erisaIndex(m: Prob): Int {
            if (augend == 0L) return -1
            val acc = ((codeReg * m.total) and 0xffffffffL) / augend
            if (acc >= TOTAL_LIMIT) return -1
            var iSym = 0
            var wAcc = (acc and 0xffff).toInt()
            var wFs = 0
            var occured: Int
            while (true) {
                occured = m.occ[iSym]
                if (wAcc < occured) break
                wAcc -= occured
                wFs += occured
                if (++iSym >= m.sorts) return -1
            }
            codeReg = (codeReg - (((augend * wFs + m.total - 1) and 0xffffffffL) / m.total)) and 0xffffffffL
            augend = ((augend * occured) and 0xffffffffL) / m.total
            while (augend and 0x8000L == 0L) {
                var nb = getABit()
                if (nb == 1) {
                    if (++postBits >= 256) return -1
                    nb = 0
                }
                codeReg = ((codeReg shl 1) or (nb.toLong() and 1L)) and 0xffffffffL
                augend = (augend shl 1) and 0xffffffffL
            }
            codeReg = codeReg and 0xFFFF
            return iSym
        }

        fun erisaCode(m: Prob): Int {
            val i = erisaIndex(m)
            if (i < 0) return ESC
            val s = m.sym[i]
            m.increase(i)
            return s
        }

        private fun erisaBytes(dst: ByteArray, count: Int): Int {
            var prob = lastProb!!
            var i = 0
            while (i < count) {
                if (nLength > 0) {
                    val cur = minOf(count - i, nLength)
                    nLength -= cur
                    for (j in 0 until cur) dst[i++] = 0
                    continue
                }
                val iSym = erisaIndex(prob)
                if (iSym < 0) break
                val symbol = prob.sym[iSym]
                prob.increase(iSym)
                dst[i++] = symbol.toByte()
                if (symbol == 0) {
                    val r = erisaIndex(runLenProb)
                    if (r < 0) break
                    val run = runLenProb.sym[r]
                    runLenProb.increase(r)
                    if (run < 0) break
                    nLength = run
                }
                prob = table[symbol and 0xFF]!!
            }
            lastProb = prob
            return i
        }
    }
}
