package com.shjprofessionals.careride1

import androidx.compose.ui.window.ComposeUIViewController
import com.shjprofessionals.careride1.App
import com.shjprofessionals.careride1.core.di.appModule
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        startKoin {
            modules(appModule)
        }
    }
) {
    App()
}
