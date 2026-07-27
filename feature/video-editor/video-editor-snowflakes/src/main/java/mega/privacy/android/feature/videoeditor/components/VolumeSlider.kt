package mega.privacy.android.feature.videoeditor.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.AppTheme
import mega.android.core.ui.theme.values.IconColor
import mega.android.core.ui.theme.values.TextColor
import mega.android.core.ui.tokens.theme.DSTokens
import mega.privacy.android.icon.pack.IconPack
import kotlin.math.roundToInt


/**
 * Volume slider spanning `0..[MAX_VOLUME]`, rendered as an M3 standard slider
 * (medium size) with a leading icon inside the track and a value indicator
 * (0..200) shown while dragging.
 *
 * The value indicator is drawn inline (not in a popup) so it follows the thumb
 * in the same layout pass. It floats above the slider bounds, so ancestors
 * must not clip (the tool deck draws its rounded shape instead of clipping).
 *
 * The decorative leading icon reflects the current level: off when muted, min
 * up to 10%, plain up to 50%, max above.
 *
 * @param value current gain in `0..[MAX_VOLUME]`
 * @param onValueChange reports the new gain in the same range
 * @param modifier applied to the slider container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val colors = SliderDefaults.colors(
        thumbColor = DSTokens.colors.button.primary,
        activeTrackColor = DSTokens.colors.button.primary,
        inactiveTrackColor = DSTokens.colors.border.strong,
    )
    BoxWithConstraints(
        modifier = modifier.height(ThumbSize.height),
        contentAlignment = Alignment.BottomStart,
    ) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..MAX_VOLUME,
            colors = colors,
            interactionSource = interactionSource,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = interactionSource,
                    colors = colors,
                    thumbSize = ThumbSize,
                )
            },
            track = { sliderState ->
                val range = sliderState.valueRange
                val trackFraction =
                    ((sliderState.value - range.start) / (range.endInclusive - range.start))
                        .coerceIn(0f, 1f)
                val activeColor = DSTokens.colors.button.primary
                val inactiveColor = DSTokens.colors.border.strong
                Canvas(
                    Modifier
                        .fillMaxWidth()
                        .height(TrackHeight),
                ) {
                    drawVolumeTrack(trackFraction, activeColor, inactiveColor)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(ThumbSize.height),
        )

        val fraction = (value / MAX_VOLUME).coerceIn(0f, 1f)

        // Decorative leading icon inside the track; dark (inverse) while the
        // active track still covers it, accent once the fill has retreated
        // past it. Mute is a separate button supplied by the caller.
        val activeWidth = maxWidth * fraction
        val iconOnActiveTrack = activeWidth >= IconStartPadding
        val icon = when {
            value <= 0f -> IconPack.Medium.Thin.Outline.VolumeOff
            value <= MIN_LEVEL -> IconPack.Medium.Thin.Outline.Volume
            value <= MID_LEVEL -> IconPack.Medium.Thin.Outline.VolumeMin
            else -> IconPack.Medium.Thin.Outline.VolumeMax
        }
        MegaIcon(
            imageVector = icon,
            tint = if (iconOnActiveTrack) IconColor.Inverse else IconColor.Accent,
            contentDescription = null,
            modifier = Modifier
                .padding(
                    start = IconStartPadding,
                    bottom = (ThumbSize.height - IconSize) / 2,
                )
                .size(IconSize),
        )

        // Value indicator, drawn inline and anchored to the thumb fraction so
        // it never trails the thumb the way a window-positioned popup does.
        val dragged by interactionSource.collectIsDraggedAsState()
        val pressed by interactionSource.collectIsPressedAsState()
        val indicatorAlpha by animateFloatAsState(
            targetValue = if (dragged || pressed) 1f else 0f,
            label = "indicatorAlpha",
        )
        if (indicatorAlpha > 0f) {
            val thumbCenterX = ThumbSize.width / 2 + (maxWidth - ThumbSize.width) * fraction
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = thumbCenterX - IndicatorSize.width / 2,
                        y = -(IndicatorSize.height + IndicatorGap),
                    )
                    .size(IndicatorSize)
                    .alpha(indicatorAlpha)
                    .background(
                        color = DSTokens.colors.background.inverse,
                        shape = RoundedCornerShape(percent = 50),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                MegaText(
                    text = (value * 100).roundToInt().toString(),
                    style = AppTheme.typography.labelLarge,
                    textColor = TextColor.Inverse,
                )
            }
        }
    }
}

/**
 * Draws the M3-style split track: active and inactive segments separated by a
 * gap around the thumb, 12dp outer / 2dp inner corners, and a stop indicator
 * dot near the trailing end. A divider break at unity gain (100%) visually
 * splits the range into a volume half and a boost half. Mirrors in RTL.
 */
private fun DrawScope.drawVolumeTrack(
    fraction: Float,
    activeColor: Color,
    inactiveColor: Color,
) {
    val gap = ThumbTrackGap.toPx() + ThumbSize.width.toPx() / 2
    val thumbCenter = size.width * fraction
    val unityX = size.width * (UNITY_VOLUME / MAX_VOLUME)
    scale(scaleX = if (layoutDirection == LayoutDirection.Rtl) -1f else 1f, scaleY = 1f) {
        drawTrackSegmentWithUnityBreak(
            left = 0f,
            right = thumbCenter - gap,
            startCorner = TrackOuterCorner.toPx(),
            endCorner = TrackInsideCorner.toPx(),
            color = activeColor,
            unityX = unityX,
        )
        val inactiveStart = thumbCenter + gap
        drawTrackSegmentWithUnityBreak(
            left = inactiveStart,
            right = size.width,
            startCorner = TrackInsideCorner.toPx(),
            endCorner = TrackOuterCorner.toPx(),
            color = inactiveColor,
            unityX = unityX,
        )
        val stopCenterX = size.width - TrackOuterCorner.toPx()
        if (stopCenterX > inactiveStart) {
            drawCircle(
                color = activeColor,
                radius = StopIndicatorSize.toPx() / 2,
                center = Offset(stopCenterX, size.height / 2),
            )
        }
    }
}

/**
 * Draws a track segment, split in two around the unity mark when the segment
 * crosses it; when the thumb (and its gap) already sits on the mark, the
 * segment is drawn whole and the thumb gap doubles as the break.
 */
private fun DrawScope.drawTrackSegmentWithUnityBreak(
    left: Float,
    right: Float,
    startCorner: Float,
    endCorner: Float,
    color: Color,
    unityX: Float,
) {
    if (right <= left) return
    val halfBreak = UnityBreak.toPx() / 2
    if (unityX - halfBreak > left && unityX + halfBreak < right) {
        drawTrackSegment(left, unityX - halfBreak, startCorner, TrackInsideCorner.toPx(), color)
        drawTrackSegment(unityX + halfBreak, right, TrackInsideCorner.toPx(), endCorner, color)
    } else {
        drawTrackSegment(left, right, startCorner, endCorner, color)
    }
}

private fun DrawScope.drawTrackSegment(
    left: Float,
    right: Float,
    startCorner: Float,
    endCorner: Float,
    color: Color,
) {
    val maxCorner = (right - left) / 2
    val start = CornerRadius(startCorner.coerceAtMost(maxCorner))
    val end = CornerRadius(endCorner.coerceAtMost(maxCorner))
    val path = Path().apply {
        addRoundRect(
            RoundRect(
                left = left,
                top = 0f,
                right = right,
                bottom = size.height,
                topLeftCornerRadius = start,
                bottomLeftCornerRadius = start,
                topRightCornerRadius = end,
                bottomRightCornerRadius = end,
            ),
        )
    }
    drawPath(path, color)
}

/** Maximum gain the slider can reach (200% → 2× amplification at export). */
const val MAX_VOLUME = 2f

/** Unity gain (100%) — the boundary between the volume and boost halves. */
private const val UNITY_VOLUME = 1f

/** Width of the track break drawn at unity gain. */
private val UnityBreak = 4.dp

// Gain thresholds for the leading icon: volume-min up to 10%, volume up to 50%,
// volume-max above.
private const val MIN_LEVEL = 0.3f
private const val MID_LEVEL = 0.6f

// M3 standard slider, medium size: 40dp track, 4x52dp handle, 48x44dp value
// indicator floating 4dp above the handle. The track is drawn manually because
// the design wants 12dp outer corners and SliderDefaults.Track only draws
// fully-rounded ones; gap / inside-corner / stop-dot metrics match M3.
private val TrackHeight = 40.dp
private val ThumbSize = DpSize(4.dp, 52.dp)
private val TrackOuterCorner = 12.dp
private val TrackInsideCorner = 2.dp
private val ThumbTrackGap = 6.dp
private val StopIndicatorSize = 4.dp
private val IconSize = 24.dp
private val IconStartPadding = 16.dp
private val IndicatorSize = DpSize(48.dp, 44.dp)
private val IndicatorGap = 4.dp

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun VolumeSliderPreview() {
    AndroidThemeForPreviews {
        VolumeSlider(
            value = 1.5f,
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun VolumeSliderPreview2() {
    AndroidThemeForPreviews {
        VolumeSlider(
            value = 0.1f,
            onValueChange = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
