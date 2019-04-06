package org.kexie.android.ftper.model.bean;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "workers")
public class WorkerEntity {

    @PrimaryKey
    @NonNull
    private String id = UUID.randomUUID().toString();

    private String name;

    private int doSize;

    private int type;

    private int size;

    @ForeignKey(entity = ConfigEntity.class,
            parentColumns = {"id"},
            childColumns = "configId")
    private int configId;

    private String remote;

    private String local;

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public int getConfigId() {
        return configId;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDoSize() {
        return doSize;
    }

    public void setDoSize(int doSize) {
        this.doSize = doSize;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
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