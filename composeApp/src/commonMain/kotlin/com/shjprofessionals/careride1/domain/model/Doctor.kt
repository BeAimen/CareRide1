package com.shjprofessionals.careride1.domain.model

data class Doctor(
    val id: String,
    val name: String,
    val specialty: Specialty,
    val imageUrl: String?,
    val location: String,
    val rating: Float,
    val reviewCount: Int,
    val isAvailableToday: Boolean,
    val isBoosted: Boolean,
    val bio: String,
    val yearsOfExperience: Int,
    val languages: List<String> = listOf("English"),
    val acceptingNewPatients: Boolean = true
) {
    fun getRankingReason(searchQuery: String): RankingReason {
        val queryLower = searchQuery.lowercase()
        return RankingReason(
            specialtyMatch = specialty.displayName.lowercase().contains(queryLower) ||
                    specialty.name.lowercase().contains(queryLower),
            locationMatch = location.lowercase().contains(queryLower),
            highAvailability = isAvailableToday,
            highRating = rating >= 4.5f,
            sponsored = isBoosted
        )
    }
}
