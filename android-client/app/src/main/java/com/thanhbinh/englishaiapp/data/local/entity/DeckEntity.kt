package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "decks",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["folderId"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DeckEntity(
    @PrimaryKey(autoGenerate = true)
    val deckId: Int = 0,
    val folderId: Int, // Khóa ngoại liên kết với Folders
    val deckName: String,
    val createdAt: Long = System.currentTimeMillis()
)