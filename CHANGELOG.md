# Changelog

All notable changes to FuruArc are listed here.

## 1.02

### Added
- **Display Style** (Settings > Display, top): Classic (the previous look), Minimal, Soft, Gradient and Solid, plus themed styles Sakura, Ocean, Forest, Sunset, Neon and Paper. Each has its own colours, corner radius and card style.
- **Navigation Bar Style** (Settings > Display, below Display Style): Classic, Floating, Floating Icons (larger icons only, in the same floating bar), Icons Only, Sliding Pill, Top Line and Floating Button (a round button that fans out the tabs).
- **Screen transitions**: all screen changes use one soft cross-fade.
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
