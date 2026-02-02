package com.careride.feature.patient.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.careride.core.designsystem.components.CareRidePrimaryButton
import com.careride.core.designsystem.components.CareRideSecondaryButton
import com.careride.core.designsystem.components.InfoRow
import com.careride.core.designsystem.components.SectionHeader
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.domain.model.SubscriptionStatus

class ManageSubscriptionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<ManageSubscriptionViewModel>()
        val state by viewModel.state.collectAsState()

        ManageSubscriptionContent(
            state = state,
            onBackClick = { navigator.pop() },
            onCancelClick = viewModel::showCancelConfirmation,
            onConfirmCancel = viewModel::confirmCancel,
            onDismissCancel = viewModel::hideCancelConfirmation,
            onReactivateClick = viewModel::showReactivateConfirmation,
            onConfirmReactivate = viewModel::confirmReactivate,
            onDismissReactivate = viewModel::hideReactivateConfirmation,
            onDismissMessage = viewModel::clearMessage,
            onSubscribe = { navigator.push(PaywallScreen()) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManageSubscriptionContent(
    state: ManageSubscriptionState,
    onBackClick: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmCancel: () -> Unit,
    onDismissCancel: () -> Unit,
    onReactivateClick: () -> Unit,
    onConfirmReactivate: () -> Unit,
    onDismissReactivate: () -> Unit,
    onDismissMessage: () -> Unit,
    onSubscribe: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            onDismissMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manage Subscription") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(CareRideTheme.spacing.md)
            ) {
                when (val status = state.status) {
                    is SubscriptionStatus.Active -> {
                        ActiveSubscriptionContent(
                            status = status,
                            onCancelClick = onCancelClick,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    is SubscriptionStatus.Cancelled -> {
                        CancelledSubscriptionContent(
                            status = status,
                            onReactivateClick = onReactivateClick,
                            onSubscribe = onSubscribe,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    is SubscriptionStatus.Expired -> {
                        ExpiredSubscriptionContent(
                            status = status,
                            onSubscribe = onSubscribe,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SubscriptionStatus.None -> {
                        NoSubscriptionContent(
                            onSubscribe = onSubscribe,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    // Cancel confirmation dialog
    if (state.showCancelConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissCancel,
            title = { Text("Cancel Subscription?") },
            text = {
                Text(
                    "Your subscription will remain active until the end of your current billing period. " +
                            "After that, you won't be able to message doctors."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmCancel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Subscription")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCancel) {
                    Text("Keep Subscription")
                }
            }
        )
    }

    // Reactivate confirmation dialog
    if (state.showReactivateConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissReactivate,
            title = { Text("Reactivate Subscription?") },
            text = {
                Text("Your subscription will be reactivated and will continue to renew automatically.")
            },
            confirmButton = {
                TextButton(onClick = onConfirmReactivate) {
                    Text("Reactivate")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissReactivate) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActiveSubscriptionContent(
    status: SubscriptionStatus.Active,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Status badge
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Column {
                    Text(
                        text = "Active Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "You have full messaging access",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        SectionHeader(title = "Subscription Details")

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.Star,
            label = "Plan",
            value = status.subscription.planName
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.DateRange,
            label = "Renews on",
            value = status.renewalDateFormatted
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        InfoRow(
            icon = Icons.Default.Info,
            label = "Days left",
            value = "${status.daysRemaining} days"
        )

        Spacer(modifier = Modifier.weight(1f))

        // Cancel button
        OutlinedButton(
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Cancel Subscription")
        }
    }
}

@Composable
private fun CancelledSubscriptionContent(
    status: SubscriptionStatus.Cancelled,
    onReactivateClick: () -> Unit,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Column {
                    Text(
                        text = "Subscription Cancelled",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    if (status.isStillAccessible) {
                        Text(
                            text = "Access until ${status.activeUntilFormatted}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        if (status.isStillAccessible) {
            Text(
                text = "Your subscription was cancelled but you still have access until your current billing period ends.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            CareRidePrimaryButton(
                text = "Reactivate Subscription",
                onClick = onReactivateClick,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Your subscription has ended. Subscribe again to continue messaging doctors.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.weight(1f))

            CareRidePrimaryButton(
                text = "Subscribe Again",
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ExpiredSubscriptionContent(
    status: SubscriptionStatus.Expired,
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Column {
                    Text(
                        text = "Subscription Expired",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Expired on ${status.expiredDateFormatted}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "Your subscription has expired. Renew to continue messaging doctors.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        CareRidePrimaryButton(
            text = "Renew Subscription",
            onClick = onSubscribe,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun NoSubscriptionContent(
    onSubscribe: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Column {
                    Text(
                        text = "No Active Subscription",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Subscribe to message doctors",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "You can browse doctors for free, but a subscription is required to send messages.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.weight(1f))

        CareRidePrimaryButton(
            text = "View Plans",
            onClick = onSubscribe,
            modifier = Modifier.fillMaxWidth()
        )
    }
}