package com.careride.feature.patient.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.careride.domain.model.Doctor
import com.careride.domain.repository.DoctorRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PatientHomeState(
    val searchQuery: String = "",
    val doctors: List<Doctor> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedDoctorForInfo: Doctor? = null // For "Why am I seeing this?" sheet
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
            try {
                doctorRepository.getAllDoctors().collect { doctors ->
                    _state.update {
                        it.copy(
                            doctors = doctors,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load doctors"
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _state.update { it.copy(searchQuery = query) }
        searchQueryFlow.value = query
    }

    private suspend fun searchDoctors(query: String) {
        _state.update { it.copy(isLoading = true) }
        try {
            doctorRepository.searchDoctors(query).collect { doctors ->
                _state.update {
                    it.copy(
                        doctors = doctors,
                        isLoading = false,
                        error = null
                    )
                }
            }
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }

    fun onRetry() {
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
}