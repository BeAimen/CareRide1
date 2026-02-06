package com.shjprofessionals.careride1.domain.model

data class DoctorProfile(
    val id: String,

    val name: String,
    val email: String,
    val phone: String = "",
    val dateOfBirth: String = "",
    val gender: Gender = Gender.PREFER_NOT_TO_SAY,

    val title: DoctorTitle = DoctorTitle.MD,
    val specialty: Specialty = Specialty.GENERAL_PRACTICE,
    val subSpecialties: List<String> = emptyList(),
    val licenseNumber: String = "",
    val licenseState: String = "",
    val npiNumber: String = "",
    val yearsOfExperience: Int = 0,

    val education: List<Education> = emptyList(),
    val boardCertifications: List<String> = emptyList(),
    val hospitalAffiliations: List<String> = emptyList(),
    val awards: List<String> = emptyList(),

    val practiceName: String = "",
    val practiceAddress: Address = Address(),
    val officePhone: String = "",
    val officeHours: List<OfficeHours> = defaultOfficeHours(),
    val consultationFeeMin: Int = 0,
    val consultationFeeMax: Int = 0,
    val acceptedInsurance: List<String> = emptyList(),

    val isAvailableToday: Boolean = true,
    val acceptingNewPatients: Boolean = true,
    val averageWaitTimeDays: Int = 3,
    val offersTelehealth: Boolean = true,
    val offersInPerson: Boolean = true,

    val bio: String = "",
    val treatmentPhilosophy: String = "",
    val areasOfExpertise: List<String> = emptyList(),
    val proceduresOffered: List<String> = emptyList(),
    val conditionsTreated: List<String> = emptyList(),

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

        total += 3
        if (name.isNotBlank()) completed++
        if (phone.isNotBlank()) completed++
        if (licenseNumber.isNotBlank()) completed++

        total += 3
        if (yearsOfExperience > 0) completed++
        if (education.isNotEmpty()) completed++
        if (boardCertifications.isNotEmpty()) completed++

        total += 2
        if (practiceAddress.city.isNotBlank()) completed++
        if (officePhone.isNotBlank()) completed++

        total += 2
        if (bio.isNotBlank()) completed++
        if (areasOfExpertise.isNotEmpty()) completed++

        return ((completed.toFloat() / total) * 100).toInt()
    }

    fun toDoctor(): Doctor = Doctor(
        id = id,
        name = displayName,
        specialty = specialty,
        location = practiceAddress.formatted.ifBlank { "Location not set" },
        rating = rating,
        reviewCount = reviewCount,
        isAvailableToday = isAvailableToday,
        isBoosted = isBoosted,
        bio = bio,
        yearsOfExperience = yearsOfExperience,
        acceptingNewPatients = acceptingNewPatients
    )
}

data class Education(
    val degree: String,
    val institution: String,
    val year: Int,
    val honors: String = ""
) {
    val display: String get() = "$degree - $institution ($year)"
}

data class OfficeHours(
    val dayOfWeek: DayOfWeek,
    val openTime: String,
    val closeTime: String,
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
    NP("", "Nurse Practitioner"),
    PA("", "Physician Assistant"),
    PSY("", "Psychologist"),
    THER("", "Therapist")
}

fun defaultOfficeHours(): List<OfficeHours> = listOf(
    OfficeHours(DayOfWeek.MONDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.TUESDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.WEDNESDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.THURSDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.FRIDAY, "09:00", "17:00"),
    OfficeHours(DayOfWeek.SATURDAY, "09:00", "13:00", isClosed = true),
    OfficeHours(DayOfWeek.SUNDAY, "09:00", "13:00", isClosed = true)
)
