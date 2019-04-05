package org.kexie.android.ftper.model.bean

import java.io.File


data class WorkerConfig(
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val file:File,
        val remote:String)