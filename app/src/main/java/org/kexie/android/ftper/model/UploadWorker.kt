package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters

class UploadWorker(context: Context,
                   workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val mConfig = workerParams.inputData.loadConfig(context)

    override fun doWork(): ListenableWorker.Result {
        return ListenableWorker.Result.failure()
    }

    override fun onStopped() {
        super.onStopped()
    }
}
