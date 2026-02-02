package com.careride.domain.model

/**
 * Analytics data for doctor boost performance.
 * Stub data for demo purposes.
 */
data class BoostAnalytics(
    val profileViews: Int,
    val profileViewsChange: Int, // Change from last period (can be negative)
    val searchAppearances: Int,
    val searchAppearancesChange: Int,
    val messageRequests: Int,
    val messageRequestsChange: Int,
    val averagePosition: Float, // Average position in search results
    val positionChange: Float,
    val period: AnalyticsPeriod = AnalyticsPeriod.LAST_30_DAYS
) {
    val profileViewsChangePercent: String
        get() = formatChange(profileViewsChange, profileViews - profileViewsChange)

    val searchAppearancesChangePercent: String
        get() = formatChange(searchAppearancesChange, searchAppearances - searchAppearancesChange)

    val messageRequestsChangePercent: String
        get() = formatChange(messageRequestsChange, messageRequests - messageRequestsChange)

    private fun formatChange(change: Int, previousValue: Int): String {
        if (previousValue == 0) return if (change > 0) "+100%" else "0%"
        val percent = (change.toFloat() / previousValue * 100).toInt()
        return if (percent >= 0) "+$percent%" else "$percent%"
    }
}

enum class AnalyticsPeriod(val displayName: String) {
    LAST_7_DAYS("Last 7 days"),
    LAST_30_DAYS("Last 30 days"),
    LAST_90_DAYS("Last 90 days")
}