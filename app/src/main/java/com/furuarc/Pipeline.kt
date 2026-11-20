package com.furuarc

import android.net.Uri

/**
 * Connects workflows between screens: from the Media Asset Converter result straight to Repack (Pack).
 * [onRepack] is set by MainActivity; [ignoreBackup] makes Pack skip the "_backup" folder (Overwrite Original Files backup).
 */
object Pipeline {
    @Volatile var ignoreBackup = false
    @Volatile var onRepack: ((Uri) -> Unit)? = null
}
