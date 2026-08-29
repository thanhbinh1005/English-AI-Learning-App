package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.*
import com.thanhbinh.englishaiapp.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    suspend fun getAllMessagesSync(): List<ChatMessageEntity>

    @Delete
    suspend fun deleteMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()

    @Query("SELECT COUNT(*) FROM chat_history")
    suspend fun getMessageCount(): Int
}
