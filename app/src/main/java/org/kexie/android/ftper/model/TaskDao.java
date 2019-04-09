package org.kexie.android.ftper.model;

import androidx.room.*;
import org.kexie.android.ftper.model.bean.TaskEntity;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert
    long add(TaskEntity taskEntity);

    @Query("delete from tasks where id=:id")
    void removeById(int id);

    @Query("select * from tasks")
    List<TaskEntity> loadAll();

    @Query("select * from tasks where id=:id")
    TaskEntity findById(int id);

    @Query("update tasks set isFinish=1 where id=:id")
    void markFinish(int id);
}
