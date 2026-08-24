package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_docs")
data class ScannedDocEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val fileName: String,// tên file
    val fileSize: String,// dung lượng file
    val createdAt: Long, // Lưu timestamp để sắp xếp ngày tháng
    val fileType: String, // PDF hoặc WORD
    val filePath: String,  // Đường dẫn lưu file thực tế trong máy
    val content: String
)
