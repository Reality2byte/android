package mega.privacy.android.feature.videoeditor.components

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.AppTheme
import mega.android.core.ui.theme.values.TextColor
import mega.android.core.ui.tokens.theme.DSTokens
import kotlin.math.roundToInt

private const val THUMB_COUNT = 10

// Vertical layout: 16dp time ruler, 12dp gap, 60dp strip; the playhead line
// extends 5dp above and 7dp below the strip.
private val RulerHeight = 16.dp
private val RulerGap = 12.dp
private val StripHeight = 60.dp
private val StripTop = RulerHeight + RulerGap
private val PlayheadOverhangTop = 5.dp
private val PlayheadOverhangBottom = 7.dp
private val TotalHeight = StripTop + StripHeight + PlayheadOverhangBottom
private val PlayheadWidth = 4.dp
private val HandleWidth = 8.dp
private val HandleTouchWidth = 32.dp
private val WindowBorder = 2.dp
private val StripCorner = 4.dp

/**
 * Trim filmstrip: a time ruler with a playhead-time badge, a thumbnail strip
 * with a bordered trim window (draggable in/out handles), a selection-duration
 * badge, and a playhead. Tap or drag on any non-handle part of the strip seeks
 * the playhead inside the trim window.
 *
 * @param minTrimRangeMs Smallest window the handles can close down to, in
 * milliseconds. Absolute, so the floor doesn't scale with the video length;
 * sources shorter than it are pinned to the full range.
 * @param formatTime formats an absolute position in milliseconds for the time
 * ruler and the playhead badge (e.g. "00:39").
 */
@Composable
fun Filmstrip(
    sourceUri: Uri?,
    durationMs: Long,
    trimStartMs: Long,
    trimEndMs: Long,
    playheadMs: Long,
    onTrimChange: (Long, Long) -> Unit,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier,
    minTrimRangeMs: Long = 1_000L,
    onSeek: (Long) -> Unit = {},
) {
    var widthPx by remember { mutableStateOf(0) }
    var scrubbing by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val onTrimState = rememberUpdatedState(onTrimChange)
    val onSeekState = rememberUpdatedState(onSeek)

    val safeDuration = durationMs.coerceAtLeast(1L)
    val startFrac = trimStartMs.toFloat() / safeDuration
    val endFrac = trimEndMs.toFloat() / safeDuration
    val playFrac = (playheadMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    val minGapFrac = (minTrimRangeMs.toFloat() / safeDuration).coerceIn(0f, 1f)

    val startFracState = rememberUpdatedState(startFrac)
    val endFracState = rememberUpdatedState(endFrac)
    val trimStartMsState = rememberUpdatedState(trimStartMs)
    val trimEndMsState = rememberUpdatedState(trimEndMs)

    val thumbnails = rememberFilmstripThumbnails(sourceUri, durationMs)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(TotalHeight),
    ) {
        TimeRuler(
            durationMs = safeDuration,
            formatTime = formatTime,
            modifier = Modifier
                .fillMaxWidth()
                .height(RulerHeight),
        )

        Box(
            modifier = Modifier
                .offset(y = StripTop)
                .fillMaxWidth()
                .height(StripHeight)
                .clip(RoundedCornerShape(StripCorner))
                .onSizeChanged { widthPx = it.width }
                .systemGestureExclusion()
                .pointerInput(widthPx, safeDuration) {
                    detectTapGestures(onTap = { offset ->
                        if (widthPx > 0) {
                            val frac = (offset.x / widthPx).coerceIn(0f, 1f)
                            val ms = (frac * safeDuration).toLong()
                            onSeekState.value(
                                ms.coerceIn(trimStartMsState.value, trimEndMsState.value),
                            )
                        }
                    })
                }
                .pointerInput(widthPx, safeDuration) {
                    detectDragGestures(
                        onDragStart = { scrubbing = true },
                        onDragEnd = { scrubbing = false },
                        onDragCancel = { scrubbing = false },
                        onDrag = { change, _ ->
                            if (widthPx > 0) {
                                val frac = (change.position.x / widthPx).coerceIn(0f, 1f)
                                val ms = (frac * safeDuration).toLong()
                                onSeekState.value(
                                    ms.coerceIn(trimStartMsState.value, trimEndMsState.value),
                                )
                                change.consume()
                            }
                        },
                    )
                },
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                repeat(THUMB_COUNT) { i ->
                    val bm = thumbnails.getOrNull(i)
                    if (bm != null) {
                        Image(
                            bitmap = bm.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(placeholderColor(i)),
                        )
                    }
                }
            }

            if (widthPx > 0) {
                val widthDp = with(density) { widthPx.toDp() }
                val startDp = widthDp * startFrac
                val endDp = widthDp * endFrac
                val windowColor = DSTokens.colors.border.strongSelected
                val gripColor = DSTokens.colors.icon.inverseSecondary
                val dimColor = DSTokens.colors.background.blur

                // Dim the trimmed-away regions. Extended by the corner radius
                // under the opaque handles so the handles' rounded corners
                // don't leave bright un-dimmed notches at the window corners.
                Box(
                    modifier = Modifier
                        .width((startDp + StripCorner).coerceAtLeast(0.dp))
                        .fillMaxHeight()
                        .background(dimColor),
                )
                Box(
                    modifier = Modifier
                        .offset(x = endDp - StripCorner)
                        .width((widthDp - endDp + StripCorner).coerceAtLeast(0.dp))
                        .fillMaxHeight()
                        .background(dimColor),
                )

                // Trim window frame.
                Box(
                    modifier = Modifier
                        .offset(x = startDp)
                        .width((endDp - startDp).coerceAtLeast(0.dp))
                        .fillMaxHeight()
                        .border(WindowBorder, windowColor, RoundedCornerShape(StripCorner)),
                )

                TrimHandle(
                    xOffset = startDp,
                    handleColor = windowColor,
                    gripColor = gripColor,
                    roundStart = true,
                    onDrag = { dx ->
                        val newStartFrac = (startFracState.value + dx / widthPx)
                            .coerceIn(0f, (endFracState.value - minGapFrac).coerceAtLeast(0f))
                        onTrimState.value(
                            (newStartFrac * safeDuration).toLong(),
                            trimEndMsState.value,
                        )
                    },
                )
                TrimHandle(
                    xOffset = endDp - HandleWidth,
                    handleColor = windowColor,
                    gripColor = gripColor,
                    roundStart = false,
                    onDrag = { dx ->
                        val newEndFrac = (endFracState.value + dx / widthPx)
                            .coerceIn((startFracState.value + minGapFrac).coerceAtMost(1f), 1f)
                        onTrimState.value(
                            trimStartMsState.value,
                            (newEndFrac * safeDuration).toLong(),
                        )
                    },
                )

                // Selection duration, just inside the window next to the left
                // handle.
                Box(
                    modifier = Modifier
                        .offset(x = startDp + HandleWidth, y = WindowBorder)
                        .background(
                            color = dimColor,
                            shape = RoundedCornerShape(topStart = 2.dp, bottomEnd = 2.dp),
                        )
                        .padding(horizontal = 2.dp),
                ) {
                    MegaText(
                        text = formatSelectionSeconds(trimEndMs - trimStartMs),
                        style = AppTheme.typography.bodySmall,
                        textColor = TextColor.OnColor,
                    )
                }
            }
        }

        if (widthPx > 0) {
            val widthDp = with(density) { widthPx.toDp() }
            // Keep the line between the handles so it never rides over them
            // when the playhead sits at the window edges.
            val minCenter = widthDp * startFrac + HandleWidth + PlayheadWidth / 2
            val maxCenter = widthDp * endFrac - HandleWidth - PlayheadWidth / 2
            val playDp = if (maxCenter <= minCenter) {
                (minCenter + maxCenter) / 2
            } else {
                (widthDp * playFrac).coerceIn(minCenter, maxCenter)
            }

            // Playhead-time badge over the ruler, centred on (and clamped
            // around) the playhead; only shown while scrubbing.
            AnimatedVisibility(
                visible = scrubbing,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .centerAtFraction(playFrac)
                        .background(DSTokens.colors.icon.brand, RoundedCornerShape(4.dp))
                        .padding(horizontal = 2.dp),
                ) {
                    MegaText(
                        text = formatTime(playheadMs),
                        style = AppTheme.typography.labelMedium,
                        textColor = TextColor.Accent,
                    )
                }
            }

            // Playhead line, extending above and below the strip. The shadow
            // keeps it visible over light frames.
            Box(
                modifier = Modifier
                    .offset(
                        x = playDp - PlayheadWidth / 2,
                        y = StripTop - PlayheadOverhangTop,
                    )
                    .width(PlayheadWidth)
                    .height(PlayheadOverhangTop + StripHeight + PlayheadOverhangBottom)
                    .shadow(4.dp, RoundedCornerShape(percent = 50))
                    .background(Color.White, RoundedCornerShape(percent = 50)),
            )
        }
    }
}

/**
 * Evenly distributed time labels with dot separators: start, quarter, half,
 * three-quarter and full duration.
 */
@Composable
private fun TimeRuler(
    durationMs: Long,
    formatTime: (Long) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val labelStyle = AppTheme.typography.bodySmall.copy(fontSize = 10.sp, lineHeight = 16.sp)
        listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEachIndexed { index, fraction ->
            if (index > 0) {
                MegaText(text = "•", style = labelStyle, textColor = TextColor.Secondary)
            }
            MegaText(
                text = formatTime((durationMs * fraction).toLong()),
                style = labelStyle,
                textColor = TextColor.Secondary,
            )
        }
    }
}

/**
 * Occupies the full available width and places the content centred at
 * [fraction] of it, clamped so the content stays fully visible.
 */
private fun Modifier.centerAtFraction(fraction: Float): Modifier =
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minWidth = 0, minHeight = 0))
        layout(constraints.maxWidth, placeable.height) {
            val x = (constraints.maxWidth * fraction - placeable.width / 2f).roundToInt()
                .coerceIn(0, (constraints.maxWidth - placeable.width).coerceAtLeast(0))
            placeable.place(x, 0)
        }
    }

private fun formatSelectionSeconds(selectionMs: Long): String {
    val tenths = (selectionMs.coerceAtLeast(0L) + 50) / 100
    return "${tenths / 10}.${tenths % 10}s"
}

@Composable
private fun TrimHandle(
    xOffset: Dp,
    handleColor: Color,
    gripColor: Color,
    roundStart: Boolean,
    onDrag: (Float) -> Unit,
) {
    val onDragState = rememberUpdatedState(onDrag)
    val shape = if (roundStart) {
        RoundedCornerShape(topStart = StripCorner, bottomStart = StripCorner)
    } else {
        RoundedCornerShape(topEnd = StripCorner, bottomEnd = StripCorner)
    }
    // The touch target is wider than the 8dp visual handle.
    Box(
        modifier = Modifier
            .offset(x = xOffset - (HandleTouchWidth - HandleWidth) / 2)
            .width(HandleTouchWidth)
            .fillMaxHeight()
            .systemGestureExclusion()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == down.id } ?: break
                        if (!change.pressed) break
                        val dx = change.position.x - change.previousPosition.x
                        if (dx != 0f) onDragState.value(dx)
                        change.consume()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(HandleWidth)
                .fillMaxHeight()
                .background(handleColor, shape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(20.dp)
                    .background(gripColor, RoundedCornerShape(2.dp)),
            )
        }
    }
}

@Composable
private fun rememberFilmstripThumbnails(uri: Uri?, durationMs: Long): List<Bitmap?> {
    val context = LocalContext.current
    val thumbs = remember(uri, durationMs) {
        mutableStateListOf<Bitmap?>().apply { repeat(THUMB_COUNT) { add(null) } }
    }
    LaunchedEffect(uri, durationMs) {
        if (uri == null || durationMs <= 0L) return@LaunchedEffect
        withContext(Dispatchers.IO) {
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, uri)
                for (i in 0 until THUMB_COUNT) {
                    ensureActive()
                    val timeUs = (durationMs * 1000L * i / THUMB_COUNT).coerceAtLeast(0L)
                    val bm = try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                            retriever.getScaledFrameAtTime(
                                timeUs,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC,
                                THUMB_TARGET_PX,
                                THUMB_TARGET_PX,
                            )
                        } else {
                            retriever.getFrameAtTime(
                                timeUs,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                        }
                    } catch (_: Throwable) {
                        null
                    }
                    if (bm != null) thumbs[i] = bm
                }
            } catch (c: CancellationException) {
                throw c
            } catch (_: Throwable) {
                // Leave remaining slots null — UI shows placeholder colours.
            } finally {
                runCatching { retriever.release() }
            }
        }
    }
    return thumbs
}

private const val THUMB_TARGET_PX = 240

private fun placeholderColor(index: Int): Color {
    val palette = listOf(
        Color(0xFF2A1340), Color(0xFF3B1D58), Color(0xFF5B2A6D),
        Color(0xFF8A3F73), Color(0xFFB14E73), Color(0xFFD16968),
        Color(0xFFE38866), Color(0xFFE0A267), Color(0xFFC78C5A), Color(0xFF6B4A48),
    )
    return palette[index.coerceIn(0, palette.lastIndex)]
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun FilmstripPreview() {
    AndroidThemeForPreviews {
        Filmstrip(
            sourceUri = null,
            durationMs = 32_000L,
            trimStartMs = 6_000L,
            trimEndMs = 26_000L,
            playheadMs = 14_000L,
            onTrimChange = { _, _ -> },
            formatTime = { ms ->
                val totalSeconds = ms / 1000
                "%02d:%02d".format(totalSeconds / 60, totalSeconds % 60)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )
    }
}
