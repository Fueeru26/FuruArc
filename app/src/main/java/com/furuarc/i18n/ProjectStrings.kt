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
        "Buat patch kecil berisi hanya file yang kamu ubah. Bongkar arsip asli dua kali: ke folder kerja proyek (untuk diubah) dan ke folder lain yang dibiarkan asli (sebagai pembanding). Ubah file di folder kerja, lalu bandingkan kedua folder: file yang berubah dan file baru disalin ke folder baru, dan folder itu dikemas menjadi arsip patch. Bongkar, Bandingkan (setelah menyalin), dan Kemas ditandai Selesai otomatis jika berhasil; langkah lain dicentang sendiri. Simpan arsip asli.",
        "Make a small patch that holds only the files you changed. Unpack the original archive twice: into the project's work folder (to be edited) and into another folder that is left untouched (for comparison). Edit the files in the work folder, then compare the two folders: the changed and new files are copied into a new folder, and that folder is packed as the patch archive. Unpack, Compare (after copying) and Pack are marked Done automatically when they succeed; the other steps you tick yourself. Keep the original archive.",
        "変更したファイルだけを含む小さなパッチを作ります。元のアーカイブを2回展開します。1つはプロジェクトの作業フォルダ（編集用）、もう1つは手を付けない別のフォルダ（比較用）です。作業フォルダのファイルを編集し、2つのフォルダを比較して、変更されたファイルと新しいファイルを新しいフォルダにコピーし、そのフォルダをパッチのアーカイブとしてパックします。展開、比較（コピー後）、パックは成功すると自動で完了になり、他の手順は自分でチェックします。元のアーカイブは残しておきます。",
        "制作只包含你修改过的文件的小补丁。把原始档案解包两次：一次解包到项目的工作文件夹（用于修改），另一次解包到不动的文件夹（用于对比）。修改工作文件夹中的文件，然后对比两个文件夹：有变化的文件和新文件会复制到新文件夹，再把该文件夹打包成补丁档案。解包、对比（复制后）和打包在成功后会自动标记为完成，其他步骤需自己勾选。请保留原始档案。",
        "바뀐 파일만 담은 작은 패치를 만듭니다. 원본 아카이브를 두 번 풉니다. 하나는 프로젝트 작업 폴더(편집용), 다른 하나는 건드리지 않는 폴더(비교용)입니다. 작업 폴더의 파일을 편집한 뒤 두 폴더를 비교하면 바뀐 파일과 새 파일이 새 폴더로 복사되고, 그 폴더를 패치 아카이브로 묶습니다. 풀기, 비교(복사 후), 묶기는 성공하면 자동으로 완료 처리되고, 나머지 단계는 직접 체크합니다. 원본 아카이브는 보관하세요.",
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
    val clnSub get() = p("Bongkar, cadangkan, hapus duplikat, rapikan nama, kecilkan aset, kemas", "Unpack, back up, remove duplicates, tidy names, shrink assets, pack", "展開、バックアップ、重複削除、名前の整理、アセット縮小、パック", "解包、备份、删除重复项、整理名称、缩小资源、打包", "풀기, 백업, 중복 제거, 이름 정리, 에셋 축소, 묶기")
    val clnNewTitle get() = p("Proyek Pembersihan baru", "New Clean Up Project", "新しいクリーンアッププロジェクト", "新建清理项目", "새 정리 프로젝트")
    val clnIntro get() = p(
        "Rapikan game yang sudah dibongkar: cadangkan folder dulu, lalu hapus file kembar, perbaiki nama file yang bermasalah (misalnya karakter non-Latin), dan kecilkan aset media, lalu kemas lagi. Pencari Duplikat menghapus file dari folder kerja, jadi jangan lewati langkah Cadangkan dan simpan arsip asli. Bongkar, Cadangkan, Konversi, dan Kemas ditandai Selesai otomatis jika berjalan tanpa gagal; langkah lain dicentang sendiri.",
        "Tidy up a game that has been unpacked: back up the folder first, then delete identical files, fix problematic file names (for example non-Latin characters) and shrink media assets, then pack it again. The Duplicate Finder deletes files from the work folder, so do not skip the Back up step and keep the original archive. Unpack, Back up, Convert and Pack are marked Done automatically when they ran without failures; the other steps you tick yourself.",
        "展開したゲームを整理します。まずフォルダをバックアップし、同一ファイルの削除、問題のあるファイル名（非ラテン文字など）の修正、メディアアセットの縮小を行い、再びパックします。重複ファインダーは作業フォルダからファイルを削除するため、バックアップの手順を省略せず、元のアーカイブも残してください。展開・バックアップ・変換・パックは失敗なく終わると自動で完了になり、他の手順は自分でチェックします。",
        "整理已解包的游戏：先备份文件夹，再删除相同文件、修复有问题的文件名（例如非拉丁字符）、缩小媒体资源，然后重新打包。重复文件查找器会从工作文件夹中删除文件，所以不要跳过备份步骤，并保留原始档案。解包、备份、转换和打包在无失败运行后会自动标记为完成，其他步骤需自己勾选。",
        "풀어 놓은 게임을 정리합니다. 먼저 폴더를 백업하고, 같은 파일 삭제, 문제 있는 파일 이름(예: 비라틴 문자) 수정, 미디어 에셋 축소 후 다시 묶습니다. 중복 찾기는 작업 폴더에서 파일을 삭제하므로 백업 단계를 건너뛰지 말고 원본 아카이브를 보관하세요. 풀기·백업·변환·묶기는 실패 없이 실행되면 자동 완료 처리되고, 나머지 단계는 직접 체크합니다.",
    )
    val locTitle get() = p("Proyek Lokalisasi Aset", "Localize Assets Project", "アセットのローカライズプロジェクト", "资源本地化项目", "에셋 현지화 프로젝트")
    val locSub get() = p("Bongkar, cari gambar bertulisan, edit, konversi, bandingkan, kemas", "Unpack, find images with text, edit, convert, compare, pack", "展開、文字入り画像の検索、編集、変換、比較、パック", "解包、查找带文字的图片、编辑、转换、对比、打包", "풀기, 글자가 있는 이미지 찾기, 편집, 변환, 비교, 묶기")
    val locNewTitle get() = p("Proyek Lokalisasi Aset baru", "New Localize Assets Project", "新しいアセットのローカライズプロジェクト", "新建资源本地化项目", "새 에셋 현지화 프로젝트")
    val locIntro get() = p(
        "Terjemahkan teks yang ada di dalam gambar game (tombol, judul, menu). Bongkar arsip, lihat gambar untuk menemukan yang bertulisan, ubah di Editor Gambar (tutup teks lama dengan Isi dari warna sekitar atau Pipet, lalu tulis teks baru), samakan formatnya dengan game, bandingkan dengan folder asli, lalu kemas. Bongkar, Konversi, Bandingkan, dan Kemas ditandai Selesai otomatis jika berjalan tanpa gagal; langkah lain dicentang sendiri. Ukuran dan format gambar sebaiknya sama dengan aslinya. Simpan arsip asli.",
        "Translate the text that is inside game images (buttons, titles, menus). Unpack the archive, view the images to find the ones with text, edit them in the Image Editor (cover the old text with Fill from the surrounding colors or the Picker, then write the new text), match the game's format, compare with the original folder, then pack. Unpack, Convert, Compare and Pack are marked Done automatically when they ran without failures; the other steps you tick yourself. Keep each image's size and format the same as the original. Keep the original archive.",
        "ゲーム画像の中の文字（ボタン、タイトル、メニュー）を翻訳します。アーカイブを展開し、画像を見て文字入りのものを探し、画像エディタで編集（周囲の色で埋めるやスポイトで元の文字を隠し、新しい文字を書く）、ゲームの形式に合わせて変換し、元のフォルダと比較してからパックします。展開・変換・比較・パックは失敗なく終わると自動で完了になり、他の手順は自分でチェックします。画像のサイズと形式は元と同じにしてください。元のアーカイブは残しておきます。",
        "翻译游戏图片中的文字（按钮、标题、菜单）。先解包，查看图片找出带文字的，在图片编辑器中编辑（用“用周围的颜色填充”或取色器盖住原文字，再写新文字），转换成与游戏相同的格式，与原文件夹对比后打包。解包、转换、对比和打包在无失败运行后会自动标记为完成，其他步骤需自己勾选。图片的尺寸和格式应与原图一致。请保留原始档案。",
        "게임 이미지 속 글자(버튼, 제목, 메뉴)를 번역합니다. 아카이브를 풀고, 이미지를 보며 글자가 있는 것을 찾고, 이미지 편집기에서 편집(주변 색으로 채우기나 스포이트로 원래 글자를 덮고 새 글자를 씀)한 뒤, 게임과 같은 형식으로 변환하고, 원본 폴더와 비교한 다음 묶습니다. 풀기·변환·비교·묶기는 실패 없이 실행되면 자동 완료 처리되고, 나머지 단계는 직접 체크합니다. 이미지의 크기와 형식은 원본과 같게 유지하세요. 원본 아카이브는 보관하세요.",
    )
    val newBtn get() = p("Proyek baru", "New project", "新規プロジェクト", "新建项目", "새 프로젝트")
    val newTitle get() = p("Proyek Terjemahan baru", "New Translation Project", "新しい翻訳プロジェクト", "新建翻译项目", "새 번역 프로젝트")
    val nameLabel get() = p("Nama proyek (misalnya nama game)", "Project name (for example the game name)", "プロジェクト名（例：ゲーム名）", "项目名称（例如游戏名称）", "프로젝트 이름 (예: 게임 이름)")
    val create get() = p("Buat", "Create", "作成", "创建", "만들기")
    val cancel get() = p("Batal", "Cancel", "キャンセル", "取消", "취소")
    val emptyTitle get() = p("Belum ada proyek", "No projects yet", "プロジェクトはまだありません", "还没有项目", "프로젝트가 없습니다")
    val emptySub get() = p("Buat proyek pertama untuk mengikuti langkah-langkahnya satu per satu.", "Create your first project to follow its steps one by one.", "最初のプロジェクトを作成して、手順を一つずつ進めましょう。", "创建第一个项目，逐步完成各个步骤。", "첫 프로젝트를 만들어 단계를 하나씩 따라가 보세요.")
    val createBtn get() = p("Buat proyek", "Create project", "プロジェクトを作成", "创建项目", "프로젝트 만들기")
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
        "Arsip sumber dan folder kerja dipilih sekali. Langkah Bongkar memakai arsip sumber dan menyimpan hasil ke folder kerja; Ekspor, Impor, dan Kemas memakai folder kerja sebagai folder output. Untuk Kemas, folder input terisi otomatis dari hasil Bongkar (atau dari folder salinan di Proyek Buat Patch); pada proyek lain pilih sendiri di alatnya.",
        "The source archive and the work folder are chosen once. Unpack uses the source archive and saves to the work folder; Export, Import and Pack use the work folder as their output folder. For Pack the input folder is filled in from the unpacked result (or from the copy folder in the Make Patch Project); in other projects pick it in the tool.",
        "ソースのアーカイブと作業フォルダは一度だけ選びます。展開はソースのアーカイブを使い、結果を作業フォルダに保存します。エクスポート、インポート、パックは作業フォルダを出力フォルダとして使います。パックの入力フォルダは展開結果（パッチ作成プロジェクトではコピー先フォルダ）から自動で入ります。他のプロジェクトではツールで選んでください。",
        "源档案和工作文件夹只需选择一次。解包使用源档案并保存到工作文件夹；导出、导入和打包把工作文件夹用作输出文件夹。打包的输入文件夹会根据解包结果自动填入（制作补丁项目中则是复制目标文件夹）；其他项目请在工具中自行选择。",
        "원본 아카이브와 작업 폴더는 한 번만 고릅니다. 풀기는 원본 아카이브를 사용해 작업 폴더에 저장하고, 내보내기·가져오기·묶기는 작업 폴더를 출력 폴더로 씁니다. 묶기의 입력 폴더는 풀기 결과(패치 만들기 프로젝트에서는 복사 폴더)에서 자동으로 채워지며, 다른 프로젝트에서는 도구에서 직접 고르세요.",
    )
    // ---- game mechanism (selector before the steps) ----
    val mechTitle get() = p("Mekanisme game", "Game mechanism", "ゲームの仕組み", "游戏机制", "게임 방식")
    val mechMv get() = p("RPGM MV / MZ", "RPGM MV / MZ", "RPGM MV / MZ", "RPGM MV / MZ", "RPGM MV / MZ")
    val mechXp get() = p("RPGM XP / VX / VX Ace", "RPGM XP / VX / VX Ace", "RPGM XP / VX / VX Ace", "RPGM XP / VX / VX Ace", "RPGM XP / VX / VX Ace")
    val mechVn get() = p("Visual Novel / umum", "Visual Novel / general", "ビジュアルノベル / 一般", "视觉小说 / 通用", "비주얼 노벨 / 일반")
    fun mechName(m: Int) = when (m) { 0 -> mechMv; 1 -> mechXp; else -> mechVn }
    val mechNote get() = p(
        "Pilih mekanisme game dulu; daftar langkah menyesuaikan. RPGM MV / MZ tidak punya arsip: teks diekspor langsung dari data JSON dan aset didekripsi / dienkripsi per file. Status langkah yang sama tetap tersimpan saat mekanisme diganti. Mengganti dari atau ke MV / MZ mengosongkan pilihan sumber karena jenisnya berbeda (folder, bukan arsip).",
        "Pick the game mechanism first; the list of steps follows it. RPGM MV / MZ has no archive: text is exported straight from the JSON data and assets are decrypted / encrypted per file. The state of steps that exist in both lists is kept when the mechanism changes. Switching from or to MV / MZ clears the chosen source because it is a different kind (a folder, not an archive).",
        "最初にゲームの仕組みを選ぶと、手順の一覧がそれに合わせて変わります。RPGM MV / MZ にはアーカイブがなく、テキストは JSON データから直接出力し、アセットはファイルごとに復号・暗号化します。両方の一覧にある手順の状態は仕組みを切り替えても保持されます。MV / MZ との切り替え時は、種類（フォルダとアーカイブ）が異なるためソースの選択がクリアされます。",
        "先选择游戏机制，步骤列表会随之变化。RPGM MV / MZ 没有档案：文本直接从 JSON 数据导出，资源逐文件解密 / 加密。两种列表中都有的步骤，切换机制后状态会保留。切换到或离开 MV / MZ 时会清除已选的源，因为类型不同（文件夹而非档案）。",
        "먼저 게임 방식을 고르면 단계 목록이 그에 맞게 바뀝니다. RPGM MV / MZ에는 아카이브가 없어 텍스트는 JSON 데이터에서 바로 내보내고 에셋은 파일별로 복호화 / 암호화합니다. 두 목록에 모두 있는 단계의 상태는 방식을 바꿔도 유지됩니다. MV / MZ로 또는 MV / MZ에서 바꾸면 종류(폴더, 아카이브 아님)가 달라 선택한 원본이 지워집니다.",
    )
    val mvSourceLabel get() = p("Folder game (asli, tidak diubah)", "Game folder (original, untouched)", "ゲームフォルダ（元のまま）", "游戏文件夹（原版，不修改）", "게임 폴더 (원본, 변경 안 함)")
    val mvWorkLabel get() = p("Folder hasil", "Result folder", "結果フォルダ", "结果文件夹", "결과 폴더")
    val mvPickNote get() = p(
        "Folder game adalah folder game asli (yang berisi data/js) dan tidak pernah diubah. Folder hasil menerima file yang dihasilkan: teks ekspor dan JSON hasil impor (Terjemahan), atau aset yang dienkripsi ulang. Dekripsi: pilih folder tujuan sendiri di alatnya (di Proyek Ekstrak Aset folder hasil terisi otomatis). Enkripsi memakai System.json game untuk kuncinya (diminta sekali di alatnya), jadi kuncinya sama dengan game asli.",
        "The game folder is the original game (the one with data/js) and is never changed. The result folder receives the files that are produced: the exported text and the imported JSON files (Translation), or the re-encrypted assets. Decrypt: pick its destination folder in the tool (in the Extract Assets project the result folder is filled in automatically). Encrypt uses the game's System.json for the key (asked once in the tool), so the key stays the same as in the original game.",
        "ゲームフォルダは元のゲーム（data/js を含むフォルダ）で、変更されません。結果フォルダには、出力されたテキストや取り込み後の JSON（翻訳）、再暗号化されたアセットが保存されます。復号: ツールで保存先を選びます（アセット抽出プロジェクトでは結果フォルダが自動入力されます）。暗号化は鍵のためにゲームの System.json を使う（ツールで一度だけ指定）ので、元のゲームと同じ鍵になります。",
        "游戏文件夹是原版游戏（含 data/js 的文件夹），不会被修改。结果文件夹接收生成的文件：导出的文本和导入后的 JSON（翻译），或重新加密的资源。解密：在工具中自行选择目标文件夹（提取资源项目中结果文件夹会自动填入）。加密使用游戏的 System.json 获取密钥（在工具中只需指定一次），因此密钥与原版游戏相同。",
        "게임 폴더는 원본 게임(data/js가 있는 폴더)이며 변경되지 않습니다. 결과 폴더에는 만들어진 파일(내보낸 텍스트와 가져온 JSON(번역) 또는 다시 암호화한 에셋)이 저장됩니다. 복호화: 도구에서 저장할 폴더를 직접 고릅니다(에셋 추출 프로젝트에서는 결과 폴더가 자동 입력). 암호화는 키를 위해 게임의 System.json을 사용(도구에서 한 번만 지정)하므로 원본 게임과 같은 키가 됩니다.",
    )
    val next get() = p("Lanjutkan", "Continue", "続ける", "继续", "계속")
    val open get() = p("Buka", "Open", "開く", "打开", "열기")
    val done get() = p("Selesai", "Done", "完了", "完成", "완료")
    val stNone get() = p("Belum", "Not started", "未着手", "未开始", "시작 전")
    val stOpen get() = p("Dibuka", "Opened", "開いた", "已打开", "열림")
    val stDone get() = p("Selesai", "Done", "完了", "完成", "완료")
    val duplicate get() = p("Duplikat proyek", "Duplicate project", "プロジェクトを複製", "复制项目", "프로젝트 복제")
    val copySuffix get() = p(" (salinan)", " (copy)", "（コピー）", "（副本）", " (복사본)")
    val reportTitle get() = p("Laporan proyek", "Project report", "プロジェクトレポート", "项目报告", "프로젝트 보고서")
    fun reportText(pr: com.furuarc.core.Project): String {
        val sb = StringBuilder()
        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US)
        sb.append(typeTitle(pr.type)).append(": ").append(pr.name).append('\n')
        sb.append(p("Dibuat: ", "Created: ", "作成: ", "创建：", "생성: ")).append(date.format(java.util.Date(pr.created))).append('\n')
        sb.append(progress(pr.doneCount(), pr.steps.size)).append('\n')
        sb.append(mechTitle).append(": ").append(mechName(pr.mech)).append('\n')
        sb.append(if (pr.mech == 0) mvSourceLabel else sourceLabel).append(": ").append(pr.sourceName.ifEmpty { "-" }).append('\n')
        sb.append(if (pr.mech == 0) mvWorkLabel else workLabel).append(": ").append(pr.workName.ifEmpty { "-" }).append("\n\n")
        pr.steps.forEachIndexed { i, st ->
            val state = when (pr.state(st)) { 2 -> stDone; 1 -> stOpen; else -> stNone }
            sb.append(i + 1).append(". ").append(stepTitle(st)).append(" - ").append(state).append('\n')
        }
        if (pr.note.isNotBlank()) sb.append('\n').append(noteTitle).append(":\n").append(pr.note.trim()).append('\n')
        return sb.toString()
    }
    val rename get() = p("Ganti nama", "Rename", "名前を変更", "重命名", "이름 변경")
    val delete get() = p("Hapus proyek", "Delete project", "プロジェクトを削除", "删除项目", "프로젝트 삭제")
    val deleteAsk get() = p("Hapus proyek ini? File game dan hasil kerja tidak ikut terhapus, hanya catatan langkahnya.", "Delete this project? Game files and results are not deleted, only the step notes.", "このプロジェクトを削除しますか？ゲームのファイルや作業結果は削除されず、手順の記録だけが消えます。", "删除此项目？游戏文件和成果不会被删除，只会删除步骤记录。", "이 프로젝트를 삭제할까요? 게임 파일과 결과물은 삭제되지 않고 단계 기록만 삭제됩니다.")
    val yes get() = p("Hapus", "Delete", "削除", "删除", "삭제")

    fun typeTitle(type: Int) = when (type) { 1 -> cmpTitle; 6 -> locTitle; 5 -> clnTitle; 4 -> audTitle; 3 -> patTitle; 2 -> extTitle; else -> trTitle }
    fun typeNew(type: Int) = when (type) { 1 -> cmpNewTitle; 6 -> locNewTitle; 5 -> clnNewTitle; 4 -> audNewTitle; 3 -> patNewTitle; 2 -> extNewTitle; else -> newTitle }
    fun typeIntro(type: Int) = when (type) { 1 -> cmpIntro; 6 -> locIntro; 5 -> clnIntro; 4 -> audIntro; 3 -> patIntro; 2 -> extIntro; else -> intro }

    fun stepTitle(s: Int) = when (s) {
        22 -> p("Dekripsi aset", "Decrypt assets", "アセットを復号", "解密资源", "에셋 복호화")
        23 -> p("Enkripsi ulang aset", "Re-encrypt assets", "アセットを再暗号化", "重新加密资源", "에셋 다시 암호화")
        24 -> p("Cek / edit plugin (opsional)", "Check / edit plugins (optional)", "プラグインの確認・編集（任意）", "检查 / 编辑插件（可选）", "플러그인 확인 / 편집 (선택)")
        21 -> p("Cadangkan folder", "Back up the folder", "フォルダをバックアップ", "备份文件夹", "폴더 백업")
        19 -> p("Edit gambar", "Edit images", "画像を編集", "编辑图片", "이미지 편집")
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
    fun stepDesc(s: Int, type: Int = 0, mech: Int = 2): String = (if (mech == 0) mvDesc(s, type) else null) ?: stepDesc0(s, type)

    /** Step texts of the RPG Maker MV / MZ lists (null = same text as the other mechanisms). */
    private fun mvDesc(s: Int, type: Int): String? = when {
        s == 22 && type == 2 -> p("Dekripsi aset game ke folder hasil (terisi otomatis): pilih folder game, kuncinya dibaca dari System.json atau ditebak dari header gambar.", "Decrypt the game assets into the result folder (filled in automatically): pick the game folder, the key is read from System.json or guessed from an image header.", "ゲームのアセットを結果フォルダ（自動入力）に復号します。ゲームフォルダを選ぶと、鍵は System.json から読み取るか画像ヘッダーから推測します。", "把游戏资源解密到结果文件夹（自动填入）：选择游戏文件夹，密钥从 System.json 读取或根据图片头推测。", "게임 에셋을 결과 폴더(자동 입력)로 복호화합니다. 게임 폴더를 고르면 키는 System.json에서 읽거나 이미지 헤더로 추정합니다.")
        s == 22 -> p("Dekripsi aset yang akan dikerjakan: folder game terisi otomatis, pilih sendiri folder tujuan hasil dekripsi di alatnya. Kuncinya dibaca dari System.json atau ditebak dari header gambar.", "Decrypt the assets you will work on: the game folder is filled in, pick the destination folder for the decrypted files in the tool. The key is read from System.json or guessed from an image header.", "作業するアセットを復号します。ゲームフォルダは自動入力され、復号先フォルダはツールで選びます。鍵は System.json から読み取るか画像ヘッダーから推測します。", "解密要处理的资源：游戏文件夹已自动填入，请在工具中选择解密后文件的目标文件夹。密钥从 System.json 读取或根据图片头推测。", "작업할 에셋을 복호화합니다. 게임 폴더는 자동 입력되고 복호화 결과 폴더는 도구에서 고릅니다. 키는 System.json에서 읽거나 이미지 헤더로 추정합니다.")
        s == 23 -> p("Enkripsi ulang aset yang sudah diedit / dikompres: pilih foldernya di alat, hasil masuk ke folder hasil (terisi otomatis). Alat meminta System.json game sekali untuk kuncinya, jadi kuncinya sama dengan game asli. System.json di hasil ditulis ulang; Bandingkan Folder mengabaikannya.", "Re-encrypt the edited / compressed assets: pick their folder in the tool, the result goes into the result folder (filled in automatically). The tool asks once for the game's System.json for the key, so the key is the same as the original. The System.json in the result is rewritten; Compare Folders ignores it.", "編集・圧縮したアセットを再暗号化します。ツールでそのフォルダを選ぶと、結果は結果フォルダ（自動入力）に保存されます。鍵のためにゲームの System.json を一度だけ指定するので、元と同じ鍵になります。結果の System.json は書き換えられ、フォルダ比較では無視されます。", "重新加密已编辑 / 压缩的资源：在工具中选择其文件夹，结果保存到结果文件夹（自动填入）。工具会要求一次游戏的 System.json 以获取密钥，因此密钥与原版相同。结果中的 System.json 会被重写，比较文件夹时会忽略它。", "편집·압축한 에셋을 다시 암호화합니다. 도구에서 해당 폴더를 고르면 결과가 결과 폴더(자동 입력)에 저장됩니다. 키를 위해 게임의 System.json을 한 번 지정하므로 원본과 같은 키가 됩니다. 결과의 System.json은 다시 쓰이며 폴더 비교에서는 무시됩니다.")
        s == 24 -> p("Buka Pengelola Plugin untuk memindai, mengedit, dan memeriksa plugin (misalnya teks di dalam plugin). File plugin yang diedit disalin ke folder hasil agar ikut masuk patch. Lewati jika tidak ada.", "Open the Plugin Manager to scan, edit and check plugins (for example text inside plugins). Copy the edited plugin files into the result folder so they join the patch. Skip it if there is nothing to do.", "プラグインマネージャーでプラグインをスキャン・編集・確認します（例：プラグイン内のテキスト）。編集したプラグインは結果フォルダにコピーするとパッチに含まれます。不要なら省略してください。", "打开插件管理器来扫描、编辑和检查插件（例如插件内的文本）。编辑后的插件文件请复制到结果文件夹以便纳入补丁。没有需要时可跳过。", "플러그인 관리자를 열어 플러그인을 스캔·편집·확인합니다(예: 플러그인 안의 텍스트). 편집한 플러그인 파일은 결과 폴더에 복사해야 패치에 포함됩니다. 필요 없으면 건너뛰세요.")
        s == 2 -> p("Ekspor teks dari data JSON game langsung (tanpa membongkar apa pun): folder game dan folder hasil terisi otomatis.", "Export the text straight from the game's JSON data (nothing to unpack): the game folder and the result folder are filled in.", "ゲームの JSON データから直接テキストを出力します（展開は不要）。ゲームフォルダと結果フォルダは自動入力されます。", "直接从游戏的 JSON 数据导出文本（无需解包）：游戏文件夹和结果文件夹已自动填入。", "게임의 JSON 데이터에서 바로 텍스트를 내보냅니다(풀 것이 없음). 게임 폴더와 결과 폴더가 자동 입력됩니다.")
        s == 4 -> p("Impor teks terjemahan ke data JSON: folder game dan folder hasil terisi otomatis, hasilnya hanya file JSON yang berubah (folder game tidak diubah).", "Import the translated text into the JSON data: the game folder and result folder are filled in; the result is only the changed JSON files (the game folder is not changed).", "翻訳したテキストを JSON データに取り込みます。ゲームフォルダと結果フォルダは自動入力され、結果は変更された JSON だけです（ゲームフォルダは変更されません）。", "把翻译文本导入 JSON 数据：游戏文件夹和结果文件夹已自动填入，结果只有改动过的 JSON 文件（不修改游戏文件夹）。", "번역한 텍스트를 JSON 데이터로 가져옵니다. 게임 폴더와 결과 폴더가 자동 입력되며 결과는 변경된 JSON 파일뿐입니다(게임 폴더는 변경 안 됨).")
        s == 16 && (type == 0 || type == 1 || type == 3 || type == 6) -> p("Bandingkan Folder: folder game (asli) dan folder hasil terisi otomatis, System.json dan file ekspor diabaikan. Pilih folder tujuan lalu salin file yang berubah dan baru: itulah folder patch.", "Compare Folders: the game folder (original) and the result folder are filled in, System.json and the export file are ignored. Pick a destination folder and copy the changed and new files: that is the patch folder.", "フォルダ比較：ゲームフォルダ（元）と結果フォルダは自動入力され、System.json と出力ファイルは無視されます。保存先を選んで変更・新規ファイルをコピーすると、それがパッチフォルダになります。", "比较文件夹：游戏文件夹（原版）和结果文件夹已自动填入，会忽略 System.json 和导出文件。选择目标文件夹并复制已更改和新增的文件，即为补丁文件夹。", "폴더 비교: 게임 폴더(원본)와 결과 폴더가 자동 입력되고 System.json과 내보낸 파일은 무시됩니다. 대상 폴더를 골라 변경·추가된 파일을 복사하면 그것이 패치 폴더입니다.")
        s == 21 -> p("Cadangkan folder game (salin semua file ke folder lain) sebelum dirapikan.", "Back up the game folder (copy every file to another folder) before tidying it.", "整理する前にゲームフォルダをバックアップします（全ファイルを別のフォルダにコピー）。", "整理前备份游戏文件夹（把所有文件复制到另一个文件夹）。", "정리하기 전에 게임 폴더를 백업합니다(모든 파일을 다른 폴더로 복사).")
        else -> null
    }

    private fun stepDesc0(s: Int, type: Int = 0) = if (type == 6 && s == 11) p("Lihat gambar di Media Player untuk menemukan yang berisi teks (tombol, judul, menu). Tambahkan folder kerja ke Folder.", "View the images in the Media Player to find the ones with text (buttons, titles, menus). Add the work folder under Folders.", "メディアプレーヤーで画像を見て、文字入りのもの（ボタン、タイトル、メニュー）を探します。作業フォルダをフォルダに追加してください。", "在媒体播放器中查看图片，找出带文字的（按钮、标题、菜单）。请把工作文件夹添加到“文件夹”。", "미디어 플레이어에서 이미지를 보며 글자가 있는 것(버튼, 제목, 메뉴)을 찾습니다. 작업 폴더를 폴더에 추가하세요.")
        else if (type == 6 && s == 12) p("Ubah gambar hasil sunting ke format yang dipakai game (misalnya PNG) di Konverter Campuran, disimpan ke folder baru. Lewati jika formatnya sudah sama.", "Convert the edited images to the format the game uses (for example PNG) in the Mixed Converter, saved to a new folder. Skip it if the format is already the same.", "混合コンバーターで、編集した画像をゲームが使う形式（例: PNG）に変換し、新しいフォルダに保存します。形式がすでに同じなら省略します。", "在混合转换器中把编辑后的图片转换成游戏使用的格式（例如 PNG），保存到新文件夹。格式已相同则跳过。", "혼합 변환기에서 편집한 이미지를 게임이 쓰는 형식(예: PNG)으로 변환해 새 폴더에 저장합니다. 형식이 이미 같으면 건너뜁니다.")
        else if (type == 6 && s == 16) p("Buka Bandingkan Folder: pilih folder asli (bongkaran yang tidak diubah) untuk memastikan hanya gambar yang dimaksud yang berubah; folder kerja terisi sebagai folder diedit.", "Open Compare Folders: pick the original folder (the untouched unpack) to make sure only the intended images changed; the work folder is filled in as the edited folder.", "フォルダ比較を開きます。元のフォルダ（手を付けていない展開結果）を選んで、意図した画像だけが変わったか確認します。作業フォルダが編集後のフォルダとして入ります。", "打开对比文件夹：选择原始文件夹（未改动的解包结果），确认只有预期的图片发生了变化；工作文件夹会自动作为编辑后的文件夹。", "폴더 비교를 엽니다. 원본 폴더(건드리지 않은 압축 해제본)를 골라 의도한 이미지만 바뀌었는지 확인합니다. 작업 폴더가 편집한 폴더로 채워집니다.")
        else if (type == 6 && s == 5) p("Kemas folder hasil sunting menjadi arsip baru di engine yang sama.", "Pack the edited folder into a new archive of the same engine.", "編集したフォルダを同じエンジンの新しいアーカイブにパックします。", "将编辑后的文件夹打包成同一引擎的新档案。", "편집한 폴더를 같은 엔진의 새 아카이브로 묶습니다.")
        else if (type == 6 && s == 1) p("Bongkar arsip game ke folder kerja (lewati jika game sudah berupa folder biasa).", "Unpack the game archive into a work folder (skip if the game is already a plain folder).", "ゲームのアーカイブを作業フォルダに展開します（すでに通常のフォルダなら省略）。", "将游戏档案解包到工作文件夹（如果游戏已是普通文件夹则跳过）。", "게임 아카이브를 작업 폴더로 풉니다 (이미 일반 폴더면 건너뜁니다).")
        else if (type == 5 && s == 13) p("Cari file yang sama persis dan hapus kembarannya (satu per kelompok selalu tersisa). Kerjakan pada salinan.", "Find identical files and delete the copies (one per group always remains). Work on a copy.", "同一ファイルを探して複製を削除します（各グループに1つは必ず残ります）。コピーで作業してください。", "找出相同文件并删除副本（每组始终保留一个）。请在副本上操作。", "같은 파일을 찾아 사본을 삭제합니다 (그룹마다 하나는 항상 남습니다). 사본으로 작업하세요.")
        else if (type == 5 && s == 12) p("Kecilkan gambar, audio, dan video sekaligus di Konverter Campuran, disimpan ke folder baru.", "Shrink images, audio and video at once in the Mixed Converter, saved to a new folder.", "混合コンバーターで画像・音声・動画をまとめて縮小し、新しいフォルダに保存します。", "在混合转换器中一次性缩小图片、音频和视频，保存到新文件夹。", "혼합 변환기에서 이미지·오디오·비디오를 한꺼번에 줄여 새 폴더에 저장합니다.")
        else if (type == 5 && s == 5) p("Kemas folder yang sudah rapi menjadi arsip baru di engine yang sama.", "Pack the tidied folder into a new archive of the same engine.", "整理したフォルダを同じエンジンの新しいアーカイブにパックします。", "将整理好的文件夹打包成同一引擎的新档案。", "정리한 폴더를 같은 엔진의 새 아카이브로 묶습니다.")
        else if ((type == 4 || type == 5) && s == 1) p("Bongkar arsip game ke folder kerja (lewati jika game sudah berupa folder biasa).", "Unpack the game archive into a work folder (skip if the game is already a plain folder).", "ゲームのアーカイブを作業フォルダに展開します（すでに通常のフォルダなら省略）。", "将游戏档案解包到工作文件夹（如果游戏已是普通文件夹则跳过）。", "게임 아카이브를 작업 폴더로 풉니다 (이미 일반 폴더면 건너뜁니다).")
        else if (type == 3 && s == 1) p("Bongkar arsip dua kali: ke folder kerja (untuk diubah) dan ke folder lain yang dibiarkan asli (pembanding). Untuk yang kedua, pilih folder tujuan lain di alat Bongkar.", "Unpack the archive twice: into the work folder (to edit) and into another folder that stays untouched (for comparison). For the second one pick a different output folder in the Unpack tool.", "アーカイブを2回展開します。作業フォルダ（編集用）と、手を付けない別のフォルダ（比較用）です。2回目は展開ツールで別の出力フォルダを選びます。", "把档案解包两次：一次到工作文件夹（用于修改），一次到不动的另一个文件夹（用于对比）。第二次请在解包工具中选择其他输出文件夹。", "아카이브를 두 번 풉니다. 작업 폴더(편집용)와 건드리지 않는 다른 폴더(비교용)입니다. 두 번째는 압축 해제 도구에서 다른 출력 폴더를 고르세요.")
        else if (type == 3 && s == 5) p("Kemas folder tujuan salinan (hanya file yang berubah) menjadi arsip patch; folder itu terisi otomatis dari langkah Bandingkan.", "Pack the folder the changed files were copied into as the patch archive; it is filled in from the Compare step.", "変更したファイルだけのフォルダをパッチのアーカイブとしてパックします。", "把只含改动文件的文件夹打包成补丁档案。", "바뀐 파일만 든 폴더를 패치 아카이브로 묶습니다.")
        else if (type == 1 && s == 1) p("Bongkar arsip game ke folder kerja (lewati jika game sudah berupa folder biasa).", "Unpack the game archive into a work folder (skip if the game is already a plain folder).", "ゲームのアーカイブを作業フォルダに展開します（すでに通常のフォルダなら省略）。", "将游戏档案解包到工作文件夹（如果游戏已是普通文件夹则跳过）。", "게임 아카이브를 작업 폴더로 풉니다 (이미 일반 폴더면 건너뜁니다).")
        else if (type == 1 && s == 5) p("Kemas folder hasil kompres menjadi arsip baru di engine yang sama.", "Pack the compressed folder into a new archive of the same engine.", "圧縮したフォルダを同じエンジンの新しいアーカイブにパックします。", "将压缩后的文件夹打包成同一引擎的新档案。", "압축된 폴더를 같은 엔진의 새 아카이브로 묶습니다.")
        else stepDescBase(s)

    private fun stepDescBase(s: Int) = when (s) {
        21 -> p("Salin seluruh folder kerja ke folder lain (Bandingkan Folder > Cadangkan) sebelum menghapus duplikat atau mengubah nama file.", "Copy the whole work folder to another folder (Compare Folders > Backup) before deleting duplicates or renaming files.", "重複の削除やファイル名の変更の前に、作業フォルダ全体を別のフォルダにコピーします（フォルダ比較 > バックアップ）。", "在删除重复项或重命名文件之前，把整个工作文件夹复制到另一个文件夹（对比文件夹 > 备份）。", "중복을 삭제하거나 파일 이름을 바꾸기 전에 작업 폴더 전체를 다른 폴더로 복사합니다 (폴더 비교 > 백업).")
        19 -> p("Buka gambar di Editor Gambar, tutup teks lama dengan bentuk atau kuas berwarna latar (atau Pipet), tulis teks terjemahan di lapisan teks, lalu Ekspor dengan nama file yang sama ke folder kerja.", "Open the image in the Image Editor, cover the old text with a shape or brush in the background colour (or use the Eyedropper), write the translation on a text layer, then Export it under the same file name into the work folder.", "画像エディタで画像を開き、元の文字を背景色の図形やブラシ（またはスポイト）で隠し、文字レイヤーに翻訳を入力して、作業フォルダに同じファイル名で書き出します。", "在图片编辑器中打开图片，用背景色的形状或画笔（或取色器）盖住原文字，在文字图层写入译文，然后以相同文件名导出到工作文件夹。", "이미지 편집기에서 이미지를 열고 원래 글자를 배경색 도형이나 브러시(또는 스포이트)로 가린 뒤, 글자 레이어에 번역문을 입력하고 같은 파일 이름으로 작업 폴더에 내보냅니다.")
        18 -> p("Ganti nama file bermasalah (non-Latin, karakter khusus) dan perbarui file game yang memanggilnya di Optimasi Nama.", "Rename problematic files (non-Latin, special characters) and update the game files that refer to them in Name Optimization.", "問題のあるファイル名（非ラテン文字、特殊文字）を名前の最適化で変更し、参照しているゲームファイルも更新します。", "在名称优化中重命名有问题的文件（非拉丁字符、特殊字符），并更新引用它们的游戏文件。", "이름 최적화에서 문제 있는 파일(비라틴 문자, 특수 문자)의 이름을 바꾸고 이를 참조하는 게임 파일도 갱신합니다.")
        17 -> p("Cari teks yang belum diterjemahkan atau masih bahasa asli di Pemeriksaan Terjemahan.", "Find text that is still untranslated or in the original language in the Translation Check.", "翻訳チェックで、未翻訳または原文のままのテキストを探します。", "在翻译检查中查找尚未翻译或仍为原文的文本。", "번역 점검에서 번역되지 않았거나 원문 그대로인 텍스트를 찾습니다.")
        15 -> p("Ubah file di folder kerja memakai Editor Teks (teks biasa, JSON, struktur, ganti massal).", "Change files in the work folder with the Text Editors (plain text, JSON, structured, bulk replace).", "テキストエディタ（プレーンテキスト、JSON、構造、一括置換）で作業フォルダのファイルを変更します。", "用文本编辑器（纯文本、JSON、结构化、批量替换）修改工作文件夹中的文件。", "텍스트 편집기(일반 텍스트, JSON, 구조, 일괄 바꾸기)로 작업 폴더의 파일을 바꿉니다.")
        16 -> p("Buka Bandingkan Folder: pilih folder asli (bongkaran yang tidak diubah); folder kerja terisi sebagai folder diedit. Setelah dibandingkan, pilih folder tujuan lalu salin file yang berubah dan file baru.", "Open Compare Folders: pick the original folder (the untouched unpack); the work folder is filled in as the edited folder. After comparing, pick the target folder and copy the changed and new files.", "フォルダ比較を開きます。元のフォルダ（手を付けていない展開結果）を選ぶと、作業フォルダが編集後のフォルダとして入ります。比較したらコピー先を選び、変更されたファイルと新しいファイルをコピーします。", "打开对比文件夹：选择原始文件夹（未改动的解包结果），工作文件夹会自动作为编辑后的文件夹。对比后选择目标文件夹，复制有变化的和新文件。", "폴더 비교를 엽니다. 원본 폴더(건드리지 않은 압축 해제본)를 고르면 작업 폴더가 편집한 폴더로 채워집니다. 비교한 뒤 대상 폴더를 고르고 바뀐 파일과 새 파일을 복사합니다.")
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
