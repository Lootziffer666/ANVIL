package io.anvil.core.externaladapters

import java.io.File
import java.util.concurrent.TimeUnit

data class ProcessResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val timedOut: Boolean = false,
)

/** Seam between adapters and the real OS process — lets tests inject a [FakeProcessRunner] instead of shelling out. */
interface ProcessRunner {
    fun run(command: List<String>, workingDir: File, timeoutSeconds: Long = 120): ProcessResult
}

class RealProcessRunner : ProcessRunner {
    override fun run(command: List<String>, workingDir: File, timeoutSeconds: Long): ProcessResult {
        val process = ProcessBuilder(command)
            .directory(workingDir)
            .start()
        val stdout = StringBuilder()
        val stderr = StringBuilder()
        val stdoutReader = Thread { process.inputStream.bufferedReader().forEachLine { stdout.appendLine(it) } }
        val stderrReader = Thread { process.errorStream.bufferedReader().forEachLine { stderr.appendLine(it) } }
        stdoutReader.start()
        stderrReader.start()

        val finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!finished) {
            process.destroyForcibly()
        }
        stdoutReader.join(2000)
        stderrReader.join(2000)

        return ProcessResult(
            exitCode = if (finished) process.exitValue() else -1,
            stdout = stdout.toString(),
            stderr = stderr.toString(),
            timedOut = !finished,
        )
    }
}
