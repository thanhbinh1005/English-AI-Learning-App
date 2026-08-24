package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val userId: Int = 0,
    val username: String,
    val email: String,
    val password: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)