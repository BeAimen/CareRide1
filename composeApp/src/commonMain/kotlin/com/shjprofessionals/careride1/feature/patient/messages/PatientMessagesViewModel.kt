package com.shjprofessionals.careride1.feature.patient.messages

import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.core.util.toAppError

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientMessagesState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.None
)

class PatientMessagesViewModel(
    private val messageRepository: MessageRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PatientMessagesState())
    val state: StateFlow<PatientMessagesState> = _state.asStateFlow()

    init {
        loadConversations()
        observeSubscription()
    }

    private fun loadConversations() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                messageRepository.getConversations().collect { conversations ->
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
                        error = e.toAppError()
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

    fun refresh() {
        loadConversations()
    }
}

