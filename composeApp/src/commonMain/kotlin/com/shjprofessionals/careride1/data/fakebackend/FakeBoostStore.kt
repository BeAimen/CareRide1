package com.careride.data.fakebackend

import com.careride.domain.model.AnalyticsPeriod
import com.careride.domain.model.BillingPeriod
import com.careride.domain.model.BoostAnalytics
import com.careride.domain.model.BoostPlan
import com.careride.domain.model.DoctorBoost
import com.careride.domain.model.DoctorBoostStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import kotlin.time.Clock

/**
 * In-memory boost storage for fake backend.
 */
class FakeBoostStore {

    // Current doctor ID (simulated logged-in doctor)
    private val currentDoctorId = "doc_001" // Dr. Sarah Chen (already boosted in seed data)

    private val boosts = mutableMapOf<String, DoctorBoost>()

    private val _statusFlow = MutableStateFlow<DoctorBoostStatus>(DoctorBoostStatus.None)
    val statusFlow: StateFlow<DoctorBoostStatus> = _statusFlow.asStateFlow()

    private fun now(): Long = Clock.System.now().toEpochMilliseconds()

    init {
        // Seed with existing boost for demo
        seedExistingBoost()
    }

    private fun seedExistingBoost() {
        // Dr. Sarah Chen already has a boost (matches isBoosted = true in doctor data)
        val existingBoost = DoctorBoost(
            id = "boost_existing_001",
            doctorId = currentDoctorId,
            planId = "boost_pro",
            planName = "Pro Boost",
            priceInCents = 9999,
            billingPeriod = BillingPeriod.MONTHLY,
            boostMultiplier = 3.0f,
            createdAt = now() - (15 * 24 * 60 * 60 * 1000), // 15 days ago
            expiresAt = now() + (15 * 24 * 60 * 60 * 1000), // 15 days from now
            cancelledAt = null
        )

        boosts[currentDoctorId] = existingBoost
        _statusFlow.value = DoctorBoostStatus.Active(existingBoost)
    }

    fun createBoost(plan: BoostPlan): DoctorBoost {
        val currentTime = now()
        val expiresAt = currentTime + (plan.billingPeriod.months.toLong() * 30 * 24 * 60 * 60 * 1000)

        val boost = DoctorBoost(
            id = "boost_${currentTime}",
            doctorId = currentDoctorId,
            planId = plan.id,
            planName = plan.name,
            priceInCents = plan.priceInCents,
            billingPeriod = plan.billingPeriod,
            boostMultiplier = plan.boostMultiplier,
            createdAt = currentTime,
            expiresAt = expiresAt,
            cancelledAt = null
        )

        boosts[currentDoctorId] = boost
        _statusFlow.value = DoctorBoostStatus.Active(boost)

        return boost
    }

    fun cancelBoost(): DoctorBoost? {
        val current = boosts[currentDoctorId] ?: return null

        val cancelled = current.copy(cancelledAt = now())
        boosts[currentDoctorId] = cancelled
        _statusFlow.value = DoctorBoostStatus.Cancelled(cancelled)

        return cancelled
    }

    fun reactivateBoost(): DoctorBoost? {
        val current = boosts[currentDoctorId] ?: return null
        if (current.cancelledAt == null) return null
        if (current.isExpired()) return null

        val reactivated = current.copy(cancelledAt = null)
        boosts[currentDoctorId] = reactivated
        _statusFlow.value = DoctorBoostStatus.Active(reactivated)

        return reactivated
    }

    fun getStatus(): DoctorBoostStatus {
        val boost = boosts[currentDoctorId]

        return when {
            boost == null -> DoctorBoostStatus.None
            boost.cancelledAt != null -> {
                if (boost.isExpired()) {
                    DoctorBoostStatus.Expired(boost)
                } else {
                    DoctorBoostStatus.Cancelled(boost)
                }
            }
            boost.isExpired() -> DoctorBoostStatus.Expired(boost)
            else -> DoctorBoostStatus.Active(boost)
        }
    }

    fun refreshStatus(): DoctorBoostStatus {
        val status = getStatus()
        _statusFlow.value = status
        return status
    }

    /**
     * Generate stub analytics data
     */
    fun getAnalytics(): BoostAnalytics {
        val hasBoost = getStatus().isActive()

        // Generate realistic-looking fake data
        val baseViews = if (hasBoost) 150 else 45
        val baseSearches = if (hasBoost) 320 else 95
        val baseMessages = if (hasBoost) 12 else 3
        val basePosition = if (hasBoost) 2.3f else 8.5f

        return BoostAnalytics(
            profileViews = baseViews + Random.nextInt(-20, 30),
            profileViewsChange = if (hasBoost) Random.nextInt(20, 50) else Random.nextInt(-5, 10),
            searchAppearances = baseSearches + Random.nextInt(-30, 50),
            searchAppearancesChange = if (hasBoost) Random.nextInt(50, 120) else Random.nextInt(-10, 20),
            messageRequests = baseMessages + Random.nextInt(-2, 5),
            messageRequestsChange = if (hasBoost) Random.nextInt(3, 8) else Random.nextInt(-1, 2),
            averagePosition = basePosition + Random.nextFloat() * 2 - 1,
            positionChange = if (hasBoost) -(Random.nextFloat() * 3 + 1) else Random.nextFloat() * 2,
            period = AnalyticsPeriod.LAST_30_DAYS
        )
    }

    fun clear() {
        boosts.clear()
        _statusFlow.value = DoctorBoostStatus.None
    }
}