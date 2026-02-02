package com.careride.data.fakebackend

import com.careride.domain.model.PatientSubscription
import com.careride.domain.model.SubscriptionPlan
import com.careride.domain.model.SubscriptionRecordStatus
import com.careride.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Clock

/**
 * In-memory subscription storage for fake backend.
 * Simulates a database/remote storage for patient subscriptions.
 */
class FakeSubscriptionStore {

    // Current patient ID (simulated logged-in user)
    private val currentPatientId = "patient_001"

    // Subscription storage
    private val subscriptions = mutableMapOf<String, PatientSubscription>()

    // Observable status
    private val _statusFlow = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.None)
    val statusFlow: StateFlow<SubscriptionStatus> = _statusFlow.asStateFlow()

    // Subscription history
    private val history = mutableListOf<PatientSubscription>()

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    /**
     * Create a new subscription after checkout
     */
    fun createSubscription(plan: SubscriptionPlan): PatientSubscription {
        val currentTime = now()
        val expiresAt = currentTime + (plan.billingPeriod.months.toLong() * 30 * 24 * 60 * 60 * 1000)

        val subscription = PatientSubscription(
            id = "sub_${currentTime}",
            patientId = currentPatientId,
            planId = plan.id,
            planName = plan.name,
            priceInCents = plan.priceInCents,
            billingPeriod = plan.billingPeriod,
            status = SubscriptionRecordStatus.ACTIVE,
            createdAt = currentTime,
            expiresAt = expiresAt,
            cancelledAt = null
        )

        subscriptions[currentPatientId] = subscription
        history.add(subscription)

        val newStatus = SubscriptionStatus.Active(subscription)
        _statusFlow.value = newStatus

        return subscription
    }

    /**
     * Cancel current subscription
     */
    fun cancelSubscription(): PatientSubscription? {
        val current = subscriptions[currentPatientId] ?: return null

        val cancelled = current.copy(
            status = SubscriptionRecordStatus.CANCELLED,
            cancelledAt = now()
        )

        subscriptions[currentPatientId] = cancelled

        // Update history
        val historyIndex = history.indexOfLast { it.id == cancelled.id }
        if (historyIndex >= 0) {
            history[historyIndex] = cancelled
        }

        _statusFlow.value = SubscriptionStatus.Cancelled(cancelled)

        return cancelled
    }

    /**
     * Reactivate a cancelled subscription
     */
    fun reactivateSubscription(): PatientSubscription? {
        val current = subscriptions[currentPatientId] ?: return null

        if (current.status != SubscriptionRecordStatus.CANCELLED) return null
        if (current.isExpired()) return null

        val reactivated = current.copy(
            status = SubscriptionRecordStatus.ACTIVE,
            cancelledAt = null
        )

        subscriptions[currentPatientId] = reactivated

        val historyIndex = history.indexOfLast { it.id == reactivated.id }
        if (historyIndex >= 0) {
            history[historyIndex] = reactivated
        }

        _statusFlow.value = SubscriptionStatus.Active(reactivated)

        return reactivated
    }

    /**
     * Get current subscription
     */
    fun getCurrentSubscription(): PatientSubscription? {
        return subscriptions[currentPatientId]
    }

    /**
     * Get current status
     */
    fun getStatus(): SubscriptionStatus {
        val subscription = subscriptions[currentPatientId]

        return when {
            subscription == null -> SubscriptionStatus.None
            subscription.cancelledAt != null -> {
                if (subscription.isExpired()) {
                    SubscriptionStatus.Expired(subscription)
                } else {
                    SubscriptionStatus.Cancelled(subscription)
                }
            }
            subscription.isExpired() -> SubscriptionStatus.Expired(subscription)
            else -> SubscriptionStatus.Active(subscription)
        }
    }

    /**
     * Refresh status (simulate re-fetching from server)
     */
    fun refreshStatus(): SubscriptionStatus {
        val status = getStatus()
        _statusFlow.value = status
        return status
    }

    /**
     * Get subscription history
     */
    fun getHistory(): List<PatientSubscription> {
        return history.filter { it.patientId == currentPatientId }
            .sortedByDescending { it.createdAt }
    }

    /**
     * Clear all data (for testing)
     */
    fun clear() {
        subscriptions.clear()
        history.clear()
        _statusFlow.value = SubscriptionStatus.None
    }
}