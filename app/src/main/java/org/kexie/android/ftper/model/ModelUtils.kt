@file:JvmName("ModelUtils")
package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.Data
import org.kexie.android.ftper.R
import org.kexie.android.ftper.model.bean.WorkerConfig
import java.io.File

fun Data.loadConfig(context: Context): WorkerConfig {
    return WorkerConfig(
            host = this.getString(context.getString(R.string.host_key))!!,
            password = this.getString(context.getString(R.string.password_key))!!,
            username = this.getString(context.getString(R.string.username_key))!!,
            file = File(context.getString(R.string.path_key)),
            port = this.getInt(context.getString(R.string.port_key), Int.MIN_VALUE))
}