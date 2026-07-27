package mega.privacy.android.feature.videoeditor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mega.android.core.ui.tokens.theme.DSTokens

/**
 * Rounded-top surface for the editor's tool deck. The rounded corners are
 * drawn by the background shape rather than a clip, so panel content that
 * floats slightly beyond the deck's top edge (e.g. the volume slider's value
 * indicator) is not cut off.
 */
@Composable
fun ToolDeckSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.background(
            color = DSTokens.colors.background.surface1,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ),
        content = content,
    )
}
