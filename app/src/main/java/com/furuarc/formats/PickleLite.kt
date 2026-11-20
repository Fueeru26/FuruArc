package com.furuarc.formats

import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** Old Python 2 string (raw bytes). */
class PyBytes(val bytes: ByteArray)

/**
 * Minimal pickle reader (protocols 2-4), enough for the RPA index:
 * dict { name -> [ (offset, length[, prefix]) ] }.
 */
object PickleLite {
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
        fun bytes(n: Int): ByteArray { val b = ByteArray(n); bb.get(b); return b }
        @Suppress("UNCHECKED_CAST")
        fun topList(): MutableList<Any?> = st.last() as MutableList<Any?>
        @Suppress("UNCHECKED_CAST")
        fun topMap(): MutableMap<Any?, Any?> = st.last() as MutableMap<Any?, Any?>

        while (true) {
            when (val op = bb.get().toInt() and 0xff) {
                0x80 -> bb.get()                                  // PROTO
                0x95 -> bb.position(bb.position() + 8)            // FRAME
                '}'.code -> st.add(LinkedHashMap<Any?, Any?>())
                ']'.code -> st.add(ArrayList<Any?>())
                ')'.code -> st.add(emptyList<Any?>())
                '('.code -> st.add(Mark)
                'N'.code -> st.add(null)
                0x88 -> st.add(true)
                0x89 -> st.add(false)
                'K'.code -> st.add((bb.get().toInt() and 0xff).toLong())
                'M'.code -> st.add((bb.short.toInt() and 0xffff).toLong())
                'J'.code -> st.add(bb.int.toLong())
                0x8a -> {                                          // LONG1
                    val n = bb.get().toInt() and 0xff
                    st.add(readLongLE(bytes(n)))
                }
                0x8b -> { val n = bb.int; st.add(readLongLE(bytes(n))) }   // LONG4
                'G'.code -> { st.add(bb.getLong(bb.position()).let { Double.fromBits(java.lang.Long.reverseBytes(it)) }); bb.position(bb.position() + 8) }
                0x8d -> st.add(PyBytes(bytes(bb.long.toInt())))   // BINBYTES8
                'X'.code -> st.add(String(bytes(bb.int), Charsets.UTF_8))
                0x8c -> st.add(String(bytes(bb.get().toInt() and 0xff), Charsets.UTF_8))
                'U'.code -> st.add(PyBytes(bytes(bb.get().toInt() and 0xff)))
                'T'.code -> st.add(PyBytes(bytes(bb.int)))
                'C'.code -> st.add(PyBytes(bytes(bb.get().toInt() and 0xff)))
                'B'.code -> st.add(PyBytes(bytes(bb.int)))
                0x85 -> { val a = pop(); st.add(listOf(a)) }
                0x86 -> { val b = pop(); val a = pop(); st.add(listOf(a, b)) }
                0x87 -> { val c = pop(); val b = pop(); val a = pop(); st.add(listOf(a, b, c)) }
                't'.code, 'l'.code -> st.add(popMark())
                'a'.code -> { val v = pop(); topList().add(v) }
                'e'.code -> { val items = popMark(); topList().addAll(items) }
                's'.code -> { val v = pop(); val k = pop(); topMap()[k] = v }
                'u'.code -> {
                    val items = popMark()
                    val m = topMap()
                    var i = 0
                    while (i + 1 < items.size) { m[items[i]] = items[i + 1]; i += 2 }
                }
                'q'.code -> memo[bb.get().toInt() and 0xff] = st.last()
                'r'.code -> memo[bb.int] = st.last()
                0x94 -> { memo[memoIdx++] = st.last() }            // MEMOIZE
                'h'.code -> st.add(memo[bb.get().toInt() and 0xff])
                'j'.code -> st.add(memo[bb.int])
                '.'.code -> return st.last()
                else -> throw IllegalStateException("Opcode pickle tidak didukung: 0x${op.toString(16)}")
            }
        }
    }

    private fun readLongLE(b: ByteArray): Long {
        var v = 0L
        for (i in b.indices.reversed()) v = (v shl 8) or (b[i].toLong() and 0xff)
        if (b.isNotEmpty() && b.size < 8 && (b.last().toInt() and 0x80) != 0) v -= (1L shl (8 * b.size))
        return v
    }

    // ---------- writer (protocol 2) ----------

    /** Build a pickle dict { name -> [(offset, length)] } for the RPA index. */
    fun buildRpaIndex(items: List<Triple<String, Long, Long>>): ByteArray {
        val o = ByteArrayOutputStream()
        o.write(0x80); o.write(2)
        o.write('}'.code)
        o.write('('.code)
        for ((name, off, len) in items) {
            val nb = name.toByteArray(Charsets.UTF_8)
            o.write('X'.code); writeIntLE(o, nb.size); o.write(nb)
            o.write(']'.code)
            o.write('('.code)
            writeInt(o, off); writeInt(o, len)
            o.write(0x86)            // TUPLE2
            o.write('e'.code)        // APPENDS
        }
        o.write('u'.code)            // SETITEMS
        o.write('.'.code)
        return o.toByteArray()
    }

    private fun writeIntLE(o: ByteArrayOutputStream, v: Int) {
        for (i in 0 until 4) o.write((v shr (8 * i)) and 0xff)
    }

    private fun writeInt(o: ByteArrayOutputStream, v: Long) {
        if (v in Int.MIN_VALUE..Int.MAX_VALUE) {
            o.write('J'.code); writeIntLE(o, v.toInt())
        } else {
            o.write(0x8a); o.write(8)
            for (i in 0 until 8) o.write(((v shr (8 * i)) and 0xff).toInt())
        }
    }
}
