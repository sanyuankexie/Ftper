package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.WorkerType
import org.kexie.android.ftper.model.bean.WorkerEntity
import org.kexie.android.ftper.viewmodel.bean.TransferItem
import java.util.*

class TransferViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mDao = getApplication<AppGlobal>()
        .appDatabase
        .transferDao

    private val mWorkManager = WorkManager.getInstance()

    //只会在主线程操作该集合
    private val mWorkerTable = ArrayMap<UUID, LiveData<WorkInfo>>()

    private val mItems = MediatorLiveData<List<TransferItem>>()

    private val mMain = Handler(Looper.getMainLooper())

    private val mWorkerThread = HandlerThread(toString())
        .apply {
            start()
        }

    private val mWorker = Handler(mWorkerThread.looper)

    private val mReloadTask = object : Runnable {
        @MainThread
        override fun run() {
            reloadInternal()
            mMain.postDelayed(this, 1000)
        }
    }

    val item:LiveData<List<TransferItem>>
        get() = mItems

    fun setActive(active: Boolean) {
        mMain.removeCallbacks(mReloadTask)
        if (active) {
            mMain.post(mReloadTask)
        }
    }

    @MainThread
    private fun reloadInternal() {
        //开始之前先取消订阅
        mWorkerTable.values.forEach {
            mItems.removeSource(it)
        }
        mWorkerTable.clear()
        mWorker.post {
            //在工作线程加载实体类
            val entity = mDao.loadAll()
            //映射成View数据
            val items = entity.map { it.toReadabilityData() }
            //转发到主线程
            mMain.post {
                entity.forEach { entity ->
                    //获取WorkerId
                    val workerId = UUID.fromString(entity.workerId)
                    //重新订阅
                    val workInfo = mWorkManager
                        .getWorkInfoByIdLiveData(workerId)
                    mItems.addSource(workInfo, WorkerObserver(entity.id, workerId))
                    mWorkerTable[workerId] = workInfo
                }
                mItems.value = items
            }
        }
    }

    private inner class WorkerObserver(
        val itemId: Int,
        val workerId: UUID
    ) : Observer<WorkInfo> {
        override fun onChanged(info: WorkInfo?) {
            if (info == null) {
                mWorkerTable[workerId]?.let {
                    mItems.removeSource(it)
                }
                mWorkerTable.remove(workerId)
            } else {
                mItems.value?.let {
                    //拷贝一份用于刷新
                    val items = it.toMutableList()
                    val index = items.indexOfFirst { item ->
                        item.id == itemId
                    }
                    if (index != -1) {
                        items[index] = items[index]
                            .copy(state = info.state.name)
                        mItems.value = items
                    }
                }
            }
        }
    }

    private fun WorkerEntity.toReadabilityData(): TransferItem {
        val icon = ContextCompat.getDrawable(
            getApplication(),
            if (this.type == WorkerType.UPLOAD)
                R.drawable.up
            else
                R.drawable.dl
        )
        val str = getApplication<Application>()
            .getString(R.string.loading_text)
        return TransferItem(
            id = this.id,
            name = this.name,
            size = str,
            state = str,
            percent = (this.doSize.toFloat() / this.size.toFloat() * 100f).toInt(),
            icon = icon!!
        )
    }

    override fun onCleared() {
        mWorkerThread.quit()
        mMain.removeCallbacksAndMessages(null)
    }
}
