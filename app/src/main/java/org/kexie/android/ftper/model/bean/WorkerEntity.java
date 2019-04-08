package org.kexie.android.ftper.model.bean;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import org.kexie.android.ftper.model.WorkerType;
import org.kexie.android.ftper.widget.Utils;

import java.io.File;
import java.util.UUID;


@Entity(tableName = "workers",indices = @Index("workerId"))
public class WorkerEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @WorkerType
    private int type;

    private long doSize = 0;

    private long size = 0;

    @TypeConverters(Utils.class)
    private UUID workerId;

    @TypeConverters(Utils.class)
    private File local;

    private String name;

    private int configId;

    private String remote;

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

    @TypeConverters(Utils.class)
    public UUID getWorkerId() {
        return workerId;
    }

    @TypeConverters(Utils.class)
    public void setWorkerId(UUID workerId) {
        this.workerId = workerId;
    }

    @TypeConverters(Utils.class)
    public File getLocal() {
        return local;
    }

    @TypeConverters(Utils.class)
    public void setLocal(File local) {
        this.local = local;
    }

}