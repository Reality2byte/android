package mega.privacy.android.shared.search.presentation.component

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.analytics.tracker.AnalyticsTracker
import mega.privacy.android.shared.resources.R as sharedR
import mega.privacy.mobile.analytics.event.SearchTagsShowAllPressedEvent
import mega.privacy.mobile.analytics.event.SearchTagsShowLessPressedEvent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SearchTagsViewTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val analyticsTracker: AnalyticsTracker = mock()

    @Before
    fun setUp() {
        Analytics.initialise(analyticsTracker)
    }

    @Test
    fun `test that a chip is displayed for every tag when tags fit the collapsed rows`() {
        setupComposeContent(FEW_TAGS)

        FEW_TAGS.forEach { tag ->
            composeRule.onNodeWithContentDescription("#$tag").assertIsDisplayed()
        }
    }

    @Test
    fun `test that toggle is not displayed when tags fit the collapsed rows`() {
        setupComposeContent(FEW_TAGS)

        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that show all toggle is displayed when tags overflow the collapsed rows`() {
        setupComposeContent(MANY_TAGS)

        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).assertIsDisplayed()
        composeRule.onNodeWithText(showAllText()).assertIsDisplayed()
    }

    @Test
    fun `test that last tag is displayed when show all is clicked`() {
        setupComposeContent(MANY_TAGS)

        composeRule.onNodeWithContentDescription("#${MANY_TAGS.last()}")
            .assertIsNotDisplayed()

        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).performClick()

        composeRule.onNodeWithContentDescription("#${MANY_TAGS.last()}").assertIsDisplayed()
        composeRule.onNodeWithText(showLessText()).assertIsDisplayed()
    }

    @Test
    fun `test that tags collapse when show less is clicked`() {
        setupComposeContent(MANY_TAGS)

        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).performClick()
        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).performClick()

        composeRule.onNodeWithContentDescription("#${MANY_TAGS.last()}")
            .assertIsNotDisplayed()
        composeRule.onNodeWithText(showAllText()).assertIsDisplayed()
    }

    @Test
    fun `test that tags are expanded when expandedByDefault is true`() {
        setupComposeContent(MANY_TAGS, expandedByDefault = true)

        composeRule.onNodeWithContentDescription("#${MANY_TAGS.last()}").assertIsDisplayed()
        composeRule.onNodeWithText(showLessText()).assertIsDisplayed()
    }

    @Test
    fun `test that toggling the section tracks the show all and show less events`() {
        setupComposeContent(MANY_TAGS)

        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).performClick()
        composeRule.onNodeWithTag(SEARCH_TAGS_TOGGLE_TAG).performClick()

        inOrder(analyticsTracker) {
            verify(analyticsTracker).trackEvent(SearchTagsShowAllPressedEvent)
            verify(analyticsTracker).trackEvent(SearchTagsShowLessPressedEvent)
        }
    }

    @Test
    fun `test that onTagClicked is invoked with the tag when a chip is clicked`() {
        var clickedTag: String? = null
        setupComposeContent(FEW_TAGS, onTagClicked = { clickedTag = it })

        composeRule.onNodeWithContentDescription("#${FEW_TAGS.first()}").performClick()

        assertThat(clickedTag).isEqualTo(FEW_TAGS.first())
    }

    private fun setupComposeContent(
        tags: List<String>,
        onTagClicked: (String) -> Unit = {},
        expandedByDefault: Boolean = false,
    ) {
        composeRule.setContent {
            AndroidThemeForPreviews {
                SearchTagsView(
                    tags = tags,
                    onTagClicked = onTagClicked,
                    expandedByDefault = expandedByDefault,
                )
            }
        }
    }

    private fun showAllText() =
        composeRule.activity.getString(sharedR.string.search_tags_show_all)

    private fun showLessText() =
        composeRule.activity.getString(sharedR.string.search_tags_show_less)

    private companion object {
        val FEW_TAGS = listOf("marketing", "2026")
        val MANY_TAGS = List(40) { "longtagname$it" }
    }
}
