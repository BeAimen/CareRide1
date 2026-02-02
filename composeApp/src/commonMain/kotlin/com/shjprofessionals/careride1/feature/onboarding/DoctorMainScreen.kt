package com.shjprofessionals.careride1.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.shjprofessionals.careride1.core.navigation.DoctorBoostNavTab
import com.shjprofessionals.careride1.core.navigation.DoctorInboxNavTab
import com.shjprofessionals.careride1.core.navigation.DoctorProfileNavTab

class DoctorMainScreen : Screen {

    @Composable
    override fun Content() {
        TabNavigator(DoctorInboxNavTab) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        TabNavigationItem(DoctorInboxNavTab)
                        TabNavigationItem(DoctorBoostNavTab)
                        TabNavigationItem(DoctorProfileNavTab)
                    }
                }
            ) { paddingValues ->
                Box(modifier = Modifier.padding(paddingValues)) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current

    NavigationBarItem(
        selected = tabNavigator.current.key == tab.key,
        onClick = { tabNavigator.current = tab },
        icon = {
            tab.options.icon?.let { painter ->
                Icon(painter = painter, contentDescription = tab.options.title)
            }
        },
        label = { Text(tab.options.title) }
    )
}
