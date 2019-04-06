package org.kexie.android.ftper.model;

import androidx.room.*;
import org.kexie.android.ftper.model.bean.WorkerEntity;

import java.util.List;

@Dao
public interface TransferDao {

    @Update
    void update(WorkerEntity configEntity);

    @Insert
    void add(WorkerEntity configEntity);

    @Delete
    void remove(WorkerEntity configEntity);

    @Query("select * from workers")
    List<WorkerEntity> loadAll();

    @Query("select * from workers where id=:id")
    WorkerEntity findById(String id);

}
