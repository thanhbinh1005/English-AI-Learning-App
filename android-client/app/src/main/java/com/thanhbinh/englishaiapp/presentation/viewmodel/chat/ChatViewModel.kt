package com.thanhbinh.englishaiapp.presentation.viewmodel.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.thanhbinh.englishaiapp.data.local.AppDatabase
import com.thanhbinh.englishaiapp.data.local.entity.ChatMessageEntity
import com.thanhbinh.englishaiapp.data.local.entity.ChatSessionEntity
import com.thanhbinh.englishaiapp.data.model.ChatMessage
import com.thanhbinh.englishaiapp.utils.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val chatHistoryDao = db.chatHistoryDao()
    private val chatSessionDao = db.chatSessionDao()

    private val initialWelcomeMessage = ChatMessage(
        text = "Xin chào! 👋 Mình là Trợ lý AI Ngôn ngữ chạy trên mô hình Llama 3.1.\n" +
                "Bạn có thể hỏi mình bất cứ câu hỏi nào về ngữ pháp, luyện hội thoại tiếng Anh, hoặc nhờ giải thích từ vựng nhé!",
        isUser = false
    )

    private val _sessions = MutableStateFlow<List<ChatSessionEntity>>(emptyList())
    val sessions = _sessions.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String>("")
    val currentSessionId = _currentSessionId.asStateFlow()

    private val _currentSession = MutableStateFlow<ChatSessionEntity?>(null)
    val currentSession = _currentSession.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(listOf(initialWelcomeMessage))
    val messages = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText = _inputText.asStateFlow()

    private var messageObservationJob: Job? = null

    init {
        observeSessions()
    }

    private fun observeSessions() {
        viewModelScope.launch {
            chatSessionDao.getAllSessions().collectLatest { sessionList ->
                _sessions.value = sessionList

                if (sessionList.isEmpty()) {
                    // Tạo phiên chat đầu tiên nếu chưa có
                    withContext(Dispatchers.IO) {
                        val newSession = ChatSessionEntity(
                            id = UUID.randomUUID().toString(),
                            title = "Cuộc trò chuyện mới",
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        chatSessionDao.insertSession(newSession)
                        chatHistoryDao.insertMessage(
                            ChatMessageEntity.fromChatMessage(initialWelcomeMessage, newSession.id)
                        )
                        _currentSessionId.value = newSession.id
                    }
                } else {
                    if (_currentSessionId.value.isEmpty() || sessionList.none { it.id == _currentSessionId.value }) {
                        _currentSessionId.value = sessionList.first().id
                    }
                    _currentSession.value = sessionList.find { it.id == _currentSessionId.value }
                }
            }
        }

        viewModelScope.launch {
            _currentSessionId.collectLatest { sessionId ->
                if (sessionId.isNotEmpty()) {
                    _currentSession.value = _sessions.value.find { it.id == sessionId }
                    observeMessagesForSession(sessionId)
                }
            }
        }
    }

    private fun observeMessagesForSession(sessionId: String) {
        messageObservationJob?.cancel()
        messageObservationJob = viewModelScope.launch {
            chatHistoryDao.getMessagesBySession(sessionId).collectLatest { entities ->
                if (entities.isEmpty()) {
                    withContext(Dispatchers.IO) {
                        chatHistoryDao.insertMessage(
                            ChatMessageEntity.fromChatMessage(initialWelcomeMessage, sessionId)
                        )
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

    fun createNewChat() {
        viewModelScope.launch(Dispatchers.IO) {
            val newSession = ChatSessionEntity(
                id = UUID.randomUUID().toString(),
                title = "Cuộc trò chuyện mới",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            chatSessionDao.insertSession(newSession)
            chatHistoryDao.insertMessage(
                ChatMessageEntity.fromChatMessage(initialWelcomeMessage, newSession.id)
            )
            _currentSessionId.value = newSession.id
        }
        _inputText.value = ""
        _isLoading.value = false
    }

    fun selectSession(sessionId: String) {
        if (_currentSessionId.value != sessionId) {
            _currentSessionId.value = sessionId
            _inputText.value = ""
            _isLoading.value = false
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            chatHistoryDao.deleteMessagesBySession(sessionId)
            chatSessionDao.deleteSessionById(sessionId)
            if (_currentSessionId.value == sessionId) {
                val remaining = _sessions.value.filter { it.id != sessionId }
                if (remaining.isNotEmpty()) {
                    _currentSessionId.value = remaining.first().id
                } else {
                    createNewChat()
                }
            }
        }
    }

    fun sendMessage(customText: String? = null) {
        val rawMessage = customText ?: _inputText.value
        val trimmedMessage = rawMessage.trim()
        val activeSessionId = _currentSessionId.value

        if (trimmedMessage.isBlank() || _isLoading.value || activeSessionId.isEmpty()) {
            return
        }

        // Tự động đặt tiêu đề cuộc trò chuyện theo câu hỏi đầu tiên của người dùng
        viewModelScope.launch(Dispatchers.IO) {
            val current = chatSessionDao.getSessionById(activeSessionId)
            if (current != null) {
                val newTitle = if (current.title == "Cuộc trò chuyện mới" || current.title.isBlank()) {
                    if (trimmedMessage.length > 32) trimmedMessage.take(32) + "..." else trimmedMessage
                } else {
                    current.title
                }
                chatSessionDao.updateSession(
                    current.copy(
                        title = newTitle,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        // 1. Thêm tin nhắn của người dùng và lưu vào Room DB
        val userMsg = ChatMessage(
            text = trimmedMessage,
            isUser = true
        )
        viewModelScope.launch(Dispatchers.IO) {
            chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(userMsg, activeSessionId))
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
                        chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(aiMsg, activeSessionId))
                    }
                } catch (e: Exception) {
                    val parseErrorMsg = ChatMessage(
                        text = "Lỗi phân tích cú pháp phản hồi: ${e.message}",
                        isUser = false,
                        isError = true
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(parseErrorMsg, activeSessionId))
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
                    chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(errorMsg, activeSessionId))
                }
            }
        )
    }

    fun clearChat() {
        val activeSessionId = _currentSessionId.value
        if (activeSessionId.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                chatHistoryDao.deleteMessagesBySession(activeSessionId)
                chatHistoryDao.insertMessage(ChatMessageEntity.fromChatMessage(initialWelcomeMessage, activeSessionId))
            }
        }
        _isLoading.value = false
        _inputText.value = ""
    }
}
