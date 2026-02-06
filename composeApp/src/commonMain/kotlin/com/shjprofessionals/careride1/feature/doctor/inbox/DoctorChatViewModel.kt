package com.shjprofessionals.careride1.feature.doctor.inbox

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.core.util.Validators
import com.shjprofessionals.careride1.core.util.toAppError
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.model.Message
import com.shjprofessionals.careride1.domain.model.QuickReply
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorChatState(
    val conversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val messageInput: String = "",
    val quickReplies: List<QuickReply> = emptyList(),
    val suggestedQuickReplies: List<QuickReply> = emptyList(),
    val showQuickReplies: Boolean = false,
    val instantQuickReplySend: Boolean = false,
    val isLoading: Boolean = true,
    val isSending: Boolean = false,
    val error: AppError? = null
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
                        error = e.toAppError()
                    )
                }
            }
        }
    }

    private fun pickSuggested(quickReplies: List<QuickReply>): List<QuickReply> {
        val preferred = listOf("qr_followup_1", "qr_schedule_1", "qr_general_1")
        val byId = quickReplies.associateBy { it.id }
        val first = preferred.mapNotNull { byId[it] }
        val rest = quickReplies.filterNot { qr -> first.any { it.id == qr.id } }
        return (first + rest).distinctBy { it.id }.take(3)
    }

    private fun loadQuickReplies() {
        val quickReplies = messageRepository.getQuickReplies()
        _state.update {
            it.copy(
                quickReplies = quickReplies,
                suggestedQuickReplies = pickSuggested(quickReplies)
            )
        }
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

    fun toggleInstantQuickReplySend() {
        _state.update { it.copy(instantQuickReplySend = !it.instantQuickReplySend) }
    }

    fun onQuickReplySelected(quickReply: QuickReply) {
        if (_state.value.instantQuickReplySend) {
            sendQuickReplyNow(quickReply)
        } else {
            insertQuickReply(quickReply)
        }
    }

    private fun insertQuickReply(quickReply: QuickReply) {
        _state.update {
            it.copy(
                messageInput = quickReply.message,
                showQuickReplies = false
            )
        }
    }

    private fun sendQuickReplyNow(quickReply: QuickReply) {
        val validation = Validators.validateMessage(quickReply.message)

        validation
            .onInvalid { reason ->
                _state.update { it.copy(error = AppError.Validation(reason)) }
            }
            .onValid { sanitized ->
                screenModelScope.launch {
                    _state.update { it.copy(isSending = true, showQuickReplies = false) }

                    messageRepository.sendDoctorMessage(conversationId, sanitized)
                        .onSuccess {
                            _state.update { it.copy(isSending = false) }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isSending = false,
                                    error = error.toAppError()
                                )
                            }
                        }
                }
            }
    }

    fun sendMessage() {
        val content = _state.value.messageInput
        val validation = Validators.validateMessage(content)

        validation
            .onInvalid { reason ->
                _state.update { it.copy(error = AppError.Validation(reason)) }
            }
            .onValid { sanitized ->
                screenModelScope.launch {
                    _state.update { it.copy(isSending = true, messageInput = "") }

                    messageRepository.sendDoctorMessage(conversationId, sanitized)
                        .onSuccess {
                            _state.update { it.copy(isSending = false) }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isSending = false,
                                    messageInput = content,
                                    error = error.toAppError()
                                )
                            }
                        }
                }
            }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
