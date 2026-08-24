package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.thanhbinh.englishaiapp.data.local.entity.FolderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE userId = :userId")
    fun getFoldersByUser(userId: Int): Flow<List<FolderEntity>>

    @Insert
    suspend fun insert(folder: FolderEntity): Long
    @Update
    suspend fun update(folder: FolderEntity)
    @Delete
    suspend fun delete(folder: FolderEntity)
}