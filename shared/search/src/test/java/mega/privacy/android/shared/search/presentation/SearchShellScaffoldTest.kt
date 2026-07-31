package mega.privacy.android.shared.search.presentation

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import mega.android.core.ui.model.LocalizedText
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.shared.resources.R as sharedR
import mega.privacy.android.shared.search.presentation.component.FILTER_CHIPS_TAG
import mega.privacy.android.shared.search.presentation.model.SearchEmptyContent
import mega.privacy.android.shared.search.presentation.model.SearchFilterChipState
import mega.privacy.android.shared.search.presentation.model.SearchShellState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class SearchShellScaffoldTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val emptyContent = SearchEmptyContent(
        title = LocalizedText.Literal("Empty"),
        description = LocalizedText.Literal("No results"),
        image = mega.privacy.android.icon.pack.R.drawable.ic_search_02,
    )

    @Test
    fun `test that recent searches are displayed before searching when there are recent searches`() {
        setupContent(
            SearchShellState(
                isPreSearch = true,
                recentSearches = RECENT_SEARCHES,
                isRecentSearchesLoading = false,
            )
        )

        composeRule.onNodeWithTag(SEARCH_SHELL_RECENT_SEARCHES_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_SHELL_RESULTS_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that landing is displayed before searching when there are no recent searches`() {
        setupContent(
            SearchShellState(
                isPreSearch = true,
                recentSearches = emptyList(),
                isRecentSearchesLoading = false,
            )
        )

        composeRule.onNodeWithTag(SEARCH_SHELL_LANDING_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_SHELL_RESULTS_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that tags are displayed before searching when there are tags`() {
        setupContent(
            SearchShellState(
                isPreSearch = true,
                tags = TAGS,
                isRecentSearchesLoading = false,
            )
        )

        composeRule.onNodeWithTag(SEARCH_SHELL_TAGS_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_SHELL_LANDING_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that tags are expanded when there are no recent searches`() {
        setupContent(
            SearchShellState(
                isPreSearch = true,
                tags = MANY_TAGS,
                isRecentSearchesLoading = false,
            )
        )

        composeRule.onNodeWithText(getString(sharedR.string.search_tags_show_less))
            .assertIsDisplayed()
    }

    @Test
    fun `test that tags are collapsed when there are recent searches`() {
        setupContent(
            SearchShellState(
                isPreSearch = true,
                tags = MANY_TAGS,
                recentSearches = RECENT_SEARCHES,
                isRecentSearchesLoading = false,
            )
        )

        composeRule.onNodeWithText(getString(sharedR.string.search_tags_show_all))
            .assertIsDisplayed()
    }

    private fun getString(resId: Int) = composeRule.activity.getString(resId)

    @Test
    fun `test that tags section is visible when tags load after recent searches`() {
        var shellState by mutableStateOf(
            SearchShellState(
                isPreSearch = true,
                recentSearches = List(30) { "query$it" },
                isRecentSearchesLoading = false,
            )
        )
        composeRule.setContent {
            AndroidThemeForPreviews {
                SearchShellScaffold(
                    state = shellState,
                    landingContent = emptyContent,
                    emptyContent = emptyContent,
                    onSearchTextChange = {},
                    onBack = {},
                    onRecentSearchSelected = {},
                    onClearRecentSearches = {},
                    resultsContent = { ResultsPlaceholder() },
                )
            }
        }
        composeRule.onNodeWithTag(SEARCH_SHELL_TAGS_TAG).assertDoesNotExist()

        shellState = shellState.copy(tags = TAGS)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SEARCH_SHELL_TAGS_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that tags are not displayed after a search is performed`() {
        setupContent(
            SearchShellState(
                isPreSearch = false,
                tags = TAGS,
            )
        )

        composeRule.onNodeWithTag(SEARCH_SHELL_TAGS_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that onTagSelected is invoked when a tag chip is clicked`() {
        var selectedTag: String? = null
        setupContent(
            SearchShellState(
                isPreSearch = true,
                tags = TAGS,
                isRecentSearchesLoading = false,
            ),
            onTagSelected = { selectedTag = it },
        )

        composeRule.onNodeWithText("#${TAGS.first()}", useUnmergedTree = true).performClick()

        assertThat(selectedTag).isEqualTo(TAGS.first())
    }

    @Test
    fun `test that empty view is displayed when search returns no results`() {
        setupContent(
            SearchShellState(
                isPreSearch = false,
                isEmpty = true,
            )
        )

        composeRule.onNodeWithTag(SEARCH_SHELL_EMPTY_TAG).assertIsDisplayed()
        composeRule.onNodeWithTag(SEARCH_SHELL_RESULTS_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that results are displayed when search returns results`() {
        setupContent(
            SearchShellState(
                isPreSearch = false,
                isEmpty = false,
                isLoading = false,
            )
        )

        composeRule.onNodeWithTag(SEARCH_SHELL_RESULTS_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that filter chips are displayed when filters are provided`() {
        setupContent(
            SearchShellState(
                isPreSearch = false,
                filters = listOf(
                    SearchFilterChipState(
                        FILTER_ID,
                        LocalizedText.Literal("Type"),
                        isSelected = false
                    ),
                ),
            )
        )

        composeRule.onNodeWithTag(FILTER_CHIPS_TAG).assertIsDisplayed()
    }

    @Test
    fun `test that filter chips are not displayed when there are no filters`() {
        setupContent(
            SearchShellState(
                isPreSearch = false,
                filters = emptyList(),
            )
        )

        composeRule.onNodeWithTag(FILTER_CHIPS_TAG).assertIsNotDisplayed()
    }

    @Test
    fun `test that filter chips row is displayed when only a tag filter is active`() {
        setupContent(
            SearchShellState(
                isPreSearch = false,
                filters = emptyList(),
                selectedTag = "marketing",
            )
        )

        composeRule.onNodeWithTag(FILTER_CHIPS_TAG).assertIsDisplayed()
    }

    private fun setupContent(
        state: SearchShellState,
        onTagSelected: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            AndroidThemeForPreviews {
                SearchShellScaffold(
                    state = state,
                    landingContent = emptyContent,
                    emptyContent = emptyContent,
                    onSearchTextChange = {},
                    onBack = {},
                    onRecentSearchSelected = {},
                    onClearRecentSearches = {},
                    onTagSelected = onTagSelected,
                    resultsContent = { ResultsPlaceholder() },
                )
            }
        }
    }

    @Composable
    private fun ResultsPlaceholder() {
        Box(modifier = Modifier.fillMaxSize())
    }

    private companion object {
        const val FILTER_ID = "type"
        val RECENT_SEARCHES = listOf("query1", "query2")
        val TAGS = listOf("marketing", "2026")
        val MANY_TAGS = List(40) { "longtagname$it" }
    }
}
