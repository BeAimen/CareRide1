package com.shjprofessionals.careride1.domain.repository

import com.shjprofessionals.careride1.domain.model.BoostAnalytics
import com.shjprofessionals.careride1.domain.model.BoostPlan
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus
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
