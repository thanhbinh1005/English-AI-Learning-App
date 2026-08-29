package com.thanhbinh.englishaiapp

import com.thanhbinh.englishaiapp.data.local.entity.ChatMessageEntity
import com.thanhbinh.englishaiapp.data.local.entity.ChatSessionEntity
import com.thanhbinh.englishaiapp.data.model.ChatMessage
import com.thanhbinh.englishaiapp.utils.AppConfig
import org.junit.Assert.*
import org.junit.Test

class ChatMessageTest {

    @Test
    fun testChatMessageCreation_userMessage() {
        val message = ChatMessage(
            text = "hiện tại đơn là thì như nào",
            isUser = true
        )
        assertEquals("hiện tại đơn là thì như nào", message.text)
        assertTrue(message.isUser)
        assertFalse(message.isError)
        assertNotNull(message.id)
        assertTrue(message.timestamp > 0)
    }

    @Test
    fun testChatMessageCreation_aiMessage() {
        val message = ChatMessage(
            text = "Thì hiện tại đơn diễn tả một hành động lặp đi lặp lại...",
            isUser = false
        )
        assertEquals("Thì hiện tại đơn diễn tả một hành động lặp đi lặp lại...", message.text)
        assertFalse(message.isUser)
        assertFalse(message.isError)
    }

    @Test
    fun testChatMessageCreation_errorMessage() {
        val message = ChatMessage(
            text = "Lỗi kết nối máy chủ",
            isUser = false,
            isError = true
        )
        assertTrue(message.isError)
        assertFalse(message.isUser)
    }

    @Test
    fun testChatMessageEntity_mapping() {
        val original = ChatMessage(
            id = "test-uuid-1234",
            text = "Hello Llama AI",
            isUser = true,
            timestamp = 1700000000000L,
            isError = false
        )

        val entity = ChatMessageEntity.fromChatMessage(original, "session-abc")
        assertEquals(original.id, entity.id)
        assertEquals("session-abc", entity.sessionId)
        assertEquals(original.text, entity.text)
        assertEquals(original.isUser, entity.isUser)
        assertEquals(original.timestamp, entity.timestamp)
        assertEquals(original.isError, entity.isError)

        val converted = entity.toChatMessage()
        assertEquals(original, converted)
    }

    @Test
    fun testChatSessionEntity_creation() {
        val session = ChatSessionEntity(
            id = "session-1",
            title = "Học ngữ pháp tiếng Anh",
            createdAt = 1700000000000L,
            updatedAt = 1700000005000L,
            messageCount = 4
        )
        assertEquals("session-1", session.id)
        assertEquals("Học ngữ pháp tiếng Anh", session.title)
        assertEquals(4, session.messageCount)
        assertTrue(session.updatedAt >= session.createdAt)
    }

    @Test
    fun testAppConfigEndpoints() {
        assertEquals("http://${AppConfig.currentServerIp}:${AppConfig.SERVER_PORT}/summarize", AppConfig.SUMMARIZE_URL)
        assertEquals("http://${AppConfig.currentServerIp}:${AppConfig.SERVER_PORT}/chat", AppConfig.CHAT_URL)
        assertEquals(10L, AppConfig.CONNECT_TIMEOUT_SECONDS)
    }
}
