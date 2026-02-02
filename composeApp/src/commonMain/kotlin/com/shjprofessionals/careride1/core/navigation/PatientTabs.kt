package com.shjprofessionals.careride1.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.shjprofessionals.careride1.feature.patient.home.PatientHomeTab
import com.shjprofessionals.careride1.feature.patient.messages.PatientMessagesTab
import com.shjprofessionals.careride1.feature.patient.profile.PatientProfileTab

object PatientHomeNavTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Home)
            return remember { TabOptions(index = 0u, title = "Home", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(PatientHomeTab()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object PatientMessagesNavTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.MailOutline)
            return remember { TabOptions(index = 1u, title = "Messages", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(PatientMessagesTab()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object PatientProfileNavTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember { TabOptions(index = 2u, title = "Profile", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(PatientProfileTab()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
