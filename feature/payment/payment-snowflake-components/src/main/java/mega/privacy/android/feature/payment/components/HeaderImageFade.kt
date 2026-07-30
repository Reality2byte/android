package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidTheme
import mega.android.core.ui.tokens.theme.DSTokens

/**
 * Fade blending the bottom of the subscription header artwork into the page background
 * (DSN-3131 / DSN-3130 "Rectangle 1"). Align it to the bottom edge of the header image.
 */
@Composable
fun HeaderImageFade(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // required, so a shorter parent cannot squeeze the gradient into reaching the
            // background colour early
            .requiredHeight(HEADER_IMAGE_FADE_HEIGHT)
            .background(
                Brush.verticalGradient(
                    0f to Color.Transparent,
                    HEADER_IMAGE_FADE_SOLID_STOP to DSTokens.colors.background.pageBackground,
                )
            )
            .testTag(TEST_TAG_HEADER_IMAGE_FADE),
    )
}

/**
 * Height of the fade in the Figma banner.
 */
val HEADER_IMAGE_FADE_HEIGHT = 86.dp

/**
 * Where the Figma gradient reaches the page background colour.
 */
private const val HEADER_IMAGE_FADE_SOLID_STOP = 0.73f

/**
 * Tag for the header image fade
 */
const val TEST_TAG_HEADER_IMAGE_FADE = "header_image_fade"

@CombinedThemePreviews
@Composable
private fun HeaderImageFadePreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        HeaderImageFade()
    }
}
