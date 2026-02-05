package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePatientProfileStore {

    private val _profile = MutableStateFlow<PatientProfile?>(null)
    val profile: StateFlow<PatientProfile?> = _profile.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    /**
     * Initialize or update profile from authenticated user
     */
    fun syncWithAuthUser(user: User) {
        val currentProfile = _profile.value

        if (currentProfile == null) {
            _profile.value = PatientProfile(
                id = user.id,
                name = user.name,
                email = user.email
            )
        } else {
            _profile.value = currentProfile.copy(
                id = user.id,
                name = user.name,
                email = user.email
            )
        }
    }

    fun getProfile(): PatientProfile? = _profile.value

    fun updateBasicInfo(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dateOfBirth: String? = null,
        gender: Gender? = null
    ): PatientProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            name = name ?: current.name,
            email = email ?: current.email,
            phone = phone ?: current.phone,
            dateOfBirth = dateOfBirth ?: current.dateOfBirth,
            gender = gender ?: current.gender
        )
        _profile.value = updated
        return updated
    }

    fun updateAddress(address: Address): PatientProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(address = address)
        _profile.value = updated
        return updated
    }

    fun updateMedicalInfo(
        bloodType: BloodType? = null,
        allergies: List<String>? = null,
        medications: List<String>? = null,
        medicalConditions: List<String>? = null,
        primaryPhysician: String? = null
    ): PatientProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            bloodType = bloodType ?: current.bloodType,
            allergies = allergies ?: current.allergies,
            medications = medications ?: current.medications,
            medicalConditions = medicalConditions ?: current.medicalConditions,
            primaryPhysician = primaryPhysician ?: current.primaryPhysician
        )
        _profile.value = updated
        return updated
    }

    fun updateInsurance(insurance: InsuranceInfo): PatientProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(insurance = insurance)
        _profile.value = updated
        return updated
    }

    fun updateEmergencyContact(contact: EmergencyContact): PatientProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(emergencyContact = contact)
        _profile.value = updated
        return updated
    }

    fun updatePreferences(
        preferredLanguage: String? = null,
        communicationPreference: CommunicationPreference? = null,
        accessibilityNeeds: List<String>? = null
    ): PatientProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            preferredLanguage = preferredLanguage ?: current.preferredLanguage,
            communicationPreference = communicationPreference ?: current.communicationPreference,
            accessibilityNeeds = accessibilityNeeds ?: current.accessibilityNeeds
        )
        _profile.value = updated
        return updated
    }

    fun updateNotificationSettings(settings: NotificationSettings) {
        _notificationSettings.value = settings
    }

    fun updateSingleNotificationSetting(
        pushEnabled: Boolean? = null,
        emailEnabled: Boolean? = null,
        smsEnabled: Boolean? = null,
        appointmentReminders: Boolean? = null,
        messageNotifications: Boolean? = null,
        healthTips: Boolean? = null,
        promotionalEmails: Boolean? = null,
        reminderTimeMinutes: Int? = null
    ) {
        val current = _notificationSettings.value
        _notificationSettings.value = current.copy(
            pushEnabled = pushEnabled ?: current.pushEnabled,
            emailEnabled = emailEnabled ?: current.emailEnabled,
            smsEnabled = smsEnabled ?: current.smsEnabled,
            appointmentReminders = appointmentReminders ?: current.appointmentReminders,
            messageNotifications = messageNotifications ?: current.messageNotifications,
            healthTips = healthTips ?: current.healthTips,
            promotionalEmails = promotionalEmails ?: current.promotionalEmails,
            reminderTimeMinutes = reminderTimeMinutes ?: current.reminderTimeMinutes
        )
    }

    fun clear() {
        _profile.value = null
        _notificationSettings.value = NotificationSettings()
    }

    fun deleteAccount() {
        _profile.value = null
        _notificationSettings.value = NotificationSettings()
    }
}