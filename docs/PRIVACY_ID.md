# Privasi

[English](PRIVACY.md) | **Bahasa Indonesia**

FuruArc bekerja pada file yang kamu pilih, langsung di perangkatmu. Tidak ada akun, dan aplikasi tidak memuat library analitik, pelaporan crash, maupun iklan. Library yang dipakai hanya komponen AndroidX.

## File kamu

Uji Coba Format hanya membuat file contoh kecil buatan aplikasi di cache privat aplikasi dan menghapusnya setelah selesai. File dan folder dibuka lewat Storage Access Framework Android, jadi FuruArc hanya melihat apa yang kamu pilih. File diproses di perangkat dan tidak pernah diunggah oleh aplikasi.

## Kapan aplikasi memakai jaringan

Tidak ada yang dikirim kecuali kamu memakai fitur berikut (atau, untuk cek pembaruan, membiarkannya aktif):

| Fitur | Tujuan koneksi | Yang dikirim |
| --- | --- | --- |
| Cek pembaruan | `api.github.com` (GitHub Releases `Fueeru26/FuruArc`) | Permintaan web biasa. Berjalan saat aplikasi dibuka (paling sering tiap 6 jam) dan saat kamu mengecek manual. |
| Unduh pembaruan | aset rilis di `github.com` | Hanya setelah kamu menekan "Download versi terbaru". Pemasangan selalu meminta konfirmasimu. |
| Terjemahan online | Penyedia yang kamu pilih: instance Lingva, Bilibili, Google, DeepL, Gemini, OpenAI, DeepSeek, Anthropic, Groq, OpenRouter atau endpoint khusus yang kamu isi | Teks yang kamu pilih untuk diterjemahkan dan, untuk penyedia yang membutuhkannya, API key-mu. |
| Pencarian cover (Aset Game) | VNDB, Bangumi (`bgm.tv`), Steam, Bing dan `api.nextmoe.dev` | Teks pencarian nama game, dan permintaan gambar yang kamu pilih. |

Seperti permintaan web lainnya, koneksi ini memperlihatkan alamat IP-mu ke server yang dihubungi. Tiap penyedia punya kebijakan privasi dan ketentuan sendiri; bacalah sebelum mengirim teks yang tidak ingin kamu bagikan.

## Yang disimpan di perangkatmu

- Pengaturan aplikasi, pustaka game, favorit, daftar putar, draf, glosarium dan riwayat terjemahan, di penyimpanan privat aplikasi.
- API key terjemahan online, di penyimpanan privat aplikasi. Kunci ini tidak dienkripsi oleh FuruArc, dan dikecualikan dari backup Android serta transfer antar perangkat.

Menghapus aplikasi ikut menghapus data ini.

## Izin

| Izin | Alasan |
| --- | --- |
| `INTERNET` | Fitur jaringan di atas. |
| `FOREGROUND_SERVICE` (sinkron data, pemutaran media) | Tugas panjang serta pemutaran audio dan video di latar belakang tetap berjalan dengan notifikasi. |
| `POST_NOTIFICATIONS` | Progres tugas, kontrol media dan pemberitahuan pembaruan (Android 13+). |
| `REQUEST_INSTALL_PACKAGES` | Membuka pemasang sistem untuk APK pembaruan yang kamu unduh di dalam aplikasi. |

Tidak ada izin penyimpanan yang luas.

## Perubahan dan kontak

Jika halaman ini berubah, perubahannya dicatat di [CHANGELOG](../CHANGELOG.md). Pertanyaan: buka [diskusi atau issue](https://github.com/Fueeru26/FuruArc). Untuk masalah keamanan lihat [SECURITY.md](../SECURITY.md).
