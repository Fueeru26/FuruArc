package com.furuarc.core

import android.net.Uri
import android.provider.DocumentsContract
import com.furuarc.Engine
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/** Game index cache on disk (filesDir/gameidx/<id>.bin) so a game does not need to be rescanned each time it is opened. */
object GameIdx {
    private const val MAGIC = 0x47494433
    private fun dir() = File(Engine.app.filesDir, "gameidx").also { it.mkdirs() }
    private fun file(id: String) = File(dir(), "$id.bin")

    fun delete(id: String) {
        file(id).delete()
    }

    private fun hex(b: ByteArray?) = b?.joinToString("") { String.format("%02x", it) } ?: ""
    private fun unhex(s: String): ByteArray? = if (s.isEmpty()) null else ByteArray(s.length / 2) { s.substring(it * 2, it * 2 + 2).toInt(16).toByte() }

    fun save(id: String, idx: GameIndex, texts: List<TextItem>) {
        try {
            val arcIdx = HashMap<String, Int>()
            idx.arcs.forEachIndexed { i, a -> arcIdx[a.path] = i }
            val tmp = File(file(id).path + ".tmp")
            DataOutputStream(GZIPOutputStream(tmp.outputStream().buffered())).use { o ->
                o.writeInt(MAGIC)
                o.writeLong(idx.sig)
                o.writeUTF(idx.engine)
                o.writeUTF(idx.title ?: "")
                o.writeUTF(hex(idx.key))
                o.writeInt(idx.files)
                o.writeInt(idx.arcs.size)
                for (a in idx.arcs) {
                    o.writeUTF(a.path); o.writeUTF(a.fmt); o.writeInt(a.count); o.writeUTF(a.error ?: "")
                    val r = a.ref
                    o.writeBoolean(r != null)
                    if (r != null) { o.writeUTF(DocumentsContract.getDocumentId(r.uri)); o.writeUTF(r.fmtId); o.writeInt(r.key ?: -1) }
                }
                fun wa(l: List<GAsset>) {
                    o.writeInt(l.size)
                    for (a in l) {
                        o.writeUTF(a.path); o.writeLong(a.size); o.writeInt(a.kind); o.writeBoolean(a.enc)
                        if (a.arc == null) { o.writeInt(-1); o.writeUTF(DocumentsContract.getDocumentId(a.uri)) }
                        else { o.writeInt(arcIdx[a.arc.path] ?: -1); o.writeUTF(a.entry) }
                    }
                }
                wa(idx.audio); wa(idx.images); wa(idx.videos)
                o.writeInt(texts.size)
                for (t in texts) {
                    o.writeUTF(t.path); o.writeLong(t.size); o.writeInt(t.type); o.writeUTF(t.kind?.id ?: "")
                    if (t.arc == null) { o.writeInt(-1); o.writeUTF(DocumentsContract.getDocumentId(t.uri)) }
                    else { o.writeInt(arcIdx[t.arc.path] ?: -1); o.writeUTF(t.entry) }
                }
            }
            tmp.renameTo(file(id))
        } catch (e: Exception) {
            // the cache only speeds things up; failing to save does not matter
        }
    }

    /** Read the cache; null if missing / corrupt / old version. */
    fun load(id: String, tree: Uri): Pair<GameIndex, List<TextItem>>? {
        val f = file(id)
        if (!f.exists()) return null
        return try {
            DataInputStream(GZIPInputStream(f.inputStream().buffered())).use { i ->
                if (i.readInt() != MAGIC) return null
                val sig = i.readLong()
                val engine = i.readUTF()
                val title = i.readUTF().ifEmpty { null }
                val key = unhex(i.readUTF())
                val files = i.readInt()
                fun docUri(d: String) = DocumentsContract.buildDocumentUriUsingTree(tree, d)
                val arcs = ArrayList<ArcStatus>()
                repeat(i.readInt()) {
                    val path = i.readUTF(); val fmt = i.readUTF(); val count = i.readInt(); val err = i.readUTF().ifEmpty { null }
                    var ref: ArcRef? = null
                    if (i.readBoolean()) {
                        val d = i.readUTF(); val fid = i.readUTF(); val k = i.readInt()
                        ref = ArcRef(docUri(d), fid, if (k < 0) null else k, path)
                    }
                    arcs.add(ArcStatus(ref, path, fmt, count, err))
                }
                fun ra(): List<GAsset> {
                    val n = i.readInt()
                    val out = ArrayList<GAsset>(n)
                    repeat(n) {
                        val path = i.readUTF(); val size = i.readLong(); val kind = i.readInt(); val enc = i.readBoolean()
                        val ai = i.readInt(); val s = i.readUTF()
                        if (ai < 0) out.add(GAsset(path, size, docUri(s), kind, enc, null, "", GameScan.isSpecial(path)))
                        else {
                            val r = arcs.getOrNull(ai)?.ref ?: return@repeat
                            out.add(GAsset(path, size, r.uri, kind, enc, r, s, GameScan.isSpecial(path)))
                        }
                    }
                    return out
                }
                val au = ra(); val im = ra(); val vi = ra()
                val tn = i.readInt()
                val texts = ArrayList<TextItem>(tn)
                repeat(tn) {
                    val path = i.readUTF(); val size = i.readLong(); val type = i.readInt()
                    val kind = i.readUTF().let { k -> ScriptKind.values().firstOrNull { it.id == k } }
                    val ai = i.readInt(); val s = i.readUTF()
                    if (ai < 0) texts.add(TextItem(path, size, docUri(s), type, kind))
                    else {
                        val r = arcs.getOrNull(ai)?.ref ?: return@repeat
                        texts.add(TextItem(path, size, r.uri, type, kind, r, s))
                    }
                }
                Pair(GameIndex(engine, title, key, files, au, im, vi, emptyList(), arcs, sig), texts)
            }
        } catch (e: Exception) {
            null
        }
    }
}
