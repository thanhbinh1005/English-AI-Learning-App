package com.thanhbinh.englishaiapp.data.local.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "history")
public class HistoryItem {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String sourceText;
    public String translatedText;
    public String sourceLang;
    public String targetLang;
    public boolean isFavorite;
    public long timestamp;

    public HistoryItem(String sourceText, String translatedText, String sourceLang, String targetLang) {
        this.sourceText = sourceText;
        this.translatedText = translatedText;
        this.sourceLang = sourceLang;
        this.targetLang = targetLang;
        this.timestamp = System.currentTimeMillis();
        this.isFavorite = false;
    }
}
