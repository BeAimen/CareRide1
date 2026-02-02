package com.careride.core.di

import com.careride.data.repository.BoostRepositoryImpl
import com.careride.data.repository.DoctorRepositoryImpl
import com.careride.data.repository.MessageRepositoryImpl
import com.careride.data.repository.SubscriptionRepositoryImpl
import com.careride.domain.repository.BoostRepository
import com.careride.domain.repository.DoctorRepository
import com.careride.domain.repository.MessageRepository
import com.careride.domain.repository.SubscriptionRepository
import com.careride.feature.doctor.boost.DoctorBoostCheckoutViewModel
import com.careride.feature.doctor.boost.DoctorBoostViewModel
import com.careride.feature.doctor.inbox.DoctorChatViewModel
import com.careride.feature.doctor.inbox.DoctorInboxViewModel
import com.careride.feature.patient.doctorprofile.DoctorProfileViewModel
import com.careride.feature.patient.home.PatientHomeViewModel
import com.careride.feature.patient.messages.PatientChatViewModel
import com.careride.feature.patient.messages.PatientMessagesViewModel
import com.careride.feature.patient.profile.PatientProfileViewModel
import com.careride.feature.patient.subscription.ManageSubscriptionViewModel
import com.careride.feature.patient.subscription.MockCheckoutViewModel
import com.careride.feature.patient.subscription.PaywallViewModel
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
        DoctorProfileViewModel(
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