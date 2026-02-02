package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme

@Composable
fun DoctorCardSkeleton(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(CareRideTheme.radii.lg)
            )
            .padding(CareRideTheme.spacing.md)
    ) {
        Row {
            // Avatar skeleton
            Spacer(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(CareRideTheme.spacing.sm))

            Column(modifier = Modifier.weight(1f)) {
                // Name skeleton
                Spacer(
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(CareRideTheme.radii.sm))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

                // Specialty skeleton
                Spacer(
                    modifier = Modifier
                        .height(16.dp)
                        .fillMaxWidth(0.4f)
                        .clip(RoundedCornerShape(CareRideTheme.radii.sm))
                        .background(brush)
                )

                Spacer(modifier = Modifier.height(CareRideTheme.spacing.xs))

                // Location skeleton
                Spacer(
                    modifier = Modifier
                        .height(14.dp)
                        .fillMaxWidth(0.5f)
                        .clip(RoundedCornerShape(CareRideTheme.radii.sm))
                        .background(brush)
                )
            }
        }

        Spacer(modifier = Modifier.height(CareRideTheme.spacing.sm))

        // Rating row skeleton
        Spacer(
            modifier = Modifier
                .height(16.dp)
                .fillMaxWidth(0.45f)
                .clip(RoundedCornerShape(CareRideTheme.radii.sm))
                .background(brush)
        )
    }
}

@Composable
fun DoctorListSkeleton(
    itemCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CareRideTheme.spacing.sm)
    ) {
        repeat(itemCount) {
            DoctorCardSkeleton()
        }
    }
}
