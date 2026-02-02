package com.shjprofessionals.careride1.data.repository

import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.PatientSubscription
import com.shjprofessionals.careride1.domain.model.SubscriptionPlan
import com.shjprofessionals.careride1.domain.model.SubscriptionPlans
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of SubscriptionRepository using FakeBackend.
 * Ready to be swapped with Firebase implementation.
 */
class SubscriptionRepositoryImpl : SubscriptionRepository {

    private val store = FakeBackend.subscriptionStore

    // Simulate network delay
    private suspend fun simulateNetworkDelay() {
        delay(300)
    }

    override fun observeSubscriptionStatus(): Flow<SubscriptionStatus> {
        return store.statusFlow
    }

    override suspend fun getSubscriptionStatus(): SubscriptionStatus {
        simulateNetworkDelay()
        return store.getStatus()
    }

    override suspend fun getAvailablePlans(): List<SubscriptionPlan> {
        simulateNetworkDelay()
        return FakeBackend.getAvailablePlans()
    }

    override suspend fun getSubscriptionHistory(): List<PatientSubscription> {
        simulateNetworkDelay()
        return store.getHistory()
    }

    override suspend fun confirmSubscription(planId: String): Result<SubscriptionStatus> {
        simulateNetworkDelay()

        val plan = SubscriptionPlans.getById(planId)
            ?: return Result.failure(IllegalArgumentException("Invalid plan ID: $planId"))

        return try {
            val subscription = store.createSubscription(plan)
            Result.success(SubscriptionStatus.Active(subscription))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelSubscription(): Result<SubscriptionStatus> {
        simulateNetworkDelay()

        val cancelled = store.cancelSubscription()
            ?: return Result.failure(IllegalStateException("No active subscription to cancel"))

        return Result.success(SubscriptionStatus.Cancelled(cancelled))
    }

    override suspend fun restorePurchases(): Result<SubscriptionStatus> {
        simulateNetworkDelay()

        // Simulate checking with "server" for existing purchases
        val status = store.refreshStatus()
        return Result.success(status)
    }

    override suspend fun reactivateSubscription(): Result<SubscriptionStatus> {
        simulateNetworkDelay()

        val reactivated = store.reactivateSubscription()
            ?: return Result.failure(IllegalStateException("Cannot reactivate subscription"))

        return Result.success(SubscriptionStatus.Active(reactivated))
    }
}
