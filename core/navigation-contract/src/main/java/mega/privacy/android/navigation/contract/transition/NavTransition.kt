package mega.privacy.android.navigation.contract.transition

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

/**
 * Slide transition for forward navigation (entering screen slides in from right)
 */
val slideForwardTransition = slideInHorizontally(
    initialOffsetX = { it }
) togetherWith slideOutHorizontally(
    targetOffsetX = { -it }
)

/**
 * Slide transition for backward navigation (entering screen slides in from left)
 */
val slideBackwardTransition = slideInHorizontally(
    initialOffsetX = { -it }
) togetherWith slideOutHorizontally(
    targetOffsetX = { it }
)

/**
 * Screen exit transition with fade in animation
 */
val fadeTransition = fadeIn(
    animationSpec = tween(FADE_ANIM_DURATION_MS)
) togetherWith fadeOut(
    animationSpec = tween(FADE_ANIM_DURATION_MS)
)

/**
 * Forward fade transition that keeps the screen underneath fully opaque for the whole
 * animation: only the incoming (top-most) entry fades in, while the outgoing entry is held
 * at full opacity beneath it and removed instantly at the end.
 *
 * Unlike [fadeTransition] (a crossfade where both entries are partially transparent at the
 * midpoint), this never exposes a translucent host window - use it when the Activity hosting
 * the [androidx.navigation3.ui.NavDisplay] has a transparent/translucent background that would
 * otherwise reveal the Activity behind it mid-transition.
 */
val opaqueFadeForwardTransition = (fadeIn(
    animationSpec = tween(FADE_ANIM_DURATION_MS)
) togetherWith fadeOut(
    // Hold the outgoing entry opaque for the whole duration, then drop it in a single frame.
    animationSpec = tween(durationMillis = 1, delayMillis = FADE_ANIM_DURATION_MS)
)).apply {
    // Draw the incoming entry on top so its fade-in is visible over the opaque outgoing entry.
    targetContentZIndex = 1f
}

/**
 * Backward (pop) counterpart of [opaqueFadeForwardTransition]: the outgoing (top-most) entry
 * fades out while the revealed entry beneath it is shown at full opacity immediately, so the
 * translucent host window is never exposed mid-transition.
 */
val opaqueFadeBackwardTransition = (EnterTransition.None togetherWith fadeOut(
    animationSpec = tween(FADE_ANIM_DURATION_MS)
)).apply {
    // Keep the revealed entry beneath the fading-out one.
    targetContentZIndex = -1f
}

/**
 * Forward transition for modal-style destinations: the entering screen slides in from the bottom
 * over the outgoing screen, which is held fully visible beneath it for the whole animation and
 * removed instantly at the end.
 */
val slideUpForwardTransition = (slideInVertically(
    initialOffsetY = { it },
    animationSpec = tween(SLIDE_ANIM_DURATION_MS),
) togetherWith fadeOut(
    // Hold the outgoing entry visible for the whole duration, then drop it in a single frame.
    animationSpec = tween(durationMillis = 1, delayMillis = SLIDE_ANIM_DURATION_MS)
)).apply {
    // Draw the incoming entry on top so it slides over the outgoing entry.
    targetContentZIndex = 1f
}

/**
 * Backward (pop) counterpart of [slideUpForwardTransition]: the outgoing (top-most) entry slides
 * down out of the screen while the revealed entry beneath it is shown at full opacity immediately.
 */
val slideDownBackwardTransition = (EnterTransition.None togetherWith slideOutVertically(
    targetOffsetY = { it },
    animationSpec = tween(SLIDE_ANIM_DURATION_MS),
)).apply {
    // Keep the revealed entry beneath the sliding-out one.
    targetContentZIndex = -1f
}

private const val FADE_ANIM_DURATION_MS = 500
private const val SLIDE_ANIM_DURATION_MS = 400