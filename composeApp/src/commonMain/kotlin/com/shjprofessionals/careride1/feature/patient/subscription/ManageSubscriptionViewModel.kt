package com.shjprofessionals.careride1.feature.patient.subscription

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ManageSubscriptionState(
    val status: SubscriptionStatus = SubscriptionStatus.None,
    val isLoading: Boolean = false,
    val showCancelConfirmation: Boolean = false,
    val showReactivateConfirmation: Boolean = false,
    val message: String? = null
)

class ManageSubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ScreenModel {

    private val _state = MutableStateFlow(ManageSubscriptionState())
    val state: StateFlow<ManageSubscriptionState> = _state.asStateFlow()

    init {
        observeSubscription()
    }

    private fun observeSubscription() {
        screenModelScope.launch {
            subscriptionRepository.observeSubscriptionStatus().collect { status ->
                _state.update { it.copy(status = status) }
            }
        }
    }

    fun showCancelConfirmation() {
        _state.update { it.copy(showCancelConfirmation = true) }
    }

    fun hideCancelConfirmation() {
        _state.update { it.copy(showCancelConfirmation = false) }
    }

    fun confirmCancel() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, showCancelConfirmation = false) }

            subscriptionRepository.cancelSubscription()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Subscription cancelled. You'll have access until the end of your billing period."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Failed to cancel: ${error.message}"
                        )
                    }
                }
        }
    }

    fun showReactivateConfirmation() {
        _state.update { it.copy(showReactivateConfirmation = true) }
    }

    fun hideReactivateConfirmation() {
        _state.update { it.copy(showReactivateConfirmation = false) }
    }

    fun confirmReactivate() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, showReactivateConfirmation = false) }

            subscriptionRepository.reactivateSubscription()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Subscription reactivated!"
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            message = "Failed to reactivate: ${error.message}"
                        )
                    }
                }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }
}
