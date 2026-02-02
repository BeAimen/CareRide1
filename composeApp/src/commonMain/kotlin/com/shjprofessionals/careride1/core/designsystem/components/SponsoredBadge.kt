package com.careride.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.careride.core.designsystem.theme.CareRideLightColors
import com.careride.core.designsystem.theme.CareRideTheme

@Composable
fun SponsoredBadge(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(
                color = CareRideLightColors.SponsoredContainer,
                shape = RoundedCornerShape(CareRideTheme.radii.sm)
            )
            .padding(
                horizontal = CareRideTheme.spacing.xs,
                vertical = CareRideTheme.spacing.xxs
            )
            .semantics {
                contentDescription = "This is a sponsored listing"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(12.dp),
            tint = CareRideLightColors.Sponsored
        )
        Text(
            text = "Featured",
            style = MaterialTheme.typography.labelSmall,
            color = CareRideLightColors.Sponsored,
            modifier = Modifier.padding(start = CareRideTheme.spacing.xxs)
        )
    }
}