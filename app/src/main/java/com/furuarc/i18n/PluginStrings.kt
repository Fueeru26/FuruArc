package com.furuarc.i18n

import com.furuarc.settings.Lang

import com.furuarc.core.PluginStr2

/** Teks Pengelola Plugin (Pemindai Plugin dan Editor Plugin.js). */
class PluginStr(private val id: Boolean, private val lang: Lang = if (id) Lang.ID else Lang.EN) : PluginStr2 {
    private fun t(a: String, b: String) = if (id) a else (Tr.get(lang, b) ?: b)
    private fun tt(a: String, b: String, vararg x: Any?) = if (id) a else Tr.trf(lang, b, *x)

    val mgrTitle get() = t("Pengelola Plugin", "Plugin Manager")
    val mgrSub get() = t("Pindai plugin RPG Maker MV/MZ dan atur plugins.js", "Scan RPG Maker MV/MZ plugins and manage plugins.js")
    val scanTitle get() = t("Pemindai Plugin", "Plugin Scanner")
    val scanSub get() = t("Cari teks dan nilai yang bisa diubah di file plugin", "Find editable texts and values in plugin files")
    val editTitle get() = t("Editor Plugin.js", "Plugin.js Editor")
    val editSub get() = t("Aktif/nonaktifkan plugin dan tambah plugin baru", "Enable/disable plugins and add new ones")

    // pemindai
    val folderLabel get() = t("Folder plugin (js/plugins)", "Plugin folder (js/plugins)")
    val scanBtn get() = t("Pindai", "Scan")
    val scanHint get() = t(
        "Pilih folder plugin game RPG Maker MV/MZ (js/plugins). Folder game atau folder js juga boleh; yang dipindai hanya js/plugins.",
        "Pick the plugin folder of an RPG Maker MV/MZ game (js/plugins). The game or js folder also works; only js/plugins is scanned.",
    )
    val scanning get() = t("Memindai…", "Scanning…")
    val noPlugins get() = t("Tidak ada file plugin (.js) di folder ini.", "No plugin (.js) files in this folder.")
    fun summary(files: Int, texts: Int, values: Int) = tt("$files plugin dipindai · $texts teks · $values nilai yang bisa diubah", "{0} plugin(s) scanned · {1} text(s) · {2} editable value(s)", files, texts, values,
    )
    val nothingEditable get() = t("Tidak ditemukan teks non-ASCII maupun nilai bawaan yang bisa diubah.", "No non-ASCII texts or editable default values were found.")
    val infoTitle get() = t("Informasi plugin", "Plugin information")
    fun infoLine(params: Int, texts: Int, values: Int) = tt("$params parameter · $texts teks · $values nilai", "{0} parameter(s) · {1} text(s) · {2} value(s)", params, texts, values)
    val areaTitle get() = t("Area Pengeditan Plugin", "Plugin Editing Area")
    val areaHint get() = t(
        "Isi kolom hanya pada butir yang mau diubah; kolom kosong = tidak diubah. Teks di kode ditulis ulang dengan escape yang benar. Nilai bawaan di kode (mis. parameters['x'] || 10) berlaku bila plugins.js tidak mengisinya; nilai di anotasi (@default) hanya mengubah bawaan yang dilihat editor RPG Maker.",
        "Fill only the items you want to change; an empty field = unchanged. Texts in code are rewritten with proper escaping. Code defaults (e.g. parameters['x'] || 10) apply when plugins.js does not set them; annotation values (@default) only change the default shown in the RPG Maker editor.",
    )
    val filterAll get() = t("Semua", "All")
    val filterText get() = t("Teks", "Texts")
    val filterValue get() = t("Nilai", "Values")
    val search get() = t("Cari…", "Search…")
    fun pageLabel(p: Int, n: Int, total: Int) = tt("Halaman $p dari $n · $total butir", "Page {0} of {1} · {2} item(s)", p, n, total)
    val kindCode get() = t("Teks di kode", "Text in code")
    val kindAnno get() = t("Teks anotasi", "Annotation text")
    val kindCodeVal get() = t("Nilai bawaan di kode", "Default value in code")
    val kindParamText get() = t("Parameter teks (plugins.js)", "Text parameter (plugins.js)")
    val kindParamVal get() = t("Parameter nilai (plugins.js)", "Value parameter (plugins.js)")
    val kindAnnoVal get() = t("Nilai @default", "@default value")
    val fieldText get() = t("Terjemahan", "Translation")
    val fieldValue get() = t("Nilai baru", "New value")
    val currentValue get() = t("Nilai saat ini", "Current value")
    val outLabel get() = t("Folder output", "Output folder")
    val apply get() = t("Terapkan", "Apply")
    val needOut get() = t("Pilih folder output dulu.", "Pick an output folder first.")
    val nothingChanged get() = t("Belum ada perubahan untuk diterapkan.", "No changes to apply yet.")
    fun applied(files: Int, items: Int) = tt("Selesai: $items butir diubah di $files file plugin.", "Done: {0} item(s) changed in {1} plugin file(s).", items, files)
    fun badValue(label: String) = tt("Nilai \"$label\" harus angka atau true/false (dilewati).", "Value \"{0}\" must be a number or true/false (skipped).", label)
    val outNote get() = t("Hanya file plugin yang berubah ditulis ke folder output (nama file sama).", "Only changed plugin files are written to the output folder (same file names).")

    // editor plugins.js
    val upPluginsJs get() = t("Unggah plugins.js", "Upload plugins.js")
    val upNewPlugin get() = t("Unggah Plugin Baru (.js)", "Upload New Plugin (.js)")
    val addPlugin get() = t("Tambahkan Plugin", "Add Plugin")
    val savePluginsJs get() = t("Simpan Plugin.js", "Save Plugin.js")
    val listTitle get() = t("Daftar Plugin", "Plugin List")
    val searchPlugin get() = t("Cari plugin", "Search plugin")
    val fAll get() = t("Semua", "All")
    val fOn get() = t("Aktif", "Enabled")
    val fOff get() = t("Tidak aktif", "Disabled")
    val stOn get() = t("Aktif", "Enabled")
    val stOff get() = t("Tidak aktif", "Disabled")
    val newTag get() = t("BARU", "NEW")
    fun count(total: Int, on: Int) = tt("$total plugin · $on aktif", "{0} plugin(s) · {1} enabled", total, on)
    val noneMatch get() = t("Tidak ada plugin yang cocok.", "No matching plugins.")
    val tapHint get() = t("Ketuk plugin untuk melihat informasi dan mengubah statusnya.", "Tap a plugin to see its information and change its status.")
    val dlgName get() = t("Nama", "Name")
    val dlgStatus get() = t("Status", "Status")
    val dlgDesc get() = t("Deskripsi", "Description")
    val dlgParams get() = t("Parameter", "Parameters")
    val dlgNone get() = t("(tidak ada)", "(none)")
    val dlgClose get() = t("Tutup", "Close")
    val dlgOrder get() = t("Urutan muat", "Load order")
    fun pendingNew(name: String) = tt("Siap ditambahkan: $name", "Ready to add: {0}", name)
    fun added(name: String) = tt("Plugin \"$name\" ditambahkan ke daftar (paling bawah).", "Plugin \"{0}\" added to the list (at the bottom).", name)
    fun dup(name: String) = tt("Plugin \"$name\" sudah ada di daftar.", "Plugin \"{0}\" is already in the list.", name)
    val needJs get() = t("Unggah plugins.js dulu.", "Upload plugins.js first.")
    val badJs get() = t("File plugins.js tidak bisa dibaca. Pastikan isinya daftar \$plugins bawaan RPG Maker.", "plugins.js could not be read. Make sure it holds the standard \$plugins list.")
    fun saved(n: Int, newFiles: Int) = t(
        "plugins.js disimpan ($n plugin)." + if (newFiles > 0) " $newFiles file plugin baru disalin ke folder output/plugins." else "",
        "plugins.js saved ($n plugin(s))." + if (newFiles > 0) " $newFiles new plugin file(s) copied to output/plugins." else "",
    )
    val saveNote get() = t(
        "plugins.js ditulis ke folder output; file plugin baru disalin ke subfolder plugins/. Salin keduanya ke js/ game (plugins.js) dan js/plugins/ (file plugin).",
        "plugins.js is written to the output folder; new plugin files are copied into a plugins/ subfolder. Copy them to the game's js/ (plugins.js) and js/plugins/ (plugin files).",
    )
    val leaveMsg get() = t("Perubahan yang belum disimpan akan hilang.", "Unsaved changes will be lost.")
    val leaveYes get() = t("Keluar", "Leave")
    val leaveNo get() = t("Tetap di sini", "Stay")
    val unsaved get() = t("Ada perubahan yang belum disimpan.", "There are unsaved changes.")

    // menu tambahan
    val chkTitle get() = t("Pemeriksa Plugin", "Plugin Checker")
    val chkSub get() = t("Cek plugins.js vs folder plugin: file hilang, ganda, dependensi, urutan", "Check plugins.js vs the plugin folder: missing, duplicate, dependencies, order")
    val cmpTitle get() = t("Pembanding plugins.js", "plugins.js Comparer")
    val cmpSub get() = t("Bandingkan dua plugins.js: tambah, hapus, status, parameter, urutan", "Compare two plugins.js: added, removed, status, parameters, order")

    val infoAll get() = t("Lihat semua informasi", "View all information")
    val infoPageTitle get() = t("Informasi plugin", "Plugin information")
    val infoSearch get() = t("Cari plugin", "Search plugin")
    val infoClose get() = t("Tutup", "Close")

    // pemindai: plugins.js, ekspor/impor terjemahan
    val pluginsJsLabel get() = t("plugins.js (opsional, untuk parameter plugin)", "plugins.js (optional, for plugin parameters)")
    val pluginsJsHint get() = t("Jika dipilih (atau ada di folder game/js), parameter teks dan nilai di plugins.js ikut dipindai dan bisa diedit. Parameter berisi JSON bersarang dilewati.", "If picked (or found in the game/js folder), text and value parameters in plugins.js are scanned and editable too. Parameters holding nested JSON are skipped.")
    val trSectionTitle get() = t("Ekspor / impor terjemahan plugin", "Export / import plugin translation")
    val trExportBtn get() = t("Ekspor teks plugin", "Export plugin texts")
    val trImportBtn get() = t("Impor terjemahan", "Import translation")
    fun trExported(n: Int, name: String) = tt("$n teks diekspor ke $name di folder output.", "{0} text(s) exported to {1} in the output folder.", n, name)
    fun trImported(n: Int) = tt("$n terjemahan diisi ke kolom editor. Periksa lalu tekan Terapkan.", "{0} translation(s) filled into the editor fields. Review, then press Apply.", n)
    val trNoMatch get() = t("Tidak ada terjemahan yang cocok dengan teks hasil pindai.", "No translation matched the scanned texts.")
    val closeNote get() = t("Tutup", "Close")
    fun trFailed(m: String) = tt("Gagal: $m", "Failed: {0}", m)
    val trNoText get() = t("Tidak ada teks untuk diekspor.", "There are no texts to export.")
    val bakNote get() = t("Jika file sudah ada di folder output, salinannya disimpan sebagai .bak sebelum ditimpa.", "If a file already exists in the output folder, a .bak copy is kept before it is overwritten.")

    // editor: folder plugin, urutan, hapus, profil, peringatan
    val folderOptLabel get() = t("Folder plugin (opsional, untuk peringatan)", "Plugin folder (optional, for warnings)")
    val moveUp get() = t("Naik", "Up")
    val moveDown get() = t("Turun", "Down")
    val remove get() = t("Hapus", "Remove")
    val removeTitle get() = t("Hapus dari daftar?", "Remove from list?")
    fun removeMsg(n: String) = tt("Plugin \"$n\" dihapus dari daftar (file plugin-nya tidak dihapus).", "Plugin \"{0}\" is removed from the list (its plugin file is not deleted).", n)
    val profSave get() = t("Simpan profil", "Save profile")
    val profApply get() = t("Terapkan profil", "Apply profile")
    val profName get() = t("Nama profil", "Profile name")
    val profNone get() = t("Belum ada profil tersimpan.", "No saved profiles yet.")
    val profDelete get() = t("Hapus", "Delete")
    val profCancel get() = t("Batal", "Cancel")
    val profOk get() = t("Simpan", "Save")
    fun profSaved(n: String, c: Int) = tt("Profil \"$n\" disimpan ($c plugin).", "Profile \"{0}\" saved ({1} plugin(s)).", n, c)
    fun profApplied(n: String, c: Int) = tt("Profil \"$n\" diterapkan ($c plugin diubah).", "Profile \"{0}\" applied ({1} plugin(s) changed).", n, c)
    val profNote get() = t("Profil menyimpan status aktif/nonaktif tiap plugin; saat diterapkan hanya plugin dengan nama yang sama yang berubah.", "A profile stores each plugin's enabled/disabled state; applying it only changes plugins with the same name.")
    fun viewIssues(n: Int) = tt("Lihat Peringatan ($n)", "View Warnings ({0})", n)
    val issuesPageTitle get() = t("Peringatan Plugin", "Plugin Warnings")
    val viewParams get() = t("Lihat Parameter", "View Parameters")
    val paramsPageTitle get() = t("Parameter Plugin", "Plugin Parameters")
    val paramsSearch get() = t("Cari parameter", "Search parameter")
    val paramsDone get() = t("Selesai", "Done")
    val issuesTitle get() = t("Peringatan", "Warnings")
    fun issuesCount(e: Int, w: Int, i: Int) = tt("$e galat · $w peringatan · $i info", "{0} error(s) · {1} warning(s) · {2} info", e, w, i)
    val noIssues get() = t("Tidak ditemukan masalah.", "No problems found.")
    val dlgIssues get() = t("Peringatan plugin ini", "Warnings for this plugin")
    val lvlErr get() = t("GALAT", "ERROR")
    val lvlWarn get() = t("PERINGATAN", "WARNING")
    val lvlInfo get() = t("INFO", "INFO")

    // pemeriksa
    val chkJsLabel get() = t("plugins.js", "plugins.js")
    val chkFolderLabel get() = t("Folder plugin (js/plugins)", "Plugin folder (js/plugins)")
    val chkRun get() = t("Periksa", "Check")
    val chkHint get() = t("Pilih plugins.js dan folder plugin. Yang diperiksa: nama ganda, plugin terdaftar tapi file tidak ada, file tidak terdaftar, file kosong, dan dependensi (@base, @orderAfter, @orderBefore) beserta urutannya.", "Pick plugins.js and the plugin folder. Checked: duplicate names, registered plugins whose file is missing, unregistered files, empty files, and dependencies (@base, @orderAfter, @orderBefore) with their order.")
    val chkNeed get() = t("Pilih plugins.js dulu.", "Pick plugins.js first.")
    val chkAll get() = t("Semua", "All")

    // pembanding
    val cmpA get() = t("plugins.js A (lama)", "plugins.js A (old)")
    val cmpB get() = t("plugins.js B (baru)", "plugins.js B (new)")
    val cmpHint get() = t("Pilih dua file plugins.js; hasilnya otomatis muncul.", "Pick two plugins.js files; the result appears automatically.")
    val cmpSame get() = t("Kedua plugins.js setara (tidak ada perbedaan).", "Both plugins.js are equivalent (no differences).")
    fun cmpSummary(n: Int) = tt("$n perbedaan", "{0} difference(s)", n)
    val dAll get() = t("Semua", "All")
    val dAdded get() = t("Baru", "Added")
    val dRemoved get() = t("Hilang", "Removed")
    val dStatus get() = t("Status", "Status")
    val dParams get() = t("Param", "Params")
    val dOrder get() = t("Urutan", "Order")
    val kAdded get() = t("DITAMBAH", "ADDED")
    val kRemoved get() = t("DIHAPUS", "REMOVED")
    val kStatus get() = t("STATUS", "STATUS")
    val kParams get() = t("PARAMETER", "PARAMETERS")
    val kOrder get() = t("URUTAN", "ORDER")

    // PluginStr2 (pesan pemeriksaan)
    override fun dup(a: Int, b: Int) = tt("Nama plugin ganda dengan parameter yang sama (posisi $a dan $b); kemungkinan tidak perlu.", "Duplicate plugin name with identical parameters (positions {0} and {1}); probably unnecessary.", a, b)
    override fun dupOff(a: Int, b: Int) = tt("Nama plugin muncul lagi (posisi $a dan $b) tetapi salah satunya nonaktif; biasanya tidak masalah.", "Plugin name appears again (positions {0} and {1}) but one of them is disabled; usually harmless.", a, b)
    override fun dupMulti(a: Int, b: Int) = tt("Nama plugin ganda (posisi $a dan $b); header plugin menyebut boleh dipakai lebih dari sekali.", "Duplicate plugin name (positions {0} and {1}); the plugin header says it may be used more than once.", a, b)
    override fun dupParams(a: Int, b: Int) = tt("Nama plugin ganda (posisi $a dan $b) dengan parameter berbeda; kemungkinan instance yang disengaja.", "Duplicate plugin name (positions {0} and {1}) with different parameters; likely intentional instances.", a, b)
    override fun unregGroup(n: Int) = tt("$n file ada di folder plugin tetapi tidak terdaftar di plugins.js (ketuk untuk melihat daftar).", "{0} files are in the plugin folder but not registered in plugins.js (tap to see the list).", n)
    override val snipList get() = t("plugins.js (posisi, status, nama):", "plugins.js (position, status, name):")
    override val snipParams get() = t("Beda parameter:", "Parameter differences:")
    override val snipHeader get() = t("Header plugin:", "Plugin header:")
    val maybeIntended get() = t("Mungkin disengaja", "May be intentional")
    val sureProblem get() = t("Hampir pasti bermasalah", "Almost certainly a problem")
    val tapForCode get() = t("Ketuk untuk melihat kode", "Tap to see the code")
    val ignoreBtn get() = t("Abaikan…", "Ignore…")
    val ignoreTitle get() = t("Abaikan temuan", "Ignore finding")
    val ignoreBody get() = t("Temuan yang diabaikan tidak muncul lagi di pemindaian berikutnya (berlaku untuk semua game). Bisa dipulihkan lewat tombol Diabaikan.", "Ignored findings no longer show in later scans (applies to all games). Restore them with the Ignored button.")
    val ignoreThis get() = t("Abaikan untuk plugin ini", "Ignore for this plugin")
    val ignoreKind get() = t("Abaikan jenis ini untuk semua plugin", "Ignore this type for all plugins")
    val ignoreCancel get() = t("Batal", "Cancel")
    fun ignoredBtn(n: Int) = tt("Diabaikan ($n)", "Ignored ({0})", n)
    val ignoredTitle get() = t("Temuan yang diabaikan", "Ignored findings")
    val restoreAll get() = t("Pulihkan semua", "Restore all")
    val restoreOne get() = t("Pulihkan", "Restore")
    val allPlugins get() = t("semua plugin", "all plugins")
    val searchIssues get() = t("Cari nama plugin atau teks", "Search plugin name or text")
    fun kindName(k: String) = when (k) {
        "dup" -> t("Nama ganda", "Duplicate name")
        "missing_on" -> t("File hilang (aktif)", "Missing file (enabled)")
        "missing_off" -> t("File belum ada (nonaktif)", "Missing file (disabled)")
        "empty" -> t("File kosong", "Empty file")
        "unreg" -> t("Tidak terdaftar", "Unregistered")
        "base_missing" -> t("@base tidak ada", "@base missing")
        "base_off" -> t("@base nonaktif", "@base disabled")
        "base_order" -> t("Urutan @base", "@base order")
        "after" -> t("Urutan @orderAfter", "@orderAfter order")
        "before" -> t("Urutan @orderBefore", "@orderBefore order")
        else -> k
    }
    override val missingOn get() = t("Aktif tetapi file .js-nya tidak ada di folder plugin (game bisa crash).", "Enabled but its .js file is missing from the plugin folder (the game may crash).")
    override val missingOff get() = t("Nonaktif dan file .js-nya belum ada di folder plugin (mungkin plugin yang belum selesai).", "Disabled and its .js file is not in the plugin folder yet (maybe an unfinished plugin).")
    override val emptyFile get() = t("File plugin kosong (0 byte).", "The plugin file is empty (0 bytes).")
    val unregisteredOld get() = t("File ada di folder plugin tetapi tidak terdaftar di plugins.js.", "The file is in the plugin folder but not registered in plugins.js.")
    override fun baseMissing(d: String) = tt("Butuh plugin dasar \"$d\" (@base) tetapi tidak ada di daftar.", "Needs base plugin \"{0}\" (@base) but it is not in the list.", d)
    override fun baseOff(d: String) = tt("Butuh plugin dasar \"$d\" (@base) tetapi plugin itu nonaktif.", "Needs base plugin \"{0}\" (@base) but it is disabled.", d)
    override fun baseOrder(d: String) = tt("\"$d\" (@base) harus dimuat sebelum plugin ini; pindahkan ke atas.", "\"{0}\" (@base) must load before this plugin; move it up.", d)
    override fun afterOrder(d: String) = tt("Sebaiknya dimuat setelah \"$d\" (@orderAfter), tetapi urutannya terbalik.", "Should load after \"{0}\" (@orderAfter) but the order is reversed.", d)
    override fun beforeOrder(d: String) = tt("Sebaiknya dimuat sebelum \"$d\" (@orderBefore), tetapi urutannya terbalik.", "Should load before \"{0}\" (@orderBefore) but the order is reversed.", d)

    companion object {
        val ID = PluginStr(true)
        val EN = PluginStr(false)
        private val cache = HashMap<Lang, PluginStr>()
        fun of(lang: Lang): PluginStr = cache.getOrPut(lang) { PluginStr(lang == Lang.ID, lang) }
    }
}

val Str.pl: PluginStr get() = PluginStr.of(this.langOf())
