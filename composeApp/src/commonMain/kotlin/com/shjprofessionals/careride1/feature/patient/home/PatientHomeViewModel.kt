package com.shjprofessionals.careride1.feature.patient.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.core.util.ErrorHandler
import com.shjprofessionals.careride1.core.util.Validators
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.repository.DoctorRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.shjprofessionals.careride1.core.util.toAppError
data class PatientHomeState(
    val searchQuery: String = "",
    val doctors: List<Doctor> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val selectedDoctorForInfo: Doctor? = null
)

@OptIn(FlowPreview::class)
class PatientHomeViewModel(
    private val doctorRepository: DoctorRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PatientHomeState())
    val state: StateFlow<PatientHomeState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")

    init {
        observeSearch()
        loadDoctors()
    }

    private fun observeSearch() {
        screenModelScope.launch {
            searchQueryFlow
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    searchDoctors(query)
                }
        }
    }

    private fun loadDoctors() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            runCatchingWithHandler(
                onError = { error ->
                    _state.update { it.copy(isLoading = false, error = error) }
                },
                retryAction = ::loadDoctors
            ) {
                doctorRepository.getAllDoctors().collect { doctors ->
                    _state.update {
                        it.copy(
                            doctors = doctors,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        val validation = Validators.validateSearchQuery(query)

        validation.onValid { sanitized ->
            _state.update { it.copy(searchQuery = sanitized) }
            searchQueryFlow.value = sanitized
        }
    }

    private suspend fun searchDoctors(query: String) {
        _state.update { it.copy(isLoading = true) }

        runCatchingWithHandler(
            onError = { error ->
                _state.update { it.copy(isLoading = false, error = error) }
            },
            retryAction = { screenModelScope.launch { searchDoctors(query) } }
        ) {
            doctorRepository.searchDoctors(query).collect { doctors ->
                _state.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false,
                        error = null
                    )
                }
            }
        }
    }

    fun onRetry() {
        _state.update { it.copy(error = null) }
        if (_state.value.searchQuery.isEmpty()) {
            loadDoctors()
        } else {
            screenModelScope.launch {
                searchDoctors(_state.value.searchQuery)
            }
        }
    }

    fun showWhyThisDoctor(doctor: Doctor) {
        _state.update { it.copy(selectedDoctorForInfo = doctor) }
    }

    fun hideWhyThisDoctor() {
        _state.update { it.copy(selectedDoctorForInfo = null) }
    }

    fun dismissError() {
        _state.update { it.copy(error = null) }
    }
}

/**
 * Helper to run with error handling
 */
private inline fun ScreenModel.runCatchingWithHandler(
    onError: (AppError) -> Unit,
    noinline retryAction: (() -> Unit)? = null,
    block: () -> Unit
) {
    try {
        block()
    } catch (e: Exception) {
        val appError = e.toAppError()
        onError(appError)
        ErrorHandler.emit(appError, retryAction)
    }
}
