package com.thanhbinh.englishaiapp.presentation.viewmodel.scan

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanhbinh.englishaiapp.data.local.AppDatabase
import com.thanhbinh.englishaiapp.data.local.entity.ScannedDocEntity
import com.thanhbinh.englishaiapp.utils.AppConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class ScanResultViewModel(application: Application) : AndroidViewModel(application) {
    private val scanDao = AppDatabase.Companion.getDatabase(application).scanDao()


    // Biến để giữ ID của file hiện tại
    // Lưu ID dưới dạng Int (khớp với Entity)
    // Quản lý ID file hiện tại (0 là file chưa lưu)
    private val _currentDocId = MutableStateFlow(0)
    val currentDocId = _currentDocId.asStateFlow()

    // HÀM 1: LƯU MỚI HOÀN TOÀN (Ép buộc tạo dòng mới)
    fun saveNewDocument(name: String, content: String, type: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val newDoc = ScannedDocEntity(
                    id = 0, // ID 0 để Room tự sinh ID mới
                    fileName = name,
                    content = content,
                    fileType = type,
                    createdAt = System.currentTimeMillis(),
                    fileSize = "${content.length / 1024} KB",
                    filePath = ""
                )
                val id = scanDao.insertDoc(newDoc)
                _currentDocId.value = id.toInt() // Ghi nhớ ID vừa tạo
                onSuccess()
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Lỗi lưu mới: ${e.message}")
            }
        }
    }

    // HÀM 2: CẬP NHẬT (Chỉ ghi đè lên file đang mở)
    fun updateCurrentDocument(name: String, content: String, type: String, onSuccess: () -> Unit) {
        val id = _currentDocId.value
        if (id == 0) return // Không có ID thì không cập nhật

        viewModelScope.launch {
            try {
                val existingDoc = scanDao.getDocById(id)
                if (existingDoc != null) {
                    val updatedDoc = existingDoc.copy(
                        fileName = name,
                        content = content,
                        fileType = type,
                        createdAt = System.currentTimeMillis()
                    )
                    scanDao.updateDoc(updatedDoc)
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("DB_ERROR", "Lỗi cập nhật: ${e.message}")
            }
        }
    }
    // Thêm hàm này vào ScanResultViewModel
    fun getDocumentById(id: Int, onResult: (ScannedDocEntity) -> Unit) {
        viewModelScope.launch {
            val doc = scanDao.getDocById(id)
            if (doc != null) {
                onResult(doc)
            }
        }
    }

    // Thêm hàm này để đặt ID khi mở file cũ
    fun setCurrentDocId(id: Int) {
        _currentDocId.value = id
    }
    fun summarizeText(text: String, onResult: (String) -> Unit) {
        if (text.isBlank()) return onResult("Văn bản rỗng")

        val client = OkHttpClient()

        // Gọi từ file cấu hình, không viết chết URL ở đây nữa
        val url = AppConfig.SUMMARIZE_URL

        val jsonBody = JSONObject().apply {
            put("text", text)
        }

        val request = Request.Builder()
            .url(url)
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // In lỗi ra Logcat để kiểm tra trên máy tính
                    Log.e("FLASK_ERROR", "--- LỖI KẾT NỐI API LOCAL ---")
                    e.printStackTrace()

                    Handler(Looper.getMainLooper()).post {
                        onResult("Lỗi kết nối máy chủ: ${e.message}")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()
                    // In log phản hồi từ Flask ra Console máy tính
                    Log.d("FLASK_DEBUG", "Status: ${response.code}, Body: $responseBody")

                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            // Lấy giá trị từ key 'summary' mà Flask trả về
                            val result = jsonResponse.getString("summary")

                            Handler(Looper.getMainLooper()).post {
                                onResult(result.trim())
                            }
                        } catch (e: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                onResult("Lỗi phân tích dữ liệu từ Python")
                            }
                        }
                    } else {
                        Handler(Looper.getMainLooper()).post {
                            onResult("Lỗi Server Flask (${response.code})")
                        }
                    }
                }
            })

    }
}