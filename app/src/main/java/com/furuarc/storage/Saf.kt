package com.furuarc.storage

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import com.furuarc.core.ConflictPolicy
import com.furuarc.core.PackInput
import com.furuarc.core.RandomSource
import java.io.FileInputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

/** Read an archive via the Storage Access Framework with random access (seek). */
class ChannelSource(private val pfd: ParcelFileDescriptor) : RandomSource {
    private val stream = FileInputStream(pfd.fileDescriptor)
    private val ch: FileChannel = stream.channel
    override val length: Long = ch.size()

    override fun readAt(pos: Long, buf: ByteArray, off: Int, len: Int): Int =
        ch.read(ByteBuffer.wrap(buf, off, len), pos)

    override fun close() {
        stream.close()
        pfd.close()
    }
}

/** Result of opening the target file: write stream + its document URI (for verification after extraction). */
class OutFile(val stream: OutputStream, val uri: Uri)

/**
 * Write files to the user-chosen folder (SAF), creating subfolders automatically.
 * Fast: the contents of each folder are queried once then cached, and it is safe for use by many threads.
 */
class TreeWriter(private val ctx: Context, private val treeUri: Uri) {
    private class Dir(val docId: String, var children: HashMap<String, String>?) {
        /** names of children that are folders (not files); filled together with [children] */
        val dirNames = HashSet<String>()
    }

    private val lock = Any()
    private val dirs = HashMap<String, Dir>()

    init {
        dirs[""] = Dir(DocumentsContract.getTreeDocumentId(treeUri), null)
    }

    private fun docUri(id: String): Uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)

    /** Folder contents (name -> document id), queried only once. Call inside the lock. */
    private fun loadChildren(d: Dir): HashMap<String, String> {
        val cached = d.children
        if (cached != null) return cached
        val map = HashMap<String, String>()
        val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, d.docId)
        val proj = arrayOf(
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE,
        )
        ctx.contentResolver.query(childUri, proj, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0) ?: continue
                val name = c.getString(1) ?: continue
                map[name] = id
                if (c.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) d.dirNames.add(name)
            }
        }
        d.children = map
        return map
    }

    /** Find/create the folder chain. Call inside the lock. */
    private fun dirFor(parts: List<String>): Dir {
        var key = ""
        var cur = dirs[""] ?: throw IOException("Root folder tidak ada")
        for (p in parts) {
            key += "/$p"
            val known = dirs[key]
            if (known != null) {
                cur = known
                continue
            }
            val kids = loadChildren(cur)
            val existingId = kids[p]
            val next = if (existingId != null && p in cur.dirNames) {
                Dir(existingId, null)
            } else {
                // Does not exist yet, or the name is used by a FILE (e.g. output folder same as the archive folder:
                // archive "a.pfs" and result folder "a.pfs"). In the second case a folder with another name is created.
                val dirName = if (existingId == null) p else uniqueName(kids, p, "_ekstrak")
                val u = DocumentsContract.createDocument(
                    ctx.contentResolver, docUri(cur.docId),
                    DocumentsContract.Document.MIME_TYPE_DIR, dirName,
                ) ?: throw IOException("Gagal membuat folder $dirName")
                val newId = DocumentsContract.getDocumentId(u)
                kids[dirName] = newId
                cur.dirNames.add(dirName)
                Dir(newId, HashMap())
            }
            dirs[key] = next
            cur = next
        }
        return cur
    }

    private class Prep(val dir: Dir, val name: String, val existingId: String?)

    private fun uniqueName(kids: Map<String, String>, name: String, suffix: String = ""): String {
        if (suffix.isNotEmpty()) {
            var cand = name + suffix
            var n = 2
            while (kids.containsKey(cand)) cand = "$name$suffix$n".also { n++ }
            return cand
        }
        val dot = name.lastIndexOf('.')
        val base = if (dot > 0) name.substring(0, dot) else name
        val ext = if (dot > 0) name.substring(dot) else ""
        var i = 1
        while (true) {
            val cand = "$base ($i)$ext"
            if (!kids.containsKey(cand)) return cand
            i++
        }
    }

    /**
     * Open the target file for writing. If it already exists: OVERWRITE overwrites, RENAME creates "name (1).ext",
     * SKIP returns null (the file is not written).
     */
    fun open(path: String, policy: ConflictPolicy = ConflictPolicy.OVERWRITE): OutFile? {
        // drop "..", "." and empty segments so it cannot escape the target folder
        val parts = path.replace('\\', '/').split('/').filter { it.isNotEmpty() && it != "." && it != ".." }
        if (parts.isEmpty()) throw IOException("Nama file kosong")
        val name = parts.last()

        // Decide the final name inside the lock and "reserve" it so parallel workers do not collide.
        val prep: Prep = synchronized(lock) {
            val d = dirFor(parts.dropLast(1))
            val kids = loadChildren(d)
            val ex = kids[name]
            if (ex == null) {
                kids[name] = ""
                Prep(d, name, null)
            } else if (ex.isEmpty()) {
                null // the same name is being created by another worker
            } else if (name in d.dirNames) {
                // a FOLDER with the same name already exists: do not overwrite it, use another name
                val n = uniqueName(kids, name)
                kids[n] = ""
                Prep(d, n, null)
            } else when (policy) {
                ConflictPolicy.SKIP -> null
                ConflictPolicy.OVERWRITE -> Prep(d, name, ex)
                ConflictPolicy.RENAME -> {
                    val n = uniqueName(kids, name)
                    kids[n] = ""
                    Prep(d, n, null)
                }
            }
        } ?: return null

        val existingId = prep.existingId
        val uri: Uri = if (existingId != null) {
            docUri(existingId)
        } else {
            val created = try {
                DocumentsContract.createDocument(
                    ctx.contentResolver, docUri(prep.dir.docId), "application/octet-stream", prep.name,
                ) ?: throw IOException("Gagal membuat file ${prep.name}")
            } catch (e: Exception) {
                synchronized(lock) { loadChildren(prep.dir).remove(prep.name) }
                throw e
            }
            synchronized(lock) { loadChildren(prep.dir)[prep.name] = DocumentsContract.getDocumentId(created) }
            created
        }
        // "wt" = write + truncate, so the old file does not need to be deleted first
        val os = ctx.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Gagal membuka ${prep.name}")
        return OutFile(os, uri)
    }

    /** Contents of the existing file [path] in the target folder (null if it does not exist). Used to make a backup before overwriting. */
    fun readExisting(path: String): ByteArray? {
        val parts = path.replace('\\', '/').split('/').filter { it.isNotEmpty() && it != "." && it != ".." }
        if (parts.isEmpty()) return null
        val id = synchronized(lock) {
            val d = dirFor(parts.dropLast(1))
            loadChildren(d)[parts.last()]
        }
        if (id.isNullOrEmpty()) return null
        return try {
            ctx.contentResolver.openInputStream(docUri(id))?.use { it.readBytes() }
        } catch (e: Exception) {
            null
        }
    }

    /** Document URI of the folder [parts] under the target folder (if already created), for the Open folder button. */
    fun dirUri(parts: List<String>): Uri? = synchronized(lock) {
        var key = ""
        for (p in parts) key += "/$p"
        dirs[key]?.let { docUri(it.docId) }
    }
}

/** List all files in a folder (recursive) as material for pack, one query per folder. */
/** [recursive] = false takes only the top-level files of the folder (no subfolders). */
fun listTree(ctx: Context, treeUri: Uri, recursive: Boolean = true): List<PackInput> {
    val resolver = ctx.contentResolver
    val out = ArrayList<PackInput>()
    val proj = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_SIZE,
    )

    fun walk(docId: String, prefix: String) {
        val childUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, docId)
        val subdirs = ArrayList<Pair<String, String>>()
        resolver.query(childUri, proj, null, null, null)?.use { c ->
            while (c.moveToNext()) {
                val id = c.getString(0) ?: continue
                val name = c.getString(1) ?: continue
                val mime = c.getString(2)
                if (mime == DocumentsContract.Document.MIME_TYPE_DIR) {
                    subdirs.add(Pair(id, name))
                } else {
                    val size = if (c.isNull(3)) 0L else c.getLong(3)
                    val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, id)
                    out.add(PackInput(prefix + name, size) {
                        resolver.openInputStream(uri) ?: throw IOException("Gagal membuka $name")
                    })
                }
            }
        }
        if (recursive) for (sub in subdirs) walk(sub.first, prefix + sub.second + "/")
    }

    walk(DocumentsContract.getTreeDocumentId(treeUri), "")
    return out.sortedBy { it.path }
}
