package com.careride.feature.patient.messages

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.careride.data.fakebackend.FakeBackend
import com.careride.domain.model.Conversation
import com.careride.domain.model.Doctor
import com.careride.domain.model.Message
import com.careride.domain.model.SubscriptionStatus
import com.careride.domain.repository.MessageRepository
import com.careride.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientChatState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: String? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.None
) {
    val canSendMessage: Boolean
        get() = subscriptionStatus.canMessage() && messageInput.isNotBlank() && !isSending

    val isInputEnabled: Boolean
        get() = subscriptionStatus.canMessage()

    val doctor: Doctor?
        get() = conversation?.doctor
}

class PatientChatViewModel(
    private val conversationId: String,
    private val messageRepository: MessageRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PatientChatState())
    val state: StateFlow<PatientChatState> = _state.asStateFlow()

    init {
        loadConversation()
        loadMessages()
        observeSubscription()
        markAsRead()
    }

    private fun loadConversation() {
        // Get conversation from store
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

    private fun observeSubscription() {
        screenModelScope.launch {
            subscriptionRepository.observeSubscriptionStatus().collect { status ->
                _state.update { it.copy(subscriptionStatus = status) }
            }
        }
    }

    private fun markAsRead() {
        screenModelScope.launch {
            messageRepository.markAsRead(conversationId)
        }
    }

    fun onMessageInputChange(value: String) {
        _state.update { it.copy(messageInput = value) }
    }

    fun sendMessage() {
        val content = _state.value.messageInput.trim()
        if (content.isBlank()) return
        if (!_state.value.subscriptionStatus.canMessage()) return

        screenModelScope.launch {
            _state.update { it.copy(isSending = true, messageInput = "") }

            messageRepository.sendMessage(conversationId, content)
                .onSuccess {
                    _state.update { it.copy(isSending = false) }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isSending = false,
                            messageInput = content, // Restore message
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