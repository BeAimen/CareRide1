package com.shjprofessionals.careride1

import androidx.compose.ui.window.ComposeUIViewController
import com.careride.App
import com.careride.core.di.appModule
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