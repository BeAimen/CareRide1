package com.shjprofessionals.careride1.domain.model

data class RankingReason(
    val specialtyMatch: Boolean = false,
    val locationMatch: Boolean = false,
    val highAvailability: Boolean = false,
    val highRating: Boolean = false,
    val sponsored: Boolean = false
) {
    fun toDisplayList(): List<String> {
        return buildList {
            if (sponsored) add("Sponsored placement")
            if (specialtyMatch) add("Matches your search specialty")
            if (locationMatch) add("Near your location")
            if (highAvailability) add("High availability this week")
            if (highRating) add("Highly rated by patients")
        }
    }
}
