# Supported engines

**English** | [Bahasa Indonesia](ENGINES_ID.md)

What FuruArc can do for each engine in version 1.05. The in-app **Engine List** (VN Tools and RPGM Tools) always shows the exact options of the build you have installed. Format behavior was checked against the open-source tools [GARbro](https://github.com/morkt/GARbro) and [msg-tool](https://github.com/lifegpc/msg-tool).

Legend: ✔ supported, – not supported.

## Visual novels (24 engines)

**Archive** = list and extract (View Archive Contents, Unpack Archive). **Pack** = create an archive from a folder. **Script text** = Export / Import Script Text (CSV, TXT, JSON or JSON VNTP) for the script type shown.

| Engine | Archive extensions | Archive | Pack | Script text | Notes |
| --- | --- | :---: | :---: | --- | --- |
| Artemis | `.pfs` | ✔ pf2, pf6, pf8 | ✔ pf2, pf6, pf8 | ✔ `.txt`, `.ast` | |
| Kirikiri | `.xp3` | ✔ v1, v2 | ✔ | ✔ `.ks`, `.scn` (PSB) | No per-game cipher yet, only an optional 1-byte XOR. Optional deflate when packing. |
| Ren'Py | `.rpa` | ✔ RPA-2.0, 3.0, 3.2 | ✔ RPA-3.0 (2.0 selectable) | ✔ `.rpy` | `.rpyc` files can be decompiled to `.rpy`; screens are written as comments. |
| BGI / Ethornell | `.arc` | ✔ PackFile, ARC20 | ✔ ARC20 (PackFile selectable) | ✔ extension-less scripts | DSC and CompressedBG data inside is extracted as is. |
| Softpal ADV System | `.pac` | ✔ classic, Amuse Craft | ✔ | – | Script files starting with `$` are encrypted and extracted as is. |
| WillPlus / AdvHD | `.arc` | ✔ | ✔ | ✔ `.ws2` | |
| NScripter | `.sar`, `.nsa` | ✔ SAR, NSA type 0 | ✔ SAR, uncompressed NSA | ✔ `0.txt` | `nscript.dat` can be decoded to `0.txt` and encoded back. Compressed NSA (SPB, LZSS, NBZ) is not supported. |
| Tyrano Script | `.nw` (`package.nw`) | ✔ ZIP | ✔ ZIP (deflate) | ✔ `.ks` | `app.asar` (Electron) and packages attached inside an `.exe` are not supported. |
| FVP / Favorite | `.bin` | ✔ | ✔ | ✔ `.hcb` | |
| Adv32 / Circus | `.pck`, `.dat`, `.crm` | ✔ | ✔ | ✔ `.mes` | A `.dat` needs at least 2 files to pack. Files inside are extracted as is. |
| Silky Engine | `.arc` | ✔ | ✔ | ✔ `.mes` | The name XOR key is guessed from the padding. |
| CatSystem 2 | `.int` | ✔ | ✔ | ✔ `.cst` | An encrypted index needs the game's password in the options. |
| Siglus Engine | `.pck`, `Gameexe.dat`, voice `.ovk` | ✔ | ✔ | ✔ scene `.dat` | An exe key can be entered in the options (Scene.pck is detected automatically; use Key Finder for an encrypted `Gameexe.dat`). |
| QLIE | `.pack` | ✔ plain entries only | – | ✔ `.s` | Compressed or encrypted entries need the game key and are not supported. |
| Yu-Ris | `.ypf` | ✔ versions 234 to 500 | ✔ versions 480 to 500 | ✔ `.ybn` | |
| Escu:de | `.bin` | ✔ | ✔ | ✔ `.bin` | |
| Yaneurao Itufuru | `.scd` | ✔ | ✔ | ✔ extension-less scripts | File names up to 12 bytes when packing. |
| EntisGLS | `.noa` | ✔ Raw, ERISA, BSHF | ✔ Raw or BSHF (password) | ✔ `.csx` | |
| NeXAS | `.pac` | ✔ raw, LZSS, zlib, zstd | ✔ raw or zlib | ✔ `.bin` | Packing is not yet tested on real game archives. |
| Majiro | `.arc` | ✔ v1, v2, v3 | ✔ | – | |
| AGE / Eushully | `.alf` | ✔ with an index file | ✔ one ALF plus a new `SYS4INI.BIN` | – | Choose the **Index file** (`SYS4INI.BIN`, `SYS5INI.BIN` or `.AAI`) in the options. Several ALF files can be unpacked in one batch. |
| Shiina Rio | `.war` | ✔ WARC 1.1 | ✔ WARC 1.1 (stored, names up to 15 bytes) | – | Later WARC versions use a per-title scheme and are not supported. |
| Unity | `.unity3d`, `.bundle` | ✔ UnityFS (LZMA, LZ4) | ✔ uncompressed UnityFS v6 or v7 | – | Container level only: encrypted bundles and the objects inside serialized files are not processed. |
| HexenHaus | `.arc`, ODIO, WAG | ✔ | – | – | Read-only. |

Notes:

- Archives without magic bytes (Circus, FVP, WillPlus, Silky, NScripter and others) are recognised by their structure. If detection fails, pick the engine yourself in the Engine List.
- **Format Test** (File & Folder Inspector) packs sample files with every packable format on your device and unpacks them again, which shows quickly whether a format works on your phone. It also checks edge samples, repacking, codecs, script text and small real archives (`assets/formattest`).
- The **Key Finder** looks for encryption keys in a game folder and lists the files that use them.
- File names inside archives can be read as Auto, Shift-JIS, UTF-8, GBK, Big5 or EUC-KR (**File Name Encoding**).
- Games often store images, audio and scripts in engine-specific formats. The Game Assets viewers and the **Special Asset Converter** handle many of them (for example `.crx`, `.pgd`, `.tlg`, `.g00`, OVK, NWA, MIO).

## RPG Maker

| Engine | Files | What FuruArc does |
| --- | --- | --- |
| MV, MZ | per-file asset encryption (not an archive) | Decrypt and encrypt images and audio (key read from `System.json` or detected from an image), data translation, save editor (`rpgsave` / `rmmzsave`), value editor, plugin scanner, `plugins.js` editor, checker and comparer. |
| XP | `.rgssad` | Unpack and pack archives. |
| VX | `.rgss2a` | Unpack and pack archives. |
| VX Ace | `.rgss3a` | Unpack and pack archives. |

## Missing an engine or a format?

Open a [feature request](https://github.com/Fueeru26/FuruArc/issues/new/choose) with the engine name, the extensions, a link to public format documentation and, if one exists, a free sample file. See [CONTRIBUTING.md](../CONTRIBUTING.md).
