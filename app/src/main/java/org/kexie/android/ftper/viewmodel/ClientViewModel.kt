package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
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
import org.kexie.android.ftper.viewmodel.bean.ConfigItem
import org.kexie.android.ftper.viewmodel.bean.FileItem
import java.io.File
import java.io.IOException
import java.net.MalformedURLException

class ClientViewModel(application: Application)
    : AndroidViewModel(application) {

    /**
     * 使用[WorkManager]执行上传下载任务
     */
    private val mWorkManager = WorkManager.getInstance()
    /**
     * 轻量级的[HandlerThread]执行简单的删除和加载列表任务
     */
    private val mWorkerThread = HandlerThread(toString())
        .apply{
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

    }

    fun connect(configItem: ConfigItem?) {
        if (configItem == null) {
            mFiles.value = emptyList()
            return
        }
        mHandler.post {
            if (mClient.isConnected) {
                mClient.disconnect()
            }
            try {
                mClient.connect(configItem.host, configItem.port!!.toInt())
                mClient.login(configItem.username, configItem.password)
                if (!FTPReply.isPositiveCompletion(mClient.replyCode)) {
                    throw Exception()
                }
                mClient.enterLocalPassiveMode()
                mFiles.postValue(mClient.listFiles()
                    .map {
                        FileItem(
                            name = it.name,
                            size = it.size,
                            icon = ContextCompat.getDrawable(
                                getApplication(),
                                R.drawable.file
                            )!!,
                            type = it.type
                        )
                    })
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
        }
    }

    fun upload(file: File) {


    }

    fun download(fileItem: FileItem) {

    }

    fun mkdir() {

    }

    fun delete(fileItem: FileItem) {
        mIsLoading.value = true
        mHandler.post {
            try {
                mClient.dele(fileItem.name)
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