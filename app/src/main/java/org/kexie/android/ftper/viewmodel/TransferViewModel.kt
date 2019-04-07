package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.WorkerThread
import androidx.collection.ArrayMap
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import androidx.work.WorkInfo
import androidx.work.WorkManager
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.WorkerType
import org.kexie.android.ftper.model.bean.WorkerEntity
import org.kexie.android.ftper.viewmodel.bean.TransferItem
import org.kexie.android.ftper.widget.Utils
import java.util.*

class TransferViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mDao = getApplication<AppGlobal>()
        .appDatabase
        .transferDao

    private val mWorkManager = WorkManager.getInstance()

    //只会在主线程操作该集合
    private val mStates = ArrayMap<Int, LiveData<Pair<Int, WorkInfo.State>>>()

    private val mEntities = MutableLiveData<Map<Int, WorkerEntity>>()

    private val mItems = MediatorLiveData<Map<Int, TransferItem>>()
        .apply {
            addSource(mEntities) { newItems ->
                var oldItems = this.value
                if (oldItems == null) {
                    oldItems = emptyMap()
                }
                val cross = oldItems.keys.intersect(newItems.keys)
                val add = newItems.keys.subtract(cross)
                val remove = oldItems.keys.subtract(cross)
                val post = ArrayMap<Int, TransferItem>()
                add.forEach { addItemId ->
                    val entity = newItems.getValue(addItemId)
                    val workerId = UUID.fromString(entity.workerId)
                    val workInfo = mWorkManager.getWorkInfoByIdLiveData(workerId)
                    val state = Transformations.map(workInfo)
                    { info ->
                        addItemId to info.state
                    }
                    addSource(state)
                    { pair ->
                        value?.let {
                            //拷贝一份
                            val newMap = it.toMutableMap()
                            newMap[pair.first]?.let { item ->
                                newMap[pair.first] = item.copy(state = pair.second.name)
                                //刷新
                                value = newMap
                            }
                        }
                    }
                    mStates[addItemId] = state
                    val item = entity.toReadabilityData(null)
                    post[addItemId] = item
                }
                cross.forEach { updateItemId ->
                    val entity = newItems.getValue(updateItemId)
                    val old = oldItems.getValue(updateItemId)
                    post[updateItemId] = entity.toReadabilityData(old)
                }
                remove.forEach { removeItemId ->
                    mStates.remove(removeItemId)?.let {
                        removeSource(it)
                    }
                }
                value = post
            }
        }

    private val mWorkerThread = HandlerThread(toString())
        .apply {
            start()
        }

    private val mWorker = Handler(mWorkerThread.looper)

    private val mReloadTask = object : Runnable {
        @WorkerThread
        override fun run() {
            mEntities.postValue(mDao.loadAll()
                .map { it.id to it }
                .toMap())
            mWorker.postDelayed(this, 1000)
        }
    }

    private val icons = arrayOf(
        ContextCompat.getDrawable(getApplication(), R.drawable.up)!!,
        ContextCompat.getDrawable(getApplication(), R.drawable.dl)!!
    )

    val item: LiveData<List<TransferItem>>
        get() = Transformations.map(mItems) {
            it.values.toMutableList()
                .sortedBy { item -> item.id }
        }

    fun setActive(active: Boolean) {
        mWorker.removeCallbacks(mReloadTask)
        if (active) {
            mWorker.post(mReloadTask)
        }
    }

    private fun WorkerEntity.toReadabilityData(old: TransferItem?): TransferItem {
        val icon = if (this.type == WorkerType.UPLOAD)
            icons[0]
        else
            icons[1]

        return TransferItem(
            id = this.id,
            name = this.name,
            size = "${Utils.sizeToString(this.doSize)}/${Utils.sizeToString(this.size)}",
            state = old?.state ?: getApplication<Application>()
                .getString(R.string.loading_text),
            percent = (this.doSize.toFloat() / this.size.toFloat() * 100f).toInt(),
            icon = icon
        )
    }

    override fun onCleared() {
        mWorkerThread.quit()
    }
}
