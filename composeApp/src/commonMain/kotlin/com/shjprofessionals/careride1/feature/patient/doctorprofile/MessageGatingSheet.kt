package com.shjprofessionals.careride1.feature.patient.doctorprofile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.components.CareRidePrimaryButton
import com.shjprofessionals.careride1.core.designsystem.components.CareRideSecondaryButton
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageGatingSheet(
    doctorName: String,
    onDismiss: () -> Unit,
    onViewPlans: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CareRideTheme.spacing.lg)
                .padding(bottom = CareRideTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lock icon
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            Text(
                text = "Subscription Required",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            Text(
                text = "To message $doctorName, you need an active CareRide subscription.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Benefits
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(CareRideTheme.spacing.md)
                ) {
                    Text(
                        text = "With a subscription, you can:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

                    BenefitItem("Message any doctor directly")
                    BenefitItem("Get responses within 24 hours")
                    BenefitItem("Unlimited conversations")
                    BenefitItem("Cancel anytime")
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.lg))

            // CTA buttons
            CareRidePrimaryButton(
                text = "View Plans",
                onClick = onViewPlans,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            CareRideSecondaryButton(
                text = "Maybe Later",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BenefitItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = CareRideTheme.spacing.xxs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "âœ“",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
