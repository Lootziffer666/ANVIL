# Platform Abstraction Layer

**Gate:** A17
**Status:** Active
**Datei:** `app/platform.js`

## Zweck

Anvil muss auf Android *und* Windows laufen — mit denselben Daten.
Die Platform Abstraction Layer erkennt das Betriebssystem und passt
Pfade, Features und UI-Hinweise an.

## Erkennung

| Platform | Erkennung | Icon |
|----------|-----------|------|
| Android | `navigator.userAgent` enthält "Android" | 📱 |
| Windows | `navigator.platform` enthält "Win" | 🖥️ |
| macOS | `navigator.platform` enthält "Mac" | 💻 |
| Linux | `navigator.platform` enthält "Linux" | 🐧 |
| Browser | Fallback | 🌐 |

## Portable Pfade

**Regel:** Intern immer POSIX (`/`). Konvertierung nur an der OS-Grenze.

```js
// Intern:  projects/my-app/src/main.js
// Windows: projects\my-app\src\main.js
// Android: projects/my-app/src/main.js

AnvilPlatform.toNative("projects/my-app/src/main.js");
// → Windows: "projects\my-app\src\main.js"

AnvilPlatform.toPosix("projects\my-app\src\main.js");
// → "projects/my-app/src/main.js"
```

## Feature Flags

```js
AnvilPlatform.canUsePake()       // true on Windows/Mac/Linux
AnvilPlatform.canUseLocalAI()    // true everywhere except iOS
AnvilPlatform.canUseFileSystem() // true on Android/Windows/Mac/Linux
AnvilPlatform.isDesktop()        // true on Windows/Mac/Linux
AnvilPlatform.isMobile()         // true on Android/iOS
```

## Storage

```js
AnvilPlatform.saveLocal("workspace", data);  // → localStorage
AnvilPlatform.loadLocal("workspace");
AnvilPlatform.removeLocal("workspace");
```

## UI Integration

Platform indicator shown in header:
```
🖥️ Windows  |  📱 Android  |  🌐 Browser
```
