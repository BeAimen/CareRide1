package com.careride.domain.repository

import com.careride.domain.model.BoostAnalytics
import com.careride.domain.model.BoostPlan
import com.careride.domain.model.DoctorBoostStatus
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing doctor boost subscriptions.
 */
interface BoostRepository {

    /**
     * Observe current boost status
     */
    fun observeBoostStatus(): Flow<DoctorBoostStatus>

    /**
     * Get current boost status
     */
    suspend fun getBoostStatus(): DoctorBoostStatus

    /**
     * Get available boost plans
     */
    suspend fun getAvailablePlans(): List<BoostPlan>

    /**
     * Confirm boost subscription after checkout
     */
    suspend fun confirmBoost(planId: String): Result<DoctorBoostStatus>

    /**
     * Cancel boost subscription
     */
    suspend fun cancelBoost(): Result<DoctorBoostStatus>

    /**
     * Reactivate cancelled boost
     */
    suspend fun reactivateBoost(): Result<DoctorBoostStatus>

    /**
     * Get analytics data
     */
    suspend fun getAnalytics(): BoostAnalytics
}