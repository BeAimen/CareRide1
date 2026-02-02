package com.shjprofessionals.careride1.feature.doctor.inbox

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DoctorInboxState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val unreadCount: Int = 0
)

class DoctorInboxViewModel(
    private val messageRepository: MessageRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DoctorInboxState())
    val state: StateFlow<DoctorInboxState> = _state.asStateFlow()

    init {
        loadConversations()
        observeUnreadCount()
    }

    private fun loadConversations() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                messageRepository.getDoctorConversations().collect { conversations ->
                    _state.update {
                        it.copy(
                            conversations = conversations,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load conversations"
                    )
                }
            }
        }
    }

    private fun observeUnreadCount() {
        screenModelScope.launch {
            messageRepository.getDoctorUnreadCount().collect { count ->
                _state.update { it.copy(unreadCount = count) }
            }
        }
    }

    fun refresh() {
        loadConversations()
    }
}
