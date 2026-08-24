package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.*
import com.thanhbinh.englishaiapp.data.local.entity.CardEntity
import com.thanhbinh.englishaiapp.data.local.entity.ReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    // Lấy tất cả thẻ trong một Deck (Bộ từ vựng)
    @Query("SELECT * FROM cards WHERE deckId = :dId")
    fun getCardsByDeckId(dId: Int): Flow<List<CardEntity>>

    // Thêm: Lấy thẻ kèm theo trạng thái Review (Dùng cho màn hình quản lý từ)
    @Query("""
        SELECT * FROM cards 
        LEFT JOIN reviews ON cards.cardId = reviews.card_id
        WHERE cards.deckId = :dId
    """)
    fun getCardsWithReviewStatus(dId: Int): Flow<Map<CardEntity, ReviewEntity?>>
    // Lấy một thẻ cụ thể theo ID
    @Query("SELECT * FROM cards WHERE cardId = :cId")
    suspend fun getCardById(cId: Int): CardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity): Long

    @Update
    suspend fun updateCard(card: CardEntity)

    @Delete
    suspend fun deleteCard(card: CardEntity)
}