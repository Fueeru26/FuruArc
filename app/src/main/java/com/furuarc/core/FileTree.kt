package com.furuarc.core

import java.util.BitSet

/**
 * Folder/file tree built from a name list that is ALREADY sorted (String.compareTo).
 * Because it is sorted, all files in one folder lie in a contiguous index range [from, to),
 * so a folder's checked state can be computed from the selection BitSet without walking its children.
 */
class FileTree(names: List<String>) {
    class Node(val path: String, val name: String, val depth: Int, val isDir: Boolean, val from: Int) {
        var to: Int = from + 1
        val children = ArrayList<Node>()
        val count: Int get() = to - from
    }

    val root = Node("", "", -1, true, 0)
    val size = names.size

    /** All folders (ordered by path), for folder lookup. */
    val dirNodes: List<Node>

    /** File node for each index (null if the name is empty). Used by search results. */
    val fileNodes: Array<Node?> = arrayOfNulls(names.size)

    init {
        val dirs = HashMap<String, Node>()
        dirs[""] = root
        for (i in names.indices) {
            val parts = names[i].replace('\\', '/').split('/').filter { it.isNotEmpty() }
            if (parts.isEmpty()) continue
            var parent = root
            var key = ""
            for (k in 0 until parts.size - 1) {
                key = if (key.isEmpty()) parts[k] else key + "/" + parts[k]
                var d = dirs[key]
                if (d == null) {
                    d = Node(key, parts[k], k, true, i)
                    dirs[key] = d
                    parent.children.add(d)
                }
                d.to = i + 1
                parent = d
            }
            val fileNode = Node(parts.joinToString("/"), parts.last(), parts.size - 1, false, i)
            fileNodes[i] = fileNode
            parent.children.add(fileNode)
        }
        root.to = names.size
        sortChildren(root)
        dirNodes = dirs.values.filter { it !== root }.sortedBy { it.path.lowercase() }
    }

    private fun sortChildren(n: Node) {
        n.children.sortWith(compareBy<Node>({ !it.isDir }, { it.name.lowercase() }))
        for (c in n.children) if (c.isDir) sortChildren(c)
    }

    /** Visible rows: an opened folder ([expanded] holds its path) also shows its children. */
    fun visible(expanded: Set<String>): List<Node> {
        val out = ArrayList<Node>()
        fun walk(n: Node) {
            for (c in n.children) {
                out.add(c)
                if (c.isDir && expanded.contains(c.path)) walk(c)
            }
        }
        walk(root)
        return out
    }

    companion object {
        const val NONE = 0
        const val PARTIAL = 1
        const val ALL = 2

        fun countIn(sel: BitSet, from: Int, to: Int): Int {
            var c = 0
            var i = sel.nextSetBit(from)
            while (i in 0 until to) {
                c++
                i = sel.nextSetBit(i + 1)
            }
            return c
        }
    }

    fun stateOf(sel: BitSet, n: Node): Int {
        val c = countIn(sel, n.from, n.to)
        return if (c == 0) NONE else if (c >= n.count) ALL else PARTIAL
    }

    /** New copy with the node toggled: all selected -> clear, otherwise -> select all. */
    fun toggle(sel: BitSet, n: Node): BitSet {
        val r = sel.clone() as BitSet
        r.set(n.from, n.to, stateOf(sel, n) != ALL)
        return r
    }

    /** (number of selected files, number of topmost fully-selected folders). */
    fun counts(sel: BitSet): Pair<Int, Int> {
        var folders = 0
        fun walk(n: Node) {
            for (c in n.children) {
                if (!c.isDir) continue
                if (stateOf(sel, c) == ALL) folders++ else walk(c)
            }
        }
        walk(root)
        return Pair(sel.cardinality(), folders)
    }
}
