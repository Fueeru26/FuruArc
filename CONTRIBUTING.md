# Contributing to FuruArc

Thanks for your interest in FuruArc! Bug reports, engine format notes, translations and pull requests are all welcome. Please follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Reporting a bug

Open an [issue](https://github.com/Fueeru26/FuruArc/issues/new/choose) and include:

- App version (shown on the Settings > Guide page) and Android version / device.
- The feature you used and the exact steps.
- The engine and archive/file extension involved, and the error message shown in the app.
- If possible, a small sample file (never share copyrighted game files you are not allowed to distribute; a hex dump of the first bytes or the file list is often enough).

## Suggesting an engine or format

Open a "Feature request" issue with the engine name, the archive/script extensions, a link to any public format documentation, and a free sample if one exists.

## Development setup

1. Install JDK 11 and the Android SDK (platform 33).
2. Clone the repository and run `./gradlew assembleDebug`, or open the folder in Android Studio / AndroidIDE.
3. Release signing is not needed for debug builds (see the Signing note in the README for release builds). Never commit keystores or passwords (`*.jks`, `*.keystore` and `keystore.properties` are in `.gitignore`).

Toolchain: Kotlin 1.6.10, Jetpack Compose 1.1.1 with Material 2, minSdk 24. These versions are pinned on purpose (the Compose compiler must match the Kotlin version exactly), so please do not bump them in a feature pull request.

## Code guidelines

- Kotlin package: `com.furuarc`. Keep one archive format per file in `formats/` and register it in the format registry.
- Compose 1.1.1 limitations: no `WindowInsets.ime`; `PointerInputChange.consume()` needs `@OptIn(ExperimentalComposeUiApi::class)`.
- Use the shared UI components in `ui/` (`AppCard`, `AppDialog`, `AppTextField`, `PrimaryButton`, `ScreenHeader`, ...) so screens look consistent. The screen header does not scroll, so never offset content upward under it.
- All file access goes through the Storage Access Framework helpers in `storage/`; do not request broad storage permissions.
- Developer comments and commit messages are in English.

## Strings and translations

User-facing text lives in `i18n/`:

- The app UI is written in Indonesian and English (`NavStrings.kt`, `AboutStrings.kt`, and the other `*Strings.kt` files).
- Japanese, Chinese and Korean translations are in `TrData.kt`, one row per string: `English | ja | zh | ko` (a literal `\n` is a newline). The English text must match the source string exactly.
- The `docs/languages/` folder holds the full app text in all five languages as JSON files (Indonesian, English, Japanese, Chinese, Korean), handy for reviewing or proposing translation fixes.
- When you add or change an English string, add or update its translation row.

## Documentation

When you add or change an engine or format, update the table in `docs/ENGINES.md` (and `docs/ENGINES_ID.md`). Keep `README.md` and `README_ID.md` in step with each other.

## Pull requests

1. Fork the repository and create a branch from `main`.
2. Keep each pull request focused on one change.
3. Make sure `./gradlew assembleDebug` succeeds.
4. Describe what changed and how you tested it (device, Android version, engine/sample used).

By contributing you agree that your contribution is licensed under the [GPL-3.0](LICENSE) like the rest of the project.
