package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.R
import org.kexie.android.ftper.model.bean.WorkerConfig
import java.io.File

abstract class FTPWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    protected val mConfig = workerParams.inputData.loadConfig()

    protected val mClient = FTPClient()

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

    private fun Data.loadConfig(): WorkerConfig {
        return WorkerConfig(
                host = this.getString(applicationContext.getString(R.string.host_key))!!,
                password = this.getString(applicationContext.getString(R.string.password_key))!!,
                username = this.getString(applicationContext.getString(R.string.username_key))!!,
                file = File(this.getString(applicationContext.getString(R.string.path_key))!!),
                port = this.getInt(applicationContext.getString(R.string.port_key), Int.MIN_VALUE))
    }
}
