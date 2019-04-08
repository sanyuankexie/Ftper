package org.kexie.android.ftper.viewmodel

import android.app.Application
import android.graphics.drawable.Drawable
import android.os.AsyncTask
import android.os.Handler
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
import org.kexie.android.ftper.model.bean.TaskEntity
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

    private val mTaskDao = getApplication<AppGlobal>()
        .appDatabase
        .taskDao

    private val mConfigDao = getApplication<AppGlobal>()
        .appDatabase
        .configDao

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

    private val mExecutor = Executors.newCachedThreadPool()

    private val mSync = Handler.createAsync(Looper.getMainLooper()) {

        return@createAsync true
    }

    private lateinit var mIcons: Array<Drawable>

    private val mRunningTask = SparseArrayCompat<TransferTask>()

    val onError: Observable<String> = mOnError.observeOn(AndroidSchedulers.mainThread())

    val onSuccess: Observable<String> = mOnSuccess.observeOn(AndroidSchedulers.mainThread())

    val onInfo: Observable<String> = mOnInfo.observeOn(AndroidSchedulers.mainThread())

    //妥协
    private val mAdapter = TaskItemQuickAdapter()

    val adapter: GenericQuickAdapter<TaskItem>
        get() = mAdapter

    init {
        mExecutor.execute {
            mIcons = arrayOf(
                ContextCompat.getDrawable(getApplication(), R.drawable.up)!!,
                ContextCompat.getDrawable(getApplication(), R.drawable.dl)!!
            )
            val items = mTaskDao
                .loadAll()
                .map {
                    it.toReadabilityData()
                }
            mSync.post {
                mAdapter.setNewData(items)
            }
        }
    }

    fun start(taskId: Int) {
        val item = mAdapter.data.firstOrNull { taskId == it.id }
        if (item != null) {
            startInternal(taskId)
        } else {
            mExecutor.submit {
                mTaskDao.findById(taskId)?.let { task ->
                    val newItem = task.toReadabilityData()
                    mSync.post {
                        mAdapter.addData(newItem)
                        startInternal(taskId)
                    }
                }
            }
        }
    }

    fun pause(taskId: Int) {


    }

    fun remove(taskId: Int) {


    }

    private fun startInternal(taskId: Int) {
        val runTask = mRunningTask[taskId]

    }

    @WorkerThread
    private fun TaskEntity.toReadabilityData(): TaskItem {
        return TaskItem(
            id = this.id,
            name = this.name,
            percent = 0,
            state = TaskState.WAIT_START,
            icon = mIcons[this.type],
            size = ""
        )
    }

    override fun onCleared() {
        mExecutor.shutdown()
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
        protected val config: Config
    ) : AsyncTask<Unit, Progress, ResultType>() {

        companion object {
            const val UPDATE = 10001
            const val FINISH = 10002
        }

        private val mNext = AtomicBoolean(false)
        protected val next
            get() = mNext.get()

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

        fun makeResult(type: ResultType): Result {
            return Result(config.id, type)
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
                        return ResultType.ERROR
                    }
                    files.size == 1 -> {
                        val remote = files[0]
                        val local = config.local
                        if (!local.exists()) {
                            local.createNewFile()
                        }
                        //本地文件大于或等于云端文件大小
                        if (local.length() >= remote.size) {
                            return ResultType.ERROR
                        }
                        //设置断点重传位置开始传输
                        client.restartOffset = local.length();
                        val input = client.retrieveFileStream(config.remote)
                        //否则打开问以append的方式
                        val out = BufferedOutputStream(FileOutputStream(local, true))
                        val buffer = ByteArray(1024)
                        while (true) {
                            if (!next) {
                                return ResultType.CANCELLED
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
                            ResultType.ERROR
                    }
                    else -> {
                        throw RuntimeException()
                    }
                }
            } catch (e: Throwable) {
                //发生奇怪的问题
                e.printStackTrace()
                return ResultType.ERROR
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
                        return ResultType.CANCELLED
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
                    ResultType.ERROR
            } catch (e: Throwable) {
                e.printStackTrace()
                return ResultType.ERROR
            }
        }
    }
}
