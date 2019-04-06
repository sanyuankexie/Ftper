package org.kexie.android.ftper.model;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.kexie.android.ftper.model.TransferStatus.*;

@IntDef({UPLOAD_WAIT_START,
        DOWNLOAD_WAIT_START,
        UPLOADING,
        DOWNLOADING,
        UPLOAD_FINISH,
        DOWNLOAD_FINISH,
        UPLOAD_FAILED,
        DOWNLOAD_FAILED})
@Retention(RetentionPolicy.SOURCE)
public @interface TransferStatus {
    // 说明任务还没有开始调度,
    // 有两种可能
    // 一是任务没有成功提交
    // 二是提交了还没有开始
    // 通过WorkerManager检查是否提交,若未提交应该重新创建请求
    int UPLOAD_WAIT_START = 0;
    int DOWNLOAD_WAIT_START = 1;
    // 任务已经开始
    int UPLOADING = 2;
    int DOWNLOADING = 3;
    // 任务完成
    int UPLOAD_FINISH = 4;
    int DOWNLOAD_FINISH = 5;
    // 任务因一些原因失败
    int UPLOAD_FAILED = 6;
    int DOWNLOAD_FAILED = 7;
}
