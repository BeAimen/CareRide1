package com.careride.feature.doctor.boost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.careride.core.designsystem.components.*
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.domain.model.DoctorBoostStatus

class DoctorBoostTab : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<DoctorBoostViewModel>()
        val state by viewModel.state.collectAsState()

        DoctorBoostContent(
            state = state,
            onPlanSelect = viewModel::selectPlan,
            onContinue = {
                state.selectedPlan?.let { plan ->
                    navigator.push(DoctorBoostCheckoutScreen(planId = plan.id))
                }
            },
            onCancelClick = viewModel::showCancelDialog,
            onConfirmCancel = viewModel::confirmCancel,
            onDismissCancel = viewModel::dismissCancelDialog,
            onReactivateClick = viewModel::showReactivateDialog,
            onConfirmReactivate = viewModel::confirmReactivate,
            onDismissReactivate = viewModel::dismissReactivateDialog,
            onDismissMessage = viewModel::clearMessage
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DoctorBoostContent(
    state: DoctorBoostState,
    onPlanSelect: (com.careride.domain.model.BoostPlan) -> Unit,
    onContinue: () -> Unit,
    onCancelClick: () -> Unit,
    onConfirmCancel: () -> Unit,
    onDismissCancel: () -> Unit,
    onReactivateClick: () -> Unit,
    onConfirmReactivate: () -> Unit,
    onDismissReactivate: () -> Unit,
    onDismissMessage: () -> Unit
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
                title = {
                    Text(
                        text = "Boost Your Profile",
                        style = MaterialTheme.typography.headlineSmall
                    )
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
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(CareRideTheme.spacing.md)
                ) {
                    // Status banner (if active or cancelled)
                    BoostStatusBanner(
                        status = state.status,
                        onManageClick = {
                            when (state.status) {
                                is DoctorBoostStatus.Cancelled -> onReactivateClick()
                                else -> onCancelClick()
                            }
                        }
                    )

                    if (state.status.isActive()) {
                        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))
                    }

                    // Analytics section (if has/had boost)
                    if (state.analytics != null && state.status !is DoctorBoostStatus.None) {
                        AnalyticsSection(analytics = state.analytics)

                        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))
                    }

                    // Plans section
                    if (!state.status.isActive() || state.status is DoctorBoostStatus.Cancelled) {
                        PlansSection(
                            plans = state.plans,
                            selectedPlan = state.selectedPlan,
                            onPlanSelect = onPlanSelect,
                            showUpgrade = state.status.isActive()
                        )
                    } else {
                        // Active boost - show current plan details
                        CurrentPlanSection(status = state.status as DoctorBoostStatus.Active)
                    }
                }

                // Bottom CTA
                if (!state.status.isActive()) {
                    Surface(
                        shadowElevation = CareRideTheme.elevation.lg,
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CareRideTheme.spacing.md)
                        ) {
                            state.selectedPlan?.let { plan ->
                                Text(
                                    text = "Total: ${plan.displayPrice} ${plan.billingDescription}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )

                                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                            }

                            CareRidePrimaryButton(
                                text = "Continue to Checkout",
                                onClick = onContinue,
                                enabled = state.selectedPlan != null && !state.isProcessing,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                } else if (state.status is DoctorBoostStatus.Active) {
                    // Cancel button for active boost
                    Surface(
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(CareRideTheme.spacing.md)
                        ) {
                            OutlinedButton(
                                onClick = onCancelClick,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Cancel Boost")
                            }
                        }
                    }
                }
            }
        }
    }

    // Cancel dialog
    if (state.showCancelDialog) {
        AlertDialog(
            onDismissRequest = onDismissCancel,
            title = { Text("Cancel Boost?") },
            text = {
                Text(
                    "Your boost will remain active until the end of your billing period. " +
                            "After that, your profile will no longer be featured."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmCancel,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Boost")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissCancel) {
                    Text("Keep Boost")
                }
            }
        )
    }

    // Reactivate dialog
    if (state.showReactivateDialog) {
        AlertDialog(
            onDismissRequest = onDismissReactivate,
            title = { Text("Reactivate Boost?") },
            text = {
                Text("Your boost will be reactivated and continue to renew automatically.")
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
private fun AnalyticsSection(analytics: com.careride.domain.model.BoostAnalytics) {
    SectionHeader(title = "Performance (${analytics.period.displayName})")

    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
    ) {
        AnalyticsCard(
            title = "Profile Views",
            value = analytics.profileViews.toString(),
            change = analytics.profileViewsChangePercent,
            isPositiveChange = analytics.profileViewsChange >= 0,
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )

        AnalyticsCard(
            title = "Search Hits",
            value = analytics.searchAppearances.toString(),
            change = analytics.searchAppearancesChangePercent,
            isPositiveChange = analytics.searchAppearancesChange >= 0,
            icon = Icons.Default.Search,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
    ) {
        AnalyticsCard(
            title = "Messages",
            value = analytics.messageRequests.toString(),
            change = analytics.messageRequestsChangePercent,
            isPositiveChange = analytics.messageRequestsChange >= 0,
            icon = Icons.Default.Email,
            modifier = Modifier.weight(1f)
        )

        AnalyticsCard(
            title = "Avg Position",
            value = "#${analytics.averagePosition.toInt()}",
            change = if (analytics.positionChange < 0) {
                "+${(-analytics.positionChange).toInt()}"
            } else {
                "-${analytics.positionChange.toInt()}"
            },
            isPositiveChange = analytics.positionChange < 0, // Lower position is better
            icon = Icons.Default.Star,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PlansSection(
    plans: List<com.careride.domain.model.BoostPlan>,
    selectedPlan: com.careride.domain.model.BoostPlan?,
    onPlanSelect: (com.careride.domain.model.BoostPlan) -> Unit,
    showUpgrade: Boolean
) {
    SectionHeader(
        title = if (showUpgrade) "Upgrade Your Plan" else "Choose a Boost Plan"
    )

    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

    plans.forEach { plan ->
        BoostPlanCard(
            plan = plan,
            isSelected = selectedPlan?.id == plan.id,
            onSelect = { onPlanSelect(plan) }
        )
        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
    }
}

@Composable
private fun CurrentPlanSection(status: DoctorBoostStatus.Active) {
    SectionHeader(title = "Current Plan")

    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(CareRideTheme.spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = status.boost.planName,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${status.boost.boostMultiplier}x boost",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            InfoRow(
                icon = Icons.Default.DateRange,
                label = "Renews",
                value = status.renewalDateFormatted
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

            InfoRow(
                icon = Icons.Default.Info,
                label = "Days left",
                value = "${status.daysRemaining} days"
            )
        }
    }
}