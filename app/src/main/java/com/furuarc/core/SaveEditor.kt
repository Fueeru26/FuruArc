package com.furuarc.core

/** A single value that can be changed in the save. [key] = location inside the JSON (separated by "/"). [type]: 0 number, 1 true/false, 2 text. */
class EditField(val key: String, val label: String, val value: String, val type: Int)

class EditGroup(val title: String, val fields: List<EditField>)

/** [id]: general, chars, items, weapons, armors, vars, switches. The "chars" section contains one group per character. */
class EditSection(val id: String, val groups: List<EditGroup>)

/** Original names from the game database (id -> name), used to label the Value Editor. */
class GameNames(
    val items: Map<Int, String> = emptyMap(),
    val weapons: Map<Int, String> = emptyMap(),
    val armors: Map<Int, String> = emptyMap(),
    val actors: Map<Int, String> = emptyMap(),
    val variables: Map<Int, String> = emptyMap(),
    val switches: Map<Int, String> = emptyMap(),
) {
    val total: Int get() = items.size + weapons.size + armors.size + actors.size + variables.size + switches.size
}

/** Result of the save structure check. [fatal]: 0 OK, 1 not a JSON object, 2 not an RPG Maker save. */
class SaveCheck(val fatal: Int, val missing: List<String>, val badTypes: List<String>)

/**
 * Value editor for RPGM MV/MZ saves (JSON): gold, characters, inventory, variables, switches.
 * Only existing values can be changed (the JSON structure does not change).
 */
object SaveEditor {
    private val PARAMS = listOf("MHP", "MMP", "ATK", "DEF", "MAT", "MDF", "AGI", "LUK")

    @Suppress("UNCHECKED_CAST")
    private fun obj(v: Any?) = v as? Map<String, Any?>

    /** Plain array, or the JsonEx wrapper {"@c":..,"@a":[..]}. Returns the list and the path prefix. */
    private fun arr(v: Any?): Pair<List<Any?>, List<String>>? {
        if (v is List<*>) return Pair(v, emptyList())
        val m = obj(v) ?: return null
        val a = m["@a"] as? List<*> ?: return null
        return Pair(a, listOf("@a"))
    }

    private fun field(path: List<String>, label: String, v: Any?): EditField? = when (v) {
        is JNum -> EditField(path.joinToString("/"), label, v.raw, 0)
        is Boolean -> EditField(path.joinToString("/"), label, v.toString(), 1)
        is String -> EditField(path.joinToString("/"), label, v, 2)
        else -> null
    }

    fun sections(root: Any?, names: GameNames? = null): List<EditSection> {
        val m = obj(root) ?: return emptyList()
        val out = ArrayList<EditSection>()

        // general
        val party = obj(m["party"])
        if (party != null) {
            val f = listOfNotNull(
                field(listOf("party", "_gold"), "Gold", party["_gold"]),
                field(listOf("party", "_steps"), "Steps", party["_steps"]),
            )
            if (f.isNotEmpty()) out.add(EditSection("general", listOf(EditGroup("", f))))
        }

        // characters
        val actors = obj(m["actors"])?.get("_data")?.let { arr(it) }
        if (actors != null) {
            val groups = ArrayList<EditGroup>()
            for ((i, a) in actors.first.withIndex()) {
                val am = obj(a) ?: continue
                val base = listOf("actors", "_data") + actors.second + i.toString()
                val fs = ArrayList<EditField>()
                for ((k, lbl) in listOf("_level" to "Level", "_hp" to "HP", "_mp" to "MP", "_tp" to "TP")) {
                    field(base + k, lbl, am[k])?.let { fs.add(it) }
                }
                obj(am["_exp"])?.let { ex ->
                    for ((k, v) in ex) if (!k.startsWith("@")) field(base + "_exp" + k, "EXP (class $k)", v)?.let { fs.add(it) }
                }
                arr(am["_paramPlus"])?.let { (list, pre) ->
                    for ((pi, v) in list.withIndex()) {
                        field(base + "_paramPlus" + pre + pi.toString(), "+" + (PARAMS.getOrNull(pi) ?: "P$pi"), v)?.let { fs.add(it) }
                    }
                }
                if (fs.isEmpty()) continue
                val name = ((am["_name"] as? String) ?: "").ifEmpty { names?.actors?.get(i) ?: "" }
                groups.add(EditGroup("#$i" + if (name.isNotEmpty()) " — $name" else "", fs))
            }
            if (groups.isNotEmpty()) out.add(EditSection("chars", groups))
        }

        // inventory (if database names are known, items not yet owned are shown too with count 0)
        if (party != null) {
            val inv = listOf(
                arrayOf("items", "_items", "Item", names?.items),
                arrayOf("weapons", "_weapons", "Weapon", names?.weapons),
                arrayOf("armors", "_armors", "Armor", names?.armors),
            )
            for (row in inv) {
                val id = row[0] as String
                val key = row[1] as String
                val lbl = row[2] as String
                @Suppress("UNCHECKED_CAST")
                val nm = row[3] as Map<Int, String>?
                val mp = obj(party[key])
                if (mp == null && nm.isNullOrEmpty()) continue
                val fs = ArrayList<EditField>()
                val have = HashSet<Int>()
                if (mp != null) {
                    val keys = mp.keys.filter { !it.startsWith("@") }.sortedBy { it.toIntOrNull() ?: Int.MAX_VALUE }
                    for (k in keys) {
                        k.toIntOrNull()?.let { have.add(it) }
                        val n = k.toIntOrNull()?.let { nm?.get(it) }
                        field(listOf("party", key, k), if (n.isNullOrEmpty()) "$lbl #$k" else "$n (#$k)", mp[k])?.let { fs.add(it) }
                    }
                }
                if (mp != null && nm != null) {
                    for ((i, n) in nm.entries.sortedBy { it.key }) {
                        if (i in have || n.isEmpty()) continue
                        fs.add(EditField("party/$key/$i", "$n (#$i)", "0", 0))
                    }
                }
                if (fs.isNotEmpty()) out.add(EditSection(id, listOf(EditGroup("", fs))))
            }
        }

        // variables & switches
        for ((id, key, lbl) in listOf(Triple("vars", "variables", "Variable"), Triple("switches", "switches", "Switch"))) {
            val nm = if (id == "vars") names?.variables else names?.switches
            val a = obj(m[key])?.get("_data")?.let { arr(it) } ?: continue
            val fs = ArrayList<EditField>()
            for ((i, v) in a.first.withIndex()) {
                if (v == null) continue
                val n = nm?.get(i)
                field(listOf(key, "_data") + a.second + i.toString(), if (n.isNullOrEmpty()) "$lbl #$i" else "$n (#$i)", v)?.let { fs.add(it) }
            }
            if (fs.isNotEmpty()) out.add(EditSection(id, listOf(EditGroup("", fs))))
        }
        return out
    }

    private val NUM = Regex("-?\\d+(\\.\\d+)?([eE][+-]?\\d+)?")

    /**
     * Apply [edits] (key -> new text) to [root]. Returns the number of values changed.
     * Invalid numbers throw IllegalArgumentException(key).
     */
    @Suppress("UNCHECKED_CAST")
    fun apply(root: Any?, edits: Map<String, String>): Int {
        var n = 0
        for ((key, text) in edits) {
            val segs = key.split("/")
            var cur: Any? = root
            for (s in segs.dropLast(1)) {
                cur = when (cur) {
                    is Map<*, *> -> cur[s]
                    is List<*> -> cur.getOrNull(s.toIntOrNull() ?: -1)
                    else -> null
                }
            }
            val last = segs.last()
            val old: Any? = when (cur) {
                is Map<*, *> -> cur[last]
                is List<*> -> cur.getOrNull(last.toIntOrNull() ?: -1)
                else -> null
            }
            // items/weapons/armors not yet owned: count > 0 adds them to the inventory
            if (old == null && cur is MutableMap<*, *> && segs.size == 3 && segs[0] == "party" &&
                (segs[1] == "_items" || segs[1] == "_weapons" || segs[1] == "_armors")
            ) {
                val t = text.trim()
                if (!NUM.matches(t)) throw IllegalArgumentException(key)
                if (t == "0") continue
                (cur as MutableMap<String, Any?>)[last] = JNum(t)
                n++
                continue
            }
            val nv: Any? = when (old) {
                is JNum -> {
                    val t = text.trim()
                    if (!NUM.matches(t)) throw IllegalArgumentException(key)
                    if (t == old.raw) continue
                    JNum(t)
                }
                is Boolean -> {
                    val b = text.trim().lowercase() == "true"
                    if (b == old) continue
                    b
                }
                is String -> if (text == old) continue else text
                else -> continue
            }
            when (cur) {
                is MutableMap<*, *> -> (cur as MutableMap<String, Any?>)[last] = nv
                is MutableList<*> -> (cur as MutableList<Any?>)[last.toInt()] = nv
                else -> continue
            }
            n++
        }
        return n
    }

    /** Check the structure of an RPG Maker (MV/MZ) save JSON. Changes nothing. */
    fun validate(root: Any?): SaveCheck {
        val m = obj(root) ?: return SaveCheck(1, emptyList(), emptyList())
        val expected = listOf("system", "party", "actors", "variables", "switches")
        val missing = expected.filter { !m.containsKey(it) }
        val known = expected + listOf("map", "screen", "messages", "selfSwitches", "troop", "player", "timer", "mapId")
        if (known.count { m.containsKey(it) } < 2 || missing.size >= 4) return SaveCheck(2, missing, emptyList())
        val bad = ArrayList<String>()
        val party = m["party"]
        if (party != null && obj(party) == null) bad.add("party")
        obj(party)?.get("_gold")?.let { if (it !is JNum) bad.add("party/_gold") }
        for (k in listOf("variables", "switches", "actors")) {
            val v = m[k] ?: continue
            val d = obj(v)?.get("_data")
            if (d != null && arr(d) == null) bad.add("$k/_data")
        }
        return SaveCheck(0, missing, bad)
    }

    private fun dbNames(text: String?): Map<Int, String> {
        if (text == null) return emptyMap()
        val a = try {
            Json.parse(text) as? List<*>
        } catch (e: Exception) {
            null
        } ?: return emptyMap()
        val r = LinkedHashMap<Int, String>()
        for ((i, o) in a.withIndex()) {
            val n = (obj(o)?.get("name") as? String)?.trim() ?: continue
            if (n.isNotEmpty()) r[i] = n
        }
        return r
    }

    private fun listNames(v: Any?): Map<Int, String> {
        val a = v as? List<*> ?: return emptyMap()
        val r = LinkedHashMap<Int, String>()
        for ((i, o) in a.withIndex()) (o as? String)?.trim()?.takeIf { it.isNotEmpty() }?.let { r[i] = it }
        return r
    }

    /** Read names from the game database file contents (JSON text; null = file does not exist). */
    fun namesFrom(system: String?, items: String?, weapons: String?, armors: String?, actors: String?): GameNames {
        val sys = try {
            if (system == null) null else obj(Json.parse(system))
        } catch (e: Exception) {
            null
        }
        return GameNames(
            items = dbNames(items), weapons = dbNames(weapons), armors = dbNames(armors), actors = dbNames(actors),
            variables = listNames(sys?.get("variables")), switches = listNames(sys?.get("switches")),
        )
    }
}
