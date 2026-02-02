package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideLightColors
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import com.shjprofessionals.careride1.domain.model.BillingPeriod
import com.shjprofessionals.careride1.domain.model.BoostPlan

@Composable
fun BoostPlanCard(
    plan: BoostPlan,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outline
    }

    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CareRideTheme.radii.lg),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        onClick = onSelect
    ) {
        Column(
            modifier = Modifier.padding(CareRideTheme.spacing.md)
        ) {
            // Header row
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (plan.isPopular) {
                    Surface(
                        color = CareRideLightColors.Sponsored,
                        shape = RoundedCornerShape(CareRideTheme.radii.sm)
                    ) {
                        Text(
                            text = "Popular",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.padding(
                                horizontal = CareRideTheme.spacing.xs,
                                vertical = CareRideTheme.spacing.xxs
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

            Text(
                text = plan.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Price
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = plan.displayPrice,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                Text(
                    text = plan.billingDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (plan.billingPeriod == BillingPeriod.YEARLY) {
                Text(
                    text = "Just ${plan.monthlyEquivalentDisplay}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Boost multiplier
            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
            Surface(
                color = CareRideLightColors.SponsoredContainer,
                shape = RoundedCornerShape(CareRideTheme.radii.sm)
            ) {
                Text(
                    text = "${plan.boostMultiplier}x visibility boost",
                    style = MaterialTheme.typography.labelMedium,
                    color = CareRideLightColors.Sponsored,
                    modifier = Modifier.padding(
                        horizontal = CareRideTheme.spacing.sm,
                        vertical = CareRideTheme.spacing.xxs
                    )
                )
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))

            // Features
            plan.features.forEach { feature ->
                Row(
                    modifier = Modifier.padding(vertical = CareRideTheme.spacing.xxs),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                    Text(
                        text = feature,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                    Text(
                        text = "Selected",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
