package com.shjprofessionals.careride1.feature.patient.subscription

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.SubscriptionPlan
import com.shjprofessionals.careride1.domain.model.SubscriptionPlans
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CheckoutStep {
    REVIEW,
    PROCESSING,
    SUCCESS,
    FAILURE,
    CANCELLED
}

data class MockCheckoutState(
    val plan: SubscriptionPlan? = null,
    val step: CheckoutStep = CheckoutStep.REVIEW,
    val isLoading: Boolean = true,
    val error: String? = null,
    val subscriptionStatus: SubscriptionStatus? = null
)

class MockCheckoutViewModel(
    private val planId: String,
    private val subscriptionRepository: SubscriptionRepository
) : ScreenModel {

    private val _state = MutableStateFlow(MockCheckoutState())
    val state: StateFlow<MockCheckoutState> = _state.asStateFlow()

    init {
        loadPlan()
    }

    private fun loadPlan() {
        val plan = SubscriptionPlans.getById(planId)
        _state.update {
            it.copy(
                plan = plan,
                isLoading = false,
                error = if (plan == null) "Invalid plan" else null
            )
        }
    }

    fun processPayment() {
        screenModelScope.launch {
            _state.update { it.copy(step = CheckoutStep.PROCESSING) }

            // Simulate payment processing delay
            delay(2000)

            // Confirm subscription with repository
            subscriptionRepository.confirmSubscription(planId)
                .onSuccess { status ->
                    _state.update {
                        it.copy(
                            step = CheckoutStep.SUCCESS,
                            subscriptionStatus = status
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            step = CheckoutStep.FAILURE,
                            error = error.message ?: "Payment failed"
                        )
                    }
                }
        }
    }

    fun simulateFailure() {
        screenModelScope.launch {
            _state.update { it.copy(step = CheckoutStep.PROCESSING) }
            delay(1500)
            _state.update {
                it.copy(
                    step = CheckoutStep.FAILURE,
                    error = "Payment was declined. Please try again."
                )
            }
        }
    }

    fun cancelCheckout() {
        _state.update { it.copy(step = CheckoutStep.CANCELLED) }
    }

    fun retry() {
        _state.update { it.copy(step = CheckoutStep.REVIEW, error = null) }
    }
}
