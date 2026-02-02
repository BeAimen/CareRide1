package com.careride.feature.patient.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.careride.domain.model.SubscriptionStatus
import com.careride.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientProfileState(
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.None
)

class PatientProfileViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PatientProfileState())
    val state: StateFlow<PatientProfileState> = _state.asStateFlow()

    init {
        observeSubscription()
    }

    private fun observeSubscription() {
        screenModelScope.launch {
            subscriptionRepository.observeSubscriptionStatus().collect { status ->
                _state.update { it.copy(subscriptionStatus = status) }
            }
        }
    }
}