package org.kexie.android.ftper.model

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.kexie.android.ftper.R
import org.kexie.android.ftper.app.AppGlobal
import org.kexie.android.ftper.model.bean.ConfigEntity
import org.kexie.android.ftper.model.bean.WorkerEntity

abstract class TransferWorker(context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams) {

    protected lateinit var database: AppDatabase

    protected lateinit var worker: WorkerEntity

    protected lateinit var server: ConfigEntity

    protected lateinit var client: FTPClient

    @Throws(Throwable::class)
    protected fun prepare() {
        database = (applicationContext as AppGlobal).appDatabase
        worker = database.transferDao.findByWorkerId(id.toString())!!
        server = database.configDao.findById(worker.configId)!!
        when {
            worker.status == TransferStatus.DOWNLOAD_WAIT_START -> {
                worker.status = TransferStatus.DOWNLOADING
                database.transferDao.update(worker)
            }
            worker.status == TransferStatus.UPLOAD_WAIT_START -> {
                worker.status = TransferStatus.UPLOADING
                database.transferDao.update(worker)
            }
        }
        client = FTPClient()
            .apply {
                val timeout = 5000
                controlEncoding = applicationContext.getString(R.string.gbk)
                connectTimeout = timeout
                defaultTimeout = timeout
                connect(server.host, server.port)
                login(server.username, server.password)
                soTimeout = timeout
                setFileType(FTP.BINARY_FILE_TYPE)
            }
    }
}
