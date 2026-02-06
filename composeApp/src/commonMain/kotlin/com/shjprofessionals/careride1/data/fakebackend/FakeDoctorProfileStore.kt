package com.shjprofessionals.careride1.data.fakebackend

import com.shjprofessionals.careride1.core.util.BioGenerator
import com.shjprofessionals.careride1.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeDoctorProfileStore {

    private val _profile = MutableStateFlow<DoctorProfile?>(null)
    val profileFlow: StateFlow<DoctorProfile?> = _profile.asStateFlow()

    fun syncWithAuthUser(user: User) {
        val currentProfile = _profile.value

        if (currentProfile == null) {
            val defaultSpecialty = Specialty.GENERAL_PRACTICE
            val newProfile = DoctorProfile(
                id = user.id,
                name = user.name,
                email = user.email,
                specialty = defaultSpecialty,
                bio = "",
                areasOfExpertise = BioGenerator.suggestExpertise(defaultSpecialty).take(5),
                conditionsTreated = BioGenerator.suggestConditions(defaultSpecialty).take(5)
            )
            _profile.value = newProfile
        } else {
            _profile.value = currentProfile.copy(
                id = user.id,
                name = user.name,
                email = user.email
            )
        }
    }

    fun getProfile(): DoctorProfile? = _profile.value

    fun getCurrentDoctor(): Doctor? = _profile.value?.toDoctor()

    fun getCurrentDoctorId(): String? = _profile.value?.id

    fun updateBasicInfo(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        dateOfBirth: String? = null,
        gender: Gender? = null,
        title: DoctorTitle? = null
    ): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            name = name ?: current.name,
            email = email ?: current.email,
            phone = phone ?: current.phone,
            dateOfBirth = dateOfBirth ?: current.dateOfBirth,
            gender = gender ?: current.gender,
            title = title ?: current.title
        )
        _profile.value = updated
        return updated
    }

    fun updateProfessionalInfo(
        specialty: Specialty? = null,
        subSpecialties: List<String>? = null,
        licenseNumber: String? = null,
        licenseState: String? = null,
        npiNumber: String? = null,
        yearsOfExperience: Int? = null
    ): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            specialty = specialty ?: current.specialty,
            subSpecialties = subSpecialties ?: current.subSpecialties,
            licenseNumber = licenseNumber ?: current.licenseNumber,
            licenseState = licenseState ?: current.licenseState,
            npiNumber = npiNumber ?: current.npiNumber,
            yearsOfExperience = yearsOfExperience ?: current.yearsOfExperience
        )
        _profile.value = updated
        return updated
    }

    fun updateEducation(education: List<Education>): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(education = education)
        _profile.value = updated
        return updated
    }

    fun addEducation(education: Education): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(education = current.education + education)
        _profile.value = updated
        return updated
    }

    fun removeEducation(index: Int): DoctorProfile? {
        val current = _profile.value ?: return null
        if (index < 0 || index >= current.education.size) return null
        val updated = current.copy(education = current.education.filterIndexed { i, _ -> i != index })
        _profile.value = updated
        return updated
    }

    fun updateCredentials(
        boardCertifications: List<String>? = null,
        hospitalAffiliations: List<String>? = null,
        awards: List<String>? = null
    ): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            boardCertifications = boardCertifications ?: current.boardCertifications,
            hospitalAffiliations = hospitalAffiliations ?: current.hospitalAffiliations,
            awards = awards ?: current.awards
        )
        _profile.value = updated
        return updated
    }

    fun updatePracticeInfo(
        practiceName: String? = null,
        practiceAddress: Address? = null,
        officePhone: String? = null,
        officeHours: List<OfficeHours>? = null,
        consultationFeeMin: Int? = null,
        consultationFeeMax: Int? = null,
        acceptedInsurance: List<String>? = null
    ): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            practiceName = practiceName ?: current.practiceName,
            practiceAddress = practiceAddress ?: current.practiceAddress,
            officePhone = officePhone ?: current.officePhone,
            officeHours = officeHours ?: current.officeHours,
            consultationFeeMin = consultationFeeMin ?: current.consultationFeeMin,
            consultationFeeMax = consultationFeeMax ?: current.consultationFeeMax,
            acceptedInsurance = acceptedInsurance ?: current.acceptedInsurance
        )
        _profile.value = updated
        return updated
    }

    fun toggleAvailability(): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(isAvailableToday = !current.isAvailableToday)
        _profile.value = updated
        return updated
    }

    fun toggleAcceptingNewPatients(): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(acceptingNewPatients = !current.acceptingNewPatients)
        _profile.value = updated
        return updated
    }

    fun updateBio(bio: String): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(bio = bio)
        _profile.value = updated
        return updated
    }

    fun updateTreatmentPhilosophy(philosophy: String): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(treatmentPhilosophy = philosophy)
        _profile.value = updated
        return updated
    }

    fun updateExpertise(
        areasOfExpertise: List<String>? = null,
        proceduresOffered: List<String>? = null,
        conditionsTreated: List<String>? = null
    ): DoctorProfile? {
        val current = _profile.value ?: return null
        val updated = current.copy(
            areasOfExpertise = areasOfExpertise ?: current.areasOfExpertise,
            proceduresOffered = proceduresOffered ?: current.proceduresOffered,
            conditionsTreated = conditionsTreated ?: current.conditionsTreated
        )
        _profile.value = updated
        return updated
    }

    fun generateBio(): String {
        val current = _profile.value ?: return ""
        return BioGenerator.generateBio(current)
    }

    fun generatePhilosophy(): String {
        val current = _profile.value ?: return ""
        return BioGenerator.generatePhilosophy(current.specialty)
    }

    fun getSuggestedExpertise(): List<String> {
        val current = _profile.value ?: return emptyList()
        return BioGenerator.suggestExpertise(current.specialty)
    }

    fun getSuggestedConditions(): List<String> {
        val current = _profile.value ?: return emptyList()
        return BioGenerator.suggestConditions(current.specialty)
    }

    fun clear() {
        _profile.value = null
    }

    fun updateProfile(
        bio: String? = null,
        location: String? = null,
        yearsOfExperience: Int? = null
    ): DoctorProfile? {
        val current = _profile.value ?: return null

        val newAddress = if (location != null) {
            current.practiceAddress.copy(
                city = location.split(",").firstOrNull()?.trim() ?: location,
                state = location.split(",").getOrNull(1)?.trim() ?: ""
            )
        } else {
            current.practiceAddress
        }

        val updated = current.copy(
            bio = bio ?: current.bio,
            practiceAddress = newAddress,
            yearsOfExperience = yearsOfExperience ?: current.yearsOfExperience
        )
        _profile.value = updated
        return updated
    }
}
