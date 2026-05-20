package io.anvil.core.quality

// Safety Policy §1: Command Guard Allowlist + Blocklist.
object CommandPolicy {
    val allowed = setOf("git", "gradle", "kotlinc", "adb", "ktlint", "detekt")
    val forbidden = setOf("rm", "curl", "wget", "sh", "bash", "eval", "exec", "chmod", "sudo", "su")

    fun isAllowed(command: String): Boolean {
        val name = baseCommand(command) ?: return false
        return name in allowed && name !in forbidden
    }

    fun isForbidden(command: String): Boolean {
        val name = baseCommand(command) ?: return true
        return name in forbidden
    }

    private fun baseCommand(command: String): String? =
        command.trim().split(" ", "\t").firstOrNull()
            ?.substringAfterLast("/")?.substringAfterLast("\\")
            ?.takeIf { it.isNotBlank() }
}
