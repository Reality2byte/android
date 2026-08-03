package mega.privacy.android.app.presentation.imagepreview.view

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import mega.android.core.ui.components.tooltip.direction.TooltipDirection
import mega.android.core.ui.components.tooltip.popup.interactive.InteractiveTopDirectionTooltipPopup
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Renders the onboarding tooltip that points to the edit button in the image preview top bar
 * when the previewed node is an editable video.
 *
 * The tooltip uses [LayoutCoordinates] captured via `onGloballyPositioned`, which can
 * become stale or detached during state transitions. The visibility is guarded by:
 *  1. [LayoutCoordinates.isAttached] — the captured coordinates must still be valid
 *  2. Lifecycle state — the screen must be resumed (popup is a separate window)
 */
@Composable
internal fun VideoEditorTooltip(
    anchorCoordinates: LayoutCoordinates?,
    onDismiss: () -> Unit,
) {
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    if (!lifecycleState.isAtLeast(Lifecycle.State.RESUMED)) return

    anchorCoordinates?.takeIf { it.isAttached }?.let { coordinates ->
        InteractiveTopDirectionTooltipPopup(
            modifier = Modifier
                .testTag(VIDEO_EDITOR_TOOLTIP_TAG)
                .widthIn(max = 280.dp),
            direction = TooltipDirection.Top.Centre,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
            title = stringResource(sharedR.string.video_editor_tooltip_title),
            body = stringResource(sharedR.string.video_editor_tooltip_description),
            needCloseIcon = true,
            anchorViewCoordinates = coordinates,
            onDismissRequest = onDismiss,
        )
    }
}

internal const val VIDEO_EDITOR_TOOLTIP_TAG = "image_preview:video_editor_tooltip"
