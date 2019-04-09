package org.kexie.android.ftper.viewmodel.bean

enum class TaskState(val text: String) {
    WAIT_START("等待中"),
    PENDING("队列中"),
    RUNNING("执行中"),
    FINISH("已完成"),
    FAILED("出错");
}