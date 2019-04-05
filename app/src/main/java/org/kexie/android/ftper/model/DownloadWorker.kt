package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.kexie.android.ftper.model.bean.WorkerConfig

class DownloadWorker(context: Context,
                     workerParams: WorkerParameters) : Worker(context, workerParams) {

    val config = WorkerConfig.loadForm(context, workerParams.inputData)


    override fun doWork(): ListenableWorker.Result {
        return ListenableWorker.Result.failure()
    }

    override fun onStopped() {
        super.onStopped()
    }
}
