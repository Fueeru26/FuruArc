# FAQ and troubleshooting

**English** | [Bahasa Indonesia](FAQ_ID.md)

## General

**Which Android versions are supported?**
Android 7.0 (API 24) and newer.

**Does FuruArc need access to all my files?**
No. It uses Android's Storage Access Framework, so you pick the files and folders it may open. No broad storage permission is requested.

**Are my files or text uploaded anywhere?**
Files are processed on your device. Text leaves the device only when you use online translation, and the app only connects to the internet for the features listed in [PRIVACY.md](PRIVACY.md).

**Can I use it on any game?**
FuruArc is a general-purpose modding and localization utility. Use it only with games and files you own or are allowed to modify.

## Archives

**The file names look garbled after unpacking.**
Change **File Name Encoding** (Auto, Shift-JIS, UTF-8, GBK, Big5 or EUC-KR) in the options and try again.

**FuruArc does not recognise my archive.**
Open the Engine List and choose the engine yourself. Formats without magic bytes are only recognised by their structure, so a damaged or unusual file may not be detected. **File Inspection** (in the File & Folder Inspector) shows the format, checksum and corrupt files.

**The archive is encrypted or the file list is empty.**
Run the **Key Finder** on the game folder. Some engines have an option for the key: Siglus (exe key), CatSystem 2 (index password), EntisGLS NOA (password). For the supported versions of each engine see [ENGINES.md](ENGINES.md).

**Pack Archive is missing for my engine.**
Some formats are read-only (for example HexenHaus, and QLIE). The table in [ENGINES.md](ENGINES.md) shows what each engine supports.

**The game does not start after I pack or edit files.**
Keep the original file names and the folder structure. For names with non-Latin characters, **File Name Optimizer** makes them safe and updates the references. Packing for engines that need a per-game cipher may not produce an archive the game accepts.

## Translating a game

**What is the usual order?**
1. Unpack the archive (VN Tools > Unpack Archive).
2. Export the text (Export Script Text, or Data Translation for RPG Maker).
3. Translate it in the **Translation Editor** (drafts, glossary, online translation).
4. Write the result with **Update Translation**.
5. Put it back with Import Script Text, then pack the archive again or copy the files into the game.

**Which scripts can be exported?**
See the "Script text" column in [ENGINES.md](ENGINES.md).

**My control codes such as `\C[2]` or tags such as `[r]` broke.**
The Translation Editor has a **control code check** that warns when codes differ from the original, and **Translation Scan** (File & Folder Inspector) finds control codes, long lines and empty entries.

**Online translation fails.**
Use **Test connection** in the translation window. Lingva and Bilibili are free and need no key; other providers need your own API key with a working quota. Public Lingva servers are sometimes down, so try another one.

**How can I check that packing and unpacking work on my device?**
Open **Format Test** in the File & Folder Inspector, choose the engine, upload an archive of that engine and tap Start test. It extracts one random file, packs it again and compares the new archive with your original. Your own file is only read, never changed, and no output folder is used.

## Running and updating

**A long job stopped when I closed the app.**
Turn on **Run in background** in Settings. On Android 13+ the app asks for the notification permission when you enable it.

**How do updates work?**
The app checks the latest GitHub release. Nothing is downloaded until you tap "Download latest version", and Android always asks you to confirm the installation. Each release lists a SHA-256 checksum of the APK.

**The installer says the app was not installed, or the update will not install.**
Allow installing from this source when Android asks. If you installed a debug build or a build signed with another key, Android refuses an update signed with the release key: uninstall that build first (export your settings and lists in Media Settings if you want to keep them).

**Version 1.01 installed as a second app.**
The application ID changed from `com.example.furuarc` to `com.furuarc` in 1.01, so data from older builds is not carried over.

## Reporting a problem

Open an [issue](https://github.com/Fueeru26/FuruArc/issues/new/choose) and include the app and Android versions (the Format Test report copies both), the exact steps, the engine and file extension, and the error shown in the app. Do not share copyrighted game files; a file list or the first bytes in hex are usually enough. See [CONTRIBUTING.md](../CONTRIBUTING.md).
