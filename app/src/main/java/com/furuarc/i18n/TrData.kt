package com.furuarc.i18n

/** Translation data for JA/ZH/KO (English | Japanese | Chinese | Korean). Split so each constant stays under 64 KB. */
internal object TrData {
    const val T0 = """
/** Translation data for JA/ZH/KO (English | Japanese | Chinese | Korean). Split so each constant stays under 64 KB. */
/** Translation data for JA/ZH/KO (English | Japanese | Chinese | Korean). Split so each constant stays under 64 KB. */
File Inspection | ファイル検査 | 文件检查 | 파일 검사
File & Folder Inspector | ファイル・フォルダー検査 | 文件与文件夹检查器 | 파일 및 폴더 검사기
Format Test | フォーマットテスト | 格式测试 | 포맷 테스트
Pack sample data with each format, unpack it again and confirm the result is identical | 各形式でサンプルデータをパックし、再び展開して結果が同じか確認します | 用每种格式打包示例数据，再解包并确认结果一致 | 각 형식으로 샘플 데이터를 묶고 다시 풀어 결과가 같은지 확인합니다
The app creates small sample files in temporary storage, packs them with every format that can be packed (also edge samples: empty file, Japanese names, subfolders, 300 files), unpacks, packs again and compares. It also tests small real archives when present, the compression codecs and script text export/import. None of your files are read or changed. | アプリは一時保存領域に小さなサンプルファイルを作成し、パックできるすべての形式でパックし (空ファイル、日本語名、サブフォルダ、300ファイルなどの端のケースも含む)、展開して再びパックし、比較します。小さな実アーカイブ (ある場合)、圧縮コーデック、スクリプトのテキスト書き出し/取り込みもテストします。お使いのファイルは読み取りも変更もされません。 | 应用会在临时存储中创建小型示例文件，用每种可打包的格式打包（还包括边缘样例：空文件、日文名称、子文件夹、300 个文件），解包、再次打包并比较。若存在小型真实压缩包，也会测试，并测试压缩编解码器和脚本文本导出/导入。不会读取或修改你的任何文件。 | 앱이 임시 저장소에 작은 샘플 파일을 만들고, 묶을 수 있는 모든 형식으로 묶고(빈 파일, 일본어 이름, 하위 폴더, 300개 파일 같은 경계 사례 포함) 풀고 다시 묶어 비교합니다. 작은 실제 아카이브(있을 경우), 압축 코덱, 스크립트 텍스트 내보내기/가져오기도 테스트합니다. 사용자의 파일은 읽거나 변경하지 않습니다.
This test only proves that what the app packs, the app can unpack again. A real game may still reject an archive if its engine needs a key or a special layout. | このテストは、アプリがパックしたものをアプリ自身が再び展開できることだけを確認します。エンジンが鍵や特別な構造を必要とする場合、実際のゲームはアーカイブを受け付けないことがあります。 | 此测试只能证明本应用打包的内容可以被本应用再次解包。如果引擎需要密钥或特殊结构，真实的游戏仍可能拒绝该压缩包。 | 이 테스트는 앱이 묶은 것을 앱이 다시 풀 수 있다는 점만 확인합니다. 엔진이 키나 특수한 구조를 요구하면 실제 게임은 아카이브를 거부할 수 있습니다.
Start test | テストを開始 | 开始测试 | 테스트 시작
Run again | もう一度実行 | 重新运行 | 다시 실행
Stop | 停止 | 停止 | 중지
Testing {0}/{1}: {2}… | テスト中 {0}/{1}: {2}… | 正在测试 {0}/{1}：{2}… | 테스트 중 {0}/{1}: {2}…
{0} passed · {1} warnings · {2} failed · {3} skipped | {0} 件合格 · {1} 件警告 · {2} 件失敗 · {3} 件スキップ | 通过 {0} · 警告 {1} · 失败 {2} · 跳过 {3} | 통과 {0} · 경고 {1} · 실패 {2} · 건너뜀 {3}
ALL PASSED | すべて合格 | 全部通过 | 모두 통과
Stopped before finishing | 完了前に停止しました | 未完成即已停止 | 끝나기 전에 중지되었습니다
Format Test Report | フォーマットテストレポート | 格式测试报告 | 포맷 테스트 보고서
{0} files · {1} KB · {2} ms | {0} ファイル · {1} KB · {2} ms | {0} 个文件 · {1} KB · {2} 毫秒 | 파일 {0}개 · {1} KB · {2} ms
Failed: {0} | 失敗: {0} | 失败：{0} | 실패: {0}
Different number of files: expected {0}, got {1} ({2}) | ファイル数が異なります: 期待 {0}、結果 {1} ({2}) | 文件数量不同：应为 {0}，实际 {1}（{2}） | 파일 수가 다릅니다: 예상 {0}, 결과 {1} ({2})
File did not come back after unpacking: {0} | 展開後にファイルが戻りませんでした: {0} | 解包后找不到文件：{0} | 풀고 난 뒤 파일이 없습니다: {0}
File content differs after unpacking: {0} | 展開後にファイルの内容が異なります: {0} | 解包后文件内容不同：{0} | 푼 뒤 파일 내용이 다릅니다: {0}
Passed, but automatic detection picks {0} | 合格ですが、自動判別は {0} を選びます | 已通过，但自动识别选择了 {0} | 통과했지만 자동 감지가 {0}을(를) 선택합니다
Passed, but automatic detection does not recognise this archive | 合格ですが、自動判別がこのアーカイブを認識しません | 已通过，但自动识别无法识别此压缩包 | 통과했지만 자동 감지가 이 아카이브를 인식하지 못합니다
Skipped: needs files from a real game (an index file or the unpacked original archive) | スキップ: 実際のゲームのファイル(インデックスファイルまたは展開した元のアーカイブ)が必要です | 已跳过：需要真实游戏的文件（索引文件或解包后的原始压缩包） | 건너뜀: 실제 게임의 파일(인덱스 파일 또는 원본 아카이브를 푼 결과)이 필요합니다
Support Tools > File & Folder Inspector > Format Test | サポートツール > ファイル・フォルダー検査 > フォーマットテスト | 支持工具 > 文件与文件夹检查器 > 格式测试 | 지원 도구 > 파일 및 폴더 검사기 > 포맷 테스트
Checks that packing and unpacking work for every format on this device.\n• Tap Start test. The app creates three small sample files in temporary storage, packs them with every format that can be packed (several option choices are tested one by one), unpacks them again and compares names and contents.\n• Result per format: ✓ passed, ! passed with a warning (e.g. automatic detection picks another format), ✕ failed with the reason, – skipped (formats that need files from a real game, such as AGE and Siglus PCK). Filter: All, !, ✕, ✓.\n• Stop cancels the test. The test does not read or change any of your files.\n• Copy report or Export .txt to include the result (with the app version and device) when reporting a problem.\n• The test only proves that what the app packs, the app can unpack again; a real game may still reject an archive if its engine needs a key or a special layout.\n• The test also covers: edge samples (empty file, exactly 1 MiB, Japanese / spaced names, subfolders, 300 files; problems are shown as warnings), packing the unpacked files again (the archive must be the same), every format that accepts the archive (when more than one does), small real archives from assets/formattest (checked by SHA-256), the LZMA/LZ4/LZSS codecs and script text export/import. | この端末で、すべての形式のパックと展開が正しく動作するか確認します。\n• 「テストを開始」をタップします。アプリは一時保存領域に小さなサンプルファイルを3つ作成し、パックできるすべての形式でパックして(オプションの選択肢もいくつか個別にテストします)、再び展開して名前と内容を比較します。\n• 形式ごとの結果: ✓ 合格、! 警告付きで合格(例: 自動判別が別の形式を選ぶ)、✕ 失敗(理由付き)、– スキップ(AGE や Siglus PCK など、実際のゲームのファイルが必要な形式)。フィルター: すべて、!、✕、✓。\n• 「停止」でテストを中止できます。テストはお使いのファイルを読み取りも変更もしません。\n• 問題を報告するときは、「レポートをコピー」または「.txt をエクスポート」で結果(アプリのバージョンと端末情報つき)を添付できます。\n• このテストは、アプリがパックしたものをアプリ自身が展開できることだけを確認します。エンジンが鍵や特別な構造を必要とする場合、実際のゲームはアーカイブを受け付けないことがあります。\n• テストには次も含まれます: 端のケース (空ファイル、ちょうど1 MiB、日本語/空白入りの名前、サブフォルダ、300ファイル。問題は警告として表示)、展開したファイルの再パック (同じアーカイブになる必要あり)、アーカイブを受け入れるすべての形式 (複数ある場合)、assets/formattest の小さな実アーカイブ (SHA-256で確認)、LZMA/LZ4/LZSSコーデック、スクリプトのテキスト書き出し/取り込み。 | 检查此设备上每种格式的打包和解包是否正常。\n• 点击“开始测试”。应用会在临时存储中创建三个小型示例文件，用每种可打包的格式打包（部分选项会逐个测试），再解包并比较名称和内容。\n• 每种格式的结果：✓ 通过，! 通过但有警告（例如自动识别选择了其他格式），✕ 失败并显示原因，– 已跳过（需要真实游戏文件的格式，如 AGE 和 Siglus PCK）。筛选：全部、!、✕、✓。\n• “停止”可取消测试。测试不会读取或修改你的任何文件。\n• 报告问题时，可用“复制报告”或“导出 .txt”附上结果（含应用版本和设备信息）。\n• 此测试只能证明本应用打包的内容可以被本应用再次解包；如果引擎需要密钥或特殊结构，真实游戏仍可能拒绝该压缩包。\n• 测试还包括：边缘样例（空文件、恰好 1 MiB、日文/带空格的名称、子文件夹、300 个文件；问题以警告显示）、把解包的文件再次打包（压缩包必须相同）、所有接受该压缩包的格式（多于一个时）、assets/formattest 中的小型真实压缩包（用 SHA-256 检查）、LZMA/LZ4/LZSS 编解码器，以及脚本文本导出/导入。 | 이 기기에서 모든 형식의 묶기와 풀기가 제대로 동작하는지 확인합니다.\n• '테스트 시작'을 누르세요. 앱이 임시 저장소에 작은 샘플 파일 3개를 만들고, 묶을 수 있는 모든 형식으로 묶은 뒤(일부 옵션 선택지는 하나씩 시험합니다) 다시 풀어 이름과 내용을 비교합니다.\n• 형식별 결과: ✓ 통과, ! 경고와 함께 통과(예: 자동 감지가 다른 형식을 선택), ✕ 실패(이유 표시), – 건너뜀(AGE, Siglus PCK처럼 실제 게임 파일이 필요한 형식). 필터: 전체, !, ✕, ✓.\n• '중지'를 누르면 테스트가 취소됩니다. 테스트는 사용자의 파일을 읽거나 변경하지 않습니다.\n• 문제를 신고할 때는 '보고서 복사' 또는 '.txt 내보내기'로 결과(앱 버전과 기기 정보 포함)를 첨부할 수 있어요.\n• 이 테스트는 앱이 묶은 것을 앱이 다시 풀 수 있다는 점만 확인합니다. 엔진이 키나 특수한 구조를 요구하면 실제 게임은 아카이브를 거부할 수 있습니다.\n• 테스트에는 다음도 포함돼요: 경계 사례(빈 파일, 정확히 1 MiB, 일본어/공백 이름, 하위 폴더, 300개 파일. 문제는 경고로 표시), 푼 파일 다시 묶기(아카이브가 같아야 함), 아카이브를 인식하는 모든 형식(둘 이상일 때), assets/formattest의 작은 실제 아카이브(SHA-256로 확인), LZMA/LZ4/LZSS 코덱, 스크립트 텍스트 내보내기/가져오기.
Inspect, compare and scan archives, scripts, folders and translations | アーカイブ、スクリプト、フォルダ、翻訳を検査・比較・スキャンします | 检查、比较和扫描压缩包、脚本、文件夹及翻译 | 아카이브, 스크립트, 폴더, 번역을 검사, 비교, 스캔해요
Format, checksum, corrupt files, encryption and wrong code in one file | 1つのファイルの形式、チェックサム、破損、暗号化、不正なコードを確認します | 检查单个文件的格式、校验和、损坏、加密及错误代码 | 파일 하나의 형식, 체크섬, 손상, 암호화, 잘못된 코드를 확인해요
Folder Inspection | フォルダ検査 | 文件夹检查 | 폴더 검사
Inspect every archive and script in a folder at once | フォルダ内のすべてのアーカイブとスクリプトを一括で検査します | 一次性检查文件夹中的所有压缩包和脚本 | 폴더 안의 모든 아카이브와 스크립트를 한 번에 검사해요
Compare Files | ファイル比較 | 文件比较 | 파일 비교
Compare two archives, scripts or translation files | 2つのアーカイブ、スクリプト、翻訳ファイルを比較します | 比较两个压缩包、脚本或翻译文件 | 아카이브, 스크립트, 번역 파일 두 개를 비교해요
Translation Scan | 翻訳スキャン | 翻译扫描 | 번역 스캔
Check control codes, long lines, empty entries and inconsistent translations | 制御コード、長すぎる行、空の項目、不一致の翻訳を確認します | 检查控制码、过长的行、空条目及不一致的翻译 | 제어 코드, 긴 줄, 빈 항목, 일관되지 않은 번역을 확인해요
HEALTHY | 正常 | 正常 | 정상
WARNINGS | 警告 | 警告 | 경고
PROBLEMS FOUND | 問題あり | 发现问题 | 문제 발견
Unpack this archive | このアーカイブを展開 | 解包此压缩包 | 이 아카이브 풀기
Match with a hash from the source | 配布元のハッシュと照合 | 与来源提供的哈希比对 | 출처의 해시와 대조
Paste a CRC32, MD5 or SHA-256 | CRC32、MD5、SHA-256を貼り付け | 粘贴 CRC32、MD5 或 SHA-256 | CRC32, MD5 또는 SHA-256 붙여넣기
✕ Does not match this file's CRC32, MD5 or SHA-256 | ✕ このファイルのCRC32、MD5、SHA-256と一致しません | ✕ 与此文件的 CRC32、MD5 或 SHA-256 不匹配 | ✕ 이 파일의 CRC32, MD5, SHA-256과 일치하지 않아요
Unusual hash length (CRC32 = 8, MD5 = 32, SHA-256 = 64 characters). | ハッシュの長さが不正です(CRC32 = 8、MD5 = 32、SHA-256 = 64文字)。 | 哈希长度异常(CRC32 = 8、MD5 = 32、SHA-256 = 64 个字符)。 | 해시 길이가 올바르지 않아요(CRC32 = 8, MD5 = 32, SHA-256 = 64자).
Copy report | レポートをコピー | 复制报告 | 보고서 복사
Export .txt | .txtで書き出し | 导出 .txt | .txt 내보내기
Report copied | レポートをコピーしました | 报告已复制 | 보고서를 복사했어요
Report saved | レポートを保存しました | 报告已保存 | 보고서를 저장했어요
Could not save the report | レポートを保存できませんでした | 无法保存报告 | 보고서를 저장하지 못했어요
Folder to inspect | 検査するフォルダ | 要检查的文件夹 | 검사할 폴더
Pick a game folder. Every archive and script inside is checked (index/file start only, so it is fast). Plain image, audio and video files are skipped. | ゲームフォルダを選択してください。内部のすべてのアーカイブとスクリプトを検査します(インデックス/ファイル先頭のみのため高速です)。通常の画像、音声、動画ファイルはスキップされます。 | 请选择游戏文件夹。将检查其中所有压缩包和脚本(仅检查索引/文件开头,因此速度很快)。普通的图片、音频和视频文件会被跳过。 | 게임 폴더를 선택하세요. 안에 있는 모든 아카이브와 스크립트를 검사해요(인덱스/파일 시작 부분만 확인하므로 빨라요). 일반 이미지, 오디오, 비디오 파일은 건너뛰어요.
Folder contents | フォルダの内容 | 文件夹内容 | 폴더 내용
Summary | 概要 | 摘要 | 요약
Search files (name, type or note) | ファイルを検索(名前、種類、メモ) | 搜索文件(名称、类型或备注) | 파일 검색(이름, 유형, 메모)
Largest files | サイズの大きいファイル | 最大的文件 | 용량이 큰 파일
Top extensions | 主な拡張子 | 常见扩展名 | 주요 확장자
Detected game engine | 検出されたゲームエンジン | 检测到的游戏引擎 | 감지된 게임 엔진
This folder contains files from several engines | このフォルダには複数のエンジンのファイルが含まれています | 此文件夹包含多个引擎的文件 | 이 폴더에 여러 엔진의 파일이 들어 있어요
No game engine recognised from the file names | ファイル名からゲームエンジンを特定できませんでした | 无法根据文件名识别游戏引擎 | 파일 이름으로 게임 엔진을 인식하지 못했어요
Engines are recognised from file names/extensions and readable archive formats; common extensions can be misleading. | エンジンはファイル名/拡張子と読み取り可能なアーカイブ形式から判別します。一般的な拡張子は誤認の原因になることがあります。 | 引擎根据文件名/扩展名和可读取的压缩包格式识别;常见扩展名可能会造成误判。 | 엔진은 파일 이름/확장자와 읽을 수 있는 아카이브 형식으로 인식해요. 흔한 확장자는 잘못 인식될 수 있어요.
All | すべて | 全部 | 전체
No files match the filter. | フィルターに一致するファイルがありません。 | 没有符合筛选条件的文件。 | 필터와 일치하는 파일이 없어요.
Tap a file to see its full result. | ファイルをタップすると詳細結果を表示します。 | 点按文件可查看完整结果。 | 파일을 탭하면 전체 결과를 볼 수 있어요.
File A (old) | ファイルA(旧) | 文件 A(旧) | 파일 A(이전)
File B (new) | ファイルB(新) | 文件 B(新) | 파일 B(새 파일)
Compare | 比較 | 比较 | 비교
Pick two files of the same kind: two archives, two scripts/text files, or two translation files (CSV/TXT/JSON). | 同じ種類のファイルを2つ選択してください(アーカイブ2つ、スクリプト/テキスト2つ、または翻訳ファイル2つ(CSV/TXT/JSON))。 | 请选择两个同类文件:两个压缩包、两个脚本/文本文件,或两个翻译文件(CSV/TXT/JSON)。 | 같은 종류의 파일 두 개를 선택하세요. 아카이브 두 개, 스크립트/텍스트 파일 두 개 또는 번역 파일 두 개(CSV/TXT/JSON)예요.
Fast | 高速 | 快速 | 빠르게
Thorough | 詳細 | 精确 | 정밀하게
Fast: compares file names and sizes inside archives. Thorough: also compares the content of same-size files (slower). | 高速: アーカイブ内のファイル名とサイズを比較します。詳細: 同じサイズのファイルは内容も比較します(低速)。 | 快速:比较压缩包内的文件名和大小。精确:还会比较大小相同文件的内容(较慢)。 | 빠르게: 아카이브 안의 파일 이름과 크기를 비교해요. 정밀하게: 크기가 같은 파일은 내용도 비교해요(느려요).
Added | 追加 | 新增 | 추가됨
Changed | 変更 | 已更改 | 변경됨
Removed | 削除 | 已删除 | 삭제됨
Same | 同一 | 相同 | 동일
Original changed | 原文が変更 | 原文已更改 | 원문 변경됨
Translation changed | 翻訳が変更 | 译文已更改 | 번역 변경됨
✓ Both files are identical | ✓ 2つのファイルは同一です | ✓ 两个文件完全相同 | ✓ 두 파일이 동일해요
✕ The files differ | ✕ ファイルが異なります | ✕ 文件不同 | ✕ 파일이 서로 달라요
The text comparison counts added/removed lines without regard to line order. | テキスト比較では、行の順序に関係なく追加/削除された行を数えます。 | 文本比较统计新增/删除的行数,不考虑行的顺序。 | 텍스트 비교는 줄 순서와 상관없이 추가/삭제된 줄을 세어요.
Lines added in B | Bで追加された行 | B 中新增的行 | B에서 추가된 줄
Lines removed from A | Aから削除された行 | A 中被删除的行 | A에서 삭제된 줄
Differences | 差分 | 差异 | 차이점
Search… | 検索… | 搜索… | 검색…
Translation file (CSV/TXT/JSON) | 翻訳ファイル(CSV/TXT/JSON) | 翻译文件(CSV/TXT/JSON) | 번역 파일(CSV/TXT/JSON)
Pick a translation export made by FuruArc (RPGM or VN). | FuruArcで書き出した翻訳ファイル(RPGMまたはVN)を選択してください。 | 请选择由 FuruArc 导出的翻译文件(RPGM 或 VN)。 | FuruArc에서 내보낸 번역 파일(RPGM 또는 VN)을 선택하세요.
Line length limit (characters) | 行の長さの上限(文字数) | 行长度上限(字符数) | 줄 길이 제한(글자 수)
Total entries | 総項目数 | 条目总数 | 전체 항목 수
Filled | 入力済み | 已填写 | 입력됨
Empty | 空 | 为空 | 비어 있음
Same as original | 原文と同じ | 与原文相同 | 원문과 동일
Control codes differ | 制御コードが不一致 | 控制码不一致 | 제어 코드 불일치
Lines too long | 長すぎる行 | 行过长 | 너무 긴 줄
Inconsistent | 不一致 | 不一致 | 일관성 없음
Same original text but different translations. | 同じ原文に対して異なる翻訳があります。 | 原文相同但译文不同。 | 원문은 같은데 번역이 달라요.
Longest line | 最長行 | 最长行 | 가장 긴 줄
Duplicate keys (file+path) | 重複キー(ファイル+パス) | 重复键(文件+路径) | 중복 키(파일+경로)
Showing | 表示中 | 显示 | 표시 중
Original | 原文 | 原文 | 원문
Translation | 翻訳 | 译文 | 번역
File Cutter | ファイルカッター | 文件截取 | 파일 자르기
Take the start, middle or end of a large file as a small file, e.g. to send as a sample | 大きなファイルの先頭・中間・末尾を小さなファイルとして切り出します(サンプル送付用など) | 截取大文件的开头、中间或结尾生成小文件,例如用于发送样本 | 큰 파일의 시작, 중간, 끝 부분을 작은 파일로 잘라내요(샘플 전송용 등)
Source file | 元ファイル | 源文件 | 원본 파일
Part to take | 切り出す部分 | 截取部分 | 가져올 부분
Start | 先頭 | 开头 | 시작
End | 末尾 | 结尾 | 끝
Part size | 部分サイズ | 截取大小 | 부분 크기
Amount | 量 | 数量 | 크기
An archive index is usually at the start of the file (sometimes at the end). 1-2 MB from the start is usually enough to analyse the structure. | アーカイブのインデックスは通常ファイルの先頭にあります(末尾の場合もあります)。構造の解析には先頭の1~2 MBで通常は十分です。 | 压缩包索引通常位于文件开头(有时在结尾)。通常取开头的 1-2 MB 就足以分析结构。 | 아카이브 인덱스는 보통 파일 시작 부분에 있어요(끝에 있을 때도 있어요). 구조를 분석하려면 보통 시작 부분 1-2 MB면 충분해요.
Output file name | 出力ファイル名 | 输出文件名 | 출력 파일 이름
Output Folder | 出力フォルダ | 输出文件夹 | 출력 폴더
Cut File | ファイルを切り出す | 截取文件 | 파일 자르기
Pick the source file first. | 先に元ファイルを選択してください。 | 请先选择源文件。 | 먼저 원본 파일을 선택하세요.
Cutting… | 切り出し中… | 正在截取… | 자르는 중…
Key Finder | キー検出 | 密钥查找 | 키 찾기
Find the encryption key in a game folder and the files that use it | ゲームフォルダから暗号化キーと、それを使用しているファイルを検出します | 在游戏文件夹中查找加密密钥及使用该密钥的文件 | 게임 폴더에서 암호화 키와 그 키를 사용하는 파일을 찾아요
Pick the game folder and press Analyze. The app scans the whole folder, looks for the encryption key and shows which files are encrypted with it. Everything runs offline. | ゲームフォルダを選択して「解析」を押してください。フォルダ全体をスキャンして暗号化キーを探し、そのキーで暗号化されたファイルを表示します。すべてオフラインで動作します。 | 请选择游戏文件夹并点按“分析”。应用会扫描整个文件夹,查找加密密钥,并显示使用该密钥加密的文件。全程离线运行。 | 게임 폴더를 선택하고 '분석'을 누르세요. 앱이 폴더 전체를 스캔해서 암호화 키를 찾고, 그 키로 암호화된 파일을 보여줘요. 모든 작업은 오프라인으로 실행돼요.
Analyze | 解析 | 分析 | 분석
Looking for | 検索対象 | 查找目标 | 찾는 대상
All supported engines | 対応するすべてのエンジン | 所有支持的引擎 | 지원되는 모든 엔진
No key could be found. | キーが見つかりませんでした。 | 未找到密钥。 | 키를 찾지 못했어요.
Verified | 検証済み | 已验证 | 검증됨
Candidate | 候補 | 候选 | 후보
Key | キー | 密钥 | 키
Found in | 検出場所 | 发现于 | 발견 위치
Copy key | キーをコピー | 复制密钥 | 키 복사
Key copied | キーをコピーしました | 密钥已复制 | 키를 복사했어요
Notes | メモ | 备注 | 메모
Paste this key into the Key option when Unpack/Pack asks for it. | 展開/パックでキーの入力を求められたら、このキーを「キー」オプションに貼り付けてください。 | 当解包/打包要求输入密钥时,请将此密钥粘贴到“密钥”选项中。 | 풀기/묶기에서 키를 요청하면 이 키를 '키' 옵션에 붙여넣으세요.
✓ Nothing found in this category. | ✓ このカテゴリには該当なしです。 | ✓ 此类别中没有发现任何内容。 | ✓ 이 카테고리에서는 아무것도 발견되지 않았어요.
File Toolkit | ファイルツールキット | 文件工具箱 | 파일 도구함
Cut, split, join and batch-rename files | ファイルの切り出し、分割、結合、一括リネーム | 截取、分割、合并和批量重命名文件 | 파일 자르기, 나누기, 합치기, 일괄 이름 변경
Source file | 元ファイル | 源文件 | 원본 파일
Output Folder | 出力フォルダ | 输出文件夹 | 출력 폴더
Pick the source file first. | 先に元ファイルを選択してください。 | 请先选择源文件。 | 먼저 원본 파일을 선택하세요.
Pick the Output Folder first. | 先に出力フォルダを選択してください。 | 请先选择输出文件夹。 | 먼저 출력 폴더를 선택하세요.
File Splitter | ファイル分割 | 文件分割 | 파일 나누기
Split a large file into numbered parts (.001, .002, …) | 大きなファイルを連番のパーツ(.001、.002、…)に分割します | 将大文件分割为带编号的分卷(.001、.002、…) | 큰 파일을 번호가 붙은 조각(.001, .002, …)으로 나눠요
Split by | 分割方法 | 分割方式 | 나누는 기준
By size | サイズで分割 | 按大小 | 크기별
By part count | パーツ数で分割 | 按分卷数 | 조각 수별
Size of each part | 各パーツのサイズ | 每个分卷的大小 | 조각당 크기
Number of parts | パーツ数 | 分卷数量 | 조각 수
Part base name | パーツのベース名 | 分卷基础名称 | 조각 기본 이름
Split File | ファイルを分割 | 分割文件 | 파일 나누기
Splitting… | 分割中… | 正在分割… | 나누는 중…
The part size is not smaller than the file, so the result is a single part. | パーツサイズがファイル以上のため、結果は1つのパーツになります。 | 分卷大小不小于文件大小,因此结果只有一个分卷。 | 조각 크기가 파일 크기보다 작지 않아서 결과는 조각 하나가 돼요.
The source file is empty. | 元ファイルが空です。 | 源文件为空。 | 원본 파일이 비어 있어요.
The source file stopped being readable before the end. | 元ファイルが途中で読み取れなくなりました。 | 源文件在读取到末尾之前中断了。 | 원본 파일을 끝까지 읽기 전에 읽을 수 없게 됐어요.
File Joiner | ファイル結合 | 文件合并 | 파일 합치기
Join numbered parts (.001, .002, …) back into one file | 連番のパーツ(.001、.002、…)を1つのファイルに結合します | 将带编号的分卷(.001、.002、…)合并回一个文件 | 번호가 붙은 조각(.001, .002, …)을 하나의 파일로 합쳐요
File parts | ファイルパーツ | 文件分卷 | 파일 조각
Output file name | 出力ファイル名 | 输出文件名 | 출력 파일 이름
Join Files | ファイルを結合 | 合并文件 | 파일 합치기
Joining… | 結合中… | 正在合并… | 합치는 중…
Pick at least 2 parts. | 2つ以上のパーツを選択してください。 | 请至少选择 2 个分卷。 | 조각을 2개 이상 선택하세요.
Warning: the sequence does not start at .001, make sure no leading part is missing. | 警告: 連番が.001から始まっていません。先頭のパーツが欠けていないか確認してください。 | 警告:序号不是从 .001 开始,请确认没有缺少开头的分卷。 | 경고: 순서가 .001부터 시작하지 않아요. 앞부분 조각이 빠지지 않았는지 확인하세요.
Batch Rename | 一括リネーム | 批量重命名 | 일괄 이름 변경
Rename many files at once, with a preview before applying | 多数のファイルを一度にリネームします(適用前にプレビュー可能) | 一次性重命名多个文件,应用前可预览 | 많은 파일의 이름을 한 번에 바꿔요(적용 전 미리보기 가능)
Folder with the files | ファイルのあるフォルダ | 文件所在文件夹 | 파일이 있는 폴더
Pick a folder first. | 先にフォルダを選択してください。 | 请先选择文件夹。 | 먼저 폴더를 선택하세요.
Only extensions (optional) | 対象の拡張子(任意) | 仅限扩展名(可选) | 확장자 제한(선택)
Example: png, jpg. Leave empty for all files. | 例: png, jpg。空欄の場合はすべてのファイルが対象です。 | 例如:png, jpg。留空则处理所有文件。 | 예: png, jpg. 비워 두면 모든 파일이 대상이에요.
Find & Replace | 検索と置換 | 查找和替换 | 찾아 바꾸기
Number Pattern | 連番パターン | 编号模式 | 번호 패턴
Prefix/Suffix | 接頭辞/接尾辞 | 前缀/后缀 | 접두사/접미사
Fix Garbled Names | 文字化けした名前を修正 | 修复乱码文件名 | 깨진 이름 고치기
Find | 検索 | 查找 | 찾기
Replace with | 置換後 | 替换为 | 바꿀 내용
Match case | 大文字小文字を区別 | 区分大小写 | 대소문자 구분
Ignore case | 大文字小文字を区別しない | 忽略大小写 | 대소문자 무시
Name pattern | 名前パターン | 名称模式 | 이름 패턴
{nama} = original name without extension, {no} = sequence number (restarts in each folder). The file extension is always kept. | {nama} = 拡張子を除いた元の名前、{no} = 連番(フォルダごとにリセット)。拡張子は常に保持されます。 | {nama} = 不含扩展名的原文件名,{no} = 序号(每个文件夹重新开始)。扩展名始终保留。 | {nama} = 확장자를 뺀 원래 이름, {no} = 일련번호(폴더마다 다시 시작). 확장자는 항상 유지돼요.
Start at | 開始番号 | 起始编号 | 시작 번호
Digits | 桁数 | 位数 | 자릿수
Prefix | 接頭辞 | 前缀 | 접두사
Suffix (before extension) | 接尾辞(拡張子の前) | 后缀(扩展名之前) | 접미사(확장자 앞)
Keep case | 変更しない | 保持大小写 | 그대로 유지
lowercase | 小文字 | 小写 | 소문자
UPPERCASE | 大文字 | 大写 | 대문자
Preview | プレビュー | 预览 | 미리보기
Search file names in preview | プレビュー内のファイル名を検索 | 在预览中搜索文件名 | 미리보기에서 파일 이름 검색
No files match the search. | 検索に一致するファイルがありません。 | 没有符合搜索条件的文件。 | 검색과 일치하는 파일이 없어요.
No matching files in this folder. | このフォルダに該当するファイルがありません。 | 此文件夹中没有匹配的文件。 | 이 폴더에 일치하는 파일이 없어요.
Some names clash or are invalid (marked red). Change the settings so it can be applied. | 名前が重複しているか無効なものがあります(赤で表示)。適用できるよう設定を変更してください。 | 部分名称冲突或无效(标为红色)。请修改设置后再应用。 | 이름이 겹치거나 올바르지 않은 항목이 있어요(빨간색 표시). 적용할 수 있도록 설정을 바꿔 주세요.
Renaming… | リネーム中… | 正在重命名… | 이름 변경 중…
File names in the folder will change. When it finishes you can still revert with the Undo button. | フォルダ内のファイル名が変更されます。完了後も「元に戻す」ボタンで復元できます。 | 文件夹中的文件名将被更改。完成后仍可使用“撤销”按钮恢复。 | 폴더 안의 파일 이름이 바뀌어요. 완료 후에도 '실행 취소' 버튼으로 되돌릴 수 있어요.
Undo | 元に戻す | 撤销 | 실행 취소
Middle | 中間 | 中间 | 중간
Start offset | 開始オフセット | 起始偏移 | 시작 오프셋
The offset is beyond the end of the file. | オフセットがファイルの末尾を超えています。 | 偏移量超出了文件末尾。 | 오프셋이 파일 끝을 넘었어요.
Create checksum (.sha256) | チェックサムを作成(.sha256) | 创建校验和(.sha256) | 체크섬 만들기(.sha256)
No checksum | チェックサムなし | 无校验和 | 체크섬 없음
Or pick a folder with parts | またはパーツのあるフォルダを選択 | 或选择包含分卷的文件夹 | 또는 조각이 있는 폴더 선택
No numbered parts (.001, .002, …) in this folder. | このフォルダに連番のパーツ(.001、.002、…)がありません。 | 此文件夹中没有带编号的分卷(.001、.002、…)。 | 이 폴더에 번호가 붙은 조각(.001, .002, …)이 없어요.
Ready to join | 結合の準備完了 | 可以合并 | 합칠 준비 완료
not verified | 未検証 | 未验证 | 검증되지 않음
checksum matches | チェックサム一致 | 校验和匹配 | 체크섬 일치
whole-file checksum does NOT match | ファイル全体のチェックサムが一致しません | 整个文件的校验和不匹配 | 파일 전체 체크섬이 일치하지 않아요
checksum has no entry for these parts | チェックサムにこれらのパーツの項目がありません | 校验和中没有这些分卷的条目 | 체크섬에 이 조각들의 항목이 없어요
Rule | ルール | 规则 | 규칙
This folder only | このフォルダのみ | 仅此文件夹 | 이 폴더만
Include subfolders | サブフォルダを含む | 包含子文件夹 | 하위 폴더 포함
Names were wrongly read as | 誤って読み取られた文字コード | 名称被错误读取为 | 잘못 읽힌 인코딩
Should be encoding | 正しい文字コード | 应为编码 | 올바른 인코딩
Duplicate File Finder | 重複ファイル検出 | 重复文件查找 | 중복 파일 찾기
Find identical files in a folder and its subfolders | フォルダとサブフォルダ内の同一ファイルを検出します | 查找文件夹及其子文件夹中的相同文件 | 폴더와 하위 폴더에서 동일한 파일을 찾아요
Folder to scan | スキャンするフォルダ | 要扫描的文件夹 | 스캔할 폴더
Reading the folder… | フォルダを読み込み中… | 正在读取文件夹… | 폴더를 읽는 중…
No duplicate files in this folder. | このフォルダに重複ファイルはありません。 | 此文件夹中没有重复文件。 | 이 폴더에 중복 파일이 없어요.
Select all but one | 1つを残して全選択 | 保留一个,其余全选 | 하나만 남기고 모두 선택
Clear selection | 選択解除 | 清除选择 | 선택 해제
Selected files are deleted permanently and cannot be restored. At least one file stays in every group. | 選択したファイルは完全に削除され、復元できません。各グループに最低1つのファイルが残ります。 | 所选文件将被永久删除且无法恢复。每组至少会保留一个文件。 | 선택한 파일은 영구적으로 삭제되며 복원할 수 없어요. 각 그룹에 파일이 최소 하나는 남아요.
Hex Viewer | 16進ビューア | 十六进制查看器 | 헥스 뷰어
View raw file content by offset, search text or bytes | ファイルの生データをオフセット指定で表示し、テキストやバイト列を検索します | 按偏移量查看文件原始内容,可搜索文本或字节 | 파일의 원본 내용을 오프셋별로 보고 텍스트나 바이트를 검색해요
File | ファイル | 文件 | 파일
8 bytes/row | 1行8バイト | 每行 8 字节 | 행당 8바이트
16 bytes/row | 1行16バイト | 每行 16 字节 | 행당 16바이트
Go to offset (hex) | オフセットへ移動(16進) | 跳转到偏移量(十六进制) | 오프셋으로 이동(16진수)
Go | 移動 | 跳转 | 이동
Text or bytes to find | 検索するテキストまたはバイト列 | 要查找的文本或字节 | 찾을 텍스트 또는 바이트
UTF-8 text | UTF-8テキスト | UTF-8 文本 | UTF-8 텍스트
Shift-JIS text | Shift-JISテキスト | Shift-JIS 文本 | Shift-JIS 텍스트
Hex | 16進 | 十六进制 | 16진수
Find next | 次を検索 | 查找下一个 | 다음 찾기
Not found up to the end of the file. | ファイルの末尾まで見つかりませんでした。 | 直到文件末尾都未找到。 | 파일 끝까지 찾지 못했어요.
Invalid search (hex must have an even length, 64 bytes at most). | 検索条件が無効です(16進は偶数桁、最大64バイトまで)。 | 搜索无效(十六进制长度必须为偶数,最多 64 字节)。 | 검색이 올바르지 않아요(16진수는 짝수 길이여야 하며 최대 64바이트예요).
Invalid hex offset. | 16進オフセットが無効です。 | 十六进制偏移量无效。 | 16진수 오프셋이 올바르지 않아요.
Offsets and hex search use hexadecimal, e.g. search: 4D 5A 90. | オフセットと16進検索は16進数で指定します(例: 4D 5A 90)。 | 偏移量和十六进制搜索均使用十六进制,例如搜索:4D 5A 90。 | 오프셋과 16진수 검색은 16진수를 사용해요. 예: 4D 5A 90.
The file is empty. | ファイルが空です。 | 文件为空。 | 파일이 비어 있어요.
File Name Optimizer | ファイル名オプティマイザー | 文件名优化器 | 파일 이름 최적화
Rename files with special characters to safe names and update their references in the game | 特殊文字を含むファイル名を安全な名前に変更し、ゲーム内の参照も更新します | 将含特殊字符的文件重命名为安全名称,并更新游戏中的引用 | 특수 문자가 있는 파일을 안전한 이름으로 바꾸고 게임 내 참조도 함께 업데이트해요
Source Folder | 元フォルダ | 源文件夹 | 원본 폴더
Backup Folder (Optional) | バックアップフォルダ(任意) | 备份文件夹(可选) | 백업 폴더(선택)
Clear Backup Folder | バックアップフォルダをクリア | 清除备份文件夹 | 백업 폴더 지우기
Pick the Source Folder (the game folder) first. | 先に元フォルダ(ゲームフォルダ)を選択してください。 | 请先选择源文件夹(游戏文件夹)。 | 먼저 원본 폴더(게임 폴더)를 선택하세요.
Reading the folder… | フォルダを読み込み中… | 正在读取文件夹… | 폴더를 읽는 중…
File scope | 対象ファイル | 文件范围 | 파일 범위
All files with special characters | 特殊文字を含むすべてのファイル | 所有含特殊字符的文件 | 특수 문자가 있는 모든 파일
Only media assets with special characters | 特殊文字を含むメディアアセットのみ | 仅含特殊字符的媒体资源 | 특수 문자가 있는 미디어 에셋만
New name style | 新しい名前の形式 | 新名称样式 | 새 이름 스타일
Keep the ASCII part (戦闘BGM → BGM_001) | ASCII部分を残す(戦闘BGM → BGM_001) | 保留 ASCII 部分(戦闘BGM → BGM_001) | ASCII 부분 유지(戦闘BGM → BGM_001)
Number only (file_001) | 番号のみ(file_001) | 仅编号(file_001) | 번호만(file_001)
Include folder names | フォルダ名を含める | 包含文件夹名称 | 폴더 이름 포함
Include ASCII names with spaces or special marks | スペースや記号を含むASCII名も対象にする | 包含带空格或特殊符号的 ASCII 名称 | 공백이나 특수 기호가 있는 ASCII 이름도 포함
Reference replacement | 参照の置換 | 引用替换 | 참조 교체
Safe (whole name between quotes / slashes) | 安全(引用符/スラッシュで囲まれた名前全体) | 安全(引号/斜杠之间的完整名称) | 안전(따옴표/슬래시로 둘러싸인 전체 이름)
Wide (every occurrence of the name) | 広範囲(名前のすべての出現箇所) | 广泛(名称的每一处出现) | 넓게(이름이 나오는 모든 곳)
Fix reference letter case | 参照の大文字小文字を修正 | 修正引用的大小写 | 참조의 대소문자 수정
Text file extensions to check | 確認するテキストファイルの拡張子 | 要检查的文本文件扩展名 | 확인할 텍스트 파일 확장자
Comma separated. Files that are not UTF-8 or larger than 24 MB are skipped and reported. | カンマ区切り。UTF-8でないファイルや24 MBを超えるファイルはスキップされ、報告されます。 | 以逗号分隔。非 UTF-8 或大于 24 MB 的文件将被跳过并报告。 | 쉼표로 구분해요. UTF-8이 아니거나 24 MB보다 큰 파일은 건너뛰고 알려줘요.
Build Readiness Report | ビルド準備レポート | 构建就绪报告 | 빌드 준비 보고서
No name or format problems detected. | 名前や形式の問題は検出されませんでした。 | 未检测到名称或格式问题。 | 이름이나 형식 문제가 발견되지 않았어요.
Problem paths | 問題のあるパス | 有问题的路径 | 문제가 있는 경로
Scan and Preview | スキャンしてプレビュー | 扫描并预览 | 스캔 및 미리보기
Planning new names… | 新しい名前を作成中… | 正在规划新名称… | 새 이름을 정하는 중…
Preview | プレビュー | 预览 | 미리보기
No names need to be changed with these settings. | この設定では変更が必要な名前はありません。 | 按当前设置,没有需要更改的名称。 | 이 설정으로는 바꿀 이름이 없어요.
No names will be renamed, but some references will get their letter case corrected. | 名前の変更はありませんが、一部の参照の大文字小文字が修正されます。 | 不会重命名任何名称,但部分引用的大小写将被修正。 | 이름은 바뀌지 않지만 일부 참조의 대소문자가 수정돼요.
Refill | 再入力 | 重新填充 | 다시 채우기
Checked files list | 確認したファイル一覧 | 已检查文件列表 | 확인한 파일 목록
Checked files | 確認したファイル | 已检查文件 | 확인한 파일
Changed text files | 変更されたテキストファイル | 已更改的文本文件 | 변경된 텍스트 파일
Files without references | 参照のないファイル | 无引用的文件 | 참조가 없는 파일
No files in this list. | この一覧にファイルはありません。 | 此列表中没有文件。 | 이 목록에 파일이 없어요.
List | 一覧 | 列表 | 목록
[Folder] | [フォルダ] | [文件夹] | [폴더]
Settings changed. Scan again to refresh the preview. | 設定が変更されました。プレビューを更新するには再スキャンしてください。 | 设置已更改。请重新扫描以刷新预览。 | 설정이 변경됐어요. 미리보기를 새로고침하려면 다시 스캔하세요.
Rename and Update References | リネームして参照を更新 | 重命名并更新引用 | 이름 변경 및 참조 업데이트
Continue | 続行 | 继续 | 계속
Continue without backup | バックアップなしで続行 | 不备份并继续 | 백업 없이 계속
Failed items | 失敗した項目 | 失败项目 | 실패한 항목
Change log saved in rename_mapping.json (Source Folder). | 変更ログはrename_mapping.json(元フォルダ内)に保存されました。 | 更改日志已保存到 rename_mapping.json(源文件夹中)。 | 변경 로그를 rename_mapping.json(원본 폴더)에 저장했어요.
Next step: compress assets for the APK with the Mixed Asset Converter. | 次のステップ: Mixed Asset ConverterでAPK用にアセットを圧縮します。 | 下一步:使用 Mixed Asset Converter 压缩 APK 用的资源。 | 다음 단계: Mixed Asset Converter로 APK용 에셋을 압축하세요.
Open Mixed Asset Converter | Mixed Asset Converterを開く | 打开 Mixed Asset Converter | Mixed Asset Converter 열기
Restore Original Names | 元の名前に戻す | 恢复原始名称 | 원래 이름으로 복원
Restore original names? | 元の名前に戻しますか? | 要恢复原始名称吗? | 원래 이름으로 복원할까요?
The last run in rename_mapping.json is reverted: references are restored by reverse replacement, then file and folder names are restored. Letter-case fixes are not reverted. | rename_mapping.jsonの最後の実行内容を元に戻します。参照を逆置換で復元した後、ファイル名とフォルダ名を復元します。大文字小文字の修正は元に戻りません。 | 将撤销 rename_mapping.json 中的最近一次运行:先通过反向替换恢复引用,再恢复文件和文件夹名称。大小写修正不会被撤销。 | rename_mapping.json의 마지막 실행 내용을 되돌려요. 역방향 교체로 참조를 복원한 뒤 파일 및 폴더 이름을 복원해요. 대소문자 수정은 되돌리지 않아요.
Restore | 復元 | 恢复 | 복원
rename_mapping.json not found or empty. | rename_mapping.jsonが見つからないか、空です。 | 未找到 rename_mapping.json 或文件为空。 | rename_mapping.json을 찾을 수 없거나 비어 있어요.
Media Asset Converter | メディアアセットコンバーター | 媒体资源转换器 | 미디어 에셋 변환기
Convert audio, image and video formats, and text encoding | 音声・画像・動画の形式とテキストエンコードを変換します | 转换音频、图片、视频格式及文本编码 | 오디오, 이미지, 동영상 형식과 텍스트 인코딩을 변환해요
Mixed Asset Converter | 混合アセットコンバーター | 混合资源转换器 | 혼합 에셋 변환기
Convert and compress audio, images and video together from one folder | 1つのフォルダ内の音声・画像・動画をまとめて変換・圧縮します | 一次性转换并压缩同一文件夹中的音频、图片和视频 | 한 폴더의 오디오, 이미지, 동영상을 한 번에 변환하고 압축해요
Copy unrecognized files as they are | 認識できないファイルはそのままコピー | 未识别的文件原样复制 | 인식되지 않는 파일은 그대로 복사
Skipped (switch off) | スキップ(オフ) | 已跳过(已关闭) | 건너뜀(꺼짐)
No files of this type in the folder | このフォルダにこの種類のファイルはありません | 此文件夹中没有该类型的文件 | 이 폴더에 해당 유형의 파일이 없어요
Enable at least one type (Audio, Images or Video) or copy unrecognized files. | 少なくとも1つの種類(音声・画像・動画)を有効にするか、認識できないファイルのコピーを有効にしてください。 | 请至少启用一种类型(音频、图片或视频),或启用复制未识别的文件。 | 유형(오디오, 이미지, 동영상)을 하나 이상 켜거나 인식되지 않는 파일 복사를 켜 주세요.
Image resolution | 画像の解像度 | 图片分辨率 | 이미지 해상도
RPG Maker MV/MZ (mixed) | RPGツクール MV/MZ(混合) | RPG Maker MV/MZ(混合) | RPG Maker MV/MZ(혼합)
Ren'Py (mixed) | Ren'Py(混合) | Ren'Py(混合) | Ren'Py(혼합)
Kirikiri (mixed) | 吉里吉里(混合) | Kirikiri(混合) | Kirikiri(혼합)
Audio Asset Converter | 音声アセットコンバーター | 音频资源转换器 | 오디오 에셋 변환기
Convert audio formats and compress with bitrate, sample rate, channels and volume | 音声形式を変換し、ビットレート・サンプリングレート・チャンネル・音量で圧縮します | 转换音频格式,并通过比特率、采样率、声道和音量进行压缩 | 오디오 형식을 변환하고 비트레이트, 샘플레이트, 채널, 볼륨으로 압축해요
Image Asset Converter | 画像アセットコンバーター | 图片资源转换器 | 이미지 에셋 변환기
Convert image formats and compress with quality, size and rotation | 画像形式を変換し、品質・サイズ・回転で圧縮します | 转换图片格式,并通过质量、尺寸和旋转进行压缩 | 이미지 형식을 변환하고 품질, 크기, 회전으로 압축해요
Video Asset Converter | 動画アセットコンバーター | 视频资源转换器 | 동영상 에셋 변환기
Convert video formats and compress with resolution, bitrate and frame rate | 動画形式を変換し、解像度・ビットレート・フレームレートで圧縮します | 转换视频格式,并通过分辨率、比特率和帧率进行压缩 | 동영상 형식을 변환하고 해상도, 비트레이트, 프레임 레이트로 압축해요
Text Encoding Converter | テキストエンコードコンバーター | 文本编码转换器 | 텍스트 인코딩 변환기
Convert text files between Shift-JIS/GBK/Big5 and UTF-8 | テキストファイルをShift-JIS/GBK/Big5とUTF-8の間で変換します | 在 Shift-JIS/GBK/Big5 与 UTF-8 之间转换文本文件 | 텍스트 파일을 Shift-JIS/GBK/Big5와 UTF-8 사이에서 변환해요
File | ファイル | 文件 | 파일
Folder | フォルダ | 文件夹 | 폴더
Source files (multiple allowed) | ソースファイル(複数可) | 源文件(可多选) | 원본 파일(여러 개 가능)
Source folder | ソースフォルダ | 源文件夹 | 원본 폴더
This folder only | このフォルダのみ | 仅此文件夹 | 이 폴더만
Include subfolders | サブフォルダを含める | 包含子文件夹 | 하위 폴더 포함
Scanning folder… | フォルダをスキャン中… | 正在扫描文件夹… | 폴더 검색 중…
No matching files in this folder. | このフォルダに一致するファイルはありません。 | 此文件夹中没有匹配的文件。 | 이 폴더에 일치하는 파일이 없어요.
Input format | 入力形式 | 输入格式 | 입력 형식
All Available Formats | すべての対応形式 | 所有可用格式 | 사용 가능한 모든 형식
The selected format is not available in the folder | 選択した形式はこのフォルダにありません | 所选格式在此文件夹中不可用 | 선택한 형식이 폴더에 없어요
If the folder contains files of different formats, change Input format to choose which files are converted. The subfolder structure is kept in the output folder. | フォルダに異なる形式のファイルがある場合は、入力形式を変更して変換するファイルを選択してください。サブフォルダ構造は出力フォルダでも維持されます。 | 如果文件夹中包含多种格式的文件,请更改输入格式来选择要转换的文件。子文件夹结构会保留在输出文件夹中。 | 폴더에 서로 다른 형식의 파일이 있으면 입력 형식을 바꿔 변환할 파일을 선택하세요. 하위 폴더 구조는 출력 폴더에 그대로 유지돼요.
Only extensions | 拡張子を指定 | 仅限扩展名 | 확장자 지정
Separate with commas. Required, so binary files are not touched. | カンマで区切ります。バイナリファイルを変更しないよう、入力が必須です。 | 用逗号分隔。为避免改动二进制文件,此项必填。 | 쉼표로 구분하세요. 바이너리 파일을 건드리지 않도록 필수 항목이에요.
Output folder | 出力フォルダ | 输出文件夹 | 출력 폴더
If the result file already exists | 変換後のファイルが既にある場合 | 如果结果文件已存在 | 결과 파일이 이미 있는 경우
Overwrite | 上書き | 覆盖 | 덮어쓰기
Skip | スキップ | 跳过 | 건너뛰기
Start Conversion | 変換を開始 | 开始转换 | 변환 시작
Pick an output folder first. | 先に出力フォルダを選択してください。 | 请先选择输出文件夹。 | 먼저 출력 폴더를 선택해 주세요.
Result | 結果 | 结果 | 결과
Cancelled by the user. | ユーザーによりキャンセルされました。 | 已被用户取消。 | 사용자가 취소했어요.
Finished. | 完了しました。 | 已完成。 | 완료되었어요.
Failures | 失敗 | 失败 | 실패
Follow Source | 元に合わせる | 与源相同 | 원본과 동일
Output format not available, please choose manually! | 出力形式を利用できません。手動で選択してください。 | 输出格式不可用,请手动选择! | 출력 형식을 사용할 수 없어요. 직접 선택해 주세요!
Quality | 品質 | 质量 | 품질
Size | サイズ | 大小 | 크기
Estimated size | 推定サイズ | 预估大小 | 예상 크기
The entered size exceeds the source file size! | 入力したサイズが元のファイルサイズを超えています。 | 输入的大小超过了源文件大小! | 입력한 크기가 원본 파일 크기를 초과해요!
A process is running, please cancel it first | 処理中です。先にキャンセルしてください | 有任务正在运行,请先取消 | 작업이 진행 중이에요. 먼저 취소해 주세요
Cancel the running conversion? Files already converted are kept; the file being processed is not written. | 実行中の変換をキャンセルしますか?変換済みのファイルは保持され、処理中のファイルは書き込まれません。 | 要取消正在进行的转换吗?已转换的文件会保留,正在处理的文件不会写入。 | 진행 중인 변환을 취소할까요? 이미 변환된 파일은 유지되고, 처리 중인 파일은 저장되지 않아요.
Discard larger results? | サイズが大きくなった結果を破棄しますか? | 要丢弃变大的结果吗? | 더 커진 결과를 버릴까요?
Yes, discard | はい、破棄する | 是,丢弃 | 예, 버리기
No | いいえ | 否 | 아니요
A process is running. Cancel it and go back? | 処理中です。キャンセルして戻りますか? | 有任务正在运行。要取消并返回吗? | 작업이 진행 중이에요. 취소하고 돌아갈까요?
Only compress if larger than | 次より大きい場合のみ圧縮 | 仅在大于以下大小时压缩 | 다음보다 클 때만 압축
File size limit (KB) | ファイルサイズ上限(KB) | 文件大小限制(KB) | 파일 크기 제한(KB)
Files equal to or smaller than this limit are not compressed (still converted if the format differs). | この上限以下のファイルは圧縮されません(形式が異なる場合は変換されます)。 | 小于或等于此限制的文件不会被压缩(格式不同时仍会转换)。 | 이 제한 이하의 파일은 압축하지 않아요(형식이 다르면 변환은 해요).
"""
    const val T1 = """
Rough estimate; the actual size may differ. | おおよその推定値です。実際のサイズは異なる場合があります。 | 仅为粗略估算,实际大小可能不同。 | 대략적인 추정치예요. 실제 크기와 다를 수 있어요.
Estimating time left… | 残り時間を推定中… | 正在估算剩余时间… | 남은 시간 계산 중…
unchanged (0%) | 変化なし(0%) | 无变化(0%) | 변화 없음(0%)
Output format | 出力形式 | 输出格式 | 출력 형식
Bitrate (kbps) | ビットレート(kbps) | 比特率(kbps) | 비트레이트(kbps)
Automatic | 自動 | 自动 | 자동
Sample rate | サンプリングレート | 采样率 | 샘플레이트
Keep source | 元のまま | 保持原样 | 원본 유지
Mono | モノラル | 单声道 | 모노
Stereo | ステレオ | 立体声 | 스테레오
Volume | 音量 | 音量 | 볼륨
Compress Audio | 音声を圧縮 | 压缩音频 | 오디오 압축
Quality: AAC/Opus/MP3/Vorbis bitrate is multiplied by the percentage (WAV and FLAC: lower sample rate). Size: bitrate is calculated from the target (WAV and FLAC: lower sample rate and channels; a FLAC result may end up smaller than the target). | 品質: AAC/Opus/MP3/Vorbisのビットレートに指定した割合を掛けます(WAVとFLACはサンプリングレートを下げます)。サイズ: 目標からビットレートを計算します(WAVとFLACはサンプリングレートとチャンネル数を下げます。FLACの結果は目標より小さくなる場合があります)。 | 质量:AAC/Opus/MP3/Vorbis 的比特率将乘以该百分比(WAV 和 FLAC:降低采样率)。大小:根据目标大小计算比特率(WAV 和 FLAC:降低采样率和声道数;FLAC 的结果可能比目标更小)。 | 품질: AAC/Opus/MP3/Vorbis 비트레이트에 해당 비율을 곱해요(WAV와 FLAC은 샘플레이트를 낮춰요). 크기: 목표 크기로 비트레이트를 계산해요(WAV와 FLAC은 샘플레이트와 채널 수를 낮춰요. FLAC 결과는 목표보다 작아질 수 있어요).
Compress Image | 画像を圧縮 | 压缩图片 | 이미지 압축
Quality: JPG/WEBP use the slider value as quality; PNG becomes a palette PNG (color count = slider × 256, transparency kept). Size: quality/color count is searched automatically until the result is near the target. | 品質: JPG/WEBPはスライダーの値を品質として使用します。PNGはパレットPNGになります(色数=スライダー×256、透明度は維持)。サイズ: 結果が目標に近づくまで品質/色数を自動で探索します。 | 质量:JPG/WEBP 将滑块值作为质量;PNG 会变为调色板 PNG(颜色数=滑块值×256,保留透明度)。大小:自动搜索质量/颜色数,直到结果接近目标大小。 | 품질: JPG/WEBP는 슬라이더 값을 품질로 사용하고, PNG는 팔레트 PNG가 돼요(색상 수 = 슬라이더 × 256, 투명도 유지). 크기: 결과가 목표에 가까워질 때까지 품질/색상 수를 자동으로 찾아요.
Quality (1-100) | 品質(1-100) | 质量(1-100) | 품질(1-100)
PNG and lossless WEBP ignore quality (always lossless). | PNGと可逆圧縮WEBPでは品質は無視されます(常に可逆)。 | PNG 和无损 WEBP 会忽略质量设置(始终无损)。 | PNG와 무손실 WEBP는 품질을 무시해요(항상 무손실).
Resolution | 解像度 | 分辨率 | 해상도
Original | オリジナル | 原始 | 원본
Percent | パーセント | 百分比 | 퍼센트
Scale | 拡大縮小 | 缩放 | 배율
Keep source aspect ratio | 元の縦横比を維持 | 保持原始宽高比 | 원본 가로세로 비율 유지
Rotate clockwise | 時計回りに回転 | 顺时针旋转 | 시계 방향 회전
Background for transparent images (JPG) | 透過画像の背景(JPG) | 透明图片的背景(JPG) | 투명 이미지의 배경(JPG)
White | 白 | 白色 | 흰색
Black | 黒 | 黑色 | 검은색
EXIF orientation is applied automatically. For animated GIFs only the first frame is used. | EXIFの向きは自動で適用されます。アニメーションGIFは最初のフレームのみ使用されます。 | 会自动应用 EXIF 方向信息。动画 GIF 仅使用第一帧。 | EXIF 방향이 자동으로 적용돼요. 애니메이션 GIF는 첫 프레임만 사용해요.
Resolution (shorter side) | 解像度(短辺) | 分辨率(短边) | 해상도(짧은 변)
Video bitrate (kbps) | 動画ビットレート(kbps) | 视频比特率(kbps) | 동영상 비트레이트(kbps)
Compress Video | 動画を圧縮 | 压缩视频 | 동영상 압축
Quality: the video bitrate is multiplied by the percentage. Size: the bitrate is calculated from the target and the Video Bitrate choice is ignored. | 品質: 動画ビットレートに指定した割合を掛けます。サイズ: 目標からビットレートを計算し、動画ビットレートの選択は無視されます。 | 质量:视频比特率将乘以该百分比。大小:根据目标大小计算比特率,并忽略视频比特率的选择。 | 품질: 동영상 비트레이트에 해당 비율을 곱해요. 크기: 목표 크기로 비트레이트를 계산하며 동영상 비트레이트 선택은 무시돼요.
Frame rate | フレームレート | 帧率 | 프레임 레이트
Keep | 維持 | 保持 | 유지
Remove | 削除 | 移除 | 제거
Direction | 方向 | 方向 | 방향
To UTF-8 | UTF-8へ | 转为 UTF-8 | UTF-8로
From UTF-8 | UTF-8から | 从 UTF-8 转换 | UTF-8에서
Source encoding | 変換元エンコード | 源编码 | 원본 인코딩
Target encoding | 変換先エンコード | 目标编码 | 대상 인코딩
UTF-8 BOM | UTF-8 BOM | UTF-8 BOM | UTF-8 BOM
No BOM | BOMなし | 无 BOM | BOM 없음
With BOM | BOMあり | 带 BOM | BOM 있음
Line endings | 改行コード | 换行符 | 줄바꿈
Invalid characters | 無効な文字 | 无效字符 | 잘못된 문자
Fail the file | ファイルを失敗扱いにする | 将该文件视为失败 | 파일을 실패로 처리
Replace (?) | 置換(?) | 替换为(?) | 대체(?)
Settings Presets | 設定プリセット | 设置预设 | 설정 프리셋
Output Format & Compression | 出力形式と圧縮 | 输出格式与压缩 | 출력 형식 및 압축
Audio Settings | 音声設定 | 音频设置 | 오디오 설정
Advanced Settings | 詳細設定 | 高级设置 | 고급 설정
Image Settings | 画像設定 | 图片设置 | 이미지 설정
Rotation & Background | 回転と背景 | 旋转与背景 | 회전 및 배경
Video Settings | 動画設定 | 视频设置 | 동영상 설정
Text Settings | テキスト設定 | 文本设置 | 텍스트 설정
Output & Result | 出力と結果 | 输出与结果 | 출력 및 결과
Choose a preset | プリセットを選択 | 选择预设 | 프리셋 선택
Apply | 適用 | 应用 | 적용
Save these settings as a preset | この設定をプリセットとして保存 | 将这些设置保存为预设 | 이 설정을 프리셋으로 저장
Delete preset | プリセットを削除 | 删除预设 | 프리셋 삭제
Preset name | プリセット名 | 预设名称 | 프리셋 이름
Last used settings | 前回使用した設定 | 上次使用的设置 | 마지막으로 사용한 설정
A preset stores all settings of this converter (except the source files and folders). | プリセットには、このコンバーターのすべての設定(ソースファイルとフォルダを除く)が保存されます。 | 预设会保存此转换器的所有设置(源文件和文件夹除外)。 | 프리셋에는 이 변환기의 모든 설정이 저장돼요(원본 파일과 폴더 제외).
Light BGM (M4A 96 kbps) | 軽量BGM(M4A 96 kbps) | 轻量 BGM(M4A 96 kbps) | 가벼운 BGM(M4A 96 kbps)
Small SE (OGG 64 kbps, mono) | 小型SE(OGG 64 kbps、モノラル) | 小体积音效(OGG 64 kbps,单声道) | 작은 효과음(OGG 64 kbps, 모노)
Lean PNG assets (80% quality) | 軽量PNGアセット(品質80%) | 精简 PNG 资源(质量 80%) | 슬림 PNG 에셋(품질 80%)
Light JPG (70% quality) | 軽量JPG(品質70%) | 轻量 JPG(质量 70%) | 가벼운 JPG(품질 70%)
Light 720p video | 軽量720p動画 | 轻量 720p 视频 | 가벼운 720p 동영상
Shift-JIS to UTF-8 | Shift-JISからUTF-8へ | Shift-JIS 转 UTF-8 | Shift-JIS에서 UTF-8로
Tick the extensions to process. | 処理する拡張子にチェックを入れてください。 | 勾选要处理的扩展名。 | 처리할 확장자를 선택하세요.
Create a sample and compare with the source | サンプルを作成して元ファイルと比較 | 创建样本并与源文件对比 | 샘플을 만들어 원본과 비교
Converts one file (the first in the folder) with the current settings and shows a before/after comparison (audio is playable). Nothing is written to your folders; processing all files still starts from the Start Conversion button. | 現在の設定でフォルダ内の最初の1ファイルを変換し、変換前後の比較を表示します(音声は再生できます)。フォルダには何も書き込まれません。すべてのファイルの処理は「変換を開始」ボタンから行います。 | 使用当前设置转换一个文件(文件夹中的第一个),并显示转换前后的对比(音频可播放)。不会向你的文件夹写入任何内容;处理全部文件仍需点击“开始转换”按钮。 | 현재 설정으로 폴더의 첫 번째 파일 하나를 변환하고 변환 전후를 비교해서 보여줘요(오디오는 재생할 수 있어요). 폴더에는 아무것도 저장되지 않으며, 모든 파일 처리는 '변환 시작' 버튼으로 시작해요.
Creating sample… | サンプルを作成中… | 正在创建样本… | 샘플 만드는 중…
Creating a sample, please wait | サンプルを作成中です。お待ちください | 正在创建样本,请稍候 | 샘플을 만드는 중이에요. 잠시만 기다려 주세요
Continue with all files | すべてのファイルで続行 | 继续处理全部文件 | 모든 파일로 계속
Play | 再生 | 播放 | 재생
Replace original files (back up to _backup) | 元のファイルを置き換える(_backupにバックアップ) | 替换原文件(备份到 _backup) | 원본 파일 대체(_backup에 백업)
Folder mode only. Results are written into the source folder. If the result has the same name, the original is backed up first to a _backup folder inside the source folder; if the extension changes, the original is kept and the result is added next to it. | フォルダモード専用です。結果はソースフォルダに書き込まれます。同じ名前の場合は、先に元のファイルをソースフォルダ内の_backupフォルダにバックアップします。拡張子が変わる場合は、元のファイルを残したまま、隣に結果を追加します。 | 仅限文件夹模式。结果会写入源文件夹。如果结果与原文件同名,会先将原文件备份到源文件夹内的 _backup 文件夹;如果扩展名改变,则保留原文件,并在其旁边添加结果文件。 | 폴더 모드 전용이에요. 결과는 원본 폴더에 저장돼요. 결과 파일 이름이 같으면 원본을 먼저 원본 폴더 안의 _backup 폴더에 백업하고, 확장자가 바뀌면 원본은 그대로 두고 그 옆에 결과 파일을 추가해요.
Undo | 元に戻す | 撤销 | 실행 취소
Undone | 元に戻しました | 已撤销 | 취소했어요
Undo failed | 元に戻せませんでした | 撤销失败 | 실행 취소 실패
Show more | もっと見る | 显示更多 | 더 보기
Undo = delete the result file, or restore the original from _backup if the result replaced it. | 元に戻す = 結果ファイルを削除します。結果が元のファイルを置き換えていた場合は_backupから元のファイルを復元します。 | 撤销 = 删除结果文件;如果结果替换了原文件,则从 _backup 恢复原文件。 | 실행 취소 = 결과 파일을 삭제해요. 결과가 원본을 대체했다면 _backup에서 원본을 복원해요.
Conversion finished | 変換が完了しました | 转换完成 | 변환 완료
Conversion failed | 変換に失敗しました | 转换失败 | 변환 실패
Volume normalization | 音量の正規化 | 音量标准化 | 볼륨 노멀라이즈
Off | オフ | 关 | 끔
Light | 弱 | 轻 | 약
Medium | 中 | 中 | 중
Loud | 強 | 响 | 강
Evens out loudness across files (RMS): Light -20 dB, Medium -16 dB, Loud -12 dB. Peaks are limited to -1 dB to avoid clipping. Needs an extra scan pass, so it takes a bit longer. | ファイル間の音量を均一にします(RMS): 弱 -20 dB、中 -16 dB、強 -12 dB。クリッピングを防ぐためピークは -1 dB に制限されます。追加のスキャンが必要なため、少し時間がかかります。 | 使各文件的响度保持一致(RMS):轻 -20 dB,中 -16 dB,响 -12 dB。为避免削波,峰值限制在 -1 dB。需要额外扫描一遍,因此耗时会稍长。 | 파일 간 음량을 고르게 맞춰요(RMS): 약 -20 dB, 중 -16 dB, 강 -12 dB. 클리핑을 막기 위해 피크는 -1 dB로 제한돼요. 추가 스캔이 필요해서 시간이 조금 더 걸려요.
Trim silence at start and end | 先頭と末尾の無音をカット | 裁剪开头和结尾的静音 | 시작과 끝의 무음 자르기
Keep OGG loop tags (LOOPSTART/LOOPLENGTH) | OGGのループタグを維持(LOOPSTART/LOOPLENGTH) | 保留 OGG 循环标签(LOOPSTART/LOOPLENGTH) | OGG 루프 태그 유지(LOOPSTART/LOOPLENGTH)
Loop tags are read from the source OGG, adjusted for sample rate/trimming, and written to the result OGG. The tags are written to both OGG (Vorbis) and OGG (Opus). The built-in RPGM MV/MZ loop reader understands OGG Vorbis tags, so choose OGG (Vorbis) for looping BGM. | ループタグは元のOGGから読み取り、サンプリングレートやトリミングに合わせて調整し、変換後のOGGに書き込みます。タグはOGG(Vorbis)とOGG(Opus)の両方に書き込まれます。RPGツクール MV/MZ内蔵のループ読み込みはOGG Vorbisのタグに対応しているため、ループするBGMにはOGG(Vorbis)を選んでください。 | 循环标签会从源 OGG 读取,根据采样率/裁剪进行调整,并写入结果 OGG。标签会同时写入 OGG (Vorbis) 和 OGG (Opus)。RPG Maker MV/MZ 内置的循环读取器只能识别 OGG Vorbis 标签,因此循环播放的 BGM 请选择 OGG (Vorbis)。 | 루프 태그는 원본 OGG에서 읽어 샘플레이트/자르기에 맞게 조정한 뒤 결과 OGG에 저장해요. 태그는 OGG(Vorbis)와 OGG(Opus) 모두에 저장돼요. RPG Maker MV/MZ의 내장 루프 리더는 OGG Vorbis 태그만 인식하므로, 반복 재생하는 BGM은 OGG(Vorbis)를 선택하세요.
Round to a multiple of | 次の倍数に丸める | 取整为以下数值的倍数 | 다음 배수로 맞추기
For tilesets/spritesheets so sizes stay grid-aligned (e.g. 48). Applies to Percent and Size resolution; never exceeds the source size. | タイルセットやスプライトシートのサイズをグリッドに揃えるための設定です(例: 48)。パーセントとサイズの解像度に適用され、元のサイズを超えることはありません。 | 用于图块集/精灵图,使尺寸与网格对齐(例如 48)。适用于百分比和大小两种分辨率方式;不会超过源尺寸。 | 타일셋/스프라이트시트의 크기를 그리드에 맞추기 위한 설정이에요(예: 48). 퍼센트와 크기 해상도에 적용되며 원본 크기를 넘지 않아요.
Dithering when PNG colors are reduced | PNGの減色時にディザリング | 减少 PNG 颜色时使用抖动 | PNG 색상 줄일 때 디더링
Output Folder | 出力フォルダ | 输出文件夹 | 출력 폴더
Overwrite Originals | 元ファイルを上書き | 覆盖原文件 | 원본 덮어쓰기
Back up to the _backup folder | _backupフォルダにバックアップ | 备份到 _backup 文件夹 | _backup 폴더에 백업
Results are written straight into the source folder. If a result has the same name as the original, the original is overwritten; when ticked, the original is first copied to a _backup folder inside the source folder (clashing names get (1)). If the extension changes, the original is kept and the result is added next to it. | 結果はソースフォルダに直接書き込まれます。結果が元のファイルと同じ名前の場合は元のファイルが上書きされます。チェックを入れると、先に元のファイルをソースフォルダ内の_backupフォルダにコピーします(名前が重複する場合は(1)が付きます)。拡張子が変わる場合は、元のファイルを残したまま、隣に結果を追加します。 | 结果会直接写入源文件夹。如果结果与原文件同名,则原文件会被覆盖;勾选后,会先将原文件复制到源文件夹内的 _backup 文件夹(重名时会加上 (1))。如果扩展名改变,则保留原文件,并在其旁边添加结果文件。 | 결과는 원본 폴더에 바로 저장돼요. 결과 파일 이름이 원본과 같으면 원본이 덮어써져요. 체크하면 원본을 먼저 원본 폴더 안의 _backup 폴더에 복사해요(이름이 겹치면 (1)이 붙어요). 확장자가 바뀌면 원본은 그대로 두고 그 옆에 결과 파일을 추가해요.
Warning: without a backup the originals are overwritten permanently and the result cannot be undone. Make sure you have another copy. | 警告: バックアップなしでは元のファイルが完全に上書きされ、元に戻せません。必ず別のコピーを保管してください。 | 警告:不备份的话,原文件会被永久覆盖,且无法撤销。请确保你有另一份副本。 | 경고: 백업하지 않으면 원본이 영구적으로 덮어써지며 되돌릴 수 없어요. 다른 사본이 있는지 꼭 확인하세요.
Create comparison sample | 比較サンプルを作成 | 创建对比样本 | 비교 샘플 만들기
No backup | バックアップなし | 不备份 | 백업 안 함
Silence at the start and end (below about -50 dBFS) is removed, leaving a 10 ms margin. The middle of the file is untouched. Handy for SE/BGM with blank gaps; the result gets shorter and loop tags are adjusted automatically. | 先頭と末尾の無音(約 -50 dBFS 未満)を、10 ms の余白を残して削除します。ファイルの中間部分は変更されません。無音の隙間があるSEやBGMに便利です。結果は短くなり、ループタグは自動で調整されます。 | 会移除开头和结尾的静音(低于约 -50 dBFS),并保留 10 ms 的余量。文件中间部分不受影响。适合处理首尾带空白的音效/BGM;结果会变短,循环标签会自动调整。 | 시작과 끝의 무음(약 -50 dBFS 미만)을 10 ms 여유를 남기고 제거해요. 파일 중간 부분은 그대로예요. 앞뒤에 빈 구간이 있는 효과음/BGM에 편리해요. 결과가 짧아지고 루프 태그는 자동으로 조정돼요.
When PNG colors are reduced (Quality below 100%), color error is spread to neighboring pixels so gradients (skin, sky, shadows) look smooth without color bands, but slightly noisy and the file can be several times larger than without dithering. Good for gradient images; leave it off for pixel art. | PNGの色数を減らす場合(品質が100%未満)、色の誤差を周囲のピクセルに拡散させることで、グラデーション(肌、空、影など)を色の段差なく滑らかに見せます。ただし少しノイズが乗り、ディザリングなしの場合の数倍のファイルサイズになることがあります。グラデーションのある画像に向いており、ピクセルアートではオフにしてください。 | 减少 PNG 颜色数时(质量低于 100%),会将色彩误差扩散到相邻像素,使渐变(肤色、天空、阴影)平滑、没有色带,但会略有噪点,文件可能比不使用抖动时大数倍。适合带渐变的图片;像素画请保持关闭。 | PNG 색상 수를 줄일 때(품질 100% 미만) 색상 오차를 주변 픽셀로 퍼뜨려 그라데이션(피부, 하늘, 그림자)이 색 띠 없이 부드럽게 보이지만, 약간 노이즈가 생기고 디더링을 쓰지 않을 때보다 파일이 몇 배 커질 수 있어요. 그라데이션이 있는 이미지에 적합하며, 픽셀 아트에는 꺼 두세요.
Special Asset Converter | 特殊アセットコンバーター | 特殊资源转换器 | 특수 에셋 변환기
Convert game-engine assets: Circus audio (.pcm) to OGG/WAV and Circus images (.crx) to PNG | ゲームエンジンのアセットを変換します: Circusの音声(.pcm)をOGG/WAVに、Circusの画像(.crx)をPNGに | 转换游戏引擎资源:将 Circus 音频(.pcm)转为 OGG/WAV,将 Circus 图片(.crx)转为 PNG | 게임 엔진 에셋을 변환해요: Circus 오디오(.pcm)를 OGG/WAV로, Circus 이미지(.crx)를 PNG로
Circus (Adv32) assets | Circus(Adv32)アセット | Circus(Adv32)资源 | Circus(Adv32) 에셋
Result of .pcm audio | .pcm音声の変換結果 | .pcm 音频的结果 | .pcm 오디오 결과
Always WAV | 常にWAV | 始终为 WAV | 항상 WAV
Check Compatibility | 互換性を確認 | 检查兼容性 | 호환성 확인
Compatibility Checker | 互換性チェッカー | 兼容性检查器 | 호환성 검사기
Target engine | 対象エンジン | 目标引擎 | 대상 엔진
Checking result files… | 結果ファイルを確認中… | 正在检查结果文件… | 결과 파일 확인 중…
No problems detected for this engine. | このエンジンでは問題は検出されませんでした。 | 未检测到此引擎存在问题。 | 이 엔진에서는 문제가 발견되지 않았어요.
Info | 情報 | 信息 | 정보
Warning | 警告 | 警告 | 경고
Incompatible | 非互換 | 不兼容 | 호환되지 않음
Repack this folder | このフォルダを再パック | 重新打包此文件夹 | 이 폴더 다시 묶기
RPGM MV/MZ: OGG 96 kbps | RPGツクール MV/MZ: OGG 96 kbps | RPGM MV/MZ:OGG 96 kbps | RPGM MV/MZ: OGG 96 kbps
RPGM MV/MZ: M4A 96 kbps (OGG counterpart) | RPGツクール MV/MZ: M4A 96 kbps(OGGの対応ファイル) | RPGM MV/MZ:M4A 96 kbps(对应 OGG 的配套文件) | RPGM MV/MZ: M4A 96 kbps(OGG 짝 파일)
Ren'Py: OGG 128 kbps | Ren'Py: OGG 128 kbps | Ren'Py:OGG 128 kbps | Ren'Py: OGG 128 kbps
Kirikiri: WAV 44100 Hz (safest) | 吉里吉里: WAV 44100 Hz(最も安全) | Kirikiri:WAV 44100 Hz(最稳妥) | Kirikiri: WAV 44100 Hz(가장 안전)
RPGM tileset: PNG 50%, multiple of 48 | RPGツクール タイルセット: PNG 50%、48の倍数 | RPGM 图块集:PNG 50%,48 的倍数 | RPGM 타일셋: PNG 50%, 48의 배수
Ren'Py: WEBP 85% (lean) | Ren'Py: WEBP 85%(軽量) | Ren'Py:WEBP 85%(精简) | Ren'Py: WEBP 85%(슬림)
Kirikiri: full-quality PNG | 吉里吉里: 高品質PNG | Kirikiri:原画质 PNG | Kirikiri: 최고 품질 PNG
RPGM MV/MZ: WebM 720p | RPGツクール MV/MZ: WebM 720p | RPGM MV/MZ:WebM 720p | RPGM MV/MZ: WebM 720p
RPGM mobile/iOS: MP4 H.264 720p | RPGツクール モバイル/iOS: MP4 H.264 720p | RPGM 移动端/iOS:MP4 H.264 720p | RPGM 모바일/iOS: MP4 H.264 720p
Plugin Manager | プラグインマネージャー | 插件管理器 | 플러그인 관리자
Scan RPG Maker MV/MZ plugins and manage plugins.js | RPG Maker MV/MZのプラグインをスキャンし、plugins.jsを管理します | 扫描 RPG Maker MV/MZ 插件并管理 plugins.js | RPG Maker MV/MZ 플러그인을 스캔하고 plugins.js를 관리합니다
Plugin Scanner | プラグインスキャナー | 插件扫描器 | 플러그인 스캐너
Find editable texts and values in plugin files | プラグインファイル内の編集可能なテキストと値を検索します | 查找插件文件中可编辑的文本和数值 | 플러그인 파일에서 편집 가능한 텍스트와 값을 찾습니다
Plugin.js Editor | Plugin.jsエディター | Plugin.js 编辑器 | Plugin.js 편집기
Enable/disable plugins and add new ones | プラグインの有効化/無効化と新規追加を行います | 启用/禁用插件并添加新插件 | 플러그인을 활성화/비활성화하고 새로 추가합니다
Plugin folder (js/plugins) | プラグインフォルダー (js/plugins) | 插件文件夹 (js/plugins) | 플러그인 폴더 (js/plugins)
Scan | スキャン | 扫描 | 스캔
No plugin (.js) files in this folder. | このフォルダーにプラグイン (.js) ファイルがありません。 | 此文件夹中没有插件 (.js) 文件。 | 이 폴더에 플러그인 (.js) 파일이 없어요.
No non-ASCII texts or editable default values were found. | 非ASCIIのテキストや編集可能な既定値は見つかりませんでした。 | 未找到非 ASCII 文本或可编辑的默认值。 | 비ASCII 텍스트나 편집 가능한 기본값을 찾지 못했어요.
Plugin information | プラグイン情報 | 插件信息 | 플러그인 정보
Plugin Editing Area | プラグイン編集エリア | 插件编辑区 | 플러그인 편집 영역
All | すべて | 全部 | 전체
Texts | テキスト | 文本 | 텍스트
Values | 値 | 数值 | 값
Search… | 検索… | 搜索… | 검색…
Text in code | コード内のテキスト | 代码中的文本 | 코드 내 텍스트
Annotation text | 注釈テキスト | 注释文本 | 주석 텍스트
Default value in code | コード内の既定値 | 代码中的默认值 | 코드 내 기본값
Text parameter (plugins.js) | テキストパラメーター (plugins.js) | 文本参数 (plugins.js) | 텍스트 매개변수 (plugins.js)
Value parameter (plugins.js) | 値パラメーター (plugins.js) | 数值参数 (plugins.js) | 값 매개변수 (plugins.js)
@default value | @default値 | @default 值 | @default 값
Translation | 翻訳 | 翻译 | 번역
New value | 新しい値 | 新值 | 새 값
Current value | 現在の値 | 当前值 | 현재 값
Output folder | 出力フォルダー | 输出文件夹 | 출력 폴더
Apply | 適用 | 应用 | 적용
Pick an output folder first. | 先に出力フォルダーを選択してください。 | 请先选择输出文件夹。 | 먼저 출력 폴더를 선택해 주세요.
No changes to apply yet. | 適用する変更はまだありません。 | 暂无可应用的更改。 | 아직 적용할 변경 사항이 없어요.
Only changed plugin files are written to the output folder (same file names). | 変更されたプラグインファイルのみが出力フォルダーに書き込まれます (ファイル名は同じ)。 | 仅将已更改的插件文件写入输出文件夹 (文件名相同)。 | 변경된 플러그인 파일만 출력 폴더에 기록돼요 (파일 이름은 동일).
Upload plugins.js | plugins.jsをアップロード | 上传 plugins.js | plugins.js 업로드
Upload New Plugin (.js) | 新しいプラグイン (.js) をアップロード | 上传新插件 (.js) | 새 플러그인 (.js) 업로드
Add Plugin | プラグインを追加 | 添加插件 | 플러그인 추가
Save Plugin.js | Plugin.jsを保存 | 保存 Plugin.js | Plugin.js 저장
Plugin List | プラグイン一覧 | 插件列表 | 플러그인 목록
Search plugin | プラグインを検索 | 搜索插件 | 플러그인 검색
Enabled | 有効 | 已启用 | 활성화됨
Disabled | 無効 | 已禁用 | 비활성화됨
NEW | 新規 | 新增 | 신규
No matching plugins. | 一致するプラグインがありません。 | 没有匹配的插件。 | 일치하는 플러그인이 없어요.
Tap a plugin to see its information and change its status. | プラグインをタップすると、情報の表示と状態の変更ができます。 | 点按插件可查看其信息并更改状态。 | 플러그인을 탭하면 정보를 보고 상태를 변경할 수 있어요.
Name | 名前 | 名称 | 이름
Status | 状態 | 状态 | 상태
Description | 説明 | 说明 | 설명
Parameters | パラメーター | 参数 | 매개변수
(none) | (なし) | (无) | (없음)
Load order | 読み込み順 | 加载顺序 | 로드 순서
Upload plugins.js first. | 先にplugins.jsをアップロードしてください。 | 请先上传 plugins.js。 | 먼저 plugins.js를 업로드해 주세요.
Unsaved changes will be lost. | 保存していない変更は失われます。 | 未保存的更改将会丢失。 | 저장하지 않은 변경 사항은 사라져요.
Leave | 離れる | 离开 | 나가기
Stay | 留まる | 留下 | 머물기
There are unsaved changes. | 保存されていない変更があります。 | 有未保存的更改。 | 저장되지 않은 변경 사항이 있어요.
Plugin Checker | プラグインチェッカー | 插件检查器 | 플러그인 검사기
Check plugins.js vs the plugin folder: missing, duplicate, dependencies, order | plugins.jsとプラグインフォルダーを照合: 欠落、重複、依存関係、順序 | 对比 plugins.js 与插件文件夹: 缺失、重复、依赖关系、顺序 | plugins.js와 플러그인 폴더 비교: 누락, 중복, 종속성, 순서
plugins.js Comparer | plugins.js比較 | plugins.js 对比器 | plugins.js 비교기
Compare two plugins.js: added, removed, status, parameters, order | 2つのplugins.jsを比較: 追加、削除、状態、パラメーター、順序 | 比较两个 plugins.js: 新增、删除、状态、参数、顺序 | plugins.js 두 개를 비교: 추가, 삭제, 상태, 매개변수, 순서
View all information | すべての情報を表示 | 查看全部信息 | 모든 정보 보기
plugins.js (optional, for plugin parameters) | plugins.js (任意、プラグインパラメーター用) | plugins.js (可选，用于插件参数) | plugins.js (선택 사항, 플러그인 매개변수용)
If picked (or found in the game/js folder), text and value parameters in plugins.js are scanned and editable too. Parameters holding nested JSON are skipped. | 選択した場合 (またはgame/jsフォルダーで見つかった場合)、plugins.js内のテキストと値のパラメーターもスキャンされ、編集できます。ネストされたJSONを含むパラメーターはスキップされます。 | 如果已选择 (或在 game/js 文件夹中找到)，也会扫描并可编辑 plugins.js 中的文本和数值参数。包含嵌套 JSON 的参数将被跳过。 | 선택하면 (또는 game/js 폴더에서 발견되면) plugins.js의 텍스트 및 값 매개변수도 스캔되어 편집할 수 있어요. 중첩된 JSON이 들어 있는 매개변수는 건너뜁니다.
Export / import plugin translation | プラグイン翻訳のエクスポート/インポート | 导出/导入插件翻译 | 플러그인 번역 내보내기/가져오기
Export plugin texts | プラグインテキストをエクスポート | 导出插件文本 | 플러그인 텍스트 내보내기
Import translation | 翻訳をインポート | 导入翻译 | 번역 가져오기
No translation matched the scanned texts. | スキャンしたテキストに一致する翻訳がありませんでした。 | 没有与已扫描文本匹配的翻译。 | 스캔한 텍스트와 일치하는 번역이 없어요.
There are no texts to export. | エクスポートするテキストがありません。 | 没有可导出的文本。 | 내보낼 텍스트가 없어요.
If a file already exists in the output folder, a .bak copy is kept before it is overwritten. | 出力フォルダーに同名のファイルがある場合、上書き前に.bakのコピーが保存されます。 | 如果输出文件夹中已存在同名文件，覆盖前会保留一份 .bak 副本。 | 출력 폴더에 같은 파일이 이미 있으면 덮어쓰기 전에 .bak 사본을 보관해요.
Plugin folder (optional, for warnings) | プラグインフォルダー (任意、警告用) | 插件文件夹 (可选，用于警告) | 플러그인 폴더 (선택 사항, 경고용)
Up | 上へ | 上移 | 위로
Down | 下へ | 下移 | 아래로
Remove | 削除 | 移除 | 제거
Remove from list? | リストから削除しますか? | 从列表中移除? | 목록에서 제거할까요?
Save profile | プロファイルを保存 | 保存配置方案 | 프로필 저장
Apply profile | プロファイルを適用 | 应用配置方案 | 프로필 적용
Profile name | プロファイル名 | 方案名称 | 프로필 이름
No saved profiles yet. | 保存されたプロファイルはまだありません。 | 暂无已保存的配置方案。 | 저장된 프로필이 아직 없어요.
A profile stores each plugin's enabled/disabled state; applying it only changes plugins with the same name. | プロファイルは各プラグインの有効/無効の状態を保存します。適用すると、同じ名前のプラグインのみが変更されます。 | 配置方案会保存每个插件的启用/禁用状态；应用时只会更改名称相同的插件。 | 프로필은 각 플러그인의 활성화/비활성화 상태를 저장해요. 적용하면 이름이 같은 플러그인만 변경됩니다.
Plugin Warnings | プラグインの警告 | 插件警告 | 플러그인 경고
View Parameters | パラメーターを表示 | 查看参数 | 매개변수 보기
Plugin Parameters | プラグインパラメーター | 插件参数 | 플러그인 매개변수
Search parameter | パラメーターを検索 | 搜索参数 | 매개변수 검색
Done | 完了 | 完成 | 완료
Warnings | 警告 | 警告 | 경고
No problems found. | 問題は見つかりませんでした。 | 未发现问题。 | 문제를 찾지 못했어요.
Warnings for this plugin | このプラグインの警告 | 此插件的警告 | 이 플러그인의 경고
ERROR | エラー | 错误 | 오류
WARNING | 警告 | 警告 | 경고
INFO | 情報 | 信息 | 정보
plugins.js | plugins.js | plugins.js | plugins.js
Check | チェック | 检查 | 검사
Pick plugins.js and the plugin folder. Checked: duplicate names, registered plugins whose file is missing, unregistered files, empty files, and dependencies (@base, @orderAfter, @orderBefore) with their order. | plugins.jsとプラグインフォルダーを選択してください。チェック内容: 名前の重複、ファイルが欠落している登録済みプラグイン、未登録ファイル、空ファイル、依存関係 (@base、@orderAfter、@orderBefore) とその順序。 | 请选择 plugins.js 和插件文件夹。检查内容: 重名、已登记但文件缺失的插件、未登记的文件、空文件，以及依赖关系 (@base、@orderAfter、@orderBefore) 及其顺序。 | plugins.js와 플러그인 폴더를 선택해 주세요. 검사 항목: 이름 중복, 파일이 없는 등록된 플러그인, 등록되지 않은 파일, 빈 파일, 종속성 (@base, @orderAfter, @orderBefore)과 그 순서.
Pick plugins.js first. | 先にplugins.jsを選択してください。 | 请先选择 plugins.js。 | 먼저 plugins.js를 선택해 주세요.
plugins.js A (old) | plugins.js A (旧) | plugins.js A (旧) | plugins.js A (이전)
plugins.js B (new) | plugins.js B (新) | plugins.js B (新) | plugins.js B (최신)
Pick two plugins.js files; the result appears automatically. | plugins.jsを2つ選択すると、結果が自動的に表示されます。 | 选择两个 plugins.js 后，结果会自动显示。 | plugins.js 두 개를 선택하면 결과가 자동으로 표시돼요.
Both plugins.js are equivalent (no differences). | 2つのplugins.jsは同等です (差異なし)。 | 两个 plugins.js 等效 (无差异)。 | 두 plugins.js는 동일해요 (차이 없음).
Added | 追加 | 新增 | 추가됨
Removed | 削除 | 已删除 | 삭제됨
Params | パラメーター | 参数 | 매개변수
Order | 順序 | 顺序 | 순서
ADDED | 追加 | 新增 | 추가됨
REMOVED | 削除 | 已删除 | 삭제됨
STATUS | 状態 | 状态 | 상태
PARAMETERS | パラメーター | 参数 | 매개변수
ORDER | 順序 | 顺序 | 순서
plugins.js (position, status, name): | plugins.js (位置、状態、名前): | plugins.js (位置、状态、名称): | plugins.js (위치, 상태, 이름):
Parameter differences: | パラメーターの差異: | 参数差异: | 매개변수 차이:
Plugin header: | プラグインヘッダー: | 插件头部: | 플러그인 헤더:
May be intentional | 問題ない可能性あり | 可能是有意为之 | 의도된 것일 수 있음
Almost certainly a problem | ほぼ確実に問題あり | 几乎可以肯定有问题 | 거의 확실히 문제임
Tap to see the code | タップしてコードを表示 | 点按查看代码 | 탭하여 코드 보기
Ignore… | 無視… | 忽略… | 무시…
Ignore finding | 検出項目を無視 | 忽略此项 | 검출 항목 무시
Ignored findings no longer show in later scans (applies to all games). Restore them with the Ignored button. | 無視した項目は以降のスキャンに表示されません (すべてのゲームに適用)。「無視済み」ボタンで元に戻せます。 | 被忽略的项目不会再出现在之后的扫描中 (适用于所有游戏)。可通过“已忽略”按钮恢复。 | 무시한 항목은 이후 스캔에 표시되지 않아요 (모든 게임에 적용). '무시됨' 버튼으로 복원할 수 있어요.
Ignore for this plugin | このプラグインで無視 | 对此插件忽略 | 이 플러그인에서 무시
Ignore this type for all plugins | この種類をすべてのプラグインで無視 | 对所有插件忽略此类型 | 이 유형을 모든 플러그인에서 무시
Ignored findings | 無視した検出項目 | 已忽略的项目 | 무시한 검출 항목
Restore all | すべて復元 | 全部恢复 | 모두 복원
Restore | 復元 | 恢复 | 복원
all plugins | すべてのプラグイン | 所有插件 | 모든 플러그인
Search plugin name or text | プラグイン名またはテキストを検索 | 搜索插件名称或文本 | 플러그인 이름 또는 텍스트 검색
Duplicate name | 名前の重複 | 名称重复 | 이름 중복
Missing file (enabled) | ファイル欠落 (有効) | 文件缺失 (已启用) | 파일 없음 (활성화)
Missing file (disabled) | ファイル欠落 (無効) | 文件缺失 (已禁用) | 파일 없음 (비활성화)
Empty file | 空ファイル | 空文件 | 빈 파일
Unregistered | 未登録 | 未登记 | 미등록
@base missing | @baseがありません | 缺少 @base | @base 없음
@base disabled | @baseが無効 | @base 已禁用 | @base 비활성화됨
@base order | @baseの順序 | @base 顺序 | @base 순서
@orderAfter order | @orderAfterの順序 | @orderAfter 顺序 | @orderAfter 순서
@orderBefore order | @orderBeforeの順序 | @orderBefore 顺序 | @orderBefore 순서
Enabled but its .js file is missing from the plugin folder (the game may crash). | 有効ですが、.jsファイルがプラグインフォルダーにありません (ゲームがクラッシュする可能性があります)。 | 已启用，但其 .js 文件不在插件文件夹中 (游戏可能崩溃)。 | 활성화되어 있지만 .js 파일이 플러그인 폴더에 없어요 (게임이 중단될 수 있어요).
Disabled and its .js file is not in the plugin folder yet (maybe an unfinished plugin). | 無効で、.jsファイルもまだプラグインフォルダーにありません (未完成のプラグインかもしれません)。 | 已禁用，且其 .js 文件尚未放入插件文件夹 (可能是未完成的插件)。 | 비활성화되어 있고 .js 파일도 아직 플러그인 폴더에 없어요 (미완성 플러그인일 수 있어요).
The plugin file is empty (0 bytes). | プラグインファイルが空です (0バイト)。 | 插件文件为空 (0 字节)。 | 플러그인 파일이 비어 있어요 (0바이트).
The file is in the plugin folder but not registered in plugins.js. | ファイルはプラグインフォルダーにありますが、plugins.jsに登録されていません。 | 文件在插件文件夹中，但未在 plugins.js 中登记。 | 파일이 플러그인 폴더에 있지만 plugins.js에 등록되어 있지 않아요.
Copy this page | このページをコピー | 复制此页面 | 이 페이지 복사
Copied | コピーしました | 已复制 | 복사됨
Loading audio… | 音声を読み込み中… | 正在加载音频… | 오디오 로드 중…
Audio cannot be played | 音声を再生できません | 无法播放音频 | 오디오를 재생할 수 없어요
Play | 再生 | 播放 | 재생
Pause | 一時停止 | 暂停 | 일시정지
Processing... | 処理中... | 处理中... | 처리 중...
Output file name | 出力ファイル名 | 输出文件名 | 출력 파일 이름
Translate text | テキストを翻訳 | 翻译文本 | 텍스트 번역
Check packing integrity | パッキングの整合性を確認 | 检查打包完整性 | 패킹 무결성 확인
Packing integrity | パッキングの整合性 | 打包完整性 | 패킹 무결성
Source files checked | 確認したソースファイル | 已检查的源文件 | 확인한 원본 파일
Size matches source | ソースとサイズが一致 | 大小与源文件一致 | 원본과 크기 일치
Size differs | サイズが異なる | 大小不一致 | 크기가 다름
Missing from archive / unreadable | アーカイブに存在しない / 読み取り不可 | 归档中缺失 / 无法读取 | 아카이브에 없음 / 읽을 수 없음
Content matches source (CRC32) | 内容がソースと一致 (CRC32) | 内容与源文件一致 (CRC32) | 내용이 원본과 일치 (CRC32)
The archive matches the source folder | アーカイブはソースフォルダーと一致しています | 归档与源文件夹一致 | 아카이브가 원본 폴더와 일치해요
The archive is read back and every file is extracted to memory and compared to the source folder. For formats that rebuild their contents (e.g. Scene.pck) the extracted files are compared, not the archive bytes. | アーカイブを読み戻し、すべてのファイルをメモリに展開してソースフォルダーと比較します。内容を再構築する形式 (例: Scene.pck) では、アーカイブのバイト列ではなく展開されたファイルを比較します。 | 读回归档，将所有文件解压到内存并与源文件夹比较。对于会重建内容的格式 (例如 Scene.pck)，比较的是解压后的文件，而非归档的字节。 | 아카이브를 다시 읽어 모든 파일을 메모리에 추출한 뒤 원본 폴더와 비교해요. 내용을 다시 구성하는 형식 (예: Scene.pck)은 아카이브 바이트가 아니라 추출된 파일을 비교합니다.
View contents as text | 内容をテキストで表示 | 以文本查看内容 | 내용을 텍스트로 보기
File contents as text | テキストとしてのファイル内容 | 文件内容 (文本) | 파일 내용 (텍스트)
(first 1 MB shown) | (先頭1 MBを表示) | (显示前 1 MB) | (처음 1 MB만 표시)
The file could not be read | ファイルを読み込めませんでした | 无法读取该文件 | 파일을 읽을 수 없어요
Online Translation | オンライン翻訳 | 在线翻译 | 온라인 번역
Translation Service | 翻訳サービス | 翻译服务 | 번역 서비스
Translation Languages | 翻訳言語 | 翻译语言 | 번역 언어
Translation Rows | 翻訳行数 | 翻译行数 | 번역 행 수
Use API Key | APIキーを使用 | 使用 API 密钥 | API 키 사용
Bilibili model | Bilibiliモデル | Bilibili 模型 | Bilibili 모델
Refill | 再入力 | 重新填充 | 다시 채우기
Lingva servers (one per line) | Lingvaサーバー(1行に1つ) | Lingva 服务器(每行一个) | Lingva 서버(한 줄에 하나)
Provider | プロバイダー | 提供商 | 제공업체
API Key | APIキー | API 密钥 | API 키
Provider URL | プロバイダーURL | 提供商 URL | 제공업체 URL
Base URL of an OpenAI-style API, e.g. https://host/v1 (without /chat/completions). | OpenAI形式APIのベースURL(例: https://host/v1、/chat/completionsは含めない)。 | OpenAI 风格 API 的基础 URL,例如 https://host/v1(不含 /chat/completions)。 | OpenAI 방식 API의 기본 URL입니다. 예: https://host/v1 (/chat/completions 제외)
Load model list | モデル一覧を読み込む | 加载模型列表 | 모델 목록 불러오기
Loading models… | モデルを読み込み中… | 正在加载模型… | 모델 불러오는 중…
Enter the API key, then press Load model list. | APIキーを入力してから「モデル一覧を読み込む」を押してください。 | 请输入 API 密钥,然后点击“加载模型列表”。 | API 키를 입력한 후 '모델 목록 불러오기'를 누르세요.
Model (manual, if no list is available) | モデル(一覧がない場合は手動入力) | 模型(无可用列表时手动输入) | 모델(목록이 없으면 직접 입력)
"""
    const val T2 = """
Auto-fill parts that are not translated with the Original Text | 翻訳されていない部分を原文で自動補完 | 用原文自动填充未翻译的部分 | 번역되지 않은 부분을 원문으로 자동 채우기
Automatic (Auto) | 自動(Auto) | 自动(Auto) | 자동(Auto)
Show | 表示 | 显示 | 표시
Hide | 非表示 | 隐藏 | 숨기기
Model | モデル | 模型 | 모델
Enter a model name your account can use. | アカウントで使用できるモデル名を入力してください。 | 请输入您的账号可使用的模型名称。 | 계정에서 사용할 수 있는 모델 이름을 입력하세요.
Google Cloud Translation API (v2). The glossary uses placeholders. | Google Cloud Translation API (v2)。用語集はプレースホルダーを使用します。 | Google Cloud Translation API (v2)。术语表使用占位符。 | Google Cloud Translation API (v2). 용어집은 플레이스홀더를 사용합니다.
OpenRouter: one key for many models (model names look like provider/model; some end with :free). The glossary is sent in the prompt. | OpenRouter: 1つのキーで多数のモデルを利用できます(モデル名は provider/model の形式で、末尾が :free のものもあります)。用語集はプロンプトで送信されます。 | OpenRouter:一个密钥即可使用多种模型(模型名称形如 provider/model,部分以 :free 结尾)。术语表通过提示词发送。 | OpenRouter: 키 하나로 여러 모델을 사용할 수 있습니다(모델 이름은 provider/model 형식이며 일부는 :free로 끝납니다). 용어집은 프롬프트로 전송됩니다.
DeepL API (suffix :fx = Free plan). The glossary uses placeholders. | DeepL API(接尾辞 :fx = 無料プラン)。用語集はプレースホルダーを使用します。 | DeepL API(后缀 :fx = 免费套餐)。术语表使用占位符。 | DeepL API(접미사 :fx = 무료 요금제). 용어집은 플레이스홀더를 사용합니다.
Language model (LLM): the glossary is sent in the prompt. | 言語モデル(LLM): 用語集はプロンプトで送信されます。 | 语言模型(LLM):术语表通过提示词发送。 | 언어 모델(LLM): 용어집은 프롬프트로 전송됩니다.
Source Language | 原文の言語 | 源语言 | 원문 언어
Target Language | 翻訳先の言語 | 目标语言 | 번역 대상 언어
Translate | 翻訳 | 翻译 | 번역
Empty fields | 空の欄 | 空白字段 | 빈 항목
This page | このページ | 当前页 | 이 페이지
All rows | すべての行 | 所有行 | 모든 행
All rows and This page overwrite existing entries (Undo can revert). | 「すべての行」と「このページ」は既存の内容を上書きします(元に戻すで復元できます)。 | “所有行”和“当前页”会覆盖现有内容(可通过撤销恢复)。 | '모든 행'과 '이 페이지'는 기존 내용을 덮어씁니다(실행 취소로 되돌릴 수 있습니다).
Online Translation Glossary | オンライン翻訳の用語集 | 在线翻译术语表 | 온라인 번역 용어집
Source text | 原文 | 原文 | 원문
Translation | 翻訳 | 译文 | 번역
Copy from editor glossary | エディターの用語集からコピー | 从编辑器术语表复制 | 편집기 용어집에서 복사
No terms yet. | 用語はまだありません。 | 暂无术语。 | 아직 용어가 없습니다.
Translation Quality (LLM) | 翻訳品質(LLM) | 翻译质量(LLM) | 번역 품질(LLM)
Language style | 文体 | 语言风格 | 문체
Natural | 自然 | 自然 | 자연스럽게
Casual | カジュアル | 口语 | 캐주얼
Formal | フォーマル | 正式 | 격식체
Literal (close to the source) | 直訳(原文に忠実) | 直译(贴近原文) | 직역(원문에 충실)
Context notes (optional) | 文脈メモ(任意) | 上下文说明(可选) | 문맥 메모(선택)
E.g. genre, mood, relationships, how names are addressed. Sent to the model with every request. | 例: ジャンル、雰囲気、人間関係、名前の呼び方。リクエストごとにモデルへ送信されます。 | 例如:题材、氛围、人物关系、称呼方式。每次请求都会发送给模型。 | 예: 장르, 분위기, 인물 관계, 이름 호칭. 모든 요청과 함께 모델로 전송됩니다.
Include the 3 previous rows as context | 前の3行を文脈として含める | 包含前 3 行作为上下文 | 이전 3행을 문맥으로 포함
Helps pronouns and conversation tone; uses more tokens. | 代名詞や会話のトーンの精度が上がりますが、トークンを多く消費します。 | 有助于处理代词和对话语气,但会消耗更多 token。 | 대명사와 대화 어조를 파악하는 데 도움이 되지만 토큰을 더 많이 사용합니다.
This section only applies to LLM providers (Gemini, OpenAI, DeepSeek, Claude, Groq, Custom). | この設定はLLMプロバイダー(Gemini、OpenAI、DeepSeek、Claude、Groq、カスタム)にのみ適用されます。 | 本部分仅适用于 LLM 提供商(Gemini、OpenAI、DeepSeek、Claude、Groq、自定义)。 | 이 항목은 LLM 제공업체(Gemini, OpenAI, DeepSeek, Claude, Groq, 사용자 지정)에만 적용됩니다.
Test connection | 接続テスト | 测试连接 | 연결 테스트
Testing… | テスト中… | 正在测试… | 테스트 중…
Parallel requests | 並列リクエスト | 并行请求 | 병렬 요청
Faster, but more likely to hit rate limits (HTTP 429). Not used for Lingva. | 高速になりますが、レート制限(HTTP 429)に達しやすくなります。Lingvaでは使用されません。 | 速度更快,但更容易触发速率限制(HTTP 429)。Lingva 不使用此项。 | 더 빠르지만 요청 제한(HTTP 429)에 걸릴 가능성이 높아집니다. Lingva에서는 사용되지 않습니다.
Translation Results | 翻訳結果 | 翻译结果 | 번역 결과
Use cache (skip text already translated with the same settings) | キャッシュを使用(同じ設定で翻訳済みのテキストをスキップ) | 使用缓存(跳过已用相同设置翻译过的文本) | 캐시 사용(같은 설정으로 이미 번역한 텍스트는 건너뜀)
Auto-wrap lines when the original has several lines | 原文が複数行の場合は自動で折り返す | 原文为多行时自动换行 | 원문이 여러 줄이면 자동 줄바꿈
Max line width (characters) | 最大行幅(文字数) | 最大行宽(字符数) | 최대 줄 너비(글자 수)
Generate from text | テキストから生成 | 从文本生成 | 텍스트에서 생성
Finding terms… | 用語を検索中… | 正在查找术语… | 용어 찾는 중…
No repeated terms were found. | 繰り返し出てくる用語は見つかりませんでした。 | 未找到重复出现的术语。 | 반복되는 용어를 찾지 못했습니다.
Finds repeated names/terms (capitalised Latin names, katakana, names with honorifics) and translates them with the selected provider. | 繰り返し出てくる名前や用語(大文字始まりのラテン文字の名前、カタカナ、敬称付きの名前)を検出し、選択したプロバイダーで翻訳します。 | 查找重复出现的名称/术语(首字母大写的拉丁字母名称、片假名、带敬称的名称),并使用所选提供商进行翻译。 | 반복되는 이름/용어(대문자로 시작하는 라틴 문자 이름, 가타카나, 경칭이 붙은 이름)를 찾아 선택한 제공업체로 번역합니다.
Online | オンライン | 在线 | 온라인
Cancelled; partial results were applied. | キャンセルしました。途中までの結果は適用されました。 | 已取消,已应用部分结果。 | 취소되었습니다. 일부 결과는 적용되었습니다.
Nothing to translate. | 翻訳するものがありません。 | 没有需要翻译的内容。 | 번역할 내용이 없습니다.
Enter the API key first. | 先にAPIキーを入力してください。 | 请先输入 API 密钥。 | 먼저 API 키를 입력하세요.
Enter the model name first. | 先にモデル名を入力してください。 | 请先输入模型名称。 | 먼저 모델 이름을 입력하세요.
Going back while running cancels it; partial results are still applied. | 実行中に戻るとキャンセルされます。途中までの結果は適用されます。 | 运行中返回会取消任务,已完成的部分结果仍会应用。 | 실행 중에 뒤로 가면 취소됩니다. 일부 결과는 그대로 적용됩니다.
Script Tools | スクリプトツール | 脚本工具 | 스크립트 도구
View archive contents | アーカイブの内容を表示 | 查看压缩包内容 | 아카이브 내용 보기
Archive Contents | アーカイブの内容 | 压缩包内容 | 아카이브 내용
Tap an image or text file name to Preview | 画像またはテキストのファイル名をタップするとプレビューします | 点按图片或文本文件名即可预览 | 이미지나 텍스트 파일 이름을 누르면 미리 볼 수 있어요
Inspect an archive or script: format, checksum, corrupt files, encryption and wrong code | アーカイブまたはスクリプトを検査: 形式、チェックサム、破損ファイル、暗号化、誤ったコード | 检查压缩包或脚本:格式、校验和、损坏文件、加密和错误编码 | 아카이브 또는 스크립트 검사: 형식, 체크섬, 손상된 파일, 암호화, 잘못된 코드
Archive / Script File | アーカイブ/スクリプトファイル | 压缩包/脚本文件 | 아카이브/스크립트 파일
Inspecting file… | ファイルを検査中… | 正在检查文件… | 파일 검사 중…
Pick an archive (VN or RPGM) or a script to inspect. Everything is checked offline on your device. | 検査するアーカイブ(VNまたはRPGM)かスクリプトを選択してください。すべてお使いの端末上でオフラインで確認されます。 | 请选择要检查的压缩包(VN 或 RPGM)或脚本。所有检查均在您的设备上离线完成。 | 검사할 아카이브(VN 또는 RPGM)나 스크립트를 선택하세요. 모든 검사는 기기에서 오프라인으로 진행됩니다.
Information | 情報 | 信息 | 정보
Inspection results | 検査結果 | 检查结果 | 검사 결과
Checksum | チェックサム | 校验和 | 체크섬
Check Integrity | 整合性チェック | 完整性检查 | 무결성 검사
Extracts every file in memory to look for corrupt files. May take long for big archives. | すべてのファイルをメモリ上で展開して破損ファイルを調べます。大きなアーカイブでは時間がかかる場合があります。 | 在内存中解压所有文件以查找损坏文件。大型压缩包可能需要较长时间。 | 모든 파일을 메모리에서 추출해 손상된 파일을 찾습니다. 큰 아카이브는 시간이 오래 걸릴 수 있습니다.
Checking files | ファイルを確認中 | 正在检查文件 | 파일 확인 중
Compute Checksum | チェックサムを計算 | 计算校验和 | 체크섬 계산
Computing checksum… | チェックサムを計算中… | 正在计算校验和… | 체크섬 계산 중…
Integrity check results | 整合性チェックの結果 | 完整性检查结果 | 무결성 검사 결과
Archive Tools | アーカイブツール | 压缩包工具 | 아카이브 도구
Unpack Archive | アーカイブを展開 | 解包压缩包 | 아카이브 풀기
The archive format is detected automatically, then extracted | アーカイブ形式を自動検出して展開します | 自动检测压缩包格式后解压 | 아카이브 형식을 자동으로 감지해 추출해요
Pack Archive | アーカイブを作成 | 打包压缩包 | 아카이브 만들기
Pick an archive format, then pack a folder | アーカイブ形式を選び、フォルダーをパックします | 选择压缩包格式,然后打包文件夹 | 아카이브 형식을 선택한 후 폴더를 묶어요
Export/import text and contents list, type detected automatically | テキストと内容リストのエクスポート/インポート(種類は自動検出) | 导出/导入文本和内容列表,类型自动检测 | 텍스트와 내용 목록 내보내기/가져오기(유형 자동 감지)
Engine List | エンジン一覧 | 引擎列表 | 엔진 목록
Unpack, pack archives and manage scripts per engine | エンジンごとにアーカイブの展開・作成とスクリプトの管理を行います | 按引擎解包、打包压缩包并管理脚本 | 엔진별로 아카이브 풀기/만들기 및 스크립트 관리
Script Contents List | スクリプト内容リスト | 脚本内容列表 | 스크립트 내용 목록
Write every file name inside an archive to a text file | アーカイブ内のすべてのファイル名をテキストファイルに書き出します | 将压缩包内所有文件名写入文本文件 | 아카이브 안의 모든 파일 이름을 텍스트 파일로 저장해요
Collect dialogue and choices from every script in a folder, type detected automatically | フォルダー内のすべてのスクリプトからセリフと選択肢を収集します(種類は自動検出) | 收集文件夹内所有脚本的对话和选项,类型自动检测 | 폴더 안의 모든 스크립트에서 대사와 선택지를 수집해요(유형 자동 감지)
Apply translations to every script in a folder, type detected automatically | フォルダー内のすべてのスクリプトに翻訳を適用します(種類は自動検出) | 将翻译应用到文件夹内所有脚本,类型自动检测 | 폴더 안의 모든 스크립트에 번역을 적용해요(유형 자동 감지)
Auto-detect | 自動検出 | 自动检测 | 자동 감지
Archive format | アーカイブ形式 | 压缩包格式 | 아카이브 형식
Choose archive format… | アーカイブ形式を選択… | 选择压缩包格式… | 아카이브 형식 선택…
Archive format not recognised. Try Engine List if you know the engine. | アーカイブ形式を認識できません。エンジンがわかる場合は「エンジン一覧」をお試しください。 | 无法识别压缩包格式。如果知道引擎,请尝试“引擎列表”。 | 아카이브 형식을 인식하지 못했어요. 엔진을 알고 있다면 '엔진 목록'을 이용해 보세요.
Decompile rpyc → rpy | rpyc → rpy をデコンパイル | 反编译 rpyc → rpy | rpyc → rpy 디컴파일
Export Script Text | スクリプトテキストをエクスポート | 导出脚本文本 | 스크립트 텍스트 내보내기
Import Script Text | スクリプトテキストをインポート | 导入脚本文本 | 스크립트 텍스트 가져오기
Decode nscript.dat | nscript.datをデコード | 解码 nscript.dat | nscript.dat 디코딩
Encode to nscript.dat | nscript.datにエンコード | 编码为 nscript.dat | nscript.dat로 인코딩
Archive Contents List | アーカイブ内容リスト | 压缩包内容列表 | 아카이브 내용 목록
Turn .rpyc files into readable, editable .rpy scripts | .rpycファイルを読み取り・編集可能な.rpyスクリプトに変換します | 将 .rpyc 文件转换为可读、可编辑的 .rpy 脚本 | .rpyc 파일을 읽고 편집할 수 있는 .rpy 스크립트로 변환해요
Turn nscript.dat into a readable 0.txt | nscript.datを読み取り可能な0.txtに変換します | 将 nscript.dat 转换为可读的 0.txt | nscript.dat를 읽을 수 있는 0.txt로 변환해요
Turn 0.txt back into nscript.dat | 0.txtをnscript.datに戻します | 将 0.txt 转换回 nscript.dat | 0.txt를 다시 nscript.dat로 변환해요
Save the names and sizes of the files inside an archive as text | アーカイブ内のファイルの名前とサイズをテキストとして保存します | 将压缩包内文件的名称和大小保存为文本 | 아카이브 안 파일의 이름과 크기를 텍스트로 저장해요
0.txt scripts | 0.txtスクリプト | 0.txt 脚本 | 0.txt 스크립트
scene .dat | シーン .dat | 场景 .dat | 씬 .dat
.bin scripts | .binスクリプト | .bin 脚本 | .bin 스크립트
extension-less scripts | 拡張子なしのスクリプト | 无扩展名脚本 | 확장자 없는 스크립트
scripts | スクリプト | 脚本 | 스크립트
Game folder (with .rpyc) | ゲームフォルダー(.rpycを含む) | 游戏文件夹(含 .rpyc) | 게임 폴더(.rpyc 포함)
Unpacked folder (with scripts) | 展開済みフォルダー(スクリプトを含む) | 已解包文件夹(含脚本) | 추출된 폴더(스크립트 포함)
Unpacked folder (with nscript.dat / 0.txt) | 展開済みフォルダー(nscript.dat / 0.txtを含む) | 已解包文件夹(含 nscript.dat / 0.txt) | 추출된 폴더(nscript.dat / 0.txt 포함)
No .rpyc files in this folder. | このフォルダーに.rpycファイルがありません。 | 此文件夹中没有 .rpyc 文件。 | 이 폴더에 .rpyc 파일이 없어요.
nscript.dat was not found in this folder. | このフォルダーにnscript.datが見つかりませんでした。 | 在此文件夹中未找到 nscript.dat。 | 이 폴더에서 nscript.dat를 찾지 못했어요.
0.txt / 00.txt was not found in this folder. | このフォルダーに0.txt / 00.txtが見つかりませんでした。 | 在此文件夹中未找到 0.txt / 00.txt。 | 이 폴더에서 0.txt / 00.txt를 찾지 못했어요.
No recognised script files in this folder. | このフォルダーに認識できるスクリプトファイルがありません。 | 此文件夹中没有可识别的脚本文件。 | 이 폴더에 인식할 수 있는 스크립트 파일이 없어요.
Result: 0.txt (original text, game encoding). Use Export Script Text to translate it. | 結果: 0.txt(原文テキスト、ゲームの文字コード)。翻訳するには「スクリプトテキストをエクスポート」を使用してください。 | 结果:0.txt(原始文本,游戏编码)。如需翻译,请使用“导出脚本文本”。 | 결과: 0.txt(원문 텍스트, 게임 인코딩). 번역하려면 '스크립트 텍스트 내보내기'를 사용하세요.
Result: nscript.dat. Copy it into the game folder in place of the old one. | 結果: nscript.dat。ゲームフォルダー内の古いファイルと置き換えてコピーしてください。 | 结果:nscript.dat。请将其复制到游戏文件夹中,替换旧文件。 | 결과: nscript.dat. 게임 폴더의 기존 파일을 대체하도록 복사하세요.
Result: <archive name>.txt with the size and name of every file. | 結果: <archive name>.txt(すべてのファイルのサイズと名前を記載)。 | 结果:<archive name>.txt,包含每个文件的大小和名称。 | 결과: <archive name>.txt (모든 파일의 크기와 이름 포함).
Decompile | デコンパイル | 反编译 | 디컴파일
Export Text | テキストをエクスポート | 导出文本 | 텍스트 내보내기
Import Text | テキストをインポート | 导入文本 | 텍스트 가져오기
Decode | デコード | 解码 | 디코딩
Encode | エンコード | 编码 | 인코딩
Save List | リストを保存 | 保存列表 | 목록 저장
Export finished | エクスポート完了 | 导出完成 | 내보내기 완료
Import finished | インポート完了 | 导入完成 | 가져오기 완료
Decompilation finished | デコンパイル完了 | 反编译完成 | 디컴파일 완료
Decode finished | デコード完了 | 解码完成 | 디코딩 완료
Encode finished | エンコード完了 | 编码完成 | 인코딩 완료
List saved | リストを保存しました | 列表已保存 | 목록을 저장했어요
Texts exported | テキストをエクスポートしました | 文本已导出 | 텍스트를 내보냈어요
Texts replaced | テキストを置き換えました | 文本已替换 | 텍스트를 교체했어요
Files decompiled | ファイルをデコンパイルしました | 文件已反编译 | 파일을 디컴파일했어요
Files listed | ファイルを一覧にしました | 文件已列出 | 파일을 목록으로 만들었어요
Succeeded | 成功 | 成功 | 성공
Translation file (CSV/TXT/JSON) | 翻訳ファイル(CSV/TXT/JSON) | 翻译文件(CSV/TXT/JSON) | 번역 파일(CSV/TXT/JSON)
File format | ファイル形式 | 文件格式 | 파일 형식
script_translation | script_translation | script_translation | script_translation
No text could be collected from these files. | これらのファイルからテキストを収集できませんでした。 | 无法从这些文件中收集文本。 | 이 파일에서 텍스트를 수집하지 못했어요.
The translation file is empty or its format is not recognised. | 翻訳ファイルが空か、形式を認識できません。 | 翻译文件为空,或格式无法识别。 | 번역 파일이 비어 있거나 형식을 인식하지 못했어요.
No entries matched the scripts in this folder. | このフォルダーのスクリプトに一致するエントリーがありません。 | 没有与此文件夹中的脚本匹配的条目。 | 이 폴더의 스크립트와 일치하는 항목이 없어요.
This file already looks like plain text, not an encrypted nscript.dat. | このファイルは暗号化されたnscript.datではなく、すでにプレーンテキストのようです。 | 此文件看起来已是纯文本,不是加密的 nscript.dat。 | 이 파일은 암호화된 nscript.dat가 아니라 이미 일반 텍스트인 것 같아요.
Pick an archive that contains files first. | 先にファイルを含むアーカイブを選択してください。 | 请先选择包含文件的压缩包。 | 먼저 파일이 들어 있는 아카이브를 선택하세요.
Guide | ガイド | 指南 | 가이드
How to use, Q&A and troubleshooting | 使い方、Q&A、トラブルシューティング | 使用方法、问答与故障排除 | 사용법, Q&A, 문제 해결
How to Use | 使い方 | 使用方法 | 사용 방법
A complete guide to every feature | すべての機能の詳しいガイド | 每项功能的完整指南 | 모든 기능에 대한 상세 가이드
Questions and answers on how the features work | 各機能の仕組みに関するQ&A | 关于各项功能原理的问答 | 기능 작동 방식에 대한 Q&A
Sources and references used | 使用した情報源と参考資料 | 使用的来源与参考资料 | 사용한 출처와 참고 자료
About | アプリについて | 关于 | 정보
Developer Contact and Report a Problem | 開発者への連絡と問題の報告 | 联系开发者并报告问题 | 개발자 연락 및 문제 신고
Main reference for archive and script structures: PFS, XP3, CatSystem 2, Yu-Ris, Escu:de, Softpal, Circus, HexenHaus, Itufuru, BGI, Favorite, WillPlus, Silky, EntisGLS, QLIE, NScripter and more. | アーカイブとスクリプト構造の主要な参考資料: PFS、XP3、CatSystem 2、Yu-Ris、Escu:de、Softpal、Circus、HexenHaus、Itufuru、BGI、Favorite、WillPlus、Silky、EntisGLS、QLIE、NScripterなど。 | 压缩包与脚本结构的主要参考资料:PFS、XP3、CatSystem 2、Yu-Ris、Escu:de、Softpal、Circus、HexenHaus、Itufuru、BGI、Favorite、WillPlus、Silky、EntisGLS、QLIE、NScripter 等。 | 아카이브와 스크립트 구조의 주요 참고 자료: PFS, XP3, CatSystem 2, Yu-Ris, Escu:de, Softpal, Circus, HexenHaus, Itufuru, BGI, Favorite, WillPlus, Silky, EntisGLS, QLIE, NScripter 등.
Cross-check for visual novel archive formats (Majiro, Circus, Softpal, EntisGLS NOA and others), including the ERISA/BSHF implementation. | ビジュアルノベルのアーカイブ形式(Majiro、Circus、Softpal、EntisGLS NOAなど)の照合用資料。ERISA/BSHFの実装を含みます。 | 用于交叉核对视觉小说压缩包格式(Majiro、Circus、Softpal、EntisGLS NOA 等),包括 ERISA/BSHF 的实现。 | 비주얼 노벨 아카이브 형식(Majiro, Circus, Softpal, EntisGLS NOA 등) 교차 확인용 자료이며 ERISA/BSHF 구현을 포함합니다.
Visual novel archive decoders; reference for the checksum, BSHF and ERISA-N of EntisGLS NOA archives. | ビジュアルノベルのアーカイブデコーダー。EntisGLS NOAアーカイブのチェックサム、BSHF、ERISA-Nの参考資料。 | 视觉小说压缩包解码器;EntisGLS NOA 压缩包的校验和、BSHF 和 ERISA-N 的参考资料。 | 비주얼 노벨 아카이브 디코더. EntisGLS NOA 아카이브의 체크섬, BSHF, ERISA-N 참고 자료.
Pack/unpack tool for EntisGLS NOA archives; reference for compression and encryption options. | EntisGLS NOAアーカイブのパック/展開ツール。圧縮と暗号化オプションの参考資料。 | EntisGLS NOA 压缩包的打包/解包工具;压缩和加密选项的参考资料。 | EntisGLS NOA 아카이브용 묶기/풀기 도구. 압축 및 암호화 옵션 참고 자료.
Reference for the header, directory and encode types of EntisGLS NOA archives. | EntisGLS NOAアーカイブのヘッダー、ディレクトリ、エンコード種別の参考資料。 | EntisGLS NOA 压缩包的头部、目录和编码类型的参考资料。 | EntisGLS NOA 아카이브의 헤더, 디렉터리, 인코딩 유형 참고 자료.
CSXTool/CSXToolPlus: reference for exporting and importing EntisGLS .csx script text. | CSXTool/CSXToolPlus: EntisGLSの.csxスクリプトテキストのエクスポート/インポートの参考資料。 | CSXTool/CSXToolPlus:导出和导入 EntisGLS .csx 脚本文本的参考资料。 | CSXTool/CSXToolPlus: EntisGLS .csx 스크립트 텍스트 내보내기/가져오기 참고 자료.
Unpack/repack tool for NeXAS PAC archives; starting point for variants. | NeXAS PACアーカイブの展開/再パックツール。派生バージョンの出発点。 | NeXAS PAC 压缩包的解包/重新打包工具;各变体的起点。 | NeXAS PAC 아카이브 풀기/다시 묶기 도구. 변형 버전의 출발점.
Unpack/repack for the GIGA NeXAS variant, including compression choice (no/zlib/zstd). | GIGA NeXAS派生版の展開/再パック(圧縮の選択 なし/zlib/zstd を含む)。 | GIGA NeXAS 变体的解包/重新打包,包括压缩方式选择(无/zlib/zstd)。 | GIGA NeXAS 변형 버전용 풀기/다시 묶기이며 압축 선택(없음/zlib/zstd)을 포함합니다.
Reference for the NeXAS PAC structure (header, index, Huffman) and the script text format. | NeXAS PACの構造(ヘッダー、インデックス、Huffman)とスクリプトテキスト形式の参考資料。 | NeXAS PAC 结构(头部、索引、Huffman)及脚本文本格式的参考资料。 | NeXAS PAC 구조(헤더, 인덱스, Huffman)와 스크립트 텍스트 형식 참고 자료.
Reference for config and scripts of newer NeXAS versions. | 新しいバージョンのNeXASの設定とスクリプトの参考資料。 | 较新版本 NeXAS 的配置和脚本的参考资料。 | 최신 버전 NeXAS의 설정 및 스크립트 참고 자료.
Reference for converting NeXAS GIGA/Entergram data to JSON. | NeXAS GIGA/Entergramのデータを JSON に変換するための参考資料。 | 将 NeXAS GIGA/Entergram 数据转换为 JSON 的参考资料。 | NeXAS GIGA/Entergram 데이터를 JSON으로 변환하기 위한 참고 자료.
Reference for dumping/importing NeXAS BINU8 and DATU8 strings. | NeXAS BINU8とDATU8の文字列のダンプ/インポートの参考資料。 | 导出/导入 NeXAS BINU8 和 DATU8 字符串的参考资料。 | NeXAS BINU8 및 DATU8 문자열 덤프/가져오기 참고 자료.
Reference for the NeXAS PAC index Huffman decoder. | NeXAS PACインデックスのHuffmanデコーダーの参考資料。 | NeXAS PAC 索引 Huffman 解码器的参考资料。 | NeXAS PAC 인덱스 Huffman 디코더 참고 자료.
Catalogue of NeXAS projects (nexas-datu8, NeXAS_SPM_VIEWER, map_format_explorer). | NeXASプロジェクトのカタログ(nexas-datu8、NeXAS_SPM_VIEWER、map_format_explorer)。 | NeXAS 项目目录(nexas-datu8、NeXAS_SPM_VIEWER、map_format_explorer)。 | NeXAS 프로젝트 목록(nexas-datu8, NeXAS_SPM_VIEWER, map_format_explorer).
Keyless Google Translate frontend for Online Translation. | オンライン翻訳用のキー不要なGoogle翻訳フロントエンド。 | 用于在线翻译的免密钥 Google 翻译前端。 | 온라인 번역용 키가 필요 없는 Google 번역 프런트엔드.
Google Translate v2 API documentation. | Google翻訳 v2 APIのドキュメント。 | Google 翻译 v2 API 文档。 | Google 번역 v2 API 문서.
DeepL API documentation. | DeepL APIのドキュメント。 | DeepL API 文档。 | DeepL API 문서.
Gemini API documentation. | Gemini APIのドキュメント。 | Gemini API 文档。 | Gemini API 문서.
OpenAI API documentation. | OpenAI APIのドキュメント。 | OpenAI API 文档。 | OpenAI API 문서.
Collection of VN translation tools, including EntisDecryptTool (archive key from the IDR_COTOMI resource). | EntisDecryptToolを含むVN翻訳ツール集(IDR_COTOMIリソースからのアーカイブキー)。 | VN 翻译工具合集,包括 EntisDecryptTool(来自 IDR_COTOMI 资源的压缩包密钥)。 | EntisDecryptTool을 포함한 VN 번역 도구 모음(IDR_COTOMI 리소스의 아카이브 키).
Reference for the Favorite/FVP archive and script formats. | Favorite/FVPのアーカイブとスクリプト形式の参考資料。 | Favorite/FVP 压缩包和脚本格式的参考资料。 | Favorite/FVP 아카이브 및 스크립트 형식 참고 자료.
Reference for WillPlus/AdvHD .ws2 scripts. | WillPlus/AdvHDの.ws2スクリプトの参考資料。 | WillPlus/AdvHD .ws2 脚本的参考资料。 | WillPlus/AdvHD .ws2 스크립트 참고 자료.
Collection of visual novel tools, reference for archive and script structures. | ビジュアルノベルツール集。アーカイブとスクリプト構造の参考資料。 | 视觉小说工具合集,压缩包和脚本结构的参考资料。 | 비주얼 노벨 도구 모음. 아카이브와 스크립트 구조 참고 자료.
Visual novel pack/unpack tools, cross-check for archive structures. | ビジュアルノベルのパック/展開ツール。アーカイブ構造の照合用。 | 视觉小说打包/解包工具,用于交叉核对压缩包结构。 | 비주얼 노벨 묶기/풀기 도구. 아카이브 구조 교차 확인용.
Visual novel unpack tools, cross-check for archive structures. | ビジュアルノベルの展開ツール。アーカイブ構造の照合用。 | 视觉小说解包工具,用于交叉核对压缩包结构。 | 비주얼 노벨 풀기 도구. 아카이브 구조 교차 확인용.
Reference for the Yu-Ris YPF archive format. | Yu-Ris YPFアーカイブ形式の参考資料。 | Yu-Ris YPF 压缩包格式的参考资料。 | Yu-Ris YPF 아카이브 형식 참고 자료.
Reference for visual novel archive and script handling. | ビジュアルノベルのアーカイブとスクリプト処理の参考資料。 | 视觉小说压缩包和脚本处理的参考资料。 | 비주얼 노벨 아카이브 및 스크립트 처리 참고 자료.
Reference for the SiglusEngine Scene.pck, Gameexe.dat, exe key and scene structure. | SiglusEngineのScene.pck、Gameexe.dat、exeキー、シーン構造の参考資料。 | SiglusEngine 的 Scene.pck、Gameexe.dat、exe 密钥和场景结构的参考资料。 | SiglusEngine의 Scene.pck, Gameexe.dat, exe 키, 씬 구조 참고 자료.
Collection of SiglusEngine tools and docs (Scene.pck, Gameexe.dat, OVK). | SiglusEngineのツールとドキュメント集(Scene.pck、Gameexe.dat、OVK)。 | SiglusEngine 工具和文档合集(Scene.pck、Gameexe.dat、OVK)。 | SiglusEngine 도구 및 문서 모음(Scene.pck, Gameexe.dat, OVK).
Cross-check for SiglusEngine Scene.pck and Gameexe.dat unpacking behaviour. | SiglusEngineのScene.pckとGameexe.datの展開動作の照合用。 | 用于交叉核对 SiglusEngine Scene.pck 和 Gameexe.dat 的解包行为。 | SiglusEngine Scene.pck 및 Gameexe.dat 풀기 동작 교차 확인용.
Reference for decompiling Ren'Py .rpyc into .rpy (MIT licence). The rpyc → rpy feature in FuruArc is a partial port of this project. | Ren'Pyの.rpycを.rpyにデコンパイルするための参考資料(MITライセンス)。FuruArcのrpyc → rpy機能は、このプロジェクトの部分的な移植です。 | 将 Ren'Py .rpyc 反编译为 .rpy 的参考资料(MIT 许可证)。FuruArc 的 rpyc → rpy 功能是该项目的部分移植。 | Ren'Py .rpyc를 .rpy로 디컴파일하기 위한 참고 자료(MIT 라이선스). FuruArc의 rpyc → rpy 기능은 이 프로젝트를 부분적으로 이식한 것입니다.
Java port of the LAME MP3 encoder (LGPL v3). Included as source code inside the app for MP3 output. | LAME MP3エンコーダーのJava移植版(LGPL v3)。MP3出力のためにソースコードとしてアプリ内に含まれています。 | LAME MP3 编码器的 Java 移植版(LGPL v3)。以源代码形式包含在应用内,用于 MP3 输出。 | LAME MP3 인코더의 Java 포팅(LGPL v3). MP3 출력을 위해 소스 코드 형태로 앱에 포함되어 있습니다.
Java port of Xiph.Org libogg/libvorbis (BSD licence). Included as source code for OGG Vorbis output. | Xiph.Org libogg/libvorbisのJava移植版(BSDライセンス)。OGG Vorbis出力のためにソースコードとして含まれています。 | Xiph.Org libogg/libvorbis 的 Java 移植版(BSD 许可证)。以源代码形式包含,用于 OGG Vorbis 输出。 | Xiph.Org libogg/libvorbis의 Java 포팅(BSD 라이선스). OGG Vorbis 출력을 위해 소스 코드 형태로 포함되어 있습니다.
Official engine documentation, used as a reference for the .rpy script and RPA archive formats. | .rpyスクリプトとRPAアーカイブ形式の参考として使用した公式エンジンドキュメント。 | 官方引擎文档,用作 .rpy 脚本和 RPA 压缩包格式的参考。 | .rpy 스크립트와 RPA 아카이브 형식의 참고 자료로 사용한 공식 엔진 문서.
FuruArc developer | FuruArc開発者 | FuruArc 开发者 | FuruArc 개발자
Contact | 連絡先 | 联系方式 | 연락처
Collaboration | コラボレーション | 合作 | 협업
Thanks to Furukammi Team for helping with the development of the app | アプリの開発にご協力いただいたFurukammi Teamに感謝します | 感谢 Furukammi Team 对应用开发的帮助 | 앱 개발에 도움을 준 Furukammi Team에 감사드립니다
Visit website | ウェブサイトを開く | 访问网站 | 웹사이트 방문
Donation | 寄付 | 捐赠 | 후원
Your support helps further development. Thank you! | 皆さまのご支援が今後の開発の助けになります。ありがとうございます! | 您的支持有助于应用的后续开发。谢谢! | 여러분의 후원은 앞으로의 개발에 큰 힘이 됩니다. 감사합니다!
No app can open this link | このリンクを開けるアプリがありません | 没有可打开此链接的应用 | 이 링크를 열 수 있는 앱이 없습니다
Troubleshooting | トラブルシューティング | 故障排除 | 문제 해결
Fixes for common errors and problems | よくあるエラーや問題の対処法 | 常见错误和问题的解决方法 | 자주 발생하는 오류와 문제의 해결 방법
Report a Problem | 問題を報告 | 报告问题 | 문제 신고
Include the last process log | 直近の処理ログを含める | 包含最近的处理日志 | 마지막 처리 로그 포함
Send report by email | メールでレポートを送信 | 通过邮件发送报告 | 이메일로 신고 보내기
FuruArc problem report | FuruArc 問題レポート | FuruArc 问题报告 | FuruArc 문제 신고
Describe the problem (steps taken, game/engine, error message): | 問題の内容を記入してください(行った操作、ゲーム/エンジン、エラーメッセージ): | 请描述问题(操作步骤、游戏/引擎、错误信息): | 문제를 설명해 주세요(수행한 작업, 게임/엔진, 오류 메시지):
No email app found | メールアプリが見つかりません | 未找到邮件应用 | 이메일 앱을 찾을 수 없습니다
Licences and Thanks | ライセンスと謝辞 | 许可证与致谢 | 라이선스 및 감사의 말
{0} problems · {1} warnings | 問題 {0} 件 · 警告 {1} 件 | {0} 个问题 · {1} 个警告 | 문제 {0}개 · 경고 {1}개
{0} files not checked yet (checked in batches of 5000). | 未チェックのファイルが {0} 件あります (5000 件ずつ順次チェックします)。 | 还有 {0} 个文件未检查 (每批检查 5000 个)。 | 아직 확인하지 않은 파일이 {0}개 있습니다 (5000개씩 나눠서 확인합니다).
{0} — {1} files ({2}) | {0} — {1} 個のファイル ({2}) | {0} — {1} 个文件 ({2}) | {0} — 파일 {1}개 ({2})
Failed: {0} | 失敗: {0} | 失败: {0} | 실패: {0}
Folder contents: Audio {0} files ({1}) · Images {2} files ({3}) · Video {4} files ({5}) · Unrecognized {6} files | フォルダの内容: 音声 {0} 件 ({1}) · 画像 {2} 件 ({3}) · 動画 {4} 件 ({5}) · 未認識 {6} 件 | 文件夹内容: 音频 {0} 个 ({1}) · 图片 {2} 个 ({3}) · 视频 {4} 个 ({5}) · 无法识别 {6} 个 | 폴더 내용: 오디오 {0}개 ({1}) · 이미지 {2}개 ({3}) · 동영상 {4}개 ({5}) · 인식 불가 {6}개
Folder: only {0} files. The subfolder structure is kept in the output folder. | フォルダ: {0} 個のファイルのみ。サブフォルダ構造は出力フォルダにそのまま保持されます。 | 文件夹: 仅 {0} 个文件。子文件夹结构将保留在输出文件夹中。 | 폴더: 파일 {0}개뿐입니다. 하위 폴더 구조는 출력 폴더에 그대로 유지됩니다.
Discard larger results ({0}) | 大きくなった結果を破棄 ({0}) | 丢弃变大的结果 ({0}) | 더 커진 결과 버리기 ({0})
Source Extensions ({0}/{1}) | 対象の拡張子 ({0}/{1}) | 源扩展名 ({0}/{1}) | 원본 확장자 ({0}/{1})
{0}: {1} file(s), {2} → {3} ({4}) | {0}: {1} 個のファイル, {2} → {3} ({4}) | {0}: {1} 个文件, {2} → {3} ({4}) | {0}: 파일 {1}개, {2} → {3} ({4})
{0} parts selected | {0} 個のパートを選択中 | 已选择 {0} 个分卷 | 파트 {0}개 선택됨
Missing parts: number {0}. | 不足しているパート: {0} 番。 | 缺少的分卷: 第 {0} 号。 | 누락된 파트: {0}번.
{0} files renamed, {1} failed and were restored to their old names. | {0} 個のファイルの名前を変更しましたが、{1} 個は失敗したため元の名前に戻しました。 | 已重命名 {0} 个文件, {1} 个失败并已恢复为原名称。 | 파일 {0}개의 이름을 바꿨고, {1}개는 실패하여 원래 이름으로 복원했습니다.
Join {0} sets | {0} セットを結合 | 合并 {0} 组 | {0}개 세트 합치기
{0} duplicate groups · {1} extra files · {2} can be saved | 重複グループ {0} 件 · 余分なファイル {1} 件 · {2} 節約可能 | {0} 个重复组 · {1} 个多余文件 · 可节省 {2} | 중복 그룹 {0}개 · 추가 파일 {1}개 · {2} 절약 가능
Done: {0} files deleted. | 完了: {0} 個のファイルを削除しました。 | 完成: 已删除 {0} 个文件。 | 완료: 파일 {0}개를 삭제했습니다.
Non-ASCII names: {0} | 非 ASCII の名前: {0} | 非 ASCII 名称: {0} | 비ASCII 이름: {0}
Checking references in text files… ({0}/{1}) | テキストファイル内の参照を確認中… ({0}/{1}) | 正在检查文本文件中的引用… ({0}/{1}) | 텍스트 파일의 참조를 확인하는 중… ({0}/{1})
Original copies of the edited text files will be kept in the Backup Folder ({0}). File and folder names are recorded in rename_mapping.json so they can be restored. | 編集したテキストファイルの元のコピーはバックアップフォルダ ({0}) に保存されます。ファイル名とフォルダ名は rename_mapping.json に記録されるため、元に戻せます。 | 已编辑文本文件的原始副本将保存在备份文件夹 ({0}) 中。文件和文件夹名称会记录在 rename_mapping.json 中, 以便还原。 | 편집한 텍스트 파일의 원본 사본은 백업 폴더 ({0})에 보관됩니다. 파일 및 폴더 이름은 rename_mapping.json에 기록되어 복원할 수 있습니다.
Restoring file names… ({0}/{1}) | ファイル名を復元中… ({0}/{1}) | 正在还原文件名… ({0}/{1}) | 파일 이름을 복원하는 중… ({0}/{1})
Restored: {0} files, {1} folders, {2} text files. | 復元しました: ファイル {0} 件、フォルダ {1} 件、テキストファイル {2} 件。 | 已还原: {0} 个文件, {1} 个文件夹, {2} 个文本文件。 | 복원됨: 파일 {0}개, 폴더 {1}개, 텍스트 파일 {2}개.
Translating {0} / {1}… | 翻訳中 {0} / {1}… | 正在翻译 {0} / {1}… | 번역 중 {0} / {1}…
{0} term(s) added. Review them and remove any that are wrong. | {0} 件の用語を追加しました。確認して、誤りがあれば削除してください。 | 已添加 {0} 个术语。请检查并删除不正确的术语。 | 용어 {0}개를 추가했습니다. 확인한 후 잘못된 것은 삭제하세요.
Done: {0} item(s) changed in {1} plugin file(s). | 完了: {1} 個のプラグインファイルで {0} 件を変更しました。 | 完成: 在 {1} 个插件文件中修改了 {0} 项。 | 완료: 플러그인 파일 {1}개에서 {0}개 항목을 변경했습니다.
Plugin "{0}" is already in the list. | プラグイン "{0}" は既にリストにあります。 | 插件 "{0}" 已在列表中。 | 플러그인 "{0}"은(는) 이미 목록에 있습니다.
Profile "{0}" applied ({1} plugin(s) changed). | プロファイル "{0}" を適用しました (プラグイン {1} 件を変更)。 | 已应用配置 "{0}" (更改了 {1} 个插件)。 | 프로필 "{0}"을(를) 적용했습니다 (플러그인 {1}개 변경됨).
Plugin name appears again (positions {0} and {1}) but one of them is disabled; usually harmless. | プラグイン名が重複しています (位置 {0} と {1}) が、片方は無効です。通常は問題ありません。 | 插件名称重复出现 (位置 {0} 和 {1}), 但其中一个已禁用; 通常无影响。 | 플러그인 이름이 다시 나타나지만 (위치 {0} 및 {1}) 하나는 비활성화되어 있습니다. 보통은 문제없습니다.
Needs base plugin "{0}" (@base) but it is not in the list. | ベースプラグイン "{0}" (@base) が必要ですが、リストにありません。 | 需要基础插件 "{0}" (@base), 但列表中没有。 | 기본 플러그인 "{0}"(@base)이(가) 필요하지만 목록에 없습니다.
Page {0} of {1} (50 KB/page) | {0} / {1} ページ (1 ページ 50 KB) | 第 {0} 页, 共 {1} 页 (每页 50 KB) | {0} / {1} 페이지 (페이지당 50 KB)
Content differs: {0} | 内容が異なります: {0} | 内容不同: {0} | 내용이 다릅니다: {0}
Done: {0} texts written to {1} | 完了: {0} 件のテキストを {1} に書き込みました | 完成: 已将 {0} 条文本写入 {1} | 완료: 텍스트 {0}개를 {1}에 기록했습니다
Encoded: {0} → {1} | エンコード: {0} → {1} | 已编码: {0} → {1} | 인코딩: {0} → {1}
Scanning archives ({0}/{1}): {2} | アーカイブをスキャン中 ({0}/{1}): {2} | 正在扫描压缩包 ({0}/{1}): {2} | 아카이브 검사 중 ({0}/{1}): {2}
Search failed: {0} | 検索に失敗しました: {0} | 搜索失败: {0} | 검색 실패: {0}
Create an archive from a folder | フォルダからアーカイブを作成 | 从文件夹创建压缩包 | 폴더로 아카이브 만들기
Archive File | アーカイブファイル | 压缩包文件 | 아카이브 파일
Files | ファイル | 文件 | 파일
Create Archive | アーカイブを作成 | 创建压缩包 | 아카이브 만들기
English | English | English | English
Teal | ティール | 青绿色 | 청록색
Yellow | イエロー | 黄色 | 노란색
Cancelled | キャンセルしました | 已取消 | 취소됨
FAILED {0}: {1} | 失敗 {0}: {1} | 失败 {0}: {1} | 실패 {0}: {1}
Done: {0} bytes ({1} s) | 完了: {0} バイト ({1} 秒) | 完成: {0} 字节 ({1} 秒) | 완료: {0}바이트 ({1}초)
Keep going | 続行 | 继续 | 계속하기
If the file already exists | ファイルが既に存在する場合 | 如果文件已存在 | 파일이 이미 있는 경우
Finished | 完了 | 已完成 | 완료
Skipped | スキップしました | 已跳过 | 건너뜀
Log copied | ログをコピーしました | 日志已复制 | 로그를 복사했습니다
Settings saved successfully | 設定を保存しました | 设置保存成功 | 설정을 저장했습니다
Display settings | 表示設定 | 显示设置 | 표시 설정
"""
    const val T3 = """
Used when no output folder is chosen | 出力フォルダが未選択のときに使用されます | 未选择输出文件夹时使用 | 출력 폴더를 선택하지 않았을 때 사용됩니다
Prevents the screen from turning off during a process | 処理中に画面がオフになるのを防ぎます | 处理过程中防止屏幕熄灭 | 작업 중 화면이 꺼지지 않게 합니다
Extraction check | 展開チェック | 解包检查 | 풀기 검사
File content checksum (Adler32/CRC32) | ファイル内容のチェックサム (Adler32/CRC32) | 文件内容校验和 (Adler32/CRC32) | 파일 내용 체크섬 (Adler32/CRC32)
A checksum mismatch can mean a wrong XOR key or a corrupted file. | チェックサムの不一致は、XOR キーの誤りまたはファイルの破損を示している可能性があります。 | 校验和不匹配可能意味着 XOR 密钥错误或文件已损坏。 | 체크섬이 일치하지 않으면 XOR 키가 잘못되었거나 파일이 손상되었을 수 있습니다.
Cannot preview this file | このファイルはプレビューできません | 无法预览此文件 | 이 파일은 미리 볼 수 없습니다
Clear results | 結果をクリア | 清除结果 | 결과 지우기
Extract the contents of a .{0} archive | .{0} アーカイブの内容を展開 | 解包 .{0} 压缩包的内容 | .{0} 아카이브의 내용 풀기
Assets, translation data, and save cheats for RPG Maker | RPG Maker のアセット、翻訳データ、セーブチート | RPG Maker 的资源、翻译数据和存档修改 | RPG Maker용 에셋, 번역 데이터, 세이브 치트
Export a save file to edit its values, then import it back | セーブファイルをエクスポートして値を編集し、再度インポートします | 导出存档文件以编辑数值, 然后再导入回去 | 세이브 파일을 내보내 값을 편집한 뒤 다시 가져옵니다
Encrypt images and audio into .png_ .ogg_ .m4a_ | 画像と音声を .png_ .ogg_ .m4a_ に暗号化 | 将图片和音频加密为 .png_ .ogg_ .m4a_ | 이미지와 오디오를 .png_ .ogg_ .m4a_로 암호화
Turn a save file into an editable JSON file | セーブファイルを編集可能な JSON ファイルに変換 | 将存档文件转换为可编辑的 JSON 文件 | 세이브 파일을 편집 가능한 JSON 파일로 변환
Asset File(s) | アセットファイル | 资源文件 | 에셋 파일
general | 一般 | 常规 | 일반
Items | アイテム | 物品 | 아이템
vars | 変数 | 变量 | 변수
No value was changed | 値は変更されていません | 未更改任何值 | 변경된 값이 없습니다
{0} files · {1} | {0} 個のファイル · {1} | {0} 个文件 · {1} | 파일 {0}개 · {1}
Generate random key | ランダムキーを生成 | 生成随机密钥 | 무작위 키 생성
Export | エクスポート | 导出 | 내보내기
Edited JSON file | 編集済み JSON ファイル | 已编辑的 JSON 文件 | 편집된 JSON 파일
Edit values (gold, items, variables...) in a text editor, keeping the JSON valid. | 値 (所持金、アイテム、変数など) をテキストエディタで編集します。JSON の形式は正しく保ってください。 | 在文本编辑器中编辑数值 (金币、物品、变量等), 并保持 JSON 有效。 | 텍스트 편집기에서 값(골드, 아이템, 변수 등)을 편집하세요. JSON 형식은 올바르게 유지해야 합니다.
Save created | セーブを作成しました | 已创建存档 | 세이브를 만들었습니다
No game data files (Map001.json, Actors.json, ...) found in this folder | このフォルダにゲームデータファイル (Map001.json、Actors.json など) が見つかりません | 在此文件夹中未找到游戏数据文件 (Map001.json, Actors.json, ...) | 이 폴더에서 게임 데이터 파일(Map001.json, Actors.json 등)을 찾을 수 없습니다
Encrypting {0} files | {0} 個のファイルを暗号化中 | 正在加密 {0} 个文件 | 파일 {0}개 암호화 중
Written: {0} | 書き込み完了: {0} | 已写入: {0} | 기록됨: {0}
Choose the game data folder so items, characters, variables and switches show their real names. | ゲームデータフォルダを選択すると、アイテム、キャラクター、変数、スイッチが実際の名前で表示されます。 | 选择游戏数据文件夹后, 物品、角色、变量和开关将显示真实名称。 | 게임 데이터 폴더를 선택하면 아이템, 캐릭터, 변수, 스위치가 실제 이름으로 표시됩니다.
Value | 値 | 值 | 값
Only rows that match the current search and filter are changed. Press Apply afterwards to write them. | 現在の検索とフィルターに一致する行のみが変更されます。変更後に適用を押して書き込んでください。 | 仅更改符合当前搜索和筛选条件的行。之后请点击应用以写入。 | 현재 검색 및 필터와 일치하는 행만 변경됩니다. 이후 적용을 눌러 기록하세요.
Not found in game | ゲーム内に見つかりません | 在游戏中未找到 | 게임에서 찾을 수 없음
Keep control codes such as \C[2], \N[1] and \V[3] in the translation; imports that change them are reported. | \C[2]、\N[1]、\V[3] などの制御文字は翻訳にそのまま残してください。これらを変更するインポートは報告されます。 | 请在译文中保留 \C[2]、\N[1]、\V[3] 等控制代码; 修改了这些代码的导入会被报告。 | \C[2], \N[1], \V[3] 같은 제어 코드는 번역문에 그대로 유지하세요. 이를 변경하는 가져오기는 보고됩니다.
No images to preview | プレビューする画像がありません | 没有可预览的图片 | 미리 볼 이미지가 없습니다
Warning: unexpected value type at: {0} | 警告: 予期しない値の型があります: {0} | 警告: 此处存在意外的值类型: {0} | 경고: 예상치 못한 값 형식: {0}
Text / Draft File | テキスト / ドラフトファイル | 文本 / 草稿文件 | 텍스트 / 초안 파일
Original Text | 原文 | 原文 | 원문
Draft loaded: {0} of {1} filled | ドラフトを読み込みました: {1} 件中 {0} 件入力済み | 已加载草稿: 已填写 {0} / {1} | 초안을 불러왔습니다: {1}개 중 {0}개 입력됨
Translation file written: {0} | 翻訳ファイルを書き込みました: {0} | 已写入翻译文件: {0} | 번역 파일을 기록했습니다: {0}
First letter of translations | 翻訳の先頭文字 | 译文的首字母 | 번역문의 첫 글자
Don't cut sentences (break lines only at spaces) | 文を途中で切らない (スペースの位置でのみ改行) | 不截断句子 (仅在空格处换行) | 문장을 자르지 않기 (공백에서만 줄바꿈)
Ignore upper/lower case | 大文字と小文字を区別しない | 忽略大小写 | 대소문자 무시
Original term | 原語 | 原术语 | 원어
Translation copied | 翻訳をコピーしました | 译文已复制 | 번역을 복사했습니다
No edits yet | 編集はまだありません | 尚无编辑 | 아직 편집 내용이 없습니다
Adds the control codes at the start and end of the original text (such as \C[6] or \!) to translations that lack them. Codes in the middle of a sentence are not placed; those rows keep the warning. | 原文の先頭と末尾にある制御文字 (\C[6] や \! など) が不足している翻訳に追加します。文中の制御文字は配置されないため、該当行には警告が残ります。 | 为缺少原文开头和结尾控制代码 (如 \C[6] 或 \!) 的译文补上这些代码。句子中间的代码不会被放置, 这些行仍会保留警告。 | 원문의 시작과 끝에 있는 제어 코드(\C[6], \! 등)가 없는 번역문에 이를 추가합니다. 문장 중간의 코드는 배치되지 않으며 해당 행에는 경고가 남습니다.
Saves the draft to the Output Folder (the same _draf file is overwritten) at the chosen interval, when the app goes to the background, and when you leave the editor. Needs an Output Folder. | 指定した間隔、アプリがバックグラウンドに移ったとき、エディタを離れるときに、ドラフトを出力フォルダに保存します (同じ _draf ファイルが上書きされます)。出力フォルダが必要です。 | 按所选间隔、应用转入后台时以及离开编辑器时, 将草稿保存到输出文件夹 (同一个 _draf 文件会被覆盖)。需要设置输出文件夹。 | 선택한 간격마다, 앱이 백그라운드로 전환될 때, 편집기를 나갈 때 초안을 출력 폴더에 저장합니다 (같은 _draf 파일을 덮어씁니다). 출력 폴더가 필요합니다.
{0} archives selected | {0} 個のアーカイブを選択中 | 已选择 {0} 个压缩包 | 아카이브 {0}개 선택됨
None of the selected files is an archive that can be extracted | 選択したファイルに、展開できるアーカイブはありません | 所选文件中没有可解包的压缩包 | 선택한 파일 중 풀 수 있는 아카이브가 없습니다
Replace | 置換 | 替换 | 바꾸기
Suggestions | 候補 | 建议 | 추천
✓ Matches this file's {0} | ✓ このファイルの{0}と一致します | ✓ 与此文件的{0}匹配 | ✓ 이 파일의 {0}와(과) 일치합니다
Continue with the next batch ({0} left) | 次のバッチに進む(残り{0}) | 继续处理下一批(剩余{0}) | 다음 배치 계속하기 ({0}개 남음)
Page {0} / {1} · {2} files | {0} / {1}ページ · {2}ファイル | 第 {0} / {1} 页 · {2} 个文件 | {0} / {1} 페이지 · 파일 {2}개
Scanning: {0} ({1}/{2}) | スキャン中: {0} ({1}/{2}) | 正在扫描: {0} ({1}/{2}) | 스캔 중: {0} ({1}/{2})
{0} · {1} files | {0} · {1}ファイル | {0} · {1} 个文件 | {0} · 파일 {1}개
Processing {0} of {1}: {2} | 処理中 {0}/{1}: {2} | 正在处理 {0}/{1}: {2} | 처리 중 {0}/{1}: {2}
{0} result files larger than their sources will be deleted (or the originals restored from _backup if the results replaced them). This cannot be undone. | 元ファイルより大きい結果ファイル{0}件が削除されます(結果で置き換えられている場合は_backupから元のファイルを復元します)。この操作は元に戻せません。 | 比源文件更大的 {0} 个结果文件将被删除(若结果已替换原文件,则从 _backup 还原原文件)。此操作无法撤销。 | 원본보다 큰 결과 파일 {0}개가 삭제됩니다(결과로 대체된 경우 _backup에서 원본을 복원합니다). 이 작업은 되돌릴 수 없습니다.
Result Files ({0}) | 結果ファイル({0}) | 结果文件({0}) | 결과 파일 ({0})
{0} file(s) checked · Incompatible: {1} · Warnings: {2} · Info: {3} | {0}ファイルをチェック · 非互換: {1} · 警告: {2} · 情報: {3} | 已检查 {0} 个文件 · 不兼容: {1} · 警告: {2} · 信息: {3} | 파일 {0}개 검사 · 비호환: {1} · 경고: {2} · 정보: {3}
{0} parts · {1}\n{2} … {3} | {0}パーツ · {1}\n{2} … {3} | {0} 个分卷 · {1}\n{2} … {3} | {0}개 파트 · {1}\n{2} … {3}
Done: {0} parts joined into {1} ({2}) in the output folder. | 完了: {0}個のパーツを結合して{1}({2})を出力フォルダーに作成しました。 | 完成: 已将 {0} 个分卷合并为 {1}({2}),保存在输出文件夹中。 | 완료: 파트 {0}개를 합쳐 {1}({2})을(를) 출력 폴더에 만들었습니다.
Restored: {0} files to their previous names. | 復元しました: {0}個のファイルを元の名前に戻しました。 | 已还原: {0} 个文件已恢复为原名称。 | 복원됨: 파일 {0}개를 이전 이름으로 되돌렸습니다.
checksum does NOT match on {0} | {0}のチェックサムが一致しません | {0} 的校验和不匹配 | {0}의 체크섬이 일치하지 않습니다
{0} files · {1} each | {0}ファイル · 各{1} | {0} 个文件 · 每个 {1} | 파일 {0}개 · 각 {1}
{0} files deleted, {1} could not be deleted. | {0}個のファイルを削除しました。{1}個は削除できませんでした。 | 已删除 {0} 个文件,{1} 个无法删除。 | 파일 {0}개를 삭제했고, {1}개는 삭제하지 못했습니다.
ASCII names with spaces/special marks: {0} | スペースや特殊記号を含むASCII名: {0} | 含空格/特殊符号的 ASCII 名称: {0} | 공백/특수 기호가 있는 ASCII 이름: {0}
{0} renamed files were not found in any text file (they may be loaded by scripts that build names, or simply unused). | 名前を変更した{0}個のファイルは、どのテキストファイルにも見つかりませんでした(名前を組み立てるスクリプトから読み込まれているか、単に未使用の可能性があります)。 | 在任何文本文件中都没有找到已重命名的 {0} 个文件(它们可能由拼接名称的脚本加载,或者只是未被使用)。 | 이름을 바꾼 파일 {0}개가 어떤 텍스트 파일에서도 발견되지 않았습니다(이름을 조합하는 스크립트에서 불러오거나 단순히 사용되지 않을 수 있습니다).
Editing references… ({0}/{1}) | 参照を編集中… ({0}/{1}) | 正在编辑引用… ({0}/{1}) | 참조 편집 중… ({0}/{1})
Restoring folder names… ({0}/{1}) | フォルダー名を復元中… ({0}/{1}) | 正在还原文件夹名称… ({0}/{1}) | 폴더 이름 복원 중… ({0}/{1})
Get {0} API Key | {0} APIキーを取得 | 获取 {0} API 密钥 | {0} API 키 받기
Connection OK. "Hello" → "{0}" | 接続OK。"Hello" → "{0}" | 连接正常。"Hello" → "{0}" | 연결 성공. "Hello" → "{0}"
Stopped: {0}. {1} row(s) already applied. | 停止しました: {0}。{1}行はすでに適用済みです。 | 已停止: {0}。已应用 {1} 行。 | 중지됨: {0}. {1}개 행은 이미 적용되었습니다.
Value "{0}" must be a number or true/false (skipped). | 値"{0}"は数値またはtrue/falseである必要があります(スキップしました)。 | 值"{0}"必须是数字或 true/false(已跳过)。 | 값 "{0}"은(는) 숫자 또는 true/false여야 합니다(건너뜀).
{0} text(s) exported to {1} in the output folder. | {0}件のテキストを出力フォルダーの{1}にエクスポートしました。 | 已将 {0} 条文本导出到输出文件夹中的 {1}。 | 텍스트 {0}개를 출력 폴더의 {1}(으)로 내보냈습니다.
View Warnings ({0}) | 警告を表示({0}) | 查看警告({0}) | 경고 보기 ({0})
Duplicate plugin name (positions {0} and {1}); the plugin header says it may be used more than once. | プラグイン名が重複しています(位置 {0} と {1})。プラグインのヘッダーには複数回使用できると記載されています。 | 插件名称重复(位置 {0} 和 {1});插件头部声明可以多次使用。 | 플러그인 이름이 중복됩니다(위치 {0}, {1}). 플러그인 헤더에 여러 번 사용할 수 있다고 되어 있습니다.
Needs base plugin "{0}" (@base) but it is disabled. | ベースプラグイン"{0}"(@base)が必要ですが、無効になっています。 | 需要基础插件"{0}"(@base),但它已被禁用。 | 기반 플러그인 "{0}"(@base)이(가) 필요하지만 비활성화되어 있습니다.
{0} problem(s) found | {0}件の問題が見つかりました | 发现 {0} 个问题 | 문제 {0}개 발견
Collect dialogue and choices from {0} into one CSV/TXT/JSON file | {0}のセリフと選択肢を1つのCSV/TXT/JSONファイルにまとめます | 将 {0} 中的对话和选项收集到一个 CSV/TXT/JSON 文件中 | {0}의 대사와 선택지를 하나의 CSV/TXT/JSON 파일로 모읍니다
{0}: {1} texts replaced | {0}: {1}件のテキストを置換しました | {0}: 已替换 {1} 条文本 | {0}: 텍스트 {1}개 교체됨
{0} files listed in {1} | {1}に{0}個のファイルが記載されています | {1} 中列出了 {0} 个文件 | {1}에 파일 {0}개가 나열되어 있습니다
{0} dialogue texts found | セリフのテキストが{0}件見つかりました | 找到 {0} 条对话文本 | 대사 텍스트 {0}개 발견
Searching covers ({0}/{1}): {2} | カバーを検索中 ({0}/{1}): {2} | 正在搜索封面 ({0}/{1}): {2} | 커버 검색 중 ({0}/{1}): {2}
App Settings | アプリ設定 | 应用设置 | 앱 설정
Input Folder (to be made into an archive) | 入力フォルダー(アーカイブ化するフォルダー) | 输入文件夹(将制作成压缩包) | 입력 폴더 (아카이브로 만들 폴더)
XOR key 0-255 (optional) | XORキー 0-255(任意) | XOR 密钥 0-255(可选) | XOR 키 0-255 (선택)
Theme mode | テーマモード | 主题模式 | 테마 모드
Indonesian | インドネシア語 | 印度尼西亚语 | 인도네시아어
Purple | パープル | 紫色 | 보라색
Detecting… | 検出中… | 正在检测… | 감지 중…
Cannot open the file | ファイルを開けません | 无法打开文件 | 파일을 열 수 없습니다
Error: {0} | エラー: {0} | 错误: {0} | 오류: {0}
Processing | 処理中 | 处理中 | 처리 중
Entire archive | アーカイブ全体 | 整个压缩包 | 아카이브 전체
XOR key | XORキー | XOR 密钥 | XOR 키
about {0} left | 残り約{0} | 约剩 {0} | 약 {0} 남음
Total size | 合計サイズ | 总大小 | 총 크기
No app can open this folder | このフォルダーを開けるアプリがありません | 没有可以打开此文件夹的应用 | 이 폴더를 열 수 있는 앱이 없습니다
Unsaved changes | 未保存の変更 | 未保存的更改 | 저장하지 않은 변경 사항
Language settings | 言語設定 | 语言设置 | 언어 설정
Remove default | デフォルトを解除 | 移除默认 | 기본값 해제
Choose language | 言語を選択 | 选择语言 | 언어 선택
Files checked | チェックしたファイル | 已检查的文件 | 검사한 파일
{0} of {1} match | {1}件中{0}件が一致 | {1} 项中有 {0} 项匹配 | {1}개 중 {0}개 일치
{0}: unreadable | {0}: 読み取れません | {0}: 无法读取 | {0}: 읽을 수 없음
This file cannot be shown as text | このファイルはテキストとして表示できません | 此文件无法以文本形式显示 | 이 파일은 텍스트로 표시할 수 없습니다
No files found | ファイルが見つかりません | 未找到文件 | 파일을 찾을 수 없습니다
Create a .{0} archive from a folder | フォルダーから.{0}アーカイブを作成 | 从文件夹创建 .{0} 压缩包 | 폴더에서 .{0} 아카이브 만들기
Decrypt/encrypt MV & MZ assets, unpack/pack XP, VX & VX Ace archives | MV・MZのアセットを復号/暗号化、XP・VX・VX Aceのアーカイブを展開/パック | 解密/加密 MV 和 MZ 资源,解包/打包 XP、VX 和 VX Ace 压缩包 | MV 및 MZ 에셋 복호화/암호화, XP, VX 및 VX Ace 아카이브 풀기/묶기
Decrypt Assets | アセットを復号 | 解密资源 | 에셋 복호화
Encrypt images and audio into .rpgmvp .rpgmvo .rpgmvm | 画像と音声を.rpgmvp .rpgmvo .rpgmvmに暗号化 | 将图片和音频加密为 .rpgmvp .rpgmvo .rpgmvm | 이미지와 오디오를 .rpgmvp .rpgmvo .rpgmvm으로 암호화
Import Save | セーブをインポート | 导入存档 | 세이브 가져오기
Value Editor | 値エディター | 数值编辑器 | 값 편집기
General | 一般 | 常规 | 일반
weapons | 武器 | 武器 | 무기
Variables | 変数 | 变量 | 변수
Pick the exported JSON (or the save file itself). Edit the values that appear below the Apply button, then press Apply. The result is written to the output folder with the same file name. | エクスポートしたJSON(またはセーブファイル本体)を選択します。適用ボタンの下に表示される値を編集し、適用を押してください。結果は同じファイル名で出力フォルダーに書き込まれます。 | 选择导出的 JSON(或存档文件本身)。编辑应用按钮下方显示的数值,然后点击应用。结果将以相同的文件名写入输出文件夹。 | 내보낸 JSON(또는 세이브 파일 자체)을 선택하세요. 적용 버튼 아래에 나타나는 값을 수정한 다음 적용을 누르세요. 결과는 같은 파일 이름으로 출력 폴더에 저장됩니다.
Key from System.json: {0} | System.jsonのキー: {0} | 来自 System.json 的密钥: {0} | System.json의 키: {0}
Decrypt | 復号 | 解密 | 복호화
Import | インポート | 导入 | 가져오기
RPGM MZ (zlib) | RPGM MZ (zlib) | RPGM MZ (zlib) | RPGM MZ (zlib)
Decryption finished | 復号が完了しました | 解密完成 | 복호화 완료
Values changed | 値を変更しました | 数值已更改 | 값이 변경되었습니다
No translatable text was found | 翻訳可能なテキストが見つかりませんでした | 未找到可翻译的文本 | 번역할 수 있는 텍스트를 찾지 못했습니다
Decrypting {0} files | {0}個のファイルを復号中 | 正在解密 {0} 个文件 | 파일 {0}개 복호화 중
Back up the original save (the original file gets a .bak extension) | 元のセーブをバックアップ(元のファイルに.bak拡張子が付きます) | 备份原存档(原文件将添加 .bak 扩展名) | 원본 세이브 백업 (원본 파일에 .bak 확장자가 붙습니다)
Names loaded: {0} | 読み込んだ名前: {0} | 已加载名称: {0} | 불러온 이름: {0}
Set {0} shown rows | 表示中の{0}行を設定 | 设置显示的 {0} 行 | 표시된 {0}개 행 설정
Items you don't own yet are listed with 0. Enter a number to add them. | 未所持のアイテムは0で表示されます。数値を入力すると追加できます。 | 尚未拥有的物品显示为 0。输入数字即可添加。 | 아직 가지고 있지 않은 아이템은 0으로 표시됩니다. 숫자를 입력하면 추가됩니다.
Empty / untranslated | 空 / 未翻訳 | 空 / 未翻译 | 비어 있음 / 미번역
Images: {0} · Audio: {1} · Other: {2} | 画像: {0} · 音声: {1} · その他: {2} | 图片: {0} · 音频: {1} · 其他: {2} | 이미지: {0} · 오디오: {1} · 기타: {2}
This image cannot be shown | この画像は表示できません | 无法显示此图片 | 이 이미지를 표시할 수 없습니다
Verification failed: the generated save could not be read back | 検証に失敗しました: 生成したセーブを読み戻せませんでした | 验证失败: 无法重新读取生成的存档 | 검증 실패: 생성된 세이브를 다시 읽을 수 없습니다
Pick the .txt/.csv/.json file from Export Text (RPG Maker or VN) (or a saved draft). Translation fields appear below the buttons. Save Draft keeps your work (file name gets _draf); Update Translation writes the finished file. | テキストのエクスポート(RPG MakerまたはVN)で作成した.txt/.csv/.jsonファイル(または保存した下書き)を選択します。翻訳欄はボタンの下に表示されます。下書き保存で作業内容を保存し(ファイル名に_drafが付きます)、翻訳を更新で完成したファイルを書き出します。 | 选择由导出文本(RPG Maker 或 VN)生成的 .txt/.csv/.json 文件(或已保存的草稿)。翻译栏位显示在按钮下方。保存草稿可保留你的工作(文件名会加上 _draf);更新翻译则写出完成的文件。 | 텍스트 내보내기(RPG Maker 또는 VN)에서 만든 .txt/.csv/.json 파일(또는 저장한 초안)을 선택하세요. 번역 입력란은 버튼 아래에 나타납니다. 초안 저장은 작업 내용을 보관하고(파일 이름에 _draf가 붙습니다), 번역 업데이트는 완성된 파일을 저장합니다.
Insert original text | 原文を挿入 | 插入原文 | 원문 삽입
File loaded: {0} entries | ファイルを読み込みました: {0}件 | 文件已加载: {0} 条 | 파일 불러옴: {0}개 항목
Translation Area | 翻訳エリア | 翻译区域 | 번역 영역
Uppercase | 大文字 | 大写 | 대문자
Replace words in translations | 翻訳内の単語を置換 | 替换译文中的词语 | 번역문의 단어 바꾸기
Whole words only | 単語全体のみ | 仅匹配整个单词 | 전체 단어만
No terms yet | 用語はまだありません | 暂无术语 | 아직 용어가 없습니다
Control codes differ from the original | 制御文字が原文と異なります | 控制代码与原文不同 | 제어 코드가 원문과 다릅니다
Clear History | 履歴を消去 | 清除历史记录 | 기록 지우기
Also fill empty fields (control codes only) | 空欄も埋める(制御文字のみ) | 同时填充空字段(仅控制代码) | 빈 입력란도 채우기 (제어 코드만)
On | オン | 开 | 켜짐
Archive {0}/{1}: {2} | アーカイブ {0}/{1}: {2} | 压缩包 {0}/{1}: {2} | 아카이브 {0}/{1}: {2}
Failed {0}: {1} | 失敗 {0}: {1} | 失败 {0}: {1} | 실패 {0}: {1}
{0} terms imported | {0}件の用語をインポートしました | 已导入 {0} 个术语 | 용어 {0}개를 가져왔습니다
Confirm exit: Editors | 終了の確認: エディター | 确认退出: 编辑器 | 종료 확인: 편집기
Checking {0}/{1}… | 確認中… {0}/{1} | 正在检查… {0}/{1} | 확인 중… {0}/{1}
Folder size: {0} | フォルダサイズ: {0} | 文件夹大小：{0} | 폴더 크기: {0}
Lines A: {0} · Lines B: {1} · same: {2} · added: {3} · removed: {4} | A の行数: {0} · B の行数: {1} · 同一: {2} · 追加: {3} · 削除: {4} | A 行数：{0} · B 行数：{1} · 相同：{2} · 新增：{3} · 删除：{4} | A 줄 수: {0} · B 줄 수: {1} · 동일: {2} · 추가: {3} · 삭제: {4}
{0} keys found · {1} files scanned | キー {0} 件を検出 · {1} ファイルをスキャン | 发现 {0} 个密钥 · 已扫描 {1} 个文件 | 키 {0}개 발견 · 파일 {1}개 검사
{0}: {1} files, {2} → ~{3} | {0}: {1} ファイル、{2} → 約{3} | {0}：{1} 个文件，{2} → 约 {3} | {0}: 파일 {1}개, {2} → 약 {3}
Converted: {0} · Unchanged: {1} · Skipped: {2} · Failed: {3} | 変換済み: {0} · 変更なし: {1} · スキップ: {2} · 失敗: {3} | 已转换：{0} · 未更改：{1} · 已跳过：{2} · 失败：{3} | 변환됨: {0} · 변경 없음: {1} · 건너뜀: {2} · 실패: {3}
Estimated result size: ~{0} from {1} ({2}) | 推定結果サイズ: 約{0}（元: {1}、{2}） | 预计结果大小：约 {0}（原始 {1}，{2}） | 예상 결과 크기: 약 {0} (원본 {1}, {2})
Built-in: {0} | 内蔵: {0} | 内置：{0} | 내장: {0}
Will make {0} parts ({1} each, last part {2}). | {0} 個のパーツを作成します（各 {1}、最後のパーツは {2}）。 | 将生成 {0} 个分卷（每个 {1}，最后一个为 {2}）。 | {0}개 파트를 만듭니다 (각 {1}, 마지막 파트 {2}).
"{0}" is not a numbered part (the name must end with .001, .002, …). | 「{0}」は番号付きパーツではありません（名前は .001、.002、… で終わる必要があります）。 | “{0}”不是带编号的分卷（名称必须以 .001、.002、… 结尾）。 | "{0}"은(는) 번호가 붙은 파트가 아닙니다 (이름은 .001, .002, …로 끝나야 합니다).
{0} files · {1} renamed · {2} unchanged · {3} with problems | {0} ファイル · {1} 名前変更 · {2} 変更なし · {3} 問題あり | {0} 个文件 · {1} 个已重命名 · {2} 个未更改 · {3} 个有问题 | 파일 {0}개 · 이름 변경 {1}개 · 변경 없음 {2}개 · 문제 {3}개
\nSHA-256 checksum saved as {0}.sha256. | \nSHA-256 チェックサムを {0}.sha256 として保存しました。 | \nSHA-256 校验和已保存为 {0}.sha256。 | \nSHA-256 체크섬을 {0}.sha256로 저장했습니다.
✗ {0} — {1} (the result may be corrupt) | ✗ {0} — {1}（結果が破損している可能性があります） | ✗ {0} — {1}（结果可能已损坏） | ✗ {0} — {1} (결과가 손상되었을 수 있습니다)
{0} files selected ({1}) | {0} ファイルを選択中（{1}） | 已选择 {0} 个文件（{1}） | 파일 {0}개 선택됨 ({1})
Offset {0} – {1} of {2} | オフセット {0} – {1} / {2} | 偏移量 {0} – {1} / {2} | 오프셋 {0} – {1} / {2}
Names too long (name > 100 or path > 200 characters): {0} | 名前が長すぎます（名前が100文字超、またはパスが200文字超）: {0} | 名称过长（名称超过 100 或路径超过 200 个字符）：{0} | 이름이 너무 깁니다 (이름 100자 초과 또는 경로 200자 초과): {0}
{0} text files skipped (not UTF-8 or too large). | テキストファイル {0} 件をスキップしました（UTF-8 以外、またはサイズ超過）。 | 已跳过 {0} 个文本文件（非 UTF-8 或过大）。 | 텍스트 파일 {0}개를 건너뛰었습니다 (UTF-8이 아니거나 너무 큼).
Renaming files… ({0}/{1}) | ファイル名を変更中… ({0}/{1}) | 正在重命名文件… ({0}/{1}) | 파일 이름 변경 중… ({0}/{1})
Done: {0} files and {1} folders renamed; {2} text files updated ({3} replacements). | 完了: ファイル {0} 件とフォルダ {1} 件の名前を変更、テキストファイル {2} 件を更新（{3} 件置換）。 | 完成：已重命名 {0} 个文件和 {1} 个文件夹；已更新 {2} 个文本文件（{3} 处替换）。 | 완료: 파일 {0}개와 폴더 {1}개 이름 변경, 텍스트 파일 {2}개 업데이트 ({3}건 치환).
{0} model(s) loaded. | {0} 個のモデルを読み込みました。 | 已加载 {0} 个模型。 | 모델 {0}개를 불러왔습니다.
Connection failed: {0} | 接続に失敗しました: {0} | 连接失败：{0} | 연결 실패: {0}
{0} plugin(s) scanned · {1} text(s) · {2} editable value(s) | プラグイン {0} 件をスキャン · テキスト {1} 件 · 編集可能な値 {2} 件 | 已扫描 {0} 个插件 · {1} 条文本 · {2} 个可编辑值 | 플러그인 {0}개 검사 · 텍스트 {1}개 · 편집 가능한 값 {2}개
{0} plugin(s) · {1} enabled | プラグイン {0} 件 · 有効 {1} 件 | {0} 个插件 · {1} 个已启用 | 플러그인 {0}개 · 활성 {1}개
{0} translation(s) filled into the editor fields. Review, then press Apply. | {0} 件の翻訳をエディタの欄に入力しました。確認してから「適用」を押してください。 | 已将 {0} 条翻译填入编辑器字段。请检查后点击“应用”。 | 번역 {0}개를 편집기 필드에 채웠습니다. 확인 후 '적용'을 누르세요.
{0} error(s) · {1} warning(s) · {2} info | エラー {0} 件 · 警告 {1} 件 · 情報 {2} 件 | {0} 个错误 · {1} 个警告 · {2} 条信息 | 오류 {0}개 · 경고 {1}개 · 정보 {2}개
Duplicate plugin name (positions {0} and {1}) with different parameters; likely intentional instances. | プラグイン名が重複しています（位置 {0} と {1}）。パラメータが異なるため、意図的なインスタンスの可能性があります。 | 插件名称重复（位置 {0} 和 {1}），参数不同；可能是有意创建的多个实例。 | 플러그인 이름이 중복됩니다 (위치 {0} 및 {1}). 매개변수가 달라 의도된 인스턴스일 수 있습니다.
"{0}" (@base) must load before this plugin; move it up. | 「{0}」(@base) はこのプラグインより先に読み込む必要があります。上に移動してください。 | “{0}”（@base）必须先于此插件加载；请将其上移。 | "{0}"(@base)은(는) 이 플러그인보다 먼저 로드되어야 합니다. 위로 이동하세요.
Missing from archive: {0} | アーカイブにありません: {0} | 压缩包中缺失：{0} | 아카이브에 없음: {0}
Put a CSV/TXT/JSON translation back into {0} | CSV/TXT/JSON の翻訳を {0} に書き戻します | 将 CSV/TXT/JSON 翻译写回 {0} | CSV/TXT/JSON 번역을 {0}에 다시 넣기
Decompiled: {0} | 逆コンパイル済み: {0} | 已反编译：{0} | 디컴파일됨: {0}
FuruArc is a free app independently developed by Fueeru.\n\nWe would like to thank all the developers and communities of the open-source projects that provided the libraries, tools and source code that support the development of this app. Information about the source and licence of each component can be found in the Source menu.\n\nCode that has been adapted or integrated remains subject to the licence terms of its original project. Please refer to each repository for further licence information.\n\nThis app is intended for personal use, such as translating and modifying games that you legally own. All trademarks, names and copyrights of games and third-party assets remain the property of their respective owners. | FuruArc は Fueeru が独自に開発した無料アプリです。\n\n本アプリの開発を支えてくださったライブラリ、ツール、ソースコードを提供してくださったすべてのオープンソースプロジェクトの開発者とコミュニティの皆さまに感謝いたします。各コンポーネントの出典とライセンスについては、Source メニューをご覧ください。\n\n改変または組み込みを行ったコードは、元のプロジェクトのライセンス条件に従います。詳しいライセンス情報は、各リポジトリをご確認ください。\n\n本アプリは、正規に所有しているゲームの翻訳や改造など、個人利用を目的としています。ゲームおよび第三者のアセットに関するすべての商標、名称、著作権は、それぞれの権利者に帰属します。 | FuruArc 是由 Fueeru 独立开发的免费应用。\n\n衷心感谢所有开源项目的开发者和社区，他们提供的库、工具和源代码为本应用的开发提供了支持。各组件的来源和许可证信息可在 Source 菜单中查看。\n\n经过改编或集成的代码仍受其原项目许可证条款的约束。更多许可证信息请参阅各代码仓库。\n\n本应用仅供个人使用，例如翻译和修改您合法拥有的游戏。游戏及第三方资源的所有商标、名称和版权均归其各自权利人所有。 | FuruArc는 Fueeru가 독자적으로 개발한 무료 앱입니다.\n\n이 앱의 개발을 뒷받침해 준 라이브러리, 도구, 소스 코드를 제공해 주신 모든 오픈소스 프로젝트의 개발자와 커뮤니티에 감사드립니다. 각 구성 요소의 출처와 라이선스 정보는 Source 메뉴에서 확인할 수 있습니다.\n\n수정하거나 통합한 코드는 원래 프로젝트의 라이선스 조건을 따릅니다. 자세한 라이선스 정보는 각 저장소를 참고하세요.\n\n이 앱은 본인이 합법적으로 소유한 게임의 번역 및 수정 같은 개인 용도로 만들어졌습니다. 게임 및 제3자 에셋의 모든 상표, 이름, 저작권은 각 권리자에게 귀속됩니다.
Could not read: {0} | 読み取れませんでした: {0} | 无法读取：{0} | 읽을 수 없음: {0}
Covers found: {0} of {1} games | カバー検出: {1} 本中 {0} 本 | 已找到封面：{0} / {1} 个游戏 | 표지 발견: 게임 {1}개 중 {0}개
Theme, color, language and system | テーマ、色、言語、システム | 主题、颜色、语言和系统 | 테마, 색상, 언어 및 시스템
Output Directory | 出力フォルダ | 输出目录 | 출력 폴더
XOR key 0-255 (empty = auto) | XOR キー 0-255（空欄 = 自動） | XOR 密钥 0-255（留空 = 自动） | XOR 키 0-255 (비워 두면 자동)
Dark | ダーク | 深色 | 다크
Blue | ブルー | 蓝色 | 블루
Pink | ピンク | 粉色 | 핑크
Unrecognized | 認識できません | 无法识别 | 인식할 수 없음
Invalid folder | 無効なフォルダ | 无效的文件夹 | 잘못된 폴더
Done: {0} succeeded, {1} failed ({2} s) | 完了: 成功 {0} 件、失敗 {1} 件（{2} 秒） | 完成：成功 {0} 个，失败 {1} 个（{2} 秒） | 완료: 성공 {0}개, 실패 {1}개 ({2}초)
Cancel process? | 処理をキャンセルしますか? | 要取消处理吗？ | 작업을 취소할까요?
Partial | 一部 | 部分 | 부분
0x%02X (auto-detected) | 0x%02X（自動検出） | 0x%02X（自动检测） | 0x%02X (자동 감지)
Extraction finished | 展開が完了しました | 解压完成 | 추출 완료
Time | 時間 | 时间 | 시간
Ignore when creating archives (comma separated, * allowed) | アーカイブ作成時に除外（カンマ区切り、* 使用可） | 创建压缩包时忽略（逗号分隔，可用 *） | 아카이브 생성 시 제외 (쉼표로 구분, * 사용 가능)
Settings have not been saved, are you sure you want to leave? | 設定が保存されていません。このまま離れますか? | 设置尚未保存，确定要离开吗？ | 설정이 저장되지 않았습니다. 정말 나가시겠어요?
System settings | システム設定 | 系统设置 | 시스템 설정
Background processing | バックグラウンド処理 | 后台处理 | 백그라운드 처리
Open with | アプリで開く | 打开方式 | 다른 앱으로 열기
Size matches | サイズ一致 | 大小匹配 | 크기 일치
Not available for this format | この形式では利用できません | 此格式不可用 | 이 형식에서는 사용할 수 없습니다
{0}: size {1}, expected {2} | {0}: サイズ {1}、期待値 {2} | {0}：大小 {1}，预期 {2} | {0}: 크기 {1}, 예상 {2}
Search files or folders… | ファイルまたはフォルダを検索… | 搜索文件或文件夹… | 파일 또는 폴더 검색…
Tap the name of an image, audio or text file to preview it; use the checkbox to select | 画像・音声・テキストファイルの名前をタップするとプレビューできます。選択にはチェックボックスを使います | 点按图片、音频或文本文件的名称即可预览；使用复选框进行选择 | 이미지, 오디오, 텍스트 파일의 이름을 탭하면 미리 볼 수 있습니다. 선택하려면 체크박스를 사용하세요
Wrong file: this is not a {0} archive | ファイルが違います: {0} アーカイブではありません | 文件错误：这不是 {0} 压缩包 | 잘못된 파일: {0} 아카이브가 아닙니다
Data Translation | データ翻訳 | 数据翻译 | 데이터 번역
Encrypt Assets | アセットを暗号化 | 加密资源 | 에셋 암호화
Collect dialog and names from data/*.json into one file | data/*.json からセリフと名前を 1 つのファイルにまとめます | 将 data/*.json 中的对话和名称汇总到一个文件 | data/*.json의 대사와 이름을 하나의 파일로 모읍니다
Turn the edited JSON back into a save file | 編集した JSON をセーブファイルに戻します | 将编辑后的 JSON 转换回存档文件 | 편집한 JSON을 세이브 파일로 되돌립니다
Edit gold, characters, items, variables and switches of a save | セーブデータの所持金、キャラクター、アイテム、変数、スイッチを編集します | 编辑存档中的金币、角色、物品、变量和开关 | 세이브의 골드, 캐릭터, 아이템, 변수, 스위치를 편집합니다
chars | 文字 | 字符 | 글자
Weapons | 武器 | 武器 | 무기
Switches | スイッチ | 开关 | 스위치
Upload System.json (Optional) | System.json をアップロード（任意） | 上传 System.json（可选） | System.json 업로드 (선택)
Key detected from an image: {0} | 画像からキーを検出しました: {0} | 已从图片中检测到密钥：{0} | 이미지에서 키 감지됨: {0}
Encrypt | 暗号化 | 加密 | 암호화
Result: terjemahan.csv, .txt or .json in the output folder. | 結果: 出力フォルダに terjemahan.csv、.txt、または .json が作成されます。 | 结果：输出目录中会生成 terjemahan.csv、.txt 或 .json。 | 결과: 출력 폴더에 terjemahan.csv, .txt 또는 .json이 생성됩니다.
RPGM MV (LZString) | RPGM MV (LZString) | RPGM MV (LZString) | RPGM MV (LZString)
Encryption finished | 暗号化が完了しました | 加密完成 | 암호화 완료
No matching asset files found in this folder | このフォルダに該当するアセットファイルが見つかりません | 此文件夹中未找到匹配的资源文件 | 이 폴더에서 일치하는 에셋 파일을 찾을 수 없습니다
The translation file has no translations to apply | 翻訳ファイルに適用できる翻訳がありません | 翻译文件中没有可应用的翻译 | 번역 파일에 적용할 번역이 없습니다
System.json updated (key and encrypted flags) | System.json を更新しました（キーと暗号化フラグ） | System.json 已更新（密钥和加密标志） | System.json이 업데이트되었습니다 (키 및 암호화 플래그)
Backup created: {0} | バックアップを作成しました: {0} | 已创建备份：{0} | 백업을 만들었습니다: {0}
No database files (System, Items, Weapons, Armors, Actors) were found in this folder | このフォルダにデータベースファイル（System、Items、Weapons、Armors、Actors）が見つかりません | 此文件夹中未找到数据库文件（System、Items、Weapons、Armors、Actors） | 이 폴더에서 데이터베이스 파일(System, Items, Weapons, Armors, Actors)을 찾을 수 없습니다
All ON (shown) | すべて ON（表示中） | 全部开启（当前显示） | 모두 ON (표시 중)
No rows to change | 変更する行がありません | 没有要更改的行 | 변경할 행이 없습니다
Control code warnings | 制御文字の警告 | 控制码警告 | 제어 코드 경고
Preview image | プレビュー画像 | 预览图片 | 미리보기 이미지
The JSON must be an object ({ … }) like an RPG Maker save | JSON は RPG Maker のセーブのようにオブジェクト（{ … }）である必要があります | JSON 必须是对象（{ … }），就像 RPG Maker 存档一样 | JSON은 RPG Maker 세이브처럼 객체({ … })여야 합니다
Upload a System.json file to get the encryption key automatically | System.json ファイルをアップロードすると、暗号化キーを自動取得します | 上传 System.json 文件即可自动获取加密密钥 | System.json 파일을 업로드하면 암호화 키를 자동으로 가져옵니다
Save Draft | 下書きを保存 | 保存草稿 | 임시 저장
Original text copied | 元のテキストをコピーしました | 已复制原文 | 원문을 복사했습니다
Translation file written | 翻訳ファイルを書き出しました | 翻译文件已写入 | 번역 파일을 작성했습니다
Save Data Editing | セーブデータ編集 | 存档数据编辑 | 세이브 데이터 편집
Lowercase | 小文字 | 小写 | 소문자
{0} texts changed | {0} 件のテキストを変更 | 已更改 {0} 条文本 | 텍스트 {0}개 변경됨
Matches: {0} in {1} texts | 一致: {1} 件のテキスト中 {0} 件 | 匹配：{1} 条文本中 {0} 处 | 일치: 텍스트 {1}개 중 {0}건
Fill identical entries | 同一エントリを一括入力 | 填充相同条目 | 동일 항목 채우기
Edit History | 編集履歴 | 编辑历史 | 편집 기록
Showing the latest {0} of {1} edits. Tap an entry to open it. | 最新 {0} 件を表示中（全 {1} 件）。項目をタップすると開きます。 | 显示最近 {0} 条，共 {1} 条编辑。点按条目即可打开。 | 전체 {1}개 중 최근 {0}개 편집을 표시 중입니다. 항목을 탭하면 열립니다.
Native text input | ネイティブテキスト入力 | 原生文本输入 | 네이티브 텍스트 입력
{0} min | {0} 分 | {0} 分钟 | {0}분
{0} ({1} skipped) | {0}（{1} 件スキップ） | {0}（跳过 {1} 个） | {0} ({1}개 건너뜀)
Import / export glossary (CSV) | 用語集のインポート/エクスポート（CSV） | 导入/导出术语表（CSV） | 용어집 가져오기/내보내기 (CSV)
{0} terms exported | {0} 件の用語をエクスポートしました | 已导出 {0} 个术语 | 용어 {0}개를 내보냈습니다
Ask before leaving any editor (Translation Editor, Save Editor, Plugin Scanner, Plugin.js Editor) when there are unsaved changes | 未保存の変更があるとき、エディタ（翻訳エディタ、セーブエディタ、プラグインスキャナ、Plugin.js エディタ）を閉じる前に確認します | 当有未保存的更改时，离开任何编辑器（翻译编辑器、存档编辑器、插件扫描器、Plugin.js 编辑器）前先询问 | 저장하지 않은 변경 사항이 있을 때 편집기(번역 편집기, 세이브 편집기, 플러그인 스캐너, Plugin.js 편집기)를 나가기 전에 확인합니다
{0} files checked · {1} healthy · {2} warnings · {3} problems | {0}個のファイルを確認 · 正常 {1} · 警告 {2} · 問題 {3} | 已检查 {0} 个文件 · 正常 {1} · 警告 {2} · 问题 {3} | 파일 {0}개 확인 · 정상 {1} · 경고 {2} · 문제 {3}
{0} files in {1} folders | {1}個のフォルダに{0}個のファイル | {1} 个文件夹中共 {0} 个文件 | 폴더 {1}개에 파일 {0}개
Entries A/B: {0} / {1} · Filled A/B: {2} / {3} | エントリ A/B: {0} / {1} · 入力済み A/B: {2} / {3} | 条目 A/B：{0} / {1} · 已填写 A/B：{2} / {3} | 항목 A/B: {0} / {1} · 입력됨 A/B: {2} / {3}
Encrypted files ({0}) | 暗号化されたファイル ({0}) | 已加密文件 ({0}) | 암호화된 파일 ({0})
{0} file(s) selected · {1} | {0}個のファイルを選択中 · {1} | 已选择 {0} 个文件 · {1} | 파일 {0}개 선택됨 · {1}
Size: {0} → {1} | サイズ: {0} → {1} | 大小：{0} → {1} | 크기: {0} → {1}
{0}% · about {1} left | {0}% · 残り約{1} | {0}% · 约剩 {1} | {0}% · 약 {1} 남음
Sample: {0} | サンプル: {0} | 示例：{0} | 샘플: {0}
"""
    const val T4 = """
Too many parts (maximum {0}). Increase the part size. | パーツが多すぎます (最大 {0})。パーツサイズを大きくしてください。 | 分块过多（最多 {0} 个）。请增大分块大小。 | 파트가 너무 많습니다 (최대 {0}개). 파트 크기를 늘려 주세요.
"{0}" belongs to a different file. Pick parts of one file only. | 「{0}」は別のファイルに属しています。1つのファイルのパーツのみを選んでください。 | “{0}”属于另一个文件。请只选择同一个文件的分块。 | "{0}"은(는) 다른 파일의 파트입니다. 한 파일의 파트만 선택해 주세요.
Rename {0} files? | {0}個のファイルの名前を変更しますか? | 要重命名 {0} 个文件吗？ | 파일 {0}개의 이름을 바꿀까요?
{0} parts · {1} | {0}パーツ · {1} | {0} 个分块 · {1} | 파트 {0}개 · {1}
✗ {0} — failed: {1} | ✗ {0} — 失敗: {1} | ✗ {0} — 失败：{1} | ✗ {0} — 실패: {1}
Delete {0} selected files | 選択した{0}個のファイルを削除 | 删除所选的 {0} 个文件 | 선택한 파일 {0}개 삭제
Found at offset {0} (shown on the first row). | オフセット {0} で見つかりました (先頭行に表示)。 | 在偏移 {0} 处找到（显示在第一行）。 | 오프셋 {0}에서 찾았습니다 (첫 번째 행에 표시).
Names that differ only in letter case (clash on Windows/Android): {0} | 大文字小文字のみが異なる名前 (Windows/Androidで衝突): {0} | 仅大小写不同的名称（在 Windows/Android 上会冲突）：{0} | 대소문자만 다른 이름 (Windows/Android에서 충돌): {0}
Page {0} / {1} · {2} names | ページ {0} / {1} · {2}件の名前 | 第 {0} / {1} 页 · {2} 个名称 | 페이지 {0} / {1} · 이름 {2}개
Renaming folders… ({0}/{1}) | フォルダ名を変更中… ({0}/{1}) | 正在重命名文件夹…（{0}/{1}） | 폴더 이름 변경 중… ({0}/{1})
{0} original text files backed up to {1}. | 元のテキストファイル{0}個を{1}にバックアップしました。 | 已将 {0} 个原始文本文件备份到 {1}。 | 원본 텍스트 파일 {0}개를 {1}에 백업했습니다.
Could not load models: {0} | モデルを読み込めませんでした: {0} | 无法加载模型：{0} | 모델을 불러오지 못했습니다: {0}
Cache: {0} entries | キャッシュ: {0}件 | 缓存：{0} 条 | 캐시: {0}개 항목
{0} parameter(s) · {1} text(s) · {2} value(s) | パラメータ {0} · テキスト {1} · 値 {2} | {0} 个参数 · {1} 条文本 · {2} 个值 | 매개변수 {0}개 · 텍스트 {1}개 · 값 {2}개
Ready to add: {0} | 追加可能: {0} | 可添加：{0} | 추가 가능: {0}
Plugin "{0}" is removed from the list (its plugin file is not deleted). | プラグイン「{0}」を一覧から削除します (プラグインファイル自体は削除されません)。 | 插件“{0}”将从列表中移除（不会删除其插件文件）。 | 플러그인 "{0}"을(를) 목록에서 제거합니다 (플러그인 파일은 삭제되지 않습니다).
{0} difference(s) | {0}件の差異 | {0} 处差异 | 차이 {0}개
{0} files are in the plugin folder but not registered in plugins.js (tap to see the list). | {0}個のファイルがプラグインフォルダにありますが plugins.js に登録されていません (タップで一覧表示)。 | 插件文件夹中有 {0} 个文件未在 plugins.js 中注册（点按查看列表）。 | 플러그인 폴더에 있지만 plugins.js에 등록되지 않은 파일이 {0}개 있습니다 (탭하면 목록 표시).
Should load after "{0}" (@orderAfter) but the order is reversed. | 「{0}」(@orderAfter) の後に読み込まれるべきですが、順序が逆になっています。 | 应在“{0}”(@orderAfter) 之后加载，但顺序颠倒了。 | "{0}"(@orderAfter) 뒤에 로드되어야 하지만 순서가 반대입니다.
In the archive but not in the source folder: {0} | アーカイブにはありますが、ソースフォルダにはありません: {0} | 在压缩包中但不在源文件夹中：{0} | 아카이브에는 있지만 소스 폴더에는 없음: {0}
Found {0} files ({1}) | {0}個のファイルが見つかりました ({1}) | 找到 {0} 个文件（{1}） | 파일 {0}개를 찾았습니다 ({1})
{0} files could not be decompiled (see log) | {0}個のファイルを逆コンパイルできませんでした (ログを参照) | {0} 个文件无法反编译（见日志） | 파일 {0}개를 디컴파일하지 못했습니다 (로그 참조)
Only the "{0}" entry in the Game Assets list is removed. The real folder and its files in storage are NOT deleted. | ゲームアセット一覧から「{0}」の項目のみが削除されます。ストレージ内の実際のフォルダとファイルは削除されません。 | 仅会从游戏资源列表中移除“{0}”条目。存储空间中的实际文件夹及其文件不会被删除。 | 게임 에셋 목록에서 "{0}" 항목만 제거됩니다. 저장소의 실제 폴더와 파일은 삭제되지 않습니다.
Using {0}. Holds temporary files for previews; safe to delete. | {0} を使用中。プレビュー用の一時ファイルを保持します。削除しても問題ありません。 | 正在使用 {0}。用于存放预览的临时文件，可以安全删除。 | {0}을(를) 사용 중입니다. 미리보기용 임시 파일을 보관하며, 삭제해도 안전합니다.
{0} / {1} files | {0} / {1} 個のファイル | {0} / {1} 个文件 | 파일 {0} / {1}개
Back | 戻る | 返回 | 뒤로
Output filename | 出力ファイル名 | 输出文件名 | 출력 파일 이름
Compress (zlib) | 圧縮 (zlib) | 压缩 (zlib) | 압축 (zlib)
Theme color | テーマカラー | 主题颜色 | 테마 색상
Light blue | ライトブルー | 浅蓝色 | 하늘색
Orange | オレンジ | 橙色 | 주황색
Unrecognized format (check magic bytes) | 認識できない形式です (マジックバイトを確認してください) | 无法识别的格式（请检查魔数） | 인식할 수 없는 형식입니다 (매직 바이트를 확인하세요)
Failed to write the output file | 出力ファイルの書き込みに失敗しました | 写入输出文件失败 | 출력 파일을 쓰지 못했습니다
{0} files will be packed into {1} | {0}個のファイルを{1}にパックします | 将把 {0} 个文件打包到 {1} | 파일 {0}개를 {1}(으)로 묶습니다
A process is running. Are you sure you want to cancel? | 処理を実行中です。キャンセルしますか? | 有任务正在运行。确定要取消吗？ | 작업이 실행 중입니다. 취소하시겠습니까?
{0} files and {1} folders selected | {0}個のファイルと{1}個のフォルダを選択中 | 已选择 {0} 个文件和 {1} 个文件夹 | 파일 {0}개와 폴더 {1}개 선택됨
None (not encrypted) | なし (暗号化なし) | 无（未加密） | 없음 (암호화 안 됨)
Archive created | アーカイブを作成しました | 压缩包已创建 | 아카이브를 만들었습니다
Open folder | フォルダを開く | 打开文件夹 | 폴더 열기
{0} files · {1} (before compression) | {0}個のファイル · {1} (圧縮前) | {0} 个文件 · {1}（压缩前） | 파일 {0}개 · {1} (압축 전)
Yes, leave | はい、離れる | 是，离开 | 예, 나갑니다
If the file already exists when exporting | エクスポート時にファイルが既に存在する場合 | 导出时如果文件已存在 | 내보낼 때 파일이 이미 있는 경우
Keeps running when the app is closed or the screen is off, and shows a notification | アプリを閉じたり画面を消したりしても実行を続け、通知を表示します | 关闭应用或熄屏后仍会继续运行，并显示通知 | 앱을 닫거나 화면이 꺼져도 계속 실행되며 알림을 표시합니다
Check extracted files | 展開したファイルを確認 | 检查已解包的文件 | 푼 파일 확인
Unreadable | 読み取れません | 无法读取 | 읽을 수 없음
All files look intact | すべてのファイルは問題ありません | 所有文件看起来都完好 | 모든 파일이 온전해 보입니다
{0}: checksum mismatch | {0}: チェックサムが一致しません | {0}：校验和不匹配 | {0}: 체크섬이 일치하지 않습니다
{0} results | {0}件の結果 | {0} 条结果 | 결과 {0}개
VN Tools | VN Tools | VN Tools | VN Tools
Wrong file: detected {0}, not {1} | ファイルが違います: {1} ではなく {0} が検出されました | 文件不正确：检测到 {0}，而不是 {1} | 잘못된 파일: {1}이(가) 아니라 {0}이(가) 감지되었습니다
Export game text to a file for translation, then import it back | ゲームのテキストを翻訳用にファイルへ書き出し、後で取り込みます | 将游戏文本导出为文件以便翻译，之后再导入回去 | 번역을 위해 게임 텍스트를 파일로 내보낸 뒤 다시 가져옵니다
Turn .png_ .ogg_ .m4a_ files back into normal files | .png_ .ogg_ .m4a_ ファイルを通常のファイルに戻します | 将 .png_ .ogg_ .m4a_ 文件还原为普通文件 | .png_ .ogg_ .m4a_ 파일을 일반 파일로 되돌립니다
Write the translated text back into data/*.json | 翻訳したテキストを data/*.json に書き戻します | 将翻译后的文本写回 data/*.json | 번역한 텍스트를 data/*.json에 다시 씁니다
Data folder | データフォルダ | 数据文件夹 | 데이터 폴더
Save File / JSON | セーブファイル / JSON | 存档文件 / JSON | 저장 파일 / JSON
Characters | キャラクター | 角色 | 캐릭터
armors | armors | armors | armors
No editable values were found in this file | このファイルには編集可能な値が見つかりませんでした | 此文件中未找到可编辑的值 | 이 파일에서 편집할 수 있는 값을 찾지 못했습니다
This is not a System.json file | System.json ファイルではありません | 这不是 System.json 文件 | System.json 파일이 아닙니다
No key found yet | キーはまだ見つかっていません | 尚未找到密钥 | 아직 키를 찾지 못했습니다
Only images in img/ and audio in audio/ are encrypted. System.json in the output folder is updated with the key. | img/ の画像と audio/ の音声のみが暗号化されます。出力フォルダの System.json にキーが反映されます。 | 仅加密 img/ 中的图片和 audio/ 中的音频。输出文件夹中的 System.json 会更新为新密钥。 | img/의 이미지와 audio/의 오디오만 암호화됩니다. 출력 폴더의 System.json에 키가 반영됩니다.
Choose the same data folder you exported from. Translated files go to the output folder (choose the data folder itself to overwrite). | エクスポート元と同じデータフォルダを選んでください。翻訳済みファイルは出力フォルダに保存されます (上書きする場合はデータフォルダ自体を選択)。 | 请选择导出时使用的同一个数据文件夹。翻译后的文件会保存到输出文件夹（选择数据文件夹本身则会覆盖原文件）。 | 내보낼 때 사용한 것과 같은 데이터 폴더를 선택하세요. 번역된 파일은 출력 폴더에 저장됩니다 (덮어쓰려면 데이터 폴더 자체를 선택).
Save format to create | 作成するセーブ形式 | 要创建的存档格式 | 만들 저장 형식
Save exported | セーブをエクスポートしました | 存档已导出 | 저장 파일을 내보냈습니다
The key must be exactly 32 hex characters (0-9, a-f) | キーは正確に32桁の16進数 (0-9, a-f) である必要があります | 密钥必须恰好为 32 个十六进制字符 (0-9, a-f) | 키는 정확히 32자리 16진수 문자(0-9, a-f)여야 합니다
No translation matched the game files. Check that this is the same folder that was exported | ゲームファイルに一致する翻訳がありませんでした。エクスポートしたときと同じフォルダか確認してください | 没有翻译与游戏文件匹配。请检查是否为导出时使用的同一个文件夹 | 게임 파일과 일치하는 번역이 없습니다. 내보낸 것과 같은 폴더인지 확인하세요
{0} texts written to {1} | {0}件のテキストを{1}に書き込みました | 已将 {0} 条文本写入 {1} | 텍스트 {0}개를 {1}에 썼습니다
No existing save to back up | バックアップする既存のセーブがありません | 没有可备份的现有存档 | 백업할 기존 저장 파일이 없습니다
Search name, ID or value | 名前、ID、値で検索 | 搜索名称、ID 或值 | 이름, ID 또는 값 검색
All OFF (shown) | すべてOFF (表示中) | 全部关闭（当前显示的） | 모두 끄기 (표시된 항목)
Entries in file | ファイル内のエントリ | 文件中的条目 | 파일 내 항목
Control codes differ: {0} | 制御文字が異なります: {0} | 控制码不一致：{0} | 제어 코드가 다릅니다: {0}
Next image | 次の画像 | 下一张图片 | 다음 이미지
This does not look like an RPG Maker save (no party, actors, variables or switches) | RPG Maker のセーブデータではないようです (パーティ、アクター、変数、スイッチがありません) | 这似乎不是 RPG Maker 存档（没有队伍、角色、变量或开关） | RPG Maker 저장 파일이 아닌 것 같습니다 (파티, 액터, 변수, 스위치가 없음)
Translation Editor | 翻訳エディター | 翻译编辑器 | 번역 편집기
Update Translation | 翻訳を更新 | 更新翻译 | 번역 업데이트
No translatable entries were found in this file | このファイルには翻訳可能なエントリが見つかりませんでした | 此文件中未找到可翻译的条目 | 이 파일에서 번역할 수 있는 항목을 찾지 못했습니다
Entries written | 書き込んだエントリ | 已写入的条目 | 쓴 항목
Filled {0} of {1} - {2}% / 100% | {1}個中{0}個入力済み - {2}% / 100% | 已填写 {0} / {1} - {2}% / 100% | {1}개 중 {0}개 입력됨 - {2}% / 100%
Auto wrap translations | 翻訳を自動改行 | 自动换行翻译 | 번역 자동 줄바꿈
Undo last bulk change | 直前の一括変更を元に戻す | 撤销上一次批量更改 | 마지막 일괄 변경 되돌리기
Glossary | 用語集 | 术语表 | 용어집
Empty entries whose original text is exactly the same as a translated entry get the same translation. | 原文がまったく同じ翻訳済みエントリがある場合、未入力のエントリに同じ翻訳を設定します。 | 原文与某个已翻译条目完全相同的空条目，将采用相同的翻译。 | 원문이 번역된 항목과 완전히 같은 빈 항목에는 같은 번역이 적용됩니다.
New | 新規 | 新建 | 새로 만들기
{0} edits. Tap an entry to open it. | {0}件の編集。エントリをタップして開きます。 | {0} 处编辑。点按条目即可打开。 | {0}개 편집됨. 항목을 탭하면 열립니다.
Use Android's built-in text fields to avoid bugs instead of the app's own text fields | アプリ独自のテキストフィールドではなく Android 標準のテキストフィールドを使用して不具合を避ける | 使用 Android 内置文本框而非应用自带文本框，以避免出现问题 | 앱 자체 텍스트 필드 대신 Android 기본 텍스트 필드를 사용하여 버그를 방지합니다
Auto-saved {0} | {0} を自動保存しました | 已自动保存 {0} | {0} 자동 저장됨
Archives | アーカイブ | 压缩包 | 아카이브
Simple CSV with two columns: original term, translation. | 2列のシンプルなCSV: 原語、訳語。 | 两列的简单 CSV：原词、译词。 | 두 열로 된 간단한 CSV: 원어, 번역어.
No terms to use | 使用する用語がありません | 没有可用的术语 | 사용할 용어가 없습니다
There are unsaved changes. Leave anyway? | 未保存の変更があります。このまま離れますか? | 有未保存的更改。仍要离开吗？ | 저장하지 않은 변경 사항이 있습니다. 그래도 나가시겠습니까?
{0} plain image/audio/video files skipped | 通常の画像/音声/動画ファイル {0} 件をスキップしました | 已跳过 {0} 个普通图片/音频/视频文件 | 일반 이미지/오디오/비디오 파일 {0}개를 건너뛰었습니다
{0} empty files (0 bytes) | 空のファイル {0} 件 (0 バイト) | {0} 个空文件（0 字节） | 빈 파일 {0}개 (0바이트)
Done: {0} ({1}) saved in the output folder. | 完了: {0} ({1}) を出力フォルダに保存しました。 | 完成：{0}（{1}）已保存到输出文件夹。 | 완료: {0}({1})을(를) 출력 폴더에 저장했습니다.
Page {0} / {1} | ページ {0} / {1} | 第 {0} / {1} 页 | {0} / {1} 페이지
{0} file(s) selected · {1} · {2} | {0} 個のファイルを選択 · {1} · {2} | 已选择 {0} 个文件 · {1} · {2} | 파일 {0}개 선택됨 · {1} · {2}
… and {0} more | …ほか {0} 件 | …还有 {0} 项 | …외 {0}개
Source: {0}\nResult: {1}\nChange: {2} | 元: {0}\n結果: {1}\n変更: {2} | 来源：{0}\n结果：{1}\n更改：{2} | 원본: {0}\n결과: {1}\n변경: {2}
Could not create a sample: {0} | サンプルを作成できませんでした: {0} | 无法创建示例：{0} | 샘플을 만들 수 없습니다: {0}
Done: the {0} file was split into {1} parts ({2}.001 etc.) in the output folder. | 完了: {0} ファイルを {1} 個に分割し、出力フォルダに保存しました ({2}.001 など)。 | 完成：{0} 文件已拆分为 {1} 个部分（{2}.001 等），保存在输出文件夹中。 | 완료: {0} 파일을 {1}개로 분할하여 출력 폴더에 저장했습니다 ({2}.001 등).
Part number {0} was selected more than once. | パート番号 {0} が複数回選択されています。 | 分卷编号 {0} 被重复选择。 | 파트 번호 {0}이(가) 여러 번 선택되었습니다.
Done: {0} files renamed. | 完了: {0} 個のファイルの名前を変更しました。 | 完成：已重命名 {0} 个文件。 | 완료: 파일 {0}개의 이름을 변경했습니다.
Checksum found: {0} | チェックサムが見つかりました: {0} | 找到校验和：{0} | 체크섬을 찾았습니다: {0}
Checking file contents… {0}/{1} | ファイルの内容を確認中… {0}/{1} | 正在检查文件内容… {0}/{1} | 파일 내용 확인 중… {0}/{1}
Delete {0} files? | {0} 個のファイルを削除しますか？ | 删除 {0} 个文件？ | 파일 {0}개를 삭제할까요?
Folder contents: {0} files, {1} folders | フォルダの内容: ファイル {0} 個、フォルダ {1} 個 | 文件夹内容：{0} 个文件，{1} 个文件夹 | 폴더 내용: 파일 {0}개, 폴더 {1}개
Formats often unsupported by Android WebView: {0} | Android WebView で非対応のことが多い形式: {0} | Android WebView 通常不支持的格式：{0} | Android WebView에서 지원되지 않는 경우가 많은 형식: {0}
Rename {0} items? | {0} 個の項目の名前を変更しますか？ | 重命名 {0} 个项目？ | 항목 {0}개의 이름을 변경할까요?
Restoring references… ({0}/{1}) | 参照を復元中… ({0}/{1}) | 正在还原引用… ({0}/{1}) | 참조 복원 중… ({0}/{1})
{0} items failed. | {0} 件が失敗しました。 | {0} 项失败。 | {0}개 항목이 실패했습니다.
{0} rows · ~{1} characters · {2} request(s) | {0} 行 · 約 {1} 文字 · {2} リクエスト | {0} 行 · 约 {1} 个字符 · {2} 次请求 | {0}행 · 약 {1}자 · 요청 {2}회
Retry failed ({0}) | 失敗した項目を再試行 ({0}) | 重试失败项 ({0}) | 실패 항목 재시도 ({0})
Page {0} of {1} · {2} item(s) | {1} ページ中 {0} ページ目 · {2} 件 | 第 {0} 页，共 {1} 页 · {2} 项 | {1}페이지 중 {0}페이지 · {2}개 항목
Plugin "{0}" added to the list (at the bottom). | プラグイン「{0}」をリストに追加しました (末尾)。 | 插件“{0}”已添加到列表（末尾）。 | 플러그인 "{0}"을(를) 목록 맨 아래에 추가했습니다.
Profile "{0}" saved ({1} plugin(s)). | プロファイル「{0}」を保存しました (プラグイン {1} 個)。 | 配置文件“{0}”已保存（{1} 个插件）。 | 프로필 "{0}"을(를) 저장했습니다 (플러그인 {1}개).
Duplicate plugin name with identical parameters (positions {0} and {1}); probably unnecessary. | プラグイン名とパラメータが同一の重複です (位置 {0} と {1})。おそらく不要です。 | 插件名称和参数完全相同的重复项（位置 {0} 和 {1}），可能是多余的。 | 플러그인 이름과 매개변수가 동일한 중복입니다 (위치 {0} 및 {1}). 불필요할 수 있습니다.
Ignored ({0}) | 無視 ({0}) | 已忽略 ({0}) | 무시됨 ({0})
Should load before "{0}" (@orderBefore) but the order is reversed. | 「{0}」より前に読み込む必要があります (@orderBefore) が、順序が逆になっています。 | 应在“{0}”之前加载 (@orderBefore)，但顺序相反。 | "{0}"보다 먼저 로드되어야 하지만 (@orderBefore) 순서가 반대로 되어 있습니다.
Size differs: {0} ({1}, expected {2}) | サイズが異なります: {0} ({1}、期待値 {2}) | 大小不一致：{0}（{1}，应为 {2}） | 크기가 다릅니다: {0} ({1}, 예상 {2})
Text is taken raw as in the .rpy (\n, \" and {i} tags kept). Decompile the .rpyc first if there is no .rpy. | テキストは .rpy の内容そのまま取得されます (\n、\" や {i} タグは保持)。.rpy がない場合は先に .rpyc を逆コンパイルしてください。 | 文本按 .rpy 中的原样读取（保留 \n、\" 和 {i} 标签）。如果没有 .rpy，请先反编译 .rpyc。 | 텍스트는 .rpy에 있는 그대로 가져옵니다 (\n, \" 및 {i} 태그 유지). .rpy가 없으면 먼저 .rpyc를 디컴파일하세요.
Decoded: {0} → {1} | デコード結果: {0} → {1} | 已解码：{0} → {1} | 디코딩됨: {0} → {1}
Could not read the folder: {0} | フォルダを読み取れませんでした: {0} | 无法读取文件夹：{0} | 폴더를 읽을 수 없습니다: {0}
From the internet ({0}) | インターネットから ({0}) | 来自互联网 ({0}) | 인터넷에서 ({0})
Extract game archives (RPA, XP3, PFS) | ゲームのアーカイブを展開 (RPA, XP3, PFS) | 解包游戏压缩包（RPA、XP3、PFS） | 게임 아카이브 풀기 (RPA, XP3, PFS)
Tap to select… | タップして選択… | 点按选择… | 탭하여 선택…
Format | 形式 | 格式 | 형식
Extract Archive | アーカイブを展開 | 解包压缩包 | 아카이브 풀기
Language | 言語 | 语言 | 언어
Green | 緑 | 绿色 | 초록
Red | 赤 | 红色 | 빨강
Failed to read | 読み取りに失敗しました | 读取失败 | 읽지 못했습니다
{0}: {1} files | {0}: {1} 個のファイル | {0}：{1} 个文件 | {0}: 파일 {1}개
Saving the result… | 結果を保存中… | 正在保存结果… | 결과 저장 중…
Yes, cancel | はい、キャンセルします | 是，取消 | 예, 취소합니다
Select files | ファイルを選択 | 选择文件 | 파일 선택
Not detected | 検出されませんでした | 未检测到 | 감지되지 않음
Failed | 失敗 | 失败 | 실패
Copy log | ログをコピー | 复制日志 | 로그 복사
Tool settings | ツール設定 | 工具设置 | 도구 설정
Default output folder | 既定の出力フォルダ | 默认输出文件夹 | 기본 출력 폴더
Keep screen on while running | 実行中は画面をオンにしておく | 运行时保持屏幕常亮 | 실행 중 화면 켜 두기
Checking… {0} / {1} | 確認中… {0} / {1} | 正在检查… {0} / {1} | 확인 중… {0} / {1}
Total size (found / expected) | 合計サイズ (検出 / 期待値) | 总大小（实际 / 应为） | 총 크기 (발견 / 예상)
{0} problems found | {0} 件の問題が見つかりました | 发现 {0} 个问题 | 문제 {0}개를 발견했습니다
Too large to preview (max {0}) | 大きすぎてプレビューできません (最大 {0}) | 文件过大，无法预览（最大 {0}） | 너무 커서 미리 볼 수 없습니다 (최대 {0})
Select results | 結果を選択 | 选择结果 | 결과 선택
Unpack & pack visual novel engine archives | ビジュアルノベルエンジンのアーカイブを展開・パック | 解包和打包视觉小说引擎压缩包 | 비주얼 노벨 엔진 아카이브 풀기 및 묶기
RPGM Tools | RPGM ツール | RPGM 工具 | RPGM 도구
Save Editor | セーブエディタ | 存档编辑器 | 세이브 편집기
Turn .rpgmvp .rpgmvo .rpgmvm files back into normal files | .rpgmvp .rpgmvo .rpgmvm ファイルを通常のファイルに戻す | 将 .rpgmvp .rpgmvo .rpgmvm 文件还原为普通文件 | .rpgmvp .rpgmvo .rpgmvm 파일을 일반 파일로 되돌리기
Export Save | セーブをエクスポート | 导出存档 | 세이브 내보내기
Asset Folder | アセットフォルダ | 资源文件夹 | 에셋 폴더
Search ID or value | ID または値を検索 | 搜索 ID 或值 | ID 또는 값 검색
items | アイテム | 物品 | 아이템
Armors | 防具 | 防具 | 방어구
Invalid value: {0} | 無効な値: {0} | 无效的值：{0} | 잘못된 값: {0}
Encryption key not found | 暗号化キーが見つかりません | 未找到加密密钥 | 암호화 키를 찾을 수 없습니다
Encryption key (32 hex characters, optional) | 暗号化キー (16進数32文字、省略可) | 加密密钥（32 位十六进制字符，可选） | 암호화 키 (16진수 32자, 선택 사항)
Translation file | 翻訳ファイル | 翻译文件 | 번역 파일
Save file | セーブファイル | 存档文件 | 세이브 파일
Back up the original save before replacing it in the game. | ゲームで置き換える前に、元のセーブをバックアップしてください。 | 在游戏中替换之前，请先备份原存档。 | 게임에서 교체하기 전에 원본 세이브를 백업하세요.
Values applied | 値を適用しました | 已应用值 | 값을 적용했습니다
No encryption key found. Enter one manually or choose a folder that contains data/System.json | 暗号化キーが見つかりません。手動で入力するか、data/System.json を含むフォルダを選択してください | 未找到加密密钥。请手动输入，或选择包含 data/System.json 的文件夹 | 암호화 키를 찾을 수 없습니다. 직접 입력하거나 data/System.json이 들어 있는 폴더를 선택하세요
Unknown save format (only RPG Maker MV/MZ saves are supported) | 不明なセーブ形式です (RPG Maker MV/MZ のセーブのみ対応) | 未知的存档格式（仅支持 RPG Maker MV/MZ 存档） | 알 수 없는 세이브 형식입니다 (RPG Maker MV/MZ 세이브만 지원)
Done: {0} texts replaced in {1} files | 完了: {1} 個のファイルで {0} 件のテキストを置き換えました | 完成：在 {1} 个文件中替换了 {0} 条文本 | 완료: 파일 {1}개에서 텍스트 {0}개를 교체했습니다
Data folder (Optional) | データフォルダ (省略可) | 数据文件夹（可选） | 데이터 폴더 (선택 사항)
Bulk actions | 一括操作 | 批量操作 | 일괄 작업
Apply to all characters | すべてのキャラクターに適用 | 应用到所有角色 | 모든 캐릭터에 적용
Replaced | 置換済み | 已替换 | 교체됨
…and {0} more warnings | …ほか警告 {0} 件 | …还有 {0} 条警告 | …경고 {0}개 더 있음
Enter or detect the key first to preview images | 画像をプレビューするには、先にキーを入力または検出してください | 请先输入或检测密钥以预览图片 | 이미지를 미리 보려면 먼저 키를 입력하거나 감지하세요
Warning: missing parts: {0}. The save may not load correctly | 警告: 不足している部分: {0}。セーブが正しく読み込めない可能性があります | 警告：缺少部分：{0}。存档可能无法正确加载 | 경고: 누락된 부분: {0}. 세이브가 올바르게 로드되지 않을 수 있습니다
Fill in translations for an exported text file (RPG Maker or VN) | エクスポートしたテキストファイル (RPG Maker または VN) の翻訳を入力 | 为导出的文本文件（RPG Maker 或 VN）填写翻译 | 내보낸 텍스트 파일(RPG Maker 또는 VN)의 번역 입력
Search text | テキストを検索 | 搜索文本 | 텍스트 검색
Fill in at least one translation first | 先に翻訳を 1 件以上入力してください | 请先至少填写一条翻译 | 먼저 번역을 하나 이상 입력하세요
Draft saved: {0} | 下書きを保存しました: {0} | 草稿已保存：{0} | 임시 저장됨: {0}
Editor Settings | エディタ設定 | 编辑器设置 | 편집기 설정
Max characters per line | 1 行あたりの最大文字数 | 每行最大字符数 | 줄당 최대 글자 수
{0} texts restored | {0} 件のテキストを復元しました | 已还原 {0} 条文本 | 텍스트 {0}개를 복원했습니다
A list of original term → translation pairs. Apply fills empty entries whose original text is exactly a term, and replaces leftover original terms inside translations you already wrote. Terms found in an entry's original text also appear as tap-to-insert suggestions under its translation field. | 原文の用語 → 訳語のペアの一覧です。「適用」を実行すると、原文が用語と完全に一致する空の項目が埋められ、すでに入力済みの訳文に残っている原文の用語も置き換えられます。項目の原文に含まれる用語は、訳文欄の下にタップで挿入できる候補としても表示されます。 | 原文术语 → 译文的配对列表。执行“应用”会填充原文与术语完全一致的空条目，并替换你已写好的译文中残留的原文术语。条目原文中出现的术语也会作为点按即可插入的建议显示在其译文栏下方。 | 원문 용어 → 번역 쌍의 목록입니다. 적용하면 원문이 용어와 정확히 일치하는 빈 항목을 채우고, 이미 작성한 번역 안에 남아 있는 원문 용어를 교체합니다. 항목의 원문에 있는 용어는 번역 입력란 아래에 탭하여 삽입할 수 있는 제안으로도 표시됩니다.
Copy translation | 翻訳をコピー | 复制翻译 | 번역 복사
Edited | 編集済み | 已编辑 | 편집됨
Auto-fill control codes | 制御文字を自動入力 | 自动填充控制码 | 제어 코드 자동 입력
Auto-save editor draft | エディタの下書きを自動保存 | 自动保存编辑器草稿 | 편집기 초안 자동 저장
Auto-save every {0} min | {0} 分ごとに自動保存 | 每 {0} 分钟自动保存 | {0}분마다 자동 저장
Skipped {0}: {1} | {0} をスキップしました: {1} | 已跳过 {0}：{1} | {0}을(를) 건너뜀: {1}
Merge | 結合 | 合并 | 병합
Could not read or write the file | ファイルを読み書きできませんでした | 无法读取或写入文件 | 파일을 읽거나 쓸 수 없습니다
With a game-library look that can load several game assets without unpacking the files, as the second tab (after Home). The (+) icon adds a game: pick the game folder, fill in Game Name (auto-filled from the folder name, editable), press Next, then pick an optional cover (if empty, the game icon is used automatically when found) and Add. Search filters the list, Refresh re-checks the folders. The three-dot menu on a card: Rename, Change Cover, Delete Folder (this only removes the list entry, not the real folder or files). Tap a card to open Audio, Images, Video and Text: previews of the game assets with search, sorting, a mini audio player, an image viewer (zoom, swipe), a video player and a text viewer (encoding choice, dialogue extracted from scripts). Encrypted RPG Maker MV/MZ assets are previewed automatically (key from System.json) without changing the original files. Game archives (xp3, rpa, pfs, etc.; every format Unpack Archive supports) are scanned automatically and their contents appear in all four menus with the archive path/file name; audio and video from archives are extracted temporarily to the cache when played. The list is saved so an already scanned game opens instantly without scanning again; folder changes are checked silently in the background and the list is updated only if something changed (the reload button on the game page forces a rescan). Each menu has sorting (name/size, ascending/descending) and pages (Audio and Text 50 files, Images 60, Video 20 per page). The preview cache and its limit (200 MB to 10 GB, default 1 GB) are set in the Settings tab. Below the four menus is an info card (cover, engine, file count, archives read/failed, media loaded/failed; failures are counted as media turn out not to be viewable, e.g. encrypted). Long-press a file, or tap the select-mode icon at the right of "Favorites only", to select several: Extract (to a chosen folder; archive and encrypted files are decrypted), Share (max 20 files / 200 MB) and Favorite; a Favorites only filter is on each list. Internet covers: the cover window has Auto search (VNDB, NextMoe, Steam, then Bing; uses the first result of the first source that has one) and Manual search (search field, source VNDB/NextMoe/Steam/Bing, max 6 results). The cloud icon on the main screen auto-searches covers for all games. The engine label on covers can be turned on in Settings > System Settings (off by default). The sort menu has a Random option: every time it is chosen the list is reshuffled. Auto search always tries VNDB first, then NextMoe, Steam and finally Bing. In every paged list, tap the "Page x / y" label to open "Go to page" and jump straight to a page. | ゲームライブラリ風の画面で、複数のゲームアセットをファイルを展開せずに読み込めます。2番目のタブ（ホームの次）です。(+)アイコンでゲームを追加します。ゲームフォルダを選び、ゲーム名(フォルダ名から自動入力され、編集できます)を入力して「次へ」を押し、任意でカバーを選んで(空の場合は、見つかればゲームのアイコンが自動的に使われます)「追加」を押します。「検索」でリストを絞り込み、「更新」でフォルダを再確認します。カードの三点メニューには、名前変更、カバー変更、フォルダ削除があります(これはリストの項目を削除するだけで、実際のフォルダやファイルは削除されません)。カードをタップすると、音声、画像、動画、テキストが開きます。ゲームアセットのプレビューで、検索、並べ替え、ミニ音声プレーヤー、画像ビューア(ズーム、スワイプ)、動画プレーヤー、テキストビューア(エンコーディング選択、スクリプトから抽出したセリフ)を使えます。暗号化されたRPG Maker MV/MZのアセットは、元のファイルを変更せずに自動でプレビューされます(キーはSystem.jsonから取得します)。ゲームのアーカイブ(xp3、rpa、pfsなど。アーカイブ展開が対応するすべての形式)は自動的にスキャンされ、その内容がアーカイブのパスやファイル名とともに4つすべてのメニューに表示されます。アーカイブ内の音声と動画は、再生時に一時的にキャッシュへ展開されます。リストは保存されるため、スキャン済みのゲームは再スキャンなしですぐに開きます。フォルダの変更はバックグラウンドで静かに確認され、変更があった場合のみリストが更新されます(ゲームページの再読み込みボタンで強制的に再スキャンできます)。各メニューには並べ替え(名前/サイズ、昇順/降順)とページ分割があります(1ページあたり、音声とテキストは50ファイル、画像は60、動画は20)。プレビューキャッシュとその上限(200 MB〜10 GB、初期値は1 GB)は設定タブで指定します。4つのメニューの下には情報カードがあります(カバー、エンジン、ファイル数、読み込めた/失敗したアーカイブ、読み込めた/失敗したメディア。失敗数は、暗号化などで表示できないメディアが判明するたびに加算されます)。ファイルを長押しするか、「お気に入りのみ」の右にある選択モードのアイコンをタップすると複数選択でき、抽出(選んだフォルダへ。アーカイブ内や暗号化されたファイルは復号されます)、共有(最大20ファイル/200 MB)、お気に入り登録ができます。各リストには「お気に入りのみ」フィルターがあります。インターネットカバー:カバーのウィンドウには、自動検索(VNDB、NextMoe、Steam、Bingの順に試し、結果のある最初のソースの先頭の結果を使います)と手動検索(検索欄、ソースはVNDB/NextMoe/Steam/Bing、最大6件)があります。メイン画面の雲アイコンは、すべてのゲームのカバーを自動検索します。カバーのエンジンラベルは、設定 > システム設定で表示できます(初期状態ではオフ)。並べ替えメニューにはランダムがあり、選ぶたびにリストがシャッフルされます。自動検索は常にVNDB、NextMoe、Steam、最後にBingの順で試します。ページ分割されたすべてのリストで、「ページ x / y」のラベルをタップすると「ページ移動」が開き、目的のページへ直接移動できます。 | 采用游戏库的外观，可以在不解包文件的情况下加载多个游戏资源，是第二个标签页（主页之后）。点击(+)图标添加游戏：选择游戏文件夹，填写游戏名称(根据文件夹名自动填入，可编辑)，点击“下一步”，然后可选择封面(留空时，如果能找到游戏图标，会自动使用游戏图标)，最后点击“添加”。“搜索”用于筛选列表，“刷新”用于重新检查文件夹。卡片上的三点菜单包括：重命名、更换封面、删除文件夹(这只会移除列表中的条目，不会删除真实的文件夹或文件)。点按卡片可打开音频、图片、视频和文本：这是游戏资源的预览，支持搜索、排序、迷你音频播放器、图片查看器(缩放、滑动)、视频播放器和文本查看器(可选择编码，可提取脚本中的对话)。加密的RPG Maker MV/MZ资源会被自动预览(密钥取自System.json)，不会修改原始文件。游戏压缩包(xp3、rpa、pfs等；解包压缩包支持的所有格式)会被自动扫描，其内容会连同压缩包路径/文件名显示在全部四个菜单中；播放时，压缩包内的音频和视频会被临时解压到缓存。列表会被保存，因此已扫描过的游戏可以立即打开，无需再次扫描；文件夹的变化会在后台静默检查，只有发生变化时才会更新列表(游戏页面上的重新加载按钮可强制重新扫描)。每个菜单都有排序(名称/大小，升序/降序)和分页(每页：音频和文本50个文件，图片60个，视频20个)。预览缓存及其上限(200 MB至10 GB，默认1 GB)在“设置”标签页中设定。四个菜单下方有一张信息卡片(封面、引擎、文件数、已读取/失败的压缩包、已加载/失败的媒体；当发现媒体无法查看时，例如已加密，才会计入失败数)。长按文件，或点按“仅收藏”右侧的选择模式图标，即可多选：提取(到所选文件夹；压缩包内的文件和加密文件会被解密)、分享(最多20个文件/200 MB)和收藏；每个列表都有“仅收藏”筛选。网络封面：封面窗口提供自动搜索(依次为VNDB、NextMoe、Steam，然后是Bing；使用第一个有结果的来源的第一条结果)和手动搜索(搜索框，来源可选VNDB/NextMoe/Steam/Bing，最多6条结果)。主界面上的云图标会为所有游戏自动搜索封面。封面上的引擎标签可以在“设置”>“系统设置”中开启(默认关闭)。排序菜单中有“随机”选项：每次选择都会重新打乱列表。自动搜索始终先尝试VNDB，然后是NextMoe、Steam，最后是Bing。在每个分页列表中，点按“第 x / y 页”标签即可打开“跳转到页面”，直接跳到某一页。 | 게임 라이브러리 형태로, 여러 게임 에셋을 파일을 풀지 않고도 불러올 수 있습니다. 두 번째 탭입니다(홈 다음). (+) 아이콘으로 게임을 추가합니다. 게임 폴더를 선택하고, 게임 이름(폴더 이름에서 자동으로 채워지며 수정할 수 있습니다)을 입력한 뒤 '다음'을 누릅니다. 이후 선택 사항으로 커버를 고르고(비워 두면 게임 아이콘이 발견될 경우 자동으로 사용됩니다) '추가'를 누릅니다. '검색'은 목록을 걸러 내고, '새로고침'은 폴더를 다시 확인합니다. 카드의 점 세 개 메뉴에는 이름 변경, 커버 변경, 폴더 삭제가 있습니다(목록의 항목만 지울 뿐, 실제 폴더나 파일은 삭제되지 않습니다). 카드를 탭하면 오디오, 이미지, 비디오, 텍스트가 열립니다. 게임 에셋을 미리 볼 수 있으며 검색, 정렬, 미니 오디오 플레이어, 이미지 뷰어(확대/축소, 스와이프), 비디오 플레이어, 텍스트 뷰어(인코딩 선택, 스크립트에서 추출한 대사)를 지원합니다. 암호화된 RPG Maker MV/MZ 에셋은 원본 파일을 바꾸지 않고 자동으로 미리 보기가 됩니다(키는 System.json에서 가져옵니다). 게임 아카이브(xp3, rpa, pfs 등 아카이브 풀기가 지원하는 모든 형식)는 자동으로 스캔되며, 그 내용이 아카이브 경로/파일 이름과 함께 네 개의 모든 메뉴에 표시됩니다. 아카이브 안의 오디오와 비디오는 재생할 때 캐시에 임시로 추출됩니다. 목록은 저장되므로 이미 스캔한 게임은 다시 스캔하지 않고 바로 열립니다. 폴더 변경은 백그라운드에서 조용히 확인되며, 변경 사항이 있을 때만 목록이 업데이트됩니다(게임 페이지의 다시 불러오기 버튼으로 강제로 재스캔할 수 있습니다). 각 메뉴에는 정렬(이름/크기, 오름차순/내림차순)과 페이지 나누기가 있습니다(페이지당 오디오와 텍스트는 50개 파일, 이미지는 60개, 비디오는 20개). 미리 보기 캐시와 그 한도(200 MB~10 GB, 기본 1 GB)는 설정 탭에서 지정합니다. 네 개의 메뉴 아래에는 정보 카드가 있습니다(커버, 엔진, 파일 수, 읽은/실패한 아카이브, 불러온/실패한 미디어. 암호화 등으로 미디어를 볼 수 없다고 확인될 때마다 실패 수가 집계됩니다). 파일을 길게 누르거나 '즐겨찾기만' 오른쪽의 선택 모드 아이콘을 탭하면 여러 개를 선택할 수 있으며, 추출(선택한 폴더로. 아카이브 안의 파일과 암호화된 파일은 복호화됩니다), 공유(최대 20개 파일/200 MB), 즐겨찾기를 할 수 있습니다. 각 목록에는 '즐겨찾기만' 필터가 있습니다. 인터넷 커버: 커버 창에는 자동 검색(VNDB, NextMoe, Steam, 그다음 Bing 순서로 시도하며, 결과가 있는 첫 번째 소스의 첫 번째 결과를 사용합니다)과 수동 검색(검색창, 소스는 VNDB/NextMoe/Steam/Bing, 최대 6개 결과)이 있습니다. 메인 화면의 구름 아이콘은 모든 게임의 커버를 자동으로 검색합니다. 커버의 엔진 라벨은 설정 > 시스템 설정에서 켤 수 있습니다(기본값은 꺼짐). 정렬 메뉴에는 무작위 옵션이 있으며, 선택할 때마다 목록이 다시 섞입니다. 자동 검색은 항상 VNDB를 먼저, 그다음 NextMoe, Steam, 마지막으로 Bing을 시도합니다. 페이지로 나뉜 모든 목록에서 'x / y 페이지' 라벨을 탭하면 '페이지로 이동'이 열려 원하는 페이지로 바로 이동할 수 있습니다.
"""
    const val T5 = """
Create an archive from a folder.\n• Choose the archive format (the engine name is shown). Only writable formats are listed.\n• Pick the input folder, enter the output file name and format options (compression, version, encoding, key).\n• Fill the ignore filter for files that must not be packed. | フォルダからアーカイブを作成します。\n• アーカイブ形式を選びます(エンジン名が表示されます)。書き込み可能な形式のみが表示されます。\n• 入力フォルダを選び、出力ファイル名と形式のオプション(圧縮、バージョン、エンコーディング、キー)を入力します。\n• パックしないファイルには、除外フィルターを設定します。 | 从文件夹创建压缩包。\n• 选择压缩包格式(会显示引擎名称)。只列出可写入的格式。\n• 选择输入文件夹，输入输出文件名和格式选项(压缩、版本、编码、密钥)。\n• 为不需要打包的文件填写忽略过滤规则。 | 폴더에서 아카이브를 만듭니다.\n• 아카이브 형식을 선택합니다(엔진 이름이 표시됩니다). 쓰기가 가능한 형식만 나열됩니다.\n• 입력 폴더를 선택하고, 출력 파일 이름과 형식 옵션(압축, 버전, 인코딩, 키)을 입력합니다.\n• 묶지 않을 파일에는 제외 필터를 입력합니다.
Support Tools > File & Folder Inspector > Duplicate File Finder | サポートツール > ファイル・フォルダー検査 > 重複ファイル検索 | 支持工具 > 文件与文件夹检查器 > 重复文件查找器 | 지원 도구 > 파일 및 폴더 검사기 > 중복 파일 찾기
Support Tools > File Toolkit > File Splitter | サポートツール > ファイルツールキット > ファイル分割 | 支持工具 > 文件工具箱 > 文件分割器 | 지원 도구 > 파일 도구 상자 > 파일 분할기
VN Tools > Siglus Engine | VNツール > Siglus Engine | VN工具 > Siglus Engine | VN 도구 > Siglus Engine
RPGM Tools > Assets | RPGMツール > アセット | RPGM工具 > 资源 | RPGM 도구 > 에셋
RPGM Tools > Plugin Manager | RPGMツール > プラグインマネージャー | RPGM工具 > 插件管理器 | RPGM 도구 > 플러그인 관리자
File & Folder Inspector > File Inspection | ファイル・フォルダー検査 > ファイル検査 | 文件与文件夹检查器 > 文件检查 | 파일 및 폴더 검사기 > 파일 검사
File & Folder Inspector > Translation Scan | ファイル・フォルダー検査 > 翻訳スキャン | 文件与文件夹检查器 > 翻译扫描 | 파일 및 폴더 검사기 > 번역 스캔
Does this app need internet? | このアプリにはインターネットが必要ですか? | 这个应用需要联网吗？ | 이 앱은 인터넷이 필요한가요?
Why are extracted file names garbled? | 展開したファイル名が文字化けするのはなぜですか? | 为什么解包出来的文件名是乱码？ | 추출한 파일 이름이 깨지는 이유는 무엇인가요?
Full unpack vs Select Files? | 全体展開とファイル選択の違いは何ですか? | 完整解包与选择文件有什么区别？ | 전체 풀기와 파일 선택의 차이는 무엇인가요?
What are the control-code warnings in the editor? | エディタのコントロールコードの警告とは何ですか? | 编辑器中的控制码警告是什么意思？ | 편집기의 제어 코드 경고는 무엇인가요?
What is the difference between Check extracted files and Check packing integrity? | 「展開したファイルを確認」と「パックの整合性を確認」の違いは何ですか? | “检查提取的文件”和“检查打包完整性”有什么区别？ | '추출된 파일 확인'과 '묶기 무결성 확인'의 차이는 무엇인가요?
Does the Plugin Scanner change my original plugins? | プラグインスキャナーは元のプラグインを変更しますか? | 插件扫描器会修改我的原始插件吗？ | 플러그인 스캐너가 원본 플러그인을 변경하나요?
Plain text with the status, information, findings and checksums (or the list of differences). Open it in any text editor and send it to the game creator or the developer when reporting. | ステータス、情報、検出結果、チェックサム(または差分のリスト)を含むプレーンテキストです。任意のテキストエディタで開けます。報告の際は、ゲームの制作者や開発者に送ってください。 | 纯文本，包含状态、信息、发现的问题和校验和(或差异列表)。可用任何文本编辑器打开，反馈问题时可发给游戏作者或开发者。 | 상태, 정보, 발견 사항, 체크섬(또는 차이 목록)이 담긴 일반 텍스트입니다. 아무 텍스트 편집기로나 열 수 있으며, 문제를 신고할 때 게임 제작자나 개발자에게 보내세요.
It extracts every file of the archive to an empty sink and compares checksums when available, so corrupt files are found. It is as slow as extracting the whole archive. | アーカイブ内のすべてのファイルを空の出力先に展開し、チェックサムがある場合は照合するため、破損したファイルを検出できます。アーカイブ全体を展開するのと同じ時間がかかります。 | 它会把压缩包中的每个文件提取到一个空的输出目标，并在有校验和时进行比对，因此能发现损坏的文件。速度与提取整个压缩包一样慢。 | 아카이브의 모든 파일을 빈 출력 대상에 추출하고, 체크섬이 있으면 비교하므로 손상된 파일을 찾아낼 수 있습니다. 아카이브 전체를 추출하는 것만큼 오래 걸립니다.
The archive may use a game-specific key, be truncated from a bad download, or use a format version that is not supported yet. Use File Inspection to see the cause. | アーカイブがゲーム固有のキーを使っているか、ダウンロードの不具合で途中までしかないか、まだ対応していない形式バージョンを使っている可能性があります。原因はファイル検査で確認してください。 | 压缩包可能使用了游戏专用的密钥，可能因下载不完整而被截断，也可能使用了尚不支持的格式版本。请使用文件检查查看原因。 | 아카이브가 게임 전용 키를 사용하거나, 잘못된 다운로드로 일부가 잘렸거나, 아직 지원되지 않는 형식 버전을 사용할 수 있습니다. 원인은 파일 검사에서 확인하세요.
Run Key Finder (main menu) on the game folder; copy the Verified key and paste it into the Key option. A Candidate status means the key is only the best guess. | ゲームフォルダに対してキーファインダー(メインメニュー)を実行し、確認済みのキーをコピーして、キーのオプションに貼り付けてください。候補というステータスは、そのキーが最も有力な推測にすぎないことを意味します。 | 对游戏文件夹运行密钥查找器(主菜单)；复制已验证的密钥，粘贴到“密钥”选项中。“候选”状态表示该密钥只是最接近的猜测。 | 게임 폴더에 대해 키 파인더(메인 메뉴)를 실행하고, 확인된 키를 복사하여 키 옵션에 붙여넣으세요. 후보 상태는 그 키가 가장 유력한 추측일 뿐이라는 뜻입니다.
The file is probably incomplete or corrupt. Download it again and verify the checksum in File Inspection. If the format is right but reading fails, try changing the Filename encoding. | ファイルが不完全か破損している可能性があります。もう一度ダウンロードし、ファイル検査でチェックサムを確認してください。形式は正しいのに読み込みに失敗する場合は、ファイル名のエンコーディングを変更してみてください。 | 文件可能不完整或已损坏。请重新下载，并在文件检查中核对校验和。如果格式正确但读取失败，请尝试更改文件名编码。 | 파일이 불완전하거나 손상되었을 가능성이 큽니다. 다시 다운로드하고 파일 검사에서 체크섬을 확인하세요. 형식이 맞는데도 읽기에 실패한다면 파일 이름 인코딩을 바꿔 보세요.
Make sure the version matches: MV uses .rpgmvp/.rpgmvo/.rpgmvm, MZ uses .png_/.ogg_/.m4a_. The key is read from System.json; make sure that file is in the chosen folder. | バージョンが一致しているか確認してください。MVは.rpgmvp/.rpgmvo/.rpgmvm、MZは.png_/.ogg_/.m4a_を使います。キーはSystem.jsonから読み取られるため、選んだフォルダにこのファイルがあることを確認してください。 | 请确认版本匹配：MV使用.rpgmvp/.rpgmvo/.rpgmvm，MZ使用.png_/.ogg_/.m4a_。密钥从System.json中读取，请确保该文件位于所选文件夹中。 | 버전이 일치하는지 확인하세요. MV는 .rpgmvp/.rpgmvo/.rpgmvm, MZ는 .png_/.ogg_/.m4a_를 사용합니다. 키는 System.json에서 읽으므로, 선택한 폴더에 이 파일이 있는지 확인하세요.
Enable background processing in Settings or keep the screen on. For big archives do not close the app until it finishes. | 設定でバックグラウンド処理を有効にするか、画面を点灯したままにしてください。大きなアーカイブは、完了するまでアプリを閉じないでください。 | 请在设置中启用后台处理，或保持屏幕常亮。处理大型压缩包时，请在完成前不要关闭应用。 | 설정에서 백그라운드 처리를 켜거나 화면을 켜 두세요. 큰 아카이브는 작업이 끝날 때까지 앱을 닫지 마세요.
The archive was probably damaged during download or copying. Download it again. If only a few files are listed, extract the rest with Select Files. | ダウンロードやコピーの途中でアーカイブが破損した可能性があります。もう一度ダウンロードしてください。一部のファイルしか表示されない場合は、残りをファイル選択で展開してください。 | 压缩包可能在下载或复制过程中损坏。请重新下载。如果只列出了少量文件，请用选择文件提取其余文件。 | 다운로드나 복사 중에 아카이브가 손상되었을 가능성이 큽니다. 다시 다운로드하세요. 일부 파일만 나열된다면 나머지는 파일 선택으로 추출하세요.
Quick start | クイックスタート | 快速入门 | 빠른 시작
Support Tools > File Name Optimizer | サポートツール > ファイル名オプティマイザー | 支持工具 > 文件名优化器 | 지원 도구 > 파일 이름 최적화
Finds identical files in a folder and its subfolders.\n• Pick a folder; the app groups files by size and then compares their content with SHA-256 (with progress and a Cancel button).\n• Results are shown per group. By default every file in a group is selected except one; tap a file to change the selection. One file in each group always stays.\n• The Delete button permanently deletes the selected files after a confirmation. | フォルダとそのサブフォルダ内にある同一のファイルを探します。\n• フォルダを選ぶと、アプリはファイルをサイズでグループ化し、その後SHA-256で内容を比較します(進行状況とキャンセルボタンが表示されます)。\n• 結果はグループごとに表示されます。初期状態では、各グループで1つを除くすべてのファイルが選択されています。ファイルをタップすると選択を変更できます。各グループで必ず1つのファイルが残ります。\n• 削除ボタンは、確認のあと、選択したファイルを完全に削除します。 | 在文件夹及其子文件夹中查找相同的文件。\n• 选择一个文件夹；应用先按大小对文件分组，再用SHA-256比较其内容(显示进度，并有取消按钮)。\n• 结果按组显示。默认情况下，每组中除一个文件外其余都被选中；点按文件可更改选择。每组中始终会保留一个文件。\n• 确认后，删除按钮会永久删除所选文件。 | 폴더와 그 하위 폴더에서 동일한 파일을 찾습니다.\n• 폴더를 선택하면 앱이 파일을 크기별로 묶은 뒤 SHA-256으로 내용을 비교합니다(진행률과 취소 버튼이 표시됩니다).\n• 결과는 그룹별로 표시됩니다. 기본적으로 각 그룹에서 하나를 제외한 모든 파일이 선택됩니다. 파일을 탭하면 선택을 바꿀 수 있습니다. 각 그룹에서 파일 하나는 항상 남습니다.\n• 삭제 버튼은 확인 후 선택한 파일을 영구적으로 삭제합니다.
Splits one large file into numbered parts (.001, .002, etc.), e.g. so it can be sent or uploaded.\n• Pick the source file, then how to split: By size (size of each part in KB/MB) or By part count. The number of parts that will be made is shown before you run it (maximum 9999 parts).\n• The Create checksum option (on by default) writes a name.sha256 file with the SHA-256 of every part and of the whole file, which File Joiner uses to verify the result.\n• The base name can be changed; results are named name.001, name.002, etc. and saved to the Output Folder. The source file is not changed. | 1つの大きなファイルを、番号付きのパート(.001、.002など)に分割します。たとえば送信やアップロードのために使えます。\n• 元のファイルを選び、分割方法として「サイズ指定」(各パートのサイズをKB/MBで指定)または「パート数指定」を選びます。作成されるパート数は、実行前に表示されます(最大9999パート)。\n• チェックサムを作成するオプション(初期状態でオン)は、各パートと全体のSHA-256を記録したname.sha256ファイルを書き出します。ファイル結合はこれを使って結果を検証します。\n• ベース名は変更でき、結果はname.001、name.002などの名前で出力フォルダに保存されます。元のファイルは変更されません。 | 将一个大文件分割成带编号的分卷(.001、.002等)，例如便于发送或上传。\n• 选择源文件，然后选择分割方式：按大小(每个分卷的大小，单位KB/MB)或按分卷数。运行前会显示将生成的分卷数(最多9999个)。\n• 创建校验和选项(默认开启)会写入一个name.sha256文件，其中包含每个分卷和整个文件的SHA-256，文件合并器会用它来验证结果。\n• 基本名称可以修改；结果命名为name.001、name.002等，并保存到输出文件夹。源文件不会被修改。 | 큰 파일 하나를 번호가 붙은 조각(.001, .002 등)으로 나눕니다. 예를 들어 전송이나 업로드를 위해 사용할 수 있습니다.\n• 원본 파일을 선택한 뒤 분할 방식을 고릅니다. 크기 기준(각 조각의 크기를 KB/MB로 지정) 또는 조각 수 기준입니다. 만들어질 조각 수는 실행 전에 표시됩니다(최대 9999개).\n• 체크섬 만들기 옵션(기본값 켜짐)은 각 조각과 전체 파일의 SHA-256을 담은 name.sha256 파일을 기록하며, 파일 합치기가 이를 이용해 결과를 검증합니다.\n• 기본 이름은 변경할 수 있으며, 결과는 name.001, name.002 등의 이름으로 출력 폴더에 저장됩니다. 원본 파일은 변경되지 않습니다.
Workflow for SiglusEngine games.\n• Unpack Scene.pck via Engine List > Siglus Engine (or auto-detect). The result holds scene .dat files and _FuruArc_Siglus.meta (do not delete it).\n• The Scene.pck exe key is found automatically from the scene data and stored in _FuruArc_Siglus.meta, so you need not fill it. An encrypted Gameexe.dat needs the key: get it from Key Finder and paste it into the Key option.\n• Export Text on the scene .dat files, translate, then Import Text.\n• Pack Archive in Siglus Engine builds a new Scene.pck from the same folder. Gameexe.dat (as Gameexe.ini) and OVK voice files can be unpacked/packed too.\n• Texts are picked by filtering non-ASCII strings, so some internal strings may show up. | SiglusEngineゲームのワークフローです。\n• Scene.pckは、エンジン一覧 > Siglus Engine(または自動検出)で展開します。結果には、シーンの.datファイルと_FuruArc_Siglus.metaが含まれます(削除しないでください)。\n• Scene.pckのexeキーはシーンデータから自動的に見つかり、_FuruArc_Siglus.metaに保存されるため、入力は不要です。暗号化されたGameexe.datにはキーが必要です。キーファインダーで取得し、キーのオプションに貼り付けてください。\n• シーンの.datファイルでテキストを書き出し、翻訳してから、テキストを取り込みます。\n• Siglus Engineのアーカイブをパックは、同じフォルダから新しいScene.pckを作成します。Gameexe.dat(Gameexe.iniとして)とOVK音声ファイルも展開/パックできます。\n• テキストは非ASCII文字列を絞り込んで抽出するため、内部用の文字列が含まれることがあります。 | SiglusEngine游戏的工作流程。\n• 通过引擎列表 > Siglus Engine(或自动检测)解包Scene.pck。结果包含场景.dat文件和_FuruArc_Siglus.meta(请勿删除)。\n• Scene.pck的exe密钥会从场景数据中自动找到，并保存在_FuruArc_Siglus.meta中，因此无需填写。加密的Gameexe.dat需要密钥：请从密钥查找器获取，并粘贴到“密钥”选项中。\n• 对场景.dat文件导出文本，翻译后再导入文本。\n• Siglus Engine中的打包压缩包会用同一文件夹生成新的Scene.pck。Gameexe.dat(作为Gameexe.ini)和OVK语音文件也可以解包/打包。\n• 文本是通过筛选非ASCII字符串提取的，因此可能会混入一些内部字符串。 | SiglusEngine 게임의 작업 순서입니다.\n• 엔진 목록 > Siglus Engine(또는 자동 감지)에서 Scene.pck를 풉니다. 결과에는 장면 .dat 파일과 _FuruArc_Siglus.meta가 들어 있습니다(삭제하지 마세요).\n• Scene.pck의 exe 키는 장면 데이터에서 자동으로 찾아 _FuruArc_Siglus.meta에 저장되므로 입력할 필요가 없습니다. 암호화된 Gameexe.dat에는 키가 필요합니다. 키 파인더에서 얻어 키 옵션에 붙여넣으세요.\n• 장면 .dat 파일에서 텍스트를 내보내고, 번역한 뒤 텍스트 가져오기를 합니다.\n• Siglus Engine의 아카이브 묶기는 같은 폴더에서 새 Scene.pck를 만듭니다. Gameexe.dat(Gameexe.ini로)와 OVK 음성 파일도 풀거나 묶을 수 있습니다.\n• 텍스트는 ASCII가 아닌 문자열을 걸러서 추출하므로, 일부 내부 문자열이 함께 나타날 수 있습니다.
• Decrypt Assets: turns .rpgmvp/.rpgmvo/.rpgmvm (MV) or .png_/.ogg_/.m4a_ (MZ) back into normal files.\n• Encrypt Assets: the reverse; System.json in the output is updated with the key.\n• Unpack/Pack RGSS archives (XP .rgssad, VX .rgss2a, VX Ace .rgss3a).\n• Decrypt/Encrypt: choose File (several allowed) or Folder with the toggle above the input field. | • アセットを復号:.rpgmvp/.rpgmvo/.rpgmvm(MV)または.png_/.ogg_/.m4a_(MZ)を通常のファイルに戻します。\n• アセットを暗号化:その逆の操作です。出力先のSystem.jsonにキーが書き込まれます。\n• RGSSアーカイブの展開/パック(XPは.rgssad、VXは.rgss2a、VX Aceは.rgss3a)。\n• 復号/暗号化:入力欄の上にあるトグルで、ファイル(複数可)またはフォルダを選びます。 | • 解密资源：将.rpgmvp/.rpgmvo/.rpgmvm(MV)或.png_/.ogg_/.m4a_(MZ)还原为普通文件。\n• 加密资源：反向操作；输出中的System.json会写入密钥。\n• 解包/打包RGSS压缩包(XP为.rgssad，VX为.rgss2a，VX Ace为.rgss3a)。\n• 解密/加密：用输入框上方的开关选择文件(可多选)或文件夹。 | • 에셋 복호화: .rpgmvp/.rpgmvo/.rpgmvm(MV) 또는 .png_/.ogg_/.m4a_(MZ)를 일반 파일로 되돌립니다.\n• 에셋 암호화: 그 반대입니다. 출력의 System.json에 키가 기록됩니다.\n• RGSS 아카이브 풀기/묶기(XP는 .rgssad, VX는 .rgss2a, VX Ace는 .rgss3a).\n• 복호화/암호화: 입력란 위의 토글로 파일(여러 개 가능) 또는 폴더를 선택합니다.
Manage RPG Maker MV/MZ plugins. Four tools:\n• Plugin Scanner: pick the js/plugins folder (the game or js folder also works; plugins.js is optional for parameters) and press Scan. View all information lists every plugin with search and 50 per page. The Plugin Editing Area holds non-ASCII texts in code, annotation texts (@plugindesc, @text, @desc), code defaults, @default values and plugins.js parameters. Fill only the fields to change, pick an output folder and press Apply; only changed files are written and old files are kept as .bak. Texts can be exported (CSV/TXT/JSON) and their translation imported back.\n• Plugin.js Editor: upload plugins.js, browse the list (search, Enabled/Disabled filter, 50 per page), tap a plugin for the Enabled switch, Up/Down load order and Remove from list. Upload New Plugin then Add Plugin. An optional plugin folder gives dependency warnings (@base, @orderAfter, @orderBefore). Profiles store and apply each plugin's enabled state. Save Plugin.js writes plugins.js to the output folder (automatic .bak backup) and copies new plugins to a plugins/ subfolder.\n• Plugin Checker: match plugins.js against the plugin folder (duplicate names, missing files, unregistered files, empty files, dependencies and order).\n• plugins.js Comparer: compare two versions (added, removed, status, parameters, order).\n• In Plugin.js Editor: View Warnings opens a warnings page (50 per page); tap a plugin then View Parameters to edit parameter values (50 rows per page); Upload New Plugin appears after plugins.js is uploaded. | RPG Maker MV/MZのプラグインを管理します。4つのツールがあります。\n• プラグインスキャナー:js/pluginsフォルダを選び(ゲームフォルダやjsフォルダでも動作します。パラメータ用のplugins.jsは省略可能です)、スキャンを押します。「すべての情報を表示」では、すべてのプラグインを検索付き、1ページ50件で一覧表示します。プラグイン編集エリアには、コード内の非ASCIIテキスト、注釈テキスト(@plugindesc、@text、@desc)、コードの初期値、@default値、plugins.jsのパラメータが表示されます。変更したい項目だけを入力し、出力フォルダを選んで適用を押します。変更されたファイルのみが書き込まれ、古いファイルは.bakとして残ります。テキストはCSV/TXT/JSONで書き出し、翻訳を取り込み直せます。\n• Plugin.jsエディタ:plugins.jsをアップロードし、一覧を閲覧します(検索、有効/無効フィルター、1ページ50件)。プラグインをタップすると、有効スイッチ、読み込み順の上へ/下へ、リストから削除が使えます。新しいプラグインをアップロードしてから、プラグインを追加を押します。プラグインフォルダを任意で指定すると、依存関係の警告(@base、@orderAfter、@orderBefore)が表示されます。プロファイルは、各プラグインの有効状態を保存して適用します。Plugin.jsを保存は、plugins.jsを出力フォルダに書き出し(.bakを自動でバックアップ)、新しいプラグインをplugins/サブフォルダにコピーします。\n• プラグインチェッカー:plugins.jsとプラグインフォルダを照合します(重複する名前、不足ファイル、未登録ファイル、空のファイル、依存関係と順序)。\n• plugins.js比較:2つのバージョンを比較します(追加、削除、状態、パラメータ、順序)。\n• Plugin.jsエディタでは、警告を表示で警告ページ(1ページ50件)が開きます。プラグインをタップしてパラメータを表示を押すと、パラメータの値を編集できます(1ページ50行)。新しいプラグインをアップロードは、plugins.jsをアップロードした後に表示されます。 | 管理RPG Maker MV/MZ插件。共有四个工具：\n• 插件扫描器：选择js/plugins文件夹(选择游戏文件夹或js文件夹也可以；plugins.js用于参数，可不提供)，然后点击扫描。“查看全部信息”会列出所有插件，支持搜索，每页50个。插件编辑区包含代码中的非ASCII文本、注释文本(@plugindesc、@text、@desc)、代码默认值、@default值和plugins.js参数。只填写需要更改的字段，选择输出文件夹后点击应用；只会写入有更改的文件，旧文件保留为.bak。文本可导出(CSV/TXT/JSON)，翻译后可再导入。\n• Plugin.js编辑器：上传plugins.js，浏览列表(搜索、启用/禁用筛选，每页50个)，点按插件可使用启用开关、上移/下移加载顺序以及从列表中移除。先上传新插件，再点击添加插件。可选的插件文件夹会提供依赖关系警告(@base、@orderAfter、@orderBefore)。配置文件用于保存并应用每个插件的启用状态。保存Plugin.js会把plugins.js写入输出文件夹(自动生成.bak备份)，并把新插件复制到plugins/子文件夹。\n• 插件检查器：将plugins.js与插件文件夹进行比对(重名、缺失文件、未注册文件、空文件、依赖关系和顺序)。\n• plugins.js比较器：比较两个版本(新增、移除、状态、参数、顺序)。\n• 在Plugin.js编辑器中：查看警告会打开警告页面(每页50个)；点按插件，再点击查看参数即可编辑参数值(每页50行)；上传新插件在上传plugins.js之后才会出现。 | RPG Maker MV/MZ 플러그인을 관리합니다. 네 가지 도구가 있습니다.\n• 플러그인 스캐너: js/plugins 폴더를 선택하고(게임 폴더나 js 폴더도 가능하며, 파라미터용 plugins.js는 선택 사항입니다) 스캔을 누릅니다. 모든 정보 보기는 모든 플러그인을 검색 기능과 함께 페이지당 50개씩 나열합니다. 플러그인 편집 영역에는 코드 속 ASCII가 아닌 텍스트, 주석 텍스트(@plugindesc, @text, @desc), 코드 기본값, @default 값, plugins.js 파라미터가 들어 있습니다. 변경할 항목만 입력하고 출력 폴더를 선택한 뒤 적용을 누릅니다. 변경된 파일만 기록되며 이전 파일은 .bak으로 보관됩니다. 텍스트는 내보내고(CSV/TXT/JSON) 번역본을 다시 가져올 수 있습니다.\n• Plugin.js 편집기: plugins.js를 업로드하고 목록을 둘러봅니다(검색, 사용/사용 안 함 필터, 페이지당 50개). 플러그인을 탭하면 사용 스위치, 로드 순서 위로/아래로, 목록에서 제거를 쓸 수 있습니다. 새 플러그인 업로드 후 플러그인 추가를 누릅니다. 플러그인 폴더를 선택적으로 지정하면 의존성 경고(@base, @orderAfter, @orderBefore)가 표시됩니다. 프로필은 각 플러그인의 사용 상태를 저장하고 적용합니다. Plugin.js 저장은 plugins.js를 출력 폴더에 기록하고(.bak 자동 백업) 새 플러그인을 plugins/ 하위 폴더에 복사합니다.\n• 플러그인 검사기: plugins.js를 플러그인 폴더와 대조합니다(중복 이름, 누락된 파일, 등록되지 않은 파일, 빈 파일, 의존성과 순서).\n• plugins.js 비교기: 두 버전을 비교합니다(추가, 제거, 상태, 파라미터, 순서).\n• Plugin.js 편집기에서: 경고 보기는 경고 페이지를 엽니다(페이지당 50개). 플러그인을 탭한 뒤 파라미터 보기를 누르면 파라미터 값을 편집할 수 있습니다(페이지당 50행). 새 플러그인 업로드는 plugins.js를 업로드한 뒤에 나타납니다.
Pick one archive or script (VN or RPGM).\n• The banner above the results: green = healthy, yellow = warnings, red = problems.\n• Archive: format, engine, file count, format match, duplicate names. Archive Contents browses the files, Unpack this archive opens the Unpack screen directly.\n• Compute Checksum shows CRC32/MD5/SHA-256. Paste a hash from the source site into the "Match" field to be sure the download is intact.\n• Check Integrity extracts every file to an empty sink to find corrupt files.\n• Script: size, type, engine, encoding, encrypted or not, and wrong code or tags.\n• Unrecognised files show a guess of the engine from their extension.\n• When the content reads as text, the View content as text button opens a paged preview screen (50 KB per page) that can be copied page by page. | アーカイブまたはスクリプト(VNまたはRPGM)を1つ選びます。\n• 結果の上のバナー:緑は正常、黄色は警告、赤は問題を表します。\n• アーカイブ:形式、エンジン、ファイル数、形式の一致、重複する名前。アーカイブの内容でファイルを閲覧でき、このアーカイブを展開すると展開画面が直接開きます。\n• チェックサムを計算では、CRC32/MD5/SHA-256が表示されます。配布元サイトのハッシュを「照合」欄に貼り付けると、ダウンロードが完全であることを確認できます。\n• 整合性を確認は、すべてのファイルを空の出力先に展開して、破損したファイルを探します。\n• スクリプト:サイズ、種類、エンジン、エンコーディング、暗号化の有無、誤ったコードやタグ。\n• 認識できないファイルには、拡張子からエンジンを推測した結果が表示されます。\n• 内容がテキストとして読める場合は、テキストとして内容を表示ボタンでページ分割されたプレビュー画面(1ページ50 KB)が開き、ページごとにコピーできます。 | 选择一个压缩包或脚本(VN或RPGM)。\n• 结果上方的横幅：绿色表示正常，黄色表示有警告，红色表示有问题。\n• 压缩包：格式、引擎、文件数、格式匹配、重复名称。压缩包内容可浏览文件，解包此压缩包会直接打开解包界面。\n• 计算校验和会显示CRC32/MD5/SHA-256。把来源网站上的哈希值粘贴到“匹配”栏，即可确认下载是否完整。\n• 检查完整性会把每个文件提取到一个空的输出目标，以找出损坏的文件。\n• 脚本：大小、类型、引擎、编码、是否加密，以及错误的代码或标签。\n• 无法识别的文件会根据扩展名给出引擎的推测。\n• 当内容可作为文本读取时，以文本查看内容按钮会打开分页预览界面(每页50 KB)，可逐页复制。 | 아카이브 또는 스크립트(VN 또는 RPGM) 하나를 선택합니다.\n• 결과 위의 배너: 녹색은 정상, 노란색은 경고, 빨간색은 문제를 뜻합니다.\n• 아카이브: 형식, 엔진, 파일 수, 형식 일치 여부, 중복 이름. 아카이브 내용에서 파일을 둘러볼 수 있고, 이 아카이브 풀기를 누르면 풀기 화면이 바로 열립니다.\n• 체크섬 계산은 CRC32/MD5/SHA-256을 보여 줍니다. 배포 사이트의 해시를 '일치' 입력란에 붙여넣으면 다운로드가 온전한지 확실히 확인할 수 있습니다.\n• 무결성 검사는 모든 파일을 빈 출력 대상에 추출하여 손상된 파일을 찾습니다.\n• 스크립트: 크기, 종류, 엔진, 인코딩, 암호화 여부, 잘못된 코드나 태그.\n• 인식되지 않는 파일은 확장자를 바탕으로 엔진을 추측해 보여 줍니다.\n• 내용이 텍스트로 읽히면 텍스트로 내용 보기 버튼이 페이지 단위 미리 보기 화면(페이지당 50 KB)을 열며, 페이지별로 복사할 수 있습니다.
Pick a translation export (CSV/TXT/JSON) from RPGM or VN. Results: filled and empty entries, control codes that differ from the original, lines longer than the chosen limit, translations equal to the original, and inconsistent translations (same original, different translation). Tap a category to see its entries. | RPGMまたはVNの翻訳エクスポート(CSV/TXT/JSON)を選びます。結果:入力済みと空のエントリ、原文と異なるコントロールコード、指定した上限を超える行、原文と同じ翻訳、一貫性のない翻訳(同じ原文に異なる翻訳)。カテゴリをタップすると、そのエントリが表示されます。 | 选择来自RPGM或VN的翻译导出文件(CSV/TXT/JSON)。结果包括：已填写和为空的条目、与原文不同的控制码、超过所选长度上限的行、与原文相同的译文，以及不一致的翻译(相同原文，不同译文)。点按某个类别即可查看其条目。 | RPGM 또는 VN의 번역 내보내기 파일(CSV/TXT/JSON)을 선택합니다. 결과: 채워진 항목과 빈 항목, 원문과 다른 제어 코드, 지정한 한도보다 긴 줄, 원문과 같은 번역, 일관성 없는 번역(같은 원문, 다른 번역). 카테고리를 탭하면 해당 항목을 볼 수 있습니다.
Only for three features: (1) Online Translation in the Translation Editor (globe icon), (2) online cover search in Game Assets (Auto search / Manual search / cloud icon; sources are VNDB, NextMoe, Steam and Bing), and (3) Check for updates: the app reads the latest release on GitHub (automatically when the app opens if you are online, or via Settings > Check for updates; only the version number and release notes are read, no personal data is sent, and the download opens in your browser). For cover search only the game name is sent to those sites, then the chosen image is downloaded and kept in the app storage. Without internet you can still pick a cover from a file on the device or have it taken from inside the game folder. All unpacking, packing, text export/import, inspection and asset browsing still run offline on your device. Text translated online is sent to the provider you choose (Lingva, Google, DeepL, Gemini, OpenAI, DeepSeek, Claude, Groq or a Custom URL); API keys are stored on the device only. Links in the Source and About menus are opened by your browser. | 次の3つの機能のみです。(1)翻訳エディタのオンライン翻訳(地球儀アイコン)、(2)ゲームアセットのオンラインカバー検索(自動検索/手動検索/雲アイコン。ソースはVNDB、NextMoe、Steam、Bing)、(3)アップデートの確認(アプリを開いたとき、オンラインなら自動で、または 設定 > アップデートを確認 から、GitHub の最新リリースを読み取ります。読み取るのはバージョン番号とリリースノートのみで、個人データは送信されず、ダウンロードはブラウザで開きます)。カバー検索では、ゲーム名だけがそれらのサイトに送信され、選んだ画像がダウンロードされてアプリのストレージに保存されます。インターネットがなくても、端末内のファイルやゲームフォルダ内からカバーを選べます。展開、パック、テキストの書き出し/取り込み、検査、アセットの閲覧は、すべて端末上でオフラインのまま動作します。オンラインで翻訳したテキストは、選んだプロバイダ(Lingva、Google、DeepL、Gemini、OpenAI、DeepSeek、Claude、Groq、またはカスタムURL)に送信されます。APIキーは端末内にのみ保存されます。ソースとアプリ情報メニューのリンクは、ブラウザで開かれます。 | 仅用于三项功能：(1)翻译编辑器中的在线翻译(地球图标)，(2)游戏资源中的在线封面搜索(自动搜索/手动搜索/云图标；来源为VNDB、NextMoe、Steam和Bing)，以及(3)检查更新(应用打开时若已联网会自动读取 GitHub 上的最新版本，也可通过 设置 > 检查更新 手动检查；只读取版本号和发行说明，不发送任何个人数据，下载会在浏览器中打开)。封面搜索只会向这些网站发送游戏名称，然后下载所选图片并保存在应用存储中。没有网络时，仍可从设备上的文件中选择封面，或从游戏文件夹中获取。所有解包、打包、文本导出/导入、检查和资源浏览仍然在你的设备上离线运行。在线翻译的文本会发送给你选择的服务商(Lingva、Google、DeepL、Gemini、OpenAI、DeepSeek、Claude、Groq或自定义URL)；API密钥只保存在设备上。来源和关于菜单中的链接会用浏览器打开。 | 세 가지 기능에만 필요합니다. (1) 번역 편집기의 온라인 번역(지구본 아이콘), (2) 게임 에셋의 온라인 커버 검색(자동 검색/수동 검색/구름 아이콘. 소스는 VNDB, NextMoe, Steam, Bing), (3) 업데이트 확인(앱을 열 때 온라인이면 자동으로, 또는 설정 > 업데이트 확인 에서 GitHub의 최신 릴리스를 읽습니다. 버전 번호와 릴리스 노트만 읽으며 개인 데이터는 전송되지 않고, 다운로드는 브라우저에서 열립니다). 커버 검색에서는 게임 이름만 해당 사이트로 전송되며, 선택한 이미지는 다운로드되어 앱 저장소에 보관됩니다. 인터넷이 없어도 기기의 파일에서 커버를 고르거나 게임 폴더 안에서 가져올 수 있습니다. 풀기, 묶기, 텍스트 내보내기/가져오기, 검사, 에셋 탐색은 모두 기기에서 오프라인으로 실행됩니다. 온라인으로 번역한 텍스트는 선택한 제공업체(Lingva, Google, DeepL, Gemini, OpenAI, DeepSeek, Claude, Groq 또는 사용자 지정 URL)로 전송됩니다. API 키는 기기에만 저장됩니다. 소스 및 정보 메뉴의 링크는 브라우저에서 열립니다.
Names inside the archive use a specific encoding (usually Shift-JIS). Change the Filename encoding option; the file list reloads automatically. | アーカイブ内の名前は特定のエンコーディング(通常はShift-JIS)を使っています。ファイル名のエンコーディングオプションを変更してください。ファイル一覧は自動的に再読み込みされます。 | 压缩包内的名称使用了特定的编码(通常是Shift-JIS)。请更改文件名编码选项；文件列表会自动重新加载。 | 아카이브 안의 이름이 특정 인코딩(보통 Shift-JIS)을 사용합니다. 파일 이름 인코딩 옵션을 바꿔 보세요. 파일 목록이 자동으로 다시 불러와집니다.
Full unpack extracts everything. Select Files lets you tick only certain files or folders, which is faster and saves space. | 全体展開はすべてを展開します。ファイル選択では、必要なファイルやフォルダだけにチェックを付けられるため、より速く、容量も節約できます。 | 完整解包会提取全部内容。选择文件让你只勾选部分文件或文件夹，速度更快，也更节省空间。 | 전체 풀기는 모든 것을 추출합니다. 파일 선택은 필요한 파일이나 폴더만 체크할 수 있어 더 빠르고 공간도 절약됩니다.
Codes like \C[2], \N[1] or script tags must stay in the translation so colours, names and pauses remain correct. The editor warns when they differ from the original. | \C[2]、\N[1]などのコードやスクリプトタグは、色、名前、ウェイトが正しく保たれるよう、翻訳にもそのまま残す必要があります。原文と異なる場合は、エディタが警告します。 | \C[2]、\N[1]之类的代码或脚本标签必须保留在译文中，这样颜色、名字和停顿才能保持正确。当它们与原文不同时，编辑器会发出警告。 | \C[2], \N[1] 같은 코드나 스크립트 태그는 색상, 이름, 일시 정지가 올바르게 유지되도록 번역문에도 그대로 남아 있어야 합니다. 원문과 다르면 편집기가 경고합니다.
Check extracted files (after Unpack) compares the output with the archive: size, and the checksum when the archive stores one (XP3), or a re-extraction in memory for other formats. Check packing integrity (after Pack) reads the new archive back and compares each file with the source folder. | 展開したファイルを確認(展開後)は、出力をアーカイブと比較します。サイズを比べ、アーカイブがチェックサムを保存している場合(XP3)はそれも比べ、それ以外の形式ではメモリ上で再展開して比較します。パックの整合性を確認(パック後)は、作成したアーカイブを読み戻し、各ファイルを元のフォルダと比較します。 | 检查提取的文件(解包后)会将输出与压缩包进行比较：比较大小；如果压缩包保存了校验和(XP3)，则比较校验和；其他格式则在内存中重新提取比对。检查打包完整性(打包后)会回读新的压缩包，并将每个文件与源文件夹进行比较。 | 추출된 파일 확인(풀기 후)은 출력물을 아카이브와 비교합니다. 크기를 비교하고, 아카이브가 체크섬을 저장하는 경우(XP3)에는 체크섬도 비교하며, 그 밖의 형식은 메모리에서 다시 추출하여 비교합니다. 묶기 무결성 확인(묶기 후)은 새 아카이브를 다시 읽어 각 파일을 원본 폴더와 비교합니다.
No. Results go to the output folder; if a file with the same name already exists there, the old copy is kept as .bak. The Plugin.js Editor works the same way. Copy the result into the game folder yourself when you are sure. | いいえ。結果は出力フォルダに保存され、同じ名前のファイルがすでにある場合、古いものは.bakとして残ります。Plugin.jsエディタも同様に動作します。問題がないと確認できたら、結果を自分でゲームフォルダにコピーしてください。 | 不会。结果会保存到输出文件夹；如果那里已有同名文件，旧文件会保留为.bak。Plugin.js编辑器的做法相同。确认无误后，请自行把结果复制到游戏文件夹。 | 아니요. 결과는 출력 폴더에 저장되며, 같은 이름의 파일이 이미 있으면 이전 파일은 .bak으로 보관됩니다. Plugin.js 편집기도 같은 방식으로 동작합니다. 문제가 없다고 확신하면 결과를 직접 게임 폴더에 복사하세요.
What does the "too long" warning in Translation Scan mean? | 翻訳スキャンの「長すぎる」という警告は何を意味しますか? | 翻译扫描中的“过长”警告是什么意思？ | 번역 스캔의 '너무 김' 경고는 무엇을 의미하나요?
A script check says "may be wrong". Is it broken? | スクリプトのチェックで「誤りの可能性があります」と表示されました。壊れているのですか? | 脚本检查提示“可能有误”。是损坏了吗？ | 스크립트 검사에서 '오류일 수 있음'이라고 나옵니다. 망가진 건가요?
Is it OK to unpack copyrighted games? | 著作権のあるゲームを展開しても問題ありませんか? | 解包受版权保护的游戏可以吗？ | 저작권이 있는 게임을 풀어도 괜찮은가요?
Archive format not recognised | アーカイブ形式が認識されません | 无法识别压缩包格式 | 아카이브 형식이 인식되지 않음
Extracted file names are garbled | 展開したファイル名が文字化けします | 提取的文件名是乱码 | 추출한 파일 이름이 깨짐
Import Text changes nothing | テキストの取り込みで何も変わりません | 导入文本没有任何变化 | 텍스트 가져오기를 해도 아무것도 바뀌지 않음
Cannot write files or the output folder is unusable | ファイルを書き込めない、または出力フォルダが使えません | 无法写入文件或输出文件夹不可用 | 파일을 쓸 수 없거나 출력 폴더를 사용할 수 없음
A script is reported to have wrong quotes or tags | スクリプトに誤った引用符やタグがあると報告されます | 脚本被报告存在错误的引号或标签 | 스크립트에 잘못된 따옴표나 태그가 있다고 보고됨
"""
    const val T6 = """
1. On the Game Tools tab choose RPGM Tools (RPG Maker) or VN Tools (visual novels). The Support Tools tab holds the Translation Editor, Text Editor, File & Folder Inspector, File Toolkit and Key Finder; the Settings tab holds settings, Guide, Source and About.\n2. Pick a file or folder with the upload card (the Android file picker).\n3. Pick an output folder, then press the process button.\n4. Watch progress and the log on the same screen; the result is in the output folder. | 1. ゲームツールタブで RPGMツール（RPG Maker）または VNツール（ビジュアルノベル）を選びます。サポートツールタブには、翻訳エディター、テキストエディター、ファイル・フォルダー検査、ファイルツールキット、キーファインダーがあります。設定タブには、各種設定、ガイド、ソース、アプリ情報があります。\n2. アップロードカード（Android のファイルピッカー）でファイルまたはフォルダーを選びます。\n3. 出力フォルダーを選び、処理ボタンを押します。\n4. 同じ画面で進行状況とログを確認できます。結果は出力フォルダーに保存されます。 | 1. 在游戏工具标签页中选择 RPGM 工具（RPG Maker）或 VN 工具（视觉小说）。辅助工具标签页包含翻译编辑器、文本编辑器、文件与文件夹检查器、文件工具箱和密钥查找器；设置标签页包含各项设置、指南、来源和关于。\n2. 通过上传卡片（Android 文件选择器）选择文件或文件夹。\n3. 选择输出文件夹，然后点击处理按钮。\n4. 在同一界面查看进度和日志，结果会保存在输出文件夹中。 | 1. 게임 도구 탭에서 RPGM 도구(RPG Maker) 또는 VN 도구(비주얼 노벨)를 선택합니다. 지원 도구 탭에는 번역 편집기, 텍스트 편집기, 파일 및 폴더 검사기, 파일 툴킷, 키 파인더가 있고, 설정 탭에는 각종 설정, 가이드, 출처, 정보가 있습니다.\n2. 업로드 카드(Android 파일 선택기)로 파일 또는 폴더를 선택합니다.\n3. 출력 폴더를 선택한 다음 처리 버튼을 누릅니다.\n4. 같은 화면에서 진행 상황과 로그를 확인할 수 있으며, 결과는 출력 폴더에 저장됩니다.
Renames files and folders whose names contain special characters (non-Latin, optionally spaces and special marks) to safe names and updates the game files that refer to them (json, js, html, ks, rpy, etc.). Useful when porting a game to APK because APK builders often fail on non-Latin names. Pick the Source Folder, set the File scope (all files with special characters, including RPG Maker encrypted files, or only media assets) and the New name style (ASCII part kept), press Scan and Preview, then Rename and Update References. The optional Backup Folder keeps original copies of edited text files; every change is recorded in rename_mapping.json and can be reverted with Restore Original Names. There is also a Build Readiness Report (problem names, names clashing by letter case, formats often unsupported on Android) and a Fix reference letter case option. Scan, Apply and Restore run in the background: you can leave the screen, and with Background Processing on you get progress and completion notifications. | 特殊文字（ラテン文字以外、必要に応じてスペースや特殊記号）を含むファイル名やフォルダー名を安全な名前に変更し、それらを参照しているゲームファイル（json、js、html、ks、rpy など）も更新します。APK ビルダーはラテン文字以外の名前で失敗することが多いため、ゲームを APK に移植するときに便利です。ソースフォルダーを選び、ファイル範囲（RPG Maker の暗号化ファイルを含む特殊文字を含むすべてのファイル、またはメディアアセットのみ）と新しい名前のスタイル（ASCII 部分を維持）を設定し、スキャンとプレビューを押してから、名前変更と参照の更新を押します。任意のバックアップフォルダーには、編集したテキストファイルの元のコピーが保存されます。すべての変更は rename_mapping.json に記録され、元の名前に戻すで元に戻せます。ビルド準備レポート（問題のある名前、大文字小文字だけが異なる名前の衝突、Android で非対応のことが多い形式）と、参照の大文字小文字を修正するオプションもあります。スキャン、適用、復元はバックグラウンドで実行されるため、画面を離れても問題ありません。バックグラウンド処理をオンにしておくと、進行状況と完了の通知が届きます。 | 将名称中含有特殊字符（非拉丁字符，也可包括空格和特殊符号）的文件和文件夹重命名为安全名称，并更新引用它们的游戏文件（json、js、html、ks、rpy 等）。由于 APK 打包工具经常无法处理非拉丁字符的名称，因此在将游戏移植为 APK 时很有用。选择源文件夹，设置文件范围（所有含特殊字符的文件，包括 RPG Maker 加密文件，或仅媒体资源）和新名称样式（保留 ASCII 部分），点击扫描并预览，然后点击重命名并更新引用。可选的备份文件夹会保存被编辑文本文件的原始副本；所有更改都会记录在 rename_mapping.json 中，可通过恢复原名撤销。此外还有打包就绪报告（问题名称、仅大小写不同而冲突的名称、Android 上常不支持的格式）以及修正引用大小写选项。扫描、应用和恢复都在后台运行：你可以离开此界面，开启后台处理后会收到进度和完成通知。 | 이름에 특수 문자(라틴 문자가 아닌 문자, 필요에 따라 공백과 특수 기호)가 포함된 파일과 폴더를 안전한 이름으로 바꾸고, 이를 참조하는 게임 파일(json, js, html, ks, rpy 등)도 함께 업데이트합니다. APK 빌더는 라틴 문자가 아닌 이름에서 실패하는 경우가 많기 때문에 게임을 APK로 이식할 때 유용합니다. 소스 폴더를 선택하고, 파일 범위(RPG Maker 암호화 파일을 포함해 특수 문자가 있는 모든 파일, 또는 미디어 에셋만)와 새 이름 스타일(ASCII 부분 유지)을 설정한 뒤 스캔 및 미리보기를 누르고, 이름 변경 및 참조 업데이트를 누릅니다. 선택 사항인 백업 폴더에는 수정된 텍스트 파일의 원본 사본이 보관됩니다. 모든 변경 내용은 rename_mapping.json에 기록되며 원래 이름 복원으로 되돌릴 수 있습니다. 빌드 준비 보고서(문제가 있는 이름, 대소문자만 달라 충돌하는 이름, Android에서 지원되지 않는 경우가 많은 형식)와 참조 대소문자 수정 옵션도 있습니다. 스캔, 적용, 복원은 백그라운드에서 실행되므로 화면을 벗어나도 되며, 백그라운드 처리를 켜 두면 진행 상황과 완료 알림을 받을 수 있습니다.
Support Tools > File & Folder Inspector > Hex Viewer | サポートツール > ファイル・フォルダー検査 > 16進ビューア | 辅助工具 > 文件与文件夹检查器 > 十六进制查看器 | 지원 도구 > 파일 및 폴더 검사기 > 헥스 뷰어
Support Tools > File Toolkit > File Joiner | サポートツール > ファイルツールキット > ファイル結合 | 辅助工具 > 文件工具箱 > 文件合并 | 지원 도구 > 파일 툴킷 > 파일 병합
VN Tools > Script Tools | VNツール > スクリプトツール | VN 工具 > 脚本工具 | VN 도구 > 스크립트 도구
RPGM Tools > Translation | RPGMツール > 翻訳 | RPGM 工具 > 翻译 | RPGM 도구 > 번역
File & Folder Inspector > Folder Inspection | ファイル・フォルダー検査 > フォルダー検査 | 文件与文件夹检查器 > 文件夹检查 | 파일 및 폴더 검사기 > 폴더 검사
How to Use, Q&A and Troubleshooting. The Source (references and their links) and About menus are in the Settings tab. The About menu has contact, donation, Report a Problem (an email with app and device info filled in, last log optional), and Licences and Thanks. | 使い方、Q&A、トラブルシューティングがあります。ソース（参考資料とそのリンク）とアプリ情報メニューは設定タブにあります。アプリ情報メニューには、連絡先、寄付、問題を報告（アプリとデバイスの情報が入力済みのメールで、最新のログは任意）、ライセンスと謝辞があります。 | 包含使用方法、问答和故障排除。来源（参考资料及其链接）和关于菜单位于设置标签页中。关于菜单包含联系方式、捐赠、报告问题（会生成已填入应用和设备信息的邮件，最近的日志为可选项）以及许可和致谢。 | 사용 방법, Q&A, 문제 해결이 있습니다. 출처(참고 자료와 링크)와 정보 메뉴는 설정 탭에 있습니다. 정보 메뉴에는 연락처, 후원, 문제 신고(앱과 기기 정보가 미리 입력된 이메일이며 최근 로그는 선택 사항), 라이선스 및 감사의 말이 있습니다.
Why are some developer names in the Engine List? | エンジン一覧に開発元の名前が表示されるのはなぜですか？ | 为什么引擎列表中会出现一些开发商名称？ | 엔진 목록에 개발사 이름이 표시되는 이유는 무엇인가요?
How do I get the key for Siglus Gameexe.dat? | Siglus Gameexe.dat のキーはどうやって取得しますか？ | 如何获取 Siglus Gameexe.dat 的密钥？ | Siglus Gameexe.dat의 키는 어떻게 얻나요?
How do I translate an RPGM game? | RPGM ゲームを翻訳するにはどうしますか？ | 如何翻译 RPGM 游戏？ | RPGM 게임은 어떻게 번역하나요?
What is the File & Folder Inspector for and which tool should I use? | ファイル・フォルダー検査は何のためのもので、どのツールを使えばよいですか？ | 文件与文件夹检查器有什么用，应该使用哪个工具？ | 파일 및 폴더 검사기는 무엇에 쓰는 것이며 어떤 도구를 사용해야 하나요?
Folder Inspection stops at 5000 files, how do I continue? | フォルダー検査が 5000 ファイルで止まります。続けるにはどうしますか？ | 文件夹检查在 5000 个文件处停止，如何继续？ | 폴더 검사가 5000개 파일에서 멈춥니다. 어떻게 계속하나요?
Why does a @default value not change the game? | @default の値を変えてもゲームに反映されないのはなぜですか？ | 为什么修改 @default 的值后游戏没有变化？ | @default 값을 바꿔도 게임에 반영되지 않는 이유는 무엇인가요?
Some translation lines are longer than the limit you chose (control codes are not counted), so they may be cut off in the game dialog box. Adjust them with auto wrap in the Translation Editor. | 一部の翻訳行が、選択した上限より長くなっています（制御コードは数えません）。そのため、ゲームのメッセージウィンドウで途切れる可能性があります。翻訳エディターの自動折り返しで調整してください。 | 部分翻译行超出了你所选的长度上限（控制代码不计入），因此在游戏对话框中可能会被截断。请使用翻译编辑器中的自动换行进行调整。 | 일부 번역 줄이 선택한 제한보다 깁니다(제어 코드는 포함되지 않음). 그래서 게임 대화 상자에서 잘릴 수 있습니다. 번역 편집기의 자동 줄바꿈으로 조정하세요.
Not necessarily. Script checks are heuristic (e.g. unpaired quotes, incomplete if/endif tags). Treat them as warnings to verify manually. | 必ずしもそうではありません。スクリプトのチェックはヒューリスティックです（例：対応していない引用符、不完全な if/endif タグ）。警告として扱い、手動で確認してください。 | 不一定。脚本检查是启发式的（例如引号不成对、if/endif 标签不完整）。请将其视为警告，并手动核实。 | 꼭 그렇지는 않습니다. 스크립트 검사는 휴리스틱 방식입니다(예: 짝이 맞지 않는 따옴표, 불완전한 if/endif 태그). 경고로 받아들이고 직접 확인하세요.
Only use it on games you own, for personal purposes such as translation or modding, and follow the game creator's rules. | ご自身が所有するゲームに対してのみ、翻訳や改造などの個人的な目的で使用し、ゲーム制作者のルールに従ってください。 | 请仅对你拥有的游戏使用，且仅用于翻译或修改等个人用途，并遵守游戏作者的规定。 | 본인이 소유한 게임에만 사용하고, 번역이나 모딩 같은 개인적인 용도로만 사용하며, 게임 제작자의 규칙을 따르세요.
• Try File & Folder Inspector > File Inspection to see the engine guessed from the extension.\n• If you know the engine, open it via VN Tools > Engine List.\n• The archive may use game-specific encryption or an unsupported format version. | • ファイル・フォルダー検査 > ファイル検査 で、拡張子から推測されたエンジンを確認してください。\n• エンジンが分かっている場合は、VNツール > エンジン一覧 から開いてください。\n• アーカイブがゲーム固有の暗号化や、未対応のフォーマットバージョンを使っている可能性があります。 | • 尝试使用 文件与文件夹检查器 > 文件检查，查看根据扩展名推测出的引擎。\n• 如果你知道引擎，请通过 VN 工具 > 引擎列表 打开。\n• 该压缩包可能使用了游戏专用加密，或是不受支持的格式版本。 | • 파일 및 폴더 검사기 > 파일 검사에서 확장자로 추정된 엔진을 확인해 보세요.\n• 엔진을 알고 있다면 VN 도구 > 엔진 목록에서 여세요.\n• 아카이브가 게임 전용 암호화를 사용하거나 지원되지 않는 형식 버전일 수 있습니다.
Change the Filename encoding option (Shift-JIS, UTF-8, GBK, Big5, EUC-KR). The list reloads automatically. | ファイル名のエンコード設定（Shift-JIS、UTF-8、GBK、Big5、EUC-KR）を変更してください。リストは自動的に再読み込みされます。 | 更改文件名编码选项（Shift-JIS、UTF-8、GBK、Big5、EUC-KR）。列表会自动重新加载。 | 파일 이름 인코딩 옵션(Shift-JIS, UTF-8, GBK, Big5, EUC-KR)을 변경하세요. 목록이 자동으로 다시 로드됩니다.
Choose the same data folder you exported from, do not edit the file and path columns, and make sure the translation column is filled. Check the entry counts with Translation Scan. | エクスポートしたときと同じデータフォルダーを選び、ファイル列とパス列は編集せず、翻訳列が入力されていることを確認してください。エントリー数は翻訳スキャンで確認できます。 | 选择与导出时相同的数据文件夹，不要编辑文件列和路径列，并确保翻译列已填写。可以使用翻译扫描检查条目数量。 | 내보낼 때와 같은 데이터 폴더를 선택하고, 파일 열과 경로 열은 수정하지 말며, 번역 열이 채워져 있는지 확인하세요. 항목 수는 번역 스캔으로 확인할 수 있습니다.
Pick the output folder again with the Android folder picker and grant access. Avoid system folders or restricted SD cards; try a folder in internal storage. | Android のフォルダーピッカーで出力フォルダーをもう一度選び、アクセスを許可してください。システムフォルダーや制限のある SD カードは避け、内部ストレージ内のフォルダーを試してください。 | 使用 Android 文件夹选择器重新选择输出文件夹并授予访问权限。避免使用系统文件夹或受限的 SD 卡，请尝试内部存储中的文件夹。 | Android 폴더 선택기로 출력 폴더를 다시 선택하고 접근 권한을 허용하세요. 시스템 폴더나 제한된 SD 카드는 피하고 내부 저장소의 폴더를 사용해 보세요.
That is a heuristic warning, not a certainty. Open the mentioned line and check it manually; in translated scripts make sure quotes and tags stay paired. | これは確実なものではなく、ヒューリスティックによる警告です。該当する行を開いて手動で確認してください。翻訳済みスクリプトでは、引用符とタグの対応が保たれていることを確認してください。 | 这是启发式警告，并不一定是错误。请打开所提到的那一行并手动检查；在已翻译的脚本中，请确保引号和标签保持成对。 | 이것은 확정이 아니라 휴리스틱 경고입니다. 해당 줄을 열어 직접 확인하세요. 번역된 스크립트에서는 따옴표와 태그의 짝이 유지되어야 합니다.
VN Tools > Unpack Archive | VNツール > アーカイブ展開 | VN 工具 > 解包压缩包 | VN 도구 > 아카이브 풀기
Support Tools > Media Asset Converter | サポートツール > メディアアセット変換 | 辅助工具 > 媒体资源转换器 | 지원 도구 > 미디어 에셋 변환기
Views raw file content by offset.\n• Pick a file; content is shown 128 rows per page with offset, hex and ASCII columns (8 or 16 bytes per row). The go-to offset (hex) field with Go, the search mode and box, and the Find button sit above the content; page navigation « ‹ › » (first, back, next, last) is below the Find button and below the content.\n• Search: type text (UTF-8 or Shift-JIS) or hex bytes (e.g. 4D 5A 90). Find next continues from the previous hit and shows the hit on the first row. | ファイルの生データをオフセット単位で表示します。\n• ファイルを選ぶと、内容が1ページ128行で表示されます（オフセット、16進、ASCII の列。1行あたり8バイトまたは16バイト）。移動先オフセット（16進）欄と移動ボタン、検索モードと検索ボックス、検索ボタンは内容の上にあり、ページ移動 « ‹ › »（最初、前へ、次へ、最後）は検索ボタンの下と内容の下にあります。\n• 検索：テキスト（UTF-8 または Shift-JIS）または16進バイト列（例：4D 5A 90）を入力します。次を検索は前回の一致位置から続けて検索し、一致した箇所を先頭行に表示します。 | 按偏移量查看文件的原始内容。\n• 选择文件后，内容按每页 128 行显示，包含偏移量、十六进制和 ASCII 列（每行 8 或 16 字节）。跳转偏移量（十六进制）输入框及跳转按钮、搜索模式和搜索框、查找按钮位于内容上方；翻页按钮 « ‹ › »（首页、上一页、下一页、末页）位于查找按钮下方和内容下方。\n• 搜索：输入文本（UTF-8 或 Shift-JIS）或十六进制字节（例如 4D 5A 90）。查找下一个会从上一个匹配处继续，并将匹配项显示在第一行。 | 파일의 원본 내용을 오프셋 기준으로 보여 줍니다.\n• 파일을 선택하면 내용이 페이지당 128행으로 표시되며 오프셋, 헥스, ASCII 열이 있습니다(행당 8바이트 또는 16바이트). 이동할 오프셋(헥스) 입력란과 이동 버튼, 검색 모드와 검색 상자, 찾기 버튼은 내용 위쪽에 있고, 페이지 이동 « ‹ › »(처음, 이전, 다음, 마지막)는 찾기 버튼 아래와 내용 아래에 있습니다.\n• 검색: 텍스트(UTF-8 또는 Shift-JIS)나 헥스 바이트(예: 4D 5A 90)를 입력합니다. 다음 찾기는 이전 검색 결과에서 이어서 찾고, 찾은 위치를 첫 번째 행에 표시합니다.
The reverse of File Splitter: joins numbered parts (.001, .002, etc.) back into one file.\n• Pick parts manually (long-press a file in the file picker to select several), or pick a folder: every numbered part set inside is detected automatically (one level) and can be joined in one go.\n• The order follows the number at the end of the name, not the order you picked them. The app checks that the parts belong to one file, have no duplicate numbers and no missing numbers; if something is wrong the message is shown and the join button stays disabled.\n• If a .sha256 file is available (picked together with the parts, or found in the folder), the result is verified automatically and the outcome is shown when done.\n• The result is saved to the Output Folder using the parts' base name (the name can be changed for a manual pick). | ファイル分割の逆の操作で、番号付きのパーツ（.001、.002 など）を1つのファイルに結合します。\n• パーツを手動で選ぶか（ファイルピッカーでファイルを長押しすると複数選択できます）、フォルダーを選びます。フォルダー内の番号付きパーツのセットはすべて自動的に検出され（1階層のみ）、まとめて結合できます。\n• 順序は選んだ順ではなく、名前の末尾の番号に従います。アプリは、パーツが同じファイルのものであること、番号の重複や欠番がないことを確認します。問題があるとメッセージが表示され、結合ボタンは無効のままになります。\n• .sha256 ファイルがある場合（パーツと一緒に選んだ、またはフォルダー内で見つかった場合）、結果は自動的に検証され、完了時に結果が表示されます。\n• 結果は、パーツの基本名を使って出力フォルダーに保存されます（手動で選んだ場合は名前を変更できます）。 | 这是文件拆分的逆操作：将带编号的分卷（.001、.002 等）合并回一个文件。\n• 可手动选择分卷（在文件选择器中长按文件可多选），也可选择文件夹：其中所有带编号的分卷组会被自动识别（仅一层），并可一次性合并。\n• 顺序依据名称末尾的编号，而不是你选择的顺序。应用会检查这些分卷是否属于同一个文件、是否有重复编号或缺失编号；如有问题会显示提示，合并按钮保持禁用。\n• 如果存在 .sha256 文件（与分卷一起选择，或在文件夹中找到），会自动校验结果，并在完成时显示校验结果。\n• 结果会以分卷的基本名称保存到输出文件夹（手动选择时可以更改名称）。 | 파일 분할의 반대 기능으로, 번호가 붙은 조각(.001, .002 등)을 하나의 파일로 합칩니다.\n• 조각을 직접 선택하거나(파일 선택기에서 파일을 길게 누르면 여러 개를 선택할 수 있습니다) 폴더를 선택합니다. 폴더 안의 번호가 붙은 조각 묶음이 모두 자동으로 감지되며(한 단계만) 한 번에 합칠 수 있습니다.\n• 순서는 선택한 순서가 아니라 이름 끝의 번호를 따릅니다. 앱은 조각들이 하나의 파일에 속하는지, 중복된 번호나 빠진 번호가 없는지 확인하며, 문제가 있으면 메시지가 표시되고 합치기 버튼은 비활성 상태로 유지됩니다.\n• .sha256 파일이 있으면(조각과 함께 선택했거나 폴더에서 발견된 경우) 결과가 자동으로 검증되고 완료 시 결과가 표시됩니다.\n• 결과는 조각의 기본 이름으로 출력 폴더에 저장됩니다(직접 선택한 경우 이름을 바꿀 수 있습니다).
Works on scripts; the type is detected per file (.rpy, .ks, .s, .cst, .dat Siglus, .bin Escu:de, .hcb, .ws2, .mes (Silky/Circus), BGI, .csx EntisGLS, .bin NeXAS, .txt).\n• Export Script Text: collects dialogue and choices from all scripts in a folder into one CSV, TXT or JSON file.\n• Import Script Text: writes translations back into the scripts.\n• Script Contents List: writes every file name inside an archive to a text file.\nEngine-specific tools (e.g. rpyc → rpy, nscript.dat decode) are under Engine List.\n• Export Script Text also defaults to TXT and shows a Translate text button in its result. | スクリプトを扱います。種類はファイルごとに検出されます（.rpy、.ks、.s、.cst、.dat Siglus、.bin Escu:de、.hcb、.ws2、.mes（Silky/Circus）、BGI、.csx EntisGLS、.bin NeXAS、.txt）。\n• スクリプトテキストをエクスポート：フォルダー内のすべてのスクリプトから台詞と選択肢を集めて、1つの CSV、TXT、JSON ファイルにまとめます。\n• スクリプトテキストをインポート：翻訳をスクリプトに書き戻します。\n• スクリプト内容リスト：アーカイブ内のすべてのファイル名をテキストファイルに書き出します。\nエンジン固有のツール（例：rpyc → rpy、nscript.dat のデコード）はエンジン一覧にあります。\n• スクリプトテキストをエクスポートも初期設定は TXT で、結果画面に「テキストを翻訳」ボタンが表示されます。 | 用于处理脚本，类型按文件自动识别（.rpy、.ks、.s、.cst、.dat Siglus、.bin Escu:de、.hcb、.ws2、.mes（Silky/Circus）、BGI、.csx EntisGLS、.bin NeXAS、.txt）。\n• 导出脚本文本：将文件夹中所有脚本的对话和选项收集到一个 CSV、TXT 或 JSON 文件中。\n• 导入脚本文本：将翻译写回脚本。\n• 脚本内容列表：将压缩包内所有文件名写入一个文本文件。\n特定引擎的工具（例如 rpyc → rpy、nscript.dat 解码）位于引擎列表中。\n• 导出脚本文本的默认格式同样是 TXT，完成后结果页会显示翻译文本按钮。 | 스크립트를 다룹니다. 형식은 파일마다 감지됩니다(.rpy, .ks, .s, .cst, .dat Siglus, .bin Escu:de, .hcb, .ws2, .mes(Silky/Circus), BGI, .csx EntisGLS, .bin NeXAS, .txt).\n• 스크립트 텍스트 내보내기: 폴더의 모든 스크립트에서 대사와 선택지를 모아 하나의 CSV, TXT 또는 JSON 파일로 만듭니다.\n• 스크립트 텍스트 가져오기: 번역을 스크립트에 다시 씁니다.\n• 스크립트 내용 목록: 아카이브 안의 모든 파일 이름을 텍스트 파일로 저장합니다.\n엔진 전용 도구(예: rpyc → rpy, nscript.dat 디코드)는 엔진 목록에 있습니다.\n• 스크립트 텍스트 내보내기도 기본값은 TXT이며, 결과 화면에 텍스트 번역 버튼이 표시됩니다.
• Export Text: collects dialogue and names from data/*.json into one file (CSV, TXT or JSON).\n• Import Text: writes translations back. Choose the same data folder you exported from, or the data folder itself to overwrite.\n• The default export format is TXT (CSV or JSON can be chosen). When Export Text finishes, the Translate text button opens the Translation Editor with the exported file already loaded. | • テキストをエクスポート：data/*.json から台詞と名前を集めて、1つのファイル（CSV、TXT、JSON）にまとめます。\n• テキストをインポート：翻訳を書き戻します。エクスポートしたときと同じデータフォルダーを選ぶか、上書きする場合はデータフォルダーそのものを選んでください。\n• エクスポート形式の初期設定は TXT です（CSV または JSON も選べます）。テキストをエクスポートが完了すると、「テキストを翻訳」ボタンから、エクスポートしたファイルが読み込み済みの翻訳エディターが開きます。 | • 导出文本：将 data/*.json 中的对话和名称收集到一个文件（CSV、TXT 或 JSON）中。\n• 导入文本：将翻译写回。请选择与导出时相同的数据文件夹，或选择数据文件夹本身以覆盖。\n• 默认导出格式为 TXT（也可选择 CSV 或 JSON）。导出文本完成后，点击翻译文本按钮会打开翻译编辑器，并已载入导出的文件。 | • 텍스트 내보내기: data/*.json에서 대사와 이름을 모아 하나의 파일(CSV, TXT 또는 JSON)로 만듭니다.\n• 텍스트 가져오기: 번역을 다시 씁니다. 내보낼 때 사용한 것과 같은 데이터 폴더를 선택하거나, 덮어쓰려면 데이터 폴더 자체를 선택하세요.\n• 기본 내보내기 형식은 TXT입니다(CSV나 JSON도 선택할 수 있습니다). 텍스트 내보내기가 끝나면 텍스트 번역 버튼으로 내보낸 파일이 이미 불러와진 번역 편집기를 열 수 있습니다.
Support Tools tab > Translation Editor. Opens an export (CSV/TXT/JSON) from RPGM or VN so you can translate inside the app.\n• All / Empty / Filled filter, search and jump to the next empty field.\n• Editor Settings: first letter, auto wrap, replace words. Glossary and auto-fill identical entries.\n• Undo bulk changes (up to 10 steps). A warning shows when control codes differ from the original.\n• Save back in the same format.\n• Globe icon = Online Translation (full page): Translation Service (Lingva Translate Free with no API key, or Use API Key: Google, DeepL, Gemini, OpenAI, DeepSeek, Claude, Groq, Custom; the model is picked from a dropdown and can be loaded from your account), Translation Languages, Translation Rows (empty fields / this page / all rows) and its own glossary. Parts without letters (sound effects/expressions) are filled with the Original Text when that option is on. Control codes are protected automatically; rows that lose a code are skipped and can be retried with "Retry failed". LLM providers get a language style, context notes and an option to send the 3 previous rows as context. There is also Test connection, parallel requests, a result cache, auto line wrap and an automatic glossary generator. In the row list, the "Online" filter shows results you have not edited yet. Needs internet. | サポートツールタブ > 翻訳エディター。RPGM または VN からエクスポートしたファイル（CSV/TXT/JSON）を開き、アプリ内で翻訳できます。\n• すべて／空／入力済みのフィルター、検索、次の空欄へのジャンプ。\n• エディター設定：先頭の文字、自動折り返し、単語の置換。用語集と、同一エントリーの自動入力。\n• 一括変更の取り消し（最大10回）。制御コードが元の内容と異なる場合は警告が表示されます。\n• 同じ形式で保存し直せます。\n• 地球儀アイコン＝オンライン翻訳（全画面）：翻訳サービス（API キー不要の Lingva Translate 無料版、または API キーを使用：Google、DeepL、Gemini、OpenAI、DeepSeek、Claude、Groq、カスタム。モデルはドロップダウンから選び、アカウントから読み込むこともできます）、翻訳言語、翻訳行（空欄／このページ／すべての行）、専用の用語集があります。文字を含まない部分（効果音や表情など）は、そのオプションがオンの場合、原文で埋められます。制御コードは自動的に保護され、コードが失われた行はスキップされ、「失敗分を再試行」でやり直せます。LLM プロバイダーでは、言語スタイル、文脈メモ、直前の3行を文脈として送るオプションを使えます。接続テスト、並列リクエスト、結果キャッシュ、自動行折り返し、自動用語集生成もあります。行リストの「オンライン」フィルターは、まだ編集していない翻訳結果を表示します。インターネット接続が必要です。 | 辅助工具标签页 > 翻译编辑器。可打开来自 RPGM 或 VN 的导出文件（CSV/TXT/JSON），让你在应用内直接翻译。\n• 全部／空白／已填写筛选、搜索，以及跳转到下一个空白项。\n• 编辑器设置：首字母、自动换行、替换词语。术语表和自动填充相同条目。\n• 撤销批量更改（最多 10 步）。当控制代码与原文不一致时会显示警告。\n• 以相同格式保存回去。\n• 地球图标 = 在线翻译（整页）：翻译服务（无需 API 密钥的 Lingva Translate 免费版，或使用 API 密钥：Google、DeepL、Gemini、OpenAI、DeepSeek、Claude、Groq、自定义；模型从下拉列表中选择，也可从你的账号加载）、翻译语言、翻译行（空白项／本页／所有行），以及独立的术语表。不含文字的部分（音效／表情）在开启相应选项时会用原文填充。控制代码会被自动保护；丢失代码的行会被跳过，可通过“重试失败项”再次尝试。对于 LLM 提供商，可设置语言风格、上下文备注，以及发送前 3 行作为上下文的选项。此外还有测试连接、并行请求、结果缓存、自动换行和自动术语表生成器。在行列表中，“在线”筛选会显示你尚未编辑的结果。需要联网。 | 지원 도구 탭 > 번역 편집기. RPGM 또는 VN에서 내보낸 파일(CSV/TXT/JSON)을 열어 앱 안에서 번역할 수 있습니다.\n• 전체/비어 있음/입력됨 필터, 검색, 다음 빈 칸으로 이동.\n• 편집기 설정: 첫 글자, 자동 줄바꿈, 단어 바꾸기. 용어집과 동일 항목 자동 채우기.\n• 일괄 변경 되돌리기(최대 10단계). 제어 코드가 원본과 다르면 경고가 표시됩니다.\n• 같은 형식으로 다시 저장합니다.\n• 지구본 아이콘 = 온라인 번역(전체 화면): 번역 서비스(API 키가 필요 없는 Lingva Translate 무료 버전, 또는 API 키 사용: Google, DeepL, Gemini, OpenAI, DeepSeek, Claude, Groq, 사용자 지정; 모델은 드롭다운에서 선택하며 계정에서 불러올 수도 있습니다), 번역 언어, 번역 행(빈 칸/이 페이지/모든 행), 별도의 용어집이 있습니다. 글자가 없는 부분(효과음/표정)은 해당 옵션이 켜져 있으면 원문으로 채워집니다. 제어 코드는 자동으로 보호되며, 코드가 사라진 행은 건너뛰고 "실패 항목 다시 시도"로 다시 시도할 수 있습니다. LLM 제공업체에는 언어 스타일, 문맥 메모, 이전 3행을 문맥으로 보내는 옵션이 제공됩니다. 연결 테스트, 병렬 요청, 결과 캐시, 자동 줄바꿈, 자동 용어집 생성기도 있습니다. 행 목록에서 "온라인" 필터는 아직 편집하지 않은 결과를 보여 줍니다. 인터넷이 필요합니다.
Pick a game folder. Every archive and script inside is checked in turn (index and file start only, so it is fast; plain images, audio and video are skipped).\n• The list is sorted with problems first. Filters: All, !, ✕, ✓.\n• Tap a file for its full result; Back returns to the list.\n• Export .txt saves a summary of the whole folder. | ゲームフォルダーを選びます。中にあるすべてのアーカイブとスクリプトが順番にチェックされます（インデックスとファイルの先頭部分のみなので高速です。通常の画像、音声、動画はスキップされます）。\n• リストは問題のあるものが先頭になるよう並べ替えられます。フィルター：すべて、!、✕、✓。\n• ファイルをタップすると詳細な結果が表示され、戻るでリストに戻ります。\n• .txt をエクスポートで、フォルダー全体の概要を保存します。 | 选择游戏文件夹。其中的每个压缩包和脚本会依次被检查（仅检查索引和文件开头，因此速度很快；普通图片、音频和视频会被跳过）。\n• 列表按问题优先排序。筛选：全部、!、✕、✓。\n• 点击文件可查看完整结果，返回即可回到列表。\n• 导出 .txt 会保存整个文件夹的摘要。 | 게임 폴더를 선택합니다. 안에 있는 모든 아카이브와 스크립트를 차례로 검사합니다(인덱스와 파일 시작 부분만 확인하므로 빠르며, 일반 이미지, 오디오, 비디오는 건너뜁니다).\n• 목록은 문제가 있는 항목이 먼저 오도록 정렬됩니다. 필터: 전체, !, ✕, ✓.\n• 파일을 탭하면 전체 결과가 표시되고, 뒤로를 누르면 목록으로 돌아갑니다.\n• .txt 내보내기는 폴더 전체의 요약을 저장합니다.
Settings tab. Tool Settings are shown right at the top and save automatically when changed. App Settings opens from its menu; every change is applied and saved automatically.\n• Tool Settings: what to do when a file already exists on export (Overwrite, Skip, Rename) and the Default Output Folder used when no Output Folder is chosen.\n• Display settings: Display Style (Classic, Minimal, Soft, Gradient, Solid, plus the themed styles Sakura, Ocean, Forest, Sunset, Neon, Paper), Navigation Bar Style (Classic, Floating, Floating Icons, Icons Only, Sliding Pill, Top Line, Floating Button), theme mode (Follow system, Light, Dark or Schedule with dark start/end hours; default Follow system), theme colour, a Seasonal style (automatic by month), and switches for Animations and Haptic feedback. System settings also has a "Open with… for all files" switch.\n• Language settings: Follow system (default), English, Japanese, Chinese, Korean or Indonesian. If the system language is not supported, English is used. Japanese, Chinese and Korean currently cover only part of the interface (main menu, settings and Game Assets); the rest is shown in English. Kanji/hanzi/hangul use the system font.\n• System settings: Engine label on Game Assets covers (off by default), Background Processing (keeps running when the app is closed or the screen is off, with a notification), Keep screen on while running, Confirm exit for the Translation Editor and Value Editor (asks first when there are unsaved changes), and Native text input (uses Android's built-in text fields to avoid bugs). | 設定タブ。ツール設定は一番上に表示され、変更すると自動的に保存されます。アプリ設定はそのメニューから開き、すべての変更は自動的に適用・保存されます。\n• ツール設定：エクスポート時にファイルがすでに存在する場合の動作（上書き、スキップ、名前を変更）と、出力フォルダーが選ばれていないときに使う既定の出力フォルダー。\n• 表示設定：表示スタイル（クラシック、ミニマル、ソフト、グラデーション、ソリッド、テーマスタイルの桜、海、森、夕焼け、ネオン、和紙）、ナビゲーションバーのスタイル（クラシック、フローティング、フローティングアイコン、アイコンのみ、スライドピル、トップライン、フローティングボタン）、テーマモード（システムに従う、ライト、ダーク、またはダークの開始・終了時刻を指定するスケジュール。初期設定はシステムに従う）、テーマカラー、季節スタイル（月に応じて自動）、アニメーションと触覚フィードバックのスイッチ。システム設定には「すべてのファイルを開く」スイッチもあります。\n• 言語設定：システムに従う（初期設定）、英語、日本語、中国語、韓国語、インドネシア語。システム言語が未対応の場合は英語が使われます。日本語、中国語、韓国語は現在、画面の一部（メインメニュー、設定、ゲームアセット）のみ対応しており、それ以外は英語で表示されます。漢字／ハングルにはシステムフォントが使われます。\n• システム設定：ゲームアセットのカバーにエンジン名を表示（初期設定はオフ）、バックグラウンド処理（アプリを閉じたり画面がオフになっても実行を続け、通知を表示します）、実行中は画面をオンにしておく、翻訳エディターと値エディターの終了確認（未保存の変更があるときに先に確認します）、ネイティブテキスト入力（不具合を避けるため Android 標準のテキスト欄を使います）。 | 设置标签页。工具设置显示在最上方，更改后会自动保存。应用设置可从其菜单打开，所有更改都会自动应用并保存。\n• 工具设置：导出时文件已存在的处理方式（覆盖、跳过、重命名），以及未选择输出文件夹时使用的默认输出文件夹。\n• 显示设置：显示风格（经典、极简、柔和、渐变、纯色，以及主题风格樱花、海洋、森林、日落、霓虹、纸张）、导航栏风格（经典、悬浮、悬浮图标、仅图标、滑动胶囊、顶部线条、悬浮按钮）、主题模式（跟随系统、浅色、深色，或设置深色开始/结束时间的定时模式；默认跟随系统）、主题颜色、季节风格（按月份自动），以及动画和触觉反馈开关。系统设置中还有“用此应用打开所有文件”开关。\n• 语言设置：跟随系统（默认）、英语、日语、中文、韩语或印尼语。如果系统语言不受支持，则使用英语。日语、中文和韩语目前仅覆盖部分界面（主菜单、设置和游戏资源），其余部分以英语显示。汉字/韩文使用系统字体。\n• 系统设置：在游戏资源封面上显示引擎标签（默认关闭）、后台处理（关闭应用或熄屏后仍继续运行，并显示通知）、运行时保持屏幕常亮、翻译编辑器和值编辑器的退出确认（有未保存的更改时会先询问），以及原生文本输入（使用 Android 内置文本框以避免问题）。 | 설정 탭. 도구 설정은 맨 위에 표시되며 변경하면 자동으로 저장됩니다. 앱 설정은 해당 메뉴에서 열 수 있으며, 모든 변경 사항은 자동으로 적용되고 저장됩니다.\n• 도구 설정: 내보낼 때 파일이 이미 있을 경우의 동작(덮어쓰기, 건너뛰기, 이름 바꾸기)과, 출력 폴더를 선택하지 않았을 때 사용하는 기본 출력 폴더.\n• 화면 설정: 화면 스타일(클래식, 미니멀, 소프트, 그라데이션, 솔리드, 테마 스타일인 벚꽃, 바다, 숲, 노을, 네온, 종이), 내비게이션 바 스타일(클래식, 플로팅, 플로팅 아이콘, 아이콘만, 슬라이딩 필, 상단 라인, 플로팅 버튼), 테마 모드(시스템 설정 따르기, 라이트, 다크 또는 다크 시작/종료 시간을 정하는 일정; 기본값은 시스템 설정 따르기), 테마 색상, 계절 스타일(월에 따라 자동), 애니메이션과 햅틱 피드백 스위치. 시스템 설정에는 “모든 파일 열기” 스위치도 있습니다.\n• 언어 설정: 시스템 설정 따르기(기본값), 영어, 일본어, 중국어, 한국어 또는 인도네시아어. 시스템 언어가 지원되지 않으면 영어가 사용됩니다. 일본어, 중국어, 한국어는 현재 인터페이스의 일부(메인 메뉴, 설정, 게임 에셋)만 지원하며, 나머지는 영어로 표시됩니다. 한자/한글에는 시스템 글꼴이 사용됩니다.\n• 시스템 설정: 게임 에셋 표지에 엔진 라벨 표시(기본값은 꺼짐), 백그라운드 처리(앱을 닫거나 화면이 꺼져도 계속 실행되며 알림이 표시됩니다), 실행 중 화면 켜 두기, 번역 편집기와 값 편집기의 종료 확인(저장하지 않은 변경 사항이 있으면 먼저 물어봅니다), 기본 텍스트 입력(오류를 피하기 위해 Android 기본 텍스트 필드를 사용합니다).
Some games are recognised by their developer's archive format. When the engine name is known the list uses it with the developer in brackets, e.g. Adv32 (Circus) or FVP (Favorite). When it is not known (Escu:de, HexenHaus), the developer name is used. One developer can use different engines per title; if a game does not match, try another engine (e.g. Kirikiri or NeXAS for newer Circus titles). | 一部のゲームは、開発元のアーカイブ形式で認識されます。エンジン名が分かっている場合、リストにはエンジン名と、括弧内に開発元が表示されます（例：Adv32 (Circus)、FVP (Favorite)）。エンジン名が分からない場合（Escu:de、HexenHaus）は、開発元の名前が使われます。同じ開発元でもタイトルごとに異なるエンジンを使うことがあります。ゲームが一致しない場合は、別のエンジンを試してください（例：Circus の新しいタイトルには Kirikiri や NeXAS）。 | 部分游戏可通过其开发商的压缩包格式识别。当引擎名称已知时，列表会使用该名称，并在括号中标注开发商，例如 Adv32 (Circus) 或 FVP (Favorite)。当引擎未知时（Escu:de、HexenHaus），则使用开发商名称。同一开发商的不同作品可能使用不同的引擎；如果游戏不匹配，请尝试其他引擎（例如 Circus 的较新作品可尝试 Kirikiri 或 NeXAS）。 | 일부 게임은 개발사의 아카이브 형식으로 인식됩니다. 엔진 이름을 알 수 있으면 목록에 엔진 이름과 괄호 안의 개발사가 표시됩니다(예: Adv32 (Circus), FVP (Favorite)). 엔진을 알 수 없으면(Escu:de, HexenHaus) 개발사 이름이 사용됩니다. 한 개발사가 작품마다 다른 엔진을 사용할 수도 있으므로, 게임이 맞지 않으면 다른 엔진을 시도해 보세요(예: Circus의 최신 작품에는 Kirikiri나 NeXAS).
"""
    const val T7 = """
For Scene.pck you need not: the key is found automatically from the scene data. For Gameexe.dat, open Key Finder on the game folder (with Scene.pck), copy the key and paste it into the Key option. The Siglus key is not stored whole in SiglusEngine.exe (it is assembled at run time), so it is derived from the scene data. | Scene.pck の場合、キーは不要です。キーはシーンデータから自動的に見つかります。Gameexe.dat の場合は、ゲームフォルダー（Scene.pck を含む）でキーファインダーを開き、キーをコピーして、キーオプションに貼り付けてください。Siglus のキーは SiglusEngine.exe にそのままの形では保存されておらず（実行時に組み立てられます）、シーンデータから導き出されます。 | 对于 Scene.pck，不需要密钥：密钥会从场景数据中自动找到。对于 Gameexe.dat，请在游戏文件夹（包含 Scene.pck）上打开密钥查找器，复制密钥并粘贴到密钥选项中。Siglus 的密钥并未完整存储在 SiglusEngine.exe 中（它在运行时才组装），因此是从场景数据中推导出来的。 | Scene.pck에는 키가 필요하지 않습니다. 키는 장면 데이터에서 자동으로 찾습니다. Gameexe.dat의 경우 게임 폴더(Scene.pck 포함)에서 키 파인더를 열고, 키를 복사하여 키 옵션에 붙여넣으세요. Siglus 키는 SiglusEngine.exe에 통째로 저장되어 있지 않고(실행 중에 조합됩니다) 장면 데이터에서 도출됩니다.
Export Text → translate in the Translation Editor (or any editor) → Import Text into the data folder. Replace the game's data with the result. | テキストをエクスポート → 翻訳エディター（または任意のエディター）で翻訳 → データフォルダーにテキストをインポート。その結果でゲームのデータを置き換えてください。 | 导出文本 → 在翻译编辑器（或任意编辑器）中翻译 → 将文本导入到数据文件夹中。然后用结果替换游戏的数据。 | 텍스트 내보내기 → 번역 편집기(또는 아무 편집기)에서 번역 → 데이터 폴더에 텍스트 가져오기. 그 결과로 게임의 데이터를 교체하세요.
One file → File Inspection. A whole game folder → Folder Inspection. Two versions of a file (e.g. before and after an update) → Compare Files. A translation export → Translation Scan. To check that packing and unpacking work for every format on this device → Format Test. | 1つのファイル → ファイル検査。ゲームフォルダー全体 → フォルダー検査。ファイルの2つのバージョン（例：アップデート前後）→ ファイル比較。翻訳エクスポート → 翻訳スキャン。この端末で、すべての形式のパックと展開が動作するか確認する → フォーマットテスト。 | 单个文件 → 文件检查。整个游戏文件夹 → 文件夹检查。同一文件的两个版本（例如更新前后）→ 比较文件。翻译导出文件 → 翻译扫描。检查此设备上每种格式的打包和解包是否正常 → 格式测试。 | 파일 하나 → 파일 검사. 게임 폴더 전체 → 폴더 검사. 한 파일의 두 버전(예: 업데이트 전후) → 파일 비교. 번역 내보내기 파일 → 번역 스캔. 이 기기에서 모든 형식의 묶기와 풀기가 동작하는지 확인 → 포맷 테스트.
Inspection runs in batches of 5000 files. When it finishes, tap Continue with the next batch to check the rest. The summary (size, file count, engine) always covers the whole folder. | 検査は5000ファイルごとのバッチで実行されます。完了したら、「次のバッチを続ける」をタップして残りをチェックしてください。概要（サイズ、ファイル数、エンジン）は常にフォルダー全体が対象です。 | 检查以每批 5000 个文件的方式运行。完成后，点击“继续下一批”检查其余文件。摘要（大小、文件数、引擎）始终涵盖整个文件夹。 | 검사는 5000개 파일 단위의 배치로 실행됩니다. 끝나면 "다음 배치 계속"을 탭하여 나머지를 검사하세요. 요약(크기, 파일 수, 엔진)은 항상 폴더 전체를 기준으로 합니다.
Why does Compare say "same" although the archive content differs? | アーカイブの内容が違うのに、比較で「同じ」と表示されるのはなぜですか？ | 为什么压缩包内容不同，比较却显示“相同”？ | 아카이브 내용이 다른데 비교에서 "동일"이라고 나오는 이유는 무엇인가요?
Are my original files modified? | 元のファイルは変更されますか？ | 我的原始文件会被修改吗？ | 원본 파일이 수정되나요?
Siglus Scene.pck fails to unpack, or Pack Archive asks for the .meta file | Siglus Scene.pck を展開できない、または アーカイブをパックで .meta ファイルを求められる | Siglus Scene.pck 解包失败，或打包压缩包时要求提供 .meta 文件 | Siglus Scene.pck가 풀리지 않거나, 아카이브 묶기에서 .meta 파일을 요구합니다
How do I report a problem or suggest something? | 問題の報告や提案はどうすればよいですか？ | 如何报告问题或提出建议？ | 문제를 신고하거나 제안하려면 어떻게 하나요?
RPGM game shows a black screen or crashes after changing plugins | プラグインを変更した後、RPGM ゲームが黒い画面になる、またはクラッシュする | 更改插件后，RPGM 游戏出现黑屏或崩溃 | 플러그인을 바꾼 뒤 RPGM 게임에서 검은 화면이 나오거나 충돌합니다
Extracted content is corrupt or will not open | 展開した内容が壊れている、または開けない | 提取出的内容已损坏或无法打开 | 추출한 내용이 손상되었거나 열리지 않습니다
Translations look cut off or colour codes break in the game | 翻訳が途切れる、またはゲーム内で色コードが崩れる | 翻译显示被截断，或颜色代码在游戏中出错 | 번역이 잘려 보이거나 게임에서 색상 코드가 깨집니다
Storage is full | ストレージがいっぱいです | 存储空间已满 | 저장 공간이 가득 찼습니다
The app closes by itself or shows a blank screen | アプリが勝手に終了する、または空白の画面が表示される | 应用自行关闭或显示空白屏幕 | 앱이 저절로 종료되거나 빈 화면이 표시됩니다
Extract a visual novel archive.\n• Pick the archive. The format is detected automatically and its info (format, file count, supported engines) is shown.\n• If the format has options (e.g. filename encoding or XOR key) they appear below. Changing the encoding reloads the file list.\n• "View archive contents" browses files and folders with search and image/text Preview.\n• "Select files" extracts only part of the archive.\n• Pick an output folder and start. Afterwards you get Open Folder, Copy Log and Verify.\n• Preview supports images, audio (ogg, mp3, wav, m4a, flac, etc. with play/pause and a seek bar) and text. Long text is split into 50 KB pages with ‹ › buttons and a Copy this page button. | ビジュアルノベルのアーカイブを展開します。\n• アーカイブを選択します。形式は自動的に検出され、形式・ファイル数・対応エンジンなどの情報が表示されます。\n• 形式にオプション(ファイル名のエンコードやXORキーなど)がある場合は、下に表示されます。エンコードを変更するとファイル一覧が再読み込みされます。\n• 「アーカイブの内容を表示」では、検索と画像/テキストのプレビュー付きでファイルやフォルダを閲覧できます。\n• 「ファイルを選択」を使うと、アーカイブの一部だけを展開できます。\n• 出力フォルダを選んで開始します。完了後は「フォルダを開く」「ログをコピー」「検証」が使えます。\n• プレビューは画像、音声(ogg、mp3、wav、m4a、flacなど。再生/一時停止とシークバー付き)、テキストに対応しています。長いテキストは50 KBごとのページに分割され、‹ › ボタンと「このページをコピー」ボタンが使えます。 | 解包视觉小说压缩包。\n• 选择压缩包。应用会自动识别格式，并显示其信息（格式、文件数量、支持的引擎）。\n• 如果该格式有选项（例如文件名编码或 XOR 密钥），会显示在下方。更改编码会重新加载文件列表。\n• “查看压缩包内容”可浏览文件和文件夹，支持搜索以及图片/文本预览。\n• “选择文件”只解包压缩包中的一部分。\n• 选择输出文件夹后开始。完成后可使用“打开文件夹”、“复制日志”和“验证”。\n• 预览支持图片、音频（ogg、mp3、wav、m4a、flac 等，带播放/暂停和进度条）以及文本。长文本会按每页 50 KB 分页，可使用 ‹ › 按钮和“复制本页”按钮。 | 비주얼 노벨 아카이브를 풉니다.\n• 아카이브를 선택합니다. 형식이 자동으로 감지되며 형식, 파일 수, 지원 엔진 등의 정보가 표시됩니다.\n• 해당 형식에 옵션(예: 파일 이름 인코딩 또는 XOR 키)이 있으면 아래에 나타납니다. 인코딩을 변경하면 파일 목록이 다시 로드됩니다.\n• "아카이브 내용 보기"에서는 검색과 이미지/텍스트 미리보기와 함께 파일과 폴더를 탐색할 수 있습니다.\n• "파일 선택"은 아카이브의 일부만 풉니다.\n• 출력 폴더를 선택하고 시작합니다. 완료 후 "폴더 열기", "로그 복사", "검증"을 사용할 수 있습니다.\n• 미리보기는 이미지, 오디오(ogg, mp3, wav, m4a, flac 등, 재생/일시정지 및 탐색 막대 지원), 텍스트를 지원합니다. 긴 텍스트는 50 KB 단위 페이지로 나뉘며 ‹ › 버튼과 "이 페이지 복사" 버튼을 사용할 수 있습니다.
Converts game asset formats. The menu is in Support Tools, between File Helper Tools and Key Finder.\n• Every converter has a File / Folder toggle at the top. Files can be multi-selected; Folder processes all matching files (choose Include subfolders to recurse; the structure is kept in the output folder). Output Format defaults to Follow Source (same format as each source file); if the source format is not available as an output (e.g. MP3), red text appears and you must pick a format manually.\n• Compress (audio, image, video): choose Quality (1%–100% slider, 100% = no compression; the quality is compressed) or Size (estimated result size in KB; the size is targeted and must not exceed the source file size). PNG is compressed by reducing the number of colors. If the output format is the same the file is compressed directly; otherwise it is converted first, then compressed. Folder mode has an Only compress if larger than X KB option. The estimated result size is shown above the Start Conversion button; progress (percent and time left) and the result (Source vs Result size comparison) are shown below it. Folder mode exports into the output folder under the source folder's name (source "aset" + output "hasil" gives "hasil/aset/...").\n• Settings sections can be expanded/collapsed by tapping their title (like the Q&A menu). Settings Presets saves and applies all converter settings (built-in presets and Last used settings are included).\n• Folder mode: choose which source extensions to process (Source Extensions section); Create a sample and compare with the source (audio and image) converts one file first and shows a before/after comparison (audio is playable); Replace original files backs the original up to a _backup folder before overwriting it.\n• When finished, Result Files lists every file with an Undo button (deletes the result, or restores the original from _backup), a per-extension summary, and a completion notification.\n• Audio: Volume normalization (evens out loudness), Trim silence at start/end, and Keep OGG loop tags (LOOPSTART/LOOPLENGTH; written to both OGG Vorbis and OGG Opus). Image: PNG is compressed into a palette PNG (dithering option), and Round to a multiple keeps tileset sizes grid-aligned.\n• Audio: M4A (AAC), OGG (Opus, Android 10+), WAV, MP3, FLAC or OGG (Vorbis); set bitrate, sample rate, mono/stereo and volume (1%–200% slider). Video files can also be a source (the audio is extracted). MP3, FLAC and OGG (Vorbis) are encoded by Java code inside the app (not Android's encoders), so they are slower than M4A/Opus on long files. Notes: MP3 supports only 8000–48000 Hz and standard bitrates (values are rounded to the nearest); FLAC is lossless (bitrate is unused) and slightly larger than flac -3; OGG (Vorbis) always uses 44100 or 48000 Hz and stereo (mono sources are duplicated to two channels). Choose OGG (Vorbis) for looping BGM in RPGM MV/MZ and Ren'Py because the loop tags are written.\n• Image: PNG, JPG or WebP; set Resolution (Original, Percent with a 1%–100% slider, or Size Width × Height in px with Keep source aspect ratio), rotation and the JPG background for transparent images.\n• Video: MP4 (H.264 or H.265) or WebM (VP9); set resolution, bitrate (dropdown, default Auto), frame rate and audio. It can take a long time; keep the app open.\n• Mixed Assets: Folder mode only, for a folder holding audio, images and video together. Files are grouped by extension; each type has its own rules section (output format, compression, Advanced Settings) opened by tapping its title, and the switch on the right decides whether that type is processed (types missing from the folder are disabled automatically). Audio and images are processed first, video last. The Copy unrecognized files as they are option copies other files into the output folder. Text and Special are not included.\n• When finished, if some results are larger than their sources a Discard larger results button appears (with confirmation); the Cancel button during a run also asks for confirmation.\n• Special Assets: Circus .pcm (XPCM) audio to OGG/WAV and Circus .crx images to PNG (differential CRX is merged with its base image). Each converter also has per-engine presets (RPGM, Ren'Py, Kirikiri).\n• Text Encoding: Shift-JIS/GBK/Big5/EUC-KR/EUC-JP to UTF-8 or back, with BOM, line ending and invalid-character options. Folder mode only processes the extensions you enter.\n• Input Format and Output Format options are sorted A–Z ("All Available Formats" and "Follow Source" stay on top). Long explanations across the app are packed into a "See details (?)" link; tap it to open, tap anywhere to close.\n• After a conversion, Check Compatibility compares the results with the target engine's rules (format, file names, image size, text encoding). In Overwrite Originals mode a Repack this folder button jumps straight to Pack.\n• If the result file exists: Rename, Overwrite or Skip. Results are written to the output folder; source files are never changed. | ゲームアセットの形式を変換します。メニューは「サポートツール」内の「ファイルヘルパーツール」と「キーファインダー」の間にあります。\n• すべてのコンバーターの上部に「ファイル / フォルダ」の切り替えがあります。ファイルは複数選択できます。フォルダでは条件に合うすべてのファイルを処理します(「サブフォルダを含める」を選ぶと再帰的に処理し、構成は出力フォルダにそのまま保たれます)。出力形式の初期値は「元の形式に合わせる」(各元ファイルと同じ形式)です。元の形式が出力形式として使えない場合(MP3など)は赤字が表示されるので、手動で形式を選んでください。\n• 圧縮(音声、画像、動画): 「品質」(1%~100%のスライダー。100% = 無圧縮。品質を指定して圧縮します)または「サイズ」(結果の推定サイズをKB単位で指定。サイズを目標に圧縮し、元ファイルのサイズを超えてはいけません)を選びます。PNGは色数を減らして圧縮します。出力形式が同じ場合は直接圧縮し、異なる場合は先に変換してから圧縮します。フォルダモードには「Xキロバイトより大きい場合のみ圧縮」オプションがあります。推定結果サイズは「変換開始」ボタンの上に、進捗(パーセントと残り時間)と結果(元と結果のサイズ比較)はその下に表示されます。フォルダモードでは、元フォルダ名のフォルダの下に出力フォルダへ書き出されます(元が「aset」、出力が「hasil」なら「hasil/aset/...」になります)。\n• 設定セクションは、タイトルをタップすると展開/折りたたみできます(Q&Aメニューと同様です)。「設定プリセット」では、コンバーターのすべての設定を保存・適用できます(組み込みプリセットと「前回使用した設定」を含みます)。\n• フォルダモード: 処理する元の拡張子を選べます(「元の拡張子」セクション)。「サンプルを作成して元と比較」(音声と画像)は、まず1ファイルだけ変換して変換前後の比較を表示します(音声は再生できます)。「元ファイルを置き換える」は、上書きする前に元ファイルを_backupフォルダにバックアップします。\n• 完了すると、「結果ファイル」にすべてのファイルが「元に戻す」ボタン付きで一覧表示されます(結果を削除するか、_backupから元ファイルを復元します)。拡張子ごとの集計と完了通知も表示されます。\n• 音声: 「音量の正規化」(音量を均一にします)、「先頭/末尾の無音をカット」、「OGGループタグを保持」(LOOPSTART/LOOPLENGTH。OGG VorbisとOGG Opusの両方に書き込まれます)。画像: PNGはパレットPNG(ディザリングのオプションあり)に圧縮され、「倍数に丸める」でタイルセットのサイズがグリッドに揃います。\n• 音声: M4A (AAC)、OGG (Opus、Android 10以上)、WAV、MP3、FLAC、OGG (Vorbis)に対応し、ビットレート、サンプルレート、モノラル/ステレオ、音量(1%~200%のスライダー)を設定できます。動画ファイルも元ファイルにできます(音声を抽出します)。MP3、FLAC、OGG (Vorbis)はアプリ内のJavaコードでエンコードされるため(Androidのエンコーダーは使いません)、長いファイルではM4A/Opusより遅くなります。注意: MP3は8000~48000 Hzと標準ビットレートのみ対応です(値は最も近いものに丸められます)。FLACは可逆圧縮です(ビットレートは使いません)。flac -3よりわずかに大きくなります。OGG (Vorbis)は常に44100 Hzまたは48000 Hzのステレオになります(モノラルの元ファイルは2チャンネルに複製されます)。RPGM MV/MZやRen'PyでループBGMを使う場合は、ループタグが書き込まれるOGG (Vorbis)を選んでください。\n• 画像: PNG、JPG、WebPに対応し、「解像度」(「元のまま」、1%~100%のスライダーの「パーセント」、または「サイズ」で幅×高さをpx指定し「元のアスペクト比を保持」)、回転、透過画像用のJPG背景色を設定できます。\n• 動画: MP4 (H.264またはH.265)、WebM (VP9)に対応し、解像度、ビットレート(ドロップダウン。初期値は「自動」)、フレームレート、音声を設定できます。長時間かかることがあるので、アプリを開いたままにしてください。\n• 混合アセット: フォルダモード専用で、音声・画像・動画が一緒に入ったフォルダ向けです。ファイルは拡張子ごとにグループ化され、種類ごとに独自のルールセクション(出力形式、圧縮、詳細設定)があり、タイトルをタップして開きます。右側のスイッチでその種類を処理するかどうかを決めます(フォルダにない種類は自動的に無効になります)。音声と画像が先に処理され、動画は最後です。「認識できないファイルをそのままコピー」オプションを使うと、その他のファイルを出力フォルダにコピーします。テキストと特殊は含まれません。\n• 完了時に、元より大きくなった結果がある場合は「大きくなった結果を破棄」ボタンが表示されます(確認あり)。実行中の「キャンセル」ボタンも確認が表示されます。\n• 特殊アセット: Circusの.pcm (XPCM)音声をOGG/WAVに、Circusの.crx画像をPNGに変換します(差分CRXはベース画像と合成されます)。各コンバーターにはエンジン別のプリセット(RPGM、Ren'Py、Kirikiri)もあります。\n• テキストエンコード: Shift-JIS/GBK/Big5/EUC-KR/EUC-JPからUTF-8へ、またはその逆に変換します。BOM、改行コード、無効な文字のオプションがあります。フォルダモードでは入力した拡張子のみ処理します。\n• 入力形式と出力形式のオプションはA~Z順に並びます(「すべての対応形式」と「元の形式に合わせる」は先頭に固定)。アプリ内の長い説明は「詳細を見る (?)」リンクにまとめられており、タップで開き、どこかをタップすると閉じます。\n• 変換後、「互換性チェック」で結果を対象エンジンのルール(形式、ファイル名、画像サイズ、テキストエンコード)と照合できます。「元ファイルを上書き」モードでは、「このフォルダを再パック」ボタンからすぐに「パック」へ移動できます。\n• 結果ファイルが既に存在する場合は、「名前を変更」「上書き」「スキップ」から選びます。結果は出力フォルダに書き込まれ、元ファイルが変更されることはありません。 | 转换游戏资源格式。该菜单位于“辅助工具”中，在“文件辅助工具”和“密钥查找器”之间。\n• 每个转换器顶部都有“文件 / 文件夹”切换。文件可以多选；文件夹模式会处理所有匹配的文件（选择“包含子文件夹”可递归处理，目录结构会保留在输出文件夹中）。输出格式默认为“跟随源格式”（与每个源文件的格式相同）；如果源格式无法作为输出格式（例如 MP3），会显示红色文字，此时必须手动选择格式。\n• 压缩（音频、图片、视频）：选择“质量”（1%–100% 滑块，100% = 不压缩；按质量进行压缩）或“大小”（以 KB 为单位的预估结果大小；以该大小为目标，且不能超过源文件大小）。PNG 通过减少颜色数量来压缩。如果输出格式相同，则直接压缩；否则先转换再压缩。文件夹模式有“仅当大于 X KB 时才压缩”选项。预估结果大小显示在“开始转换”按钮上方；进度（百分比和剩余时间）和结果（源文件与结果大小对比）显示在按钮下方。文件夹模式会以源文件夹的名称导出到输出文件夹下（源文件夹“aset”+ 输出文件夹“hasil”会得到“hasil/aset/...”）。\n• 点击标题可展开/折叠设置部分（与问答菜单相同）。“设置预设”可保存并应用转换器的所有设置（包括内置预设和“上次使用的设置”）。\n• 文件夹模式：可选择要处理的源扩展名（“源扩展名”部分）；“创建样本并与源文件对比”（音频和图片）会先转换一个文件，并显示转换前后的对比（音频可播放）；“替换原文件”会在覆盖之前先把原文件备份到 _backup 文件夹。\n• 完成后，“结果文件”会列出每个文件，并带有“撤销”按钮（删除结果，或从 _backup 恢复原文件），还有按扩展名的汇总和完成通知。\n• 音频：“音量标准化”（使响度均衡）、“裁剪开头/结尾的静音”以及“保留 OGG 循环标签”（LOOPSTART/LOOPLENGTH；会同时写入 OGG Vorbis 和 OGG Opus）。图片：PNG 会被压缩为调色板 PNG（可选抖动），“取整为倍数”可使图块集尺寸与网格对齐。\n• 音频：M4A (AAC)、OGG (Opus，Android 10 及以上)、WAV、MP3、FLAC 或 OGG (Vorbis)；可设置比特率、采样率、单声道/立体声和音量（1%–200% 滑块）。视频文件也可以作为源文件（提取其中的音频）。MP3、FLAC 和 OGG (Vorbis) 由应用内的 Java 代码编码（不使用 Android 的编码器），因此处理长文件时比 M4A/Opus 慢。注意：MP3 仅支持 8000–48000 Hz 和标准比特率（数值会取最接近的值）；FLAC 是无损的（不使用比特率），体积比 flac -3 略大；OGG (Vorbis) 始终使用 44100 或 48000 Hz 和立体声（单声道源会复制为双声道）。在 RPGM MV/MZ 和 Ren'Py 中使用循环 BGM 时，请选择 OGG (Vorbis)，因为它会写入循环标签。\n• 图片：PNG、JPG 或 WebP；可设置“分辨率”（“原始大小”、带 1%–100% 滑块的“百分比”，或以 px 为单位的“尺寸”宽 × 高并“保持源宽高比”）、旋转，以及透明图片的 JPG 背景色。\n• 视频：MP4 (H.264 或 H.265) 或 WebM (VP9)；可设置分辨率、比特率（下拉菜单，默认“自动”）、帧率和音频。可能需要很长时间，请保持应用打开。\n• 混合资源：仅限文件夹模式，适用于同时包含音频、图片和视频的文件夹。文件按扩展名分组；每种类型都有自己的规则部分（输出格式、压缩、高级设置），点击其标题即可展开，右侧的开关决定是否处理该类型（文件夹中没有的类型会自动禁用）。先处理音频和图片，最后处理视频。“原样复制无法识别的文件”选项会把其他文件复制到输出文件夹。不包含文本和特殊类型。\n• 完成后，如果某些结果比源文件更大，会出现“丢弃更大的结果”按钮（需确认）；运行期间点击“取消”按钮同样需要确认。\n• 特殊资源：将 Circus 的 .pcm (XPCM) 音频转换为 OGG/WAV，将 Circus 的 .crx 图片转换为 PNG（差分 CRX 会与其基础图片合并）。每个转换器还提供各引擎的预设（RPGM、Ren'Py、Kirikiri）。\n• 文本编码：在 Shift-JIS/GBK/Big5/EUC-KR/EUC-JP 与 UTF-8 之间互相转换，可设置 BOM、换行符和无效字符选项。文件夹模式只处理您输入的扩展名。\n• 输入格式和输出格式选项按 A–Z 排序（“所有可用格式”和“跟随源格式”始终置顶）。应用中的长说明都收在“查看详情 (?)”链接里；点击打开，点击任意位置关闭。\n• 转换后，“检查兼容性”会将结果与目标引擎的规则（格式、文件名、图片尺寸、文本编码）进行对比。在“覆盖原文件”模式下，“重新打包此文件夹”按钮可直接跳转到“打包”。\n• 如果结果文件已存在：重命名、覆盖或跳过。结果会写入输出文件夹；源文件不会被更改。 | 게임 에셋 형식을 변환합니다. 메뉴는 지원 도구에서 파일 도우미 도구와 키 파인더 사이에 있습니다.\n• 모든 변환기 상단에 "파일 / 폴더" 전환이 있습니다. 파일은 여러 개를 선택할 수 있으며, 폴더는 일치하는 모든 파일을 처리합니다("하위 폴더 포함"을 선택하면 하위까지 처리하며 구조는 출력 폴더에 그대로 유지됩니다). 출력 형식의 기본값은 "원본 형식 따르기"(각 원본 파일과 같은 형식)입니다. 원본 형식을 출력 형식으로 쓸 수 없는 경우(예: MP3) 빨간 글씨가 나타나며 형식을 직접 선택해야 합니다.\n• 압축(오디오, 이미지, 비디오): "품질"(1%~100% 슬라이더, 100% = 압축 없음, 품질을 기준으로 압축) 또는 "크기"(KB 단위의 예상 결과 크기, 크기를 목표로 압축하며 원본 파일 크기를 넘을 수 없음)를 선택합니다. PNG는 색상 수를 줄여 압축합니다. 출력 형식이 같으면 바로 압축하고, 다르면 먼저 변환한 뒤 압축합니다. 폴더 모드에는 "X KB보다 클 때만 압축" 옵션이 있습니다. 예상 결과 크기는 "변환 시작" 버튼 위에, 진행률(퍼센트와 남은 시간)과 결과(원본과 결과 크기 비교)는 버튼 아래에 표시됩니다. 폴더 모드는 원본 폴더 이름으로 출력 폴더 아래에 내보냅니다(원본 "aset" + 출력 "hasil"이면 "hasil/aset/..."이 됩니다).\n• 설정 섹션은 제목을 탭하여 펼치거나 접을 수 있습니다(Q&A 메뉴와 같습니다). "설정 프리셋"은 변환기의 모든 설정을 저장하고 적용합니다(기본 제공 프리셋과 "마지막으로 사용한 설정" 포함).\n• 폴더 모드: 처리할 원본 확장자를 선택할 수 있습니다("원본 확장자" 섹션). "샘플을 만들어 원본과 비교"(오디오와 이미지)는 먼저 파일 하나를 변환하여 변환 전후 비교를 보여줍니다(오디오는 재생할 수 있습니다). "원본 파일 교체"는 덮어쓰기 전에 원본을 _backup 폴더에 백업합니다.\n• 완료되면 "결과 파일"에 모든 파일이 "되돌리기" 버튼과 함께 나열됩니다(결과를 삭제하거나 _backup에서 원본을 복원). 확장자별 요약과 완료 알림도 제공됩니다.\n• 오디오: "볼륨 정규화"(음량을 고르게 맞춤), "시작/끝 무음 자르기", "OGG 루프 태그 유지"(LOOPSTART/LOOPLENGTH, OGG Vorbis와 OGG Opus 모두에 기록됨). 이미지: PNG는 팔레트 PNG로 압축되며(디더링 옵션 있음), "배수로 반올림"은 타일셋 크기를 그리드에 맞춥니다.\n• 오디오: M4A (AAC), OGG (Opus, Android 10 이상), WAV, MP3, FLAC 또는 OGG (Vorbis)를 지원하며 비트레이트, 샘플 레이트, 모노/스테레오, 볼륨(1%~200% 슬라이더)을 설정할 수 있습니다. 비디오 파일도 원본으로 사용할 수 있습니다(오디오를 추출). MP3, FLAC, OGG (Vorbis)는 앱 내부의 Java 코드로 인코딩되므로(Android 인코더를 사용하지 않음) 긴 파일에서는 M4A/Opus보다 느립니다. 참고: MP3는 8000~48000 Hz와 표준 비트레이트만 지원합니다(값은 가장 가까운 값으로 반올림됩니다). FLAC은 무손실이며(비트레이트는 사용되지 않음) flac -3보다 약간 큽니다. OGG (Vorbis)는 항상 44100 또는 48000 Hz와 스테레오를 사용합니다(모노 원본은 두 채널로 복제됨). RPGM MV/MZ와 Ren'Py에서 반복 BGM을 쓸 때는 루프 태그가 기록되는 OGG (Vorbis)를 선택하세요.\n• 이미지: PNG, JPG 또는 WebP를 지원하며 "해상도"("원본", 1%~100% 슬라이더의 "퍼센트", 또는 px 단위의 "크기" 가로 × 세로와 "원본 가로세로 비율 유지"), 회전, 투명 이미지용 JPG 배경을 설정할 수 있습니다.\n• 비디오: MP4 (H.264 또는 H.265) 또는 WebM (VP9)을 지원하며 해상도, 비트레이트(드롭다운, 기본값 "자동"), 프레임 레이트, 오디오를 설정할 수 있습니다. 시간이 오래 걸릴 수 있으니 앱을 열어 두세요.\n• 혼합 에셋: 폴더 모드 전용이며 오디오, 이미지, 비디오가 함께 들어 있는 폴더용입니다. 파일은 확장자별로 그룹화되며 각 유형에는 자체 규칙 섹션(출력 형식, 압축, 고급 설정)이 있어 제목을 탭하여 엽니다. 오른쪽 스위치로 해당 유형을 처리할지 결정합니다(폴더에 없는 유형은 자동으로 비활성화됨). 오디오와 이미지를 먼저 처리하고 비디오는 마지막에 처리합니다. "인식되지 않는 파일을 그대로 복사" 옵션은 다른 파일을 출력 폴더로 복사합니다. 텍스트와 특수 에셋은 포함되지 않습니다.\n• 완료되었을 때 일부 결과가 원본보다 크면 "더 큰 결과 버리기" 버튼이 나타납니다(확인 절차 있음). 실행 중 "취소" 버튼도 확인을 요청합니다.\n• 특수 에셋: Circus .pcm (XPCM) 오디오를 OGG/WAV로, Circus .crx 이미지를 PNG로 변환합니다(차분 CRX는 기본 이미지와 합쳐집니다). 각 변환기에는 엔진별 프리셋(RPGM, Ren'Py, Kirikiri)도 있습니다.\n• 텍스트 인코딩: Shift-JIS/GBK/Big5/EUC-KR/EUC-JP를 UTF-8로 또는 그 반대로 변환하며 BOM, 줄 바꿈, 잘못된 문자 옵션이 있습니다. 폴더 모드는 입력한 확장자만 처리합니다.\n• 입력 형식과 출력 형식 옵션은 A~Z 순으로 정렬됩니다("사용 가능한 모든 형식"과 "원본 형식 따르기"는 맨 위에 유지). 앱 전반의 긴 설명은 "자세히 보기 (?)" 링크에 담겨 있으며, 탭하면 열리고 아무 곳이나 탭하면 닫힙니다.\n• 변환 후 "호환성 확인"은 결과를 대상 엔진의 규칙(형식, 파일 이름, 이미지 크기, 텍스트 인코딩)과 비교합니다. "원본 덮어쓰기" 모드에서는 "이 폴더 다시 묶기" 버튼으로 바로 묶기로 이동할 수 있습니다.\n• 결과 파일이 이미 있는 경우: 이름 바꾸기, 덮어쓰기 또는 건너뛰기. 결과는 출력 폴더에 기록되며 원본 파일은 절대 변경되지 않습니다.
Support Tools > File Toolkit > File Cutter | サポートツール > ファイルツールキット > ファイルカッター | 辅助工具 > 文件工具箱 > 文件切割器 | 지원 도구 > 파일 툴킷 > 파일 커터
Support Tools > File Toolkit > Batch Rename | サポートツール > ファイルツールキット > 一括名前変更 | 辅助工具 > 文件工具箱 > 批量重命名 | 지원 도구 > 파일 툴킷 > 일괄 이름 바꾸기
VN Tools > Engine List | VNツール > エンジン一覧 | VN 工具 > 引擎列表 | VN 도구 > 엔진 목록
RPGM Tools > Cheat / Save | RPGMツール > チート / セーブ | RPGM 工具 > 修改器 / 存档 | RPGM 도구 > 치트 / 세이브
File & Folder Inspector (main menu) | ファイル・フォルダー検査(メインメニュー) | 文件与文件夹检查器（主菜单） | 파일 및 폴더 검사기(메인 메뉴)
File & Folder Inspector > Compare Files | ファイル・フォルダー検査 > ファイル比較 | 文件与文件夹检查器 > 文件比较 | 파일 및 폴더 검사기 > 파일 비교
Tips | ヒント | 小贴士 | 팁
How does the app recognise an archive format? | アプリはどのようにアーカイブの形式を判別しますか? | 应用如何识别压缩包格式？ | 앱은 아카이브 형식을 어떻게 인식하나요?
What is the XOR key in archive options? | アーカイブオプションのXORキーとは何ですか? | 压缩包选项中的 XOR 密钥是什么？ | 아카이브 옵션의 XOR 키란 무엇인가요?
How do I translate a visual novel? | ビジュアルノベルを翻訳するにはどうすればいいですか? | 如何翻译视觉小说？ | 비주얼 노벨은 어떻게 번역하나요?
How do I make sure my downloaded archive is not corrupt? | ダウンロードしたアーカイブが壊れていないことを確認するにはどうすればいいですか? | 如何确保我下载的压缩包没有损坏？ | 내려받은 아카이브가 손상되지 않았는지 어떻게 확인하나요?
Does Folder Inspection modify or move files? | フォルダ検査はファイルを変更または移動しますか? | 文件夹检查会修改或移动文件吗？ | 폴더 검사는 파일을 수정하거나 이동하나요?
Fast mode only compares file names and sizes inside the archive, so different content of the same size is not detected. Use Thorough mode to compare content too. | 高速モードはアーカイブ内のファイル名とサイズのみを比較するため、サイズが同じで内容が異なる場合は検出されません。内容も比較するには、詳細モードを使ってください。 | 快速模式只比较压缩包内的文件名和大小，因此大小相同但内容不同的文件不会被检测出来。如需同时比较内容，请使用详细模式。 | 빠른 모드는 아카이브 내부의 파일 이름과 크기만 비교하므로, 크기가 같고 내용이 다른 경우는 감지되지 않습니다. 내용까지 비교하려면 정밀 모드를 사용하세요.
No, unless you deliberately choose an output folder equal to the source (e.g. Import Text into the data folder). The app works on copies in the output folder. | いいえ。ただし、出力フォルダを元と同じフォルダに意図的に指定した場合(例: データフォルダへのテキストのインポート)は除きます。アプリは出力フォルダ内のコピーに対して処理を行います。 | 不会，除非您特意把输出文件夹设为与源文件夹相同（例如将文本导入到 data 文件夹）。应用只会处理输出文件夹中的副本。 | 아니요. 다만 출력 폴더를 의도적으로 원본과 같은 폴더로 지정한 경우(예: 텍스트를 data 폴더로 가져오기)는 예외입니다. 앱은 출력 폴더에 있는 복사본으로 작업합니다.
If it says a key is needed, fill the Key option. Pack Archive needs the _FuruArc_Siglus.meta created when unpacking; unpack the original Scene.pck first and edit the resulting folder (do not delete the .meta file). | キーが必要と表示された場合は、キーオプションに入力してください。アーカイブのパックには、展開時に作成された_FuruArc_Siglus.metaが必要です。まず元のScene.pckを展開し、できたフォルダを編集してください(.metaファイルは削除しないでください)。 | 如果提示需要密钥，请填写“密钥”选项。打包压缩包需要解包时生成的 _FuruArc_Siglus.meta；请先解包原始的 Scene.pck，再编辑生成的文件夹（不要删除 .meta 文件）。 | 키가 필요하다고 나오면 키 옵션에 입력하세요. 아카이브 묶기에는 풀 때 만들어진 _FuruArc_Siglus.meta가 필요합니다. 먼저 원본 Scene.pck를 풀고 생성된 폴더를 편집하세요(.meta 파일은 삭제하지 마세요).
Contact the developer via Settings tab > About (email or Github). | 設定タブ > アプリについて(EメールまたはGithub)から開発者に連絡してください。 | 请通过“设置”标签页 > 关于（电子邮件或 Github）联系开发者。 | 설정 탭 > 정보(이메일 또는 Github)에서 개발자에게 문의하세요.
Run RPGM Tools > Plugin Manager > Plugin Checker: look for enabled plugins whose file is missing, duplicate names, and @base/order warnings. Restore from the .bak backup in the output folder, or compare with the original plugins.js using the plugins.js Comparer. After editing plugin texts, make sure quotes and special characters are still valid. | RPGMツール > プラグインマネージャー > プラグインチェッカーを実行してください。ファイルが見つからない有効なプラグイン、重複した名前、@base/順序の警告を確認します。出力フォルダの.bakバックアップから復元するか、plugins.js比較ツールで元のplugins.jsと比較してください。プラグインのテキストを編集した後は、引用符や特殊文字が正しいままか確認してください。 | 运行 RPGM 工具 > 插件管理器 > 插件检查器：查看已启用但文件缺失的插件、重复的名称，以及 @base/顺序警告。可以从输出文件夹中的 .bak 备份恢复，或用 plugins.js 对比器与原始 plugins.js 对比。编辑插件文本后，请确认引号和特殊字符仍然有效。 | RPGM 도구 > 플러그인 관리자 > 플러그인 검사기를 실행하세요. 파일이 없는 활성화된 플러그인, 중복된 이름, @base/순서 경고를 확인합니다. 출력 폴더의 .bak 백업에서 복원하거나, plugins.js 비교 도구로 원본 plugins.js와 비교하세요. 플러그인 텍스트를 편집한 뒤에는 따옴표와 특수 문자가 여전히 올바른지 확인하세요.
The XOR key may be wrong. Enter the key (hex) manually in the archive options, or use Check Integrity to see which files are affected. | XORキーが間違っている可能性があります。アーカイブオプションでキー(16進数)を手動入力するか、「整合性チェック」でどのファイルが影響を受けているか確認してください。 | XOR 密钥可能不正确。请在压缩包选项中手动输入密钥（十六进制），或使用“完整性检查”查看哪些文件受到影响。 | XOR 키가 잘못되었을 수 있습니다. 아카이브 옵션에 키(16진수)를 직접 입력하거나, "무결성 검사"로 어떤 파일이 영향을 받는지 확인하세요.
Run Translation Scan: look at the Control codes differ and Lines too long categories, then fix them in the Translation Editor (auto wrap). | 翻訳スキャンを実行し、「制御コードが異なる」と「行が長すぎる」の分類を確認して、翻訳エディター(自動折り返し)で修正してください。 | 运行翻译扫描：查看“控制码不一致”和“行过长”分类，然后在翻译编辑器中修复（自动换行）。 | 번역 스캔을 실행하여 "제어 코드가 다름"과 "줄이 너무 김" 분류를 확인한 뒤 번역 편집기(자동 줄 바꿈)에서 수정하세요.
Extracted output can be several times larger than the archive. Free some space, or use Select Files so only the needed files are extracted. | 展開された出力は、アーカイブの数倍の大きさになることがあります。空き容量を確保するか、「ファイルを選択」で必要なファイルだけを展開してください。 | 解包后的输出可能比压缩包大数倍。请腾出一些空间，或使用“选择文件”，只解包需要的文件。 | 풀린 출력은 아카이브보다 몇 배 클 수 있습니다. 공간을 확보하거나 "파일 선택"으로 필요한 파일만 푸세요.
Restart the app and repeat the steps. If it still happens, send it via About > Report a Problem with the log attached. | アプリを再起動して、手順をやり直してください。それでも解決しない場合は、アプリについて > 問題を報告からログを添付して送信してください。 | 重启应用并重复这些步骤。如果仍然出现，请通过 关于 > 报告问题 发送，并附上日志。 | 앱을 다시 시작하고 단계를 반복하세요. 그래도 문제가 계속되면 정보 > 문제 신고를 통해 로그를 첨부하여 보내 주세요.
VN Tools > Pack Archive | VNツール > アーカイブのパック | VN 工具 > 打包压缩包 | VN 도구 > 아카이브 묶기
"""
    const val T8 = """
Finds the game encryption key.\n• Open Key Finder on the main menu (or the Key Finder item in the Siglus Engine, Kirikiri, CatSystem 2, EntisGLS and RPGM MV/MZ engine menus).\n• Pick the game folder, then press Analyze.\n• Result: the key, the file it was found in, its status (Verified or Candidate) and the list of files encrypted with that key (50 per page). Press Copy key and paste it into the Key option of Unpack/Pack Archive.\n• Supported: Siglus Engine (recovered from Scene.pck), RPGM MV/MZ (System.json or PNG header), Kirikiri XP3 (1-byte XOR key). Keys that are obfuscated or assembled at run time cannot be found unless the data itself gives a hint.\n• A .txt report export is at the bottom of the results. | ゲームの暗号化キーを探します。\n• メインメニューの「キーファインダー」を開きます(またはSiglus Engine、Kirikiri、CatSystem 2、EntisGLS、RPGM MV/MZの各エンジンメニューにある「キーファインダー」項目)。\n• ゲームフォルダを選び、「解析」を押します。\n• 結果: キー、見つかったファイル、ステータス(検証済みまたは候補)、そのキーで暗号化されたファイルの一覧(1ページ50件)が表示されます。「キーをコピー」を押して、アーカイブの展開/パックの「キー」オプションに貼り付けます。\n• 対応: Siglus Engine(Scene.pckから復元)、RPGM MV/MZ(System.jsonまたはPNGヘッダー)、Kirikiri XP3(1バイトのXORキー)。難読化されたキーや実行時に組み立てられるキーは、データ自体にヒントがない限り見つけられません。\n• 結果の下部から.txtレポートをエクスポートできます。 | 查找游戏的加密密钥。\n• 在主菜单中打开“密钥查找器”（或 Siglus Engine、Kirikiri、CatSystem 2、EntisGLS 和 RPGM MV/MZ 引擎菜单中的“密钥查找器”项）。\n• 选择游戏文件夹，然后点击“分析”。\n• 结果：密钥、找到该密钥的文件、状态（已验证或候选）以及使用该密钥加密的文件列表（每页 50 个）。点击“复制密钥”，并粘贴到解包/打包压缩包的“密钥”选项中。\n• 支持：Siglus Engine（从 Scene.pck 中恢复）、RPGM MV/MZ（System.json 或 PNG 文件头）、Kirikiri XP3（1 字节 XOR 密钥）。经过混淆或在运行时才拼装的密钥无法找到，除非数据本身有线索。\n• 结果底部可导出 .txt 报告。 | 게임의 암호화 키를 찾습니다.\n• 메인 메뉴에서 "키 파인더"를 엽니다(또는 Siglus Engine, Kirikiri, CatSystem 2, EntisGLS, RPGM MV/MZ 엔진 메뉴의 "키 파인더" 항목).\n• 게임 폴더를 선택한 다음 "분석"을 누릅니다.\n• 결과: 키, 키가 발견된 파일, 상태(검증됨 또는 후보), 그리고 해당 키로 암호화된 파일 목록(페이지당 50개)이 표시됩니다. "키 복사"를 누른 뒤 아카이브 풀기/묶기의 "키" 옵션에 붙여넣으세요.\n• 지원: Siglus Engine(Scene.pck에서 복구), RPGM MV/MZ(System.json 또는 PNG 헤더), Kirikiri XP3(1바이트 XOR 키). 난독화되었거나 실행 중에 조합되는 키는 데이터 자체에 단서가 없으면 찾을 수 없습니다.\n• 결과 하단에서 .txt 보고서를 내보낼 수 있습니다.
Takes part of a large file as a small file, e.g. to send as a sample.\n• Pick the source file, then the part to take: Start, Middle (from the offset you enter) or End.\n• Enter the amount to take and its unit (B, KB or MB). In Middle mode the start offset uses the same unit.\n• The result is saved to the Output Folder (follows the Default Output Folder if set). The source file is not changed. | 大きなファイルの一部を小さなファイルとして取り出します(サンプルとして送る場合など)。\n• 元のファイルを選び、取り出す部分を選びます: 先頭、中間(入力したオフセットから)、または末尾。\n• 取り出す量と単位(B、KB、MB)を入力します。中間モードでは、開始オフセットも同じ単位です。\n• 結果は出力フォルダに保存されます(設定されている場合は「デフォルトの出力フォルダ」に従います)。元のファイルは変更されません。 | 从大文件中截取一部分作为小文件，例如用来发送样本。\n• 选择源文件，然后选择要截取的部分：开头、中间（从您输入的偏移量开始）或结尾。\n• 输入要截取的大小及其单位（B、KB 或 MB）。在“中间”模式下，起始偏移量使用相同的单位。\n• 结果会保存到输出文件夹（如果设置了“默认输出文件夹”，则跟随该设置）。源文件不会被更改。 | 큰 파일의 일부를 작은 파일로 가져옵니다(예: 샘플로 보낼 때).\n• 원본 파일을 선택한 다음 가져올 부분을 선택합니다: 시작, 중간(입력한 오프셋부터) 또는 끝.\n• 가져올 양과 단위(B, KB 또는 MB)를 입력합니다. 중간 모드에서는 시작 오프셋도 같은 단위를 사용합니다.\n• 결과는 출력 폴더에 저장됩니다("기본 출력 폴더"가 설정되어 있으면 그 설정을 따릅니다). 원본 파일은 변경되지 않습니다.
Renames many files in one folder at once.\n• Pick the folder, choose this folder only or including subfolders, then (optionally) enter the extensions to process, e.g. png, jpg.\n• Choose a rule: Find & Replace (on the name without extension), Number Pattern (a pattern with {nama} and {no}; start number, digit count, numbering restarts in each folder), Prefix/Suffix, or Fix Garbled Names. Letters can be made lowercase or uppercase. The file extension is always kept.\n• Fix Garbled Names repairs file names garbled (e.g. ƒXƒNƒŠƒvƒg) by a wrong encoding when extracting archives: pick the encoding the names were wrongly read as and the encoding they should be (Shift-JIS, GBK, Big5, EUC-KR, UTF-8). Folder names are not repaired.\n• The preview shows old and new names, 50 files per page (use ‹ ›), with a search box that filters the view by old name, new name or folder. Names that clash or are invalid are marked red and the Rename button stays disabled until fixed.\n• After applying, the Undo button restores the previous names. | 1つのフォルダ内の多数のファイルの名前を一括で変更します。\n• フォルダを選び、このフォルダのみかサブフォルダを含めるかを選んで、(任意で)処理する拡張子(例: png, jpg)を入力します。\n• ルールを選びます: 検索と置換(拡張子を除いた名前が対象)、連番パターン({nama}と{no}を使ったパターン。開始番号、桁数。番号はフォルダごとに振り直されます)、接頭辞/接尾辞、または文字化けした名前の修復。英字は小文字または大文字にできます。ファイルの拡張子は常に保持されます。\n• 「文字化けした名前の修復」は、アーカイブ展開時に誤ったエンコードで文字化けしたファイル名(例: ƒXƒNƒŠƒvƒg)を修復します。名前が誤って読み取られたエンコードと、本来のエンコード(Shift-JIS、GBK、Big5、EUC-KR、UTF-8)を選びます。フォルダ名は修復されません。\n• プレビューには変更前と変更後の名前が1ページ50件で表示され(‹ › で移動)、検索ボックスで変更前の名前、変更後の名前、フォルダのいずれかで絞り込めます。重複した名前や無効な名前は赤で表示され、修正するまで「名前を変更」ボタンは無効のままです。\n• 適用後は、「元に戻す」ボタンで以前の名前に戻せます。 | 一次性批量重命名同一文件夹中的多个文件。\n• 选择文件夹，选择仅此文件夹或包含子文件夹，然后（可选）输入要处理的扩展名，例如 png, jpg。\n• 选择规则：查找和替换（针对不含扩展名的名称）、编号模式（使用 {nama} 和 {no} 的模式；起始编号、位数，每个文件夹重新开始编号）、前缀/后缀，或修复乱码文件名。字母可转换为小写或大写。文件扩展名始终保留。\n• “修复乱码文件名”用于修复解包时因编码错误而出现乱码的文件名（例如 ƒXƒNƒŠƒvƒg）：选择名称被错误读取时使用的编码，以及正确的编码（Shift-JIS、GBK、Big5、EUC-KR、UTF-8）。文件夹名称不会被修复。\n• 预览会显示旧名称和新名称，每页 50 个文件（使用 ‹ ›），并带有搜索框，可按旧名称、新名称或文件夹筛选。冲突或无效的名称会标为红色，在修复之前“重命名”按钮保持禁用。\n• 应用后，可用“撤销”按钮恢复之前的名称。 | 한 폴더 안의 여러 파일 이름을 한 번에 바꿉니다.\n• 폴더를 선택하고, 이 폴더만 할지 하위 폴더를 포함할지 고른 다음, (선택 사항으로) 처리할 확장자를 입력합니다(예: png, jpg).\n• 규칙을 선택합니다: 찾아 바꾸기(확장자를 제외한 이름 기준), 번호 패턴({nama}와 {no}를 사용하는 패턴, 시작 번호, 자릿수, 번호는 폴더마다 다시 시작), 접두사/접미사, 또는 깨진 이름 고치기. 영문자는 소문자나 대문자로 바꿀 수 있습니다. 파일 확장자는 항상 유지됩니다.\n• "깨진 이름 고치기"는 아카이브를 풀 때 잘못된 인코딩으로 깨진 파일 이름(예: ƒXƒNƒŠƒvƒg)을 복구합니다. 이름이 잘못 읽힌 인코딩과 원래 인코딩(Shift-JIS, GBK, Big5, EUC-KR, UTF-8)을 선택하세요. 폴더 이름은 복구되지 않습니다.\n• 미리보기에는 이전 이름과 새 이름이 페이지당 50개 파일로 표시되며(‹ › 사용), 검색 상자로 이전 이름, 새 이름 또는 폴더를 기준으로 걸러 볼 수 있습니다. 충돌하거나 유효하지 않은 이름은 빨간색으로 표시되며, 해결할 때까지 "이름 바꾸기" 버튼은 비활성 상태로 유지됩니다.\n• 적용 후에는 "되돌리기" 버튼으로 이전 이름을 복원할 수 있습니다.
All supported engines (A–Z). Tap one for Unpack/Pack specific to that engine and its script tools. NeXAS (.pac): list/extract/pack (index Huffman+XOR, zlib) and .bin script text; LZSS/zstd variants are not supported yet.\n• Engine names: Adv32 (Circus), FVP (Favorite) and Softpal ADV System use the engine name. Escu:de and HexenHaus still use the developer name because their engine name is unknown. Circus has no other engine: older titles use Adv32, newer ones use Kirikiri or NeXAS (use that engine's menu).\n• AGE (Eushully): open an .ALF file and pick the "Index file" (SYS4INI.BIN / SYS5INI.BIN / .AAI from the same game folder) in the options; Pack rebuilds one ALF (a new SYS4INI.BIN is saved next to it). Shiina Rio: WARC 1.1 only (other versions use per-title encryption), Pack writes stored entries. Unity: UnityFS bundles (.unity3d, .bundle), LZMA/LZ4 are read; packing writes an uncompressed bundle; encrypted bundles and the objects inside (SerializedFile) are not processed. | 対応しているすべてのエンジン(A~Z)です。タップすると、そのエンジン専用の展開/パックとスクリプトツールが開きます。NeXAS (.pac): 一覧表示/展開/パック(インデックスはHuffman+XOR、zlib)と.binスクリプトテキストに対応。LZSS/zstd系は未対応です。\n• エンジン名: Adv32 (Circus)、FVP (Favorite)、Softpal ADV Systemはエンジン名を使っています。Escu:deとHexenHausは、エンジン名が不明なため開発元の名前のままです。Circusに他のエンジンはありません。古い作品はAdv32、新しい作品はKirikiriまたはNeXASを使っています(そのエンジンのメニューを使ってください)。\n• AGE (Eushully): .ALFファイルを開き、オプションで同じゲームフォルダの「インデックスファイル」(SYS4INI.BIN / SYS5INI.BIN / .AAI) を選びます。パックは1つのALFを再構築します(新しいSYS4INI.BINが隣に保存されます)。Shiina Rio: WARC 1.1のみ(他のバージョンはタイトルごとの暗号化)、パックは無圧縮エントリを書き込みます。Unity: UnityFSバンドル(.unity3d、.bundle)、LZMA/LZ4を読み取り、パックは無圧縮バンドルを作成します。暗号化バンドルと内部オブジェクト(SerializedFile)は処理しません。 | 所有支持的引擎（A–Z）。点击某个引擎可进入该引擎专用的解包/打包及其脚本工具。NeXAS (.pac)：列出/提取/打包（索引为 Huffman+XOR，zlib）以及 .bin 脚本文本；暂不支持 LZSS/zstd 变体。\n• 引擎名称：Adv32 (Circus)、FVP (Favorite) 和 Softpal ADV System 使用引擎名称。Escu:de 和 HexenHaus 因为引擎名称未知，仍使用开发商名称。Circus 没有其他引擎：较早的作品使用 Adv32，较新的使用 Kirikiri 或 NeXAS（请使用该引擎的菜单）。\n• AGE (Eushully)：打开 .ALF 文件，并在选项中选择同一游戏文件夹的“索引文件”(SYS4INI.BIN / SYS5INI.BIN / .AAI)；打包会重建一个 ALF（新的 SYS4INI.BIN 保存在旁边）。Shiina Rio：仅支持 WARC 1.1（其他版本使用按游戏区分的加密），打包会写入未压缩条目。Unity：UnityFS 包（.unity3d、.bundle），可读取 LZMA/LZ4；打包会生成未压缩的包；加密包及其内部对象（SerializedFile）不会处理。 | 지원되는 모든 엔진(A–Z)입니다. 하나를 탭하면 해당 엔진 전용 풀기/묶기와 스크립트 도구로 이동합니다. NeXAS (.pac): 목록/추출/묶기(인덱스는 Huffman+XOR, zlib) 및 .bin 스크립트 텍스트를 지원하며, LZSS/zstd 변형은 아직 지원하지 않습니다.\n• 엔진 이름: Adv32 (Circus), FVP (Favorite), Softpal ADV System은 엔진 이름을 사용합니다. Escu:de와 HexenHaus는 엔진 이름을 알 수 없어서 여전히 개발사 이름을 사용합니다. Circus에는 다른 엔진이 없습니다. 오래된 작품은 Adv32를, 최신 작품은 Kirikiri 또는 NeXAS를 사용합니다(해당 엔진의 메뉴를 사용하세요).\n• AGE (Eushully): .ALF 파일을 열고 옵션에서 같은 게임 폴더의 "인덱스 파일"(SYS4INI.BIN / SYS5INI.BIN / .AAI)을 선택하세요. 묶기는 ALF 하나를 다시 만들고 새 SYS4INI.BIN을 옆에 저장해요. Shiina Rio: WARC 1.1만 지원하며(다른 버전은 타이틀별 암호화), 묶기는 무압축 항목을 써요. Unity: UnityFS 번들(.unity3d, .bundle)이며 LZMA/LZ4를 읽고, 묶기는 무압축 번들을 만들어요. 암호화된 번들과 내부 오브젝트(SerializedFile)는 처리하지 않아요.
• Export Save turns a save file into editable JSON, Import Save turns it back.\n• Value Editor edits gold, characters, items, variables and switches directly in a save. | • 「セーブをエクスポート」はセーブファイルを編集可能なJSONにし、「セーブをインポート」で元に戻します。\n• 「値エディター」では、所持金、キャラクター、アイテム、変数、スイッチをセーブ内で直接編集できます。 | • “导出存档”可将存档文件转为可编辑的 JSON，“导入存档”可将其转回。\n• “数值编辑器”可直接在存档中编辑金币、角色、物品、变量和开关。 | • "세이브 내보내기"는 세이브 파일을 편집 가능한 JSON으로 바꾸고, "세이브 가져오기"는 다시 되돌립니다.\n• "값 편집기"에서는 골드, 캐릭터, 아이템, 변수, 스위치를 세이브에서 직접 편집할 수 있습니다.
Contains seven tools, all offline, in this order: File Inspection, Folder Inspection, Compare Files, Hex Viewer, Format Test, Duplicate File Finder and Translation Scan. At the bottom of each report, a button copies the report or exports it to a .txt file. | 7つのツールがあり、すべてオフラインで動作します。順番は、ファイル検査、フォルダー検査、ファイル比較、Hex ビューア、フォーマットテスト、重複ファイル検出、翻訳スキャンです。レポートの下部にあるボタンで、レポートをコピーするか .txt ファイルにエクスポートできます。 | 包含七个工具，全部离线运行，顺序为：文件检查、文件夹检查、文件比较、十六进制查看器、格式测试、重复文件查找和翻译扫描。报告底部有一个按钮，可复制报告或将其导出为 .txt 文件。 | 오프라인으로 동작하는 일곱 가지 도구가 다음 순서로 있습니다: 파일 검사, 폴더 검사, 파일 비교, Hex 뷰어, 포맷 테스트, 중복 파일 찾기, 번역 스캔. 보고서 하단의 버튼으로 보고서를 복사하거나 .txt 파일로 내보낼 수 있습니다.
Pick File A (old) and File B (new) of the same kind.\n• Two archives: files added, changed, removed and same, with filter and search. Fast mode compares names and sizes, Thorough mode also compares the content of same-size files (slower).\n• Two scripts/text files: number of added and removed lines (line order is ignored).\n• Two translation files: new, removed entries, changed originals and changed translations.\n• Other binary files: compared by size and hash. | 同じ種類のファイルA(古い方)とファイルB(新しい方)を選びます。\n• 2つのアーカイブ: 追加、変更、削除、同一のファイルを、フィルターと検索付きで表示します。高速モードは名前とサイズを比較し、詳細モードはサイズが同じファイルの内容も比較します(時間がかかります)。\n• 2つのスクリプト/テキストファイル: 追加された行数と削除された行数(行の順序は無視されます)。\n• 2つの翻訳ファイル: 新規、削除されたエントリ、変更された原文、変更された訳文。\n• その他のバイナリファイル: サイズとハッシュで比較します。 | 选择同类型的文件 A（旧）和文件 B（新）。\n• 两个压缩包：显示新增、已更改、已删除和相同的文件，支持筛选和搜索。快速模式比较名称和大小，详细模式还会比较大小相同的文件的内容（较慢）。\n• 两个脚本/文本文件：新增和删除的行数（忽略行的顺序）。\n• 两个翻译文件：新增、删除的条目，原文变更和译文变更。\n• 其他二进制文件：按大小和哈希值比较。 | 같은 종류의 파일 A(이전)와 파일 B(새것)를 선택합니다.\n• 아카이브 두 개: 추가, 변경, 삭제, 동일한 파일을 필터와 검색과 함께 보여줍니다. 빠른 모드는 이름과 크기를 비교하고, 정밀 모드는 크기가 같은 파일의 내용까지 비교합니다(더 느림).\n• 스크립트/텍스트 파일 두 개: 추가된 줄 수와 삭제된 줄 수(줄 순서는 무시됨).\n• 번역 파일 두 개: 새 항목, 삭제된 항목, 변경된 원문, 변경된 번역.\n• 기타 바이너리 파일: 크기와 해시로 비교합니다.
• Always keep a copy of the original archive or folder before overwriting.\n• For big archives keep the screen on or enable background processing.\n• If extracted file names look garbled, change the filename encoding (Shift-JIS, UTF-8, GBK, etc.). | • 上書きする前に、元のアーカイブまたはフォルダのコピーを必ず保管してください。\n• 大きなアーカイブでは、画面をオンのままにするか、バックグラウンド処理を有効にしてください。\n• 展開したファイル名が文字化けしている場合は、ファイル名のエンコード(Shift-JIS、UTF-8、GBKなど)を変更してください。 | • 覆盖之前，务必保留原始压缩包或文件夹的副本。\n• 处理大型压缩包时，请保持屏幕常亮或启用后台处理。\n• 如果解包出的文件名出现乱码，请更改文件名编码（Shift-JIS、UTF-8、GBK 等）。 | • 덮어쓰기 전에 항상 원본 아카이브나 폴더의 사본을 보관하세요.\n• 큰 아카이브는 화면을 켜 두거나 백그라운드 처리를 활성화하세요.\n• 풀린 파일 이름이 깨져 보이면 파일 이름 인코딩(Shift-JIS, UTF-8, GBK 등)을 변경하세요.
By the signature (magic bytes) at the start of the file, and for formats without one by checking the structure. If it is not recognised, try Engine List when you know the engine. | ファイル先頭のシグネチャ(マジックバイト)で判別し、シグネチャのない形式は構造を調べて判別します。認識されない場合は、エンジンがわかっていれば「エンジン一覧」を試してください。 | 通过文件开头的签名（魔数）识别；对于没有签名的格式，则检查其结构。如果无法识别，且您知道游戏使用的引擎，请尝试“引擎列表”。 | 파일 시작 부분의 시그니처(매직 바이트)로 인식하며, 시그니처가 없는 형식은 구조를 확인하여 인식합니다. 인식되지 않으면 엔진을 알고 있을 때 "엔진 목록"을 사용해 보세요.
Some games disguise archive contents with a single-byte XOR. The app tries to detect it; if that fails, enter the key (hex) manually. | 一部のゲームは、1バイトのXORでアーカイブの内容を偽装しています。アプリは検出を試みますが、失敗した場合はキー(16進数)を手動で入力してください。 | 有些游戏用单字节 XOR 伪装压缩包内容。应用会尝试检测；如果失败，请手动输入密钥（十六进制）。 | 일부 게임은 1바이트 XOR로 아카이브 내용을 위장합니다. 앱이 이를 감지하려고 시도하며, 실패하면 키(16진수)를 직접 입력하세요.
Unpack the script archive → Export Script Text → translate → Import Script Text → Pack Archive if the game reads from an archive (or put the files in the game folder). | スクリプトアーカイブを展開 → スクリプトテキストをエクスポート → 翻訳 → スクリプトテキストをインポート → ゲームがアーカイブから読み込む場合はアーカイブをパック(またはファイルをゲームフォルダに置きます)。 | 解包脚本压缩包 → 导出脚本文本 → 翻译 → 导入脚本文本 → 如果游戏从压缩包读取，则打包压缩包（或者将文件放入游戏文件夹）。 | 스크립트 아카이브 풀기 → 스크립트 텍스트 내보내기 → 번역 → 스크립트 텍스트 가져오기 → 게임이 아카이브에서 읽는 경우 아카이브 묶기(또는 파일을 게임 폴더에 넣으세요).
In File Inspection press Compute Checksum, then paste the hash (CRC32, MD5 or SHA-256) from the source site into the Match field. A ✓ appears when it matches. If the source gives no hash, use Check Integrity. | 「ファイル検査」で「チェックサムを計算」を押し、配布元サイトのハッシュ(CRC32、MD5、SHA-256)を「照合」欄に貼り付けます。一致すると✓が表示されます。配布元がハッシュを提供していない場合は、「整合性チェック」を使ってください。 | 在“压缩包和脚本检查”中点击“计算校验和”，然后把来源网站提供的哈希值（CRC32、MD5 或 SHA-256）粘贴到“匹配”栏。匹配时会出现 ✓。如果来源没有提供哈希值，请使用“完整性检查”。 | "파일 검사"에서 "체크섬 계산"을 누른 다음, 배포처 사이트의 해시(CRC32, MD5 또는 SHA-256)를 "일치 확인" 입력란에 붙여넣으세요. 일치하면 ✓가 나타납니다. 배포처에서 해시를 제공하지 않으면 "무결성 검사"를 사용하세요.
No. All inspections are read-only. Nothing in the folder is changed. | いいえ。すべての検査は読み取り専用で、フォルダ内のものは何も変更されません。 | 不会。所有检查都是只读的，文件夹中的任何内容都不会被更改。 | 아니요. 모든 검사는 읽기 전용이며 폴더의 어떤 것도 변경되지 않습니다.
What is in the .txt report and where can I open it? | .txtレポートには何が含まれ、どこで開けますか? | .txt 报告里有什么内容，在哪里可以打开？ | .txt 보고서에는 무엇이 들어 있고 어디서 열 수 있나요?
What is Check Integrity and why is it slow? | 整合性チェックとは何ですか?なぜ時間がかかるのですか? | 什么是完整性检查？为什么很慢？ | 무결성 검사란 무엇이며 왜 느린가요?
Why is my game not recognised or fails to unpack? | ゲームが認識されない、または展開に失敗するのはなぜですか? | 为什么我的游戏无法识别或解包失败？ | 내 게임이 인식되지 않거나 풀기에 실패하는 이유는 무엇인가요?
Unpacked content is garbage because of the key | キーのせいで展開した内容が文字化けしている | 由于密钥问题，解包出的内容是乱码 | 키 때문에 풀린 내용이 깨져 있음
"Archive truncated" or the index cannot be read | 「アーカイブが途中で切れています」と表示される、またはインデックスを読み取れない | “压缩包被截断”或无法读取索引 | "아카이브가 잘렸습니다" 또는 인덱스를 읽을 수 없음
RPGM images or audio will not open after decryption | 復号後にRPGMの画像や音声が開けない | 解密后 RPGM 图片或音频无法打开 | 복호화 후 RPGM 이미지나 오디오가 열리지 않음
The process stops when the screen turns off or the app is closed | 画面が消灯したりアプリを閉じたりすると処理が止まる | 屏幕熄灭或应用被关闭时，处理会停止 | 화면이 꺼지거나 앱을 닫으면 작업이 중단됨
Check Integrity reports corrupt files | 整合性チェックで破損したファイルが報告された | 完整性检查报告有文件损坏 | 무결성 검사에서 손상된 파일이 보고됨
{0} ignored | {0} 件を無視 | 已忽略 {0} 项 | {0}개 무시됨
Tidak bisa membuka file | ファイルを開けません | 无法打开文件 | 파일을 열 수 없습니다
Engine yang didukung | 対応エンジン | 支持的引擎 | 지원되는 엔진
Supported engine | 対応エンジン | 支持的引擎 | 지원되는 엔진
Laporan Pemeriksaan Folder | フォルダ検査レポート | 文件夹检查报告 | 폴더 검사 보고서
Folder Inspection Report | フォルダ検査レポート | 文件夹检查报告 | 폴더 검사 보고서
Isi folder | フォルダの内容 | 文件夹内容 | 폴더 내용
File terbesar:\n | 最大のファイル:\n | 最大的文件:\n | 가장 큰 파일:\n
Largest files:\n | 最大のファイル:\n | 最大的文件:\n | 가장 큰 파일:\n
Engine terdeteksi:\n | 検出されたエンジン:\n | 检测到的引擎:\n | 감지된 엔진:\n
Detected engines:\n | 検出されたエンジン:\n | 检测到的引擎:\n | 감지된 엔진:\n
Daftar file | ファイル一覧 | 文件列表 | 파일 목록
Jenis kedua file berbeda: satu arsip, satu bukan arsip. | 2つのファイルの種類が異なります。一方はアーカイブ、もう一方はアーカイブではありません。 | 两个文件的类型不同：一个是压缩包，另一个不是。 | 두 파일의 종류가 다릅니다. 하나는 아카이브이고 다른 하나는 아카이브가 아닙니다.
The two files are of different kinds: one is an archive, the other is not. | 2つのファイルの種類が異なります。一方はアーカイブ、もう一方はアーカイブではありません。 | 两个文件的类型不同：一个是压缩包，另一个不是。 | 두 파일의 종류가 다릅니다. 하나는 아카이브이고 다른 하나는 아카이브가 아닙니다.
Laporan Bandingkan Pemeriksaan | 検査比較レポート | 检查对比报告 | 검사 비교 보고서
Comparison Report | 比較レポート | 对比报告 | 비교 보고서
teliti (isi dibandingkan) | 詳細（内容を比較） | 精确（比较内容） | 정밀(내용 비교)
thorough (content compared) | 詳細（内容を比較） | 精确（比较内容） | 정밀(내용 비교)
cepat (nama dan ukuran) | 高速（名前とサイズ） | 快速（名称和大小） | 빠름(이름과 크기)
fast (name and size) | 高速（名前とサイズ） | 快速（名称和大小） | 빠름(이름과 크기)
Baris ditambah | 追加された行 | 新增的行 | 추가된 줄
Added lines | 追加された行 | 新增的行 | 추가된 줄
Baris hilang | 削除された行 | 删除的行 | 삭제된 줄
Removed lines | 削除された行 | 删除的行 | 삭제된 줄
HASIL: kedua file identik\n | 結果: 2つのファイルは同一です\n | 结果：两个文件完全相同\n | 결과: 두 파일이 동일합니다\n
RESULT: both files are identical\n | 結果: 2つのファイルは同一です\n | 结果：两个文件完全相同\n | 결과: 두 파일이 동일합니다\n
HASIL: kedua file berbeda\n | 結果: 2つのファイルは異なります\n | 结果：两个文件不同\n | 결과: 두 파일이 다릅니다\n
RESULT: the files differ\n | 結果: 2つのファイルは異なります\n | 结果：两个文件不同\n | 결과: 두 파일이 다릅니다\n
File terlalu besar untuk dipindai. | ファイルが大きすぎてスキャンできません。 | 文件过大，无法扫描。 | 파일이 너무 커서 스캔할 수 없습니다.
File too large to scan. | ファイルが大きすぎてスキャンできません。 | 文件过大，无法扫描。 | 파일이 너무 커서 스캔할 수 없습니다.
Tidak ada entri terjemahan. Pilih hasil ekspor (CSV/TXT/JSON) dari FuruArc. | 翻訳エントリがありません。FuruArcで書き出した結果（CSV/TXT/JSON）を選んでください。 | 没有翻译条目。请选择由 FuruArc 导出的结果（CSV/TXT/JSON）。 | 번역 항목이 없습니다. FuruArc에서 내보낸 결과(CSV/TXT/JSON)를 선택하세요.
No translation entries found. Pick an export (CSV/TXT/JSON) made by FuruArc. | 翻訳エントリがありません。FuruArcで書き出した結果（CSV/TXT/JSON）を選んでください。 | 没有翻译条目。请选择由 FuruArc 导出的结果（CSV/TXT/JSON）。 | 번역 항목이 없습니다. FuruArc에서 내보낸 결과(CSV/TXT/JSON)를 선택하세요.
Ekstensi .{0} biasanya arsip engine: {1}, tetapi isinya tidak cocok dengan format yang didukung (mungkin terenkripsi khusus, terpotong, atau versi format belum didukung). Coba lewat Daftar Engine. | 拡張子 .{0} は通常 {1} のアーカイブですが、内容が対応フォーマットと一致しません（特殊な暗号化、破損、または未対応バージョンの可能性があります）。エンジン一覧から試してください。 | 扩展名 .{0} 通常是 {1} 引擎的压缩包，但内容与支持的格式不符（可能是特殊加密、文件被截断或格式版本尚不支持）。请尝试通过引擎列表打开。 | 확장자 .{0}은(는) 보통 {1} 엔진의 아카이브지만, 내용이 지원되는 형식과 맞지 않습니다(특수 암호화, 잘림 또는 미지원 버전일 수 있음). 엔진 목록에서 시도해 보세요.
The .{0} extension is usually an archive of: {1}, but the content does not match a supported format (maybe specially encrypted, truncated or an unsupported version). Try Engine List. | 拡張子 .{0} は通常 {1} のアーカイブですが、内容が対応フォーマットと一致しません（特殊な暗号化、破損、または未対応バージョンの可能性があります）。エンジン一覧から試してください。 | 扩展名 .{0} 通常是 {1} 引擎的压缩包，但内容与支持的格式不符（可能是特殊加密、文件被截断或格式版本尚不支持）。请尝试通过引擎列表打开。 | 확장자 .{0}은(는) 보통 {1} 엔진의 아카이브지만, 내용이 지원되는 형식과 맞지 않습니다(특수 암호화, 잘림 또는 미지원 버전일 수 있음). 엔진 목록에서 시도해 보세요.
Tidak bisa membaca file asli | 元のファイルを読み込めません | 无法读取原始文件 | 원본 파일을 읽을 수 없습니다
Format .ast tidak dikenali (posisi {0}) | .ast 形式を認識できません（位置 {0}） | 无法识别 .ast 格式（位置 {0}） | .ast 형식을 인식할 수 없습니다(위치 {0})
Bukan skrip .ast | .ast スクリプトではありません | 不是 .ast 脚本 | .ast 스크립트가 아닙니다
Parameter MP3 tidak didukung (rate {0}, {1} kbps) | MP3 のパラメータは未対応です（レート {0}、{1} kbps） | 不支持的 MP3 参数（采样率 {0}，{1} kbps） | 지원되지 않는 MP3 매개변수입니다(레이트 {0}, {1} kbps)
Gagal menyandikan MP3 (kode {0}) | MP3 のエンコードに失敗しました（コード {0}） | MP3 编码失败（代码 {0}） | MP3 인코딩에 실패했습니다(코드 {0})
Parameter Vorbis tidak didukung (rate {0}, {1} kbps) | Vorbis のパラメータは未対応です（レート {0}、{1} kbps） | 不支持的 Vorbis 参数（采样率 {0}，{1} kbps） | 지원되지 않는 Vorbis 매개변수입니다(레이트 {0}, {1} kbps)
Gagal menyiapkan analisis Vorbis | Vorbis 解析の準備に失敗しました | 准备 Vorbis 分析失败 | Vorbis 분석 준비에 실패했습니다
FLAC hanya mendukung mono/stereo di sini | ここでは FLAC はモノラル/ステレオのみ対応しています | 此处 FLAC 仅支持单声道/立体声 | 여기서는 FLAC이 모노/스테레오만 지원합니다
Sample rate FLAC tidak valid | FLAC のサンプルレートが無効です | FLAC 采样率无效 | FLAC 샘플 레이트가 올바르지 않습니다
DSC FORMAT 1.00 | DSC FORMAT 1.00 | DSC FORMAT 1.00 | DSC FORMAT 1.00
Bukan DSC | DSC ではありません | 不是 DSC | DSC가 아닙니다
Header DSC tidak valid | DSC ヘッダーが無効です | DSC 头无效 | DSC 헤더가 올바르지 않습니다
Tabel Huffman DSC tidak valid | DSC のハフマンテーブルが無効です | DSC 霍夫曼表无效 | DSC 허프만 테이블이 올바르지 않습니다
DSC terpotong | DSC が途中で切れています | DSC 数据被截断 | DSC가 잘려 있습니다
Kode DSC tidak valid | DSC のコードが無効です | DSC 编码无效 | DSC 코드가 올바르지 않습니다
Referensi DSC tidak valid | DSC の参照が無効です | DSC 引用无效 | DSC 참조가 올바르지 않습니다
DSC melebihi ukuran | DSC がサイズを超えています | DSC 超出大小 | DSC가 크기를 초과했습니다
Bukan skrip BGI | BGI スクリプトではありません | 不是 BGI 脚本 | BGI 스크립트가 아닙니다
Kepala BGI tidak valid | BGI ヘッダーが無効です | BGI 头无效 | BGI 헤더가 올바르지 않습니다
Skrip BGI tidak terbaca | BGI スクリプトを読み取れません | 无法读取 BGI 脚本 | BGI 스크립트를 읽을 수 없습니다
Bukan skrip BGI yang dikenali | 認識できる BGI スクリプトではありません | 不是可识别的 BGI 脚本 | 인식할 수 있는 BGI 스크립트가 아닙니다
Pointer string BGI di luar file | BGI の文字列ポインタがファイルの範囲外です | BGI 字符串指针超出文件范围 | BGI 문자열 포인터가 파일 범위를 벗어났습니다
Panjang kunci Blowfish harus 4..56 byte | Blowfish のキー長は 4～56 バイトである必要があります | Blowfish 密钥长度必须为 4..56 字节 | Blowfish 키 길이는 4~56바이트여야 합니다
bukan file CatScene | CatScene ファイルではありません | 不是 CatScene 文件 | CatScene 파일이 아닙니다
zlib tidak lengkap | zlib データが不完全です | zlib 数据不完整 | zlib 데이터가 불완전합니다
data terlalu kecil | データが小さすぎます | 数据过小 | 데이터가 너무 작습니다
header tidak valid | ヘッダーが無効です | 头无效 | 헤더가 올바르지 않습니다
penanda string {0} tidak valid | 文字列 {0} のマーカーが無効です | 字符串 {0} 的标记无效 | 문자열 {0}의 표식이 올바르지 않습니다
string {0} tidak berakhir | 文字列 {0} が終了していません | 字符串 {0} 未结束 | 문자열 {0}이(가) 끝나지 않았습니다
teks "{0}…" memuat huruf yang tidak ada di encoding {1} | テキスト「{0}…」に {1} エンコーディングにない文字が含まれています | 文本“{0}…”包含 {1} 编码中不存在的字符 | 텍스트 "{0}…"에 {1} 인코딩에 없는 문자가 있습니다
Data CRX terpotong | CRX データが途中で切れています | CRX 数据被截断 | CRX 데이터가 잘려 있습니다
Bukan gambar CRX yang didukung | 対応している CRX 画像ではありません | 不是受支持的 CRX 图像 | 지원되는 CRX 이미지가 아닙니다
Ukuran gambar CRX tidak valid | CRX 画像のサイズが無効です | CRX 图像大小无效 | CRX 이미지 크기가 올바르지 않습니다
Gambar terlalu besar ({0} x {1}) | 画像が大きすぎます（{0} x {1}） | 图像过大（{0} x {1}） | 이미지가 너무 큽니다({0} x {1})
Palet CRX terpotong | CRX パレットが途中で切れています | CRX 调色板被截断 | CRX 팔레트가 잘려 있습니다
CRX ini memakai kompresi Zstd (versi baru) yang belum didukung | この CRX は未対応の Zstd 圧縮（新バージョン）を使用しています | 此 CRX 使用了尚不支持的 Zstd 压缩（新版本） | 이 CRX는 아직 지원되지 않는 Zstd 압축(새 버전)을 사용합니다
CRX rusak: baris pertama memakai filter vertikal | CRX が破損しています：最初の行が垂直フィルターを使用しています | CRX 已损坏：第一行使用了垂直滤波器 | CRX 손상: 첫 줄이 수직 필터를 사용합니다
Bukan CRXD | CRXD ではありません | 不是 CRXD | CRXD가 아닙니다
CRXD ini merujuk selisih di file lain (CRXJ), belum didukung | この CRXD は別ファイル（CRXJ）の差分を参照しており、未対応です | 此 CRXD 引用其他文件（CRXJ）中的差分，尚不支持 | 이 CRXD는 다른 파일(CRXJ)의 차분을 참조하며 아직 지원되지 않습니다
Gambar basis tidak ditemukan: {0} (pilih folder yang memuat basisnya) | ベース画像が見つかりません：{0}（ベースを含むフォルダを選んでください） | 找不到基础图像：{0}（请选择包含基础图像的文件夹） | 기본 이미지를 찾을 수 없습니다: {0}(기본 이미지가 있는 폴더를 선택하세요)
Basis dan selisih berbeda format | ベースと差分でフォーマットが異なります | 基础图像与差分格式不同 | 기본 이미지와 차분의 형식이 다릅니다
Bukan file XPCM | XPCM ファイルではありません | 不是 XPCM 文件 | XPCM 파일이 아닙니다
Ukuran Ogg di header XPCM tidak valid | XPCM ヘッダー内の Ogg サイズが無効です | XPCM 头中的 Ogg 大小无效 | XPCM 헤더의 Ogg 크기가 올바르지 않습니다
Header XPCM terpotong | XPCM ヘッダーが途中で切れています | XPCM 头被截断 | XPCM 헤더가 잘려 있습니다
PCM mentah | 生の PCM | 原始 PCM | 원시 PCM
Ogg Vorbis | Ogg Vorbis | Ogg Vorbis | Ogg Vorbis
Data Ogg terpotong | Ogg データが途中で切れています | Ogg 数据被截断 | Ogg 데이터가 잘려 있습니다
Format audio tidak ada | オーディオ形式がありません | 没有音频格式 | 오디오 형식이 없습니다
Audio terlalu besar | オーディオが大きすぎます | 音频过大 | 오디오가 너무 큽니다
Data PCM terpotong | PCM データが途中で切れています | PCM 数据被截断 | PCM 데이터가 잘려 있습니다
Mode XPCM {0} ({1}) belum didukung | XPCM モード {0}（{1}）は未対応です | 尚不支持 XPCM 模式 {0}（{1}） | XPCM 모드 {0}({1})은(는) 아직 지원되지 않습니다
Format audio XPCM bukan PCM (tag {0}) | XPCM のオーディオ形式が PCM ではありません（タグ {0}） | XPCM 音频格式不是 PCM（标签 {0}） | XPCM 오디오 형식이 PCM이 아닙니다(태그 {0})
Bukan skrip Circus .mes | Circus の .mes スクリプトではありません | 不是 Circus .mes 脚本 | Circus .mes 스크립트가 아닙니다
Tabel offset Circus tidak valid | Circus のオフセットテーブルが無効です | Circus 偏移表无效 | Circus 오프셋 테이블이 올바르지 않습니다
Teks Circus tidak berakhir NUL | Circus のテキストが NUL で終わっていません | Circus 文本未以 NUL 结尾 | Circus 텍스트가 NUL로 끝나지 않습니다
Tidak ada teks | テキストがありません | 没有文本 | 텍스트가 없습니다
RPG Maker MV/MZ | RPG Maker MV/MZ | RPG Maker MV/MZ | RPG Maker MV/MZ
Kirikiri / Kirikiroid2 | Kirikiri / Kirikiroid2 | Kirikiri / Kirikiroid2 | Kirikiri / Kirikiroid2
File kosong (0 byte). | 空のファイルです（0 バイト）。 | 文件为空（0 字节）。 | 빈 파일입니다(0바이트).
Empty file (0 bytes). | 空のファイルです（0 バイト）。 | 文件为空（0 字节）。 | 빈 파일입니다(0바이트).
Nama file berisi # atau %, yang merusak alamat file di engine berbasis web. | ファイル名に # または % が含まれており、Web ベースのエンジンでファイルのアドレスが壊れます。 | 文件名包含 # 或 %，会破坏基于 Web 的引擎中的文件地址。 | 파일 이름에 # 또는 %가 있어 웹 기반 엔진에서 파일 주소가 깨집니다.
File name contains # or %, which breaks the file address in web-based engines. | ファイル名に # または % が含まれており、Web ベースのエンジンでファイルのアドレスが壊れます。 | 文件名包含 # 或 %，会破坏基于 Web 的引擎中的文件地址。 | 파일 이름에 # 또는 %가 있어 웹 기반 엔진에서 파일 주소가 깨집니다.
Ren'Py membaca spasi di nama gambar sebagai pemisah atribut, jadi nama aslinya tidak terbaca utuh. | Ren'Py は画像名のスペースを属性の区切りとして扱うため、元の名前が完全には読み取られません。 | Ren'Py 会把图像名称中的空格当作属性分隔符，因此无法完整读取原名称。 | Ren'Py는 이미지 이름의 공백을 속성 구분자로 읽기 때문에 원래 이름이 온전히 읽히지 않습니다.
Ren'Py treats spaces in image names as attribute separators, so the full name is not read as one. | Ren'Py は画像名のスペースを属性の区切りとして扱うため、元の名前が完全には読み取られません。 | Ren'Py 会把图像名称中的空格当作属性分隔符，因此无法完整读取原名称。 | Ren'Py는 이미지 이름의 공백을 속성 구분자로 읽기 때문에 원래 이름이 온전히 읽히지 않습니다.
Nama berisi spasi/karakter non-ASCII; di Android (huruf besar-kecil dibedakan) nama harus cocok persis dengan yang dirujuk game. | 名前にスペースまたは非 ASCII 文字が含まれています。Android（大文字と小文字を区別）では、ゲームが参照する名前と完全に一致する必要があります。 | 名称包含空格或非 ASCII 字符；在 Android 上（区分大小写）名称必须与游戏引用的名称完全一致。 | 이름에 공백 또는 비 ASCII 문자가 있습니다. Android(대소문자 구분)에서는 게임이 참조하는 이름과 정확히 일치해야 합니다.
Name has spaces or non-ASCII characters; on Android (case-sensitive) it must match exactly what the game refers to. | 名前にスペースまたは非 ASCII 文字が含まれています。Android（大文字と小文字を区別）では、ゲームが参照する名前と完全に一致する必要があります。 | 名称包含空格或非 ASCII 字符；在 Android 上（区分大小写）名称必须与游戏引用的名称完全一致。 | 이름에 공백 또는 비 ASCII 문자가 있습니다. Android(대소문자 구분)에서는 게임이 참조하는 이름과 정확히 일치해야 합니다.
.{0} tidak diputar {1}; hanya .ogg dan .m4a. | .{0} は {1} で再生されません。.ogg と .m4a のみです。 | {1} 不会播放 .{0}；仅支持 .ogg 和 .m4a。 | .{0}은(는) {1}에서 재생되지 않습니다. .ogg와 .m4a만 가능합니다.
.{0} is not played by {1}; only .ogg and .m4a. | .{0} は {1} で再生されません。.ogg と .m4a のみです。 | {1} 不会播放 .{0}；仅支持 .ogg 和 .m4a。 | .{0}은(는) {1}에서 재생되지 않습니다. .ogg와 .m4a만 가능합니다.
.{0} tidak termasuk format audio resmi {1} (Opus, Ogg Vorbis, MP3, WAV, FLAC). | .{0} は {1} の公式オーディオ形式（Opus、Ogg Vorbis、MP3、WAV、FLAC）に含まれません。 | .{0} 不属于 {1} 的官方音频格式（Opus、Ogg Vorbis、MP3、WAV、FLAC）。 | .{0}은(는) {1}의 공식 오디오 형식(Opus, Ogg Vorbis, MP3, WAV, FLAC)이 아닙니다.
.{0} is not an official {1} audio format (Opus, Ogg Vorbis, MP3, WAV, FLAC). | .{0} は {1} の公式オーディオ形式（Opus、Ogg Vorbis、MP3、WAV、FLAC）に含まれません。 | .{0} 不属于 {1} 的官方音频格式（Opus、Ogg Vorbis、MP3、WAV、FLAC）。 | .{0}은(는) {1}의 공식 오디오 형식(Opus, Ogg Vorbis, MP3, WAV, FLAC)이 아닙니다.
.{0} hanya jalan bila engine/plugin-nya mendukung; yang aman .ogg atau .wav. | .{0} はエンジン/プラグインが対応している場合のみ動作します。安全なのは .ogg または .wav です。 | .{0} 仅在引擎/插件支持时才能使用；稳妥的选择是 .ogg 或 .wav。 | .{0}은(는) 엔진/플러그인이 지원할 때만 동작합니다. 안전한 형식은 .ogg 또는 .wav입니다.
.{0} works only if the engine/plugin supports it; .ogg or .wav are safe. | .{0} はエンジン/プラグインが対応している場合のみ動作します。安全なのは .ogg または .wav です。 | .{0} 仅在引擎/插件支持时才能使用；稳妥的选择是 .ogg 或 .wav。 | .{0}은(는) 엔진/플러그인이 지원할 때만 동작합니다. 안전한 형식은 .ogg 또는 .wav입니다.
.{0} kurang lazim di {1}; yang aman .ogg, .m4a, atau .mp3. | .{0} は {1} ではあまり一般的ではありません。安全なのは .ogg、.m4a、.mp3 です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 .ogg、.m4a 或 .mp3。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 .ogg, .m4a, .mp3입니다.
"""
    const val T9 = """
.{0} is uncommon in {1}; .ogg, .m4a or .mp3 are safe. | .{0} は {1} ではあまり一般的ではありません。安全なのは .ogg、.m4a、.mp3 です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 .ogg、.m4a 或 .mp3。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 .ogg, .m4a, .mp3입니다.
Pasangan .{0} tidak ada. RPG Maker memakai .ogg di Windows/Android dan .m4a di iOS/Safari; tanpa pasangan, audio bisa diam di platform lain. | 対応する .{0} がありません。RPG Maker は Windows/Android では .ogg、iOS/Safari では .m4a を使うため、ペアがないと他のプラットフォームで音が出ないことがあります。 | 缺少对应的 .{0} 文件。RPG Maker 在 Windows/Android 上使用 .ogg，在 iOS/Safari 上使用 .m4a；缺少配对文件时，其他平台可能没有声音。 | 짝이 되는 .{0} 파일이 없습니다. RPG Maker는 Windows/Android에서 .ogg, iOS/Safari에서 .m4a를 사용하므로 짝이 없으면 다른 플랫폼에서 소리가 나지 않을 수 있습니다.
No .{0} counterpart. RPG Maker uses .ogg on Windows/Android and .m4a on iOS/Safari; without it audio may be silent on other platforms. | 対応する .{0} がありません。RPG Maker は Windows/Android では .ogg、iOS/Safari では .m4a を使うため、ペアがないと他のプラットフォームで音が出ないことがあります。 | 缺少对应的 .{0} 文件。RPG Maker 在 Windows/Android 上使用 .ogg，在 iOS/Safari 上使用 .m4a；缺少配对文件时，其他平台可能没有声音。 | 짝이 되는 .{0} 파일이 없습니다. RPG Maker는 Windows/Android에서 .ogg, iOS/Safari에서 .m4a를 사용하므로 짝이 없으면 다른 플랫폼에서 소리가 나지 않을 수 있습니다.
WAV sangat besar untuk HTML5; pertimbangkan .ogg atau .m4a. | WAV は HTML5 には非常に大きいため、.ogg または .m4a をご検討ください。 | WAV 对 HTML5 来说体积很大；建议改用 .ogg 或 .m4a。 | WAV는 HTML5에 쓰기엔 매우 큽니다. .ogg 또는 .m4a를 고려하세요.
WAV is very large for HTML5; consider .ogg or .m4a. | WAV は HTML5 には非常に大きいため、.ogg または .m4a をご検討ください。 | WAV 对 HTML5 来说体积很大；建议改用 .ogg 或 .m4a。 | WAV는 HTML5에 쓰기엔 매우 큽니다. .ogg 또는 .m4a를 고려하세요.
{0} memuat gambar sebagai .png (nama dilengkapi .png oleh engine); .{1} tidak akan terbaca. | {0} は画像を .png として読み込みます（エンジンが名前に .png を付加します）。.{1} は読み込まれません。 | {0} 以 .png 方式加载图像（引擎会自动补上 .png）；.{1} 将无法读取。 | {0}은(는) 이미지를 .png로 불러옵니다(엔진이 이름에 .png를 붙임). .{1}은(는) 읽히지 않습니다.
{0} loads images as .png (the engine appends .png); .{1} will not be found. | {0} は画像を .png として読み込みます（エンジンが名前に .png を付加します）。.{1} は読み込まれません。 | {0} 以 .png 方式加载图像（引擎会自动补上 .png）；.{1} 将无法读取。 | {0}은(는) 이미지를 .png로 불러옵니다(엔진이 이름에 .png를 붙임). .{1}은(는) 읽히지 않습니다.
.{0} tidak termasuk format gambar {1} (PNG, JPG, WEBP, AVIF). | .{0} は {1} の画像形式（PNG、JPG、WEBP、AVIF）に含まれません。 | .{0} 不属于 {1} 的图像格式（PNG、JPG、WEBP、AVIF）。 | .{0}은(는) {1}의 이미지 형식(PNG, JPG, WEBP, AVIF)이 아닙니다.
.{0} is not an {1} image format (PNG, JPG, WEBP, AVIF). | .{0} は {1} の画像形式（PNG、JPG、WEBP、AVIF）に含まれません。 | .{0} 不属于 {1} 的图像格式（PNG、JPG、WEBP、AVIF）。 | .{0}은(는) {1}의 이미지 형식(PNG, JPG, WEBP, AVIF)이 아닙니다.
.{0} tidak dibaca KAG standar; yang aman PNG, JPG, BMP, TLG. | .{0} は標準の KAG では読み込まれません。安全なのは PNG、JPG、BMP、TLG です。 | 标准 KAG 不会读取 .{0}；稳妥的选择是 PNG、JPG、BMP、TLG。 | .{0}은(는) 표준 KAG에서 읽지 않습니다. 안전한 형식은 PNG, JPG, BMP, TLG입니다.
.{0} is not read by standard KAG; PNG, JPG, BMP, TLG are safe. | .{0} は標準の KAG では読み込まれません。安全なのは PNG、JPG、BMP、TLG です。 | 标准 KAG 不会读取 .{0}；稳妥的选择是 PNG、JPG、BMP、TLG。 | .{0}은(는) 표준 KAG에서 읽지 않습니다. 안전한 형식은 PNG, JPG, BMP, TLG입니다.
.{0} kurang lazim di {1}; yang aman PNG, JPG, WEBP. | .{0} は {1} ではあまり一般的ではありません。安全なのは PNG、JPG、WEBP です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 PNG、JPG、WEBP。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 PNG, JPG, WEBP입니다.
.{0} is uncommon in {1}; PNG, JPG, WEBP are safe. | .{0} は {1} ではあまり一般的ではありません。安全なのは PNG、JPG、WEBP です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 PNG、JPG、WEBP。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 PNG, JPG, WEBP입니다.
Ukuran {0}×{1} melebihi 4096 px; GPU banyak HP gagal memuat tekstur sebesar ini. | サイズ {0}×{1} が 4096 px を超えています。多くのスマートフォンの GPU はこの大きさのテクスチャを読み込めません。 | 尺寸 {0}×{1} 超过 4096 px；许多手机的 GPU 无法加载这么大的纹理。 | 크기 {0}×{1}이(가) 4096 px를 초과합니다. 많은 휴대폰 GPU가 이런 크기의 텍스처를 불러오지 못합니다.
Size {0}×{1} exceeds 4096 px; many phone GPUs fail to load textures this large. | サイズ {0}×{1} が 4096 px を超えています。多くのスマートフォンの GPU はこの大きさのテクスチャを読み込めません。 | 尺寸 {0}×{1} 超过 4096 px；许多手机的 GPU 无法加载这么大的纹理。 | 크기 {0}×{1}이(가) 4096 px를 초과합니다. 많은 휴대폰 GPU가 이런 크기의 텍스처를 불러오지 못합니다.
.{0} kurang lazim di {1}; yang aman .webm (VP8/VP9) dan .mp4 (H.264). | .{0} は {1} ではあまり一般的ではありません。安全なのは .webm（VP8/VP9）と .mp4（H.264）です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 .webm（VP8/VP9）和 .mp4（H.264）。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 .webm(VP8/VP9)과 .mp4(H.264)입니다.
.{0} is uncommon in {1}; .webm (VP8/VP9) and .mp4 (H.264) are safe. | .{0} は {1} ではあまり一般的ではありません。安全なのは .webm（VP8/VP9）と .mp4（H.264）です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 .webm（VP8/VP9）和 .mp4（H.264）。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 .webm(VP8/VP9)과 .mp4(H.264)입니다.
.{0} belum tentu diputar {1}; yang aman .webm, .ogv, .mp4. | .{0} は {1} で再生できるとは限りません。安全なのは .webm、.ogv、.mp4 です。 | {1} 不一定能播放 .{0}；稳妥的选择是 .webm、.ogv、.mp4。 | .{0}은(는) {1}에서 재생된다는 보장이 없습니다. 안전한 형식은 .webm, .ogv, .mp4입니다.
.{0} may not play in {1}; .webm, .ogv, .mp4 are safe. | .{0} は {1} で再生できるとは限りません。安全なのは .webm、.ogv、.mp4 です。 | {1} 不一定能播放 .{0}；稳妥的选择是 .webm、.ogv、.mp4。 | .{0}은(는) {1}에서 재생된다는 보장이 없습니다. 안전한 형식은 .webm, .ogv, .mp4입니다.
.{0} hanya jalan dengan plugin video tertentu; yang lazim .mpg/.wmv/.mp4. | .{0} は特定の動画プラグインでのみ動作します。一般的なのは .mpg/.wmv/.mp4 です。 | .{0} 仅在使用特定视频插件时才能使用；常见格式为 .mpg/.wmv/.mp4。 | .{0}은(는) 특정 동영상 플러그인에서만 동작합니다. 일반적인 형식은 .mpg/.wmv/.mp4입니다.
.{0} needs a specific video plugin; .mpg/.wmv/.mp4 are common. | .{0} は特定の動画プラグインでのみ動作します。一般的なのは .mpg/.wmv/.mp4 です。 | .{0} 仅在使用特定视频插件时才能使用；常见格式为 .mpg/.wmv/.mp4。 | .{0}은(는) 특정 동영상 플러그인에서만 동작합니다. 일반적인 형식은 .mpg/.wmv/.mp4입니다.
.{0} kurang lazim di {1}; yang aman .mp4 dan .webm. | .{0} は {1} ではあまり一般的ではありません。安全なのは .mp4 と .webm です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 .mp4 和 .webm。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 .mp4와 .webm입니다.
.{0} is uncommon in {1}; .mp4 and .webm are safe. | .{0} は {1} ではあまり一般的ではありません。安全なのは .mp4 と .webm です。 | .{0} 在 {1} 中不太常见；稳妥的选择是 .mp4 和 .webm。 | .{0}은(는) {1}에서 흔하지 않습니다. 안전한 형식은 .mp4와 .webm입니다.
Ukuran {0}×{1} bukan kelipatan {2}×{3} (aturan {4}); bagian gambar bisa terpotong atau bergeser. | サイズ {0}×{1} は {2}×{3} の倍数ではありません（{4} の規則）。画像の一部が切れたりずれたりすることがあります。 | 尺寸 {0}×{1} 不是 {2}×{3} 的倍数（{4} 的规则）；图像部分区域可能被裁切或错位。 | 크기 {0}×{1}이(가) {2}×{3}의 배수가 아닙니다({4} 규칙). 이미지 일부가 잘리거나 어긋날 수 있습니다.
Size {0}×{1} is not a multiple of {2}×{3} (rule for {4}); parts of the image may be cut off or shifted. | サイズ {0}×{1} は {2}×{3} の倍数ではありません（{4} の規則）。画像の一部が切れたりずれたりすることがあります。 | 尺寸 {0}×{1} 不是 {2}×{3} 的倍数（{4} 的规则）；图像部分区域可能被裁切或错位。 | 크기 {0}×{1}이(가) {2}×{3}의 배수가 아닙니다({4} 규칙). 이미지 일부가 잘리거나 어긋날 수 있습니다.
karakter tunggal | 単体キャラクター | 单个角色 | 단일 캐릭터
single character | 単体キャラクター | 单个角色 | 단일 캐릭터
lembar karakter | キャラクターシート | 角色图集 | 캐릭터 시트
character sheet | キャラクターシート | 角色图集 | 캐릭터 시트
Bukan UTF-8; {0} membaca teks sebagai UTF-8 sehingga huruf jadi rusak. | UTF-8 ではありません。{0} はテキストを UTF-8 として読むため、文字化けします。 | 不是 UTF-8；{0} 将文本按 UTF-8 读取，因此文字会出现乱码。 | UTF-8이 아닙니다. {0}은(는) 텍스트를 UTF-8로 읽기 때문에 글자가 깨집니다.
Not UTF-8; {0} reads text as UTF-8 so characters will be garbled. | UTF-8 ではありません。{0} はテキストを UTF-8 として読むため、文字化けします。 | 不是 UTF-8；{0} 将文本按 UTF-8 读取，因此文字会出现乱码。 | UTF-8이 아닙니다. {0}은(는) 텍스트를 UTF-8로 읽기 때문에 글자가 깨집니다.
UTF-16; {0} mengharapkan UTF-8. | UTF-16 です。{0} は UTF-8 を想定しています。 | UTF-16；{0} 需要 UTF-8。 | UTF-16입니다. {0}은(는) UTF-8을 기대합니다.
UTF-16; {0} expects UTF-8. | UTF-16 です。{0} は UTF-8 を想定しています。 | UTF-16；{0} 需要 UTF-8。 | UTF-16입니다. {0}은(는) UTF-8을 기대합니다.
UTF-8 dengan BOM; JSON.parse di engine web bisa gagal membaca file ini. | BOM 付き UTF-8 です。Web エンジンの JSON.parse でこのファイルを読み込めない場合があります。 | 带 BOM 的 UTF-8；Web 引擎中的 JSON.parse 可能无法读取此文件。 | BOM이 있는 UTF-8입니다. 웹 엔진의 JSON.parse가 이 파일을 읽지 못할 수 있습니다.
UTF-8 with BOM; JSON.parse in the web engine may fail to read this file. | BOM 付き UTF-8 です。Web エンジンの JSON.parse でこのファイルを読み込めない場合があります。 | 带 BOM 的 UTF-8；Web 引擎中的 JSON.parse 可能无法读取此文件。 | BOM이 있는 UTF-8입니다. 웹 엔진의 JSON.parse가 이 파일을 읽지 못할 수 있습니다.
UTF-8 dengan BOM; BOM bisa merusak tag pertama di skrip. | BOM 付き UTF-8 です。BOM がスクリプトの最初のタグを壊す場合があります。 | 带 BOM 的 UTF-8；BOM 可能破坏脚本中的第一个标签。 | BOM이 있는 UTF-8입니다. BOM이 스크립트의 첫 번째 태그를 망가뜨릴 수 있습니다.
UTF-8 with BOM; the BOM can break the first tag of the script. | BOM 付き UTF-8 です。BOM がスクリプトの最初のタグを壊す場合があります。 | 带 BOM 的 UTF-8；BOM 可能破坏脚本中的第一个标签。 | BOM이 있는 UTF-8입니다. BOM이 스크립트의 첫 번째 태그를 망가뜨릴 수 있습니다.
UTF-8 tanpa BOM; Kirikiri membaca teks tanpa BOM sebagai ANSI (Shift-JIS), sehingga huruf bisa rusak. Beri BOM UTF-8 atau pakai UTF-16. | BOM なし UTF-8 です。Kirikiri は BOM のないテキストを ANSI（Shift-JIS）として読むため、文字化けすることがあります。UTF-8 の BOM を付けるか UTF-16 を使ってください。 | 不带 BOM 的 UTF-8；Kirikiri 会把无 BOM 的文本按 ANSI（Shift-JIS）读取，文字可能出现乱码。请添加 UTF-8 BOM 或改用 UTF-16。 | BOM이 없는 UTF-8입니다. Kirikiri는 BOM 없는 텍스트를 ANSI(Shift-JIS)로 읽어 글자가 깨질 수 있습니다. UTF-8 BOM을 추가하거나 UTF-16을 사용하세요.
UTF-8 without BOM; Kirikiri reads text without BOM as ANSI (Shift-JIS) so characters can be garbled. Add a UTF-8 BOM or use UTF-16. | BOM なし UTF-8 です。Kirikiri は BOM のないテキストを ANSI（Shift-JIS）として読むため、文字化けすることがあります。UTF-8 の BOM を付けるか UTF-16 を使ってください。 | 不带 BOM 的 UTF-8；Kirikiri 会把无 BOM 的文本按 ANSI（Shift-JIS）读取，文字可能出现乱码。请添加 UTF-8 BOM 或改用 UTF-16。 | BOM이 없는 UTF-8입니다. Kirikiri는 BOM 없는 텍스트를 ANSI(Shift-JIS)로 읽어 글자가 깨질 수 있습니다. UTF-8 BOM을 추가하거나 UTF-16을 사용하세요.
FuruArc/0.92 (Android; pencarian cover) | FuruArc/0.92 (Android; カバー検索) | FuruArc/0.92 (Android; 封面搜索) | FuruArc/0.92 (Android; 커버 검색)
Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 | Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 | Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 | Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36
vndb_img=1; vndb_samesite=1 | vndb_img=1; vndb_samesite=1 | vndb_img=1; vndb_samesite=1 | vndb_img=1; vndb_samesite=1
application/json; charset=utf-8 | application/json; charset=utf-8 | application/json; charset=utf-8 | application/json; charset=utf-8
terlalu besar | 大きすぎます | 过大 | 너무 큽니다
Urutan Nemesis tidak valid | Nemesis のシーケンスが無効です | Nemesis 序列无效 | Nemesis 시퀀스가 올바르지 않습니다
Cotopha Image file | Cotopha 画像ファイル | Cotopha 图像文件 | Cotopha 이미지 파일
Bukan file CSX EntisGLS | EntisGLS の CSX ファイルではありません | 不是 EntisGLS CSX 文件 | EntisGLS CSX 파일이 아닙니다
Rekaman CSX rusak | CSX レコードが破損しています | CSX 记录已损坏 | CSX 레코드가 손상되었습니다
Jumlah string CSX tidak valid | CSX の文字列数が無効です | CSX 字符串数量无效 | CSX 문자열 수가 올바르지 않습니다
conststr rusak | conststr が破損しています | conststr 已损坏 | conststr가 손상되었습니다
CSX tanpa seksi conststr (kemungkinan CSX 1.x: belum didukung) | CSX に conststr セクションがありません（CSX 1.x の可能性があり、未対応です） | CSX 没有 conststr 段（可能是 CSX 1.x：尚不支持） | CSX에 conststr 섹션이 없습니다(CSX 1.x일 수 있으며 아직 지원되지 않습니다)
Tidak ada teks non-ASCII di conststr | conststr に非 ASCII テキストがありません | conststr 中没有非 ASCII 文本 | conststr에 비 ASCII 텍스트가 없습니다
Bukan skrip Escu:de (ESCR1_00) | Escu:de スクリプト（ESCR1_00）ではありません | 不是 Escu:de 脚本（ESCR1_00） | Escu:de 스크립트(ESCR1_00)가 아닙니다
Jumlah string tidak valid | 文字列数が無効です | 字符串数量无效 | 문자열 수가 올바르지 않습니다
Ukuran VM tidak valid | VM サイズが無効です | VM 大小无效 | VM 크기가 올바르지 않습니다
String terpotong | 文字列が途中で切れています | 字符串被截断 | 문자열이 잘려 있습니다
# FuruArc checksum (SHA-256)\n | # FuruArc チェックサム（SHA-256）\n | # FuruArc 校验和（SHA-256）\n | # FuruArc 체크섬(SHA-256)\n
yyyy-MM-dd HH:mm | yyyy-MM-dd HH:mm | yyyy-MM-dd HH:mm | yyyy-MM-dd HH:mm
STATUS: ADA MASALAH ({0} masalah, {1} peringatan) | ステータス: 問題あり（問題 {0} 件、警告 {1} 件） | 状态：存在问题（{0} 个问题，{1} 个警告） | 상태: 문제 있음(문제 {0}개, 경고 {1}개)
STATUS: PROBLEMS FOUND ({0} problems, {1} warnings) | ステータス: 問題あり（問題 {0} 件、警告 {1} 件） | 状态：存在问题（{0} 个问题，{1} 个警告） | 상태: 문제 있음(문제 {0}개, 경고 {1}개)
STATUS: ADA PERINGATAN ({0} peringatan) | ステータス: 警告あり（警告 {0} 件） | 状态：有警告（{0} 个警告） | 상태: 경고 있음(경고 {0}개)
STATUS: WARNINGS ({0} warnings) | ステータス: 警告あり（警告 {0} 件） | 状态：有警告（{0} 个警告） | 상태: 경고 있음(경고 {0}개)
STATUS: SEHAT | ステータス: 正常 | 状态：正常 | 상태: 정상
STATUS: HEALTHY | ステータス: 正常 | 状态：正常 | 상태: 정상
Laporan Pemeriksaan File | ファイル検査レポート | 文件检查报告 | 파일 검사 보고서
File Inspection Report | ファイル検査レポート | 文件检查报告 | 파일 검사 보고서
Hasil pemeriksaan | 検査結果 | 检查结果 | 검사 결과
Pemeriksaan integritas | 整合性チェック | 完整性检查 | 무결성 검사
Integrity check | 整合性チェック | 完整性检查 | 무결성 검사
Laporan Pemindaian Terjemahan | 翻訳スキャンレポート | 翻译扫描报告 | 번역 스캔 보고서
Translation Scan Report | 翻訳スキャンレポート | 翻译扫描报告 | 번역 스캔 보고서
… dan {0} lainnya\n | …ほか {0} 件\n | …以及其他 {0} 项\n | … 외 {0}개\n
… and {0} more\n | …ほか {0} 件\n | …以及其他 {0} 项\n | … 외 {0}개\n
Kode kontrol berbeda | 制御コードが異なります | 控制码不同 | 제어 코드가 다릅니다
Baris terlalu panjang | 行が長すぎます | 行过长 | 줄이 너무 깁니다
Tidak konsisten | 一貫性がありません | 不一致 | 일관성이 없습니다
Sama dengan teks asli | 原文と同じです | 与原文相同 | 원문과 같습니다
RPG Maker XP/VX/VX Ace | RPG Maker XP/VX/VX Ace | RPG Maker XP/VX/VX Ace | RPG Maker XP/VX/VX Ace
Kirikiri / KAG | Kirikiri / KAG | Kirikiri / KAG | Kirikiri / KAG
NScripter / ONScripter | NScripter / ONScripter | NScripter / ONScripter | NScripter / ONScripter
Siglus Engine | Siglus Engine | Siglus Engine | Siglus Engine
Softpal ADV System | Softpal ADV System | Softpal ADV System | Softpal ADV System
WillPlus / AdvHD | WillPlus / AdvHD | WillPlus / AdvHD | WillPlus / AdvHD
Wolf RPG | Wolf RPG | Wolf RPG | Wolf RPG
(tanpa ekstensi) | （拡張子なし） | （无扩展名） | (확장자 없음)
Izin folder hilang | フォルダの権限が失われました | 文件夹权限丢失 | 폴더 권한이 사라졌습니다
Folder tidak dapat dibuka | フォルダを開けません | 无法打开文件夹 | 폴더를 열 수 없습니다
Bukan skrip HCB | HCB スクリプトではありません | 不是 HCB 脚本 | HCB 스크립트가 아닙니다
Opcode HCB tidak dikenal: 0x%02x di 0x%x | 不明な HCB オペコード: 0x%02x（0x%x） | 未知的 HCB 操作码：0x%02x（位于 0x%x） | 알 수 없는 HCB 옵코드: 0x%02x (0x%x)
String HCB rusak di 0x%x | 0x%x の HCB 文字列が破損しています | 0x%x 处的 HCB 字符串已损坏 | 0x%x의 HCB 문자열이 손상되었습니다
Instruksi HCB melewati batas | HCB 命令が範囲を超えています | HCB 指令越界 | HCB 명령이 범위를 벗어났습니다
Alamat lompat 0x%x tidak menuju awal instruksi | ジャンプ先 0x%x が命令の先頭ではありません | 跳转地址 0x%x 未指向指令起始位置 | 점프 주소 0x%x이(가) 명령의 시작이 아닙니다
Ukuran file | ファイルサイズ | 文件大小 | 파일 크기
File size | ファイルサイズ | 文件大小 | 파일 크기
Jumlah file | ファイル数 | 文件数量 | 파일 수
File count | ファイル数 | 文件数量 | 파일 수
Jumlah folder | フォルダ数 | 文件夹数量 | 폴더 수
Folder count | フォルダ数 | 文件夹数量 | 폴더 수
Total ukuran asli | 元サイズの合計 | 原始大小合计 | 원본 크기 합계
Total original size | 元サイズの合計 | 原始大小合计 | 원본 크기 합계
Total terkompresi | 圧縮後の合計 | 压缩后合计 | 압축 후 합계
Total packed | 圧縮後の合計 | 压缩后合计 | 압축 후 합계
File terbesar | 最大のファイル | 最大的文件 | 가장 큰 파일
Largest file | 最大のファイル | 最大的文件 | 가장 큰 파일
Jenis file teratas | 上位のファイル種類 | 最常见的文件类型 | 상위 파일 종류
Top file types | 多いファイル形式 | 常见文件类型 | 많은 파일 형식
Kunci XOR | XOR鍵 | XOR 密钥 | XOR 키
tidak ada | なし | 无 | 없음
Path terpanjang | 最長パス | 最长路径 | 가장 긴 경로
Longest path | 最長パス | 最长路径 | 가장 긴 경로
Isi arsip sesuai format {0}: indeks berhasil dibaca. | アーカイブは {0} 形式に一致しています。インデックスを正常に読み込みました。 | 压缩包符合 {0} 格式：索引读取成功。 | 아카이브가 {0} 형식과 일치합니다. 인덱스를 정상적으로 읽었습니다.
Archive matches the {0} format: the index was read successfully. | アーカイブは {0} 形式に一致しています。インデックスを正常に読み込みました。 | 压缩包符合 {0} 格式：索引读取成功。 | 아카이브가 {0} 형식과 일치합니다. 인덱스를 정상적으로 읽었습니다.
Ekstensi .{0} tidak lazim untuk format ini (biasanya {1}), tapi isinya terbaca benar. | 拡張子 .{0} はこの形式では一般的ではありません (通常は {1}) が、内容は正しく読み取れました。 | 扩展名 .{0} 对此格式不常见（通常为 {1}），但内容可正常读取。 | 확장자 .{0}은(는) 이 형식에서 일반적이지 않지만 (보통 {1}) 내용은 정상적으로 읽혔습니다.
Extension .{0} is unusual for this format (usually {1}), but the content reads fine. | 拡張子 .{0} はこの形式では一般的ではありません (通常は {1}) が、内容は正しく読み取れました。 | 扩展名 .{0} 对此格式不常见（通常为 {1}），但内容可正常读取。 | 확장자 .{0}은(는) 이 형식에서 일반적이지 않지만 (보통 {1}) 내용은 정상적으로 읽혔습니다.
Arsip tidak berisi file apa pun. | アーカイブにファイルが含まれていません。 | 压缩包中没有任何文件。 | 아카이브에 파일이 없습니다.
The archive contains no files. | アーカイブにファイルが含まれていません。 | 压缩包中没有任何文件。 | 아카이브에 파일이 없습니다.
Semua posisi file berada di dalam ukuran arsip. | すべてのファイル位置がアーカイブのサイズ内にあります。 | 所有文件位置都在压缩包大小范围内。 | 모든 파일 위치가 아카이브 크기 안에 있습니다.
Every file position lies within the archive size. | すべてのファイル位置がアーカイブのサイズ内にあります。 | 所有文件位置都在压缩包大小范围内。 | 모든 파일 위치가 아카이브 크기 안에 있습니다.
{0} file menunjuk posisi di luar arsip (arsip terpotong/rusak), mis. {1} | {0} 個のファイルがアーカイブ外の位置を指しています (アーカイブの切り詰め/破損)。例: {1} | {0} 个文件指向压缩包之外的位置（压缩包被截断/损坏），例如 {1} | 파일 {0}개가 아카이브 밖의 위치를 가리킵니다 (아카이브가 잘렸거나 손상됨). 예: {1}
{0} files point outside the archive (truncated/corrupt), e.g. {1} | {0} 個のファイルがアーカイブ外の位置を指しています (アーカイブの切り詰め/破損)。例: {1} | {0} 个文件指向压缩包之外的位置（压缩包被截断/损坏），例如 {1} | 파일 {0}개가 아카이브 밖의 위치를 가리킵니다 (아카이브가 잘렸거나 손상됨). 예: {1}
{0} file memiliki ukuran yang melewati akhir arsip. | {0} 個のファイルのサイズがアーカイブの末尾を超えています。 | {0} 个文件的大小超出了压缩包末尾。 | 파일 {0}개의 크기가 아카이브 끝을 넘어갑니다.
{0} files extend past the end of the archive. | {0} 個のファイルのサイズがアーカイブの末尾を超えています。 | {0} 个文件的大小超出了压缩包末尾。 | 파일 {0}개의 크기가 아카이브 끝을 넘어갑니다.
{0} nama file ganda (tidak peka huruf besar/kecil); sebagian bisa saling menimpa saat diekstrak. | {0} 個のファイル名が重複しています (大文字小文字を区別しない)。展開時に上書きされる可能性があります。 | 有 {0} 个重复文件名（不区分大小写），解包时部分文件可能互相覆盖。 | 중복된 파일 이름이 {0}개 있습니다 (대소문자 구분 없음). 풀 때 일부가 서로 덮어쓸 수 있습니다.
{0} duplicate file names (case-insensitive); some may overwrite each other on extract. | {0} 個のファイル名が重複しています (大文字小文字を区別しない)。展開時に上書きされる可能性があります。 | 有 {0} 个重复文件名（不区分大小写），解包时部分文件可能互相覆盖。 | 중복된 파일 이름이 {0}개 있습니다 (대소문자 구분 없음). 풀 때 일부가 서로 덮어쓸 수 있습니다.
{0} nama file memuat karakter kontrol/tidak valid (mungkin salah encoding nama). | {0} 個のファイル名に制御文字/無効な文字が含まれています (ファイル名のエンコードが違う可能性があります)。 | {0} 个文件名包含控制字符/无效字符（文件名编码可能不正确）。 | 파일 이름 {0}개에 제어 문자/잘못된 문자가 있습니다 (파일 이름 인코딩이 다를 수 있음).
{0} file names contain control/invalid characters (possibly a wrong filename encoding). | {0} 個のファイル名に制御文字/無効な文字が含まれています (ファイル名のエンコードが違う可能性があります)。 | {0} 个文件名包含控制字符/无效字符（文件名编码可能不正确）。 | 파일 이름 {0}개에 제어 문자/잘못된 문자가 있습니다 (파일 이름 인코딩이 다를 수 있음).
Ada path sepanjang {0} karakter; sebagian sistem file menolak path > 255. | {0} 文字のパスがあります。255 文字を超えるパスを拒否するファイルシステムがあります。 | 有一个路径长达 {0} 个字符；部分文件系统不接受超过 255 的路径。 | 경로 길이가 {0}자인 항목이 있습니다. 일부 파일 시스템은 255자를 넘는 경로를 허용하지 않습니다.
A path is {0} characters long; some file systems reject paths over 255. | {0} 文字のパスがあります。255 文字を超えるパスを拒否するファイルシステムがあります。 | 有一个路径长达 {0} 个字符；部分文件系统不接受超过 255 的路径。 | 경로 길이가 {0}자인 항목이 있습니다. 일부 파일 시스템은 255자를 넘는 경로를 허용하지 않습니다.
{0} file berukuran 0 byte. | {0} 個のファイルが 0 バイトです。 | {0} 个文件大小为 0 字节。 | 파일 {0}개가 0바이트입니다.
{0} files are 0 bytes. | {0} 個のファイルが 0 バイトです。 | {0} 个文件大小为 0 字节。 | 파일 {0}개가 0바이트입니다.
Format ini menyimpan checksum Adler32 tiap file; dicek penuh lewat Periksa Integritas. | この形式は各ファイルの Adler32 チェックサムを保存しています。完全な確認は整合性チェックで行います。 | 此格式为每个文件保存 Adler32 校验和；可通过完整性检查进行完整校验。 | 이 형식은 파일별 Adler32 체크섬을 저장합니다. 무결성 검사에서 전체를 확인합니다.
This format stores an Adler32 checksum per file; checked in full by Integrity Check. | この形式は各ファイルの Adler32 チェックサムを保存しています。完全な確認は整合性チェックで行います。 | 此格式为每个文件保存 Adler32 校验和；可通过完整性检查进行完整校验。 | 이 형식은 파일별 Adler32 체크섬을 저장합니다. 무결성 검사에서 전체를 확인합니다.
Pemeriksaan di atas hanya membaca indeks. Gunakan Periksa Integritas untuk menguji isi tiap file. | 上記のチェックはインデックスのみを読み取ります。各ファイルの内容を調べるには整合性チェックを使用してください。 | 以上检查仅读取索引。请使用完整性检查来测试每个文件的内容。 | 위 검사는 인덱스만 읽습니다. 각 파일의 내용을 검사하려면 무결성 검사를 사용하세요.
The checks above only read the index. Use Integrity Check to test each file's content. | 上記のチェックはインデックスのみを読み取ります。各ファイルの内容を調べるには整合性チェックを使用してください。 | 以上检查仅读取索引。请使用完整性检查来测试每个文件的内容。 | 위 검사는 인덱스만 읽습니다. 각 파일의 내용을 검사하려면 무결성 검사를 사용하세요.
Jumlah scene | シーン数 | 场景数 | 장면 수
Scene count | シーン数 | 场景数 | 장면 수
Mode enkripsi exe | exe 暗号化モード | exe 加密模式 | exe 암호화 모드
Exe encryption mode | exe 暗号化モード | exe 加密模式 | exe 암호화 모드
ada (butuh kunci) | あり (鍵が必要) | 有（需要密钥） | 있음 (키 필요)
yes (key needed) | あり (鍵が必要) | 有（需要密钥） | 있음 (키 필요)
Scene.pck tidak memakai kunci exe; bisa dibongkar tanpa kunci. | この Scene.pck は exe 鍵を使用していません。鍵なしで展開できます。 | 此 Scene.pck 未使用 exe 密钥，无需密钥即可解包。 | 이 Scene.pck는 exe 키를 사용하지 않습니다. 키 없이 풀 수 있습니다.
This Scene.pck uses no exe key; it can be unpacked without a key. | この Scene.pck は exe 鍵を使用していません。鍵なしで展開できます。 | 此 Scene.pck 未使用 exe 密钥，无需密钥即可解包。 | 이 Scene.pck는 exe 키를 사용하지 않습니다. 키 없이 풀 수 있습니다.
Scene.pck memakai kunci exe; kunci berhasil dipulihkan otomatis ({0}/{1} scene terverifikasi). | この Scene.pck は exe 鍵を使用しており、鍵を自動復元しました ({0}/{1} シーンを検証済み)。 | 此 Scene.pck 使用了 exe 密钥，已自动恢复密钥（已验证 {0}/{1} 个场景）。 | 이 Scene.pck는 exe 키를 사용하며, 키를 자동으로 복원했습니다 (장면 {0}/{1}개 검증됨).
This Scene.pck uses an exe key; it was recovered automatically ({0}/{1} scenes verified). | この Scene.pck は exe 鍵を使用しており、鍵を自動復元しました ({0}/{1} シーンを検証済み)。 | 此 Scene.pck 使用了 exe 密钥，已自动恢复密钥（已验证 {0}/{1} 个场景）。 | 이 Scene.pck는 exe 키를 사용하며, 키를 자동으로 복원했습니다 (장면 {0}/{1}개 검증됨).
Scene.pck memakai kunci exe dan kunci tidak bisa dipulihkan otomatis. Coba Pencari Kunci atau isi opsi Kunci. | この Scene.pck は exe 鍵を使用しており、鍵を自動復元できませんでした。キーファインダーを試すか、鍵オプションを入力してください。 | 此 Scene.pck 使用了 exe 密钥，无法自动恢复。请尝试密钥查找器，或填写密钥选项。 | 이 Scene.pck는 exe 키를 사용하며 키를 자동으로 복원하지 못했습니다. 키 찾기를 사용하거나 키 옵션을 입력하세요.
This Scene.pck uses an exe key that could not be recovered automatically. Try Key Finder or fill the Key option. | この Scene.pck は exe 鍵を使用しており、鍵を自動復元できませんでした。キーファインダーを試すか、鍵オプションを入力してください。 | 此 Scene.pck 使用了 exe 密钥，无法自动恢复。请尝试密钥查找器，或填写密钥选项。 | 이 Scene.pck는 exe 키를 사용하며 키를 자동으로 복원하지 못했습니다. 키 찾기를 사용하거나 키 옵션을 입력하세요.
Scene.pck memuat potongan sumber asli (original source); tidak ikut dikemas ulang oleh FuruArc. | この Scene.pck には元ソース (original source) のチャンクがあり、FuruArc では再パックされません。 | 此 Scene.pck 含有原始源码（original source）数据块，FuruArc 不会重新打包。 | 이 Scene.pck에는 원본 소스(original source) 청크가 있으며 FuruArc는 이를 다시 묶지 않습니다.
This Scene.pck has original-source chunks; FuruArc does not repack them. | この Scene.pck には元ソース (original source) のチャンクがあり、FuruArc では再パックされません。 | 此 Scene.pck 含有原始源码（original source）数据块，FuruArc 不会重新打包。 | 이 Scene.pck에는 원본 소스(original source) 청크가 있으며 FuruArc는 이를 다시 묶지 않습니다.
Kemas Arsip butuh file _FuruArc_Siglus.meta hasil bongkar di folder yang sama. | アーカイブのパックには、展開時に作成された _FuruArc_Siglus.meta ファイルが同じフォルダに必要です。 | 打包压缩包需要同一文件夹中解包时生成的 _FuruArc_Siglus.meta 文件。 | 아카이브 묶기에는 풀기 때 생성된 _FuruArc_Siglus.meta 파일이 같은 폴더에 있어야 합니다.
Pack Archive needs the _FuruArc_Siglus.meta file from unpacking in the same folder. | アーカイブのパックには、展開時に作成された _FuruArc_Siglus.meta ファイルが同じフォルダに必要です。 | 打包压缩包需要同一文件夹中解包时生成的 _FuruArc_Siglus.meta 文件。 | 아카이브 묶기에는 풀기 때 생성된 _FuruArc_Siglus.meta 파일이 같은 폴더에 있어야 합니다.
Gameexe.dat tanpa kunci exe. | exe 鍵なしの Gameexe.dat です。 | 未使用 exe 密钥的 Gameexe.dat。 | exe 키가 없는 Gameexe.dat입니다.
Gameexe.dat without an exe key. | exe 鍵なしの Gameexe.dat です。 | 未使用 exe 密钥的 Gameexe.dat。 | exe 키가 없는 Gameexe.dat입니다.
Gameexe.dat memakai kunci exe; cari kuncinya dengan Pencari Kunci lalu isi opsi Kunci. | Gameexe.dat は exe 鍵を使用しています。キーファインダーで鍵を探し、鍵オプションに入力してください。 | Gameexe.dat 使用了 exe 密钥；请用密钥查找器找到密钥，再填入密钥选项。 | Gameexe.dat는 exe 키를 사용합니다. 키 찾기로 키를 찾은 뒤 키 옵션에 입력하세요.
Gameexe.dat uses an exe key; find it with Key Finder, then fill the Key option. | Gameexe.dat は exe 鍵を使用しています。キーファインダーで鍵を探し、鍵オプションに入力してください。 | Gameexe.dat 使用了 exe 密钥；请用密钥查找器找到密钥，再填入密钥选项。 | Gameexe.dat는 exe 키를 사용합니다. 키 찾기로 키를 찾은 뒤 키 옵션에 입력하세요.
Setiap entri berisi audio Ogg (suara karakter). | すべてのエントリに Ogg 音声 (キャラクターボイス) が含まれています。 | 每个条目都包含 Ogg 音频（角色语音）。 | 모든 항목에 Ogg 오디오(캐릭터 음성)가 들어 있습니다.
Every entry holds Ogg audio (character voice). | すべてのエントリに Ogg 音声 (キャラクターボイス) が含まれています。 | 每个条目都包含 Ogg 音频（角色语音）。 | 모든 항목에 Ogg 오디오(캐릭터 음성)가 들어 있습니다.
Tanda pengenal (magic) sesuai format {0}. | シグネチャ (マジック) が {0} 形式に一致しています。 | 标识（magic）符合 {0} 格式。 | 시그니처(매직)가 {0} 형식과 일치합니다.
The signature (magic) matches the {0} format. | シグネチャ (マジック) が {0} 形式に一致しています。 | 标识（magic）符合 {0} 格式。 | 시그니처(매직)가 {0} 형식과 일치합니다.
Indeks arsip gagal dibaca: {0}. Arsip kemungkinan terpotong/rusak, atau butuh opsi lain (mis. encoding nama file). | アーカイブのインデックスを読み込めませんでした: {0}。アーカイブが切り詰められているか破損している、または別のオプション (例: ファイル名のエンコード) が必要な可能性があります。 | 无法读取压缩包索引：{0}。压缩包可能被截断/损坏，或需要其他选项（例如文件名编码）。 | 아카이브 인덱스를 읽지 못했습니다: {0}. 아카이브가 잘렸거나 손상되었거나 다른 옵션(예: 파일 이름 인코딩)이 필요할 수 있습니다.
The archive index could not be read: {0}. The archive may be truncated/corrupt, or need another option (e.g. filename encoding). | アーカイブのインデックスを読み込めませんでした: {0}。アーカイブが切り詰められているか破損している、または別のオプション (例: ファイル名のエンコード) が必要な可能性があります。 | 无法读取压缩包索引：{0}。压缩包可能被截断/损坏，或需要其他选项（例如文件名编码）。 | 아카이브 인덱스를 읽지 못했습니다: {0}. 아카이브가 잘렸거나 손상되었거나 다른 옵션(예: 파일 이름 인코딩)이 필요할 수 있습니다.
{0} file berhasil dibaca dan didekompresi tanpa galat ({1}). | {0} 個のファイルをエラーなく読み込み・展開しました ({1})。 | 已读取并解压 {0} 个文件，无错误（{1}）。 | 파일 {0}개를 오류 없이 읽고 압축 해제했습니다 ({1}).
{0} files were read and decompressed without errors ({1}). | {0} 個のファイルをエラーなく読み込み・展開しました ({1})。 | 已读取并解压 {0} 个文件，无错误（{1}）。 | 파일 {0}개를 오류 없이 읽고 압축 해제했습니다 ({1}).
{0} dari {1} file gagal dibaca/ukurannya tidak cocok: {2}{3}. Arsip mungkin rusak, atau butuh kunci/opsi yang berbeda. | {1} 個中 {0} 個のファイルで読み込み失敗またはサイズ不一致がありました: {2}{3}。アーカイブが破損しているか、別の鍵/オプションが必要な可能性があります。 | {1} 个文件中有 {0} 个读取失败或大小不匹配：{2}{3}。压缩包可能已损坏，或需要不同的密钥/选项。 | 파일 {1}개 중 {0}개를 읽지 못했거나 크기가 일치하지 않습니다: {2}{3}. 아카이브가 손상되었거나 다른 키/옵션이 필요할 수 있습니다.
{0} of {1} files failed to read / had a size mismatch: {2}{3}. The archive may be corrupt, or need a different key/option. | {1} 個中 {0} 個のファイルで読み込み失敗またはサイズ不一致がありました: {2}{3}。アーカイブが破損しているか、別の鍵/オプションが必要な可能性があります。 | {1} 个文件中有 {0} 个读取失败或大小不匹配：{2}{3}。压缩包可能已损坏，或需要不同的密钥/选项。 | 파일 {1}개 중 {0}개를 읽지 못했거나 크기가 일치하지 않습니다: {2}{3}. 아카이브가 손상되었거나 다른 키/옵션이 필요할 수 있습니다.
Checksum Adler32 cocok untuk {0} file. | {0} 個のファイルで Adler32 チェックサムが一致しました。 | {0} 个文件的 Adler32 校验和匹配。 | 파일 {0}개의 Adler32 체크섬이 일치합니다.
Adler32 checksum matches for {0} files. | {0} 個のファイルで Adler32 チェックサムが一致しました。 | {0} 个文件的 Adler32 校验和匹配。 | 파일 {0}개의 Adler32 체크섬이 일치합니다.
Checksum Adler32 tidak cocok pada {0} file: {1}{2} | {0} 個のファイルで Adler32 チェックサムが一致しません: {1}{2} | {0} 个文件的 Adler32 校验和不匹配：{1}{2} | 파일 {0}개의 Adler32 체크섬이 일치하지 않습니다: {1}{2}
Adler32 mismatch on {0} files: {1}{2} | {0} 個のファイルで Adler32 チェックサムが一致しません: {1}{2} | {0} 个文件的 Adler32 校验和不匹配：{1}{2} | 파일 {0}개의 Adler32 체크섬이 일치하지 않습니다: {1}{2}
Kirikiri / KAG (XP3) | Kirikiri / KAG (XP3) | Kirikiri / KAG (XP3) | Kirikiri / KAG (XP3)
RPG Maker MV / MZ | RPG Maker MV / MZ | RPG Maker MV / MZ | RPG Maker MV / MZ
RPG Maker XP / VX / VX Ace | RPG Maker XP / VX / VX Ace | RPG Maker XP / VX / VX Ace | RPG Maker XP / VX / VX Ace
Nama file | ファイル名 | 文件名 | 파일 이름
File name | ファイル名 | 文件名 | 파일 이름
File besar: hanya {0} pertama yang dianalisis isinya. | 大きいファイル: 先頭の {0} のみ内容を解析しました。 | 大文件：仅分析了前 {0} 的内容。 | 큰 파일: 처음 {0}만 내용을 분석했습니다.
Large file: only the first {0} were analysed. | 大きいファイル: 先頭の {0} のみ内容を解析しました。 | 大文件：仅分析了前 {0} 的内容。 | 큰 파일: 처음 {0}만 내용을 분석했습니다.
The file is empty (0 bytes). | ファイルは空です (0 バイト)。 | 文件为空（0 字节）。 | 파일이 비어 있습니다 (0바이트).
Jenis file | ファイルの種類 | 文件类型 | 파일 유형
File type | ファイルの種類 | 文件类型 | 파일 유형
Aset RPG Maker terenkripsi | 暗号化された RPG Maker アセット | 已加密的 RPG Maker 资源 | 암호화된 RPG Maker 에셋
Encrypted RPG Maker asset | 暗号化された RPG Maker アセット | 已加密的 RPG Maker 资源 | 암호화된 RPG Maker 에셋
File ini TERENKRIPSI (header RPGMV). Dekripsi lewat Alat RPGM > Aset > Dekripsi. | このファイルは暗号化されています (RPGMV ヘッダー)。RPGM ツール > アセット > 復号 で復号してください。 | 此文件已加密（RPGMV 文件头）。请通过 RPGM 工具 > 资源 > 解密 进行解密。 | 이 파일은 암호화되어 있습니다 (RPGMV 헤더). RPGM 도구 > 에셋 > 복호화에서 복호화하세요.
This file is ENCRYPTED (RPGMV header). Decrypt it via RPGM Tools > Assets > Decrypt. | このファイルは暗号化されています (RPGMV ヘッダー)。RPGM ツール > アセット > 復号 で復号してください。 | 此文件已加密（RPGMV 文件头）。请通过 RPGM 工具 > 资源 > 解密 进行解密。 | 이 파일은 암호화되어 있습니다 (RPGMV 헤더). RPGM 도구 > 에셋 > 복호화에서 복호화하세요.
Kunci terdeteksi | 検出された鍵 | 检测到的密钥 | 감지된 키
Detected key | 検出された鍵 | 检测到的密钥 | 감지된 키
Gambar PNG | PNG 画像 | PNG 图像 | PNG 이미지
PNG image | PNG 画像 | PNG 图像 | PNG 이미지
Gambar JPEG | JPEG 画像 | JPEG 图像 | JPEG 이미지
JPEG image | JPEG 画像 | JPEG 图像 | JPEG 이미지
Gambar GIF | GIF 画像 | GIF 图像 | GIF 이미지
GIF image | GIF 画像 | GIF 图像 | GIF 이미지
Gambar BMP | BMP 画像 | BMP 图像 | BMP 이미지
BMP image | BMP 画像 | BMP 图像 | BMP 이미지
Gambar WebP | WebP 画像 | WebP 图像 | WebP 이미지
"""
    const val T10 = """
WebP image | WebP 画像 | WebP 图像 | WebP 이미지
Audio WAV | WAV 音声 | WAV 音频 | WAV 오디오
WAV audio | WAV 音声 | WAV 音频 | WAV 오디오
Audio/video Ogg | Ogg 音声/動画 | Ogg 音频/视频 | Ogg 오디오/비디오
Ogg audio/video | Ogg 音声/動画 | Ogg 音频/视频 | Ogg 오디오/비디오
Audio MP3 | MP3 音声 | MP3 音频 | MP3 오디오
Audio/video MP4/M4A | MP4/M4A 音声/動画 | MP4/M4A 音频/视频 | MP4/M4A 오디오/비디오
Arsip ZIP | ZIP アーカイブ | ZIP 压缩包 | ZIP 아카이브
ZIP archive | ZIP アーカイブ | ZIP 压缩包 | ZIP 아카이브
Ini arsip ZIP. Jika dari game Tyrano, pakai Bongkar Arsip (format Tyrano). | これは ZIP アーカイブです。Tyrano ゲームの場合は、アーカイブの展開 (Tyrano 形式) を使用してください。 | 这是 ZIP 压缩包。如果来自 Tyrano 游戏，请使用解包压缩包（Tyrano 格式）。 | ZIP 아카이브입니다. Tyrano 게임이라면 아카이브 풀기(Tyrano 형식)를 사용하세요.
This is a ZIP archive. For a Tyrano game, use Unpack Archive (Tyrano format). | これは ZIP アーカイブです。Tyrano ゲームの場合は、アーカイブの展開 (Tyrano 形式) を使用してください。 | 这是 ZIP 压缩包。如果来自 Tyrano 游戏，请使用解包压缩包（Tyrano 格式）。 | ZIP 아카이브입니다. Tyrano 게임이라면 아카이브 풀기(Tyrano 형식)를 사용하세요.
Arsip RAR/7z (tidak didukung) | RAR/7z アーカイブ (非対応) | RAR/7z 压缩包（不支持） | RAR/7z 아카이브 (지원 안 함)
RAR/7z archive (unsupported) | RAR/7z アーカイブ (非対応) | RAR/7z 压缩包（不支持） | RAR/7z 아카이브 (지원 안 함)
Format ini tidak didukung FuruArc. Ekstrak dulu dengan aplikasi arsip lain. | この形式は FuruArc では対応していません。先に別のアーカイブツールで展開してください。 | FuruArc 不支持此格式。请先用其他压缩工具解压。 | 이 형식은 FuruArc에서 지원하지 않습니다. 먼저 다른 압축 도구로 풀어 주세요.
FuruArc does not support this format. Extract it with another archive tool first. | この形式は FuruArc では対応していません。先に別のアーカイブツールで展開してください。 | FuruArc 不支持此格式。请先用其他压缩工具解压。 | 이 형식은 FuruArc에서 지원하지 않습니다. 먼저 다른 압축 도구로 풀어 주세요.
Bytecode TJS2 (skrip Kirikiri terkompilasi) | TJS2 バイトコード (コンパイル済み Kirikiri スクリプト) | TJS2 字节码（已编译的 Kirikiri 脚本） | TJS2 바이트코드 (컴파일된 Kirikiri 스크립트)
TJS2 bytecode (compiled Kirikiri script) | TJS2 バイトコード (コンパイル済み Kirikiri スクリプト) | TJS2 字节码（已编译的 Kirikiri 脚本） | TJS2 바이트코드 (컴파일된 Kirikiri 스크립트)
Skrip sudah terkompilasi menjadi bytecode; tidak bisa dibaca/diterjemahkan sebagai teks. | スクリプトはバイトコードにコンパイルされているため、テキストとして読み取り/翻訳できません。 | 脚本已编译为字节码，无法作为文本读取/翻译。 | 스크립트가 바이트코드로 컴파일되어 있어 텍스트로 읽거나 번역할 수 없습니다.
The script is compiled bytecode; it cannot be read/translated as text. | スクリプトはバイトコードにコンパイルされているため、テキストとして読み取り/翻訳できません。 | 脚本已编译为字节码，无法作为文本读取/翻译。 | 스크립트가 바이트코드로 컴파일되어 있어 텍스트로 읽거나 번역할 수 없습니다.
Skrip PSB (.scn Kirikiri/E-mote) | PSB スクリプト (Kirikiri/E-mote の .scn) | PSB 脚本（Kirikiri/E-mote 的 .scn） | PSB 스크립트 (Kirikiri/E-mote .scn)
PSB script (Kirikiri/E-mote .scn) | PSB スクリプト (Kirikiri/E-mote の .scn) | PSB 脚本（Kirikiri/E-mote 的 .scn） | PSB 스크립트 (Kirikiri/E-mote .scn)
Format PSB biner; belum ada alat teks untuknya di FuruArc. | バイナリ形式の PSB です。FuruArc にはまだ対応するテキストツールがありません。 | 二进制 PSB 格式；FuruArc 暂时没有对应的文本工具。 | 바이너리 PSB 형식입니다. FuruArc에는 아직 이를 위한 텍스트 도구가 없습니다.
Binary PSB format; FuruArc has no text tool for it yet. | バイナリ形式の PSB です。FuruArc にはまだ対応するテキストツールがありません。 | 二进制 PSB 格式；FuruArc 暂时没有对应的文本工具。 | 바이너리 PSB 형식입니다. FuruArc에는 아직 이를 위한 텍스트 도구가 없습니다.
Skrip teks Kirikiri (header FE FE) | Kirikiri テキストスクリプト (FE FE ヘッダー) | Kirikiri 文本脚本（FE FE 文件头） | Kirikiri 텍스트 스크립트 (FE FE 헤더)
Kirikiri text script (FE FE header) | Kirikiri テキストスクリプト (FE FE ヘッダー) | Kirikiri 文本脚本（FE FE 文件头） | Kirikiri 텍스트 스크립트 (FE FE 헤더)
Mode 0: UTF-16 polos (tidak dienkripsi). | モード 0: プレーン UTF-16 (暗号化なし)。 | 模式 0：纯 UTF-16（未加密）。 | 모드 0: 일반 UTF-16 (암호화 안 됨).
Mode 0: plain UTF-16 (not encrypted). | モード 0: プレーン UTF-16 (暗号化なし)。 | 模式 0：纯 UTF-16（未加密）。 | 모드 0: 일반 UTF-16 (암호화 안 됨).
Mode 1: TERENKRIPSI (XOR). Butuh dekripsi bawaan Kirikiri/Kirikiroid. | モード 1: 暗号化 (XOR)。Kirikiri/Kirikiroid 標準の復号が必要です。 | 模式 1：已加密（XOR）。需要 Kirikiri/Kirikiroid 内置解密。 | 모드 1: 암호화됨 (XOR). Kirikiri/Kirikiroid 내장 복호화가 필요합니다.
Mode 1: ENCRYPTED (XOR). Needs Kirikiri/Kirikiroid decryption. | モード 1: 暗号化 (XOR)。Kirikiri/Kirikiroid 標準の復号が必要です。 | 模式 1：已加密（XOR）。需要 Kirikiri/Kirikiroid 内置解密。 | 모드 1: 암호화됨 (XOR). Kirikiri/Kirikiroid 내장 복호화가 필요합니다.
Mode 2: TERKOMPRESI/terenkripsi (zlib). Butuh dekode bawaan Kirikiri. | モード 2: 圧縮/暗号化 (zlib)。Kirikiri 標準のデコードが必要です。 | 模式 2：已压缩/加密（zlib）。需要 Kirikiri 内置解码。 | 모드 2: 압축/암호화됨 (zlib). Kirikiri 내장 디코딩이 필요합니다.
Mode 2: COMPRESSED/encrypted (zlib). Needs Kirikiri decoding. | モード 2: 圧縮/暗号化 (zlib)。Kirikiri 標準のデコードが必要です。 | 模式 2：已压缩/加密（zlib）。需要 Kirikiri 内置解码。 | 모드 2: 압축/암호화됨 (zlib). Kirikiri 내장 디코딩이 필요합니다.
Mode {0} tidak dikenal. | モード {0} は不明です。 | 未知模式 {0}。 | 알 수 없는 모드 {0}.
Unknown mode {0}. | モード {0} は不明です。 | 未知模式 {0}。 | 알 수 없는 모드 {0}.
RENPY RPC | RENPY RPC | RENPY RPC | RENPY RPC
File save RPG Maker | RPG Maker セーブファイル | RPG Maker 存档文件 | RPG Maker 세이브 파일
RPG Maker save file | RPG Maker セーブファイル | RPG Maker 存档文件 | RPG Maker 세이브 파일
Isi save tidak bisa didekode (rusak atau bukan save MV/MZ). | セーブの内容をデコードできません (破損しているか、MV/MZ のセーブではありません)。 | 无法解码存档内容（已损坏或不是 MV/MZ 存档）。 | 세이브 내용을 디코딩할 수 없습니다 (손상되었거나 MV/MZ 세이브가 아님).
The save content cannot be decoded (corrupt or not an MV/MZ save). | セーブの内容をデコードできません (破損しているか、MV/MZ のセーブではありません)。 | 无法解码存档内容（已损坏或不是 MV/MZ 存档）。 | 세이브 내용을 디코딩할 수 없습니다 (손상되었거나 MV/MZ 세이브가 아님).
Save berhasil didekode ({0}, resep {1}). | セーブのデコードに成功しました ({0}、レシピ {1})。 | 存档解码成功（{0}，配方 {1}）。 | 세이브를 디코딩했습니다 ({0}, 레시피 {1}).
Save decoded successfully ({0}, recipe {1}). | セーブのデコードに成功しました ({0}、レシピ {1})。 | 存档解码成功（{0}，配方 {1}）。 | 세이브를 디코딩했습니다 ({0}, 레시피 {1}).
JSON di dalam save valid. | セーブ内の JSON は有効です。 | 存档内的 JSON 有效。 | 세이브 안의 JSON이 유효합니다.
The JSON inside the save is valid. | セーブ内の JSON は有効です。 | 存档内的 JSON 有效。 | 세이브 안의 JSON이 유효합니다.
RPG Maker XP | RPG Maker XP | RPG Maker XP | RPG Maker XP
RPG Maker VX | RPG Maker VX | RPG Maker VX | RPG Maker VX
RPG Maker VX Ace | RPG Maker VX Ace | RPG Maker VX Ace | RPG Maker VX Ace
Data Ruby Marshal ({0}) | Ruby Marshal データ ({0}) | Ruby Marshal 数据（{0}） | Ruby Marshal 데이터 ({0})
Ruby Marshal data ({0}) | Ruby Marshal データ ({0}) | Ruby Marshal 数据（{0}） | Ruby Marshal 데이터 ({0})
Header Marshal 4.8 valid. | Marshal 4.8 ヘッダーは有効です。 | Marshal 4.8 文件头有效。 | Marshal 4.8 헤더가 유효합니다.
Valid Marshal 4.8 header. | Marshal 4.8 ヘッダーは有効です。 | Marshal 4.8 文件头有效。 | Marshal 4.8 헤더가 유효합니다.
Ini data biner game, bukan teks; sunting lewat Alat RPGM atau editor RPG Maker. | これはテキストではなくバイナリのゲームデータです。RPGM ツールまたは RPG Maker のエディターで編集してください。 | 这是二进制游戏数据，不是文本；请用 RPGM 工具或 RPG Maker 编辑器进行编辑。 | 텍스트가 아닌 바이너리 게임 데이터입니다. RPGM 도구 또는 RPG Maker 에디터로 편집하세요.
This is binary game data, not text; edit it with RPGM Tools or the RPG Maker editor. | これはテキストではなくバイナリのゲームデータです。RPGM ツールまたは RPG Maker のエディターで編集してください。 | 这是二进制游戏数据，不是文本；请用 RPGM 工具或 RPG Maker 编辑器进行编辑。 | 텍스트가 아닌 바이너리 게임 데이터입니다. RPGM 도구 또는 RPG Maker 에디터로 편집하세요.
File Scripts berisi skrip Ruby yang dikompresi zlib. | Scripts ファイルには zlib 圧縮された Ruby スクリプトが入っています。 | Scripts 文件包含经 zlib 压缩的 Ruby 脚本。 | Scripts 파일에는 zlib으로 압축된 Ruby 스크립트가 들어 있습니다.
The Scripts file holds zlib-compressed Ruby scripts. | Scripts ファイルには zlib 圧縮された Ruby スクリプトが入っています。 | Scripts 文件包含经 zlib 压缩的 Ruby 脚本。 | Scripts 파일에는 zlib으로 압축된 Ruby 스크립트가 들어 있습니다.
Skrip NScripter (nscript.dat) | NScripter スクリプト (nscript.dat) | NScripter 脚本（nscript.dat） | NScripter 스크립트 (nscript.dat)
NScripter script (nscript.dat) | NScripter スクリプト (nscript.dat) | NScripter 脚本（nscript.dat） | NScripter 스크립트 (nscript.dat)
Isinya sudah tampak berupa teks biasa (tidak terenkripsi XOR 0x84). | 内容はすでにプレーンテキストのようです (XOR 0x84 で暗号化されていません)。 | 内容看起来已是纯文本（未经 XOR 0x84 加密）。 | 내용이 이미 일반 텍스트로 보입니다 (XOR 0x84로 암호화되지 않음).
The content already looks like plain text (not XOR 0x84 encrypted). | 内容はすでにプレーンテキストのようです (XOR 0x84 で暗号化されていません)。 | 内容看起来已是纯文本（未经 XOR 0x84 加密）。 | 내용이 이미 일반 텍스트로 보입니다 (XOR 0x84로 암호화되지 않음).
TERENKRIPSI (XOR 0x84). Dekode lewat Alat Skrip NScripter > Dekode nscript.dat. | 暗号化されています (XOR 0x84)。NScripter スクリプトツール > nscript.dat のデコード でデコードしてください。 | 已加密（XOR 0x84）。请通过 NScripter 脚本工具 > 解码 nscript.dat 进行解码。 | 암호화되어 있습니다 (XOR 0x84). NScripter 스크립트 도구 > nscript.dat 디코딩에서 디코딩하세요.
ENCRYPTED (XOR 0x84). Decode it with NScripter Script Tools > Decode nscript.dat. | 暗号化されています (XOR 0x84)。NScripter スクリプトツール > nscript.dat のデコード でデコードしてください。 | 已加密（XOR 0x84）。请通过 NScripter 脚本工具 > 解码 nscript.dat 进行解码。 | 암호화되어 있습니다 (XOR 0x84). NScripter 스크립트 도구 > nscript.dat 디코딩에서 디코딩하세요.
Isi tidak dikenali sebagai nscript.dat (bukan XOR 0x84 sederhana). | 内容が nscript.dat として認識されません (単純な XOR 0x84 ではありません)。 | 内容无法识别为 nscript.dat（不是简单的 XOR 0x84）。 | 내용이 nscript.dat로 인식되지 않습니다 (단순 XOR 0x84가 아님).
The content is not recognised as nscript.dat (not a simple XOR 0x84). | 内容が nscript.dat として認識されません (単純な XOR 0x84 ではありません)。 | 内容无法识别为 nscript.dat（不是简单的 XOR 0x84）。 | 내용이 nscript.dat로 인식되지 않습니다 (단순 XOR 0x84가 아님).
Data biner / tidak dikenal | バイナリ/不明なデータ | 二进制/未知数据 | 바이너리/알 수 없는 데이터
Binary / unknown data | バイナリ/不明なデータ | 二进制/未知数据 | 바이너리/알 수 없는 데이터
Entropi sangat tinggi: isi kemungkinan besar TERENKRIPSI atau terkompresi. | エントロピーが非常に高く、内容は暗号化または圧縮されている可能性が高いです。 | 熵值非常高：内容很可能已加密或压缩。 | 엔트로피가 매우 높습니다. 내용이 암호화되었거나 압축되었을 가능성이 높습니다.
Very high entropy: the content is most likely ENCRYPTED or compressed. | エントロピーが非常に高く、内容は暗号化または圧縮されている可能性が高いです。 | 熵值非常高：内容很可能已加密或压缩。 | 엔트로피가 매우 높습니다. 내용이 암호화되었거나 압축되었을 가능성이 높습니다.
File biner yang tidak dikenali FuruArc sebagai skrip. | FuruArc がスクリプトとして認識できないバイナリファイルです。 | FuruArc 无法识别为脚本的二进制文件。 | FuruArc가 스크립트로 인식하지 못하는 바이너리 파일입니다.
A binary file FuruArc does not recognise as a script. | FuruArc がスクリプトとして認識できないバイナリファイルです。 | FuruArc 无法识别为脚本的二进制文件。 | FuruArc가 스크립트로 인식하지 못하는 바이너리 파일입니다.
Jika ini arsip game, coba Bongkar Arsip; format tak dikenal tidak bisa diperiksa lebih lanjut. | ゲームのアーカイブであれば、アーカイブの展開を試してください。不明な形式はこれ以上調べられません。 | 如果这是游戏压缩包，请尝试解包压缩包；未知格式无法进一步检查。 | 게임 아카이브라면 아카이브 풀기를 시도해 보세요. 알 수 없는 형식은 더 이상 검사할 수 없습니다.
If this is a game archive, try Unpack Archive; unknown formats cannot be inspected further. | ゲームのアーカイブであれば、アーカイブの展開を試してください。不明な形式はこれ以上調べられません。 | 如果这是游戏压缩包，请尝试解包压缩包；未知格式无法进一步检查。 | 게임 아카이브라면 아카이브 풀기를 시도해 보세요. 알 수 없는 형식은 더 이상 검사할 수 없습니다.
JavaScript (plugin/skrip RPG Maker MV/MZ) | JavaScript (RPG Maker MV/MZ のプラグイン/スクリプト) | JavaScript（RPG Maker MV/MZ 插件/脚本） | JavaScript (RPG Maker MV/MZ 플러그인/스크립트)
JavaScript (RPG Maker MV/MZ plugin/script) | JavaScript (RPG Maker MV/MZ のプラグイン/スクリプト) | JavaScript（RPG Maker MV/MZ 插件/脚本） | JavaScript (RPG Maker MV/MZ 플러그인/스크립트)
Kurung kurawal seimbang ({ } = {0}). | 波括弧は対応しています ({ } = {0})。 | 大括号配对平衡（{ } = {0}）。 | 중괄호가 균형을 이룹니다 ({ } = {0}).
Braces balanced ({ } = {0}). | 波括弧は対応しています ({ } = {0})。 | 大括号配对平衡（{ } = {0}）。 | 중괄호가 균형을 이룹니다 ({ } = {0}).
Kurung kurawal tidak seimbang: { = {0}, } = {1} (bisa terpengaruh kurung dalam string/komentar). | 波括弧が対応していません: { = {0}、} = {1} (文字列/コメント内の括弧の影響を受ける場合があります)。 | 大括号不平衡：{ = {0}，} = {1}（可能受字符串/注释中的括号影响）。 | 중괄호가 균형이 맞지 않습니다: { = {0}, } = {1} (문자열/주석 안의 중괄호 영향일 수 있음).
Unbalanced braces: { = {0}, } = {1} (may be skewed by braces in strings/comments). | 波括弧が対応していません: { = {0}、} = {1} (文字列/コメント内の括弧の影響を受ける場合があります)。 | 大括号不平衡：{ = {0}，} = {1}（可能受字符串/注释中的括号影响）。 | 중괄호가 균형이 맞지 않습니다: { = {0}, } = {1} (문자열/주석 안의 중괄호 영향일 수 있음).
Daftar plugin game; urutannya menentukan urutan pemuatan. | ゲームのプラグイン一覧です。順序が読み込み順になります。 | 游戏的插件列表；其顺序即加载顺序。 | 게임의 플러그인 목록입니다. 순서가 로드 순서입니다.
The game's plugin list; its order is the load order. | ゲームのプラグイン一覧です。順序が読み込み順になります。 | 游戏的插件列表；其顺序即加载顺序。 | 게임의 플러그인 목록입니다. 순서가 로드 순서입니다.
Teks biasa | プレーンテキスト | 纯文本 | 일반 텍스트
Plain text | プレーンテキスト | 纯文本 | 일반 텍스트
Tidak dikenali sebagai skrip VN/RPGM tertentu. Untuk skrip bernama khusus, ganti ekstensinya (.rpy, .ks, .s, .txt Artemis). | 特定の VN/RPGM スクリプトとして認識されません。独自名のスクリプトは、正しい拡張子 (.rpy、.ks、.s、Artemis の .txt) に変更してください。 | 无法识别为特定的 VN/RPGM 脚本。对于自定义命名的脚本，请改为正确的扩展名（.rpy、.ks、.s、Artemis 的 .txt）。 | 특정 VN/RPGM 스크립트로 인식되지 않습니다. 이름이 따로 지정된 스크립트는 올바른 확장자(.rpy, .ks, .s, Artemis .txt)로 바꿔 주세요.
Not recognised as a specific VN/RPGM script. For custom-named scripts, give it the right extension (.rpy, .ks, .s, Artemis .txt). | 特定の VN/RPGM スクリプトとして認識されません。独自名のスクリプトは、正しい拡張子 (.rpy、.ks、.s、Artemis の .txt) に変更してください。 | 无法识别为特定的 VN/RPGM 脚本。对于自定义命名的脚本，请改为正确的扩展名（.rpy、.ks、.s、Artemis 的 .txt）。 | 특정 VN/RPGM 스크립트로 인식되지 않습니다. 이름이 따로 지정된 스크립트는 올바른 확장자(.rpy, .ks, .s, Artemis .txt)로 바꿔 주세요.
Ini file aset, bukan skrip. | これはアセットファイルであり、スクリプトではありません。 | 这是资源文件，不是脚本。 | 에셋 파일이며 스크립트가 아닙니다.
This is an asset file, not a script. | これはアセットファイルであり、スクリプトではありません。 | 这是资源文件，不是脚本。 | 에셋 파일이며 스크립트가 아닙니다.
Ekstensi .{0} tidak cocok dengan isinya ({1}). | 拡張子 .{0} が内容 ({1}) と一致しません。 | 扩展名 .{0} 与内容（{1}）不符。 | 확장자 .{0}이(가) 내용({1})과 일치하지 않습니다.
Extension .{0} does not match the content ({1}). | 拡張子 .{0} が内容 ({1}) と一致しません。 | 扩展名 .{0} 与内容（{1}）不符。 | 확장자 .{0}이(가) 내용({1})과 일치하지 않습니다.
Ekstensi sesuai dengan isinya. | 拡張子は内容と一致しています。 | 扩展名与内容相符。 | 확장자가 내용과 일치합니다.
The extension matches the content. | 拡張子は内容と一致しています。 | 扩展名与内容相符。 | 확장자가 내용과 일치합니다.
Jumlah baris | 行数 | 行数 | 줄 수
Line count | 行数 | 行数 | 줄 수
Akhir baris | 改行コード | 换行符 | 줄바꿈
campur (CRLF {0}, LF {1}) | 混在 (CRLF {0}、LF {1}) | 混合（CRLF {0}，LF {1}） | 혼합 (CRLF {0}, LF {1})
mixed (CRLF {0}, LF {1}) | 混在 (CRLF {0}、LF {1}) | 混合（CRLF {0}，LF {1}） | 혼합 (CRLF {0}, LF {1})
{0} karakter tidak valid (�): encoding mungkin bukan {1}. | 無効な文字が {0} 個あります (�)。エンコードが {1} ではない可能性があります。 | 有 {0} 个无效字符（�）：编码可能不是 {1}。 | 잘못된 문자(�)가 {0}개 있습니다. 인코딩이 {1}이(가) 아닐 수 있습니다.
{0} invalid characters (�): the encoding may not be {1}. | 無効な文字が {0} 個あります (�)。エンコードが {1} ではない可能性があります。 | 有 {0} 个无效字符（�）：编码可能不是 {1}。 | 잘못된 문자(�)가 {0}개 있습니다. 인코딩이 {1}이(가) 아닐 수 있습니다.
Ada byte NUL ({0}) di dalam teks; file mungkin biner atau terenkripsi. | テキスト内に NUL バイト ({0}) があります。ファイルはバイナリか暗号化されている可能性があります。 | 文本中有 NUL 字节（{0}）；文件可能是二进制或已加密。 | 텍스트에 NUL 바이트({0})가 있습니다. 파일이 바이너리이거나 암호화되었을 수 있습니다.
There are NUL bytes ({0}) in the text; the file may be binary or encrypted. | テキスト内に NUL バイト ({0}) があります。ファイルはバイナリか暗号化されている可能性があります。 | 文本中有 NUL 字节（{0}）；文件可能是二进制或已加密。 | 텍스트에 NUL 바이트({0})가 있습니다. 파일이 바이너리이거나 암호화되었을 수 있습니다.
UTF-8 tanpa BOM. | BOM なしの UTF-8 です。 | 无 BOM 的 UTF-8。 | BOM 없는 UTF-8입니다.
UTF-8 without BOM. | BOM なしの UTF-8 です。 | 无 BOM 的 UTF-8。 | BOM 없는 UTF-8입니다.
Isi file terbaca sebagai teks (tidak terenkripsi). | ファイルの内容はテキストとして読み取れます (暗号化なし)。 | 文件内容可作为文本读取（未加密）。 | 파일 내용을 텍스트로 읽을 수 있습니다 (암호화되지 않음).
The file reads as text (not encrypted). | ファイルの内容はテキストとして読み取れます (暗号化なし)。 | 文件内容可作为文本读取（未加密）。 | 파일 내용을 텍스트로 읽을 수 있습니다 (암호화되지 않음).
Teks yang bisa diekspor | エクスポート可能なテキスト | 可导出的文本 | 내보낼 수 있는 텍스트
Exportable texts | エクスポート可能なテキスト | 可导出的文本 | 내보낼 수 있는 텍스트
Skrip Ren'Py (.rpy) | Ren'Py スクリプト (.rpy) | Ren'Py 脚本（.rpy） | Ren'Py 스크립트 (.rpy)
Ren'Py script (.rpy) | Ren'Py スクリプト (.rpy) | Ren'Py 脚本（.rpy） | Ren'Py 스크립트 (.rpy)
Jumlah label | ラベル数 | 标签数 | 라벨 수
Label count | ラベル数 | 标签数 | 라벨 수
Struktur indentasi/blok tidak diperiksa penuh; ini pemeriksaan dangkal. | インデント/ブロック構造は完全には確認していません。簡易チェックです。 | 缩进/代码块结构未做完整检查；这只是浅层检查。 | 들여쓰기/블록 구조는 완전히 검사하지 않습니다. 간단한 검사입니다.
Indentation/block structure is not fully checked; this is a shallow check. | インデント/ブロック構造は完全には確認していません。簡易チェックです。 | 缩进/代码块结构未做完整检查；这只是浅层检查。 | 들여쓰기/블록 구조는 완전히 검사하지 않습니다. 간단한 검사입니다.
Tidak ada indentasi tab (Ren'Py menolak tab). | タブによるインデントはありません (Ren'Py はタブを受け付けません)。 | 没有使用制表符缩进（Ren'Py 不接受制表符）。 | 탭 들여쓰기가 없습니다 (Ren'Py는 탭을 허용하지 않음).
No tab indentation (Ren'Py rejects tabs). | タブによるインデントはありません (Ren'Py はタブを受け付けません)。 | 没有使用制表符缩进（Ren'Py 不接受制表符）。 | 탭 들여쓰기가 없습니다 (Ren'Py는 탭을 허용하지 않음).
{0} baris berindentasi TAB (Ren'Py error), mulai baris {1}. | {0} 行が TAB でインデントされています (Ren'Py ではエラー)。{1} 行目から。 | 有 {0} 行使用 TAB 缩进（Ren'Py 会报错），从第 {1} 行开始。 | {0}줄이 TAB으로 들여쓰기되어 있습니다 (Ren'Py 오류). {1}번째 줄부터.
{0} lines are indented with TABs (Ren'Py errors), starting at line {1}. | {0} 行が TAB でインデントされています (Ren'Py ではエラー)。{1} 行目から。 | 有 {0} 行使用 TAB 缩进（Ren'Py 会报错），从第 {1} 行开始。 | {0}줄이 TAB으로 들여쓰기되어 있습니다 (Ren'Py 오류). {1}번째 줄부터.
Tidak ada label ganda. | 重複するラベルはありません。 | 没有重复的标签。 | 중복된 라벨이 없습니다.
No duplicate labels. | 重複するラベルはありません。 | 没有重复的标签。 | 중복된 라벨이 없습니다.
Label ganda: {0} | 重複ラベル: {0} | 重复标签：{0} | 중복 라벨: {0}
Duplicate labels: {0} | 重複ラベル: {0} | 重复标签：{0} | 중복 라벨: {0}
Tanda kutip " berpasangan di semua baris. | 引用符 " はすべての行で対になっています。 | 所有行的双引号 " 都成对出现。 | 모든 줄에서 큰따옴표 "가 짝을 이룹니다.
Double quotes " are paired on every line. | 引用符 " はすべての行で対になっています。 | 所有行的双引号 " 都成对出现。 | 모든 줄에서 큰따옴표 "가 짝을 이룹니다.
{0} baris dengan tanda kutip tidak berpasangan, mulai baris {1}. | {0} 行で引用符が対になっていません ({1} 行目など)。 | 有 {0} 行引号不成对，例如第 {1} 行。 | 따옴표가 짝을 이루지 않는 줄이 {0}개 있습니다 (예: {1}번째 줄).
{0} lines with unpaired quotes, e.g. lines {1}. | {0} 行で引用符が対になっていません ({1} 行目など)。 | 有 {0} 行引号不成对，例如第 {1} 行。 | 따옴표가 짝을 이루지 않는 줄이 {0}개 있습니다 (예: {1}번째 줄).
Tag teks { } tidak seimbang di {0} baris, mis. baris {1}. | テキストタグ { } が {0} 行で対応していません ({1} 行目など)。 | 文本标签 { } 在 {0} 行中不平衡，例如第 {1} 行。 | 텍스트 태그 { }가 {0}줄에서 균형이 맞지 않습니다 (예: {1}번째 줄).
Text tags { } unbalanced on {0} lines, e.g. lines {1}. | テキストタグ { } が {0} 行で対応していません ({1} 行目など)。 | 文本标签 { } 在 {0} 行中不平衡，例如第 {1} 行。 | 텍스트 태그 { }가 {0}줄에서 균형이 맞지 않습니다 (예: {1}번째 줄).
Tag teks { } seimbang di dalam string. | 文字列内のテキストタグ { } は対応しています。 | 字符串内的文本标签 { } 平衡。 | 문자열 안의 텍스트 태그 { }가 균형을 이룹니다.
Text tags { } are balanced inside strings. | 文字列内のテキストタグ { } は対応しています。 | 字符串内的文本标签 { } 均已配对。 | 문자열 안의 텍스트 태그 { }가 짝이 맞습니다.
Skrip TyranoScript (.ks) | TyranoScript スクリプト (.ks) | TyranoScript 脚本 (.ks) | TyranoScript 스크립트 (.ks)
Skrip KAG Kirikiri (.ks) | KAG Kirikiri スクリプト (.ks) | KAG Kirikiri 脚本 (.ks) | KAG Kirikiri 스크립트 (.ks)
Kirikiri KAG script (.ks) | Kirikiri KAG スクリプト (.ks) | Kirikiri KAG 脚本 (.ks) | Kirikiri KAG 스크립트 (.ks)
Semua tag [ ] tertutup di tiap baris. | すべての行で [ ] タグが閉じています。 | 每一行的 [ ] 标签都已闭合。 | 모든 줄에서 [ ] 태그가 닫혀 있습니다.
Every [ ] tag is closed on its line. | すべての行で [ ] タグが閉じています。 | 每一行的 [ ] 标签都已闭合。 | 모든 줄에서 [ ] 태그가 닫혀 있습니다.
{0} baris dengan kurung [ ] tidak seimbang, mis. baris {1} (tag lintas baris juga terdeteksi di sini). | [ ] が対応していない行が {0} 行あります (例: {1} 行目)。複数行にまたがるタグもここで検出されます。 | 有 {0} 行的 [ ] 不配对，例如第 {1} 行（跨行标签也会在此检出）。 | [ ]가 짝이 맞지 않는 줄이 {0}개 있습니다. 예: {1}번째 줄 (여러 줄에 걸친 태그도 여기서 감지됩니다).
{0} lines with unbalanced [ ], e.g. lines {1} (multi-line tags are flagged here too). | [ ] が対応していない行が {0} 行あります (例: {1} 行目)。複数行にまたがるタグもここで検出されます。 | 有 {0} 行的 [ ] 不配对，例如第 {1} 行（跨行标签也会在此检出）。 | [ ]가 짝이 맞지 않는 줄이 {0}개 있습니다. 예: {1}번째 줄 (여러 줄에 걸친 태그도 여기서 감지됩니다).
[{0}] ({1}) dan [{2}] ({3}) tidak seimbang. | [{0}] ({1}) と [{2}] ({3}) が対応していません。 | [{0}] ({1}) 与 [{2}] ({3}) 不配对。 | [{0}] ({1})와 [{2}] ({3})의 짝이 맞지 않습니다.
[{0}] ({1}) and [{2}] ({3}) are unbalanced. | [{0}] ({1}) と [{2}] ({3}) が対応していません。 | [{0}] ({1}) 与 [{2}] ({3}) 不配对。 | [{0}] ({1})와 [{2}] ({3})의 짝이 맞지 않습니다.
Lompatan ke label yang tidak ada di file ini: {0} | このファイルにないラベルへのジャンプ: {0} | 跳转到本文件中不存在的标签：{0} | 이 파일에 없는 라벨로의 점프: {0}
Jumps to labels not in this file: {0} | このファイルにないラベルへのジャンプ: {0} | 跳转到本文件中不存在的标签：{0} | 이 파일에 없는 라벨로의 점프: {0}
Skrip NScripter/ONScripter (0.txt) | NScripter/ONScripter スクリプト (0.txt) | NScripter/ONScripter 脚本 (0.txt) | NScripter/ONScripter 스크립트 (0.txt)
NScripter/ONScripter script (0.txt) | NScripter/ONScripter スクリプト (0.txt) | NScripter/ONScripter 脚本 (0.txt) | NScripter/ONScripter 스크립트 (0.txt)
Blok *define ditemukan. | *define ブロックが見つかりました。 | 找到了 *define 块。 | *define 블록을 찾았습니다.
*define block found. | *define ブロックが見つかりました。 | 找到了 *define 块。 | *define 블록을 찾았습니다.
Blok *define tidak ada (mungkin bukan 0.txt utama). | *define ブロックがありません (メインの 0.txt ではない可能性があります)。 | 没有 *define 块（可能不是主 0.txt）。 | *define 블록이 없습니다 (메인 0.txt가 아닐 수 있습니다).
No *define block (may not be the main 0.txt). | *define ブロックがありません (メインの 0.txt ではない可能性があります)。 | 没有 *define 块（可能不是主 0.txt）。 | *define 블록이 없습니다 (메인 0.txt가 아닐 수 있습니다).
Skrip QLIE (.s teks) | QLIE スクリプト (.s テキスト) | QLIE 脚本 (.s 文本) | QLIE 스크립트 (.s 텍스트)
QLIE script (.s text) | QLIE スクリプト (.s テキスト) | QLIE 脚本 (.s 文本) | QLIE 스크립트 (.s 텍스트)
Tanda kutip pada baris perintah ^ berpasangan. | ^ コマンド行の引用符は対になっています。 | ^ 命令行中的引号成对。 | ^ 명령 줄의 따옴표가 짝을 이룹니다.
Quotes on ^ command lines are paired. | ^ コマンド行の引用符は対になっています。 | ^ 命令行中的引号成对。 | ^ 명령 줄의 따옴표가 짝을 이룹니다.
Tanda kutip tidak berpasangan di baris {0}. | {0} 行目の引用符が対になっていません。 | 第 {0} 行的引号不成对。 | {0}번째 줄의 따옴표가 짝을 이루지 않습니다.
Unpaired quotes on lines {0}. | {0} 行目の引用符が対になっていません。 | 第 {0} 行的引号不成对。 | {0}번째 줄의 따옴표가 짝을 이루지 않습니다.
Tidak ada label (@@) ganda. | 重複する (@@) ラベルはありません。 | 没有重复的 (@@) 标签。 | 중복된 (@@) 라벨이 없습니다.
No duplicate (@@) labels. | 重複する (@@) ラベルはありません。 | 没有重复的 (@@) 标签。 | 중복된 (@@) 라벨이 없습니다.
Label @@ ganda: {0} | 重複する @@ ラベル: {0} | 重复的 @@ 标签：{0} | 중복된 @@ 라벨: {0}
Duplicate @@ labels: {0} | 重複する @@ ラベル: {0} | 重复的 @@ 标签：{0} | 중복된 @@ 라벨: {0}
Skrip Artemis (.ast) | Artemis スクリプト (.ast) | Artemis 脚本 (.ast) | Artemis 스크립트 (.ast)
Artemis script (.ast) | Artemis スクリプト (.ast) | Artemis 脚本 (.ast) | Artemis 스크립트 (.ast)
Struktur tabel .ast terbaca utuh. | .ast のテーブル構造を完全に解析できました。 | .ast 表结构已完整解析。 | .ast 테이블 구조를 완전히 파싱했습니다.
The .ast table structure parsed fully. | .ast のテーブル構造を完全に解析できました。 | .ast 表结构已完整解析。 | .ast 테이블 구조를 완전히 파싱했습니다.
Struktur .ast tidak terbaca: {0} | .ast の構造を解析できません: {0} | 无法解析 .ast 结构：{0} | .ast 구조를 파싱할 수 없습니다: {0}
Could not parse the .ast structure: {0} | .ast の構造を解析できません: {0} | 无法解析 .ast 结构：{0} | .ast 구조를 파싱할 수 없습니다: {0}
Skrip Artemis (.txt) | Artemis スクリプト (.txt) | Artemis 脚本 (.txt) | Artemis 스크립트 (.txt)
Artemis script (.txt) | Artemis スクリプト (.txt) | Artemis 脚本 (.txt) | Artemis 스크립트 (.txt)
Tag [ ] seimbang di setiap baris tag. | すべてのタグ行で [ ] タグが対応しています。 | 每个标签行的 [ ] 标签均已配对。 | 모든 태그 줄에서 [ ] 태그가 짝이 맞습니다.
[ ] tags are balanced on every tag line. | すべてのタグ行で [ ] タグが対応しています。 | 每个标签行的 [ ] 标签均已配对。 | 모든 태그 줄에서 [ ] 태그가 짝이 맞습니다.
Kurung [ ] tidak seimbang di baris {0}. | {0} 行目の [ ] が対応していません。 | 第 {0} 行的 [ ] 不配对。 | {0}번째 줄의 [ ]가 짝이 맞지 않습니다.
Unbalanced [ ] on lines {0}. | {0} 行目の [ ] が対応していません。 | 第 {0} 行的 [ ] 不配对。 | {0}번째 줄의 [ ]가 짝이 맞지 않습니다.
Skrip CatSystem 2 (.cst) | CatSystem 2 スクリプト (.cst) | CatSystem 2 脚本 (.cst) | CatSystem 2 스크립트 (.cst)
CatSystem 2 script (.cst) | CatSystem 2 スクリプト (.cst) | CatSystem 2 脚本 (.cst) | CatSystem 2 스크립트 (.cst)
Jumlah string | 文字列数 | 字符串数量 | 문자열 수
String count | 文字列数 | 字符串数量 | 문자열 수
Header dan tabel string CatScene valid. | CatScene のヘッダーと文字列テーブルは有効です。 | CatScene 头部和字符串表有效。 | CatScene 헤더와 문자열 테이블이 올바릅니다.
CatScene header and string table are valid. | CatScene のヘッダーと文字列テーブルは有効です。 | CatScene 头部和字符串表有效。 | CatScene 헤더와 문자열 테이블이 올바릅니다.
Panjang data cocok dengan header. | データ長がヘッダーと一致しています。 | 数据长度与头部一致。 | 데이터 길이가 헤더와 일치합니다.
Data length matches the header. | データ長がヘッダーと一致しています。 | 数据长度与头部一致。 | 데이터 길이가 헤더와 일치합니다.
Panjang data di header tidak cocok dengan isi. | ヘッダーのデータ長が内容と一致しません。 | 头部中的数据长度与内容不一致。 | 헤더의 데이터 길이가 내용과 일치하지 않습니다.
The data length in the header does not match the content. | ヘッダーのデータ長が内容と一致しません。 | 头部中的数据长度与内容不一致。 | 헤더의 데이터 길이가 내용과 일치하지 않습니다.
Tidak terenkripsi. | 暗号化されていません。 | 未加密。 | 암호화되어 있지 않습니다.
Not encrypted. | 暗号化されていません。 | 未加密。 | 암호화되어 있지 않습니다.
Struktur CatScene tidak valid: {0} | CatScene の構造が無効です: {0} | CatScene 结构无效：{0} | CatScene 구조가 올바르지 않습니다: {0}
Invalid CatScene structure: {0} | CatScene の構造が無効です: {0} | CatScene 结构无效：{0} | CatScene 구조가 올바르지 않습니다: {0}
Scene SiglusEngine (.dat, hasil bongkar Scene.pck) | SiglusEngine シーン (.dat、展開した Scene.pck から) | SiglusEngine 场景 (.dat，来自已解包的 Scene.pck) | SiglusEngine 씬 (.dat, 풀어낸 Scene.pck에서)
SiglusEngine scene (.dat, from an unpacked Scene.pck) | SiglusEngine シーン (.dat、展開した Scene.pck から) | SiglusEngine 场景 (.dat，来自已解包的 Scene.pck) | SiglusEngine 씬 (.dat, 풀어낸 Scene.pck에서)
Kepala scene Siglus valid. | Siglus シーンのヘッダーは有効です。 | Siglus 场景头部有效。 | Siglus 씬 헤더가 올바릅니다.
Siglus scene header is valid. | Siglus シーンのヘッダーは有効です。 | Siglus 场景头部有效。 | Siglus 씬 헤더가 올바릅니다.
File besar; statistik string tidak dihitung. Ekspor Teks tetap bisa dipakai. | ファイルが大きいため、文字列の統計は省略しました。テキストのエクスポートは引き続き使えます。 | 文件较大，已跳过字符串统计。仍可使用导出文本。 | 파일이 커서 문자열 통계를 건너뛰었습니다. 텍스트 내보내기는 계속 사용할 수 있습니다.
Large file; string statistics were skipped. Export Text still works. | ファイルが大きいため、文字列の統計は省略しました。テキストのエクスポートは引き続き使えます。 | 文件较大，已跳过字符串统计。仍可使用导出文本。 | 파일이 커서 문자열 통계를 건너뛰었습니다. 텍스트 내보내기는 계속 사용할 수 있습니다.
Struktur scene Siglus tidak valid (terpotong/rusak). | Siglus シーンの構造が無効です (途切れているか破損しています)。 | Siglus 场景结构无效（被截断或已损坏）。 | Siglus 씬 구조가 올바르지 않습니다 (잘렸거나 손상됨).
Invalid Siglus scene structure (truncated/corrupt). | Siglus シーンの構造が無効です (途切れているか破損しています)。 | Siglus 场景结构无效（被截断或已损坏）。 | Siglus 씬 구조가 올바르지 않습니다 (잘렸거나 손상됨).
Kepala dan tabel string scene valid. | シーンのヘッダーと文字列テーブルは有効です。 | 场景头部和字符串表有效。 | 씬 헤더와 문자열 테이블이 올바릅니다.
Scene header and string table are valid. | シーンのヘッダーと文字列テーブルは有効です。 | 场景头部和字符串表有效。 | 씬 헤더와 문자열 테이블이 올바릅니다.
Teks dipilih dengan menyaring string non-ASCII, bukan analisis bytecode. | テキストはバイトコード解析ではなく、非 ASCII 文字列の絞り込みで抽出しています。 | 文本是通过筛选非 ASCII 字符串提取的，而非字节码分析。 | 텍스트는 바이트코드 분석이 아니라 비 ASCII 문자열을 걸러서 선택합니다.
Texts are picked by filtering non-ASCII strings, not by bytecode analysis. | テキストはバイトコード解析ではなく、非 ASCII 文字列の絞り込みで抽出しています。 | 文本是通过筛选非 ASCII 字符串提取的，而非字节码分析。 | 텍스트는 바이트코드 분석이 아니라 비 ASCII 문자열을 걸러서 선택합니다.
Skrip Ren'Py terkompilasi (.rpyc) | コンパイル済み Ren'Py スクリプト (.rpyc) | 已编译的 Ren'Py 脚本 (.rpyc) | 컴파일된 Ren'Py 스크립트 (.rpyc)
Compiled Ren'Py script (.rpyc) | コンパイル済み Ren'Py スクリプト (.rpyc) | 已编译的 Ren'Py 脚本 (.rpyc) | 컴파일된 Ren'Py 스크립트 (.rpyc)
Versi kontainer | コンテナのバージョン | 容器版本 | 컨테이너 버전
Container version | コンテナのバージョン | 容器版本 | 컨테이너 버전
RENPY RPC2 | RENPY RPC2 | RENPY RPC2 | RENPY RPC2
RPC1 / zlib | RPC1 / zlib | RPC1 / zlib | RPC1 / zlib
Jumlah statement teratas | 最上位のステートメント数 | 顶层语句数 | 최상위 문 수
Top-level statements | 最上位のステートメント数 | 顶层语句数 | 최상위 문 수
Kontainer dan pickle berhasil dibaca. | コンテナと pickle を正常に読み込みました。 | 已成功读取容器和 pickle。 | 컨테이너와 pickle을 정상적으로 읽었습니다.
Container and pickle were read successfully. | コンテナと pickle を正常に読み込みました。 | 已成功读取容器和 pickle。 | 컨테이너와 pickle을 정상적으로 읽었습니다.
Baris hasil dekompilasi | 逆コンパイルされた行数 | 反编译的行数 | 디컴파일된 줄 수
Decompiled lines | 逆コンパイルされた行数 | 反编译的行数 | 디컴파일된 줄 수
Bisa didekompilasi ke .rpy lewat Alat Skrip Ren'Py. | Ren'Py スクリプトツールで .rpy に逆コンパイルできます。 | 可通过 Ren'Py 脚本工具反编译为 .rpy。 | Ren'Py 스크립트 도구로 .rpy로 디컴파일할 수 있습니다.
Can be decompiled to .rpy via the Ren'Py script tool. | Ren'Py スクリプトツールで .rpy に逆コンパイルできます。 | 可通过 Ren'Py 脚本工具反编译为 .rpy。 | Ren'Py 스크립트 도구로 .rpy로 디컴파일할 수 있습니다.
Terbaca, tapi gagal didekompilasi: {0} | 読み込めましたが、逆コンパイルに失敗しました: {0} | 可以读取，但反编译失败：{0} | 읽을 수는 있지만 디컴파일에 실패했습니다: {0}
Readable, but decompilation failed: {0} | 読み込めましたが、逆コンパイルに失敗しました: {0} | 可以读取，但反编译失败：{0} | 읽을 수는 있지만 디컴파일에 실패했습니다: {0}
Bukan rpyc yang valid atau rusak: {0} | 有効な rpyc ではないか、破損しています: {0} | 不是有效的 rpyc 或已损坏：{0} | 올바른 rpyc가 아니거나 손상되었습니다: {0}
Not a valid rpyc or corrupt: {0} | 有効な rpyc ではないか、破損しています: {0} | 不是有效的 rpyc 或已损坏：{0} | 올바른 rpyc가 아니거나 손상되었습니다: {0}
Data JSON | JSON データ | JSON 数据 | JSON 데이터
JSON data | JSON データ | JSON 数据 | JSON 데이터
JSON valid. | 有効な JSON です。 | JSON 有效。 | 올바른 JSON입니다.
Valid JSON. | 有効な JSON です。 | JSON 有效。 | 올바른 JSON입니다.
objek ({0} kunci) | オブジェクト ({0} 個のキー) | 对象（{0} 个键） | 객체 (키 {0}개)
object ({0} keys) | オブジェクト ({0} 個のキー) | 对象（{0} 个键） | 객체 (키 {0}개)
array ({0} elemen) | 配列 ({0} 個の要素) | 数组（{0} 个元素） | 배열 (요소 {0}개)
array ({0} items) | 配列 ({0} 個の要素) | 数组（{0} 个元素） | 배열 (요소 {0}개)
nilai tunggal | 単一の値 | 单个值 | 단일 값
single value | 単一の値 | 单个值 | 단일 값
Data RPG Maker | RPG Maker データ | RPG Maker 数据 | RPG Maker 데이터
Struktur data RPG Maker dikenali dan bisa diekspor lewat Terjemahan Data. | RPG Maker のデータ構造を認識しました。データ翻訳からエクスポートできます。 | 已识别 RPG Maker 数据结构，可通过数据翻译导出。 | RPG Maker 데이터 구조를 인식했습니다. 데이터 번역에서 내보낼 수 있습니다.
"""
    const val T11 = """
RPG Maker data structure recognised; exportable via Data Translation. | RPG Maker のデータ構造を認識しました。データ翻訳からエクスポートできます。 | 已识别 RPG Maker 数据结构，可通过数据翻译导出。 | RPG Maker 데이터 구조를 인식했습니다. 데이터 번역에서 내보낼 수 있습니다.
JSON valid tetapi struktur RPG Maker tidak dikenali: {0} | JSON は有効ですが、RPG Maker の構造を認識できません: {0} | JSON 有效，但无法识别 RPG Maker 结构：{0} | JSON은 올바르지만 RPG Maker 구조를 인식하지 못했습니다: {0}
Valid JSON but the RPG Maker structure is not recognised: {0} | JSON は有効ですが、RPG Maker の構造を認識できません: {0} | JSON 有效，但无法识别 RPG Maker 结构：{0} | JSON은 올바르지만 RPG Maker 구조를 인식하지 못했습니다: {0}
Kunci enkripsi aset | アセットの暗号化キー | 资源加密密钥 | 에셋 암호화 키
Asset encryption key | アセットの暗号化キー | 资源加密密钥 | 에셋 암호화 키
Gambar terenkripsi: {0}, audio terenkripsi: {1}. | 暗号化された画像: {0}、暗号化された音声: {1}。 | 已加密图片：{0}，已加密音频：{1}。 | 암호화된 이미지: {0}, 암호화된 오디오: {1}.
Encrypted images: {0}, encrypted audio: {1}. | 暗号化された画像: {0}、暗号化された音声: {1}。 | 已加密图片：{0}，已加密音频：{1}。 | 암호화된 이미지: {0}, 암호화된 오디오: {1}.
Judul game | ゲームタイトル | 游戏标题 | 게임 제목
Game title | ゲームタイトル | 游戏标题 | 게임 제목
Arsip terpotong di posisi {0} | アーカイブが {0} の位置で途切れています | 压缩包在位置 {0} 处被截断 | 아카이브가 {0} 위치에서 잘렸습니다
Arsip terpotong | アーカイブが途切れています | 压缩包被截断 | 아카이브가 잘렸습니다
Tidak ada teks Itufuru ditemukan | Itufuru のテキストが見つかりません | 未找到 Itufuru 文本 | Itufuru 텍스트를 찾을 수 없습니다
karakter tak terduga | 予期しない文字 | 意外的字符 | 예기치 않은 문자
JSON tidak valid ({0}) di baris {1}, kolom {2} | JSON が無効です ({0}): {1} 行目、{2} 列目 | JSON 无效（{0}）：第 {1} 行，第 {2} 列 | JSON이 올바르지 않습니다 ({0}): {1}번째 줄, {2}번째 열
terlalu dalam | ネストが深すぎます | 嵌套过深 | 중첩이 너무 깊습니다
data berakhir terlalu cepat | データが早く終わりすぎています | 数据过早结束 | 데이터가 너무 일찍 끝났습니다
literal salah | リテラルが不正です | 字面量错误 | 리터럴이 올바르지 않습니다
angka kosong | 数値が空です | 数字为空 | 숫자가 비어 있습니다
string tidak ditutup | 文字列が閉じられていません | 字符串未闭合 | 문자열이 닫히지 않았습니다
escape terpotong | エスケープが途切れています | 转义序列被截断 | 이스케이프가 잘렸습니다
escape \u terpotong | \u エスケープが途切れています | \u 转义序列被截断 | \u 이스케이프가 잘렸습니다
escape \u salah | \u エスケープが不正です | \u 转义序列错误 | \u 이스케이프가 올바르지 않습니다
escape \{0} tidak dikenal | 不明なエスケープ \{0} です | 未知的转义序列 \{0} | 알 수 없는 이스케이프 \{0}입니다
array tidak ditutup | 配列が閉じられていません | 数组未闭合 | 배열이 닫히지 않았습니다
diharapkan , atau ] | , または ] が必要です | 应为 , 或 ] | , 또는 ]가 필요합니다
diharapkan nama kunci | キー名が必要です | 应为键名 | 키 이름이 필요합니다
objek tidak ditutup | オブジェクトが閉じられていません | 对象未闭合 | 객체가 닫히지 않았습니다
diharapkan , atau } | , または } が必要です | 应为 , 或 } | , 또는 }가 필요합니다
Kirikiri XP3 | Kirikiri XP3 | Kirikiri XP3 | Kirikiri XP3
Tidak ada file terenkripsi yang dikenali di folder ini. | このフォルダーに、認識できる暗号化ファイルはありません。 | 此文件夹中没有可识别的加密文件。 | 이 폴더에 인식된 암호화 파일이 없습니다.
No recognised encrypted files in this folder. | このフォルダーに、認識できる暗号化ファイルはありません。 | 此文件夹中没有可识别的加密文件。 | 이 폴더에 인식된 암호화 파일이 없습니다.
{0}: Scene.pck Siglus ini tidak memakai kunci exe. | {0}: この Siglus Scene.pck は exe キーを使用していません。 | {0}：此 Siglus Scene.pck 不使用 exe 密钥。 | {0}: 이 Siglus Scene.pck는 exe 키를 사용하지 않습니다.
{0}: this Siglus Scene.pck uses no exe key. | {0}: この Siglus Scene.pck は exe キーを使用していません。 | {0}：此 Siglus Scene.pck 不使用 exe 密钥。 | {0}: 이 Siglus Scene.pck는 exe 키를 사용하지 않습니다.
{0}: kunci Siglus tidak ditemukan (data scene tidak cocok dengan pola yang dikenal). | {0}: Siglus キーが見つかりません (シーンデータが既知のパターンと一致しません)。 | {0}：未找到 Siglus 密钥（场景数据与已知模式不匹配）。 | {0}: Siglus 키를 찾을 수 없습니다 (씬 데이터가 알려진 패턴과 일치하지 않음).
{0}: Siglus key not found (scene data does not match the known pattern). | {0}: Siglus キーが見つかりません (シーンデータが既知のパターンと一致しません)。 | {0}：未找到 Siglus 密钥（场景数据与已知模式不匹配）。 | {0}: Siglus 키를 찾을 수 없습니다 (씬 데이터가 알려진 패턴과 일치하지 않음).
Dipulihkan dari data Scene.pck: {0} dari {1} scene berhasil didekripsi dengan kunci ini. | Scene.pck のデータから復元しました: {1} 個中 {0} 個のシーンがこのキーで正しく復号できます。 | 已从 Scene.pck 数据中恢复：{1} 个场景中有 {0} 个可用此密钥正确解密。 | Scene.pck 데이터에서 복원했습니다: 씬 {1}개 중 {0}개가 이 키로 올바르게 복호화됩니다.
Recovered from the Scene.pck data: {0} of {1} scenes decrypt correctly with this key. | Scene.pck のデータから復元しました: {1} 個中 {0} 個のシーンがこのキーで正しく復号できます。 | 已从 Scene.pck 数据中恢复：{1} 个场景中有 {0} 个可用此密钥正确解密。 | Scene.pck 데이터에서 복원했습니다: 씬 {1}개 중 {0}개가 이 키로 올바르게 복호화됩니다.
Gameexe.dat juga cocok dengan kunci ini. | Gameexe.dat もこのキーと一致します。 | Gameexe.dat 也与此密钥匹配。 | Gameexe.dat도 이 키와 일치합니다.
Gameexe.dat matches this key too. | Gameexe.dat もこのキーと一致します。 | Gameexe.dat 也与此密钥匹配。 | Gameexe.dat도 이 키와 일치합니다.
Gameexe.dat di folder ini tidak cocok dengan kunci ini. | このフォルダーの Gameexe.dat はこのキーと一致しません。 | 此文件夹中的 Gameexe.dat 与此密钥不匹配。 | 이 폴더의 Gameexe.dat는 이 키와 일치하지 않습니다.
Gameexe.dat here does not match this key. | このフォルダーの Gameexe.dat はこのキーと一致しません。 | 此文件夹中的 Gameexe.dat 与此密钥不匹配。 | 이 폴더의 Gameexe.dat는 이 키와 일치하지 않습니다.
Kunci tidak tersimpan utuh di SiglusEngine.exe (dirakit saat game berjalan), jadi dicari dari data scene. | キーは SiglusEngine.exe に完全な形では保存されていない (実行時に組み立てられる) ため、シーンデータから求めました。 | 密钥并未完整存储在 SiglusEngine.exe 中（运行时才组装），因此从场景数据中推导。 | 키는 SiglusEngine.exe에 온전히 저장되어 있지 않아(실행 시 조립됨) 씬 데이터에서 찾았습니다.
The key is not stored whole in SiglusEngine.exe (it is assembled at run time), so it is derived from the scene data. | キーは SiglusEngine.exe に完全な形では保存されていない (実行時に組み立てられる) ため、シーンデータから求めました。 | 密钥并未完整存储在 SiglusEngine.exe 中（运行时才组装），因此从场景数据中推导。 | 키는 SiglusEngine.exe에 온전히 저장되어 있지 않아(실행 시 조립됨) 씬 데이터에서 찾았습니다.
Kunci exe (16 byte) | exe キー (16 バイト) | exe 密钥 (16 字节) | exe 키 (16바이트)
Exe key (16 bytes) | exe キー (16 バイト) | exe 密钥 (16 字节) | exe 키 (16바이트)
Gameexe.dat Siglus terenkripsi, tetapi tidak ada Scene.pck di folder ini untuk mencari kuncinya. | Siglus の Gameexe.dat は暗号化されていますが、キーを求めるための Scene.pck がこのフォルダーにありません。 | Siglus 的 Gameexe.dat 已加密，但此文件夹中没有可用于推导密钥的 Scene.pck。 | Siglus의 Gameexe.dat가 암호화되어 있지만, 키를 찾을 Scene.pck가 이 폴더에 없습니다.
Siglus Gameexe.dat is encrypted, but there is no Scene.pck here to derive the key from. | Siglus の Gameexe.dat は暗号化されていますが、キーを求めるための Scene.pck がこのフォルダーにありません。 | Siglus 的 Gameexe.dat 已加密，但此文件夹中没有可用于推导密钥的 Scene.pck。 | Siglus의 Gameexe.dat가 암호화되어 있지만, 키를 찾을 Scene.pck가 이 폴더에 없습니다.
Kunci enkripsi aset (16 byte) | アセットの暗号化キー (16 バイト) | 资源加密密钥 (16 字节) | 에셋 암호화 키 (16바이트)
Asset encryption key (16 bytes) | アセットの暗号化キー (16 バイト) | 资源加密密钥 (16 字节) | 에셋 암호화 키 (16바이트)
Tertulis di System.json (encryptionKey) dan cocok dengan header PNG terenkripsi ({0}). | System.json (encryptionKey) に記載があり、暗号化された PNG ヘッダー ({0}) と一致します。 | 记录在 System.json (encryptionKey) 中，且与加密 PNG 头部 ({0}) 匹配。 | System.json (encryptionKey)에 기록되어 있으며 암호화된 PNG 헤더 ({0})와 일치합니다.
Stored in System.json (encryptionKey) and matches the encrypted PNG header ({0}). | System.json (encryptionKey) に記載があり、暗号化された PNG ヘッダー ({0}) と一致します。 | 记录在 System.json (encryptionKey) 中，且与加密 PNG 头部 ({0}) 匹配。 | System.json (encryptionKey)에 기록되어 있으며 암호화된 PNG 헤더 ({0})와 일치합니다.
Diturunkan dari header PNG terenkripsi; ini kunci yang benar-benar dipakai aset. | 暗号化された PNG ヘッダーから求めたキーで、アセットが実際に使用しているキーです。 | 由加密 PNG 头部推导而来，这是资源实际使用的密钥。 | 암호화된 PNG 헤더에서 구한 키로, 에셋이 실제로 사용하는 키입니다.
Derived from an encrypted PNG header; this is the key the assets really use. | 暗号化された PNG ヘッダーから求めたキーで、アセットが実際に使用しているキーです。 | 由加密 PNG 头部推导而来，这是资源实际使用的密钥。 | 암호화된 PNG 헤더에서 구한 키로, 에셋이 실제로 사용하는 키입니다.
System.json ({0}) memuat kunci berbeda ({1}); kunci dari header aset yang dipakai. | System.json ({0}) には異なるキー ({1}) が記載されています。アセットヘッダーから求めたキーを使用します。 | System.json ({0}) 中记录的是另一个密钥 ({1})；将使用由资源头部得到的密钥。 | System.json ({0})에 다른 키 ({1})가 들어 있습니다. 에셋 헤더에서 구한 키를 사용합니다.
System.json ({0}) holds a different key ({1}); the key from the asset header is used. | System.json ({0}) には異なるキー ({1}) が記載されています。アセットヘッダーから求めたキーを使用します。 | System.json ({0}) 中记录的是另一个密钥 ({1})；将使用由资源头部得到的密钥。 | System.json ({0})에 다른 키 ({1})가 들어 있습니다. 에셋 헤더에서 구한 키를 사용합니다.
Tertulis di System.json (encryptionKey); belum bisa dicocokkan dengan header PNG (tidak ada .rpgmvp/.png_). | System.json (encryptionKey) に記載がありますが、PNG ヘッダーとの照合はできませんでした (.rpgmvp/.png_ がありません)。 | 记录在 System.json (encryptionKey) 中；无法与 PNG 头部核对（没有 .rpgmvp/.png_）。 | System.json (encryptionKey)에 기록되어 있지만 PNG 헤더와 대조할 수 없었습니다 (.rpgmvp/.png_ 없음).
Stored in System.json (encryptionKey); could not be checked against a PNG header (no .rpgmvp/.png_). | System.json (encryptionKey) に記載がありますが、PNG ヘッダーとの照合はできませんでした (.rpgmvp/.png_ がありません)。 | 记录在 System.json (encryptionKey) 中；无法与 PNG 头部核对（没有 .rpgmvp/.png_）。 | System.json (encryptionKey)에 기록되어 있지만 PNG 헤더와 대조할 수 없었습니다 (.rpgmvp/.png_ 없음).
System.json tidak memuat kunci; kunci diturunkan dari header PNG terenkripsi. | System.json にキーがないため、暗号化された PNG ヘッダーから求めました。 | System.json 中没有密钥；已由加密 PNG 头部推导。 | System.json에 키가 없어 암호화된 PNG 헤더에서 구했습니다.
System.json has no key; derived from an encrypted PNG header. | System.json にキーがないため、暗号化された PNG ヘッダーから求めました。 | System.json 中没有密钥；已由加密 PNG 头部推导。 | System.json에 키가 없어 암호화된 PNG 헤더에서 구했습니다.
Ada {0} aset RPGM terenkripsi, tetapi kunci tidak bisa ditemukan (tidak ada System.json dengan encryptionKey dan tidak ada gambar PNG terenkripsi). | 暗号化された RPGM アセットが {0} 個ありますが、キーが見つかりません (encryptionKey を含む System.json も、暗号化された PNG 画像もありません)。 | 发现 {0} 个加密的 RPGM 资源，但找不到密钥（没有含 encryptionKey 的 System.json，也没有加密的 PNG 图片）。 | 암호화된 RPGM 에셋이 {0}개 있지만 키를 찾을 수 없습니다 (encryptionKey가 있는 System.json도, 암호화된 PNG 이미지도 없음).
{0} encrypted RPGM assets found, but no key could be found (no System.json with encryptionKey and no encrypted PNG). | 暗号化された RPGM アセットが {0} 個ありますが、キーが見つかりません (encryptionKey を含む System.json も、暗号化された PNG 画像もありません)。 | 发现 {0} 个加密的 RPGM 资源，但找不到密钥（没有含 encryptionKey 的 System.json，也没有加密的 PNG 图片）。 | 암호화된 RPGM 에셋이 {0}개 있지만 키를 찾을 수 없습니다 (encryptionKey가 있는 System.json도, 암호화된 PNG 이미지도 없음).
{0}: {1} tidak memakai kunci XOR. | {0}: {1} は XOR キーを使用していません。 | {0}：{1} 不使用 XOR 密钥。 | {0}: {1}은(는) XOR 키를 사용하지 않습니다.
{0}: {1} uses no XOR key. | {0}: {1} は XOR キーを使用していません。 | {0}：{1} 不使用 XOR 密钥。 | {0}: {1}은(는) XOR 키를 사용하지 않습니다.
Kunci XOR 1 byte | 1 バイト XOR キー | 1 字节 XOR 密钥 | 1바이트 XOR 키
1-byte XOR key | 1 バイト XOR キー | 1 字节 XOR 密钥 | 1바이트 XOR 키
Ditebak dari header isi arsip yang diketahui (kandidat dari suara terbanyak). {0} entri. | アーカイブ内の既知のファイルヘッダーから推測しました (多数決による候補)。{0} 件。 | 根据压缩包内已知文件头推测（多数票候选）。{0} 项。 | 아카이브 안의 알려진 파일 헤더로 추측했습니다 (다수결 후보). {0}개 항목.
Guessed from known file headers inside the archive (majority vote). {0} entries. | アーカイブ内の既知のファイルヘッダーから推測しました (多数決による候補)。{0} 件。 | 根据压缩包内已知文件头推测（多数票候选）。{0} 项。 | 아카이브 안의 알려진 파일 헤더로 추측했습니다 (다수결 후보). {0}개 항목.
(bawaan, tidak ada resource V_CODE2 di exe) | (デフォルト。exe に V_CODE2 リソースがありません) | （默认值，exe 中没有 V_CODE2 资源） | (기본값, exe에 V_CODE2 리소스 없음)
(default, no V_CODE2 resource in exe) | (デフォルト。exe に V_CODE2 リソースがありません) | （默认值，exe 中没有 V_CODE2 资源） | (기본값, exe에 V_CODE2 리소스 없음)
, nama meragukan | 、名前が不確か | ，名称存疑 | , 이름이 불확실
, names doubtful | 、名前が不確か | ，名称存疑 | , 이름이 불확실
Password arsip INT | INT アーカイブのパスワード | INT 压缩包密码 | INT 아카이브 비밀번호
INT archive password | INT アーカイブのパスワード | INT 压缩包密码 | INT 아카이브 비밀번호
Password dari resource V_CODE2 pada exe. Nama file di indeks {0} arsip .int terenkripsi dibaca dengan password ini. | exe の V_CODE2 リソースから得たパスワードです。暗号化された {0} 個の .int アーカイブのインデックス内のファイル名を、このパスワードで読み取りました。 | 来自 exe 的 V_CODE2 资源的密码。已用它读取 {0} 个加密 .int 压缩包索引中的文件名。 | exe의 V_CODE2 리소스에서 가져온 비밀번호입니다. 암호화된 .int 아카이브 {0}개의 인덱스에 있는 파일 이름을 이 비밀번호로 읽었습니다.
Password from the exe's V_CODE2 resource. File names in {0} encrypted .int archive(s) read with it. | exe の V_CODE2 リソースから得たパスワードです。暗号化された {0} 個の .int アーカイブのインデックス内のファイル名を、このパスワードで読み取りました。 | 来自 exe 的 V_CODE2 资源的密码。已用它读取 {0} 个加密 .int 压缩包索引中的文件名。 | exe의 V_CODE2 리소스에서 가져온 비밀번호입니다. 암호화된 .int 아카이브 {0}개의 인덱스에 있는 파일 이름을 이 비밀번호로 읽었습니다.
Arsip .int terenkripsi ditemukan tetapi password tidak bisa dipulihkan. Sertakan file .exe game di folder yang sama. | 暗号化された .int アーカイブが見つかりましたが、パスワードを復元できませんでした。ゲームの .exe を同じフォルダーに入れてください。 | 发现加密的 .int 压缩包，但无法恢复密码。请把游戏的 .exe 放在同一文件夹中。 | 암호화된 .int 아카이브를 찾았지만 비밀번호를 복구할 수 없습니다. 게임의 .exe를 같은 폴더에 넣어 주세요.
Encrypted .int archives found but the password could not be recovered. Include the game's .exe in the folder. | 暗号化された .int アーカイブが見つかりましたが、パスワードを復元できませんでした。ゲームの .exe を同じフォルダーに入れてください。 | 发现加密的 .int 压缩包，但无法恢复密码。请把游戏的 .exe 放在同一文件夹中。 | 암호화된 .int 아카이브를 찾았지만 비밀번호를 복구할 수 없습니다. 게임의 .exe를 같은 폴더에 넣어 주세요.
Password arsip NOA | NOA アーカイブのパスワード | NOA 压缩包密码 | NOA 아카이브 비밀번호
NOA archive password | NOA アーカイブのパスワード | NOA 压缩包密码 | NOA 아카이브 비밀번호
Password dari resource IDR_COTOMI pada exe. | exe の IDR_COTOMI リソースから得たパスワードです。 | 来自 exe 的 IDR_COTOMI 资源的密码。 | exe의 IDR_COTOMI 리소스에서 가져온 비밀번호입니다.
Password from the exe's IDR_COTOMI resource. | exe の IDR_COTOMI リソースから得たパスワードです。 | 来自 exe 的 IDR_COTOMI 资源的密码。 | exe의 IDR_COTOMI 리소스에서 가져온 비밀번호입니다.
Arsip NOA ditemukan tetapi password tidak ditemukan di exe (mungkin tidak terenkripsi, atau exe tidak ada/terbungkus). Arsip tak terenkripsi dibuka tanpa password. | NOA アーカイブが見つかりましたが、exe にパスワードがありません (暗号化されていないか、exe がないかパックされている可能性があります)。暗号化されていないアーカイブはパスワードなしで開けます。 | 发现 NOA 压缩包，但 exe 中没有密码（可能未加密，或 exe 缺失/已加壳）。未加密的压缩包无需密码即可打开。 | NOA 아카이브를 찾았지만 exe에 비밀번호가 없습니다 (암호화되지 않았거나, exe가 없거나 패킹되었을 수 있습니다). 암호화되지 않은 아카이브는 비밀번호 없이 열립니다.
NOA archives found but no password in the exe (maybe not encrypted, or the exe is missing/packed). Unencrypted archives open without a password. | NOA アーカイブが見つかりましたが、exe にパスワードがありません (暗号化されていないか、exe がないかパックされている可能性があります)。暗号化されていないアーカイブはパスワードなしで開けます。 | 发现 NOA 压缩包，但 exe 中没有密码（可能未加密，或 exe 缺失/已加壳）。未加密的压缩包无需密码即可打开。 | NOA 아카이브를 찾았지만 exe에 비밀번호가 없습니다 (암호화되지 않았거나, exe가 없거나 패킹되었을 수 있습니다). 암호화되지 않은 아카이브는 비밀번호 없이 열립니다.
Laporan Pencari Kunci | キーファインダーレポート | 密钥查找报告 | 키 파인더 보고서
Key Finder Report | キーファインダーレポート | 密钥查找报告 | 키 파인더 보고서
File terenkripsi ( | 暗号化ファイル ( | 加密文件 ( | 암호화된 파일 (
Encrypted files ( | 暗号化ファイル ( | 加密文件 ( | 암호화된 파일 (
Bukan skrip scn | scn スクリプトではありません | 不是 scn 脚本 | scn 스크립트가 아닙니다
Format gambar tidak didukung | サポートされていない画像形式です | 不支持的图片格式 | 지원하지 않는 이미지 형식입니다
Gambar tidak bisa dibaca | 画像を読み込めません | 无法读取图片 | 이미지를 읽을 수 없습니다
Gagal menyandikan gambar | 画像のエンコードに失敗しました | 图片编码失败 | 이미지 인코딩에 실패했습니다
Memori tidak cukup untuk gambar ini (coba kecilkan ukurannya) | この画像にはメモリが足りません (サイズを小さくしてください) | 内存不足，无法处理此图片（请缩小尺寸） | 이 이미지를 처리할 메모리가 부족합니다 (크기를 줄여 보세요)
Bukan teks {0} yang valid (pilih penyandian lain atau aktifkan Ganti karakter rusak) | 有効な {0} テキストではありません (別のエンコードを選ぶか、「壊れた文字を置換」を有効にしてください) | 不是有效的 {0} 文本（请选择其他编码，或启用“替换损坏字符”） | 올바른 {0} 텍스트가 아닙니다 (다른 인코딩을 선택하거나 '깨진 문자 바꾸기'를 켜세요)
Bukan teks UTF-8 yang valid | 有効な UTF-8 テキストではありません | 不是有效的 UTF-8 文本 | 올바른 UTF-8 텍스트가 아닙니다
Ada karakter yang tidak ada di {0} (aktifkan Ganti karakter rusak untuk menggantinya dengan ?) | {0} にない文字があります (「壊れた文字を置換」を有効にすると ? に置き換えられます) | 有 {0} 中不存在的字符（启用“替换损坏字符”后将替换为 ?） | {0}에 없는 문자가 있습니다 ('깨진 문자 바꾸기'를 켜면 ?로 바뀝니다)
File terlalu besar untuk file teks | ファイルがテキストファイルとしては大きすぎます | 文件过大，无法作为文本文件处理 | 텍스트 파일로 쓰기에는 파일이 너무 큽니다
OGG (Opus) memerlukan Android 10 ke atas | OGG (Opus) には Android 10 以降が必要です | OGG (Opus) 需要 Android 10 或更高版本 | OGG (Opus)에는 Android 10 이상이 필요합니다
Perangkat ini tidak punya encoder {0} | この端末には {0} エンコーダーがありません | 此设备没有 {0} 编码器 | 이 기기에는 {0} 인코더가 없습니다
Encoder menolak pengaturan ini: {0} | エンコーダーがこの設定を拒否しました: {0} | 编码器拒绝了此设置：{0} | 인코더가 이 설정을 거부했습니다: {0}
Gagal membuat file Output: {0} | 出力ファイルを作成できませんでした: {0} | 创建输出文件失败：{0} | 출력 파일을 만들지 못했습니다: {0}
Encoder tidak merespons | エンコーダーが応答しません | 编码器无响应 | 인코더가 응답하지 않습니다
Buffer encoder tidak tersedia | エンコーダーのバッファーが利用できません | 编码器缓冲区不可用 | 인코더 버퍼를 사용할 수 없습니다
Buffer encoder terlalu kecil | エンコーダーのバッファーが小さすぎます | 编码器缓冲区过小 | 인코더 버퍼가 너무 작습니다
Audio kosong atau tidak bisa disandikan | 音声が空か、エンコードできません | 音频为空或无法编码 | 오디오가 비어 있거나 인코딩할 수 없습니다
Format file tidak didukung ({0}) | サポートされていないファイル形式です ({0}) | 不支持的文件格式 ({0}) | 지원하지 않는 파일 형식입니다 ({0})
Tidak ada trek audio di file ini | このファイルに音声トラックがありません | 此文件中没有音轨 | 이 파일에 오디오 트랙이 없습니다
Trek audio tidak dikenal | 不明な音声トラックです | 未知的音轨 | 알 수 없는 오디오 트랙입니다
Perangkat tidak bisa mendekode {0} | この端末では {0} をデコードできません | 此设备无法解码 {0} | 이 기기에서는 {0}을(를) 디코딩할 수 없습니다
Gagal memulai decoder {0}: {1} | {0} デコーダーの起動に失敗しました: {1} | 启动 {0} 解码器失败：{1} | {0} 디코더를 시작하지 못했습니다: {1}
Buffer decoder tidak tersedia | デコーダーのバッファーが利用できません | 解码器缓冲区不可用 | 디코더 버퍼를 사용할 수 없습니다
Tidak ada data audio yang bisa didekode | デコードできる音声データがありません | 没有可解码的音频数据 | 디코딩할 오디오 데이터가 없습니다
Shader GL gagal dikompilasi | GL シェーダーのコンパイルに失敗しました | GL 着色器编译失败 | GL 셰이더 컴파일에 실패했습니다
EGL tidak bisa diinisialisasi | EGL を初期化できません | 无法初始化 EGL | EGL을 초기화할 수 없습니다
Konfigurasi EGL tidak ditemukan | EGL の設定が見つかりません | 未找到 EGL 配置 | EGL 구성을 찾을 수 없습니다
EGL gagal dipakai | EGL の使用に失敗しました | EGL 使用失败 | EGL 사용에 실패했습니다
uniform mat4 uMat;\nattribute vec4 aPos;\nattribute vec4 aTex;\nvarying vec2 vTex;\nvoid main() { gl_Position = aPos; vTex = (uMat * aTex).xy; } | uniform mat4 uMat;\nattribute vec4 aPos;\nattribute vec4 aTex;\nvarying vec2 vTex;\nvoid main() { gl_Position = aPos; vTex = (uMat * aTex).xy; } | uniform mat4 uMat;\nattribute vec4 aPos;\nattribute vec4 aTex;\nvarying vec2 vTex;\nvoid main() { gl_Position = aPos; vTex = (uMat * aTex).xy; } | uniform mat4 uMat;\nattribute vec4 aPos;\nattribute vec4 aTex;\nvarying vec2 vTex;\nvoid main() { gl_Position = aPos; vTex = (uMat * aTex).xy; }
#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTex;\nuniform samplerExternalOES sTex;\nvoid main() { gl_FragColor = texture2D(sTex, vTex); } | #extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTex;\nuniform samplerExternalOES sTex;\nvoid main() { gl_FragColor = texture2D(sTex, vTex); } | #extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTex;\nuniform samplerExternalOES sTex;\nvoid main() { gl_FragColor = texture2D(sTex, vTex); } | #extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 vTex;\nuniform samplerExternalOES sTex;\nvoid main() { gl_FragColor = texture2D(sTex, vTex); }
Program GL gagal di-link | GLプログラムのリンクに失敗しました | GL 程序链接失败 | GL 프로그램 링크에 실패했습니다
Frame video tidak sampai ke GL (timeout) | ビデオフレームがGLに届きませんでした(タイムアウト) | 视频帧未能到达 GL(超时) | 비디오 프레임이 GL에 도달하지 않았습니다(시간 초과)
Tidak ada trek video di file ini | このファイルにビデオトラックがありません | 此文件中没有视频轨道 | 이 파일에 비디오 트랙이 없습니다
Trek video tidak dikenal | 不明なビデオトラックです | 未知的视频轨道 | 알 수 없는 비디오 트랙입니다
Ukuran video tidak diketahui | ビデオのサイズが不明です | 视频尺寸未知 | 비디오 크기를 알 수 없습니다
Perangkat ini tidak punya encoder video {0} | この端末には{0}ビデオエンコーダーがありません | 此设备没有 {0} 视频编码器 | 이 기기에는 {0} 비디오 인코더가 없습니다
Encoder video menolak pengaturan ini ({0}x{1}, {2} kbps): {3} | ビデオエンコーダーがこの設定を拒否しました({0}x{1}, {2} kbps): {3} | 视频编码器拒绝了此设置({0}x{1}, {2} kbps):{3} | 비디오 인코더가 이 설정을 거부했습니다({0}x{1}, {2} kbps): {3}
Perangkat tidak bisa mendekode video {0} | この端末では{0}ビデオをデコードできません | 此设备无法解码 {0} 视频 | 이 기기에서는 {0} 비디오를 디코딩할 수 없습니다
Gagal memulai decoder video {0}: {1} | {0}ビデオデコーダーの起動に失敗しました: {1} | 启动 {0} 视频解码器失败:{1} | {0} 비디오 디코더를 시작하지 못했습니다: {1}
Encoder video tidak merespons | ビデオエンコーダーが応答しません | 视频编码器无响应 | 비디오 인코더가 응답하지 않습니다
Tidak ada frame video yang berhasil disandikan | エンコードできたビデオフレームがありません | 没有成功编码的视频帧 | 인코딩된 비디오 프레임이 없습니다
Trek video hasil tidak ditemukan | 出力のビデオトラックが見つかりません | 找不到输出的视频轨道 | 출력 비디오 트랙을 찾을 수 없습니다
Trek audio tidak ditemukan | オーディオトラックが見つかりません | 找不到音频轨道 | 오디오 트랙을 찾을 수 없습니다
Filename encoding | ファイル名のエンコーディング | 文件名编码 | 파일 이름 인코딩
Encoding nama file | ファイル名のエンコーディング | 文件名编码 | 파일 이름 인코딩
Auto (default of this format) | 自動(この形式の既定) | 自动(此格式的默认值) | 자동(이 형식의 기본값)
Auto (bawaan format ini) | 自動(この形式の既定) | 自动(此格式的默认值) | 자동(이 형식의 기본값)
GBK (Simplified Chinese) | GBK(簡体字中国語) | GBK(简体中文) | GBK(중국어 간체)
GBK (Mandarin Sederhana) | GBK(簡体字中国語) | GBK(简体中文) | GBK(중국어 간체)
Big5 (Traditional Chinese) | Big5(繁体字中国語) | Big5(繁体中文) | Big5(중국어 번체)
Big5 (Mandarin Tradisional) | Big5(繁体字中国語) | Big5(繁体中文) | Big5(중국어 번체)
Tidak bisa membaca | 読み取れません | 无法读取 | 읽을 수 없습니다
Data Huffman NeXAS habis | NeXAS のハフマンデータが不足しています | NeXAS 哈夫曼数据已耗尽 | NeXAS 허프만 데이터가 부족합니다
Pohon Huffman NeXAS tidak valid | NeXAS のハフマンツリーが無効です | NeXAS 哈夫曼树无效 | NeXAS 허프만 트리가 올바르지 않습니다
Bukan skrip NeXAS | NeXAS スクリプトではありません | 不是 NeXAS 脚本 | NeXAS 스크립트가 아닙니다
Bukan skrip NeXAS (kepala tidak valid) | NeXAS スクリプトではありません(ヘッダーが無効) | 不是 NeXAS 脚本(头部无效) | NeXAS 스크립트가 아닙니다(헤더가 올바르지 않음)
Blok teks NeXAS tidak ditemukan | NeXAS のテキストブロックが見つかりません | 找不到 NeXAS 文本块 | NeXAS 텍스트 블록을 찾을 수 없습니다
Tidak ada teks non-ASCII | ASCII 以外のテキストがありません | 没有非 ASCII 文本 | ASCII가 아닌 텍스트가 없습니다
bad response format | 応答形式が不正です | 响应格式错误 | 응답 형식이 올바르지 않습니다
into {0}. Output only the translation, without any explanation or extra text. Keep line breaks and any symbols or placeholders unchanged.\n\n | {0}へ翻訳してください。翻訳文のみを出力し、説明や余計なテキストは付けないでください。改行および記号やプレースホルダーはそのまま保持してください。\n\n | 翻译成{0}。只输出译文,不要任何解释或多余文字。保持换行以及所有符号和占位符不变。\n\n | {0}(으)로 번역하세요. 설명이나 불필요한 텍스트 없이 번역문만 출력하세요. 줄바꿈과 모든 기호 및 자리표시자는 그대로 유지하세요.\n\n
bad response | 応答が不正です | 响应错误 | 응답이 올바르지 않습니다
empty response | 応答が空です | 响应为空 | 응답이 비어 있습니다
{0}: bad response | {0}: 応答が不正です | {0}:响应错误 | {0}: 응답이 올바르지 않습니다
google: bad response | google: 応答が不正です | google:响应错误 | google: 응답이 올바르지 않습니다
google: HTTP {0} | google: HTTP {0} | google:HTTP {0} | google: HTTP {0}
Lingva: no server | Lingva: サーバーがありません | Lingva:没有服务器 | Lingva: 서버가 없습니다
Simplified Chinese | 簡体字中国語 | 简体中文 | 중국어 간체
Traditional Chinese | 繁体字中国語 | 繁体中文 | 중국어 번체
the source language (detect it) | 原文の言語(自動検出) | 源语言(自动检测) | 원문 언어(자동 감지)
Rules:\n- Tokens that look like QX0XQ, QX1XQ are placeholders: copy them exactly, never translate, remove, split or add them.\n | ルール:\n- QX0XQ、QX1XQ のようなトークンはプレースホルダーです。そのままコピーし、翻訳・削除・分割・追加をしないでください。\n | 规则:\n- 形如 QX0XQ、QX1XQ 的标记是占位符:请原样复制,切勿翻译、删除、拆分或添加。\n | 규칙:\n- QX0XQ, QX1XQ 같은 토큰은 자리표시자입니다. 그대로 복사하고 번역, 삭제, 분리, 추가를 하지 마세요.\n
- Keep line breaks and leading/trailing spaces. Keep names and honorific style consistent. Translate naturally for game dialogue.\n | - 改行と前後の空白を保持してください。名前や敬称のスタイルは統一してください。ゲームの会話として自然に翻訳してください。\n | - 保留换行以及首尾空格。人名和敬称风格保持一致。按游戏对话的方式自然翻译。\n | - 줄바꿈과 앞뒤 공백을 유지하세요. 이름과 존칭 스타일을 일관되게 하세요. 게임 대사답게 자연스럽게 번역하세요.\n
- Do not add notes. Return only JSON: {"translations": [...]} with exactly the same number of strings in the same order.\n | - 注釈は付けないでください。JSON のみを返してください: {"translations": [...]}。文字列の数と順序は完全に同じにしてください。\n | - 不要添加注释。只返回 JSON:{"translations": [...]},字符串数量和顺序必须完全一致。\n | - 주석을 달지 마세요. JSON만 반환하세요: {"translations": [...]}. 문자열의 개수와 순서가 완전히 같아야 합니다.\n
- Style: casual, colloquial everyday speech as spoken by the characters.\n | - スタイル: キャラクターが話すような、くだけた日常会話の口調。\n | - 风格:口语化、随意,如角色日常说话的语气。\n | - 스타일: 등장인물이 말하는 듯한 편안한 일상 구어체.\n
- Style: polite and formal.\n | - スタイル: 丁寧でフォーマルな口調。\n | - 风格:礼貌、正式。\n | - 스타일: 공손하고 격식 있는 말투.\n
- Style: stay close to the source wording (literal), but still grammatical.\n | - スタイル: 原文の表現に近く(直訳調)、かつ文法的に正しく。\n | - 风格:贴近原文措辞(直译),但语法正确。\n | - 스타일: 원문 표현에 가깝게(직역투) 하되 문법에 맞게.\n
- Style: natural and fluent, as a professional localizer would write.\n | - スタイル: プロのローカライザーが書くような、自然で流暢な文章。\n | - 风格:自然流畅,如专业本地化人员所写。\n | - 스타일: 전문 현지화 작가가 쓴 듯 자연스럽고 유창하게.\n
- The input may be an object {"context_before": [...], "strings": [...]}. context_before holds the preceding lines for context only: never translate or return them; translate only "strings".\n | - 入力は {"context_before": [...], "strings": [...]} というオブジェクトの場合があります。context_before は文脈用の直前の行です。翻訳も返却もせず、"strings" のみ翻訳してください。\n | - 输入可能是对象 {"context_before": [...], "strings": [...]}。context_before 是仅供参考的前文:不要翻译也不要返回,只翻译 "strings"。\n | - 입력이 {"context_before": [...], "strings": [...]} 형태의 객체일 수 있습니다. context_before는 문맥용 이전 줄이므로 번역하거나 반환하지 말고 "strings"만 번역하세요.\n
Notes about this game (context, tone, characters):\n | このゲームに関するメモ(背景、トーン、キャラクター):\n | 关于此游戏的说明(背景、语气、角色):\n | 이 게임에 대한 메모(배경, 분위기, 등장인물):\n
Glossary (use these translations exactly whenever the source term appears):\n | 用語集(原文にこれらの用語が出てきたら、必ずこの訳を使用):\n | 术语表(原文出现这些术语时,必须使用这些译法):\n | 용어집(원문에 이 용어가 나오면 반드시 이 번역을 사용):\n
Gemini: empty response (blocked?) | Gemini: 応答が空です(ブロックされた可能性あり) | Gemini:响应为空(可能被拦截?) | Gemini: 응답이 비어 있습니다(차단됨?)
Gemini: empty response | Gemini: 応答が空です | Gemini:响应为空 | Gemini: 응답이 비어 있습니다
Claude: empty response | Claude: 応答が空です | Claude:响应为空 | Claude: 응답이 비어 있습니다
no model list | モデル一覧がありません | 没有模型列表 | 모델 목록이 없습니다
Ukuran mdf tidak valid | mdf のサイズが無効です | mdf 大小无效 | mdf 크기가 올바르지 않습니다
Dekompresi mdf gagal | mdf の展開に失敗しました | mdf 解压失败 | mdf 압축 해제에 실패했습니다
Array PSB tidak valid | PSB の配列が無効です | PSB 数组无效 | PSB 배열이 올바르지 않습니다
Bukan PSB | PSB ではありません | 不是 PSB | PSB가 아닙니다
Versi PSB belum didukung ({0}) | PSB のバージョンは未対応です({0}) | 尚不支持的 PSB 版本({0}) | 지원되지 않는 PSB 버전입니다({0})
Header PSB tidak valid | PSB のヘッダーが無効です | PSB 头部无效 | PSB 헤더가 올바르지 않습니다
Tabel nama PSB rusak | PSB の名前テーブルが壊れています | PSB 名称表已损坏 | PSB 이름 테이블이 손상되었습니다
PSB terlalu dalam | PSB の階層が深すぎます | PSB 嵌套过深 | PSB 계층이 너무 깊습니다
Tipe PSB tidak dikenal: {0} | 不明な PSB タイプ: {0} | 未知的 PSB 类型:{0} | 알 수 없는 PSB 타입: {0}
pickle: MARK tidak ada | pickle: MARK がありません | pickle:缺少 MARK | pickle: MARK가 없습니다
pickle terpotong | pickle が途中で切れています | pickle 被截断 | pickle이 중간에 잘렸습니다
pickle: APPEND ke tipe tak dikenal | pickle: 不明な型への APPEND | pickle:对未知类型执行 APPEND | pickle: 알 수 없는 타입에 APPEND
pickle: SETITEMS ke tipe tak dikenal | pickle: 不明な型への SETITEMS | pickle:对未知类型执行 SETITEMS | pickle: 알 수 없는 타입에 SETITEMS
Opcode pickle tidak didukung: 0x{0} | 未対応の pickle オペコードです: 0x{0} | 不支持的 pickle 操作码:0x{0} | 지원되지 않는 pickle 옵코드입니다: 0x{0}
Bukan file aset RPGM terenkripsi | 暗号化された RPGM アセットファイルではありません | 不是加密的 RPGM 资源文件 | 암호화된 RPGM 에셋 파일이 아닙니다
# FuruArc translation v1\n | # FuruArc translation v1\n | # FuruArc translation v1\n | # FuruArc translation v1\n
# Ubah hanya baris [T]. Baris [O] adalah teks asli (jangan diubah). Pisahkan blok dengan baris kosong.\n\n | # [T] 行のみ編集してください。[O] 行は原文です(変更しないでください)。ブロックは空行で区切ります。\n\n | # 请只修改 [T] 行。[O] 行是原文(请勿修改)。块之间用空行分隔。\n\n | # [T] 줄만 수정하세요. [O] 줄은 원문입니다(수정하지 마세요). 블록은 빈 줄로 구분합니다.\n\n
# FuruArc draft | # FuruArc draft | # FuruArc draft | # FuruArc draft
# GameArc draft | # GameArc draft | # GameArc draft | # GameArc draft
# FuruArc draft\n | # FuruArc draft\n | # FuruArc draft\n | # FuruArc draft\n
FuruArc translation v1 | FuruArc translation v1 | FuruArc translation v1 | FuruArc translation v1
JSON VNTP hanya untuk dibaca penerjemah; tidak bisa diimpor. Impor berkas JSON/CSV/TXT dari ekspor FuruArc. | VNTP の JSON は翻訳者の閲覧専用で、インポートできません。FuruArc のエクスポートによる JSON/CSV/TXT ファイルをインポートしてください。 | VNTP JSON 仅供译者阅读,无法导入。请导入 FuruArc 导出的 JSON/CSV/TXT 文件。 | VNTP JSON은 번역가가 읽기 전용으로만 쓰며 가져올 수 없습니다. FuruArc에서 내보낸 JSON/CSV/TXT 파일을 가져오세요.
JSON: kunci "entries" tidak ada | JSON: "entries" キーがありません | JSON:缺少 "entries" 键 | JSON: "entries" 키가 없습니다
JSON: bentuk berkas tidak dikenali | JSON: ファイル構造を認識できません | JSON:无法识别文件结构 | JSON: 파일 구조를 인식할 수 없습니다
Slot data tidak ada | データスロットがありません | 没有数据槽 | 데이터 슬롯이 없습니다
Header rpyc rusak | rpyc のヘッダーが壊れています | rpyc 头部已损坏 | rpyc 헤더가 손상되었습니다
Bukan file rpyc Ren'Py | Ren'Py の rpyc ファイルではありません | 不是 Ren'Py 的 rpyc 文件 | Ren'Py rpyc 파일이 아닙니다
Struktur rpyc tidak dikenali | rpyc の構造を認識できません | 无法识别 rpyc 结构 | rpyc 구조를 인식할 수 없습니다
\n# Decompiled by FuruArc (port of unrpyc: https://github.com/CensoredUsername/unrpyc)\n | \n# Decompiled by FuruArc (port of unrpyc: https://github.com/CensoredUsername/unrpyc)\n | \n# Decompiled by FuruArc (port of unrpyc: https://github.com/CensoredUsername/unrpyc)\n | \n# Decompiled by FuruArc (port of unrpyc: https://github.com/CensoredUsername/unrpyc)\n
Unknown AST node: {0} | Unknown AST node: {0} | Unknown AST node: {0} | Unknown AST node: {0}
# [FuruArc] testcase {0} tidak didekompilasi | # [FuruArc] testcase {0} は逆コンパイルされていません | # [FuruArc] testcase {0} 未被反编译 | # [FuruArc] testcase {0}은(는) 디컴파일되지 않았습니다
show layer {0} | show layer {0} | show layer {0} | show layer {0}
Unmatched paired with {0} != {1} | Unmatched paired with {0} != {1} | Unmatched paired with {0} != {1} | Unmatched paired with {0} != {1}
translate {0} strings: | translate {0} strings: | translate {0} strings: | translate {0} strings:
"""
    const val T12 = """
# [FuruArc] screen {0} tidak didekompilasi (screen language) | # [FuruArc] screen {0} は逆コンパイルされていません(screen language) | # [FuruArc] screen {0} 未被反编译(screen language) | # [FuruArc] screen {0}은(는) 디컴파일되지 않았습니다(screen language)
Unknown ATL node: {0} | Unknown ATL node: {0} | Unknown ATL node: {0} | Unknown ATL node: {0}
Skrip biner | バイナリスクリプト | 二进制脚本 | 바이너리 스크립트
Teks 暗号 minimal 8 byte | 暗号テキストは最低 8 バイト必要です | 暗号文本至少需要 8 字节 | 暗号 텍스트는 최소 8바이트여야 합니다
LZSS terlalu pendek | LZSS データが短すぎます | LZSS 数据过短 | LZSS 데이터가 너무 짧습니다
LZSS: ukuran asli tidak wajar | LZSS: 元のサイズが不正です | LZSS:原始大小异常 | LZSS: 원본 크기가 비정상입니다
LZSS: data habis | LZSS: データが不足しています | LZSS:数据已耗尽 | LZSS: 데이터가 부족합니다
LZSS: referensi mundur tidak valid | LZSS: 後方参照が無効です | LZSS:回溯引用无效 | LZSS: 뒤쪽 참조가 올바르지 않습니다
Scene.pck ini terenkripsi dengan kunci exe. Isi opsi Kunci (hex 16 byte atau teks 暗号). | この Scene.pck は exe の鍵で暗号化されています。鍵オプションを入力してください(16 バイトの hex または暗号テキスト)。 | 此 Scene.pck 已用 exe 密钥加密。请填写密钥选项(16 字节 hex 或暗号文本)。 | 이 Scene.pck는 exe 키로 암호화되어 있습니다. 키 옵션을 입력하세요(16바이트 hex 또는 暗号 텍스트).
Kunci exe salah atau data scene rusak | exe の鍵が違うか、シーンデータが壊れています | exe 密钥错误或场景数据已损坏 | exe 키가 잘못되었거나 씬 데이터가 손상되었습니다
Bukan scene .dat Siglus | Siglus の scene .dat ではありません | 不是 Siglus 的 scene .dat | Siglus scene .dat가 아닙니다
Bukan skrip Silky .mes | Silky の .mes スクリプトではありません | 不是 Silky 的 .mes 脚本 | Silky .mes 스크립트가 아닙니다
Header Silky tidak valid | Silky のヘッダーが無効です | Silky 头部无效 | Silky 헤더가 올바르지 않습니다
String Silky terpotong | Silky の文字列が途中で切れています | Silky 字符串被截断 | Silky 문자열이 중간에 잘렸습니다
Instruksi Silky terpotong | Silky の命令が途中で切れています | Silky 指令被截断 | Silky 명령이 중간에 잘렸습니다
Skrip Silky rusak | Silky スクリプトが壊れています | Silky 脚本已损坏 | Silky 스크립트가 손상되었습니다
Ruby bersarang tidak diizinkan | ルビの入れ子は許可されていません | 不允许嵌套注音 | 루비 중첩은 허용되지 않습니다
Penutup ruby tanpa pembuka | 開始のないルビの終了があります | 有结束注音但缺少开始注音 | 시작 없이 루비 종료가 있습니다
Baris baru di dalam ruby tidak diizinkan | ルビ内の改行は許可されていません | 注音内不允许换行 | 루비 안의 줄바꿈은 허용되지 않습니다
Tidak bisa membaca file | ファイルを読み取れません | 无法读取文件 | 파일을 읽을 수 없습니다
Jenis file tidak didukung | 未対応のファイル形式です | 不支持的文件类型 | 지원되지 않는 파일 형식입니다
File terlalu besar | ファイルが大きすぎます | 文件过大 | 파일이 너무 큽니다
Bukan file CRX (kepala CRXG/CRXD tidak ditemukan) | CRX ファイルではありません(CRXG/CRXD ヘッダーが見つかりません) | 不是 CRX 文件(找不到 CRXG/CRXD 头部) | CRX 파일이 아닙니다(CRXG/CRXD 헤더를 찾을 수 없음)
XPCM mode {0} ({1}) belum didukung. Yang didukung: Ogg, PCM mentah, ADPCM | XPCM モード {0}({1})は未対応です。対応: Ogg、生 PCM、ADPCM | 尚不支持 XPCM 模式 {0}({1})。支持:Ogg、原始 PCM、ADPCM | XPCM 모드 {0}({1})은(는) 아직 지원되지 않습니다. 지원: Ogg, 원시 PCM, ADPCM
XPCM mode {0} tidak dikenal | 不明な XPCM モード {0} | 未知的 XPCM 模式 {0} | 알 수 없는 XPCM 모드 {0}
Bukan skrip WS2 | WS2 スクリプトではありません | 不是 WS2 脚本 | WS2 스크립트가 아닙니다
Bukan skrip WS2 yang dikenali (opcode tidak cocok V1–V3) | 認識できる WS2 スクリプトではありません(オペコードが V1~V3 に一致しません) | 不是可识别的 WS2 脚本(操作码不匹配 V1–V3) | 인식할 수 있는 WS2 스크립트가 아닙니다(옵코드가 V1–V3과 맞지 않음)
BURIKO ARC20 | BURIKO ARC20 | BURIKO ARC20 | BURIKO ARC20
Index BGI tidak valid | BGI のインデックスが無効です | BGI 索引无效 | BGI 인덱스가 올바르지 않습니다
Entri BGI di luar arsip: {0} | BGI のエントリーがアーカイブの範囲外です: {0} | BGI 条目超出压缩包范围:{0} | BGI 항목이 아카이브 범위를 벗어났습니다: {0}
Archive type | アーカイブの種類 | 压缩包类型 | 아카이브 종류
Tipe arsip | アーカイブの種類 | 压缩包类型 | 아카이브 종류
BURIKO ARC20 (v2, most games) | BURIKO ARC20(v2、ほとんどのゲーム) | BURIKO ARC20(v2,大多数游戏) | BURIKO ARC20(v2, 대부분의 게임)
BURIKO ARC20 (v2, kebanyakan game) | BURIKO ARC20(v2、ほとんどのゲーム) | BURIKO ARC20(v2,大多数游戏) | BURIKO ARC20(v2, 대부분의 게임)
PackFile (v1, old games) | PackFile(v1、古いゲーム) | PackFile(v1,旧游戏) | PackFile(v1, 오래된 게임)
PackFile (v1, game lama) | PackFile(v1、古いゲーム) | PackFile(v1,旧游戏) | PackFile(v1, 오래된 게임)
Choose v2 if you are not sure. Names: v2 max 95 bytes, v1 max 15 bytes. | わからない場合は v2 を選んでください。名前の長さ: v2 は最大 95 バイト、v1 は最大 15 バイト。 | 不确定时请选 v2。名称长度:v2 最多 95 字节,v1 最多 15 字节。 | 잘 모르면 v2를 선택하세요. 이름 길이: v2는 최대 95바이트, v1은 최대 15바이트.
Pilih v2 jika tidak mengetahuinya. Nama: v2 maks 95 byte, v1 maks 15 byte. | わからない場合は v2 を選んでください。名前の長さ: v2 は最大 95 バイト、v1 は最大 15 バイト。 | 不确定时请选 v2。名称长度:v2 最多 95 字节,v1 最多 15 字节。 | 잘 모르면 v2를 선택하세요. 이름 길이: v2는 최대 95바이트, v1은 최대 15바이트.
Decode DSC files (script/data) | DSC ファイルをデコード(スクリプト/データ) | 解码 DSC 文件(脚本/数据) | DSC 파일 디코딩(스크립트/데이터)
Dekode file DSC (skrip/data) | DSC ファイルをデコード(スクリプト/データ) | 解码 DSC 文件(脚本/数据) | DSC 파일 디코딩(스크립트/데이터)
Files starting with 'DSC FORMAT 1.00' are decompressed. Leave off to keep originals (needed for repacking). | 'DSC FORMAT 1.00' で始まるファイルを展開(解凍)します。オフにすると元のまま保持されます(再パックに必要)。 | 以 'DSC FORMAT 1.00' 开头的文件会被解压。关闭则保留原文件(重新打包时需要)。 | 'DSC FORMAT 1.00'으로 시작하는 파일의 압축을 해제합니다. 끄면 원본을 유지합니다(다시 묶을 때 필요).
File berawalan 'DSC FORMAT 1.00' didekompres. Matikan untuk menyimpan aslinya (perlu bila ingin dikemas ulang). | 'DSC FORMAT 1.00' で始まるファイルを展開(解凍)します。オフにすると元のまま保持されます(再パックに必要)。 | 以 'DSC FORMAT 1.00' 开头的文件会被解压。关闭则保留原文件(重新打包时需要)。 | 'DSC FORMAT 1.00'으로 시작하는 파일의 압축을 해제합니다. 끄면 원본을 유지합니다(다시 묶을 때 필요).
Nama terlalu panjang untuk v1 (maks 15 byte): {0} | v1 には名前が長すぎます(最大 15 バイト): {0} | 名称对 v1 来说过长(最多 15 字节):{0} | v1에는 이름이 너무 깁니다(최대 15바이트): {0}
Ukuran total melebihi 4 GB | 合計サイズが 4 GB を超えています | 总大小超过 4 GB | 총 크기가 4 GB를 초과합니다
Nama terlalu panjang (maks 95 byte): {0} | 名前が長すぎます(最大 95 バイト): {0} | 名称过长(最多 95 字节):{0} | 이름이 너무 깁니다(최대 95바이트): {0}
File terlalu kecil | ファイルが小さすぎます | 文件过小 | 파일이 너무 작습니다
Jumlah entri tidak valid | エントリー数が無効です | 条目数量无效 | 항목 수가 올바르지 않습니다
Arsip INT terenkripsi (ada __key__.dat): isi opsi Password dengan password game (bisa dicari di Pencari Kunci dari file .exe game) | INT アーカイブは暗号化されています(__key__.dat あり)。パスワードオプションにゲームのパスワードを入力してください(ゲームの .exe からキーファインダーで探せます) | INT 压缩包已加密(存在 __key__.dat):请在密码选项中填写游戏密码(可用密钥查找器从游戏的 .exe 中查找) | INT 아카이브가 암호화되어 있습니다(__key__.dat 있음). 비밀번호 옵션에 게임 비밀번호를 입력하세요(키 파인더로 게임 .exe에서 찾을 수 있습니다)
Password salah atau indeks INT tidak cocok | パスワードが違うか、INT のインデックスが一致しません | 密码错误或 INT 索引不匹配 | 비밀번호가 틀렸거나 INT 인덱스가 일치하지 않습니다
Index INT tidak terbaca (kemungkinan terenkripsi, butuh kunci game) | INT のインデックスを読み取れません(暗号化されている可能性があり、ゲームのキーが必要です) | 无法读取 INT 索引(可能已加密,需要游戏密钥) | INT 인덱스를 읽을 수 없습니다(암호화되었을 수 있으며 게임 키가 필요합니다)
Index di luar arsip | インデックスがアーカイブの範囲外です | 索引超出压缩包范围 | 인덱스가 아카이브 범위를 벗어났습니다
Password salah | パスワードが違います | 密码错误 | 비밀번호가 틀렸습니다
Password (encrypted INT) | パスワード(INT 暗号化) | 密码(加密的 INT) | 비밀번호(암호화된 INT)
Password (INT terenkripsi) | パスワード(INT 暗号化) | 密码(加密的 INT) | 비밀번호(암호화된 INT)
Only for archives with __key__.dat. Use Key Finder on the game's .exe. | __key__.dat があるアーカイブ専用です。ゲームの .exe に対してキーファインダーを使ってください。 | 仅适用于带有 __key__.dat 的压缩包。请对游戏的 .exe 使用密钥查找器。 | __key__.dat이 있는 아카이브 전용입니다. 게임 .exe에 키 파인더를 사용하세요.
Hanya untuk arsip ber-__key__.dat. Cari lewat Pencari Kunci pada .exe game. | __key__.dat があるアーカイブ専用です。ゲームの .exe に対してキーファインダーを使ってください。 | 仅适用于带有 __key__.dat 的压缩包。请对游戏的 .exe 使用密钥查找器。 | __key__.dat이 있는 아카이브 전용입니다. 게임 .exe에 키 파인더를 사용하세요.
Name field size | 名前フィールドのサイズ | 名称字段大小 | 이름 필드 크기
Ukuran kolom nama | 名前フィールドのサイズ | 名称字段大小 | 이름 필드 크기
64 bytes (0x40, most games) | 64 バイト(0x40、ほとんどのゲーム) | 64 字节(0x40,大多数游戏) | 64바이트(0x40, 대부분의 게임)
64 byte (0x40, kebanyakan game) | 64 バイト(0x40、ほとんどのゲーム) | 64 字节(0x40,大多数游戏) | 64바이트(0x40, 대부분의 게임)
32 bytes (0x20, old games) | 32 バイト(0x20、古いゲーム) | 32 字节(0x20,旧游戏) | 32바이트(0x20, 오래된 게임)
32 byte (0x20, game lama) | 32 バイト(0x20、古いゲーム) | 32 字节(0x20,旧游戏) | 32바이트(0x20, 오래된 게임)
Choose 64 if you are not sure. | わからない場合は 64 を選んでください。 | 不确定时请选 64。 | 잘 모르면 64를 선택하세요.
Pilih 64 jika tidak mengetahuinya. | わからない場合は 64 を選んでください。 | 不确定时请选 64。 | 잘 모르면 64를 선택하세요.
Nama terlalu panjang (maks {0} byte): {1} | 名前が長すぎます(最大 {0} バイト): {1} | 名称过长(最多 {0} 字节):{1} | 이름이 너무 깁니다(최대 {0}바이트): {1}
CRM (Adv32 / Circus) | CRM (Adv32 / Circus) | CRM (Adv32 / Circus) | CRM (Adv32 / Circus)
Jumlah entri CRM tidak valid | CRM のエントリー数が無効です | CRM 条目数量无效 | CRM 항목 수가 올바르지 않습니다
Index CRM tidak valid | CRM のインデックスが無効です | CRM 索引无效 | CRM 인덱스가 올바르지 않습니다
Tidak ada file | ファイルがありません | 没有文件 | 파일이 없습니다
Nama terlalu panjang (maks 23 byte): {0} | 名前が長すぎます(最大 23 バイト): {0} | 名称过长(最多 23 字节):{0} | 이름이 너무 깁니다(최대 23바이트): {0}
DAT (Adv32 / Circus) | DAT (Adv32 / Circus) | DAT (Adv32 / Circus) | DAT (Adv32 / Circus)
Name field width | 名前フィールドの幅 | 名称字段宽度 | 이름 필드 너비
Lebar kolom nama | 名前フィールドの幅 | 名称字段宽度 | 이름 필드 너비
60 bytes (VOICE.DAT of D.C. II) | 60 バイト(D.C. II の VOICE.DAT) | 60 字节(D.C. II 的 VOICE.DAT) | 60바이트(D.C. II의 VOICE.DAT)
60 byte (VOICE.DAT D.C. II) | 60 バイト(D.C. II の VOICE.DAT) | 60 字节(D.C. II 的 VOICE.DAT) | 60바이트(D.C. II의 VOICE.DAT)
Choose 60 if you are not sure. Names use backslash folders and must fit width-1 bytes. | わからない場合は 60 を選んでください。名前はバックスラッシュ区切りのフォルダーを使い、幅-1 バイトに収める必要があります。 | 不确定时请选 60。名称使用反斜杠分隔文件夹,且必须不超过 宽度-1 字节。 | 잘 모르면 60을 선택하세요. 이름은 백슬래시 폴더를 사용하며 너비-1바이트 안에 들어가야 합니다.
Pilih 60 jika tidak mengetahuinya. Nama memakai folder dengan backslash dan harus muat (lebar-1) byte. | わからない場合は 60 を選んでください。名前はバックスラッシュ区切りのフォルダーを使い、幅-1 バイトに収める必要があります。 | 不确定时请选 60。名称使用反斜杠分隔文件夹,且必须不超过 宽度-1 字节。 | 잘 모르면 60을 선택하세요. 이름은 백슬래시 폴더를 사용하며 너비-1바이트 안에 들어가야 합니다.
Index DAT (Circus) tidak valid | DAT(Circus)のインデックスが無効です | DAT(Circus)索引无效 | DAT(Circus) 인덱스가 올바르지 않습니다
Index DAT tidak valid | DAT のインデックスが無効です | DAT 索引无效 | DAT 인덱스가 올바르지 않습니다
Offset pertama tidak valid | 最初のオフセットが無効です | 第一个偏移量无效 | 첫 번째 오프셋이 올바르지 않습니다
Layout tidak cocok | レイアウトが一致しません | 布局不匹配 | 레이아웃이 일치하지 않습니다
Nama kosong/tidak valid | 名前が空か無効です | 名称为空或无效 | 이름이 비어 있거나 올바르지 않습니다
Offset tidak valid | オフセットが無効です | 偏移量无效 | 오프셋이 올바르지 않습니다
DAT Circus membutuhkan minimal 2 file | Circus の DAT には 2 個以上のファイルが必要です | Circus 的 DAT 至少需要 2 个文件 | Circus DAT에는 파일이 2개 이상 필요합니다
PCK (Adv32 / Circus) | PCK (Adv32 / Circus) | PCK (Adv32 / Circus) | PCK (Adv32 / Circus)
Index PCK tidak valid | PCK のインデックスが無効です | PCK 索引无效 | PCK 인덱스가 올바르지 않습니다
Entri PCK tidak valid | PCK のエントリーが無効です | PCK 条目无效 | PCK 항목이 올바르지 않습니다
Nama terlalu panjang (maks 55 byte): {0} | 名前が長すぎます(最大 55 バイト): {0} | 名称过长(最多 55 字节):{0} | 이름이 너무 깁니다(최대 55바이트): {0}
Index Escu:de tidak valid | Escu:de のインデックスが無効です | Escu:de 索引无效 | Escu:de 인덱스가 올바르지 않습니다
Entri Escu:de di luar arsip | Escu:de のエントリーがアーカイブの範囲外です | Escu:de 条目超出压缩包范围 | Escu:de 항목이 아카이브 범위를 벗어났습니다
Nama Escu:de tidak valid | Escu:de の名前が無効です | Escu:de 名称无效 | Escu:de 이름이 올바르지 않습니다
Data LZW terlalu pendek | LZW データが短すぎます | LZW 数据过短 | LZW 데이터가 너무 짧습니다
Ukuran LZW tidak valid | LZW のサイズが無効です | LZW 大小无效 | LZW 크기가 올바르지 않습니다
Data LZW habis | LZW データが不足しています | LZW 数据已耗尽 | LZW 데이터가 부족합니다
Lebar token LZW melebihi batas | LZW のトークン幅が上限を超えています | LZW 标记位宽超出限制 | LZW 토큰 너비가 한도를 초과했습니다
Kamus LZW penuh | LZW 辞書がいっぱいです | LZW 字典已满 | LZW 사전이 가득 찼습니다
Token LZW di luar batas: {0} | LZW トークンが範囲外です: {0} | LZW 标记超出范围:{0} | LZW 토큰이 범위를 벗어났습니다: {0}
Token LZW rusak | LZW トークンが壊れています | LZW 标记已损坏 | LZW 토큰이 손상되었습니다
BIN (FVP / Favorite) | BIN (FVP / Favorite) | BIN (FVP / Favorite) | BIN (FVP / Favorite)
Index BIN (Favorite) tidak valid | BIN(Favorite)のインデックスが無効です | BIN(Favorite)索引无效 | BIN(Favorite) 인덱스가 올바르지 않습니다
Header BIN tidak valid | BIN のヘッダーが無効です | BIN 头部无效 | BIN 헤더가 올바르지 않습니다
Offset nama tidak valid | 名前のオフセットが無効です | 名称偏移量无效 | 이름 오프셋이 올바르지 않습니다
Entri BIN tidak valid | BIN のエントリーが無効です | BIN 条目无效 | BIN 항목이 올바르지 않습니다
ARC (HexenHaus ARCC) | ARC (HexenHaus ARCC) | ARC (HexenHaus ARCC) | ARC (HexenHaus ARCC)
Seksi {0} tidak ditemukan di ARCC | ARCC にセクション {0} が見つかりません | ARCC 中找不到区段 {0} | ARCC에서 섹션 {0}을(를) 찾을 수 없습니다
Jumlah entri ARCC tidak valid | ARCC のエントリー数が無効です | ARCC 条目数量无效 | ARCC 항목 수가 올바르지 않습니다
Offset ADDR tidak valid | ADDR のオフセットが無効です | ADDR 偏移量无效 | ADDR 오프셋이 올바르지 않습니다
ARCC tidak berisi file | ARCC にファイルがありません | ARCC 中没有文件 | ARCC에 파일이 없습니다
Pack ARCC belum didukung | ARCC のパックは未対応です | 尚不支持打包 ARCC | ARCC 묶기는 아직 지원되지 않습니다
Header ODIO tidak valid | ODIO のヘッダーが無効です | ODIO 头部无效 | ODIO 헤더가 올바르지 않습니다
Indeks ODIO tidak valid | ODIO のインデックスが無効です | ODIO 索引无效 | ODIO 인덱스가 올바르지 않습니다
ODIO kosong | ODIO が空です | ODIO 为空 | ODIO가 비어 있습니다
Offset ODIO tidak berurutan | ODIO のオフセットが順序どおりではありません | ODIO 偏移量顺序不正确 | ODIO 오프셋이 순서대로 되어 있지 않습니다
Pack ODIO belum didukung | ODIO のパックは未対応です | 尚不支持打包 ODIO | ODIO 묶기는 아직 지원되지 않습니다
Jumlah entri WAG tidak valid | WAG のエントリー数が無効です | WAG 条目数量无效 | WAG 항목 수가 올바르지 않습니다
WAG tidak berisi entri yang terbaca | WAG に読み取れるエントリーがありません | WAG 中没有可读取的条目 | WAG에 읽을 수 있는 항목이 없습니다
Pack WAG belum didukung | WAG のパックは未対応です | 尚不支持打包 WAG | WAG 묶기는 아직 지원되지 않습니다
SCD (Yaneurao Itufuru) | SCD (Yaneurao Itufuru) | SCD (Yaneurao Itufuru) | SCD (Yaneurao Itufuru)
Index Itufuru tidak valid | Itufuru のインデックスが無効です | Itufuru 索引无效 | Itufuru 인덱스가 올바르지 않습니다
Entri Itufuru tidak valid | Itufuru のエントリーが無効です | Itufuru 条目无效 | Itufuru 항목이 올바르지 않습니다
Nama terlalu panjang (maks 12 byte): {0} | 名前が長すぎます(最大 12 バイト): {0} | 名称过长(最多 12 字节):{0} | 이름이 너무 깁니다(최대 12바이트): {0}
Versi Majiro tidak didukung | 未対応の Majiro バージョンです | 不支持的 Majiro 版本 | 지원되지 않는 Majiro 버전입니다
Index Majiro tidak valid | Majiroのインデックスが無効です | Majiro 索引无效 | Majiro 인덱스가 올바르지 않아요
Tabel Majiro tidak cocok | Majiroのテーブルが一致しません | Majiro 表不匹配 | Majiro 테이블이 일치하지 않아요
Entri Majiro tidak valid: {0} | Majiroのエントリが無効です: {0} | Majiro 条目无效：{0} | Majiro 항목이 올바르지 않아요: {0}
Majiro tidak berisi file | Majiroにファイルがありません | Majiro 不包含文件 | Majiro에 파일이 없어요
Index SAR/NSA tidak valid | SAR/NSAのインデックスが無効です | SAR/NSA 索引无效 | SAR/NSA 인덱스가 올바르지 않아요
Header SAR/NSA tidak valid | SAR/NSAのヘッダーが無効です | SAR/NSA 头部无效 | SAR/NSA 헤더가 올바르지 않아요
Nama tidak valid | 無効な名前です | 名称无效 | 이름이 올바르지 않아요
Entri tidak valid | 無効なエントリです | 条目无效 | 항목이 올바르지 않아요
Tipe kompresi tidak valid | 無効な圧縮タイプです | 压缩类型无效 | 압축 유형이 올바르지 않아요
Entri di luar arsip: {0} | エントリがアーカイブの範囲外です: {0} | 条目超出压缩包范围：{0} | 항목이 아카이브 범위를 벗어났어요: {0}
Kompresi NSA tipe {0} belum didukung | NSA圧縮タイプ {0} は未対応です | 尚不支持 NSA 压缩类型 {0} | NSA 압축 유형 {0}은(는) 아직 지원되지 않아요
SAR (arc.sar, no compression) | SAR (arc.sar、無圧縮) | SAR（arc.sar，无压缩） | SAR (arc.sar, 무압축)
SAR (arc.sar, tanpa kompresi) | SAR (arc.sar、無圧縮) | SAR（arc.sar，无压缩） | SAR (arc.sar, 무압축)
NSA (arc.nsa, stored) | NSA (arc.nsa、無圧縮) | NSA（arc.nsa，无压缩） | NSA (arc.nsa, 무압축)
NSA (arc.nsa, tanpa kompresi) | NSA (arc.nsa、無圧縮) | NSA（arc.nsa，无压缩） | NSA (arc.nsa, 무압축)
Choose SAR if you are not sure. Pick NSA if the game expects arc.nsa. Files are stored uncompressed. | よくわからない場合はSARを選んでください。ゲームがarc.nsaを必要とする場合はNSAを選びます。ファイルは無圧縮で保存されます。 | 不确定时请选择 SAR。游戏需要 arc.nsa 时请选择 NSA。文件将以未压缩方式存储。 | 잘 모르겠다면 SAR을 선택하세요. 게임이 arc.nsa를 요구하면 NSA를 선택하세요. 파일은 압축 없이 저장돼요.
Pilih SAR jika tidak mengetahuinya. Pilih NSA bila game memakai arc.nsa. Isi file disimpan tanpa kompresi. | よくわからない場合はSARを選んでください。ゲームがarc.nsaを必要とする場合はNSAを選びます。ファイルは無圧縮で保存されます。 | 不确定时请选择 SAR。游戏需要 arc.nsa 时请选择 NSA。文件将以未压缩方式存储。 | 잘 모르겠다면 SAR을 선택하세요. 게임이 arc.nsa를 요구하면 NSA를 선택하세요. 파일은 압축 없이 저장돼요.
Terlalu banyak file (maks 65535) | ファイルが多すぎます (最大65535) | 文件过多（最多 65535 个） | 파일이 너무 많아요 (최대 65535)
Nama terlalu panjang: {0} | 名前が長すぎます: {0} | 名称过长：{0} | 이름이 너무 길어요: {0}
NeXAS PAC | NeXAS PAC | NeXAS PAC | NeXAS PAC
Tanpa kompresi (varian 0) | 無圧縮 (バリアント0) | 无压缩（变体 0） | 무압축 (변형 0)
Files that do not shrink are stored raw. | 小さくならないファイルはそのまま保存されます。 | 无法缩小的文件将原样存储。 | 줄어들지 않는 파일은 원본 그대로 저장돼요.
File yang tidak mengecil disimpan mentah. | 小さくならないファイルはそのまま保存されます。 | 无法缩小的文件将原样存储。 | 줄어들지 않는 파일은 원본 그대로 저장돼요.
Huffman at end (like the game) | 末尾にHuffman (ゲームと同じ) | 末尾 Huffman（与游戏一致） | 끝에 Huffman (게임과 동일)
Huffman di akhir (seperti game) | 末尾にHuffman (ゲームと同じ) | 末尾 Huffman（与游戏一致） | 끝에 Huffman (게임과 동일)
Plain after header | ヘッダーの後にそのまま配置 | 头部之后为原始数据 | 헤더 뒤에 원본 그대로
Polos setelah header | ヘッダーの後にそのまま配置 | 头部之后为原始数据 | 헤더 뒤에 원본 그대로
Bukan arsip NeXAS PAC | NeXAS PACアーカイブではありません | 不是 NeXAS PAC 压缩包 | NeXAS PAC 아카이브가 아니에요
Panjang indeks NeXAS tidak valid | NeXASのインデックス長が無効です | NeXAS 索引长度无效 | NeXAS 인덱스 길이가 올바르지 않아요
Indeks NeXAS kosong atau rusak | NeXASのインデックスが空または破損しています | NeXAS 索引为空或已损坏 | NeXAS 인덱스가 비어 있거나 손상됐어요
Varian kompresi {0} ({1}) belum didukung: {2} | 圧縮バリアント {0} ({1}) は未対応です: {2} | 尚不支持压缩变体 {0}（{1}）：{2} | 압축 변형 {0}({1})은(는) 아직 지원되지 않아요: {2}
Entri terlalu besar | エントリが大きすぎます | 条目过大 | 항목이 너무 커요
Ukuran hasil dekompresi tidak cocok: {0} | 展開後のサイズが一致しません: {0} | 解压后大小不匹配：{0} | 압축 해제 후 크기가 일치하지 않아요: {0}
Nama terlalu panjang (maks. 63 byte): {0} | 名前が長すぎます (最大63バイト): {0} | 名称过长（最多 63 字节）：{0} | 이름이 너무 길어요 (최대 63바이트): {0}
Ukuran file berubah saat dikemas: {0} | パック中にファイルサイズが変わりました: {0} | 打包时文件大小发生变化：{0} | 묶는 중에 파일 크기가 변경됐어요: {0}
Arsip melebihi 4 GB | アーカイブが4 GBを超えています | 压缩包超过 4 GB | 아카이브가 4 GB를 초과해요
Password (encrypted NOA) | パスワード (暗号化NOA) | 密码（加密的 NOA） | 비밀번호 (암호화된 NOA)
Password (NOA terenkripsi) | パスワード (暗号化NOA) | 密码（加密的 NOA） | 비밀번호 (암호화된 NOA)
Only for BSHF-encrypted entries. Key Finder can read it from the game's .exe (IDR_COTOMI). | BSHF暗号化エントリ専用です。キー検索で、ゲームの.exe (IDR_COTOMI) から読み取れます。 | 仅用于 BSHF 加密的条目。密钥查找器可从游戏的 .exe（IDR_COTOMI）中读取。 | BSHF 암호화 항목 전용이에요. 키 찾기로 게임의 .exe(IDR_COTOMI)에서 읽을 수 있어요.
Hanya untuk entri terenkripsi BSHF. Pencari Kunci bisa membacanya dari .exe game (IDR_COTOMI). | BSHF暗号化エントリ専用です。キー検索で、ゲームの.exe (IDR_COTOMI) から読み取れます。 | 仅用于 BSHF 加密的条目。密钥查找器可从游戏的 .exe（IDR_COTOMI）中读取。 | BSHF 암호화 항목 전용이에요. 키 찾기로 게임의 .exe(IDR_COTOMI)에서 읽을 수 있어요.
Password (empty = no encryption) | パスワード (空欄 = 暗号化なし) | 密码（留空 = 不加密） | 비밀번호 (비워 두면 암호화 안 함)
Password (kosong = tanpa enkripsi) | パスワード (空欄 = 暗号化なし) | 密码（留空 = 不加密） | 비밀번호 (비워 두면 암호화 안 함)
Filled: every file is BSHF-encrypted with this password. Compression (ERISA) is not available when packing. | 入力した場合: すべてのファイルがこのパスワードでBSHF暗号化されます。パック時は圧縮 (ERISA) を利用できません。 | 填写后：所有文件都将用此密码进行 BSHF 加密。打包时无法使用压缩（ERISA）。 | 입력하면 모든 파일이 이 비밀번호로 BSHF 암호화돼요. 묶을 때는 압축(ERISA)을 사용할 수 없어요.
Diisi: semua file dienkripsi BSHF dengan password ini. Kompresi (ERISA) tidak tersedia saat kemas. | 入力した場合: すべてのファイルがこのパスワードでBSHF暗号化されます。パック時は圧縮 (ERISA) を利用できません。 | 填写后：所有文件都将用此密码进行 BSHF 加密。打包时无法使用压缩（ERISA）。 | 입력하면 모든 파일이 이 비밀번호로 BSHF 암호화돼요. 묶을 때는 압축(ERISA)을 사용할 수 없어요.
Bukan arsip NOA | NOAアーカイブではありません | 不是 NOA 压缩包 | NOA 아카이브가 아니에요
EMSAC-Binary Archive | EMSAC-Binary Archive | EMSAC-Binary Archive | EMSAC-Binary Archive
NOA kosong atau indeks tidak terbaca | NOAが空、またはインデックスを読み取れません | NOA 为空或无法读取索引 | NOA가 비어 있거나 인덱스를 읽을 수 없어요
Indeks NOA di luar arsip | NOAのインデックスがアーカイブの範囲外です | NOA 索引超出压缩包范围 | NOA 인덱스가 아카이브 범위를 벗어났어요
Catatan DirEntry tidak ditemukan | DirEntryレコードが見つかりません | 未找到 DirEntry 记录 | DirEntry 레코드를 찾을 수 없어요
Ukuran DirEntry tidak valid | DirEntryのサイズが無効です | DirEntry 大小无效 | DirEntry 크기가 올바르지 않아요
Jumlah entri NOA tidak valid | NOAのエントリ数が無効です | NOA 条目数量无效 | NOA 항목 수가 올바르지 않아요
Indeks NOA terpotong | NOAのインデックスが途中で切れています | NOA 索引被截断 | NOA 인덱스가 잘려 있어요
Indeks NOA rusak | NOAのインデックスが破損しています | NOA 索引已损坏 | NOA 인덱스가 손상됐어요
Nama NOA rusak | NOAの名前が破損しています | NOA 名称已损坏 | NOA 이름이 손상됐어요
Catatan filedata rusak: {0} | filedataレコードが破損しています: {0} | filedata 记录已损坏：{0} | filedata 레코드가 손상됐어요: {0}
Skema enkripsi 0x{0} belum didukung ({1}) | 暗号化方式 0x{0} は未対応です ({1}) | 尚不支持加密方案 0x{0}（{1}） | 암호화 방식 0x{0}({1})은(는) 아직 지원되지 않아요
Entri terenkripsi: isi opsi Password (cari dengan Pencari Kunci dari .exe game) | 暗号化エントリです。パスワードオプションを入力してください (キー検索でゲームの.exeから取得できます) | 条目已加密：请填写密码选项（可用密钥查找器从游戏的 .exe 中获取） | 암호화된 항목이에요. 비밀번호 옵션을 입력하세요 (키 찾기로 게임의 .exe에서 찾을 수 있어요)
Checksum tidak cocok untuk {0} — password kemungkinan salah | {0} のチェックサムが一致しません。パスワードが間違っている可能性があります | {0} 的校验和不匹配，密码可能有误 | {0}의 체크섬이 일치하지 않아요. 비밀번호가 틀렸을 수 있어요
Dekompresi ERISA berakhir lebih awal: {0} | ERISAの展開が途中で終了しました: {0} | ERISA 解压提前结束：{0} | ERISA 압축 해제가 일찍 끝났어요: {0}
ERISA-Archive file | ERISA-Archive file | ERISA-Archive file | ERISA-Archive file
Index PFS tidak valid | PFSのインデックスが無効です | PFS 索引无效 | PFS 인덱스가 올바르지 않아요
Jumlah entri pf2 tidak valid | pf2のエントリ数が無効です | pf2 条目数量无效 | pf2 항목 수가 올바르지 않아요
Index pf2 terpotong | pf2のインデックスが途中で切れています | pf2 索引被截断 | pf2 인덱스가 잘려 있어요
Archive version | アーカイブのバージョン | 压缩包版本 | 아카이브 버전
Versi arsip | アーカイブのバージョン | 压缩包版本 | 아카이브 버전
Auto (pf8, most common) | 自動 (pf8、最も一般的) | 自动（pf8，最常见） | 자동 (pf8, 가장 일반적)
Auto (pf8, paling umum) | 自動 (pf8、最も一般的) | 自动（pf8，最常见） | 자동 (pf8, 가장 일반적)
pf8 (encrypted, newer Artemis) | pf8 (暗号化、新しいArtemis) | pf8（加密，较新的 Artemis） | pf8 (암호화, 최신 Artemis)
pf8 (terenkripsi, Artemis baru) | pf8 (暗号化、新しいArtemis) | pf8（加密，较新的 Artemis） | pf8 (암호화, 최신 Artemis)
pf2 (old, plain) | pf2 (旧形式、暗号化なし) | pf2（旧版，未加密） | pf2 (구형, 암호화 없음)
pf2 (lama, polos) | pf2 (旧形式、暗号化なし) | pf2（旧版，未加密） | pf2 (구형, 암호화 없음)
Bukan arsip QLIE | QLIEアーカイブではありません | 不是 QLIE 压缩包 | QLIE 아카이브가 아니에요
Index QLIE tidak valid | QLIEのインデックスが無効です | QLIE 索引无效 | QLIE 인덱스가 올바르지 않아요
Index terpotong | インデックスが途中で切れています | 索引被截断 | 인덱스가 잘려 있어요
Panjang nama tidak valid | 名前の長さが無効です | 名称长度无效 | 이름 길이가 올바르지 않아요
Nama QLIE tidak terbaca | QLIEの名前を読み取れません | 无法读取 QLIE 名称 | QLIE 이름을 읽을 수 없어요
Entri QLIE di luar arsip | QLIEのエントリがアーカイブの範囲外です | QLIE 条目超出压缩包范围 | QLIE 항목이 아카이브 범위를 벗어났어요
Index QLIE tidak pas | QLIEのインデックスが合いません | QLIE 索引不匹配 | QLIE 인덱스가 맞지 않아요
File terkompresi/terenkripsi QLIE (butuh kunci game, belum didukung) | QLIEの圧縮/暗号化ファイルです (ゲームのキーが必要で、未対応です) | QLIE 压缩/加密文件（需要游戏密钥，尚不支持） | QLIE 압축/암호화 파일이에요 (게임 키가 필요하며 아직 지원되지 않아요)
Pack QLIE belum didukung | QLIEのパックは未対応です | 尚不支持打包 QLIE | QLIE 묶기는 아직 지원되지 않아요
Arsip RGSSAD rusak | RGSSADアーカイブが破損しています | RGSSAD 压缩包已损坏 | RGSSAD 아카이브가 손상됐어요
File terlalu besar: {0} | ファイルが大きすぎます: {0} | 文件过大：{0} | 파일이 너무 커요: {0}
RGSS3A (VX Ace) | RGSS3A (VX Ace) | RGSS3A (VX Ace) | RGSS3A (VX Ace)
Arsip RGSS3A rusak | RGSS3Aアーカイブが破損しています | RGSS3A 压缩包已损坏 | RGSS3A 아카이브가 손상됐어요
Terlalu banyak file | ファイルが多すぎます | 文件过多 | 파일이 너무 많아요
Arsip melebihi 2 GB | アーカイブが2 GBを超えています | 压缩包超过 2 GB | 아카이브가 2 GB를 초과해요
Header RPA tidak valid | RPAのヘッダーが無効です | RPA 头部无效 | RPA 헤더가 올바르지 않아요
Versi tidak didukung: {0} | 未対応のバージョンです: {0} | 不支持的版本：{0} | 지원되지 않는 버전이에요: {0}
Index RPA bukan dict | RPAのインデックスがdictではありません | RPA 索引不是 dict | RPA 인덱스가 dict가 아니에요
RPA version | RPAバージョン | RPA 版本 | RPA 버전
Versi RPA | RPAバージョン | RPA 版本 | RPA 버전
RPA-3.0 (Ren'Py 6.x - 8.x, recommended) | RPA-3.0 (Ren'Py 6.x - 8.x、推奨) | RPA-3.0（Ren'Py 6.x - 8.x，推荐） | RPA-3.0 (Ren'Py 6.x - 8.x, 권장)
RPA-3.0 (Ren'Py 6.x - 8.x, disarankan) | RPA-3.0 (Ren'Py 6.x - 8.x、推奨) | RPA-3.0（Ren'Py 6.x - 8.x，推荐） | RPA-3.0 (Ren'Py 6.x - 8.x, 권장)
RPA-2.0 (old Ren'Py 6.x) | RPA-2.0 (旧Ren'Py 6.x) | RPA-2.0（旧版 Ren'Py 6.x） | RPA-2.0 (구형 Ren'Py 6.x)
RPA-2.0 (Ren'Py 6.x lama) | RPA-2.0 (旧Ren'Py 6.x) | RPA-2.0（旧版 Ren'Py 6.x） | RPA-2.0 (구형 Ren'Py 6.x)
Choose RPA-3.0 if you are not sure. | よくわからない場合はRPA-3.0を選んでください。 | 不确定时请选择 RPA-3.0。 | 잘 모르겠다면 RPA-3.0을 선택하세요.
Pilih RPA-3.0 jika tidak mengetahuinya. | よくわからない場合はRPA-3.0を選んでください。 | 不确定时请选择 RPA-3.0。 | 잘 모르겠다면 RPA-3.0을 선택하세요.
XOR key (hex, optional) | XORキー (hex、任意) | XOR 密钥（hex，可选） | XOR 키 (hex, 선택)
Kunci XOR (hex, opsional) | XORキー (hex、任意) | XOR 密钥（hex，可选） | XOR 키 (hex, 선택)
Up to 8 hex digits (Ren'Py default: DEADBEEF). Only used by RPA-3.0. | 最大8桁のhex (Ren'Pyの既定値: DEADBEEF)。RPA-3.0のみで使用されます。 | 最多 8 位 hex（Ren'Py 默认值：DEADBEEF）。仅用于 RPA-3.0。 | 최대 8자리 hex (Ren'Py 기본값: DEADBEEF). RPA-3.0에서만 사용돼요.
Maks 8 digit hex (bawaan Ren'Py: DEADBEEF). Hanya untuk RPA-3.0. | 最大8桁のhex (Ren'Pyの既定値: DEADBEEF)。RPA-3.0のみで使用されます。 | 最多 8 位 hex（Ren'Py 默认值：DEADBEEF）。仅用于 RPA-3.0。 | 최대 8자리 hex (Ren'Py 기본값: DEADBEEF). RPA-3.0에서만 사용돼요.
Exe key (only if the game needs it) | exeキー (ゲームが必要とする場合のみ) | exe 密钥（仅在游戏需要时） | exe 키 (게임에서 필요한 경우에만)
Kunci exe (hanya jika game memerlukannya) | exeキー (ゲームが必要とする場合のみ) | exe 密钥（仅在游戏需要时） | exe 키 (게임에서 필요한 경우에만)
Exe key (only if the game needs it). Leave empty: for Scene.pck the key is found automatically. Gameexe.dat needs the key if it is encrypted (use Key Finder): 16 hex bytes (e.g. 0x12, 0x34, … or 32 hex digits) or the 暗号 text. | exeキー (ゲームが必要とする場合のみ)。空欄のままにしてください: Scene.pckではキーが自動的に検出されます。Gameexe.datが暗号化されている場合はキーが必要です (キー検索を使用)。16バイトのhex (例: 0x12, 0x34, … または32桁のhex) または暗号テキストを入力します。 | exe 密钥（仅在游戏需要时）。请留空：Scene.pck 的密钥会自动查找。若 Gameexe.dat 已加密则需要密钥（使用密钥查找器）：16 个 hex 字节（例如 0x12, 0x34, … 或 32 位 hex）或 暗号 文本。 | exe 키 (게임에서 필요한 경우에만). 비워 두세요: Scene.pck는 키를 자동으로 찾아요. Gameexe.dat가 암호화되어 있으면 키가 필요해요 (키 찾기 사용): 16바이트 hex (예: 0x12, 0x34, … 또는 32자리 hex) 또는 暗号 텍스트.
Kunci exe (hanya jika game memerlukannya). Kosongkan: untuk Scene.pck kunci dicari otomatis. Gameexe.dat yang terenkripsi butuh kunci (cari dengan Pencari Kunci): 16 byte hex (mis. 0x12, 0x34, … atau 32 digit hex) atau teks 暗号. | exeキー (ゲームが必要とする場合のみ)。空欄のままにしてください: Scene.pckではキーが自動的に検出されます。Gameexe.datが暗号化されている場合はキーが必要です (キー検索を使用)。16バイトのhex (例: 0x12, 0x34, … または32桁のhex) または暗号テキストを入力します。 | exe 密钥（仅在游戏需要时）。请留空：Scene.pck 的密钥会自动查找。若 Gameexe.dat 已加密则需要密钥（使用密钥查找器）：16 个 hex 字节（例如 0x12, 0x34, … 或 32 位 hex）或 暗号 文本。 | exe 키 (게임에서 필요한 경우에만). 비워 두세요: Scene.pck는 키를 자동으로 찾아요. Gameexe.dat가 암호화되어 있으면 키가 필요해요 (키 찾기 사용): 16바이트 hex (예: 0x12, 0x34, … 또는 32자리 hex) 또는 暗号 텍스트.
Kunci tidak valid | キーが無効です | 密钥无效 | 키가 올바르지 않아요
Daftar nama scene tidak valid | シーン名リストが無効です | 场景名称列表无效 | 씬 이름 목록이 올바르지 않아요
Data scene di luar arsip | シーンデータがアーカイブの範囲外です | 场景数据超出压缩包范围 | 씬 데이터가 아카이브 범위를 벗어났어요
File {0} tidak ditemukan di folder. Bongkar Scene.pck asli dulu, lalu edit isi folder hasilnya. | フォルダに {0} が見つかりません。まず元のScene.pckを展開し、出力されたフォルダの内容を編集してください。 | 文件夹中未找到 {0}。请先解包原始 Scene.pck，再编辑生成的文件夹内容。 | 폴더에서 {0}을(를) 찾을 수 없어요. 먼저 원본 Scene.pck를 푼 다음, 생성된 폴더의 내용을 편집하세요.
{0} tidak valid | {0} が無効です | {0} 无效 | {0}이(가) 올바르지 않아요
Scene.pck ini memakai kunci exe, tetapi {0} tidak memuat kunci. Isi opsi Kunci (hex 16 byte) — cari dengan Pencari Kunci. | このScene.pckはexeキーを使用していますが、{0} にキーが含まれていません。キーオプション (16バイトのhex) を入力してください。キー検索で探せます。 | 此 Scene.pck 使用 exe 密钥，但 {0} 中不含密钥。请填写密钥选项（16 字节 hex），可用密钥查找器查找。 | 이 Scene.pck는 exe 키를 사용하지만 {0}에 키가 없어요. 키 옵션(16바이트 hex)을 입력하세요. 키 찾기로 찾을 수 있어요.
Scene "{0}.dat" tidak ada di folder | フォルダにシーン「{0}.dat」がありません | 文件夹中没有场景“{0}.dat” | 폴더에 씬 "{0}.dat"가 없어요
Ukuran Scene.pck melebihi 2 GB | Scene.pckのサイズが2 GBを超えています | Scene.pck 大小超过 2 GB | Scene.pck 크기가 2 GB를 초과해요
Gameexe.dat ini terenkripsi dengan kunci exe. Isi opsi Kunci; cari dengan Pencari Kunci (di folder game yang punya Scene.pck). | このGameexe.datはexeキーで暗号化されています。キーオプションを入力してください。キー検索で探せます (Scene.pckがあるゲームのフォルダで)。 | 此 Gameexe.dat 已用 exe 密钥加密。请填写密钥选项，可用密钥查找器查找（在含有 Scene.pck 的游戏文件夹中）。 | 이 Gameexe.dat는 exe 키로 암호화되어 있어요. 키 옵션을 입력하세요. 키 찾기로 찾을 수 있어요 (Scene.pck가 있는 게임 폴더에서).
Kunci exe salah atau Gameexe.dat rusak | exeキーが間違っているか、Gameexe.datが破損しています | exe 密钥错误或 Gameexe.dat 已损坏 | exe 키가 틀렸거나 Gameexe.dat가 손상됐어요
Gameexe.dat tidak valid | Gameexe.datが無効です | Gameexe.dat 无效 | Gameexe.dat가 올바르지 않아요
Gameexe.ini tidak ditemukan di folder | フォルダにGameexe.iniが見つかりません | 文件夹中未找到 Gameexe.ini | 폴더에서 Gameexe.ini를 찾을 수 없어요
OVK (SiglusEngine voice) | OVK (SiglusEngineのボイス) | OVK（SiglusEngine 语音） | OVK (SiglusEngine 음성)
Tabel OVK tidak valid | OVKのテーブルが無効です | OVK 表无效 | OVK 테이블이 올바르지 않아요
Entri OVK di luar arsip | OVKのエントリがアーカイブの範囲外です | OVK 条目超出压缩包范围 | OVK 항목이 아카이브 범위를 벗어났어요
Entri OVK bukan OGG | OVKのエントリがOGGではありません | OVK 条目不是 OGG | OVK 항목이 OGG가 아니에요
Tidak ada file .ogg di folder | フォルダに.oggファイルがありません | 文件夹中没有 .ogg 文件 | 폴더에 .ogg 파일이 없어요
Ukuran OVK melebihi 2 GB | OVKのサイズが2 GBを超えています | OVK 大小超过 2 GB | OVK 크기가 2 GB를 초과해요
ARC (Silky Engine) | ARC (Silky Engine) | ARC（Silky Engine） | ARC (Silky Engine)
Index ARC (Silky) tidak valid | ARC (Silky) のインデックスが無効です | ARC（Silky）索引无效 | ARC(Silky) 인덱스가 올바르지 않아요
Entri ARC (Silky) tidak valid | ARC (Silky) のエントリが無効です | ARC（Silky）条目无效 | ARC(Silky) 항목이 올바르지 않아요
Choose 32 if you are not sure. | よくわからない場合は32を選んでください。 | 不确定时请选择 32。 | 잘 모르겠다면 32를 선택하세요.
Pilih 32 jika tidak mengetahuinya. | よくわからない場合は32を選んでください。 | 不确定时请选择 32。 | 잘 모르겠다면 32를 선택하세요.
Name XOR key 0-255 (0 = none) | 名前のXORキー 0-255 (0 = なし) | 名称 XOR 密钥 0-255（0 = 无） | 이름 XOR 키 0-255 (0 = 없음)
"""
    const val T13 = """
Kunci XOR nama 0-255 (0 = tanpa) | 名前のXORキー 0-255 (0 = なし) | 名称 XOR 密钥 0-255（0 = 无） | 이름 XOR 키 0-255 (0 = 없음)
Same key the original game uses. 0 for most. | 元のゲームと同じキーを指定します。ほとんどの場合は0です。 | 与原游戏使用相同的密钥。大多数情况为 0。 | 원본 게임과 같은 키를 쓰세요. 대부분 0이에요.
Samakan dengan kunci game asli. 0 untuk kebanyakan. | 元のゲームと同じキーを指定します。ほとんどの場合は0です。 | 与原游戏使用相同的密钥。大多数情况为 0。 | 원본 게임과 같은 키를 쓰세요. 대부분 0이에요.
PAC (Softpal ADV System) | PAC (Softpal ADV System) | PAC（Softpal ADV System） | PAC (Softpal ADV System)
Index PAC tidak valid | PACのインデックスが無効です | PAC 索引无效 | PAC 인덱스가 올바르지 않아요
File PAC terlalu kecil | PACファイルが小さすぎます | PAC 文件过小 | PAC 파일이 너무 작아요
Jumlah entri PAC tidak valid | PACのエントリ数が無効です | PAC 条目数量无效 | PAC 항목 수가 올바르지 않아요
Index PAC di luar arsip | PACのインデックスがアーカイブの範囲外です | PAC 索引超出压缩包范围 | PAC 인덱스가 아카이브 범위를 벗어났어요
Layout PAC tidak cocok | PACのレイアウトが一致しません | PAC 布局不匹配 | PAC 레이아웃이 일치하지 않아요
Entri PAC di luar arsip: {0} | PACのエントリがアーカイブの範囲外です: {0} | PAC 条目超出压缩包范围：{0} | PAC 항목이 아카이브 범위를 벗어났어요: {0}
PAC variant | PACバリアント | PAC 变体 | PAC 변형
Varian PAC | PACバリアント | PAC 变体 | PAC 변형
Classic Softpal (no magic) | 従来型Softpal (マジックなし) | 经典 Softpal（无魔数） | 클래식 Softpal (매직 없음)
Softpal klasik (tanpa magic) | 従来型Softpal (マジックなし) | 经典 Softpal（无魔数） | 클래식 Softpal (매직 없음)
Amuse Craft ("PAC " magic) | Amuse Craft (マジック "PAC ") | Amuse Craft（魔数 "PAC "） | Amuse Craft (매직 "PAC ")
Amuse Craft (magic "PAC ") | Amuse Craft (マジック "PAC ") | Amuse Craft（魔数 "PAC "） | Amuse Craft (매직 "PAC ")
Choose Classic if you are not sure. | よくわからない場合は従来型を選んでください。 | 不确定时请选择经典。 | 잘 모르겠다면 클래식을 선택하세요.
Pilih Klasik jika tidak mengetahuinya. | よくわからない場合は従来型を選んでください。 | 不确定时请选择经典。 | 잘 모르겠다면 클래식을 선택하세요.
File ZIP terlalu kecil | ZIPファイルが小さすぎます | ZIP 文件过小 | ZIP 파일이 너무 작아요
Akhir direktori ZIP tidak ditemukan | ZIPのディレクトリ終端が見つかりません | 未找到 ZIP 目录结尾 | ZIP 디렉터리 끝을 찾을 수 없어요
ZIP64 belum didukung | ZIP64は未対応です | 尚不支持 ZIP64 | ZIP64는 아직 지원되지 않아요
Direktori ZIP tidak valid | ZIPのディレクトリが無効です | ZIP 目录无效 | ZIP 디렉터리가 올바르지 않아요
Entri ZIP rusak | ZIPのエントリが破損しています | ZIP 条目已损坏 | ZIP 항목이 손상됐어요
ZIP terenkripsi belum didukung: {0} | 暗号化ZIPは未対応です: {0} | 尚不支持加密 ZIP：{0} | 암호화된 ZIP은 아직 지원되지 않아요: {0}
Metode kompresi {0} belum didukung: {1} | 圧縮メソッド {0} は未対応です: {1} | 尚不支持压缩方法 {0}：{1} | 압축 방식 {0}은(는) 아직 지원되지 않아요: {1}
Header lokal ZIP rusak | ZIPのローカルヘッダーが破損しています | ZIP 本地头已损坏 | ZIP 로컬 헤더가 손상됐어요
Data deflate tidak valid | deflateデータが無効です | deflate 数据无效 | deflate 데이터가 올바르지 않아요
Compression level | 圧縮レベル | 压缩级别 | 압축 수준
Level kompresi | 圧縮レベル | 压缩级别 | 압축 수준
Fast (bigger file) | 高速 (ファイル大) | 快速（文件较大） | 빠름 (파일 큼)
Cepat (file lebih besar) | 高速 (ファイル大) | 快速（文件较大） | 빠름 (파일 큼)
Terbaik (lebih lambat) | 最高 (低速) | 最佳（较慢） | 최상 (느림)
Index ARC tidak valid | ARCのインデックスが無効です | ARC 索引无效 | ARC 인덱스가 올바르지 않아요
Header ARC tidak valid | ARCのヘッダーが無効です | ARC 头部无效 | ARC 헤더가 올바르지 않아요
Ekstensi tidak valid | 無効な拡張子です | 扩展名无效 | 확장자가 올바르지 않아요
Tabel ARC di luar arsip | ARCのテーブルがアーカイブの範囲外です | ARC 表超出压缩包范围 | ARC 테이블이 아카이브 범위를 벗어났어요
Entri ARC tidak valid | ARCのエントリが無効です | ARC 条目无效 | ARC 항목이 올바르지 않아요
Auto (by longest name) | 自動 (最も長い名前に合わせる) | 自动（按最长名称） | 자동 (가장 긴 이름 기준)
Auto (sesuai nama terpanjang) | 自動 (最も長い名前に合わせる) | 自动（按最长名称） | 자동 (가장 긴 이름 기준)
Choose Auto if you are not sure. | よくわからない場合は自動を選んでください。 | 不确定时请选择自动。 | 잘 모르겠다면 자동을 선택하세요.
Pilih Auto jika tidak mengetahuinya. | よくわからない場合は自動を選んでください。 | 不确定时请选择自动。 | 잘 모르겠다면 자동을 선택하세요.
Ekstensi tidak didukung: {0} | 未対応の拡張子です: {0} | 不支持的扩展名：{0} | 지원되지 않는 확장자예요: {0}
Nama file terlalu panjang (maks 12 byte tanpa ekstensi) | ファイル名が長すぎます (拡張子を除いて最大12バイト) | 文件名过长（不含扩展名最多 12 字节） | 파일 이름이 너무 길어요 (확장자 제외 최대 12바이트)
Nama file terlalu panjang untuk 8 byte (maks 8 byte tanpa ekstensi) | ファイル名が8バイトには長すぎます (拡張子を除いて最大8バイト) | 文件名超出 8 字节限制（不含扩展名最多 8 字节） | 파일 이름이 8바이트로는 너무 길어요 (확장자 제외 최대 8바이트)
Terlalu banyak jenis ekstensi | 拡張子の種類が多すぎます | 扩展名种类过多 | 확장자 종류가 너무 많아요
Offset index XP3 tidak valid | XP3のインデックスオフセットが無効です | XP3 索引偏移量无效 | XP3 인덱스 오프셋이 올바르지 않아요
Flag index XP3 tidak didukung: {0} | XP3のインデックスフラグは未対応です: {0} | 尚不支持 XP3 索引标志：{0} | XP3 인덱스 플래그는 아직 지원되지 않아요: {0}
Segmen XP3 terlalu besar | XP3のセグメントが大きすぎます | XP3 分段过大 | XP3 세그먼트가 너무 커요
Kunci XOR 0-255 (opsional) | XORキー 0-255 (任意) | XOR 密钥 0-255（可选） | XOR 키 0-255 (선택)
Leave empty for no encryption. Must match the key the game expects. | 暗号化しない場合は空欄にしてください。ゲームが想定するキーと一致させる必要があります。 | 不加密时请留空。必须与游戏所需的密钥一致。 | 암호화하지 않으면 비워 두세요. 게임에서 기대하는 키와 같아야 해요.
Kosongkan bila tidak terenkripsi. Harus sama dengan kunci yang dipakai game. | 暗号化しない場合は空欄にしてください。ゲームが想定するキーと一致させる必要があります。 | 不加密时请留空。必须与游戏所需的密钥一致。 | 암호화하지 않으면 비워 두세요. 게임에서 기대하는 키와 같아야 해요.
Header YPF tidak valid | YPFのヘッダーが無効です | YPF 头部无效 | YPF 헤더가 올바르지 않아요
Index YPF tidak valid | YPFのインデックスが無効です | YPF 索引无效 | YPF 인덱스가 올바르지 않아요
Nama YPF tidak terbaca | YPFの名前を読み取れません | 无法读取 YPF 名称 | YPF 이름을 읽을 수 없어요
Pack YPF belum didukung | YPFのパックは未対応です | 尚不支持打包 YPF | YPF 묶기는 아직 지원되지 않아요
Thumbs.db, .DS_Store, desktop.ini | Thumbs.db, .DS_Store, desktop.ini | Thumbs.db, .DS_Store, desktop.ini | Thumbs.db, .DS_Store, desktop.ini
Root folder tidak ada | ルートフォルダがありません | 没有根文件夹 | 루트 폴더가 없어요
Gagal membuat folder {0} | フォルダ {0} を作成できませんでした | 创建文件夹 {0} 失败 | 폴더 {0}을(를) 만들지 못했어요
Nama file kosong | ファイル名が空です | 文件名为空 | 파일 이름이 비어 있어요
Gagal membuat file {0} | ファイル {0} を作成できませんでした | 创建文件 {0} 失败 | 파일 {0}을(를) 만들지 못했어요
Gagal membuka {0} | {0} を開けませんでした | 无法打开 {0} | {0}을(를) 열지 못했어요
FuruArc log | FuruArcログ | FuruArc 日志 | FuruArc 로그
Lihat detail (?) | 詳細を見る (?) | 查看详情 (?) | 자세히 보기 (?)
See details (?) | 詳細を見る (?) | 查看详情 (?) | 자세히 보기 (?)
Pergi ke halaman | ページへ移動 | 转到页面 | 페이지로 이동
BGI / Ethornell | BGI / Ethornell | BGI / Ethornell | BGI / Ethornell
Tyrano Script | Tyrano Script | Tyrano Script | Tyrano Script
Silky Engine | Silky Engine | Silky Engine | Silky Engine
Siglus Engine (Gameexe.dat) | Siglus Engine (Gameexe.dat) | Siglus Engine (Gameexe.dat) | Siglus Engine (Gameexe.dat)
Siglus Engine (Voice OVK) | Siglus Engine (Voice OVK) | Siglus Engine (Voice OVK) | Siglus Engine (Voice OVK)
Voice OVK | Voice OVK | Voice OVK | Voice OVK
Yaneurao Itufuru | Yaneurao Itufuru | Yaneurao Itufuru | Yaneurao Itufuru
Adv32 (Circus) DAT | Adv32 (Circus) DAT | Adv32 (Circus) DAT | Adv32 (Circus) DAT
Adv32 (Circus) CRM | Adv32 (Circus) CRM | Adv32 (Circus) CRM | Adv32 (Circus) CRM
RPGM MV | RPGM MV | RPGM MV | RPGM MV
RPGM MZ | RPGM MZ | RPGM MZ | RPGM MZ
RPGM XP | RPGM XP | RPGM XP | RPGM XP
RPGM VX | RPGM VX | RPGM VX | RPGM VX
RPGM VX ACE | RPGM VX ACE | RPGM VX ACE | RPGM VX ACE
GBK (Mandarin sederhana) | GBK (簡体字中国語) | GBK（简体中文） | GBK (중국어 간체)
Big5 (Mandarin tradisional) | Big5 (繁体字中国語) | Big5（繁体中文） | Big5 (중국어 번체)
txt, ks, tjs, ini, csv, srt, ass, lrc, log | txt, ks, tjs, ini, csv, srt, ass, lrc, log | txt, ks, tjs, ini, csv, srt, ass, lrc, log | txt, ks, tjs, ini, csv, srt, ass, lrc, log
JSON VNTP | JSON VNTP | JSON VNTP | JSON VNTP
Manage the Game List | ゲームリストの管理 | 管理游戏列表 | 게임 목록 관리
Tap the settings icon (two sliders) on the main Game Assets screen to open the list order and three tools: find covers online, add a main folder (every subfolder inside is added as a game at once; ones already listed are skipped), and delete the whole list (only the list; the real folders and files are safe). | ゲームアセットのメイン画面で設定アイコン（2本のスライダー）をタップすると、リストの並び順と3つのツールが開きます：カバーをオンラインで探す、メインフォルダを追加（中のサブフォルダがすべて一度にゲームとして追加され、すでにあるものはスキップ）、リスト全体を削除（リストのみで、実際のフォルダとファイルは安全です）。 | 在游戏资源主界面点击设置图标（两个滑块）可打开列表排序和三个工具：在线查找封面、添加主文件夹（其中所有子文件夹一次性作为游戏添加，已在列表中的会跳过）、删除整个列表（仅删除列表，实际的文件夹和文件不受影响）。 | 게임 에셋 메인 화면에서 설정 아이콘(슬라이더 두 개)을 누르면 목록 정렬과 세 가지 도구가 열립니다: 온라인 커버 찾기, 메인 폴더 추가(안의 모든 하위 폴더가 한 번에 게임으로 추가되며 이미 있는 것은 건너뜀), 목록 전체 삭제(목록만 삭제되며 실제 폴더와 파일은 안전합니다).
"""
    private val T14 = """
Yu-Ris (YPF) | Yu-Ris (YPF) | Yu-Ris (YPF) | Yu-Ris (YPF)
Pick the Yu-Ris engine in the Engine List. Unpack: .ypf (versions 234–500) with names and compressed files. Pack: builds a new .ypf (default version 494, enter 480–500; name/data hashes are computed automatically, .ybn files are compressed). Script Tools: export/import text from ysbin/yst*.ybn scripts (WORD commands and strings inside expressions, Shift-JIS); the XOR key is read automatically and every offset is shifted, so text length is free. Flow: unpack data.ypf/Scripts.ypf → export text → translate → import → repack. Not supported yet: YPF embedded in an .exe, and older scripts (version < 480) were tested only on a v494 sample. | エンジン一覧で Yu-Ris エンジンを選びます。展開：.ypf（バージョン 234〜500）を名前と圧縮ファイル付きで展開。パック：新しい .ypf を作成（既定はバージョン 494、480〜500 を入力可。名前/データのハッシュは自動計算、.ybn は圧縮されます）。スクリプトツール：ysbin/yst*.ybn スクリプトのテキストをエクスポート/インポート（WORD コマンドと式内の文字列、Shift-JIS）。XOR キーは自動で読み取られ、すべてのオフセットがずらされるため、テキストの長さは自由です。手順：data.ypf/Scripts.ypf を展開 → テキストをエクスポート → 翻訳 → インポート → 再パック。未対応：.exe に埋め込まれた YPF。古いスクリプト（バージョン 480 未満）は v494 のサンプルでのみ検証しています。 | 在引擎列表中选择 Yu-Ris 引擎。解包：.ypf（版本 234–500），包含名称和压缩文件。打包：生成新的 .ypf（默认版本 494，可填 480–500；名称/数据哈希自动计算，.ybn 会被压缩）。脚本工具：导出/导入 ysbin/yst*.ybn 脚本的文本（WORD 命令和表达式内的字符串，Shift-JIS）；XOR 密钥自动读取，所有偏移量都会随之移动，因此文本长度不受限制。流程：解包 data.ypf/Scripts.ypf → 导出文本 → 翻译 → 导入 → 重新打包。暂不支持：嵌入 .exe 的 YPF；旧版脚本（版本 < 480）仅在 v494 样本上测试过。 | 엔진 목록에서 Yu-Ris 엔진을 선택하세요. 풀기: .ypf(버전 234–500)를 이름과 압축 파일까지 풉니다. 묶기: 새 .ypf를 만듭니다(기본 버전 494, 480–500 입력 가능, 이름/데이터 해시는 자동 계산, .ybn은 압축됨). 스크립트 도구: ysbin/yst*.ybn 스크립트의 텍스트 내보내기/가져오기(WORD 명령과 식 안의 문자열, Shift-JIS). XOR 키는 자동으로 읽고 모든 오프셋을 옮기므로 텍스트 길이는 자유입니다. 순서: data.ypf/Scripts.ypf 풀기 → 텍스트 내보내기 → 번역 → 가져오기 → 다시 묶기. 아직 지원하지 않음: .exe에 붙은 YPF, 그리고 구버전 스크립트(버전 480 미만)는 v494 샘플로만 테스트했습니다.
Shift-JIS texts from Yu-Ris .ybn scripts (ysbin/yst*.ybn in data.ypf): WORD commands (dialogue) and non-ASCII string literals inside expressions (choices, names, etc.). The XOR key is read automatically. Import rebuilds the expression section and shifts every offset, so text length is free. The .ybn size changes, so repack the .ypf from the Pack menu. | Yu-Ris の .ybn スクリプト（data.ypf 内の ysbin/yst*.ybn）の Shift-JIS テキスト：WORD コマンド（台詞）と、式内の ASCII 以外の文字列リテラル（選択肢、名前など）。XOR キーは自動で読み取られます。インポートでは式セクションを再構築してすべてのオフセットをずらすため、テキストの長さは自由です。.ybn のサイズが変わるので、パックメニューから .ypf を再パックしてください。 | 来自 Yu-Ris .ybn 脚本（data.ypf 内的 ysbin/yst*.ybn）的 Shift-JIS 文本：WORD 命令（对话）以及表达式中的非 ASCII 字符串字面量（选项、名称等）。XOR 密钥自动读取。导入时会重建表达式段并移动所有偏移量，因此文本长度不受限制。.ybn 的大小会改变，请通过打包菜单重新打包 .ypf。 | Yu-Ris .ybn 스크립트(data.ypf 안의 ysbin/yst*.ybn)의 Shift-JIS 텍스트: WORD 명령(대사)과 식 안의 ASCII가 아닌 문자열 리터럴(선택지, 이름 등). XOR 키는 자동으로 읽습니다. 가져오기는 식 구역을 다시 만들고 모든 오프셋을 옮기므로 텍스트 길이는 자유입니다. .ybn 크기가 바뀌므로 묶기 메뉴에서 .ypf를 다시 묶으세요.
YPF version (480–500) | YPFバージョン（480〜500） | YPF 版本（480–500） | YPF 버전(480–500)
Bukan skrip YSTB | YSTB スクリプトではありません | 不是 YSTB 脚本 | YSTB 스크립트가 아닙니다
Tidak ada teks (skrip kosong) | テキストがありません（空のスクリプト） | 没有文本（空脚本） | 텍스트가 없습니다(빈 스크립트)
Header YSTB tidak valid | YSTB のヘッダーが無効です | YSTB 头部无效 | YSTB 헤더가 올바르지 않습니다
Versi YPF yang didukung untuk pack: 480–500 | パック対応の YPF バージョン：480〜500 | 打包支持的 YPF 版本：480–500 | 묶기를 지원하는 YPF 버전: 480–500
Versi YPF (480–500) | YPFバージョン（480〜500） | YPF 版本（480–500） | YPF 버전(480–500)
file .ybn | .ybn ファイル | .ybn 文件 | .ybn 파일
"""
    private val T15 = """
Pick an engine, then convert its special assets: images (.crx .pgd .pna .hg2 .hg3 .tlg .eri .g00 .rct .rc8 .ycg) to PNG and audio (.pcm .nwa .mio .emi .ovk) to OGG/WAV | エンジンを選び、専用アセットを変換します：画像（.crx .pgd .pna .hg2 .hg3 .tlg .eri .g00 .rct .rc8 .ycg）を PNG に、音声（.pcm .nwa .mio .emi .ovk）を OGG/WAV に | 选择引擎后转换其专用资源：图像（.crx .pgd .pna .hg2 .hg3 .tlg .eri .g00 .rct .rc8 .ycg）转为 PNG，音频（.pcm .nwa .mio .emi .ovk）转为 OGG/WAV | 엔진을 고른 뒤 전용 에셋을 변환합니다: 이미지(.crx .pgd .pna .hg2 .hg3 .tlg .eri .g00 .rct .rc8 .ycg)는 PNG로, 오디오(.pcm .nwa .mio .emi .ovk)는 OGG/WAV로
Images .crx, .pgd, .pna · audio .pcm | 画像 .crx, .pgd, .pna ・音声 .pcm | 图像 .crx, .pgd, .pna · 音频 .pcm | 이미지 .crx, .pgd, .pna · 오디오 .pcm
Images .pgd | 画像 .pgd | 图像 .pgd | 이미지 .pgd
Audio .ovk (Ogg container) | 音声 .ovk（Ogg コンテナ） | 音频 .ovk（Ogg 容器） | 오디오 .ovk(Ogg 컨테이너)
Images .hg2, .hg3 | 画像 .hg2, .hg3 | 图像 .hg2, .hg3 | 이미지 .hg2, .hg3
Images .eri · audio .emi, .mio | 画像 .eri ・音声 .emi, .mio | 图像 .eri · 音频 .emi, .mio | 이미지 .eri · 오디오 .emi, .mio
Images .tlg · audio .ovk, .mio | 画像 .tlg ・音声 .ovk, .mio | 图像 .tlg · 音频 .ovk, .mio | 이미지 .tlg · 오디오 .ovk, .mio
Images .rct, .rc8 | 画像 .rct, .rc8 | 图像 .rct, .rc8 | 이미지 .rct, .rc8
Images .spm | 画像 .spm | 图像 .spm | 이미지 .spm
Images .g00 · audio .nwa | 画像 .g00 ・音声 .nwa | 图像 .g00 · 音频 .nwa | 이미지 .g00 · 오디오 .nwa
Images .pgd, .ycg | 画像 .pgd, .ycg | 图像 .pgd, .ycg | 이미지 .pgd, .ycg
Pick .crx, .pgd, .pna and/or .pcm (XPCM) files extracted from Circus archives. The type is recognised by extension and the result keeps the name with the new extension. Differential CRX (CRXD) and differential PGD need their base file selected too (same folder). PNA (AdvHD) is an atlas of several images. | Circus アーカイブから展開した .crx、.pgd、.pna、.pcm（XPCM）ファイルを選びます。種類は拡張子で判別され、結果は同じ名前で新しい拡張子になります。差分 CRX（CRXD）と差分 PGD は、基になるファイルも同じフォルダで選ぶ必要があります。PNA（AdvHD）は複数画像のアトラスです。 | 选择从 Circus 档案中提取的 .crx、.pgd、.pna 和/或 .pcm（XPCM）文件。类型按扩展名识别，结果保留原名并换成新扩展名。差分 CRX（CRXD）和差分 PGD 需要同时选择其基础文件（同一文件夹）。PNA（AdvHD）是多张图像的图集。 | Circus 아카이브에서 추출한 .crx, .pgd, .pna, .pcm(XPCM) 파일을 고르세요. 종류는 확장자로 판별하며 결과는 같은 이름에 새 확장자가 붙습니다. 차분 CRX(CRXD)와 차분 PGD는 기반 파일도 같은 폴더에서 함께 선택해야 합니다. PNA(AdvHD)는 여러 이미지를 담은 아틀라스입니다.
Pick .pgd files. Support uses the documented PGD decoder (Softpal family: GE, PGD3, PGD2). No public source for an Artemis-specific PGD variant was found; unrecognised files are reported as failed. | .pgd ファイルを選びます。文書化されている PGD デコーダ（Softpal 系：GE、PGD3、PGD2）を使います。Artemis 専用の PGD 変種に関する公開資料は見つからず、認識できないファイルは失敗として報告されます。 | 选择 .pgd 文件。使用已有文档的 PGD 解码器（Softpal 系列：GE、PGD3、PGD2）。未找到 Artemis 专用 PGD 变体的公开资料；无法识别的文件会报告为失败。 | .pgd 파일을 고르세요. 문서화된 PGD 디코더(Softpal 계열: GE, PGD3, PGD2)를 사용합니다. Artemis 전용 PGD 변형에 대한 공개 자료는 찾지 못했으며, 인식하지 못한 파일은 실패로 보고됩니다.
Pick .ovk files. Each OVK holds many Ogg Vorbis streams, so the result is one .zip of the .ogg files (untouched). The entry table is validated against the file size because OVK has no signature. | .ovk ファイルを選びます。各 OVK には多数の Ogg Vorbis が入っているため、結果は .ogg ファイル（無変更）をまとめた 1 つの .zip になります。OVK には識別子がないため、エントリ表をファイルサイズと照合して検証します。 | 选择 .ovk 文件。每个 OVK 包含多个 Ogg Vorbis 流，因此结果是一个包含这些 .ogg 文件（未改动）的 .zip。由于 OVK 没有标识，会用文件大小校验条目表。 | .ovk 파일을 고르세요. 각 OVK에는 여러 Ogg Vorbis 스트림이 들어 있어 결과는 .ogg 파일(원본 그대로)을 담은 .zip 하나입니다. OVK에는 식별자가 없으므로 항목 표를 파일 크기와 대조해 검증합니다.
Pick .hg2/.hg3 files extracted from CatSystem2 archives. Layers/frames are composed onto the full canvas. HG-3 containing JPEG is decoded with Android's decoder. | CatSystem2 アーカイブから展開した .hg2/.hg3 ファイルを選びます。レイヤー/フレームはキャンバス全体に合成されます。JPEG を含む HG-3 は Android のデコーダで処理します。 | 选择从 CatSystem2 档案中提取的 .hg2/.hg3 文件。图层/帧会合成到完整画布上。含 JPEG 的 HG-3 使用 Android 解码器解码。 | CatSystem2 아카이브에서 추출한 .hg2/.hg3 파일을 고르세요. 레이어/프레임은 전체 캔버스에 합성됩니다. JPEG를 포함한 HG-3은 Android 디코더로 해독합니다.
Pick .eri (image), .mio (Entis audio) and/or .emi files. ERI images: lossless paths only (RunLength, Huffman, Nemesis). MIO audio: lossless and lossy (LOT) ported from GARbro/vgmstream, but the lossy path is completely untested. Honest note: in GARbro .emi is actually an Entis image (EMSAC), not audio, so .emi files are reported as failed. | .eri（画像）、.mio（Entis 音声）、.emi ファイルを選びます。ERI 画像はロスレス経路のみ（RunLength、Huffman、Nemesis）。MIO 音声は GARbro/vgmstream からロスレスとロッシー（LOT）を移植しましたが、ロッシー経路はまったく未検証です。正直な注記：GARbro では .emi は音声ではなく Entis の画像（EMSAC）なので、.emi は失敗として報告されます。 | 选择 .eri（图像）、.mio（Entis 音频）和/或 .emi 文件。ERI 图像：仅支持无损路径（RunLength、Huffman、Nemesis）。MIO 音频：无损和有损（LOT）移植自 GARbro/vgmstream，但有损路径完全未经测试。坦率说明：在 GARbro 中 .emi 实际是 Entis 图像（EMSAC）而非音频，因此 .emi 文件会报告为失败。 | .eri(이미지), .mio(Entis 오디오), .emi 파일을 고르세요. ERI 이미지는 무손실 경로만 지원합니다(RunLength, Huffman, Nemesis). MIO 오디오는 무손실과 손실(LOT)을 GARbro/vgmstream에서 이식했지만 손실 경로는 전혀 검증되지 않았습니다. 솔직한 안내: GARbro에서 .emi는 오디오가 아니라 Entis 이미지(EMSAC)이므로 .emi 파일은 실패로 보고됩니다.
Pick .tlg (TLG0/5/6 images), .ovk (Ogg container, .zip result) and/or .mio (MIO audio) files. | .tlg（TLG0/5/6 画像）、.ovk（Ogg コンテナ、結果は .zip）、.mio（MIO 音声）ファイルを選びます。 | 选择 .tlg（TLG0/5/6 图像）、.ovk（Ogg 容器，结果为 .zip）和/或 .mio（MIO 音频）文件。 | .tlg(TLG0/5/6 이미지), .ovk(Ogg 컨테이너, 결과는 .zip), .mio(MIO 오디오) 파일을 고르세요.
Pick .rct and .rc8 files. The RC8 paired with an RCT (named "<name>_.rc8") is used automatically as the alpha channel when selected in the same folder; a standalone RC8 is converted as a paletted image. | .rct と .rc8 ファイルを選びます。RCT と対になる RC8（名前は "<名前>_.rc8"）は、同じフォルダで選ばれていれば自動的にアルファチャンネルとして使われます。単独の RC8 はパレット画像として変換されます。 | 选择 .rct 和 .rc8 文件。与 RCT 配对的 RC8（名为 "<名称>_.rc8"）若在同一文件夹中被选中，会自动用作 Alpha 通道；单独的 RC8 按调色板图像转换。 | .rct와 .rc8 파일을 고르세요. RCT와 짝인 RC8(이름은 "<이름>_.rc8")이 같은 폴더에서 선택되어 있으면 자동으로 알파 채널로 쓰이며, 단독 RC8은 팔레트 이미지로 변환됩니다.
Pick .spm files. Honest warning: the documented NeXAS SPM (Karaik/NeXAS_DX) holds a sprite map (image names, hitboxes, animation), not pixel data, so it cannot be turned into PNG yet. Files are reported as failed until a sample proves otherwise. | .spm ファイルを選びます。正直な警告：文書化されている NeXAS の SPM（Karaik/NeXAS_DX）は画素データではなくスプライトマップ（画像名、ヒットボックス、アニメーション）なので、まだ PNG に変換できません。サンプルで別の事実が確認されるまで、ファイルは失敗として報告されます。 | 选择 .spm 文件。坦率提醒：已有文档的 NeXAS SPM（Karaik/NeXAS_DX）保存的是精灵图映射（图像名、碰撞框、动画），而不是像素数据，因此暂时无法转换为 PNG。在样本证明并非如此之前，文件会报告为失败。 | .spm 파일을 고르세요. 솔직한 경고: 문서화된 NeXAS SPM(Karaik/NeXAS_DX)은 픽셀 데이터가 아니라 스프라이트 맵(이미지 이름, 히트박스, 애니메이션)이므로 아직 PNG로 바꿀 수 없습니다. 샘플로 다른 사실이 확인될 때까지 파일은 실패로 보고됩니다.
Pick .g00 (type 0, 1, 2 images) and/or .nwa (audio) files. G00 type 3 (XOR'd JPEG) is not supported yet. | .g00（タイプ 0、1、2 の画像）と .nwa（音声）ファイルを選びます。G00 タイプ 3（XOR された JPEG）はまだ未対応です。 | 选择 .g00（类型 0、1、2 的图像）和/或 .nwa（音频）文件。G00 类型 3（XOR 加密的 JPEG）暂不支持。 | .g00(타입 0, 1, 2 이미지)와 .nwa(오디오) 파일을 고르세요. G00 타입 3(XOR된 JPEG)은 아직 지원하지 않습니다.
Pick .pgd and/or .ycg files. YCG covers method 1 only (two zlib streams); method 2 (YSSnp) is not supported yet. PGD uses the Softpal-family decoder; no Yu-Ris-specific PGD variant exists in the reference sources (the documented Yu-Ris image extension is .ycg). | .pgd と .ycg ファイルを選びます。YCG は方式 1（2 本の zlib ストリーム）のみ対応で、方式 2（YSSnp）は未対応です。PGD は Softpal 系デコーダを使います。参照資料には Yu-Ris 専用の PGD 変種はありません（文書化されている Yu-Ris の画像拡張子は .ycg です）。 | 选择 .pgd 和/或 .ycg 文件。YCG 仅支持方式 1（两个 zlib 流）；方式 2（YSSnp）暂不支持。PGD 使用 Softpal 系列解码器；参考资料中不存在 Yu-Ris 专用的 PGD 变体（有文档的 Yu-Ris 图像扩展名是 .ycg）。 | .pgd와 .ycg 파일을 고르세요. YCG는 방식 1(zlib 스트림 2개)만 지원하며 방식 2(YSSnp)는 아직 지원하지 않습니다. PGD는 Softpal 계열 디코더를 씁니다. 참고 자료에는 Yu-Ris 전용 PGD 변형이 없습니다(문서화된 Yu-Ris 이미지 확장자는 .ycg).
Automatic and Always WAV produce .wav (16-bit PCM). OGG: the decoded audio is encoded to Ogg Vorbis 160 kbps (44.1/48 kHz); if the file holds Ogg inside, that Ogg is copied untouched. | 「自動」と「常に WAV」は .wav（16 ビット PCM）を出力します。OGG：デコードした音声を Ogg Vorbis 160 kbps（44.1/48 kHz）にエンコードします。ファイル内に Ogg がある場合は、その Ogg をそのままコピーします。 | “自动”和“始终 WAV”输出 .wav（16 位 PCM）。OGG：将解码后的音频编码为 Ogg Vorbis 160 kbps（44.1/48 kHz）；若文件内含 Ogg，则原样复制该 Ogg。 | '자동'과 '항상 WAV'는 .wav(16비트 PCM)를 만듭니다. OGG: 디코딩한 오디오를 Ogg Vorbis 160 kbps(44.1/48 kHz)로 인코딩하며, 파일 안에 Ogg가 있으면 그 Ogg를 그대로 복사합니다.
All frames (as .zip) | すべてのフレーム（.zip） | 所有帧（.zip） | 모든 프레임(.zip)
Off: only the first frame/layer becomes a .png. On: all frames are saved in one .zip of numbered PNGs. | オフ：最初のフレーム/レイヤーだけが .png になります。オン：すべてのフレームを番号付き PNG の 1 つの .zip に保存します。 | 关闭：仅第一帧/图层保存为 .png。开启：所有帧保存在一个包含编号 PNG 的 .zip 中。 | 끔: 첫 프레임/레이어만 .png가 됩니다. 켬: 모든 프레임을 번호가 붙은 PNG의 .zip 하나로 저장합니다.
All these decoders were written from reference source code (GARbro and other projects) without real sample files; results need testing on each game. Images are saved as 32-bit PNG. | これらのデコーダはすべて、実サンプルなしで参照ソースコード（GARbro などのプロジェクト）から作成されています。結果は各ゲームで確認が必要です。画像は 32 ビット PNG で保存されます。 | 这些解码器均依据参考源码（GARbro 等项目）编写，没有真实样本文件；结果需要在各个游戏中测试。图像保存为 32 位 PNG。 | 이 디코더들은 모두 실제 샘플 없이 참고 소스 코드(GARbro 등)를 바탕으로 작성되었으므로 각 게임에서 결과를 확인해야 합니다. 이미지는 32비트 PNG로 저장됩니다.
Bukan file OVK yang dikenali | 認識できる OVK ファイルではありません | 不是可识别的 OVK 文件 | 인식할 수 있는 OVK 파일이 아닙니다
OVK kosong | OVK が空です | OVK 为空 | OVK가 비어 있습니다
Bukan file NWA yang dikenali | 認識できる NWA ファイルではありません | 不是可识别的 NWA 文件 | 인식할 수 있는 NWA 파일이 아닙니다
Bukan file MIO/EMI yang dikenali | 認識できる MIO/EMI ファイルではありません | 不是可识别的 MIO/EMI 文件 | 인식할 수 있는 MIO/EMI 파일이 아닙니다
Jenis file tidak didukung | 未対応のファイル種別です | 不支持的文件类型 | 지원하지 않는 파일 종류입니다
Special Asset Converter | 専用アセットコンバーター | 专用资源转换器 | 전용 에셋 변환기
In the Media Asset Converter, open "Special Asset Converter" and pick an engine: Adv32 (Circus), Artemis, BGI/Ethornell, CatSystem 2, EntisGLS, Kirikiri, Majiro, NeXAS, Siglus or Yu-Ris. Each engine has converters for its media formats (images to PNG, audio to OGG/WAV; OVK becomes a .zip of .ogg files). All the new decoders were written from reference source code without real samples, so results need testing on the game. Not possible yet: NeXAS SPM (it holds a sprite map, not pixels), YCG method 2, G00 type 3, lossy ERI, and .emi (it is actually an image in GARbro). The lossy MIO path is untested. | メディアアセットコンバーターで「専用アセットコンバーター」を開き、エンジンを選びます：Adv32 (Circus)、Artemis、BGI/Ethornell、CatSystem 2、EntisGLS、Kirikiri、Majiro、NeXAS、Siglus、Yu-Ris。各エンジンにそのメディア形式用のコンバーターがあります（画像は PNG、音声は OGG/WAV。OVK は .ogg をまとめた .zip になります）。新しいデコーダはすべて実サンプルなしで参照ソースコードから作成したため、ゲームでの確認が必要です。未対応：NeXAS の SPM（画素ではなくスプライトマップ）、YCG 方式 2、G00 タイプ 3、ロッシー ERI、.emi（GARbro では画像）。ロッシー MIO 経路は未検証です。 | 在媒体资源转换器中打开“专用资源转换器”并选择引擎：Adv32 (Circus)、Artemis、BGI/Ethornell、CatSystem 2、EntisGLS、Kirikiri、Majiro、NeXAS、Siglus 或 Yu-Ris。每个引擎都有针对其媒体格式的转换器（图像转 PNG，音频转 OGG/WAV；OVK 会变成包含 .ogg 的 .zip）。所有新解码器都是依据参考源码、在没有真实样本的情况下编写的，因此结果需要在游戏中测试。暂不支持：NeXAS SPM（内容是精灵图映射而非像素）、YCG 方式 2、G00 类型 3、有损 ERI，以及 .emi（在 GARbro 中其实是图像）。有损 MIO 路径未经测试。 | 미디어 에셋 변환기에서 "전용 에셋 변환기"를 열고 엔진을 고르세요: Adv32 (Circus), Artemis, BGI/Ethornell, CatSystem 2, EntisGLS, Kirikiri, Majiro, NeXAS, Siglus, Yu-Ris. 엔진마다 해당 미디어 형식용 변환기가 있습니다(이미지는 PNG, 오디오는 OGG/WAV, OVK는 .ogg를 담은 .zip). 새 디코더는 모두 실제 샘플 없이 참고 소스 코드로 작성했으므로 게임에서 결과를 확인해야 합니다. 아직 불가능: NeXAS SPM(픽셀이 아니라 스프라이트 맵), YCG 방식 2, G00 타입 3, 손실 ERI, .emi(GARbro에서는 이미지). 손실 MIO 경로는 검증되지 않았습니다.
Game Assets now also read engine-specific media: .crx, .pgd, .pna, .ycg, .hg2, .hg3, .tlg, .eri, .g00, .rct and .rc8 images show like normal images (tiles, viewer, swipe), and .pcm (XPCM), .nwa, .mio and .ovk audio can be played. Decoded results are kept in a temporary cache. Notes: differential images (CRXD, PGD3, RCT overlay) cannot be previewed because their base file is not looked up; OVK plays only its first entry (use the Special Asset Converter for all of them); Extract/Share still deliver the original file. After updating the app, tap Refresh in the game list so special files are detected. | ゲームアセットがエンジン固有のメディアも読めるようになりました。.crx、.pgd、.pna、.ycg、.hg2、.hg3、.tlg、.eri、.g00、.rct、.rc8 の画像は通常の画像と同じように表示され（タイル、ビューア、スワイプ）、.pcm（XPCM）、.nwa、.mio、.ovk の音声は再生できます。デコード結果は一時キャッシュに保存されます。注意：差分画像（CRXD、PGD3、RCT オーバーレイ）は基になるファイルを探さないためプレビューできません。OVK は最初のエントリのみ再生します（すべては専用アセットコンバーターを使用）。抽出/共有は元のファイルのままです。アプリ更新後は、ゲーム一覧で更新を押して専用ファイルを検出してください。 | 游戏资源现在也能读取引擎专用媒体：.crx、.pgd、.pna、.ycg、.hg2、.hg3、.tlg、.eri、.g00、.rct、.rc8 图像会像普通图像一样显示（方块、查看器、滑动），.pcm（XPCM）、.nwa、.mio、.ovk 音频可以播放。解码结果保存在临时缓存中。注意：差分图像（CRXD、PGD3、RCT 叠加）因不会查找其基础文件而无法预览；OVK 只播放第一个条目（全部请用专用资源转换器）；提取/分享仍然提供原始文件。更新应用后，请在游戏列表中点击刷新以检测专用文件。 | 게임 에셋이 이제 엔진 전용 미디어도 읽습니다. .crx, .pgd, .pna, .ycg, .hg2, .hg3, .tlg, .eri, .g00, .rct, .rc8 이미지는 일반 이미지처럼 표시되고(타일, 뷰어, 스와이프) .pcm(XPCM), .nwa, .mio, .ovk 오디오는 재생할 수 있습니다. 디코딩 결과는 임시 캐시에 저장됩니다. 참고: 차분 이미지(CRXD, PGD3, RCT 오버레이)는 기반 파일을 찾지 않으므로 미리볼 수 없고, OVK는 첫 항목만 재생합니다(전체는 전용 에셋 변환기 사용). 추출/공유는 여전히 원본 파일을 전달합니다. 앱을 업데이트한 뒤에는 게임 목록에서 새로고침을 눌러 전용 파일을 감지하세요.
Ubah format media khusus dari beberapa engine | 複数のエンジンの専用メディア形式を変換 | 转换多个引擎的专用媒体格式 | 여러 엔진의 전용 미디어 형식 변환
Format hasil gambar | 画像の出力形式 | 图像输出格式 | 이미지 출력 형식
Format hasil audio | 音声の出力形式 | 音频输出格式 | 오디오 출력 형식
Dekoder Android gagal membaca gambar | Android のデコーダーが画像を読めませんでした | Android 解码器无法读取图像 | Android 디코더가 이미지를 읽지 못했습니다
Gagal menulis JPG | JPG の書き込みに失敗しました | 写入 JPG 失败 | JPG 쓰기에 실패했습니다
"""
    const val T16 = """
    Support Tools > Text Editor > Structured Text | サポートツール > テキストエディター > 構造化テキスト | 辅助工具 > 文本编辑器 > 结构化文本 | 지원 도구 > 텍스트 편집기 > 구조화 텍스트
    Pick the format you want to edit: JSON, CSV / TSV, INI / CFG, XML or Markdown. All of them use the same Create File / Upload File panel, Output & Result, encoding settings and leave confirmation as Plain Text. The back button in an editor takes you to this format list; XML and Markdown open in a text editor with dedicated highlighting. | 編集したい形式を選びます: JSON、CSV / TSV、INI / CFG、XML、Markdown。どれもプレーンテキストと同じ「ファイルを作成/ファイルをアップロード」パネル、出力と結果、エンコーディング設定、終了時の確認を使います。エディターの戻るボタンでこの形式一覧に戻ります。XML と Markdown は専用の色分け付きテキストエディターで開きます。 | 选择要编辑的格式:JSON、CSV / TSV、INI / CFG、XML 或 Markdown。它们都与纯文本使用相同的“创建文件/上传文件”面板、输出与结果、编码设置和离开确认。编辑器中的返回按钮会回到此格式列表;XML 和 Markdown 会在带专用着色的文本编辑器中打开。 | 편집할 형식을 고르세요: JSON, CSV / TSV, INI / CFG, XML, Markdown. 모두 일반 텍스트와 같은 파일 만들기/파일 올리기 패널, 출력 및 결과, 인코딩 설정, 나갈 때 확인을 사용해요. 편집기의 뒤로 가기 버튼을 누르면 이 형식 목록으로 돌아와요. XML과 Markdown은 전용 색상 표시가 있는 텍스트 편집기에서 열려요.
    Support Tools > Text Editor > Structured Text > JSON | サポートツール > テキストエディター > 構造化テキスト > JSON | 辅助工具 > 文本编辑器 > 结构化文本 > JSON | 지원 도구 > 텍스트 편집기 > 구조화 텍스트 > JSON
    Edits JSON (e.g. RPG Maker data files) as a tree or as text.\n• Tree view: tap an entry holding an object or array to go into it; the path is in the breadcrumb (tap any part, or the ← button, to go up). Tap a plain value to change its key, type (text, number, boolean, null, object, array) and value (a switch for booleans, − and + buttons with a step for numbers). Changing the type converts the value as well as it can.\n• The ⋮ menu on each entry: Edit, Duplicate, Move up, Move down, Sort keys (for objects), Copy path (e.g. events[3].pages[0]) and Delete. The Add button creates a new entry at the level you are viewing.\n• Find searches keys or values across the whole tree; tap a result to jump to it.\n• Text view: a text editor with Find & Replace and a Tidy button. Switching to Tree checks the JSON; if it is invalid, an error message (line and column) appears and you stay in the Text view. An invalid JSON file opens directly in the Text view.\n• Key order and number formatting are kept. While the tree is unchanged, the file is saved with its original text. From a changed Tree view the save format can be Pretty (2-space indentation) or Compact (one line) in JSON Settings; from the Text view the file is saved exactly as typed, and only if the JSON is valid.\n• The ↶ and ↷ buttons in the Tree view undo and redo tree changes (up to 40 steps, limited by memory). | JSON(RPG Maker のデータファイルなど)をツリーまたはテキストとして編集します。\n• ツリー表示: オブジェクトや配列を持つ項目をタップして中に入ります。パスはパンくずリストに表示され、どの部分でも、または ← ボタンで上の階層へ戻れます。通常の値をタップすると、キー、型(テキスト、数値、真偽値、null、オブジェクト、配列)、値を変更できます(真偽値はスイッチ、数値はステップ付きの − と + ボタン)。型を変更すると、値は可能な範囲で変換されます。\n• 各項目の ⋮ メニュー: 編集、複製、上へ移動、下へ移動、キーを並べ替え(オブジェクトのみ)、パスをコピー(例: events[3].pages[0])、削除。「追加」ボタンで、表示中の階層に新しい項目を作成します。\n• 「検索」はツリー全体からキーまたは値を探します。結果をタップするとその場所へ移動します。\n• テキスト表示: 検索と置換、「整形」ボタン付きのテキストエディターです。ツリーに切り替えると JSON が検証され、無効な場合はエラーメッセージ(行と列)が表示され、テキスト表示にとどまります。無効な JSON ファイルは最初からテキスト表示で開きます。\n• キーの順序と数値の書式は保持されます。ツリーを変更していない間は、元のテキストのまま保存されます。変更後のツリー表示からの保存形式は、JSON の設定で「整形」(2スペースのインデント)または「圧縮」(1行)を選べます。テキスト表示からは入力したとおりに保存され、JSON が有効な場合のみ保存できます。\n• ツリー表示の ↶ と ↷ ボタンで、ツリーの変更を元に戻す/やり直しできます(最大40ステップ、メモリにより制限されます)。 | 以树形或文本方式编辑 JSON(例如 RPG Maker 的数据文件)。\n• 树形视图:点按包含对象或数组的条目即可进入;路径显示在面包屑中(点按任意一级或 ← 按钮可返回上一级)。点按普通值可修改其键、类型(文本、数字、布尔值、null、对象、数组)和值(布尔值用开关,数字用带步长的 − 和 + 按钮)。更改类型时会尽可能转换原有的值。\n• 每个条目的 ⋮ 菜单:编辑、复制、上移、下移、按键排序(仅对象)、复制路径(例如 events[3].pages[0])和删除。“添加”按钮会在当前所在层级创建新条目。\n• “查找”会在整个树中搜索键或值,点按结果即可跳转到对应位置。\n• 文本视图:带查找与替换和“整理”按钮的文本编辑器。切换到树形会检查 JSON;若无效,会显示错误信息(行和列),并停留在文本视图。无效的 JSON 文件会直接在文本视图中打开。\n• 键的顺序和数字格式会被保留。树形视图未修改时,文件按原文本保存。修改后的树形视图保存时,可在 JSON 设置中选择“美化”(2 个空格缩进)或“紧凑”(单行);从文本视图保存时则完全按输入内容保存,并且仅在 JSON 有效时才能保存。\n• 树形视图中的 ↶ 和 ↷ 按钮用于撤销和重做树的更改(最多 40 步,受内存限制)。 | JSON(예: RPG Maker 데이터 파일)을 트리나 텍스트로 편집해요.\n• 트리 보기: 객체나 배열이 들어 있는 항목을 눌러 안으로 들어가요. 경로는 위쪽 경로 표시줄에 나오며, 아무 부분이나 ← 버튼을 눌러 위로 올라갈 수 있어요. 일반 값을 누르면 키, 유형(텍스트, 숫자, 불리언, null, 객체, 배열), 값을 바꿀 수 있어요(불리언은 스위치, 숫자는 단계가 있는 − 와 + 버튼). 유형을 바꾸면 값도 가능한 한 변환돼요.\n• 각 항목의 ⋮ 메뉴: 편집, 복제, 위로 이동, 아래로 이동, 키 정렬(객체만), 경로 복사(예: events[3].pages[0]), 삭제. 추가 버튼은 지금 보고 있는 단계에 새 항목을 만들어요.\n• 찾기는 트리 전체에서 키나 값을 검색하고, 결과를 누르면 그 위치로 이동해요.\n• 텍스트 보기: 찾기 및 바꾸기와 정리 버튼이 있는 텍스트 편집기예요. 트리로 전환하면 JSON을 검사하고, 올바르지 않으면 오류 메시지(줄과 열)가 나오며 텍스트 보기에 머물러요. 올바르지 않은 JSON 파일은 처음부터 텍스트 보기로 열려요.\n• 키 순서와 숫자 형식은 유지돼요. 트리를 바꾸지 않았다면 원본 텍스트 그대로 저장돼요. 바꾼 트리 보기에서 저장할 때는 JSON 설정에서 보기 좋게(공백 2칸 들여쓰기) 또는 압축(한 줄)을 고를 수 있고, 텍스트 보기에서는 입력한 그대로 저장되며 JSON이 올바를 때만 저장돼요.\n• 트리 보기의 ↶ 와 ↷ 버튼으로 트리 변경을 실행 취소하고 다시 실행할 수 있어요(최대 40단계, 메모리에 따라 제한돼요).
    Support Tools > Text Editor > Structured Text > INI / CFG | サポートツール > テキストエディター > 構造化テキスト > INI / CFG | 辅助工具 > 文本编辑器 > 结构化文本 > INI / CFG | 지원 도구 > 텍스트 편집기 > 구조화 텍스트 > INI / CFG
    Edits INI, CFG or properties files.\n• The first list holds the sections ([name]); keys above the first section go under (global). Tap a section to see its key=value pairs.\n• Tap a pair to change its key and value; the ⋮ menu has Edit and Delete. The ＋ New key button adds at the end of the section, and ＋ New section adds a section at the end of the file. The ⋮ menu on a section: Rename section and Delete (with its contents).\n• Comments, blank lines, spacing and the original order are not changed; only the lines you change are rewritten. ↶ and ↷ undo and redo (up to 40 steps).\n• Only the = sign is recognised as the separator; if your file uses a different format, open it with Plain Text. | INI、CFG、properties ファイルを編集します。\n• 最初の一覧にはセクション([名前])が並びます。最初のセクションより前のキーは(グローバル)に入ります。セクションをタップすると key=value のペアが表示されます。\n• ペアをタップするとキーと値を変更できます。⋮ メニューには編集と削除があります。「＋ 新しいキー」はセクションの末尾に、「＋ 新しいセクション」はファイルの末尾にセクションを追加します。セクションの ⋮ メニュー: セクション名を変更、削除(内容も含む)。\n• コメント、空行、スペース、元の順序は変更されず、変更した行だけが書き直されます。↶ と ↷ で元に戻す/やり直しができます(最大40ステップ)。\n• 区切りとして認識されるのは = だけです。ファイルの形式が異なる場合は、プレーンテキストで開いてください。 | 编辑 INI、CFG 或 properties 文件。\n• 第一个列表显示各节([名称]);第一个节之前的键归入(全局)。点按某个节可查看其 key=value 键值对。\n• 点按键值对可修改其键和值;⋮ 菜单包含编辑和删除。“＋ 新建键”会添加到该节末尾,“＋ 新建节”会在文件末尾添加节。节上的 ⋮ 菜单:重命名节和删除(包括其内容)。\n• 注释、空行、空格和原有顺序都不会改变;只有您修改的行才会被重写。↶ 和 ↷ 用于撤销和重做(最多 40 步)。\n• 只有 = 号会被识别为分隔符;如果文件格式不同,请用纯文本打开。 | INI, CFG, properties 파일을 편집해요.\n• 첫 목록에는 섹션([이름])이 나와요. 첫 섹션보다 위에 있는 키는 (전역)에 들어가요. 섹션을 누르면 key=value 쌍을 볼 수 있어요.\n• 쌍을 누르면 키와 값을 바꿀 수 있고, ⋮ 메뉴에는 편집과 삭제가 있어요. ＋ 새 키는 섹션 끝에, ＋ 새 섹션은 파일 끝에 섹션을 추가해요. 섹션의 ⋮ 메뉴: 섹션 이름 변경, 삭제(내용 포함).\n• 주석, 빈 줄, 공백, 원래 순서는 바뀌지 않고, 바꾼 줄만 다시 써요. ↶ 와 ↷ 로 실행 취소와 다시 실행을 할 수 있어요(최대 40단계).\n• 구분자로 인식되는 것은 = 뿐이에요. 파일 형식이 다르면 일반 텍스트로 여세요.
    Why do the CSV columns look shifted or merged? | CSV の列がずれたり結合されて見えるのはなぜですか? | 为什么 CSV 的列看起来错位或合并了? | CSV 열이 밀려 보이거나 합쳐져 보이는 이유는 무엇인가요?
    The column separator may have been guessed wrongly. Open Options > CSV Settings and choose the right Column separator (Comma, Semicolon, Tab or Pipe), then open the Encoding & Line Endings section and press Reload with this encoding. | 列の区切り文字が誤って推測された可能性があります。オプション > CSV の設定で正しい列の区切り文字(カンマ、セミコロン、タブ、パイプ)を選び、「エンコーディングと改行」セクションを開いて「このエンコーディングで再読み込み」を押してください。 | 可能是列分隔符被错误推测。请打开 选项 > CSV 设置,选择正确的列分隔符(逗号、分号、制表符或竖线),然后展开“编码与换行符”部分并点按“用此编码重新加载”。 | 열 구분자를 잘못 추측했을 수 있어요. 옵션 > CSV 설정에서 알맞은 열 구분자(쉼표, 세미콜론, 탭, 세로선)를 고른 다음 인코딩 및 줄 끝 섹션을 열어 “이 인코딩으로 다시 불러오기”를 누르세요.
    Are comments in an INI file lost when I edit it in Structured Text? | 構造化テキストで INI ファイルを編集すると、コメントは消えますか? | 在结构化文本中编辑 INI 文件时,注释会丢失吗? | 구조화 텍스트에서 INI 파일을 편집하면 주석이 사라지나요?
    No. The INI editor only rewrites the lines you change, add or delete. Comments, blank lines and the rest of the order stay as they were. | いいえ。INI エディターは、変更、追加、削除した行だけを書き直します。コメント、空行、その他の順序は元のままです。 | 不会。INI 编辑器只会重写您修改、添加或删除的行。注释、空行以及其余部分的顺序都保持原样。 | 아니요. INI 편집기는 바꾸거나 추가하거나 삭제한 줄만 다시 써요. 주석, 빈 줄, 나머지 순서는 원래대로 유지돼요.
    Edit plain text and structured text (JSON, CSV, INI, XML, Markdown) | プレーンテキストと構造化テキスト(JSON、CSV、INI、XML、Markdown)を編集します | 编辑纯文本和结构化文本(JSON、CSV、INI、XML、Markdown) | 일반 텍스트와 구조화 텍스트(JSON, CSV, INI, XML, Markdown)를 편집해요
    Pick a format: JSON, CSV/TSV, INI/CFG, XML or Markdown | 形式を選びます: JSON、CSV/TSV、INI/CFG、XML、Markdown | 选择格式:JSON、CSV/TSV、INI/CFG、XML 或 Markdown | 형식을 고르세요: JSON, CSV/TSV, INI/CFG, XML, Markdown
    Tree and text views; suits RPG Maker data | ツリーとテキスト表示。RPG Maker のデータに最適です | 树形和文本视图,适合 RPG Maker 数据 | 트리와 텍스트 보기로, RPG Maker 데이터에 알맞아요
    A cell table; edit contents and add or delete rows and columns | セルの表。内容の編集と行・列の追加/削除ができます | 单元格表格;可编辑内容,并添加或删除行和列 | 셀 표로, 내용을 편집하고 행과 열을 추가하거나 삭제해요
    Sections and key=value pairs; comments stay intact | セクションと key=value のペア。コメントはそのまま保持されます | 节与 key=value 键值对;注释保持不变 | 섹션과 key=value 쌍을 편집해요. 주석은 그대로 유지돼요
    Text editor with XML highlighting and checking | XML の色分けとチェック機能付きテキストエディター | 带 XML 语法着色和检查功能的文本编辑器 | XML 색상 표시와 검사 기능이 있는 텍스트 편집기
    Text editor with Markdown highlighting | Markdown の色分け付きテキストエディター | 带 Markdown 语法着色的文本编辑器 | Markdown 색상 표시가 있는 텍스트 편집기
    Symbol bar above the editor | エディター上の記号バー | 编辑器上方的符号栏 | 편집기 위 기호 막대
    Syntax highlighting | 構文の色分け | 语法着色 | 구문 색상 표시
    Highlighting covers JSON, XML/HTML, INI/CFG, .ks, .rpy and Markdown, and is switched off automatically for text above 300,000 characters to stay smooth. The symbol bar inserts characters at the cursor (⇥ = Tab). | 色分けは JSON、XML/HTML、INI/CFG、.ks、.rpy、Markdown に対応し、動作を軽く保つため30万文字を超えるテキストでは自動的にオフになります。記号バーはカーソル位置に文字を挿入します(⇥ = Tab)。 | 着色适用于 JSON、XML/HTML、INI/CFG、.ks、.rpy 和 Markdown;为保持流畅,超过 300,000 个字符的文本会自动关闭。符号栏会在光标处插入字符(⇥ = Tab)。 | 색상 표시는 JSON, XML/HTML, INI/CFG, .ks, .rpy, Markdown에 적용되며, 원활함을 위해 300,000자를 넘는 텍스트에서는 자동으로 꺼져요. 기호 막대는 커서 위치에 문자를 넣어요(⇥ = Tab).
    Check XML | XML をチェック | 检查 XML | XML 검사
    XML is well-formed. | XML は整形式です。 | XML 格式良好。 | XML이 올바른 형식이에요.
    Invalid XML at line {0}, column {1}: {2} | XML が無効です({0} 行 {1} 列): {2} | XML 无效(第 {0} 行,第 {1} 列):{2} | XML이 올바르지 않아요({0}줄 {1}열): {2}
    CSV Settings | CSV の設定 | CSV 设置 | CSV 설정
    Column separator | 列の区切り文字 | 列分隔符 | 열 구분자
    Comma ( , ) | カンマ( , ) | 逗号( , ) | 쉼표( , )
    Semicolon ( ; ) | セミコロン( ; ) | 分号( ; ) | 세미콜론( ; )
    Tab | タブ | 制表符 | 탭
    Pipe (|) | パイプ(|) | 竖线(|) | 세로선(|)
    First row is the column headers | 1行目は列見出し | 第一行是列标题 | 첫 행은 열 제목
    Quotes when saving | 保存時の引用符 | 保存时的引号 | 저장 시 따옴표
    Keep original | 元に従う | 与原文件一致 | 원본 따르기
    When needed | 必要なときのみ | 仅在需要时 | 필요할 때만
    All cells | すべてのセル | 所有单元格 | 모든 셀
    Auto separator guesses from the first few lines (comma, semicolon, tab or pipe). While the table is unchanged the file is saved with its original text. Once changed, cells are rewritten; quotes follow your choice (Keep original = quote every cell when nearly all cells in the original file were quoted). | 「自動」は最初の数行から区切り文字(カンマ、セミコロン、タブ、パイプ)を推測します。表を変更していない間は元のテキストのまま保存されます。変更後はセルが書き直され、引用符は選択に従います(「元に従う」は、元のファイルのほぼすべてのセルが引用されていた場合にすべてのセルを引用します)。 | “自动”会根据前几行推测分隔符(逗号、分号、制表符或竖线)。表格未修改时,文件按原文本保存;修改后会重写单元格,引号按所选方式处理(“与原文件一致”指原文件中几乎所有单元格都带引号时,为所有单元格加引号)。 | 자동은 첫 몇 줄로 구분자(쉼표, 세미콜론, 탭, 세로선)를 추측해요. 표를 바꾸지 않았다면 원본 텍스트 그대로 저장돼요. 바꾼 뒤에는 셀을 다시 쓰고, 따옴표는 선택에 따라 처리해요(원본 따르기는 원본 파일의 거의 모든 셀에 따옴표가 있었다면 모든 셀에 따옴표를 붙여요).
    Search in cells | セル内を検索 | 在单元格中搜索 | 셀에서 검색
    {0} rows, {1} columns, separator: {2} | {0} 行、{1} 列、区切り: {2} | {0} 行,{1} 列,分隔符:{2} | {0}행, {1}열, 구분자: {2}
    Edit header | 見出しを編集 | 编辑标题 | 제목 편집
    Insert column left | 左に列を挿入 | 在左侧插入列 | 왼쪽에 열 삽입
    Insert column right | 右に列を挿入 | 在右侧插入列 | 오른쪽에 열 삽입
    Delete column | 列を削除 | 删除列 | 열 삭제
    Insert row above | 上に行を挿入 | 在上方插入行 | 위에 행 삽입
    Insert row below | 下に行を挿入 | 在下方插入行 | 아래에 행 삽입
    The table is empty. Tap ＋ Row to start. | 表が空です。「＋ 行」をタップして開始します。 | 表格为空。点按“＋ 行”开始。 | 표가 비어 있어요. ＋ 행을 눌러 시작하세요.
    Row | 行 | 行 | 행
    Column | 列 | 列 | 열
    Cell {0}{1} | セル {0}{1} | 单元格 {0}{1} | 셀 {0}{1}
    (global) | (グローバル) | (全局) | (전역)
    No sections or keys. Tap ＋ New section to add one. | セクションもキーもありません。「＋ 新しいセクション」をタップして追加します。 | 没有节或键。点按“＋ 新建节”进行添加。 | 섹션이나 키가 없어요. ＋ 새 섹션을 눌러 추가하세요.
    Rename section | セクション名を変更 | 重命名节 | 섹션 이름 변경
    New section | 新しいセクション | 新建节 | 새 섹션
    New key | 新しいキー | 新建键 | 새 키
    Section name | セクション名 | 节名称 | 섹션 이름
    The JSON root is not an object or array, so it can only be edited in the Text view. | JSON のルートがオブジェクトでも配列でもないため、テキスト表示でのみ編集できます。 | JSON 的根不是对象或数组,因此只能在文本视图中编辑。 | JSON의 루트가 객체나 배열이 아니어서 텍스트 보기에서만 편집할 수 있어요.
    Text Editor | テキストエディター | 文本编辑器 | 텍스트 편집기
    Plain Text | プレーンテキスト | 纯文本 | 일반 텍스트
    Edit pure text files such as .txt, .ini, .ks and .rpy | .txt、.ini、.ks、.rpy などのテキストファイルを編集します | 编辑 .txt、.ini、.ks、.rpy 等纯文本文件 | .txt, .ini, .ks, .rpy 같은 순수 텍스트 파일을 편집해요
    Structured Text | 構造化テキスト | 结构化文本 | 구조화 텍스트
    Pick a file | ファイルを選択 | 选择文件 | 파일 선택
    Pick a folder | フォルダを選択 | 选择文件夹 | 폴더 선택
    Extension filter | 拡張子フィルター | 扩展名筛选 | 확장자 필터
    Separate with commas, e.g. txt, ini, ks. Leave empty for all files. | カンマで区切ります(例: txt, ini, ks)。空にするとすべてのファイルが対象です。 | 用逗号分隔,例如 txt, ini, ks。留空表示所有文件。 | 쉼표로 구분해요(예: txt, ini, ks). 비워 두면 모든 파일이 대상이에요.
    Search file name | ファイル名を検索 | 搜索文件名 | 파일 이름 검색
    {0} files found | {0} 個のファイルが見つかりました | 找到 {0} 个文件 | 파일 {0}개를 찾았어요
    Opening file… | ファイルを開いています… | 正在打开文件… | 파일을 여는 중…
    The file is too large to edit ({0}). Limit: {1}. | ファイルが大きすぎて編集できません({0})。上限は {1} です。 | 文件太大,无法编辑({0})。上限为 {1}。 | 파일이 너무 커서 편집할 수 없어요({0}). 한도는 {1}이에요.
    This file does not look like text (it contains null bytes). To view it, use the Hex Viewer in File & Folder Inspector. | このファイルはテキストではないようです(ヌルバイトを含みます)。内容を見るにはファイル・フォルダー検査の Hex ビューアーを使ってください。 | 此文件似乎不是文本(包含空字节)。如需查看内容,请使用文件与文件夹检查器中的十六进制查看器。 | 이 파일은 텍스트가 아닌 것 같아요(널 바이트가 들어 있어요). 내용을 보려면 파일 및 폴더 검사기의 Hex 뷰어를 사용하세요.
    Could not open the file: {0} | ファイルを開けませんでした: {0} | 无法打开文件:{0} | 파일을 열지 못했어요: {0}
    Line | 行 | 行 | 줄
    Options | オプション | 选项 | 옵션
    Tidy | 整形 | 整理 | 정리
    Saving… | 保存中… | 正在保存… | 저장 중…
    Line {0}, Col {1} · {2} chars | {0} 行 {1} 列 · {2} 文字 | 第 {0} 行,第 {1} 列 · {2} 个字符 | {0}줄 {1}열 · {2}자
    """
    const val T17 = """
    Support Tools > Text Editor | サポートツール > テキストエディター | 辅助工具 > 文本编辑器 | 지원 도구 > 텍스트 편집기
    A Support Tools menu between Translation Editor and Media Asset Converter; it holds Plain Text, Structured Text and Bulk Replace.\n• Plain Text and Structured Text use the same flow: choose Create File or Upload File, press its button, and the Text Editor Area appears below on the same page (no new editor page).\n• Create File: pick a File Format (none for JSON), fill in the Output file name (default text_file), set Output & Result (If the result file already exists and Output Folder), then press Create text file. The button is enabled once the Output Folder is set. The file is created in the Output Folder and opens straight in the editor; Save writes to the same file.\n• Upload File: pick a text file (only the formats in the File Format dropdown; other formats show the warning File format not supported), set Output & Result, then press Edit text file. Output & Result offers Output Folder or Overwrite Originals; with Back up ticked, the original is first copied to the _backup folder inside the Output Folder.\n• Files over 4 MB are not edited (up to 40 MB they open read-only), and files containing null bytes are treated as not text (use the Hex Viewer in File Inspection).\n• Editor Look, encoding and save format are under the Options button in the Text Editor Area. The encoding is detected automatically (BOM, UTF-8, then Shift-JIS) and can be chosen manually, including GBK, Big5, EUC-KR and UTF-16; text that cannot be saved in the chosen encoding is rejected instead of being replaced by question marks.\n• Leaving or switching files with unsaved changes shows a confirmation.\n• Auto draft: unsaved changes are written to a draft in the app storage 2 seconds after the last change. When the same file is uploaded again you are offered Restore; the draft is deleted after a successful save or when you choose to leave without saving. | 「テキストエディター」はサポートツールのメニューで、翻訳エディターとメディアアセット変換の間にあります。プレーンテキスト、構造化テキスト、一括置換が入っています。\n• プレーンテキストと構造化テキストは同じ流れです。「ファイルを作成」または「ファイルをアップロード」を選んでボタンを押すと、同じページの下に「テキストエディター領域」が表示されます(新しいエディターページは開きません)。\n• ファイルを作成: ファイル形式を選び(JSON にはありません)、出力ファイル名を入力し(初期値は text_file)、出力と結果(ファイルが既にある場合/出力フォルダ)を設定して「テキストファイルを作成」を押します。出力フォルダを指定するとボタンが使えます。ファイルは出力フォルダに作られ、そのままエディターで開きます。保存は同じファイルに書き込みます。\n• ファイルをアップロード: テキストファイルを選びます(「ファイル形式」のドロップダウンにある形式のみ。それ以外は「ファイル形式は未対応です」と警告が出ます)。出力と結果を設定して「テキストファイルを編集」を押します。出力と結果では出力フォルダか「元のファイルを上書き」を選べます。バックアップにチェックを入れると、先に元のファイルが出力フォルダ内の_backupフォルダにコピーされます。\n• 4 MB を超えるファイルは編集できず(40 MB までは読み取り専用で開きます)、ヌルバイトを含むファイルはテキストではないと見なされます(ファイル検査の16進ビューアを使ってください)。\n• エディターの表示、エンコーディング、保存形式は、テキストエディター領域の「オプション」ボタンにあります。エンコーディングは自動検出(BOM、UTF-8、次に Shift-JIS)され、GBK、Big5、EUC-KR、UTF-16 を含め手動でも選べます。選んだエンコーディングで保存できないテキストは、疑問符に置き換えられずに拒否されます。\n• 未保存の変更があるときにページを離れたりファイルを切り替えたりすると確認が表示されます。\n• 自動下書き: 未保存の変更は、最後の変更から2秒後にアプリの保存領域の下書きに書き込まれます。同じファイルをもう一度アップロードすると「復元」の提案が出ます。下書きは、保存に成功したとき、または保存せずに終了を選んだときに削除されます。 | “文本编辑器”是辅助工具中的菜单,位于翻译编辑器和媒体资源转换器之间,包含纯文本、结构化文本和批量替换。\n• 纯文本和结构化文本使用相同的流程:选择“创建文件”或“上传文件”,点按其按钮,同一页面下方就会出现“文本编辑器区域”(不会再打开新的编辑器页面)。\n• 创建文件:选择文件格式(JSON 没有此项),填写输出文件名(默认 text_file),设置输出与结果(文件已存在时和输出文件夹),然后点按“创建文本文件”。指定输出文件夹后按钮才可用。文件会创建在输出文件夹中并直接在编辑器中打开;保存会写入同一个文件。\n• 上传文件:选择文本文件(只能是“文件格式”下拉菜单中的格式;其他格式会显示“不支持该文件格式”的警告),设置输出与结果,然后点按“编辑文本文件”。输出与结果可选择输出文件夹或“覆盖原文件”;勾选备份后,会先将原文件复制到输出文件夹内的 _backup 文件夹。\n• 超过 4 MB 的文件不能编辑(最大 40 MB 以只读方式打开),含有空字节的文件被视为非文本(请使用文件检查中的十六进制查看器)。\n• 编辑器外观、编码和保存格式在文本编辑器区域的“选项”按钮中。编码会自动检测(BOM、UTF-8,然后是 Shift-JIS),也可手动选择,包括 GBK、Big5、EUC-KR 和 UTF-16;无法用所选编码保存的文本会被拒绝,而不是替换成问号。\n• 有未保存的更改时离开页面或切换文件会显示确认。\n• 自动草稿:未保存的更改会在最后一次更改 2 秒后写入应用存储中的草稿。再次上传同一文件时会提示“恢复”;保存成功后或选择不保存直接离开时,草稿会被删除。 | 텍스트 편집기는 보조 도구 메뉴로, 번역 편집기와 미디어 에셋 변환기 사이에 있어요. 일반 텍스트, 구조화 텍스트, 일괄 바꾸기가 들어 있어요.\n• 일반 텍스트와 구조화 텍스트는 같은 흐름이에요. 파일 만들기 또는 파일 올리기를 고르고 버튼을 누르면 같은 페이지 아래에 텍스트 편집기 영역이 나타나요(새 편집기 페이지는 열리지 않아요).\n• 파일 만들기: 파일 형식을 고르고(JSON에는 없어요), 출력 파일 이름을 입력하고(기본값 text_file), 출력 및 결과(파일이 이미 있으면/출력 폴더)를 설정한 뒤 텍스트 파일 만들기를 눌러요. 출력 폴더를 정하면 버튼이 켜져요. 파일은 출력 폴더에 만들어지고 바로 편집기에서 열려요. 저장은 같은 파일에 기록해요.\n• 파일 올리기: 텍스트 파일을 골라요(파일 형식 드롭다운에 있는 형식만 가능하고, 그 외에는 지원하지 않는 파일 형식이라는 경고가 나와요). 출력 및 결과를 설정한 뒤 텍스트 파일 편집을 눌러요. 출력 및 결과에서는 출력 폴더나 원본 파일 덮어쓰기를 고를 수 있어요. 백업에 체크하면 원본을 먼저 출력 폴더 안의 _backup 폴더에 복사해요.\n• 4 MB를 넘는 파일은 편집하지 않고(40 MB까지는 읽기 전용으로 열려요), 널 바이트가 들어 있는 파일은 텍스트가 아닌 것으로 봐요(파일 검사의 16진수 뷰어를 쓰세요).\n• 편집기 화면, 인코딩, 저장 형식은 텍스트 편집기 영역의 옵션 버튼에 있어요. 인코딩은 자동으로 감지하고(BOM, UTF-8, 그다음 Shift-JIS) GBK, Big5, EUC-KR, UTF-16을 포함해 직접 고를 수도 있어요. 고른 인코딩으로 저장할 수 없는 텍스트는 물음표로 바뀌지 않고 거부돼요.\n• 저장하지 않은 변경 사항이 있을 때 페이지를 나가거나 파일을 바꾸면 확인 창이 나와요.\n• 자동 임시 저장: 저장하지 않은 변경 사항은 마지막 변경 2초 뒤에 앱 저장소의 임시 저장본에 기록돼요. 같은 파일을 다시 올리면 복원을 제안해요. 임시 저장본은 저장에 성공했을 때나 저장하지 않고 나가기를 선택했을 때 삭제돼요.
    Support Tools > Text Editor > Plain Text | サポートツール > テキストエディター > プレーンテキスト | 辅助工具 > 文本编辑器 > 纯文本 | 지원 도구 > 텍스트 편집기 > 일반 텍스트
    Edits pure text files (.txt, .ini, .cfg, .ks, .rpy, .csv, .xml and similar).\n• Choose Create File or Upload File at the top; the editor appears in the Text Editor Area below it.\n• The ↶ and ↷ buttons undo and redo (up to 60 steps).\n• Find opens the Find & Replace panel: match case, regex (groups can be used in the replace field), Next, the ‹ button for the previous match, Replace and Replace all.\n• Line opens a dialog to jump to a line number. Options opens Editor Look, Encoding & Line Endings and Output & Result.\n• The symbol bar above the editor inserts hard-to-type characters ({ } [ ] ( ) : ; = < > / # and Tab) at the cursor.\n• Syntax highlighting is automatic for JSON, XML/HTML, INI/CFG, .ks, .rpy and Markdown (switched off for text above 300,000 characters). The symbol bar and highlighting can be switched off in Editor Look.\n• The status bar shows line, column, character count, encoding and line endings. The title gets a ● mark when there are unsaved changes.\n• Save writes according to Output & Result and the chosen encoding.\n• Show line numbers and Show spaces, tabs and line ends (a dot · for a space, → for a tab, ¶ for a line end) can be switched on in Editor Look.\n• Options > View changes shows the difference between the current text and the text as opened or last saved (added and removed lines).\n• Files above 4 MB (up to 40 MB) open read-only, page by page: you can search and copy but not edit or save. | テキストファイル(.txt、.ini、.cfg、.ks、.rpy、.csv、.xml など)を編集します。\n• 上部で「ファイルを作成」または「ファイルをアップロード」を選ぶと、その下の「テキストエディター領域」にエディターが表示されます。\n• ↶ と ↷ ボタンで元に戻す/やり直しができます(最大60ステップ)。\n• 「検索」で検索と置換のパネルが開きます。大文字と小文字の区別、正規表現(置換欄でグループを使えます)、次へ、前の一致に戻る ‹ ボタン、置換、すべて置換があります。\n• 「行」で行番号へ移動するダイアログが開きます。「オプション」ではエディターの表示、エンコーディングと改行、出力と結果を設定できます。\n• エディター上の記号バーは、入力しにくい文字({ } [ ] ( ) : ; = < > / # と Tab)をカーソル位置に挿入します。\n• JSON、XML/HTML、INI/CFG、.ks、.rpy、Markdown では構文が自動で色分けされます(30万文字を超えるテキストではオフになります)。記号バーと色分けは「エディターの表示」でオフにできます。\n• ステータスバーには行、列、文字数、エンコーディング、改行コードが表示されます。未保存の変更があるとタイトルに ● が付きます。\n• 「保存」は、出力と結果の設定と選択したエンコーディングに従って書き込みます。\n• 「行番号を表示」と「スペース、タブ、行末を表示」は「エディターの表示」でオンにできます(スペースは ·、タブは →、行末は ¶ で表示)。\n• 「オプション」>「変更点を表示」で、現在のテキストと、開いたとき(または最後に保存したとき)のテキストとの差分(追加・削除された行)を表示します。\n• 4 MB を超えるファイル(最大 40 MB)は、読み取り専用でページごとに開きます。検索とコピーはできますが、編集や保存はできません。 | 编辑纯文本文件(.txt、.ini、.cfg、.ks、.rpy、.csv、.xml 等)。\n• 在顶部选择“创建文件”或“上传文件”;编辑器会显示在其下方的“文本编辑器区域”中。\n• ↶ 和 ↷ 按钮用于撤销和重做(最多 60 步)。\n• “查找”会打开查找与替换面板:区分大小写、正则表达式(替换栏中可使用分组)、下一个、用于上一个匹配的 ‹ 按钮、替换和全部替换。\n• “行”会打开跳转到行号的对话框。“选项”可设置编辑器外观、编码与换行符,以及输出与结果。\n• 编辑器上方的符号栏会在光标处插入不易输入的字符({ } [ ] ( ) : ; = < > / # 和 Tab)。\n• JSON、XML/HTML、INI/CFG、.ks、.rpy 和 Markdown 会自动进行语法着色(超过 300,000 个字符的文本会关闭)。符号栏和着色可在“编辑器外观”中关闭。\n• 状态栏显示行、列、字符数、编码和换行符。有未保存的更改时,标题前会显示 ●。\n• “保存”会按照输出与结果设置和所选编码写入。\n• 可在“编辑器外观”中开启“显示行号”和“显示空格、制表符和行尾”(空格显示为 ·,制表符为 →,行尾为 ¶)。\n• “选项” > “查看更改”会显示当前文本与打开时(或上次保存时)文本的差异(新增和删除的行)。\n• 超过 4 MB(最大 40 MB)的文件会以只读方式分页打开:可以搜索和复制,但不能编辑或保存。 | 순수 텍스트 파일(.txt, .ini, .cfg, .ks, .rpy, .csv, .xml 등)을 편집해요.\n• 위쪽에서 파일 만들기 또는 파일 올리기를 고르면 그 아래 텍스트 편집기 영역에 편집기가 나타나요.\n• ↶ 와 ↷ 버튼으로 실행 취소와 다시 실행을 할 수 있어요(최대 60단계).\n• 찾기를 누르면 찾기 및 바꾸기 패널이 열려요. 대소문자 구분, 정규식(바꿀 칸에서 그룹을 쓸 수 있어요), 다음, 이전 일치로 가는 ‹ 버튼, 바꾸기, 모두 바꾸기가 있어요.\n• 줄을 누르면 줄 번호로 이동하는 창이 열려요. 옵션에서는 편집기 화면, 인코딩 및 줄 끝, 출력 및 결과를 설정해요.\n• 편집기 위의 기호 막대는 입력하기 어려운 문자({ } [ ] ( ) : ; = < > / # 와 Tab)를 커서 위치에 넣어 줘요.\n• JSON, XML/HTML, INI/CFG, .ks, .rpy, Markdown은 구문 색상이 자동으로 표시돼요(300,000자를 넘는 텍스트에서는 꺼져요). 기호 막대와 색상 표시는 편집기 화면에서 끌 수 있어요.\n• 상태 표시줄에는 줄, 열, 글자 수, 인코딩, 줄 끝이 표시돼요. 저장하지 않은 변경 사항이 있으면 제목에 ● 가 붙어요.\n• 저장은 출력 및 결과 설정과 선택한 인코딩에 맞춰 기록해요.\n• 편집기 화면에서 줄 번호 표시와 공백, 탭, 줄 끝 표시를 켤 수 있어요(공백은 ·, 탭은 →, 줄 끝은 ¶ 로 표시돼요).\n• 옵션 > 변경 사항 보기는 현재 텍스트와 열었을 때(또는 마지막으로 저장했을 때) 텍스트의 차이(추가·삭제된 줄)를 보여 줘요.\n• 4 MB를 넘는 파일(최대 40 MB)은 읽기 전용으로 페이지별로 열려요. 검색과 복사는 되지만 편집과 저장은 할 수 없어요.
    Support Tools > Text Editor > Structured Text > CSV / TSV | サポートツール > テキストエディター > 構造化テキスト > CSV / TSV | 辅助工具 > 文本编辑器 > 结构化文本 > CSV / TSV | 지원 도구 > 텍스트 편집기 > 구조화 텍스트 > CSV / TSV
    Edits CSV or TSV tables (e.g. translation files).\n• The separator (comma, semicolon, tab or pipe) is guessed automatically from the first few lines and can be chosen manually in CSV Settings. Cells containing quotes, separators or line breaks are read correctly.\n• The table shows rows and columns in fixed-size cells; scroll sideways for more columns. Tap a cell to edit its contents (multiple lines are allowed). Tap a row number for Insert above or below, Duplicate and Delete; tap a column header for Insert left or right, Delete and Edit header. The ＋ Row and ＋ Column buttons add at the end.\n• The first row is treated as the column headers (this can be switched off in CSV Settings; when off, the columns are labelled A, B, C).\n• Find searches the text in all cells, then ‹ and › move between results. ↶ and ↷ undo and redo (up to 40 steps).\n• While the table is unchanged the file is saved with its original text. Once changed, the quotes used when saving follow your choice: Keep original, When needed or All cells.\n• The column header menu also has Sort A → Z and Z → A (numbers are sorted as numbers). Find has two modes: Find (jump between results) and Filter (only the rows containing the text are shown).\n• Paste adds the clipboard contents (e.g. rows copied from a spreadsheet, tab-separated) as new rows at the end of the table. | CSV または TSV の表(翻訳ファイルなど)を編集します。\n• 区切り文字(カンマ、セミコロン、タブ、パイプ)は最初の数行から自動で推測され、CSV の設定で手動でも選べます。引用符、区切り文字、改行を含むセルも正しく読み込まれます。\n• 表は固定サイズのセルで行と列を表示します。他の列は横にスクロールして見ます。セルをタップすると内容を編集できます(複数行も可)。行番号をタップすると、上/下に挿入、複製、削除ができます。列見出しをタップすると、左/右に挿入、削除、見出しの編集ができます。「＋ 行」と「＋ 列」ボタンは末尾に追加します。\n• 1行目は列見出しとして扱われます(CSV の設定でオフにできます。オフの場合、列には A、B、C が付きます)。\n• 「検索」はすべてのセルのテキストを探し、‹ と › で結果間を移動します。↶ と ↷ で元に戻す/やり直しができます(最大40ステップ)。\n• 表を変更していない間は元のテキストのまま保存されます。変更後は、保存時の引用符は選択(元に従う、必要なときのみ、すべてのセル)に従います。\n• 列見出しのメニューには、A → Z / Z → A の並べ替えもあります(数値は数値として並べ替えます)。「検索」には2つのモードがあります。検索(結果間を移動)と絞り込み(テキストを含む行だけを表示)です。\n• 「貼り付け」は、クリップボードの内容(スプレッドシートからコピーしたタブ区切りの行など)を表の末尾に新しい行として追加します。 | 编辑 CSV 或 TSV 表格(例如翻译文件)。\n• 分隔符(逗号、分号、制表符或竖线)会根据前几行自动推测,也可在 CSV 设置中手动选择。包含引号、分隔符或换行的单元格也能被正确读取。\n• 表格以固定大小的单元格显示行和列;左右滑动可查看更多列。点按单元格可编辑其内容(允许多行)。点按行号可进行在上方或下方插入、复制和删除;点按列标题可进行在左侧或右侧插入、删除和编辑标题。“＋ 行”和“＋ 列”按钮会在末尾添加。\n• 第一行被视为列标题(可在 CSV 设置中关闭;关闭后列会标为 A、B、C)。\n• “查找”会搜索所有单元格中的文本,然后用 ‹ 和 › 在结果之间切换。↶ 和 ↷ 用于撤销和重做(最多 40 步)。\n• 表格未修改时,文件按原文本保存。修改后,保存时的引号按所选方式处理:与原文件一致、仅在需要时或所有单元格。\n• 列标题菜单还包含按 A → Z 和 Z → A 排序(数字按数值排序)。“查找”有两种模式:查找(在结果之间跳转)和筛选(只显示包含该文本的行)。\n• “粘贴”会把剪贴板内容(例如从电子表格复制的、以制表符分隔的行)作为新行添加到表格末尾。 | CSV나 TSV 표(예: 번역 파일)를 편집해요.\n• 구분자(쉼표, 세미콜론, 탭, 세로선)는 첫 몇 줄로 자동 추측하며 CSV 설정에서 직접 고를 수도 있어요. 따옴표, 구분자, 줄바꿈이 들어 있는 셀도 올바르게 읽어요.\n• 표는 크기가 고정된 셀로 행과 열을 보여 줘요. 다른 열은 옆으로 밀어서 확인하세요. 셀을 누르면 내용을 편집할 수 있어요(여러 줄 가능). 행 번호를 누르면 위/아래에 삽입, 복제, 삭제를 할 수 있고, 열 제목을 누르면 왼쪽/오른쪽에 삽입, 삭제, 제목 편집을 할 수 있어요. ＋ 행과 ＋ 열 버튼은 끝에 추가해요.\n• 첫 행은 열 제목으로 취급돼요(CSV 설정에서 끌 수 있고, 끄면 열에 A, B, C가 붙어요).\n• 찾기는 모든 셀의 텍스트를 검색하고, ‹ 와 › 로 결과 사이를 이동해요. ↶ 와 ↷ 로 실행 취소와 다시 실행을 할 수 있어요(최대 40단계).\n• 표를 바꾸지 않았다면 원본 텍스트 그대로 저장돼요. 바꾼 뒤에는 저장할 때의 따옴표가 선택(원본 따르기, 필요할 때만, 모든 셀)에 따라 처리돼요.\n• 열 제목 메뉴에는 A → Z, Z → A 정렬도 있어요(숫자는 숫자로 정렬해요). 찾기에는 두 가지 모드가 있어요. 찾기(결과 사이 이동)와 필터(텍스트가 들어 있는 행만 표시)예요.\n• 붙여넣기는 클립보드 내용(예: 스프레드시트에서 복사한 탭 구분 행)을 표 끝에 새 행으로 추가해요.
    Support Tools > Text Editor > Structured Text > XML and Markdown | サポートツール > テキストエディター > 構造化テキスト > XML と Markdown | 辅助工具 > 文本编辑器 > 结构化文本 > XML 和 Markdown | 지원 도구 > 텍스트 편집기 > 구조화 텍스트 > XML 및 Markdown
    XML and Markdown open in the same text editor as Plain Text (Find & Replace, undo and redo, symbol bar, encoding) with dedicated syntax highlighting.\n• XML: Check XML tests whether the document is well-formed and shows the line and column of the first error (the file is not changed). Tidy XML sets 2-space indentation; documents with a DOCTYPE are not tidied so that no data is lost, and spacing inside mixed text can change. A tidy can be undone with undo.\n• Markdown: Preview shows a simple rendering (headings, lists, bold, italic, code, links, quotes, rules); press Edit to return to the editor.\n• Highlighting is switched off automatically for text above 300,000 characters. | XML と Markdown は、プレーンテキストと同じテキストエディター(検索と置換、元に戻す/やり直し、記号バー、エンコーディング)で、専用の構文色分け付きで開きます。\n• XML: 「XML をチェック」で文書が整形式かどうかを確認し、最初のエラーの行と列を表示します(ファイルは変更されません)。「XML を整形」は2スペースのインデントに整えます。DOCTYPE を含む文書はデータが失われないよう整形されず、混在テキスト内の空白が変わることがあります。整形は「元に戻す」で取り消せます。\n• Markdown: 「プレビュー」で簡易レンダリング(見出し、リスト、太字、斜体、コード、リンク、引用、区切り線)を表示します。「編集」を押すとエディターに戻ります。\n• 30万文字を超えるテキストでは色分けが自動的にオフになります。 | XML 和 Markdown 会在与纯文本相同的文本编辑器中打开(查找与替换、撤销和重做、符号栏、编码),并带有专用的语法着色。\n• XML:“检查 XML”会检查文档是否格式良好,并显示第一个错误的行和列(不会修改文件)。“整理 XML”会设置 2 个空格缩进;包含 DOCTYPE 的文档不会被整理,以免丢失数据,混合文本中的空白也可能改变。整理后可用撤销还原。\n• Markdown:“预览”会显示简单渲染效果(标题、列表、粗体、斜体、代码、链接、引用、分隔线);点按“编辑”返回编辑器。\n• 超过 300,000 个字符的文本会自动关闭着色。 | XML과 Markdown은 일반 텍스트와 같은 텍스트 편집기(찾기 및 바꾸기, 실행 취소와 다시 실행, 기호 막대, 인코딩)에서 전용 구문 색상과 함께 열려요.\n• XML: XML 검사는 문서가 올바른 형식인지 확인하고 첫 번째 오류의 줄과 열을 보여 줘요(파일은 바뀌지 않아요). XML 정리는 공백 2칸 들여쓰기로 정리해요. DOCTYPE이 있는 문서는 데이터 손실을 막기 위해 정리하지 않고, 혼합 텍스트 안의 공백은 달라질 수 있어요. 정리는 실행 취소로 되돌릴 수 있어요.\n• Markdown: 미리보기는 간단한 렌더링(제목, 목록, 굵게, 기울임, 코드, 링크, 인용, 구분선)을 보여 줘요. 편집을 누르면 편집기로 돌아가요.\n• 300,000자를 넘는 텍스트에서는 색상 표시가 자동으로 꺼져요.
    Support Tools > Text Editor > Bulk Replace | サポートツール > テキストエディター > 一括置換 | 辅助工具 > 文本编辑器 > 批量替换 | 지원 도구 > 텍스트 편집기 > 일괄 바꾸기
    Finds and replaces text across many files in one folder at once.\n• Pick the source folder (subfolders optional), the extension filter and the name search to decide which files are checked. Enter the text to find and its replacement; match case and regex (groups can be used in the replace field) are available.\n• Count matches checks every file (up to 4 MB, not binary) and lists the files with their number of matches without changing anything.\n• Apply to all asks for confirmation, then writes the results according to Output & Result: Output Folder, or Overwrite Originals with Back up to _backup. Each file keeps its encoding and line endings; a file containing characters that cannot be saved in its encoding counts as failed.\n• The final result shows how many files were changed, how many failed and the total number of replacements. It is best to check with Count matches first and keep Back up ticked. | 1つのフォルダ内の多数のファイルで、テキストを一度に検索・置換します。\n• ソースフォルダ(サブフォルダは任意)、拡張子フィルター、名前検索で対象ファイルを決めます。検索する文字列と置換後の文字列を入力します。大文字と小文字の区別と正規表現(置換欄でグループを使えます)が使えます。\n• 「一致を数える」は各ファイル(4 MB まで、バイナリを除く)を調べ、何も変更せずに、ファイルの一覧と一致数を表示します。\n• 「すべてに適用」は確認のあと、出力と結果の設定に従って書き込みます。出力フォルダ、または_backupへのバックアップ付きの元のファイルの上書きです。各ファイルのエンコーディングと改行コードは保持され、そのエンコーディングで保存できない文字を含むファイルは失敗として数えられます。\n• 最終結果には、変更したファイル数、失敗した数、置換の合計が表示されます。先に「一致を数える」で確認し、バックアップをオンにしておくことをおすすめします。 | 一次性在一个文件夹的多个文件中查找并替换文本。\n• 通过源文件夹(子文件夹可选)、扩展名筛选和名称搜索来决定要检查的文件。输入要查找的文本及其替换内容;可使用区分大小写和正则表达式(替换栏中可使用分组)。\n• “统计匹配”会检查每个文件(最大 4 MB,非二进制),并在不做任何更改的情况下列出文件及其匹配数。\n• “应用到全部”会先请求确认,然后按照输出与结果设置写入:输出文件夹,或覆盖原文件并备份到 _backup。每个文件都会保留自己的编码和换行符;包含无法用其编码保存的字符的文件会计为失败。\n• 最终结果会显示已更改的文件数、失败数以及替换总数。建议先用“统计匹配”检查,并保持勾选备份。 | 한 폴더의 여러 파일에서 텍스트를 한꺼번에 찾아 바꿔요.\n• 소스 폴더(하위 폴더는 선택), 확장자 필터, 이름 검색으로 검사할 파일을 정해요. 찾을 텍스트와 바꿀 텍스트를 입력해요. 대소문자 구분과 정규식(바꿀 칸에서 그룹을 쓸 수 있어요)을 쓸 수 있어요.\n• 일치 개수 세기는 각 파일(4 MB까지, 바이너리 제외)을 검사해 아무것도 바꾸지 않고 파일 목록과 일치 개수를 보여 줘요.\n• 모두 적용은 확인을 받은 뒤 출력 및 결과 설정에 맞춰 기록해요. 출력 폴더, 또는 _backup에 백업하는 원본 파일 덮어쓰기예요. 각 파일의 인코딩과 줄 끝은 유지되고, 해당 인코딩으로 저장할 수 없는 글자가 있는 파일은 실패로 세어요.\n• 마지막 결과에는 변경한 파일 수, 실패한 수, 바꾼 항목 총수가 표시돼요. 먼저 일치 개수 세기로 확인하고 백업을 켜 두는 것을 권해요.
    Are my changes lost if the app closes before I save? | 保存する前にアプリが閉じると、変更は失われますか? | 如果在保存前应用关闭,我的更改会丢失吗? | 저장하기 전에 앱이 닫히면 변경 사항이 사라지나요?
    Not necessarily. The editor writes an automatic draft 2 seconds after the last change. When you open the same file again you are offered Restore. The draft is deleted after a successful save or when you choose Leave without saving. | 必ずしもそうではありません。エディターは最後の変更から2秒後に自動で下書きを書き込みます。同じファイルをもう一度開くと「復元」が提案されます。下書きは、保存に成功したとき、または保存せずに終了を選んだときに削除されます。 | 不一定。编辑器会在最后一次更改 2 秒后自动写入草稿。再次打开同一文件时会提示“恢复”。成功保存后,或您选择不保存直接离开时,草稿会被删除。 | 꼭 그렇지는 않아요. 편집기는 마지막 변경 2초 뒤에 자동으로 임시 저장본을 기록해요. 같은 파일을 다시 열면 복원을 제안해요. 임시 저장본은 저장에 성공했을 때나 저장하지 않고 나가기를 선택했을 때 삭제돼요.
    Show line numbers | 行番号を表示 | 显示行号 | 줄 번호 표시
    Show spaces, tabs and line ends | スペース、タブ、行末を表示 | 显示空格、制表符和行尾 | 공백, 탭, 줄 끝 표시
    Large file ({0}) opened read-only, page by page. Changes cannot be saved. | 大きなファイル({0})は読み取り専用でページごとに開きます。変更は保存できません。 | 大文件({0})以只读方式分页打开,无法保存更改。 | 큰 파일({0})은 읽기 전용으로 페이지별로 열려요. 변경 사항은 저장할 수 없어요.
    Paste | 貼り付け | 粘贴 | 붙여넣기
    {0} rows pasted at the end of the table. | 表の末尾に {0} 行を貼り付けました。 | 已在表格末尾粘贴 {0} 行。 | 표 끝에 {0}행을 붙여넣었어요.
    The clipboard is empty. | クリップボードが空です。 | 剪贴板为空。 | 클립보드가 비어 있어요.
    Sort A → Z | A → Z で並べ替え | 按 A → Z 排序 | A → Z 정렬
    Sort Z → A | Z → A で並べ替え | 按 Z → A 排序 | Z → A 정렬
    Filter | 絞り込み | 筛选 | 필터
    {0} of {1} rows | {1} 行中 {0} 行 | {1} 行中的 {0} 行 | {1}행 중 {0}행
    Tidy XML | XML を整形 | 整理 XML | XML 정리
    XML tidied (2-space indentation). | XML を整形しました(2スペースのインデント)。 | XML 已整理(2 个空格缩进)。 | XML을 정리했어요(공백 2칸 들여쓰기).
    XML with a DOCTYPE is not tidied automatically so that no data is lost. | DOCTYPE を含む XML は、データが失われないよう自動整形されません。 | 包含 DOCTYPE 的 XML 不会自动整理,以免丢失数据。 | DOCTYPE이 있는 XML은 데이터 손실을 막기 위해 자동으로 정리하지 않아요.
    Could not tidy the XML: {0} | XML を整形できませんでした: {0} | 无法整理 XML:{0} | XML을 정리하지 못했어요: {0}
    View changes | 変更点を表示 | 查看更改 | 변경 사항 보기
    Changes since opened or saved | 開いた/保存したときからの変更 | 自打开或保存以来的更改 | 열거나 저장한 이후의 변경 사항
    Too many changes to show line by line. | 変更が多すぎるため、行ごとには表示できません。 | 更改过多,无法逐行显示。 | 변경 사항이 너무 많아 줄 단위로 표시할 수 없어요.
    No changes. | 変更はありません。 | 没有更改。 | 변경 사항이 없어요.
    +{0} lines, −{1} lines | +{0} 行、−{1} 行 | +{0} 行,−{1} 行 | +{0}줄, −{1}줄
    Draft found | 下書きが見つかりました | 发现草稿 | 임시 저장본을 찾았어요
    There is an unsaved draft of changes for this file. Restore it? | このファイルには未保存の変更の下書きがあります。復元しますか? | 此文件有未保存的更改草稿。要恢复吗? | 이 파일에 저장하지 않은 변경 사항의 임시 저장본이 있어요. 복원할까요?
    Ignore | 無視 | 忽略 | 무시
    Draft restored. | 下書きを復元しました。 | 草稿已恢复。 | 임시 저장본을 복원했어요.
    Bulk Replace | 一括置換 | 批量替换 | 일괄 바꾸기
    Find and replace text across many files in one folder | 1つのフォルダ内の多数のファイルで、テキストを一括で検索・置換します | 在一个文件夹的多个文件中批量查找并替换文本 | 한 폴더의 여러 파일에서 텍스트를 한꺼번에 찾아 바꿔요
    Pick the source folder first. | 先にソースフォルダを選択してください。 | 请先选择源文件夹。 | 먼저 소스 폴더를 선택하세요.
    Enter the text to find first. | 先に検索する文字列を入力してください。 | 请先输入要查找的文本。 | 먼저 찾을 텍스트를 입력하세요.
    Count matches | 一致を数える | 统计匹配 | 일치 개수 세기
    Apply to all | すべてに適用 | 应用到全部 | 모두 적용
    Processing {0} / {1}… | 処理中 {0} / {1}… | 正在处理 {0} / {1}… | 처리 중 {0} / {1}…
    {0} of {1} files contain {2} matches. | {1} 個中 {0} 個のファイルに {2} 件の一致があります。 | {1} 个文件中有 {0} 个包含 {2} 处匹配。 | 파일 {1}개 중 {0}개에 일치 {2}개가 있어요.
    {0} files skipped (too large or not text). | {0} 個のファイルをスキップしました(大きすぎるかテキストではありません)。 | 已跳过 {0} 个文件(太大或不是文本)。 | 파일 {0}개를 건너뛰었어요(너무 크거나 텍스트가 아니에요).
    {0} files changed, {1} failed ({2} replacements). | {0} 個のファイルを変更、{1} 個が失敗({2} 件を置換)。 | 已更改 {0} 个文件,{1} 个失败(共替换 {2} 处)。 | 파일 {0}개 변경, {1}개 실패(바꾼 항목 {2}개).
    Apply the replacement? | 置換を適用しますか? | 应用替换? | 바꾸기를 적용할까요?
    {0} files will be changed according to Output & Result. With Overwrite Originals and Back up ticked, the originals are first saved in _backup. | {0} 個のファイルが、出力と結果の設定に従って変更されます。「元のファイルを上書き」でバックアップにチェックが入っている場合は、先に元のファイルが_backupに保存されます。 | 将按照输出与结果设置更改 {0} 个文件。选择“覆盖原文件”并勾选备份时,会先把原文件保存到 _backup。 | 출력 및 결과 설정에 따라 파일 {0}개를 변경해요. 원본 파일 덮어쓰기에서 백업을 체크했다면 원본을 먼저 _backup에 저장해요.
    Go to line | 行へ移動 | 转到行 | 줄로 이동
    Line number | 行番号 | 行号 | 줄 번호
    Lines 1 to {0}. | 1 〜 {0} 行。 | 第 1 行到第 {0} 行。 | 1줄부터 {0}줄까지예요.
    Editor Options | エディターのオプション | 编辑器选项 | 편집기 옵션
    Leave without saving? Your changes will be lost. | 保存せずに終了しますか?変更内容は失われます。 | 不保存就离开吗?更改将会丢失。 | 저장하지 않고 나갈까요? 변경 내용이 사라져요.
    Reload the file? | ファイルを再読み込みしますか? | 重新加载文件? | 파일을 다시 불러올까요?
    The file will be read again with the selected encoding and unsaved changes will be lost. | 選択したエンコーディングでファイルを読み直します。未保存の変更は失われます。 | 将使用所选编码重新读取文件,未保存的更改将会丢失。 | 선택한 인코딩으로 파일을 다시 읽어요. 저장되지 않은 변경 사항은 사라져요.
    Reload | 再読み込み | 重新加载 | 다시 불러오기
    Reload with this encoding | このエンコーディングで再読み込み | 用此编码重新加载 | 이 인코딩으로 다시 불러오기
    Text to find | 検索する文字列 | 要查找的文本 | 찾을 텍스트
    Regex | 正規表現 | 正则表达式 | 정규식
    Replace all | すべて置換 | 全部替换 | 모두 바꾸기
    Invalid regex pattern or replacement. | 正規表現または置換文字列が無効です。 | 正则表达式或替换内容无效。 | 정규식 또는 바꿀 내용이 올바르지 않아요.
    No match found. | 見つかりませんでした。 | 未找到。 | 찾을 수 없어요.
    {0} matches | {0} 件一致 | {0} 处匹配 | {0}개 일치
    {0} replaced. | {0} 件を置換しました。 | 已替换 {0} 处。 | {0}개를 바꿨어요.
    Editor Look | エディターの表示 | 编辑器外观 | 편집기 화면
    Font size | 文字サイズ | 字号 | 글꼴 크기
    Wrap long lines | 長い行を折り返す | 长行自动换行 | 긴 줄 줄바꿈
    Encoding & Line Endings | エンコーディングと改行 | 编码与换行符 | 인코딩 및 줄 끝
    Encoding when opening | 開くときのエンコーディング | 打开时的编码 | 열 때 인코딩
    Save as | 保存形式 | 保存为 | 저장 형식
    Same as the original | 元のファイルと同じ | 与原文件相同 | 원본과 동일
    Auto: BOM first, then UTF-8, falling back to Shift-JIS. Text that cannot be saved in the chosen encoding is rejected instead of being replaced by question marks. The original line endings (LF or CRLF) are kept unless you change them. | 自動: まず BOM、次に UTF-8、失敗した場合は Shift-JIS で読み込みます。選択したエンコーディングで保存できない文字がある場合は、疑問符に置き換えず保存を中止します。元の改行コード(LF または CRLF)は、変更しない限り保持されます。 | 自动:先识别 BOM,其次 UTF-8,失败则使用 Shift-JIS。无法用所选编码保存的文字会被拒绝保存,而不是替换成问号。除非您更改,否则会保留原文件的换行符(LF 或 CRLF)。 | 자동: BOM을 먼저 확인하고, 그다음 UTF-8, 실패하면 Shift-JIS로 읽어요. 선택한 인코딩으로 저장할 수 없는 글자가 있으면 물음표로 바꾸지 않고 저장을 거부해요. 원본의 줄 끝(LF 또는 CRLF)은 바꾸지 않는 한 유지돼요.
    {0} characters cannot be saved in {1}. Choose UTF-8 under Save as, or change those characters. | {0} 文字は {1} で保存できません。「保存形式」で UTF-8 を選ぶか、その文字を変更してください。 | 有 {0} 个字符无法用 {1} 保存。请在“保存为”中选择 UTF-8,或修改这些字符。 | {0}개의 글자를 {1}(으)로 저장할 수 없어요. 저장 형식에서 UTF-8을 고르거나 해당 글자를 바꿔 주세요.
    Result file name | 結果のファイル名 | 结果文件名 | 결과 파일 이름
    File mode can only save to the Output Folder. To overwrite the original (with a backup), use Folder mode. | ファイルモードでは出力フォルダにのみ保存できます。元のファイルを(バックアップ付きで)上書きするには、フォルダモードを使ってください。 | 文件模式只能保存到输出文件夹。若要覆盖原文件(并备份),请使用文件夹模式。 | 파일 모드는 출력 폴더에만 저장할 수 있어요. 원본을 (백업과 함께) 덮어쓰려면 폴더 모드를 사용하세요.
    The result is written straight into the original file in the source folder. When ticked, the original is first copied to a _backup folder inside the source folder (clashing names get (1)); the backup is made once for each opened file. With Output Folder, later saves in the same session overwrite the same result file. | 結果はソースフォルダ内の元のファイルに直接書き込まれます。チェックを入れると、先に元のファイルをソースフォルダ内の_backupフォルダにコピーします(名前が重複する場合は(1)が付きます)。バックアップは開いたファイルごとに1回だけ作成されます。出力フォルダの場合、同じセッションでの2回目以降の保存は同じ結果ファイルを上書きします。 | 结果会直接写入源文件夹中的原文件。勾选后,会先将原文件复制到源文件夹内的 _backup 文件夹(重名时会加上 (1));每个打开的文件只备份一次。使用输出文件夹时,同一次会话中后续的保存会覆盖同一个结果文件。 | 결과는 원본 폴더의 원본 파일에 바로 저장돼요. 체크하면 원본을 먼저 원본 폴더 안의 _backup 폴더에 복사해요(이름이 겹치면 (1)이 붙어요). 백업은 열어 둔 파일마다 한 번만 만들어져요. 출력 폴더를 쓸 때는 같은 세션에서 다시 저장하면 같은 결과 파일을 덮어써요.
    Saved to the original file: {0} | 元のファイルに保存しました: {0} | 已保存到原文件:{0} | 원본 파일에 저장했어요: {0}
    Saved again: {0} | 再度保存しました: {0} | 已再次保存:{0} | 다시 저장했어요: {0}
    Saved: {0} | 保存しました: {0} | 已保存:{0} | 저장했어요: {0}
    Skipped: {0} already exists in the output folder. | スキップしました: {0} は出力フォルダにすでにあります。 | 已跳过:输出文件夹中已有 {0}。 | 건너뛰었어요: 출력 폴더에 {0}이(가) 이미 있어요.
    Save failed: {0} | 保存に失敗しました: {0} | 保存失败:{0} | 저장하지 못했어요: {0}
    Could not read the original file to back it up. | バックアップのために元のファイルを読み込めませんでした。 | 无法读取原文件以进行备份。 | 백업을 위해 원본 파일을 읽을 수 없어요.
    Could not write the file. | ファイルに書き込めませんでした。 | 无法写入文件。 | 파일에 쓸 수 없어요.
    Tree | ツリー | 树形 | 트리
    JSON Settings | JSON の設定 | JSON 设置 | JSON 설정
    View when opening | 開くときの表示 | 打开时的视图 | 열 때 보기
    Save format from the Tree view | ツリー表示からの保存形式 | 从树形视图保存的格式 | 트리 보기에서 저장할 형식
    Pretty | 整形 | 美化 | 보기 좋게
    Compact | 圧縮 | 紧凑 | 압축
    Pretty uses 2-space indentation; Compact writes a single line. While the tree is unchanged the file is saved with its original text, and from the Text view it is saved exactly as typed (the JSON must be valid). Key order and number formatting are kept. | 「整形」は2スペースのインデント、「圧縮」は1行で書き出します。ツリーを変更していない間は元のテキストのまま保存され、テキスト表示からは入力したとおりに保存されます(JSON が有効である必要があります)。キーの順序と数値の書式は保持されます。 | “美化”使用 2 个空格缩进;“紧凑”写成单行。树形视图未修改时,文件会按原文本保存;从文本视图保存时则完全按输入内容保存(JSON 必须有效)。键的顺序和数字格式会被保留。 | “보기 좋게”는 공백 2칸 들여쓰기를, “압축”은 한 줄로 저장해요. 트리를 바꾸지 않았다면 원본 텍스트 그대로 저장되고, 텍스트 보기에서는 입력한 그대로 저장돼요(올바른 JSON이어야 해요). 키 순서와 숫자 형식은 유지돼요.
    Not saved. {0} | 保存されませんでした。{0} | 未保存。{0} | 저장하지 않았어요. {0}
    Root | ルート | 根 | 루트
    Empty. Tap Add to create an entry. | 空です。「追加」をタップして項目を作成します。 | 为空。点按“添加”来创建条目。 | 비어 있어요. 추가를 눌러 항목을 만드세요.
    {0} properties | {0} 個のプロパティ | {0} 个属性 | 속성 {0}개
    {0} items | {0} 個の項目 | {0} 个项目 | 항목 {0}개
    Add Entry | 項目を追加 | 添加条目 | 항목 추가
    Edit “{0}” | 「{0}」を編集 | 编辑“{0}” | “{0}” 편집
    Type | 型 | 类型 | 유형
    Text value | テキスト値 | 文本值 | 텍스트 값
    Number value | 数値 | 数值 | 숫자 값
    Step | ステップ | 步长 | 단계
    Invalid number. | 数値が無効です。 | 数字无效。 | 올바르지 않은 숫자예요.
    This key already exists. | このキーはすでに存在します。 | 此键已存在。 | 이 키는 이미 있어요.
    The key cannot be empty. | キーを空にすることはできません。 | 键不能为空。 | 키는 비워 둘 수 없어요.
    Contents are kept as long as the type is unchanged. Open this entry from the list to edit its contents. It holds {0} entries. | 型を変更しない限り内容は保持されます。内容を編集するには、リストからこの項目を開いてください。{0} 個の項目が含まれています。 | 只要不更改类型,内容就会保留。若要编辑内容,请在列表中打开此条目。其中包含 {0} 个条目。 | 유형을 바꾸지 않으면 내용이 그대로 유지돼요. 내용을 편집하려면 목록에서 이 항목을 여세요. 항목 {0}개가 들어 있어요.
    Edit | 編集 | 编辑 | 편집
    Duplicate | 複製 | 复制 | 복제
    Move up | 上へ移動 | 上移 | 위로 이동
    Move down | 下へ移動 | 下移 | 아래로 이동
    Sort keys | キーを並べ替え | 按键排序 | 키 정렬
    Copy path | パスをコピー | 复制路径 | 경로 복사
    Path copied. | パスをコピーしました。 | 路径已复制。 | 경로를 복사했어요.
    Search keys or values | キーまたは値を検索 | 搜索键或值 | 키 또는 값 검색
    TEXT | テキスト | 文本 | 텍스트
    NUMBER | 数値 | 数字 | 숫자
    BOOLEAN | 真偽値 | 布尔值 | 불리언
    NULL | NULL | NULL | NULL
    OBJECT | オブジェクト | 对象 | 객체
    ARRAY | 配列 | 数组 | 배열
    Including subfolders | サブフォルダを含む | 包含子文件夹 | 하위 폴더 포함
    Why does text look garbled in the Text Editor? | テキストエディターで文字化けするのはなぜですか? | 为什么文本编辑器中的文字显示为乱码? | 텍스트 편집기에서 글자가 깨져 보이는 이유는 무엇인가요?
    The file encoding may not have been detected correctly. Open Options > Encoding & Line Endings, choose the right encoding (e.g. Shift-JIS, GBK, Big5 or EUC-KR), then press Reload with this encoding. Older visual novel scripts often use Shift-JIS. | ファイルのエンコーディングが正しく判定されなかった可能性があります。オプション > エンコーディングと改行を開き、適切なエンコーディング(Shift-JIS、GBK、Big5、EUC-KR など)を選んで、「このエンコーディングで再読み込み」を押してください。古いビジュアルノベルのスクリプトは Shift-JIS を使っていることがよくあります。 | 可能是文件编码没有被正确识别。请打开 选项 > 编码与换行符,选择合适的编码(例如 Shift-JIS、GBK、Big5 或 EUC-KR),然后点按“用此编码重新加载”。较旧的视觉小说脚本经常使用 Shift-JIS。 | 파일 인코딩이 제대로 감지되지 않았을 수 있어요. 옵션 > 인코딩 및 줄 끝을 열어 알맞은 인코딩(예: Shift-JIS, GBK, Big5, EUC-KR)을 고른 다음 “이 인코딩으로 다시 불러오기”를 누르세요. 오래된 비주얼 노벨 스크립트는 Shift-JIS를 쓰는 경우가 많아요.
    Why does the Text Editor refuse to save? | テキストエディターで保存できないのはなぜですか? | 为什么文本编辑器拒绝保存? | 텍스트 편집기가 저장을 거부하는 이유는 무엇인가요?
    Two common causes: some characters cannot be saved in the chosen encoding (choose UTF-8 under Save as), or the JSON in the Text view is invalid (fix it according to the error message). Also make sure an Output Folder is picked, or choose Overwrite Originals under Upload File. | よくある原因は2つあります。選択したエンコーディングで保存できない文字がある(「保存形式」で UTF-8 を選んでください)、またはテキスト表示の JSON が無効(エラーメッセージに従って修正してください)です。出力フォルダが選ばれているか、または「ファイルをアップロード」で「元のファイルを上書き」を選んでいるかも確認してください。 | 常见原因有两个:有字符无法用所选编码保存(请在“保存为”中选择 UTF-8),或文本视图中的 JSON 无效(请根据错误信息修正)。另外请确认已选择输出文件夹,或在“上传文件”中选择“覆盖原文件”。 | 흔한 원인은 두 가지예요. 선택한 인코딩으로 저장할 수 없는 글자가 있거나(저장 형식에서 UTF-8을 고르세요), 텍스트 보기의 JSON이 올바르지 않은 경우예요(오류 메시지에 따라 고치세요). 출력 폴더를 골랐는지, 또는 파일 올리기에서 원본 파일 덮어쓰기를 골랐는지도 확인하세요.
    Does the Text Editor change my original files? | テキストエディターは元のファイルを変更しますか? | 文本编辑器会修改我的原文件吗? | 텍스트 편집기가 원본 파일을 바꾸나요?
    Only if you choose Overwrite Originals under Upload File. With Back up ticked, the original is first copied to the _backup folder inside the Output Folder. With Output Folder, the original is not touched. | 「ファイルをアップロード」で「元のファイルを上書き」を選んだ場合のみです。バックアップにチェックが入っていれば、先に元のファイルが出力フォルダ内の_backupフォルダにコピーされます。出力フォルダの場合、元のファイルには手を加えません。 | 只有在您于“上传文件”中选择“覆盖原文件”时才会。若已勾选备份,会先将原文件复制到输出文件夹内的 _backup 文件夹。使用输出文件夹时,不会改动原文件。 | 파일 올리기에서 원본 파일 덮어쓰기를 선택했을 때만 바꿔요. 백업에 체크해 두면 원본을 먼저 출력 폴더 안의 _backup 폴더에 복사해요. 출력 폴더를 쓰면 원본은 건드리지 않아요.
    """
    const val T18 = """
Create or edit plain text and structured text files | プレーンテキストと構造化テキストのファイルを作成または編集します | 创建或编辑纯文本和结构化文本文件 | 일반 텍스트와 구조화 텍스트 파일을 만들거나 편집해요
Create File | ファイルを作成 | 创建文件 | 파일 만들기
Upload File | ファイルをアップロード | 上传文件 | 파일 올리기
File Format | ファイル形式 | 文件格式 | 파일 형식
Upload text file | テキストファイルをアップロード | 上传文本文件 | 텍스트 파일 올리기
File format not supported | ファイル形式は未対応です | 不支持该文件格式 | 지원하지 않는 파일 형식이에요
Create text file | テキストファイルを作成 | 创建文本文件 | 텍스트 파일 만들기
Edit text file | テキストファイルを編集 | 编辑文本文件 | 텍스트 파일 편집
Text Editor Area | テキストエディター領域 | 文本编辑器区域 | 텍스트 편집기 영역
Backup folder | バックアップフォルダ | 备份文件夹 | 백업 폴더
A new JSON file contains {} (an empty object) so it can be edited straight away in the Tree view. | 新しい JSON ファイルには {} (空のオブジェクト)が入るので、ツリー表示ですぐに編集できます。 | 新建的 JSON 文件内容为 {}(空对象),因此可直接在树状视图中编辑。 | 새 JSON 파일에는 {}(빈 객체)가 들어 있어서 트리 보기에서 바로 편집할 수 있어요.
Create File makes a new text file in the Output Folder using the chosen File Format and name. If that name already exists, the result follows "If the result file already exists" (Rename, Overwrite or Skip). Once the file is created, the Text Editor Area appears below: type the content and press Save; later saves write to the same file. This button is enabled once the Output Folder is set. | 「ファイルを作成」は、選んだファイル形式と名前で出力フォルダに新しいテキストファイルを作ります。同じ名前が既にある場合は「結果のファイルが既にある場合」の選択(名前変更、上書き、スキップ)に従います。作成後、下に「テキストエディター領域」が表示されます。内容を入力して「保存」を押すと、以降の保存は同じファイルに書き込まれます。このボタンは出力フォルダを指定すると使えます。 | “创建文件”会按所选文件格式和名称在输出文件夹中创建新的文本文件。若该名称已存在,则按“如果结果文件已存在”的选择处理(重命名、覆盖或跳过)。创建后下方会出现“文本编辑器区域”:输入内容并点按“保存”,之后的保存会写入同一个文件。指定输出文件夹后此按钮才可用。 | 파일 만들기는 고른 파일 형식과 이름으로 출력 폴더에 새 텍스트 파일을 만들어요. 같은 이름이 이미 있으면 결과 파일이 이미 있을 때 선택(이름 변경, 덮어쓰기, 건너뛰기)을 따라요. 파일이 만들어지면 아래에 텍스트 편집기 영역이 나타나요. 내용을 입력하고 저장을 누르면 이후 저장은 같은 파일에 기록돼요. 출력 폴더를 정하면 이 버튼이 켜져요.
Upload File opens a text file from storage for editing. Only the formats listed under File Format in Create File can be uploaded; other formats show a warning. After pressing Edit text file, the Text Editor Area appears below. Save writes the result according to Output & Result: to the Output Folder, or with Overwrite Original straight into the uploaded file (the original is first copied to a _backup folder inside the Output Folder when Back up is ticked). | 「ファイルをアップロード」は、保存先のテキストファイルを開いて編集します。アップロードできるのは「ファイルを作成」のファイル形式にある形式だけで、それ以外は警告が出ます。「テキストファイルを編集」を押すと、下に「テキストエディター領域」が表示されます。「保存」は出力と結果に従って書き込みます。出力フォルダへ書くか、「元のファイルを上書き」ならアップロードしたファイルに直接書きます(バックアップにチェックがあれば、先に元のファイルが出力フォルダ内の_backupフォルダにコピーされます)。 | “上传文件”会打开存储中的文本文件进行编辑。只能上传“创建文件”中文件格式列出的格式,其他格式会显示警告。点按“编辑文本文件”后,下方会出现“文本编辑器区域”。“保存”按输出与结果写入:写到输出文件夹,或选择“覆盖原文件”时直接写入上传的文件(勾选备份时,会先将原文件复制到输出文件夹内的 _backup 文件夹)。 | 파일 올리기는 저장소의 텍스트 파일을 열어 편집해요. 파일 만들기의 파일 형식에 있는 형식만 올릴 수 있고, 그 외에는 경고가 나와요. 텍스트 파일 편집을 누르면 아래에 텍스트 편집기 영역이 나타나요. 저장은 출력 및 결과에 따라 기록해요. 출력 폴더에 쓰거나, 원본 파일 덮어쓰기면 올린 파일에 바로 써요(백업에 체크하면 원본을 먼저 출력 폴더 안의 _backup 폴더에 복사해요).
The result is written straight into the uploaded file. When ticked, the original is first copied to a _backup folder inside the Output Folder (clashing names get (1)); the backup is made once for each opened file. If the file provider is read-only the overwrite fails: use the Output Folder. | 結果はアップロードしたファイルに直接書き込まれます。チェックすると、先に元のファイルが出力フォルダ内の_backupフォルダにコピーされます(名前が重なる場合は(1)が付きます)。バックアップは開いたファイルごとに1回作られます。ファイルプロバイダーが読み取り専用の場合、上書きは失敗します。出力フォルダを使ってください。 | 结果会直接写入上传的文件。勾选后,会先将原文件复制到输出文件夹内的 _backup 文件夹(重名时加 (1));每个打开的文件只备份一次。若文件提供方为只读,覆盖会失败:请使用输出文件夹。 | 결과는 올린 파일에 바로 기록돼요. 체크하면 원본을 먼저 출력 폴더 안의 _backup 폴더에 복사해요(이름이 겹치면 (1)이 붙어요). 백업은 연 파일마다 한 번만 만들어요. 파일 제공자가 읽기 전용이면 덮어쓰기가 실패해요. 출력 폴더를 쓰세요.
File created: {0} | ファイルを作成しました: {0} | 文件已创建:{0} | 파일을 만들었어요: {0}
Could not create the file: {0} | ファイルを作成できませんでした: {0} | 无法创建文件:{0} | 파일을 만들 수 없어요: {0}
The original file cannot be written (read-only provider). Choose Output Folder. | 元のファイルに書き込めません(読み取り専用のプロバイダー)。出力フォルダを選んでください。 | 无法写入原文件(只读提供方)。请选择输出文件夹。 | 원본 파일에 쓸 수 없어요(읽기 전용 제공자). 출력 폴더를 고르세요.
    Home and Feature Search | ホームと機能検索 | 主页与功能搜索 | 홈과 기능 검색
Special media formats in Game Assets | ゲームアセットの専用メディア形式 | 游戏资源中的专用媒体格式 | 게임 에셋의 전용 미디어 형식
Home | ホーム | 主页 | 홈
Search features, engines, or extensions… | 機能・エンジン・拡張子を検索… | 搜索功能、引擎或扩展名… | 기능, 엔진, 확장자 검색…
Clear search | 検索をクリア | 清除搜索 | 검색 지우기
Recently used | 最近使った機能 | 最近使用 | 최근 사용
Clear | クリア | 清除 | 지우기
Quick actions | クイック操作 | 快捷操作 | 빠른 작업
Favorites | お気に入り | 收藏 | 즐겨찾기
No favorites yet. Search for a feature and tap its star to add it. | お気に入りはまだありません。機能を検索し、星をタップして追加してください。 | 还没有收藏。搜索功能并点按其星标即可添加。 | 즐겨찾기가 아직 없습니다. 기능을 검색해 별을 눌러 추가하세요.
No matching features. | 一致する機能がありません。 | 没有匹配的功能。 | 일치하는 기능이 없습니다.
Recent searches | 最近の検索 | 最近搜索 | 최근 검색
Add to favorites | お気に入りに追加 | 加入收藏 | 즐겨찾기에 추가
Remove from favorites | お気に入りから削除 | 取消收藏 | 즐겨찾기에서 삭제
The Home tab is the start page of the app.\n• Feature Search: type a feature, engine, or file extension (e.g. "xp3", "tlg", "translate"), then tap a result to open it; its menu path is shown under the title. Tap the star to add it to Favorites.\n• Recently used: the features you opened last.\n• Quick actions: shortcuts to features. Tap "Edit Quick Actions" to add features by search (up to 10), remove them with the X button, and reorder them by dragging the three-line icon.\n• Find features by file: pick a file, then choose a feature that fits its extension; the file is loaded into that feature automatically.\n• After opening a feature from Home, the back button always returns you to Home.\n• In any search box, the back button or a tap outside the box closes the keyboard and returns the box to its normal look. | ホームタブはアプリの開始ページです。\n• 機能検索: 機能・エンジン・ファイル拡張子（例: "xp3"、"tlg"、"translate"）を入力し、結果をタップして開きます。タイトルの下にメニューの経路が表示されます。星をタップするとお気に入りに追加されます。\n• 最近使った機能: 直近に開いた機能です。\n• クイック操作: 機能へのショートカットです。「クイック操作を編集」をタップすると、検索で機能を追加（最大10件）、Xボタンで削除、三本線アイコンのドラッグで並べ替えができます。\n• ファイルから機能を探す: ファイルを選び、その拡張子に合う機能を選ぶと、ファイルが自動的にその機能へ読み込まれます。\n• ホームから機能を開いた場合、戻るボタンで必ずホームに戻ります。\n• どの検索ボックスでも、戻るボタンまたは枠外のタップでキーボードが閉じ、通常の表示に戻ります。 | 主页标签是应用的起始页。\n• 功能搜索：输入功能、引擎或文件扩展名（如 "xp3"、"tlg"、"translate"），点按结果即可打开，标题下方会显示其菜单路径。点按星标可加入收藏。\n• 最近使用：你最近打开的功能。\n• 快捷操作：功能的快捷方式。点按“编辑快捷操作”，可通过搜索添加功能（最多 10 个）、用 X 按钮删除，并拖动三条线图标调整顺序。\n• 通过文件查找功能：选择一个文件，再选择与其扩展名匹配的功能；文件会自动载入该功能。\n• 从主页打开功能后，返回按钮始终会回到主页。\n• 在任何搜索框中，按返回键或点按框外区域都会收起键盘，并让搜索框恢复正常外观。 | 홈 탭은 앱의 시작 페이지입니다.\n• 기능 검색: 기능, 엔진 또는 파일 확장자(예: "xp3", "tlg", "translate")를 입력하고 결과를 누르면 열리며, 제목 아래에 메뉴 경로가 표시됩니다. 별을 누르면 즐겨찾기에 추가됩니다.\n• 최근 사용: 최근에 연 기능입니다.\n• 빠른 작업: 기능 바로가기입니다. "빠른 작업 편집"을 누르면 검색으로 기능을 추가(최대 10개)하고, X 버튼으로 삭제하며, 세 줄 아이콘을 끌어 순서를 바꿀 수 있습니다.\n• 파일로 기능 찾기: 파일을 선택한 뒤 확장자에 맞는 기능을 고르면 파일이 해당 기능에 자동으로 불러와집니다.\n• 홈에서 기능을 열면 뒤로 가기 버튼이 항상 홈으로 돌아옵니다.\n• 모든 검색창에서 뒤로 가기 버튼이나 창 밖을 누르면 키보드가 닫히고 검색창이 평소 모양으로 돌아옵니다.
Edit Quick Actions | クイック操作を編集 | 编辑快捷操作 | 빠른 작업 편집
Search a feature to add… | 追加する機能を検索… | 搜索要添加的功能… | 추가할 기능 검색…
Quick actions list | クイック操作の一覧 | 快捷操作列表 | 빠른 작업 목록
No quick actions yet. Search a feature above to add one. | クイック操作はまだありません。上で機能を検索して追加してください。 | 还没有快捷操作。请在上方搜索功能并添加。 | 빠른 작업이 아직 없습니다. 위에서 기능을 검색해 추가하세요.
Maximum 10 quick actions. | クイック操作は最大10件です。 | 快捷操作最多 10 个。 | 빠른 작업은 최대 10개입니다.
Drag to reorder | ドラッグで並べ替え | 拖动以排序 | 끌어서 순서 변경
Remove from Quick actions | クイック操作から削除 | 从快捷操作中移除 | 빠른 작업에서 삭제
Add to Quick actions | クイック操作に追加 | 添加到快捷操作 | 빠른 작업에 추가
Close | 閉じる | 关闭 | 닫기
Upload file | ファイルをアップロード | 上传文件 | 파일 업로드
Find features by file | ファイルから機能を探す | 通过文件查找功能 | 파일로 기능 찾기
(For Global) | (全世界向け) | （面向全球） | (글로벌용)
(Indonesia only) | (インドネシア専用) | （仅限印度尼西亚） | (인도네시아 전용)
Pick a file, then choose a feature that fits it | ファイルを選び、合う機能を選びます | 选择文件，再选择适合它的功能 | 파일을 고른 뒤 알맞은 기능을 선택하세요
Features for this file | このファイル向けの機能 | 适用于此文件的功能 | 이 파일에 맞는 기능
This extension is not recognized. Try File Inspection or the Hex Viewer. | この拡張子は認識されません。ファイル検査またはHexビューアをお試しください。 | 无法识别此扩展名。请尝试“文件检查”或十六进制查看器。 | 이 확장자는 인식되지 않습니다. 파일 검사나 Hex 뷰어를 사용해 보세요.
Check for updates | アップデートを確認 | 检查更新 | 업데이트 확인
Fetch update information from GitHub | GitHub から更新情報を取得 | 从 GitHub 获取更新信息 | GitHub에서 업데이트 정보 가져오기
Checking for updates… | アップデートを確認中… | 正在检查更新… | 업데이트 확인 중…
You are using the latest version. | 最新バージョンを使用しています。 | 你正在使用最新版本。 | 최신 버전을 사용 중입니다.
Download latest version | 最新バージョンをダウンロード | 下载最新版本 | 최신 버전 다운로드
Downloading update | アップデートをダウンロード中 | 正在下载更新 | 업데이트 다운로드 중
Download complete. Tap Install app to install it. | ダウンロードが完了しました。「アプリをインストール」をタップしてインストールしてください。 | 下载完成。点按“安装应用”即可安装。 | 다운로드가 완료되었습니다. '앱 설치'를 눌러 설치하세요.
Download complete | ダウンロード完了 | 下载完成 | 다운로드 완료
Install app | アプリをインストール | 安装应用 | 앱 설치
The download failed. | ダウンロードに失敗しました。 | 下载失败。 | 다운로드에 실패했습니다.
Try again | もう一度試す | 重试 | 다시 시도
Cancel download | ダウンロードをキャンセル | 取消下载 | 다운로드 취소
Tap to install. | タップしてインストールします。 | 点按以安装。 | 탭하여 설치합니다.
Tap to open the release page. | リリースページを開くにはタップしてください。 | 点按以打开发布页面。 | 탭하여 릴리스 페이지를 엽니다.
App updates are not available right now. | アプリのアップデートは現在ご利用いただけません。 | 目前暂无应用更新。 | 현재 앱 업데이트를 사용할 수 없습니다.
Could not check for updates. Make sure the device is connected to the internet. | アップデートを確認できませんでした。端末がインターネットに接続されているか確認してください。 | 无法检查更新。请确认设备已连接到互联网。 | 업데이트를 확인할 수 없습니다. 기기가 인터넷에 연결되어 있는지 확인하세요.
A new version is available | 新しいバージョンがあります | 有新版本可用 | 새 버전이 있습니다
Installed | インストール済み | 已安装 | 설치됨
Latest | 最新 | 最新 | 최신
Release notes | リリースノート | 发行说明 | 릴리스 노트
Download | ダウンロード | 下载 | 다운로드
Release page | リリースページ | 发布页面 | 릴리스 페이지
Later | 後で | 稍后 | 나중에
This release has no APK attached; the release page will be opened. | このリリースには APK が添付されていません。リリースページを開きます。 | 此版本未附带 APK，将打开发布页面。 | 이 릴리스에는 APK가 첨부되어 있지 않아 릴리스 페이지가 열립니다.
"""
    const val T19 = """
FuruArc was built by studying and referring to the open projects below. Thanks to their authors. Tap a link to open it in your browser. | FuruArc は、以下のオープンプロジェクトを調べ、参考にして作られました。作者の皆さまに感謝します。リンクをタップするとブラウザで開きます。 | FuruArc 是在研究并参考下列开源项目的基础上开发的，在此感谢各位作者。点击链接即可在浏览器中打开。 | FuruArc는 아래의 오픈 프로젝트를 살펴보고 참고하여 만들었어요. 제작자분들께 감사드립니다. 링크를 누르면 브라우저에서 열려요.
Other engine archive formats were written from the file structure tested directly on sample archives. This app is not affiliated with the projects above. | その他のエンジンのアーカイブ形式は、サンプルアーカイブで直接検証したファイル構造に基づいて実装しています。このアプリは上記のプロジェクトとは無関係です。 | 其他引擎的压缩包格式是根据在样本压缩包上直接测试得到的文件结构编写的。本应用与上述项目没有隶属关系。 | 그 밖의 엔진 아카이브 형식은 샘플 아카이브에서 직접 확인한 파일 구조를 바탕으로 작성했어요. 이 앱은 위의 프로젝트들과 관련이 없습니다.
Opens your email app with the developer's address, app version and device info filled in. Nothing is sent automatically; you can edit the message before sending. | 開発者のメールアドレス、アプリのバージョン、端末情報が入力された状態でメールアプリが開きます。自動的には何も送信されません。送信前にメッセージを編集できます。 | 将打开邮件应用，并自动填入开发者地址、应用版本和设备信息。不会自动发送任何内容，发送前可以编辑邮件。 | 개발자 주소, 앱 버전, 기기 정보가 채워진 상태로 이메일 앱이 열려요. 자동으로 전송되는 내용은 없으며, 보내기 전에 메시지를 수정할 수 있어요.
Parts are numbered .001, .002, etc. Join them back with File Joiner (or another tool such as HJSplit). The source file is not changed. | 分割したパートには .001、.002 のような番号が付きます。ファイル結合（または HJSplit などの別ツール）で元に戻せます。元のファイルは変更されません。 | 分割后的各部分会编号为 .001、.002 等。可使用文件合并（或 HJSplit 等其他工具）将它们还原。源文件不会被修改。 | 나뉜 조각에는 .001, .002 같은 번호가 붙어요. 파일 합치기(또는 HJSplit 같은 다른 도구)로 다시 합칠 수 있어요. 원본 파일은 바뀌지 않아요.
Long-press a file in the file picker to select all parts (.001, .002, …) at once. Parts are joined in the order of the number at the end of the name, not the order you picked them. | ファイルピッカーでファイルを長押しすると、すべてのパート（.001、.002、…）をまとめて選択できます。パートは選択した順ではなく、名前末尾の番号の順に結合されます。 | 在文件选择器中长按文件，可一次选中所有分块（.001、.002、…）。合并时按文件名末尾的编号顺序，而不是你选择的顺序。 | 파일 선택기에서 파일을 길게 누르면 모든 조각(.001, .002, …)을 한 번에 선택할 수 있어요. 조각은 선택한 순서가 아니라 이름 끝의 번호 순서대로 합쳐져요.
Also pick the .sha256 file if there is one to verify the result. When you pick a folder, all numbered part sets inside are detected automatically (one level) and a matching .sha256 file is used automatically. | .sha256 ファイルがある場合は、結果を検証するために一緒に選択してください。フォルダを選ぶと、中にある連番パートのセットがすべて自動検出され（1階層）、対応する .sha256 ファイルも自動的に使われます。 | 如果有 .sha256 文件，请一并选择，用于校验结果。选择文件夹时，会自动检测其中所有编号分块组（仅一层），并自动使用对应的 .sha256 文件。 | .sha256 파일이 있으면 결과를 검증할 수 있도록 함께 선택하세요. 폴더를 고르면 안에 있는 번호가 붙은 조각 묶음을 모두 자동으로 찾고(한 단계), 맞는 .sha256 파일도 자동으로 사용해요.
For garbled file names (e.g. ƒXƒNƒŠƒvƒg) caused by a wrong encoding when extracting archives. Names that are already correct are left alone. Folder names are not repaired. | アーカイブの展開時にエンコードを誤ったために文字化けしたファイル名（例: ƒXƒNƒŠƒvƒg）を修復します。すでに正しい名前はそのままです。フォルダ名は修復されません。 | 用于修复解压压缩包时因编码错误而乱码的文件名（例如 ƒXƒNƒŠƒvƒg）。已经正确的名称不会改动。文件夹名称不会被修复。 | 아카이브를 풀 때 인코딩이 잘못되어 깨진 파일 이름(예: ƒXƒNƒŠƒvƒg)을 고쳐요. 이미 올바른 이름은 그대로 두며, 폴더 이름은 고치지 않아요.
Pick a folder; the app groups files by size and then compares their content with SHA-256. One file in each group is always kept. Deletion is permanent. | フォルダを選ぶと、アプリがファイルをサイズでグループ化し、その後 SHA-256 で内容を比較します。各グループで1つのファイルは必ず残ります。削除は元に戻せません。 | 选择一个文件夹，应用会先按大小对文件分组，再用 SHA-256 比较内容。每组始终会保留一个文件。删除后无法恢复。 | 폴더를 고르면 앱이 파일을 크기별로 묶은 다음 SHA-256으로 내용을 비교해요. 각 그룹에서 파일 하나는 항상 남겨 두며, 삭제하면 되돌릴 수 없어요.
Archives read: {0}, failed: {1} | 読み込んだアーカイブ: {0}、失敗: {1} | 已读取压缩包：{0}，失败：{1} | 읽은 아카이브: {0}, 실패: {1}
Archives read: {0} | 読み込んだアーカイブ: {0} | 已读取压缩包：{0} | 읽은 아카이브: {0}
The letmoe source is not usable in this app yet. | letmoe ソースは、このアプリではまだ利用できません。 | 此应用暂时还不能使用 letmoe 来源。 | letmoe 소스는 아직 이 앱에서 사용할 수 없어요.
Covers are searched automatically on VNDB, NextMoe, Steam, then Bing by game name. The first result of the first source that has one is used. | カバーはゲーム名をもとに、VNDB、NextMoe、Steam、Bing の順で自動検索されます。結果が見つかった最初のソースの最初の結果が使われます。 | 封面会根据游戏名称依次在 VNDB、NextMoe、Steam、Bing 上自动搜索，并使用第一个有结果的来源中的第一条结果。 | 표지는 게임 이름으로 VNDB, NextMoe, Steam, Bing 순서로 자동 검색해요. 결과가 있는 첫 번째 소스의 첫 번째 결과를 사용해요.
Done: {0} files, {1} failed. Folder: {2} | 完了: {0} 個のファイル、{1} 件失敗。フォルダ: {2} | 完成：{0} 个文件，{1} 个失败。文件夹：{2} | 완료: 파일 {0}개, {1}개 실패. 폴더: {2}
Done: {0} files. Folder: {1} | 完了: {0} 個のファイル。フォルダ: {1} | 完成：{0} 个文件。文件夹：{1} | 완료: 파일 {0}개. 폴더: {1}
Pick one folder that holds a mix of audio, images and video. The app groups files by extension, and each type uses its own rules (output format, compression and Advanced Settings, same as the Audio, Image and Video converters). Tap a type's title to open its rules; the switch on the right decides whether that type is processed. Switched-off types are left untouched, and types missing from the folder are disabled automatically. Audio and images are processed first, video last (one at a time). In Overwrite Originals mode files whose format stays the same are replaced in place, while files whose format changes produce a new file next to them. The Text and Special converters are not included. | オーディオ、画像、動画が混在している1つのフォルダを選んでください。アプリは拡張子でファイルをグループ化し、種類ごとに独自のルール（出力形式、圧縮、詳細設定。オーディオ・画像・動画コンバーターと同じ）を使います。種類のタイトルをタップするとそのルールを開けます。右側のスイッチでその種類を処理するかどうかを決めます。オフにした種類は手を加えず、フォルダにない種類は自動的に無効になります。オーディオと画像が先に、動画は最後に（1つずつ）処理されます。「元ファイルを上書き」モードでは、形式が変わらないファイルはその場で置き換えられ、形式が変わるファイルはその隣に新しいファイルが作られます。テキストコンバーターと特殊コンバーターは含まれません。 | 请选择一个同时包含音频、图片和视频的文件夹。应用会按扩展名对文件分组，每种类型使用各自的规则（输出格式、压缩和高级设置，与音频、图片、视频转换器相同）。点击类型标题可打开其规则；右侧的开关决定是否处理该类型。关闭的类型不会被改动，文件夹中不存在的类型会自动禁用。先处理音频和图片，最后处理视频（逐个进行）。在“覆盖原文件”模式下，格式不变的文件会被就地替换，格式改变的文件则会在旁边生成新文件。不包含文本转换器和特殊转换器。 | 오디오, 이미지, 비디오가 섞여 있는 폴더 하나를 고르세요. 앱은 확장자별로 파일을 묶고, 유형마다 자체 규칙(출력 형식, 압축, 고급 설정. 오디오·이미지·비디오 변환기와 같음)을 사용해요. 유형 제목을 누르면 해당 규칙을 열 수 있고, 오른쪽 스위치로 그 유형을 처리할지 정해요. 꺼 둔 유형은 건드리지 않으며, 폴더에 없는 유형은 자동으로 비활성화돼요. 오디오와 이미지를 먼저, 비디오는 마지막에(하나씩) 처리해요. ‘원본 덮어쓰기’ 모드에서는 형식이 그대로인 파일은 제자리에서 교체되고, 형식이 바뀌는 파일은 옆에 새 파일이 만들어져요. 텍스트 변환기와 특수 변환기는 포함되지 않아요.
Off by default: files that are not audio, images or video are skipped and do not appear in the output folder. When checked, they are copied unchanged into the output folder (folder structure kept) so the result folder is complete. Not used in Overwrite Originals mode because the originals are already in place. | デフォルトはオフです。オーディオ・画像・動画以外のファイルはスキップされ、出力フォルダには表示されません。オンにすると、それらは変更なしで出力フォルダにコピーされ（フォルダ構造は維持）、結果フォルダが完全な状態になります。元ファイルがすでに所定の場所にあるため、「元ファイルを上書き」モードでは使われません。 | 默认关闭：非音频、图片、视频的文件会被跳过，不会出现在输出文件夹中。勾选后，这些文件会原样复制到输出文件夹（保留文件夹结构），使结果文件夹完整。“覆盖原文件”模式下不使用此选项，因为原文件本来就在原位。 | 기본값은 꺼짐이에요. 오디오·이미지·비디오가 아닌 파일은 건너뛰며 출력 폴더에 나타나지 않아요. 체크하면 그런 파일이 변경 없이 출력 폴더로 복사되어(폴더 구조 유지) 결과 폴더가 온전해져요. 원본이 이미 제자리에 있으므로 ‘원본 덮어쓰기’ 모드에서는 사용하지 않아요.
Recommended: enable "Background processing" and "Keep screen on while running" in Settings. | 推奨: 設定で「バックグラウンド処理」と「実行中は画面をオンにしておく」を有効にしてください。 | 建议：在设置中启用“后台处理”和“运行时保持屏幕常亮”。 | 권장: 설정에서 ‘백그라운드 처리’와 ‘실행 중 화면 켜 두기’를 켜 주세요.
Quality: 100% = no compression. Size: each result is tuned to be close to that number (approximate). If the output format differs, the file is converted and compressed in one pass. Files already smaller than the target are not compressed. | 品質: 100% = 圧縮なし。サイズ: 各結果はその数値に近づくように調整されます（おおよそ）。出力形式が異なる場合、変換と圧縮は1回のパスで行われます。すでに目標より小さいファイルは圧縮されません。 | 质量：100% = 不压缩。大小：每个结果会被调整到接近该数值（约数）。如果输出格式不同，将在一次处理中完成转换和压缩。已经小于目标的文件不会被压缩。 | 품질: 100% = 압축 없음. 크기: 각 결과가 그 값에 가깝게 조정돼요(대략적). 출력 형식이 다르면 변환과 압축이 한 번에 이루어져요. 이미 목표보다 작은 파일은 압축하지 않아요.
This button appears when some results are larger than their source files (this can happen with OGG (Vorbis) compression, conversion to WAV/FLAC, or any other format that is simply bigger). Pressing it deletes those larger result files. If a result replaced the original and a backup exists in _backup, the original is restored. Results without a backup cannot be discarded and are left as they are. Source files are never deleted. | このボタンは、一部の結果が元ファイルより大きくなった場合に表示されます（OGG (Vorbis) 圧縮、WAV/FLAC への変換、その他単純に容量が大きい形式で起こり得ます）。押すと、それらの大きくなった結果ファイルが削除されます。結果が元ファイルを置き換えていて _backup にバックアップがある場合は、元のファイルが復元されます。バックアップのない結果は破棄できず、そのまま残ります。元ファイルは削除されません。 | 当部分结果比源文件更大时（使用 OGG (Vorbis) 压缩、转换为 WAV/FLAC 或其他本身体积更大的格式时可能出现），会显示此按钮。点击后会删除这些变大的结果文件。如果结果替换了原文件且 _backup 中有备份，则会恢复原文件。没有备份的结果无法丢弃，会保持原样。源文件永远不会被删除。 | 일부 결과가 원본 파일보다 커졌을 때 이 버튼이 나타나요(OGG (Vorbis) 압축, WAV/FLAC로의 변환, 또는 단순히 용량이 더 큰 다른 형식에서 생길 수 있어요). 누르면 더 커진 결과 파일이 삭제돼요. 결과가 원본을 대체했고 _backup에 백업이 있으면 원본이 복원돼요. 백업이 없는 결과는 버릴 수 없어 그대로 남아요. 원본 파일은 절대 삭제되지 않아요.
OGG (Vorbis) cannot go below about 50 kbps (stereo), so sources that are already low-bitrate will not shrink and the result can be larger. Results larger than the source can be removed with the Discard larger results button after the process finishes. For smaller files choose OGG (Opus) or M4A. | OGG (Vorbis) は（ステレオで）約 50 kbps より下げられないため、すでにビットレートの低いソースは小さくならず、結果が大きくなることがあります。ソースより大きくなった結果は、処理の完了後に「大きくなった結果を破棄」ボタンで削除できます。より小さいファイルにするには OGG (Opus) または M4A を選んでください。 | OGG (Vorbis) 的码率（立体声）无法低于约 50 kbps，因此本身码率已很低的源文件不会变小，结果甚至可能更大。比源文件更大的结果可在处理完成后用“丢弃变大的结果”按钮删除。若想得到更小的文件，请选择 OGG (Opus) 或 M4A。 | OGG (Vorbis)는 (스테레오 기준) 약 50 kbps 아래로는 내릴 수 없어서, 이미 비트레이트가 낮은 원본은 작아지지 않고 결과가 더 커질 수 있어요. 원본보다 커진 결과는 처리가 끝난 뒤 ‘더 커진 결과 버리기’ 버튼으로 지울 수 있어요. 더 작은 파일을 원하면 OGG (Opus)나 M4A를 고르세요.
{0}% smaller | {0}% 小さい | 减小 {0}% | {0}% 작아짐
{0}% larger | {0}% 大きい | 增大 {0}% | {0}% 커짐
Encoders come from the device. H.265 and VP9 are not on every device; if missing, the file is marked as failed. WebM needs Android 10+ to keep its audio (Opus). | エンコーダーは端末のものが使われます。H.265 と VP9 はすべての端末にあるわけではなく、ない場合はそのファイルは失敗扱いになります。WebM で音声（Opus）を保持するには Android 10 以上が必要です。 | 编码器由设备提供。并非所有设备都支持 H.265 和 VP9，缺失时该文件会被标记为失败。WebM 需要 Android 10 及以上才能保留其音频 (Opus)。 | 인코더는 기기에 내장된 것을 사용해요. H.265와 VP9은 모든 기기에 있는 것은 아니며, 없으면 해당 파일은 실패로 표시돼요. WebM의 오디오(Opus)를 유지하려면 Android 10 이상이 필요해요.
Resolution is only reduced (never enlarged) and frame rate only lowered. AAC audio is copied without re-encoding; other audio is encoded to AAC 128 kbps. | 解像度は下げるだけ（拡大はしません）、フレームレートも下げるだけです。AAC 音声は再エンコードせずにコピーされ、それ以外の音声は AAC 128 kbps にエンコードされます。 | 分辨率只会降低（不会放大），帧率也只会降低。AAC 音频直接复制而不重新编码；其他音频会编码为 AAC 128 kbps。 | 해상도는 줄이기만 하고(키우지 않음) 프레임 레이트도 낮추기만 해요. AAC 오디오는 다시 인코딩하지 않고 복사하며, 그 밖의 오디오는 AAC 128 kbps로 인코딩해요.
Files that are already UTF-8 (or already in the target encoding) keep their content and count as "unchanged". Shift-JIS uses CP932 (windows-31j), which includes the Windows extra characters. | すでに UTF-8（またはすでに対象のエンコード）のファイルは内容がそのまま保たれ、「変更なし」として数えられます。Shift-JIS は CP932（windows-31j）を使い、Windows の拡張文字を含みます。 | 已经是 UTF-8（或已是目标编码）的文件会保留原内容，并计为“未更改”。Shift-JIS 使用 CP932 (windows-31j)，包含 Windows 扩展字符。 | 이미 UTF-8(또는 이미 대상 인코딩)인 파일은 내용이 그대로 유지되며 ‘변경 없음’으로 집계돼요. Shift-JIS는 Windows 확장 문자를 포함하는 CP932(windows-31j)를 사용해요.
Pick .pcm (XPCM) and/or .crx files extracted from Circus archives (PCK/CRM). The type is recognised by extension and the result keeps the name with the new extension. | Circus アーカイブ（PCK/CRM）から展開した .pcm（XPCM）および/または .crx ファイルを選んでください。種類は拡張子で判別され、結果は新しい拡張子の名前で保存されます。 | 请选择从 Circus 压缩包 (PCK/CRM) 中提取出的 .pcm (XPCM) 和/或 .crx 文件。类型按扩展名识别，结果沿用原名称并使用新扩展名。 | Circus 아카이브(PCK/CRM)에서 추출한 .pcm(XPCM) 및/또는 .crx 파일을 고르세요. 유형은 확장자로 인식하며, 결과는 이름은 그대로 두고 새 확장자로 저장돼요.
Automatic: XPCM containing Ogg Vorbis becomes the original .ogg untouched (quality and loop tags kept), anything else becomes .wav. Always WAV: Ogg is decoded with Android's decoder first. Supported: Ogg, raw PCM and ADPCM; old MDCT-compressed XPCM (modes 1 and 3) is not supported yet and is reported as failed. | 自動: Ogg Vorbis を含む XPCM は元の .ogg にそのまま戻されます（品質とループタグは維持）。それ以外は .wav になります。常に WAV: Ogg はまず Android のデコーダーでデコードされます。対応: Ogg、生 PCM、ADPCM。古い MDCT 圧縮の XPCM（モード 1 と 3）はまだ未対応で、失敗として報告されます。 | 自动：包含 Ogg Vorbis 的 XPCM 会还原为原始 .ogg（保留音质和循环标签），其他情况转为 .wav。始终为 WAV：Ogg 会先用 Android 解码器解码。支持：Ogg、原始 PCM 和 ADPCM；旧版 MDCT 压缩的 XPCM（模式 1 和 3）暂不支持，将报告为失败。 | 자동: Ogg Vorbis가 들어 있는 XPCM은 원래의 .ogg로 그대로 복원되고(음질과 루프 태그 유지), 그 밖에는 .wav가 돼요. 항상 WAV: Ogg는 먼저 Android 디코더로 디코딩해요. 지원: Ogg, 원시 PCM, ADPCM. 오래된 MDCT 압축 XPCM(모드 1, 3)은 아직 지원하지 않으며 실패로 보고돼요.
CRX images become .png (24-bit, 32-bit with alpha, or paletted). Differential CRX (CRXD, e.g. facial expressions) is merged with its base image automatically; the base file must be selected/present in the same folder (its name is stored in the file). Zstd-compressed CRX (newer versions) is not supported yet. | CRX 画像は .png になります（24ビット、アルファ付き32ビット、またはパレット）。差分 CRX（CRXD。例: 表情）はベース画像と自動的に統合されます。ベースファイルは同じフォルダで選択済み/存在している必要があります（名前はファイル内に記録されています）。Zstd 圧縮の CRX（新しいバージョン）はまだ未対応です。 | CRX 图片会转为 .png（24 位、带 Alpha 的 32 位或调色板）。差分 CRX（CRXD，例如表情）会自动与其基础图像合并；基础文件必须已被选中或位于同一文件夹中（其名称记录在文件内）。使用 Zstd 压缩的 CRX（较新版本）暂不支持。 | CRX 이미지는 .png가 돼요(24비트, 알파가 있는 32비트 또는 팔레트). 차분 CRX(CRXD, 예: 표정)는 기본 이미지와 자동으로 합쳐져요. 기본 파일은 같은 폴더에서 선택했거나 존재해야 해요(이름은 파일 안에 저장되어 있어요). Zstd로 압축된 CRX(최신 버전)는 아직 지원하지 않아요.
General rules (format, file names, image size, text encoding), not a guarantee the game will run. Engine versions and plugins differ; still test in the game. | 一般的なルール（形式、ファイル名、画像サイズ、テキストのエンコード）であり、ゲームが動作することの保証ではありません。エンジンのバージョンやプラグインによって異なるため、必ずゲームで確認してください。 | 仅为通用规则（格式、文件名、图片尺寸、文本编码），并不保证游戏一定能运行。引擎版本和插件各不相同，仍请在游戏中实际测试。 | 일반적인 규칙(형식, 파일 이름, 이미지 크기, 텍스트 인코딩)일 뿐 게임이 실행된다는 보장은 아니에요. 엔진 버전과 플러그인에 따라 다르므로 게임에서 꼭 확인해 보세요.
Opens Repack with this source folder already chosen (the _backup folder is not packed). Choose the engine/archive format there. | このソースフォルダをあらかじめ選択した状態で再パックを開きます（_backup フォルダはパックされません）。エンジン/アーカイブ形式はそこで選んでください。 | 将打开重新打包，并已选好此源文件夹（_backup 文件夹不会被打包）。请在那里选择引擎/压缩包格式。 | 이 원본 폴더가 이미 선택된 상태로 다시 묶기를 열어요(_backup 폴더는 묶지 않아요). 엔진/아카이브 형식은 거기서 고르세요.
This feature prepares a game (especially RPG Maker MV/MZ) before it is built into an APK. APK builders often fail on file names with non-Latin characters (Hanzi, Kana, Hangul, etc.). The File Name Optimizer renames those files and folders to safe (ASCII) names, then updates the game files that refer to the old names (json, js, html, css, txt, ks, rpy, etc.) so the game still finds its assets. How it works: 1) pick the game folder (everything inside is scanned, including subfolders); 2) set the scope and options; 3) press Scan and Preview to see the list of new names and which files will change, without changing anything; 4) press Rename and Update References. Fill in the Backup Folder to keep copies of the original text files that get edited; every rename is also recorded in rename_mapping.json in the Source Folder so it can be reverted with the Restore Original Names button. Files encrypted by RPG Maker (.rpgmvp, .rpgmvo, etc.) are renamed too while File scope = All files with special characters, because references use the same base name. | この機能は、ゲーム（特に RPGツクール MV/MZ）を APK にビルドする前の準備を行います。APK ビルダーは、漢字・かな・ハングルなどの非ラテン文字を含むファイル名で失敗することがよくあります。ファイル名オプティマイザーは、そうしたファイルとフォルダを安全な（ASCII の）名前にリネームし、古い名前を参照しているゲームファイル（json、js、html、css、txt、ks、rpy など）も更新して、ゲームがアセットを見つけられるようにします。手順: 1) ゲームフォルダを選ぶ（サブフォルダを含め中身すべてがスキャンされます）。2) 対象範囲とオプションを設定する。3) 「スキャンしてプレビュー」を押して、何も変更せずに新しい名前の一覧と変更されるファイルを確認する。4) 「リネームして参照を更新」を押す。編集されるテキストファイルの元のコピーを残したい場合は、バックアップフォルダを入力してください。すべてのリネームは元フォルダの rename_mapping.json にも記録され、「元の名前に戻す」ボタンで元に戻せます。RPGツクールで暗号化されたファイル（.rpgmvp、.rpgmvo など）も、ファイル範囲が「特殊文字を含むすべてのファイル」の間はリネームされます。参照が同じベース名を使っているためです。 | 此功能用于在将游戏（尤其是 RPG Maker MV/MZ）打包成 APK 之前做好准备。APK 打包工具遇到含非拉丁字符（汉字、假名、谚文等）的文件名时常常会失败。文件名优化器会把这些文件和文件夹重命名为安全的（ASCII）名称，并更新引用了旧名称的游戏文件（json、js、html、css、txt、ks、rpy 等），让游戏仍能找到资源。工作方式：1) 选择游戏文件夹（其中所有内容都会被扫描，包括子文件夹）；2) 设置范围和选项；3) 点击“扫描并预览”，在不做任何更改的情况下查看新名称列表以及将被修改的文件；4) 点击“重命名并更新引用”。如果想保留被编辑的文本文件的原始副本，请填写备份文件夹；每次重命名也会记录在源文件夹中的 rename_mapping.json 里，可通过“恢复原始名称”按钮还原。当文件范围为“所有含特殊字符的文件”时，RPG Maker 加密的文件（.rpgmvp、.rpgmvo 等）也会被重命名，因为引用使用的是相同的基础名称。 | 이 기능은 게임(특히 RPG Maker MV/MZ)을 APK로 빌드하기 전에 준비해 줘요. APK 빌더는 한자, 가나, 한글 같은 비라틴 문자가 들어간 파일 이름에서 자주 실패해요. 파일 이름 최적화는 그런 파일과 폴더의 이름을 안전한(ASCII) 이름으로 바꾸고, 예전 이름을 참조하는 게임 파일(json, js, html, css, txt, ks, rpy 등)도 업데이트해서 게임이 에셋을 계속 찾을 수 있게 해요. 사용 방법: 1) 게임 폴더를 고릅니다(하위 폴더를 포함해 안의 모든 것을 스캔해요). 2) 범위와 옵션을 설정합니다. 3) ‘스캔 및 미리보기’를 눌러 아무것도 바꾸지 않고 새 이름 목록과 수정될 파일을 확인합니다. 4) ‘이름 변경 및 참조 업데이트’를 누릅니다. 수정되는 텍스트 파일의 원본 사본을 남기려면 백업 폴더를 입력하세요. 모든 이름 변경은 원본 폴더의 rename_mapping.json에도 기록되어 ‘원래 이름으로 복원’ 버튼으로 되돌릴 수 있어요. RPG Maker로 암호화된 파일(.rpgmvp, .rpgmvo 등)도 파일 범위가 ‘특수 문자가 있는 모든 파일’인 동안에는 이름이 바뀌어요. 참조가 같은 기본 이름을 쓰기 때문이에요.
All files with special characters: every file (and folder, if included) whose name has non-ASCII characters is renamed, including RPG Maker encrypted files (.rpgmvp, .rpgmvo, .png_, .ogg_), data, plugins and others. Only media assets: only images, audio and video (png, jpg, webp, ogg, m4a, mp3, wav, mp4, webm, etc.) are renamed; other files are left alone. | 特殊文字を含むすべてのファイル: 名前に非 ASCII 文字を含むすべてのファイル（含める設定ならフォルダも）が、RPGツクールの暗号化ファイル（.rpgmvp、.rpgmvo、.png_、.ogg_）、データ、プラグインなどを含めてリネームされます。メディアアセットのみ: 画像、オーディオ、動画（png、jpg、webp、ogg、m4a、mp3、wav、mp4、webm など）だけがリネームされ、それ以外のファイルはそのままです。 | 所有含特殊字符的文件：名称含非 ASCII 字符的所有文件（若选择包含则也包括文件夹）都会被重命名，包括 RPG Maker 加密文件（.rpgmvp、.rpgmvo、.png_、.ogg_）、数据、插件等。仅媒体资源：只重命名图片、音频和视频（png、jpg、webp、ogg、m4a、mp3、wav、mp4、webm 等），其他文件保持不变。 | 특수 문자가 있는 모든 파일: 이름에 비ASCII 문자가 있는 모든 파일(포함하도록 설정하면 폴더도)의 이름이 바뀌며, RPG Maker 암호화 파일(.rpgmvp, .rpgmvo, .png_, .ogg_), 데이터, 플러그인 등도 포함돼요. 미디어 에셋만: 이미지, 오디오, 비디오(png, jpg, webp, ogg, m4a, mp3, wav, mp4, webm 등)만 이름을 바꾸고 다른 파일은 그대로 둬요.
Keep the ASCII part: Latin letters and digits from the old name are reused, followed by a serial number (BGM_戦闘01 → BGM_01_001); if there are none, file_001 is used. The same name in different folders gets the same new name, so references stay consistent. Numbers are skipped when they clash with existing files. | ASCII 部分を残す: 古い名前のラテン文字と数字を再利用し、その後に連番を付けます（BGM_戦闘01 → BGM_01_001）。ない場合は file_001 が使われます。異なるフォルダにある同じ名前には同じ新しい名前が付くため、参照の整合性が保たれます。既存のファイルと衝突する番号はスキップされます。 | 保留 ASCII 部分：沿用旧名称中的拉丁字母和数字，后面加上序号（BGM_戦闘01 → BGM_01_001）；如果没有，则使用 file_001。不同文件夹中的同名文件会得到相同的新名称，因此引用保持一致。与现有文件冲突的编号会被跳过。 | ASCII 부분 유지: 예전 이름의 라틴 문자와 숫자를 재사용하고 그 뒤에 일련번호를 붙여요(BGM_戦闘01 → BGM_01_001). 없으면 file_001을 사용해요. 서로 다른 폴더의 같은 이름은 같은 새 이름을 받아 참조가 일관되게 유지돼요. 기존 파일과 겹치는 번호는 건너뛰어요.
Off by default. When checked, names that use only Latin letters but contain spaces or marks such as & # % ' ( ) ! are renamed too (marks become _, e.g. "Actor 1.png" → "Actor_1.png"). | デフォルトはオフです。オンにすると、ラテン文字だけでもスペースや & # % ' ( ) ! などの記号を含む名前がリネームされます（記号は _ になります。例: "Actor 1.png" → "Actor_1.png"）。 | 默认关闭。勾选后，即使只由拉丁字母组成、但含有空格或 & # % ' ( ) ! 等符号的名称也会被重命名（符号会变成 _，例如“Actor 1.png”→“Actor_1.png”）。 | 기본값은 꺼짐이에요. 체크하면 라틴 문자만 쓰였더라도 공백이나 & # % ' ( ) ! 같은 기호가 들어 있는 이름도 바뀌어요(기호는 _로 바뀝니다. 예: "Actor 1.png" → "Actor_1.png").
Safe (recommended): a name is replaced only when it stands whole as a value, for example "背景", "背景.png", "img/pictures/背景", storage=背景.png, including \u80cc\u666f and %E8%83%8C forms which are decoded first. Dialogue text that happens to contain the same word is not touched. Wide: every occurrence of the name (2+ characters) is replaced anywhere, like the original script; useful when the game builds names from pieces of text, but it may also change dialogue or character names that are the same. Check the Preview before using this mode. | 安全（推奨）: 名前が値として単独で現れる場合のみ置き換えます。例: "背景"、"背景.png"、"img/pictures/背景"、storage=背景.png、および先にデコードされる \u80cc\u666f や %E8%83%8C の形式。たまたま同じ単語を含む会話文は変更されません。広範: 名前（2文字以上）のすべての出現箇所を、元のスクリプトのようにどこでも置き換えます。ゲームがテキストの断片から名前を組み立てる場合に便利ですが、同じ文字列の会話文やキャラクター名まで変わることがあります。このモードを使う前にプレビューを確認してください。 | 安全（推荐）：仅当名称作为完整的值出现时才替换，例如“背景”、“背景.png”、“img/pictures/背景”、storage=背景.png，以及会先被解码的 \u80cc\u666f 和 %E8%83%8C 形式。恰好含有相同词语的对话文本不会被改动。宽松：像原始脚本一样，替换任何位置出现的名称（2 个字符以上）；当游戏用文本片段拼接名称时很有用，但也可能改动相同的对话或角色名。使用此模式前请先查看预览。 | 안전(권장): 이름이 값으로 온전히 나올 때만 바꿔요. 예: "背景", "背景.png", "img/pictures/背景", storage=背景.png, 그리고 먼저 디코딩되는 \u80cc\u666f, %E8%83%8C 형태. 우연히 같은 단어가 들어 있는 대사 텍스트는 건드리지 않아요. 넓게: 원래 스크립트처럼 이름(2자 이상)이 나오는 모든 곳을 바꿔요. 게임이 텍스트 조각으로 이름을 만들 때 유용하지만, 같은 이름의 대사나 캐릭터 이름도 바뀔 수 있어요. 이 모드를 쓰기 전에 미리보기를 확인하세요.
Android is case-sensitive, Windows is not. Games from Windows often call Bgm1.ogg while the file is bgm1.ogg, so the asset fails to load in the APK. When checked, references that differ from the real file name only in letter case are corrected (Safe mode only). Ambiguous names (two files that differ only in case) are skipped. This fix is not reverted by Restore Original Names; use a Backup Folder if you want to be able to recover it. | Android は大文字小文字を区別しますが、Windows は区別しません。Windows 用のゲームは、ファイルが bgm1.ogg なのに Bgm1.ogg と呼び出すことが多く、APK でアセットが読み込めなくなります。オンにすると、実際のファイル名と大文字小文字だけが異なる参照が修正されます（安全モードのみ）。曖昧な名前（大文字小文字だけが違う2つのファイル）はスキップされます。この修正は「元の名前に戻す」では元に戻りません。復元できるようにしたい場合はバックアップフォルダを使ってください。 | Android 区分大小写，Windows 则不区分。来自 Windows 的游戏常常调用 Bgm1.ogg，而文件实际是 bgm1.ogg，导致资源在 APK 中无法加载。勾选后，仅大小写与真实文件名不同的引用会被修正（仅限安全模式）。有歧义的名称（两个文件只有大小写不同）会被跳过。此修正无法通过“恢复原始名称”撤销；如需能够恢复，请使用备份文件夹。 | Android는 대소문자를 구분하지만 Windows는 구분하지 않아요. Windows용 게임은 파일이 bgm1.ogg인데 Bgm1.ogg로 부르는 경우가 많아서 APK에서 에셋을 불러오지 못해요. 체크하면 실제 파일 이름과 대소문자만 다른 참조가 수정돼요(안전 모드에서만). 모호한 이름(대소문자만 다른 파일 두 개)은 건너뛰어요. 이 수정은 ‘원래 이름으로 복원’으로 되돌려지지 않으니, 복구하고 싶다면 백업 폴더를 사용하세요.
To rename: {0} files and {1} folders. References updated in {2} text files ({3} replacements, {4} case fixes). | リネーム対象: ファイル {0} 個、フォルダ {1} 個。テキストファイル {2} 個で参照を更新（置換 {3} 件、大文字小文字の修正 {4} 件）。 | 待重命名：{0} 个文件和 {1} 个文件夹。在 {2} 个文本文件中更新引用（{3} 处替换，{4} 处大小写修正）。 | 이름 변경 대상: 파일 {0}개, 폴더 {1}개. 텍스트 파일 {2}개에서 참조 업데이트(교체 {3}건, 대소문자 수정 {4}건).
To rename: {0} files and {1} folders. References updated in {2} text files ({3} replacements). | リネーム対象: ファイル {0} 個、フォルダ {1} 個。テキストファイル {2} 個で参照を更新（置換 {3} 件）。 | 待重命名：{0} 个文件和 {1} 个文件夹。在 {2} 个文本文件中更新引用（{3} 处替换）。 | 이름 변경 대상: 파일 {0}개, 폴더 {1}개. 텍스트 파일 {2}개에서 참조 업데이트(교체 {3}건).
No Backup Folder is set. The game's text files will be changed directly with no backup copy. File and folder names can still be restored through rename_mapping.json, but text contents are only restored by reverse replacement (not from original copies) and letter-case fixes cannot be restored. Backing up the game folder first is recommended. Continue without a backup? | バックアップフォルダが設定されていません。ゲームのテキストファイルはバックアップのコピーなしで直接変更されます。ファイル名とフォルダ名は rename_mapping.json から復元できますが、テキストの内容は逆置換でしか戻せず（元のコピーからではありません）、大文字小文字の修正は復元できません。先にゲームフォルダをバックアップすることをおすすめします。バックアップなしで続行しますか? | 未设置备份文件夹。游戏的文本文件将被直接修改，不会生成备份副本。文件名和文件夹名仍可通过 rename_mapping.json 恢复，但文本内容只能通过反向替换来还原（不是从原始副本恢复），并且大小写修正无法恢复。建议先备份游戏文件夹。要在没有备份的情况下继续吗？ | 백업 폴더가 설정되지 않았어요. 게임의 텍스트 파일이 백업 사본 없이 직접 수정돼요. 파일과 폴더 이름은 rename_mapping.json으로 복원할 수 있지만, 텍스트 내용은 (원본 사본이 아니라) 역치환으로만 되돌릴 수 있고 대소문자 수정은 복원할 수 없어요. 먼저 게임 폴더를 백업하는 것을 권장해요. 백업 없이 계속할까요?
No API key needed; uses public Lingva servers (Google Translate quality). If every server fails, the free Google Translate endpoint (unofficial, may be rate limited) is tried. Game text is sent to a third-party server; public servers can be slow or down. | API キーは不要です。公開されている Lingva サーバー（Google 翻訳相当の品質）を使います。すべてのサーバーが失敗した場合は、無料の Google 翻訳エンドポイント（非公式で、レート制限される場合があります）を試します。ゲームのテキストはサードパーティのサーバーに送信されます。公開サーバーは遅かったり停止していたりすることがあります。 | 无需 API 密钥；使用公共 Lingva 服务器（Google 翻译的质量）。如果所有服务器都失败，会尝试免费的 Google 翻译接口（非官方，可能受到频率限制）。游戏文本会发送到第三方服务器；公共服务器可能较慢或不可用。 | API 키는 필요 없어요. 공개 Lingva 서버(Google 번역 수준의 품질)를 사용해요. 모든 서버가 실패하면 무료 Google 번역 엔드포인트(비공식이며 요청 제한이 걸릴 수 있어요)를 시도해요. 게임 텍스트는 제3자 서버로 전송되며, 공개 서버는 느리거나 중단될 수 있어요.
Free, no API key needed. Run by Bilibili (Index-Translate), which publishes no usage limits. Captured text is sent to Bilibili for translation. | 無料で API キーは不要です。Bilibili（Index-Translate）が運営しており、利用制限は公開されていません。取得したテキストは翻訳のために Bilibili に送信されます。 | 免费，无需 API 密钥。由哔哩哔哩 (Index-Translate) 运营，未公布使用限制。抓取到的文本会发送给哔哩哔哩进行翻译。 | 무료이며 API 키가 필요 없어요. Bilibili(Index-Translate)에서 운영하며 사용 한도는 공개되어 있지 않아요. 가져온 텍스트는 번역을 위해 Bilibili로 전송돼요.
9B = balanced (recommended), 35B-A3B = most accurate but still a preview, 2B = fastest, Nailong = long texts. The glossary uses placeholders. | 9B = バランス型（推奨）、35B-A3B = 最も正確だがまだプレビュー版、2B = 最速、Nailong = 長文向け。用語集にはプレースホルダーが使われます。 | 9B = 均衡（推荐），35B-A3B = 最准确但仍为预览版，2B = 最快，Nailong = 长文本。术语表使用占位符。 | 9B = 균형형(권장), 35B-A3B = 가장 정확하지만 아직 미리보기 단계, 2B = 가장 빠름, Nailong = 긴 텍스트용. 용어집은 자리표시자를 사용해요.
The key is stored on this device only, excluded from backups and never logged. Text to translate is sent to the selected provider. | キーはこの端末にのみ保存され、バックアップには含まれず、ログにも記録されません。翻訳するテキストは選択したプロバイダーに送信されます。 | 密钥仅保存在此设备上，不会包含在备份中，也不会写入日志。待翻译的文本会发送给所选的服务商。 | 키는 이 기기에만 저장되고 백업에서 제외되며 로그에도 남지 않아요. 번역할 텍스트는 선택한 제공업체로 전송돼요.
For Online Translation only, separate from the editor glossary. Gemini/OpenAI receive it in the prompt; Lingva, Google and DeepL use placeholders (good for names/terms, less precise for common words like "I"). | オンライン翻訳専用で、エディターの用語集とは別です。Gemini/OpenAI にはプロンプトで渡され、Lingva、Google、DeepL にはプレースホルダーが使われます（名前や用語には向いていますが、"I" のような一般的な単語では精度が下がります）。 | 仅用于在线翻译，与编辑器的术语表相互独立。Gemini/OpenAI 通过提示词接收它；Lingva、Google 和 DeepL 使用占位符（适合人名和术语，但对“I”这类常用词不够精确）。 | 온라인 번역에만 쓰이며 편집기의 용어집과는 별개예요. Gemini/OpenAI에는 프롬프트로 전달되고, Lingva, Google, DeepL은 자리표시자를 사용해요(이름이나 용어에는 좋지만 "I" 같은 일반 단어에는 덜 정확해요).
Done: {0} row(s) translated ({1} from cache), {2} failed (control codes lost/error), you can retry, {3} without text skipped. | 完了: {0} 行を翻訳しました（キャッシュから {1} 行）、{2} 行が失敗しました（制御コードの消失/エラー）。再試行できます、テキストのない {3} 行をスキップしました。 | 完成：已翻译 {0} 行（{1} 行来自缓存），{2} 行失败（控制码丢失/出错），可以重试，跳过了 {3} 行无文本的内容。 | 완료: {0}행 번역(캐시에서 {1}행), {2}행 실패(제어 코드 손실/오류), 다시 시도할 수 있어요, 텍스트가 없는 {3}행은 건너뜀.
Done: {0} row(s) translated, {1} failed (control codes lost/error), you can retry, {2} without text skipped. | 完了: {0} 行を翻訳しました、{1} 行が失敗しました（制御コードの消失/エラー）。再試行できます、テキストのない {2} 行をスキップしました。 | 完成：已翻译 {0} 行，{1} 行失败（控制码丢失/出错），可以重试，跳过了 {2} 行无文本的内容。 | 완료: {0}행 번역, {1}행 실패(제어 코드 손실/오류), 다시 시도할 수 있어요, 텍스트가 없는 {2}행은 건너뜀.
Done: {0} row(s) translated ({1} from cache), {2} failed (control codes lost/error), you can retry. | 完了: {0} 行を翻訳しました（キャッシュから {1} 行）、{2} 行が失敗しました（制御コードの消失/エラー）。再試行できます。 | 完成：已翻译 {0} 行（{1} 行来自缓存），{2} 行失败（控制码丢失/出错），可以重试。 | 완료: {0}행 번역(캐시에서 {1}행), {2}행 실패(제어 코드 손실/오류), 다시 시도할 수 있어요.
Done: {0} row(s) translated, {1} failed (control codes lost/error), you can retry. | 完了: {0} 行を翻訳しました、{1} 行が失敗しました（制御コードの消失/エラー）。再試行できます。 | 完成：已翻译 {0} 行，{1} 行失败（控制码丢失/出错），可以重试。 | 완료: {0}행 번역, {1}행 실패(제어 코드 손실/오류), 다시 시도할 수 있어요.
Done: {0} row(s) translated ({1} from cache), {2} without text skipped. | 完了: {0} 行を翻訳しました（キャッシュから {1} 行）、テキストのない {2} 行をスキップしました。 | 完成：已翻译 {0} 行（{1} 行来自缓存），跳过了 {2} 行无文本的内容。 | 완료: {0}행 번역(캐시에서 {1}행), 텍스트가 없는 {2}행은 건너뜀.
Done: {0} row(s) translated, {1} without text skipped. | 完了: {0} 行を翻訳しました、テキストのない {1} 行をスキップしました。 | 完成：已翻译 {0} 行，跳过了 {1} 行无文本的内容。 | 완료: {0}행 번역, 텍스트가 없는 {1}행은 건너뜀.
Done: {0} row(s) translated ({1} from cache). | 完了: {0} 行を翻訳しました（キャッシュから {1} 行）。 | 完成：已翻译 {0} 行（{1} 行来自缓存）。 | 완료: {0}행 번역(캐시에서 {1}행).
Done: {0} row(s) translated. | 完了: {0} 行を翻訳しました。 | 完成：已翻译 {0} 行。 | 완료: {0}행 번역.
Pick the plugin folder of an RPG Maker MV/MZ game (js/plugins). The game or js folder also works; only js/plugins is scanned. | RPGツクール MV/MZ ゲームのプラグインフォルダ（js/plugins）を選んでください。ゲームフォルダや js フォルダでも使えますが、スキャンされるのは js/plugins のみです。 | 请选择 RPG Maker MV/MZ 游戏的插件文件夹 (js/plugins)。选择游戏文件夹或 js 文件夹也可以，但只会扫描 js/plugins。 | RPG Maker MV/MZ 게임의 플러그인 폴더(js/plugins)를 고르세요. 게임 폴더나 js 폴더도 되지만 js/plugins만 스캔해요.
Fill only the items you want to change; an empty field = unchanged. Texts in code are rewritten with proper escaping. Code defaults (e.g. parameters['x'] || 10) apply when plugins.js does not set them; annotation values (@default) only change the default shown in the RPG Maker editor. | 変更したい項目だけを入力してください。空欄は変更なしです。コード内のテキストは適切にエスケープして書き換えられます。コード内の既定値（例: parameters['x'] || 10）は plugins.js で設定されていない場合に適用されます。アノテーションの値（@default）は、RPGツクールのエディターに表示される既定値だけを変更します。 | 只填写想要修改的项目；留空表示不更改。代码中的文本会以正确的转义方式改写。代码默认值（例如 parameters['x'] || 10）在 plugins.js 未设置时生效；注解值 (@default) 只会改变 RPG Maker 编辑器中显示的默认值。 | 바꾸고 싶은 항목만 채우세요. 비워 두면 변경하지 않아요. 코드 안의 텍스트는 올바르게 이스케이프하여 다시 써요. 코드 기본값(예: parameters['x'] || 10)은 plugins.js에 설정이 없을 때 적용돼요. 주석 값(@default)은 RPG Maker 편집기에 표시되는 기본값만 바꿔요.
plugins.js could not be read. Make sure it holds the standard ${'$'}plugins list. | plugins.js を読み込めませんでした。標準の ${'$'}plugins リストが入っていることを確認してください。 | 无法读取 plugins.js。请确认其中包含标准的 ${'$'}plugins 列表。 | plugins.js를 읽을 수 없어요. 표준 ${'$'}plugins 목록이 들어 있는지 확인하세요.
plugins.js saved ({0} plugin(s)). {1} new plugin file(s) copied to output/plugins. | plugins.js を保存しました（プラグイン {0} 個）。 新しいプラグインファイル {1} 個を output/plugins にコピーしました。 | 已保存 plugins.js（{0} 个插件）。 已将 {1} 个新插件文件复制到 output/plugins。 | plugins.js를 저장했어요(플러그인 {0}개). 새 플러그인 파일 {1}개를 output/plugins에 복사했어요.
plugins.js saved ({0} plugin(s)). | plugins.js を保存しました（プラグイン {0} 個）。 | 已保存 plugins.js（{0} 个插件）。 | plugins.js를 저장했어요(플러그인 {0}개).
plugins.js is written to the output folder; new plugin files are copied into a plugins/ subfolder. Copy them to the game's js/ (plugins.js) and js/plugins/ (plugin files). | plugins.js は出力フォルダに書き出され、新しいプラグインファイルは plugins/ サブフォルダにコピーされます。ゲームの js/（plugins.js）と js/plugins/（プラグインファイル）にコピーしてください。 | plugins.js 会写入输出文件夹；新的插件文件会复制到 plugins/ 子文件夹中。请把它们复制到游戏的 js/（plugins.js）和 js/plugins/（插件文件）。 | plugins.js는 출력 폴더에 기록되고, 새 플러그인 파일은 plugins/ 하위 폴더로 복사돼요. 게임의 js/(plugins.js)와 js/plugins/(플러그인 파일)로 복사하세요.
"""
    const val T20 = """
Script type is detected per file: .rpy (Ren'Py), .ks (Kirikiri/Tyrano), .s (QLIE), .cst (CatSystem 2), .dat (SiglusEngine) and .txt/.ast (NScripter/Artemis). "Auto" encoding fits most files. | スクリプトの種類はファイルごとに判別されます: .rpy（Ren'Py）、.ks（Kirikiri/Tyrano）、.s（QLIE）、.cst（CatSystem 2）、.dat（SiglusEngine）、.txt/.ast（NScripter/Artemis）。ほとんどのファイルでは「自動」のエンコードが適しています。 | 脚本类型按文件分别识别：.rpy (Ren'Py)、.ks (Kirikiri/Tyrano)、.s (QLIE)、.cst (CatSystem 2)、.dat (SiglusEngine) 以及 .txt/.ast (NScripter/Artemis)。“自动”编码适用于大多数文件。 | 스크립트 유형은 파일마다 판별해요: .rpy(Ren'Py), .ks(Kirikiri/Tyrano), .s(QLIE), .cst(CatSystem 2), .dat(SiglusEngine), .txt/.ast(NScripter/Artemis). 대부분의 파일에는 ‘자동’ 인코딩이 맞아요.
Output keeps the folder structure (script.rpyc → script.rpy). Screens are not decompiled yet and are written as comments. | 出力はフォルダ構造を維持します（script.rpyc → script.rpy）。スクリーンはまだデコンパイルされず、コメントとして書き出されます。 | 输出会保留文件夹结构（script.rpyc → script.rpy）。界面 (screen) 暂不支持反编译，会写成注释。 | 출력은 폴더 구조를 유지해요(script.rpyc → script.rpy). 스크린은 아직 디컴파일하지 않고 주석으로 기록돼요.
".ks" scripts: each text line (inline tags such as [r] [p] included) becomes one entry; do not change or remove tags. ".scn" (PSB) scripts: speaker names, messages and choices; newline = real newline. If the file already has an extra language slot, the translation goes to the last slot; otherwise the original text is overwritten. | ".ks" スクリプト: 各テキスト行（[r] [p] などのインラインタグを含む）が1つのエントリになります。タグは変更・削除しないでください。".scn"（PSB）スクリプト: 話者名、メッセージ、選択肢。改行は実際の改行です。ファイルにすでに追加の言語スロットがある場合、翻訳は最後のスロットに入り、ない場合は元のテキストが上書きされます。 | “.ks”脚本：每一行文本（包括 [r] [p] 等行内标签）成为一个条目，请勿修改或删除标签。“.scn” (PSB) 脚本：包含说话人名称、消息和选项，换行即真实换行。如果文件已有额外的语言槽位，译文会写入最后一个槽位；否则会覆盖原文。 | ".ks" 스크립트: 각 텍스트 줄([r] [p] 같은 인라인 태그 포함)이 항목 하나가 돼요. 태그는 바꾸거나 지우지 마세요. ".scn"(PSB) 스크립트: 화자 이름, 메시지, 선택지가 대상이며 줄바꿈은 실제 줄바꿈이에요. 파일에 이미 추가 언어 슬롯이 있으면 번역이 마지막 슬롯에 들어가고, 없으면 원문을 덮어써요.
Text lines, speaker names (#name) and [glink] button texts are collected. Do not change or remove tags. | テキスト行、話者名（#name）、[glink] ボタンのテキストが収集されます。タグは変更・削除しないでください。 | 会收集文本行、说话人名称 (#name) 和 [glink] 按钮文本。请勿修改或删除标签。 | 텍스트 줄, 화자 이름(#name), [glink] 버튼 텍스트를 수집해요. 태그는 바꾸거나 지우지 마세요.
Text lines and select choices are taken from the decoded 0.txt. Pick the encoding that matches the game. | テキスト行と select の選択肢は、デコードされた 0.txt から取得されます。ゲームに合ったエンコードを選んでください。 | 文本行和 select 选项取自解码后的 0.txt。请选择与游戏匹配的编码。 | 텍스트 줄과 select 선택지는 디코딩된 0.txt에서 가져와요. 게임에 맞는 인코딩을 고르세요.
Dialogue lines ([n] = line break, keep it) and ^select choices are taken from text .s scripts. Pick the encoding that matches the game. | セリフ行（[n] = 改行。そのまま残してください）と ^select の選択肢がテキスト形式の .s スクリプトから取得されます。ゲームに合ったエンコードを選んでください。 | 对话行（[n] = 换行，请保留）和 ^select 选项取自文本格式的 .s 脚本。请选择与游戏匹配的编码。 | 대사 줄([n] = 줄바꿈, 그대로 두세요)과 ^select 선택지는 텍스트형 .s 스크립트에서 가져와요. 게임에 맞는 인코딩을 고르세요.
All dialogue/narration lines of Artemis .txt files are collected (# names, * labels, [tags] and // comments skipped). Pick the encoding that matches the game. .ast files: dialogue, choices, speaker names and ruby text are collected; tags and file names stay untouched. | Artemis .txt ファイルのすべてのセリフ/地の文が収集されます（# 名前、* ラベル、[タグ]、// コメントはスキップ）。ゲームに合ったエンコードを選んでください。.ast ファイル: セリフ、選択肢、話者名、ルビテキストが収集され、タグとファイル名はそのままです。 | 会收集 Artemis .txt 文件中的所有对话/旁白行（跳过 # 名称、* 标签、[标签] 和 // 注释）。请选择与游戏匹配的编码。.ast 文件：会收集对话、选项、说话人名称和注音文本；标签和文件名保持不变。 | Artemis .txt 파일의 모든 대사/지문 줄을 수집해요(# 이름, * 라벨, [태그], // 주석은 건너뜀). 게임에 맞는 인코딩을 고르세요. .ast 파일: 대사, 선택지, 화자 이름, 루비 텍스트를 수집하며 태그와 파일 이름은 그대로 둬요.
Dialogue, speaker names and choice command texts are collected from .cst files. Pick the game's encoding (usually Shift-JIS). | .cst ファイルから、セリフ、話者名、選択肢コマンドのテキストが収集されます。ゲームのエンコードを選んでください（通常は Shift-JIS）。 | 会从 .cst 文件中收集对话、说话人名称和选项命令文本。请选择游戏所用的编码（通常为 Shift-JIS）。 | .cst 파일에서 대사, 화자 이름, 선택지 명령 텍스트를 수집해요. 게임의 인코딩을 고르세요(보통 Shift-JIS).
Dialogue/name/choice texts are collected from SiglusEngine scene .dat files (from an unpacked Scene.pck) by filtering non-ASCII strings, not by bytecode analysis, so a few internal strings may show up. Delete such lines from the export if you do not want them changed. | セリフ/名前/選択肢のテキストは、SiglusEngine のシーン .dat ファイル（展開した Scene.pck のもの）から、バイトコード解析ではなく非 ASCII 文字列のフィルタリングで収集されるため、内部文字列が少し混ざることがあります。変更したくない場合は、そうした行をエクスポートから削除してください。 | 对话/名称/选项文本是从 SiglusEngine 场景 .dat 文件（来自已解包的 Scene.pck）中，通过过滤非 ASCII 字符串收集的，而非字节码分析，因此可能混入少量内部字符串。如果不想修改这些内容，请从导出中删除相应行。 | 대사/이름/선택지 텍스트는 SiglusEngine 장면 .dat 파일(압축을 푼 Scene.pck의 것)에서 바이트코드 분석이 아니라 비ASCII 문자열 필터링으로 수집해요. 그래서 내부 문자열이 일부 섞일 수 있어요. 바꾸고 싶지 않다면 내보낸 목록에서 그런 줄을 지우세요.
All strings from Escu:de binary scripts (ESCR1_00, unpacked .bin files) are collected; internal resource names are filtered. The string table is rewritten with new offsets on import. Use Shift-JIS for original games. | Escu:de バイナリスクリプト（ESCR1_00、展開した .bin ファイル）のすべての文字列が収集され、内部のリソース名は除外されます。インポート時に文字列テーブルは新しいオフセットで書き直されます。オリジナルのゲームでは Shift-JIS を使ってください。 | 会收集 Escu:de 二进制脚本（ESCR1_00、已解包的 .bin 文件）中的所有字符串，并过滤掉内部资源名称。导入时字符串表会以新的偏移量重写。原版游戏请使用 Shift-JIS。 | Escu:de 바이너리 스크립트(ESCR1_00, 압축을 푼 .bin 파일)의 모든 문자열을 수집하고 내부 리소스 이름은 걸러내요. 가져올 때 문자열 테이블은 새 오프셋으로 다시 써요. 원본 게임에는 Shift-JIS를 사용하세요.
pushstring texts of Favorite/FVP .hcb scripts are collected (ASCII resource names skipped). Jump/call addresses are recomputed on import. Texts over 254 bytes are left unchanged. Use Shift-JIS for original games. | Favorite/FVP .hcb スクリプトの pushstring テキストが収集されます（ASCII のリソース名はスキップ）。ジャンプ/コールのアドレスはインポート時に再計算されます。254 バイトを超えるテキストは変更されません。オリジナルのゲームでは Shift-JIS を使ってください。 | 会收集 Favorite/FVP .hcb 脚本中的 pushstring 文本（跳过 ASCII 资源名）。跳转/调用地址会在导入时重新计算。超过 254 字节的文本保持不变。原版游戏请使用 Shift-JIS。 | Favorite/FVP .hcb 스크립트의 pushstring 텍스트를 수집해요(ASCII 리소스 이름은 건너뜀). 점프/호출 주소는 가져올 때 다시 계산해요. 254바이트를 넘는 텍스트는 바꾸지 않아요. 원본 게임에는 Shift-JIS를 사용하세요.
Messages, choices and speaker names of WillPlus/AdvHD .ws2 scripts are collected through a disassembler (encrypted files are detected automatically). The %LC/%LF name prefix and %K%P message suffix are preserved; leave other % codes inside texts untouched. | WillPlus/AdvHD .ws2 スクリプトのメッセージ、選択肢、話者名が、逆アセンブラ経由で収集されます（暗号化されたファイルは自動検出）。%LC/%LF の名前プレフィックスと %K%P のメッセージサフィックスは保持されます。テキスト内のその他の % コードには触れないでください。 | 通过反汇编器收集 WillPlus/AdvHD .ws2 脚本的消息、选项和说话人名称（自动检测加密文件）。%LC/%LF 名称前缀和 %K%P 消息后缀会被保留；文本中的其他 % 代码请勿改动。 | WillPlus/AdvHD .ws2 스크립트의 메시지, 선택지, 화자 이름을 디스어셈블러로 수집해요(암호화된 파일은 자동 감지). %LC/%LF 이름 접두사와 %K%P 메시지 접미사는 그대로 유지돼요. 텍스트 안의 다른 % 코드는 건드리지 마세요.
Messages and speaker names of Silky Engine .mes scripts (Plus and Ai6Win variants detected automatically). Newline = real newline, ruby = [text]; do not nest [ ] or put newlines inside. Addresses are recomputed on import. | Silky Engine .mes スクリプト（Plus と Ai6Win のバリアントは自動検出）のメッセージと話者名です。改行は実際の改行、ルビは [text] です。[ ] を入れ子にしたり、中に改行を入れたりしないでください。アドレスはインポート時に再計算されます。 | Silky Engine .mes 脚本（自动检测 Plus 和 Ai6Win 变体）的消息和说话人名称。换行即真实换行，注音为 [text]；请勿嵌套 [ ] 或在其中放入换行。地址会在导入时重新计算。 | Silky Engine .mes 스크립트(Plus와 Ai6Win 변형은 자동 감지)의 메시지와 화자 이름이에요. 줄바꿈은 실제 줄바꿈이고 루비는 [text]예요. [ ]를 중첩하거나 그 안에 줄바꿈을 넣지 마세요. 주소는 가져올 때 다시 계산해요.
Speaker names and messages of Adv32 (Circus) .mes scripts. Latin letters are converted from full-width to ASCII on export and back to full-width on import (the game font has no half-width letters). Newline = real newline. The header offset table is recomputed on import; characters outside Shift-JIS become ？. | Adv32（Circus）.mes スクリプトの話者名とメッセージです。ラテン文字はエクスポート時に全角から ASCII に変換され、インポート時に全角に戻されます（ゲームのフォントに半角文字がないため）。改行は実際の改行です。ヘッダーのオフセットテーブルはインポート時に再計算され、Shift-JIS にない文字は ？ になります。 | Adv32 (Circus) .mes 脚本的说话人名称和消息。拉丁字母在导出时由全角转换为 ASCII，导入时再转回全角（游戏字体没有半角字母）。换行即真实换行。头部偏移表会在导入时重新计算；Shift-JIS 以外的字符会变成 ？。 | Adv32(Circus) .mes 스크립트의 화자 이름과 메시지예요. 라틴 문자는 내보낼 때 전각에서 ASCII로 바뀌고 가져올 때 다시 전각으로 돌아와요(게임 글꼴에 반각 문자가 없기 때문이에요). 줄바꿈은 실제 줄바꿈이에요. 헤더 오프셋 테이블은 가져올 때 다시 계산하며, Shift-JIS에 없는 문자는 ？로 바뀌어요.
Messages, names and choices of BGI/Ethornell scripts (V0, V1, and BurikoCompiledScriptVer1.00 with header). Scripts in archives are usually DSC-compressed: unpack with the Decode DSC option first. Changed texts are appended to the file end and pointers redirected. Ruby is left unchanged. Old V0 scripts are not auto-detected: use the BGI engine menu. | BGI/Ethornell スクリプト（V0、V1、ヘッダー付きの BurikoCompiledScriptVer1.00）のメッセージ、名前、選択肢です。アーカイブ内のスクリプトは通常 DSC 圧縮されているため、先に「DSC をデコード」オプションで展開してください。変更したテキストはファイル末尾に追加され、ポインタがリダイレクトされます。ルビは変更されません。古い V0 スクリプトは自動検出されないため、BGI エンジンのメニューを使ってください。 | BGI/Ethornell 脚本（V0、V1，以及带头部的 BurikoCompiledScriptVer1.00）的消息、名称和选项。压缩包中的脚本通常经过 DSC 压缩：请先使用“解码 DSC”选项解包。修改后的文本会追加到文件末尾，并重定向指针。注音保持不变。旧版 V0 脚本无法自动识别：请使用 BGI 引擎菜单。 | BGI/Ethornell 스크립트(V0, V1, 헤더가 있는 BurikoCompiledScriptVer1.00)의 메시지, 이름, 선택지예요. 아카이브 안의 스크립트는 보통 DSC로 압축되어 있으니 먼저 ‘DSC 디코드’ 옵션으로 풀어 주세요. 바뀐 텍스트는 파일 끝에 덧붙이고 포인터를 다시 연결해요. 루비는 바꾸지 않아요. 오래된 V0 스크립트는 자동 감지되지 않으니 BGI 엔진 메뉴를 사용하세요.
Yaneurao Itufuru script texts (scanned from text opcodes 0x02 and 0x1E). Lengths are rewritten on import; 0x02 lines must end with a newline (added automatically). Not auto-detected: run via the Itufuru engine menu. | Yaneurao Itufuru スクリプトのテキストです（テキストオペコード 0x02 と 0x1E からスキャン）。長さはインポート時に書き換えられ、0x02 の行は改行で終わる必要があります（自動で追加されます）。自動検出はされないため、Itufuru エンジンのメニューから実行してください。 | Yaneurao Itufuru 脚本的文本（从文本操作码 0x02 和 0x1E 扫描得到）。长度会在导入时重写；0x02 行必须以换行结尾（会自动添加）。不会自动识别：请通过 Itufuru 引擎菜单运行。 | Yaneurao Itufuru 스크립트의 텍스트예요(텍스트 오피코드 0x02와 0x1E에서 스캔). 길이는 가져올 때 다시 쓰며, 0x02 줄은 줄바꿈으로 끝나야 해요(자동으로 추가돼요). 자동 감지되지 않으니 Itufuru 엔진 메뉴로 실행하세요.
Texts (UTF-16) from the conststr section of EntisGLS .csx scripts, version 2.x–3.x; only non-ASCII strings are collected. Scripts come from the NOA archive (script.noa) — Key Finder can find the archive password. CSX 1.x (strings in the image section) is not supported. | EntisGLS .csx スクリプト（バージョン 2.x–3.x）の conststr セクションのテキスト（UTF-16）です。非 ASCII の文字列だけが収集されます。スクリプトは NOA アーカイブ（script.noa）から得られ、キー検出でアーカイブのパスワードを探せます。CSX 1.x（文字列がイメージセクションにある形式）は未対応です。 | EntisGLS .csx 脚本（2.x–3.x 版）conststr 段中的文本 (UTF-16)；只收集非 ASCII 字符串。脚本来自 NOA 压缩包 (script.noa)，可用密钥查找来找出压缩包密码。不支持 CSX 1.x（字符串位于镜像段中）。 | EntisGLS .csx 스크립트(버전 2.x–3.x)의 conststr 섹션 텍스트(UTF-16)예요. 비ASCII 문자열만 수집해요. 스크립트는 NOA 아카이브(script.noa)에서 가져오며, 키 찾기로 아카이브 비밀번호를 찾을 수 있어요. CSX 1.x(문자열이 이미지 섹션에 있는 형식)는 지원하지 않아요.
Shift-JIS texts from the text block of NeXAS .bin scripts (Script.pac); only non-ASCII segments are collected. Import rewrites the text block without recalculating offsets — length is free, but test in the game. | NeXAS .bin スクリプト（Script.pac）のテキストブロックにある Shift-JIS テキストです。非 ASCII のセグメントだけが収集されます。インポートはオフセットを再計算せずにテキストブロックを書き換えます。長さは自由ですが、ゲームで確認してください。 | NeXAS .bin 脚本 (Script.pac) 文本块中的 Shift-JIS 文本；只收集非 ASCII 段。导入时会重写文本块，但不会重新计算偏移量——长度不受限制，但请在游戏中测试。 | NeXAS .bin 스크립트(Script.pac)의 텍스트 블록에 있는 Shift-JIS 텍스트예요. 비ASCII 구간만 수집해요. 가져올 때 오프셋을 다시 계산하지 않고 텍스트 블록을 다시 써요. 길이는 자유지만 게임에서 확인해 보세요.
Entries whose original text changed are skipped. Latin text automatically gets a ` prefix so it is read as text, not a command. | 元のテキストが変更されたエントリはスキップされます。ラテン文字のテキストには、コマンドではなくテキストとして読み込まれるよう、自動的に ` プレフィックスが付きます。 | 原文已更改的条目会被跳过。拉丁文本会自动加上 ` 前缀，以便被当作文本而不是命令读取。 | 원문이 바뀐 항목은 건너뛰어요. 라틴 문자 텍스트에는 명령이 아니라 텍스트로 읽히도록 ` 접두사가 자동으로 붙어요.
Entries whose original text changed are skipped. Output .ks files are UTF-16 (BOM), which Kirikiri supports. | 元のテキストが変更されたエントリはスキップされます。出力される .ks ファイルは UTF-16（BOM）で、Kirikiri が対応しています。 | 原文已更改的条目会被跳过。输出的 .ks 文件为 UTF-16 (BOM)，Kirikiri 支持该编码。 | 원문이 바뀐 항목은 건너뛰어요. 출력되는 .ks 파일은 Kirikiri가 지원하는 UTF-16(BOM)이에요.
Entries whose original text changed since export are skipped. | エクスポート後に元のテキストが変更されたエントリはスキップされます。 | 导出后原文发生更改的条目会被跳过。 | 내보낸 뒤 원문이 바뀐 항목은 건너뛰어요.
VNTP JSON holds only names and dialogue (in order, duplicates kept). It is meant for translators and cannot be imported into the Translation Editor. | VNTP JSON には名前とセリフだけが（順序どおり、重複も含めて）入っています。翻訳者向けであり、翻訳エディターにはインポートできません。 | VNTP JSON 只包含名称和对话（按顺序，保留重复项）。它是为译者准备的，无法导入到翻译编辑器。 | VNTP JSON에는 이름과 대사만(순서대로, 중복 포함) 들어 있어요. 번역가용이며 번역 편집기로는 가져올 수 없어요.
Done: {0} texts replaced in {1} files ({2} entries skipped, original text changed) | 完了: {0} 個のテキストを {1} 個のファイルで置き換えました（{2} 件スキップ。元のテキストが変更されています） | 完成：在 {1} 个文件中替换了 {0} 段文本（跳过 {2} 条，原文已更改） | 완료: 파일 {1}개에서 텍스트 {0}개를 교체했어요({2}개 건너뜀, 원문이 바뀜)
{0} characters | {0} 文字 | {0} 个字符 | {0}자
Extension | 拡張子 | 扩展名 | 확장자
Entropy | エントロピー | 熵 | 엔트로피
Choices (^select) | 選択肢（^select） | 选项 (^select) | 선택지(^select)
Compression | 圧縮 | 压缩 | 압축
entries | 件 | 个条目 | 개 항목
Folder: | フォルダ: | 文件夹： | 폴더:
Files scanned: | スキャンしたファイル: | 已扫描文件： | 스캔한 파일:
Keys found: | 見つかったキー: | 找到的密钥： | 찾은 키:
Status: | 状態: | 状态： | 상태:
Type: | 種類: | 类型： | 유형:
Key: | キー: | 密钥： | 키:
Source: | ソース: | 来源： | 출처:
File: | ファイル: | 文件： | 파일:
Total entries: | 総項目数: | 条目总数： | 전체 항목 수:
Filled: | 入力済み: | 已填写： | 입력됨:
Empty: | 空: | 为空： | 비어 있음:
Same as original: | 原文と同じ: | 与原文相同： | 원문과 동일:
Control codes differ: | 制御コードが異なる: | 控制码不一致： | 제어 코드가 다름:
Lines longer than {0}: | {0} 文字を超える行: | 超过 {0} 个字符的行： | {0}자를 넘는 줄:
Inconsistent (same original, different translation): | 不整合（原文は同じで訳文が異なる）: | 不一致（原文相同但译文不同）： | 불일치(원문은 같은데 번역이 다름):
Duplicate keys (file+path): | 重複キー（ファイル+パス）: | 重复键（文件+路径）： | 중복 키(파일+경로):
Longest line: | 最長行: | 最长行： | 가장 긴 줄:
tilesets | タイルセット | 图块集 | 타일셋
faces | 顔グラフィック | 脸图 | 얼굴 그래픽
@default in the annotation is only the default shown by the RPG Maker editor. The values the game uses live in plugins.js (parameters) or in code defaults (e.g. parameters['x'] || 10); both can be edited in the Plugin Scanner. | 注釈内の @default は、RPGツクールのエディターに表示される既定値にすぎません。ゲームが実際に使う値は plugins.js（parameters）またはコード内の既定値（例: parameters['x'] || 10）にあり、どちらもプラグインスキャナーで編集できます。 | 注解中的 @default 只是 RPG Maker 编辑器中显示的默认值。游戏实际使用的值存放在 plugins.js（parameters）或代码默认值（例如 parameters['x'] || 10）中，二者都可以在插件扫描器中编辑。 | 주석의 @default는 RPG Maker 편집기에 표시되는 기본값일 뿐이에요. 게임이 실제로 쓰는 값은 plugins.js(parameters)나 코드 기본값(예: parameters['x'] || 10)에 있으며, 둘 다 플러그인 스캐너에서 편집할 수 있어요.
Shift-JIS (Japanese) | Shift-JIS（日本語） | Shift-JIS（日语） | Shift-JIS(일본어)
EUC-KR (Korean) | EUC-KR（韓国語） | EUC-KR（韩语） | EUC-KR(한국어)
48 bytes | 48 バイト | 48 字节 | 48바이트
36 bytes | 36 バイト | 36 字节 | 36바이트
Index | インデックス | 索引 | 인덱스
zlib (variant 4) | zlib（バリアント 4） | zlib（变体 4） | zlib(변형 4)
pf6 (plain) | pf6（無暗号） | pf6（未加密） | pf6(평문)
32 bytes (0x20) | 32 バイト (0x20) | 32 字节 (0x20) | 32바이트 (0x20)
30 bytes (0x1E) | 30 バイト (0x1E) | 30 字节 (0x1E) | 30바이트 (0x1E)
20 bytes (0x14) | 20 バイト (0x14) | 20 字节 (0x14) | 20바이트 (0x14)
16 bytes (0x10) | 16 バイト (0x10) | 16 字节 (0x10) | 16바이트 (0x10)
Normal | 標準 | 标准 | 보통
8 bytes | 8 バイト | 8 字节 | 8바이트
12 bytes | 12 バイト | 12 字节 | 12바이트
Healthy | 正常 | 正常 | 정상
Could not read the file: | ファイルを読み込めませんでした: | 无法读取文件： | 파일을 읽을 수 없어요:
Checked: | チェック済み: | 已检查： | 검사함:
Healthy: | 正常: | 正常： | 정상:
Warnings: | 警告: | 警告： | 경고:
Problems: | 問題: | 问题： | 문제:
Skipped (plain image/audio/video): | スキップ（通常の画像/オーディオ/動画）: | 已跳过（普通图片/音频/视频）： | 건너뜀(일반 이미지/오디오/비디오):
Total size: | 合計サイズ: | 总大小： | 총 크기:
Files: | ファイル: | 文件： | 파일:
, folders: | 、フォルダ: | ，文件夹： | , 폴더:
Top extensions: | 上位の拡張子: | 常见扩展名： | 상위 확장자:
files: | 個のファイル: | 个文件： | 개 파일:
Not yet checked: | 未チェック: | 尚未检查： | 아직 검사하지 않음:
files (next batch) | 個のファイル（次のバッチ） | 个文件（下一批） | 개 파일(다음 배치)
Size A | サイズ A | 大小 A | 크기 A
Size B | サイズ B | 大小 B | 크기 B
Added: | 追加: | 新增： | 추가됨:
Changed: | 変更: | 已更改： | 변경됨:
Removed: | 削除: | 已删除： | 삭제됨:
Same: | 同一: | 相同： | 동일:
Mode: | モード: | 模式： | 모드:
Lines A: | A の行数: | A 的行数： | A 줄 수:
Lines B: | B の行数: | B 的行数： | B 줄 수:
Added in B: | B で追加: | B 中新增： | B에서 추가됨:
Removed from A: | A から削除: | A 中已删除： | A에서 삭제됨:
Entries A/B: | 項目数 A/B: | 条目数 A/B： | 항목 수 A/B:
Filled A/B: | 入力済み A/B: | 已填写 A/B： | 입력됨 A/B:
New entries: | 新規項目: | 新条目： | 새 항목:
Removed entries: | 削除された項目: | 已删除的条目： | 삭제된 항목:
Original changed: | 原文が変更: | 原文已更改： | 원문이 변경됨:
Translation changed: | 訳文が変更: | 译文已更改： | 번역이 변경됨:
Created: | 作成日: | 创建时间： | 생성일:
"""
    const val T21 = """
Media Player and "Open with…" | Media Player と「アプリで開く」 | 媒体播放器与“打开方式” | 미디어 플레이어와 “다음으로 열기”
Media Player is on the Support Tools tab with three menus: Image Gallery, Audio Player and Video Player.\n• Audio and Video Player have four buttons at the top: Player, Queue, Folders and Playlists.\n• Folders: tap "Add Folder" and pick a folder; only the folder names are listed (turn on "This folder or subfolders too" to scan subfolders as well). Tap a folder to scan it; its content becomes the Queue. "Add Files" appends single files to the Queue.\n• Queue: a list with cover, title, artist, album and duration, 50 items per page (adjustable). Use the up/down arrows to reorder, and the three-dot button for Song info, Play next, Remove from queue, Add to Favorites, Add to playlist, Share… and Open in File Inspection.\n• Playlists: tap "Add Playlist" to create one. Playlists are shown in 3 columns; the three dots offer Rename and Delete. "Favorites" is always first and cannot be renamed or deleted. Tap a list to load it into the Queue.\n• Player: cover, title, Favorite, Info, add to playlist, repeat, shuffle, speed, A–B loop, sleep timer, Equalizer, and background play with notification controls. The Video Player adds Rotate Screen and Fullscreen buttons, double-tap to skip, and vertical swipes for brightness (left) and volume (right).\n• Image Gallery: Gallery, Folders and Collections buttons. The gallery can be a list or a grid (60 images per page), with a three-dot button for Add to Favorites, Add to collection, etc. Collections work like Playlists.\n• Folders, Playlists/Collections and Favorites are separate for Audio, Video and Images.\n• The Media Settings button holds the player settings plus export and import of the settings together with all lists.\n• In Game Assets, media viewers have a Media Player icon that opens the asset in Media Player.\n• The seek bar jumps straight to the tapped position.\n• Open with…: files from a file manager or the Share/Send to menu can be opened with FuruArc. Media opens in Media Player, text and archive formats in the matching feature, and unknown formats directly in File Inspection. It can be switched off in System settings.\n• Apply to Queue / Gallery: tap Folder, Favorites, Collection or Playlist. If the Queue (or Gallery) is empty it is loaded directly. Otherwise an "Apply to Queue" window offers Load (replace, with a confirmation warning) and Add (append to the current list). The folder three-dot menu has Load into Queue and Add to Queue. Emptying the Queue also empties the Player.\n• Video Player: the Subtitle icon (next to the playlist icon) uploads an SRT, VTT or ASS/SSA subtitle file (UTF-8); it lasts while the app is open.\n• The audio notification is media-style: cover, title, artist, a seek bar with times, and Favorite, Previous, Play/Pause, Next and Close buttons.\n• "Open with…" is temporary: opened media does not overwrite your Gallery, Player and Queue; they come back when you leave the Media Player screen.\n• Search: the magnifier icon left of the select-mode icon (Queue and Gallery) shows or hides the search field. Select mode asks for confirmation before deleting.\n• Folder / Favorites / Collection / Playlist: the three-dot menu has "View content" (a preview with search and pages) and "Empty". "Load into Queue/Gallery" goes straight to the overwrite warning.\n• Media Settings: "Arrange the list" (Drag mode or Up / down arrows), separate Remember last position switches for Audio and Video (off by default), and automatic subtitles & lyrics with a text size option.\n• The last played audio and video with their position are restored (paused) when the app opens again. Leaving and re-entering the video player also continues from the last position without starting to play.\n• Synced lyrics (.lrc) in the Audio Player and subtitles in the Video Player: found automatically by the same file name, can be chosen manually, shifted by ±0.5 s, and resized. Position bookmarks and picture-in-picture are available in the Player.\n• Video fullscreen: swiping from the screen edges (status / navigation area) does not change brightness or volume. | メディアプレーヤーはサポートツールタブにあり、画像ギャラリー、オーディオプレーヤー、ビデオプレーヤーの3つのメニューがあります。\n• オーディオ/ビデオプレーヤーの上部には、プレーヤー、キュー、フォルダー、プレイリストの4つのボタンがあります。\n• フォルダー：「フォルダーを追加」でフォルダーを選ぶと、フォルダー名だけが一覧表示されます（「このフォルダーとサブフォルダー」をオンにするとサブフォルダーもスキャン）。フォルダーをタップするとスキャンされ、その内容がキューになります。「ファイルを追加」は個別のファイルをキューに追加します。\n• キュー：カバー、タイトル、アーティスト、アルバム、長さを表示し、1ページ50件（変更可）。上下の矢印で順序を変更でき、3点ボタンから曲の情報、次に再生、キューから削除、お気に入りに追加、プレイリストに追加、共有…、ファイル検査で開くを選べます。\n• プレイリスト：「プレイリストを追加」で作成します。3列で表示され、3点ボタンから名前の変更と削除ができます。「お気に入り」は常に先頭にあり、名前の変更も削除もできません。リストをタップするとキューに読み込まれます。\n• プレーヤー：カバー、タイトル、お気に入り、情報、プレイリストに追加、リピート、シャッフル、速度、A–Bループ、スリープタイマー、イコライザー、通知から操作できるバックグラウンド再生。ビデオプレーヤーには画面回転と全画面ボタン、ダブルタップでスキップ、縦スワイプで明るさ（左）と音量（右）の調整があります。\n• 画像ギャラリー：ギャラリー、フォルダー、コレクションのボタン。リストまたはグリッド表示（1ページ60枚）で、3点ボタンからお気に入りに追加、コレクションに追加などができます。コレクションはプレイリストと同じ仕組みです。\n• フォルダー、プレイリスト/コレクション、お気に入りは、オーディオ、ビデオ、画像ごとに別々です。\n• 「メディア設定」ボタンにはプレーヤー設定と、設定とすべてのリストのエクスポート/インポートがあります。\n• ゲームアセットのメディアビューアにあるメディアプレーヤーのアイコンで、そのアセットをメディアプレーヤーで開けます。\n• シークバーはタップした位置へ直接ジャンプします。\n• アプリで開く：ファイルマネージャーや共有/送信メニューからのファイルを FuruArc で開けます。メディアはメディアプレーヤー、テキストとアーカイブは対応する機能、不明な形式はファイル検査で直接開きます。システム設定でオフにできます。\n• キュー／ギャラリーに適用: フォルダー、お気に入り、コレクション、プレイリストをタップします。キュー（ギャラリー）が空なら直接読み込まれ、内容がある場合は「キューに適用」ウィンドウで「読み込む」（置き換え、確認の警告あり）か「追加」を選べます。フォルダーの︙メニューには「キューに読み込む」「キューに追加」があります。キューを空にするとプレーヤーも空になります。\n• 動画プレーヤー: プレイリストアイコンの隣の字幕アイコンで SRT、VTT、ASS/SSA 字幕ファイル（UTF-8）をアップロードできます。アプリを開いている間のみ有効です。\n• 音声の通知はメディア形式です: カバー、タイトル、アーティスト、時間付きシークバー、お気に入り/前へ/再生・一時停止/次へ/閉じるボタン。\n• 「アプリで開く」は一時的です: 開いたメディアはギャラリー、プレーヤー、キューを上書きせず、メディアプレーヤー画面を離れると元に戻ります。\n• 検索: 選択モードアイコンの左にある虫眼鏡アイコンで検索欄の表示/非表示を切り替えます（キューとギャラリー）。選択モードの削除は確認が表示されます。\n• フォルダー/お気に入り/コレクション/プレイリスト: ︙メニューに「内容を見る」（検索とページ付きプレビュー）と「空にする」があります。「キュー/ギャラリーに読み込む」は上書きの警告を直接表示します。\n• メディア設定: 「リストの並べ替え」（ドラッグ／上下ボタン）、音声と動画で別々の「最後の位置を記憶」（初期はオフ）、字幕・歌詞の自動読み込みと文字サイズ。\n• 最後に再生した音声と動画は位置とともに、アプリを再び開いたときに一時停止状態で復元されます。動画プレーヤーを出て戻ったときも、再生を始めずに最後の位置から続きます。\n• 音声プレーヤーの同期歌詞（.lrc）と動画プレーヤーの字幕: 同名ファイルを自動検索し、手動選択、±0.5秒の調整、文字サイズ変更ができます。位置ブックマークとピクチャーインピクチャーも使えます。\n• 動画の全画面: 画面端（ステータス/ナビゲーション領域）からのスワイプでは明るさや音量は変わりません。 | 媒体播放器位于辅助工具标签页，包含三个菜单：图片库、音频播放器和视频播放器。\n• 音频和视频播放器顶部有四个按钮：播放器、队列、文件夹和播放列表。\n• 文件夹：点按“添加文件夹”并选择文件夹；只会列出文件夹名称（开启“此文件夹及子文件夹”可同时扫描子文件夹）。点按文件夹即可扫描，其内容将成为队列。“添加文件”可把单个文件追加到队列。\n• 队列：显示封面、标题、艺术家、专辑和时长，每页 50 项（可调整）。用上/下箭头调整顺序，三点按钮提供歌曲信息、下一首播放、从队列移除、添加到收藏、添加到播放列表、分享…和在文件检查中打开。\n• 播放列表：点按“添加播放列表”新建。列表以 3 列显示，三点按钮可重命名和删除。“收藏”始终排在第一位，不能重命名或删除。点按列表即可载入队列。\n• 播放器：封面、标题、收藏、信息、添加到播放列表、循环、随机、倍速、A–B 循环、睡眠定时、均衡器，以及带通知控制的后台播放。视频播放器还有旋转屏幕和全屏按钮、双击快进/快退，以及上下滑动调节亮度（左）和音量（右）。\n• 图片库：图库、文件夹和收藏集按钮。图库可用列表或网格显示（每页 60 张），三点按钮提供添加到收藏、添加到收藏集等。收藏集的用法与播放列表相同。\n• 文件夹、播放列表/收藏集和收藏对音频、视频和图片是分开的。\n• “媒体设置”按钮包含播放器设置，以及设置和所有列表的导出与导入。\n• 在游戏资源中，媒体查看器带有媒体播放器图标，可在媒体播放器中打开该资源。\n• 进度条会直接跳到点按的位置。\n• 打开方式：可从文件管理器或“分享/发送到”菜单用 FuruArc 打开文件。媒体在媒体播放器中打开，文本和压缩包格式在对应功能中打开，未知格式直接在文件检查中打开。可在系统设置中关闭。\n• 应用到队列/图片库：点击文件夹、收藏、收藏集或播放列表。若队列（图片库）为空则直接载入；否则会出现“应用到队列”窗口，可选择“载入”（替换，带确认警告）或“添加”（追加）。文件夹的三点菜单有“载入队列”和“添加到队列”。清空队列时播放器也会清空。\n• 视频播放器：播放列表图标旁的字幕图标可上传 SRT、VTT 或 ASS/SSA 字幕文件（UTF-8），仅在应用打开期间有效。\n• 音频通知为媒体样式：封面、标题、艺术家、带时间的进度条，以及收藏、上一首、播放/暂停、下一首、关闭按钮。\n• “打开方式”是临时的：打开的媒体不会覆盖您的图片库、播放器和队列，离开媒体播放器界面后会恢复。\n• 搜索：选择模式图标左侧的放大镜图标可显示/隐藏搜索框（队列和图片库）。选择模式下删除前会要求确认。\n• 文件夹/收藏/收藏集/播放列表：三点菜单提供“查看内容”（带搜索和分页的预览）和“清空”。“载入队列/图片库”会直接显示覆盖警告。\n• 媒体设置：“列表排序方式”（拖动模式或上下箭头）、音频和视频各自独立的“记住上次位置”（默认关闭），以及自动字幕和歌词及其字号。\n• 再次打开应用时，会以暂停状态恢复上次播放的音频和视频及其位置。离开再进入视频播放器也会从上次位置继续，且不会自动开始播放。\n• 音频播放器的同步歌词（.lrc）和视频播放器的字幕：按同名文件自动查找，可手动选择、调整 ±0.5 秒并更改字号。播放器中还提供位置书签和画中画。\n• 视频全屏：从屏幕边缘（状态栏/导航栏区域）滑动不会改变亮度或音量。 | 미디어 플레이어는 지원 도구 탭에 있으며 이미지 갤러리, 오디오 플레이어, 비디오 플레이어 세 메뉴가 있습니다.\n• 오디오/비디오 플레이어 상단에는 플레이어, 대기열, 폴더, 재생목록 네 개의 버튼이 있습니다.\n• 폴더: "폴더 추가"를 눌러 폴더를 선택하면 폴더 이름만 목록으로 표시됩니다("이 폴더와 하위 폴더"를 켜면 하위 폴더도 검색). 폴더를 누르면 검색되고 그 내용이 대기열이 됩니다. "파일 추가"는 개별 파일을 대기열에 추가합니다.\n• 대기열: 커버, 제목, 아티스트, 앨범, 길이를 표시하며 페이지당 50개(변경 가능)입니다. 위/아래 화살표로 순서를 바꾸고, 점 세 개 버튼으로 곡 정보, 다음에 재생, 대기열에서 삭제, 즐겨찾기에 추가, 재생목록에 추가, 공유…, 파일 검사에서 열기를 선택할 수 있습니다.\n• 재생목록: "재생목록 추가"로 만듭니다. 3열로 표시되며 점 세 개 버튼으로 이름 바꾸기와 삭제를 할 수 있습니다. "즐겨찾기"는 항상 맨 앞에 있으며 이름을 바꾸거나 삭제할 수 없습니다. 목록을 누르면 대기열로 불러옵니다.\n• 플레이어: 커버, 제목, 즐겨찾기, 정보, 재생목록에 추가, 반복, 셔플, 속도, A–B 반복, 수면 타이머, 이퀄라이저, 알림으로 제어하는 백그라운드 재생. 비디오 플레이어에는 화면 회전과 전체 화면 버튼, 두 번 탭하여 건너뛰기, 세로 스와이프로 밝기(왼쪽)와 볼륨(오른쪽) 조절이 있습니다.\n• 이미지 갤러리: 갤러리, 폴더, 컬렉션 버튼. 목록 또는 격자(페이지당 60장)로 볼 수 있고, 점 세 개 버튼으로 즐겨찾기에 추가, 컬렉션에 추가 등을 할 수 있습니다. 컬렉션은 재생목록과 같은 방식입니다.\n• 폴더, 재생목록/컬렉션, 즐겨찾기는 오디오, 비디오, 이미지별로 따로 관리됩니다.\n• "미디어 설정" 버튼에는 플레이어 설정과, 설정 및 모든 목록의 내보내기/가져오기가 있습니다.\n• 게임 에셋의 미디어 뷰어에 있는 미디어 플레이어 아이콘으로 해당 에셋을 미디어 플레이어에서 열 수 있습니다.\n• 탐색 막대가 누른 위치로 바로 이동합니다.\n• 다음으로 열기: 파일 관리자나 공유/보내기 메뉴의 파일을 FuruArc로 열 수 있습니다. 미디어는 미디어 플레이어, 텍스트와 아카이브는 해당 기능, 알 수 없는 형식은 파일 검사에서 바로 열립니다. 시스템 설정에서 끌 수 있습니다.\n• 대기열/갤러리에 적용: 폴더, 즐겨찾기, 컬렉션 또는 재생목록을 누르세요. 대기열(갤러리)이 비어 있으면 바로 불러오고, 내용이 있으면 “대기열에 적용” 창에서 “불러오기”(교체, 확인 경고 포함) 또는 “추가”를 선택합니다. 폴더의 점 세 개 메뉴에는 “대기열에 불러오기”와 “대기열에 추가”가 있습니다. 대기열을 비우면 플레이어도 비워집니다.\n• 동영상 플레이어: 재생목록 아이콘 옆 자막 아이콘으로 SRT, VTT, ASS/SSA 자막 파일(UTF-8)을 업로드합니다. 앱이 열려 있는 동안만 유지됩니다.\n• 오디오 알림은 미디어 스타일입니다: 커버, 제목, 아티스트, 시간이 있는 탐색 막대, 즐겨찾기/이전/재생·일시정지/다음/닫기 버튼.\n• “다음으로 열기”는 임시입니다: 열린 미디어는 갤러리, 플레이어, 대기열을 덮어쓰지 않으며 미디어 플레이어 화면을 나가면 복원됩니다.\n• 검색: 선택 모드 아이콘 왼쪽의 돋보기 아이콘으로 검색창을 표시/숨깁니다(대기열과 갤러리). 선택 모드에서 삭제하기 전에 확인 창이 표시됩니다.\n• 폴더/즐겨찾기/컬렉션/재생목록: 점 세 개 메뉴에 “내용 보기”(검색과 페이지가 있는 미리보기)와 “비우기”가 있습니다. “대기열/갤러리에 불러오기”는 덮어쓰기 경고를 바로 표시합니다.\n• 미디어 설정: “목록 정렬 방식”(드래그 모드 또는 위/아래 버튼), 오디오와 동영상 각각의 “마지막 위치 기억”(기본값 꺼짐), 자막·가사 자동 불러오기와 글자 크기.\n• 마지막으로 재생한 오디오와 동영상은 위치와 함께 앱을 다시 열 때 일시정지 상태로 복원됩니다. 동영상 플레이어를 나갔다 다시 들어와도 재생을 시작하지 않고 마지막 위치부터 이어집니다.\n• 오디오 플레이어의 동기화 가사(.lrc)와 동영상 플레이어의 자막: 같은 이름의 파일을 자동으로 찾고, 직접 선택하거나 ±0.5초 조절, 글자 크기 변경이 가능합니다. 위치 북마크와 PIP도 사용할 수 있습니다.\n• 동영상 전체 화면: 화면 가장자리(상태/탐색 영역)에서 스와이프해도 밝기나 볼륨이 바뀌지 않습니다.
"""
    const val T22 = """
Bukan bundle UnityFS | UnityFSバンドルではありません | 不是 UnityFS 包 | UnityFS 번들이 아니에요
Header Unity terpotong | Unityのヘッダーが途切れています | Unity 头部被截断 | Unity 헤더가 잘렸어요
Data Unity terpotong | Unityのデータが途切れています | Unity 数据被截断 | Unity 데이터가 잘렸어요
String Unity tidak berakhir | Unityの文字列が終了していません | Unity 字符串没有结束 | Unity 문자열이 끝나지 않아요
Blok Unity tidak lengkap | Unityのブロックが不完全です | Unity 块不完整 | Unity 블록이 불완전해요
Kompresi Unity {0} tidak dikenal | 不明なUnity圧縮方式 {0} | 未知的 Unity 压缩方式 {0} | 알 수 없는 Unity 압축 {0}
Versi UnityFS {0} belum didukung | UnityFSバージョン {0} は未対応です | 暂不支持 UnityFS 版本 {0} | UnityFS 버전 {0}은(는) 아직 지원되지 않아요
Ukuran info blok Unity tidak valid | Unityのブロック情報サイズが無効です | Unity 块信息大小无效 | Unity 블록 정보 크기가 올바르지 않아요
Kompresi info blok tidak dikenal | ブロック情報の圧縮方式が不明です | 未知的块信息压缩方式 | 알 수 없는 블록 정보 압축이에요
File terpotong: ukuran header {0} > {1} | ファイルが途切れています: ヘッダーサイズ {0} > {1} | 文件被截断：头部大小 {0} > {1} | 파일이 잘렸어요: 헤더 크기 {0} > {1}
Info blok Unity di luar file | Unityのブロック情報がファイル外です | Unity 块信息超出文件范围 | Unity 블록 정보가 파일 밖에 있어요
Info blok terpotong | ブロック情報が途切れています | 块信息被截断 | 블록 정보가 잘렸어요
Jumlah blok Unity tidak valid | Unityのブロック数が無効です | Unity 块数量无效 | Unity 블록 수가 올바르지 않아요
Blok Unity terlalu besar | Unityのブロックが大きすぎます | Unity 块过大 | Unity 블록이 너무 커요
Data blok Unity melewati akhir file | Unityのブロックデータがファイル末尾を超えています | Unity 块数据超出文件末尾 | Unity 블록 데이터가 파일 끝을 넘어요
Jumlah node Unity tidak valid | Unityのノード数が無効です | Unity 节点数量无效 | Unity 노드 수가 올바르지 않아요
Node Unity di luar data: {0} | Unityのノードがデータ外です: {0} | Unity 节点超出数据范围：{0} | Unity 노드가 데이터 밖에 있어요: {0}
Bundle Unity tidak berisi file | Unityバンドルにファイルがありません | Unity 包不包含文件 | Unity 번들에 파일이 없어요
Memori tidak cukup untuk membuka blok {0} MB | {0} MBのブロックを開くメモリが足りません | 内存不足，无法打开 {0} MB 的块 | {0} MB 블록을 열 메모리가 부족해요
Entri Unity tidak valid | Unityのエントリが無効です | Unity 条目无效 | Unity 항목이 올바르지 않아요
Data Unity berakhir lebih awal: {0} | Unityのデータが途中で終わっています: {0} | Unity 数据提前结束：{0} | Unity 데이터가 일찍 끝났어요: {0}
Tidak ada file untuk dikemas | パックするファイルがありません | 没有可打包的文件 | 묶을 파일이 없어요
Ukuran file berubah saat dikemas: {0} | パック中にファイルサイズが変わりました: {0} | 打包时文件大小发生变化：{0} | 묶는 중 파일 크기가 바뀌었어요: {0}
Data LZMA terlalu pendek | LZMAデータが短すぎます | LZMA 数据过短 | LZMA 데이터가 너무 짧아요
Properti LZMA tidak valid | LZMAのプロパティが無効です | LZMA 属性无效 | LZMA 속성이 올바르지 않아요
Data LZMA rusak | LZMAデータが壊れています | LZMA 数据已损坏 | LZMA 데이터가 손상됐어요
Data LZMA berakhir lebih awal | LZMAデータが途中で終わっています | LZMA 数据提前结束 | LZMA 데이터가 일찍 끝났어요
Data LZ4 terpotong | LZ4データが途切れています | LZ4 数据被截断 | LZ4 데이터가 잘렸어요
Data LZ4 rusak | LZ4データが壊れています | LZ4 数据已损坏 | LZ4 데이터가 손상됐어요
Ukuran hasil LZ4 tidak cocok | LZ4の出力サイズが一致しません | LZ4 输出大小不匹配 | LZ4 출력 크기가 맞지 않아요
File indeks tidak ditemukan — pilih lagi di opsi | インデックスファイルが見つかりません — オプションで選び直してください | 找不到索引文件 — 请在选项中重新选择 | 인덱스 파일을 찾을 수 없어요 — 옵션에서 다시 선택하세요
Bukan file indeks Eushully (SYS4INI.BIN / SYS5INI.BIN / .AAI) | Eushullyのインデックスファイルではありません (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 不是 Eushully 索引文件 (SYS4INI.BIN / SYS5INI.BIN / .AAI) | Eushully 인덱스 파일이 아니에요 (SYS4INI.BIN / SYS5INI.BIN / .AAI)
File indeks terpotong | インデックスファイルが途切れています | 索引文件被截断 | 인덱스 파일이 잘렸어요
Ukuran indeks tidak valid | インデックスサイズが無効です | 索引大小无效 | 인덱스 크기가 올바르지 않아요
Ini file indeks, bukan arsip. Buka file .ALF (mis. DATA1.ALF) dan pilih file indeks ini di opsi | これはインデックスファイルで、アーカイブではありません。.ALFファイル (例: DATA1.ALF) を開き、オプションでこのインデックスを選んでください | 这是索引文件，不是压缩包。请打开 .ALF 文件（如 DATA1.ALF），并在选项中选择此索引 | 이것은 인덱스 파일이며 아카이브가 아니에요. .ALF 파일(예: DATA1.ALF)을 열고 옵션에서 이 인덱스를 선택하세요
Pilih File indeks (SYS4INI.BIN / SYS5INI.BIN / .AAI) di opsi | オプションでインデックスファイル (SYS4INI.BIN / SYS5INI.BIN / .AAI) を選んでください | 请在选项中选择索引文件 (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 옵션에서 인덱스 파일(SYS4INI.BIN / SYS5INI.BIN / .AAI)을 선택하세요
Indeks kosong | インデックスが空です | 索引为空 | 인덱스가 비어 있어요
Indeks Eushully tidak valid | Eushullyのインデックスが無効です | Eushully 索引无效 | Eushully 인덱스가 올바르지 않아요
ALF "{0}" tidak ada di indeks. Isi "Nama ALF di indeks" dengan salah satu: {1} | ALF「{0}」はインデックスにありません。「インデックス内のALF名」に次のいずれかを入力してください: {1} | 索引中没有 ALF“{0}”。请在“索引中的 ALF 名称”中填入以下之一：{1} | ALF "{0}"이(가) 인덱스에 없어요. "인덱스의 ALF 이름"에 다음 중 하나를 입력하세요: {1}
Jumlah file indeks tidak valid | インデックスのファイル数が無効です | 索引中的文件数量无效 | 인덱스의 파일 수가 올바르지 않아요
Tidak ada file yang cocok untuk "{0}" — pastikan indeks dan ALF dari game yang sama | 「{0}」に一致するファイルがありません — インデックスとALFが同じゲームのものか確認してください | 没有与“{0}”匹配的文件 — 请确认索引和 ALF 来自同一个游戏 | "{0}"에 맞는 파일이 없어요 — 인덱스와 ALF가 같은 게임의 것인지 확인하세요
Pengemasan ALF belum didukung | ALFのパックは未対応です | 暂不支持打包 ALF | ALF 묶기는 아직 지원되지 않아요
Bukan arsip WARC ShiinaRio | ShiinaRioのWARCアーカイブではありません | 不是 ShiinaRio WARC 压缩包 | ShiinaRio WARC 아카이브가 아니에요
WARC versi 1.{0} memakai enkripsi per judul game — belum didukung (hanya WARC 1.1 yang terbaca) | WARCバージョン 1.{0} はタイトルごとの暗号化を使用しています — 未対応です (読めるのはWARC 1.1のみ) | WARC 版本 1.{0} 使用按游戏区分的加密 — 暂不支持（仅可读取 WARC 1.1） | WARC 버전 1.{0}은(는) 타이틀별 암호화를 써서 아직 지원되지 않아요 (WARC 1.1만 읽을 수 있어요)
Offset indeks WARC tidak valid | WARCのインデックスオフセットが無効です | WARC 索引偏移无效 | WARC 인덱스 오프셋이 올바르지 않아요
Indeks WARC terlalu besar | WARCのインデックスが大きすぎます | WARC 索引过大 | WARC 인덱스가 너무 커요
Indeks WARC tidak terbaca (mungkin skema enkripsi lain) | WARCのインデックスを読み取れません (別の暗号化方式の可能性) | 无法读取 WARC 索引（可能是其他加密方案） | WARC 인덱스를 읽을 수 없어요 (다른 암호화 방식일 수 있어요)
Entri terlalu besar | エントリが大きすぎます | 条目过大 | 항목이 너무 커요
Data YPK rusak: {0} | YPKデータが壊れています: {0} | YPK 数据已损坏：{0} | YPK 데이터가 손상됐어요: {0}
Pengemasan YH1 belum didukung: {0} | YH1圧縮は未対応です: {0} | 暂不支持 YH1 压缩：{0} | YH1 압축은 아직 지원되지 않아요: {0}
Pengemasan YLZ belum didukung: {0} | YLZ圧縮は未対応です: {0} | 暂不支持 YLZ 压缩：{0} | YLZ 압축은 아직 지원되지 않아요: {0}
Pengemasan WARC belum didukung | WARCのパックは未対応です | 暂不支持打包 WARC | WARC 묶기는 아직 지원되지 않아요
Index file (SYS4INI.BIN / SYS5INI.BIN / .AAI) | インデックスファイル (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 索引文件 (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 인덱스 파일 (SYS4INI.BIN / SYS5INI.BIN / .AAI)
Required: the ALF only holds data. Pick the index from the same game folder. | 必須: ALFにはデータしかありません。同じゲームフォルダのインデックスを選んでください。 | 必填：ALF 只包含数据。请选择同一游戏文件夹中的索引。 | 필수: ALF에는 데이터만 있어요. 같은 게임 폴더의 인덱스를 선택하세요.
ALF name in the index (optional) | インデックス内のALF名 (任意) | 索引中的 ALF 名称（可选） | 인덱스의 ALF 이름 (선택)
Only if the file was renamed (for example DATA1.ALF). | ファイル名を変更した場合のみ (例: DATA1.ALF)。 | 仅在文件被重命名时填写（例如 DATA1.ALF）。 | 파일 이름을 바꾼 경우에만 입력하세요 (예: DATA1.ALF).
Bundle format version | バンドル形式のバージョン | 包格式版本 | 번들 형식 버전
7 (Unity 2019.4 and newer) | 7 (Unity 2019.4 以降) | 7（Unity 2019.4 及更新） | 7 (Unity 2019.4 이상)
6 (Unity 5.6 - 2019.3) | 6 (Unity 5.6 - 2019.3) | 6 (Unity 5.6 - 2019.3) | 6 (Unity 5.6 - 2019.3)
Unity version string | Unityバージョン文字列 | Unity 版本字符串 | Unity 버전 문자열
Use the Unity version of the game (shown in the original bundle's header). | ゲームのUnityバージョンを入力してください (元のバンドルのヘッダーに表示されます)。 | 填写游戏的 Unity 版本（可在原包头部看到）。 | 게임의 Unity 버전을 입력하세요 (원본 번들 헤더에 표시돼요).
"""
    const val T23 = """
Pilih File indeks asli di opsi | オプションで元のインデックスファイルを選んでください | 请在选项中选择原始索引文件 | 옵션에서 원본 인덱스 파일을 선택하세요
Isi Nama ALF di indeks | インデックス内のALF名を入力してください | 请填写索引中的 ALF 名称 | 인덱스의 ALF 이름을 입력하세요
Indeks versi 3 (S3IN) belum didukung untuk dikemas | バージョン3のインデックス (S3IN) のパックは未対応です | 暂不支持打包版本 3 索引 (S3IN) | 버전 3 인덱스(S3IN) 묶기는 아직 지원되지 않아요
File berikut ada di indeks tetapi tidak ada di folder: {0} | 次のファイルはインデックスにありますがフォルダにありません: {0} | 以下文件在索引中但不在文件夹中：{0} | 다음 파일은 인덱스에 있지만 폴더에 없어요: {0}
ALF melebihi 4 GB | ALFが4 GBを超えています | ALF 超过 4 GB | ALF가 4 GB를 넘어요
Nama terlalu panjang (maks 15 byte): {0} | 名前が長すぎます (最大15バイト): {0} | 名称过长（最多 15 字节）：{0} | 이름이 너무 길어요 (최대 15바이트): {0}
Original index file (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 元のインデックスファイル (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 原始索引文件 (SYS4INI.BIN / SYS5INI.BIN / .AAI) | 원본 인덱스 파일 (SYS4INI.BIN / SYS5INI.BIN / .AAI)
Required: its header and the other ALF entries are kept; only this ALF's offsets and sizes change. | 必須: ヘッダーと他のALFのエントリは維持され、このALFのオフセットとサイズだけが変わります。 | 必填：其头部和其他 ALF 的条目会保留，只修改此 ALF 的偏移和大小。 | 필수: 헤더와 다른 ALF 항목은 유지되고 이 ALF의 오프셋과 크기만 바뀌어요.
ALF name in the index | インデックス内のALF名 | 索引中的 ALF 名称 | 인덱스의 ALF 이름
Which ALF of the index is rebuilt (for example DATA1.ALF). A new index is saved next to the ALF. | インデックスのどのALFを再構築するか (例: DATA1.ALF)。新しいインデックスはALFの隣に保存されます。 | 重建索引中的哪个 ALF（例如 DATA1.ALF）。新索引会保存在 ALF 旁边。 | 인덱스의 어느 ALF를 다시 만들지 (예: DATA1.ALF). 새 인덱스는 ALF 옆에 저장돼요.
"""
    const val T24 = """
Passed, but packing the unpacked files again gives a different archive (same files; can be normal if the format stores a time or random data) | 合格ですが、展開したファイルを再びパックすると別のアーカイブになります (ファイルは同じ。形式が時刻や乱数を保存する場合は正常なことがあります) | 已通过，但把解包的文件再次打包会得到不同的压缩包（文件相同；若格式保存时间或随机数据，则可能属正常） | 통과했지만 푼 파일을 다시 묶으면 다른 아카이브가 됩니다 (파일은 동일. 형식이 시간이나 난수를 저장하면 정상일 수 있어요)
The archive packed again has different data: {0} | 再パックしたアーカイブのデータが異なります: {0} | 再次打包的压缩包数据不同：{0} | 다시 묶은 아카이브의 데이터가 달라요: {0}
File SHA-256 does not match the expected one: {0} | ファイルのSHA-256が期待値と一致しません: {0} | 文件的 SHA-256 与预期不符：{0} | 파일의 SHA-256이 기대값과 달라요: {0}
No text was found in the sample script (the sample may not match the script format) | サンプルスクリプトにテキストが見つかりませんでした (サンプルが形式に合っていない可能性があります) | 示例脚本中未找到文本（示例可能与脚本格式不符） | 샘플 스크립트에서 텍스트를 찾지 못했어요 (샘플이 스크립트 형식과 맞지 않을 수 있어요)
Importing unchanged texts changed the script | テキストを変更せずに取り込んだのにスクリプトが変わりました | 在不修改文本的情况下导入，脚本却发生了变化 | 텍스트를 바꾸지 않고 가져왔는데 스크립트가 바뀌었어요
Importing changed texts did not work: {0} | 変更したテキストの取り込みに失敗しました: {0} | 导入修改后的文本失败：{0} | 변경한 텍스트 가져오기가 실패했어요: {0}
 · also accepted by: {0} |  · 他に受け入れる形式: {0} |  · 也被以下格式接受：{0} |  · 다음 형식도 인식해요: {0}
"""
    val ALL = listOf(T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, T17, T18, T19, T20, T21, T22, T23, T24)
}
