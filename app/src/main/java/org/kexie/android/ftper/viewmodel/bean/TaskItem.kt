package org.kexie.android.ftper.viewmodel.bean

import android.graphics.drawable.Drawable

data class TaskItem(
    //task
    val id:Int,
    val name: String,
    val percent: Int,
    val size: String,
    val state:TaskState,
    val icon: Drawable,
    val finish:Boolean
)

