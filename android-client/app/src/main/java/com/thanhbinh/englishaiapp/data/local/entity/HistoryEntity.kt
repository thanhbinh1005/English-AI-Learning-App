package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "review_history",
    foreignKeys = [
        ForeignKey(entity = CardEntity::class, parentColumns = ["cardId"], childColumns = ["cardId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Int = 0,
    val cardId: Int,
    val userId: Int, // Thêm cột này để truy vấn theo User
    val rating: Int, // 1: Again, 2: Hard, 3: Good, 4: Easy
    val reviewDate: Long = System.currentTimeMillis()
)