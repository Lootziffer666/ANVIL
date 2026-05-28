# Knight — File I/O + Diff

Gate B6. Anvil's file read/write/delete and unified-diff layer.

## API

```kotlin
val knight = Knight(fileSystem = FileSystem.SYSTEM, workspace = workspace)

val content: String     = knight.read("/workspace/path/file.kt")
val diff: FileDiff      = knight.write("/workspace/path/file.kt", newContent)
knight.delete("/workspace/path/file.kt")
val patch: String       = knight.diff(original, modified)
val state: QualityState = knight.qualityState()
```

## Workspace Safety

Every write/delete validates `path.startsWith(workspace.rootPath)`.
Violation → `KnightScopeViolation` + `QualityState.FAILED`. No silent failures.

## Platform

Inject `FileSystem.SYSTEM` (JVM/Android) or `FakeFileSystem` (tests).
Targets: `androidTarget` + `jvm`.

## Diff

Pure-Kotlin LCS unified diff in `diff/UnifiedDiff.kt`. No external dependency.
