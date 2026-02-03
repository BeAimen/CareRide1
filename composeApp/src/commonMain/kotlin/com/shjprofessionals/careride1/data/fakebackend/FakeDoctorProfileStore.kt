package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.Doctor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory store for the current doctor's editable profile.
 * Simulates profile management for the logged-in doctor.
 */
class FakeDoctorProfileStore {

    // Current logged-in doctor ID
    private val currentDoctorId = "doc_001" // Dr. Sarah Chen

    // Editable profile data (starts with data from FakeBackend.doctors)
    private val _profileFlow = MutableStateFlow<Doctor?>(null)
    val profileFlow: StateFlow<Doctor?> = _profileFlow.asStateFlow()

    init {
        // Initialize with the current doctor's data
        loadCurrentDoctor()
    }

    private fun loadCurrentDoctor() {
        val doctor = FakeBackend.doctors.find { it.id == currentDoctorId }
        _profileFlow.value = doctor
    }

    fun getCurrentDoctor(): Doctor? = _profileFlow.value

    fun getCurrentDoctorId(): String = currentDoctorId

    /**
     * Update the doctor's bio
     */
    fun updateBio(newBio: String): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(bio = newBio)
        _profileFlow.value = updated
        return updated
    }

    /**
     * Toggle availability for today
     */
    fun toggleAvailability(): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(isAvailableToday = !current.isAvailableToday)
        _profileFlow.value = updated
        return updated
    }

    /**
     * Toggle accepting new patients
     */
    fun toggleAcceptingNewPatients(): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(acceptingNewPatients = !current.acceptingNewPatients)
        _profileFlow.value = updated
        return updated
    }

    /**
     * Update location
     */
    fun updateLocation(newLocation: String): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(location = newLocation)
        _profileFlow.value = updated
        return updated
    }

    /**
     * Update languages
     */
    fun updateLanguages(languages: List<String>): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(languages = languages)
        _profileFlow.value = updated
        return updated
    }

    /**
     * Update years of experience
     */
    fun updateExperience(years: Int): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(yearsOfExperience = years)
        _profileFlow.value = updated
        return updated
    }

    /**
     * Full profile update
     */
    fun updateProfile(
        bio: String? = null,
        location: String? = null,
        isAvailableToday: Boolean? = null,
        acceptingNewPatients: Boolean? = null,
        languages: List<String>? = null,
        yearsOfExperience: Int? = null
    ): Doctor? {
        val current = _profileFlow.value ?: return null
        val updated = current.copy(
            bio = bio ?: current.bio,
            location = location ?: current.location,
            isAvailableToday = isAvailableToday ?: current.isAvailableToday,
            acceptingNewPatients = acceptingNewPatients ?: current.acceptingNewPatients,
            languages = languages ?: current.languages,
            yearsOfExperience = yearsOfExperience ?: current.yearsOfExperience
        )
        _profileFlow.value = updated
        return updated
    }

    /**
     * Reset to original data
     */
    fun reset() {
        loadCurrentDoctor()
    }
}