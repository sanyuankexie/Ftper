package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient

class UploadWorker(context: Context,
                   workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val mConfig = workerParams.inputData.loadConfig(context)

    override fun doWork(): ListenableWorker.Result {
        val client = FTPClient()
        try {
            client.connect(mConfig.host,mConfig.port)
            client.login(mConfig.username,mConfig.password)
            client.enterLocalPassiveMode()
            client.setFileType(FTP.BINARY_FILE_TYPE)

        }catch (e:Throwable)
        {

        }

        return ListenableWorker.Result.failure()
    }

    override fun onStopped() {
        super.onStopped()
    }
}
