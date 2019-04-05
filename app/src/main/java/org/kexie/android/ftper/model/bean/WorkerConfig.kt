package org.kexie.android.ftper.model.bean

import android.content.Context
import androidx.work.Data
import org.kexie.android.ftper.R


data class WorkerConfig(
        val host: String,
        val port: Int,
        val username: String,
        val password: String) {

    companion object {
        fun loadForm(context: Context, input: Data): WorkerConfig {
            return WorkerConfig(
                    host = input.getString(context.getString(R.string.host_key))!!,
                    password = input.getString(context.getString(R.string.password_key))!!,
                    username = input.getString(context.getString(R.string.username_key))!!,
                    port = input.getInt(context.getString(R.string.port_key), Int.MIN_VALUE))
        }
    }
}