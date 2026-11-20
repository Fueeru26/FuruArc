package com.furuarc.core

import android.graphics.BitmapFactory
import com.furuarc.Engine
import kotlinx.coroutines.CancellationException
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/** One cover search result. [thumb] for the grid, [full] to download, [alt] fallback if [full]/[thumb] fails. [w]/[h] 0 = not yet known. */
class CoverHit(val source: String, val title: String, val thumb: String, val full: String, val w: Int, val h: Int, val score: Double, val alt: String = "")

/**
 * Cover (portrait) search on the internet. Sources (following Tyranor Next): VNDB (Kana API), NextMoe
 * (api.nextmoe.dev), Steam (storesearch + library_600x900), and Bing (image search page).
 */
object CoverSearch {
    const val BING = "bing"
    const val STEAM = "steam"
    const val VNDB = "vndb"
    const val NEXTMOE = "nextmoe"
    val SOURCES = listOf(VNDB, NEXTMOE, STEAM, BING)
    fun label(s: String) = when (s) { BING -> "Bing"; STEAM -> "Steam"; VNDB -> "VNDB"; else -> "NextMoe" }

    private const val UA = "FuruArc/0.92 (Android; pencarian cover)"
    private const val UA_IMG = "Mozilla/5.0"
    private const val UA_DESKTOP = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    private const val MAX_BYTES = 15 * 1024 * 1024

    private fun refererFor(url: String): String? = when {
        url.contains("vndb.org") -> "https://vndb.org/"
        url.contains("steamstatic.com") -> "https://store.steampowered.com/"
        url.contains("bgm.tv") || url.contains("bangumi") -> "https://bgm.tv/"
        else -> null
    }

    private fun cookieFor(url: String): String? = if (url.contains("vndb.org")) "vndb_img=1; vndb_samesite=1" else null

    private fun http(url: String, post: String? = null, ua: String = UA, json: Boolean = false, cookie: String? = null, referer: String? = null, accept: String? = null): ByteArray {
        val c = URL(url).openConnection() as HttpURLConnection
        try {
            c.connectTimeout = 12000
            c.readTimeout = 15000
            c.instanceFollowRedirects = true
            c.setRequestProperty("User-Agent", ua)
            c.setRequestProperty("Accept-Language", "en-US,en;q=0.9")
            if (accept != null) c.setRequestProperty("Accept", accept)
            val ck = cookie ?: cookieFor(url)
            if (ck != null) c.setRequestProperty("Cookie", ck)
            val rf = referer ?: refererFor(url)
            if (rf != null) c.setRequestProperty("Referer", rf)
            if (post != null) {
                c.requestMethod = "POST"
                c.doOutput = true
                if (json) c.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                c.outputStream.use { it.write(post.toByteArray(Charsets.UTF_8)) }
            }
            if (c.responseCode !in 200..299) throw java.io.IOException("HTTP " + c.responseCode)
            val bo = ByteArrayOutputStream()
            c.inputStream.use { ins ->
                val buf = ByteArray(16384)
                while (true) {
                    val n = ins.read(buf)
                    if (n < 0) break
                    bo.write(buf, 0, n)
                    if (bo.size() > MAX_BYTES) throw java.io.IOException("terlalu besar")
                }
            }
            return bo.toByteArray()
        } finally {
            c.disconnect()
        }
    }

    private fun imgBytes(url: String): ByteArray = http(url, ua = UA_IMG, accept = "image/avif,image/webp,image/apng,image/*,*/*;q=0.8")

    private fun enc(s: String) = URLEncoder.encode(s, "UTF-8")

    /** Clean up a folder name: remove [..], 【..】, the part after "(", underscores. */
    fun cleanTitle(s: String): String {
        val c = s.replace(Regex("\\[[^\\]]*\\]|【[^】]*】"), " ")
            .replace(Regex("[（(].*"), " ")
            .replace('_', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
        return c.ifEmpty { s.trim() }
    }

    // ---- title similarity (0..1) ----
    private fun tokens(s: String): List<String> =
        s.lowercase().replace(Regex("[^\\p{L}\\p{N}]+"), " ").trim().split(' ').filter { it.isNotEmpty() }

    private fun lev1(a: String, b: String): Boolean {
        if (a == b) return true
        if (a.length < 5 || b.length < 5 || Math.abs(a.length - b.length) > 1) return false
        var i = 0
        while (i < a.length && i < b.length && a[i] == b[i]) i++
        if (a.length == b.length) return a.substring(i + 1) == b.substring(i + 1)
        return if (a.length > b.length) a.substring(i + 1) == b.substring(i) else b.substring(i + 1) == a.substring(i)
    }

    /** Title similarity 0..1: tolerates a 1-letter typo and result titles that have a long subtitle. */
    fun similarity(a: String, b: String): Double {
        val x = tokens(a)
        val y = tokens(b)
        if (x.isEmpty() || y.isEmpty()) return 0.0
        val na = x.joinToString("")
        val nb = y.joinToString("")
        if (na == nb) return 1.0
        val common = x.count { t -> y.any { lev1(t, it) } }.toDouble()
        var s = maxOf(common / maxOf(x.size, y.size), 0.9 * common / x.size)
        if (na.contains(nb) || nb.contains(na)) s = maxOf(s, 0.8 * minOf(na.length, nb.length) / maxOf(na.length, nb.length) + 0.2)
        return s
    }

    private fun str(m: Map<*, *>, k: String): String = (m[k] as? String) ?: ""
    private fun numOf(x: Any?): Int = when (x) {
        is JNum -> x.raw.toDoubleOrNull()?.toInt() ?: 0
        is Number -> x.toInt()
        else -> 0
    }
    private fun num(m: Map<*, *>?, k: String): Int = numOf(m?.get(k))

    // ---- sources ----
    private fun vndb(q: String, max: Int): List<CoverHit> {
        val body = "{\"filters\":[\"search\",\"=\"," + Json.write(q, false) + "],\"fields\":\"title,alttitle,titles.title,titles.latin,image.url,image.thumbnail,image.dims\",\"results\":" + (max + 4) + ",\"sort\":\"searchrank\"}"
        val root = Json.parse(String(http("https://api.vndb.org/kana/vn", body, json = true, accept = "application/json"), Charsets.UTF_8)) as? Map<*, *> ?: return emptyList()
        val out = ArrayList<CoverHit>()
        for (r in (root["results"] as? List<*>) ?: emptyList<Any>()) {
            val m = r as? Map<*, *> ?: continue
            val img = m["image"] as? Map<*, *> ?: continue
            val url = img["url"] as? String ?: continue
            val th = (img["thumbnail"] as? String)?.ifEmpty { null } ?: url
            val dims = img["dims"] as? List<*>
            val w = numOf(dims?.getOrNull(0))
            val h = numOf(dims?.getOrNull(1))
            val t = str(m, "title")
            val alt = str(m, "alttitle")
            var sc = maxOf(similarity(q, t), if (alt.isEmpty()) 0.0 else similarity(q, alt))
            for (x in (m["titles"] as? List<*>) ?: emptyList<Any>()) {
                val xm = x as? Map<*, *> ?: continue
                sc = maxOf(sc, similarity(q, str(xm, "title")), similarity(q, str(xm, "latin")))
            }
            out.add(CoverHit(VNDB, t.ifEmpty { alt }, th, url, w, h, sc, alt = url))
        }
        return out
    }

    private fun nextmoe(q: String, max: Int): List<CoverHit> {
        val url = "https://api.nextmoe.dev/v2/catalog/works?q=" + enc(q) + "&nsfw=true&limit=" + (max + 4) + "&include=refs"
        val root = Json.parse(String(http(url, accept = "application/json"), Charsets.UTF_8)) as? Map<*, *> ?: return emptyList()
        val out = ArrayList<CoverHit>()
        for (r in (root["items"] as? List<*>) ?: emptyList<Any>()) {
            val m = r as? Map<*, *> ?: continue
            val cv = m["cover"] as? Map<*, *> ?: continue
            val u = cv["url"] as? String ?: continue
            if (u.isEmpty()) continue
            val name = str(m, "display_name")
            val latin = str(m, "latin")
            val sc = maxOf(similarity(q, name), if (latin.isEmpty()) 0.0 else similarity(q, latin))
            out.add(CoverHit(NEXTMOE, name.ifEmpty { latin }, u, u, num(cv, "width"), num(cv, "height"), sc))
        }
        return out
    }

    private fun steam(q: String, max: Int): List<CoverHit> {
        val root = Json.parse(String(http("https://store.steampowered.com/api/storesearch/?term=" + enc(q) + "&l=schinese&cc=CN", ua = UA_IMG, referer = "https://store.steampowered.com/", accept = "application/json"), Charsets.UTF_8)) as? Map<*, *> ?: return emptyList()
        val out = ArrayList<CoverHit>()
        for (r in (root["items"] as? List<*>) ?: emptyList<Any>()) {
            val m = r as? Map<*, *> ?: continue
            val id = (m["id"] as? JNum)?.raw?.toLongOrNull() ?: continue
            val name = m["name"] as? String ?: continue
            val a = "https://cdn.akamai.steamstatic.com/steam/apps/$id/library_600x900.jpg"
            val b = "https://cdn.cloudflare.steamstatic.com/steam/apps/$id/library_600x900.jpg"
            out.add(CoverHit(STEAM, name, a, a, 600, 900, similarity(q, name), alt = b))
            if (out.size >= max + 2) break
        }
        return out
    }

    /** Bing image search (murl = original image, turl = thumbnail). */
    private fun bingImages(q: String): List<CoverHit> {
        val html = String(http("https://www.bing.com/images/search?q=" + enc("$q cover") + "&form=HDRSC2&first=1", ua = UA_DESKTOP, referer = "https://www.bing.com/"), Charsets.UTF_8)
        val out = ArrayList<CoverHit>()
        val seen = HashSet<String>()
        for (m in Regex("murl&quot;:&quot;(.*?)&quot;.*?turl&quot;:&quot;(.*?)&quot;").findAll(html)) {
            val full = m.groupValues[1].replace("&amp;", "&")
            val th = m.groupValues[2].replace("&amp;", "&")
            if (!full.startsWith("http") || !seen.add(full)) continue
            out.add(CoverHit(BING, "", th.ifEmpty { full }, full, 0, 0, 0.0, alt = th))
        }
        return out
    }

    /** Search one source (max [max] results, best match first). */
    fun search(source: String, q: String, max: Int = 6): List<CoverHit> {
        val query = cleanTitle(q)
        if (query.isEmpty()) return emptyList()
        val all = when (source) {
            VNDB -> vndb(query, max)
            NEXTMOE -> nextmoe(query, max)
            STEAM -> steam(query, max)
            else -> bingImages(query)
        }
        return all.take(max)
    }

    private fun decodeSmall(b: ByteArray, max: Int): android.graphics.Bitmap? {
        val o = BitmapFactory.Options()
        o.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(b, 0, b.size, o)
        if (o.outWidth <= 0) return null
        var sm = 1
        while (o.outWidth / (sm * 2) >= max && o.outHeight / (sm * 2) >= max) sm *= 2
        val o2 = BitmapFactory.Options()
        o2.inSampleSize = sm
        return BitmapFactory.decodeByteArray(b, 0, b.size, o2)
    }

    /** Download the result thumbnail and decode it small (for the grid); tries [CoverHit.alt] if it fails. */
    fun thumbBitmap(h: CoverHit, max: Int): android.graphics.Bitmap? {
        for (u in listOf(h.thumb, h.alt, h.full).filter { it.isNotEmpty() }.distinct()) {
            try {
                decodeSmall(imgBytes(u), max)?.let { return it }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
            }
        }
        return null
    }

    /** Download the result image to [dest]; false if it is not a decodable image. */
    fun download(hit: CoverHit, dest: File): Boolean {
        for (u in listOf(hit.full, hit.alt).filter { it.isNotEmpty() }.distinct()) {
            try {
                val b = imgBytes(u)
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(b, 0, b.size, o)
                if (o.outWidth <= 0) continue
                dest.parentFile?.mkdirs()
                dest.writeBytes(b)
                return true
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
            }
        }
        return false
    }

    fun tmpFile(name: String): File = File(File(Engine.app.cacheDir, "cover_tmp").also { it.mkdirs() }, name)

    /**
     * Automatic search: take the FIRST result (the source's own relevance order) in order VNDB, NextMoe, Steam, then Bing.
     * The first result is skipped only if its title is not similar at all or its image failed to download.
     * Returns a temporary image file + source name, or null.
     */
    fun auto(q: String, tag: String): Pair<File, String>? {
        for (src in listOf(VNDB, NEXTMOE, STEAM, BING)) {
            try {
                val hits = search(src, q, 3).let { l -> if (src == BING) l else l.filter { it.score >= 0.2 } }
                for (h in hits) {
                    val f = tmpFile("$tag.img")
                    if (download(h, f)) return Pair(f, label(src))
                }
            } catch (ce: CancellationException) {
                throw ce
            } catch (e: Exception) {
                // this source failed, continue to the next
            }
        }
        return null
    }
}
