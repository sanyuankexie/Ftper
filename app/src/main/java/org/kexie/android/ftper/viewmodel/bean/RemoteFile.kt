package org.kexie.android.ftper.viewmodel.bean

data class RemoteFile(
    val name: String,
    val size: Long,
    private val type: Int
) {
    val isFile
        get() = this.type == 0
    val isDirectory
        get() = this.type == 1
    val isSymbolicLink
        get() = this.type == 2
    val isUnknown
        get() = this.type == 3
}