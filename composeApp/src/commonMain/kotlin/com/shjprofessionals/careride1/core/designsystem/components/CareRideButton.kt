package com.careride.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.careride.core.designsystem.theme.CareRideTheme

@Composable
fun CareRidePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String = text
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp) // Accessible touch target
            .semantics { contentDescription = accessibilityLabel },
        enabled = enabled,
        shape = RoundedCornerShape(CareRideTheme.radii.lg),
        contentPadding = PaddingValues(
            horizontal = CareRideTheme.spacing.lg,
            vertical = CareRideTheme.spacing.sm
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun CareRideSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accessibilityLabel: String = text
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { contentDescription = accessibilityLabel },
        enabled = enabled,
        shape = RoundedCornerShape(CareRideTheme.radii.lg),
        contentPadding = PaddingValues(
            horizontal = CareRideTheme.spacing.lg,
            vertical = CareRideTheme.spacing.sm
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}