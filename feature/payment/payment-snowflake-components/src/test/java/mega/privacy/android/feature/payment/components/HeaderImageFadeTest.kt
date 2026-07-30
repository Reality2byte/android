package mega.privacy.android.feature.payment.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderImageFadeTest {

    @get:Rule
    var composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `test that HeaderImageFade keeps its height`() {
        composeRule.setContent {
            HeaderImageFade()
        }

        composeRule.onNodeWithTag(TEST_TAG_HEADER_IMAGE_FADE).assertFadeHeight()
    }

    /**
     * A squeezed gradient reaches the page background early, leaving a band of flat colour above the
     * content.
     */
    @Test
    fun `test that HeaderImageFade keeps its height when its parent is shorter`() {
        composeRule.setContent {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(HEADER_IMAGE_FADE_HEIGHT / 2),
            ) {
                HeaderImageFade(modifier = Modifier.align(Alignment.BottomCenter))
            }
        }

        composeRule.onNodeWithTag(TEST_TAG_HEADER_IMAGE_FADE).assertFadeHeight()
    }

    private fun SemanticsNodeInteraction.assertFadeHeight() {
        val height = composeRule.density.run { fetchSemanticsNode().size.height.toDp() }
        assertThat(height.value).isWithin(1f).of(HEADER_IMAGE_FADE_HEIGHT.value)
    }
}
