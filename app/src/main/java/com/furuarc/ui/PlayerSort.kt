package com.furuarc.ui

import android.content.Context
import com.furuarc.core.MItem
import com.furuarc.core.MediaMeta
import com.furuarc.i18n.PlayerStr

/** Sort orders of the Media Player lists (gallery, audio queue, video queue). The sort reorders the list itself, so the next / previous order follows it. */
object PlayerSort {
    const val NAME_AZ = 0
    const val NAME_ZA = 1
    const val TITLE_AZ = 2
    const val TITLE_ZA = 3
    const val ARTIST_AZ = 4
    const val ARTIST_ZA = 5
    const val DUR_LONG = 6
    const val DUR_SHORT = 7
    const val NEWEST = 8
    const val OLDEST = 9
    const val SIZE_BIG = 10
    const val SIZE_SMALL = 11
    const val RANDOM = 12

    /** Orders offered for a [kind] (1 = gallery, 2 = audio, 3 = video), in display order. */
    fun codes(kind: Int): List<Int> = when (kind) {
        1 -> listOf(NAME_AZ, NAME_ZA, NEWEST, OLDEST, SIZE_BIG, SIZE_SMALL, RANDOM)
        2 -> listOf(TITLE_AZ, TITLE_ZA, ARTIST_AZ, ARTIST_ZA, NAME_AZ, NAME_ZA, DUR_LONG, DUR_SHORT, NEWEST, OLDEST, SIZE_BIG, SIZE_SMALL, RANDOM)
        else -> listOf(NAME_AZ, NAME_ZA, DUR_LONG, DUR_SHORT, NEWEST, OLDEST, SIZE_BIG, SIZE_SMALL, RANDOM)
    }

    fun labels(pl: PlayerStr, kind: Int): List<String> = codes(kind).map { pl.sortLabel(it) }

    /** Blocking (reads tags for title / artist / duration); call on an IO dispatcher. */
    fun sort(ctx: Context, items: List<MItem>, code: Int): List<MItem> {
        val byName = Comparator<MItem> { a, b -> a.name.lowercase().compareTo(b.name.lowercase()) }
        // items without a value (no tag) always go last
        fun text(sel: (com.furuarc.core.MMeta) -> String?, desc: Boolean): List<MItem> {
            val keyed = items.map { it to sel(MediaMeta.info(ctx, it))?.lowercase() }
            val (has, none) = keyed.partition { it.second != null }
            val sorted = if (desc) has.sortedByDescending { it.second } else has.sortedBy { it.second }
            return sorted.map { it.first } + none.map { it.first }
        }
        return when (code) {
            NAME_AZ -> items.sortedWith(byName)
            NAME_ZA -> items.sortedWith(byName.reversed())
            TITLE_AZ -> text({ it.title }, false)
            TITLE_ZA -> text({ it.title }, true)
            ARTIST_AZ -> text({ it.artist }, false)
            ARTIST_ZA -> text({ it.artist }, true)
            DUR_LONG -> items.sortedByDescending { MediaMeta.info(ctx, it).durationMs }
            DUR_SHORT -> items.sortedBy { MediaMeta.info(ctx, it).durationMs }
            NEWEST -> items.sortedByDescending { it.modified }
            OLDEST -> items.sortedBy { it.modified }
            SIZE_BIG -> items.sortedByDescending { it.size }
            SIZE_SMALL -> items.sortedBy { it.size }
            else -> items.shuffled()
        }
    }
}
