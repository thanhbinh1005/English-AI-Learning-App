package com.thanhbinh.englishaiapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.thanhbinh.englishaiapp.data.local.dao.*
import com.thanhbinh.englishaiapp.data.local.entity.*

@Database(
    entities = [
        ScannedDocEntity::class,
        CollectionEntity::class,
        VocabularyEntity::class,
        HistoryItem::class,
        ChatMessageEntity::class,
        ChatSessionEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scanDao(): ScanDao
    abstract fun collectionDao(): CollectionDao
    abstract fun vocabularyDao(): VocabularyDao
    abstract fun translationHistoryDao(): TranslationHistoryDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun chatSessionDao(): ChatSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmField
        val databaseWriteExecutor: java.util.concurrent.ExecutorService = java.util.concurrent.Executors.newFixedThreadPool(4)

        @JvmStatic
        fun getInstance(context: Context): AppDatabase = getDatabase(context)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
