package mega.privacy.android.feature.videoeditor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.values.IconColor
import mega.android.core.ui.tokens.theme.DSTokens
import mega.privacy.android.icon.pack.IconPack

/**
 * Mute toggle button for the Volume tool. Shows the action it performs —
 * speaker-off while audible ("mute"), speaker-max in brand colour while muted
 * ("unmute").
 *
 * @param muted whether the volume is currently muted
 * @param onClick called when the button is tapped
 * @param contentDescription accessibility label describing the button's action
 * @param modifier applied to the button
 */
@Composable
fun MuteButton(
    muted: Boolean,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DSTokens.colors.button.secondary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        MegaIcon(
            imageVector = if (muted) {
                IconPack.Medium.Thin.Outline.VolumeMax
            } else {
                IconPack.Medium.Thin.Outline.VolumeOff
            },
            tint = IconColor.Primary,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MuteButtonPreview() {
    AndroidThemeForPreviews {
        Row {
            MuteButton(muted = false, onClick = {}, contentDescription = "Mute")
            MuteButton(muted = true, onClick = {}, contentDescription = "Unmute")
        }
    }
}
