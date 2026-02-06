package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.Specialty
import com.shjprofessionals.careride1.domain.model.SubscriptionPlan
import com.shjprofessionals.careride1.domain.model.SubscriptionPlans

object FakeBackend {

    val doctors: List<Doctor> = listOf(
        Doctor(
            id = "doc_001",
            name = "Dr. Sarah Chen",
            specialty = Specialty.CARDIOLOGY,
            location = "San Francisco, CA",
            rating = 4.9f,
            reviewCount = 127,
            isAvailableToday = true,
            isBoosted = true,
            bio = "Board-certified cardiologist with 15 years of experience in preventive cardiology and heart disease management.",
            yearsOfExperience = 15,
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_002",
            name = "Dr. Michael Roberts",
            specialty = Specialty.GENERAL_PRACTICE,
            location = "San Francisco, CA",
            rating = 4.7f,
            reviewCount = 89,
            isAvailableToday = true,
            isBoosted = false,
            bio = "Family medicine physician focused on whole-person health and preventive care.",
            yearsOfExperience = 10,
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_003",
            name = "Dr. Emily Watson",
            specialty = Specialty.DERMATOLOGY,
            location = "Oakland, CA",
            rating = 4.8f,
            reviewCount = 203,
            isAvailableToday = false,
            isBoosted = true,
            bio = "Dermatologist specializing in medical and cosmetic dermatology, including skin cancer screening.",
            yearsOfExperience = 12,
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_004",
            name = "Dr. James Park",
            specialty = Specialty.PEDIATRICS,
            location = "San Jose, CA",
            rating = 4.6f,
            reviewCount = 156,
            isAvailableToday = true,
            isBoosted = false,
            bio = "Pediatrician passionate about child wellness, vaccinations, and family-centered care.",
            yearsOfExperience = 8,
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_005",
            name = "Dr. Priya Sharma",
            specialty = Specialty.NEUROLOGY,
            location = "Palo Alto, CA",
            rating = 4.9f,
            reviewCount = 98,
            isAvailableToday = false,
            isBoosted = false,
            bio = "Neurologist with expertise in migraines, epilepsy, and neurodegenerative disorders.",
            yearsOfExperience = 14,
            acceptingNewPatients = false
        )
    )

    val authStore = FakeAuthStore()

    val patientProfileStore = FakePatientProfileStore()

    val doctorProfileStore = FakeDoctorProfileStore()

    val subscriptionStore = FakeSubscriptionStore()

    val messageStore = FakeMessageStore()

    val boostStore = FakeBoostStore()

    val subscriptionPlans: List<SubscriptionPlan> = SubscriptionPlans.all
}
