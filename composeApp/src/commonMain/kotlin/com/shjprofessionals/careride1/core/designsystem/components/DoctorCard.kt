package com.careride.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.careride.core.designsystem.theme.CareRideTheme
import com.careride.domain.model.Doctor
import com.careride.core.designsystem.theme.CareRideLightColors

@Composable
fun DoctorCard(
    doctor: Doctor,
    onClick: () -> Unit,
    onWhyThisClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = buildString {
                    append("${doctor.name}, ${doctor.specialty.displayName}")
                    if (doctor.isBoosted) append(", Featured listing")
                    append(", Rating ${doctor.rating} stars from ${doctor.reviewCount} reviews")
                    if (doctor.isAvailableToday) append(", Available today")
                }
            },
        shape = RoundedCornerShape(CareRideTheme.radii.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = CareRideTheme.elevation.sm
        )
    ) {
        Column(
            modifier = Modifier.padding(CareRideTheme.spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar placeholder
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

                Column(modifier = Modifier.weight(1f)) {
                    // Name + Sponsored badge row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = doctor.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )

                        if (doctor.isBoosted) {
                            Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                            SponsoredBadge()
                        }
                    }

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

                    // Specialty
                    Text(
                        text = doctor.specialty.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.xxs))

                    // Location
                    Text(
                        text = doctor.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

            // Rating + Availability + Info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rating
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = CareRideLightColors.Sponsored
                    )
                    Text(
                        text = "${doctor.rating}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = CareRideTheme.spacing.xxs)
                    )
                    Text(
                        text = "(${doctor.reviewCount})",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = CareRideTheme.spacing.xxs)
                    )

                    // Availability badge
                    if (doctor.isAvailableToday) {
                        Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))
                        Text(
                            text = "• Available today",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Why am I seeing this?
                IconButton(
                    onClick = onWhyThisClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Why am I seeing this doctor?",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}