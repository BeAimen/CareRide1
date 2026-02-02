package com.shjprofessionals.careride1.feature.doctor.boost

import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.core.util.toAppError

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.BoostPlan
import com.shjprofessionals.careride1.domain.model.BoostPlans
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus
import com.shjprofessionals.careride1.domain.repository.BoostRepository
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
    val error: AppError? = null,
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
                error = if (plan == null) AppError.Validation("Invalid plan") else null
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
                            error = error.toAppError()
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
                    error = AppError.Payment()
                )
            }
        }
    }

    fun retry() {
        _state.update { it.copy(step = BoostCheckoutStep.REVIEW, error = null) }
    }
}

