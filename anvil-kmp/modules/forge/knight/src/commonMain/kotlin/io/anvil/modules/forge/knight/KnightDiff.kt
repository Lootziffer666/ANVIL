package io.anvil.modules.forge.knight

object KnightDiff {
    fun unified(original: String, revised: String): String {
        val old = original.lines()
        val new = revised.lines()
        return buildString {
            appendLine("--- original")
            appendLine("+++ revised")
            old.zip(new).forEach { (o, n) ->
                if (o != n) { appendLine("-$o"); appendLine("+$n") }
            }
            new.drop(old.size).forEach { appendLine("+$it") }
            old.drop(new.size).forEach { appendLine("-$it") }
        }.trimEnd()
    }
}
