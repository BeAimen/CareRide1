package com.shjprofessionals.careride1.core.di

import com.shjprofessionals.careride1.data.repository.AuthRepositoryImpl
import com.shjprofessionals.careride1.data.repository.BoostRepositoryImpl
import com.shjprofessionals.careride1.data.repository.DoctorRepositoryImpl
import com.shjprofessionals.careride1.data.repository.MessageRepositoryImpl
import com.shjprofessionals.careride1.data.repository.SubscriptionRepositoryImpl
import com.shjprofessionals.careride1.domain.repository.AuthRepository
import com.shjprofessionals.careride1.domain.repository.BoostRepository
import com.shjprofessionals.careride1.domain.repository.DoctorRepository
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import com.shjprofessionals.careride1.feature.auth.AuthRoleSelectionViewModel
import com.shjprofessionals.careride1.feature.auth.ForgotPasswordViewModel
import com.shjprofessionals.careride1.feature.auth.SignInViewModel
import com.shjprofessionals.careride1.feature.auth.SignUpViewModel
import com.shjprofessionals.careride1.feature.doctor.boost.DoctorBoostCheckoutViewModel
import com.shjprofessionals.careride1.feature.doctor.boost.DoctorBoostViewModel
import com.shjprofessionals.careride1.feature.doctor.inbox.DoctorChatViewModel
import com.shjprofessionals.careride1.feature.doctor.inbox.DoctorInboxViewModel
import com.shjprofessionals.careride1.feature.doctor.profile.DoctorProfileViewModel
import com.shjprofessionals.careride1.feature.patient.doctorprofile.DoctorProfileViewModel as PatientDoctorProfileViewModel
import com.shjprofessionals.careride1.feature.patient.home.PatientHomeViewModel
import com.shjprofessionals.careride1.feature.patient.messages.PatientChatViewModel
import com.shjprofessionals.careride1.feature.patient.messages.PatientMessagesViewModel
import com.shjprofessionals.careride1.feature.patient.profile.EditBasicInfoViewModel
import com.shjprofessionals.careride1.feature.patient.profile.NotificationSettingsViewModel
import com.shjprofessionals.careride1.feature.patient.profile.PatientProfileViewModel
import com.shjprofessionals.careride1.feature.patient.profile.PersonalInfoViewModel
import com.shjprofessionals.careride1.feature.patient.subscription.ManageSubscriptionViewModel
import com.shjprofessionals.careride1.feature.patient.subscription.MockCheckoutViewModel
import com.shjprofessionals.careride1.feature.patient.subscription.PaywallViewModel
import org.koin.dsl.module

val appModule = module {
    // ============ Repositories ============
    single<AuthRepository> { AuthRepositoryImpl() }
    single<DoctorRepository> { DoctorRepositoryImpl() }
    single<SubscriptionRepository> { SubscriptionRepositoryImpl() }
    single<MessageRepository> { MessageRepositoryImpl() }
    single<BoostRepository> { BoostRepositoryImpl() }

    // ============ Auth ViewModels ============
    factory { SignInViewModel(get()) }
    factory { SignUpViewModel(get()) }
    factory { ForgotPasswordViewModel(get()) }
    factory { AuthRoleSelectionViewModel(get()) }

    // ============ Patient ViewModels ============
    factory { PatientHomeViewModel(get()) }
    factory { PatientProfileViewModel(get(), get()) }
    factory { PaywallViewModel(get()) }
    factory { ManageSubscriptionViewModel(get()) }
    factory { PatientMessagesViewModel(get(), get()) }
    factory { PersonalInfoViewModel(get()) }
    factory { NotificationSettingsViewModel() }

    factory { EditBasicInfoViewModel(get()) }
    factory { EditAddressViewModel() }
    factory { EditMedicalInfoViewModel() }
    factory { EditInsuranceViewModel() }
    factory { EditEmergencyContactViewModel() }

    factory { (doctorId: String) ->
        PatientDoctorProfileViewModel(
            doctorId = doctorId,
            doctorRepository = get(),
            subscriptionRepository = get(),
            messageRepository = get()
        )
    }

    factory { (planId: String) ->
        MockCheckoutViewModel(
            planId = planId,
            subscriptionRepository = get()
        )
    }

    factory { (conversationId: String) ->
        PatientChatViewModel(
            conversationId = conversationId,
            messageRepository = get(),
            subscriptionRepository = get()
        )
    }

    // ============ Doctor ViewModels ============
    factory { DoctorBoostViewModel(get()) }
    factory { DoctorInboxViewModel(get()) }
    factory { DoctorProfileViewModel(get()) }

    factory { (planId: String) ->
        DoctorBoostCheckoutViewModel(
            planId = planId,
            boostRepository = get()
        )
    }

    factory { (conversationId: String) ->
        DoctorChatViewModel(
            conversationId = conversationId,
            messageRepository = get()
        )
    }
}