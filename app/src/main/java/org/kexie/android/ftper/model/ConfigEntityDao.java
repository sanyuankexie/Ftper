package org.kexie.android.ftper.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.kexie.android.ftper.model.bean.ConfigEntity;

import java.util.List;

@Dao
public interface ConfigEntityDao {

    @Update
    void update(ConfigEntity configEntity);

    @Insert
    void add(ConfigEntity configEntity);

    @Query("select * from configs")
    List<ConfigEntity> loadAll();
}
