package com.furuarc.i18n

import com.furuarc.settings.Lang

/** Text of the Image Editor. */
class ImageEditStr(private val lang: Lang) {
    private fun p(idT: String, en: String, ja: String, zh: String, ko: String) = when (lang) { Lang.ID -> idT; Lang.JA -> ja; Lang.ZH -> zh; Lang.KO -> ko; else -> en }

    val menuTitle get() = p("Editor Gambar", "Image Editor", "画像エディタ", "图片编辑器", "이미지 편집기")
    val menuSub get() = p("Editor berlapis: kuas, teks, bentuk, crop, lalu ekspor", "Layered editor: brush, text, shapes, crop, then export", "レイヤー対応エディタ：ブラシ、文字、図形、切り抜き、書き出し", "分层编辑器：画笔、文字、形状、裁剪、导出", "레이어 편집기: 브러시, 글자, 도형, 자르기, 내보내기")

    // ---- home ----
    val newProject get() = p("Proyek Baru", "New Project", "新規プロジェクト", "新建项目", "새 프로젝트")
    val openImage get() = p("Buka Gambar", "Open Image", "画像を開く", "打开图片", "이미지 열기")
    val blankCanvas get() = p("Kanvas Kosong", "Blank Canvas", "空のキャンバス", "空白画布", "빈 캔버스")
    val quick get() = p("Mulai cepat", "Quick start", "クイックスタート", "快速开始", "빠른 시작")
    val recent get() = p("Proyek Terakhir", "Recent Projects", "最近のプロジェクト", "最近的项目", "최근 프로젝트")
    val noRecent get() = p("Belum ada proyek", "No projects yet", "プロジェクトはまだありません", "还没有项目", "프로젝트가 없습니다")
    val noRecentSub get() = p("Buka gambar atau buat kanvas kosong untuk mulai mengedit. Proyek tersimpan otomatis beserta lapisannya.", "Open an image or create a blank canvas to start editing. Projects are saved automatically together with their layers.", "画像を開くか空のキャンバスを作って編集を始めます。プロジェクトはレイヤーごと自動保存されます。", "打开图片或创建空白画布即可开始编辑。项目会连同图层自动保存。", "이미지를 열거나 빈 캔버스를 만들어 편집을 시작하세요. 프로젝트는 레이어와 함께 자동 저장됩니다.")
    val newTitle get() = p("Proyek baru", "New project", "新規プロジェクト", "新建项目", "새 프로젝트")
    val projName get() = p("Nama proyek", "Project name", "プロジェクト名", "项目名称", "프로젝트 이름")
    val canvasSize get() = p("Ukuran kanvas (px)", "Canvas size (px)", "キャンバスサイズ (px)", "画布大小 (px)", "캔버스 크기 (px)")
    val width get() = p("Lebar", "Width", "幅", "宽", "너비")
    val height get() = p("Tinggi", "Height", "高さ", "高", "높이")
    val bgColor get() = p("Warna latar", "Background colour", "背景色", "背景颜色", "배경색")
    val transparent get() = p("Transparan", "Transparent", "透明", "透明", "투명")
    val create get() = p("Buat", "Create", "作成", "创建", "만들기")
    val cancel get() = p("Batal", "Cancel", "キャンセル", "取消", "취소")
    val ok get() = p("OK", "OK", "OK", "确定", "확인")
    val rename get() = p("Ganti nama", "Rename", "名前を変更", "重命名", "이름 변경")
    val duplicate get() = p("Duplikat", "Duplicate", "複製", "复制", "복제")
    val delete get() = p("Hapus", "Delete", "削除", "删除", "삭제")
    val deleteAsk get() = p("Hapus proyek ini beserta semua lapisannya?", "Delete this project and all its layers?", "このプロジェクトとすべてのレイヤーを削除しますか？", "删除此项目及其所有图层？", "이 프로젝트와 모든 레이어를 삭제할까요?")
    val copySuffix get() = p(" (salinan)", " (copy)", "（コピー）", "（副本）", " (복사본)")
    val loadFail get() = p("Gambar atau proyek tidak bisa dibuka", "The image or project could not be opened", "画像またはプロジェクトを開けませんでした", "无法打开图片或项目", "이미지나 프로젝트를 열 수 없습니다")
    val scaledNote get() = p("Gambar besar diperkecil saat dibuka (sisi terpanjang maksimal 3072 px).", "Large images are reduced when opened (longest side at most 3072 px).", "大きな画像は開くときに縮小されます（長辺は最大 3072 px）。", "大图打开时会被缩小（最长边最多 3072 px）。", "큰 이미지는 열 때 축소됩니다(긴 변 최대 3072 px).")

    // ---- editor chrome ----
    val untitled get() = p("Tanpa nama", "Untitled", "無題", "未命名", "이름 없음")
    val undo get() = p("Urungkan", "Undo", "元に戻す", "撤销", "실행 취소")
    val redo get() = p("Ulangi", "Redo", "やり直す", "重做", "다시 실행")
    val tSelect get() = p("Pilih", "Select", "選択", "选择", "선택")
    val tDraw get() = p("Gambar", "Draw", "描画", "绘图", "그리기")
    val tText get() = p("Teks", "Text", "文字", "文字", "글자")
    val tShape get() = p("Bentuk", "Shapes", "図形", "形状", "도형")
    val tCrop get() = p("Crop", "Crop", "切り抜き", "裁剪", "자르기")
    val tLayers get() = p("Lapisan", "Layers", "レイヤー", "图层", "레이어")
    val tMore get() = p("Lainnya", "More", "その他", "更多", "더보기")
    val saved get() = p("Proyek tersimpan", "Project saved", "保存しました", "项目已保存", "프로젝트 저장됨")
    val lockedLayer get() = p("Lapisan terkunci", "The layer is locked", "レイヤーはロックされています", "图层已锁定", "레이어가 잠겨 있습니다")
    val noMemory get() = p("Memori tidak cukup untuk langkah ini", "Not enough memory for this step", "メモリが足りません", "内存不足，无法执行此步骤", "이 작업을 위한 메모리가 부족합니다")
    val exitSave get() = p("Proyek disimpan otomatis.", "The project is saved automatically.", "プロジェクトは自動保存されます。", "项目会自动保存。", "프로젝트는 자동 저장됩니다.")

    // ---- brush ----
    val brushNames get() = listOf(
        p("Kuas", "Brush", "ブラシ", "画笔", "브러시"), p("Pensil", "Pencil", "鉛筆", "铅笔", "연필"), p("Marker", "Marker", "マーカー", "马克笔", "마커"),
        p("Stabilo", "Highlighter", "蛍光ペン", "荧光笔", "형광펜"), p("Kuas lembut", "Soft brush", "ソフトブラシ", "柔边画笔", "부드러운 브러시"),
        p("Kuas keras", "Hard brush", "ハードブラシ", "硬边画笔", "단단한 브러시"), p("Airbrush", "Airbrush", "エアブラシ", "喷枪", "에어브러시"), p("Penghapus", "Eraser", "消しゴム", "橡皮擦", "지우개"),
    )
    val size get() = p("Ukuran", "Size", "サイズ", "大小", "크기")
    val opacity get() = p("Opasitas", "Opacity", "不透明度", "不透明度", "불투명도")
    val flow get() = p("Aliran", "Flow", "流量", "流量", "흐름")
    val hardness get() = p("Kekerasan", "Hardness", "硬さ", "硬度", "경도")
    val smooth get() = p("Penghalus", "Smoothing", "スムージング", "平滑", "스무딩")
    val advanced get() = p("Lanjutan", "Advanced", "詳細", "高级", "고급")
    val color get() = p("Warna", "Colour", "色", "颜色", "색상")
    val pipet get() = p("Pipet", "Eyedropper", "スポイト", "取色器", "스포이트")
    val customColor get() = p("Pilih warna", "Pick a colour", "色を選択", "选择颜色", "색상 선택")
    val hue get() = p("Rona", "Hue", "色相", "色相", "색조")
    val sat get() = p("Saturasi", "Saturation", "彩度", "饱和度", "채도")
    val value get() = p("Kecerahan", "Brightness", "明度", "明度", "명도")
    val hex get() = p("Kode hex", "Hex code", "16進コード", "十六进制", "16진 코드")

    // ---- select ----
    val alignL get() = p("Kiri", "Left", "左", "左", "왼쪽")
    val alignCH get() = p("Tengah ↔", "Centre ↔", "中央 ↔", "水平居中", "가운데 ↔")
    val alignR get() = p("Kanan", "Right", "右", "右", "오른쪽")
    val alignT get() = p("Atas", "Top", "上", "上", "위")
    val alignCV get() = p("Tengah ↕", "Centre ↕", "中央 ↕", "垂直居中", "가운데 ↕")
    val alignB get() = p("Bawah", "Bottom", "下", "下", "아래")
    val rotation get() = p("Rotasi", "Rotation", "回転", "旋转", "회전")
    val scale get() = p("Skala", "Scale", "拡大率", "缩放", "배율")
    val up get() = p("Naik", "Up", "上へ", "上移", "위로")
    val down get() = p("Turun", "Down", "下へ", "下移", "아래로")
    val lock get() = p("Kunci", "Lock", "ロック", "锁定", "잠금")
    val unlock get() = p("Buka kunci", "Unlock", "ロック解除", "解锁", "잠금 해제")
    val selectHint get() = p("Ketuk objek untuk memilih, seret untuk memindah, seret titik di sudut untuk skala dan rotasi. Dua jari: zoom dan geser kanvas.", "Tap an object to select it, drag to move, drag the corner dot to scale and rotate. Two fingers: zoom and pan the canvas.", "オブジェクトをタップして選択、ドラッグで移動、角の点をドラッグで拡大・回転。二本指でキャンバスをズーム・移動します。", "点按对象选择，拖动移动，拖动角上的圆点缩放和旋转。双指缩放和平移画布。", "객체를 탭해 선택하고 드래그해 이동하며, 모서리 점을 드래그해 크기·회전을 바꿉니다. 두 손가락으로 캔버스를 확대·이동합니다.")
    val noLayer get() = p("Belum ada lapisan. Buka panel Lapisan lalu tambah lapisan.", "No layers yet. Open the Layers panel and add one.", "レイヤーがありません。レイヤーパネルから追加してください。", "还没有图层。打开图层面板添加。", "레이어가 없습니다. 레이어 패널에서 추가하세요.")

    // ---- text ----
    val addText get() = p("Tambah teks", "Add text", "文字を追加", "添加文字", "글자 추가")
    val editText get() = p("Ubah teks", "Edit text", "文字を編集", "编辑文字", "글자 편집")
    val textContent get() = p("Isi teks", "Text", "テキスト", "文字内容", "내용")
    val fontNames get() = listOf("Sans", "Serif", "Mono", p("Tulisan tangan", "Cursive", "筆記体", "手写体", "필기체"), "Casual")
    val fontFile get() = p("Font dari file", "Font from file", "ファイルのフォント", "来自文件的字体", "파일에서 글꼴")
    val fontFileOn get() = p("Font file dipakai", "File font in use", "ファイルのフォントを使用中", "正在使用文件字体", "파일 글꼴 사용 중")
    val fontReset get() = p("Pakai font bawaan", "Use a built-in font", "内蔵フォントを使う", "使用内置字体", "기본 글꼴 사용")
    val bold get() = p("Tebal", "Bold", "太字", "粗体", "굵게")
    val italic get() = p("Miring", "Italic", "斜体", "斜体", "기울임")
    val underline get() = p("Garis bawah", "Underline", "下線", "下划线", "밑줄")
    val alignment get() = p("Rata", "Align", "配置", "对齐", "정렬")
    val alignNames get() = listOf(p("Kiri", "Left", "左", "左", "왼쪽"), p("Tengah", "Centre", "中央", "居中", "가운데"), p("Kanan", "Right", "右", "右", "오른쪽"))
    val lineSpacing get() = p("Jarak baris", "Line spacing", "行間", "行距", "줄 간격")
    val letterSpacing get() = p("Jarak huruf", "Letter spacing", "文字間隔", "字距", "자간")
    val outline get() = p("Garis tepi", "Outline", "縁取り", "描边", "윤곽선")
    val outlineW get() = p("Tebal garis tepi", "Outline width", "縁取りの太さ", "描边宽度", "윤곽선 두께")
    val shadow get() = p("Bayangan", "Shadow", "影", "阴影", "그림자")
    val textBg get() = p("Latar teks", "Text background", "文字の背景", "文字背景", "글자 배경")
    val boxWidth get() = p("Lebar kotak teks", "Text box width", "テキストボックスの幅", "文字框宽度", "글상자 너비")

    // ---- shapes ----
    val shapeNames get() = listOf(
        p("Persegi", "Rectangle", "四角形", "矩形", "사각형"), p("Persegi tumpul", "Rounded", "角丸", "圆角矩形", "둥근 사각형"), p("Elips", "Ellipse", "楕円", "椭圆", "타원"),
        p("Garis", "Line", "線", "直线", "선"), p("Panah", "Arrow", "矢印", "箭头", "화살표"), p("Poligon", "Polygon", "多角形", "多边形", "다각형"),
    )
    val fill get() = p("Isi", "Fill", "塗り", "填充", "채우기")
    val stroke get() = p("Garis", "Stroke", "線", "描边", "선")
    val strokeW get() = p("Tebal garis", "Stroke width", "線の太さ", "描边宽度", "선 두께")
    val radius get() = p("Sudut tumpul", "Corner radius", "角の丸み", "圆角半径", "모서리 반경")
    val sides get() = p("Jumlah sisi", "Sides", "辺の数", "边数", "변 수")
    val newShape get() = p("Tambah bentuk", "Add a shape", "図形を追加", "添加形状", "도형 추가")

    // ---- crop ----
    val cropFree get() = p("Bebas", "Free", "自由", "自由", "자유")
    val cropOriginal get() = p("Asli", "Original", "元の比率", "原始", "원본")
    val reset get() = p("Atur ulang", "Reset", "リセット", "重置", "초기화")
    val apply get() = p("Terapkan", "Apply", "適用", "应用", "적용")
    val cropHint get() = p("Seret sudut atau sisi kotak untuk memotong kanvas. Isi lapisan tidak diubah, hanya kanvasnya.", "Drag a corner or edge of the box to crop the canvas. The layers are not changed, only the canvas.", "枠の角や辺をドラッグしてキャンバスを切り抜きます。レイヤーの内容は変わりません。", "拖动框的角或边来裁剪画布。图层内容不会改变，只改变画布。", "상자의 모서리나 변을 드래그해 캔버스를 자릅니다. 레이어 내용은 바뀌지 않고 캔버스만 바뀝니다.")

    // ---- layers ----
    val layersTitle get() = p("Lapisan", "Layers", "レイヤー", "图层", "레이어")
    val addImage get() = p("Gambar dari galeri", "Image from gallery", "ギャラリーの画像", "相册图片", "갤러리 이미지")
    val addDraw get() = p("Lapisan gambar", "Drawing layer", "描画レイヤー", "绘图图层", "그리기 레이어")
    val addTextL get() = p("Lapisan teks", "Text layer", "文字レイヤー", "文字图层", "글자 레이어")
    val addShapeL get() = p("Lapisan bentuk", "Shape layer", "図形レイヤー", "形状图层", "도형 레이어")
    val addFillL get() = p("Lapisan warna", "Fill layer", "塗りつぶしレイヤー", "填充图层", "채우기 레이어")
    val layerImage get() = p("Gambar", "Image", "画像", "图片", "이미지")
    val layerDraw get() = p("Gambar lukis", "Drawing", "描画", "绘图", "그리기")
    val layerText get() = p("Teks", "Text", "文字", "文字", "글자")
    val layerShape get() = p("Bentuk", "Shape", "図形", "形状", "도형")
    val layerFill get() = p("Warna", "Fill", "塗り", "填充", "채우기")
    val layerMerged get() = p("Gabungan", "Merged", "結合", "合并", "병합됨")
    val blendNames get() = listOf(p("Normal", "Normal", "通常", "正常", "표준"), "Multiply", "Screen", "Overlay", "Darken", "Lighten")
    val blend get() = p("Mode campur", "Blend mode", "描画モード", "混合模式", "혼합 모드")
    val mergeDown get() = p("Gabung ke bawah", "Merge down", "下のレイヤーと結合", "向下合并", "아래로 병합")
    val flatten get() = p("Ratakan gambar", "Flatten image", "画像を統合", "合并图像", "이미지 병합")
    val flattenAsk get() = p("Semua lapisan yang terlihat digabung menjadi satu lapisan gambar. Bisa diurungkan.", "All visible layers are merged into one image layer. This can be undone.", "表示中のすべてのレイヤーを一つの画像レイヤーに統合します。元に戻せます。", "所有可见图层将合并为一个图像图层。可以撤销。", "보이는 모든 레이어를 하나의 이미지 레이어로 합칩니다. 실행 취소할 수 있습니다.")
    val show get() = p("Tampilkan", "Show", "表示", "显示", "표시")
    val hide get() = p("Sembunyikan", "Hide", "非表示", "隐藏", "숨기기")
    val layerName get() = p("Nama lapisan", "Layer name", "レイヤー名", "图层名称", "레이어 이름")
    val layerHint get() = p("Ketuk lapisan untuk memilih. Mata: tampil/sembunyi. Gembok: kunci. ▲▼ memindah urutan.", "Tap a layer to select it. Eye: show/hide. Lock: lock. ▲▼ change the order.", "レイヤーをタップして選択。目：表示/非表示、鍵：ロック、▲▼：順序変更。", "点按图层选择。眼睛：显示/隐藏；锁：锁定；▲▼：调整顺序。", "레이어를 탭해 선택합니다. 눈: 표시/숨김, 자물쇠: 잠금, ▲▼: 순서 변경.")
    val close get() = p("Tutup", "Close", "閉じる", "关闭", "닫기")

    // ---- more ----
    val rotL get() = p("Putar kiri", "Rotate left", "左に回転", "向左旋转", "왼쪽 회전")
    val rotR get() = p("Putar kanan", "Rotate right", "右に回転", "向右旋转", "오른쪽 회전")
    val flipH get() = p("Balik horizontal", "Flip horizontal", "左右反転", "水平翻转", "좌우 반전")
    val flipV get() = p("Balik vertikal", "Flip vertical", "上下反転", "垂直翻转", "상하 반전")
    val straighten get() = p("Luruskan", "Straighten", "傾き補正", "矫正", "수평 맞추기")
    val canvasSizeBtn get() = p("Ukuran kanvas", "Canvas size", "キャンバスサイズ", "画布大小", "캔버스 크기")
    val scaleAll get() = p("Skalakan semuanya", "Scale everything", "全体を拡大縮小", "整体缩放", "전체 크기 조절")
    val expandCanvas get() = p("Ubah kanvas (isi tidak diskala)", "Change canvas (content not scaled)", "キャンバスを変更（内容は拡縮しない）", "更改画布（内容不缩放）", "캔버스 변경(내용 크기 유지)")
    val grid get() = p("Grid rule of thirds", "Rule-of-thirds grid", "三分割グリッド", "三分网格", "삼분할 격자")
    val snap get() = p("Snap ke tengah dan tepi", "Snap to centre and edges", "中央と端にスナップ", "吸附到中心和边缘", "중앙·가장자리에 맞춤")
    val history get() = p("Riwayat", "History", "履歴", "历史记录", "기록")
    val export get() = p("Ekspor", "Export", "書き出し", "导出", "내보내기")
    val save get() = p("Simpan proyek", "Save project", "プロジェクトを保存", "保存项目", "프로젝트 저장")
    val canvasBg get() = p("Latar kanvas", "Canvas background", "キャンバスの背景", "画布背景", "캔버스 배경")
    val moreHint get() = p("Perubahan di sini berlaku untuk seluruh kanvas dan bisa diurungkan.", "Changes here apply to the whole canvas and can be undone.", "ここでの変更はキャンバス全体に適用され、元に戻せます。", "这里的更改作用于整个画布，可以撤销。", "여기서의 변경은 캔버스 전체에 적용되며 실행 취소할 수 있습니다.")
    val angle get() = p("Sudut", "Angle", "角度", "角度", "각도")
    val keepRatio get() = p("Kunci rasio", "Keep ratio", "比率を固定", "锁定比例", "비율 고정")

    // ---- export ----
    val exportTitle get() = p("Ekspor gambar", "Export image", "画像を書き出す", "导出图片", "이미지 내보내기")
    val format get() = p("Format", "Format", "形式", "格式", "형식")
    val quality get() = p("Kualitas", "Quality", "品質", "质量", "품질")
    val exportSize get() = p("Lebar hasil (px)", "Output width (px)", "出力幅 (px)", "输出宽度 (px)", "출력 너비 (px)")
    val originalSize get() = p("Ukuran asli", "Original size", "元のサイズ", "原始大小", "원본 크기")
    val transparentBg get() = p("Latar transparan (PNG, WebP)", "Transparent background (PNG, WebP)", "背景を透明にする（PNG、WebP）", "透明背景（PNG、WebP）", "투명 배경 (PNG, WebP)")
    val fileName get() = p("Nama file", "File name", "ファイル名", "文件名", "파일 이름")
    val exportBtn get() = p("Simpan sebagai…", "Save as…", "名前を付けて保存…", "另存为…", "다른 이름으로 저장…")
    val exported get() = p("Gambar tersimpan", "Image saved", "画像を保存しました", "图片已保存", "이미지를 저장했습니다")
    val exportFail get() = p("Gagal menyimpan gambar", "Could not save the image", "画像を保存できませんでした", "无法保存图片", "이미지를 저장할 수 없습니다")
    val historyEmpty get() = p("Belum ada langkah.", "No steps yet.", "まだ手順がありません。", "还没有步骤。", "아직 단계가 없습니다.")
    val stepNames get() = mapOf(
        "open" to p("Buka", "Open", "開く", "打开", "열기"), "new" to p("Proyek baru", "New project", "新規", "新建项目", "새 프로젝트"),
        "brush" to p("Kuas", "Brush", "ブラシ", "画笔", "브러시"), "erase" to p("Penghapus", "Eraser", "消しゴム", "橡皮擦", "지우개"),
        "move" to p("Pindah / ubah", "Move / transform", "移動・変形", "移动/变换", "이동/변형"), "edit" to p("Ubah", "Edit", "編集", "编辑", "편집"),
        "add" to p("Tambah lapisan", "Add layer", "レイヤーを追加", "添加图层", "레이어 추가"), "del" to p("Hapus lapisan", "Delete layer", "レイヤーを削除", "删除图层", "레이어 삭제"),
        "order" to p("Urutan lapisan", "Layer order", "レイヤーの順序", "图层顺序", "레이어 순서"), "merge" to p("Gabung", "Merge", "結合", "合并", "병합"),
        "crop" to p("Crop", "Crop", "切り抜き", "裁剪", "자르기"), "rotate" to p("Putar", "Rotate", "回転", "旋转", "회전"), "flip" to p("Balik", "Flip", "反転", "翻转", "반전"),
        "canvas" to p("Kanvas", "Canvas", "キャンバス", "画布", "캔버스"),
    )
}

fun imageEditStr(lang: Lang) = ImageEditStr(lang)
