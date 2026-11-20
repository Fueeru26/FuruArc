package com.furuarc.core

import java.math.BigDecimal

/**
 * JSON tree operations for the Text File Editor (Structured Text). Uses the [Json] model:
 * object = LinkedHashMap (key order preserved), array = ArrayList, number = [JNum] (original text preserved).
 * The path to a node = a list of keys (String) and indices (Int).
 */
object JsonTree {
    const val OBJECT = "object"
    const val ARRAY = "array"
    const val TEXT = "text"
    const val NUMBER = "number"
    const val BOOLEAN = "boolean"
    const val NULL = "null"

    val TYPES = listOf(TEXT, NUMBER, BOOLEAN, NULL, OBJECT, ARRAY)

    fun typeOf(v: Any?): String = when (v) {
        null -> NULL
        is Map<*, *> -> OBJECT
        is List<*> -> ARRAY
        is Boolean -> BOOLEAN
        is JNum, is Number -> NUMBER
        else -> TEXT
    }

    fun isContainer(v: Any?): Boolean = v is Map<*, *> || v is List<*>

    @Suppress("UNCHECKED_CAST")
    fun obj(v: Any?): LinkedHashMap<String, Any?> = v as LinkedHashMap<String, Any?>

    @Suppress("UNCHECKED_CAST")
    fun arr(v: Any?): ArrayList<Any?> = v as ArrayList<Any?>

    fun size(v: Any?): Int = when (v) {
        is Map<*, *> -> v.size
        is List<*> -> v.size
        else -> 0
    }

    /** Direct children: key (String) or index (Int) with their values. */
    fun children(v: Any?): List<Pair<Any, Any?>> = when (v) {
        is Map<*, *> -> v.entries.map { Pair<Any, Any?>(it.key as String, it.value) }
        is List<*> -> v.mapIndexed { i, x -> Pair<Any, Any?>(i, x) }
        else -> emptyList()
    }

    fun exists(root: Any?, path: List<Any>): Boolean {
        var cur = root
        for (k in path) {
            cur = when {
                cur is Map<*, *> && k is String -> if (cur.containsKey(k)) cur[k] else return false
                cur is List<*> && k is Int -> if (k in 0 until cur.size) cur[k] else return false
                else -> return false
            }
        }
        return true
    }

    fun get(root: Any?, path: List<Any>): Any? {
        var cur = root
        for (k in path) {
            cur = when {
                cur is Map<*, *> && k is String -> cur[k]
                cur is List<*> && k is Int -> if (k in 0 until cur.size) cur[k] else null
                else -> return null
            }
        }
        return cur
    }

    private val IDENT = Regex("[A-Za-z_\\$][A-Za-z0-9_\\$]*")

    /** Path in dot/bracket notation, e.g. events[3].pages[0]. */
    fun pathString(path: List<Any>): String {
        val sb = StringBuilder()
        for (k in path) {
            if (k is Int) {
                sb.append('[').append(k).append(']')
            } else {
                val s = k as String
                if (IDENT.matches(s)) {
                    if (sb.isNotEmpty()) sb.append('.')
                    sb.append(s)
                } else {
                    sb.append("[\"").append(s.replace("\\", "\\\\").replace("\"", "\\\"")).append("\"]")
                }
            }
        }
        return sb.toString()
    }

    private fun clip(s: String, max: Int): String {
        val one = s.replace("\n", "↵").replace("\r", "")
        return if (one.length > max) one.substring(0, max) + "…" else one
    }

    /** Compact display of a scalar value: text in quotes (empty string shown as ""), number/boolean/null as is. */
    fun scalarText(v: Any?, max: Int = 160): String = when (v) {
        null -> "null"
        is String -> "\"" + clip(v, max) + "\""
        is JNum -> v.raw
        is Boolean -> if (v) "true" else "false"
        is Map<*, *> -> "{…}"
        is List<*> -> "[…]"
        else -> v.toString()
    }

    /** Preview of a container's contents: the first few entries, e.g. name: "", pan: 0. */
    fun inner(v: Any?, max: Int = 90): String {
        val sb = StringBuilder()
        var n = 0
        val items: List<Pair<String?, Any?>> = when (v) {
            is Map<*, *> -> v.entries.take(5).map { Pair<String?, Any?>(it.key as String, it.value) }
            is List<*> -> v.take(5).map { Pair<String?, Any?>(null, it) }
            else -> emptyList()
        }
        for ((k, x) in items) {
            if (n > 0) sb.append(", ")
            if (k != null) sb.append(k).append(": ")
            sb.append(scalarText(x, 20))
            n++
            if (sb.length > max) break
        }
        val total = size(v)
        if (total > n || sb.length > max) sb.append(", …")
        return clip(sb.toString(), max + 6)
    }

    fun defaultFor(type: String): Any? = when (type) {
        OBJECT -> LinkedHashMap<String, Any?>()
        ARRAY -> ArrayList<Any?>()
        TEXT -> ""
        NUMBER -> JNum("0")
        BOOLEAN -> false
        else -> null
    }

    private val NUM = Regex("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")

    fun isNumber(s: String): Boolean = NUM.matches(s.trim())

    /** Convert the value type as far as possible (numeric text -> number, number -> text, etc.). */
    fun convert(v: Any?, type: String): Any? {
        if (typeOf(v) == type) return v
        return when (type) {
            TEXT -> when (v) {
                null -> ""
                is JNum -> v.raw
                is Boolean -> v.toString()
                else -> Json.write(v, false)
            }
            NUMBER -> when (v) {
                is String -> if (isNumber(v)) JNum(v.trim()) else JNum("0")
                is Boolean -> JNum(if (v) "1" else "0")
                else -> JNum("0")
            }
            BOOLEAN -> when (v) {
                is String -> v.trim().equals("true", true)
                is JNum -> (v.raw.toDoubleOrNull() ?: 0.0) != 0.0
                else -> false
            }
            NULL -> null
            OBJECT -> {
                val m = LinkedHashMap<String, Any?>()
                if (v is List<*>) v.forEachIndexed { i, x -> m[i.toString()] = x }
                m
            }
            ARRAY -> {
                val l = ArrayList<Any?>()
                if (v is Map<*, *>) for (x in v.values) l.add(x)
                l
            }
            else -> v
        }
    }

    /** Add/subtract [step] to the number [raw] (BigDecimal so it does not become scientific notation); null if invalid. */
    fun step(raw: String, step: String, sign: Int): String? = try {
        val a = BigDecimal(raw.trim())
        val b = BigDecimal(step.trim())
        (if (sign >= 0) a.add(b) else a.subtract(b)).toPlainString()
    } catch (e: Exception) {
        null
    }

    fun deepCopy(v: Any?): Any? = when (v) {
        is Map<*, *> -> {
            val m = LinkedHashMap<String, Any?>()
            for ((k, x) in v) m[k as String] = deepCopy(x)
            m
        }
        is List<*> -> {
            val l = ArrayList<Any?>(v.size)
            for (x in v) l.add(deepCopy(x))
            l
        }
        else -> v
    }

    private fun rebuild(map: LinkedHashMap<String, Any?>, entries: List<Pair<String, Any?>>) {
        map.clear()
        for ((k, x) in entries) map[k] = x
    }

    private fun entriesOf(map: LinkedHashMap<String, Any?>): List<Pair<String, Any?>> =
        map.entries.map { Pair(it.key, it.value) }

    fun put(container: Any?, key: Any, value: Any?) {
        when {
            container is Map<*, *> && key is String -> obj(container)[key] = value
            container is List<*> && key is Int -> arr(container)[key] = value
        }
    }

    fun remove(container: Any?, key: Any) {
        when {
            container is Map<*, *> && key is String -> obj(container).remove(key)
            container is List<*> && key is Int -> if (key in 0 until size(container)) arr(container).removeAt(key)
        }
    }

    /** Rename a key without changing the order. false if the new name is already used by another key. */
    fun renameKey(map: LinkedHashMap<String, Any?>, old: String, new: String): Boolean {
        if (old == new) return true
        if (map.containsKey(new)) return false
        rebuild(map, entriesOf(map).map { if (it.first == old) Pair(new, it.second) else it })
        return true
    }

    fun uniqueKey(map: Map<String, Any?>, base: String): String {
        var k = base + "_copy"
        var i = 2
        while (map.containsKey(k)) { k = base + "_copy" + i; i++ }
        return k
    }

    /** Insert a copy of [value] right after [after] (object: with key [newKey]). */
    fun insertAfter(container: Any?, after: Any, newKey: String, value: Any?) {
        if (container is Map<*, *>) {
            val m = obj(container)
            val es = entriesOf(m).toMutableList()
            val i = es.indexOfFirst { it.first == after }
            es.add(if (i < 0) es.size else i + 1, Pair(newKey, value))
            rebuild(m, es)
        } else if (container is List<*>) {
            val l = arr(container)
            val i = (after as? Int) ?: (l.size - 1)
            l.add((i + 1).coerceIn(0, l.size), value)
        }
    }

    /** Move a child up (-1) or down (+1). false if already at the end. */
    fun move(container: Any?, key: Any, delta: Int): Boolean {
        if (container is List<*> && key is Int) {
            val l = arr(container)
            val j = key + delta
            if (key !in l.indices || j !in l.indices) return false
            java.util.Collections.swap(l, key, j)
            return true
        }
        if (container is Map<*, *> && key is String) {
            val m = obj(container)
            val es = entriesOf(m).toMutableList()
            val i = es.indexOfFirst { it.first == key }
            val j = i + delta
            if (i < 0 || j !in es.indices) return false
            java.util.Collections.swap(es, i, j)
            rebuild(m, es)
            return true
        }
        return false
    }

    fun sortKeys(map: LinkedHashMap<String, Any?>) {
        rebuild(map, entriesOf(map).sortedWith(compareBy({ it.first.lowercase() }, { it.first })))
    }

    /** Add a new child. Object: with key [key]; array: appended at the end. */
    fun add(container: Any?, key: String, value: Any?) {
        if (container is Map<*, *>) obj(container)[key] = value
        else if (container is List<*>) arr(container).add(value)
    }

    /** Search for a key or scalar value containing [q]; result = paths of matching nodes (at most [limit]). */
    fun search(root: Any?, q: String, caseSensitive: Boolean, limit: Int = 300): List<List<Any>> {
        val out = ArrayList<List<Any>>()
        if (q.isEmpty()) return out
        val path = ArrayList<Any>()
        fun hit(s: String) = s.contains(q, ignoreCase = !caseSensitive)
        fun walk(v: Any?) {
            if (out.size >= limit) return
            for ((k, x) in children(v)) {
                if (out.size >= limit) return
                path.add(k)
                val keyHit = k is String && hit(k)
                val valHit = !isContainer(x) && hit(
                    when (x) { null -> "null"; is JNum -> x.raw; else -> x.toString() },
                )
                if (keyHit || valHit) out.add(ArrayList(path))
                if (isContainer(x)) walk(x)
                path.removeAt(path.size - 1)
            }
        }
        walk(root)
        return out
    }
}
