package com.furuarc.ui

/**
 * Supported engines.
 * [id]       = engine id (stored in state so the screen can be restored).
 * [formatId] = format id in FormatRegistry (for archive-based engines).
 * [defaultOut] = output file name of Pack.
 * [rpgm]     = RPG Maker engine (RPGM Tools > Engine List).
 * [crypt]    = RPGM MV/MZ: assets are encrypted per file (not an archive), so its menu is Decrypt/Encrypt.
 */
class VnEngine(
    val id: String,
    val badge: String,
    val name: String,
    val ext: String,
    val defaultOut: String,
    val formatId: String = id,
    val rpgm: Boolean = false,
    val crypt: Boolean = false,
    /** Variant of another engine: not shown in the Engine List, its menu is in the parent engine's menu. */
    val parent: String? = null,
    /** Caption in the variant menu title, e.g. "Gameexe.dat". */
    val suffix: String = "",
)


val VN_ENGINES = listOf(
    VnEngine("pfs", "PFS", "Artemis", "pfs", "root.pfs"),
    VnEngine("xp3", "XP3", "Kirikiri", "xp3", "data.xp3"),
    VnEngine("rpa", "RPA", "Renpy", "rpa", "archive.rpa"),
    VnEngine("bgi", "BGI", "BGI / Ethornell", "arc", "data.arc"),
    VnEngine("softpal", "PAC", "Softpal ADV System", "pac", "data.pac"),
    VnEngine("willplus", "ARC", "WillPlus / AdvHD", "arc", "Rio.arc"),
    VnEngine("nscripter", "SAR", "NScripter", "sar", "arc.sar"),
    VnEngine("tyrano", "NW", "Tyrano Script", "nw", "package.nw"),
    VnEngine("favorite", "BIN", "FVP (Favorite)", "bin", "data.bin"),
    VnEngine("circus", "PCK", "Adv32 (Circus)", "pck", "data.pck"),
    VnEngine("silky", "ARC", "Silky Engine", "arc", "data.arc"),
    VnEngine("catsystem2", "INT", "CatSystem 2", "int", "data.int"),
    VnEngine("siglus", "PCK", "Siglus Engine", "pck", "Scene.pck"),
    VnEngine("siglus_gameexe", "DAT", "Siglus Engine (Gameexe.dat)", "dat", "Gameexe.dat", formatId = "siglus_gameexe", parent = "siglus", suffix = "Gameexe.dat"),
    VnEngine("siglus_ovk", "OVK", "Siglus Engine (Voice OVK)", "ovk", "z0001.ovk", formatId = "siglus_ovk", parent = "siglus", suffix = "Voice OVK"),
    VnEngine("qlie", "PACK", "QLIE", "pack", "data.pack"),
    VnEngine("yuris", "YPF", "Yu-Ris", "ypf", "data.ypf"),
    VnEngine("escude", "BIN", "Escu:de", "bin", "data.bin"),
    VnEngine("itufuru", "SCD", "Yaneurao Itufuru", "scd", "data.scd"),
    VnEngine("entisgls", "NOA", "EntisGLS", "noa", "data.noa", formatId = "noa"),
    VnEngine("nexas", "PAC", "NeXAS", "pac", "Script.pac"),
    VnEngine("majiro", "ARC", "Majiro", "arc", "data.arc"),
    VnEngine("circusdat", "DAT", "Adv32 (Circus) DAT", "dat", "data.dat", formatId = "circusdat", parent = "circus", suffix = "DAT"),
    VnEngine("circuscrm", "CRM", "Adv32 (Circus) CRM", "crm", "data.crm", formatId = "circuscrm", parent = "circus", suffix = "CRM"),
    VnEngine("arcc", "ARC", "HexenHaus", "arc", "data.arc"),
    VnEngine("odio", "ODIO", "HexenHaus (ODIO)", "dat", "audio.dat", formatId = "odio", parent = "arcc", suffix = "ODIO"),
    VnEngine("wag", "WAG", "HexenHaus (WAG)", "wag", "data.wag", formatId = "wag", parent = "arcc", suffix = "WAG"),
)

val RPGM_ENGINES = listOf(
    VnEngine("rpgm_mv", "MV", "RPGM MV", "", "", rpgm = true, crypt = true),
    VnEngine("rpgm_mz", "MZ", "RPGM MZ", "", "", rpgm = true, crypt = true),
    VnEngine("rpgm_xp", "XP", "RPGM XP", "rgssad", "Game.rgssad", formatId = "rgss1", rpgm = true),
    VnEngine("rpgm_vx", "VX", "RPGM VX", "rgss2a", "Game.rgss2a", formatId = "rgss1", rpgm = true),
    VnEngine("rpgm_vxa", "VA", "RPGM VX ACE", "rgss3a", "Game.rgss3a", formatId = "rgss3", rpgm = true),
)

fun vnEngine(id: String?): VnEngine =
    VN_ENGINES.firstOrNull { it.id == id } ?: RPGM_ENGINES.firstOrNull { it.id == id } ?: VN_ENGINES[0]
