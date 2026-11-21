package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Teks Media Player (lima bahasa). */
class PlayerStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val menuTitle get() = p("Media Player", "Media Player", "メディアプレーヤー", "媒体播放器", "미디어 플레이어")
    val menuSub get() = p("Lihat gambar, putar audio dan video dari file atau folder", "View images, play audio and video from files or folders", "ファイルやフォルダーから画像表示・音声/動画再生", "从文件或文件夹查看图片、播放音频和视频", "파일이나 폴더에서 이미지 보기, 오디오/비디오 재생")
    val imageTitle get() = p("Galeri Gambar", "Image Gallery", "画像ギャラリー", "图片库", "이미지 갤러리")
    val imageSub get() = p("Lihat gambar, zoom, dan slideshow", "View images, zoom and slideshow", "画像表示、ズーム、スライドショー", "查看图片、缩放、幻灯片", "이미지 보기, 확대, 슬라이드쇼")
    val audioTitle get() = p("Pemutar Audio", "Audio Player", "オーディオプレーヤー", "音频播放器", "오디오 플레이어")
    val audioSub get() = p("Daftar putar, acak, ulang, kecepatan, loop A-B, timer tidur", "Playlist, shuffle, repeat, speed, A-B loop, sleep timer", "プレイリスト、シャッフル、リピート、速度、A-Bループ、スリープタイマー", "播放列表、随机、循环、倍速、A-B 循环、睡眠定时", "재생목록, 셔플, 반복, 속도, A-B 반복, 수면 타이머")
    val videoTitle get() = p("Pemutar Video", "Video Player", "ビデオプレーヤー", "视频播放器", "비디오 플레이어")
    val videoSub get() = p("Putar video, kecepatan, lanjut dari posisi terakhir", "Play videos, speed, resume from last position", "動画再生、速度、前回の位置から再開", "播放视频、倍速、从上次位置继续", "비디오 재생, 속도, 마지막 위치부터 이어 재생")
    val settingsTitle get() = p("Pengaturan Media", "Media Settings", "メディア設定", "媒体设置", "미디어 설정")
    val settingsSub get() = p("Pengaturan pemutar, ekspor dan impor", "Player settings, export and import", "プレーヤー設定、エクスポートとインポート", "播放器设置、导出和导入", "플레이어 설정, 내보내기 및 가져오기")

    val pickFiles get() = p("Pilih file", "Pick files", "ファイルを選択", "选择文件", "파일 선택")
    val pickFolder get() = p("Pilih folder", "Pick folder", "フォルダーを選択", "选择文件夹", "폴더 선택")
    val subfolders get() = p("Termasuk subfolder", "Include subfolders", "サブフォルダーを含める", "包含子文件夹", "하위 폴더 포함")
    val scanning get() = p("Memindai…", "Scanning…", "スキャン中…", "扫描中…", "검색 중…")
    fun found(n: Int) = p("$n file", "$n files", "${n}件", "$n 个文件", "${n}개 파일")
    val emptyHint get() = p("Belum ada file. Pilih file atau folder untuk memulai.", "Nothing here yet. Pick files or a folder to start.", "まだありません。ファイルまたはフォルダーを選択してください。", "还没有文件。请选择文件或文件夹。", "아직 없습니다. 파일이나 폴더를 선택하세요.")
    val noMatch get() = p("Tidak ada file yang cocok di sini.", "No matching files here.", "一致するファイルがありません。", "这里没有匹配的文件。", "일치하는 파일이 없습니다.")
    val loadFail get() = p("Gagal memutar file ini.", "Could not play this file.", "このファイルを再生できません。", "无法播放此文件。", "이 파일을 재생할 수 없습니다.")
    val loading get() = p("Memuat…", "Loading…", "読み込み中…", "加载中…", "불러오는 중…")
    val close get() = p("Tutup", "Close", "閉じる", "关闭", "닫기")

    val shuffle get() = p("Acak", "Shuffle", "シャッフル", "随机", "셔플")
    fun repeat(m: Int) = when (m) {
        1 -> p("Ulang: Semua", "Repeat: All", "リピート: 全曲", "循环：全部", "반복: 전체")
        2 -> p("Ulang: Satu", "Repeat: One", "リピート: 1曲", "循环：单曲", "반복: 한 곡")
        else -> p("Ulang: Mati", "Repeat: Off", "リピート: オフ", "循环：关", "반복: 끔")
    }
    fun speed(v: Float) = p("Kecepatan ", "Speed ", "速度 ", "倍速 ", "속도 ") + (if (v == v.toInt().toFloat()) v.toInt().toString() else v.toString()) + "x"
    fun abLoop(a: Int?, b: Int?) = when {
        a == null -> "A-B"
        b == null -> "A-"
        else -> "A-B ✓"
    }
    fun sleep(remainMin: Int) = if (remainMin <= 0) p("Timer tidur", "Sleep timer", "スリープタイマー", "睡眠定时", "수면 타이머") else p("Tidur ${remainMin} mnt", "Sleep ${remainMin} min", "スリープ ${remainMin}分", "睡眠 ${remainMin} 分", "수면 ${remainMin}분")
    val stop get() = p("Berhenti", "Stop", "停止", "停止", "정지")
    val slideshow get() = p("Slideshow", "Slideshow", "スライドショー", "幻灯片", "슬라이드쇼")
    val resetZoom get() = p("Reset zoom", "Reset zoom", "ズームをリセット", "重置缩放", "확대 초기화")

    // settings page
    val setAutoNext get() = p("Putar berikutnya otomatis", "Play next automatically", "次を自動再生", "自动播放下一个", "다음 자동 재생")
    val setAutoNextDesc get() = p("Lanjut ke file berikutnya saat selesai", "Continue with the next file when one ends", "終了したら次のファイルへ", "播放结束后继续下一个", "끝나면 다음 파일로 이동")
    val setShuffle get() = p("Acak secara bawaan", "Shuffle by default", "デフォルトでシャッフル", "默认随机", "기본 셔플")
    val setRepeat get() = p("Mode ulang", "Repeat mode", "リピートモード", "循环模式", "반복 모드")
    val setSpeed get() = p("Kecepatan bawaan", "Default speed", "デフォルト速度", "默认倍速", "기본 속도")
    val setRemember get() = p("Ingat posisi terakhir", "Remember last position", "最後の位置を記憶", "记住上次位置", "마지막 위치 기억")
    val setRememberDesc get() = p("Audio dan video lanjut dari posisi terakhir", "Audio and video resume where you left off", "音声と動画を前回の位置から再開", "音频和视频从上次位置继续", "오디오와 비디오를 마지막 위치부터 이어 재생")
    val setBackground get() = p("Putar audio di latar belakang", "Play audio in the background", "バックグラウンドで音声再生", "后台播放音频", "백그라운드 오디오 재생")
    val setBackgroundDesc get() = p("Dengan notifikasi dan tombol kontrol", "With a notification and controls", "通知とコントロール付き", "带通知和控制按钮", "알림과 컨트롤 포함")
    val setSlideshow get() = p("Jeda slideshow gambar", "Image slideshow interval", "スライドショーの間隔", "幻灯片间隔", "슬라이드쇼 간격")
    fun seconds(n: Int) = p("$n detik", "$n s", "${n}秒", "$n 秒", "${n}초")
    val setVideoAuto get() = p("Putar video otomatis", "Autoplay video", "動画を自動再生", "自动播放视频", "비디오 자동 재생")
    val setKeepOn get() = p("Layar tetap menyala saat video", "Keep screen on during video", "動画中は画面を点灯", "播放视频时保持屏幕常亮", "비디오 재생 중 화면 켜짐 유지")
    val setBackup get() = p("Cadangan pengaturan", "Settings backup", "設定のバックアップ", "设置备份", "설정 백업")
    val exportBtn get() = p("Ekspor pengaturan", "Export settings", "設定をエクスポート", "导出设置", "설정 내보내기")
    val importBtn get() = p("Impor pengaturan", "Import settings", "設定をインポート", "导入设置", "설정 가져오기")
    val resetBtn get() = p("Kembalikan ke bawaan", "Reset to defaults", "初期設定に戻す", "恢复默认", "기본값으로 복원")
    val exported get() = p("Pengaturan diekspor.", "Settings exported.", "設定をエクスポートしました。", "设置已导出。", "설정을 내보냈습니다.")
    val imported get() = p("Pengaturan diimpor.", "Settings imported.", "設定をインポートしました。", "设置已导入。", "설정을 가져왔습니다.")
    val importFail get() = p("File ini bukan pengaturan Media Player yang valid.", "This is not a valid Media Player settings file.", "有効なメディアプレーヤー設定ファイルではありません。", "这不是有效的媒体播放器设置文件。", "유효한 미디어 플레이어 설정 파일이 아닙니다.")
    val resetDone get() = p("Pengaturan dikembalikan.", "Settings restored.", "設定を元に戻しました。", "设置已恢复。", "설정이 복원되었습니다.")

    // other places
    val openInPlayer get() = p("Buka di Media Player", "Open in Media Player", "メディアプレーヤーで開く", "在媒体播放器中打开", "미디어 플레이어에서 열기")
    val preparing get() = p("Menyiapkan file…", "Preparing file…", "ファイルを準備中…", "正在准备文件…", "파일 준비 중…")
}

fun playerStr(lang: Lang) = PlayerStr(lang)
