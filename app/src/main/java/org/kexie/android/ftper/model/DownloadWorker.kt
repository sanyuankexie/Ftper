package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters

class DownloadWorker(context: Context, workerParams: WorkerParameters)
    : FTPWorker(context, workerParams) {

    override fun doWork(): ListenableWorker.Result {

        return ListenableWorker.Result.failure()
    }

    override fun onStopped() {
        super.onStopped()
    }
}
