package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.MainThread
import androidx.collection.ArrayMap
import androidx.collection.SparseArrayCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.WorkerType
import org.kexie.android.ftper.model.bean.WorkerEntity
import org.kexie.android.ftper.viewmodel.bean.TransferItem
import org.kexie.android.ftper.widget.Utils

class TransferViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mDao = getApplication<AppGlobal>()
        .appDatabase
        .transferDao

    private val mWorkManager = WorkManager.getInstance()

    private val mStates = SparseArrayCompat<LiveData<WorkInfo.State>>()

    private val mItems = MediatorLiveData<List<TransferItem>>()

    private var mUpdate: Disposable? = null

    private val mWorkerThread = HandlerThread(toString())
        .apply {
            start()
        }

    private val mMain = Handler(Looper.getMainLooper())

    private val mReloadTask = object : Runnable {
        @MainThread
        override fun run() {
            reloadInternal()
            mMain.postDelayed(this, 1000)
        }
    }

    private val mIcons = arrayOf(
        ContextCompat.getDrawable(getApplication(), R.drawable.up)!!,
        ContextCompat.getDrawable(getApplication(), R.drawable.dl)!!
    )

    val item: LiveData<List<TransferItem>>
        get() = mItems

    fun setActive(active: Boolean) {
        mMain.removeCallbacks(mReloadTask)
        if (active) {
            mMain.post(mReloadTask)
        }
    }

    private fun reloadInternal() {
        val update = mUpdate
        if (update != null && !update.isDisposed) {
            update.dispose()
        }
        mUpdate = reloadData()
            .subscribe(Consumer {
                mItems.value = it
            })
    }

    @MainThread
    private fun reloadData(): Single<List<TransferItem>> {
        class MergedContext(
            val add: Set<Int>,
            val update: Set<Int>,
            val remove: Set<Int>,
            val newEntities: Map<Int, WorkerEntity>,
            val oldItems: Map<Int, TransferItem>
        )
        return Single.just(mDao)
            .observeOn(AndroidSchedulers.from(mWorkerThread.looper))
            .map {
                it.loadAll()
            }.zipWith(Single.just(mItems.value ?: emptyList()),
                BiFunction<List<WorkerEntity>, List<TransferItem>, MergedContext>
                { newItems, oldItems ->
                    val oldItemMap = oldItems.map {
                        it.id to it
                    }.toMap()
                    val newItemMap = newItems.map {
                        it.id to it
                    }.toMap()
                    val cross = oldItemMap.keys.intersect(newItemMap.keys)
                    val add = newItemMap.keys.subtract(cross)
                    val remove = oldItemMap.keys.subtract(cross)
                    return@BiFunction MergedContext(add, cross, remove, newItemMap, oldItemMap)
                })
            .observeOn(AndroidSchedulers.mainThread())
            .map { context ->
                val result = ArrayMap<Int, TransferItem>()
                context.add.forEach { addItemId ->
                    val entity = context.newEntities.getValue(addItemId)
                    val workerId = entity.workerId
                    val workInfo = mWorkManager
                        .getWorkInfoByIdLiveData(workerId)
                    val state = Transformations.map(workInfo)
                    { info ->
                        info.state
                    }
                    mItems.addSource(state) { newState ->
                        mItems.value?.let { items ->
                            val index = items.indexOfFirst {
                                it.id == addItemId
                            }
                            val newResult = items.toMutableList()
                            newResult[index] = items[index].copy(state = newState.name)
                            mItems.value = newResult
                        }
                    }
                    mStates.put(addItemId, state)
                    result[addItemId] = entity.toReadabilityData(null)
                }
                context.update.forEach { updateItemId ->
                    val entity = context.newEntities.getValue(updateItemId)
                    val old = context.oldItems.getValue(updateItemId)
                    result[updateItemId] = entity.toReadabilityData(old)
                }
                context.remove.forEach { removeItemId ->
                    mStates[removeItemId]?.let {
                        mItems.removeSource(it)
                        mStates.remove(removeItemId)
                    }
                }
                return@map result.values
                    .sortedBy { it.id }
                    .toList()
            }
    }

    private fun WorkerEntity.toReadabilityData(old: TransferItem?): TransferItem {
        val icon = if (this.type == WorkerType.UPLOAD)
            mIcons[0]
        else
            mIcons[1]

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
        val update = mUpdate
        if (update != null && !update.isDisposed) {
            update.dispose()
        }
    }
}
