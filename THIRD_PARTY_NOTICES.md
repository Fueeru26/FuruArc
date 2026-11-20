# Third-party notices

FuruArc includes the following source code directly in `app/src/main/java` (no native libraries):

## java-lame (Java port of the LAME MP3 encoder)
- Location: `app/src/main/java/net/sourceforge/lame/{mp3,mpg}`
- Source: https://github.com/nwaldispuehl/java-lame
- License: GNU Lesser General Public License (LGPL). The original license header is kept in every file.
- Only the `mp3` and `mpg` packages are included; the `lowlevel` package (which depends on javax.sound) is not used.
- This code can be replaced with another version: it is self-contained in the `net.sourceforge.lame` package.

## vorbis-java (Java port of libogg/libvorbis, Xiph.Org Foundation)
- Location: `app/src/main/java/org/xiph/{libogg,libvorbis}`
- Source: https://github.com/tulskiy/musique (dependencies/vorbis-java)
- License: BSD-style Xiph.Org (see the header of each file). Only `libogg` and `libvorbis` are included.

## FLAC
The FLAC encoder (`core/AudioWriters.kt`) was written from scratch following the FLAC format specification (RFC 9639); it contains no third-party code.

## References
Archive and script format behavior was checked against the open-source tools GARbro (https://github.com/morkt/GARbro) and msg-tool (https://github.com/lifegpc/msg-tool).
