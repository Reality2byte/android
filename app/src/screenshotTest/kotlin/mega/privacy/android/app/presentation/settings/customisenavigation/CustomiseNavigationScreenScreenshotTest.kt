package mega.privacy.android.app.presentation.settings.customisenavigation

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import de.palm.composestateevents.consumed
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.app.presentation.settings.customisenavigation.model.CustomiseNavigationUiState
import mega.privacy.android.app.presentation.settings.customisenavigation.model.NavigationItemUiModel
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Baselines for [CustomiseNavigationScreen]: the default arrangement and the full
 * selection with the counter in its error state.
 */
class CustomiseNavigationScreenScreenshotTest {

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun CustomiseNavigationScreenDefaultArrangement() {
        AndroidThemeForPreviews {
            CustomiseNavigationScreen(
                state = data(
                    selected = listOf(home, drive, media),
                    available = listOf(chat, shares),
                ),
                onBackPressed = {},
                onSave = {},
            )
        }
    }

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun CustomiseNavigationScreenFullSelection() {
        AndroidThemeForPreviews {
            CustomiseNavigationScreen(
                state = data(
                    selected = listOf(chat, drive, media, home),
                    available = listOf(shares),
                ),
                onBackPressed = {},
                onSave = {},
            )
        }
    }

    private fun data(
        selected: List<NavigationItemUiModel>,
        available: List<NavigationItemUiModel>,
    ) = CustomiseNavigationUiState.Data(
        baseArrangement = selected,
        availableItems = available,
        menuItem = menu,
        defaultArrangementIds = listOf("home", "drive", "media"),
        savedEvent = consumed,
    )

    private val home = NavigationItemUiModel(
        id = "home",
        label = sharedR.string.general_section_home,
        icon = IconPack.Medium.Thin.Outline.Home,
    )
    private val drive = NavigationItemUiModel(
        id = "drive",
        label = sharedR.string.general_drive,
        icon = IconPack.Medium.Thin.Outline.Folder,
    )
    private val media = NavigationItemUiModel(
        id = "media",
        label = sharedR.string.media_feature_title,
        icon = IconPack.Medium.Thin.Outline.Image01,
    )
    private val chat = NavigationItemUiModel(
        id = "chat",
        label = sharedR.string.general_chat,
        icon = IconPack.Medium.Thin.Outline.MessageChatCircle,
    )
    private val shares = NavigationItemUiModel(
        id = "shares",
        label = sharedR.string.video_section_videos_location_option_shared_items,
        icon = IconPack.Medium.Thin.Outline.FolderUsers,
    )
    private val menu = NavigationItemUiModel(
        id = "menu",
        label = sharedR.string.general_menu,
        icon = IconPack.Medium.Thin.Outline.Menu01,
    )
}
