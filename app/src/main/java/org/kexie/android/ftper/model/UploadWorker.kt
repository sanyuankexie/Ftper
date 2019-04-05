package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

class UploadWorker(context: Context, workerParams: WorkerParameters)
    : FTPWorker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {
        return try {
            val client = connect()
            mConfig.file

            ListenableWorker.Result.success()
        } catch (e: Throwable) {
            e.printStackTrace()
            ListenableWorker.Result.failure()
        }
    }
}
