package com.thanhbinh.englishaiapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.thanhbinh.englishaiapp.data.local.entity.ScannedDocEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    // Lấy toàn bộ danh sách file, Flow giúp UI tự cập nhật khi DB thay đổi
    @Query("SELECT * FROM scanned_docs ORDER BY createdAt DESC")
    fun getAllScannedDocs(): Flow<List<ScannedDocEntity>>

    // Thêm một file mới vào máy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: ScannedDocEntity): Long // Phải trả về Long để lấy ID mới

    // THÊM HÀM NÀY VÀO ĐỂ UPDATE ĐƯỢC
    @Update
    suspend fun updateDoc(doc: ScannedDocEntity)
    // Xóa một file khỏi danh sách
    @Delete
    suspend fun deleteDoc(doc: ScannedDocEntity)

    // THÊM HÀM NÀY:
    @Query("SELECT * FROM scanned_docs WHERE fileName = :title LIMIT 1")
    suspend fun getDocByTitle(title: String): ScannedDocEntity?

    // --- ĐÂY LÀ HÀM BẠN ĐANG THIẾU ---
    @Query("SELECT * FROM scanned_docs WHERE id = :id LIMIT 1")
    suspend fun getDocById(id: Int): ScannedDocEntity?

    // Thêm hàm tìm kiếm theo tên (Kính lúp)
    @Query("SELECT * FROM scanned_docs WHERE fileName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchDocs(query: String): Flow<List<ScannedDocEntity>>
}