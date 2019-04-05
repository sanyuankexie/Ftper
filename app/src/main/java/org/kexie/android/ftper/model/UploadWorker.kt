package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

class UploadWorker(context: Context, workerParams: WorkerParameters)
    : FTPWorker(context, workerParams) {

    override fun doWork(): Result {
        return ListenableWorker.Result.failure()
    }

}
