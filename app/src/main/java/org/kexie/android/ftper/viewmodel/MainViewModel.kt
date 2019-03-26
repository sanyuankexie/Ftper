package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.preference.PreferenceManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.WorkManager
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.viewmodel.bean.RemoteFile
import java.io.File
import java.io.IOException
import java.net.MalformedURLException

class MainViewModel(application: Application)
    : AndroidViewModel(application) {

    companion object {
        const val portKey = "port"
        const val hostKey = "host"
        const val usernameKey = "username"
        const val passwordKey = "password"
    }

    /**
     * 用来保存用户基本数据
     */
    private val sharedPreference = PreferenceManager
        .getDefaultSharedPreferences(application)
    /**
     * 使用[WorkManager]执行上传下载任务
     */
    private val mWorkManager = WorkManager.getInstance()
    /**
     * 轻量级的[HandlerThread]执行简单的删除和加载列表任务
     */
    private val mWorkerThread = HandlerThread(toString()).apply {
        start()
    }
    /**
     * [mWorkerThread]的[Handler]
     */
    private val mHandler = Handler(mWorkerThread.looper)
    /**
     * FTP协议的[FTPClient]
     */
    private val mClient = FTPClient().apply {
        controlEncoding = "utf-8"
    }
    /**
     * 当前目录使用[LiveData]同步到View层
     */
    private val mCurrentDir = MutableLiveData<String>()
    /**
     * 使用[LiveData]列出文件列表
     */
    private val mFiles = MutableLiveData<List<RemoteFile>>()
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

    val files: LiveData<List<RemoteFile>>
        get() = mFiles

    val isLoading: LiveData<Boolean>
        get() = mIsLoading

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    fun connect(
        host: String,
        port: Int,
        username: String,
        password: String
    ) {
        if (mClient.isConnected) {
            mClient.disconnect()
        }
        try {
            mClient.connect(host, port)
            mClient.login(username, password)
            mOnSuccess.onNext("FTP服务器连接成功")
        } catch (e: MalformedURLException) {
            e.printStackTrace()
            mOnError.onNext("host格式有误,请不要包含port")
        } catch (e: IOException) {
            e.printStackTrace()
            mOnError.onNext("连接失败,清检查网络连接")
        } catch (e: Exception) {
            e.printStackTrace()
            mOnError.onNext("未知错误")
        }
    }

    fun upload(file: File) {


    }

    fun download(file: RemoteFile) {
    }

    fun mkdir() {

    }

    fun delete(file: RemoteFile) {
        mIsLoading.value = true
        mHandler.post {
            try {
                mClient.dele(file.name)
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