package org.kexie.android.ftper.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import org.apache.commons.net.ftp.FTPClient;
import org.jetbrains.annotations.NotNull;

public final class DownloadWorker extends TransferWorker {

    public DownloadWorker(@NotNull Context context,
                          @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            FTPClient client = connect();
            client.enterLocalActiveMode();

            return Result.success();
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
