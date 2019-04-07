package org.kexie.android.ftper.model;

import android.content.Context;
import android.os.SystemClock;

import com.orhanobut.logger.Logger;

import org.apache.commons.net.ftp.FTPFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

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
            // 本地文件的长度
            long localSize = local.length();
            long serverSize = 0;
            String serverName = getWorker().getName();
            int lastIndex = getWorker().getRemote().lastIndexOf('/');
            if (lastIndex != 0) {
                String workDir = getWorker().getRemote().substring(0, lastIndex);
                Logger.d(workDir);
                getClient().changeWorkingDirectory(workDir);
            }
            FTPFile[] files = getClient().listFiles(getWorker().getRemote());
            if (files.length == 1) {
                FTPFile ftpFile = files[0];
                serverSize = ftpFile.getSize();
                serverName = ftpFile.getName();
            } else if (files.length > 1) {
                throw new RuntimeException();
            }
            if (serverSize >= localSize) {
                return Result.success();
            }
            RandomAccessFile raf = new RandomAccessFile(local, "r");
            getClient().setRestartOffset(serverSize);
            raf.seek(serverSize);
            OutputStream output = client.appendFileStream(serverName);
            byte[] b = new byte[1024];
            long last = SystemClock.uptimeMillis();
            int length;
            while ((length = raf.read(b)) != -1) {
                output.write(b, 0, length);
                serverSize += length;
                long time = SystemClock.uptimeMillis();
                if (time - last >= 1000) {
                    update(serverSize, local.length());
                    last = time;
                }
            }
            output.flush();
            output.close();
            raf.close();
            update(serverSize, local.length());
            return client.completePendingCommand()
                    ? Result.success()
                    : Result.failure();
        } catch (Throwable e) {
            e.printStackTrace();
            return Result.failure();
        }
    }
}
