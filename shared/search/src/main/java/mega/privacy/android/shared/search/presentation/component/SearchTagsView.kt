package mega.privacy.android.shared.search.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.chip.MegaChip
import mega.android.core.ui.components.chip.SelectionChipStyle
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.values.IconColor
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR
import mega.privacy.mobile.analytics.event.SearchTagsShowAllPressedEvent
import mega.privacy.mobile.analytics.event.SearchTagsShowLessPressedEvent

/**
 * View to display node tags as chips in the pre-search state.
 *
 * Collapsed, the chips are capped at [COLLAPSED_MAX_LINES] rows; a Show all / Show less toggle
 * appears in the header only when there are more tags than fit.
 *
 * @param tags List of tags to display
 * @param onTagClicked Callback when a tag chip is clicked, with the tag string
 * @param modifier Modifier for styling
 * @param expandedByDefault Default expanded state; user toggles override it until it changes
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchTagsView(
    tags: List<String>,
    onTagClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    expandedByDefault: Boolean = false,
) {
    var isExpanded by rememberSaveable(expandedByDefault) { mutableStateOf(expandedByDefault) }
    // FlowRow doesn't place chips beyond maxLines, so an unplaced last chip means overflow
    var isLastTagPlaced by remember(tags, isExpanded) { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 40.dp)
                .padding(start = 16.dp, end = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MegaText(
                text = stringResource(sharedR.string.file_info_information_tags_label),
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
            )
            if (isExpanded || !isLastTagPlaced) {
                TextButton(
                    modifier = Modifier.height(40.dp).testTag(SEARCH_TAGS_TOGGLE_TAG),
                    onClick = {
                        val expanded = !isExpanded
                        isExpanded = expanded
                        Analytics.tracker.trackEvent(
                            if (expanded) {
                                SearchTagsShowAllPressedEvent
                            } else {
                                SearchTagsShowLessPressedEvent
                            }
                        )
                    },
                ) {
                    MegaText(
                        text = stringResource(
                            if (isExpanded) {
                                sharedR.string.search_tags_show_less
                            } else {
                                sharedR.string.search_tags_show_all
                            }
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.W400
                        ),
                    )
                    MegaIcon(
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(20.dp),
                        painter = rememberVectorPainter(
                            if (isExpanded) {
                                IconPack.Small.Thin.Outline.ChevronUp
                            } else {
                                IconPack.Small.Thin.Outline.ChevronDown
                            }
                        ),
                        contentDescription = null,
                        tint = IconColor.Primary,
                    )
                }
            }
        }
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxLines = if (isExpanded) Int.MAX_VALUE else COLLAPSED_MAX_LINES,
        ) {
            tags.forEachIndexed { index, tag ->
                MegaChip(
                    modifier = if (index == tags.lastIndex) {
                        Modifier.onPlaced { isLastTagPlaced = true }
                    } else {
                        Modifier
                    },
                    selected = false,
                    content = "#$tag",
                    style = SelectionChipStyle,
                    onClick = { onTagClicked(tag) },
                )
            }
        }
    }
}

private const val COLLAPSED_MAX_LINES = 1

internal const val SEARCH_TAGS_TOGGLE_TAG = "search_tags_view:button_toggle"

@CombinedThemePreviews
@Composable
private fun SearchTagsViewPreview() {
    AndroidThemeForPreviews {
        SearchTagsView(
            tags = listOf("marketing", "2026", "confidential"),
            onTagClicked = {},
        )
    }
}

@CombinedThemePreviews
@Composable
private fun SearchTagsViewManyTagsPreview() {
    AndroidThemeForPreviews {
        SearchTagsView(
            tags = List(30) { "tag$it" },
            onTagClicked = {},
        )
    }
}
