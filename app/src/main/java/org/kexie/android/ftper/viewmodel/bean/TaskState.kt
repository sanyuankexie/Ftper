package org.kexie.android.ftper.viewmodel.bean

enum class TaskState(val text: String) {
    PAUSE("暂停中"),
    PENDING("等待中"),
    RUNNING("执行中"),
    FINISH("已完成"),
    FAILED("出错");
}