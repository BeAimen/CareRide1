package com.careride.data.fakebackend

import com.careride.domain.model.SubscriptionPlans
import com.careride.domain.model.SubscriptionStatus

/**
 * Helper for testing subscription flows.
 * Use this to manually trigger state changes for verification.
 */
object SubscriptionTestHelper {

    private val store = FakeBackend.subscriptionStore

    /**
     * Simulate purchasing a monthly subscription
     */
    fun simulateMonthlyPurchase(): SubscriptionStatus {
        val plan = SubscriptionPlans.MONTHLY
        store.createSubscription(plan)
        return store.getStatus()
    }

    /**
     * Simulate purchasing a yearly subscription
     */
    fun simulateYearlyPurchase(): SubscriptionStatus {
        val plan = SubscriptionPlans.YEARLY
        store.createSubscription(plan)
        return store.getStatus()
    }

    /**
     * Simulate cancelling subscription
     */
    fun simulateCancel(): SubscriptionStatus {
        store.cancelSubscription()
        return store.getStatus()
    }

    /**
     * Simulate reactivating cancelled subscription
     */
    fun simulateReactivate(): SubscriptionStatus {
        store.reactivateSubscription()
        return store.getStatus()
    }

    /**
     * Reset to no subscription
     */
    fun reset() {
        store.clear()
    }

    /**
     * Get current status
     */
    fun getStatus(): SubscriptionStatus = store.getStatus()

    /**
     * Print current state for debugging
     */
    fun printState() {
        val status = store.getStatus()
        println("=== Subscription State ===")
        println("Status: $status")
        println("Can Message: ${status.canMessage()}")
        when (status) {
            is SubscriptionStatus.Active -> {
                println("Plan: ${status.subscription.planName}")
                println("Days Remaining: ${status.daysRemaining}")
                println("Renewal Date: ${status.renewalDateFormatted}")
            }
            is SubscriptionStatus.Cancelled -> {
                println("Active Until: ${status.activeUntilFormatted}")
                println("Still Accessible: ${status.isStillAccessible}")
            }
            is SubscriptionStatus.Expired -> {
                println("Expired On: ${status.expiredDateFormatted}")
            }
            SubscriptionStatus.None -> {
                println("No subscription")
            }
        }
        println("========================")
    }
}