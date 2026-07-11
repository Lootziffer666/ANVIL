package io.anvil.core.externaladapters

import java.io.File

/** Deterministic stand-in for [RealProcessRunner], seeded with real captured CLI output (see test files for provenance). */
class FakeProcessRunner(private val response: ProcessResult) : ProcessRunner {
    var lastCommand: List<String>? = null
        private set
    var lastWorkingDir: File? = null
        private set

    override fun run(command: List<String>, workingDir: File, timeoutSeconds: Long): ProcessResult {
        lastCommand = command
        lastWorkingDir = workingDir
        return response
    }
}
