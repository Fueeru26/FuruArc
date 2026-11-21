package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Teks pengaturan Tema dan Bahasa (lima bahasa). */
class SysStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val uiStyleLabel get() = p("Gaya Tampilan", "Display Style", "表示スタイル", "显示风格", "화면 스타일")
    val uiClassic get() = p("Klasik", "Classic", "クラシック", "经典", "클래식")
    val uiMinimal get() = p("Minimalis", "Minimal", "ミニマル", "极简", "미니멀")
    val uiSoft get() = p("Lembut", "Soft", "ソフト", "柔和", "소프트")
    val uiGradient get() = p("Gradien", "Gradient", "グラデーション", "渐变", "그라데이션")
    val uiAmoled get() = p("Warna Pekat", "Solid", "ソリッド", "纯色", "솔리드")
    val uiSakura get() = p("Sakura", "Sakura", "桜", "樱花", "벚꽃")
    val uiOcean get() = p("Samudra", "Ocean", "海", "海洋", "바다")
    val uiForest get() = p("Hutan", "Forest", "森", "森林", "숲")
    val uiSunset get() = p("Senja", "Sunset", "夕焼け", "日落", "노을")
    val uiNeon get() = p("Neon", "Neon", "ネオン", "霓虹", "네온")
    val uiPaper get() = p("Kertas", "Paper", "和紙", "纸张", "종이")
    val navFloatIcons get() = p("Ikon Layang", "Floating Icons", "フローティングアイコン", "悬浮图标", "플로팅 아이콘")
    val navFab get() = p("Tombol Mengambang", "Floating Button", "フローティングボタン", "悬浮按钮", "플로팅 버튼")
    val navStyleLabel get() = p("Gaya Navigasi Bar", "Navigation Bar Style", "ナビゲーションバーのスタイル", "导航栏风格", "내비게이션 바 스타일")
    val navClassic get() = p("Klasik", "Classic", "クラシック", "经典", "클래식")
    val navFloating get() = p("Melayang", "Floating", "フローティング", "悬浮", "플로팅")
    val navIcons get() = p("Hanya Ikon", "Icons Only", "アイコンのみ", "仅图标", "아이콘만")
    val navPill get() = p("Pil Bergeser", "Sliding Pill", "スライドピル", "滑动胶囊", "슬라이딩 필")
    val navTopLine get() = p("Garis Atas", "Top Line", "トップライン", "顶部线条", "상단 라인")
    val uiAuto get() = p("Musiman (otomatis)", "Seasonal (automatic)", "季節（自動）", "季节（自动）", "계절(자동)")
    val themeSchedule get() = p("Jadwal (otomatis)", "Schedule (automatic)", "スケジュール（自動）", "定时（自动）", "일정(자동)")
    val darkFrom get() = p("Mulai mode gelap", "Dark mode starts at", "ダークモード開始", "深色模式开始", "다크 모드 시작")
    val darkTo get() = p("Selesai mode gelap", "Dark mode ends at", "ダークモード終了", "深色模式结束", "다크 모드 종료")
    val animLabel get() = p("Animasi", "Animations", "アニメーション", "动画", "애니메이션")
    val animDesc get() = p("Animasi tekan dan transisi layar", "Press animations and screen transitions", "タップと画面遷移のアニメーション", "按压动画和屏幕过渡", "누름 애니메이션과 화면 전환")
    val hapticLabel get() = p("Getaran halus", "Haptic feedback", "触覚フィードバック", "触感反馈", "햅틱 피드백")
    val hapticDesc get() = p("Getar tipis saat menekan tab dan tombol", "A light vibration when tapping tabs and buttons", "タブやボタンをタップすると軽く振動", "点按标签和按钮时轻微震动", "탭과 버튼을 누를 때 가볍게 진동")
    val openWithLabel get() = p("Muncul di \"Buka dengan\" untuk semua file", "Show in \"Open with\" for all files", "すべてのファイルの「アプリで開く」に表示", "在所有文件的“打开方式”中显示", "모든 파일의 '연결 프로그램'에 표시")
    val openWithDesc get() = p("Format yang tidak dikenal dibuka di Pemeriksaan File", "Unknown formats open in File Check", "未対応の形式はファイルチェックで開く", "未知格式将在文件检查中打开", "알 수 없는 형식은 파일 검사에서 열림")
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
