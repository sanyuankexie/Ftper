package org.kexie.android.ftper.viewmodel.bean

data class TransferItem(
        val name :String,
        val percent:Int,
        val size:String,
        val type:Int) {

    val isUpload
        get() = type == 0

    val isDownload
        get() = type == 1
}