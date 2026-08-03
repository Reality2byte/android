package mega.privacy.android.app.presentation.settings.customisenavigation.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector
import de.palm.composestateevents.StateEvent

/**
 * UI state for the Customise navigation settings screen.
 */
@Stable
sealed interface CustomiseNavigationUiState {

    /**
     * State while the navigation items and preference are being loaded.
     */
    data object Loading : CustomiseNavigationUiState

    /**
     * Loaded state.
     *
     * @property baseArrangement the persisted selection reconciled against the enabled items,
     * falling back to the default bar when no persisted id matches. Excludes the pinned Menu item.
     * @property availableItems the enabled items not part of [baseArrangement], excluding the
     * pinned Menu item
     * @property menuItem the pinned Menu item, always shown last and never persisted
     * @property defaultArrangementIds the ids of the default bar items, used to reset the
     * pending selection
     * @property savedEvent one-shot event fired when the selection has been persisted
     */
    data class Data(
        val baseArrangement: List<NavigationItemUiModel>,
        val availableItems: List<NavigationItemUiModel>,
        val menuItem: NavigationItemUiModel,
        val defaultArrangementIds: List<String>,
        val savedEvent: StateEvent,
    ) : CustomiseNavigationUiState
}

/**
 * Display data of a main navigation item.
 *
 * @property id the stable id of the item, used to persist the selection
 * @property label the label string resource
 * @property icon the item icon
 */
data class NavigationItemUiModel(
    val id: String,
    @StringRes val label: Int,
    val icon: ImageVector,
)
