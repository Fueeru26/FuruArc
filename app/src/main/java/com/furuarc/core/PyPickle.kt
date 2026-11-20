package com.furuarc.core

import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Python 2 string (raw bytes). */
class PyBytes(val bytes: ByteArray) {
    fun text(): String = String(bytes, Charsets.UTF_8)
}

/** Python tuple (distinguished from a plain list). */
class PyTuple(private val v: List<Any?>) : AbstractList<Any?>() {
    override val size: Int get() = v.size
    override fun get(index: Int): Any? = v[index]
}

/** Reference to a global class/function (e.g. "renpy.ast.Say"). */
class PyGlobal(val module: String, val name: String) {
    val full: String get() = "$module.$name"
}

/**
 * Generic Python object produced by unpickling: class name, constructor arguments, attributes (__dict__ / state dict),
 * plus the contents if the object resembles a list ([items]) or dict ([entries]).
 */
class PyObj(val cls: String, val args: List<Any?> = emptyList()) {
    val attrs = LinkedHashMap<String, Any?>()
    var state: Any? = null
    val items = ArrayList<Any?>()
    val entries = LinkedHashMap<Any?, Any?>()

    val shortName: String get() = cls.substringAfterLast('.')
    operator fun get(name: String): Any? = attrs[name]
    override fun toString() = "PyObj($cls)"
}

/**
 * Pickle reader (binary protocol 0 through 4) for Python/Ren'Py files. Executes nothing:
 * classes and functions are only recorded by name ([PyGlobal], [PyObj]). Tuple = [PyTuple], list = MutableList,
 * dict = MutableMap, Python 2 str = [PyBytes], unicode = String.
 */
object PyPickle {
    private object Mark

    fun parse(data: ByteArray): Any? {
        val bb = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)
        val st = ArrayList<Any?>()
        val memo = HashMap<Int, Any?>()
        var memoIdx = 0

        fun pop(): Any? = st.removeAt(st.size - 1)
        fun popMark(): List<Any?> {
            val i = st.lastIndexOf(Mark)
            require(i >= 0) { "pickle: MARK tidak ada" }
            val items = ArrayList(st.subList(i + 1, st.size))
            while (st.size > i) st.removeAt(st.size - 1)
            return items
        }
        fun bytes(n: Int): ByteArray {
            require(n >= 0 && n <= bb.remaining()) { "pickle terpotong" }
            val b = ByteArray(n); bb.get(b); return b
        }
        fun line(): String {
            val sb = StringBuilder()
            while (true) {
                val c = bb.get().toInt() and 0xff
                if (c == '\n'.code) break
                sb.append(c.toChar())
            }
            return sb.toString()
        }
        fun appendTo(target: Any?, vs: List<Any?>) {
            @Suppress("UNCHECKED_CAST")
            when (target) {
                is MutableList<*> -> (target as MutableList<Any?>).addAll(vs)
                is PyObj -> target.items.addAll(vs)
                else -> throw IllegalStateException("pickle: APPEND ke tipe tak dikenal")
            }
        }
        fun setItems(target: Any?, kv: List<Any?>) {
            var i = 0
            @Suppress("UNCHECKED_CAST")
            while (i + 1 < kv.size) {
                when (target) {
                    is MutableMap<*, *> -> (target as MutableMap<Any?, Any?>)[kv[i]] = kv[i + 1]
                    is PyObj -> target.entries[kv[i]] = kv[i + 1]
                    else -> throw IllegalStateException("pickle: SETITEMS ke tipe tak dikenal")
                }
                i += 2
            }
        }
        fun build(obj: Any?, state: Any?) {
            if (obj !is PyObj) return
            @Suppress("UNCHECKED_CAST")
            when {
                state is Map<*, *> -> for ((k, v) in state) if (k is String) obj.attrs[k] = v
                state is PyTuple && state.size == 2 && (state[0] == null || state[0] is Map<*, *>) && state[1] is Map<*, *> -> {
                    for (m in listOf(state[0], state[1])) (m as? Map<*, *>)?.forEach { (k, v) -> if (k is String) obj.attrs[k] = v }
                }
                else -> obj.state = state
            }
            if (obj.shortName == "PyCode" && state is List<*> && state.size >= 4) {
                obj.attrs["source"] = state[1]
                obj.attrs["location"] = state[2]
                obj.attrs["mode"] = state[3]
            }
        }
        fun mk(cls: Any?, args: List<Any?>): Any? {
            val name = (cls as? PyGlobal)?.full ?: cls.toString()
            return when (name) {
                "__builtin__.list", "builtins.list" -> ArrayList<Any?>().also { l -> (args.firstOrNull() as? List<*>)?.let { l.addAll(it) } }
                "__builtin__.dict", "builtins.dict" -> LinkedHashMap<Any?, Any?>()
                else -> PyObj(name, args)
            }
        }

        while (true) {
            when (val op = bb.get().toInt() and 0xff) {
                0x80 -> bb.get()
                0x95 -> bb.position(bb.position() + 8)
                '}'.code -> st.add(LinkedHashMap<Any?, Any?>())
                ']'.code -> st.add(ArrayList<Any?>())
                ')'.code -> st.add(PyTuple(emptyList()))
                '('.code -> st.add(Mark)
                'N'.code -> st.add(null)
                0x88 -> st.add(true)
                0x89 -> st.add(false)
                'K'.code -> st.add((bb.get().toInt() and 0xff).toLong())
                'M'.code -> st.add((bb.short.toInt() and 0xffff).toLong())
                'J'.code -> st.add(bb.int.toLong())
                0x8a -> { val n = bb.get().toInt() and 0xff; st.add(longLE(bytes(n))) }
                0x8b -> { val n = bb.int; st.add(longLE(bytes(n))) }
                'G'.code -> {
                    val b = bytes(8)
                    st.add(ByteBuffer.wrap(b).order(ByteOrder.BIG_ENDIAN).double)
                }
                'X'.code -> st.add(String(bytes(bb.int), Charsets.UTF_8))
                0x8c -> st.add(String(bytes(bb.get().toInt() and 0xff), Charsets.UTF_8))
                0x8d -> st.add(PyBytes(bytes(bb.long.toInt())))
                'U'.code -> st.add(PyBytes(bytes(bb.get().toInt() and 0xff)))
                'T'.code -> st.add(PyBytes(bytes(bb.int)))
                'C'.code -> st.add(PyBytes(bytes(bb.get().toInt() and 0xff)))
                'B'.code -> st.add(PyBytes(bytes(bb.int)))
                'c'.code -> { val m = line(); val n = line(); st.add(PyGlobal(m, n)) }
                0x93 -> { val n = pop() as String; val m = pop() as String; st.add(PyGlobal(m, n)) }
                0x85 -> { val a = pop(); st.add(PyTuple(listOf(a))) }
                0x86 -> { val b = pop(); val a = pop(); st.add(PyTuple(listOf(a, b))) }
                0x87 -> { val c = pop(); val b = pop(); val a = pop(); st.add(PyTuple(listOf(a, b, c))) }
                't'.code -> st.add(PyTuple(popMark()))
                'l'.code -> st.add(ArrayList(popMark()))
                'd'.code -> { val m = LinkedHashMap<Any?, Any?>(); setItems(m, popMark()); st.add(m) }
                'a'.code -> { val v = pop(); appendTo(st.last(), listOf(v)) }
                'e'.code -> { val items = popMark(); appendTo(st.last(), items) }
                's'.code -> { val v = pop(); val k = pop(); setItems(st.last(), listOf(k, v)) }
                'u'.code -> { val items = popMark(); setItems(st.last(), items) }
                0x81 -> { // NEWOBJ
                    val args = pop(); val cls = pop()
                    st.add(mk(cls, (args as? List<Any?>) ?: emptyList()))
                }
                0x92 -> { // NEWOBJ_EX
                    pop(); val args = pop(); val cls = pop()
                    st.add(mk(cls, (args as? List<Any?>) ?: emptyList()))
                }
                'R'.code -> { val args = pop(); val cls = pop(); st.add(mk(cls, (args as? List<Any?>) ?: emptyList())) }
                'b'.code -> { val s = pop(); build(st.last(), s) }
                0x8f -> st.add(PyObj("__builtin__.set"))
                0x90 -> { val items = popMark(); appendTo(st.last(), items) }
                0x91 -> st.add(PyObj("__builtin__.frozenset").also { it.items.addAll(popMark()) })
                '0'.code -> pop()
                '1'.code -> popMark()
                '2'.code -> st.add(st.last())
                'q'.code -> memo[bb.get().toInt() and 0xff] = st.last()
                'r'.code -> memo[bb.int] = st.last()
                0x94 -> { memo[memoIdx++] = st.last() }
                'h'.code -> st.add(memo[bb.get().toInt() and 0xff])
                'j'.code -> st.add(memo[bb.int])
                '.'.code -> return st.last()
                else -> throw IllegalStateException("Opcode pickle tidak didukung: 0x${op.toString(16)}")
            }
        }
    }

    private fun longLE(b: ByteArray): Long {
        var v = 0L
        for (i in b.indices.reversed()) v = (v shl 8) or (b[i].toLong() and 0xff)
        if (b.isNotEmpty() && b.size < 8 && (b.last().toInt() and 0x80) != 0) v -= (1L shl (8 * b.size))
        return v
    }
}
