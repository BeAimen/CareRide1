package com.shjprofessionals.careride1.feature.doctor.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.shjprofessionals.careride1.core.util.AppError
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.BoostAnalytics
import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus
import com.shjprofessionals.careride1.domain.repository.BoostRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorProfileState(
    val doctor: Doctor? = null,
    val boostStatus: DoctorBoostStatus = DoctorBoostStatus.None,
    val analytics: BoostAnalytics? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isEditMode: Boolean = false,
    val error: AppError? = null,
    val message: String? = null,
    val editBio: String = "",
    val editLocation: String = "",
    val editYearsExperience: String = ""
) {
    val hasUnsavedChanges: Boolean
        get() = doctor?.let {
            editBio != it.bio ||
                    editLocation != it.location ||
                    editYearsExperience != it.yearsOfExperience.toString()
        } ?: false
}

class DoctorProfileViewModel(
    private val boostRepository: BoostRepository
) : ScreenModel {

    private val profileStore = FakeBackend.doctorProfileStore

    private val _state = MutableStateFlow(DoctorProfileState())
    val state: StateFlow<DoctorProfileState> = _state.asStateFlow()

    init {
        loadProfile()
        observeBoostStatus()
    }

    private fun loadProfile() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }

            try {
                profileStore.profileFlow.collect { profile ->
                    val analytics = boostRepository.getAnalytics()
                    val doctor = profile?.toDoctor()

                    _state.update { current ->
                        current.copy(
                            doctor = doctor,
                            analytics = analytics,
                            isLoading = false,
                            editBio = profile?.bio ?: "",
                            editLocation = doctor?.location ?: "",
                            editYearsExperience = profile?.yearsOfExperience?.toString() ?: ""
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = AppError.Unknown(e.message ?: "Failed to load profile")
                    )
                }
            }
        }
    }

    private fun observeBoostStatus() {
        screenModelScope.launch {
            boostRepository.observeBoostStatus().collect { status ->
                _state.update { it.copy(boostStatus = status) }
            }
        }
    }

    fun toggleEditMode() {
        val current = _state.value

        if (current.isEditMode && current.hasUnsavedChanges) {
            _state.update { state ->
                state.copy(
                    isEditMode = false,
                    editBio = state.doctor?.bio ?: "",
                    editLocation = state.doctor?.location ?: "",
                    editYearsExperience = state.doctor?.yearsOfExperience?.toString() ?: ""
                )
            }
        } else {
            _state.update { it.copy(isEditMode = !it.isEditMode) }
        }
    }

    fun onBioChange(value: String) {
        _state.update { it.copy(editBio = value) }
    }

    fun onLocationChange(value: String) {
        _state.update { it.copy(editLocation = value) }
    }

    fun onYearsExperienceChange(value: String) {
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _state.update { it.copy(editYearsExperience = value) }
        }
    }

    fun toggleAvailability() {
        screenModelScope.launch {
            profileStore.toggleAvailability()
            _state.update { it.copy(message = "Availability updated") }
        }
    }

    fun toggleAcceptingNewPatients() {
        screenModelScope.launch {
            profileStore.toggleAcceptingNewPatients()
            _state.update { it.copy(message = "Patient acceptance status updated") }
        }
    }

    fun saveChanges() {
        screenModelScope.launch {
            _state.update { it.copy(isSaving = true) }

            delay(500)

            val current = _state.value
            val years = current.editYearsExperience.toIntOrNull() ?: 0

            profileStore.updateProfile(
                bio = current.editBio,
                location = current.editLocation,
                yearsOfExperience = years
            )

            _state.update {
                it.copy(
                    isSaving = false,
                    isEditMode = false,
                    message = "Profile updated successfully"
                )
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun refresh() {
        screenModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            delay(300)
            val analytics = boostRepository.getAnalytics()
            _state.update { it.copy(analytics = analytics, isLoading = false) }
        }
    }
}
