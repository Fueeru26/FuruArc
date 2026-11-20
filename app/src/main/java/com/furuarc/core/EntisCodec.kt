package com.furuarc.core

import java.io.IOException

/**
 * EntisGLS codec (NOA archive): BSHF (32-byte block encryption based on bit permutation) and
 * ERISA-N "Nemesis" (arithmetic decompression + 64 KB phrase window). Port of GARbro ArcNOA.cs / ErisaNemesis.cs
 * and EriReader.cs (ErisaProbModel). The reference source has no ERISA encoder, so this is decoder-only.
 */
object EntisCodec {

    /** Password -> key bytes (min 32 bytes), like PrepareToDecodeBSHFCode. */
    fun bshfKey(password: String): ByteArray {
        val pw = if (password.isEmpty()) " " else password
        val raw = ByteArray(pw.length) { i -> val c = pw[i].code; (if (c < 128) c else '?'.code).toByte() }
        val len = maxOf(raw.size, 32)
        val p = ByteArray(len)
        System.arraycopy(raw, 0, p, 0, raw.size)
        var cc = raw.size
        if (cc < 32) {
            p[cc++] = 0x1b
            for (i in cc until 32) p[i] = ((p[i % cc].toInt() and 0xff) + (p[i - 1].toInt() and 0xff)).toByte()
        }
        return p
    }

    /** For one block: dest[i] = bit position (offset*8 + bit number from MSB) where source bit i lands. */
    private fun bshfPerm(key: ByteArray, passOffset: Int, dest: IntArray) {
        val mask = IntArray(32)
        val n = key.size
        var iPos = passOffset
        var iBit = 0
        for (i in 0 until 256) {
            iBit = (iBit + (key[iPos++].toInt() and 0xff)) and 0xFF
            if (iPos >= n) iPos = 0
            var iOffset = iBit shr 3
            var iMask = 0x80 shr (iBit and 7)
            while (mask[iOffset] == 0xFF) {
                iBit = (iBit + 8) and 0xFF
                iOffset = iBit shr 3
            }
            while (mask[iOffset] and iMask != 0) {
                iBit++
                iMask = iMask shr 1
                if (iMask == 0) {
                    iBit = (iBit + 8) and 0xFF
                    iOffset = iBit shr 3
                    iMask = 0x80
                }
            }
            mask[iOffset] = mask[iOffset] or iMask
            dest[i] = iOffset * 8 + Integer.numberOfLeadingZeros(iMask) - 24
        }
    }

    /** In-place decrypt/encrypt; length must be a multiple of 32 (a trailing partial block is ignored by the caller). */
    class Bshf(password: String) {
        private val key = bshfKey(password)
        private var passOffset = 0
        private val perm = IntArray(256)

        private fun nextPerm() {
            if (passOffset >= key.size) passOffset = 0
            bshfPerm(key, passOffset, perm)
            passOffset++
        }

        /** src -> result bits (decrypt): source bit i is moved to perm[i]. */
        fun decryptBlock(buf: ByteArray, off: Int) {
            nextPerm()
            val out = ByteArray(32)
            for (i in 0 until 256) {
                if ((buf[off + (i shr 3)].toInt() and (0x80 shr (i and 7))) != 0) {
                    val d = perm[i]
                    out[d shr 3] = (out[d shr 3].toInt() or (0x80 shr (d and 7))).toByte()
                }
            }
            System.arraycopy(out, 0, buf, off, 32)
        }

        /** inverse of decryptBlock. */
        fun encryptBlock(buf: ByteArray, off: Int) {
            nextPerm()
            val out = ByteArray(32)
            for (i in 0 until 256) {
                val d = perm[i]
                if ((buf[off + (d shr 3)].toInt() and (0x80 shr (d and 7))) != 0)
                    out[i shr 3] = (out[i shr 3].toInt() or (0x80 shr (i and 7))).toByte()
            }
            System.arraycopy(out, 0, buf, off, 32)
        }
    }

    /** 4-byte XOR (NOA checksum) of the original data. */
    fun xorSum(data: ByteArray, off: Int, len: Int): ByteArray {
        val c = ByteArray(4)
        for (i in 0 until len) c[i and 3] = (c[i and 3].toInt() xor data[off + i].toInt()).toByte()
        return c
    }

    // BSHF-decrypt a buffer of length len (rounded down to a multiple of 32); returns the length.
    fun bshfDecrypt(buf: ByteArray, len: Int, password: String): Int {
        val b = Bshf(password)
        val n = len / 32 * 32
        var o = 0
        while (o < n) { b.decryptBlock(buf, o); o += 32 }
        return n
    }

    // ---------------------------------------------------------------- ERISA-N

    private const val TOTAL_LIMIT = 0x2000
    private const val SYM_MAX = 0x101
    private const val SUB_MAX = 0x80
    private const val SLOT_MAX = 0x800
    private const val ESC = -1
    private val SHIFT = intArrayOf(1, 3, 4, 5)
    private val NEW_LIMIT = intArrayOf(0x01, 0x08, 0x10, 0x20)
    private const val NBUF = 0x10000
    private const val NMASK = 0xFFFF

    private class Prob {
        var total = 0
        var sorts = 0
        val occ = IntArray(SYM_MAX)
        val sym = IntArray(SYM_MAX)
        val subOcc = IntArray(SUB_MAX)
        val subSym = IntArray(SUB_MAX)

        fun init() {
            total = SYM_MAX; sorts = SYM_MAX
            for (i in 0 until 0x100) { occ[i] = 1; sym[i] = i }
            occ[0x100] = 1; sym[0x100] = ESC
            for (i in 0 until SUB_MAX) { subOcc[i] = 0; subSym[i] = -1 }
        }

        fun half() {
            total = 0
            for (i in 0 until sorts) { occ[i] = (occ[i] + 1) shr 1; total += occ[i] }
            for (i in 0 until SUB_MAX) subOcc[i] = subOcc[i] shr 1
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

        fun add(symbol: Int): Int {
            val index = sorts++
            total++
            sym[index] = symbol; occ[index] = 1
            return index
        }
    }

    private class Phrase { var first = 0; val index = IntArray(256) }

    // ERISA-N decoder over data from start to end.
    class Nemesis(private val data: ByteArray, start: Int, private val end: Int) {
        // bit reader
        private var pos = start
        private var intCount = 0
        private var intBuf = 0

        private fun prefetch(): Boolean {
            if (intCount == 0) {
                if (pos >= end) return false
                var v = 0
                for (k in 0 until 4) { v = (v shl 8) or (if (pos + k < end) data[pos + k].toInt() and 0xff else 0) }
                pos += 4
                intBuf = v; intCount = 32
            }
            return true
        }

        private fun bit(): Int {
            if (!prefetch()) return 1
            val v = intBuf ushr 31
            intCount--; intBuf = intBuf shl 1
            return v
        }

        private fun bits(n0: Int): Long {
            var n = n0
            var code = 0L
            while (n != 0) {
                if (!prefetch()) break
                val c = minOf(n, intCount)
                code = ((code shl c) or (intBuf.toLong() and 0xffffffffL ushr (32 - c))) and 0xffffffffL
                n -= c; intCount -= c
                intBuf = if (c >= 32) 0 else intBuf shl c
            }
            return code
        }

        // arithmetic
        private var codeReg = 0L
        private var augend = 0L
        private var postBits = 0

        private val baseModel = Prob()
        private val slots = arrayOfNulls<Prob>(SLOT_MAX)
        private var workUsed = 0
        private val phraseLen = Prob()
        private val phraseIdx = Prob()
        private val runLen = Prob()
        private val buf = ByteArray(NBUF)
        private val lookup = Array(256) { Phrase() }
        private var nIndex = 0
        private var left = 0
        private var next = 0
        private var eof = false
        private val last = IntArray(4)
        private var lastPos = 0

        init {
            baseModel.init()
            phraseLen.init(); phraseIdx.init(); runLen.init()
            codeReg = bits(32)
            augend = 0xFFFF
            postBits = 0
        }

        private fun decodeIndex(m: Prob): Int {
            val acc = ((codeReg * m.total) and 0xffffffffL) / augend
            if (acc >= TOTAL_LIMIT) return -1
            var iSym = 0
            var wAcc = acc.toInt() and 0xffff
            var wFs = 0
            var occured: Int
            while (true) {
                occured = m.occ[iSym]
                if (wAcc < occured) break
                wAcc -= occured; wFs += occured
                if (++iSym >= m.sorts) return -1
            }
            codeReg = (codeReg - (((augend * wFs + m.total - 1) and 0xffffffffL) / m.total)) and 0xffffffffL
            augend = ((augend * occured) and 0xffffffffL) / m.total
            if (augend == 0L) return -1
            while (augend and 0x8000L == 0L) {
                var nb = bit()
                if (nb == 1) {
                    if (++postBits >= 256) return -1
                    nb = 0
                }
                codeReg = ((codeReg shl 1) or nb.toLong()) and 0xffffffffL
                augend = augend shl 1
            }
            codeReg = codeReg and 0xFFFF
            return iSym
        }

        private fun decodeCode(m: Prob): Int {
            val i = decodeIndex(m)
            if (i < 0) return ESC
            val s = m.sym[i]
            m.increase(i)
            return s
        }

        private fun put(sym: Int) {
            last[lastPos++] = sym; lastPos = lastPos and 3
        }

        private fun remember(b: Int) {
            val ph = lookup[b]
            ph.index[ph.first] = nIndex
            ph.first = (ph.first + 1) and 0xFF
            buf[nIndex++] = b.toByte(); nIndex = nIndex and NMASK
        }

        // Decode up to count bytes into dst starting at dOff; returns the number of bytes successfully decoded.
        fun decode(dst: ByteArray, dOff: Int, count: Int): Int {
            if (eof) return 0
            var d = dOff
            var done = 0
            while (done < count) {
                if (left > 0) {
                    val n = minOf(left, count - done)
                    var lastSym = buf[(nIndex - 1) and NMASK].toInt() and 0xff
                    for (i in 0 until n) {
                        var s = lastSym
                        if (next >= 0) {
                            s = buf[next++].toInt() and 0xff
                            next = next and NMASK
                        }
                        put(s)
                        remember(s)
                        lastSym = s
                        dst[d++] = s.toByte()
                    }
                    left -= n; done += n
                    continue
                }
                var model = baseModel
                var iDeg = 0
                while (iDeg < 4) {
                    val il = last[(lastPos + 3 - iDeg) and 3] shr SHIFT[iDeg]
                    if (model.subSym[il] < 0) break
                    if (model.subSym[il] >= workUsed) throw IOException("Urutan Nemesis tidak valid")
                    model = slots[model.subSym[il]]!!
                    iDeg++
                }
                var iSym = decodeIndex(model)
                if (iSym < 0) return done
                var nSym = model.sym[iSym]
                model.increase(iSym)
                var fNemesis = false
                if (nSym == ESC) {
                    if (model !== baseModel) {
                        iSym = decodeIndex(baseModel)
                        if (iSym < 0) return done
                        nSym = baseModel.sym[iSym]
                        baseModel.increase(iSym)
                        if (nSym != ESC) model.add(nSym) else fNemesis = true
                    } else fNemesis = true
                }
                if (fNemesis) {
                    val pi = decodeCode(phraseIdx)
                    if (pi == ESC) { eof = true; return done }
                    val len = if (pi == 0) decodeCode(runLen) else decodeCode(phraseLen)
                    if (len == ESC) return done
                    val lastSym = buf[(nIndex - 1) and NMASK].toInt() and 0xff
                    val ph = lookup[lastSym]
                    left = len
                    if (pi == 0) next = -1
                    else {
                        next = ph.index[(ph.first - pi) and 0xFF]
                        if ((buf[next].toInt() and 0xff) != lastSym) throw IOException("Urutan Nemesis tidak valid")
                        next = (next + 1) and NMASK
                    }
                    continue
                }
                val b = nSym and 0xff
                put(b)
                remember(b)
                dst[d++] = b.toByte(); done++

                if (workUsed < SLOT_MAX && iDeg < 4) {
                    var iSymbol = b shr SHIFT[iDeg]
                    if (iSymbol >= SUB_MAX) throw IOException("Urutan Nemesis tidak valid")
                    if (++model.subOcc[iSymbol] >= NEW_LIMIT[iDeg]) {
                        val parent = model
                        var m = baseModel
                        var i = 0
                        while (i <= iDeg) {
                            iSymbol = last[(lastPos + 3 - i) and 3] shr SHIFT[i]
                            if (m.subSym[iSymbol] < 0) break
                            if (m.subSym[iSymbol] >= workUsed) throw IOException("Urutan Nemesis tidak valid")
                            m = slots[m.subSym[iSymbol]]!!
                            i++
                        }
                        if (i <= iDeg && m.subSym[iSymbol] < 0) {
                            val nw = Prob()
                            slots[workUsed] = nw
                            m.subSym[iSymbol] = workUsed++
                            nw.total = 0
                            var j = 0
                            for (k in 0 until parent.sorts) {
                                val o = parent.occ[k] shr 4
                                if (o > 0 && parent.sym[k] != ESC) {
                                    nw.total += o; nw.occ[j] = o; nw.sym[j] = parent.sym[k]; j++
                                }
                            }
                            nw.total++
                            nw.occ[j] = 1; nw.sym[j] = ESC
                            nw.sorts = ++j
                            for (k in 0 until SUB_MAX) { nw.subOcc[k] = 0; nw.subSym[k] = -1 }
                        }
                    }
                }
            }
            return done
        }
    }

    // ERISA-N decompression: input from off, length len, output outLen bytes.
    fun nemesisDecode(data: ByteArray, off: Int, len: Int, outLen: Int): ByteArray {
        val out = ByteArray(outLen)
        val n = Nemesis(data, off, off + len).decode(out, 0, outLen)
        return if (n == outLen) out else out.copyOf(n)
    }
}
