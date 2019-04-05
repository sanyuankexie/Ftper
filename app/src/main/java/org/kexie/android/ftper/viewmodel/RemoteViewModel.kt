package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.UploadWorker
import org.kexie.android.ftper.model.bean.ConfigEntity
import org.kexie.android.ftper.viewmodel.bean.RemoteItem
import org.kexie.android.ftper.widget.Utils
import java.io.File


class RemoteViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mDataBase = getApplication<AppGlobal>().appDatabase

    private val mDao = mDataBase.configDao

    /**
     * 使用[WorkManager]执行上传下载任务
     */
    private val mWorkManager = WorkManager.getInstance()
    /**
     * 轻量级的[HandlerThread]执行简单的删除和加载列表任务
     */
    private val mWorkerThread = HandlerThread(toString())
            .apply {
                start()
                setUncaughtExceptionHandler { t, e ->
                    e.printStackTrace()
                    Logger.d(e)
                }
            }
    /**
     * [mWorkerThread]的[Handler]
     */
    private val mHandler = Handler(mWorkerThread.looper)
    /**
     * FTP协议的[FTPClient]
     */
    private val mClient = FTPClient().apply {
        controlEncoding = getApplication<Application>().getString(R.string.gbk)
        connectTimeout = 5000
        defaultTimeout = 5000
    }
    /**
     * 当前目录使用[LiveData]同步到View层
     */
    private val mCurrentDir = MutableLiveData<String>()
    /**
     * 使用[LiveData]列出文件列表
     */
    private val mFiles = MutableLiveData<List<RemoteItem>>()
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

    private lateinit var mConfig: ConfigEntity

    val currentDir: LiveData<String>
        get() = mCurrentDir

    val files: LiveData<List<RemoteItem>>
        get() = mFiles

    val isLoading: LiveData<Boolean>
        get() = mIsLoading

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    fun changeDir(path: String) {
        mIsLoading.value = true
        mHandler.post {
            var result: Boolean
            try {
                result = mClient.changeWorkingDirectory(path)
                if (result) {
                    refreshInternal()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                result = false
            }
            if (!result) {
                mOnError.onNext(
                        getApplication<Application>()
                                .getString(R.string.check_network)
                )
            }
            mIsLoading.postValue(false)
        }
    }

    fun refresh() {
        mIsLoading.value = true
        mHandler.post {
            try {
                if (mClient.isConnected) {
                    try {
                        refreshInternal()
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        clearAll()
                        mOnInfo.onNext(
                                getApplication<Application>()
                                        .getString(R.string.re_link)
                        )
                        connectDefault()
                    }
                } else {
                    connectDefault()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                clearAll()
                mOnError.onNext(
                        getApplication<Application>()
                                .getString(R.string.no_select_service)
                )
            }
            mIsLoading.postValue(false)
        }
    }

    fun connect(id: Int) {
        if (id == Int.MIN_VALUE) {
            mFiles.postValue(emptyList())
            return
        }
        mIsLoading.value = true
        mHandler.post {
            try {
                connectInternal(id)
                mOnSuccess.onNext(
                        getApplication<Application>()
                                .getString(R.string.ftp_connect_sucess)
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                mOnError.onNext(
                        getApplication<Application>()
                                .getString(R.string.other_error)
                )
            }
            mIsLoading.postValue(false)
        }
    }

    fun upload(file: File) {
        if (!mClient.isConnected) {
            mOnError.onNext(
                    getApplication<Application>()
                            .getString(R.string.no_select_service)
            )
            return
        }
        mHandler.post {
            try {
                val constraints = Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                val input = Data.Builder()
                        .putConfig(mConfig, file, mClient.printWorkingDirectory())
                        .build()

                val request = OneTimeWorkRequest
                        .Builder(UploadWorker::class.java)
                        .setConstraints(constraints)
                        .setInputData(input)
                        .build()

                mWorkManager.enqueue(request)

            } catch (e: Throwable) {
                e.printStackTrace()

            }
        }
    }

    fun download(remoteItem: RemoteItem) {


    }

    fun mkdir(name: String) {
        if (!mClient.isConnected) {
            mOnError.onNext(
                    getApplication<Application>()
                            .getString(R.string.no_select_service)
            )
            return
        }
        mHandler.post {
            var result: Boolean
            try {
                result = mClient.makeDirectory(name)
                if (result) {
                    refreshInternal()
                }
            } catch (e: Throwable) {
                e.printStackTrace()
                result = false
            }
            if (result) {
                mOnSuccess.onNext(
                        getApplication<Application>()
                                .getString(R.string.create_sucess)
                )
            } else {
                mOnError.onNext(
                        getApplication<Application>()
                                .getString(R.string.create_error)
                )
            }
        }
    }

    fun delete(remoteItem: RemoteItem) {
        if (getApplication<Application>().getString(R.string.uplayer_dir)
                == remoteItem.name
        ) {
            return
        }
        mIsLoading.value = true
        mHandler.post {
            try {
                if (remoteItem.isDirectory) {


                } else if (remoteItem.isFile) {
                    mClient.deleteFile(remoteItem.name)
                }
                refreshInternal()
                mOnSuccess.onNext(
                        getApplication<Application>()
                                .getString(R.string.del_success)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext(
                        getApplication<Application>()
                                .getString(R.string.del_error)
                )
            }
            mIsLoading.postValue(false)
        }
    }

    @WorkerThread
    private fun clearAll() {
        mCurrentDir.postValue("")
        mFiles.postValue(emptyList())
    }

    @Throws(Exception::class)
    @WorkerThread
    private fun connectDefault() {
        connectInternal(
                PreferenceManager
                        .getDefaultSharedPreferences(getApplication())
                        .getInt(
                                getApplication<Application>()
                                        .getString(R.string.select_key), Int.MIN_VALUE
                        ))
    }

    @Throws(Exception::class)
    @WorkerThread
    private fun refreshInternal() {
        mCurrentDir.postValue(mClient.printWorkingDirectory())
        mFiles.postValue(mClient.listFiles()
                .filter { it.name != getApplication<Application>().getString(R.string.dot) }
                .map {
                    RemoteItem(
                            name = it.name,
                            size = Utils.sizeToString(it.size),
                            icon = when {
                                it.name == getApplication<Application>()
                                        .getString(R.string.uplayer_dir) ->
                                    ContextCompat.getDrawable(
                                            getApplication(),
                                            R.drawable.up
                                    )!!
                                it.isDirectory -> ContextCompat.getDrawable(
                                        getApplication(),
                                        R.drawable.dir
                                )!!
                                else -> ContextCompat.getDrawable(
                                        getApplication(),
                                        R.drawable.file
                                )!!
                            },
                            type = it.type
                    )
                })
    }

    private fun Data.Builder.putConfig(configEntity: ConfigEntity,
                                       file: File,
                                       remote:String): Data.Builder {
        val context = getApplication<Application>()
        return this.putInt(context.getString(R.string.port_key), configEntity.port)
                .putString(context.getString(R.string.host_key), configEntity.host)
                .putString(context.getString(R.string.username_key), configEntity.username)
                .putString(context.getString(R.string.password_key), configEntity.password)
                .putString(getApplication<Application>()
                        .getString(R.string.local_key),
                        file.absolutePath)
                .putString(context.getString(R.string.remote_key),remote)
    }

    @Throws(Exception::class)
    @WorkerThread
    private fun connectInternal(id: Int) {
        if (mClient.isConnected) {
            mClient.disconnect()
        }
        mConfig = mDao.findById(id)
        mClient.connect(mConfig.host, mConfig.port)
        mClient.login(mConfig.username, mConfig.password)
        if (!FTPReply.isPositiveCompletion(mClient.replyCode)) {
            throw RuntimeException()
        }
        mClient.enterLocalPassiveMode()
        mClient.setFileType(FTP.BINARY_FILE_TYPE)
        refreshInternal()
    }

    override fun onCleared() {
        mHandler.postAtFrontOfQueue {
            if (mClient.isConnected) {
                try {
                    mClient.abort()
                } catch (e: Throwable) {
                    e.printStackTrace()
                }
            }
            mWorkerThread.quit()
        }
    }
}