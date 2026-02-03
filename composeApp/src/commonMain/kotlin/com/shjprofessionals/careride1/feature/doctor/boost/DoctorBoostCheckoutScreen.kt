package com.shjprofessionals.careride1.feature.doctor.boost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.shjprofessionals.careride1.core.designsystem.components.CareRidePrimaryButton
import com.shjprofessionals.careride1.core.designsystem.components.CareRideSecondaryButton
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideLightColors
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.DoctorBoostStatus
import org.koin.core.parameter.parametersOf

data class DoctorBoostCheckoutScreen(
    val planId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel = koinScreenModel<DoctorBoostCheckoutViewModel> { parametersOf(planId) }
        val state by viewModel.state.collectAsState()

        BoostCheckoutContent(
            state = state,
            onBackClick = { navigator.pop() },
            onPayClick = viewModel::processPayment,
            onSimulateFailure = viewModel::simulateFailure,
            onCancel = { navigator.pop() },
            onRetry = viewModel::retry,
            onDone = {
                // Pop back past the checkout screen
                navigator.popUntil { screen ->
                    screen !is DoctorBoostCheckoutScreen
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BoostCheckoutContent(
    state: BoostCheckoutState,
    onBackClick: () -> Unit,
    onPayClick: () -> Unit,
    onSimulateFailure: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onDone: () -> Unit
) {
    Scaffold(
        topBar = {
            if (state.step == BoostCheckoutStep.REVIEW) {
                TopAppBar(
                    title = { Text("Boost Checkout") },
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
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (state.step) {
                BoostCheckoutStep.REVIEW -> {
                    ReviewStep(
                        state = state,
                        onPayClick = onPayClick,
                        onSimulateFailure = onSimulateFailure,
                        onCancel = onCancel
                    )
                }

                BoostCheckoutStep.PROCESSING -> {
                    ProcessingStep()
                }

                BoostCheckoutStep.SUCCESS -> {
                    SuccessStep(
                        status = state.boostStatus,
                        onDone = onDone
                    )
                }

                BoostCheckoutStep.FAILURE -> {
                    FailureStep(
                        error = state.error?.userMessage ?: "Payment failed",
                        onRetry = onRetry,
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewStep(
    state: BoostCheckoutState,
    onPayClick: () -> Unit,
    onSimulateFailure: () -> Unit,
    onCancel: () -> Unit
) {
    val plan = state.plan ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(CareRideTheme.spacing.md)
    ) {
        // Mock payment disclaimer
        Surface(
            color = MaterialTheme.colorScheme.tertiaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🧪", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                Text(
                    text = "This is a mock checkout. No real payment will be processed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        // Order summary
        Text(
            text = "Order Summary",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

        // Plan details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = CareRideLightColors.SponsoredContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(CareRideTheme.spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = CareRideLightColors.Sponsored,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                        Text(
                            text = plan.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = plan.displayPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

                Text(
                    text = "Billed ${plan.billingDescription}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                Text(
                    text = "${plan.boostMultiplier}x visibility boost",
                    style = MaterialTheme.typography.labelLarge,
                    color = CareRideLightColors.Sponsored
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total due today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = plan.displayPrice,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        // What you get
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(CareRideTheme.spacing.md)
            ) {
                Text(
                    text = "✨ What you get",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                plan.features.forEach { feature ->
                    Text(
                        text = "• $feature",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action buttons
        CareRidePrimaryButton(
            text = "Pay ${plan.displayPrice}",
            onClick = onPayClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        // Test buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
        ) {
            OutlinedButton(
                onClick = onSimulateFailure,
                modifier = Modifier.weight(1f)
            ) {
                Text("Test Failure", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
    }
}

@Composable
private fun ProcessingStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                liveRegion = LiveRegionMode.Assertive
                contentDescription = "Processing payment, please wait"
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(64.dp),
            strokeWidth = 4.dp
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "Processing Payment...",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = "Please wait while we activate your boost",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SuccessStep(
    status: DoctorBoostStatus?,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = CareRideLightColors.SponsoredContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = CareRideLightColors.Sponsored
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "Boost Activated!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = "Your profile is now featured in search results. Patients will see your listing first!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (status is DoctorBoostStatus.Active) {
            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(
                    modifier = Modifier.padding(CareRideTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Next renewal: ${status.renewalDateFormatted}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

        CareRidePrimaryButton(
            text = "View Analytics",
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FailureStep(
    error: String,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(CareRideTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.errorContainer,
            modifier = Modifier.size(100.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

        Text(
            text = "Payment Failed",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.xl))

        CareRidePrimaryButton(
            text = "Try Again",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        CareRideSecondaryButton(
            text = "Cancel",
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
