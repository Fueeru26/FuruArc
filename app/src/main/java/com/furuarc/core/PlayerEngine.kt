package com.furuarc.core

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.furuarc.Engine
import com.furuarc.PlayerService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Audio playback of the Media Player: queue, shuffle / repeat, speed, A-B loop and sleep timer.
 * It lives outside the screens so music keeps playing (with a notification) when the app is in the background.
 */
object AudioEngine {
    var queue by mutableStateOf<List<PItem>>(emptyList())
        private set
    var index by mutableStateOf(-1)
        private set
    var playing by mutableStateOf(false)
        private set
    var preparing by mutableStateOf(false)
        private set
    var failed by mutableStateOf(false)
        private set
    var duration by mutableStateOf(0)
        private set
    var position by mutableStateOf(0)
        private set
    /** A-B loop points in ms (null = not set). */
    var loopA by mutableStateOf<Int?>(null)
        private set
    var loopB by mutableStateOf<Int?>(null)
        private set
    /** Uptime (ms) when the sleep timer pauses playback; 0 = no timer. */
    var sleepAt by mutableStateOf(0L)
        private set

    private var mp: MediaPlayer? = null
    private var token = 0
    private var ticker: Job? = null
    private var shuffleOrder: List<Int> = emptyList()

    val current: PItem? get() = queue.getOrNull(index)

    fun setQueue(items: List<PItem>, start: Int) {
        queue = items
        shuffleOrder = emptyList()
        playIndex(start)
    }

    fun playIndex(i: Int) {
        if (i !in queue.indices) return
        val old = mp
        if (old != null) rememberPosition()
        releasePlayer()
        val my = ++token
        index = i
        preparing = true
        failed = false
        playing = false
        position = 0
        duration = 0
        loopA = null
        loopB = null
        val item = queue[i]
        val ctx = Engine.app
        Engine.scope.launch {
            val p = MediaPlayer()
            try {
                if (item.uri != null) p.setDataSource(ctx, item.uri) else p.setDataSource(item.path)
                mp = p
                p.setOnPreparedListener {
                    if (token != my) return@setOnPreparedListener
                    duration = it.duration
                    val saved = MediaPrefs.savedPos(item.key)
                    if (saved > 0 && saved < duration - 3000) { it.seekExact(saved); position = saved }
                    it.start()
                    applySpeed()
                    preparing = false
                    playing = true
                    startTicker()
                    PlayerService.sync(ctx)
                }
                p.setOnCompletionListener { if (token == my) onCompleted() }
                p.setOnErrorListener { _, _, _ ->
                    if (token == my) { failed = true; playing = false; preparing = false; PlayerService.sync(ctx) }
                    true
                }
                p.prepareAsync()
            } catch (e: Exception) {
                try { p.release() } catch (x: Exception) { }
                if (token == my) { failed = true; preparing = false }
            }
            PlayerService.sync(ctx)
        }
    }

    private fun onCompleted() {
        position = 0
        if (MediaPrefs.repeat == 2) { playIndex(index); return }
        if (!MediaPrefs.autoNext) { playing = false; try { mp?.seekExact(0) } catch (e: Throwable) { }; PlayerService.sync(Engine.app); return }
        val n = nextIndex(true)
        if (n >= 0) playIndex(n) else { playing = false; PlayerService.sync(Engine.app) }
    }

    /** Index of the next track; -1 when the queue ends (repeat off). [auto] = true when a track ended by itself. */
    private fun nextIndex(auto: Boolean): Int {
        val n = queue.size
        if (n == 0) return -1
        if (MediaPrefs.shuffle) {
            if (n == 1) return if (MediaPrefs.repeat == 1 || !auto) 0 else -1
            if (shuffleOrder.size != n) shuffleOrder = (0 until n).shuffled(Random(System.nanoTime()))
            val pos = shuffleOrder.indexOf(index)
            val next = pos + 1
            return if (next < n) shuffleOrder[next] else if (MediaPrefs.repeat == 1 || !auto) { shuffleOrder = (0 until n).shuffled(Random(System.nanoTime())); shuffleOrder[0] } else -1
        }
        val next = index + 1
        return if (next < n) next else if (MediaPrefs.repeat == 1 || !auto) 0 else -1
    }

    fun next() { val n = nextIndex(false); if (n >= 0) playIndex(n) }

    fun previous() {
        // after a few seconds "previous" restarts the track, like most players
        if (position > 3000) { seek(0); return }
        val n = queue.size
        if (n == 0) return
        if (MediaPrefs.shuffle && shuffleOrder.size == n) {
            val pos = shuffleOrder.indexOf(index)
            playIndex(shuffleOrder[if (pos > 0) pos - 1 else n - 1])
        } else {
            playIndex(if (index > 0) index - 1 else n - 1)
        }
    }

    fun toggle() {
        val p = mp ?: return
        try {
            if (p.isPlaying) { p.pause(); playing = false; rememberPosition() }
            else { p.start(); applySpeed(); playing = true; startTicker() }
        } catch (e: Exception) { failed = true }
        PlayerService.sync(Engine.app)
    }

    fun pause() {
        try { mp?.let { if (it.isPlaying) it.pause() } } catch (e: Exception) { }
        playing = false
        rememberPosition()
        PlayerService.sync(Engine.app)
    }

    fun seek(ms: Int) {
        val p = mp ?: return
        p.seekExact(ms.coerceIn(0, duration.coerceAtLeast(0)))
        position = ms
    }

    fun setSpeed(v: Float) {
        MediaPrefs.speed = v
        MediaPrefs.save()
        applySpeed()
    }

    private fun applySpeed() {
        val p = mp ?: return
        try { if (p.isPlaying) p.playbackParams = PlaybackParams().setSpeed(MediaPrefs.speed) } catch (e: Throwable) { }
    }

    /** A-B loop: first tap sets A, second sets B, third clears. */
    fun cycleLoop() {
        when {
            loopA == null -> loopA = position
            loopB == null -> { if (position > (loopA ?: 0) + 500) loopB = position else loopA = null }
            else -> { loopA = null; loopB = null }
        }
    }

    fun setSleep(minutes: Int) {
        sleepAt = if (minutes <= 0) 0L else SystemClock.elapsedRealtime() + minutes * 60_000L
        if (sleepAt != 0L) startTicker()
    }

    private fun rememberPosition() {
        val c = current ?: return
        try { MediaPrefs.savePos(c.key, mp?.currentPosition ?: position, duration) } catch (e: Throwable) { }
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = Engine.scope.launch {
            var n = 0
            while (isActive) {
                delay(250)
                val p = mp
                if (p != null && playing) {
                    try {
                        val pos = p.currentPosition
                        position = pos
                        val a = loopA; val b = loopB
                        if (a != null && b != null && pos >= b) p.seekExact(a)
                        if (++n % 40 == 0) rememberPosition()
                    } catch (e: Throwable) { }
                }
                if (sleepAt != 0L && SystemClock.elapsedRealtime() >= sleepAt) { sleepAt = 0L; pause() }
                if (!playing && sleepAt == 0L) break
            }
        }
    }

    private fun releasePlayer() {
        try { mp?.release() } catch (e: Exception) { }
        mp = null
    }

    fun stop() {
        token++
        rememberPosition()
        releasePlayer()
        queue = emptyList()
        index = -1
        playing = false
        preparing = false
        failed = false
        position = 0
        duration = 0
        loopA = null
        loopB = null
        sleepAt = 0L
        ticker?.cancel()
        PlayerService.sync(Engine.app)
    }
}
