# Pake Desktop Shell

**Gate:** A18
**Status:** Prototype — Icon-Asset, npm scripts und Pake-Checks vorhanden; Desktop-Build umgebungsabhängig
**Quelle:** [tw93/Pake](https://github.com/tw93/Pake) — Starred von Lootziffer666

## Zweck

> Turn any webpage into a desktop app with one command.

Pake verpackt die Anvil Web-App als native Desktop-App:
- **Windows:** `.exe` (kein Installer nötig)
- **macOS:** `.dmg`
- **Linux:** `.AppImage`

Basiert auf Tauri/Rust → ~5 MB statt 200 MB (Electron).

## Voraussetzungen

```bash
# Rust + Node.js müssen installiert sein
npm install -g pake-cli
```

## Build-Befehle

### Windows .exe
```bash
pake ./app/index.html \
  --name "Anvil" \
  --icon assets/anvil-icon.svg \
  --width 1280 --height 800 \
  --use-local-file \
  --targets .exe
```

### macOS .dmg
```bash
pake ./app/index.html \
  --name "Anvil" \
  --icon assets/anvil-icon.svg \
  --width 1280 --height 800 \
  --use-local-file \
  --targets .dmg
```

### Linux .AppImage
```bash
pake ./app/index.html \
  --name "Anvil" \
  --icon assets/anvil-icon.svg \
  --width 1280 --height 800 \
  --use-local-file \
  --targets .appimage
```

## Build-Konfiguration

```json
{
  "name": "Anvil",
  "version": "0.1.0",
  "icon": "assets/anvil-icon.svg",
  "width": 1280,
  "height": 800,
  "transparent": true,
  "fullscreen": false,
  "resizable": true,
  "inject": [],
  "user_agent": ""
}
```

> Datei: `pake.config.json` im Repo-Root

## Tauri Alternative (gitbutlerapp/gitbutler Pattern)

Wenn mehr native Funktionen nötig werden (System Tray, File Watcher,
native Dialogs), ist der Wechsel zu einem vollständigen Tauri-Setup
möglich. GitButler (Starred) zeigt das Pattern:

```
src-tauri/
  Cargo.toml
  tauri.conf.json
  src/
    main.rs
```

## Build-Script

```bash
npm run pake:check
npm run pake:build
```

`pake:check` validiert App-Einstieg, Config, Icon und Pake-CLI-Verfügbarkeit.
`pake:build` nutzt ein lokal/global installiertes `pake` oder fällt auf `npx --yes pake-cli` zurück. Der vollständige Desktop-Build bleibt umgebungsabhängig, weil Rust/Tauri-Systemabhängigkeiten außerhalb des Repo-Contracts liegen.

## Build Pipeline

```
app/index.html  →  pake-cli  →  Anvil.exe (Windows)
                               →  Anvil.dmg (macOS)
                               →  Anvil.AppImage (Linux)
```

## Vergleich: Pake vs. Electron vs. Tauri

| | Pake | Electron | Tauri |
|---|---|---|---|
| App-Größe | ~5 MB | ~200 MB | ~5 MB |
| Startup | <1s | 2-5s | <1s |
| Native APIs | Basis | Voll | Voll |
| Aufwand | 1 Befehl | Setup nötig | Setup nötig |
| Anvil-Empfehlung | ✅ Jetzt | ❌ | ⬜ Später |
