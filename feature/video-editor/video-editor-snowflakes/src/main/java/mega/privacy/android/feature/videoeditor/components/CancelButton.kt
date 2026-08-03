package mega.privacy.android.feature.videoeditor.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.button.BaseIconButton
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.tokens.theme.DSTokens
import mega.privacy.android.icon.pack.IconPack

/**
 * [SaveButton]'s low-emphasis counterpart: a secondary-filled X button
 * dismissing a tool edit.
 */
@Composable
fun CancelButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    BaseIconButton(
        modifier = modifier.size(width = 56.dp, height = 48.dp),
        icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.X),
        shape = DSTokens.shapes.medium,
        enabled = enabled,
        containerColorDefault = DSTokens.colors.button.secondary,
        containerColorPressed = DSTokens.colors.button.secondaryPressed,
        containerColorDisabled = DSTokens.colors.button.disabled,
        iconColorDefault = DSTokens.colors.icon.primary,
        iconColorDisabled = DSTokens.colors.icon.onColorDisabled,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun CancelButtonPreview() {
    AndroidThemeForPreviews {
        CancelButton(onClick = {})
    }
}
