package mega.privacy.android.feature.videoeditor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.AppTheme
import mega.android.core.ui.theme.values.IconColor
import mega.android.core.ui.theme.values.TextColor
import mega.android.core.ui.tokens.theme.DSTokens
import mega.privacy.android.icon.pack.IconPack

/**
 * Aspect-ratio preset chip for the Crop tool: a format icon + label that
 * highlights with the brand colour when selected. Decoupled from any preset
 * type — the caller supplies primitive props.
 */
@Composable
fun AspectRatioChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background =
        if (selected) DSTokens.colors.brand.containerDefault else DSTokens.colors.neutral.containerDefault
    val borderColor = if (selected) DSTokens.colors.border.brand else Color.Transparent
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .width(96.dp)
            .height(64.dp),
    ) {
        MegaIcon(
            imageVector = icon,
            tint = if (selected) IconColor.Primary else IconColor.Secondary,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
        )
        MegaText(
            text = label,
            style = if (selected) {
                AppTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
            } else {
                AppTheme.typography.labelMedium
            },
            textColor = if (selected) TextColor.Primary else TextColor.Secondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AspectRatioChipPreview() {
    AndroidThemeForPreviews {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AspectRatioChip(
                label = "Free",
                icon = IconPack.Medium.Thin.Outline.FormatFreeform,
                selected = true,
                onClick = {},
            )
            AspectRatioChip(
                label = "16:9",
                icon = IconPack.Medium.Thin.Outline.Format16x9,
                selected = false,
                onClick = {},
            )
        }
    }
}
