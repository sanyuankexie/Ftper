package org.kexie.android.ftper.model;

import androidx.room.*;
import org.kexie.android.ftper.model.bean.ConfigEntity;

import java.util.List;


/**
*像不像hibernate?
*/
@Dao
public interface ConfigDao {

    @Update
    void update(ConfigEntity configEntity);

    @Insert
    long add(ConfigEntity configEntity);

    @Query("delete from configs where id=:id")
    void removeById(int id);

    @Query("select * from configs")
    List<ConfigEntity> loadAll();

    @Query("select * from configs where id=:id")
    ConfigEntity findById(int id);
}
