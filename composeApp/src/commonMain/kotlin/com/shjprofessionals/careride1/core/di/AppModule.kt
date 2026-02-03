package com.shjprofessionals.careride1.core.di

import com.shjprofessionals.careride1.data.repository.BoostRepositoryImpl
import com.shjprofessionals.careride1.data.repository.DoctorRepositoryImpl
import com.shjprofessionals.careride1.data.repository.MessageRepositoryImpl
import com.shjprofessionals.careride1.data.repository.SubscriptionRepositoryImpl
import com.shjprofessionals.careride1.domain.repository.BoostRepository
import com.shjprofessionals.careride1.domain.repository.DoctorRepository
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import com.shjprofessionals.careride1.domain.repository.SubscriptionRepository
import com.shjprofessionals.careride1.feature.doctor.boost.DoctorBoostCheckoutViewModel
import com.shjprofessionals.careride1.feature.doctor.boost.DoctorBoostViewModel
import com.shjprofessionals.careride1.feature.doctor.inbox.DoctorChatViewModel
import com.shjprofessionals.careride1.feature.doctor.inbox.DoctorInboxViewModel
import com.shjprofessionals.careride1.feature.doctor.profile.DoctorProfileViewModel
import com.shjprofessionals.careride1.feature.patient.doctorprofile.DoctorProfileViewModel as PatientDoctorProfileViewModel
import com.shjprofessionals.careride1.feature.patient.home.PatientHomeViewModel
import com.shjprofessionals.careride1.feature.patient.messages.PatientChatViewModel
import com.shjprofessionals.careride1.feature.patient.messages.PatientMessagesViewModel
import com.shjprofessionals.careride1.feature.patient.profile.PatientProfileViewModel
import com.shjprofessionals.careride1.feature.patient.subscription.ManageSubscriptionViewModel
import com.shjprofessionals.careride1.feature.patient.subscription.MockCheckoutViewModel
import com.shjprofessionals.careride1.feature.patient.subscription.PaywallViewModel
import org.koin.dsl.module

val appModule = module {
    // ============ Repositories ============
    single<DoctorRepository> { DoctorRepositoryImpl() }
    single<SubscriptionRepository> { SubscriptionRepositoryImpl() }
    single<MessageRepository> { MessageRepositoryImpl() }
    single<BoostRepository> { BoostRepositoryImpl() }

    // ============ Patient ViewModels ============
    factory { PatientHomeViewModel(get()) }
    factory { PatientProfileViewModel(get()) }
    factory { PaywallViewModel(get()) }
    factory { ManageSubscriptionViewModel(get()) }
    factory { PatientMessagesViewModel(get(), get()) }

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
    factory { DoctorProfileViewModel(get()) }  // <-- Added

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