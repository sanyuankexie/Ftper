package org.kexie.android.ftper.model.bean;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transfers")
public class TransferEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private int percent;
    private int type;
    private int size;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPercent() {
        return percent;
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

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
