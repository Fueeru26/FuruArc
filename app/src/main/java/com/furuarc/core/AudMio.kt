package com.furuarc.core

import java.io.IOException

/**
 * Entis "MIO" (Music Interleaved) audio. Port of GARbro ArcFormats/Entis/AudioMIO.cs + MioDecoder.cs + EriReader.cs
 * (ERINA Huffman/gamma context) + ErisaMatrix.cs (DCT/LOT), and, for the ERISA/Nemesis audio architecture, from vgmstream
 * (mio_erisacontext.c; GARbro itself throws NotImplemented for that case).
 *
 * Covered:
 *  - Lossless_ERI (0x03020000), RunlengthHuffman architecture, 8/16-bit PCM (delta prediction).
 *  - LOT_ERI (5) and LOT_ERI_MSS (0x105, stereo mid/side), RunlengthHuffman/RunlengthGamma architecture
 *    (both read with the ERINA Huffman context, exactly like GARbro/vgmstream) and Nemesis (ERISA arithmetic code,
 *    vgmstream port; not yet tested with real samples).
 * Not covered (clear IOException):
 *  - EMSAC-Sound-2 (.ems): the GARbro source AudioEMS.cs is only an incomplete decompilation result (cannot be compiled).
 *  - The .emi extension in GARbro is an Entis IMAGE (EMSAC-Image / ERI), not audio.
 * Note: the Nemesis NOA decoder in EntisCodec.kt (ERISA-N with a 64 KB phrase window) is a different algorithm from the
 * ERISA code of MIO audio, so it is not used here.
 * Use of .mio in Kirikiri: no reference source states another MIO variant; only the Entis format above is
 * supported. [oggOrNull] only recognizes plain Ogg / single-byte XOR-ed Ogg given the .mio extension.
 */
object MioAudio {
    private const val MAX_OUT = 600L * 1024 * 1024
    private const val CV_LOSSLESS = 0x03020000
    private const val CV_LOT = 0x00000005
    private const val CV_LOT_MSS = 0x00000105
    private const val ARCH_GAMMA = -1
    private const val ARCH_HUFFMAN = -4
    private const val ARCH_NEMESIS = -16
    private const val MAX_DCT_DEGREE = 12
    private const val LEAD_BLOCK = 1

    private fun u32(b: ByteArray, o: Int): Long =
        (b[o].toLong() and 0xff) or ((b[o + 1].toLong() and 0xff) shl 8) or
            ((b[o + 2].toLong() and 0xff) shl 16) or ((b[o + 3].toLong() and 0xff) shl 24)

    private fun i32(b: ByteArray, o: Int): Int = u32(b, o).toInt()

    private fun ascii(b: ByteArray, o: Int, n: Int): String {
        val sb = StringBuilder()
        var i = o
        while (i < o + n && i < b.size) {
            val c = b[i].toInt() and 0xff
            if (c == 0) break
            sb.append(if (c in 32..126) c.toChar() else '?')
            i++
        }
        return sb.toString()
    }

    private fun startsWithAscii(b: ByteArray, o: Int, s: String): Boolean {
        if (o + s.length > b.size) return false
        for (i in s.indices) if ((b[o + i].toInt() and 0xff) != s[i].code) return false
        return true
    }

    /** MIO signature: 'Enti' at 0, 0x03000100 at 8, "Music Interleaved" at 0x10 (needs >= 0x21 bytes). */
    fun isMio(head: ByteArray): Boolean =
        head.size >= 0x21 && startsWithAscii(head, 0, "Enti") && i32(head, 8) == 0x03000100 &&
            startsWithAscii(head, 0x10, "Music Interleaved")

    /** If the actual content is Ogg (plain or single-byte XOR-ed), return the complete Ogg; otherwise null. */
    fun oggOrNull(b: ByteArray): ByteArray? {
        if (b.size < 4) return null
        val k = (b[0].toInt() and 0xff) xor 'O'.code
        if (((b[1].toInt() and 0xff) xor k) != 'g'.code || ((b[2].toInt() and 0xff) xor k) != 'g'.code ||
            ((b[3].toInt() and 0xff) xor k) != 'S'.code
        ) return null
        if (k == 0) return b
        return ByteArray(b.size) { i -> ((b[i].toInt() and 0xff) xor k).toByte() }
    }

    // ------------------------------------------------------------------ container parsing

    private class Info(
        val version: Int, val transformation: Int, val architecture: Int, val channels: Int, val rate: Int,
        val blockset: Long, val subband: Int, val allSamples: Long, val lapped: Long, val bps: Int
    )

    private class Chunk(val flags: Int, val samples: Int, val pos: Int, val size: Int)

    private class Sec(val id: String, val len: Long, val dataPos: Int)

    private fun readSec(b: ByteArray, p: Int): Sec? {
        if (p < 0 || p + 16 > b.size) return null
        val len = (u32(b, p + 8)) or (u32(b, p + 12) shl 32)
        return Sec(ascii(b, p, 8).padEnd(8), len, p + 16)
    }

    /** Find the section named [name] starting from [p]; return the section or null on EOF (like FindSection). */
    private fun findSec(b: ByteArray, p0: Int, name: String): Sec? {
        var p = p0.toLong()
        while (true) {
            if (p > Int.MAX_VALUE) return null
            val s = readSec(b, p.toInt()) ?: return null
            if (s.len < 0) return null
            if (s.id == name) return s
            p = s.dataPos.toLong() + s.len
        }
    }

    fun decode(b: ByteArray): PcmAudio {
        if (b.size < 0x40 || !startsWithAscii(b, 0, "Enti")) throw IOException("Bukan file Entis (.mio/.emi): tanda tangan 'Entis' tidak ditemukan")
        val sig = ascii(b, 0x10, 0x30)
        if (isMio(b)) return decodeMio(b)
        if (sig.startsWith("EMSAC-Sound")) {
            throw IOException("EMSAC Sound (.ems) belum didukung: sumber referensi GARbro (AudioEMS.cs) tidak lengkap sehingga tidak dapat diport dengan setia")
        }
        if (sig.startsWith("EMSAC-Image") || sig.startsWith("Entis Rasterized Image")) {
            throw IOException("File ini adalah gambar Entis (ERI/EMI: '$sig'), bukan audio; gunakan pengubah gambar")
        }
        throw IOException("Tipe file Entis tidak dikenal untuk audio: '$sig'")
    }

    private fun parseInfo(b: ByteArray): Pair<Info, Int> {
        val h = readSec(b, 0x40) ?: throw IOException("Header MIO terpotong")
        if (h.id != "Header  " || h.len <= 0 || h.len > Int.MAX_VALUE) throw IOException("Header MIO tidak valid")
        val streamPos = 0x50L + h.len
        var headerSize = h.len
        var p = 0x50L
        var info: Info? = null
        while (headerSize > 0x10) {
            val s = readSec(b, p.toInt()) ?: break
            p += 16
            headerSize -= 0x10
            if (s.len <= 0 || s.len > headerSize) break
            if (s.id == "SoundInf") {
                val o = p.toInt()
                if (s.len < 40 || o + 40 > b.size) throw IOException("Seksi SoundInf MIO terpotong")
                info = Info(
                    i32(b, o), i32(b, o + 4), i32(b, o + 8), i32(b, o + 12), i32(b, o + 16),
                    u32(b, o + 20), i32(b, o + 24), u32(b, o + 28), u32(b, o + 32), i32(b, o + 36)
                )
                break
            }
            headerSize -= s.len
            p += s.len
        }
        if (info == null) throw IOException("Header suara MIO (SoundInf) tidak ditemukan")
        if (streamPos > Int.MAX_VALUE || streamPos >= b.size) throw IOException("Seksi Stream MIO tidak ditemukan")
        val st = findSec(b, streamPos.toInt(), "Stream  ") ?: throw IOException("Seksi 'Stream' MIO tidak ditemukan")
        return Pair(info, st.dataPos)
    }

    private fun decodeMio(b: ByteArray): PcmAudio {
        val (info, streamStart) = parseInfo(b)
        // SoundStm chunk list (sequential reading like LoadChunks)
        val chunks = ArrayList<Chunk>()
        var total = 0L
        var p = streamStart
        while (true) {
            val s = findSec(b, p, "SoundStm") ?: break
            if (s.len < 8) throw IOException("Chunk SoundStm MIO rusak (panjang ${s.len})")
            val hp = s.dataPos
            if (hp + 8 > b.size) break
            val flags = b[hp + 1].toInt() and 0xff
            val cnt = u32(b, hp + 4)
            if (cnt > Int.MAX_VALUE) throw IOException("Jumlah sampel chunk MIO tidak masuk akal")
            val dataPos = hp + 8
            val size = minOf(s.len - 8, (b.size - dataPos).toLong()).toInt()
            chunks.add(Chunk(flags, cnt.toInt(), dataPos, size))
            total += cnt
            if (total * info.channels * 2 > MAX_OUT) throw IOException("Keluaran MIO melebihi 600 MB")
            val next = s.dataPos.toLong() + s.len
            if (next >= b.size) break
            p = next.toInt()
        }
        if (total == 0L) throw IOException("MIO tidak berisi sampel audio")
        val dec = try { Decoder(info) } catch (e: IOException) { throw e }
        val ch = info.channels
        val out = ByteArray((total * ch * 2).toInt())
        var wp = 0
        val ctx = Ctx()
        try {
            for ((ci, c) in chunks.withIndex()) {
                val bytesPer = info.bps / 8
                val wave = ByteArray(c.samples * ch * bytesPer)
                ctx.attach(b, c.pos, c.pos + c.size)
                if (!dec.decodeSound(ctx, c, wave)) throw IOException("Gagal mendekode chunk MIO #$ci (data rusak atau tidak didukung)")
                if (info.bps == 16) {
                    System.arraycopy(wave, 0, out, wp, wave.size)
                    wp += wave.size
                } else {
                    for (v in wave) {
                        val s = ((v.toInt() and 0xff) - 128) shl 8
                        out[wp++] = s.toByte(); out[wp++] = (s shr 8).toByte()
                    }
                }
            }
        } catch (e: IOException) {
            throw e
        } catch (e: OutOfMemoryError) {
            throw IOException("Memori tidak cukup untuk mendekode MIO")
        } catch (e: RuntimeException) {
            throw IOException("Data MIO rusak: ${e.javaClass.simpleName}")
        }
        return PcmAudio(info.rate, ch, out)
    }

    // ------------------------------------------------------------------ bit context + ERINA Huffman + ERISA

    private const val H_ESCAPE = 0x7FFFFFFF
    private const val H_NULL = 0x8000
    private const val H_MAX = 0x4000
    private const val H_ROOT = 0x200
    private const val CODE_FLAG = Int.MIN_VALUE

    private class HNode {
        var weight = 0
        var parent = 0
        var child = 0
        fun copyFrom(o: HNode) { weight = o.weight; parent = o.parent; child = o.child }
    }

    private class HTree {
        val t = Array(0x201) { HNode() }
        val symLookup = IntArray(0x100) { H_NULL }
        var escape = H_NULL
        var treePointer = H_ROOT

        init {
            t[H_ROOT].weight = 0
            t[H_ROOT].parent = H_NULL
            t[H_ROOT].child = H_NULL
        }

        fun increase(entry: Int) {
            t[entry].weight = (t[entry].weight + 1) and 0xFFFF
            normalize(entry)
            if (t[H_ROOT].weight >= H_MAX) halfAndRebuild()
        }

        private fun recount(parent: Int) {
            val c = t[parent].child
            t[parent].weight = (t[c].weight + t[c + 1].weight) and 0xFFFF
        }

        private fun fixLink(node: HNode, newIndex: Int) {
            if (node.child and CODE_FLAG == 0) {
                val c = node.child
                t[c].parent = newIndex
                t[c + 1].parent = newIndex
            } else {
                val code = node.child and CODE_FLAG.inv()
                if (code != H_ESCAPE) symLookup[code and 0xFF] = newIndex else escape = newIndex
            }
        }

        private fun normalize(entry0: Int) {
            var iEntry = entry0
            while (iEntry < H_ROOT) {
                var iSwap = iEntry + 1
                val weight = t[iEntry].weight
                while (iSwap < H_ROOT) {
                    if (t[iSwap].weight >= weight) break
                    ++iSwap
                }
                --iSwap
                if (iEntry == iSwap) {
                    iEntry = t[iEntry].parent
                    recount(iEntry)
                    continue
                }
                fixLink(t[iEntry], iSwap)
                fixLink(t[iSwap], iEntry)
                val node = t[iSwap]
                val entryParent = t[iEntry].parent
                val swapParent = t[iSwap].parent
                t[iSwap] = t[iEntry]
                t[iEntry] = node
                t[iSwap].parent = swapParent
                t[iEntry].parent = entryParent
                recount(swapParent)
                iEntry = swapParent
            }
        }

        fun addNew(code: Int) {
            if (treePointer > 0) {
                treePointer -= 2
                val i = treePointer
                val nw = t[i]
                nw.weight = 1
                nw.child = CODE_FLAG or code
                symLookup[code and 0xFF] = i
                val root = t[H_ROOT]
                if (root.child != H_NULL) {
                    val parent = t[i + 2]
                    val child = t[i + 1]
                    child.copyFrom(parent)
                    if (child.child and CODE_FLAG != 0) {
                        val c = child.child and CODE_FLAG.inv()
                        if (c != H_ESCAPE) symLookup[c and 0xFF] = i + 1 else escape = i + 1
                    }
                    parent.weight = (nw.weight + child.weight) and 0xFFFF
                    parent.parent = child.parent
                    parent.child = i
                    nw.parent = i + 2
                    child.parent = i + 2
                    normalize(i + 2)
                } else {
                    nw.parent = H_ROOT
                    escape = i + 1
                    val esc = t[escape]
                    esc.weight = 1
                    esc.parent = H_ROOT
                    esc.child = CODE_FLAG or H_ESCAPE
                    root.weight = 2
                    root.child = i
                }
            } else {
                val i = treePointer
                var e = t[i]
                if (e.child == (CODE_FLAG or H_ESCAPE)) e = t[i + 1]
                e.child = CODE_FLAG or code
            }
        }

        private fun halfAndRebuild() {
            var iNext = H_ROOT
            var i = H_ROOT - 1
            while (i >= treePointer) {
                if (t[i].child and CODE_FLAG != 0) {
                    t[i].weight = ((t[i].weight + 1) shr 1) and 0xFFFF
                    t[iNext--].copyFrom(t[i])
                }
                i--
            }
            ++iNext
            i = treePointer
            while (true) {
                t[i].copyFrom(t[iNext])
                t[i + 1].copyFrom(t[iNext + 1])
                iNext += 2
                val c1 = t[i]
                val c2 = t[i + 1]
                fixLink(c1, i)
                fixLink(c2, i + 1)
                val weight = (c1.weight + c2.weight) and 0xFFFF
                if (iNext <= H_ROOT) {
                    var j = iNext
                    while (true) {
                        if (weight <= t[j].weight) {
                            t[j - 1].weight = weight
                            t[j - 1].child = i
                            break
                        }
                        t[j - 1].copyFrom(t[j])
                        if (++j > H_ROOT) {
                            t[H_ROOT].weight = weight
                            t[H_ROOT].child = i
                            break
                        }
                    }
                    --iNext
                } else {
                    t[H_ROOT].weight = weight
                    t[H_ROOT].parent = H_NULL
                    t[H_ROOT].child = i
                    c1.parent = H_ROOT
                    c2.parent = H_ROOT
                    break
                }
                i += 2
            }
        }
    }

    private class Prob {
        var total = 0
        var sorts = 0
        val occ = IntArray(0x101)
        val sym = IntArray(0x101)

        init {
            total = 0x101
            sorts = 0x101
            for (i in 0 until 0x100) { occ[i] = 1; sym[i] = i }
            occ[0x100] = 1
            sym[0x100] = -1
        }

        private fun half() {
            total = 0
            for (i in 0 until sorts) { occ[i] = (occ[i] + 1) shr 1; total += occ[i] }
        }

        fun increase(index0: Int) {
            var index = index0
            val o = ++occ[index]
            val s = sym[index]
            while (--index >= 0) {
                if (occ[index] >= o) break
                occ[index + 1] = occ[index]
                sym[index + 1] = sym[index]
            }
            ++index
            occ[index] = o
            sym[index] = s
            if (++total >= 0x2000) half()
        }
    }

    private class Ctx {
        var data = ByteArray(0)
        var pos = 0
        var end = 0
        var intCount = 0
        var intBuf = 0
        var nLength = 0L

        // ERINA Huffman (order-1)
        var trees: Array<HTree?>? = null
        var lastTree: HTree? = null

        // ERISA arithmetic
        var codeReg = 0L
        var augend = 0L
        var postBits = 0
        var models: Array<Prob>? = null
        var phraseLen: Prob? = null
        var phraseIdx: Prob? = null
        var runLen: Prob? = null
        var lastProb: Prob? = null

        fun attach(d: ByteArray, s: Int, e: Int) {
            data = d; pos = s; end = e; intCount = 0
        }

        fun flush() { intCount = 0 }

        private fun prefetch(): Boolean {
            if (intCount == 0) {
                if (pos >= end) return false
                var v = 0
                for (k in 0 until 4) {
                    v = (v shl 8) or (if (pos + k < end) data[pos + k].toInt() and 0xff else 0)
                }
                pos += 4
                intBuf = v
                intCount = 32
            }
            return true
        }

        /** 0 / -1 for bits; 1 if data is exhausted (like the original GetABit). */
        fun getABit(): Int {
            if (!prefetch()) return 1
            val v = intBuf shr 31
            intCount--
            intBuf = intBuf shl 1
            return v
        }

        fun getNBits(n0: Int): Int {
            var n = n0
            var code = 0
            while (n != 0) {
                if (!prefetch()) break
                val c = minOf(n, intCount)
                code = (if (c >= 32) 0 else code shl c) or (intBuf ushr (32 - c))
                n -= c
                intCount -= c
                intBuf = if (c >= 32) 0 else intBuf shl c
            }
            return code
        }

        /** Elias gamma code; 0 if data is exhausted. */
        fun getGamma(): Int {
            if (!prefetch()) return 0
            intCount--
            val first = intBuf
            intBuf = intBuf shl 1
            if (first and CODE_FLAG == 0) return 1
            var code = 0
            var base = 2
            while (true) {
                if (!prefetch()) return 0
                val d = intBuf ushr 31
                intCount--
                intBuf = intBuf shl 1
                code = (code shl 1) or d
                if (!prefetch()) return 0
                val cont = intBuf ushr 31
                intCount--
                intBuf = intBuf shl 1
                if (cont == 0) return code + base
                base = base shl 1
            }
        }

        // ---- Huffman
        fun prepareHuffman() {
            val arr = trees ?: arrayOfNulls<HTree>(0x101).also { trees = it }
            nLength = 0
            for (i in 0 until 0x101) arr[i] = HTree()
            lastTree = arr[0]
        }

        private fun huffCommon(tree: HTree, escapeGamma: Boolean): Int {
            if (tree.escape != H_NULL) {
                var iEntry: Int
                var iChild = tree.t[H_ROOT].child
                do {
                    if (!prefetch()) return H_ESCAPE
                    iEntry = iChild + (intBuf ushr 31)
                    iChild = tree.t[iEntry].child
                    intBuf = intBuf shl 1
                    --intCount
                } while (iChild and CODE_FLAG == 0)
                tree.increase(iEntry)
                val code = iChild and CODE_FLAG.inv()
                if (code != H_ESCAPE) return code
            }
            val code: Int
            if (escapeGamma) {
                code = getGamma()
                if (code == -1) return H_ESCAPE
            } else {
                code = getNBits(8)
            }
            tree.addNew(code)
            return code
        }

        fun decodeBytes(dst: ByteArray, count: Int): Int {
            val arr = trees ?: throw IOException("Chunk MIO tanpa blok awal (tabel Huffman belum diinisialisasi)")
            var tree = lastTree ?: arr[0]!!
            var i = 0
            if (nLength > 0) {
                var length = minOf(nLength, count.toLong()).toInt()
                nLength -= length
                while (length > 0) { dst[i++] = 0; length-- }
            }
            while (i < count) {
                val symbol = huffCommon(tree!!, false)
                if (symbol == H_ESCAPE) break
                dst[i++] = symbol.toByte()
                if (symbol == 0) {
                    var length = huffCommon(arr[0x100]!!, true)
                    if (length == H_ESCAPE) break
                    if (--length != 0) {
                        nLength = length.toLong()
                        if (i + length > count) length = count - i
                        nLength -= length
                        while (length > 0) { dst[i++] = 0; length-- }
                    }
                }
                tree = arr[symbol and 0xFF]!!
            }
            lastTree = tree
            return i
        }

        // ---- ERISA
        fun initErisaRegs() {
            nLength = 0
            codeReg = getNBits(32).toLong() and 0xFFFFFFFFL
            augend = 0xFFFF
            postBits = 0
        }

        fun prepareErisa() {
            val arr = models ?: Array(0x101) { Prob() }.also { models = it }
            for (i in 0 until 0x101) arr[i] = Prob()
            phraseLen = Prob(); phraseIdx = Prob(); runLen = Prob()
            lastProb = arr[0]
            initErisaRegs()
        }

        private fun decodeIndex(m: Prob): Int {
            val acc = ((codeReg * m.total) and 0xFFFFFFFFL) / augend
            if (acc >= 0x2000) return -1
            var iSym = 0
            var wAcc = (acc and 0xFFFF).toInt()
            var wFs = 0
            var occured: Int
            while (true) {
                occured = m.occ[iSym]
                if (wAcc < occured) break
                wAcc -= occured
                wFs += occured
                if (++iSym >= m.sorts) return -1
            }
            codeReg = (codeReg - ((augend * wFs + m.total - 1) and 0xFFFFFFFFL) / m.total) and 0xFFFFFFFFL
            augend = ((augend * occured) and 0xFFFFFFFFL) / m.total
            if (augend == 0L) return -1
            while ((augend and 0x8000L) == 0L) {
                var nb = getABit()
                if (nb == 1) {
                    if (++postBits >= 256) return -1
                    nb = 0
                }
                codeReg = ((codeReg shl 1) or (nb and 1).toLong()) and 0xFFFFFFFFL
                augend = augend shl 1
            }
            codeReg = codeReg and 0xFFFF
            return iSym
        }

        /** DecodeERISACodeWords (vgmstream): number of words successfully decoded. */
        fun decodeWords(dst: ShortArray, count: Int): Int {
            val tbl = models ?: throw IOException("Chunk MIO tanpa blok awal (model ERISA belum diinisialisasi)")
            var prob = lastProb ?: tbl[0]
            var i = 0
            while (i < count) {
                if (nLength > 0) {
                    val cur = minOf(count - i.toLong(), nLength).toInt()
                    nLength -= cur
                    for (j in 0 until cur) dst[i++] = 0
                    continue
                }
                var iSym = decodeIndex(prob)
                if (iSym < 0) break
                var nSymbol = prob.sym[iSym]
                prob.increase(iSym)
                if (nSymbol == -1) {
                    val pi = phraseIdx!!
                    iSym = decodeIndex(pi)
                    if (iSym < 0) break
                    nSymbol = pi.sym[iSym]
                    pi.increase(iSym)
                    val pl = phraseLen!!
                    iSym = decodeIndex(pl)
                    if (iSym < 0) break
                    nSymbol = (nSymbol shl 8) or (pl.sym[iSym] and 0xFF)
                    pl.increase(iSym)
                    dst[i++] = nSymbol.toShort()
                    prob = tbl[0x100]
                } else {
                    dst[i++] = nSymbol.toByte().toShort()
                    prob = tbl[nSymbol and 0xFF]
                    if (nSymbol == 0) {
                        val rl = runLen!!
                        iSym = decodeIndex(rl)
                        if (iSym < 0) break
                        nLength = rl.sym[iSym].toLong() and 0xFFFFFFFFL
                        rl.increase(iSym)
                    }
                }
            }
            lastProb = prob
            return i
        }
    }

    // ------------------------------------------------------------------ DCT/LOT matrices (ErisaMatrix.cs)

    private val COS_PI4 = Math.cos(Math.PI / 4).toFloat()
    private val R2COS_PI4 = 2 * COS_PI4
    private val DCT_K: Array<FloatArray?> = arrayOfNulls<FloatArray>(MAX_DCT_DEGREE).also { arr ->
        for (i in 1 until MAX_DCT_DEGREE) {
            val n = 1 shl i
            val k = FloatArray(n)
            val nr = Math.PI / (4.0 * n)
            val dr = nr + nr
            var ir = nr
            for (j in 0 until n) {
                k[j] = Math.cos(ir).toFloat()
                ir += dr
            }
            arr[i] = k
        }
    }

    private class SinCos(var sin: Float, var cos: Float)

    private fun createRevolve(degree: Int): Array<SinCos> {
        val num = 1 shl degree
        var lc = 1
        var n = num / 2
        while (n >= 8) { n /= 8; ++lc }
        val rev = Array(lc * 8) { SinCos(0f, 0f) }
        val k = Math.PI / (num * 2)
        var next = 0
        var step = 2
        do {
            for (i in 0 until 7) {
                var ws = 1.0
                var a = 0.0
                for (j in 0 until i) {
                    a += step
                    ws = ws * rev[next + j].sin + rev[next + j].cos * Math.cos(a * k)
                }
                val r = Math.atan2(ws, Math.cos((a + step) * k))
                rev[next + i].sin = Math.sin(r).toFloat()
                rev[next + i].cos = Math.cos(r).toFloat()
            }
            next += 7
            step *= 8
        } while (step < num)
        return rev
    }

    private fun oddGivensInverse(s: FloatArray, src: Int, rev: Array<SinCos>, degree: Int) {
        val num = 1 shl degree
        var index = 1
        var step = 2
        var lc = (num / 2) / 8
        var ri = 0
        while (true) {
            ri += 7
            index += step * 7
            step *= 8
            if (lc <= 8) break
            lc /= 8
        }
        var k = index + step * (lc - 2)
        var j = lc - 2
        while (j >= 0) {
            val r1 = s[src + k]
            val r2 = s[src + k + step]
            s[src + k] = r1 * rev[ri + j].cos + r2 * rev[ri + j].sin
            s[src + k + step] = r2 * rev[ri + j].cos - r1 * rev[ri + j].sin
            k -= step
            j--
        }
        while (lc <= (num / 2) / 8) {
            ri -= 7
            step /= 8
            index -= step * 7
            for (i in 0 until lc) {
                k = i * (step * 8) + index + step * 6
                j = 6
                while (j >= 0) {
                    val r1 = s[src + k]
                    val r2 = s[src + k + step]
                    s[src + k] = r1 * rev[ri + j].cos + r2 * rev[ri + j].sin
                    s[src + k + step] = r2 * rev[ri + j].cos - r1 * rev[ri + j].sin
                    k -= step
                    j--
                }
            }
            lc *= 8
        }
    }

    private fun fastIPLOT(s: FloatArray, src: Int, degree: Int) {
        val num = 1 shl degree
        var i = 0
        while (i < num) {
            val r1 = s[src + i]
            val r2 = s[src + i + 1]
            s[src + i] = 0.5f * (r1 + r2)
            s[src + i + 1] = 0.5f * (r1 - r2)
            i += 2
        }
    }

    private fun fastILOT(dst: FloatArray, s1: FloatArray, src1: Int, s2: FloatArray, src2: Int, degree: Int) {
        val num = 1 shl degree
        var i = 0
        while (i < num) {
            val r1 = s1[src1 + i]
            val r2 = s2[src2 + i + 1]
            dst[i] = r1 + r2
            dst[i + 1] = r1 - r2
            i += 2
        }
    }

    private fun fastDCT(dst: FloatArray, dstI: Int, dInt: Int, src: FloatArray, srcI: Int, work: FloatArray, workI: Int, degree: Int) {
        if (degree == 2) {
            val r = FloatArray(4)
            r[0] = src[srcI] + src[srcI + 3]
            r[2] = src[srcI] - src[srcI + 3]
            r[1] = src[srcI + 1] + src[srcI + 2]
            r[3] = src[srcI + 1] - src[srcI + 2]
            dst[dstI] = 0.5f * (r[0] + r[1])
            dst[dstI + dInt * 2] = COS_PI4 * (r[0] - r[1])
            val k2 = DCT_K[1]!!
            r[2] = k2[0] * r[2]
            r[3] = k2[1] * r[3]
            r[0] = r[2] + r[3]
            r[1] = R2COS_PI4 * (r[2] - r[3])
            r[1] -= r[0]
            dst[dstI + dInt] = r[0]
            dst[dstI + dInt * 3] = r[1]
        } else {
            val num = 1 shl degree
            val half = num shr 1
            for (i in 0 until half) {
                work[workI + i] = src[srcI + i] + src[srcI + num - i - 1]
                work[workI + i + half] = src[srcI + i] - src[srcI + num - i - 1]
            }
            val dStep = dInt shl 1
            fastDCT(dst, dstI, dStep, work, workI, src, srcI, degree - 1)
            val k = DCT_K[degree - 1]!!
            val s2 = workI + half
            val d2 = dstI + dInt
            for (i in 0 until half) work[s2 + i] *= k[i]
            fastDCT(dst, d2, dStep, work, s2, work, workI, degree - 1)
            var nx = d2
            for (i in 0 until half) {
                dst[nx] += dst[nx]
                nx += dStep
            }
            nx = d2
            for (i in 1 until half) {
                dst[nx + dStep] -= dst[nx]
                nx += dStep
            }
        }
    }

    private fun fastIDCT(dst: FloatArray, dstI: Int, src: FloatArray, srcI: Int, sInt: Int, work: FloatArray, degree: Int) {
        if (degree == 2) {
            val b1 = FloatArray(2)
            val b2 = FloatArray(4)
            b1[0] = src[srcI]
            b1[1] = COS_PI4 * src[srcI + sInt * 2]
            b2[0] = b1[0] + b1[1]
            b2[1] = b1[0] - b1[1]
            val k2 = DCT_K[1]!!
            b1[0] = k2[0] * src[srcI + sInt]
            b1[1] = k2[1] * src[srcI + sInt * 3]
            b2[2] = b1[0] + b1[1]
            b2[3] = R2COS_PI4 * (b1[0] - b1[1])
            b2[3] -= b2[2]
            dst[dstI] = b2[0] + b2[2]
            dst[dstI + 3] = b2[0] - b2[2]
            dst[dstI + 1] = b2[1] + b2[3]
            dst[dstI + 2] = b2[1] - b2[3]
        } else {
            val num = 1 shl degree
            val half = num shr 1
            val sStep = sInt shl 1
            fastIDCT(dst, dstI, src, srcI, sStep, work, degree - 1)
            val k = DCT_K[degree - 1]!!
            val odd = dstI + half
            var nx = srcI + sInt
            for (i in 0 until half) {
                work[i] = src[nx] * k[i]
                nx += sStep
            }
            fastDCT(dst, odd, 1, work, 0, work, half, degree - 1)
            for (i in 0 until half) dst[odd + i] += dst[odd + i]
            for (i in 1 until half) dst[odd + i] -= dst[odd + i - 1]
            val r = FloatArray(4)
            val quad = half shr 1
            for (i in 0 until quad) {
                r[0] = dst[dstI + i] + dst[dstI + half + i]
                r[3] = dst[dstI + i] - dst[dstI + half + i]
                r[1] = dst[dstI + half - 1 - i] + dst[dstI + num - 1 - i]
                r[2] = dst[dstI + half - 1 - i] - dst[dstI + num - 1 - i]
                dst[dstI + i] = r[0]
                dst[dstI + half - 1 - i] = r[1]
                dst[dstI + half + i] = r[2]
                dst[dstI + num - 1 - i] = r[3]
            }
        }
    }

    private fun revolve2x2(b1: FloatArray, p1: Int, b2: FloatArray, p2: Int, sin: Float, cos: Float, step: Int, count: Int) {
        var a = p1
        var b = p2
        for (i in 0 until count) {
            val r1 = b1[a]
            val r2 = b2[b]
            b1[a] = r1 * cos - r2 * sin
            b2[b] = r1 * sin + r2 * cos
            a += step
            b += step
        }
    }

    private fun roundToInt(r: Float): Int =
        if (r >= 0.0f) Math.floor(r.toDouble() + 0.5).toInt() else Math.ceil(r.toDouble() - 0.5).toInt()

    private fun roundToWords(dst: ByteArray, d0: Int, step0: Int, src: FloatArray, count: Int) {
        var d = d0
        val step = step0 * 2
        for (i in 0 until count) {
            var v = roundToInt(src[i])
            if (v <= -0x8000) v = -0x8000 else if (v >= 0x7FFF) v = 0x7FFF
            dst[d] = v.toByte()
            dst[d + 1] = (v shr 8).toByte()
            d += step
        }
    }

    // ------------------------------------------------------------------ sound decoder (MioDecoder.cs)

    private val FREQ_WIDTH = intArrayOf(-6, -6, -5, -4, -3, -2, -1)

    private class Decoder(val info: Info) {
        var bufLength = 0
        var buffer1 = IntArray(0)
        var buffer2 = IntArray(0)
        var buffer3 = ByteArray(0)
        var divisionTable = ByteArray(0)
        var revolveCode = ByteArray(0)
        var weightCode = IntArray(0)
        var coefficient = IntArray(0)
        var matrixBuf = FloatArray(0)
        var internalBuf = FloatArray(0)
        var workBuf = FloatArray(0)
        var weightTable = FloatArray(0)
        var lastDCT = FloatArray(0)
        var nextDivision = 0
        var nextRevCode = 0
        var nextWeight = 0
        var nextCoefficient = 0
        var nextSource = 0
        var lastDCTBuf = 0
        var subbandDegree = 0
        var degreeNum = 0
        var revolve: Array<SinCos> = emptyArray()
        val freqPoint = IntArray(7)

        init {
            if (info.channels != 1 && info.channels != 2) throw IOException("Jumlah kanal MIO tidak didukung: ${info.channels}")
            when (info.transformation) {
                CV_LOSSLESS -> {
                    if (info.architecture != ARCH_HUFFMAN) throw IOException("Arsitektur MIO lossless tidak didukung: ${info.architecture}")
                    if (info.bps != 8 && info.bps != 16) throw IOException("Bit per sampel MIO lossless tidak didukung: ${info.bps}")
                }
                CV_LOT, CV_LOT_MSS -> {
                    if (info.architecture != ARCH_GAMMA && info.architecture != ARCH_HUFFMAN && info.architecture != ARCH_NEMESIS)
                        throw IOException("Arsitektur MIO tidak didukung: ${info.architecture}")
                    if (info.bps != 16) throw IOException("Bit per sampel MIO lossy harus 16, bukan ${info.bps}")
                    if (info.subband < 8 || info.subband > MAX_DCT_DEGREE) throw IOException("SubbandDegree MIO di luar jangkauan: ${info.subband}")
                    if (info.lapped != 1L) throw IOException("LappedDegree MIO tidak didukung: ${info.lapped}")
                    val subband = 1 shl info.subband
                    val blockSize = info.channels * subband
                    buffer1 = IntArray(blockSize)
                    matrixBuf = FloatArray(blockSize)
                    internalBuf = FloatArray(blockSize)
                    workBuf = FloatArray(subband)
                    weightTable = FloatArray(subband)
                    lastDCT = FloatArray(info.channels * subband)
                    initWithDegree(info.subband)
                }
                else -> throw IOException("Tipe transformasi MIO tidak didukung: 0x${Integer.toHexString(info.transformation)}")
            }
        }

        fun initWithDegree(degree: Int) {
            revolve = createRevolve(degree)
            var j = 0
            for (i in 0 until 7) {
                val w = 1 shl (degree + FREQ_WIDTH[i])
                freqPoint[i] = j + (w / 2)
                j += w
            }
            subbandDegree = degree
            degreeNum = 1 shl degree
        }

        fun decodeSound(ctx: Ctx, h: Chunk, wave: ByteArray): Boolean {
            ctx.flush()
            return when (info.transformation) {
                CV_LOSSLESS -> if (info.bps == 8) decodePcm8(ctx, h, wave) else decodePcm16(ctx, h, wave)
                else -> if (info.channels != 2 || info.transformation == CV_LOT) decodeDct(ctx, h, wave) else decodeDctMss(ctx, h, wave)
            }
        }

        private fun decodePcm8(ctx: Ctx, h: Chunk, wave: ByteArray): Boolean {
            val ns = h.samples
            val ch = info.channels
            val nBytes = ns * ch
            if (buffer3.size < nBytes) buffer3 = ByteArray(nBytes)
            if (h.flags and LEAD_BLOCK != 0) ctx.prepareHuffman()
            if (ctx.decodeBytes(buffer3, nBytes) < nBytes) return false
            var src = 0
            for (i in 0 until ch) {
                var dst = i
                var v = 0
                for (j in 0 until ns) {
                    v += buffer3[src++].toInt()
                    wave[dst] = v.toByte()
                    dst += ch
                }
            }
            return true
        }

        private fun decodePcm16(ctx: Ctx, h: Chunk, wave: ByteArray): Boolean {
            val ns = h.samples
            val ch = info.channels
            val nBytes = ns * ch * 2
            if (wave.size < nBytes) return false
            if (buffer3.size < nBytes) buffer3 = ByteArray(nBytes)
            val b4 = ByteArray(nBytes)
            if (h.flags and LEAD_BLOCK != 0) ctx.prepareHuffman()
            if (ctx.decodeBytes(buffer3, nBytes) < nBytes) return false
            for (i in 0 until ch) {
                val off = i * ns * 2
                val s1 = off
                val s2 = s1 + ns
                for (j in 0 until ns) {
                    val low = buffer3[s2 + j].toInt()
                    val high = buffer3[s1 + j].toInt()
                    b4[off + j * 2] = low.toByte()
                    b4[off + j * 2 + 1] = (high xor (low shr 7)).toByte()
                }
            }
            var src = 0
            for (i in 0 until ch) {
                var dst = i * 2
                var value = 0
                var delta = 0
                for (j in 0 until ns) {
                    val s = (b4[src].toInt() and 0xff) or (b4[src + 1].toInt() shl 8)
                    src += 2
                    delta = (delta + s).toShort().toInt()
                    value = (value + delta).toShort().toInt()
                    wave[dst] = value.toByte()
                    wave[dst + 1] = (value shr 8).toByte()
                    dst += ch * 2
                }
            }
            return true
        }

        private fun ensure(nAllSample: Int, nSubbandSlots: Int, perSlot: Int, nSubbandCount: Int, mss: Boolean) {
            if (buffer2.size < nAllSample) buffer2 = IntArray(nAllSample)
            if (buffer3.size < nAllSample * 2) buffer3 = ByteArray(nAllSample * 2)
            if (divisionTable.size < nSubbandSlots) divisionTable = ByteArray(nSubbandSlots)
            val wn = nSubbandSlots * perSlot + info.channels * 2 + 8
            if (weightCode.size < wn) { weightCode = IntArray(wn); coefficient = IntArray(wn) }
            if (mss && revolveCode.size < wn) revolveCode = ByteArray(wn)
        }

        /** Read quantized symbols into buffer2. */
        private fun readSymbols(ctx: Ctx, h: Chunk, nAll: Int, nAllSubband: Int, nDegreeWidth: Int, stride: Int, rows: Int): Boolean {
            val nem = info.architecture == ARCH_NEMESIS
            if (h.flags and LEAD_BLOCK != 0) {
                if (!nem) ctx.prepareHuffman() else ctx.prepareErisa()
            } else if (nem) {
                ctx.initErisaRegs()
            }
            if (!nem) {
                if (ctx.decodeBytes(buffer3, nAll * 2) < nAll * 2) return false
                var hb = 0
                var lb = nAll
                for (i in 0 until rows) {
                    var q = i
                    for (j in 0 until nAllSubband) {
                        val low = buffer3[lb++].toInt()
                        val high = buffer3[hb++].toInt() xor (low shr 8)
                        buffer2[q] = (low and 0xFF) or (high shl 8)
                        q += stride
                    }
                }
            } else {
                val words = ShortArray(nAll)
                if (ctx.decodeWords(words, nAll) < nAll) return false
                for (i in 0 until nAll) buffer2[i] = words[i].toInt()
            }
            return true
        }

        private fun decodeDct(ctx: Ctx, h: Chunk, wave: ByteArray): Boolean {
            val nDegreeWidth = 1 shl info.subband
            val nSampleCount = (h.samples + nDegreeWidth - 1) and (nDegreeWidth - 1).inv()
            val nSubbandCount = nSampleCount shr info.subband
            val nCh = info.channels
            val nAll = nSampleCount * nCh
            val nAllSubband = nSubbandCount * nCh
            ensure(nAll, nAllSubband, 9, nSubbandCount, false)
            if (ctx.getABit() != 0) return false
            val lastDiv = IntArray(nCh) { -1 }
            nextDivision = 0; nextWeight = 0; nextCoefficient = 0
            for (i in 0 until nSubbandCount) {
                for (j in 0 until nCh) {
                    val code = ctx.getNBits(2)
                    divisionTable[nextDivision++] = code.toByte()
                    if (code != lastDiv[j]) {
                        if (i != 0) {
                            weightCode[nextWeight++] = ctx.getNBits(32)
                            coefficient[nextCoefficient++] = ctx.getNBits(16)
                        }
                        lastDiv[j] = code
                    }
                    val cnt = 1 shl code
                    for (k in 0 until cnt) {
                        weightCode[nextWeight++] = ctx.getNBits(32)
                        coefficient[nextCoefficient++] = ctx.getNBits(16)
                    }
                }
            }
            if (nSubbandCount > 0) {
                for (i in 0 until nCh) {
                    weightCode[nextWeight++] = ctx.getNBits(32)
                    coefficient[nextCoefficient++] = ctx.getNBits(16)
                }
            }
            if (ctx.getABit() != 0) return false
            if (!readSymbols(ctx, h, nAll, nAllSubband, nDegreeWidth, nDegreeWidth, nDegreeWidth)) return false

            val rest = IntArray(nCh)
            val dstBuf = IntArray(nCh)
            nextDivision = 0; nextWeight = 0; nextCoefficient = 0; nextSource = 0
            for (i in 0 until nCh) {
                lastDiv[i] = -1
                rest[i] = h.samples
                dstBuf[i] = i * 2
            }
            var curDiv = -1
            for (i in 0 until nSubbandCount) {
                for (j in 0 until nCh) {
                    val code = divisionTable[nextDivision++].toInt()
                    val cnt = 1 shl code
                    lastDCTBuf = nDegreeWidth * j  // LappedDegree == 1
                    var lead = false
                    if (lastDiv[j] != code) {
                        if (i != 0) {
                            if (curDiv != lastDiv[j]) {
                                initWithDegree(info.subband - lastDiv[j])
                                curDiv = lastDiv[j]
                            }
                            var ns = rest[j]
                            if (ns > degreeNum) ns = degreeNum
                            decodePostBlock(wave, dstBuf[j], ns)
                            rest[j] -= ns
                            dstBuf[j] += ns * nCh * 2
                        }
                        lastDiv[j] = code
                        lead = true
                    }
                    if (curDiv != code) {
                        initWithDegree(info.subband - code)
                        curDiv = code
                    }
                    for (k in 0 until cnt) {
                        if (lead) {
                            decodeLeadBlock()
                            lead = false
                        } else {
                            var ns = rest[j]
                            if (ns > degreeNum) ns = degreeNum
                            decodeInternalBlock(wave, dstBuf[j], ns)
                            rest[j] -= ns
                            dstBuf[j] += ns * nCh * 2
                        }
                    }
                }
            }
            if (nSubbandCount > 0) {
                for (i in 0 until nCh) {
                    lastDCTBuf = nDegreeWidth * i
                    if (curDiv != lastDiv[i]) {
                        initWithDegree(info.subband - lastDiv[i])
                        curDiv = lastDiv[i]
                    }
                    var ns = rest[i]
                    if (ns > degreeNum) ns = degreeNum
                    decodePostBlock(wave, dstBuf[i], ns)
                    rest[i] -= ns
                    dstBuf[i] += ns * nCh * 2
                }
            }
            return true
        }

        private fun decodeInternalBlock(wave: ByteArray, iDst: Int, ns: Int) {
            val w = weightCode[nextWeight++]
            val c = coefficient[nextCoefficient++]
            iquant(matrixBuf, 0, buffer2, nextSource, degreeNum, w, c)
            nextSource += degreeNum
            oddGivensInverse(matrixBuf, 0, revolve, subbandDegree)
            fastIPLOT(matrixBuf, 0, subbandDegree)
            fastILOT(workBuf, lastDCT, lastDCTBuf, matrixBuf, 0, subbandDegree)
            System.arraycopy(matrixBuf, 0, lastDCT, lastDCTBuf, degreeNum)
            System.arraycopy(workBuf, 0, matrixBuf, 0, degreeNum)
            fastIDCT(internalBuf, 0, matrixBuf, 0, 1, workBuf, subbandDegree)
            if (ns != 0) roundToWords(wave, iDst, info.channels, internalBuf, ns)
        }

        private fun decodeLeadBlock() {
            val w = weightCode[nextWeight++]
            val c = coefficient[nextCoefficient++]
            val half = degreeNum / 2
            for (i in 0 until half) {
                buffer1[i * 2] = 0
                buffer1[i * 2 + 1] = buffer2[nextSource++]
            }
            iquant(lastDCT, lastDCTBuf, buffer1, 0, degreeNum, w, c)
            oddGivensInverse(lastDCT, lastDCTBuf, revolve, subbandDegree)
            var i = 0
            while (i < degreeNum) {
                lastDCT[lastDCTBuf + i] = lastDCT[lastDCTBuf + i + 1]
                i += 2
            }
            fastIPLOT(lastDCT, lastDCTBuf, subbandDegree)
        }

        private fun decodePostBlock(wave: ByteArray, iDst: Int, ns: Int) {
            val w = weightCode[nextWeight++]
            val c = coefficient[nextCoefficient++]
            val half = degreeNum / 2
            for (i in 0 until half) {
                buffer1[i * 2] = 0
                buffer1[i * 2 + 1] = buffer2[nextSource++]
            }
            iquant(matrixBuf, 0, buffer1, 0, degreeNum, w, c)
            oddGivensInverse(matrixBuf, 0, revolve, subbandDegree)
            var i = 0
            while (i < degreeNum) {
                matrixBuf[i] = -matrixBuf[i + 1]
                i += 2
            }
            fastIPLOT(matrixBuf, 0, subbandDegree)
            fastILOT(workBuf, lastDCT, lastDCTBuf, matrixBuf, 0, subbandDegree)
            System.arraycopy(workBuf, 0, matrixBuf, 0, degreeNum)
            fastIDCT(internalBuf, 0, matrixBuf, 0, 1, workBuf, subbandDegree)
            if (ns != 0) roundToWords(wave, iDst, info.channels, internalBuf, ns)
        }

        // ---- MSS variant (stereo)

        private fun decodeDctMss(ctx: Ctx, h: Chunk, wave: ByteArray): Boolean {
            val nDegreeWidth = 1 shl info.subband
            val nSampleCount = (h.samples + nDegreeWidth - 1) and (nDegreeWidth - 1).inv()
            val nSubbandCount = nSampleCount shr info.subband
            val nCh = info.channels
            val nAll = nSampleCount * nCh
            ensure(nAll, nSubbandCount, 10, nSubbandCount, true)
            if (ctx.getABit() != 0) return false
            var lastDiv = -1
            nextDivision = 0; nextRevCode = 0; nextWeight = 0; nextCoefficient = 0
            for (i in 0 until nSubbandCount) {
                val code = ctx.getNBits(2)
                divisionTable[nextDivision++] = code.toByte()
                var lead = false
                if (code != lastDiv) {
                    if (i != 0) {
                        revolveCode[nextRevCode++] = ctx.getNBits(2).toByte()
                        weightCode[nextWeight++] = ctx.getNBits(32)
                        coefficient[nextCoefficient++] = ctx.getNBits(16)
                    }
                    lead = true
                    lastDiv = code
                }
                val cnt = 1 shl code
                for (k in 0 until cnt) {
                    if (lead) {
                        revolveCode[nextRevCode++] = ctx.getNBits(2).toByte()
                        lead = false
                    } else {
                        revolveCode[nextRevCode++] = ctx.getNBits(4).toByte()
                    }
                    weightCode[nextWeight++] = ctx.getNBits(32)
                    coefficient[nextCoefficient++] = ctx.getNBits(16)
                }
            }
            if (nSubbandCount > 0) {
                revolveCode[nextRevCode++] = ctx.getNBits(2).toByte()
                weightCode[nextWeight++] = ctx.getNBits(32)
                coefficient[nextCoefficient++] = ctx.getNBits(16)
            }
            if (ctx.getABit() != 0) return false
            if (!readSymbols(ctx, h, nAll, nSubbandCount, nDegreeWidth, nDegreeWidth * 2, nDegreeWidth * 2)) return false

            var restSamples = h.samples
            var wp = 0
            lastDiv = -1
            nextDivision = 0; nextRevCode = 0; nextWeight = 0; nextCoefficient = 0; nextSource = 0
            for (i in 0 until nSubbandCount) {
                val code = divisionTable[nextDivision++].toInt()
                val cnt = 1 shl code
                var lead = false
                if (lastDiv != code) {
                    if (i != 0) {
                        val ns = minOf(restSamples, degreeNum)
                        decodePostBlockMss(wave, wp, ns)
                        restSamples -= ns
                        wp += ns * nCh * 2
                    }
                    initWithDegree(info.subband - code)
                    lastDiv = code
                    lead = true
                }
                for (k in 0 until cnt) {
                    if (lead) {
                        decodeLeadBlockMss()
                        lead = false
                    } else {
                        val ns = minOf(restSamples, degreeNum)
                        decodeInternalBlockMss(wave, wp, ns)
                        restSamples -= ns
                        wp += ns * nCh * 2
                    }
                }
            }
            if (nSubbandCount > 0) {
                val ns = minOf(restSamples, degreeNum)
                decodePostBlockMss(wave, wp, ns)
            }
            return true
        }

        private fun decodeLeadBlockMss() {
            val half = degreeNum / 2
            val w = weightCode[nextWeight++]
            val c = coefficient[nextCoefficient++]
            var lap = 0
            for (i in 0 until 2) {
                for (j in 0 until half) {
                    buffer1[j * 2] = 0
                    buffer1[j * 2 + 1] = buffer2[nextSource++]
                }
                iquant(lastDCT, lap, buffer1, 0, degreeNum, w, c)
                lap += degreeNum
            }
            val rc = revolveCode[nextRevCode++].toInt()
            val sin = Math.sin(rc * Math.PI / 8).toFloat()
            val cos = Math.cos(rc * Math.PI / 8).toFloat()
            revolve2x2(lastDCT, 0, lastDCT, degreeNum, sin, cos, 1, degreeNum)
            lap = 0
            for (i in 0 until 2) {
                oddGivensInverse(lastDCT, lap, revolve, subbandDegree)
                var j = 0
                while (j < degreeNum) {
                    lastDCT[lap + j] = lastDCT[lap + j + 1]
                    j += 2
                }
                fastIPLOT(lastDCT, lap, subbandDegree)
                lap += degreeNum
            }
        }

        private fun decodeInternalBlockMss(wave: ByteArray, iDst: Int, ns: Int) {
            var src = 0
            var lap = 0
            val w = weightCode[nextWeight++]
            val c = coefficient[nextCoefficient++]
            for (i in 0 until 2) {
                iquant(matrixBuf, src, buffer2, nextSource, degreeNum, w, c)
                nextSource += degreeNum
                src += degreeNum
            }
            val rc = revolveCode[nextRevCode++].toInt()
            val rc1 = (rc shr 2) and 3
            val rc2 = rc and 3
            var sin = Math.sin(rc1 * Math.PI / 8).toFloat()
            var cos = Math.cos(rc1 * Math.PI / 8).toFloat()
            revolve2x2(matrixBuf, 0, matrixBuf, degreeNum, sin, cos, 2, degreeNum / 2)
            sin = Math.sin(rc2 * Math.PI / 8).toFloat()
            cos = Math.cos(rc2 * Math.PI / 8).toFloat()
            revolve2x2(matrixBuf, 1, matrixBuf, degreeNum + 1, sin, cos, 2, degreeNum / 2)
            src = 0
            for (i in 0 until 2) {
                oddGivensInverse(matrixBuf, src, revolve, subbandDegree)
                fastIPLOT(matrixBuf, src, subbandDegree)
                fastILOT(workBuf, lastDCT, lap, matrixBuf, src, subbandDegree)
                System.arraycopy(matrixBuf, src, lastDCT, lap, degreeNum)
                System.arraycopy(workBuf, 0, matrixBuf, src, degreeNum)
                fastIDCTAt(src)
                if (ns != 0) roundToWords(wave, iDst + i * 2, 2, internalBuf, ns)
                src += degreeNum
                lap += degreeNum
            }
        }

        private fun decodePostBlockMss(wave: ByteArray, iDst: Int, ns: Int) {
            var lap = 0
            var src = 0
            val half = degreeNum / 2
            val w = weightCode[nextWeight++]
            val c = coefficient[nextCoefficient++]
            for (i in 0 until 2) {
                for (j in 0 until half) {
                    buffer1[j * 2] = 0
                    buffer1[j * 2 + 1] = buffer2[nextSource++]
                }
                iquant(matrixBuf, src, buffer1, 0, degreeNum, w, c)
                src += degreeNum
            }
            val rc = revolveCode[nextRevCode++].toInt()
            val sin = Math.sin(rc * Math.PI / 8).toFloat()
            val cos = Math.cos(rc * Math.PI / 8).toFloat()
            revolve2x2(matrixBuf, 0, matrixBuf, degreeNum, sin, cos, 1, degreeNum)
            src = 0
            for (i in 0 until 2) {
                oddGivensInverse(matrixBuf, src, revolve, subbandDegree)
                var j = 0
                while (j < degreeNum) {
                    matrixBuf[src + j] = -matrixBuf[src + j + 1]
                    j += 2
                }
                fastIPLOT(matrixBuf, src, subbandDegree)
                fastILOT(workBuf, lastDCT, lap, matrixBuf, src, subbandDegree)
                System.arraycopy(workBuf, 0, matrixBuf, src, degreeNum)
                fastIDCTAt(src)
                if (ns != 0) roundToWords(wave, iDst + i * 2, 2, internalBuf, ns)
                lap += degreeNum
                src += degreeNum
            }
        }

        /** FastIDCT(internalBuf, matrixBuf, src, 1, workBuf, degree). */
        private fun fastIDCTAt(src: Int) {
            fastIDCT(internalBuf, 0, matrixBuf, src, 1, workBuf, subbandDegree)
        }

        private fun iquant(dst: FloatArray, d0: Int, q: IntArray, q0: Int, num: Int, weight: Int, coef: Int) {
            val scale = Math.sqrt(2.0 / num)
            val rCoef = scale * coef
            val avg = DoubleArray(7)
            for (i in 0 until 6) avg[i] = 1.0 / Math.pow(2.0, (((weight shr (i * 5)) and 0x1F) - 15) * 0.5)
            avg[6] = 1.0
            var i = 0
            while (i < freqPoint[0]) { weightTable[i] = avg[0].toFloat(); i++ }
            for (j in 1 until 7) {
                val a = avg[j - 1]
                val k = (avg[j] - a) / (freqPoint[j] - freqPoint[j - 1])
                while (i < freqPoint[j]) {
                    weightTable[i] = (k * (i - freqPoint[j - 1]) + a).toFloat()
                    i++
                }
            }
            while (i < num) weightTable[i++] = avg[6].toFloat()
            val odd = ((((weight shr 30) and 3) + 2) / 2.0).toFloat()
            i = 15
            while (i < num) { weightTable[i] *= odd; i += 16 }
            weightTable[num - 1] = coef.toFloat()
            for (n in 0 until num) weightTable[n] = 1.0f / weightTable[n]
            for (n in 0 until num) dst[d0 + n] = (rCoef * weightTable[n].toDouble() * q[q0 + n].toDouble()).toFloat()
        }
    }
}
