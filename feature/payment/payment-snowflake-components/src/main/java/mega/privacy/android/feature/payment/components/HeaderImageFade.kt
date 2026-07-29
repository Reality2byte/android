package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
 * Vertical fade drawn over the bottom of the subscription header artwork so the image blends into
 * the page background instead of ending on a hard edge (DSN-3131 / DSN-3130 "Rectangle 1": a
 * 85.3dp-tall top-to-bottom gradient from transparent to the page background, reaching full opacity
 * at ~73% of its height).
 *
 * Place it over the bottom edge of the header image, e.g. as the last child of a [Box] with
 * `Modifier.align(Alignment.BottomCenter)`.
 */
@Composable
fun HeaderImageFade(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(HEADER_IMAGE_FADE_HEIGHT)
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
 * Height of the fade, matching the Figma rectangle that overlaps the bottom of the 222dp banner.
 */
val HEADER_IMAGE_FADE_HEIGHT = 86.dp

/**
 * Gradient stop where the fade becomes fully opaque, replicating the Figma fade which reaches the
 * page background colour at ~73% of its height.
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
