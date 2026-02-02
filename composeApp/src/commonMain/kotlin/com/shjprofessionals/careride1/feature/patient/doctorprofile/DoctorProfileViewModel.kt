package com.shjprofessionals.careride1.feature.patient.doctorprofile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.Conversation
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.domain.repository.DoctorRepository
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DoctorProfileState(
    val doctor: Doctor? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.None,
    val showGatingSheet: Boolean = false,
    val navigateToChat: Conversation? = null
)

class DoctorProfileViewModel(
    private val doctorId: String,
    private val doctorRepository: DoctorRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val messageRepository: MessageRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DoctorProfileState())
    val state: StateFlow<DoctorProfileState> = _state.asStateFlow()

    init {
        loadDoctor()
        observeSubscription()
    }

    private fun loadDoctor() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val doctor = doctorRepository.getDoctorById(doctorId)
                if (doctor != null) {
                    _state.update {
                        it.copy(
                            doctor = doctor,
                            isLoading = false
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = "Doctor not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load doctor profile"
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

    fun onMessageDoctorClick() {
        val canMessage = _state.value.subscriptionStatus.canMessage()
        if (canMessage) {
            // Create or get conversation and navigate
            screenModelScope.launch {
                messageRepository.getOrCreateConversation(doctorId)
                    .onSuccess { conversation ->
                        _state.update { it.copy(navigateToChat = conversation) }
                    }
                    .onFailure {
                        _state.update { it.copy(error = "Failed to start conversation") }
                    }
            }
        } else {
            _state.update { it.copy(showGatingSheet = true) }
        }
    }

    fun dismissGatingSheet() {
        _state.update { it.copy(showGatingSheet = false) }
    }

    fun onNavigatedToChat() {
        _state.update { it.copy(navigateToChat = null) }
    }

    fun onRetry() {
        loadDoctor()
    }
}
