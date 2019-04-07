package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.viewmodel.bean.TransferItem

class TransferViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mWorkerThread = HandlerThread(toString())
        .apply {
            start()
        }

    private val mHandler = Handler(mWorkerThread.looper)

    private val mReloadTask = object : Runnable {
        override fun run() {
            reloadInternal()
            mHandler.postDelayed(this, 1000)
        }
    }

    private val mTransferDao = getApplication<AppGlobal>()
        .appDatabase
        .transferDao

    private val mWorkManager = WorkManager.getInstance()

    private val mItem = MutableLiveData<List<TransferItem>>()

    val item: LiveData<List<TransferItem>>
        get() = mItem

    fun delete(item: TransferItem) {
        mHandler.post {
            mTransferDao.removeById(item.id)
            reloadInternal()
        }
    }

    fun setActive(active: Boolean) {
        mHandler.removeCallbacks(mReloadTask)
        if (active) {
            mHandler.post(mReloadTask)
        }
    }

    @WorkerThread
    private fun reloadInternal() {

    }

    override fun onCleared() {
        mWorkerThread.quit()
    }
}
