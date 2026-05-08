/* ═══════════════════════════════════════════
   Anvil — Platform Abstraction Layer (Gate A17)
   Detect · Adapt · Portable Paths
   ═══════════════════════════════════════════ */

const AnvilPlatform = (function () {
  "use strict";

  // ── Platform Detection ─────────────────
  function detect() {
    const ua = (typeof navigator !== "undefined" && navigator.userAgent) || "";
    const platform = (typeof navigator !== "undefined" && navigator.platform) || "";

    if (/Android/i.test(ua)) return "android";
    if (/iPhone|iPad|iPod/i.test(ua)) return "ios";
    if (/Win/i.test(platform) || /Windows/i.test(ua)) return "windows";
    if (/Mac/i.test(platform) && !/iPhone|iPad/i.test(ua)) return "macos";
    if (/Linux/i.test(platform) && !/Android/i.test(ua)) return "linux";
    return "web";
  }

  const current = detect();

  // ── Platform Metadata ──────────────────
  const META = {
    android:  { label: "Android",  icon: "📱", pathSep: "/",  home: "/storage/emulated/0", supportsFS: true, supportsDesktop: false },
    ios:      { label: "iOS",      icon: "📱", pathSep: "/",  home: null, supportsFS: false, supportsDesktop: false },
    windows:  { label: "Windows",  icon: "🖥️", pathSep: "\\", home: null, supportsFS: true, supportsDesktop: true },
    macos:    { label: "macOS",    icon: "💻", pathSep: "/",  home: null, supportsFS: true, supportsDesktop: true },
    linux:    { label: "Linux",    icon: "🐧", pathSep: "/",  home: null, supportsFS: true, supportsDesktop: true },
    web:      { label: "Browser",  icon: "🌐", pathSep: "/",  home: null, supportsFS: false, supportsDesktop: false }
  };

  function meta() { return META[current] || META.web; }

  // ── Portable Path Handling ─────────────
  // All internal paths use / (POSIX).
  // Convert to native only at OS boundary.
  function toNative(posixPath) {
    if (current === "windows") return posixPath.replace(/\//g, "\\");
    return posixPath;
  }

  function toPosix(nativePath) {
    return nativePath.replace(/\\/g, "/");
  }

  // Resolve a workspace-relative path (always POSIX inside Anvil)
  function resolve(base, rel) {
    var b = toPosix(base).replace(/\/+$/, "");
    var r = toPosix(rel).replace(/^\/+/, "");
    return b + "/" + r;
  }

  // ── Storage Abstraction ────────────────
  // localStorage works everywhere; FileSystem API only on desktop/android
  function saveLocal(key, data) {
    localStorage.setItem("anvil_" + key, JSON.stringify(data));
  }

  function loadLocal(key) {
    try { return JSON.parse(localStorage.getItem("anvil_" + key)); }
    catch (_) { return null; }
  }

  function removeLocal(key) {
    localStorage.removeItem("anvil_" + key);
  }

  // ── Feature Flags ──────────────────────
  function canUsePake() {
    return current === "windows" || current === "macos" || current === "linux";
  }

  function canUseLocalAI() {
    return current !== "ios";
  }

  function canUseFileSystem() {
    return meta().supportsFS;
  }

  function isDesktop() {
    return meta().supportsDesktop;
  }

  function isMobile() {
    return current === "android" || current === "ios";
  }

  // ── Platform Info String ───────────────
  function info() {
    var m = meta();
    return m.icon + " " + m.label;
  }

  return {
    current: current,
    detect: detect,
    meta: meta,
    toNative: toNative,
    toPosix: toPosix,
    resolve: resolve,
    saveLocal: saveLocal,
    loadLocal: loadLocal,
    removeLocal: removeLocal,
    canUsePake: canUsePake,
    canUseLocalAI: canUseLocalAI,
    canUseFileSystem: canUseFileSystem,
    isDesktop: isDesktop,
    isMobile: isMobile,
    info: info
  };
})();
