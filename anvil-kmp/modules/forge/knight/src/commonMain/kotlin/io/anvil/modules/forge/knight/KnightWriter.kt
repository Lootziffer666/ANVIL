package io.anvil.modules.forge.knight

import io.anvil.core.domain.ChangedFile
import io.anvil.core.domain.FileAction
import okio.FileSystem
import okio.Path.Companion.toPath

class KnightWriter(
    private val rootPath: String,
    private val fs: FileSystem = FileSystem.SYSTEM,
) {
    fun write(relativePath: String, content: String): ChangedFile {
        val abs = "$rootPath/$relativePath"
        requireInScope(abs, rootPath)
        val existing = runCatching { fs.read(abs.toPath()) { readUtf8() } }.getOrNull()
        val action = if (existing == null) FileAction.CREATE else FileAction.MODIFY
        fs.write(abs.toPath()) { writeUtf8(content) }
        return ChangedFile(
            path = relativePath,
            action = action,
            diff = if (existing != null) KnightDiff.unified(existing, content) else null,
            timestamp = currentTimestamp(),
        )
    }

    fun delete(relativePath: String): ChangedFile {
        val abs = "$rootPath/$relativePath"
        requireInScope(abs, rootPath)
        fs.delete(abs.toPath())
        return ChangedFile(
            path = relativePath,
            action = FileAction.DELETE,
            diff = null,
            timestamp = currentTimestamp(),
        )
    }
}
