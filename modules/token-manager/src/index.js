/* Token Manager — Gate A13
 *
 * LEGACY DISABLED: the historical browser localStorage implementation stored
 * secrets in clear text. The KMP/Bellows path now owns credential storage via
 * CredentialVaultContract. This shim keeps the old UI from crashing, purges any
 * legacy `_key` values, and refuses new secret storage.
 */

const TokenManager = (function () {
  const LEGACY_STORAGE_KEY = "anvil_tokens";
  const DISABLED_META_KEY = "anvil_tokens_disabled_meta";

  function _loadJson(key, fallback) {
    try {
      return JSON.parse(localStorage.getItem(key) || JSON.stringify(fallback));
    } catch (err) {
      return fallback;
    }
  }

  function _saveDisabledMeta(tokens) {
    localStorage.setItem(DISABLED_META_KEY, JSON.stringify(tokens));
  }

  function _purgeLegacySecrets() {
    var existingMeta = _loadJson(DISABLED_META_KEY, []);
    var legacy = _loadJson(LEGACY_STORAGE_KEY, []);
    if (legacy.length > 0) {
      var migrated = legacy.map(function (t) {
        return {
          token_id: t.token_id,
          provider: t.provider,
          label: t.label,
          key_preview: t.key_preview || "****",
          created_at: t.created_at,
          last_used: null,
          status: "disabled",
          disabled_reason: "Legacy localStorage token storage disabled; re-enter credential in Bellows CredentialVault."
        };
      });
      _saveDisabledMeta(existingMeta.concat(migrated));
      localStorage.removeItem(LEGACY_STORAGE_KEY);
      return migrated;
    }
    return existingMeta;
  }

  function _disabledError() {
    return new Error("Legacy Token Manager ist deaktiviert. Nutze Bellows CredentialVault statt localStorage.");
  }

  function create() {
    _purgeLegacySecrets();
    throw _disabledError();
  }

  function list() {
    return _purgeLegacySecrets().map(function (t) {
      return {
        token_id: t.token_id,
        provider: t.provider,
        label: t.label,
        key_preview: t.key_preview,
        status: "disabled",
        created_at: t.created_at,
        last_used: null,
        disabled_reason: t.disabled_reason
      };
    });
  }

  function remove(tokenId) {
    var tokens = _purgeLegacySecrets().filter(function (t) { return t.token_id !== tokenId; });
    _saveDisabledMeta(tokens);
    return true;
  }

  function getKey() {
    _purgeLegacySecrets();
    throw _disabledError();
  }

  function rotate() {
    _purgeLegacySecrets();
    throw _disabledError();
  }

  function getForProvider() {
    _purgeLegacySecrets();
    return [];
  }

  return { create: create, list: list, remove: remove, getKey: getKey, rotate: rotate, getForProvider: getForProvider };
})();
