package org.kexie.android.ftper.model;

import org.kexie.android.ftper.model.bean.ConfigEntity;
import org.kexie.android.ftper.model.bean.WorkerEntity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ConfigEntity.class, WorkerEntity.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    //服务器配置
    public abstract ConfigDao getConfigDao();
    //传输配置
    public abstract TransferDao getTransferDao();
}
