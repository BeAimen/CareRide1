package com.shjprofessionals.careride1.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import cafe.adriel.voyager.transitions.SlideTransition
import com.shjprofessionals.careride1.feature.doctor.boost.DoctorBoostTab
import com.shjprofessionals.careride1.feature.doctor.inbox.DoctorInboxTab
import com.shjprofessionals.careride1.feature.doctor.profile.DoctorProfileTab

object DoctorInboxNavTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Email)
            return remember { TabOptions(index = 0u, title = "Inbox", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(DoctorInboxTab()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object DoctorBoostNavTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Star)
            return remember { TabOptions(index = 1u, title = "Boost", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(DoctorBoostTab()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

object DoctorProfileNavTab : Tab {
    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Person)
            return remember { TabOptions(index = 2u, title = "Profile", icon = icon) }
        }

    @Composable
    override fun Content() {
        Navigator(DoctorProfileTab()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
