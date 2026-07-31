package mega.privacy.android.shared.search.presentation.component

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import mega.android.core.ui.model.LocalizedText
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.shared.search.presentation.model.SearchFilterChipState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SearchFilterChipsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val filters = listOf(
        SearchFilterChipState(TYPE_FILTER_ID, LocalizedText.Literal("Type"), isSelected = false),
        SearchFilterChipState(
            MODIFIED_FILTER_ID,
            LocalizedText.Literal("Last modified"),
            isSelected = true
        ),
        SearchFilterChipState(
            ADDED_FILTER_ID,
            LocalizedText.Literal("Date added"),
            isSelected = false
        ),
    )

    @Test
    fun `test that a chip is displayed for every filter`() {
        setupComposeContent()

        filters.forEach { filter ->
            composeRule.onNodeWithTag("${FILTER_CHIP_TAG}_${filter.id}").assertIsDisplayed()
        }
    }

    @Test
    fun `test that onFilterClicked is called with the chip id when a chip is clicked`() {
        val onFilterClicked = mock<(String) -> Unit>()
        setupComposeContent(onFilterClicked = onFilterClicked)

        composeRule.onNodeWithTag("${FILTER_CHIP_TAG}_$MODIFIED_FILTER_ID").performClick()

        verify(onFilterClicked).invoke(MODIFIED_FILTER_ID)
    }

    @Test
    fun `test that tag chip is displayed when a tag is selected`() {
        setupComposeContent(selectedTag = SELECTED_TAG)

        composeRule.onNodeWithTag(TAG_FILTER_CHIP_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tag chip is not displayed when no tag is selected`() {
        setupComposeContent()

        composeRule.onNodeWithTag(TAG_FILTER_CHIP_TAG).assertDoesNotExist()
    }

    @Test
    fun `test that onClearTagClicked is called when the tag chip is clicked`() {
        val onClearTagClicked = mock<() -> Unit>()
        setupComposeContent(
            selectedTag = SELECTED_TAG,
            onClearTagClicked = onClearTagClicked,
        )

        composeRule.onNodeWithTag(TAG_FILTER_CHIP_TAG).performClick()

        verify(onClearTagClicked).invoke()
    }

    private fun setupComposeContent(
        onFilterClicked: (String) -> Unit = {},
        selectedTag: String? = null,
        onClearTagClicked: () -> Unit = {},
    ) {
        composeRule.setContent {
            AndroidThemeForPreviews {
                SearchFilterChips(
                    filters = filters,
                    onFilterClicked = onFilterClicked,
                    selectedTag = selectedTag,
                    onClearTagClicked = onClearTagClicked,
                )
            }
        }
    }

    private companion object {
        const val TYPE_FILTER_ID = "type"
        const val MODIFIED_FILTER_ID = "modified"
        const val ADDED_FILTER_ID = "added"
        const val SELECTED_TAG = "marketing"
    }
}
