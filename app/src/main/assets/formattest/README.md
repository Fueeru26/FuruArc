# Real archives for the Format Test

Put a small archive from a real game here (a few KB is enough) together with a `<name>.golden` manifest. The Format Test
(on the device and in CI) unpacks it and compares the SHA-256 of every listed file, which proves that the format matches
the game and not only itself. Only add archives you are allowed to redistribute (your own, or tiny excerpts).

Manifest (plain text, `#` starts a comment):

```
format=age                      # format id (see FormatRegistry), for example xp3, pfs, age, shiinario
archive=DATA1_part.ALF          # archive file in this folder
key=90                          # optional 1-byte XOR key
opt.ageidx=@SYS4INI.BIN         # optional format option; @file = a file in this folder, passed as a path
file=SYSTEM4.BIN:<sha256>       # one line per file to check: name inside the archive : SHA-256 (hex)
```

Get the hashes with `sha256sum` on the files unpacked by another tool (for example GARbro).
