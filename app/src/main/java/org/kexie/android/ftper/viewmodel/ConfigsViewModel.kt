package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.content.SharedPreferences
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

internal const val SELECT_KEY = "select"

class ConfigsViewModel(application: Application)
    : AndroidViewModel(application),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val mPreferences = PreferenceManager
        .getDefaultSharedPreferences(getApplication())
        .apply { registerOnSharedPreferenceChangeListener(this@ConfigsViewModel) }

    private val mWorkerThread = HandlerThread(toString())
        .apply { start() }

    private val mHandler = Handler(mWorkerThread.looper)

    private val mSelect = MutableLiveData<Int>()

    private val mConfigs = MutableLiveData<List<ConfigItem>>()

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

    private val mDao = getApplication<AppGlobal>()
        .appDatabase
        .configDao

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    val configs: LiveData<List<ConfigItem>>
        get() = mConfigs

    val select: LiveData<Int>
        get() = mSelect

    init {
        mHandler.post { reload() }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (SELECT_KEY == key) {
            mSelect.value = mPreferences.getInt(SELECT_KEY, Int.MIN_VALUE)
        }
    }

    fun remove(configItem: ConfigItem) {
        mHandler.post {
            try {
                if (mSelect.value == configItem.id) {
                    mPreferences.edit()
                        .putInt(SELECT_KEY, Int.MIN_VALUE)
                        .apply()
                }
                mDao.remove(configItem.toEntity())
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
                mDao.update(configItem.toEntity())
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
                mDao.add(configItem.toEntity())
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
        mPreferences.edit()
            .putInt(SELECT_KEY, configItem.id)
            .apply()
    }

    @WorkerThread
    private fun reload() {
        val list = mDao.loadAll()
            .map { it.toReadabilityData() }
        mConfigs.postValue(list)
    }

    @Throws(Exception::class)
    private fun ConfigItem.toEntity(): ConfigEntity {
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

    private fun ConfigEntity.toReadabilityData(): ConfigItem {
        return ConfigItem(
            name = this.name,
            id = this.id,
            host = this.host,
            port = this.port.toString(),
            username = this.username,
            password = this.password,
            date = TimeUtils.millis2String(this.date),
            isSelect = this.id == mPreferences.getInt(SELECT_KEY, Int.MIN_VALUE)
        )
    }

    override fun onCleared() {
        mWorkerThread.quit()
        mPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}