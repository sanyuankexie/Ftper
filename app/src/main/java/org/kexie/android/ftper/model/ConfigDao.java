package org.kexie.android.ftper.model;

import org.kexie.android.ftper.model.bean.ConfigEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;


/**
*像不像hibernate?
*/
@Dao
public interface ConfigDao {

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
