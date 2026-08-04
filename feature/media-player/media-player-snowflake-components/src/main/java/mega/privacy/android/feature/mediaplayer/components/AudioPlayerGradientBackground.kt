package mega.privacy.android.feature.mediaplayer.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import mega.android.core.ui.tokens.theme.DSTokens

@Composable
fun AudioPlayerGradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        modifier = modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    DSTokens.colors.brand.containerDefault,
                    DSTokens.colors.background.pageBackground,
                )
            )
        ),
        content = content,
    )
}
