package org.kexie.android.ftper.model.bean


data class WorkerConfig(
        val host: String,
        val port: Int,
        val username: String,
        val password: String)