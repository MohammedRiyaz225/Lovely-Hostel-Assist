package com.example.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.groq.AssistantMode
import com.example.data.groq.ChatMessage
import com.example.data.groq.GroqClient
import com.example.data.groq.GroqModel
import com.example.data.groq.MessageRole
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome_1",
                role = MessageRole.ASSISTANT,
                text = "Hello! I am your HostelFix AI Campus & Facility Assistant powered by Groq.\n\nI can help you diagnose hostel maintenance issues, advise on immediate safety steps, explain hostel regulations, and guide you across all 18+ LPU campus hostel blocks.",
                timestamp = System.currentTimeMillis(),
                modelUsed = GroqModel.LLAMA_3_3_70B.displayName
            )
        )
    )
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _selectedModel = MutableStateFlow(GroqModel.LLAMA_3_3_70B)
    val selectedModel: StateFlow<GroqModel> = _selectedModel.asStateFlow()

    private val _selectedMode = MutableStateFlow(AssistantMode.CAMPUS_GUIDE)
    val selectedMode: StateFlow<AssistantMode> = _selectedMode.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var activeSendJob: Job? = null

    fun setModel(model: GroqModel) {
        _selectedModel.value = model
    }

    fun setMode(mode: AssistantMode) {
        _selectedMode.value = mode
    }

    fun sendMessage(userText: String) {
        if (userText.isBlank() || _isLoading.value) return

        val trimmedText = userText.trim()
        val userMessage = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            role = MessageRole.USER,
            text = trimmedText,
            timestamp = System.currentTimeMillis()
        )

        val priorHistory = _messages.value
        val currentList = priorHistory + userMessage
        _messages.value = currentList
        _isLoading.value = true

        val placeholderAssistant = ChatMessage(
            id = "loading_${System.currentTimeMillis()}",
            role = MessageRole.ASSISTANT,
            text = "",
            timestamp = System.currentTimeMillis(),
            modelUsed = _selectedModel.value.displayName,
            isLoading = true
        )
        _messages.value = currentList + placeholderAssistant

        activeSendJob?.cancel()
        activeSendJob = viewModelScope.launch {
            val reply = GroqClient.sendMessage(
                history = priorHistory,
                userMessage = trimmedText,
                model = _selectedModel.value,
                mode = _selectedMode.value
            )

            val finalAssistantMessage = ChatMessage(
                id = "assistant_${System.currentTimeMillis()}",
                role = MessageRole.ASSISTANT,
                text = reply.text,
                timestamp = System.currentTimeMillis(),
                modelUsed = reply.modelUsed,
                groundingSources = reply.sources,
                isLoading = false
            )

            // Replace loading placeholder with final message
            _messages.value = currentList + finalAssistantMessage
            _isLoading.value = false
        }
    }

    fun clearChat() {
        activeSendJob?.cancel()
        _isLoading.value = false
        _messages.value = listOf(
            ChatMessage(
                id = "welcome_reset",
                role = MessageRole.ASSISTANT,
                text = "Conversation cleared. How can I assist you with your hostel facilities or campus today?",
                timestamp = System.currentTimeMillis(),
                modelUsed = _selectedModel.value.displayName
            )
        )
    }
}
