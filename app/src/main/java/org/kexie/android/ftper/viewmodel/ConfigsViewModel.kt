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
import org.kexie.android.ftper.viewmodel.bean.Config

class ConfigsViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mPreferences = PreferenceManager
        .getDefaultSharedPreferences(getApplication())

    private val mWorkerThread = HandlerThread(toString())
        .apply {
            start()
        }

    private val mHandler = Handler(mWorkerThread.looper)

    /**
     *[AndroidViewModel]是否在处理加载任务
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

    private val mConfigs = MutableLiveData<List<Config>>()

    val configs: LiveData<List<Config>>
        get() = mConfigs

    private val mDao = getApplication<AppGlobal>().appDatabase.configDao

    init {
        mHandler.post { reload() }
    }

    fun update(config: Config) {
        mIsLoading.value = true
        mHandler.post {
            try {
                mDao.update(config.toEntity())
                reload()
                mOnSuccess.onNext("数据已更新")
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext(
                    "更新数据时出错"
                )
            }
            mIsLoading.postValue(false)
        }
    }

    fun add(config: Config) {
        mIsLoading.value = true
        mHandler.post {
            try {
                mDao.add(config.toEntity())
                reload()
                mOnSuccess.onNext("数据已更新")
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext(
                    "输入的信息有错误，Host 或 Port 有误"
                )
            }
            mIsLoading.postValue(false)
        }
    }

    @WorkerThread
    private fun reload() {
        mConfigs.postValue(mDao.loadAll().map { it.toViewData() })
    }

    @Throws(Exception::class)
    private fun Config.toEntity(): ConfigEntity {
        val thiz = this
        return ConfigEntity()
            .apply {
                id = thiz.id
                name = if (thiz.name.isNullOrBlank()) {
                    "未命名服务器"
                } else {
                    this.name
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

    private fun ConfigEntity.toViewData(): Config {
        return Config(
            id = this.id,
            name = name,
            port = this.port.toString(),
            username = this.username,
            password = this.password,
            date = TimeUtils.millis2String(this.date)
        )
    }

    override fun onCleared() {
        mWorkerThread.quit()
    }
}