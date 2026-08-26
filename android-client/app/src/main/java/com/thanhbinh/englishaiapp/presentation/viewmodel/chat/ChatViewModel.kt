package com.thanhbinh.englishaiapp.presentation.viewmodel.chat

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thanhbinh.englishaiapp.data.model.ChatMessage
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
import java.util.concurrent.TimeUnit

class ChatViewModel : ViewModel() {

    private val initialWelcomeMessage = ChatMessage(
        text = "Xin chào! 👋 Mình là Trợ lý AI Ngôn ngữ chạy trên mô hình Llama 3.1.\n" +
                "Bạn có thể hỏi mình bất cứ câu hỏi nào về ngữ pháp, luyện hội thoại tiếng Anh, hoặc nhờ giải thích từ vựng nhé!",
        isUser = false
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(initialWelcomeMessage))
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(AppConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(AppConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(AppConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    fun onInputTextChanged(newText: String) {
        _inputText.value = newText
    }

    fun sendMessage(customText: String? = null) {
        val rawMessage = customText ?: _inputText.value
        val trimmedMessage = rawMessage.trim()

        if (trimmedMessage.isBlank() || _isLoading.value) {
            return
        }

        // 1. Thêm tin nhắn của người dùng ngay lập tức
        val userMsg = ChatMessage(
            text = trimmedMessage,
            isUser = true
        )
        _messages.value = _messages.value + userMsg

        // Reset ô nhập nếu lấy từ inputText
        if (customText == null) {
            _inputText.value = ""
        }

        // 2. Bật trạng thái đang xử lý (loading / typing animation)
        _isLoading.value = true

        // 3. Gửi yêu cầu POST lên Server với cơ chế Auto-Fallback (hỗ trợ cả USB và LAN)
        val jsonPayload = JSONObject().apply {
            put("message", trimmedMessage)
        }

        AppConfig.sendPostRequestWithFallback(
            endpointPath = "/chat",
            jsonPayload = jsonPayload,
            onSuccess = { responseBody ->
                _isLoading.value = false
                try {
                    val jsonResponse = JSONObject(responseBody)
                    val replyText = jsonResponse.optString(
                        "reply",
                        jsonResponse.optString("response", jsonResponse.optString("message", ""))
                    )

                    val finalReply = if (replyText.isNotBlank()) {
                        replyText.trim()
                    } else {
                        "Không nhận được phản hồi từ AI."
                    }

                    val aiMsg = ChatMessage(
                        text = finalReply,
                        isUser = false
                    )
                    _messages.value = _messages.value + aiMsg
                } catch (e: Exception) {
                    val parseErrorMsg = ChatMessage(
                        text = "Lỗi phân tích cú pháp phản hồi: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                    _messages.value = _messages.value + parseErrorMsg
                }
            },
            onFailure = { errorMessage ->
                _isLoading.value = false
                val errorMsg = ChatMessage(
                    text = errorMessage,
                    isUser = false,
                    isError = true
                )
                _messages.value = _messages.value + errorMsg
            }
        )
    }

    fun clearChat() {
        _messages.value = listOf(initialWelcomeMessage)
        _isLoading.value = false
        _inputText.value = ""
    }
}
