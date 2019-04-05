package org.kexie.android.ftper.model

import android.content.Context
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

    protected val mDatabase = (applicationContext as AppGlobal).appDatabase

    protected val mDao = mDatabase.transferDao

    protected val mConfig by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        WorkerConfig(
                host = inputData.getString(applicationContext.getString(R.string.host_key))!!,
                password = inputData.getString(applicationContext.getString(R.string.password_key))!!,
                username = inputData.getString(applicationContext.getString(R.string.username_key))!!,
                file = File(inputData.getString(applicationContext.getString(R.string.path_key))!!),
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

    @Throws(Exception::class)
    protected fun connect(): FTPClient {
        return FTPClient()
                .apply {
                    connect(mConfig.host, mConfig.port)
                    login(mConfig.username, mConfig.password)
                    enterLocalPassiveMode()
                    setFileType(FTP.BINARY_FILE_TYPE)
                    controlEncoding = applicationContext.getString(R.string.gbk)
                    connectTimeout = 5000
                    defaultTimeout = 5000
                }
    }
}
