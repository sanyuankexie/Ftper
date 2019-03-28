package org.kexie.android.ftper.model;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import org.kexie.android.ftper.model.bean.ConfigEntity;

@Database(entities = {ConfigEntity.class},version = 4)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ConfigEntityDao getConfigDao();
}
