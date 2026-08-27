package com.thanhbinh.englishaiapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.thanhbinh.englishaiapp.data.local.entity.CollectionEntity;
import com.thanhbinh.englishaiapp.data.model.CollectionWithCount;

import java.util.List;

@Dao
public interface CollectionDao {
    @Insert
    long insert(CollectionEntity collection);

    @Update
    void update(CollectionEntity collection);

    @Delete
    void delete(CollectionEntity collection);

    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    LiveData<List<CollectionEntity>> getAllCollections();

    @Query("SELECT c.*, " +
           "(SELECT COUNT(*) FROM vocabularies v WHERE v.collectionId = c.id) AS wordCount, " +
           "(SELECT COUNT(*) FROM vocabularies v WHERE v.collectionId = c.id AND v.isLearned = 1) AS learnedCount " +
           "FROM collections c ORDER BY c.createdAt DESC")
    LiveData<List<CollectionWithCount>> getCollectionsWithCount();

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    LiveData<CollectionEntity> getCollectionById(long id);

    @Query("SELECT COUNT(*) FROM collections")
    LiveData<Integer> getTotalCollectionsCount();
}
