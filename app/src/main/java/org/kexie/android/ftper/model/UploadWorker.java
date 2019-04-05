package org.kexie.android.ftper.model;

import android.content.Context;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;
import org.kexie.android.ftper.R;

import java.io.OutputStream;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

public class UploadWorker extends FTPWorker {

    public UploadWorker(@NotNull Context context,
                        @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            FTPClient ftpClient = connect();
            FTPFile[] files = ftpClient.listFiles(getConfig().getRemote());
            if (files.length == 0) {
                return upload(ftpClient, 0);
            } else if (files.length == 1) {
                long localSize = getConfig().getLocal().length();
                long remoteSize = files[0].getSize();
                if (remoteSize >= localSize) {
                    return failure(FailureType.FILE_EXIT);
                } else {
                    return upload(ftpClient, remoteSize);
                }
            } else {
                throw new RuntimeException();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return failure(FailureType.UNKNOWN_ERROR);
        }
    }

    private Result upload(FTPClient client, long remoteSize) throws Exception {
        RandomAccessFile randomAccessFile = new RandomAccessFile(getConfig().getLocal(),
                getApplicationContext().getString(R.string.r));
        OutputStream outputStream = client.appendFileStream(getConfig().getRemote());
        if (remoteSize > 0) {
            client.setRestartOffset(remoteSize);
            randomAccessFile.seek(remoteSize);
        }
        byte[] bytes = new byte[1024];
        int c;
        while ((c = randomAccessFile.read(bytes)) != -1) {
            outputStream.write(bytes, 0, c);
        }
        outputStream.close();
        randomAccessFile.close();
        return client.completePendingCommand()
                ? Result.success()
                : failure(FailureType.UNKNOWN_ERROR);
    }
}
