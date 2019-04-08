package org.kexie.android.ftper.viewmodel.bean

enum class TaskState(val text: String) {
    WAIT_START("等待"),
    RUNNING("运行"),
    FINISH("完成"),
    FAILED("出错");
}