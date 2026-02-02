package com.careride.domain.repository

import com.careride.domain.model.Doctor
import com.careride.domain.model.Specialty
import kotlinx.coroutines.flow.Flow

interface DoctorRepository {
    /**
     * Get all doctors, with boosted doctors sorted to top
     */
    fun getAllDoctors(): Flow<List<Doctor>>

    /**
     * Search doctors by query (matches name, specialty, location)
     * Boosted doctors matching the query appear first
     */
    fun searchDoctors(query: String): Flow<List<Doctor>>

    /**
     * Get doctors by specialty
     */
    fun getDoctorsBySpecialty(specialty: Specialty): Flow<List<Doctor>>

    /**
     * Get a single doctor by ID
     */
    suspend fun getDoctorById(id: String): Doctor?
}