package org.kexie.android.ftper.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

public final class UploadWorker extends TransferWorker {

    public UploadWorker(@NotNull Context context,
                        @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            FTPClient ftpClient = connect();
            BufferedInputStream buffer = new BufferedInputStream(
                    new FileInputStream(getConfig().getLocal())
            );
            Config config = getConfig();
            FTPFile[] files = ftpClient.listFiles(
                    config.getRemote()
                            + File.separator
                            + config.getLocal().getName());
            if (files.length == 1) {
                long remoteSize = files[0].getSize();
                if (remoteSize > 0) {
                    if (buffer.skip(remoteSize) == remoteSize) {
                        ftpClient.setRestartOffset(remoteSize);
                    }
                }
            } else if (files.length > 1) {
                throw new RuntimeException();
            }
            return ftpClient.storeFile(config.getLocal().getName(), buffer)
                    ? Result.success()
                    : Result.failure();
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
