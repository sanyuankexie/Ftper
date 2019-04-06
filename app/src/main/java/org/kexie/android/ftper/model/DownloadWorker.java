package org.kexie.android.ftper.model;

import android.content.Context;
import android.os.SystemClock;

import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

public final class DownloadWorker extends TransferWorker {

    public DownloadWorker(@NotNull Context context,
                          @NotNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        try {
            prepare();
            client.enterLocalActiveMode();
            FTPFile[] files = client.listFiles(getWorker().getRemote());
            if (files.length == 0) {
                return Result.failure();
            } else if (files.length == 1) {
                FTPFile remote = files[0];
                File local = new File(getWorker().getLocal());
                if (!local.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    local.createNewFile();
                }
                if (local.length() >= remote.getSize()) {
                    return Result.success();
                }
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(local, true)
                );
                client.setRestartOffset(local.length());
                InputStream input = client.retrieveFileStream(getWorker().getRemote());
                long last = SystemClock.uptimeMillis();
                byte[] b = new byte[1024];
                int length;
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                    long time = SystemClock.uptimeMillis();
                    if (time - last > 1000) {
                        last = time;
                        update(local.length(), remote.getSize());
                    }
                }
                out.flush();
                out.close();
                input.close();
                update(local.length(), remote.getSize());
                return client.completePendingCommand()
                        ? Result.success()
                        : Result.failure();
            } else {
                throw new RuntimeException();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
