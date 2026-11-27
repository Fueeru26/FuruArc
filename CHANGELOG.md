# Changelog

All notable changes to FuruArc are listed here.

## 1.07

### Added
- **Compare Folders** (File & Folder Inspector): compares an original folder with an edited folder (by size, or also by content with a switch) and lists the changed, new and missing files with paging; the changed and new files can be copied into a chosen folder keeping their sub-folders, for example as the material of a patch; the result can be copied or saved as a report.
- **Create Project**: the Make Patch and Localize Assets projects use Compare Folders (the work folder is filled in as the edited folder), and the Pack step fills in its input folder by itself: the folder the archive was unpacked into (Translation, Localize Assets) or the copy folder of the patch (Make Patch). The projects explain that the original is unpacked twice for a patch.
- **Create Project** additions: **Duplicate project** (same kind, name, note, source archive and work folder, all steps reset), a **project report** (steps with their state, source, work folder and note) that can be copied or saved as a .txt file, and a **Back up the folder** step in the Clean Up Project before duplicates are deleted. Compare Folders has **Backup: copy all files of the edited folder** (no comparison needed). Unpack, Back up, Convert and Pack of the Clean Up Project are marked Done automatically.
- **Image Editor** additions: **Clone** tool (tap to set the source, drag to copy from it), **Fill from the surrounding colors** and **Blur / Pixelate** for a selection, **Open an image folder** with Previous / Next, an image list (50 per page) and **Save and next** (overwrites the file after one confirmation per session; the Localize Assets project opens its work folder here), and saved **text styles** (size, outline, bold, built-in font, colors).
- **Image Editor** (Game Tools tab, Support Tools, between the Text Editor and the Media Player): opens PNG, JPG, WebP and other pictures (larger than 3072 px on the longest side are reduced when opened) with a checkerboard background for transparency, zoom and pan. Tools: **Move** (drag, pinch to zoom, Fit to screen), **Select** (drag an area, then Crop, Fill with the color, Erase to transparent, Select all), **Brush** (size slider, brush or eraser), **Text** (tap the image to place text centered on that point; several lines, size, color, optional outline with its own color, bold, Sans / Serif / Mono or a font file such as .ttf / .otf) and **Picker** (tap to take a color). Transform: rotate left / right, flip horizontally / vertically, resize with an aspect-ratio lock. Colors come from a palette or an RRGGBB code. Undo and redo (the last 6 steps). Save as a new PNG, JPG or WebP file (quality slider; JPG puts transparent parts on white) or overwrite the original file after a confirmation. Leaving with unsaved changes asks first. The editor also appears in Feature Search.
- **Create Project** (Game Tools tab, below the RPGM Tools and VN Tools cards, above the Support Tools label): a menu of workflows that follow the existing tools step by step. Each project has a name, a note, an optional source archive and work folder (picked once and used to fill the tools), and a progress count. A step opens the existing tool (the engine is detected automatically), the tool's Back button returns to the project, the next unfinished step has a highlighted "Continue" button, and steps can be done in any order or skipped. Unpack, Export, Import, Pack and the converter steps are marked Done automatically when they ran without failures; the others are ticked by you. Projects can be renamed and deleted (game files and results are never deleted) and are saved in the app.
  - **Translation Project**: Unpack archive, Export text, Translate, Import text, Pack archive, Format test.
  - **Compress Game Project**: Unpack archive, Scan assets (Folder Check), Compress audio, Compress images, Compress video, Pack archive. Compressed assets may not be accepted by the game engine, so the project advises using the Compatibility Checker and keeping the original archive.
  - **Extract Assets Project**: Unpack archive, Scan assets, View assets (Media Player), Convert assets (Mixed Converter), Find duplicates, Batch rename. Unpack and Convert are marked Done automatically.
  - **Make Patch Project**: Unpack the original archive, Edit files (Text Editors), Compare with the original (changed files list, report), Pack the folder of changed files as the patch archive, Format test. Unpack and Pack are marked Done automatically.
  - **Audit / Check Game Project**: Unpack archive, Scan assets (Folder Check), Check translations, Find duplicates, Format test. The check tools only read; results can be copied or saved as a report.
  - **Clean Up Project**: Unpack archive, Scan assets, Find duplicates (delete the copies), Tidy file names (Name Optimization), Shrink assets (Mixed Converter), Pack archive. Unpack, Convert and Pack are marked Done automatically.
  - **Localize Assets Project**: Unpack archive, View assets (Media Player), Edit images (Image Editor), Convert assets (Mixed Converter), Compare with the original, Pack archive. For translating text inside game images. Unpack, Convert and Pack are marked Done automatically.

## 1.06

### Changed
- **Blue Blossom look** (new Display Style, the default) with the **Classic Floating** navigation bar (default): blue sakura artwork behind the screens (light and dark versions), translucent glass cards with a light rim and a soft blue glow, menu cards with a 62dp gradient icon badge, a thin divider, title + description and a ">" arrow, small blossoms in the card corner, a tab title with a blossom icon, and a floating glass bar where every tab shows icon + label and the active one sits in a soft blue pill. The quick actions on Home show the same blossom artwork, scaled down to the half-width tile. On the first start after the update the app switches to this look once.
- **Cards and borders**: the Game Assets cards, the cards of the Engine List and the RPGM / VN Tools cards use the same border as the menu cards (the light rim of Blue Blossom, the card border of the other styles) instead of a blue accent border; in Blue Blossom they also get the same soft glow.
- **Titles of parts**: the titles inside cards (for example "If the file already exists on export", "Default Output Folder", "Game Assets Preview Cache") use the look of the menu card titles (16sp, semi-bold). Pages that list menu cards in groups (Settings tab, App Settings, engine menus, Support Tools) separate the groups with a centered title with a line on each side, like the "Translation Area" label.
- **Label backgrounds**: the parts of a page sit on one common label background that looks like the settings cards (fill, rim and shadow or glow of the selected style): the rows of the Translation Area, every part of the Translation Editor settings, Online Translation, the Player settings, the options of the File Splitter and File Cutter, the compression options of the converters, and every section that can be hidden or shown (Media converters, the text editors, Format Test); in those sections the header and the contents share one background.
- **Button shadows**: primary and secondary buttons and the choice chips have a thin shadow (a thin white glow in dark mode).
- **Windows**: every dialog and window (confirmations, name and page dialogs, the add-game and cover dialogs, the glossary, the Media Player windows and so on) has the same border and shadow (glow in dark mode) as a label background. The width is unchanged; the height follows the content.
- **Cover cards**: the Game Assets cards, the RPGM / VN Tools cards and the Engine List cards are glass cards that hold the rounded, thinly bordered cover and a darker name pill below it (title and description in the pill for the tool cards). The card, cover and name move together in the animations.
- **Options label**: Pack, Unpack, Script and Text export / import, and Asset Encrypt / Decrypt put every setting (format choice, options, output file name, scope, encryption key, notes) in one label background titled "Options"; only the file / folder pickers stay outside. A label background always has a title (except the Value Editor lists, whose label is the page itself). The KiriKiri XOR key field is shown from the start instead of after a file is chosen.
- **Sort menu**: the sort icon opens a menu with the same border and shadow as a label background, five options visible and a scroll bar when there are more.
- **Value Editor**: the fields and their pagers share one label background. **Plugin Editor**: the plugin rows and the pager sit inside the Plugin List label background.
- **Home**: the "Clear" text of History / Recents is a trash icon and "Edit Quick Actions" is a gear icon (black in light mode, white in dark mode).
- **Label backgrounds on more pages**: Online Translation (service, quality, language, rows, results, glossary), the options, readiness report and preview of Build Readiness / Name Optimizer, the plugin list, and the option parts of Unpack / Pack / Script screens and Format Test (the option cards sit inside one label background; in Format Test the Unpack and Pack sections hold their options in the same panel).
- **Game Assets lists** (Audio, Images, Video, Text): the sort order has its own icon, to the left of the search icon, instead of a dropdown below the search field. Options: File name (A-Z), File name (Z-A), Largest first, Smallest first, Random.
- **Lists**: every list (Plugin lists and issues, folder check results, the file list of the text editors, Key Finder files, Compare Checks results, Duplicate File Finder groups, Name Optimizer previews, Bulk Rename preview, Bulk Replace matches, Format Test results) sits on a label background with 50 items per page; the page buttons appear only above 50 items. Bulk Rename has an Options part and a Preview part.
- **Text areas**: the View and the Edit text areas, including the JSON tree and text views and the CSV / INI table editors, sit on an opaque label background like the Hex Viewer. In the File Text Editor, the Edit button of the View mode opens the file in the editor at once.
- **Options parts**: a part that only holds text (the note of the script tools) has no label background; the Output Folder of the File Splitter and File Cutter sits outside the Options background. In Format Test the description is a collapsible text below the engine choice inside Options.
- **Guide**: the ‹ and › buttons of the search bar sit on a label background; the Source page lists Tyranor Next (reference for the look of the app) at the bottom. The sort menus are narrower (232dp) and the Game Assets card menu is narrower still (144dp).
- **Game Assets cards**: the round menu button is white and translucent in light mode, and its menu has the same border and shadow as the sort menu.
- **Media Player settings file**: export and import now also carry the last played audio and video, the Gallery and the Queues (with their labels), besides the settings, folders, collections, playlists and favorites. Folder access can only be restored on the same device.
- **Media Player**: the Gallery and the Audio / Video Queue have a sort icon to the left of the search icon. It sorts the list itself (so the play order follows). Gallery: file name, newest, oldest, size, random. Audio: title, artist, file name, duration, newest, oldest, size, random. Video: file name, duration, newest, oldest, size, random. Title, artist and duration are read from the file tags.
- **Translation Editor**: the translation field has no copy icon any more, so the typing area is longer, and there is more space between the field and the right edge of the label background.
- **Guide pages** (How to Use, Q&A, Troubleshooting): the header keeps its size while scrolling and has a search icon at its right end. It shows a search field with previous / next buttons; entries that contain the word open by themselves and the word is marked. How to Use and Troubleshooting entries are collapsible like Q&A (their text colour is unchanged).
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
