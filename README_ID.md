<p align="center">
  <img src="pictures/0_header.png" alt="FuruArc" width="100%">
</p>

<p align="center">
  <a href="LICENSE"><img alt="License: GPL-3.0" src="https://img.shields.io/badge/license-GPL--3.0-blue.svg"></a>
  <img alt="Platform: Android 7.0+" src="https://img.shields.io/badge/Android-7.0%2B-3DDC84.svg">
  <img alt="Kotlin" src="https://img.shields.io/badge/Kotlin-1.6.10-7F52FF.svg">
  <img alt="Jetpack Compose" src="https://img.shields.io/badge/Jetpack%20Compose-1.1.1-4285F4.svg">
  <a href="https://github.com/Fueeru26/FuruArc/releases"><img alt="Release" src="https://img.shields.io/github/v/release/Fueeru26/FuruArc?include_prereleases"></a>
</p>

# FuruArc

[English](README.md) | **Bahasa Indonesia**

**FuruArc** adalah toolkit Android untuk game **RPG Maker** dan **Visual Novel**. Semuanya berjalan langsung di HP: bongkar dan kemas arsip game, dekripsi aset, ekstrak dan terjemahkan teks skrip, edit file save, konversi audio, gambar dan video, serta telusuri aset pustaka game, tanpa perlu PC.

Aplikasi ditulis dengan Kotlin dan Jetpack Compose. Semua alat bekerja lewat Storage Access Framework Android, sehingga bisa membuka file dan folder di mana saja (termasuk folder game di penyimpanan eksternal) tanpa izin penyimpanan yang luas. Antarmuka tersedia dalam **Indonesia, Inggris, Jepang, Mandarin dan Korea**, dengan tema terang dan gelap.

> **Catatan**
> FuruArc adalah utilitas modding dan lokalisasi serbaguna. Gunakan hanya untuk game dan file milikmu sendiri atau yang boleh kamu ubah, dan hormati lisensi serta ketentuan game yang kamu kerjakan.

## Daftar isi

- [Fitur](#fitur)
- [Engine yang didukung](#engine-yang-didukung)
- [Instalasi](#instalasi)
- [Build dari source](#build-dari-source)
- [Struktur proyek](#struktur-proyek)
- [Berkontribusi](#berkontribusi)
- [Lisensi](#lisensi)
- [Kredit](#kredit)

## Fitur

### Beranda

Tab pertama adalah peluncur untuk semua fitur di aplikasi.

- **Cari fitur**: ketik nama fitur, engine, atau ekstensi file (misalnya `xp3`, `tlg`, `translate`) lalu buka hasilnya langsung. Jalur menu tampil di bawah tiap hasil, dan bintang menambahkannya ke Favorit.
- **Riwayat pencarian** dan chip **Terakhir dipakai**.
- **Aksi cepat**: hingga 10 pintasan yang kamu pilih dan urutkan sendiri (gagang seret untuk mengurutkan, ✕ untuk menghapus). Bawaannya Bongkar Arsip, Editor Terjemahan, Editor File Teks dan Konverter Aset Media.
- **Cari fitur melalui file**: pilih file apa pun dan FuruArc menyarankan fitur yang cocok dengan ekstensinya. Saat kamu memilih salah satu, file langsung dimuat ke fitur tersebut, tanpa perlu memilih ulang.
- **Favorit** untuk alat yang paling sering dipakai.

### Aset Game (pustaka)

<p align="center"><img src="pictures/1_game_assets.png" alt="Aset Game" width="100%"></p>

Tambahkan game sebagai pustaka dan telusuri isinya.

- **Pustaka game** dengan cover, pengurutan (nama, terakhir ditambahkan, acak), favorit, tambah folder induk sekaligus (tiap subfolder jadi satu game) dan pencarian cover di internet.
- **Audio**: telusuri dan putar audio game, termasuk format khusus (OVK, NWA, MIO dan `.rpgmvo` / `.rpgmvm` RPG Maker terenkripsi).
- **Gambar**: pratinjau gambar game, termasuk format khusus engine (`.crx`, `.pgd`, `.pna`, `.ycg`, `.hg2`, `.hg3`, `.tlg`, `.eri`, `.g00`, `.rct`, `.rc8`).
- **Video**: putar video game.
- **Teks**: baca dan cari file skrip dan teks.
- **Pemindaian arsip**: arsip di dalam game dipindai agar isinya tampil sebagai aset.
- **Ekstrak, bagikan dan favoritkan** aset apa pun.

### Alat RPGM

<p align="center"><img src="pictures/2_rpgm_tools.png" alt="Alat RPGM" width="100%"></p>

Dekripsi aset, terjemahkan data, edit save dan kelola plugin RPG Maker.

| Alat | Fungsi |
| --- | --- |
| Daftar Engine | RPGM MV, MZ, XP, VX dan VX Ace. |
| Dekripsi / Enkripsi Aset | Gambar dan audio MV dan MZ; kunci dibaca dari `System.json` atau dideteksi dari gambar. Mode file atau folder, termasuk subfolder. |
| Bongkar / Kemas Arsip | Arsip XP, VX dan VX Ace: `rgssad`, `rgss2a` dan `rgss3a`. |
| Terjemahan Data | Ekspor teks game untuk diterjemahkan, lalu impor kembali. |
| Editor Save | Ekspor file save untuk mengedit nilainya, lalu impor kembali (`rpgsave` / `rmmzsave`). |
| Editor Nilai | Gold, langkah, karakter (level, HP, MP, TP), item, senjata, armor, variabel dan switch, dengan aksi massal. |
| Pemindai Plugin | Pindai plugin MV/MZ untuk teks dan nilai yang bisa diedit. |
| Editor Plugin.js | Aktifkan, nonaktifkan dan urutkan plugin, atau tambah yang baru. |
| Pemeriksa dan Pembanding | Temukan duplikat, file hilang dan masalah dependensi; bandingkan file `plugins.js`. |

### Alat VN

<p align="center"><img src="pictures/3_vn_tools.png" alt="Alat VN" width="100%"></p>

Bongkar, kemas dan terjemahkan visual novel di 21 engine (lihat [Engine yang didukung](#engine-yang-didukung)).

| Alat | Fungsi |
| --- | --- |
| Daftar Engine | Pilih engine, atau biarkan FuruArc mendeteksi format otomatis. |
| Bongkar Arsip | Ekstrak seluruh arsip atau hanya file terpilih, satu atau banyak arsip sekaligus. |
| Kemas Arsip | Buat arsip untuk engine terpilih dari sebuah folder. |
| Lihat Isi Arsip | Telusuri dan cari isi arsip dengan pratinjau gambar dan teks. |
| Pencari Kunci | Cari kunci enkripsi di folder game dan file yang memakainya. |
| Ekspor Teks Skrip | Kumpulkan dialog dan pilihan ke satu file CSV, TXT, JSON atau JSON VNTP. |
| Impor Teks Skrip | Masukkan kembali terjemahan CSV, TXT atau JSON ke skrip game. |
| Daftar Isi Arsip | Simpan nama dan ukuran semua file di dalam arsip. |
| Encoding Nama File | Otomatis, Shift-JIS, UTF-8, GBK, Big5 atau EUC-KR. |

### Editor Terjemahan

<p align="center"><img src="pictures/4_translation_editor.png" alt="Editor Terjemahan" width="100%"></p>

Terjemahkan teks game hasil ekspor dengan draf, glosarium dan terjemahan online.

- **File draf**: buka ekspor `.txt`, `.csv` atau `.json`, atau draf tersimpan, dan simpan progres kapan saja.
- **Area terjemahan**: teks asli di atas tiap kolom, dengan progres, pencarian dan filter Semua / Terisi / Kosong / Online.
- **Perbarui Terjemahan**: tulis file jadi yang siap diimpor kembali ke game.
- **Terjemahan online**: Lingva dan Bilibili gratis, atau pakai API key sendiri dengan Google, DeepL, Gemini, OpenAI, DeepSeek, Claude, Groq, OpenRouter atau endpoint khusus. Permintaan paralel, uji koneksi dan perkiraan jumlah permintaan sebelum mulai.
- **Glosarium**: istilah tetap dengan saran di bawah tiap kolom; impor dan ekspor CSV.
- **Pengaturan editor**: huruf awal, bungkus otomatis, ganti kata dan isi otomatis kode kontrol.
- **Urungkan dan riwayat**: batalkan perubahan massal dan lihat suntingan terbaru.
- **Cek kode kontrol**: memperingatkan bila kode seperti `\C[2]` berbeda dari teks asli.

### Konverter Aset Media

<p align="center"><img src="pictures/5_media_asset_converter.png" alt="Konverter Aset Media" width="100%"></p>

Konversi dan kompres audio, gambar dan video game, satu file atau satu folder penuh.

- **Audio**: M4A, OGG, MP3, FLAC dan WAV, dengan bitrate, sample rate, kanal dan volume.
- **Gambar**: PNG, JPG dan WEBP, dengan kualitas, ukuran dan rotasi; dithering opsional saat warna PNG dikurangi.
- **Video**: MP4 (H.264 / H.265) dan WebM, dengan resolusi, bitrate dan frame rate.
- **Campuran**: konversi audio, gambar dan video sekaligus dari satu folder.
- **Konverter Aset Khusus**: ubah format media khusus engine menjadi PNG, JPG, WAV atau OGG.
- **Konverter Encoding Teks**: Shift-JIS, GBK, Big5 dan EUC-KR ke UTF-8, dan sebaliknya.
- **Kompres berdasarkan kualitas atau ukuran target**, dengan input beberapa file atau satu folder (struktur dipertahankan).
- **Preset dan keamanan**: preset engine, pratinjau sampel, cadangan sebelum menimpa dan urungkan per file.

### Editor Teks

<p align="center"><img src="pictures/6_more_features.png" alt="Fitur lainnya" width="100%"></p>

- **Editor File Teks Biasa**: `.txt`, `.ini`, `.ks`, `.rpy` dan sejenisnya, dengan cari dan ganti (regex), lompat ke baris, pewarnaan sintaks, urungkan/ulangi, deteksi encoding otomatis dan draf tersimpan otomatis.
- **File Teks Berstruktur**: JSON (tampilan pohon dan teks), CSV / TSV (tabel sel), INI / CFG, XML dan Markdown (dengan pratinjau).
- **Ganti Massal**: cari dan ganti teks di banyak file dalam satu folder, dengan penghitung kecocokan sebelum apa pun diubah.

### Pemeriksaan File

- **Pemeriksaan Arsip dan Skrip**: format, checksum, file rusak, enkripsi dan deteksi kode salah dalam satu file.
- **Pemeriksaan Folder**: periksa semua arsip dan skrip dalam satu folder sekaligus.
- **Bandingkan File**: bandingkan dua arsip, skrip atau file terjemahan.
- **Pencari File Duplikat**: file identik dalam folder dan subfoldernya.
- **Penampil Hex**: isi mentah file berdasarkan offset, dengan pencarian teks dan byte.
- **Pemindaian Terjemahan**: kode kontrol, baris panjang, entri kosong dan terjemahan tidak konsisten.
- **Pencari Kunci**: cari kunci enkripsi dan file yang memakainya.

### Perkakas File dan Pengoptimal Nama File

- **Pemotong File**: ambil bagian awal, tengah atau akhir file besar menjadi file kecil, misalnya untuk mengirim sampel.
- **Pemisah / Penggabung File**: pecah file besar menjadi bagian bernomor (`.001`, `.002`, ...) dan gabungkan kembali.
- **Ganti Nama Massal**: ubah nama banyak file sekaligus, dengan pratinjau sebelum diterapkan.
- **Pengoptimal Nama File**: buat nama file aman (karakter non-Latin) dan perbarui referensinya sebelum membuat APK.

### Pengaturan dan pembaruan

- Tema (terang / gelap), bahasa, warna aksen.
- **Proses latar belakang**: tugas panjang tetap berjalan saat aplikasi ditutup, dengan notifikasi saat selesai (Android 13+ meminta izin notifikasi saat kamu mengaktifkannya).
- **Periksa pembaruan**: mengecek rilis GitHub terbaru. Titik merah menandai Pengaturan bila ada versi baru, pop-up muncul sekali per versi dan, bila notifikasi latar belakang aktif, notifikasi sistem ditampilkan. FuruArc tidak pernah mengunduh atau memasang apa pun sendiri; ia hanya membuka halaman rilis atau tautan APK di browser.
- Halaman panduan, tanya jawab dan pemecahan masalah di dalam aplikasi.

## Engine yang didukung

**Visual novel (21)**

Artemis (`.pfs`), Kirikiri (`.xp3`), Ren'Py (`.rpa`), BGI / Ethornell (`.arc`), Softpal ADV System (`.pac`), WillPlus / AdvHD (`.arc`), NScripter (`.sar`), Tyrano Script (`.nw`), FVP / Favorite (`.bin`), Adv32 / Circus (`.pck`, `.dat`, `.crm`), Silky Engine (`.arc`), CatSystem 2 (`.int`), Siglus Engine (`.pck`, `Gameexe.dat`, suara `.ovk`), QLIE (`.pack`), Yu-Ris (`.ypf`), Escu:de (`.bin`), Yaneurao Itufuru (`.scd`), EntisGLS (`.noa`), NeXAS (`.pac`), Majiro (`.arc`) dan HexenHaus (`.arc`, ODIO, WAG).

Alat teks skrip (ekspor / impor) tersedia untuk sebagian engine di atas; daftar engine di aplikasi menunjukkan apa yang didukung tiap engine.

**RPG Maker**

MV dan MZ (enkripsi aset per file), XP (`.rgssad`), VX (`.rgss2a`) dan VX Ace (`.rgss3a`).

## Instalasi

Unduh APK terbaru dari halaman [Releases](https://github.com/Fueeru26/FuruArc/releases) lalu pasang. Membutuhkan **Android 7.0 (API 24)** atau lebih baru.

## Build dari source

Kebutuhan: JDK 11 dan Android SDK (platform 33). Gradle wrapper sudah disertakan.

```bash
git clone https://github.com/Fueeru26/FuruArc.git
cd FuruArc
./gradlew assembleDebug
```

APK debug ada di `app/build/outputs/apk/debug/`.

| Komponen | Versi |
| --- | --- |
| Android Gradle Plugin | 4.2.2 |
| Gradle | 7.5.1 |
| Kotlin | 1.6.10 |
| Jetpack Compose (UI, Material 2) | 1.1.1 |
| compileSdk / targetSdk / minSdk | 33 / 34 / 24 |

> **Penandatanganan**
> Tidak ada keystore atau sandi di repositori ini. Build debug tidak membutuhkannya. Untuk rilis bertanda tangan, sediakan keystore milikmu lewat variabel lingkungan `ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` dan `ANDROID_KEY_PASSWORD`, atau lewat `keystore.properties` (tidak dilacak git) di root proyek (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`). Tanpa itu, `assembleRelease` menghasilkan APK tanpa tanda tangan.

## Struktur proyek

```text
FuruArc/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/                          Ikon, cover engine, aturan backup
│       └── java/
│           ├── com/furuarc/
│           │   ├── MainActivity.kt       Navigasi dan kerangka aplikasi
│           │   ├── *Jobs.kt              Tugas panjang (RPGM, skrip, media, plugin, pemeriksaan)
│           │   ├── core/                 Antarmuka arsip, registri format, pembaca/penulis, pemeriksa pembaruan
│           │   ├── formats/              Satu file per format arsip engine
│           │   ├── storage/              Helper Storage Access Framework
│           │   ├── settings/             Pengaturan aplikasi
│           │   ├── i18n/                 Teks UI (ID/EN) dan data terjemahan JA/ZH/KO
│           │   └── ui/                   Layar Compose, komponen dan tema
│           ├── net/sourceforge/lame/     java-lame (LGPL), encoder MP3
│           └── org/xiph/                 vorbis-java (gaya BSD), encoder OGG/Vorbis
├── languages/                            Teks aplikasi dalam 5 bahasa berformat JSON (Indonesia, Inggris, Jepang, Mandarin, Korea)
├── pictures/                             Gambar README
├── README_ID.md                          README bahasa Indonesia
├── THIRD_PARTY_NOTICES.md
├── CONTRIBUTING.md
├── SECURITY.md
└── LICENSE
```

## Berkontribusi

Laporan bug, catatan format engine dan pull request sangat diterima. Baca [CONTRIBUTING.md](CONTRIBUTING.md) terlebih dahulu. Untuk masalah keamanan lihat [SECURITY.md](SECURITY.md).

## Lisensi

FuruArc dilisensikan di bawah **GNU General Public License v3.0**. Lihat [LICENSE](LICENSE). Kode pihak ketiga yang disertakan tetap memakai lisensinya sendiri, lihat [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

## Kredit

- Dibuat oleh **Fueeru** ([@Fueeru26](https://github.com/Fueeru26)).
- [java-lame](https://github.com/nwaldispuehl/java-lame) (LGPL): encoding MP3.
- [vorbis-java](https://github.com/tulskiy/musique) dan Xiph.Org Foundation: encoding OGG/Vorbis.
- Perilaku format dicek terhadap alat open-source [GARbro](https://github.com/morkt/GARbro) dan [msg-tool](https://github.com/lifegpc/msg-tool).
