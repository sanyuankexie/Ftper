package org.kexie.android.ftper.viewmodel.bean

import org.kexie.android.ftper.model.WorkerType

data class TransferItem(
    val id:Int,
    val name: String,
    val percent: Int,
    val size: String,
    private val state: Int,
    private val type: Int
) {
    val isUpload
        get() = type == WorkerType.UPLOAD

    val isDownload
        get() = type == WorkerType.DOWNLOAD

    val isSuccess
        get() = state == 0

    val isFailed
        get() = state == 1

    val isRunning
        get() = state == 2
}