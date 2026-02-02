package com.shjprofessionals.careride1.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Clock

/**
 * Represents a doctor's boost subscription status.
 */
sealed class DoctorBoostStatus {

    data object None : DoctorBoostStatus()

    data class Active(
        val boost: DoctorBoost
    ) : DoctorBoostStatus() {
        val daysRemaining: Int
            get() {
                val now = Clock.System.now().toEpochMilliseconds()
                val remaining = boost.expiresAt - now
                return max(0, (remaining / (24 * 60 * 60 * 1000)).toInt())
            }

        val renewalDateFormatted: String
            get() = formatDate(boost.expiresAt)
    }

    data class Cancelled(
        val boost: DoctorBoost
    ) : DoctorBoostStatus() {
        val activeUntilFormatted: String
            get() = formatDate(boost.expiresAt)

        val isStillActive: Boolean
            get() = boost.expiresAt > Clock.System.now().toEpochMilliseconds()
    }

    data class Expired(
        val lastBoost: DoctorBoost
    ) : DoctorBoostStatus() {
        val expiredDateFormatted: String
            get() = formatDate(lastBoost.expiresAt)
    }

    fun isActive(): Boolean = when (this) {
        is Active -> true
        is Cancelled -> isStillActive
        else -> false
    }

    companion object {
        private fun formatDate(timestamp: Long): String {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val monthName = localDateTime.month.name
                .take(3)
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            return "$monthName ${localDateTime.dayOfMonth}, ${localDateTime.year}"
        }
    }
}

/**
 * Doctor boost subscription record
 */
data class DoctorBoost(
    val id: String,
    val doctorId: String,
    val planId: String,
    val planName: String,
    val priceInCents: Int,
    val billingPeriod: BillingPeriod,
    val boostMultiplier: Float,
    val createdAt: Long,
    val expiresAt: Long,
    val cancelledAt: Long? = null
) {
    fun isExpired(): Boolean = expiresAt < Clock.System.now().toEpochMilliseconds()
}
