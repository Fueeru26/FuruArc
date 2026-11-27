# FAQ dan pemecahan masalah

[English](FAQ.md) | **Bahasa Indonesia**

## Umum

**Android versi berapa yang didukung?**
Android 7.0 (API 24) ke atas.

**Apakah FuruArc butuh akses ke semua file?**
Tidak. FuruArc memakai Storage Access Framework Android, jadi kamu yang memilih file dan folder yang boleh dibuka. Izin penyimpanan yang luas tidak diminta.

**Apakah file atau teks saya diunggah ke suatu tempat?**
File diproses di perangkatmu. Teks hanya keluar dari perangkat saat kamu memakai terjemahan online, dan aplikasi hanya terhubung ke internet untuk fitur yang tercantum di [PRIVACY_ID.md](PRIVACY_ID.md).

**Boleh dipakai untuk game apa saja?**
FuruArc adalah utilitas modding dan lokalisasi serbaguna. Pakai hanya untuk game dan file milikmu sendiri atau yang boleh kamu ubah.

## Arsip

**Nama file terlihat acak setelah dibongkar.**
Ganti **Encoding Nama File** (Otomatis, Shift-JIS, UTF-8, GBK, Big5 atau EUC-KR) di opsi lalu coba lagi.

**FuruArc tidak mengenali arsip saya.**
Buka Daftar Engine dan pilih engine-nya sendiri. Format tanpa magic bytes hanya dikenali dari strukturnya, jadi file rusak atau tidak biasa bisa gagal terdeteksi. **Pemeriksaan File** (di Pemeriksa File dan Folder) menampilkan format, checksum dan file rusak.

**Arsip terenkripsi atau daftar filenya kosong.**
Jalankan **Pencari Kunci** pada folder game. Beberapa engine punya opsi kunci: Siglus (kunci exe), CatSystem 2 (sandi indeks), EntisGLS NOA (sandi). Versi yang didukung tiap engine ada di [ENGINES_ID.md](ENGINES_ID.md).

**Menu Kemas Arsip tidak ada untuk engine saya.**
Sebagian format hanya bisa dibaca (misalnya HexenHaus dan QLIE). Tabel di [ENGINES_ID.md](ENGINES_ID.md) menunjukkan kemampuan tiap engine.

**Game tidak jalan setelah saya mengemas atau mengedit file.**
Pertahankan nama file asli dan struktur foldernya. Untuk nama dengan karakter non-Latin, **Pengoptimal Nama File** membuatnya aman dan memperbarui referensinya. Mengemas untuk engine yang butuh cipher per game bisa menghasilkan arsip yang tidak diterima game.

## Menerjemahkan game

**Urutan yang umum?**
1. Bongkar arsip (Alat VN > Bongkar Arsip).
2. Ekspor teksnya (Ekspor Teks Skrip, atau Terjemahan Data untuk RPG Maker).
3. Terjemahkan di **Editor Terjemahan** (draf, glosarium, terjemahan online).
4. Tulis hasilnya dengan **Perbarui Terjemahan**.
5. Masukkan kembali dengan Impor Teks Skrip, lalu kemas arsip lagi atau salin file ke dalam game.

**Skrip apa saja yang bisa diekspor?**
Lihat kolom "Teks skrip" di [ENGINES_ID.md](ENGINES_ID.md).

**Kode kontrol seperti `\C[2]` atau tag seperti `[r]` rusak.**
Editor Terjemahan punya **cek kode kontrol** yang memperingatkan bila kode berbeda dari teks asli, dan **Pemindaian Terjemahan** (Pemeriksa File dan Folder) menemukan kode kontrol, baris panjang dan entri kosong.

**Terjemahan online gagal.**
Pakai **Uji koneksi** di jendela terjemahan. Lingva dan Bilibili gratis dan tanpa kunci; penyedia lain butuh API key milikmu dengan kuota yang aktif. Server Lingva publik kadang mati, coba server lain.

**Bagaimana cara memastikan kemas dan bongkar berfungsi di perangkat saya?**
Buka **Uji Coba Format** di Pemeriksa File dan Folder, pilih engine, unggah arsip dari engine itu lalu tekan Mulai uji coba. Aplikasi mengekstrak 1 file acak, mengemasnya lagi, lalu membandingkan arsip baru dengan arsip aslimu. File milikmu hanya dibaca, tidak diubah, dan tidak ada folder output.

## Menjalankan dan memperbarui

**Tugas panjang berhenti saat aplikasi ditutup.**
Aktifkan **Proses latar belakang** di Pengaturan. Di Android 13+ aplikasi meminta izin notifikasi saat kamu mengaktifkannya.

**Bagaimana pembaruan bekerja?**
Aplikasi mengecek rilis GitHub terbaru. Tidak ada yang diunduh sebelum kamu menekan "Download versi terbaru", dan Android selalu meminta konfirmasi pemasangan.

**Pemasang bilang aplikasi tidak terpasang, atau pembaruan tidak mau dipasang.**
Izinkan pemasangan dari sumber ini saat Android bertanya. Jika kamu memasang build debug atau build yang ditandatangani kunci lain, Android menolak pembaruan yang ditandatangani kunci rilis: hapus build itu dulu (ekspor pengaturan dan daftarmu di Pengaturan Media jika ingin menyimpannya).

**Versi 1.01 terpasang sebagai aplikasi kedua.**
ID aplikasi berubah dari `com.example.furuarc` menjadi `com.furuarc` di versi 1.01, sehingga data dari build lama tidak ikut terbawa.

## Melaporkan masalah

Buka [issue](https://github.com/Fueeru26/FuruArc/issues/new/choose) dan sertakan versi aplikasi dan Android (laporan Uji Coba Format menyalin keduanya), langkah yang tepat, engine dan ekstensi file, serta error yang tampil di aplikasi. Jangan membagikan file game berhak cipta; daftar file atau byte awal dalam hex biasanya cukup. Lihat [CONTRIBUTING.md](../CONTRIBUTING.md).
