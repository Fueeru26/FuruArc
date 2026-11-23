# Engine yang didukung

[English](ENGINES.md) | **Bahasa Indonesia**

Apa yang bisa dilakukan FuruArc untuk tiap engine pada versi 1.05. **Daftar Engine** di aplikasi (Alat VN dan Alat RPGM) selalu menampilkan opsi yang tepat untuk build yang terpasang. Perilaku format dicek terhadap alat open-source [GARbro](https://github.com/morkt/GARbro) dan [msg-tool](https://github.com/lifegpc/msg-tool).

Keterangan: ✔ didukung, – tidak didukung.

## Visual novel (24 engine)

**Arsip** = lihat isi dan ekstrak (Lihat Isi Arsip, Bongkar Arsip). **Kemas** = buat arsip dari folder. **Teks skrip** = Ekspor / Impor Teks Skrip (CSV, TXT, JSON atau JSON VNTP) untuk jenis skrip yang tertulis.

| Engine | Ekstensi arsip | Arsip | Kemas | Teks skrip | Catatan |
| --- | --- | :---: | :---: | --- | --- |
| Artemis | `.pfs` | ✔ pf2, pf6, pf8 | ✔ pf2, pf6, pf8 | ✔ `.txt`, `.ast` | |
| Kirikiri | `.xp3` | ✔ v1, v2 | ✔ | ✔ `.ks`, `.scn` (PSB) | Belum ada cipher per game, hanya XOR 1 byte opsional. Deflate opsional saat mengemas. |
| Ren'Py | `.rpa` | ✔ RPA-2.0, 3.0, 3.2 | ✔ RPA-3.0 (2.0 bisa dipilih) | ✔ `.rpy` | File `.rpyc` bisa didekompilasi ke `.rpy`; layar (screen) ditulis sebagai komentar. |
| BGI / Ethornell | `.arc` | ✔ PackFile, ARC20 | ✔ ARC20 (PackFile bisa dipilih) | ✔ skrip tanpa ekstensi | Data DSC dan CompressedBG di dalamnya diekstrak apa adanya. |
| Softpal ADV System | `.pac` | ✔ klasik, Amuse Craft | ✔ | – | File skrip berawalan `$` terenkripsi dan diekstrak apa adanya. |
| WillPlus / AdvHD | `.arc` | ✔ | ✔ | ✔ `.ws2` | |
| NScripter | `.sar`, `.nsa` | ✔ SAR, NSA tipe 0 | ✔ SAR, NSA tanpa kompresi | ✔ `0.txt` | `nscript.dat` bisa diubah menjadi `0.txt` dan dikembalikan. NSA terkompresi (SPB, LZSS, NBZ) belum didukung. |
| Tyrano Script | `.nw` (`package.nw`) | ✔ ZIP | ✔ ZIP (deflate) | ✔ `.ks` | `app.asar` (Electron) dan paket yang menempel di dalam `.exe` belum didukung. |
| FVP / Favorite | `.bin` | ✔ | ✔ | ✔ `.hcb` | |
| Adv32 / Circus | `.pck`, `.dat`, `.crm` | ✔ | ✔ | ✔ `.mes` | `.dat` butuh minimal 2 file untuk dikemas. File di dalamnya diekstrak apa adanya. |
| Silky Engine | `.arc` | ✔ | ✔ | ✔ `.mes` | Kunci XOR nama ditebak dari padding. |
| CatSystem 2 | `.int` | ✔ | ✔ | ✔ `.cst` | Indeks terenkripsi butuh sandi game di opsi. |
| Siglus Engine | `.pck`, `Gameexe.dat`, suara `.ovk` | ✔ | ✔ | ✔ `.dat` scene | Kunci exe bisa diisi di opsi (Scene.pck terdeteksi otomatis; pakai Pencari Kunci untuk `Gameexe.dat` terenkripsi). |
| QLIE | `.pack` | ✔ hanya entri polos | – | ✔ `.s` | Entri terkompresi atau terenkripsi butuh kunci game dan belum didukung. |
| Yu-Ris | `.ypf` | ✔ versi 234 sampai 500 | ✔ versi 480 sampai 500 | ✔ `.ybn` | |
| Escu:de | `.bin` | ✔ | ✔ | ✔ `.bin` | |
| Yaneurao Itufuru | `.scd` | ✔ | ✔ | ✔ skrip tanpa ekstensi | Nama file maksimal 12 byte saat mengemas. |
| EntisGLS | `.noa` | ✔ Raw, ERISA, BSHF | ✔ Raw atau BSHF (sandi) | ✔ `.csx` | |
| NeXAS | `.pac` | ✔ raw, LZSS, zlib, zstd | ✔ raw atau zlib | ✔ `.bin` | Fitur kemas belum diuji pada arsip game asli. |
| Majiro | `.arc` | ✔ v1, v2, v3 | ✔ | – | |
| AGE / Eushully | `.alf` | ✔ dengan file indeks | ✔ satu ALF plus `SYS4INI.BIN` baru | – | Pilih **File indeks** (`SYS4INI.BIN`, `SYS5INI.BIN` atau `.AAI`) di opsi. Beberapa file ALF bisa dibongkar sekaligus. |
| Shiina Rio | `.war` | ✔ WARC 1.1 | ✔ WARC 1.1 (tanpa kompresi, nama maks 15 byte) | – | WARC versi lebih baru memakai skema per judul dan belum didukung. |
| Unity | `.unity3d`, `.bundle` | ✔ UnityFS (LZMA, LZ4) | ✔ UnityFS v6 atau v7 tanpa kompresi | – | Hanya tingkat kontainer: bundle terenkripsi dan objek di dalam file serialized tidak diproses. |
| HexenHaus | `.arc`, ODIO, WAG | ✔ | – | – | Hanya baca. |

Catatan:

- Arsip tanpa magic bytes (Circus, FVP, WillPlus, Silky, NScripter, dan lainnya) dikenali dari strukturnya. Jika deteksi gagal, pilih engine sendiri di Daftar Engine.
- **Uji Coba Format** (Pemeriksa File dan Folder) mengemas file contoh dengan setiap format yang bisa dikemas di perangkatmu lalu membongkarnya lagi, sehingga cepat terlihat apakah sebuah format berfungsi di ponselmu. Ia juga memeriksa contoh tepi, kemas ulang, codec, teks skrip dan arsip asli kecil (`assets/formattest`).
- **Pencari Kunci** mencari kunci enkripsi di folder game dan menampilkan file yang memakainya.
- Nama file di dalam arsip bisa dibaca sebagai Otomatis, Shift-JIS, UTF-8, GBK, Big5 atau EUC-KR (**Encoding Nama File**).
- Game sering menyimpan gambar, audio dan skrip dalam format khusus engine. Penampil Aset Game dan **Konverter Aset Khusus** menangani banyak di antaranya (misalnya `.crx`, `.pgd`, `.tlg`, `.g00`, OVK, NWA, MIO).

## RPG Maker

| Engine | File | Yang dilakukan FuruArc |
| --- | --- | --- |
| MV, MZ | enkripsi aset per file (bukan arsip) | Dekripsi dan enkripsi gambar dan audio (kunci dibaca dari `System.json` atau dideteksi dari gambar), terjemahan data, editor save (`rpgsave` / `rmmzsave`), editor nilai, pemindai plugin, editor `plugins.js`, pemeriksa dan pembanding. |
| XP | `.rgssad` | Bongkar dan kemas arsip. |
| VX | `.rgss2a` | Bongkar dan kemas arsip. |
| VX Ace | `.rgss3a` | Bongkar dan kemas arsip. |

## Engine atau format belum ada?

Buka [feature request](https://github.com/Fueeru26/FuruArc/issues/new/choose) berisi nama engine, ekstensi, tautan dokumentasi format yang terbuka dan, bila ada, file sampel gratis. Lihat [CONTRIBUTING.md](../CONTRIBUTING.md).
