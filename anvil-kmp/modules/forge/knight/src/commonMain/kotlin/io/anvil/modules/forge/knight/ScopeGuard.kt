package io.anvil.modules.forge.knight

// Safety Policy §2: Keine Datei-Mutation außerhalb Workspace.rootPath.
// Verletzung → IllegalArgumentException (kein Silent-Fail).
internal fun requireInScope(absolutePath: String, rootPath: String) {
    require(absolutePath.startsWith(rootPath)) {
        "ANVIL Workspace Safety: Pfad liegt außerhalb rootPath! " +
            "path=$absolutePath rootPath=$rootPath"
    }
}

internal fun currentTimestamp(): String =
    // TODO Gate B7: echten Timestamp via kotlinx-datetime ersetzen
    "1970-01-01T00:00:00Z"
