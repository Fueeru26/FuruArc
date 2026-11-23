# Changelog

All notable changes to FuruArc are listed here.

## 1.05

### Added
- **Format Test** In the File & Folder Inspector: packs generated sample files with every format that can be packed, unpacks them again on the device and compares names and contents. Each result is passed, warning (the round trip works but automatic detection picks another format), failed (with the reason) or skipped (formats that need files from a real game: AGE and Siglus PCK). Filter All / ! / ✕ / ✓, and a report (copy or .txt) that includes the app version, Android version and device. Texts in Indonesian, English, Japanese, Chinese and Korean; the Guide and the feature search know the new tool.
  - Edge samples for every format: an empty file, a file of exactly 1 MiB, Japanese and spaced names, subfolders and 300 files. A problem found with them is a warning, because it can be a known limit of the format.
  - The unpacked files are packed again and the new archive is checked (contents and, as a warning, byte equality).
  - Automatic detection lists every format that accepts the archive.
  - **Real archives**: small archives from real games placed in `assets/formattest` with a `.golden` manifest are unpacked and the SHA-256 of each file is compared, which proves that a format matches the game and not only itself.
  - Codec tests (LZMA, LZ4, LZSS) against reference data, and script text export/import tests (the script must not change when the texts are unchanged).
  - The test also runs on the JVM in GitHub Actions (`testDebugUnitTest`), so a broken format is found before a release.

### Changed
- The **File Inspection** menu is now **File & Folder Inspector**. Its single-file tool, formerly "Archive and Script Inspection", is now **File Inspection** (Pemeriksaan File). The tools are listed in this order: File Inspection, Folder Inspection, Compare Files, Hex Viewer, Format Test, Duplicate File Finder, Translation Scan.
- Guide, hints and report titles use the new names.

### Fixed
- QLIE: the Pack Archive menu is no longer offered for QLIE. Packing QLIE is not supported, so the option used to open and then fail with "Pack QLIE belum didukung". QLIE archives can still be listed and unpacked (plain entries only).
- Unity: packing UnityFS version 6 with a Unity version of 2019.4 or newer (the default) wrote a header that was not aligned to 16 bytes, so the bundle could not be read back. Packing now uses the same alignment rule as reading. Found by the new Format Test.

## 1.04

### Added
- **Three new visual novel engines** (24 in total), each with its own cover card in the engine list:
  - **AGE (Eushully)**: list and extract `.alf` archives. An ALF has no table of its own, so choose the **Index file** (`SYS4INI.BIN`, `SYS5INI.BIN` or `.AAI` from the same game folder) in the options; the ALF name is taken from the opened file. Several ALF files can be unpacked in one batch with the same index. **Pack** rebuilds one ALF from a folder (same file names as in the index) and saves a new `SYS4INI.BIN` next to it; the original index is chosen in the options and its header and the other ALF entries are kept.
  - **Shiina Rio**: list and extract WARC 1.1 archives (YPK/zlib entries are unpacked). Later WARC versions use a per-title encryption scheme and are reported as not supported. **Pack** writes a WARC 1.1 archive with stored entries (names up to 15 bytes).
  - **Unity**: open UnityFS asset bundles (`.unity3d`, `.bundle`), with LZMA and LZ4 block compression, list the contained nodes (`CAB-*`, `.resS`, ...) and extract them. Packing writes an uncompressed UnityFS bundle (format version 6 or 7 and Unity version string are set in the options). Encrypted bundles and the objects inside serialized files are not processed.
- Option fields can now pick a file (used by the AGE index file).
- Folder statistics recognise AGE (Eushully) and Shiina Rio files.

### Changed
- Engine List: the Unpack / Pack Archive menus now show the format, for example "Unpack Archive (PFS)".

### Fixed
- The release notes in the "New version available" window are no longer cut off: the full text is shown in a larger scrollable area.
- Scroll positions are kept when returning from a sub-screen to the Engine List and to other list screens (Guide, Game Assets lists, Library, File Check, File Tools, Name Optimizer, Plugin tools, Unpack / Pack).

## 1.03

### Added
- **Media Player** (Support Tools, placed above the Media Asset Converter, with a new window-and-play icon): Image Gallery, Audio Player and Video Player. The Audio Player is described as "Play audio, manage playlists, shuffle, repeat and adjust speed".
  - Audio and Video have four toggle buttons: **Player**, **Queue**, **Folders**, **Playlists**.
  - **Folders**: add folders and see only their names; a "This folder or subfolders too" toggle controls the scan. Single files can be added too. Folder three-dot menu: Load into Queue/Gallery and Add to Queue/Gallery, plus **View content** (search, 20 per page and Close stay fixed while only the list scrolls) and **Empty**.
  - **Queue**: cover, title, artist, album and duration read from the file tags; move items up or down with arrows; three-dot window with Song info, Play next, Remove from queue, Add to Favorites, Add to playlist, Share and Open in File Inspection; **Search**; **selection mode** (Favorites, Playlist, Delete, with confirmation before deleting); pagination (50 per page, adjustable). Emptying the Queue also empties the Player.
  - **Playlists** in a 3-column card grid (rename, delete, View content, Empty) with a built-in **Favorites** list that cannot be renamed or deleted.
  - **Apply to Queue / Gallery**: Folders, Favorites, Collections and Playlists load directly into an empty Queue/Gallery; otherwise a window offers **Load** (with a direct overwrite warning) or **Add**.
  - **Player**: cover, favorite, info, add to playlist, repeat, shuffle, speed, A-B loop, sleep timer, system equalizer, **position bookmarks** and background play.
  - **Audio Player**: media-style notification (cover, title, artist, seek bar, Favorite / Previous / Play-Pause / Next / Close) and synced lyrics (.lrc).
  - **Video Player**: Rotate Screen, Fullscreen, double-tap skip, swipe for brightness and volume, **picture-in-picture**, and **subtitles** (SRT, VTT, ASS/SSA; icon between the playlist and three-dot icons). Fullscreen hides the status and navigation bars (swipe from the edge to show them) and Back returns to portrait; swiping from the screen edges does not change brightness or volume. The position is kept when switching between the Player, Queue, Folders and Playlists tabs, and leaving and re-entering the player keeps the position without starting playback by itself.
  - Subtitles and lyrics have timing (±0.5 s), text size and automatic loading of a same-name file.
  - **Image Gallery**: Gallery, Folders and Collections; list or grid view (60 per page, adjustable); Search; selection mode; Favorites and Collections work like playlists. In grid view, thumbnails change immediately when switching pages or deleting images.
  - Folders, playlists/collections and favorites are kept separately for audio, video and images.
  - **Remember last position** for audio and video, set separately for each (off by default). The last played audio / video and its position are restored when the app is opened again.
- **Media Settings** page: audio, video, list and gallery options (pause on headphone unplug, audio focus for calls, restore last queue, skip small audio, skip seconds, default video orientation, sort order, tag titles, covers, page sizes, grid columns, image size, viewer background, **Arrange the list** with Drag mode or Up-down arrows, **Subtitles & lyrics** options and more), with the Lists section first and larger section titles. Settings can be exported and imported together with all folders, playlists, collections and favorites.
- **Open with...**: FuruArc can open files from file managers and the Share / Send to menu. Media opens temporarily in Media Player without overwriting the stored Gallery, Player or Queue; text and archive formats open in the matching feature, and unknown formats directly in File Inspection. The current screen is replaced automatically. Can be switched off in System settings.
- **Media Player icon in Game Assets** viewers to open an asset in Media Player.
- **Theme Schedule** mode (dark between chosen hours) and an automatic **Seasonal** display style.
- Settings switches for **Animations** and **Haptic feedback**.

### Changed
- "Confirm exit from Editor" is now on by default.
- About > Developer: contact and donation links are on one line each; the donation link notes were removed.
- Quick Actions default: Media Player replaces Text Editor in the third place (existing custom lists are not changed).
- The app now handles screen rotation itself instead of restarting.

### Fixed
- In the Audio and Video previews of Game Assets, tapping the seek bar moved the thumb and time but not the media. Seeking now happens once after the tap and is frame-accurate where supported (API 26+).

## 1.02

### Added
- **Display Style** (Settings > Display, top): Classic (the previous look), Minimal, Soft, Gradient and Solid, plus themed styles Sakura, Ocean, Forest, Sunset, Neon and Paper. Each has its own colours, corner radius and card style.
- **Navigation Bar Style** (Settings > Display, below Display Style): Classic, Floating and Floating Icons (larger icons only), both drawn over the content with no bar behind them, Icons Only, Sliding Pill, Top Line and Floating Button (a round button that fans out the tabs).
- **Screen transitions**: all screen changes use one soft cross-fade with a tiny upward slide.
- **Shrinking headers**: page headers get smaller while scrolling down and return when scrolling up.
- **Illustrated empty states** for empty lists (Game Assets, favorites, quick access, search without results).
- **Press animations**: buttons, cards, chips, tool tiles, dropdowns and navigation tabs shrink slightly with a springy bounce when pressed.

### Changed
- Corner radii of cards, buttons and icons now follow the selected Display Style.

## 1.01

### Added
- **System notification for updates**: when the automatic update check finds a new version, a notification is posted once per version. It uses the same setting and notification permission as "Run in background"; tapping it opens the release page.
- **English text for engine and format messages** (errors, status and hint messages that were previously shown only in Indonesian), and about 150 more Japanese / Chinese / Korean translations.
- `languages/` folder with the full app text in five languages as JSON files.

### Changed
- **In-app update download**: the update window now has a "Download latest version" button (above "Close", no scrolling) that downloads the APK from the GitHub release with a progress window and a progress notification, then shows "Install app" to open the system installer. The update windows are also wider and show release notes without Markdown symbols. Adds the `REQUEST_INSTALL_PACKAGES` permission.
- **Find features by file** (formerly "Start from file"): choosing a feature now loads the picked file into it automatically, so it no longer has to be picked again. Only features that can take a file directly are suggested.
- The update window now says "App updates are not available right now." when there is no release yet.
- Translation lookup is more accurate: fragments with surrounding spaces (for example `"Folder: "`) are now translated, and the most specific template wins when several match.
- Kotlin package moved from `com.example.furuarc` to `com.furuarc`, and the application ID changed to `com.furuarc`. It installs as a new app; data from earlier builds is not carried over.
- Release signing is no longer configured with fixed credentials: it reads `ANDROID_KEYSTORE_*` / `ANDROID_KEY_*` environment variables or an untracked `keystore.properties`. Debug builds are unaffected.

### Fixed
- Update check now also detects GitHub pre-releases (the "latest release" endpoint ignores them, so a pre-release was reported as "no update available").
- Text clipped under the fixed header, for example the "Translation Service" label in Online Translation.

## 1.00

First release
