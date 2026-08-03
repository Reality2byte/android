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
 * Primary-filled check button (56x48dp, medium corner) confirming a tool edit.
 * Core-ui only ships square icon buttons, so this geometry needs direct token
 * access, which is why it lives in the snowflakes module.
 */
@Composable
fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    BaseIconButton(
        modifier = modifier.size(width = 56.dp, height = 48.dp),
        icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.Check),
        shape = DSTokens.shapes.medium,
        enabled = enabled,
        containerColorDefault = DSTokens.colors.button.primary,
        containerColorPressed = DSTokens.colors.button.primaryPressed,
        containerColorDisabled = DSTokens.colors.button.disabled,
        iconColorDefault = DSTokens.colors.icon.inverseAccent,
        iconColorDisabled = DSTokens.colors.icon.onColorDisabled,
        contentDescription = contentDescription,
        onClick = onClick,
    )
}

@Preview(showBackground = true)
@Composable
private fun SaveButtonPreview() {
    AndroidThemeForPreviews {
        SaveButton(onClick = {})
    }
}
