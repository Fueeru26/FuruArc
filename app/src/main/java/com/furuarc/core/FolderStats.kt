package com.furuarc.core

/** One engine detected in a folder: marker file count and sample names. */
class EngineHit(val name: String, val count: Int, val samples: List<String>)

/** Folder content summary: size, count, largest files, most common extensions, and detected game engines. */
class FolderStats(
    val folders: Int,
    val files: Int,
    val bytes: Long,
    val emptyFiles: Int,
    val largest: List<Pair<String, Long>>,
    val exts: List<Pair<String, Int>>,
    val engines: List<EngineHit>,
)

/** Pure logic (no Android) so it is testable. Engine detection uses marker file names/extensions. */
object FolderAnalyzer {
    private class Rule(val name: String, val match: (String) -> Boolean)

    private fun ext(p: String) = p.substringAfterLast('.', "")
    private fun base(p: String) = p.substringAfterLast('/')
    private fun inDir(p: String, d: String) = p.startsWith("$d/") || p.contains("/$d/")

    private val RULES: List<Rule> = listOf(
        Rule("RPG Maker MV/MZ") { p ->
            ext(p) in setOf("rpgmvp", "rpgmvm", "rpgmvo", "rpgmvw", "png_", "ogg_", "m4a_") ||
                base(p) in setOf("rpg_core.js", "rpg_managers.js", "rmmz_core.js", "rmmz_managers.js")
        },
        Rule("RPG Maker XP/VX/VX Ace") { p -> ext(p) in setOf("rgssad", "rgss2a", "rgss3a", "rxdata", "rvdata", "rvdata2") },
        Rule("Ren'Py") { p -> ext(p) in setOf("rpa", "rpyc", "rpy", "rpymc") || inDir(p, "renpy") },
        Rule("TyranoScript") { p -> inDir(p, "tyrano") || base(p) == "tyrano.js" || base(p) == "tyrano_player.js" },
        Rule("Kirikiri / KAG") { p -> ext(p) in setOf("xp3", "tjs", "ks", "ksd") && !inDir(p, "tyrano") && !inDir(p, "data/scenario") || base(p) == "startup.tjs" },
        Rule("NScripter / ONScripter") { p -> ext(p) in setOf("nsa", "sar", "ns2", "ns3") || base(p) in setOf("nscript.dat", "nscr_sec.dat", "arc.nsa") },
        Rule("Siglus Engine") { p -> base(p) in setOf("scene.pck", "gameexe.dat") || ext(p) == "ovk" },
        Rule("Artemis") { p -> ext(p) in setOf("pfs", "ast", "iet") },
        Rule("CatSystem2") { p -> ext(p) in setOf("int", "cst", "fes") },
        Rule("Yu-Ris") { p -> ext(p) in setOf("ypf", "ybn") },
        Rule("Softpal ADV System") { p -> base(p) in setOf("script.src", "point.dat", "text.dat", "data.pac") },
        Rule("NeXAS") { p -> (ext(p) == "pac" && base(p) != "data.pac") || ext(p) in setOf("datu8", "binu8", "spm") },
        Rule("EntisGLS") { p -> ext(p) in setOf("noa", "csx", "eri", "mio") },
        Rule("AGE (Eushully)") { p -> ext(p) in setOf("alf", "agf", "aai") || base(p) in setOf("sys4ini.bin", "sys5ini.bin", "sys3ini.bin") },
        Rule("Shiina Rio") { p -> ext(p) in setOf("war", "warc", "s25") },
        Rule("Majiro") { p -> ext(p) in setOf("mjo", "mjs") },
        Rule("FVP (Favorite)") { p -> ext(p) == "hcb" },
        Rule("WillPlus / AdvHD") { p -> ext(p) in setOf("ws2", "wsc", "wip") },
        Rule("QLIE") { p -> ext(p) == "pack" && !inDir(p, "godot") },
        Rule("Adv32 (Circus)") { p -> ext(p) == "crm" },
        Rule("Wolf RPG") { p -> ext(p) == "wolf" || base(p) == "game.wolf" },
        Rule("Unity") { p -> base(p) == "unityplayer.dll" || base(p) in setOf("globalgamemanagers", "data.unity3d") || ext(p) in setOf("unity3d", "assetbundle") || p.contains("_data/managed/") },
        Rule("Godot") { p -> inDir(p, ".godot") || base(p) == "project.godot" },
        Rule("Flash (SWF)") { p -> ext(p) == "swf" },
    )

    fun analyze(files: List<Pair<String, Long>>, dirCount: Int, idl: Boolean = true, extraEngines: Map<String, List<String>> = emptyMap()): FolderStats {
        var bytes = 0L
        var empty = 0
        val extCount = HashMap<String, Int>()
        for ((path, size) in files) {
            bytes += size
            if (size == 0L) empty++
            val e = ext(base(path)).lowercase()
            val k = if (e.isEmpty()) (if (idl) "(tanpa ekstensi)" else "(no extension)") else ".$e"
            extCount[k] = (extCount[k] ?: 0) + 1
        }
        val largest = files.sortedByDescending { it.second }.take(5).filter { it.second > 0 }
        val exts = extCount.entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key }).take(8).map { Pair(it.key, it.value) }
        val hits = LinkedHashMap<String, MutableList<String>>()
        for (r in RULES) {
            val m = ArrayList<String>()
            for ((path, _) in files) if (r.match(path.lowercase())) m.add(path)
            if (m.isNotEmpty()) hits[r.name] = m
        }
        // engines recognized from file contents (archive format detected during scanning)
        for ((name, list) in extraEngines) {
            val cur = hits.getOrPut(name) { ArrayList() }
            for (p in list) if (p !in cur) cur.add(p)
        }
        val engines = hits.entries.map { EngineHit(it.key, it.value.size, it.value.take(3).map { s -> s }) }
            .sortedByDescending { it.count }
        return FolderStats(dirCount, files.size, bytes, empty, largest, exts, engines)
    }
}
