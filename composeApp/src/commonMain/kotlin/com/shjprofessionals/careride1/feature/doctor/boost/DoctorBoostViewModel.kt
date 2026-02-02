package com.shjprofessionals.careride1.feature.doctor.boost

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.domain.model.BoostAnalytics
import com.shjprofessionals.careride1.domain.model.BoostPlan
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus
import com.shjprofessionals.careride1.domain.repository.BoostRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DoctorBoostState(
    val status: DoctorBoostStatus = DoctorBoostStatus.None,
    val plans: List<BoostPlan> = emptyList(),
    val selectedPlan: BoostPlan? = null,
    val analytics: BoostAnalytics? = null,
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val showCancelDialog: Boolean = false,
    val showReactivateDialog: Boolean = false,
    val message: String? = null
)

class DoctorBoostViewModel(
    private val boostRepository: BoostRepository
) : ScreenModel {

    private val _state = MutableStateFlow(DoctorBoostState())
    val state: StateFlow<DoctorBoostState> = _state.asStateFlow()

    init {
        loadData()
        observeStatus()
    }

    private fun loadData() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val plans = boostRepository.getAvailablePlans()
                val analytics = boostRepository.getAnalytics()
                val popularPlan = plans.find { it.isPopular } ?: plans.firstOrNull()

                _state.update {
                    it.copy(
                        plans = plans,
                        selectedPlan = popularPlan,
                        analytics = analytics,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        message = e.message ?: "Failed to load data"
                    )
                }
            }
        }
    }

    private fun observeStatus() {
        screenModelScope.launch {
            boostRepository.observeBoostStatus().collect { status ->
                _state.update { it.copy(status = status) }
            }
        }
    }

    fun selectPlan(plan: BoostPlan) {
        _state.update { it.copy(selectedPlan = plan) }
    }

    fun showCancelDialog() {
        _state.update { it.copy(showCancelDialog = true) }
    }

    fun dismissCancelDialog() {
        _state.update { it.copy(showCancelDialog = false) }
    }

    fun confirmCancel() {
        screenModelScope.launch {
            _state.update { it.copy(isProcessing = true, showCancelDialog = false) }

            boostRepository.cancelBoost()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            message = "Boost cancelled. You'll remain visible until your billing period ends."
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            message = "Failed to cancel: ${error.message}"
                        )
                    }
                }
        }
    }

    fun showReactivateDialog() {
        _state.update { it.copy(showReactivateDialog = true) }
    }

    fun dismissReactivateDialog() {
        _state.update { it.copy(showReactivateDialog = false) }
    }

    fun confirmReactivate() {
        screenModelScope.launch {
            _state.update { it.copy(isProcessing = true, showReactivateDialog = false) }

            boostRepository.reactivateBoost()
                .onSuccess {
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            message = "Boost reactivated!"
                        )
                    }
                    // Refresh analytics
                    loadAnalytics()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            message = "Failed to reactivate: ${error.message}"
                        )
                    }
                }
        }
    }

    private fun loadAnalytics() {
        screenModelScope.launch {
            try {
                val analytics = boostRepository.getAnalytics()
                _state.update { it.copy(analytics = analytics) }
            } catch (e: Exception) {
                // Silently fail for analytics refresh
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun refresh() {
        loadData()
    }
}
