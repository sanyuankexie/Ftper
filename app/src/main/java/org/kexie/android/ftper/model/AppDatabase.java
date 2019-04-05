package org.kexie.android.ftper.model;

import org.kexie.android.ftper.model.bean.ConfigEntity;
import org.kexie.android.ftper.model.bean.TransferEntity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ConfigEntity.class, TransferEntity.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract ConfigDao getConfigDao();

    public abstract TransferDao getTransferDao();
}
