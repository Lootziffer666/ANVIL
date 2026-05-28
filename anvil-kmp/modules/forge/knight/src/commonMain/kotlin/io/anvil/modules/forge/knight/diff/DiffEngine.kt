package io.anvil.modules.forge.knight.diff

internal object DiffEngine {
    fun compute(
        original: String,
        modified: String,
        context: Int = 3,
    ): io.anvil.modules.forge.knight.UnifiedDiff {
        val old = original.lines()
        val new = modified.lines()
        val m = old.size; val n = new.size

        val dp = Array(m + 1) { IntArray(n + 1) }
        for (i in m - 1 downTo 0)
            for (j in n - 1 downTo 0)
                dp[i][j] = if (old[i] == new[j]) 1 + dp[i + 1][j + 1]
                            else maxOf(dp[i + 1][j], dp[i][j + 1])

        data class Op(val kind: Char, val line: String)
        val ops = mutableListOf<Op>()
        var i = 0; var j = 0
        while (i < m || j < n) {
            when {
                i < m && j < n && old[i] == new[j] ->
                    { ops += Op(' ', old[i]); i++; j++ }
                j < n && (i >= m || dp[i + 1][j] <= dp[i][j + 1]) ->
                    { ops += Op('+', new[j]); j++ }
                else -> { ops += Op('-', old[i]); i++ }
            }
        }

        val changed = ops.indices.filter { ops[it].kind != ' ' }
        if (changed.isEmpty()) return io.anvil.modules.forge.knight.UnifiedDiff("")

        val sb = StringBuilder("--- original\n+++ modified\n")
        var hFrom = -1; var hTo = -1
        fun flush() {
            if (hFrom < 0) return
            val slice = ops.subList(hFrom, hTo + 1)
            val oldBase = ops.take(hFrom).count { it.kind != '+' } + 1
            val newBase = ops.take(hFrom).count { it.kind != '-' } + 1
            val oldCnt = slice.count { it.kind != '+' }
            val newCnt = slice.count { it.kind != '-' }
            sb.append("@@ -$oldBase,$oldCnt +$newBase,$newCnt @@\n")
            slice.forEach { sb.append("${it.kind}${it.line}\n") }
        }
        for (idx in changed) {
            val from = maxOf(0, idx - context)
            val to   = minOf(ops.lastIndex, idx + context)
            if (hFrom < 0) { hFrom = from; hTo = to }
            else if (from <= hTo + 1) { hTo = maxOf(hTo, to) }
            else { flush(); hFrom = from; hTo = to }
        }
        flush()
        return io.anvil.modules.forge.knight.UnifiedDiff(sb.toString())
    }
}
