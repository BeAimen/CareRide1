package com.shjprofessionals.careride1.domain.model

/**
 * Complete doctor profile for healthcare app
 */
data class DoctorProfile(
    val id: String,

    // Basic Info
    val name: String,
    val email: String,
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,

    // Professional Info
    val title: DoctorTitle = DoctorTitle.MD,
    val specialty: Specialty = Specialty.GENERAL_PRACTICE,
    val subSpecialties: List<String> = emptyList(),
    val licenseNumber: String = "",
    val licenseState: String = "",
    val npiNumber: String = "",
    val yearsOfExperience: Int = 0,

    // Education & Credentials
    val education: List<Education> = emptyList(),
    val boardCertifications: List<String> = emptyList(),
    val hospitalAffiliations: List<String> = emptyList(),
    val awards: List<String> = emptyList(),

    // Practice Info
    val practiceName: String = "",
    val practiceAddress: Address = Address(),
    val officePhone: String = "",
    val officeHours: List<OfficeHours> = defaultOfficeHours(),
    val consultationFeeMin: Int = 0, // In cents
    val consultationFeeMax: Int = 0,
    val acceptedInsurance: List<String> = emptyList(),

    // Availability
    val isAvailableToday: Boolean = true,
    val acceptingNewPatients: Boolean = true,
    val averageWaitTimeDays: Int = 3,
    val offersTelehealth: Boolean = true,
    val offersInPerson: Boolean = true,

    // Languages
    val languages: List<String> = listOf("English"),

    // Bio & Expertise
    val bio: String = "",
    val treatmentPhilosophy: String = "",
    val areasOfExpertise: List<String> = emptyList(),
    val proceduresOffered: List<String> = emptyList(),
    val conditionsTreated: List<String> = emptyList(),

    // Ratings & Visibility
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val isBoosted: Boolean = false,
    val profileViews: Int = 0
) {
    val displayName: String get() = "${title.prefix}$name"

    val experienceDisplay: String get() = when {
        yearsOfExperience == 0 -> "New to practice"
        yearsOfExperience == 1 -> "1 year experience"
        yearsOfExperience < 5 -> "$yearsOfExperience years experience"
        yearsOfExperience < 10 -> "$yearsOfExperience+ years experience"
        yearsOfExperience < 20 -> "${(yearsOfExperience / 5) * 5}+ years experience"
        else -> "20+ years experience"
    }

    val feeRangeDisplay: String get() = when {
        consultationFeeMin == 0 && consultationFeeMax == 0 -> "Contact for pricing"
        consultationFeeMin == consultationFeeMax -> "$${consultationFeeMin / 100}"
        else -> "$${consultationFeeMin / 100} - $${consultationFeeMax / 100}"
    }

    val hasCompleteProfile: Boolean get() =
        name.isNotBlank() &&
                phone.isNotBlank() &&
                bio.isNotBlank() &&
                practiceAddress.city.isNotBlank()

    val profileCompletionPercent: Int get() {
        var completed = 0
        var total = 0

        // Basic (weight: 3)
        total += 3
        if (name.isNotBlank()) completed++
        if (phone.isNotBlank()) completed++
        if (licenseNumber.isNotBlank()) completed++

        // Professional (weight: 3)
        total += 3
        if (yearsOfExperience > 0) completed++
        if (education.isNotEmpty()) completed++
        if (boardCertifications.isNotEmpty()) completed++

        // Practice (weight: 2)
        total += 2
        if (practiceAddress.city.isNotBlank()) completed++
        if (officePhone.isNotBlank()) completed++

        // Bio (weight: 2)
        total += 2
        if (bio.isNotBlank()) completed++
        if (areasOfExpertise.isNotEmpty()) completed++

        return ((completed.toFloat() / total) * 100).toInt()
    }

    /**
     * Convert to the simpler Doctor model for listings
     */
    fun toDoctor(): Doctor = Doctor(
        id = id,
        name = displayName,
        specialty = specialty,
        imageUrl = null,
        location = practiceAddress.formatted.ifBlank { "Location not set" },
        rating = rating,
        reviewCount = reviewCount,
        isAvailableToday = isAvailableToday,
        isBoosted = isBoosted,
        bio = bio,
        yearsOfExperience = yearsOfExperience,
        languages = languages,
        acceptingNewPatients = acceptingNewPatients
    )
}

data class Education(
    val degree: String, // e.g., "MD", "DO", "PhD"
    val institution: String,
    val year: Int,
    val honors: String = ""
) {
    val display: String get() = "$degree - $institution ($year)"
}

data class OfficeHours(
    val dayOfWeek: DayOfWeek,
    val openTime: String, // "09:00"
    val closeTime: String, // "17:00"
    val isClosed: Boolean = false
) {
    val display: String get() = if (isClosed) "Closed" else "$openTime - $closeTime"
}

enum class DayOfWeek(val displayName: String, val shortName: String) {
    MONDAY("Monday", "Mon"),
    TUESDAY("Tuesday", "Tue"),
    WEDNESDAY("Wednesday", "Wed"),
    THURSDAY("Thursday", "Thu"),
    FRIDAY("Friday", "Fri"),
    SATURDAY("Saturday", "Sat"),
    SUNDAY("Sunday", "Sun")
}

enum class DoctorTitle(val prefix: String, val fullTitle: String) {
    MD("Dr. ", "Medical Doctor"),
    DO("Dr. ", "Doctor of Osteopathic Medicine"),
    PHD("Dr. ", "Doctor of Philosophy"),
    NP("", "Nurse Practitioner"),
    PA("", "Physician Assistant"),
    RN("", "Registered Nurse")
}

fun defaultOfficeHours(): List<OfficeHours> = listOf(
    OfficeHours(DayOfWeek.MONDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.TUESDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.WEDNESDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.THURSDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.FRIDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.SATURDAY, "10:00", "14:00"),
    OfficeHours(DayOfWeek.SUNDAY, "00:00", "00:00", isClosed = true)
)