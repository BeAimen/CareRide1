package com.shjprofessionals.careride1.core.designsystem.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.shjprofessionals.careride1.core.designsystem.theme.CareRideTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Centered loading indicator for full screen loading.
 * Announces to screen readers when displayed.
 */
@Composable
fun ScreenLoading(
    modifier: Modifier = Modifier,
    message: String? = null
) {
    val announcement = message ?: "Loading, please wait"

    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = announcement
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp)
            )

            if (message != null) {
                Spacer(modifier = Modifier.height(CareRideTheme.spacing.md))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Pull-to-refresh state holder
 */
@Stable
class PullRefreshState(
    private val refreshThresholdPx: Float,
    private val maxPullPx: Float,
    private val onRefresh: () -> Unit,
    private val coroutineScope: CoroutineScope
) {
    var isRefreshing by mutableStateOf(false)
        internal set

    var pullOffset by mutableFloatStateOf(0f)
        internal set

    val progress: Float
        get() = (pullOffset / refreshThresholdPx).coerceIn(0f, 1f)

    val isThresholdReached: Boolean
        get() = pullOffset >= refreshThresholdPx

    internal fun onPull(delta: Float): Float {
        if (isRefreshing) return 0f

        val newOffset = (pullOffset + delta).coerceIn(0f, maxPullPx)
        val consumed = newOffset - pullOffset
        pullOffset = newOffset
        return consumed
    }

    internal fun onRelease() {
        if (isThresholdReached && !isRefreshing) {
            isRefreshing = true
            coroutineScope.launch {
                onRefresh()
            }
        }
        if (!isRefreshing) {
            pullOffset = 0f
        }
    }

    fun endRefresh() {
        isRefreshing = false
        pullOffset = 0f
    }
}

@Composable
fun rememberPullRefreshState(
    isRefreshing: Boolean,
    onRefresh: () -> Unit
): PullRefreshState {
    val density = LocalDensity.current
    val thresholdPx = with(density) { 80.dp.toPx() }
    val maxPullPx = with(density) { 150.dp.toPx() }
    val coroutineScope = rememberCoroutineScope()

    val state = remember(onRefresh) {
        PullRefreshState(
            refreshThresholdPx = thresholdPx,
            maxPullPx = maxPullPx,
            onRefresh = onRefresh,
            coroutineScope = coroutineScope
        )
    }

    // Sync external refresh state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            state.isRefreshing = true
            state.pullOffset = thresholdPx * 0.5f
        } else {
            state.endRefresh()
        }
    }

    return state
}

private class PullRefreshNestedScrollConnection(
    private val state: PullRefreshState
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        // When scrolling up and we have pull offset, consume the scroll to reduce offset
        if (available.y < 0 && state.pullOffset > 0) {
            val consumed = state.onPull(available.y)
            return Offset(0f, consumed)
        }
        return Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        // When scrolling down and there's leftover scroll (list is at top), use it for pull
        if (available.y > 0 && !state.isRefreshing) {
            val consumed = state.onPull(available.y)
            return Offset(0f, consumed)
        }
        return Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // Release on fling
        if (state.pullOffset > 0) {
            state.onRelease()
        }
        return Velocity.Zero
    }
}

/**
 * Pull to refresh container that works with LazyColumn and other scrollable content.
 */
@Composable
fun RefreshableContent(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val state = rememberPullRefreshState(isRefreshing, onRefresh)
    val nestedScrollConnection = remember(state) { PullRefreshNestedScrollConnection(state) }

    Box(
        modifier = modifier
            .then(
                if (enabled) Modifier.nestedScroll(nestedScrollConnection) else Modifier
            )
    ) {
        content()

        // Refresh indicator at top
        PullRefreshIndicator(
            state = state,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun PullRefreshIndicator(
    state: PullRefreshState,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val indicatorSize = 40.dp
    val indicatorSizePx = with(density) { indicatorSize.toPx() }

    // Calculate offset - indicator starts above screen and moves down
    val offsetY = with(density) {
        (state.pullOffset - indicatorSizePx).coerceAtLeast(-indicatorSizePx / 2).toDp()
    }

    // Only show when there's pull offset or refreshing
    if (state.pullOffset > 0 || state.isRefreshing) {
        Box(
            modifier = modifier
                .offset(y = offsetY)
                .padding(top = CareRideTheme.spacing.sm)
        ) {
            Surface(
                modifier = Modifier
                    .size(indicatorSize)
                    .semantics {
                        if (state.isRefreshing) {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Refreshing"
                        }
                    },
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                tonalElevation = 2.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (state.isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        // Rotation based on pull progress
                        val rotation = state.progress * 360f
                        val alpha = state.progress.coerceIn(0.4f, 1f)

                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Pull to refresh",
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(rotation),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simple inline refresh indicator
 */
@Composable
fun RefreshIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    if (isRefreshing) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(CareRideTheme.spacing.md)
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Refreshing"
                },
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
    }
}

/**
 * Fallback with manual refresh button.
 */
@Composable
fun RefreshableContentFallback(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CareRideTheme.spacing.md, vertical = CareRideTheme.spacing.xs),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(CareRideTheme.spacing.xs))
                Text(
                    text = "Refreshing...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                TextButton(
                    onClick = onRefresh,
                    contentPadding = PaddingValues(horizontal = CareRideTheme.spacing.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(CareRideTheme.spacing.xxs))
                    Text(
                        text = "Refresh",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}