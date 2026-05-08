/* Token Manager — Gate A13 */

const TokenManager = (function () {
  const STORAGE_KEY = "anvil_tokens";

  function _load() {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY) || "[]");
    } catch { return []; }
  }

  function _save(tokens) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(tokens));
  }

  function _preview(key) {
    if (!key || key.length < 8) return "****";
    return key.slice(0, 4) + "..." + key.slice(-4);
  }

  function create(provider, label, key, scopes) {
    if (!provider || !label || !key) throw new Error("Provider, Label und Key sind Pflicht.");
    const tokens = _load();
    const id = "TOK_" + String(tokens.length + 1).padStart(3, "0");
    const token = {
      token_id: id, provider, label,
      key_preview: _preview(key),
      _key: key,  // stored encrypted in real impl
      created_at: new Date().toISOString(),
      last_used: null,
      status: "active",
      scopes: scopes || ["inference"]
    };
    tokens.push(token);
    _save(tokens);
    return { token_id: id, provider, label, status: "active" };
  }

  function list() {
    return _load().map(function (t) {
      return {
        token_id: t.token_id, provider: t.provider, label: t.label,
        key_preview: t.key_preview, status: t.status,
        created_at: t.created_at, last_used: t.last_used
      };
    });
  }

  function remove(tokenId) {
    var tokens = _load().filter(function (t) { return t.token_id !== tokenId; });
    _save(tokens);
    return true;
  }

  function getKey(tokenId) {
    var t = _load().find(function (t) { return t.token_id === tokenId && t.status === "active"; });
    if (!t) return null;
    t.last_used = new Date().toISOString();
    var all = _load();
    for (var i = 0; i < all.length; i++) {
      if (all[i].token_id === tokenId) { all[i] = t; break; }
    }
    _save(all);
    return t._key;
  }

  function rotate(tokenId, newKey) {
    var tokens = _load();
    for (var i = 0; i < tokens.length; i++) {
      if (tokens[i].token_id === tokenId) {
        tokens[i]._key = newKey;
        tokens[i].key_preview = _preview(newKey);
        tokens[i].last_used = null;
        break;
      }
    }
    _save(tokens);
    return true;
  }

  function getForProvider(providerId) {
    return _load().filter(function (t) { return t.provider === providerId && t.status === "active"; });
  }

  return { create: create, list: list, remove: remove, getKey: getKey, rotate: rotate, getForProvider: getForProvider };
})();
