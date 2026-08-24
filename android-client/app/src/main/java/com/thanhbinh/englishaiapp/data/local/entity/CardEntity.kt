package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "cards",
    foreignKeys = [
        ForeignKey(
            entity = DeckEntity::class,
            parentColumns = ["deckId"],
            childColumns = ["deckId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardEntity(
    @PrimaryKey(autoGenerate = true)
    val cardId: Int = 0,
    val deckId: Int, // Khóa ngoại liên kết với Decks
    val word: String,
    val meaning: String,
    val pronunciation: String? = null,
    val exampleSentence: String? = null,
    val imagePath: String? = null,
    val mnemonicNote: String? = null,

    // Thuật toán SM-2 (Spaced Repetition)
//    val repetition: Int = 0,
//    val easeFactor: Double = 2.5,
//    val interval: Int = 0,
//    val nextReviewDate: Long = System.currentTimeMillis()
)