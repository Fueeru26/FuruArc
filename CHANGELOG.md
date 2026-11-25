# Changelog

All notable changes to FuruArc are listed here.

## 1.06

### Changed
- **Blue Blossom look** (new Display Style, the default) with the **Classic Floating** navigation bar (default): blue sakura artwork behind the screens (light and dark versions), translucent glass cards with a light rim and a soft blue glow, menu cards with a 62dp gradient icon badge, a thin divider, title + description and a ">" arrow, small blossoms in the card corner, a tab title with a blossom icon, and a floating glass bar where every tab shows icon + label and the active one sits in a soft blue pill. The quick actions on Home show the same blossom artwork, scaled down to the half-width tile. On the first start after the update the app switches to this look once.
- **Cards and borders**: the Game Assets cards, the cards of the Engine List and the RPGM / VN Tools cards use the same border as the menu cards (the light rim of Blue Blossom, the card border of the other styles) instead of a blue accent border; in Blue Blossom they also get the same soft glow.
- **Titles of parts**: the titles inside cards (for example "If the file already exists on export", "Default Output Folder", "Game Assets Preview Cache") use the look of the menu card titles (16sp, semi-bold). Pages with several parts (Settings tab, App Settings, Editor Settings of the Translation Editor, Online Translation, engine menus, Support Tools) separate them with a centered title with a line on each side, like the "Translation Area" label.
- **Translation Editor**: the Original Text / translation rows have more space between their content and the left edge of their label background.
- **Display Style** offers **Blue Blossom** (default), **Classic**, **Minimal** and **Soft**. A saved style that no longer exists switches to Blue Blossom on the next start.
- **Navigation Bar Style** offers **Classic Floating** (default), **Classic**, **Floating**, **Floating Icons** and **Floating Button**. A saved bar that no longer exists switches to Classic Floating.
- **Theme Color** is hidden while Blue Blossom is selected, because that style has its own colours (the same idea as the hour pickers that only show for Theme Mode > Schedule). It shows again for Classic, Minimal and Soft.
- **Gradient look**: buttons, icon badges (menu cards, picker cards, quick tiles, folder and media badges) and selected chips use a two-tone gradient derived from the accent color, with an icon/text color that stays readable on it. Secondary buttons and unselected chips use a soft version of the same gradient. Disabled buttons stay flat. The gradient starts at the accent color itself and ends in a deeper shade, so its top-left corner is never lighter than the accent.
- **More colorful light mode**: the app background and card borders are tinted by the theme color, while cards stay near-white with a soft shadow, so menu cards clearly stand out from the background. In dark mode cards are clearly lighter than the background. Input controls (dropdown, dropdown menu, text field) use only a light tint of the card color and a softer border, so they no longer look heavy.
- Cover placeholders (album art, thumbnails) keep their soft flat color.
- **Card shadows in dark mode too**: Classic and Soft cards cast a grey-black shadow in light mode and the mirrored version in dark mode, a soft white glow below and around the card (the dark background is almost black so it stays visible). Game Assets cards and the Engine List cards (cover, border, shadow and name) have the same shadow and play the press, scroll jelly and entrance animations as one piece.
- **Screen animations** (modelled on the motion code of Tyranor Next; all follow the Animations switch): switching between the four main tabs moves the whole page sideways with a spring (in the direction of the tab order, 6% depth scale and fade); opening a menu or page makes it rise from the bottom over the full screen height while the previous page recedes; going back sinks the page down again. Cards, buttons and chips shrink quickly when pressed and bounce back with overshoot (cards to 92%). While a list scrolls fast its cards stretch a little along the scroll direction and spring back when it stops (scroll jelly). Cards of a new screen pop in one after the other from top to bottom (staggered entrance).
- **Translation Editor**: in the translation area every row (Original Text and the translation field) sits on its own label background (rounded panel with a thin rim, in the card colours of the selected style) so rows are easy to tell apart on the blossom artwork.
- Guide (Settings, Translation Editor), README, README_ID and the language files (`docs/languages`, Settings guide entry) describe the new options.

## 1.05

### Added
- **Format Test** in the File & Folder Inspector, now tested with a real archive of yours:
  - Choose an Engine (Format) in the dropdown (VN engines first, then RPGM XP, VX and VX Ace; formats that cannot be packed yet are not listed) and upload an archive. The file must be exactly the chosen format, otherwise a red error is shown (the format is chosen instead of detected, because detection does not work for some formats).
  - Once the file matches, two collapsible sections appear, with the settings of that format: **Unpack Archive** (including the maximum size of the extracted file, in KB or MB) and **Pack Archive**.
  - The test extracts one random file (not larger than the limit), packs it into a new archive, unpacks that new archive and compares it with your original archive: file content by SHA-256, header, format detection (and every other format that accepts it) and size. A format that needs at least two files (Circus DAT) uses two.
  - Formats that can only be read with settings (the AGE index file and ALF name, for example) work too: Unpack Archive is shown as soon as a file is chosen, a red message explains what is missing, and the file is checked again when a setting changes. Settings with the same name are shared with Pack Archive. AGE can only be packed with every file of the ALF, so it is always tested with all files, compared byte for byte, with no size limit (the extracted files and the new archive are stored in the cache folder while the test runs, and "Save samples" is only offered for small tests).
  - Files to test (in Unpack Archive): one random file or the file whose name you type, several random files, all files up to the size limit, or all files followed by a byte-for-byte comparison with the original archive, which reports whether the new archive is identical or where the first difference lies (header / index or the data of a file). The random seed is shown in the results; entering it again repeats the same test.
  - There is no output folder. The result is recorded under "Format test results" and can be copied or exported to .txt. "Save samples to a folder" keeps the extracted files, the new archive and the report for analysis.
  - File Inspection has a "Test this format" button for archives whose format can be packed; it opens the Format Test with the archive and engine already chosen.
  - The GitHub Actions jobs cache Gradle and try a download again when Maven Central answers "429 Too Many Requests".
  - Automatic tests in GitHub Actions (`testDebugUnitTest`) pack generated samples with every format (including edge samples), and check the LZMA, LZ4 and LZSS codecs and script text export/import, so a broken format is found before a release.
- **View mode** in the Text Editor (Plain Text and every Structured Text editor: JSON, CSV / TSV, INI / CFG, XML, Markdown): upload a text file and its content appears below the upload field, read-only, in the same look as "View contents as text" in File Inspection (pages of 50 KB; for a file above 4 MB the first 1 MB is shown). Above it: an **Encoding** choice (the same setting as "Encoding when opening" of the editor; a chosen encoding is also used for files that look binary, such as UTF-16 without BOM) and a **Find** button (match case or regex, Next and ‹ between matches, the match is highlighted and the list scrolls to it, also on another page of a big file). Under it, two buttons: **Edit** moves the file to the Edit mode (already uploaded there) and **Copy** copies the content (for a very long text, the page that is shown).

### Changed
- The **File Inspection** menu is now **File & Folder Inspector**. Its single-file tool, formerly "Archive and Script Inspection", is now **File Inspection** (Pemeriksaan File). The tools are listed in this order: File Inspection, Folder Inspection, Compare Files, Hex Viewer, Format Test, Duplicate File Finder, Translation Scan.
- Guide, hints and report titles use the new names.
- The Text Editor toggle **Create File / Upload File** is now **Create / Edit / View** (Indonesian: Buat / Edit / Lihat) in Plain Text and in every Structured Text editor; Guide and hints use the new names.
- **Open with... / Share**: a `.txt` file opens in View of Plain Text; a registered structured format (JSON, CSV / TSV, INI / CFG and similar, XML, Markdown) opens in View of its Structured Text editor; a file of an unknown format that is text opens in View of Plain Text. Other files still go to the matching feature or to File Inspection. The setting text "Show in Open with for all files" describes the new behaviour.

### Fixed
- Shiina Rio (WARC): the file list of an archive with a single file could be read with the wrong index decoding, giving garbled file names. Both decodings are now checked against the position of the index. Found by the Format Test.
- QLIE: the Pack Archive menu is no longer offered for QLIE. Packing QLIE is not supported, so the option used to open and then fail with "Pack QLIE belum didukung". QLIE archives can still be listed and unpacked (plain entries only).
- Unity: packing UnityFS version 6 with a Unity version of 2019.4 or newer (the default) wrote a header that was not aligned to 16 bytes, so the bundle could not be read back. Packing now uses the same alignment rule as reading. Found by the Format Test.

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
