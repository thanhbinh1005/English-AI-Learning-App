package com.thanhbinh.englishaiapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "collections")
public class CollectionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String description;
    private String accentColor;
    private long createdAt;

    public CollectionEntity(String name, String description, String accentColor, long createdAt) {
        this.name = name;
        this.description = description;
        this.accentColor = accentColor;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
