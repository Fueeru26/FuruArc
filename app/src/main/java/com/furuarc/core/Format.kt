package com.furuarc.core

/** 1536 -> "1.5 KB" */
fun formatBytes(b: Long): String {
    if (b < 1024) return "$b B"
    val units = arrayOf("KB", "MB", "GB", "TB")
    var v = b.toDouble()
    var i = -1
    while (v >= 1024 && i < units.size - 1) {
        v /= 1024
        i++
    }
    return "%.1f %s".format(v, units[i])
}

/** 75 -> "01:15", 3700 -> "1:01:40" */
fun formatDuration(sec: Long): String {
    val s = if (sec < 0) 0L else sec
    val h = s / 3600
    val m = (s % 3600) / 60
    val ss = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, ss) else "%02d:%02d".format(m, ss)
}
