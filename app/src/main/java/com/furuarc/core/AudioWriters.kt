package com.furuarc.core

import net.sourceforge.lame.mp3.Lame
import net.sourceforge.lame.mp3.MPEGMode
import org.xiph.libogg.ogg_packet
import org.xiph.libogg.ogg_page
import org.xiph.libogg.ogg_stream_state
import org.xiph.libvorbis.vorbis_block
import org.xiph.libvorbis.vorbis_comment
import org.xiph.libvorbis.vorbis_dsp_state
import org.xiph.libvorbis.vorbis_info
import org.xiph.libvorbis.vorbisenc
import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile
import java.security.MessageDigest

/*
 * Audio writers without native libraries, pure Java/Kotlin only:
 *  - MP3   : java-lame (Java port of LAME, LGPL)  -> net.sourceforge.lame
 *  - Vorbis: Java port of libvorbis/libogg from Xiph.Org (BSD) -> org.xiph
 *  - FLAC  : in-house streaming encoder (fixed predictor + Rice, adaptive stereo)
 * All accept 16-bit interleaved PCM.
 */

object VorbisInfo {
    /** Sample rate used by the Vorbis encoder: 44100 or 48000. */
    fun snapRate(rate: Int): Int = if (rate >= 46050) 48000 else 44100
}

object Mp3Info {
    val RATES = intArrayOf(8000, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000)
    private val MPEG1 = intArrayOf(32, 40, 48, 56, 64, 80, 96, 112, 128, 160, 192, 224, 256, 320)
    private val MPEG2 = intArrayOf(8, 16, 24, 32, 40, 48, 56, 64, 80, 96, 112, 128, 144, 160)

    /** Nearest valid CBR bitrate (kbps) for sample rate [rate]. */
    fun validKbps(rate: Int, kbps: Int): Int {
        val list = if (rate >= 32000) MPEG1 else MPEG2
        var best = list[0]
        for (x in list) if (Math.abs(x - kbps) < Math.abs(best - kbps)) best = x
        return best
    }
}

/** CBR MP3 (LAME). [kbps] must be valid for [rate] (see [Mp3Info.validKbps]). */
class Mp3Writer(private val os: OutputStream, rate: Int, private val ch: Int, kbps: Int) {
    private val lame = Lame()
    private var l = FloatArray(0)
    private var r = FloatArray(0)
    private var out = ByteArray(0)
    private var closed = false

    init {
        val f = lame.flags
        f.setInNumChannels(ch)
        f.setInSampleRate(rate)
        f.setMode(if (ch == 1) MPEGMode.MONO else MPEGMode.JOINT_STEREO)
        f.setBitRate(kbps)
        f.setQuality(Lame.QUALITY_MIDDLE)
        f.setWriteId3tagAutomatic(false)
        f.bWriteVbrTag = false
        lame.id3.init(f)
        if (lame.initParams() < 0) throw IOException("Parameter MP3 tidak didukung (rate $rate, $kbps kbps)")
    }

    fun write(pcm: ShortArray, n: Int) {
        val frames = n / ch
        var pos = 0
        while (pos < frames) {
            val cnt = minOf(frames - pos, 8192)
            if (l.size < cnt) { l = FloatArray(cnt); r = FloatArray(cnt) }
            for (i in 0 until cnt) {
                val base = (pos + i) * ch
                val a = pcm[base] * 65536f
                l[i] = a
                r[i] = if (ch == 2) pcm[base + 1] * 65536f else a
            }
            val need = (cnt * 1.25 + 7200).toInt()
            if (out.size < need) out = ByteArray(need)
            val k = lame.encodeBuffer(l, r, cnt, out)
            if (k < 0) throw IOException("Gagal menyandikan MP3 (kode $k)")
            if (k > 0) os.write(out, 0, k)
            pos += cnt
        }
    }

    fun finish() {
        if (out.size < 16384) out = ByteArray(16384)
        val k = lame.encodeFlush(out)
        if (k > 0) os.write(out, 0, k)
        os.flush()
    }

    fun release() {
        if (!closed) {
            closed = true
            try { lame.close() } catch (e: Exception) { }
        }
    }
}

/** OGG Vorbis (average bitrate [bitrateBps]); [tags] are written as Vorbis comments (e.g. LOOPSTART/LOOPLENGTH). */
class VorbisWriter(private val os: OutputStream, rate: Int, private val inCh: Int, bitrateBps: Int, tags: List<Pair<String, String>> = emptyList()) {
    // The built-in Vorbis template is only for stereo 44.1/48 kHz; mono is duplicated to two channels.
    private val ch = 2
    private val vi = vorbis_info()
    private val vd = vorbis_dsp_state()
    private val vb: vorbis_block
    private val st: ogg_stream_state
    private val og = ogg_page()
    private val op = ogg_packet()

    init {
        val enc = vorbisenc()
        if (!enc.vorbis_encode_init(vi, ch, rate, -1, bitrateBps, -1)) throw IOException("Parameter Vorbis tidak didukung (rate $rate, ${bitrateBps / 1000} kbps)")
        val vc = vorbis_comment()
        vc.vorbis_comment_add_tag("ENCODER", "FuruArc")
        for ((k, v) in tags) vc.vorbis_comment_add_tag(k, v)
        if (!vd.vorbis_analysis_init(vi)) throw IOException("Gagal menyiapkan analisis Vorbis")
        vb = vorbis_block(vd)
        st = ogg_stream_state(java.util.Random().nextInt(1 shl 24))
        val h1 = ogg_packet()
        val h2 = ogg_packet()
        val h3 = ogg_packet()
        vd.vorbis_analysis_headerout(vc, h1, h2, h3)
        st.ogg_stream_packetin(h1)
        st.ogg_stream_packetin(h2)
        st.ogg_stream_packetin(h3)
        while (st.ogg_stream_flush(og)) writePage()
    }

    private fun writePage() {
        os.write(og.header, 0, og.header_len)
        os.write(og.body, 0, og.body_len)
    }

    private fun drain() {
        while (vb.vorbis_analysis_blockout(vd)) {
            vb.vorbis_analysis(null)
            vb.vorbis_bitrate_addblock()
            while (vd.vorbis_bitrate_flushpacket(op)) {
                st.ogg_stream_packetin(op)
                while (st.ogg_stream_pageout(og)) writePage()
            }
        }
    }

    fun write(pcm: ShortArray, n: Int) {
        val frames = n / inCh
        var pos = 0
        while (pos < frames) {
            val cnt = minOf(frames - pos, 4096)
            val buf = vd.vorbis_analysis_buffer(cnt)
            val cur = vd.pcm_current
            for (i in 0 until cnt) {
                val base = (pos + i) * inCh
                val l = pcm[base] / 32768f
                buf[0][cur + i] = l
                buf[1][cur + i] = if (inCh >= 2) pcm[base + 1] / 32768f else l
            }
            vd.vorbis_analysis_wrote(cnt)
            drain()
            pos += cnt
        }
    }

    fun finish() {
        vd.vorbis_analysis_wrote(0)
        drain()
        while (st.ogg_stream_flush(og)) writePage()
        os.flush()
    }
}

/**
 * 16-bit streaming FLAC: 4096 block, constant/verbatim/fixed-predictor subframes of order 0-4 with partitioned Rice coding,
 * stereo chosen automatically (L/R, L/S, S/R, M/S). Compression is slightly below flac -3 because there is no LPC, but the result is
 * standard, lossless FLAC. STREAMINFO (sample count, frame size, MD5) is filled in at the end.
 */
class FlacWriter(private val raf: RandomAccessFile, private val rate: Int, private val ch: Int) {
    private val BLOCK = 4096
    private val md5 = MessageDigest.getInstance("MD5")
    private val x = Array(ch) { IntArray(BLOCK) }
    private var np = 0
    private var total = 0L
    private var frameNo = 0L
    private var minFrame = Int.MAX_VALUE
    private var maxFrame = 0
    private val bw = Bits(1 shl 16)
    private val mid = IntArray(BLOCK)
    private val side = IntArray(BLOCK)
    private val tmp = IntArray(BLOCK)
    private val md5buf = ByteArray(BLOCK * 2 * 2)

    init {
        if (ch < 1 || ch > 2) throw IOException("FLAC hanya mendukung mono/stereo di sini")
        if (rate <= 0 || rate >= (1 shl 20)) throw IOException("Sample rate FLAC tidak valid")
        raf.setLength(0)
        raf.write(byteArrayOf('f'.code.toByte(), 'L'.code.toByte(), 'a'.code.toByte(), 'C'.code.toByte(), 0x80.toByte(), 0, 0, 34))
        raf.write(ByteArray(34))
    }

    fun write(pcm: ShortArray, n: Int) {
        val frames = n / ch
        var mp = 0
        for (i in 0 until frames) {
            for (c in 0 until ch) {
                val s = pcm[i * ch + c].toInt()
                x[c][np] = s
                md5buf[mp++] = s.toByte()
                md5buf[mp++] = (s shr 8).toByte()
                if (mp == md5buf.size) { md5.update(md5buf, 0, mp); mp = 0 }
            }
            np++
            if (np == BLOCK) { encodeBlock(BLOCK); np = 0 }
        }
        if (mp > 0) md5.update(md5buf, 0, mp)
    }

    fun finish() {
        if (np > 0) { encodeBlock(np); np = 0 }
        val h = Bits(64)
        h.put((if (minFrame == Int.MAX_VALUE) BLOCK else BLOCK).toLong(), 16) // min block = max block = 4096 (the last block may be smaller)
        h.put(BLOCK.toLong(), 16)
        h.put((if (minFrame == Int.MAX_VALUE) 0 else minFrame).toLong(), 24)
        h.put(maxFrame.toLong(), 24)
        h.put(rate.toLong(), 20)
        h.put((ch - 1).toLong(), 3)
        h.put(15L, 5)
        h.put(total, 36)
        val b = h.bytes()
        raf.seek(8)
        raf.write(b, 0, 18)
        raf.write(md5.digest())
    }

    // ---------------------------------------------------------------- block encoding

    private class Plan(n: Int) {
        var kind = 1 // 0 constant, 1 verbatim, 2 fixed predictor
        var order = 0
        var bits = 0L
        var partOrder = 0
        val params = IntArray(256)
        val resid = IntArray(n)
    }

    private val plans = Array(4) { Plan(BLOCK) }
    private val work = Plan(BLOCK)

    private fun plan(sig: IntArray, n: Int, bps: Int, out: Plan) {
        // constant?
        var same = true
        for (i in 1 until n) if (sig[i] != sig[0]) { same = false; break }
        if (same) { out.kind = 0; out.bits = 8L + bps; return }
        out.kind = 1
        out.bits = 8L + n.toLong() * bps
        val maxOrder = minOf(4, n - 1)
        for (order in 0..maxOrder) {
            computeResid(sig, n, order, work.resid)
            val cnt = n - order
            var bestBits = Long.MAX_VALUE
            var bestPo = 0
            val maxPo = maxPartOrder(n, order)
            for (po in 0..maxPo) {
                val bitsPo = riceCost(work.resid, order, n, po, null)
                if (bitsPo < bestBits) { bestBits = bitsPo; bestPo = po }
            }
            val total = 8L + order.toLong() * bps + 2 + 4 + bestBits
            if (total < out.bits) {
                out.kind = 2
                out.order = order
                out.bits = total
                out.partOrder = bestPo
                riceCost(work.resid, order, n, bestPo, out.params)
                System.arraycopy(work.resid, 0, out.resid, 0, n)
            }
            if (cnt <= 0) break
        }
    }

    private fun maxPartOrder(n: Int, order: Int): Int {
        var po = 0
        while (po < 8 && n % (1 shl (po + 1)) == 0 && (n shr (po + 1)) > order) po++
        return po
    }

    private fun computeResid(s: IntArray, n: Int, order: Int, r: IntArray) {
        for (i in order until n) {
            r[i] = when (order) {
                0 -> s[i]
                1 -> s[i] - s[i - 1]
                2 -> s[i] - 2 * s[i - 1] + s[i - 2]
                3 -> s[i] - 3 * s[i - 1] + 3 * s[i - 2] - s[i - 3]
                else -> s[i] - 4 * s[i - 1] + 6 * s[i - 2] - 4 * s[i - 3] + s[i - 4]
            }
        }
    }

    /** Bit cost of the residual with 2^po partitions (4-bit parameter per partition); the chosen parameters are copied to [params] if not null. */
    private fun riceCost(r: IntArray, order: Int, n: Int, po: Int, params: IntArray?): Long {
        val parts = 1 shl po
        val psize = n shr po
        var total = 0L
        var pos = order
        for (p in 0 until parts) {
            val end = (p + 1) * psize
            val cnt = end - pos
            var sum = 0L
            for (i in pos until end) {
                val v = r[i]
                sum += ((v shl 1) xor (v shr 31)).toLong() and 0xFFFFFFFFL
            }
            // estimate k then check k-1..k+1 exactly
            var k0 = 0
            if (cnt > 0) {
                val mean = sum / cnt
                while (k0 < 14 && (1L shl (k0 + 1)) <= mean) k0++
            }
            var bestK = k0
            var bestBits = Long.MAX_VALUE
            for (k in maxOf(0, k0 - 1)..minOf(14, k0 + 1)) {
                var b = 0L
                for (i in pos until end) {
                    val v = r[i]
                    val u = ((v shl 1) xor (v shr 31)).toLong() and 0xFFFFFFFFL
                    b += (u shr k) + 1 + k
                }
                if (b < bestBits) { bestBits = b; bestK = k }
            }
            total += 4 + bestBits
            if (params != null) params[p] = bestK
            pos = end
        }
        return total
    }

    private fun writeSub(sig: IntArray, n: Int, bps: Int, pl: Plan) {
        when (pl.kind) {
            0 -> {
                bw.put(0L, 8)
                bw.putSigned(sig[0].toLong(), bps)
            }
            1 -> {
                bw.put(2L, 8) // 0 000001 0
                for (i in 0 until n) bw.putSigned(sig[i].toLong(), bps)
            }
            else -> {
                bw.put(((0b001000 or pl.order) shl 1).toLong(), 8)
                for (i in 0 until pl.order) bw.putSigned(sig[i].toLong(), bps)
                bw.put(0L, 2) // Rice 4-bit
                bw.put(pl.partOrder.toLong(), 4)
                val parts = 1 shl pl.partOrder
                val psize = n shr pl.partOrder
                var pos = pl.order
                for (p in 0 until parts) {
                    val end = (p + 1) * psize
                    val k = pl.params[p]
                    bw.put(k.toLong(), 4)
                    for (i in pos until end) {
                        val v = pl.resid[i]
                        val u = ((v shl 1) xor (v shr 31)).toLong() and 0xFFFFFFFFL
                        bw.putUnary(u shr k)
                        if (k > 0) bw.put(u and ((1L shl k) - 1), k)
                    }
                    pos = end
                }
            }
        }
    }

    private fun encodeBlock(n: Int) {
        // start from each channel's plan
        var assign: Int
        var a: IntArray
        var b: IntArray
        var bpsA = 16
        var bpsB = 16
        if (ch == 1) {
            assign = 0
            a = x[0]; b = a
            plan(a, n, 16, plans[0])
            writeFrame(n, assign, a, b, plans[0], plans[0], 16, 16, true)
        } else {
            val l = x[0]
            val r = x[1]
            for (i in 0 until n) { side[i] = l[i] - r[i]; mid[i] = (l[i] + r[i]) shr 1 }
            plan(l, n, 16, plans[0])
            plan(r, n, 16, plans[1])
            plan(side, n, 17, plans[2])
            plan(mid, n, 16, plans[3])
            val cLR = plans[0].bits + plans[1].bits
            val cLS = plans[0].bits + plans[2].bits
            val cSR = plans[2].bits + plans[1].bits
            val cMS = plans[3].bits + plans[2].bits
            val best = minOf(minOf(cLR, cLS), minOf(cSR, cMS))
            when (best) {
                cLR -> writeFrame(n, 1, l, r, plans[0], plans[1], 16, 16, false)
                cLS -> writeFrame(n, 8, l, side, plans[0], plans[2], 16, 17, false)
                cSR -> writeFrame(n, 9, side, r, plans[2], plans[1], 17, 16, false)
                else -> writeFrame(n, 10, mid, side, plans[3], plans[2], 16, 17, false)
            }
        }
        total += n
        frameNo++
    }

    private fun writeFrame(n: Int, assign: Int, a: IntArray, b: IntArray, pa: Plan, pb: Plan, bpsA: Int, bpsB: Int, mono: Boolean) {
        bw.reset()
        bw.put(0xFFF8L, 16)
        bw.put((if (n == BLOCK) 12 else 7).toLong(), 4)
        bw.put(0L, 4) // sample rate from STREAMINFO
        bw.put((if (mono) 0 else if (assign == 1) 1 else assign).toLong(), 4)
        bw.put(4L, 3) // 16 bit
        bw.put(0L, 1)
        // frame number (UTF-8)
        val fnum = frameNo
        when {
            fnum < 0x80 -> bw.put(fnum, 8)
            fnum < 0x800 -> { bw.put(0xC0L or (fnum shr 6), 8); bw.put(0x80L or (fnum and 0x3F), 8) }
            fnum < 0x10000 -> { bw.put(0xE0L or (fnum shr 12), 8); bw.put(0x80L or ((fnum shr 6) and 0x3F), 8); bw.put(0x80L or (fnum and 0x3F), 8) }
            fnum < 0x200000 -> { bw.put(0xF0L or (fnum shr 18), 8); bw.put(0x80L or ((fnum shr 12) and 0x3F), 8); bw.put(0x80L or ((fnum shr 6) and 0x3F), 8); bw.put(0x80L or (fnum and 0x3F), 8) }
            fnum < 0x4000000 -> { bw.put(0xF8L or (fnum shr 24), 8); bw.put(0x80L or ((fnum shr 18) and 0x3F), 8); bw.put(0x80L or ((fnum shr 12) and 0x3F), 8); bw.put(0x80L or ((fnum shr 6) and 0x3F), 8); bw.put(0x80L or (fnum and 0x3F), 8) }
            else -> { bw.put(0xFCL or (fnum shr 30), 8); bw.put(0x80L or ((fnum shr 24) and 0x3F), 8); bw.put(0x80L or ((fnum shr 18) and 0x3F), 8); bw.put(0x80L or ((fnum shr 12) and 0x3F), 8); bw.put(0x80L or ((fnum shr 6) and 0x3F), 8); bw.put(0x80L or (fnum and 0x3F), 8) }
        }
        if (n != BLOCK) bw.put((n - 1).toLong(), 16)
        bw.put(crc8(bw.buf, bw.byteLen()).toLong(), 8)
        writeSub(a, n, bpsA, pa)
        if (!mono) writeSub(b, n, bpsB, pb)
        bw.align()
        bw.put(crc16(bw.buf, bw.byteLen()).toLong(), 16)
        val len = bw.byteLen()
        raf.write(bw.buf, 0, len)
        if (len < minFrame) minFrame = len
        if (len > maxFrame) maxFrame = len
    }

    // ---------------------------------------------------------------- bit writer & CRC

    private class Bits(cap: Int) {
        var buf = ByteArray(cap)
        private var acc = 0L
        private var nacc = 0
        private var len = 0

        fun reset() { len = 0; acc = 0; nacc = 0 }
        fun byteLen(): Int = len

        private fun ensure(extra: Int) {
            if (len + extra > buf.size) buf = buf.copyOf(maxOf(buf.size * 2, len + extra))
        }

        fun put(v: Long, n: Int) {
            var bits = n
            while (bits > 0) {
                val take = minOf(bits, 32)
                val part = (v shr (bits - take)) and ((1L shl take) - 1)
                acc = (acc shl take) or part
                nacc += take
                bits -= take
                while (nacc >= 8) {
                    ensure(1)
                    buf[len++] = (acc shr (nacc - 8)).toByte()
                    nacc -= 8
                }
                acc = acc and ((1L shl nacc) - 1)
            }
        }

        fun putSigned(v: Long, n: Int) = put(v and ((1L shl n) - 1), n)

        fun putUnary(q: Long) {
            var z = q
            while (z >= 32) { put(0L, 32); z -= 32 }
            put(1L, (z + 1).toInt())
        }

        fun align() { if (nacc > 0) put(0L, 8 - nacc) }

        /** Copy the bytes that are already complete (for small headers). */
        fun bytes(): ByteArray { align(); return buf.copyOf(len) }
    }

    companion object {
        private val CRC8 = IntArray(256).also { t ->
            for (i in 0 until 256) {
                var c = i
                for (k in 0 until 8) c = if (c and 0x80 != 0) ((c shl 1) xor 0x07) and 0xFF else (c shl 1) and 0xFF
                t[i] = c
            }
        }
        private val CRC16 = IntArray(256).also { t ->
            for (i in 0 until 256) {
                var c = i shl 8
                for (k in 0 until 8) c = if (c and 0x8000 != 0) ((c shl 1) xor 0x8005) and 0xFFFF else (c shl 1) and 0xFFFF
                t[i] = c
            }
        }

        private fun crc8(b: ByteArray, n: Int): Int {
            var c = 0
            for (i in 0 until n) c = CRC8[c xor (b[i].toInt() and 0xFF)]
            return c
        }

        private fun crc16(b: ByteArray, n: Int): Int {
            var c = 0
            for (i in 0 until n) c = ((c shl 8) and 0xFFFF) xor CRC16[((c shr 8) xor (b[i].toInt() and 0xFF)) and 0xFF]
            return c
        }
    }
}
