package com.thanhbinh.englishaiapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "vocabularies",
    foreignKeys = @ForeignKey(
        entity = CollectionEntity.class,
        parentColumns = "id",
        childColumns = "collectionId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("collectionId")}
)
public class VocabularyEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private long collectionId;
    private String term;
    private String meaning;
    private String example;
    private boolean isLearned;
    private long createdAt;

    public VocabularyEntity(long collectionId, String term, String meaning, String example, boolean isLearned, long createdAt) {
        this.collectionId = collectionId;
        this.term = term;
        this.meaning = meaning;
        this.example = example;
        this.isLearned = isLearned;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(long collectionId) {
        this.collectionId = collectionId;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public boolean isLearned() {
        return isLearned;
    }

    public void setLearned(boolean learned) {
        isLearned = learned;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
