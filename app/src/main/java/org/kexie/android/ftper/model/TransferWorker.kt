package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import java.io.File

abstract class TransferWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    protected val database = (applicationContext as AppGlobal).appDatabase

    protected val config by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Config(
            host = inputData.getString(applicationContext.getString(R.string.host_key))!!,
            password = inputData.getString(applicationContext.getString(R.string.password_key))!!,
            username = inputData.getString(applicationContext.getString(R.string.username_key))!!,
            local = File(inputData.getString(applicationContext.getString(R.string.local_key))!!),
            remote = inputData.getString(applicationContext.getString(R.string.remote_key))!!,
            port = inputData.getInt(applicationContext.getString(R.string.port_key), Int.MIN_VALUE)
                .apply {
                    if (this == Int.MIN_VALUE) {
                        throw RuntimeException()
                    }
                }
        )
    }

    @Throws(Throwable::class)
    protected fun connect(): FTPClient {
        return FTPClient()
            .apply {
                val timeout = 5000
                controlEncoding = applicationContext.getString(R.string.gbk)
                connectTimeout = timeout
                defaultTimeout = timeout
                connect(config.host, config.port)
                login(config.username, config.password)
                soTimeout = timeout
                setFileType(FTP.BINARY_FILE_TYPE)
            }
    }

    protected data class Config(
        val host: String,
        val port: Int,
        val username: String,
        val password: String,
        val local: File,
        val remote: String
    )
}
