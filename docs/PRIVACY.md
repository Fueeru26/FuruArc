# Privacy

**English** | [Bahasa Indonesia](PRIVACY_ID.md)

FuruArc works on files you choose, on your device. It has no accounts, and it contains no analytics, crash-reporting or advertising libraries. The only libraries it uses are AndroidX components.

## Your files

Format Test only creates small generated sample files in the app's private cache and deletes them when it finishes. Files and folders are opened through Android's Storage Access Framework, so FuruArc only sees what you pick. They are processed locally and are never uploaded by the app.

## When the app uses the network

Nothing is sent unless you use one of these features (or, for the update check, leave it on):

| Feature | Where it connects | What is sent |
| --- | --- | --- |
| Update check | `api.github.com` (GitHub Releases of `Fueeru26/FuruArc`) | A normal web request. It runs when the app starts (at most every 6 hours) and when you check manually. |
| Update download | `github.com` release assets | Only after you tap "Download latest version". Installing always asks for your confirmation. |
| Online translation | The provider you select: Lingva instances, Bilibili, Google, DeepL, Gemini, OpenAI, DeepSeek, Anthropic, Groq, OpenRouter or a custom endpoint you enter | The text you choose to translate and, for providers that need one, your API key. |
| Cover search (Game Assets) | VNDB, Bangumi (`bgm.tv`), Steam, Bing and `api.nextmoe.dev` | The search text for the game name, and a request for the image you pick. |

Like any web request, these reveal your IP address to the server you contact. Each provider has its own privacy policy and terms; read them before sending text you do not want to share.

## What is stored on your device

- App settings, your game library, favorites, playlists, drafts, glossaries and translation history, in the app's private storage.
- Your online translation API key, in the app's private storage. It is not encrypted by FuruArc, and it is excluded from Android backup and device-to-device transfer.

Uninstalling the app removes this data.

## Permissions

| Permission | Why |
| --- | --- |
| `INTERNET` | The network features above. |
| `FOREGROUND_SERVICE` (data sync, media playback) | Long jobs and background audio and video playback keep running with a notification. |
| `POST_NOTIFICATIONS` | Job progress, media controls and update notices (Android 13+). |
| `REQUEST_INSTALL_PACKAGES` | Opening the system installer for an update APK you downloaded in the app. |

There is no broad storage permission.

## Changes and contact

If this page changes, the change is listed in the [CHANGELOG](../CHANGELOG.md). Questions: open a [discussion or issue](https://github.com/Fueeru26/FuruArc). For security problems see [SECURITY.md](../SECURITY.md).
