package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.NotificationSettings
import com.shjprofessionals.careride1.domain.model.PatientProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakePatientProfileStore {

    private val _profile = MutableStateFlow(
        PatientProfile(
            id = "patient_001",
            name = "John Smith",
            email = "john.smith@email.com",
            phone = "(555) 123-4567",
            dateOfBirth = "03/15/1985",
            emergencyContact = "Jane Smith",
            emergencyPhone = "(555) 987-6543"
        )
    )
    val profile: StateFlow<PatientProfile> = _profile.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettings())
    val notificationSettings: StateFlow<NotificationSettings> = _notificationSettings.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    fun updateProfile(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dateOfBirth: String? = null,
        emergencyContact: String? = null,
        emergencyPhone: String? = null
    ): PatientProfile {
        val current = _profile.value
        val updated = current.copy(
            name = name ?: current.name,
            email = email ?: current.email,
            phone = phone ?: current.phone,
            dateOfBirth = dateOfBirth ?: current.dateOfBirth,
            emergencyContact = emergencyContact ?: current.emergencyContact,
            emergencyPhone = emergencyPhone ?: current.emergencyPhone
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
        promotionalEmails: Boolean? = null
    ) {
        val current = _notificationSettings.value
        _notificationSettings.value = current.copy(
            pushEnabled = pushEnabled ?: current.pushEnabled,
            emailEnabled = emailEnabled ?: current.emailEnabled,
            smsEnabled = smsEnabled ?: current.smsEnabled,
            appointmentReminders = appointmentReminders ?: current.appointmentReminders,
            messageNotifications = messageNotifications ?: current.messageNotifications,
            promotionalEmails = promotionalEmails ?: current.promotionalEmails
        )
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    fun deleteAccount() {
        _profile.value = PatientProfile(
            id = "",
            name = "",
            email = "",
            phone = "",
            dateOfBirth = "",
            emergencyContact = "",
            emergencyPhone = ""
        )
        _isLoggedIn.value = false
    }
}