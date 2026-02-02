package com.shjprofessionals.careride1.domain.repository

import com.shjprofessionals.careride1.domain.model.PatientSubscription
import com.shjprofessionals.careride1.domain.model.SubscriptionPlan
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing patient subscriptions.
 * Interface designed for Firebase swap later.
 */
interface SubscriptionRepository {

    // ============ Read Operations ============

    /**
     * Observe current subscription status (reactive)
     */
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>

    /**
     * Get current subscription status (one-shot)
     */
    suspend fun getSubscriptionStatus(): SubscriptionStatus

    /**
     * Get available subscription plans
     */
    suspend fun getAvailablePlans(): List<SubscriptionPlan>

    /**
     * Get subscription history
     */
    suspend fun getSubscriptionHistory(): List<PatientSubscription>

    // ============ Write Operations ============

    /**
     * Confirm subscription after successful checkout.
     * Called when mock checkout completes successfully.
     *
     * @param planId The plan that was purchased
     * @return Result with new subscription status
     */
    suspend fun confirmSubscription(planId: String): Result<SubscriptionStatus>

    /**
     * Cancel current subscription.
     * Subscription remains active until expiry date.
     *
     * @return Result indicating success/failure
     */
    suspend fun cancelSubscription(): Result<SubscriptionStatus>

    /**
     * Restore purchases - re-fetch subscription state from backend.
     * Used when user claims they have an active subscription.
     *
     * @return Result with current subscription status
     */
    suspend fun restorePurchases(): Result<SubscriptionStatus>

    /**
     * Reactivate a cancelled subscription (before expiry).
     *
     * @return Result with new subscription status
     */
    suspend fun reactivateSubscription(): Result<SubscriptionStatus>
}
