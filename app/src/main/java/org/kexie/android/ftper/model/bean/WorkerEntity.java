package org.kexie.android.ftper.model.bean;

import org.kexie.android.ftper.model.WorkerType;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workers")
public class WorkerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @WorkerType
    private int type;

    private long doSize = 0;

    private long size = 0;

    private String workerId;

    private String name;

    private int configId;

    private String remote;

    private String local;

    public void setType(@WorkerType int type) {
        this.type = type;
    }

    @WorkerType
    public int getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public int getConfigId() {
        return configId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getWorkerId() {
        return workerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getDoSize() {
        return doSize;
    }

    public void setDoSize(long doSize) {
        this.doSize = doSize;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getRemote() {
        return remote;
    }

    public void setRemote(String remote) {
        this.remote = remote;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }
}