package com.thanhbinh.englishaiapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.thanhbinh.englishaiapp.data.local.entity.VocabularyEntity;

import java.util.List;

@Dao
public interface VocabularyDao {
    @Insert
    long insert(VocabularyEntity vocabulary);

    @Insert
    void insertAll(List<VocabularyEntity> vocabularies);

    @Update
    void update(VocabularyEntity vocabulary);

    @Delete
    void delete(VocabularyEntity vocabulary);

    @Query("SELECT * FROM vocabularies WHERE collectionId = :collectionId ORDER BY createdAt DESC")
    LiveData<List<VocabularyEntity>> getVocabulariesByCollectionId(long collectionId);

    @Query("SELECT * FROM vocabularies WHERE collectionId = :collectionId ORDER BY createdAt DESC")
    List<VocabularyEntity> getVocabulariesSync(long collectionId);

    @Query("SELECT * FROM vocabularies WHERE collectionId = :collectionId AND (term LIKE '%' || :query || '%' OR meaning LIKE '%' || :query || '%') ORDER BY createdAt DESC")
    LiveData<List<VocabularyEntity>> searchVocabularies(long collectionId, String query);

    @Query("SELECT COUNT(*) FROM vocabularies WHERE collectionId = :collectionId")
    LiveData<Integer> getVocabularyCountByCollectionId(long collectionId);

    @Query("SELECT COUNT(*) FROM vocabularies")
    LiveData<Integer> getTotalVocabulariesCount();

    @Query("SELECT COUNT(*) FROM vocabularies WHERE isLearned = 1")
    LiveData<Integer> getTotalLearnedVocabulariesCount();

    @Query("UPDATE vocabularies SET isLearned = :isLearned WHERE id = :id")
    void updateLearnedStatus(long id, boolean isLearned);

    @Query("SELECT LOWER(TRIM(term)) FROM vocabularies WHERE collectionId = :collectionId")
    List<String> getExistingTerms(long collectionId);

    @Query("SELECT * FROM vocabularies WHERE collectionId = :collectionId AND LOWER(TRIM(term)) = LOWER(TRIM(:term)) LIMIT 1")
    VocabularyEntity getVocabularyByTerm(long collectionId, String term);

    @Query("DELETE FROM vocabularies WHERE id NOT IN (SELECT MAX(id) FROM vocabularies GROUP BY collectionId, LOWER(TRIM(term)))")
    void removeDuplicates();
}
