package com.thanhbinh.englishaiapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object AppConfig {
    const val SERVER_PORT = 5000
    const val CONNECT_TIMEOUT_SECONDS = 10L
    const val READ_TIMEOUT_SECONDS = 60L
    const val WRITE_TIMEOUT_SECONDS = 30L

    private const val PREFS_NAME = "english_ai_server_prefs"
    private const val KEY_SERVER_IP = "pref_server_ip"

    // Danh sách các IP ứng viên tự động thử:
    // 1. "127.0.0.1" -> Dùng khi cắm cáp USB (kết hợp `adb reverse tcp:5000 tcp:5000`)
    // 2. "10.0.2.2"  -> Dùng khi chạy trên Máy ảo Android Studio (Emulator)
    // 3. Custom LAN IP -> Dùng khi kết nối mạng Wifi thật
    @Volatile
    var currentServerIp: String = "127.0.0.1"

    val BASE_URL: String
        get() = "http://$currentServerIp:$SERVER_PORT"

    val SUMMARIZE_URL: String
        get() = "$BASE_URL/summarize"

    val CHAT_URL: String
        get() = "$BASE_URL/chat"

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        currentServerIp = prefs.getString(KEY_SERVER_IP, "127.0.0.1") ?: "127.0.0.1"
    }

    fun saveServerIp(context: Context, ip: String) {
        val cleanIp = ip.trim().removePrefix("http://").removePrefix("https://").split(":")[0]
        currentServerIp = cleanIp
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_SERVER_IP, cleanIp).apply()
        Log.d("AppConfig", "Đã lưu Server IP mới: $cleanIp -> BaseURL: $BASE_URL")
    }

    fun getCandidateIps(): List<String> {
        val list = mutableListOf<String>()
        // Ưu tiên IP hiện tại đang chọn
        list.add(currentServerIp)
        // Thêm các IP dự phòng thông dụng nếu chưa có trong list
        val defaults = listOf("127.0.0.1", "10.0.2.2", "192.168.1.100")
        for (ip in defaults) {
            if (!list.contains(ip)) {
                list.add(ip)
            }
        }
        return list
    }

    /**
     * Gửi request POST JSON với cơ chế TỰ ĐỘNG THỬ (Auto-Fallback) giữa USB (127.0.0.1), Emulator (10.0.2.2), và LAN IP.
     * Nếu 127.0.0.1 thất bại do chưa bật adb reverse hoặc dùng Wifi LAN, nó sẽ tự động thử tiếp các IP khác.
     */
    fun sendPostRequestWithFallback(
        endpointPath: String, // ví dụ "/summarize" hoặc "/chat"
        jsonPayload: JSONObject,
        onSuccess: (responseBody: String) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        val candidates = getCandidateIps()
        tryNextCandidate(
            candidates = candidates,
            index = 0,
            endpointPath = endpointPath,
            jsonPayload = jsonPayload,
            lastError = null,
            onSuccess = onSuccess,
            onFailure = onFailure
        )
    }

    private fun tryNextCandidate(
        candidates: List<String>,
        index: Int,
        endpointPath: String,
        jsonPayload: JSONObject,
        lastError: String?,
        onSuccess: (responseBody: String) -> Unit,
        onFailure: (errorMessage: String) -> Unit
    ) {
        if (index >= candidates.size) {
            // Đã thử hết tất cả các IP mà vẫn không kết nối được
            Handler(Looper.getMainLooper()).post {
                val errorSummary = "Lỗi kết nối máy chủ: Không thể kết nối tới server qua USB ($currentServerIp:5000) lẫn LAN IP.\n" +
                        "👉 Nếu dùng USB: Hãy chạy lệnh 'adb reverse tcp:5000 tcp:5000' trên máy tính.\n" +
                        "👉 Nếu dùng Wifi: Nhấn biểu tượng cài đặt Server để nhập IP máy tính.\n" +
                        "Chi tiết: ${lastError ?: "Connection timeout"}"
                onFailure(errorSummary)
            }
            return
        }

        val ip = candidates[index]
        val targetUrl = "http://$ip:$SERVER_PORT$endpointPath"
        Log.d("NetworkFallback", "[$index/${candidates.size}] Đang thử kết nối tới: $targetUrl")

        // Với lần thử đầu tiên hoặc IP đang chọn thì dùng timeout đầy đủ, với candidate fallback dùng connectTimeout 4s để chuyển nhanh
        val timeoutSec = if (index == 0) CONNECT_TIMEOUT_SECONDS else 4L

        val client = OkHttpClient.Builder()
            .connectTimeout(timeoutSec, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url(targetUrl)
            .post(jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.w("NetworkFallback", "Thử kết nối $targetUrl thất bại: ${e.message}. Thử tiếp candidate tiếp theo...")
                // Thử candidate tiếp theo
                tryNextCandidate(
                    candidates = candidates,
                    index = index + 1,
                    endpointPath = endpointPath,
                    jsonPayload = jsonPayload,
                    lastError = "failed to connect to $ip:$SERVER_PORT (${e.localizedMessage ?: e.message})",
                    onSuccess = onSuccess,
                    onFailure = onFailure
                )
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && !responseBody.isNullOrBlank()) {
                    // Kết nối thành công! Cập nhật lại currentServerIp để lần sau gọi trực tiếp
                    currentServerIp = ip
                    Log.i("NetworkFallback", "✅ Kết nối THÀNH CÔNG tới: $targetUrl. Đã lưu IP hoạt động: $ip")
                    Handler(Looper.getMainLooper()).post {
                        onSuccess(responseBody)
                    }
                } else {
                    // Server phản hồi lỗi HTTP
                    Handler(Looper.getMainLooper()).post {
                        onFailure("Lỗi Server Flask ($ip:$SERVER_PORT - HTTP ${response.code}): ${response.message}")
                    }
                }
            }
        })
    }

    /**
     * Kiểm tra nhanh trạng thái kết nối tới 1 IP cụ thể
     */
    fun pingServer(targetIp: String, onResult: (Boolean, String) -> Unit) {
        val cleanIp = targetIp.trim().removePrefix("http://").removePrefix("https://").split(":")[0]
        val testUrl = "http://$cleanIp:$SERVER_PORT/"

        val client = OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder().url(testUrl).get().build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    onResult(false, "Không thể kết nối đến $cleanIp:5000 (${e.localizedMessage})")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                Handler(Looper.getMainLooper()).post {
                    if (response.isSuccessful) {
                        onResult(true, "Kết nối thành công tới Server ($cleanIp:5000)!")
                    } else {
                        onResult(false, "Server phản hồi HTTP ${response.code}")
                    }
                }
            }
        })
    }
}
