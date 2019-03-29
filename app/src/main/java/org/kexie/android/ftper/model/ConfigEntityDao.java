package org.kexie.android.ftper.model;

import androidx.room.*;
import org.kexie.android.ftper.model.bean.ConfigEntity;

import java.util.List;

@Dao
public interface ConfigEntityDao {

    @Update
    void update(ConfigEntity configEntity);

    @Insert
    void add(ConfigEntity configEntity);

    @Delete
    void remove(ConfigEntity configEntity);

    @Query("select * from configs")
    List<ConfigEntity> loadAll();

    @Query("select * from configs where id=:id")
    ConfigEntity findById(int id);
}
