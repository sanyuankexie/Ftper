package org.kexie.android.ftper.viewmodel.bean

import android.graphics.drawable.Drawable

data class TransferItem(
    val id:Int,
    val name: String,
    val percent: Int,
    val size: String,
    val state:String,
    val icon: Drawable
)