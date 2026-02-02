package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.shjprofessionals.careride1.core.util.ErrorEvent
import com.shjprofessionals.careride1.core.util.ErrorHandler
import kotlinx.coroutines.flow.collectLatest

/**
 * Snackbar host that listens to centralized ErrorHandler.
 * Place at top-level scaffold.
 */
@Composable
fun AppErrorSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        ErrorHandler.errors.collectLatest { event ->
            val result = hostState.showSnackbar(
                message = event.error.userMessage,
                actionLabel = if (event.canRetry) "Retry" else null,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                event.retryAction?.invoke()
            }
        }
    }

    SnackbarHost(hostState = hostState, modifier = modifier)
}
