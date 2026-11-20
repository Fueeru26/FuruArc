package com.furuarc.core

import java.io.IOException

/**
 * NWA audio (RealLive / Siglus). Port of GARbro RealLive/AudioNWA.cs (matched against
 * SiglusSceneScriptUtility sound.py: per-mode bit/shift formulas are identical).
 *
 * Header 0x2C bytes (LE): u16 channels, u16 bits/sample (8|16), u32 rate, i32 compression mode (-1 = raw PCM, 0..5 = DPCM),
 * i32 RLE, i32 blockCount, i32 pcmSize, i32 compressedSize, i32 sampleCount, i32 blockSize (samples), i32 lastBlockSize;
 * then the block offset table (u32 x blockCount). Each block: initial sample per channel, then an LSB-first bit stream.
 * Output is always PCM16 LE; 8-bit (unsigned) sources are converted to 16-bit.
 */
object NwaAudio {
    private const val HDR = 0x2C
    private const val MAX_OUT = 600L * 1024 * 1024

    private class Hdr(
        val channels: Int, val bps: Int, val rate: Int, val comp: Int, val rle: Boolean,
        val blocks: Int, val pcmSize: Int, val samples: Int, val blockSize: Int, val finalSize: Int
    )

    private fun u16(b: ByteArray, o: Int) = (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8)
    private fun i32(b: ByteArray, o: Int) =
        (b[o].toInt() and 0xff) or ((b[o + 1].toInt() and 0xff) shl 8) or
            ((b[o + 2].toInt() and 0xff) shl 16) or ((b[o + 3].toInt() and 0xff) shl 24)

    private fun parse(b: ByteArray): Hdr? {
        if (b.size < 0x28) return null
        val ch = u16(b, 0)
        val bps = u16(b, 2)
        if (ch < 1 || ch > 2 || (bps != 8 && bps != 16)) return null
        val rate = i32(b, 4)
        if (rate <= 0 || rate > 384000) return null
        val comp = i32(b, 8)
        if (comp < -1 || comp > 5) return null
        val pcm = i32(b, 0x14)
        if (pcm <= 0) return null
        val samples = i32(b, 0x1C)
        if (comp != -1) {
            if (samples <= 0 || pcm.toLong() != samples.toLong() * bps / 8) return null
            if (i32(b, 0x10) <= 0 || i32(b, 0x20) <= 0) return null
        }
        return Hdr(ch, bps, rate, comp, i32(b, 0xC) != 0, i32(b, 0x10), pcm, samples, i32(b, 0x20), i32(b, 0x24))
    }

    /** Validate the first 0x28 header bytes (channels 1..2, 8/16 bit, mode -1..5, PCM size consistent with sample count). */
    fun isNwa(head: ByteArray): Boolean = parse(head) != null

    private class Bits(val d: ByteArray) {
        var pos = 0
        var bit = 0
        fun get(n: Int): Int {
            var v = 0
            var got = 0
            while (got < n) {
                val cur = if (pos < d.size) d[pos].toInt() and 0xff else 0
                val take = minOf(8 - bit, n - got)
                v = v or (((cur shr bit) and ((1 shl take) - 1)) shl got)
                got += take
                bit += take
                if (bit == 8) { bit = 0; pos++ }
            }
            return v
        }
    }

    fun decode(b: ByteArray): PcmAudio {
        val h = parse(b) ?: throw IOException("Bukan file NWA yang valid (header tidak sesuai)")
        val outBytes = if (h.bps == 8) h.pcmSize.toLong() * 2 else h.pcmSize.toLong()
        if (outBytes > MAX_OUT) throw IOException("Keluaran NWA melebihi 600 MB")
        if (h.comp == -1) {
            if (h.pcmSize > b.size - HDR) throw IOException("NWA mentah terpotong: ukuran PCM melebihi isi file")
            val pcm = if (h.bps == 16) b.copyOfRange(HDR, HDR + h.pcmSize) else {
                val o = ByteArray(h.pcmSize * 2)
                for (i in 0 until h.pcmSize) {
                    val v = ((b[HDR + i].toInt() and 0xff) - 128) shl 8
                    o[i * 2] = v.toByte(); o[i * 2 + 1] = (v shr 8).toByte()
                }
                o
            }
            return PcmAudio(h.rate, h.channels, pcm)
        }
        if (h.blocks.toLong() * 4 > b.size - HDR) throw IOException("Tabel blok NWA terpotong")
        val offsets = IntArray(h.blocks) { k -> i32(b, HDR + k * 4) }
        val out = ByteArray(h.samples * 2)
        var dst = 0
        var remaining = h.samples
        val comp = h.comp
        val bytesPer = h.bps / 8
        val sample = IntArray(2)
        for (bi in 0 until h.blocks) {
            if (remaining <= 0) break
            val off = offsets[bi]
            if (off < 0 || off + h.channels * bytesPer > b.size) throw IOException("Offset blok NWA #$bi di luar file")
            var n = if (bi < h.blocks - 1) h.blockSize else (if (h.finalSize > 0) h.finalSize else h.blockSize)
            if (n > remaining) n = remaining
            var p = off
            for (c in 0 until h.channels) {
                if (h.bps == 8) { sample[c] = b[p].toInt() and 0xff; p += 1 }
                else { sample[c] = (u16(b, p) shl 16) shr 16; p += 2 }
            }
            val br = Bits(b)
            br.pos = p
            var ch = 0
            var repeat = 0
            for (i in 0 until n) {
                if (repeat == 0) {
                    val ctl = br.get(3)
                    if (ctl == 7) {
                        if (br.get(1) == 1) {
                            sample[ch] = 0
                        } else {
                            var bits = 8
                            var shift = 9
                            if (comp < 3) { bits -= comp; shift += comp }
                            val sign = 1 shl (bits - 1)
                            val mask = sign - 1
                            val v = br.get(bits)
                            val d = (v and mask) shl shift
                            sample[ch] = ((if (v and sign != 0) sample[ch] - d else sample[ch] + d) shl 16) shr 16
                        }
                    } else if (ctl != 0) {
                        val bits: Int
                        val shift: Int
                        if (comp < 3) { bits = 5 - comp; shift = 2 + ctl + comp }
                        else { bits = 3 + comp; shift = 1 + ctl }
                        val sign = 1 shl (bits - 1)
                        val mask = sign - 1
                        val v = br.get(bits)
                        val d = (v and mask) shl shift
                        sample[ch] = ((if (v and sign != 0) sample[ch] - d else sample[ch] + d) shl 16) shr 16
                    } else if (h.rle) {
                        repeat = br.get(1)
                        if (repeat == 1) {
                            repeat = br.get(2)
                            if (repeat == 3) repeat = br.get(8)
                        }
                    }
                } else {
                    repeat--
                }
                if (h.bps == 8) {
                    val v = ((sample[ch] and 0xff) - 128) shl 8
                    out[dst++] = v.toByte(); out[dst++] = (v shr 8).toByte()
                } else {
                    out[dst++] = sample[ch].toByte(); out[dst++] = (sample[ch] shr 8).toByte()
                }
                if (h.channels == 2) ch = ch xor 1
            }
            remaining -= n
        }
        if (remaining > 0) throw IOException("Data NWA kurang: $remaining sampel tidak terdekode (tabel blok tidak cukup)")
        return PcmAudio(h.rate, h.channels, out)
    }
}
