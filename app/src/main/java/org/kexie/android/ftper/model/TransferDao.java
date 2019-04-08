package org.kexie.android.ftper.model;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import org.kexie.android.ftper.model.bean.WorkerEntity;

import java.util.List;

@Dao
public interface TransferDao {

    @Update
    void update(WorkerEntity configEntity);

    @Insert
    void add(WorkerEntity configEntity);

    @Query("delete from workers where id=:id")
    void removeById(int id);

    @Query("select * from workers")
    List<WorkerEntity> loadAll();

    @Query("select * from workers where workerId=:id")
    WorkerEntity findByWorkerId(String id);

}
