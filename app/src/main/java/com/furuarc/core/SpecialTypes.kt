package com.furuarc.core

/** Decoded image: 8-bit non-premultiplied RGBA pixels, rows top to bottom ([rgba].size == width*height*4). */
class RgbaImage(val width: Int, val height: Int, val rgba: ByteArray) {
    init { require(rgba.size >= width * height * 4) { "Data piksel kurang" } }
}

/** 16-bit little-endian interleaved PCM audio: [pcm] holds the sample bytes. */
class PcmAudio(val rate: Int, val channels: Int, val pcm: ByteArray)

/** One file extracted from a container (e.g. an OVK containing many Ogg files). */
class ExtractedFile(val name: String, val data: ByteArray)
