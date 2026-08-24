package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.*
import com.thanhbinh.englishaiapp.data.local.entity.ReviewEntity
import com.thanhbinh.englishaiapp.data.local.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    // 1. Lấy trạng thái học của 1 thẻ bài cụ thể cho 1 user
    @Query("SELECT * FROM reviews WHERE card_id = :cardId AND user_id = :userId LIMIT 1")
    suspend fun getReviewStatus(cardId: Int, userId: Int): ReviewEntity?

    // 2. Cập nhật hoặc thêm mới trạng thái học (SM-2)
    // Nhờ UNIQUE(card_id, user_id), nó sẽ tự động ghi đè nếu đã tồn tại
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(review: ReviewEntity)

    // 3. Lấy danh sách thẻ cần học (due cards) cho ngày hôm nay
    @Query("""
        SELECT cards.* FROM cards 
        INNER JOIN reviews ON cards.cardId = reviews.card_id 
        WHERE reviews.user_id = :userId 
        AND reviews.next_review_timestamp <= :currentTime
    """)
    fun getCardsToReview(userId: Int, currentTime: Long): Flow<List<CardEntity>>

    @Delete
    suspend fun deleteReview(review: ReviewEntity)
}