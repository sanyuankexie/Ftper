package org.kexie.android.ftper.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.kexie.android.ftper.model.bean.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {
    @Update
    void update(TaskEntity taskEntity);

    @Insert
    void add(TaskEntity taskEntity);

    @Query("delete from tasks where id=:id")
    void removeById(int id);

    @Query("select * from tasks")
    List<TaskEntity> loadAll();

    @Query("select * from tasks where id=:id")
    TaskEntity findById(int id);
}
