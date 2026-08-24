package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thanhbinh.englishaiapp.data.local.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity): Long
    @Query("SELECT * FROM users WHERE userId = :id")
    suspend fun getUserById(id: Int): UserEntity?
}