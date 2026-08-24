package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.thanhbinh.englishaiapp.data.local.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Insert suspend fun insertHistory(history: HistoryEntity)

    @Query("SELECT * FROM review_history WHERE cardId = :cardId AND userId = :userId ORDER BY reviewDate DESC")
    fun getHistoryForCard(cardId: Int, userId: Int): Flow<List<HistoryEntity>>

    @Query("SELECT COUNT(*) FROM review_history WHERE userId = :userId AND reviewDate >= :startOfDay")
    suspend fun countLearnedToday(userId: Int, startOfDay: Long): Int
}