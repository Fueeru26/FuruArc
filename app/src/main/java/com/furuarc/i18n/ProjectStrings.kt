package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Text of the Create Project menu (Translation Project). */
class ProjectStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val menuTitle get() = p("Buat Proyek", "Create Project", "プロジェクトを作成", "创建项目", "프로젝트 만들기")
    val menuSub get() = p("Alur kerja bertahap dengan alat yang sudah ada", "Step-by-step workflows with the existing tools", "既存のツールで進める手順ワークフロー", "使用现有工具的分步工作流", "기존 도구로 진행하는 단계별 작업")
    val trTitle get() = p("Proyek Terjemahan", "Translation Project", "翻訳プロジェクト", "翻译项目", "번역 프로젝트")
    val trSub get() = p("Bongkar, ekspor teks, terjemahkan, impor, kemas, uji", "Unpack, export text, translate, import, pack, test", "展開、テキスト出力、翻訳、取り込み、パック、テスト", "解包、导出文本、翻译、导入、打包、测试", "풀기, 텍스트 내보내기, 번역, 가져오기, 묶기, 테스트")
    val newBtn get() = p("Proyek baru", "New project", "新規プロジェクト", "新建项目", "새 프로젝트")
    val newTitle get() = p("Proyek Terjemahan baru", "New Translation Project", "新しい翻訳プロジェクト", "新建翻译项目", "새 번역 프로젝트")
    val nameLabel get() = p("Nama proyek (misalnya nama game)", "Project name (for example the game name)", "プロジェクト名（例：ゲーム名）", "项目名称（例如游戏名称）", "프로젝트 이름 (예: 게임 이름)")
    val create get() = p("Buat", "Create", "作成", "创建", "만들기")
    val cancel get() = p("Batal", "Cancel", "キャンセル", "取消", "취소")
    val empty get() = p("Belum ada proyek. Tekan + untuk membuat proyek pertama.", "No projects yet. Press + to create the first one.", "プロジェクトはまだありません。+ で最初のプロジェクトを作成します。", "还没有项目。点击 + 创建第一个项目。", "프로젝트가 없습니다. +를 눌러 첫 프로젝트를 만드세요.")
    fun progress(done: Int, total: Int) = p("$done dari $total langkah selesai", "$done of $total steps done", "$total 中 $done 完了", "已完成 $done / $total 步", "$total 단계 중 $done 완료")
    val stepsTitle get() = p("Langkah", "Steps", "手順", "步骤", "단계")
    val noteTitle get() = p("Catatan", "Note", "メモ", "备注", "메모")
    val noteLabel get() = p("Catatan proyek (opsional)", "Project note (optional)", "プロジェクトのメモ（任意）", "项目备注（可选）", "프로젝트 메모 (선택)")
    val intro get() = p(
        "Setiap langkah membuka alat yang sudah ada. Setelah selesai, kembali ke sini dan centang \"Selesai\". Langkah boleh dikerjakan dalam urutan apa pun dan dilewati.",
        "Each step opens an existing tool. When it is finished, come back here and tick \"Done\". Steps can be done in any order and skipped.",
        "各手順は既存のツールを開きます。終わったらここに戻って「完了」にチェックを入れます。手順は順不同で、省略もできます。",
        "每一步都会打开现有工具。完成后回到这里勾选“完成”。步骤可以按任意顺序进行，也可以跳过。",
        "각 단계는 기존 도구를 엽니다. 끝나면 여기로 돌아와 \"완료\"를 체크하세요. 단계는 순서와 상관없이 진행하거나 건너뛸 수 있습니다.",
    )
    val open get() = p("Buka", "Open", "開く", "打开", "열기")
    val done get() = p("Selesai", "Done", "完了", "完成", "완료")
    val stNone get() = p("Belum", "Not started", "未着手", "未开始", "시작 전")
    val stOpen get() = p("Dibuka", "Opened", "開いた", "已打开", "열림")
    val stDone get() = p("Selesai", "Done", "完了", "完成", "완료")
    val rename get() = p("Ganti nama", "Rename", "名前を変更", "重命名", "이름 변경")
    val delete get() = p("Hapus proyek", "Delete project", "プロジェクトを削除", "删除项目", "프로젝트 삭제")
    val deleteAsk get() = p("Hapus proyek ini? File game dan hasil kerja tidak ikut terhapus, hanya catatan langkahnya.", "Delete this project? Game files and results are not deleted, only the step notes.", "このプロジェクトを削除しますか？ゲームのファイルや作業結果は削除されず、手順の記録だけが消えます。", "删除此项目？游戏文件和成果不会被删除，只会删除步骤记录。", "이 프로젝트를 삭제할까요? 게임 파일과 결과물은 삭제되지 않고 단계 기록만 삭제됩니다.")
    val yes get() = p("Hapus", "Delete", "削除", "删除", "삭제")

    fun stepTitle(s: Int) = when (s) {
        1 -> p("Bongkar arsip", "Unpack archive", "アーカイブを展開", "解包档案", "아카이브 풀기")
        2 -> p("Ekspor teks", "Export text", "テキストを出力", "导出文本", "텍스트 내보내기")
        3 -> p("Terjemahkan", "Translate", "翻訳", "翻译", "번역")
        4 -> p("Impor teks", "Import text", "テキストを取り込み", "导入文本", "텍스트 가져오기")
        5 -> p("Kemas arsip", "Pack archive", "アーカイブをパック", "打包档案", "아카이브 묶기")
        else -> p("Uji format", "Format test", "フォーマットテスト", "格式测试", "포맷 테스트")
    }
    fun stepDesc(s: Int) = when (s) {
        1 -> p("Bongkar arsip game ke folder kerja. Engine dikenali otomatis.", "Unpack the game archive into a work folder. The engine is detected automatically.", "ゲームのアーカイブを作業フォルダに展開します。エンジンは自動検出されます。", "将游戏档案解包到工作文件夹。引擎会自动识别。", "게임 아카이브를 작업 폴더로 풉니다. 엔진은 자동으로 감지됩니다.")
        2 -> p("Kumpulkan dialog dan pilihan menjadi satu file teks (hanya untuk engine yang didukung).", "Collect dialogue and choices into one text file (only for supported engines).", "セリフと選択肢を1つのテキストファイルにまとめます（対応エンジンのみ）。", "将对话和选项收集到一个文本文件中（仅限受支持的引擎）。", "대사와 선택지를 하나의 텍스트 파일로 모읍니다 (지원되는 엔진만).")
        3 -> p("Terjemahkan teks di Area Terjemahan, dengan draf, glosarium, dan terjemahan online.", "Translate the text in the Translation Area, with drafts, a glossary and online translation.", "翻訳エリアでテキストを翻訳します（下書き、用語集、オンライン翻訳）。", "在翻译区翻译文本，可用草稿、术语表和在线翻译。", "번역 영역에서 초안, 용어집, 온라인 번역으로 텍스트를 번역합니다.")
        4 -> p("Masukkan hasil terjemahan kembali ke file skrip.", "Put the translation back into the script files.", "翻訳結果をスクリプトファイルに書き戻します。", "将译文放回脚本文件。", "번역 결과를 스크립트 파일에 다시 넣습니다.")
        5 -> p("Kemas folder kerja menjadi arsip baru.", "Pack the work folder into a new archive.", "作業フォルダを新しいアーカイブにパックします。", "将工作文件夹打包成新档案。", "작업 폴더를 새 아카이브로 묶습니다.")
        else -> p("Periksa bahwa format bisa dikemas dan dibongkar lagi dengan benar (opsional).", "Check that the format packs and unpacks again correctly (optional).", "フォーマットが正しくパック・展開できるか確認します（任意）。", "检查该格式能否正确打包并再次解包（可选）。", "포맷이 올바르게 묶이고 다시 풀리는지 확인합니다 (선택).")
    }
}

fun projectStr(lang: Lang) = ProjectStr(lang)
