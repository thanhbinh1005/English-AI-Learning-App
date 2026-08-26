package com.thanhbinh.englishaiapp

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
    fun testAppConfigEndpoints() {
        assertEquals("http://${AppConfig.currentServerIp}:${AppConfig.SERVER_PORT}/summarize", AppConfig.SUMMARIZE_URL)
        assertEquals("http://${AppConfig.currentServerIp}:${AppConfig.SERVER_PORT}/chat", AppConfig.CHAT_URL)
        assertEquals(10L, AppConfig.CONNECT_TIMEOUT_SECONDS)
    }
}
