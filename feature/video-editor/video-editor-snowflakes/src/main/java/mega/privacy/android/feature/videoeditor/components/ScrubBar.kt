package mega.privacy.android.feature.videoeditor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.tokens.theme.DSTokens

/**
 * Tap/drag scrub bar drawn over the video preview. The track uses fixed light
 * colours against the (always dark) video frame; the playhead handle uses the
 * brand token, which is why this lives in the snowflakes module.
 */
@Composable
fun ScrubBar(
    progress: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var widthPx by remember { mutableStateOf(0) }
    val onSeekState = rememberUpdatedState(onSeek)
    Box(
        modifier = modifier
            .height(20.dp)
            .onSizeChanged { widthPx = it.width }
            .pointerInput(Unit) {
                detectTapGestures(onTap = { offset ->
                    if (widthPx > 0) onSeekState.value((offset.x / widthPx).coerceIn(0f, 1f))
                })
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        if (widthPx > 0) {
                            onSeekState.value((change.position.x / widthPx).coerceIn(0f, 1f))
                            change.consume()
                        }
                    },
                )
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Color.White.copy(alpha = 0.25f)),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(3.dp)
                .background(Color.White),
        )
        val handleOffsetDp = with(LocalDensity.current) {
            (widthPx * progress.coerceIn(0f, 1f)).toDp() - 7.dp
        }
        Box(
            modifier = Modifier
                .padding(start = handleOffsetDp.coerceAtLeast(0.dp))
                .size(14.dp)
                .clip(CircleShape)
                .background(DSTokens.colors.brand.default),
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000, widthDp = 320)
@Composable
private fun ScrubBarPreview() {
    AndroidThemeForPreviews {
        ScrubBar(
            progress = 0.4f,
            onSeek = {},
        )
    }
}
