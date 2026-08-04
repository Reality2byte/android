package mega.privacy.android.feature.mediaplayer.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.button.SecondaryFilledButton
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.theme.values.IconColor
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

@Composable
internal fun AudioPlayerPrimaryActions(
    isPodcastMode: Boolean,
    onToggleMode: () -> Unit,
    onPlaylistClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        SecondaryFilledButton(
            text = stringResource(
                if (isPodcastMode) sharedR.string.audio_player_podcast_mode_label
                else sharedR.string.audio_player_music_mode_label
            ),
            onClick = onToggleMode,
            modifier = Modifier.wrapContentWidth(),
        )
        IconButton(
            onClick = onPlaylistClicked,
            modifier = Modifier.align(Alignment.CenterEnd),
        ) {
            MegaIcon(
                imageVector = IconPack.Medium.Thin.Outline.Playlist,
                tint = IconColor.Primary,
                contentDescription = "Playlist",
            )
        }
    }
}
