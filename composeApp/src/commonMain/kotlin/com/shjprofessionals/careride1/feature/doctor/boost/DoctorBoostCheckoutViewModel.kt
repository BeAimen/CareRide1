package com.careride.feature.doctor.boost

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.careride.domain.model.BoostPlan
import com.careride.domain.model.BoostPlans
import com.careride.domain.model.DoctorBoostStatus
import com.careride.domain.repository.BoostRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class BoostCheckoutStep {
    REVIEW,
    PROCESSING,
    SUCCESS,
    FAILURE
}

data class BoostCheckoutState(
    val plan: BoostPlan? = null,
    val step: BoostCheckoutStep = BoostCheckoutStep.REVIEW,
    val isLoading: Boolean = true,
    val error: String? = null,
    val boostStatus: DoctorBoostStatus? = null
)

class DoctorBoostCheckoutViewModel(
    private val planId: String,
    private val boostRepository: BoostRepository
) : ScreenModel {

    private val _state = MutableStateFlow(BoostCheckoutState())
    val state: StateFlow<BoostCheckoutState> = _state.asStateFlow()

    init {
        loadPlan()
    }

    private fun loadPlan() {
        val plan = BoostPlans.getById(planId)
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
            _state.update { it.copy(step = BoostCheckoutStep.PROCESSING) }

            // Simulate payment processing
            delay(2000)

            boostRepository.confirmBoost(planId)
                .onSuccess { status ->
                    _state.update {
                        it.copy(
                            step = BoostCheckoutStep.SUCCESS,
                            boostStatus = status
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            step = BoostCheckoutStep.FAILURE,
                            error = error.message ?: "Payment failed"
                        )
                    }
                }
        }
    }

    fun simulateFailure() {
        screenModelScope.launch {
            _state.update { it.copy(step = BoostCheckoutStep.PROCESSING) }
            delay(1500)
            _state.update {
                it.copy(
                    step = BoostCheckoutStep.FAILURE,
                    error = "Payment was declined. Please try again."
                )
            }
        }
    }

    fun retry() {
        _state.update { it.copy(step = BoostCheckoutStep.REVIEW, error = null) }
    }
}