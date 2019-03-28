package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import androidx.annotation.WorkerThread
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.blankj.utilcode.util.TimeUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.bean.ConfigEntity
import org.kexie.android.ftper.viewmodel.bean.ConfigItem

class ConfigsViewModel(application: Application)
    : AndroidViewModel(application) {

    companion object {
        private const val sSelectKey = "select";
    }

    private val mPreferences = PreferenceManager
            .getDefaultSharedPreferences(getApplication())

    private val mWorkerThread = HandlerThread(toString())
            .apply {
                start()
            }

    private val mSelect = MutableLiveData<ConfigItem>()

    private val mHandler = Handler(mWorkerThread.looper)

    /**
     *[ConfigsViewModel]是否在处理加载任务
     */
    private val mIsLoading = MutableLiveData<Boolean>(false)
    /**
     *出错响应
     */
    private val mOnError = PublishSubject.create<String>()
    /**
     *成功响应
     */
    private val mOnSuccess = PublishSubject.create<String>()
    /**
     *信息响应
     */
    private val mOnInfo = PublishSubject.create<String>()

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    private val mConfigs = MutableLiveData<List<ConfigItem>>()

    val configs: LiveData<List<ConfigItem>>
        get() = mConfigs

    val select: LiveData<ConfigItem>
        get() = mSelect

    private val mDao = getApplication<AppGlobal>().appDatabase.configDao

    init {
        mHandler.post { reload() }
    }

    fun remove(configItem: ConfigItem) {
        mHandler.post {
            try {
                if (mSelect.value?.id == configItem.id) {
                    synchronized(mPreferences)
                    {
                        mPreferences.edit()
                                .putInt(sSelectKey, Int.MIN_VALUE)
                                .apply()
                    }
                }
                mDao.remove(configItem.toDataEntity())
                reload()
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext("删除数据时出错")
            }
        }
    }

    fun update(configItem: ConfigItem) {
        mIsLoading.value = true
        mHandler.post {
            try {
                mDao.update(configItem.toDataEntity())
                reload()
                mOnSuccess.onNext("数据已更新")
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext("更新数据时出错")
            }
            mIsLoading.postValue(false)
        }
    }

    fun add(configItem: ConfigItem) {
        mIsLoading.value = true
        mHandler.post {
            try {
                mDao.add(configItem.toDataEntity())
                reload()
                mOnSuccess.onNext("数据已更新")
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext("Host或Port有误")
            }
            mIsLoading.postValue(false)
        }
    }

    fun select(configItem: ConfigItem) {
        mConfigs.value?.let {
            it.forEach {
                it.isSelect = false
            }
        }
        configItem.isSelect = true
        mSelect.value = configItem
        synchronized(mPreferences)
        {
            mPreferences.edit()
                    .putInt(sSelectKey, configItem.id)
                    .apply()
        }
    }

    @WorkerThread
    private fun reload() {
        val list = mDao.loadAll()
                .map { it.toViewData() }
        mSelect.postValue(list.firstOrNull { it.isSelect })
        mConfigs.postValue(list)
    }

    @Throws(Exception::class)
    private fun ConfigItem.toDataEntity(): ConfigEntity {
        val thiz = this
        return ConfigEntity()
                .apply {
                    id = thiz.id
                    name = if (thiz.name.isNullOrBlank()) {
                        null
                    } else {
                        thiz.name
                    }
                    host = if (thiz.host.isNullOrBlank()) {
                        throw Exception()
                    } else {
                        thiz.host
                    }
                    port = thiz.port!!.toInt()
                    username = thiz.username
                    password = thiz.password
                    date = TimeUtils.getNowMills()
                }
    }

    private fun ConfigEntity.toViewData(): ConfigItem {
        return ConfigItem(
                name = this.name,
                id = this.id,
                host = this.host,
                port = this.port.toString(),
                username = this.username,
                password = this.password,
                date = TimeUtils.millis2String(this.date),
                isSelect = this.id == synchronized(mPreferences)
                {
                    mPreferences.getInt(sSelectKey, Int.MIN_VALUE)
                }
        )
    }

    override fun onCleared() {
        mWorkerThread.quit()
    }
}