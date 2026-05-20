package io.anvil.modules.forge.knight.diff

internal object PatchApplier {

    data class ApplyResult(
        val patched: String,
        val appliedHunks: Int,
        val rejectedHunks: Int,
    )

    private data class Hunk(
        val oldStart: Int,
        val lines: List<Pair<Char, String>>,
    )

    private val hunkHeader = Regex("""^@@ -(\d+)(?:,\d+)? \+\d+(?:,\d+)? @@""")

    fun apply(
        original: String,
        patch: io.anvil.modules.forge.knight.UnifiedDiff,
    ): ApplyResult {
        if (patch.isEmpty) return ApplyResult(original, 0, 0)
        val hunks = parseHunks(patch.text)
        if (hunks.isEmpty()) return ApplyResult(original, 0, 0)

        val target = original.lines().toMutableList()
        var offset = 0
        var applied = 0
        var rejected = 0

        for (hunk in hunks) {
            val expected  = hunk.lines.filter { it.first != '+' }.map { it.second }
            val additions = hunk.lines.filter { it.first != '-' }.map { it.second }
            val hint = (hunk.oldStart - 1 + offset).coerceAtLeast(0)
            val pos = findMatch(target, expected, hint)

            if (pos < 0) { rejected++; continue }

            repeat(expected.size) { target.removeAt(pos) }
            target.addAll(pos, additions)
            offset += additions.size - expected.size
            applied++
        }

        return ApplyResult(
            patched = target.joinToString("\n"),
            appliedHunks = applied,
            rejectedHunks = rejected,
        )
    }

    private fun parseHunks(text: String): List<Hunk> {
        val hunks = mutableListOf<Hunk>()
        val lines = text.lines()
        var i = 0
        while (i < lines.size) {
            val m = hunkHeader.find(lines[i])
            if (m != null) {
                val oldStart = m.groupValues[1].toInt()
                val ops = mutableListOf<Pair<Char, String>>()
                i++
                while (i < lines.size && !lines[i].startsWith("@@")) {
                    val l = lines[i]
                    when {
                        l.startsWith(" ") -> ops += ' ' to l.drop(1)
                        l.startsWith("-") -> ops += '-' to l.drop(1)
                        l.startsWith("+") -> ops += '+' to l.drop(1)
                    }
                    i++
                }
                hunks += Hunk(oldStart, ops)
            } else {
                i++
            }
        }
        return hunks
    }

    private fun findMatch(lines: List<String>, expected: List<String>, hint: Int): Int {
        if (expected.isEmpty()) return hint.coerceIn(0, lines.size)
        val max = lines.size - expected.size
        if (max < 0) return -1
        val base = hint.coerceIn(0, max)
        if (matches(lines, expected, base)) return base
        var r = 1
        while (r <= max) {
            val hi = base + r
            if (hi <= max && matches(lines, expected, hi)) return hi
            val lo = base - r
            if (lo >= 0 && matches(lines, expected, lo)) return lo
            r++
        }
        return -1
    }

    private fun matches(lines: List<String>, expected: List<String>, pos: Int): Boolean =
        lines.subList(pos, pos + expected.size) == expected
}
