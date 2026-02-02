package com.careride.domain.model

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.max
import kotlin.time.Clock

/**
 * Represents the patient's subscription status.
 * Sealed class enables exhaustive when statements for state handling.
 */
sealed class SubscriptionStatus {

    /**
     * No subscription exists
     */
    data object None : SubscriptionStatus()

    /**
     * Active subscription - patient can message doctors
     */
    data class Active(
        val subscription: PatientSubscription
    ) : SubscriptionStatus() {
        val daysRemaining: Int
            get() {
                val now = currentTimeMillis()
                val remaining = subscription.expiresAt - now
                return max(0, (remaining / (24 * 60 * 60 * 1000)).toInt())
            }

        val renewalDateFormatted: String
            get() = formatDate(subscription.expiresAt)
    }

    /**
     * Subscription expired - needs renewal
     */
    data class Expired(
        val lastSubscription: PatientSubscription
    ) : SubscriptionStatus() {
        val expiredDateFormatted: String
            get() = formatDate(lastSubscription.expiresAt)
    }

    /**
     * Subscription was cancelled but still active until expiry
     */
    data class Cancelled(
        val subscription: PatientSubscription
    ) : SubscriptionStatus() {
        val activeUntilFormatted: String
            get() = formatDate(subscription.expiresAt)

        val isStillAccessible: Boolean
            get() = subscription.expiresAt > currentTimeMillis()
    }

    /**
     * Whether the user can currently message doctors
     */
    fun canMessage(): Boolean = when (this) {
        is Active -> true
        is Cancelled -> isStillAccessible
        is Expired, None -> false
    }

    /**
     * Whether the user has ever subscribed
     */
    fun hasSubscribed(): Boolean = this !is None

    companion object {
        internal fun currentTimeMillis(): Long =
            Clock.System.now().toEpochMilliseconds()

        internal fun formatDate(timestamp: Long): String {
            val instant = Instant.fromEpochMilliseconds(timestamp)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val monthName = localDateTime.month.name
                .take(3)
                .lowercase()
                .replaceFirstChar { it.uppercase() }
            return "$monthName ${localDateTime.day}, ${localDateTime.year}"
        }
    }
}