package org.kexie.android.ftper.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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
            Config config = getConfig();
            FTPFile[] files = client.listFiles(config.getRemote());
            if (files.length == 0) {
                return Result.failure();
            } else if (files.length == 1) {
                File local = config.getLocal();
                if (!local.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    local.createNewFile();
                }
                if (local.length() >= files[0].getSize()) {
                    return Result.success();
                }
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(local, true)
                );
                client.setRestartOffset(local.length());
                InputStream input = client.retrieveFileStream(config.getRemote());
                byte[] b = new byte[1024];
                int length;
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                }
                out.flush();
                out.close();
                input.close();
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
