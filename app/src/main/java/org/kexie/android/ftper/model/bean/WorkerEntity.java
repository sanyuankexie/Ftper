package org.kexie.android.ftper.model.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import org.kexie.android.ftper.model.TransferStatus;

@Entity(tableName = "workers")
public class WorkerEntity {

    @PrimaryKey
    private int id;

    private String workerId;

    private String name;

    private long doSize;

    @TransferStatus
    private int status;

    private long size;

    private int configId;

    private String remote;

    private String local;

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

    public void setStatus(@TransferStatus int status) {
        this.status = status;
    }

    @TransferStatus
    public int getStatus() {
        return status;
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