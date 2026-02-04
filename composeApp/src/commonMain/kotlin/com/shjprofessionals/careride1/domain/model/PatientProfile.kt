package com.shjprofessionals.careride1.domain.model

/**
 * Complete patient profile for healthcare app
 */
data class PatientProfile(
    val id: String,

    // Basic Info
    val name: String,
    val email: String,
    val phone: String = "",
    val dateOfBirth: String = "", // MM/DD/YYYY
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,

    // Address
    val address: Address = Address(),

    // Medical Info
    val bloodType: BloodType = BloodType.UNKNOWN,
    val allergies: List<String> = emptyList(),
    val medications: List<String> = emptyList(),
    val medicalConditions: List<String> = emptyList(),
    val primaryPhysician: String = "",

    // Insurance
    val insurance: InsuranceInfo = InsuranceInfo(),

    // Emergency Contact
    val emergencyContact: EmergencyContact = EmergencyContact(),

    // Preferences
    val preferredLanguage: String = "English",
    val communicationPreference: CommunicationPreference = CommunicationPreference.EMAIL,
    val accessibilityNeeds: List<String> = emptyList()
) {
    val displayName: String get() = name.ifBlank { "Patient" }

    val hasCompleteProfile: Boolean get() =
        name.isNotBlank() &&
                phone.isNotBlank() &&
                dateOfBirth.isNotBlank()

    val hasEmergencyContact: Boolean get() =
        emergencyContact.name.isNotBlank() &&
                emergencyContact.phone.isNotBlank()

    val hasMedicalInfo: Boolean get() =
        bloodType != BloodType.UNKNOWN ||
                allergies.isNotEmpty() ||
                medications.isNotEmpty()

    val hasInsurance: Boolean get() =
        insurance.provider.isNotBlank()

    val profileCompletionPercent: Int get() {
        var completed = 0
        var total = 0

        // Basic (weight: 4)
        total += 4
        if (name.isNotBlank()) completed++
        if (phone.isNotBlank()) completed++
        if (dateOfBirth.isNotBlank()) completed++
        if (gender != Gender.PREFER_NOT_TO_SAY) completed++

        // Address (weight: 2)
        total += 2
        if (address.city.isNotBlank()) completed++
        if (address.state.isNotBlank()) completed++

        // Medical (weight: 2)
        total += 2
        if (bloodType != BloodType.UNKNOWN) completed++
        if (allergies.isNotEmpty() || medicalConditions.isNotEmpty()) completed++

        // Emergency (weight: 2)
        total += 2
        if (emergencyContact.name.isNotBlank()) completed++
        if (emergencyContact.phone.isNotBlank()) completed++

        return ((completed.toFloat() / total) * 100).toInt()
    }
}

data class Address(
    val street: String = "",
    val apartment: String = "",
    val city: String = "",
    val state: String = "",
    val zipCode: String = "",
    val country: String = "United States"
) {
    val formatted: String get() = buildString {
        if (street.isNotBlank()) append(street)
        if (apartment.isNotBlank()) append(", $apartment")
        if (city.isNotBlank()) append(", $city")
        if (state.isNotBlank()) append(", $state")
        if (zipCode.isNotBlank()) append(" $zipCode")
    }.trimStart(',', ' ')

    val isComplete: Boolean get() =
        street.isNotBlank() && city.isNotBlank() && state.isNotBlank() && zipCode.isNotBlank()
}

data class InsuranceInfo(
    val provider: String = "",
    val planName: String = "",
    val policyNumber: String = "",
    val groupNumber: String = "",
    val subscriberName: String = "",
    val subscriberRelationship: String = "Self"
)

data class EmergencyContact(
    val name: String = "",
    val relationship: String = "",
    val phone: String = "",
    val email: String = ""
)

enum class Gender(val displayName: String) {
    MALE("Male"),
    FEMALE("Female"),
    NON_BINARY("Non-binary"),
    OTHER("Other"),
    PREFER_NOT_TO_SAY("Prefer not to say")
}

enum class BloodType(val displayName: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-"),
    UNKNOWN("Unknown")
}

enum class CommunicationPreference(val displayName: String) {
    EMAIL("Email"),
    SMS("Text Message"),
    PHONE("Phone Call"),
    APP_NOTIFICATION("App Notification")
}

data class NotificationSettings(
    val pushEnabled: Boolean = true,
    val emailEnabled: Boolean = true,
    val smsEnabled: Boolean = false,
    val appointmentReminders: Boolean = true,
    val messageNotifications: Boolean = true,
    val healthTips: Boolean = true,
    val promotionalEmails: Boolean = false,
    val reminderTimeMinutes: Int = 60
)