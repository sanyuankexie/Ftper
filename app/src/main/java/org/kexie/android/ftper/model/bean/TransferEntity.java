package org.kexie.android.ftper.model.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transfers")
public class TransferEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String workerId;

    private String name;

    private int doSize;

    private int type;

    private int size;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDoSize() {
        return doSize;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDoSize(int doSize) {
        this.doSize = doSize;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }
}
