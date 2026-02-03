package com.shjprofessionals.careride1.feature.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.shjprofessionals.careride1.core.navigation.DoctorBoostNavTab
import com.shjprofessionals.careride1.core.navigation.DoctorInboxNavTab
import com.shjprofessionals.careride1.core.navigation.DoctorProfileNavTab
import com.shjprofessionals.careride1.domain.repository.MessageRepository
import org.koin.compose.koinInject

class DoctorMainScreen : Screen {

    @Composable
    override fun Content() {
        val messageRepository: MessageRepository = koinInject()

        // Observe unread count for doctor
        val unreadCount by messageRepository.getDoctorUnreadCount()
            .collectAsState(initial = 0)

        TabNavigator(DoctorInboxNavTab) {
            Scaffold(
                bottomBar = {
                    NavigationBar {
                        DoctorTabNavigationItem(
                            tab = DoctorInboxNavTab,
                            badgeCount = unreadCount
                        )
                        DoctorTabNavigationItem(
                            tab = DoctorBoostNavTab,
                            badgeCount = 0
                        )
                        DoctorTabNavigationItem(
                            tab = DoctorProfileNavTab,
                            badgeCount = 0
                        )
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
private fun RowScope.DoctorTabNavigationItem(
    tab: Tab,
    badgeCount: Int = 0
) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current.key == tab.key

    val accessibilityLabel = buildString {
        append(tab.options.title)
        if (badgeCount > 0) {
            append(", $badgeCount unread")
        }
        if (isSelected) {
            append(", selected")
        }
    }

    NavigationBarItem(
        selected = isSelected,
        onClick = { tabNavigator.current = tab },
        icon = {
            if (badgeCount > 0) {
                BadgedBox(
                    badge = {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(
                                text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                ) {
                    tab.options.icon?.let { painter ->
                        Icon(
                            painter = painter,
                            contentDescription = null
                        )
                    }
                }
            } else {
                tab.options.icon?.let { painter ->
                    Icon(
                        painter = painter,
                        contentDescription = null
                    )
                }
            }
        },
        label = { Text(tab.options.title) },
        modifier = Modifier.semantics {
            contentDescription = accessibilityLabel
        }
    )
}