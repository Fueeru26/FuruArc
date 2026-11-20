package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Teks pengaturan Tema dan Bahasa (lima bahasa). */
class SysStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val themeLabel get() = p("Tema", "Theme", "テーマ", "主题", "테마")
    val themeSystem get() = p("Ikuti sistem", "Follow system", "システムに従う", "跟随系统", "시스템 설정 따름")
    val themeLight get() = p("Terang", "Light", "ライト", "浅色", "라이트")
    val themeDark get() = p("Gelap", "Dark", "ダーク", "深色", "다크")
    val langLabel get() = p("Pilih bahasa", "Choose language", "言語を選択", "选择语言", "언어 선택")
    val langSystem get() = p("Ikuti sistem", "Follow system", "システムに従う", "跟随系统", "시스템 설정 따름")
    val langEn get() = p("Inggris", "English", "英語", "英语", "영어")
    val langJa get() = p("Jepang", "Japanese", "日本語", "日语", "일본어")
    val langZh get() = p("Cina", "Chinese", "中国語", "中文", "중국어")
    val langKo get() = p("Korea", "Korean", "韓国語", "韩语", "한국어")
    val langId get() = p("Indonesia", "Indonesian", "インドネシア語", "印度尼西亚语", "인도네시아어")
    val langNote get() = p(
        "Bahasa Jepang, Cina, dan Korea baru menerjemahkan sebagian tampilan; sisanya tampil dalam bahasa Inggris.",
        "Japanese, Chinese and Korean translate only part of the interface for now; the rest is shown in English.",
        "日本語・中国語・韓国語は現在、画面の一部のみ翻訳されています。残りは英語で表示されます。",
        "日语、中文和韩语目前仅翻译了部分界面，其余部分以英语显示。",
        "일본어·중국어·한국어는 현재 화면 일부만 번역되어 있으며, 나머지는 영어로 표시됩니다.",
    )

    companion object {
        fun of(lang: Lang) = SysStr(lang)
    }
}
