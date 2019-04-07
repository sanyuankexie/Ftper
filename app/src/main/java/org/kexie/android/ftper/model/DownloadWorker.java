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
            //转为本地主动模式
            client.enterLocalActiveMode();
            FTPFile[] files = client.listFiles(getWorker().getRemote());
            if (files.length == 0) {
                //云端无文件
                return Result.failure();
            } else if (files.length == 1) {
                FTPFile remote = files[0];
                File local = new File(getWorker().getLocal());
                if (!local.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    local.createNewFile();
                }
                //本地文件大于或等于云端文件大小
                if (local.length() >= remote.getSize()) {
                    return Result.success();
                }
                //否则打开问价以append的方式
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(local, true)
                );
                //设置断点重传位置开始传输
                client.setRestartOffset(local.length());
                InputStream input = client.retrieveFileStream(getWorker().getRemote());
                byte[] b = new byte[1024];
                int length;
                long last = SystemClock.uptimeMillis();
                //读取文件并每秒刷新数据库
                while ((length = input.read(b)) != -1) {
                    out.write(b, 0, length);
                    long time = SystemClock.uptimeMillis();
                    if (time - last >= 1000) {
                        update(local.length(), remote.getSize());
                        last = time;
                    }
                }
                out.flush();
                out.close();
                input.close();
                //最后刷新数据库
                update(local.length(), remote.getSize());
                return client.completePendingCommand()
                        ? Result.success()
                        : Result.failure();
            } else {
                throw new RuntimeException();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            //发生奇怪的问题
            return Result.failure();
        }
    }
}
