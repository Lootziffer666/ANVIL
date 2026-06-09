package io.anvil.app.bellows

import io.anvil.core.contracts.CredentialKey
import io.anvil.modules.bellows.BellowsRouter
import io.anvil.modules.bellows.ProviderFactory
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.PrintStream
import kotlin.system.exitProcess

private const val VAULT_PASSWORD_ENV = "ANVIL_BELLOWS_VAULT_PASSWORD"

fun main(args: Array<String>) {
    // UTF-8-Ausgabe erzwingen (Windows-Konsolen nutzen sonst Legacy-Codepages → Mojibake).
    System.setOut(PrintStream(System.out, true, Charsets.UTF_8))
    System.setErr(PrintStream(System.err, true, Charsets.UTF_8))

    when (val command = args.firstOrNull()) {
        "serve" -> serve(Args(args))
        "config" -> config(Args(args))
        "key" -> key(Args(args))
        "models" -> models(Args(args))
        null, "help", "--help", "-h" -> printHelp()
        else -> {
            System.err.println("Unbekannter Befehl: $command\n")
            printHelp()
            exitProcess(2)
        }
    }
}

// ── serve ────────────────────────────────────────────────────────────────────

private fun serve(args: Args) = runBlocking {
    val configFile = args.configFile()
    val config = ConfigLoader.load(configFile)
    val host = args.value("--host") ?: config.host
    val port = args.value("--port")?.toIntOrNull() ?: config.port

    val needsVault = config.providers.any { it.apiKeyRef != null } || config.gatewayKeyRef != null
    val vault = if (needsVault) {
        openVaultOrExit(
            configFile,
            promptIfMissing = false,
            missingMessage = "Provider verweisen auf den Vault, aber \$$VAULT_PASSWORD_ENV ist nicht gesetzt.\n" +
                "Setze die Variable oder nutze 'apiKeyEnv' statt 'apiKeyRef'.",
        )
    } else {
        null
    }

    // Geheimnisse vorab auflösen (suspend) — die Factory bleibt synchron & key-frei.
    val secrets = mutableMapOf<String, String>()
    if (vault != null) {
        config.providers.mapNotNull { it.apiKeyRef }.distinct().forEach { ref ->
            vault.retrieve(CredentialKey(ref))?.let { secrets[ref] = it }
        }
    }
    val gatewayKey = config.gatewayKeyRef?.let { vault?.retrieve(CredentialKey(it)) }

    val client = buildHttpClient()
    val factory = ProviderFactory(
        client = client,
        secretResolver = { secrets[it] },
        envResolver = { name -> System.getenv(name) },
    )
    val router = BellowsRouter(factory.build(config))

    printBanner(host, port, config, gatewayKey != null)
    embeddedServer(CIO, host = host, port = port) {
        bellowsGateway(router, gatewayKey)
    }.start(wait = true)
}

private fun printBanner(host: String, port: Int, config: io.anvil.modules.bellows.BellowsConfig, secured: Boolean) {
    val base = "http://$host:$port"
    val rule = "-".repeat(48)
    println(rule)
    println(" Anvil Bellows Gateway")
    println(" Endpoint : $base/v1")
    println(" Health   : $base/health")
    println(" Auth     : ${if (secured) "Bearer-Token aktiv" else "offen (nur localhost empfohlen)"}")
    println(" Provider : " + (config.providers.joinToString { "${it.id}${if (it.local) " (lokal)" else ""}" }.ifBlank { "<keine konfiguriert>" }))
    println(rule)
    println(" OpenCode:  Base-URL = $base/v1")
    println("            API-Key  = " + if (secured) "<dein Gateway-Token>" else "beliebig (z.B. 'sk-anvil')")
    println(rule)
}

// ── config ─────────────────────────────────────────────────────────────────────

private fun config(args: Args) {
    when (args.positional(1)) {
        "init" -> {
            val file = args.configFile()
            if (file.exists() && !args.has("--force")) {
                System.err.println("Existiert bereits: ${file.absolutePath}  (--force zum Überschreiben)")
                exitProcess(1)
            }
            ConfigLoader.save(file, ConfigLoader.sampleConfig())
            println("Konfiguration angelegt: ${file.absolutePath}")
            println()
            println("Nächste Schritte:")
            println("  1. ${file.name} öffnen und Provider/Modelle anpassen.")
            println("  2. Cloud-Key sicher ablegen:   bellows key set openrouter")
            println("  3. Gateway starten:            bellows serve")
        }
        "path" -> println(args.configFile().absolutePath)
        else -> {
            System.err.println("Nutzung: bellows config <init|path> [--config <pfad>] [--force]")
            exitProcess(2)
        }
    }
}

// ── key (CredentialVault) ───────────────────────────────────────────────────────

private fun key(args: Args) = runBlocking {
    val configFile = args.configFile()
    when (args.positional(1)) {
        "set" -> {
            val ref = args.positional(2) ?: run {
                System.err.println("Nutzung: bellows key set <ref>")
                exitProcess(2)
            }
            val vault = openVaultOrExit(configFile, promptIfMissing = true, missingMessage = "Kein Vault-Passwort.")
            val secret = readSecret("Secret für '$ref': ")
            if (secret.isBlank()) { System.err.println("Leeres Secret — abgebrochen."); exitProcess(1) }
            vault.store(CredentialKey(ref), secret)
            println("Gespeichert: '$ref' → ${ConfigLoader.vaultFile(configFile).name}")
        }
        "list" -> {
            val vault = openVaultOrExit(configFile, promptIfMissing = true, missingMessage = "Kein Vault-Passwort.")
            val aliases = vault.aliases()
            if (aliases.isEmpty()) println("(Vault ist leer)") else aliases.sorted().forEach { println(it) }
        }
        "rm" -> {
            val ref = args.positional(2) ?: run {
                System.err.println("Nutzung: bellows key rm <ref>")
                exitProcess(2)
            }
            val vault = openVaultOrExit(configFile, promptIfMissing = true, missingMessage = "Kein Vault-Passwort.")
            vault.delete(CredentialKey(ref))
            println("Entfernt: '$ref'")
        }
        else -> {
            System.err.println("Nutzung: bellows key <set|list|rm> [<ref>]")
            exitProcess(2)
        }
    }
}

// ── models ───────────────────────────────────────────────────────────────────

private fun models(args: Args) {
    val config = ConfigLoader.load(args.configFile())
    if (config.providers.isEmpty()) {
        println("(keine Provider konfiguriert)")
        return
    }
    config.providers.forEach { p ->
        println("${p.id}${if (p.local) " (lokal)" else ""}  ->  ${p.baseUrl}")
        if (p.models.isEmpty()) println("    (Passthrough — bedient jedes Modell)")
        p.models.forEach { println("    - $it") }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────

private fun openVault(configFile: File, promptIfMissing: Boolean): JvmCredentialVault? {
    val password = vaultPassword(promptIfMissing) ?: return null
    return JvmCredentialVault(ConfigLoader.vaultFile(configFile), password)
}

/**
 * Öffnet den Vault oder beendet das Programm mit einer verständlichen Meldung —
 * niemals mit einem rohen Stacktrace (falsches Passwort, beschädigte Datei, kein Passwort).
 */
private fun openVaultOrExit(configFile: File, promptIfMissing: Boolean, missingMessage: String): JvmCredentialVault {
    val vault = try {
        openVault(configFile, promptIfMissing)
    } catch (e: Exception) {
        System.err.println(
            "Fehler beim Öffnen des Credential-Vaults (Passwort falsch oder Datei beschädigt): ${e.message}",
        )
        exitProcess(3)
    }
    return vault ?: run {
        System.err.println(missingMessage)
        exitProcess(3)
    }
}

private fun vaultPassword(promptIfMissing: Boolean): CharArray? {
    System.getenv(VAULT_PASSWORD_ENV)?.takeIf { it.isNotBlank() }?.let { return it.toCharArray() }
    if (!promptIfMissing) return null
    val console = System.console()
    return if (console != null) {
        console.readPassword("Vault-Passwort: ")
    } else {
        print("Vault-Passwort: ")
        readlnOrNull()?.toCharArray()
    }
}

private fun readSecret(prompt: String): String {
    val console = System.console()
    return if (console != null) {
        String(console.readPassword(prompt))
    } else {
        // Nicht-interaktiv (z.B. Pipe): eine Zeile von stdin lesen.
        readlnOrNull().orEmpty()
    }.trim()
}

private fun printHelp() {
    println(
        """
        Anvil Bellows Gateway — lokaler, OpenAI-kompatibler LLM-Router (Cloud + lokal).

        Nutzung:
          bellows serve   [--config <pfad>] [--host <h>] [--port <p>]
          bellows config  init [--config <pfad>] [--force]
          bellows config  path [--config <pfad>]
          bellows key     set  <ref>   [--config <pfad>]
          bellows key     list         [--config <pfad>]
          bellows key     rm   <ref>   [--config <pfad>]
          bellows models  [--config <pfad>]
          bellows help

        Vault-Passwort über Umgebungsvariable:  $VAULT_PASSWORD_ENV
        Schnellstart:
          bellows config init
          bellows key set openrouter
          bellows serve
        """.trimIndent(),
    )
}

private class Args(argv: Array<String>) {
    private val flags = mutableMapOf<String, String>()
    private val positionals = mutableListOf<String>()

    init {
        var i = 0
        while (i < argv.size) {
            val a = argv[i]
            if (a.startsWith("--")) {
                val next = argv.getOrNull(i + 1)
                if (next != null && !next.startsWith("--")) {
                    flags[a] = next; i += 2
                } else {
                    flags[a] = "true"; i += 1
                }
            } else {
                positionals.add(a); i += 1
            }
        }
    }

    /** positional(0) = Befehl, positional(1) = Sub-Befehl, … */
    fun positional(index: Int): String? = positionals.getOrNull(index)
    fun value(name: String): String? = flags[name]?.takeUnless { it == "true" }
    fun has(name: String): Boolean = flags.containsKey(name)
    fun configFile(): File = value("--config")?.let { File(it) } ?: ConfigLoader.defaultConfigFile()
}
