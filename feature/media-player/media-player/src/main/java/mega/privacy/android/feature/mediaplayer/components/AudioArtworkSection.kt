package mega.privacy.android.feature.mediaplayer.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import mega.privacy.android.domain.entity.node.thumbnail.ThumbnailData
import mega.privacy.android.icon.pack.R as iconPackR

@Composable
internal fun AudioArtworkSection(
    artworkUri: String?,
    thumbnailData: ThumbnailData?,
    modifier: Modifier = Modifier,
) {
    var imageLoaded by remember(artworkUri, thumbnailData) { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        if (artworkUri != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artworkUri)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { imageLoaded = true },
                onError = { imageLoaded = false },
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
            )
        } else if (thumbnailData != null) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailData)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                onSuccess = { imageLoaded = true },
                onError = { imageLoaded = false },
                modifier = Modifier
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }

        if (!imageLoaded) {
            Image(
                painter = painterResource(iconPackR.drawable.ic_audio_medium_solid),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}
