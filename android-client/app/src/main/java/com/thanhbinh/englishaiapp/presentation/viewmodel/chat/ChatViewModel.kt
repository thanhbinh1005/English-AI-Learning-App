package com.thanhbinh.englishaiapp.presentation.viewmodel.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanhbinh.englishaiapp.data.local.AppDatabase
import com.thanhbinh.englishaiapp.data.local.entity.ChatMessageEntity
import com.thanhbinh.englishaiapp.data.model.ChatMessage
import com.thanhbinh.englishaiapp.utils.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val chatHistoryDao = AppDatabase.getDatabase(application).chatHistoryDao()

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

    init {
        loadChatHistory()
    }

    private fun loadChatHistory() {
        viewModelScope.launch {
            chatHistoryDao.getAllMessages().collect { entities ->
                if (entities.isEmpty()) {
                    withContext(Dispatchers.IO) {
                        chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(initialWelcomeMessage))
                    }
                } else {
                    _messages.value = entities.map { it.toChatMessage() }
                }
            }
        }
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

        // 1. Thêm tin nhắn của người dùng và lưu vào Room DB
        val userMsg = ChatMessage(
            text = trimmedMessage,
            isUser = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(userMsg))
        }

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
                    viewModelScope.launch(Dispatchers.IO) {
                        chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(aiMsg))
                    }
                } catch (e: Exception) {
                    val parseErrorMsg = ChatMessage(
                        text = "Lỗi phân tích cú pháp phản hồi: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(parseErrorMsg))
                    }
                }
            },
            onFailure = { errorMessage ->
                _isLoading.value = false
                val errorMsg = ChatMessage(
                    text = errorMessage,
                    isUser = false,
                    isError = true
                )
                viewModelScope.launch(Dispatchers.IO) {
                    chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(errorMsg))
                }
            }
        )
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            chatHistoryDao.clearHistory()
            chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(initialWelcomeMessage))
        }
        _isLoading.value = false
        _inputText.value = ""
    }
}
