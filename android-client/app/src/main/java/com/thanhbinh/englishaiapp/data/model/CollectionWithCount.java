package com.thanhbinh.englishaiapp.data.model;

import androidx.room.Embedded;
import com.thanhbinh.englishaiapp.data.local.entity.CollectionEntity;

public class CollectionWithCount {
    @Embedded
    public CollectionEntity collection;

    public int wordCount;
    public int learnedCount;
}
