package com.shjprofessionals.careride1.feature.patient.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.domain.model.NotificationSettings
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotificationSettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<NotificationSettingsViewModel>()
        val state by viewModel.state.collectAsState()

        NotificationSettingsContent(
            state = state,
            onBackClick = { navigator.pop() },
            onPushToggle = viewModel::togglePush,
            onEmailToggle = viewModel::toggleEmail,
            onSmsToggle = viewModel::toggleSms,
            onAppointmentRemindersToggle = viewModel::toggleAppointmentReminders,
            onMessageNotificationsToggle = viewModel::toggleMessageNotifications,
            onPromotionalToggle = viewModel::togglePromotional
        )
    }
}

data class NotificationSettingsState(
    val settings: NotificationSettings = NotificationSettings(),
    val isLoading: Boolean = true
)

class NotificationSettingsViewModel : ScreenModel {
    private val store = FakeBackend.patientProfileStore

    private val _state = MutableStateFlow(NotificationSettingsState())
    val state: StateFlow<NotificationSettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        screenModelScope.launch {
            store.notificationSettings.collect { settings ->
                _state.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    fun togglePush(enabled: Boolean) {
        store.updateSingleNotificationSetting(pushEnabled = enabled)
    }

    fun toggleEmail(enabled: Boolean) {
        store.updateSingleNotificationSetting(emailEnabled = enabled)
    }

    fun toggleSms(enabled: Boolean) {
        store.updateSingleNotificationSetting(smsEnabled = enabled)
    }

    fun toggleAppointmentReminders(enabled: Boolean) {
        store.updateSingleNotificationSetting(appointmentReminders = enabled)
    }

    fun toggleMessageNotifications(enabled: Boolean) {
        store.updateSingleNotificationSetting(messageNotifications = enabled)
    }

    fun togglePromotional(enabled: Boolean) {
        store.updateSingleNotificationSetting(promotionalEmails = enabled)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationSettingsContent(
    state: NotificationSettingsState,
    onBackClick: () -> Unit,
    onPushToggle: (Boolean) -> Unit,
    onEmailToggle: (Boolean) -> Unit,
    onSmsToggle: (Boolean) -> Unit,
    onAppointmentRemindersToggle: (Boolean) -> Unit,
    onMessageNotificationsToggle: (Boolean) -> Unit,
    onPromotionalToggle: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(CareRideTheme.spacing.md)
            ) {
                SectionHeader(title = "Notification Channels")

                NotificationToggleItem(
                    icon = Icons.Default.Notifications,
                    title = "Push Notifications",
                    subtitle = "Receive notifications on your device",
                    checked = state.settings.pushEnabled,
                    onToggle = onPushToggle
                )

                NotificationToggleItem(
                    icon = Icons.Default.Email,
                    title = "Email Notifications",
                    subtitle = "Receive updates via email",
                    checked = state.settings.emailEnabled,
                    onToggle = onEmailToggle
                )

                NotificationToggleItem(
                    icon = Icons.Default.Phone,
                    title = "SMS Notifications",
                    subtitle = "Receive text message alerts",
                    checked = state.settings.smsEnabled,
                    onToggle = onSmsToggle
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                SectionHeader(title = "Notification Types")

                NotificationToggleItem(
                    icon = Icons.Default.DateRange,
                    title = "Appointment Reminders",
                    subtitle = "Get reminded about upcoming appointments",
                    checked = state.settings.appointmentReminders,
                    onToggle = onAppointmentRemindersToggle
                )

                NotificationToggleItem(
                    icon = Icons.Default.MailOutline,
                    title = "Message Notifications",
                    subtitle = "Get notified when doctors reply",
                    checked = state.settings.messageNotifications,
                    onToggle = onMessageNotificationsToggle
                )

                NotificationToggleItem(
                    icon = Icons.Default.Star,
                    title = "Promotional Emails",
                    subtitle = "Receive offers and health tips",
                    checked = state.settings.promotionalEmails,
                    onToggle = onPromotionalToggle
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                // Info card
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier.padding(CareRideTheme.spacing.md),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(
                            text = "You can change these settings at any time. Critical account and security notifications will always be sent.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = CareRideTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onToggle
        )
    }
}