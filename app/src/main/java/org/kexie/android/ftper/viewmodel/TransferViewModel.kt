package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.collection.SparseArrayCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.WorkerType
import org.kexie.android.ftper.model.bean.TaskEntity
import org.kexie.android.ftper.viewmodel.TransferViewModel.ResultType.CANCELLED
import org.kexie.android.ftper.viewmodel.TransferViewModel.ResultType.ERROR
import org.kexie.android.ftper.viewmodel.bean.TaskItem
import org.kexie.android.ftper.viewmodel.bean.TaskState
import org.kexie.android.ftper.widget.GenericQuickAdapter
import org.kexie.android.ftper.widget.TaskItemQuickAdapter
import org.kexie.android.ftper.widget.Utils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class TransferViewModel(application: Application)
    : AndroidViewModel(application) {

    companion object {
        private const val START = 10000
        private const val UPDATE = 10001
        private const val FINISH = 10002
    }

    private val mTaskDao = getApplication<AppGlobal>()
        .appDatabase
        .taskDao

    private val mConfigDao = getApplication<AppGlobal>()
        .appDatabase
        .configDao

    //数据库操作工作的线程
    private val mDatabaseThread = HandlerThread(toString())
        .apply {
            start()
            Handler(looper).post {
                mIcons = arrayOf(
                    ContextCompat.getDrawable(getApplication(), R.drawable.up)!!,
                    ContextCompat.getDrawable(getApplication(), R.drawable.dl)!!
                )
                val items = mTaskDao
                    .loadAll()
                    .map {
                        it.toReadabilityData()
                    }
                mMainWorker.post {
                    mAdapter.setNewData(items)
                }
            }
        }

    private val mOnStart = PublishSubject.create<Int>()

    private val mOnRemove = PublishSubject.create<Int>()

    private val mDisposables = arrayOf(
        mOnStart.doOnNext { taskId ->
            mRunningTask[taskId]?.let {
                if (!it.isCancelled) {
                    throw RuntimeException()
                }
            }
        }.observeOn(AndroidSchedulers.from(mDatabaseThread.looper))
            .map { taskId ->
                return@map mTaskDao.findById(taskId)?.let {
                    if (it.isFinish) {
                        throw RuntimeException()
                    }
                    return@let it
                }
            }.map { task ->
                val config = mConfigDao.findById(task.configId)
                if (config == null) {
                    return@map mMainWorker.obtainMessage(FINISH)
                        .apply {
                            obj = Result(task.id, ERROR)
                        }
                } else {
                    val taskConfig = Config(
                        id = task.id,
                        local = task.local,
                        remote = task.remote,
                        port = config.port,
                        host = config.host,
                        username = config.username,
                        password = config.password
                    )
                    val newTask = if (WorkerType.DOWNLOAD == task.type) {
                        UploadTask(mMainWorker, taskConfig)
                    } else {
                        DownloadTask(mMainWorker, taskConfig)
                    }
                    mMainWorker.obtainMessage(START)
                        .apply {
                            obj = newTask
                        }
                }
            }.subscribe({
                it.sendToTarget()
            }, {
                it.printStackTrace()
            }),
        mOnRemove.doOnNext { taskId ->
            pause(taskId)
            mRunningTask.remove(taskId)
        }.observeOn(AndroidSchedulers.from(mDatabaseThread.looper))
            .doOnNext { taskId ->
                mTaskDao.removeById(taskId)
            }.observeOn(AndroidSchedulers.mainThread())
            .subscribe { taskId ->
                mAdapter.data
                    .indexOfFirst { it.id == taskId }
                    .run {
                        if (this != -1) {
                            mAdapter.remove(this)
                        }
                    }
            })

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

    private val mTaskExecutor = Executors.newCachedThreadPool()

    private val mRunningTask = SparseArrayCompat<TransferTask>()

    private lateinit var mIcons: Array<Drawable>

    private val mMainWorker = Handler(Looper.getMainLooper())
    {
        val what = it.what
        val obj = it.obj
        when {
            what == START && obj is TransferTask -> {
                obj.executeOnExecutor(mTaskExecutor)
                mRunningTask.put(obj.config.id, obj)
                return@Handler true
            }
            what == UPDATE && obj is Progress -> {
                val index = mAdapter.data
                    .indexOfFirst { item -> item.id == obj.id }
                if (index != -1) {
                    val newItem = mAdapter.getItem(index)!!.copy(
                        percent = obj.progress,
                        size = obj.size
                    )
                    mAdapter.setData(index, newItem)
                }
                return@Handler true
            }
            what == FINISH && obj is Result -> {
                val index = mAdapter.data
                    .indexOfFirst { item -> item.id == obj.id }
                if (index != -1) {
                    val newItem = mAdapter.getItem(index)!!.copy(
                        percent = 100,
                        state = when (obj.type) {
                            ResultType.CANCELLED -> TaskState.WAIT_START
                            ResultType.FINISH -> TaskState.FINISH
                            ResultType.ERROR -> TaskState.FAILED
                        }
                    )
                    mAdapter.setData(index, newItem)
                }
                return@Handler true
            }
            else -> {
                return@Handler false
            }
        }
    }

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    //妥协
    private val mAdapter = TaskItemQuickAdapter()

    val adapter: GenericQuickAdapter<TaskItem>
        get() = mAdapter

    @MainThread
    fun start(taskId: Int) {
        val item = mAdapter.data
            .firstOrNull { taskId == it.id }
        if (item != null) {
            mOnStart.onNext(taskId)
        } else {
            Observable.just(taskId)
                .observeOn(AndroidSchedulers.from(mDatabaseThread.looper))
                .map {
                    mTaskDao.findById(taskId)
                }.map {
                    it.toReadabilityData()
                }.observeOn(AndroidSchedulers.mainThread())
                .doOnNext {
                    mAdapter.addData(it)
                }.map {
                    it.id
                }.subscribe(mOnStart)
        }
    }

    @MainThread
    fun pause(taskId: Int) {
        mRunningTask[taskId]?.let {
            if (it.isCancelled) {
                it.cancel(false)
            }
        }
    }

    @MainThread
    fun remove(taskId: Int) {
        mOnRemove.onNext(taskId)
    }

    @WorkerThread
    private fun TaskEntity.toReadabilityData(): TaskItem {
        return TaskItem(
            id = this.id,
            name = this.name,
            percent = 0,
            state = TaskState.WAIT_START,
            icon = mIcons[this.type],
            size = getApplication<Application>().getString(R.string.loading_text)
        )
    }

    override fun onCleared() {
        mTaskExecutor.shutdown()
        for (i in 0 until mRunningTask.size()) {
            mRunningTask[i]?.let {
                if (!it.isCancelled) {
                    it.cancel(false)
                }
            }
        }
        mDisposables.forEach {
            it.dispose()
        }
        mRunningTask.clear()
        mDatabaseThread.quit()
    }

    private class Config(
        val id: Int,
        val local: File,
        val remote: String,
        val host: String,
        val port: Int,
        val username: String,
        val password: String
    )

    private class Progress(
        val id: Int,
        val size: String,
        val progress: Int
    )

    private class Result(
        val id: Int,
        val type: ResultType
    )

    private enum class ResultType {
        CANCELLED,
        FINISH,
        ERROR
    }

    private abstract class TransferTask(
        protected val handler: Handler,
        val config: Config
    ) : AsyncTask<Unit, Progress, ResultType>() {

        private val mNext = AtomicBoolean(false)
        protected val next
            get() = mNext.get()

        @WorkerThread
        override fun onCancelled(result: ResultType) {
            handler.obtainMessage(FINISH)
                .apply {
                    obj = Result(config.id, CANCELLED)
                    sendToTarget()
                }
        }

        @WorkerThread
        protected fun connect(): FTPClient {
            if (mNext.compareAndSet(true, true)) {
                throw AssertionError()
            }
            return FTPClient()
                .apply {
                    //5秒超时
                    val timeout = 5000
                    controlEncoding = "gbk"
                    connectTimeout = timeout
                    defaultTimeout = timeout
                    //连接到服务器
                    connect(config.host, config.port)
                    login(config.username, config.password)
                    soTimeout = timeout
                    //设置传输形式为二进制
                    setFileType(FTP.BINARY_FILE_TYPE)
                }
        }

        @WorkerThread
        protected fun publishProgress(doSize: Long, size: Long) {
            val sizeText = "${Utils.sizeToString(doSize)}/${Utils.sizeToString(size)}"
            val progress = (doSize.toFloat() / size.toFloat() * 100f).toInt()
            publishProgress(Progress(config.id, sizeText, progress))
        }

        @MainThread
        override fun onCancelled() {
            mNext.set(false)
        }

        override fun onPostExecute(resultType: ResultType) {
            handler.obtainMessage(FINISH)
                .apply {
                    obj = Result(config.id, resultType)
                    sendToTarget()
                }
        }

        @MainThread
        override fun onProgressUpdate(vararg values: Progress) {
            handler.obtainMessage(UPDATE)
                .apply {
                    obj = values[0]
                    sendToTarget()
                }
        }
    }

    private class DownloadTask(
        handler: Handler,
        config: Config
    ) : TransferTask(handler, config) {
        @WorkerThread
        override fun doInBackground(vararg params: Unit): ResultType {
            try {
                val client = connect()
                client.enterLocalActiveMode()
                val files = client.listFiles(config.remote)
                when {
                    //云端无文件
                    files.isEmpty() -> {
                        return ERROR
                    }
                    files.size == 1 -> {
                        val remote = files[0]
                        val local = config.local
                        if (!local.exists()) {
                            local.createNewFile()
                        }
                        //本地文件大于或等于云端文件大小
                        if (local.length() >= remote.size) {
                            return ERROR
                        }
                        //设置断点重传位置开始传输
                        client.restartOffset = local.length();
                        val input = client.retrieveFileStream(config.remote)
                        //否则打开问以append的方式
                        val out = BufferedOutputStream(FileOutputStream(local, true))
                        val buffer = ByteArray(1024)
                        while (true) {
                            if (!next) {
                                return CANCELLED
                            }
                            val length = input.read(buffer)
                            if (length == -1) {
                                break
                            }
                            out.write(buffer, 0, length)
                            publishProgress(local.length(), remote.size)
                        }
                        out.flush()
                        out.close()
                        input.close()
                        return if (client.completePendingCommand())
                            ResultType.FINISH
                        else
                            ERROR
                    }
                    else -> {
                        throw RuntimeException()
                    }
                }
            } catch (e: Throwable) {
                //发生奇怪的问题
                e.printStackTrace()
                return ERROR
            }
        }
    }

    private class UploadTask(
        handler: Handler,
        config: Config
    ) : TransferTask(handler, config) {
        override fun doInBackground(vararg params: Unit): ResultType {
            try {
                val client = connect()
                val local = config.local
                if (local.isFile) {
                    throw RuntimeException()
                }
                val localSize = local.length()
                var remoteName: String
                var remoteSize = 0L
                config.remote.lastIndexOf('/')
                    .run {
                        if (this != 0) {
                            client.changeWorkingDirectory(config.remote.substring(0, this))
                        }
                        remoteName = config.remote.let {
                            it.substring(this + 1, it.length)
                        }
                    }
                val files = client.listFiles(config.remote)
                when {
                    files.size == 1 -> {
                        val file = files[0]
                        remoteSize = file.size
                        remoteName = file.name
                    }
                    files.size > 1 -> {
                        throw RuntimeException()
                    }
                }
                if (remoteSize >= localSize) {
                    return ResultType.FINISH
                }
                val raf = RandomAccessFile(local, "r");
                client.restartOffset = remoteSize
                raf.seek(remoteSize)
                val out = client.appendFileStream(remoteName)
                val buffer = ByteArray(1024)
                while (true) {
                    if (!next) {
                        return CANCELLED
                    }
                    val length = raf.read(buffer)
                    if (length == -1) {
                        break
                    }
                    remoteSize += length
                    out.write(buffer, 0, length)
                    publishProgress(local.length(), remoteSize)
                }
                out.flush()
                out.close()
                raf.close()
                return if (client.completePendingCommand())
                    ResultType.FINISH
                else
                    ERROR
            } catch (e: Throwable) {
                e.printStackTrace()
                return ERROR
            }
        }
    }
}