package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Text of the Create Project menu (Translation Project). */
class ProjectStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val menuTitle get() = p("Buat Proyek", "Create Project", "プロジェクトを作成", "创建项目", "프로젝트 만들기")
    val menuSub get() = p("Alur kerja bertahap dengan alat yang sudah ada", "Step-by-step workflows with the existing tools", "既存のツールで進める手順ワークフロー", "使用现有工具的分步工作流", "기존 도구로 진행하는 단계별 작업")
    val trTitle get() = p("Proyek Terjemahan", "Translation Project", "翻訳プロジェクト", "翻译项目", "번역 프로젝트")
    val trSub get() = p("Bongkar, ekspor teks, terjemahkan, impor, kemas, uji", "Unpack, export text, translate, import, pack, test", "展開、テキスト出力、翻訳、取り込み、パック、テスト", "解包、导出文本、翻译、导入、打包、测试", "풀기, 텍스트 내보내기, 번역, 가져오기, 묶기, 테스트")
    val cmpTitle get() = p("Proyek Kompres Game", "Compress Game Project", "ゲーム圧縮プロジェクト", "游戏压缩项目", "게임 압축 프로젝트")
    val cmpSub get() = p("Bongkar, pindai, kompres audio, gambar, video, lalu kemas", "Unpack, scan, compress audio, images, video, then pack", "展開、スキャン、音声・画像・動画の圧縮、パック", "解包、扫描、压缩音频、图片、视频，然后打包", "풀기, 스캔, 오디오·이미지·비디오 압축, 묶기")
    val cmpNewTitle get() = p("Proyek Kompres Game baru", "New Compress Game Project", "新しいゲーム圧縮プロジェクト", "新建游戏压缩项目", "새 게임 압축 프로젝트")
    val cmpIntro get() = p(
        "Kompres aset game dengan alat yang sudah ada. Langkah Bongkar, Kompres, dan Kemas ditandai Selesai otomatis jika berjalan tanpa gagal; Pindai dicentang sendiri. Aset yang dikompres bisa tidak diterima engine game (misalnya format audio atau gambar lain), jadi pakai Pemeriksa Kompatibilitas di alat konverter dan coba game hasilnya. Simpan arsip asli.",
        "Compress game assets with the existing tools. Unpack, Compress and Pack are marked Done automatically when they ran without failures; Scan is ticked by you. Compressed assets may not be accepted by the game engine (for example another audio or image format), so use the Compatibility Checker in the converter tools and try the resulting game. Keep the original archive.",
        "既存のツールでゲームのアセットを圧縮します。展開・圧縮・パックは失敗なく終わると自動で完了になり、スキャンは手動でチェックします。圧縮したアセットはエンジンが受け付けない場合があるため、変換ツールの互換性チェッカーを使い、できあがったゲームを試してください。元のアーカイブは残しておきます。",
        "使用现有工具压缩游戏资源。解包、压缩和打包在无失败运行后会自动标记为完成；扫描需要自己勾选。压缩后的资源可能不被游戏引擎接受（例如其他音频或图片格式），请使用转换工具中的兼容性检查，并试玩生成的游戏。请保留原始档案。",
        "기존 도구로 게임 에셋을 압축합니다. 풀기·압축·묶기는 실패 없이 실행되면 자동으로 완료 처리되며, 스캔은 직접 체크합니다. 압축된 에셋은 게임 엔진이 받아들이지 않을 수 있으니 변환 도구의 호환성 검사기를 사용하고 결과 게임을 시험해 보세요. 원본 아카이브는 보관하세요.",
    )
    val extTitle get() = p("Proyek Ekstrak Aset", "Extract Assets Project", "アセット抽出プロジェクト", "提取资源项目", "에셋 추출 프로젝트")
    val extSub get() = p("Bongkar, pindai, lihat, konversi, hapus duplikat, ganti nama aset", "Unpack, scan, view, convert, remove duplicates, rename assets", "展開、スキャン、閲覧、変換、重複削除、名前変更", "解包、扫描、查看、转换、去重、重命名资源", "풀기, 스캔, 보기, 변환, 중복 제거, 이름 변경")
    val extNewTitle get() = p("Proyek Ekstrak Aset baru", "New Extract Assets Project", "新しいアセット抽出プロジェクト", "新建提取资源项目", "새 에셋 추출 프로젝트")
    val extIntro get() = p(
        "Ambil gambar, audio, dan video dari game untuk dilihat atau dipakai ulang. Bongkar arsip dulu, lalu pindai, lihat di Media Player, ubah ke format umum, cari duplikat, dan ganti nama. Langkah Bongkar dan Konversi ditandai Selesai otomatis jika berjalan tanpa gagal; langkah lain dicentang sendiri. Aset milik pembuat game, jadi gunakan hanya sesuai izin mereka.",
        "Pull images, audio and video out of a game to view or reuse them. Unpack the archive first, then scan, view in the Media Player, convert to common formats, find duplicates and rename. Unpack and Convert are marked Done automatically when they ran without failures; the other steps you tick yourself. Assets belong to the game's creators, so use them only as they allow.",
        "ゲームから画像・音声・動画を取り出して閲覧や再利用をします。まずアーカイブを展開し、スキャン、メディアプレーヤーで閲覧、一般的な形式へ変換、重複検索、名前変更の順に進めます。展開と変換は失敗なく終わると自動で完了になり、他の手順は自分でチェックします。アセットは制作者のものなので、許可された範囲で使ってください。",
        "从游戏中取出图片、音频和视频，用于查看或再利用。先解包，然后扫描、在媒体播放器中查看、转换为通用格式、查找重复项并重命名。解包和转换在无失败运行后会自动标记为完成，其他步骤需自己勾选。资源归游戏作者所有，请仅在其许可范围内使用。",
        "게임에서 이미지·오디오·비디오를 꺼내 보거나 재사용합니다. 먼저 아카이브를 풀고, 스캔, 미디어 플레이어로 보기, 일반 형식으로 변환, 중복 찾기, 이름 변경 순으로 진행합니다. 풀기와 변환은 실패 없이 실행되면 자동으로 완료 처리되고, 나머지 단계는 직접 체크합니다. 에셋은 제작자의 것이니 허용된 범위에서만 사용하세요.",
    )
    val patTitle get() = p("Proyek Buat Patch", "Make Patch Project", "パッチ作成プロジェクト", "制作补丁项目", "패치 만들기 프로젝트")
    val patSub get() = p("Bongkar asli, ubah file, bandingkan, kemas hanya file yang berubah", "Unpack the original, edit files, compare, pack only the changed files", "元を展開、編集、比較、変更したファイルだけをパック", "解包原版、编辑文件、对比、只打包改动的文件", "원본 풀기, 파일 편집, 비교, 바뀐 파일만 묶기")
    val patNewTitle get() = p("Proyek Buat Patch baru", "New Make Patch Project", "新しいパッチ作成プロジェクト", "新建制作补丁项目", "새 패치 만들기 프로젝트")
    val patIntro get() = p(
        "Buat patch kecil berisi hanya file yang kamu ubah. Bongkar arsip asli ke folder kerja, ubah file di editor, lalu bandingkan folder asli dan folder hasil ubahan untuk melihat file mana yang berubah. Salin hanya file yang berubah ke folder baru, lalu kemas folder itu menjadi arsip patch. Bongkar dan Kemas ditandai Selesai otomatis jika berjalan tanpa gagal; langkah lain dicentang sendiri. Simpan arsip asli.",
        "Make a small patch that holds only the files you changed. Unpack the original archive into the work folder, change files in the editors, then compare the original folder with the changed folder to see which files differ. Copy only the changed files into a new folder and pack that folder as the patch archive. Unpack and Pack are marked Done automatically when they ran without failures; the other steps you tick yourself. Keep the original archive.",
        "変更したファイルだけを含む小さなパッチを作ります。元のアーカイブを作業フォルダに展開し、エディタでファイルを変更し、元のフォルダと変更後のフォルダを比較して違うファイルを確認します。変更したファイルだけを新しいフォルダにコピーし、そのフォルダをパッチのアーカイブとしてパックします。展開とパックは失敗なく終わると自動で完了になり、他の手順は自分でチェックします。元のアーカイブは残しておきます。",
        "制作只包含你修改过的文件的小补丁。把原始档案解包到工作文件夹，用编辑器修改文件，然后对比原始文件夹和修改后的文件夹，看哪些文件不同。只把改动的文件复制到新文件夹，再把该文件夹打包成补丁档案。解包和打包在无失败运行后会自动标记为完成，其他步骤需自己勾选。请保留原始档案。",
        "바뀐 파일만 담은 작은 패치를 만듭니다. 원본 아카이브를 작업 폴더로 풀고, 편집기에서 파일을 바꾼 뒤, 원본 폴더와 바뀐 폴더를 비교해 달라진 파일을 확인합니다. 바뀐 파일만 새 폴더에 복사하고 그 폴더를 패치 아카이브로 묶습니다. 풀기와 묶기는 실패 없이 실행되면 자동으로 완료 처리되고, 나머지 단계는 직접 체크합니다. 원본 아카이브는 보관하세요.",
    )
    val audTitle get() = p("Proyek Audit / Cek Game", "Audit Game Project", "ゲーム監査プロジェクト", "游戏检查项目", "게임 점검 프로젝트")
    val audSub get() = p("Bongkar, pindai folder, cek terjemahan, cari duplikat, uji format", "Unpack, scan the folder, check translations, find duplicates, test the format", "展開、フォルダのスキャン、翻訳チェック、重複検索、形式テスト", "解包、扫描文件夹、检查翻译、查找重复项、测试格式", "풀기, 폴더 스캔, 번역 점검, 중복 찾기, 포맷 테스트")
    val audNewTitle get() = p("Proyek Audit / Cek Game baru", "New Audit Game Project", "新しいゲーム監査プロジェクト", "新建游戏检查项目", "새 게임 점검 프로젝트")
    val audIntro get() = p(
        "Periksa sebuah game tanpa mengubahnya: bongkar arsip, pindai isi folder, cek sisa teks yang belum diterjemahkan, cari file kembar, dan uji bahwa formatnya bisa dikemas dan dibongkar lagi. Alat pemeriksaan hanya membaca, jadi langkahnya dicentang sendiri (kecuali Bongkar yang otomatis jika tanpa gagal). Hasil tiap alat bisa disalin atau disimpan sebagai laporan.",
        "Check a game without changing it: unpack the archive, scan the folder, look for text that is still untranslated, find identical files, and test that the format packs and unpacks again. The check tools only read, so you tick those steps yourself (Unpack is marked Done automatically when it ran without failures). The result of each tool can be copied or saved as a report.",
        "ゲームを変更せずに調べます。アーカイブの展開、フォルダのスキャン、未翻訳テキストの確認、同一ファイルの検索、形式のパック・展開テストを行います。チェック用ツールは読み取り専用なので、手順は自分でチェックします（展開のみ失敗なく終わると自動で完了）。各ツールの結果はコピーやレポート保存ができます。",
        "在不修改游戏的前提下检查：解包档案、扫描文件夹、查找尚未翻译的文本、找出相同文件，并测试格式能否再次打包和解包。检查工具只读取，所以这些步骤需自己勾选（解包在无失败运行后会自动标记完成）。每个工具的结果都可以复制或保存为报告。",
        "게임을 바꾸지 않고 점검합니다. 아카이브 풀기, 폴더 스캔, 번역되지 않은 텍스트 찾기, 같은 파일 찾기, 포맷이 다시 묶이고 풀리는지 테스트를 합니다. 점검 도구는 읽기만 하므로 단계는 직접 체크합니다 (풀기만 실패 없이 실행되면 자동 완료). 각 도구의 결과는 복사하거나 보고서로 저장할 수 있습니다.",
    )
    val clnTitle get() = p("Proyek Pembersihan", "Clean Up Project", "クリーンアッププロジェクト", "清理项目", "정리 프로젝트")
    val clnSub get() = p("Bongkar, pindai, hapus duplikat, rapikan nama, kecilkan aset, kemas", "Unpack, scan, remove duplicates, tidy names, shrink assets, pack", "展開、スキャン、重複削除、名前の整理、アセット縮小、パック", "解包、扫描、删除重复项、整理名称、缩小资源、打包", "풀기, 스캔, 중복 제거, 이름 정리, 에셋 축소, 묶기")
    val clnNewTitle get() = p("Proyek Pembersihan baru", "New Clean Up Project", "新しいクリーンアッププロジェクト", "新建清理项目", "새 정리 프로젝트")
    val clnIntro get() = p(
        "Rapikan game yang sudah dibongkar: hapus file kembar, perbaiki nama file yang bermasalah (misalnya karakter non-Latin), dan kecilkan aset media, lalu kemas lagi. Pencari Duplikat menghapus file dari folder kerja, jadi kerjakan pada salinan dan simpan arsip asli. Bongkar, Konversi, dan Kemas ditandai Selesai otomatis jika berjalan tanpa gagal; langkah lain dicentang sendiri.",
        "Tidy up a game that has been unpacked: delete identical files, fix problematic file names (for example non-Latin characters) and shrink media assets, then pack it again. The Duplicate Finder deletes files from the work folder, so work on a copy and keep the original archive. Unpack, Convert and Pack are marked Done automatically when they ran without failures; the other steps you tick yourself.",
        "展開したゲームを整理します。同一ファイルの削除、問題のあるファイル名（非ラテン文字など）の修正、メディアアセットの縮小を行い、再びパックします。重複ファインダーは作業フォルダからファイルを削除するため、コピーで作業し、元のアーカイブを残してください。展開・変換・パックは失敗なく終わると自動で完了になり、他の手順は自分でチェックします。",
        "整理已解包的游戏：删除相同文件、修复有问题的文件名（例如非拉丁字符）、缩小媒体资源，然后重新打包。重复文件查找器会从工作文件夹中删除文件，所以请在副本上操作并保留原始档案。解包、转换和打包在无失败运行后会自动标记为完成，其他步骤需自己勾选。",
        "풀어 놓은 게임을 정리합니다. 같은 파일 삭제, 문제 있는 파일 이름(예: 비라틴 문자) 수정, 미디어 에셋 축소 후 다시 묶습니다. 중복 찾기는 작업 폴더에서 파일을 삭제하므로 사본으로 작업하고 원본 아카이브를 보관하세요. 풀기·변환·묶기는 실패 없이 실행되면 자동 완료 처리되고, 나머지 단계는 직접 체크합니다.",
    )
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
        "Setiap langkah membuka alat yang sudah ada. Langkah Bongkar, Ekspor, Impor, dan Kemas ditandai Selesai otomatis jika berjalan tanpa gagal; langkah lain dicentang sendiri. Langkah boleh dikerjakan dalam urutan apa pun dan dilewati.",
        "Each step opens an existing tool. Unpack, Export, Import and Pack are marked Done automatically when they ran without failures; the other steps you tick yourself. Steps can be done in any order and skipped.",
        "各手順は既存のツールを開きます。展開・エクスポート・インポート・パックは失敗なく終わると自動で完了になり、他の手順は自分でチェックします。手順は順不同で、省略もできます。",
        "每一步都会打开现有工具。解包、导出、导入和打包在无失败运行后会自动标记为完成，其他步骤需自己勾选。步骤可以按任意顺序进行，也可以跳过。",
        "각 단계는 기존 도구를 엽니다. 풀기·내보내기·가져오기·묶기는 실패 없이 실행되면 자동으로 완료 처리되고, 나머지 단계는 직접 체크합니다. 단계는 순서와 상관없이 진행하거나 건너뛸 수 있습니다.",
    )
    val sourceLabel get() = p("Arsip sumber (opsional)", "Source archive (optional)", "元のアーカイブ（任意）", "源档案（可选）", "원본 아카이브 (선택)")
    val workLabel get() = p("Folder kerja (opsional)", "Work folder (optional)", "作業フォルダ（任意）", "工作文件夹（可选）", "작업 폴더 (선택)")
    val pickNote get() = p(
        "Arsip sumber dan folder kerja dipilih sekali. Langkah Bongkar memakai arsip sumber dan menyimpan hasil ke folder kerja; Ekspor, Impor, dan Kemas memakai folder kerja sebagai folder output. Folder input tiap langkah tetap dipilih di alatnya.",
        "The source archive and the work folder are chosen once. Unpack uses the source archive and saves to the work folder; Export, Import and Pack use the work folder as their output folder. The input folder of each step is still chosen in its tool.",
        "元のアーカイブと作業フォルダは一度だけ選びます。展開は元のアーカイブを使い作業フォルダに保存し、出力・取り込み・パックは作業フォルダを出力先にします。各手順の入力フォルダは各ツールで選びます。",
        "源档案和工作文件夹只需选择一次。解包使用源档案并保存到工作文件夹；导出、导入和打包使用工作文件夹作为输出文件夹。每一步的输入文件夹仍在对应工具中选择。",
        "원본 아카이브와 작업 폴더는 한 번만 선택합니다. 풀기는 원본 아카이브를 쓰고 작업 폴더에 저장하며, 내보내기·가져오기·묶기는 작업 폴더를 출력 폴더로 씁니다. 각 단계의 입력 폴더는 해당 도구에서 선택합니다.",
    )
    val next get() = p("Lanjutkan", "Continue", "続ける", "继续", "계속")
    val open get() = p("Buka", "Open", "開く", "打开", "열기")
    val done get() = p("Selesai", "Done", "完了", "完成", "완료")
    val stNone get() = p("Belum", "Not started", "未着手", "未开始", "시작 전")
    val stOpen get() = p("Dibuka", "Opened", "開いた", "已打开", "열림")
    val stDone get() = p("Selesai", "Done", "完了", "完成", "완료")
    val rename get() = p("Ganti nama", "Rename", "名前を変更", "重命名", "이름 변경")
    val delete get() = p("Hapus proyek", "Delete project", "プロジェクトを削除", "删除项目", "프로젝트 삭제")
    val deleteAsk get() = p("Hapus proyek ini? File game dan hasil kerja tidak ikut terhapus, hanya catatan langkahnya.", "Delete this project? Game files and results are not deleted, only the step notes.", "このプロジェクトを削除しますか？ゲームのファイルや作業結果は削除されず、手順の記録だけが消えます。", "删除此项目？游戏文件和成果不会被删除，只会删除步骤记录。", "이 프로젝트를 삭제할까요? 게임 파일과 결과물은 삭제되지 않고 단계 기록만 삭제됩니다.")
    val yes get() = p("Hapus", "Delete", "削除", "删除", "삭제")

    fun typeTitle(type: Int) = when (type) { 1 -> cmpTitle; 5 -> clnTitle; 4 -> audTitle; 3 -> patTitle; 2 -> extTitle; else -> trTitle }
    fun typeNew(type: Int) = when (type) { 1 -> cmpNewTitle; 5 -> clnNewTitle; 4 -> audNewTitle; 3 -> patNewTitle; 2 -> extNewTitle; else -> newTitle }
    fun typeIntro(type: Int) = when (type) { 1 -> cmpIntro; 5 -> clnIntro; 4 -> audIntro; 3 -> patIntro; 2 -> extIntro; else -> intro }

    fun stepTitle(s: Int) = when (s) {
        18 -> p("Rapikan nama file", "Tidy file names", "ファイル名を整理", "整理文件名", "파일 이름 정리")
        17 -> p("Cek terjemahan", "Check translations", "翻訳をチェック", "检查翻译", "번역 점검")
        15 -> p("Ubah file", "Edit files", "ファイルを編集", "编辑文件", "파일 편집")
        16 -> p("Bandingkan dengan asli", "Compare with the original", "元と比較", "与原版对比", "원본과 비교")
        11 -> p("Lihat aset", "View assets", "アセットを閲覧", "查看资源", "에셋 보기")
        12 -> p("Konversi aset", "Convert assets", "アセットを変換", "转换资源", "에셋 변환")
        13 -> p("Cari duplikat", "Find duplicates", "重複を検索", "查找重复项", "중복 찾기")
        14 -> p("Ganti nama massal", "Batch rename", "一括名前変更", "批量重命名", "일괄 이름 변경")
        7 -> p("Pindai aset", "Scan assets", "アセットをスキャン", "扫描资源", "에셋 스캔")
        8 -> p("Kompres audio", "Compress audio", "音声を圧縮", "压缩音频", "오디오 압축")
        9 -> p("Kompres gambar", "Compress images", "画像を圧縮", "压缩图片", "이미지 압축")
        10 -> p("Kompres video", "Compress video", "動画を圧縮", "压缩视频", "비디오 압축")
        1 -> p("Bongkar arsip", "Unpack archive", "アーカイブを展開", "解包档案", "아카이브 풀기")
        2 -> p("Ekspor teks", "Export text", "テキストを出力", "导出文本", "텍스트 내보내기")
        3 -> p("Terjemahkan", "Translate", "翻訳", "翻译", "번역")
        4 -> p("Impor teks", "Import text", "テキストを取り込み", "导入文本", "텍스트 가져오기")
        5 -> p("Kemas arsip", "Pack archive", "アーカイブをパック", "打包档案", "아카이브 묶기")
        else -> p("Uji format", "Format test", "フォーマットテスト", "格式测试", "포맷 테스트")
    }
    fun stepDesc(s: Int, type: Int = 0) = if (type == 5 && s == 13) p("Cari file yang sama persis dan hapus kembarannya (satu per kelompok selalu tersisa). Kerjakan pada salinan.", "Find identical files and delete the copies (one per group always remains). Work on a copy.", "同一ファイルを探して複製を削除します（各グループに1つは必ず残ります）。コピーで作業してください。", "找出相同文件并删除副本（每组始终保留一个）。请在副本上操作。", "같은 파일을 찾아 사본을 삭제합니다 (그룹마다 하나는 항상 남습니다). 사본으로 작업하세요.")
        else if (type == 5 && s == 12) p("Kecilkan gambar, audio, dan video sekaligus di Konverter Campuran, disimpan ke folder baru.", "Shrink images, audio and video at once in the Mixed Converter, saved to a new folder.", "混合コンバーターで画像・音声・動画をまとめて縮小し、新しいフォルダに保存します。", "在混合转换器中一次性缩小图片、音频和视频，保存到新文件夹。", "혼합 변환기에서 이미지·오디오·비디오를 한꺼번에 줄여 새 폴더에 저장합니다.")
        else if (type == 5 && s == 5) p("Kemas folder yang sudah rapi menjadi arsip baru di engine yang sama.", "Pack the tidied folder into a new archive of the same engine.", "整理したフォルダを同じエンジンの新しいアーカイブにパックします。", "将整理好的文件夹打包成同一引擎的新档案。", "정리한 폴더를 같은 엔진의 새 아카이브로 묶습니다.")
        else if ((type == 4 || type == 5) && s == 1) p("Bongkar arsip game ke folder kerja (lewati jika game sudah berupa folder biasa).", "Unpack the game archive into a work folder (skip if the game is already a plain folder).", "ゲームのアーカイブを作業フォルダに展開します（すでに通常のフォルダなら省略）。", "将游戏档案解包到工作文件夹（如果游戏已是普通文件夹则跳过）。", "게임 아카이브를 작업 폴더로 풉니다 (이미 일반 폴더면 건너뜁니다).")
        else if (type == 3 && s == 1) p("Bongkar arsip asli game ke folder kerja sebagai bahan perbandingan.", "Unpack the original game archive into the work folder as the base for the comparison.", "比較の基準として、ゲームの元のアーカイブを作業フォルダに展開します。", "把游戏原始档案解包到工作文件夹，作为对比的基础。", "비교의 기준으로 게임 원본 아카이브를 작업 폴더로 풉니다.")
        else if (type == 3 && s == 5) p("Kemas folder berisi hanya file yang berubah menjadi arsip patch.", "Pack the folder that holds only the changed files as the patch archive.", "変更したファイルだけのフォルダをパッチのアーカイブとしてパックします。", "把只含改动文件的文件夹打包成补丁档案。", "바뀐 파일만 든 폴더를 패치 아카이브로 묶습니다.")
        else if (type == 1 && s == 1) p("Bongkar arsip game ke folder kerja (lewati jika game sudah berupa folder biasa).", "Unpack the game archive into a work folder (skip if the game is already a plain folder).", "ゲームのアーカイブを作業フォルダに展開します（すでに通常のフォルダなら省略）。", "将游戏档案解包到工作文件夹（如果游戏已是普通文件夹则跳过）。", "게임 아카이브를 작업 폴더로 풉니다 (이미 일반 폴더면 건너뜁니다).")
        else if (type == 1 && s == 5) p("Kemas folder hasil kompres menjadi arsip baru di engine yang sama.", "Pack the compressed folder into a new archive of the same engine.", "圧縮したフォルダを同じエンジンの新しいアーカイブにパックします。", "将压缩后的文件夹打包成同一引擎的新档案。", "압축된 폴더를 같은 엔진의 새 아카이브로 묶습니다.")
        else stepDescBase(s)

    private fun stepDescBase(s: Int) = when (s) {
        18 -> p("Ganti nama file bermasalah (non-Latin, karakter khusus) dan perbarui file game yang memanggilnya di Optimasi Nama.", "Rename problematic files (non-Latin, special characters) and update the game files that refer to them in Name Optimization.", "問題のあるファイル名（非ラテン文字、特殊文字）を名前の最適化で変更し、参照しているゲームファイルも更新します。", "在名称优化中重命名有问题的文件（非拉丁字符、特殊字符），并更新引用它们的游戏文件。", "이름 최적화에서 문제 있는 파일(비라틴 문자, 특수 문자)의 이름을 바꾸고 이를 참조하는 게임 파일도 갱신합니다.")
        17 -> p("Cari teks yang belum diterjemahkan atau masih bahasa asli di Pemeriksaan Terjemahan.", "Find text that is still untranslated or in the original language in the Translation Check.", "翻訳チェックで、未翻訳または原文のままのテキストを探します。", "在翻译检查中查找尚未翻译或仍为原文的文本。", "번역 점검에서 번역되지 않았거나 원문 그대로인 텍스트를 찾습니다.")
        15 -> p("Ubah file di folder kerja memakai Editor Teks (teks biasa, JSON, struktur, ganti massal).", "Change files in the work folder with the Text Editors (plain text, JSON, structured, bulk replace).", "テキストエディタ（プレーンテキスト、JSON、構造、一括置換）で作業フォルダのファイルを変更します。", "用文本编辑器（纯文本、JSON、结构化、批量替换）修改工作文件夹中的文件。", "텍스트 편집기(일반 텍스트, JSON, 구조, 일괄 바꾸기)로 작업 폴더의 파일을 바꿉니다.")
        16 -> p("Bandingkan folder asli dengan folder hasil ubahan; daftar file yang berubah bisa disalin atau disimpan sebagai laporan. Salin file yang berubah itu ke folder baru.", "Compare the original folder with the changed folder; the list of changed files can be copied or saved as a report. Copy those changed files into a new folder.", "元のフォルダと変更後のフォルダを比較します。変更ファイルの一覧はコピーやレポート保存ができます。それらのファイルを新しいフォルダにコピーします。", "对比原始文件夹和修改后的文件夹；改动文件的列表可以复制或保存为报告。把这些改动的文件复制到新文件夹。", "원본 폴더와 바뀐 폴더를 비교합니다. 바뀐 파일 목록은 복사하거나 보고서로 저장할 수 있습니다. 그 파일들을 새 폴더에 복사하세요.")
        11 -> p("Lihat gambar, audio, dan video hasil ekstrak di Media Player (tambahkan folder kerja ke Folder).", "View the extracted images, audio and video in the Media Player (add the work folder under Folders).", "展開した画像・音声・動画をメディアプレーヤーで閲覧します（作業フォルダをフォルダに追加）。", "在媒体播放器中查看提取的图片、音频和视频（把工作文件夹添加到“文件夹”）。", "추출한 이미지·오디오·비디오를 미디어 플레이어에서 봅니다 (작업 폴더를 폴더에 추가).")
        12 -> p("Ubah aset ke format umum di Konverter Campuran (misalnya gambar ke PNG, audio ke MP3), disimpan ke folder baru.", "Convert assets to common formats in the Mixed Converter (for example images to PNG, audio to MP3), saved to a new folder.", "混合コンバーターでアセットを一般的な形式（画像はPNG、音声はMP3など）に変換し、新しいフォルダに保存します。", "在混合转换器中把资源转换为通用格式（例如图片转 PNG、音频转 MP3），保存到新文件夹。", "혼합 변환기에서 에셋을 일반 형식(예: 이미지는 PNG, 오디오는 MP3)으로 변환해 새 폴더에 저장합니다.")
        13 -> p("Cari file yang sama persis di Pencari Duplikat agar tidak menyimpan dua kali.", "Find identical files in the Duplicate Finder so nothing is kept twice.", "重複ファインダーで同一ファイルを探し、二重に保存しないようにします。", "在重复文件查找器中找出完全相同的文件，避免重复保存。", "중복 찾기에서 완전히 같은 파일을 찾아 두 번 저장하지 않게 합니다.")
        14 -> p("Beri nama yang rapi dan berurutan di Ganti Nama Massal (opsional).", "Give the files tidy, numbered names in Batch Rename (optional).", "一括名前変更で、ファイルに整った連番の名前を付けます（任意）。", "在批量重命名中给文件起整洁的编号名称（可选）。", "일괄 이름 변경에서 파일에 깔끔한 번호 이름을 붙입니다 (선택).")
        7 -> p("Periksa isi folder game (Pemeriksaan Folder) untuk melihat jenis dan ukuran aset sebelum dikompres.", "Check the game folder (Folder Check) to see the kinds and sizes of assets before compressing.", "圧縮前に、ゲームフォルダ（フォルダチェック）でアセットの種類とサイズを確認します。", "压缩前检查游戏文件夹（文件夹检查），查看资源的类型和大小。", "압축 전에 게임 폴더(폴더 검사)에서 에셋의 종류와 크기를 확인합니다.")
        8 -> p("Kompres audio di Konverter Audio (format dan bitrate lebih kecil). Pakai mode tempat asal agar nama file tetap.", "Compress audio in the Audio Converter (smaller format and bitrate). Use the in-place mode so file names stay the same.", "音声コンバーターで音声を圧縮します（小さい形式とビットレート）。ファイル名を保つため、元の場所モードを使います。", "在音频转换器中压缩音频（更小的格式和比特率）。使用原位置模式以保持文件名不变。", "오디오 변환기에서 오디오를 압축합니다 (더 작은 형식과 비트레이트). 파일 이름을 유지하려면 제자리 모드를 쓰세요.")
        9 -> p("Kompres gambar di Konverter Gambar (kualitas atau ukuran lebih kecil).", "Compress images in the Image Converter (lower quality or size).", "画像コンバーターで画像を圧縮します（品質やサイズを下げる）。", "在图片转换器中压缩图片（降低质量或尺寸）。", "이미지 변환기에서 이미지를 압축합니다 (품질이나 크기를 낮춤).")
        10 -> p("Kompres video di Konverter Video (CRF lebih tinggi atau resolusi lebih kecil).", "Compress video in the Video Converter (higher CRF or lower resolution).", "動画コンバーターで動画を圧縮します（CRFを上げる、解像度を下げる）。", "在视频转换器中压缩视频（更高的 CRF 或更低的分辨率）。", "비디오 변환기에서 비디오를 압축합니다 (더 높은 CRF 또는 더 낮은 해상도).")
        1 -> p("Bongkar arsip game ke folder kerja. Engine dikenali otomatis.", "Unpack the game archive into a work folder. The engine is detected automatically.", "ゲームのアーカイブを作業フォルダに展開します。エンジンは自動検出されます。", "将游戏档案解包到工作文件夹。引擎会自动识别。", "게임 아카이브를 작업 폴더로 풉니다. 엔진은 자동으로 감지됩니다.")
        2 -> p("Kumpulkan dialog dan pilihan menjadi satu file teks (hanya untuk engine yang didukung).", "Collect dialogue and choices into one text file (only for supported engines).", "セリフと選択肢を1つのテキストファイルにまとめます（対応エンジンのみ）。", "将对话和选项收集到一个文本文件中（仅限受支持的引擎）。", "대사와 선택지를 하나의 텍스트 파일로 모읍니다 (지원되는 엔진만).")
        3 -> p("Terjemahkan teks di Area Terjemahan, dengan draf, glosarium, dan terjemahan online.", "Translate the text in the Translation Area, with drafts, a glossary and online translation.", "翻訳エリアでテキストを翻訳します（下書き、用語集、オンライン翻訳）。", "在翻译区翻译文本，可用草稿、术语表和在线翻译。", "번역 영역에서 초안, 용어집, 온라인 번역으로 텍스트를 번역합니다.")
        4 -> p("Masukkan hasil terjemahan kembali ke file skrip.", "Put the translation back into the script files.", "翻訳結果をスクリプトファイルに書き戻します。", "将译文放回脚本文件。", "번역 결과를 스크립트 파일에 다시 넣습니다.")
        5 -> p("Kemas folder kerja menjadi arsip baru.", "Pack the work folder into a new archive.", "作業フォルダを新しいアーカイブにパックします。", "将工作文件夹打包成新档案。", "작업 폴더를 새 아카이브로 묶습니다.")
        else -> p("Periksa bahwa format bisa dikemas dan dibongkar lagi dengan benar (opsional).", "Check that the format packs and unpacks again correctly (optional).", "フォーマットが正しくパック・展開できるか確認します（任意）。", "检查该格式能否正确打包并再次解包（可选）。", "포맷이 올바르게 묶이고 다시 풀리는지 확인합니다 (선택).")
    }
}

fun projectStr(lang: Lang) = ProjectStr(lang)
