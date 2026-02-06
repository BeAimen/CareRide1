package com.shjprofessionals.careride1.feature.doctor.inbox

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.core.util.toAppError
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DoctorInboxFilter {
    ALL,
    NEEDS_REPLY
}

data class DoctorInboxState(
    val allConversations: List<Conversation> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val unreadCount: Int = 0,
    val needsReplyCount: Int = 0,
    val filter: DoctorInboxFilter = DoctorInboxFilter.ALL,
    val archivedIds: Set<String> = emptySet()
)

class DoctorInboxViewModel(
    private val messageRepository: MessageRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DoctorInboxState())
    val state: StateFlow<DoctorInboxState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadConversations()
    }

    private fun applyStateDerivedValues(
        all: List<Conversation>,
        filter: DoctorInboxFilter,
        archivedIds: Set<String>
    ): DoctorInboxState {
        val visible = all.filterNot { archivedIds.contains(it.id) }

        val needsReply = visible.count { it.isLastMessageFromPatient }
        val unread = visible.sumOf { it.unreadCount }

        val filtered = when (filter) {
            DoctorInboxFilter.ALL -> visible
            DoctorInboxFilter.NEEDS_REPLY -> visible.filter { it.isLastMessageFromPatient }
        }

        val sorted = filtered.sortedWith(
            compareByDescending<Conversation> { it.isLastMessageFromPatient }
                .thenByDescending { it.updatedAt }
        )

        val current = _state.value
        return current.copy(
            allConversations = all,
            conversations = sorted,
            unreadCount = unread,
            needsReplyCount = needsReply,
            filter = filter,
            archivedIds = archivedIds
        )
    }

    private fun loadConversations() {
        loadJob?.cancel()
        loadJob = screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                messageRepository.getDoctorConversations().collect { conversations ->
                    _state.update {
                        applyStateDerivedValues(
                            all = conversations,
                            filter = it.filter,
                            archivedIds = it.archivedIds
                        ).copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.toAppError()) }
            }
        }
    }

    fun refresh() {
        loadConversations()
    }

    fun setFilter(filter: DoctorInboxFilter) {
        _state.update {
            applyStateDerivedValues(
                all = it.allConversations,
                filter = filter,
                archivedIds = it.archivedIds
            )
        }
    }

    fun archiveConversation(conversationId: String) {
        _state.update {
            val nextArchived = it.archivedIds + conversationId
            applyStateDerivedValues(
                all = it.allConversations,
                filter = it.filter,
                archivedIds = nextArchived
            )
        }
    }

    fun markConversationRead(conversationId: String) {
        val current = _state.value
        val optimisticAll = current.allConversations.map { c ->
            if (c.id == conversationId) c.copy(unreadCount = 0) else c
        }

        _state.update {
            applyStateDerivedValues(
                all = optimisticAll,
                filter = it.filter,
                archivedIds = it.archivedIds
            )
        }

        screenModelScope.launch {
            messageRepository.markAsReadByDoctor(conversationId)
                .onFailure { err ->
                    _state.update { it.copy(error = err.toAppError()) }
                }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
