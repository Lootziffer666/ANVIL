package io.anvil.modules.forge.knight

import kotlinx.datetime.Clock

// Safety Policy §2: Keine Datei-Mutation außerhalb Workspace.rootPath.
// Verletzung → IllegalArgumentException (kein Silent-Fail).
internal fun requireInScope(absolutePath: String, rootPath: String) {
    require(absolutePath.startsWith(rootPath)) {
        "ANVIL Workspace Safety: Pfad liegt außerhalb rootPath! " +
            "path=$absolutePath rootPath=$rootPath"
    }
}

internal fun currentTimestamp(): String = Clock.System.now().toString()
