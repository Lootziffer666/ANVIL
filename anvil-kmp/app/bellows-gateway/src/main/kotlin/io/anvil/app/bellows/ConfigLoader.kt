package io.anvil.app.bellows

import io.anvil.modules.bellows.BellowsConfig
import io.anvil.modules.bellows.ProviderConfig
import java.io.File

/**
 * Lädt/Speichert die [BellowsConfig] und löst plattformgerechte Pfade auf.
 *
 * Default-Verzeichnis:
 *  - Windows: `%APPDATA%\anvil-bellows\`
 *  - Linux/macOS: `$XDG_CONFIG_HOME/anvil-bellows/` bzw. `~/.config/anvil-bellows/`
 */
object ConfigLoader {

    private const val APP_DIR = "anvil-bellows"
    const val CONFIG_FILE = "bellows.config.json"
    const val VAULT_FILE = "bellows.keystore.jceks"

    fun configDir(): File {
        val appData = System.getenv("APPDATA")
        val xdg = System.getenv("XDG_CONFIG_HOME")
        val base = when {
            !appData.isNullOrBlank() -> File(appData)
            !xdg.isNullOrBlank() -> File(xdg)
            else -> File(System.getProperty("user.home"), ".config")
        }
        return File(base, APP_DIR)
    }

    fun defaultConfigFile(): File = File(configDir(), CONFIG_FILE)

    fun vaultFile(configFile: File): File = File(configFile.absoluteFile.parentFile, VAULT_FILE)

    fun load(file: File): BellowsConfig {
        require(file.exists()) {
            "Konfiguration nicht gefunden: ${file.absolutePath}\n" +
                "Erst anlegen mit:  bellows config init"
        }
        return gatewayJson.decodeFromString(BellowsConfig.serializer(), file.readText())
    }

    fun save(file: File, config: BellowsConfig) {
        file.parentFile?.mkdirs()
        file.writeText(gatewayJson.encodeToString(BellowsConfig.serializer(), config))
    }

    /** Beispiel-Konfiguration — enthält bewusst KEINE Klartext-Schlüssel. */
    fun sampleConfig(): BellowsConfig = BellowsConfig(
        host = "127.0.0.1",
        port = 8765,
        providers = listOf(
            ProviderConfig(
                id = "openrouter",
                baseUrl = "https://openrouter.ai/api/v1",
                apiKeyRef = "openrouter",
                models = listOf(
                    "openai/gpt-4o-mini",
                    "anthropic/claude-3.5-sonnet",
                    "deepseek/deepseek-chat",
                ),
                local = false,
                headers = mapOf(
                    "HTTP-Referer" to "https://anvil.local",
                    "X-Title" to "Anvil Bellows",
                ),
            ),
            ProviderConfig(
                id = "local-hermes",
                baseUrl = "http://localhost:1234/v1",
                apiKeyEnv = null,
                models = listOf("hermes", "NousResearch/Hermes-3-Llama-3.1-8B"),
                local = true,
            ),
        ),
    )
}
