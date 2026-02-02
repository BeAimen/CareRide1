package com.careride.feature.doctor.inbox

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.careride.data.fakebackend.FakeBackend
import com.careride.domain.model.Conversation
import com.careride.domain.model.Message
import com.careride.domain.model.QuickReply
import com.careride.domain.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DoctorChatState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val quickReplies: List<QuickReply> = emptyList(),
    val showQuickReplies: Boolean = false,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null
) {
    val canSendMessage: Boolean
        get() = messageInput.isNotBlank() && !isSending

    val patientName: String
        get() = conversation?.patientName ?: "Patient"
}

class DoctorChatViewModel(
    private val conversationId: String,
    private val messageRepository: MessageRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DoctorChatState())
    val state: StateFlow<DoctorChatState> = _state.asStateFlow()

    init {
        loadConversation()
        loadMessages()
        loadQuickReplies()
        markAsRead()
    }

    private fun loadConversation() {
        val conversations = FakeBackend.messageStore.conversations.value
        val conversation = conversations[conversationId]
        _state.update { it.copy(conversation = conversation) }
    }

    private fun loadMessages() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                messageRepository.getMessages(conversationId).collect { messages ->
                    _state.update {
                        it.copy(
                            messages = messages,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load messages"
                    )
                }
            }
        }
    }

    private fun loadQuickReplies() {
        val quickReplies = messageRepository.getQuickReplies()
        _state.update { it.copy(quickReplies = quickReplies) }
    }

    private fun markAsRead() {
        screenModelScope.launch {
            messageRepository.markAsReadByDoctor(conversationId)
        }
    }

    fun onMessageInputChange(value: String) {
        _state.update { it.copy(messageInput = value) }
    }

    fun toggleQuickReplies() {
        _state.update { it.copy(showQuickReplies = !it.showQuickReplies) }
    }

    fun hideQuickReplies() {
        _state.update { it.copy(showQuickReplies = false) }
    }

    fun useQuickReply(quickReply: QuickReply) {
        _state.update {
            it.copy(
                messageInput = quickReply.message,
                showQuickReplies = false
            )
        }
    }

    fun sendMessage() {
        val content = _state.value.messageInput.trim()
        if (content.isBlank()) return

        screenModelScope.launch {
            _state.update { it.copy(isSending = true, messageInput = "") }

            messageRepository.sendDoctorMessage(conversationId, content)
                .onSuccess {
                    _state.update { it.copy(isSending = false) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSending = false,
                            messageInput = content,
                            error = error.message ?: "Failed to send message"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}