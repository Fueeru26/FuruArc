package com.furuarc.i18n

import com.furuarc.settings.Lang

/**
 * Japanese / Chinese (Simplified) / Korean translations for text written with t("indonesia", "english").
 * Key = exact English text; text not present here is shown in English.
 * Line format: English | Japanese | Chinese | Korean
 */
object Tr {
    private val ja = HashMap<String, String>()
    private val zh = HashMap<String, String>()
    private val ko = HashMap<String, String>()

    fun tr(lang: Lang, en: String): String = get(lang, en) ?: en
    fun trf(lang: Lang, tpl: String, vararg x: Any?): String {
        var s = get(lang, tpl) ?: tpl
        for (i in x.indices) s = s.replace("{" + i + "}", x[i].toString())
        return s
    }

    fun get(lang: Lang, en: String): String? = when (lang) {
        Lang.JA -> ja[en]
        Lang.ZH -> zh[en]
        Lang.KO -> ko[en]
        else -> null
    }

    private const val TABLE = """
Game Assets | ゲームアセット | 游戏资源 | 게임 에셋
Game Tools | ゲームツール | 游戏工具 | 게임 도구
Support Tools | サポートツール | 辅助工具 | 보조 도구
Settings | 設定 | 设置 | 설정
Add | 追加 | 添加 | 추가
Search | 検索 | 搜索 | 검색
Refresh | 更新 | 刷新 | 새로고침
No games yet | ゲームがまだありません | 还没有游戏 | 게임이 아직 없습니다
Add a game folder and browse its assets | ゲームフォルダを追加してアセットを閲覧します | 添加游戏文件夹并浏览其资源 | 게임 폴더를 추가하고 에셋을 둘러보세요
Add folder | フォルダを追加 | 添加文件夹 | 폴더 추가
Search games… | ゲームを検索… | 搜索游戏… | 게임 검색…
No results | 結果なし | 无结果 | 결과 없음
Scanning folders… | フォルダをスキャン中… | 正在扫描文件夹… | 폴더 스캔 중…
Folder not accessible | フォルダにアクセスできません | 无法访问文件夹 | 폴더에 접근할 수 없습니다
Add Game | ゲームを追加 | 添加游戏 | 게임 추가
Game folder | ゲームフォルダ | 游戏文件夹 | 게임 폴더
Game name | ゲーム名 | 游戏名称 | 게임 이름
Cover (optional) | カバー（任意） | 封面（可选） | 커버(선택)
If no cover image is set, the game folder is searched for one | カバー画像がない場合、ゲームフォルダ内から探します | 如果没有封面图片，将在游戏文件夹内查找 | 커버 이미지가 없으면 게임 폴더 안에서 찾습니다
Cancel | キャンセル | 取消 | 취소
Next | 次へ | 下一步 | 다음
Save | 保存 | 保存 | 저장
This folder is already in the list. | このフォルダはすでにリストにあります。 | 此文件夹已在列表中。 | 이 폴더는 이미 목록에 있습니다.
Rename | 名前を変更 | 重命名 | 이름 변경
Change Cover | カバーを変更 | 更换封面 | 커버 변경
Delete Folder | フォルダを削除 | 删除文件夹 | 폴더 삭제
Remove folder from the list? | リストからフォルダを削除しますか？ | 从列表中移除该文件夹？ | 목록에서 폴더를 제거할까요?
Delete | 削除 | 删除 | 삭제
Audio | オーディオ | 音频 | 오디오
Images | 画像 | 图片 | 이미지
Video | ビデオ | 视频 | 비디오
Text | テキスト | 文本 | 텍스트
Scanning… | スキャン中… | 扫描中… | 스캔 중…
Scan again | 再スキャン | 重新扫描 | 다시 스캔
Encrypted RPG Maker assets are previewed automatically (decrypted temporarily; original files are not changed). | 暗号化されたRPGツクールのアセットは自動的にプレビューされます（一時的に復号。元のファイルは変更されません）。 | 加密的RPG Maker资源会自动预览（临时解密，不会修改原文件）。 | 암호화된 RPG Maker 에셋은 자동으로 미리보기됩니다(임시로 복호화하며 원본 파일은 변경되지 않습니다).
Encryption key not found, encrypted assets cannot be previewed. | 暗号化キーが見つからないため、暗号化されたアセットはプレビューできません。 | 未找到加密密钥，无法预览加密资源。 | 암호화 키를 찾을 수 없어 암호화된 에셋을 미리볼 수 없습니다.
Search files… | ファイルを検索… | 搜索文件… | 파일 검색…
Sort | 並べ替え | 排序 | 정렬
Sort by Name A ➞ Z | 名前順 A ➞ Z | 按名称 A ➞ Z | 이름순 A ➞ Z
Sort by Name Z ➞ A | 名前順 Z ➞ A | 按名称 Z ➞ A | 이름순 Z ➞ A
Sort by Size Small ➞ Large | サイズ順 小 ➞ 大 | 按大小 小 ➞ 大 | 크기순 작은 것 ➞ 큰 것
Sort by Size Large ➞ Small | サイズ順 大 ➞ 小 | 按大小 大 ➞ 小 | 크기순 큰 것 ➞ 작은 것
Sort Random | ランダム順 | 随机排序 | 무작위 정렬
Folder changed, updating the list… | フォルダが変更されました。リストを更新中… | 文件夹已更改，正在更新列表… | 폴더가 변경되어 목록을 갱신하는 중…
Preparing file from archive… | アーカイブからファイルを準備中… | 正在从压缩包准备文件… | 아카이브에서 파일 준비 중…
Cache limit | キャッシュ上限 | 缓存上限 | 캐시 한도
The cache is used for audio/video from inside archives. Encrypted RPG Maker assets play without any cache. | キャッシュはアーカイブ内の音声/動画に使われます。暗号化されたRPGツクールのアセットはキャッシュなしで再生されます。 | 缓存用于压缩包内的音频/视频。加密的RPG Maker资源无需缓存即可播放。 | 캐시는 아카이브 안의 오디오/비디오에 사용됩니다. 암호화된 RPG Maker 에셋은 캐시 없이 재생됩니다.
No files. | ファイルがありません。 | 没有文件。 | 파일이 없습니다.
Encrypted | 暗号化 | 已加密 | 암호화됨
Playback failed. This format may not be supported by the device. | 再生に失敗しました。この形式はデバイスで未対応の可能性があります。 | 播放失败。设备可能不支持此格式。 | 재생에 실패했습니다. 이 형식은 기기에서 지원되지 않을 수 있습니다.
Loading… | 読み込み中… | 加载中… | 불러오는 중…
The image cannot be shown. | 画像を表示できません。 | 无法显示图片。 | 이미지를 표시할 수 없습니다.
Previous | 前へ | 上一个 | 이전
Close | 閉じる | 关闭 | 닫기
Pinch to zoom, swipe to change image, double-tap to reset. | ピンチで拡大、スワイプで画像切替、ダブルタップでリセット。 | 双指缩放，滑动切换图片，双击重置。 | 핀치로 확대, 스와이프로 이미지 전환, 더블 탭으로 초기화.
Scanning text and scripts… | テキストとスクリプトをスキャン中… | 正在扫描文本和脚本… | 텍스트와 스크립트 스캔 중…
Encoding | エンコード | 编码 | 인코딩
Auto | 自動 | 自动 | 자동
Dialogue | セリフ | 对话 | 대사
Raw | 生データ | 原始 | 원본
Script dialogue | スクリプトのセリフ | 脚本对话 | 스크립트 대사
RPG Maker data | RPGツクールデータ | RPG Maker数据 | RPG Maker 데이터
No dialogue recognised in this file. | このファイルにセリフは見つかりませんでした。 | 此文件中未识别到对话。 | 이 파일에서 대사를 찾지 못했습니다.
File too large to extract (max 32 MB). | ファイルが大きすぎて展開できません（最大32 MB）。 | 文件过大，无法提取（最大32 MB）。 | 파일이 너무 커서 추출할 수 없습니다(최대 32MB).
Only the first 4 MB is shown. | 最初の4 MBのみ表示しています。 | 仅显示前4 MB。 | 처음 4MB만 표시합니다.
Search in text… | テキスト内を検索… | 在文本中搜索… | 텍스트에서 검색…
Game Assets Preview Cache | ゲームアセットのプレビューキャッシュ | 游戏资源预览缓存 | 게임 에셋 미리보기 캐시
Clear cache | キャッシュを消去 | 清除缓存 | 캐시 지우기
Detected Engine: | 検出されたエンジン： | 检测到的引擎： | 감지된 엔진:
Unknown | 不明 | 未知 | 알 수 없음
Auto-search on the internet | インターネットで自動検索 | 在互联网上自动搜索 | 인터넷에서 자동 검색
Manual search on the internet | インターネットで手動検索 | 在互联网上手动搜索 | 인터넷에서 수동 검색
Source | ソース | 来源 | 출처
Searching… | 検索中… | 搜索中… | 검색 중…
No matching cover was found. | 一致するカバーが見つかりませんでした。 | 未找到匹配的封面。 | 일치하는 커버를 찾지 못했습니다.
No results. | 結果がありません。 | 没有结果。 | 결과가 없습니다.
Could not download the image. | 画像をダウンロードできませんでした。 | 无法下载图片。 | 이미지를 다운로드할 수 없습니다.
Find covers online | カバーをオンラインで検索 | 在线查找封面 | 온라인에서 커버 찾기
Only games without a cover | カバーのないゲームのみ | 仅没有封面的游戏 | 커버가 없는 게임만
Replace all game covers | すべてのゲームのカバーを置換 | 替换所有游戏封面 | 모든 게임 커버 교체
Select all | すべて選択 | 全选 | 모두 선택
Clear | 解除 | 清除 | 해제
Extract | 展開 | 提取 | 추출
Share | 共有 | 分享 | 공유
Favorite | お気に入り | 收藏 | 즐겨찾기
Unfavorite | お気に入り解除 | 取消收藏 | 즐겨찾기 해제
Engine label on Game Assets covers | ゲームアセットのカバーにエンジン名を表示 | 在游戏资源封面上显示引擎标签 | 게임 에셋 커버에 엔진 라벨 표시
Show the engine name (e.g. Kirikiri / KAG) at the bottom-left of each game cover. | 各ゲームのカバーの左下にエンジン名（例：Kirikiri / KAG）を表示します。 | 在每个游戏封面的左下角显示引擎名称（如 Kirikiri / KAG）。 | 각 게임 커버의 왼쪽 아래에 엔진 이름(예: Kirikiri / KAG)을 표시합니다.
Select mode | 選択モード | 选择模式 | 선택 모드
Favorites only | お気に入りのみ | 仅收藏 | 즐겨찾기만
Long-press to select multiple files. | 長押しで複数のファイルを選択できます。 | 长按可选择多个文件。 | 길게 눌러 여러 파일을 선택하세요.
Extracting | 展開中 | 正在提取 | 추출 중
Stop | 停止 | 停止 | 중지
OK | OK | 确定 | 확인
Too many files to share at once (max 20). Use Extract. | 一度に共有できるファイルが多すぎます（最大20）。展開を使ってください。 | 一次分享的文件过多（最多20个），请使用提取。 | 한 번에 공유할 파일이 너무 많습니다(최대 20개). 추출을 사용하세요.
Total size is over 200 MB. Use Extract. | 合計サイズが200 MBを超えています。展開を使ってください。 | 总大小超过200 MB，请使用提取。 | 총 크기가 200MB를 넘습니다. 추출을 사용하세요.
Could not share the files. | ファイルを共有できませんでした。 | 无法分享文件。 | 파일을 공유할 수 없습니다.
Choose destination folder | 保存先フォルダを選択 | 选择目标文件夹 | 대상 폴더 선택
"""

    private class Tpl(val regex: Regex, val idx: List<Int>, val key: String)
    private val byPrefix = HashMap<String, MutableList<Tpl>>()
    private val shortList = ArrayList<Tpl>()
    private val msgCache = HashMap<String, String>()
    private val phRe = Regex("\\{(\\d+)\\}")

    private fun addTpl(key: String) {
        val ms = phRe.findAll(key).toList()
        if (ms.isEmpty()) return
        val parts = key.split(phRe)
        val re = Regex(parts.joinToString("(.+?)") { Regex.escape(it) }, RegexOption.DOT_MATCHES_ALL)
        val t = Tpl(re, ms.map { it.groupValues[1].toInt() }, key)
        val pre = parts[0]
        if (pre.length >= 4) byPrefix.getOrPut(pre.take(4)) { ArrayList() }.add(t) else shortList.add(t)
    }

    /** Translate a finished message (exact match or matching a template containing {n}). Only for JA/ZH/KO. */
    fun msg(lang: Lang, s: String): String {
        if ((lang != Lang.JA && lang != Lang.ZH && lang != Lang.KO) || s.length < 3 || s.length > 400) return s
        get(lang, s)?.let { return it }
        val ck = lang.ordinal.toString() + s
        msgCache[ck]?.let { return it }
        var out = s
        val cands = (byPrefix[s.take(4)] ?: emptyList<Tpl>()) + shortList
        for (t in cands) {
            val m = t.regex.matchEntire(s) ?: continue
            var r = get(lang, t.key) ?: continue
            for (j in t.idx.indices) r = r.replace("{" + t.idx[j] + "}", m.groupValues[j + 1])
            out = r
            break
        }
        if (msgCache.size > 3000) msgCache.clear()
        msgCache[ck] = out
        return out
    }

    init {
        for (line in (listOf(TABLE) + TrData.ALL).flatMap { it.lines() }) {
            val c = line.split(" | ").map { it.trim().replace("\\n", "\n") }
            if (c.size != 4) continue
            ja[c[0]] = c[1]; zh[c[0]] = c[2]; ko[c[0]] = c[3]
            if (c[0].contains("{0}")) addTpl(c[0])
        }
    }
}
