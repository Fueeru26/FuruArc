package com.furuarc.core

/** Extra per-engine tools (outside Unpack/Pack Archive). [mode] is used as the job screen mode (20..25). */
enum class ScriptTool(val mode: Int) {
    EXPORT(20), // export script text
    IMPORT(21), // import script text
    RPYC(22), // rpyc -> rpy
    NS_DECODE(23), // nscript.dat -> 0.txt
    NS_ENCODE(24), // 0.txt -> nscript.dat
    LIST(25), // archive contents list
    ;

    companion object {
        fun ofMode(m: Int) = values().firstOrNull { it.mode == m }
    }
}

object ScriptTools {
    /** special engine id for the VN Tools > Script Tools / Unpack / Pack menu that detects the format automatically. */
    const val AUTO = "auto"

    /** Script text type for engine [engineId]; null = no text tool yet. */
    fun kindFor(engineId: String?): ScriptKind? = when (engineId) {
        "rpa" -> ScriptKind.RPY
        "xp3" -> ScriptKind.KAG
        "tyrano" -> ScriptKind.TYRANO
        "nscripter" -> ScriptKind.NSCRIPT
        "qlie" -> ScriptKind.QLIE
        "pfs" -> ScriptKind.ARTEMIS
        "catsystem2" -> ScriptKind.CST
        "siglus" -> ScriptKind.SIGLUS
        "escude" -> ScriptKind.ESCUDE
        "favorite" -> ScriptKind.HCB
        "willplus" -> ScriptKind.WS2
        "silky" -> ScriptKind.SILKY
        "circus", "circuscrm", "circusdat" -> ScriptKind.CIRCUS
        "bgi" -> ScriptKind.BGI
        "itufuru" -> ScriptKind.ITUFURU
        "entisgls" -> ScriptKind.CSX
        "nexas" -> ScriptKind.NEXAS
        "yuris" -> ScriptKind.YSTB
        else -> null
    }

    /** Tools for archive engines. The RPGM engine uses its own menu. */
    fun forEngine(engineId: String, rpgm: Boolean): List<ScriptTool> {
        if (rpgm) return emptyList()
        val r = ArrayList<ScriptTool>()
        when (engineId) {
            "rpa" -> { r.add(ScriptTool.RPYC); r.add(ScriptTool.EXPORT); r.add(ScriptTool.IMPORT) }
            "xp3", "tyrano", "qlie", "pfs", "catsystem2", "siglus", "escude", "favorite", "willplus", "silky", "circus", "circuscrm", "circusdat", "bgi", "itufuru", "entisgls", "nexas", "yuris" -> { r.add(ScriptTool.EXPORT); r.add(ScriptTool.IMPORT) }
            "nscripter" -> { r.add(ScriptTool.NS_DECODE); r.add(ScriptTool.NS_ENCODE); r.add(ScriptTool.EXPORT); r.add(ScriptTool.IMPORT) }
        }
        r.add(ScriptTool.LIST)
        return r
    }

    fun isNsDat(path: String): Boolean {
        val n = path.replace('\\', '/').substringAfterLast('/').lowercase()
        return n == "nscript.dat" || n == "nscr_sec.dat" || n == "nscript.___"
    }

    fun isNsText(path: String): Boolean {
        val n = path.replace('\\', '/').substringAfterLast('/').lowercase()
        return n == "0.txt" || n == "00.txt"
    }

    fun isRpyc(path: String): Boolean {
        val p = path.lowercase()
        return p.endsWith(".rpyc") || p.endsWith(".rpymc")
    }
}
