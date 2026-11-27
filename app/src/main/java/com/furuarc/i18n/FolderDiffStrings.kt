package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Text of the Folder Compare tool (changed files, copy to a new folder). */
class FolderDiffStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val title get() = p("Bandingkan Folder", "Compare Folders", "フォルダを比較", "对比文件夹", "폴더 비교")
    val sub get() = p("Cari file yang berubah antara folder asli dan folder diedit, lalu salin ke folder baru", "Find the files that changed between the original and the edited folder, then copy them to a new folder", "元のフォルダと編集後のフォルダで変わったファイルを探し、新しいフォルダにコピーします", "找出原始文件夹与编辑后文件夹之间有变化的文件，并复制到新文件夹", "원본 폴더와 편집한 폴더에서 바뀐 파일을 찾아 새 폴더로 복사합니다")
    val intro get() = p(
        "Pilih folder asli dan folder yang sudah diedit. File yang berubah dan file baru bisa disalin ke folder lain dengan subfolder yang sama, misalnya sebagai bahan patch.",
        "Pick the original folder and the edited folder. The changed and new files can be copied into another folder with the same sub-folders, for example as the material of a patch.",
        "元のフォルダと編集後のフォルダを選びます。変更されたファイルと新しいファイルは、同じサブフォルダ構成で別のフォルダにコピーでき、パッチの素材になります。",
        "选择原始文件夹和编辑后的文件夹。有变化的文件和新文件可以按相同的子文件夹结构复制到另一个文件夹，例如作为补丁的素材。",
        "원본 폴더와 편집한 폴더를 고르세요. 바뀐 파일과 새 파일은 같은 하위 폴더 구조로 다른 폴더에 복사할 수 있어 패치 자료로 쓸 수 있습니다.",
    )
    val folderA get() = p("Folder asli", "Original folder", "元のフォルダ", "原始文件夹", "원본 폴더")
    val folderB get() = p("Folder diedit", "Edited folder", "編集後のフォルダ", "编辑后的文件夹", "편집한 폴더")
    val folderOut get() = p("Folder tujuan salinan", "Folder to copy into", "コピー先フォルダ", "复制目标文件夹", "복사할 폴더")
    val backupAll get() = p("Cadangkan: salin semua file folder diedit", "Backup: copy all files of the edited folder", "バックアップ：編集後のフォルダの全ファイルをコピー", "备份：复制编辑后文件夹的所有文件", "백업: 편집한 폴더의 모든 파일 복사")
    val backupNote get() = p("Tanpa membandingkan: seluruh isi folder diedit disalin ke folder tujuan, berguna sebagai cadangan sebelum menghapus atau mengubah banyak file.", "Without comparing: the whole edited folder is copied into the target folder, useful as a backup before deleting or changing many files.", "比較せずに、編集後のフォルダの内容をすべてコピー先に複製します。多くのファイルを削除・変更する前のバックアップに便利です。", "不进行对比：把编辑后文件夹的全部内容复制到目标文件夹，适合在删除或修改大量文件之前做备份。", "비교하지 않고 편집한 폴더 전체를 대상 폴더로 복사합니다. 많은 파일을 삭제하거나 바꾸기 전의 백업에 유용합니다.")
    val optTitle get() = p("Opsi", "Options", "オプション", "选项", "옵션")
    val deep get() = p("Periksa isi file (lebih akurat, lebih lambat)", "Check file contents (more accurate, slower)", "ファイルの内容も確認（正確だが遅い）", "检查文件内容（更准确，更慢）", "파일 내용 확인 (더 정확하지만 느림)")
    val ignoreLabel get() = p("Abaikan (pisahkan koma, boleh *)", "Ignore (comma separated, * allowed)", "無視（カンマ区切り、* 可）", "忽略（逗号分隔，可用 *）", "무시 (쉼표로 구분, * 사용 가능)")
    val ignoreNote get() = p("File atau folder dengan nama ini tidak dibandingkan dan tidak disalin, misalnya System.json yang ditulis ulang oleh alat enkripsi RPGM.", "Files or folders with these names are not compared or copied, for example System.json, which the RPGM encrypt tool rewrites.", "この名前のファイルやフォルダは比較・コピーされません。例：RPGM 暗号化ツールが書き換える System.json。", "这些名称的文件或文件夹不会被比较或复制，例如 RPGM 加密工具会重写的 System.json。", "이 이름의 파일이나 폴더는 비교·복사하지 않습니다. 예: RPGM 암호화 도구가 다시 쓰는 System.json.")
    val deepNote get() = p("Tanpa ini, hanya ukuran file yang dibandingkan, sehingga perubahan yang ukurannya sama tidak terdeteksi.", "Without it only the file sizes are compared, so a change that keeps the size is not detected.", "オフの場合はファイルサイズだけを比較するため、サイズが同じ変更は検出されません。", "不勾选时仅比较文件大小，大小相同的更改不会被发现。", "끄면 파일 크기만 비교하므로 크기가 같은 변경은 찾지 못합니다.")
    val compare get() = p("Bandingkan", "Compare", "比較する", "对比", "비교")
    val cancel get() = p("Batal", "Cancel", "キャンセル", "取消", "취소")
    val summary get() = p("Ringkasan", "Summary", "概要", "摘要", "요약")
    fun counts(changed: Int, added: Int, removed: Int, same: Int) = p(
        "Berubah: $changed\nBaru di folder diedit: $added\nHilang dari folder diedit: $removed\nSama: $same",
        "Changed: $changed\nNew in the edited folder: $added\nMissing from the edited folder: $removed\nSame: $same",
        "変更: $changed\n編集後のフォルダに新規: $added\n編集後のフォルダに無い: $removed\n同じ: $same",
        "已更改：$changed\n编辑后文件夹中新增：$added\n编辑后文件夹中缺失：$removed\n相同：$same",
        "변경: $changed\n편집한 폴더에서 새로 생김: $added\n편집한 폴더에서 없어짐: $removed\n동일: $same",
    )
    val changedList get() = p("File berubah", "Changed files", "変更されたファイル", "有变化的文件", "바뀐 파일")
    val addedList get() = p("File baru", "New files", "新しいファイル", "新文件", "새 파일")
    val removedList get() = p("File hilang", "Missing files", "無いファイル", "缺失的文件", "없어진 파일")
    val noChange get() = p("Tidak ada perbedaan.", "No differences.", "違いはありません。", "没有差异。", "차이가 없습니다.")
    val copy get() = p("Salin file berubah dan baru", "Copy changed and new files", "変更・新規ファイルをコピー", "复制有变化的和新文件", "바뀐 파일과 새 파일 복사")
    val copyNote get() = p("File di folder tujuan dengan nama yang sama akan ditimpa. Pilih folder tujuan yang kosong atau baru.", "Files with the same name in the target folder are overwritten. Pick an empty or new folder.", "コピー先に同名のファイルがある場合は上書きされます。空の新しいフォルダを選んでください。", "目标文件夹中的同名文件会被覆盖。请选择空的或新的文件夹。", "대상 폴더에 같은 이름의 파일이 있으면 덮어씁니다. 비어 있거나 새 폴더를 고르세요.")
    fun copyResult(ok: Int, fail: Int) = p("Disalin: $ok, gagal: $fail", "Copied: $ok, failed: $fail", "コピー: $ok、失敗: $fail", "已复制：$ok，失败：$fail", "복사: $ok, 실패: $fail")
}

fun folderDiffStr(lang: Lang) = FolderDiffStr(lang)
