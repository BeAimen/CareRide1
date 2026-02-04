package com.shjprofessionals.careride1.feature.patient.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.domain.model.User
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientProfileState(
    val user: User? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.None
)

class PatientProfileViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val authRepository: AuthRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PatientProfileState())
    val state: StateFlow<PatientProfileState> = _state.asStateFlow()

    init {
        observeAuth()
        observeSubscription()
    }

    private fun observeAuth() {
        screenModelScope.launch {
            authRepository.observeAuthState().collect { authState ->
                _state.update { it.copy(user = authState.currentUser()) }
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
}