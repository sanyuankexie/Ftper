package org.kexie.android.ftper.viewmodel.bean

import android.graphics.drawable.Drawable

data class RemoteItem(
    val name: String,
    val size: Long,
    val icon: Drawable,
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