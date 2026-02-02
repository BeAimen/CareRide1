package com.shjprofessionals.careride1.feature.patient.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.SubscriptionStatus
import com.shjprofessionals.careride1.feature.patient.subscription.ManageSubscriptionScreen
import com.shjprofessionals.careride1.feature.patient.subscription.PaywallScreen

class PatientProfileTab : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<PatientProfileViewModel>()
        val state by viewModel.state.collectAsState()

        PatientProfileContent(
            state = state,
            onSubscriptionClick = {
                if (state.subscriptionStatus.canMessage()) {
                    navigator.push(ManageSubscriptionScreen())
                } else {
                    navigator.push(PaywallScreen())
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientProfileContent(
    state: PatientProfileState,
    onSubscriptionClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.headlineSmall
                    )
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
                .padding(CareRideTheme.spacing.md)
        ) {
            // Subscription card
            SubscriptionStatusCard(
                status = state.subscriptionStatus,
                onClick = onSubscriptionClick
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            // Other profile options (placeholders)
            ProfileMenuItem(
                icon = Icons.Default.Person,
                title = "Personal Information",
                subtitle = "Update your details",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage notification preferences",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Lock,
                title = "Privacy & Security",
                subtitle = "Manage your data",
                onClick = { /* TODO */ }
            )

            ProfileMenuItem(
                icon = Icons.Default.Info,
                title = "About CareRide",
                subtitle = "Version 1.0.0",
                onClick = { /* TODO */ }
            )
        }
    }
}

@Composable
private fun SubscriptionStatusCard(
    status: SubscriptionStatus,
    onClick: () -> Unit
) {
    val (containerColor, iconTint, title, subtitle) = when (status) {
        is SubscriptionStatus.Active -> Tuple4(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primary,
            "Active Subscription",
            "Renews ${status.renewalDateFormatted}"
        )
        is SubscriptionStatus.Cancelled -> Tuple4(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.tertiary,
            "Subscription Cancelled",
            if (status.isStillAccessible) "Active until ${status.activeUntilFormatted}" else "Expired"
        )
        is SubscriptionStatus.Expired -> Tuple4(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            "Subscription Expired",
            "Tap to renew"
        )
        SubscriptionStatus.None -> Tuple4(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "No Subscription",
            "Tap to subscribe"
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
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
                contentDescription = "Manage subscription",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
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

// Helper class for destructuring
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
