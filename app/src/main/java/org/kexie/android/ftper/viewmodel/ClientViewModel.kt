package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import com.orhanobut.logger.Logger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.viewmodel.bean.FileItem
import java.io.File
import java.io.IOException
import java.net.MalformedURLException


class ClientViewModel(application: Application)
    : AndroidViewModel(application) {

    private val mDao = getApplication<AppGlobal>()
            .appDatabase
            .configDao

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
        controlEncoding = "gbk"
        connectTimeout = 5
    }
    /**
     * 当前目录使用[LiveData]同步到View层
     */
    private val mCurrentDir = MutableLiveData<String>()
    /**
     * 使用[LiveData]列出文件列表
     */
    private val mFiles = MutableLiveData<List<FileItem>>()
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

    val currentDir: LiveData<String>
        get() = mCurrentDir

    val files: LiveData<List<FileItem>>
        get() = mFiles

    val isLoading: LiveData<Boolean>
        get() = mIsLoading

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    fun changeDir(path: String) {
        mIsLoading.value = true
        mHandler.post {
            if (mClient.changeWorkingDirectory(path)) {
                refreshInternal()
            } else {
                mOnError.onNext("切换目录失败请检查网络连接")
            }
            mIsLoading.postValue(false)
        }
    }

    fun refresh() {
        if (!mClient.isConnected) {
            mOnError.onNext("未连接到服务器")
            return
        }
        mIsLoading.value = true
        mHandler.post {
            refreshInternal()
            mIsLoading.postValue(false)
        }
    }

    @WorkerThread
    private fun refreshInternal() {
        mCurrentDir.postValue(mClient.printWorkingDirectory())
        mClient.enterLocalPassiveMode()
        mFiles.postValue(mClient.listFiles()
                .filter { it.name != "." }
                .map {
                    FileItem(
                            name = it.name,
                            size = it.size,
                            icon = when {
                                it.name == ".." -> ContextCompat.getDrawable(
                                        getApplication(),
                                        R.drawable.up)!!
                                it.isDirectory -> ContextCompat.getDrawable(
                                        getApplication(),
                                        R.drawable.dir)!!
                                else -> ContextCompat.getDrawable(
                                        getApplication(),
                                        R.drawable.file)!!
                            },
                            type = it.type
                    )
                })
    }

    fun connect(id: Int) {
        if (id == Int.MIN_VALUE) {
            mFiles.value = emptyList()
            return
        }
        mIsLoading.value = true
        mHandler.post {
            if (mClient.isConnected) {
                mClient.disconnect()
            }
            try {
                val config = mDao.findById(id)
                mClient.connect(config.host, config.port)
                mClient.login(config.username, config.password)
                if (!FTPReply.isPositiveCompletion(mClient.replyCode)) {
                    throw Exception()
                }
                refreshInternal()
                mOnSuccess.onNext("FTP服务器连接成功")
            } catch (e: MalformedURLException) {
                e.printStackTrace()
                mOnError.onNext("host格式有误,请不要包含port")
            } catch (e: IOException) {
                e.printStackTrace()
                mOnError.onNext("连接失败,清检查网络连接")
            } catch (e: Throwable) {
                e.printStackTrace()
                mOnError.onNext("连接失败,参数有误")
            }
            mIsLoading.postValue(false)
        }
    }

    fun upload(file: File) {


    }

    fun download(fileItem: FileItem) {

    }

    fun mkdir(name: String) {

    }

    fun delete(fileItem: FileItem) {
        if (".." == fileItem.name) {
            return
        }
        mIsLoading.value = true
        mHandler.post {
            try {
                if (fileItem.isDirectory) {

                } else if (fileItem.isFile) {
                    mClient.deleteFile(fileItem.name)
                }
                refreshInternal()
                mOnSuccess.onNext("删除成功")
            } catch (e: Exception) {
                e.printStackTrace()
                mOnError.onNext("删除失败")
            }
            mIsLoading.postValue(false)
        }
    }

    override fun onCleared() {
        if (mClient.isConnected) {
            mClient.abort()
        }
        mWorkerThread.quit()
    }
}

