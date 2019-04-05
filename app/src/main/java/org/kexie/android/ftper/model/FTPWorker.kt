package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.bean.WorkerConfig
import java.io.File

abstract class FTPWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    protected val database by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        (applicationContext as AppGlobal).appDatabase
    }

    protected val dao by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        database.transferDao
    }

    protected val config by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        WorkerConfig(
                host = inputData.getString(applicationContext.getString(R.string.host_key))!!,
                password = inputData.getString(applicationContext.getString(R.string.password_key))!!,
                username = inputData.getString(applicationContext.getString(R.string.username_key))!!,
                local = File(inputData.getString(applicationContext.getString(R.string.local_key))!!),
                remote = inputData.getString(applicationContext.getString(R.string.remote_key))!!,
                port = {
                    val value = inputData.getInt(applicationContext
                            .getString(R.string.port_key), Int.MIN_VALUE)
                    if (value == Int.MIN_VALUE) {
                        throw RuntimeException()
                    }
                    value
                }())
    }

    @Throws(Throwable::class)
    protected fun connect(): FTPClient {
        return FTPClient()
                .apply {
                    connect(config.host, config.port)
                    login(config.username, config.password)
                    enterLocalPassiveMode()
                    setFileType(FTP.BINARY_FILE_TYPE)
                    controlEncoding = applicationContext.getString(R.string.gbk)
                    connectTimeout = 5000
                    defaultTimeout = 5000
                }
    }

    protected fun failure(@FailureType type: Int): ListenableWorker.Result {
        val data = Data.Builder()
                .putInt(applicationContext.getString(R.string.result_key),
                        FailureType.UNKNOWN_ERROR)
                .build()
        return ListenableWorker.Result.failure(data)
    }
}
