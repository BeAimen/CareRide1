package com.careride.data.repository

import com.careride.data.fakebackend.FakeBackend
import com.careride.domain.model.BoostAnalytics
import com.careride.domain.model.BoostPlan
import com.careride.domain.model.BoostPlans
import com.careride.domain.model.DoctorBoostStatus
import com.careride.domain.repository.BoostRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow

class BoostRepositoryImpl : BoostRepository {

    private val store = FakeBackend.boostStore

    private suspend fun simulateNetworkDelay() {
        delay(300)
    }

    override fun observeBoostStatus(): Flow<DoctorBoostStatus> {
        return store.statusFlow
    }

    override suspend fun getBoostStatus(): DoctorBoostStatus {
        simulateNetworkDelay()
        return store.getStatus()
    }

    override suspend fun getAvailablePlans(): List<BoostPlan> {
        simulateNetworkDelay()
        return BoostPlans.ALL
    }

    override suspend fun confirmBoost(planId: String): Result<DoctorBoostStatus> {
        simulateNetworkDelay()

        val plan = BoostPlans.getById(planId)
            ?: return Result.failure(IllegalArgumentException("Invalid plan ID: $planId"))

        return try {
            val boost = store.createBoost(plan)
            Result.success(DoctorBoostStatus.Active(boost))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelBoost(): Result<DoctorBoostStatus> {
        simulateNetworkDelay()

        val cancelled = store.cancelBoost()
            ?: return Result.failure(IllegalStateException("No active boost to cancel"))

        return Result.success(DoctorBoostStatus.Cancelled(cancelled))
    }

    override suspend fun reactivateBoost(): Result<DoctorBoostStatus> {
        simulateNetworkDelay()

        val reactivated = store.reactivateBoost()
            ?: return Result.failure(IllegalStateException("Cannot reactivate boost"))

        return Result.success(DoctorBoostStatus.Active(reactivated))
    }

    override suspend fun getAnalytics(): BoostAnalytics {
        simulateNetworkDelay()
        return store.getAnalytics()
    }
}