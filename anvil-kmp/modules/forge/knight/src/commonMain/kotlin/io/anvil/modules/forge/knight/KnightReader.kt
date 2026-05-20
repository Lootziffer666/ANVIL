package io.anvil.modules.forge.knight

import okio.FileSystem
import okio.Path.Companion.toPath

class KnightReader(
    private val rootPath: String,
    private val fs: FileSystem = FileSystem.SYSTEM,
) {
    fun read(relativePath: String): String {
        val abs = "$rootPath/$relativePath"
        requireInScope(abs, rootPath)
        return fs.read(abs.toPath()) { readUtf8() }
    }

    fun exists(relativePath: String): Boolean {
        val abs = "$rootPath/$relativePath"
        requireInScope(abs, rootPath)
        return fs.exists(abs.toPath())
    }
}
