package org.kexie.android.ftper.model;

import org.kexie.android.ftper.model.bean.TransferEntity;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TransferDao {

    @Update
    void update(TransferEntity configEntity);

    @Insert
    void add(TransferEntity configEntity);

    @Delete
    void remove(TransferEntity configEntity);

    @Query("select * from transfers")
    List<TransferEntity> loadAll();

    @Query("select * from transfers where id=:id")
    TransferEntity findById(int id);

}
