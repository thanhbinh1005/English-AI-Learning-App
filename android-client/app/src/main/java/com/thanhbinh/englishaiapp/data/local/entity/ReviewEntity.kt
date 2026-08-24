package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reviews",
    indices = [Index(value = ["card_id", "user_id"], unique = true)], // Đảm bảo 1 user - 1 card chỉ có 1 trạng thái học
    foreignKeys = [
        ForeignKey(entity = CardEntity::class, parentColumns = ["cardId"], childColumns = ["card_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = UserEntity::class, parentColumns = ["userId"], childColumns = ["user_id"], onDelete = ForeignKey.CASCADE)
    ]
)
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val review_id: Int = 0,
    val card_id: Int,
    val user_id: Int,
    val repetition: Int = 0,
    val ease_factor: Double = 2.5,
    val interval: Int = 0,
    val next_review_timestamp: Long,
    val status: String = "NEW" // NEW, LEARNING, REVIEW
)
