# Android Blueprint Track

**Gate:** A9 — Tasker/App-Factory Blueprint Track
**Stand:** 2026-05-08
**Status:** docs-only

---

## Zweck

Anvil bekommt einen Pfad für Android-Blueprints. Kein nativer App-Bau —
nur Blueprints, die als Build Target verfügbar sind.

## Was ist ein Blueprint?

Ein Blueprint ist eine **gespeicherte Konfiguration**, die beschreibt:
- Welche Module zusammenarbeiten
- Welche Inputs erwartet werden
- Welches Output-Format erzeugt wird
- Welches Build Target angesteuert wird

> **Blueprint ≠ fertige native App.**
> Ein Blueprint ist ein *Bauplan*, kein fertiges Gebäude.

## Was kann Tasker/App Factory liefern?

| Fähigkeit | Status | Anmerkung |
|-----------|--------|-----------|
| Einfache Automatisierungen | ✅ Machbar | Tasker Profiles/Tasks exportierbar |
| UI-Prototypen (Scenes) | ⚠️ Limitiert | Nur einfache Layouts |
| API-Integration | ✅ Machbar | HTTP Requests via Tasker |
| File I/O | ✅ Machbar | Lesen/Schreiben über Tasker |
| Komplexe UI | ❌ Nicht geeignet | Besser: native App |
| Hintergrund-Services | ⚠️ Limitiert | Tasker-Profile begrenzt |
| Store-Distribution | ❌ Nein | Tasker-Export ist kein APK |

## Was bleibt Prototyp?

- Alles, was über einfache Automation hinausgeht
- Multi-Screen Flows
- Offline-Sync
- Push Notifications

## Was muss später nativ gebaut werden?

- Alles mit eigener UI (Android Activity/Fragment)
- Store-Distribution (signierte APKs)
- Background Processing (Services/WorkManager)
- Hardware-Zugriff (Kamera, Sensoren)

## Modul-Slot: Android Blueprint

```json
{
  "name": "Android Blueprint Tool",
  "purpose": "APK-Scaffolding: Manifest, Gradle, Signatur-Konfiguration.",
  "inputs": ["config/json", "config/yaml"],
  "outputs": ["build/gradle-project", "build/apk"],
  "requiredPermissions": ["filesystem.read", "filesystem.write", "network.build-server"],
  "canRunOffline": false,
  "canExport": true,
  "failureBehavior": "Build-Fehler mit Gradle-Log und Zeilennummer."
}
```

## Build Targets

| Target | Beschreibung | Status |
|--------|-------------|--------|
| `android-blueprint` | Tasker/App-Factory Export | docs-only — kein `modules/android-blueprint/` vorhanden |
| `android-apk` | Nativer APK Build | 🔜 Geplant |
| `android-aab` | App Bundle (Store) | 🔜 Geplant |

## Warnung in UI

Wenn Build Target = `android-blueprint`:

> ⚠️ *Blueprint-Modus: Erzeugt einen Bauplan, keine fertige App.
> Für native Apps: Build Target auf `android-apk` wechseln (zukünftiges Gate).*
