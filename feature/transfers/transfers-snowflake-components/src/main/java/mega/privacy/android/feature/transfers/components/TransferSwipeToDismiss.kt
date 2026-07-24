package mega.privacy.android.feature.transfers.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.node.Ref
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.abs
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.theme.values.IconColor
import mega.android.core.ui.tokens.theme.DSTokens

private const val DISMISS_THRESHOLD_FRACTION = 0.3f

/**
 * [SwipeToDismissBox] wrapper for transfer items that only commits a dismissal once the row has
 * actually been dragged past [DISMISS_THRESHOLD_FRACTION] of its width, with a haptic tick
 * whenever that threshold is crossed in either direction, so the user knows both when releasing
 * would trigger the action and when dragging back has cancelled it. Material3 defaults (56dp
 * positional threshold and a non-configurable 125dp/s velocity threshold that commits flings
 * from any offset) make accidental dismissals far too easy on a full-width row.
 *
 * @param enableDismissFromStartToEnd Whether the start to end direction is enabled.
 * @param enableDismissFromEndToStart Whether the end to start direction is enabled.
 * @param onDismiss Called with the dismissal direction once a swipe passes the threshold and is
 * released. Runs on the state so implementations can e.g. snap back to
 * [SwipeToDismissBoxValue.Settled].
 * @param backgroundContent Content revealed behind the row while swiping, receiving the swipe
 * state to check the current dismiss direction and whether the drag has passed the dismiss
 * threshold, typically rendered with [TransferSwipeToDismissBackground].
 * @param content The row content.
 */
@Composable
internal fun TransferSwipeToDismissBox(
    enableDismissFromStartToEnd: Boolean,
    enableDismissFromEndToStart: Boolean,
    onDismiss: suspend SwipeToDismissBoxState.(SwipeToDismissBoxValue) -> Unit,
    backgroundContent: @Composable RowScope.(SwipeToDismissBoxState, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val thresholdPx = remember { mutableFloatStateOf(Float.MAX_VALUE) }
    val stateRef = remember { Ref<SwipeToDismissBoxState>() }

    // The deprecated overload is the only public API able to veto velocity-based dismissals,
    // which would otherwise commit from any offset. Its positionalThreshold parameter is ignored
    // by the current material3 implementation, hence the threshold is measured via onSizeChanged
    // and enforced in confirmValueChange on the offset the row was actually dragged to.
    @Suppress("DEPRECATION")
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            value == SwipeToDismissBoxValue.Settled ||
                    abs(currentOffset(stateRef)) >= thresholdPx.floatValue
        },
    )
    stateRef.value = state

    val thresholdCrossed by remember {
        derivedStateOf { abs(currentOffset(stateRef)) >= thresholdPx.floatValue }
    }

    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(state) {
        snapshotFlow { thresholdCrossed }
            .drop(1)
            .collect {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
    }

    SwipeToDismissBox(
        state = state,
        backgroundContent = { backgroundContent(state, thresholdCrossed) },
        modifier = modifier.onSizeChanged { size ->
            thresholdPx.floatValue = size.width * DISMISS_THRESHOLD_FRACTION
        },
        enableDismissFromStartToEnd = enableDismissFromStartToEnd,
        enableDismissFromEndToStart = enableDismissFromEndToStart,
        onDismiss = { direction ->
            scope.launch { state.onDismiss(direction) }
        },
        content = content,
    )
}

/**
 * Background behind a swiped transfer row: neutral grey while the drag is still below the
 * dismiss threshold, animating to [triggerColor] once releasing would trigger the action, so the
 * user gets a clear visual cue in addition to the haptic tick.
 *
 * @param painter The action icon.
 * @param contentDescription Content description for the action icon.
 * @param triggerColor Background colour once the drag has passed the dismiss threshold.
 * @param alignment Where the icon sits within the row, matching the swipe direction.
 * @param thresholdCrossed Whether the drag has passed the dismiss threshold.
 */
@Composable
internal fun TransferSwipeToDismissBackground(
    painter: Painter,
    contentDescription: String?,
    triggerColor: Color,
    alignment: Alignment,
    thresholdCrossed: Boolean,
    modifier: Modifier = Modifier,
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (thresholdCrossed) {
            triggerColor
        } else {
            DSTokens.colors.background.surface3
        },
        label = "transferSwipeToDismissBackground",
    )
    MegaIcon(
        painter = painter,
        contentDescription = contentDescription,
        tint = if (thresholdCrossed) IconColor.Inverse else IconColor.Secondary,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .wrapContentSize(alignment)
            .padding(12.dp)
    )
}

private fun currentOffset(stateRef: Ref<SwipeToDismissBoxState>): Float =
    stateRef.value?.let { runCatching { it.requireOffset() }.getOrNull() } ?: 0f
