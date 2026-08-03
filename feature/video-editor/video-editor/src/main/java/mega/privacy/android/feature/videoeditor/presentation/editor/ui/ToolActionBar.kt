package mega.privacy.android.feature.videoeditor.presentation.editor.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.surface.BoxSurface
import mega.android.core.ui.components.surface.SurfaceColor
import mega.android.core.ui.theme.AndroidTheme
import mega.android.core.ui.theme.AppTheme
import mega.android.core.ui.theme.values.TextColor
import mega.privacy.android.feature.videoeditor.components.CancelButton
import mega.privacy.android.feature.videoeditor.components.SaveButton
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Cancel / label / Apply bar shown beneath the active tool's panel. The surface
 * fills behind the navigation-bar inset; the inner row carries the inset
 * padding.
 */
@Composable
fun ToolActionBar(
    toolLabel: String,
    onCancel: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxSurface(
        surfaceColor = SurfaceColor.PageBackground,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CancelButton(
                onClick = onCancel,
                contentDescription = stringResource(sharedR.string.general_dialog_cancel_button),
            )
            MegaText(
                text = toolLabel,
                style = AppTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.W400
                ),
                textColor = TextColor.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            SaveButton(
                onClick = onApply,
                contentDescription = stringResource(sharedR.string.video_editor_apply_button),
            )
        }
    }
}



@Preview(showBackground = true, widthDp = 400)
@Composable
private fun ToolActionBarPreview() {
    AndroidTheme(
        isDark = true
    ) {
        ToolActionBar(
            toolLabel = "Crop",
            onApply = {},
            onCancel = {}
        )
    }
}
