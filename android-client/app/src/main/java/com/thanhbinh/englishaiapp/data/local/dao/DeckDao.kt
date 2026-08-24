package com.thanhbinh.englishaiapp.data.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.thanhbinh.englishaiapp.data.local.entity.DeckEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface DeckDao {
    @Query("SELECT * FROM decks WHERE folderId = :folderId")
    fun getDecksByFolder(folderId: Int): Flow<List<DeckEntity>>

    @Insert suspend fun insertDeck(deck: DeckEntity): Long
    @Update suspend fun updateDeck(deck: DeckEntity)
    @Delete
    suspend fun deleteDeck(deck: DeckEntity)
}