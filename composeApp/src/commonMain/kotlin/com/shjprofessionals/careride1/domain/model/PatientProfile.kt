package com.shjprofessionals.careride1.domain.model

data class PatientProfile(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val dateOfBirth: String, // Format: "MM/DD/YYYY"
    val emergencyContact: String,
    val emergencyPhone: String
)

data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val smsEnabled: Boolean = false,
    val appointmentReminders: Boolean = true,
    val messageNotifications: Boolean = true,
    val promotionalEmails: Boolean = false
)