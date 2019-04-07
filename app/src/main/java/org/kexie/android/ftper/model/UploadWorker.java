package org.kexie.android.ftper.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
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
            prepare();
            getClient().enterLocalPassiveMode();
            File local = new File(getWorker().getLocal());
            if (!local.isFile()) {
                throw new RuntimeException();
            }
            BufferedInputStream buffer = new BufferedInputStream(
                    new FileInputStream(local)
            );
            FTPFile[] files = getClient().listFiles(getWorker().getRemote());
            if (files.length == 1) {
                long remoteSize = files[0].getSize();
                if (remoteSize > 0) {
                    if (buffer.skip(remoteSize) == remoteSize) {
                        getClient().setRestartOffset(remoteSize);
                    }
                }
            } else if (files.length > 1) {
                throw new RuntimeException();
            }
            return getClient().storeFile(local.getName(), buffer)
                    ? Result.success()
                    : Result.failure();
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
