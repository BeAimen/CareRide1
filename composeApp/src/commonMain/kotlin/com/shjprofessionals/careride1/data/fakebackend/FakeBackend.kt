package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.domain.model.Doctor
import com.shjprofessionals.careride1.domain.model.Specialty
import com.shjprofessionals.careride1.domain.model.SubscriptionPlan
import com.shjprofessionals.careride1.domain.model.SubscriptionPlans

/**
 * In-memory fake backend for development.
 * Will be replaced with Firebase in production.
 */
object FakeBackend {

    // ============ Doctors (must be initialized FIRST) ============
    val doctors: List<Doctor> = listOf(
        Doctor(
            id = "doc_001",
            name = "Dr. Sarah Chen",
            specialty = Specialty.CARDIOLOGY,
            imageUrl = null,
            location = "San Francisco, CA",
            rating = 4.9f,
            reviewCount = 127,
            isAvailableToday = true,
            isBoosted = true,
            bio = "Board-certified cardiologist with 15 years of experience in preventive cardiology and heart disease management.",
            yearsOfExperience = 15,
            languages = listOf("English", "Mandarin"),
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_002",
            name = "Dr. Michael Roberts",
            specialty = Specialty.GENERAL_PRACTICE,
            imageUrl = null,
            location = "San Francisco, CA",
            rating = 4.7f,
            reviewCount = 89,
            isAvailableToday = true,
            isBoosted = false,
            bio = "Family medicine physician focused on whole-person health and preventive care.",
            yearsOfExperience = 10,
            languages = listOf("English", "Spanish"),
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_003",
            name = "Dr. Emily Watson",
            specialty = Specialty.DERMATOLOGY,
            imageUrl = null,
            location = "Oakland, CA",
            rating = 4.8f,
            reviewCount = 203,
            isAvailableToday = false,
            isBoosted = true,
            bio = "Dermatologist specializing in medical and cosmetic dermatology, including skin cancer screening.",
            yearsOfExperience = 12,
            languages = listOf("English"),
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_004",
            name = "Dr. James Park",
            specialty = Specialty.PEDIATRICS,
            imageUrl = null,
            location = "San Jose, CA",
            rating = 4.6f,
            reviewCount = 156,
            isAvailableToday = true,
            isBoosted = false,
            bio = "Pediatrician dedicated to providing compassionate care for children from birth through adolescence.",
            yearsOfExperience = 8,
            languages = listOf("English", "Korean"),
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_005",
            name = "Dr. Lisa Thompson",
            specialty = Specialty.PSYCHIATRY,
            imageUrl = null,
            location = "San Francisco, CA",
            rating = 4.9f,
            reviewCount = 78,
            isAvailableToday = true,
            isBoosted = false,
            bio = "Psychiatrist specializing in anxiety, depression, and ADHD treatment for adults.",
            yearsOfExperience = 14,
            languages = listOf("English"),
            acceptingNewPatients = false
        ),
        Doctor(
            id = "doc_006",
            name = "Dr. Robert Kim",
            specialty = Specialty.ORTHOPEDICS,
            imageUrl = null,
            location = "Palo Alto, CA",
            rating = 4.5f,
            reviewCount = 92,
            isAvailableToday = false,
            isBoosted = false,
            bio = "Orthopedic surgeon with expertise in sports medicine and joint replacement.",
            yearsOfExperience = 18,
            languages = listOf("English", "Korean"),
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_007",
            name = "Dr. Maria Garcia",
            specialty = Specialty.GYNECOLOGY,
            imageUrl = null,
            location = "San Francisco, CA",
            rating = 4.8f,
            reviewCount = 167,
            isAvailableToday = true,
            isBoosted = false,
            bio = "OB-GYN providing comprehensive women's health care with a focus on patient education.",
            yearsOfExperience = 11,
            languages = listOf("English", "Spanish"),
            acceptingNewPatients = true
        ),
        Doctor(
            id = "doc_008",
            name = "Dr. David Lee",
            specialty = Specialty.NEUROLOGY,
            imageUrl = null,
            location = "Berkeley, CA",
            rating = 4.7f,
            reviewCount = 64,
            isAvailableToday = false,
            isBoosted = true,
            bio = "Neurologist specializing in headache disorders, epilepsy, and movement disorders.",
            yearsOfExperience = 16,
            languages = listOf("English", "Cantonese"),
            acceptingNewPatients = true
        )
    )

    // ============ Stores (initialized AFTER doctors using lazy) ============
    val subscriptionStore: FakeSubscriptionStore by lazy { FakeSubscriptionStore() }
    val messageStore: FakeMessageStore by lazy { FakeMessageStore() }
    val boostStore: FakeBoostStore by lazy { FakeBoostStore() }

    // ============ Subscription Plans ============
    fun getAvailablePlans(): List<SubscriptionPlan> = SubscriptionPlans.ALL

    // ============ Doctor Operations ============
    fun searchDoctors(query: String): List<Doctor> {
        if (query.isBlank()) return sortByBoosted(doctors)

        val queryLower = query.lowercase().trim()
        val filtered = doctors.filter { doctor ->
            doctor.name.lowercase().contains(queryLower) ||
                    doctor.specialty.displayName.lowercase().contains(queryLower) ||
                    doctor.specialty.name.lowercase().replace("_", " ").contains(queryLower) ||
                    doctor.location.lowercase().contains(queryLower)
        }
        return sortByBoosted(filtered)
    }

    fun getDoctorById(id: String): Doctor? {
        return doctors.find { it.id == id }
    }

    fun getDoctorsBySpecialty(specialty: Specialty): List<Doctor> {
        return sortByBoosted(doctors.filter { it.specialty == specialty })
    }

    private fun sortByBoosted(list: List<Doctor>): List<Doctor> {
        return list.sortedByDescending { it.isBoosted }
    }
}
