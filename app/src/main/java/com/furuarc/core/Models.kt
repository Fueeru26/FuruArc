package com.furuarc.core

import java.io.Closeable
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** One file inside an archive. [meta] is used by each format for extra info. */
data class Entry(
    val name: String,
    val offset: Long,
    val size: Long,
    val packedSize: Long = size,
    val meta: Any? = null,
)

/** One source file to be put into a new archive. */
class PackInput(val path: String, val size: Long, val open: () -> InputStream)

/**
 * Common options.
 * xorKey   : 1-byte XOR (0..255), starting point for per-game ciphers (XP3, Silky).
 * compress : compress when packing (XP3, Tyrano).
 * pfsVersion: '8' (encrypted) or '6' (plain).
 * extra    : additional options from the form (key = [OptionSpec.key]); read via [str]/[int]/[bool].
 */
data class Options(
    val xorKey: Int? = null,
    val compress: Boolean = true,
    val pfsVersion: Char = '8',
    val extra: Map<String, String> = emptyMap(),
) {
    fun str(key: String, def: String = ""): String = extra[key]?.takeIf { it.isNotBlank() } ?: def
    fun int(key: String, def: Int): Int = extra[key]?.trim()?.toIntOrNull() ?: def
    fun bool(key: String, def: Boolean): Boolean = extra[key]?.let { it == "1" } ?: def
}

enum class OptKind { CHOICE, TEXT, NUMBER, BOOL }

/** One dropdown choice. [value] is stored; [en]/[id] = display text. */
class Choice(val value: String, val en: String, val id: String = en)

/**
 * Description of one option shown in the form (dropdown / text field / number / checkbox).
 * [showIf] = (other option's key, value) -> this option is shown only when that other option has that value.
 */
class OptionSpec(
    val key: String,
    val kind: OptKind,
    val en: String,
    val id: String = en,
    val choices: List<Choice> = emptyList(),
    val default: String = "",
    val hintEn: String = "",
    val hintId: String = "",
    val showIf: Pair<String, String>? = null,
)

/** File name encoding dropdown (used by many formats). */
val ENCODING_SPEC = OptionSpec(
    key = "enc", kind = OptKind.CHOICE,
    en = "Filename encoding", id = "Encoding nama file",
    choices = listOf(
        Choice("auto", "Auto (default of this format)", "Auto (bawaan format ini)"),
        Choice("sjis", "Shift-JIS (Japanese)", "Shift-JIS (Jepang)"),
        Choice("utf8", "UTF-8", "UTF-8"),
        Choice("gbk", "GBK (Simplified Chinese)", "GBK (Mandarin Sederhana)"),
        Choice("big5", "Big5 (Traditional Chinese)", "Big5 (Mandarin Tradisional)"),
        Choice("euckr", "EUC-KR (Korean)", "EUC-KR (Korea)"),
    ),
    default = "auto",
)

/** What to do if the target file already exists. */
enum class ConflictPolicy { OVERWRITE, SKIP, RENAME }

fun interface Progress {
    fun update(done: Int, total: Int, name: String)
}

/** Data source that can be read randomly (without loading the whole archive into RAM). */
interface RandomSource : Closeable {
    val length: Long
    fun readAt(pos: Long, buf: ByteArray, off: Int, len: Int): Int
}

interface ArchiveFormat {
    val id: String
    val displayName: String
    val extensions: List<String>

    /** Form options for Pack Archive / Unpack Archive (empty = no options). */
    /** Output file extension if it depends on the selected options (null = the engine's extension). */
    fun packExtension(vals: Map<String, String>): String? = null
    val packOptions: List<OptionSpec> get() = emptyList()
    val unpackOptions: List<OptionSpec> get() = emptyList()

    /** false = format is read-only (the Pack Archive menu is hidden). */
    val canPack: Boolean get() = true

    /** Check magic bytes from the start of the file. */
    fun matches(head: ByteArray): Boolean

    /** Advanced detection for formats without magic bytes (structure check). Default: same as [matches]. */
    fun detects(src: RandomSource, head: ByteArray): Boolean = matches(head)

    fun list(src: RandomSource, opts: Options): List<Entry>
    fun extract(src: RandomSource, entry: Entry, out: OutputStream, opts: Options)
    fun pack(inputs: List<PackInput>, target: File, opts: Options, progress: Progress)

    /** Guess the 1-byte XOR key from the archive contents. null = cannot be detected / the format does not use it. */
    fun detectKey(src: RandomSource, entries: List<Entry>): Int? = null

    /** Adler32 checksum of the original file stored in the archive index (if the format has one). */
    fun expectedChecksum(entry: Entry): Long? = null
}

object FormatRegistry {
    val all: List<ArchiveFormat> by lazy {
        listOf(
            // formats with magic at the start of the file
            com.furuarc.formats.RpaFormat,
            com.furuarc.formats.Xp3Format,
            com.furuarc.formats.PfsFormat,
            com.furuarc.formats.RgssV1Format,
            com.furuarc.formats.RgssV3Format,
            com.furuarc.formats.BgiFormat,
            com.furuarc.formats.TyranoFormat,
            com.furuarc.formats.CatSystem2Format,
            com.furuarc.formats.YuRisFormat,
            com.furuarc.formats.EscudeFormat,
            com.furuarc.formats.ItufuruFormat,
            com.furuarc.formats.HexenHausArccFormat,
            com.furuarc.formats.CircusCrmFormat,
            com.furuarc.formats.HexenHausOdioFormat,
            com.furuarc.formats.HexenHausWagFormat,
            com.furuarc.formats.NoaFormat,
            com.furuarc.formats.NexasFormat,
            com.furuarc.formats.MajiroFormat,
            // magic at the end of the file (trailer)
            com.furuarc.formats.QlieFormat,
            // formats without magic bytes (detected by structure) must come last
            com.furuarc.formats.SoftpalFormat,
            com.furuarc.formats.NScripterFormat,
            com.furuarc.formats.WillPlusFormat,
            com.furuarc.formats.FavoriteFormat,
            com.furuarc.formats.CircusFormat,
            com.furuarc.formats.CircusDatFormat,
            com.furuarc.formats.SilkyFormat,
            com.furuarc.formats.SiglusPckFormat,
            com.furuarc.formats.SiglusOvkFormat,
            com.furuarc.formats.SiglusGameexeFormat,
        )
    }

    fun byId(id: String): ArchiveFormat = all.first { it.id == id }

    fun detect(src: RandomSource): ArchiveFormat? {
        val n = minOf(16L, src.length).toInt()
        if (n <= 0) return null
        val head = src.readFully(0, n)
        return all.firstOrNull { it.detects(src, head) }
    }
}
