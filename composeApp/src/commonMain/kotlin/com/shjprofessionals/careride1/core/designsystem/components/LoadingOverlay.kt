package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme

/**
 * Full-screen loading overlay that blocks interaction.
 * Announces loading state to screen readers.
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    message: String? = null
) {
    if (isLoading) {
        val announcement = message ?: "Loading, please wait"

        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {} // Block clicks
                )
                .semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = announcement
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                if (message != null) {
                    Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Inline loading indicator for buttons/small areas
 */
@Composable
fun InlineLoading(
    modifier: Modifier = Modifier,
    size: Int = 24,
    contentDescription: String = "Loading"
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(size.dp)
            .semantics {
                this.contentDescription = contentDescription
            },
        strokeWidth = 2.dp
    )
}

/**
 * Content with loading state - shows skeleton or content
 */
@Composable
fun <T> LoadingContent(
    isLoading: Boolean,
    isEmpty: Boolean,
    data: T?,
    loadingContent: @Composable () -> Unit,
    emptyContent: @Composable () -> Unit,
    errorContent: (@Composable () -> Unit)? = null,
    error: Any? = null,
    content: @Composable (T) -> Unit
) {
    when {
        isLoading -> loadingContent()
        error != null && errorContent != null -> errorContent()
        isEmpty || data == null -> emptyContent()
        else -> content(data)
    }
}