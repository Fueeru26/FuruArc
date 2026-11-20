# Changelog

All notable changes to FuruArc are listed here.

## 1.01

### Added
- **System notification for updates**: when the automatic update check finds a new version, a notification is posted once per version. It uses the same setting and notification permission as "Run in background"; tapping it opens the release page.
- **English text for engine and format messages** (errors, status and hint messages that were previously shown only in Indonesian), and about 150 more Japanese / Chinese / Korean translations.
- `languages/` folder with the full app text in five languages as JSON files.

### Changed
- **Find features by file** (formerly "Start from file"): choosing a feature now loads the picked file into it automatically, so it no longer has to be picked again. Only features that can take a file directly are suggested.
- The update window now says "App updates are not available right now." when there is no release yet.
- Translation lookup is more accurate: fragments with surrounding spaces (for example `"Folder: "`) are now translated, and the most specific template wins when several match.
- Kotlin package moved from `com.example.furuarc` to `com.furuarc`, and the application ID changed to `com.furuarc`. It installs as a new app; data from earlier builds is not carried over.
- Release signing is no longer configured with fixed credentials: it reads `ANDROID_KEYSTORE_*` / `ANDROID_KEY_*` environment variables or an untracked `keystore.properties`. Debug builds are unaffected.

### Fixed
- Text clipped under the fixed header, for example the "Translation Service" label in Online Translation.

## 1.00

First release
