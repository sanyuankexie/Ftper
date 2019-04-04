package org.kexie.android.ftper.model;

import org.kexie.android.ftper.model.bean.ConfigEntity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ConfigEntity.class},version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ConfigEntityDao getConfigDao();
}
