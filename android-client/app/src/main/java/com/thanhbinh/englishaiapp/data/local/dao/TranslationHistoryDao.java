package com.thanhbinh.englishaiapp.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.thanhbinh.englishaiapp.data.local.entity.HistoryItem;

import java.util.List;

@Dao
public interface TranslationHistoryDao {
    @Query("SELECT * FROM history ORDER BY timestamp DESC")
    List<HistoryItem> getAll();

    @Insert
    void insert(HistoryItem item);

    @Update
    void update(HistoryItem item);

    @Delete
    void delete(HistoryItem item);
}
