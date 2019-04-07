package org.kexie.android.ftper.model

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.orhanobut.logger.Logger
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.bean.WorkerEntity

abstract class TransferWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    private lateinit var database: AppDatabase

    protected lateinit var worker: WorkerEntity

    protected lateinit var client: FTPClient

    @WorkerThread
    @Throws(Throwable::class)
    protected fun prepare() {
        database = (applicationContext as AppGlobal).appDatabase
        //查找相关信息找不到直接报错
        worker = database.transferDao.findByWorkerId(id.toString())!!
        val server = database.configDao.findById(worker.configId)!!
        client = FTPClient()
                .apply {
                    //5秒超时
                    val timeout = 5000
                    controlEncoding = applicationContext.getString(R.string.gbk)
                    connectTimeout = timeout
                    defaultTimeout = timeout
                    //连接到服务器
                    connect(server.host, server.port)
                    login(server.username, server.password)
                    soTimeout = timeout
                    //设置传输形式为二进制
                    setFileType(FTP.BINARY_FILE_TYPE)
                }
    }

    //更新数据库
    @WorkerThread
    protected fun update(doSize: Long, size: Long) {
        Logger.d("$doSize/$size")
        worker.doSize = doSize
        worker.size = size
        database.transferDao.update(worker)
    }
}
