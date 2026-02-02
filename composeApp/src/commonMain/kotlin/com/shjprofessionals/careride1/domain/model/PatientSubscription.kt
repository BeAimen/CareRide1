package com.shjprofessionals.careride1.domain.model

/**
 * Represents a patient's subscription record.
 * This is what gets stored in the backend.
 */
data class PatientSubscription(
    val id: String,
    val patientId: String,
    val planId: String,
    val planName: String,
    val priceInCents: Int,
    val billingPeriod: BillingPeriod,
    val status: SubscriptionRecordStatus,
    val createdAt: Long,
    val expiresAt: Long,
    val cancelledAt: Long? = null
) {
    fun getPlan(): SubscriptionPlan? = SubscriptionPlans.getById(planId)

    fun isExpired(): Boolean = expiresAt < SubscriptionStatus.currentTimeMillis()

    fun toStatus(): SubscriptionStatus {
        return when {
            cancelledAt != null -> SubscriptionStatus.Cancelled(this)
            isExpired() -> SubscriptionStatus.Expired(this)
            else -> SubscriptionStatus.Active(this)
        }
    }
}

/**
 * Database record status
 */
enum class SubscriptionRecordStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED
}
