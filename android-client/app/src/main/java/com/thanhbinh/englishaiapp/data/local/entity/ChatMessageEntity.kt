package com.thanhbinh.englishaiapp.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.thanhbinh.englishaiapp.data.model.ChatMessage
import java.util.UUID

@Entity(
    tableName = "chat_history",
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val sessionId: String = "default_session",
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false
) {
    fun toChatMessage(): ChatMessage = ChatMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        isError = isError
    )

    companion object {
        fun fromChatMessage(msg: ChatMessage, sessionId: String = "default_session"): ChatMessageEntity = ChatMessageEntity(
            id = msg.id,
            sessionId = sessionId,
            text = msg.text,
            isUser = msg.isUser,
            timestamp = msg.timestamp,
            isError = msg.isError
        )
    }
}
