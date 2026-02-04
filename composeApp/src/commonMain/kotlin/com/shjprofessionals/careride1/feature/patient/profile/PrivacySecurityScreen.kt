package com.shjprofessionals.careride1.feature.patient.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.SectionHeader
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.data.fakebackend.FakeBackend
import com.shjprofessionals.careride1.feature.onboarding.RoleSelectionScreen

class PrivacySecurityScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        var showLogoutDialog by remember { mutableStateOf(false) }
        var showDeleteDialog by remember { mutableStateOf(false) }

        PrivacySecurityContent(
            onBackClick = { navigator.pop() },
            onLogoutClick = { showLogoutDialog = true },
            onDeleteAccountClick = { showDeleteDialog = true }
        )

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out?") },
                text = { Text("Are you sure you want to log out of CareRide?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            FakeBackend.patientProfileStore.logout()
                            // Navigate to role selection and clear back stack
                            navigator.replaceAll(RoleSelectionScreen())
                        }
                    ) {
                        Text("Log Out")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account?") },
                text = {
                    Text(
                        "This action cannot be undone. All your data, conversations, " +
                                "and subscription will be permanently deleted."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            FakeBackend.patientProfileStore.deleteAccount()
                            FakeBackend.subscriptionStore.clear()
                            navigator.replaceAll(RoleSelectionScreen())
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Account")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrivacySecurityContent(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onDeleteAccountClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(CareRideTheme.spacing.md)
        ) {
            SectionHeader(title = "Privacy")

            PrivacyMenuItem(
                icon = Icons.Default.Lock,
                title = "Privacy Policy",
                subtitle = "How we handle your data",
                onClick = { /* Open privacy policy */ }
            )

            PrivacyMenuItem(
                icon = Icons.Default.Info,
                title = "Terms of Service",
                subtitle = "Usage terms and conditions",
                onClick = { /* Open ToS */ }
            )

            PrivacyMenuItem(
                icon = Icons.Default.Share,
                title = "Data Sharing",
                subtitle = "Manage how your data is shared",
                onClick = { /* Open data sharing settings */ }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SectionHeader(title = "Security")

            PrivacyMenuItem(
                icon = Icons.Default.Lock,
                title = "Change Password",
                subtitle = "Update your account password",
                onClick = { /* Open password change */ }
            )

            PrivacyMenuItem(
                icon = Icons.Default.Phone,
                title = "Two-Factor Authentication",
                subtitle = "Add an extra layer of security",
                onClick = { /* Open 2FA settings */ }
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            SectionHeader(title = "Account")

            // Logout
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogoutClick),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(vertical = CareRideTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))
                    Text(
                        text = "Log Out",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Delete account
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onDeleteAccountClick),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier.padding(vertical = CareRideTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))
                    Column {
                        Text(
                            text = "Delete Account",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Permanently delete your account and data",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxl))
        }
    }
}

@Composable
private fun PrivacyMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = CareRideTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}