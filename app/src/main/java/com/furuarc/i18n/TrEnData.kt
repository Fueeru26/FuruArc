package com.furuarc.i18n

/**
 * English text for messages that are written only in Indonesian in the code (engine and format errors, etc.).
 * Shown when the app language is English. Line format: Indonesian | English
 * Keys are matched exactly, or as a template when they contain {0}.
 */
internal object TrEnData {
    const val E0 = """
Scene.pck ini memakai kunci exe, tetapi {0} tidak memuat kunci. Isi opsi Kunci (hex 16 byte) — cari dengan Pencari Kunci. | This Scene.pck uses the exe key, but {0} does not contain the key. Fill in the Key option (16-byte hex) — find it with Key Finder.
File {0} tidak ditemukan di folder. Bongkar Scene.pck asli dulu, lalu edit isi folder hasilnya. | File {0} was not found in the folder. Unpack the original Scene.pck first, then edit the contents of the resulting folder.
Ada karakter yang tidak ada di {0} (aktifkan Ganti karakter rusak untuk menggantinya dengan ?) | Some characters do not exist in {0} (enable Replace broken characters to replace them with ?)
Bukan teks {0} yang valid (pilih penyandian lain atau aktifkan Ganti karakter rusak) | Not valid {0} text (choose another encoding or enable Replace broken characters)
XPCM mode {0} ({1}) belum didukung. Yang didukung: Ogg, PCM mentah, ADPCM | XPCM mode {0} ({1}) is not supported yet. Supported: Ogg, raw PCM, ADPCM
Gambar basis tidak ditemukan: {0} (pilih folder yang memuat basisnya) | Base image not found: {0} (choose the folder that holds the base)
Checksum tidak cocok untuk {0} — password kemungkinan salah | Checksum does not match for {0} — the password is probably wrong
teks "{0}…" memuat huruf yang tidak ada di encoding {1} | text "{0}…" contains characters that do not exist in the {1} encoding
Encoder video menolak pengaturan ini ({0}x{1}, {2} kbps): {3} | The video encoder rejected these settings ({0}x{1}, {2} kbps): {3}
Parameter Vorbis tidak didukung (rate {0}, {1} kbps) | Unsupported Vorbis parameters (rate {0}, {1} kbps)
Nama terlalu panjang untuk v1 (maks 15 byte): {0} | Name too long for v1 (max 15 bytes): {0}
Parameter MP3 tidak didukung (rate {0}, {1} kbps) | Unsupported MP3 parameters (rate {0}, {1} kbps)
Perangkat ini tidak punya encoder video {0} | This device has no {0} video encoder
Nama terlalu panjang (maks. 63 byte): {0} | Name too long (max 63 bytes): {0}
Dekompresi ERISA berakhir lebih awal: {0} | ERISA decompression ended early: {0}
JSON tidak valid ({0}) di baris {1}, kolom {2} | Invalid JSON ({0}) at line {1}, column {2}
Perangkat tidak bisa mendekode video {0} | The device cannot decode {0} video
Nama terlalu panjang (maks 95 byte): {0} | Name too long (max 95 bytes): {0}
Nama terlalu panjang (maks 23 byte): {0} | Name too long (max 23 bytes): {0}
Nama terlalu panjang (maks 55 byte): {0} | Name too long (max 55 bytes): {0}
Nama terlalu panjang (maks 12 byte): {0} | Name too long (max 12 bytes): {0}
Ukuran hasil dekompresi tidak cocok: {0} | Decompressed size does not match: {0}
Format .ast tidak dikenali (posisi {0}) | Unrecognised .ast format (position {0})
Varian kompresi {0} ({1}) belum didukung: {2} | Compression variant {0} ({1}) is not supported yet: {2}
Nama terlalu panjang (maks {0} byte): {1} | Name too long (max {0} bytes): {1}
Skema enkripsi 0x{0} belum didukung ({1}) | Encryption scheme 0x{0} is not supported yet ({1})
Format audio XPCM bukan PCM (tag {0}) | XPCM audio format is not PCM (tag {0})
Perangkat ini tidak punya encoder {0} | This device has no {0} encoder
Ukuran file berubah saat dikemas: {0} | File size changed while packing: {0}
Kompresi NSA tipe {0} belum didukung | NSA compression type {0} is not supported yet
Metode kompresi {0} belum didukung: {1} | Compression method {0} is not supported yet: {1}
Encoder menolak pengaturan ini: {0} | The encoder rejected these settings: {0}
Opcode pickle tidak didukung: 0x{0} | Unsupported pickle opcode: 0x{0}
Scene "{0}.dat" tidak ada di folder | Scene "{0}.dat" is not in the folder
ZIP terenkripsi belum didukung: {0} | Encrypted ZIP is not supported yet: {0}
Perangkat tidak bisa mendekode {0} | The device cannot decode {0}
Flag index XP3 tidak didukung: {0} | XP3 index flag not supported: {0}
Gagal memulai decoder video {0}: {1} | Failed to start the {0} video decoder: {1}
Seksi {0} tidak ditemukan di ARCC | Section {0} not found in ARCC
Gagal menyandikan MP3 (kode {0}) | MP3 encoding failed (code {0})
Format file tidak didukung ({0}) | File format not supported ({0})
Mode XPCM {0} ({1}) belum didukung | XPCM mode {0} ({1}) is not supported yet
penanda string {0} tidak valid | invalid string marker {0}
Gagal membuat file Output: {0} | Failed to create the output file: {0}
Versi PSB belum didukung ({0}) | PSB version not supported yet ({0})
Gambar terlalu besar ({0} x {1}) | Image too large ({0} x {1})
Arsip terpotong di posisi {0} | Archive truncated at position {0}
Entri Majiro tidak valid: {0} | Invalid Majiro entry: {0}
Entri BGI di luar arsip: {0} | BGI entry outside the archive: {0}
Token LZW di luar batas: {0} | LZW token out of range: {0}
Entri PAC di luar arsip: {0} | PAC entry outside the archive: {0}
Ekstensi tidak didukung: {0} | Extension not supported: {0}
Gagal memulai decoder {0}: {1} | Failed to start the {0} decoder: {1}
Tipe PSB tidak dikenal: {0} | Unknown PSB type: {0}
XPCM mode {0} tidak dikenal | Unknown XPCM mode {0}
Catatan filedata rusak: {0} | Corrupt filedata record: {0}
string {0} tidak berakhir | string {0} is not terminated
escape \{0} tidak dikenal | unknown escape \{0}
Nama terlalu panjang: {0} | Name too long: {0}
Versi tidak didukung: {0} | Version not supported: {0}
Entri di luar arsip: {0} | Entry outside the archive: {0}
Gagal membuat folder {0} | Failed to create folder {0}
File terlalu besar: {0} | File too large: {0}
Gagal membuat file {0} | Failed to create file {0}
Gagal membuka {0} | Failed to open {0}
{0} tidak valid | {0} is invalid
Tidak bisa membaca file asli | Could not read the original file
Bukan skrip .ast | Not an .ast script
Gagal menyiapkan analisis Vorbis | Failed to set up Vorbis analysis
FLAC hanya mendukung mono/stereo di sini | FLAC only supports mono/stereo here
Sample rate FLAC tidak valid | Invalid FLAC sample rate
Bukan DSC | Not DSC
Header DSC tidak valid | Invalid DSC header
Tabel Huffman DSC tidak valid | Invalid DSC Huffman table
DSC terpotong | DSC truncated
Kode DSC tidak valid | Invalid DSC code
Referensi DSC tidak valid | Invalid DSC reference
DSC melebihi ukuran | DSC exceeds the expected size
Bukan skrip BGI | Not a BGI script
Kepala BGI tidak valid | Invalid BGI header
Skrip BGI tidak terbaca | BGI script cannot be read
Bukan skrip BGI yang dikenali | Not a recognised BGI script
Pointer string BGI di luar file | BGI string pointer is outside the file
Panjang kunci Blowfish harus 4..56 byte | Blowfish key length must be 4..56 bytes
bukan file CatScene | not a CatScene file
zlib tidak lengkap | incomplete zlib data
data terlalu kecil | data too small
header tidak valid | invalid header
Data CRX terpotong | CRX data truncated
Bukan gambar CRX yang didukung | Not a supported CRX image
Ukuran gambar CRX tidak valid | Invalid CRX image size
Palet CRX terpotong | CRX palette truncated
CRX ini memakai kompresi Zstd (versi baru) yang belum didukung | This CRX uses Zstd compression (new version), which is not supported yet
CRX rusak: baris pertama memakai filter vertikal | Corrupt CRX: the first row uses a vertical filter
Bukan CRXD | Not CRXD
CRXD ini merujuk selisih di file lain (CRXJ), belum didukung | This CRXD refers to a difference in another file (CRXJ), which is not supported yet
Basis dan selisih berbeda format | Base and difference have different formats
Bukan file XPCM | Not an XPCM file
Ukuran Ogg di header XPCM tidak valid | Invalid Ogg size in XPCM header
Header XPCM terpotong | XPCM header truncated
PCM mentah | Raw PCM
Data Ogg terpotong | Ogg data truncated
Format audio tidak ada | No audio format
Audio terlalu besar | Audio too large
Data PCM terpotong | PCM data truncated
Bukan skrip Circus .mes | Not a Circus .mes script
Tabel offset Circus tidak valid | Invalid Circus offset table
Teks Circus tidak berakhir NUL | Circus text is not NUL-terminated
Tidak ada teks | No text
terlalu besar | too large
Urutan Nemesis tidak valid | Invalid Nemesis sequence
Bukan file CSX EntisGLS | Not an EntisGLS CSX file
Rekaman CSX rusak | Corrupt CSX record
Jumlah string CSX tidak valid | Invalid CSX string count
conststr rusak | corrupt conststr
CSX tanpa seksi conststr (kemungkinan CSX 1.x: belum didukung) | CSX without a conststr section (probably CSX 1.x: not supported yet)
Tidak ada teks non-ASCII di conststr | No non-ASCII text in conststr
Bukan skrip Escu:de (ESCR1_00) | Not an Escu:de script (ESCR1_00)
Jumlah string tidak valid | Invalid string count
Ukuran VM tidak valid | Invalid VM size
String terpotong | String truncated
Izin folder hilang | Folder permission lost
Folder tidak dapat dibuka | The folder cannot be opened
Bukan skrip HCB | Not an HCB script
Instruksi HCB melewati batas | HCB instruction crosses the boundary
Arsip terpotong | Archive truncated
Tidak ada teks Itufuru ditemukan | No Itufuru text found
karakter tak terduga | unexpected character
terlalu dalam | too deep
data berakhir terlalu cepat | data ended too early
literal salah | wrong literal
angka kosong | empty number
string tidak ditutup | unclosed string
escape terpotong | truncated escape
escape \u terpotong | truncated \u escape
escape \u salah | wrong \u escape
array tidak ditutup | unclosed array
diharapkan , atau ] | expected , or ]
diharapkan nama kunci | expected a key name
objek tidak ditutup | unclosed object
diharapkan , atau } | expected , or }
Bukan skrip scn | Not a scn script
Format gambar tidak didukung | Image format not supported
Gambar tidak bisa dibaca | The image cannot be read
Gagal menyandikan gambar | Failed to encode the image
Memori tidak cukup untuk gambar ini (coba kecilkan ukurannya) | Not enough memory for this image (try reducing its size)
Bukan teks UTF-8 yang valid | Not valid UTF-8 text
File terlalu besar untuk file teks | File too large for a text file
OGG (Opus) memerlukan Android 10 ke atas | OGG (Opus) requires Android 10 or later
Encoder tidak merespons | The encoder is not responding
Buffer encoder tidak tersedia | Encoder buffer unavailable
Buffer encoder terlalu kecil | Encoder buffer too small
Audio kosong atau tidak bisa disandikan | Audio is empty or cannot be encoded
Tidak ada trek audio di file ini | There is no audio track in this file
Trek audio tidak dikenal | Unknown audio track
Buffer decoder tidak tersedia | Decoder buffer unavailable
Tidak ada data audio yang bisa didekode | No audio data could be decoded
Shader GL gagal dikompilasi | GL shader failed to compile
EGL tidak bisa diinisialisasi | EGL could not be initialised
Konfigurasi EGL tidak ditemukan | EGL configuration not found
EGL gagal dipakai | EGL could not be used
Program GL gagal di-link | GL program failed to link
Frame video tidak sampai ke GL (timeout) | Video frame did not reach GL (timeout)
Tidak ada trek video di file ini | There is no video track in this file
Trek video tidak dikenal | Unknown video track
Ukuran video tidak diketahui | Video size unknown
Encoder video tidak merespons | The video encoder is not responding
Tidak ada frame video yang berhasil disandikan | No video frames were encoded successfully
Trek video hasil tidak ditemukan | Resulting video track not found
Trek audio tidak ditemukan | Audio track not found
Tidak bisa membaca | Cannot read
Data Huffman NeXAS habis | NeXAS Huffman data exhausted
Pohon Huffman NeXAS tidak valid | Invalid NeXAS Huffman tree
Bukan skrip NeXAS | Not a NeXAS script
Bukan skrip NeXAS (kepala tidak valid) | Not a NeXAS script (invalid header)
Blok teks NeXAS tidak ditemukan | NeXAS text block not found
Tidak ada teks non-ASCII | No non-ASCII text
Ukuran mdf tidak valid | Invalid mdf size
Dekompresi mdf gagal | mdf decompression failed
Array PSB tidak valid | Invalid PSB array
Bukan PSB | Not PSB
Header PSB tidak valid | Invalid PSB header
Tabel nama PSB rusak | PSB name table corrupt
PSB terlalu dalam | PSB too deep
pickle: MARK tidak ada | pickle: MARK missing
pickle terpotong | pickle truncated
pickle: APPEND ke tipe tak dikenal | pickle: APPEND to unknown type
pickle: SETITEMS ke tipe tak dikenal | pickle: SETITEMS to unknown type
Bukan file aset RPGM terenkripsi | Not an encrypted RPGM asset file
JSON VNTP hanya untuk dibaca penerjemah; tidak bisa diimpor. Impor berkas JSON/CSV/TXT dari ekspor FuruArc. | VNTP JSON is for translators to read only and cannot be imported. Import JSON/CSV/TXT files from a FuruArc export.
JSON: kunci "entries" tidak ada | JSON: the "entries" key is missing
JSON: bentuk berkas tidak dikenali | JSON: the file structure is not recognised
Slot data tidak ada | Data slot missing
Header rpyc rusak | rpyc header corrupt
Bukan file rpyc Ren'Py | Not a Ren'Py rpyc file
Struktur rpyc tidak dikenali | rpyc structure not recognised
Skrip biner | Binary script
Teks 暗号 minimal 8 byte | The 暗号 text must be at least 8 bytes
LZSS terlalu pendek | LZSS too short
LZSS: ukuran asli tidak wajar | LZSS: implausible original size
LZSS: data habis | LZSS: data exhausted
LZSS: referensi mundur tidak valid | LZSS: invalid backward reference
Scene.pck ini terenkripsi dengan kunci exe. Isi opsi Kunci (hex 16 byte atau teks 暗号). | This Scene.pck is encrypted with the exe key. Fill in the Key option (16-byte hex or 暗号 text).
Kunci exe salah atau data scene rusak | Wrong exe key or corrupt scene data
Bukan scene .dat Siglus | Not a Siglus scene .dat
Bukan skrip Silky .mes | Not a Silky .mes script
Header Silky tidak valid | Invalid Silky header
String Silky terpotong | Silky string truncated
Instruksi Silky terpotong | Silky instruction truncated
Skrip Silky rusak | Silky script corrupt
Ruby bersarang tidak diizinkan | Nested ruby is not allowed
Penutup ruby tanpa pembuka | Ruby closer without an opener
Baris baru di dalam ruby tidak diizinkan | Line breaks inside ruby are not allowed
Tidak bisa membaca file | Could not read the file
Jenis file tidak didukung | File type not supported
File terlalu besar | File too large
Bukan file CRX (kepala CRXG/CRXD tidak ditemukan) | Not a CRX file (CRXG/CRXD header not found)
Bukan skrip WS2 | Not a WS2 script
Bukan skrip WS2 yang dikenali (opcode tidak cocok V1–V3) | Not a recognised WS2 script (opcodes do not match V1–V3)
Index BGI tidak valid | Invalid BGI index
Ukuran total melebihi 4 GB | Total size exceeds 4 GB
File terlalu kecil | File too small
Jumlah entri tidak valid | Invalid entry count
Arsip INT terenkripsi (ada __key__.dat): isi opsi Password dengan password game (bisa dicari di Pencari Kunci dari file .exe game) | Encrypted INT archive (__key__.dat present): fill in the Password option with the game's password (find it with Key Finder from the game's .exe file)
Password salah atau indeks INT tidak cocok | Wrong password or the INT index does not match
Index INT tidak terbaca (kemungkinan terenkripsi, butuh kunci game) | INT index unreadable (probably encrypted, needs the game key)
Index di luar arsip | Index outside the archive
Password salah | Wrong password
Jumlah entri CRM tidak valid | Invalid CRM entry count
Index CRM tidak valid | Invalid CRM index
Tidak ada file | No files
Index DAT (Circus) tidak valid | Invalid DAT (Circus) index
Index DAT tidak valid | Invalid DAT index
Offset pertama tidak valid | Invalid first offset
Layout tidak cocok | Layout does not match
Nama kosong/tidak valid | Empty/invalid name
Offset tidak valid | Invalid offset
DAT Circus membutuhkan minimal 2 file | Circus DAT needs at least 2 files
Index PCK tidak valid | Invalid PCK index
Entri PCK tidak valid | Invalid PCK entry
Index Escu:de tidak valid | Invalid Escu:de index
Entri Escu:de di luar arsip | Escu:de entry outside the archive
Nama Escu:de tidak valid | Invalid Escu:de name
Data LZW terlalu pendek | LZW data too short
Ukuran LZW tidak valid | Invalid LZW size
Data LZW habis | LZW data exhausted
Lebar token LZW melebihi batas | LZW token width exceeds the limit
Kamus LZW penuh | LZW dictionary full
Token LZW rusak | Corrupt LZW token
Index BIN (Favorite) tidak valid | Invalid BIN (Favorite) index
Header BIN tidak valid | Invalid BIN header
Offset nama tidak valid | Invalid name offset
Entri BIN tidak valid | Invalid BIN entry
Jumlah entri ARCC tidak valid | Invalid ARCC entry count
Offset ADDR tidak valid | Invalid ADDR offset
ARCC tidak berisi file | ARCC contains no files
Pack ARCC belum didukung | ARCC packing is not supported yet
Header ODIO tidak valid | Invalid ODIO header
Indeks ODIO tidak valid | Invalid ODIO index
ODIO kosong | ODIO is empty
Offset ODIO tidak berurutan | ODIO offsets are out of order
Pack ODIO belum didukung | ODIO packing is not supported yet
Jumlah entri WAG tidak valid | Invalid WAG entry count
WAG tidak berisi entri yang terbaca | WAG contains no readable entries
Pack WAG belum didukung | WAG packing is not supported yet
Index Itufuru tidak valid | Invalid Itufuru index
Entri Itufuru tidak valid | Invalid Itufuru entry
Versi Majiro tidak didukung | Majiro version not supported
Index Majiro tidak valid | Invalid Majiro index
Tabel Majiro tidak cocok | Majiro table does not match
Majiro tidak berisi file | Majiro contains no files
Index SAR/NSA tidak valid | Invalid SAR/NSA index
Header SAR/NSA tidak valid | Invalid SAR/NSA header
Nama tidak valid | Invalid name
Entri tidak valid | Invalid entry
Tipe kompresi tidak valid | Invalid compression type
Terlalu banyak file (maks 65535) | Too many files (max 65535)
Bukan arsip NeXAS PAC | Not a NeXAS PAC archive
Panjang indeks NeXAS tidak valid | Invalid NeXAS index length
Indeks NeXAS kosong atau rusak | NeXAS index is empty or corrupt
Entri terlalu besar | Entry too large
Arsip melebihi 4 GB | Archive exceeds 4 GB
Bukan arsip NOA | Not a NOA archive
NOA kosong atau indeks tidak terbaca | NOA is empty or the index cannot be read
Indeks NOA di luar arsip | NOA index outside the archive
Catatan DirEntry tidak ditemukan | DirEntry record not found
Ukuran DirEntry tidak valid | Invalid DirEntry size
Jumlah entri NOA tidak valid | Invalid NOA entry count
Indeks NOA terpotong | NOA index truncated
Indeks NOA rusak | NOA index corrupt
Nama NOA rusak | NOA name corrupt
Entri terenkripsi: isi opsi Password (cari dengan Pencari Kunci dari .exe game) | Encrypted entry: fill in the Password option (find it with Key Finder from the game's .exe)
Index PFS tidak valid | Invalid PFS index
Jumlah entri pf2 tidak valid | Invalid pf2 entry count
Index pf2 terpotong | pf2 index truncated
Bukan arsip QLIE | Not a QLIE archive
Index QLIE tidak valid | Invalid QLIE index
Index terpotong | Index truncated
Panjang nama tidak valid | Invalid name length
Nama QLIE tidak terbaca | QLIE name unreadable
Entri QLIE di luar arsip | QLIE entry outside the archive
Index QLIE tidak pas | QLIE index does not fit
File terkompresi/terenkripsi QLIE (butuh kunci game, belum didukung) | QLIE file is compressed/encrypted (needs the game key, not supported yet)
Pack QLIE belum didukung | QLIE packing is not supported yet
Arsip RGSSAD rusak | RGSSAD archive corrupt
Arsip RGSS3A rusak | RGSS3A archive corrupt
Terlalu banyak file | Too many files
Arsip melebihi 2 GB | Archive exceeds 2 GB
Header RPA tidak valid | Invalid RPA header
Index RPA bukan dict | RPA index is not a dict
Kunci tidak valid | Invalid key
Daftar nama scene tidak valid | Invalid scene name list
Data scene di luar arsip | Scene data outside the archive
Ukuran Scene.pck melebihi 2 GB | Scene.pck size exceeds 2 GB
Gameexe.dat ini terenkripsi dengan kunci exe. Isi opsi Kunci; cari dengan Pencari Kunci (di folder game yang punya Scene.pck). | This Gameexe.dat is encrypted with the exe key. Fill in the Key option; find it with Key Finder (in the game folder that has Scene.pck).
Kunci exe salah atau Gameexe.dat rusak | Wrong exe key or corrupt Gameexe.dat
Gameexe.dat tidak valid | Invalid Gameexe.dat
Gameexe.ini tidak ditemukan di folder | Gameexe.ini not found in the folder
Tabel OVK tidak valid | Invalid OVK table
Entri OVK di luar arsip | OVK entry outside the archive
Entri OVK bukan OGG | OVK entry is not OGG
Tidak ada file .ogg di folder | No .ogg files in the folder
Ukuran OVK melebihi 2 GB | OVK size exceeds 2 GB
Index ARC (Silky) tidak valid | Invalid ARC (Silky) index
Entri ARC (Silky) tidak valid | Invalid ARC (Silky) entry
Index PAC tidak valid | Invalid PAC index
File PAC terlalu kecil | PAC file too small
Jumlah entri PAC tidak valid | Invalid PAC entry count
Index PAC di luar arsip | PAC index outside the archive
Layout PAC tidak cocok | PAC layout does not match
File ZIP terlalu kecil | ZIP file too small
Akhir direktori ZIP tidak ditemukan | ZIP end of central directory not found
ZIP64 belum didukung | ZIP64 is not supported yet
Direktori ZIP tidak valid | Invalid ZIP directory
Entri ZIP rusak | ZIP entry corrupt
Header lokal ZIP rusak | ZIP local header corrupt
Data deflate tidak valid | Invalid deflate data
Index ARC tidak valid | Invalid ARC index
Header ARC tidak valid | Invalid ARC header
Ekstensi tidak valid | Invalid extension
Tabel ARC di luar arsip | ARC table outside the archive
Entri ARC tidak valid | Invalid ARC entry
Nama file terlalu panjang (maks 12 byte tanpa ekstensi) | File name too long (max 12 bytes without extension)
Nama file terlalu panjang untuk 8 byte (maks 8 byte tanpa ekstensi) | File name too long for 8 bytes (max 8 bytes without extension)
Terlalu banyak jenis ekstensi | Too many extension types
Offset index XP3 tidak valid | Invalid XP3 index offset
Segmen XP3 terlalu besar | XP3 segment too large
Header YPF tidak valid | Invalid YPF header
Index YPF tidak valid | Invalid YPF index
Nama YPF tidak terbaca | YPF name unreadable
Pack YPF belum didukung | YPF packing is not supported yet
Root folder tidak ada | Root folder does not exist
Nama file kosong | Empty file name
GBK (Mandarin sederhana) | GBK (Simplified Chinese)
Big5 (Mandarin tradisional) | Big5 (Traditional Chinese)
Bukan skrip YSTB | Not a YSTB script
Tidak ada teks (skrip kosong) | No text (empty script)
Header YSTB tidak valid | Invalid YSTB header
Versi YPF yang didukung untuk pack: 480–500 | YPF versions supported for packing: 480–500
file .ybn | .ybn file
Bukan file OVK yang dikenali | Not a recognised OVK file
OVK kosong | OVK is empty
Bukan file NWA yang dikenali | Not a recognised NWA file
Bukan file MIO/EMI yang dikenali | Not a recognised MIO/EMI file
Dekoder Android gagal membaca gambar | The Android decoder failed to read the image
Gagal menulis JPG | Failed to write JPG
"""
    const val E1 = """
Bukan bundle UnityFS | Not a UnityFS bundle
Header Unity terpotong | Unity header is truncated
Data Unity terpotong | Unity data is truncated
String Unity tidak berakhir | Unity string is not terminated
Blok Unity tidak lengkap | Unity block is incomplete
Kompresi Unity {0} tidak dikenal | Unknown Unity compression {0}
Versi UnityFS {0} belum didukung | UnityFS version {0} is not supported yet
Ukuran info blok Unity tidak valid | Invalid Unity block info size
Kompresi info blok tidak dikenal | Unknown block info compression
File terpotong: ukuran header {0} > {1} | File truncated: header size {0} > {1}
Info blok Unity di luar file | Unity block info is outside the file
Info blok terpotong | Block info is truncated
Jumlah blok Unity tidak valid | Invalid Unity block count
Blok Unity terlalu besar | Unity block is too large
Data blok Unity melewati akhir file | Unity block data goes past the end of the file
Jumlah node Unity tidak valid | Invalid Unity node count
Node Unity di luar data: {0} | Unity node is outside the data: {0}
Bundle Unity tidak berisi file | The Unity bundle contains no files
Memori tidak cukup untuk membuka blok {0} MB | Not enough memory to open a {0} MB block
Entri Unity tidak valid | Invalid Unity entry
Data Unity berakhir lebih awal: {0} | Unity data ended early: {0}
Tidak ada file untuk dikemas | No files to pack
Ukuran file berubah saat dikemas: {0} | File size changed while packing: {0}
Data LZMA terlalu pendek | LZMA data is too short
Properti LZMA tidak valid | Invalid LZMA properties
Data LZMA rusak | LZMA data is corrupted
Data LZMA berakhir lebih awal | LZMA data ended early
Data LZ4 terpotong | LZ4 data is truncated
Data LZ4 rusak | LZ4 data is corrupted
Ukuran hasil LZ4 tidak cocok | LZ4 output size does not match
File indeks tidak ditemukan — pilih lagi di opsi | Index file not found — pick it again in the options
Bukan file indeks Eushully (SYS4INI.BIN / SYS5INI.BIN / .AAI) | Not an Eushully index file (SYS4INI.BIN / SYS5INI.BIN / .AAI)
File indeks terpotong | Index file is truncated
Ukuran indeks tidak valid | Invalid index size
Ini file indeks, bukan arsip. Buka file .ALF (mis. DATA1.ALF) dan pilih file indeks ini di opsi | This is an index file, not an archive. Open an .ALF file (e.g. DATA1.ALF) and pick this index in the options
Pilih File indeks (SYS4INI.BIN / SYS5INI.BIN / .AAI) di opsi | Pick the Index file (SYS4INI.BIN / SYS5INI.BIN / .AAI) in the options
Indeks kosong | The index is empty
Indeks Eushully tidak valid | Invalid Eushully index
ALF "{0}" tidak ada di indeks. Isi "Nama ALF di indeks" dengan salah satu: {1} | ALF "{0}" is not in the index. Fill "ALF name in the index" with one of: {1}
Jumlah file indeks tidak valid | Invalid number of files in the index
Tidak ada file yang cocok untuk "{0}" — pastikan indeks dan ALF dari game yang sama | No files match "{0}" — make sure the index and the ALF come from the same game
Pengemasan ALF belum didukung | Packing ALF is not supported yet
Bukan arsip WARC ShiinaRio | Not a ShiinaRio WARC archive
WARC versi 1.{0} memakai enkripsi per judul game — belum didukung (hanya WARC 1.1 yang terbaca) | WARC version 1.{0} uses per-title encryption — not supported yet (only WARC 1.1 can be read)
Offset indeks WARC tidak valid | Invalid WARC index offset
Indeks WARC terlalu besar | The WARC index is too large
Indeks WARC tidak terbaca (mungkin skema enkripsi lain) | The WARC index cannot be read (possibly another encryption scheme)
Entri terlalu besar | The entry is too large
Data YPK rusak: {0} | YPK data is corrupted: {0}
Pengemasan YH1 belum didukung: {0} | YH1 packing is not supported yet: {0}
Pengemasan YLZ belum didukung: {0} | YLZ packing is not supported yet: {0}
Pengemasan WARC belum didukung | Packing WARC is not supported yet
"""
    val ALL = listOf(E0, E1)
}
