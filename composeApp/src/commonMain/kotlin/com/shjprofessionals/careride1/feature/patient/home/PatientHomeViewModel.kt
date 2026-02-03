package com.shjprofessionals.careride1.feature.patient.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.core.util.ErrorHandler
import com.shjprofessionals.careride1.core.util.Validators
import com.shjprofessionals.careride1.core.util.toAppError
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.Specialty
import com.shjprofessionals.careride1.domain.repository.DoctorRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientHomeState(
    val searchQuery: String = "",
    val selectedSpecialty: Specialty? = null,
    val doctors: List<Doctor> = emptyList(),
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val selectedDoctorForInfo: Doctor? = null
) {
    val hasActiveFilters: Boolean
        get() = searchQuery.isNotEmpty() || selectedSpecialty != null

    val filterDescription: String
        get() = buildString {
            if (selectedSpecialty != null) {
                append(selectedSpecialty.displayName)
            }
            if (searchQuery.isNotEmpty()) {
                if (isNotEmpty()) append(" • ")
                append("\"$searchQuery\"")
            }
        }
}

@OptIn(FlowPreview::class)
class PatientHomeViewModel(
    private val doctorRepository: DoctorRepository
) : ScreenModel {

    private val _state = MutableStateFlow(PatientHomeState())
    val state: StateFlow<PatientHomeState> = _state.asStateFlow()

    private val searchQueryFlow = MutableStateFlow("")
    private val specialtyFlow = MutableStateFlow<Specialty?>(null)

    init {
        observeFilters()
        loadDoctors()
    }

    private fun observeFilters() {
        screenModelScope.launch {
            // Combine search query and specialty filter
            combine(
                searchQueryFlow.debounce(300).distinctUntilChanged(),
                specialtyFlow
            ) { query, specialty ->
                Pair(query, specialty)
            }.collectLatest { (query, specialty) ->
                filterDoctors(query, specialty)
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

    fun onSpecialtySelected(specialty: Specialty?) {
        _state.update { it.copy(selectedSpecialty = specialty) }
        specialtyFlow.value = specialty
    }

    fun clearFilters() {
        _state.update { it.copy(searchQuery = "", selectedSpecialty = null) }
        searchQueryFlow.value = ""
        specialtyFlow.value = null
    }

    private suspend fun filterDoctors(query: String, specialty: Specialty?) {
        _state.update { it.copy(isLoading = true) }

        runCatchingWithHandler(
            onError = { error ->
                _state.update { it.copy(isLoading = false, error = error) }
            },
            retryAction = {
                screenModelScope.launch {
                    filterDoctors(query, specialty)
                }
            }
        ) {
            val flow = when {
                // Both filters active: search first, then filter by specialty
                query.isNotEmpty() && specialty != null -> {
                    doctorRepository.searchDoctors(query).map { doctors ->
                        doctors.filter { it.specialty == specialty }
                    }
                }
                // Only specialty filter
                specialty != null -> {
                    doctorRepository.getDoctorsBySpecialty(specialty)
                }
                // Only search query
                query.isNotEmpty() -> {
                    doctorRepository.searchDoctors(query)
                }
                // No filters
                else -> {
                    doctorRepository.getAllDoctors()
                }
            }

            flow.collect { doctors ->
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
        val currentQuery = _state.value.searchQuery
        val currentSpecialty = _state.value.selectedSpecialty

        if (currentQuery.isEmpty() && currentSpecialty == null) {
            loadDoctors()
        } else {
            screenModelScope.launch {
                filterDoctors(currentQuery, currentSpecialty)
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