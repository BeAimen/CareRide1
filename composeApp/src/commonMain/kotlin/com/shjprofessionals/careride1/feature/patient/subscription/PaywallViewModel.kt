package com.careride.feature.patient.subscription

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.careride.domain.model.SubscriptionPlan
import com.careride.domain.model.SubscriptionStatus
import com.careride.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PaywallState(
    val plans: List<SubscriptionPlan> = emptyList(),
    val selectedPlan: SubscriptionPlan? = null,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.None,
    val isLoading: Boolean = true,
    val isRestoring: Boolean = false,
    val error: String? = null,
    val restoreMessage: String? = null
)

class PaywallViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PaywallState())
    val state: StateFlow<PaywallState> = _state.asStateFlow()

    init {
        loadPlans()
        observeSubscription()
    }

    private fun loadPlans() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val plans = subscriptionRepository.getAvailablePlans()
                val popularPlan = plans.find { it.isPopular } ?: plans.firstOrNull()
                _state.update {
                    it.copy(
                        plans = plans,
                        selectedPlan = popularPlan,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load plans"
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

    fun selectPlan(plan: SubscriptionPlan) {
        _state.update { it.copy(selectedPlan = plan) }
    }

    fun restorePurchases() {
        screenModelScope.launch {
            _state.update { it.copy(isRestoring = true, restoreMessage = null) }

            subscriptionRepository.restorePurchases()
                .onSuccess { status ->
                    val message = when (status) {
                        is SubscriptionStatus.Active -> "Subscription restored successfully!"
                        is SubscriptionStatus.Cancelled -> "Found cancelled subscription, active until ${status.activeUntilFormatted}"
                        else -> "No active subscription found"
                    }
                    _state.update {
                        it.copy(
                            isRestoring = false,
                            restoreMessage = message,
                            subscriptionStatus = status
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isRestoring = false,
                            restoreMessage = "Restore failed: ${error.message}"
                        )
                    }
                }
        }
    }

    fun clearRestoreMessage() {
        _state.update { it.copy(restoreMessage = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}