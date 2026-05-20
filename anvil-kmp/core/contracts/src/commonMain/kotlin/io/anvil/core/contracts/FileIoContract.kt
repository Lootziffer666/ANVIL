package io.anvil.core.contracts

interface FileIoContract {
    val rootPath: String
    fun read(relativePath: String): String
    fun exists(relativePath: String): Boolean
    fun write(relativePath: String, content: String)
    fun delete(relativePath: String)
}
