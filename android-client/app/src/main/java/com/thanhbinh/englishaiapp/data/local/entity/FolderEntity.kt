package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "folders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class FolderEntity(
    @PrimaryKey(autoGenerate = true)
    val folderId: Int = 0,
    val userId: Int, // Khóa ngoại liên kết với Users
    val folderName: String,
    val description: String? = null,
    val colorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)