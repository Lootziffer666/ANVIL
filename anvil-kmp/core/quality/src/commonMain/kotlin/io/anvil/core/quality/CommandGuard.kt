package io.anvil.core.quality

import io.anvil.core.contracts.QualityState

data class CommandValidation(
    val command: String,
    val allowed: Boolean,
    val reason: String,
)

// Warden — Safety Policy §1: kein Kommando ohne Allowlist-Eintrag.
// Verletzung → QualityState.FAILED + require-Exception (kein Silent-Fail).
object CommandGuard {

    fun validate(command: String): CommandValidation = when {
        CommandPolicy.isForbidden(command) ->
            CommandValidation(command, false, "ANVIL Safety: Verbotenes Kommando — $command")
        !CommandPolicy.isAllowed(command) ->
            CommandValidation(command, false, "ANVIL Safety: Kommando nicht in Allowlist — $command")
        else ->
            CommandValidation(command, true, "OK — ${command.trim().split(" ", "\t").first()}")
    }

    fun require(command: String) {
        val v = validate(command)
        require(v.allowed) { v.reason }
    }

    fun qualityState(command: String): QualityState =
        if (validate(command).allowed) QualityState.STABLE else QualityState.FAILED
}
