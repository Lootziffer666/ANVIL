package io.anvil.modules.forge.knight

data class UnifiedDiff(val text: String) {
    val isEmpty: Boolean get() = text.isBlank()
    override fun toString(): String = text
}
