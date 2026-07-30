package mega.privacy.android.feature.clouddrive.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.entity.Feature
import mega.privacy.android.domain.entity.navigation.Flagged
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.feature.clouddrive.presentation.shares.shares
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.MainNavItemBadge
import mega.privacy.android.navigation.contract.NavigationHandler
import mega.privacy.android.navigation.contract.NavigationUiController
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.contract.TransferHandler
import mega.privacy.android.navigation.contract.navkey.MainNavItemNavKey
import mega.privacy.android.navigation.destination.SharesNavKey
import mega.privacy.android.shared.resources.R as sharedR
import mega.privacy.mobile.analytics.core.event.identifier.NavigationEventIdentifier

/**
 * Main navigation item for the Shared items section.
 *
 * Has no default slot; it only appears in the navigation bar when the user adds it via the
 * navigation items preference.
 */
class SharesNavItem : MainNavItem, Flagged {
    override val id: String = "shares"
    override val feature: Feature = ApiFeatures.CustomisableBottomNavigation
    override val destination: MainNavItemNavKey = SharesNavKey
    override val screen: EntryProviderScope<NavKey>.(NavigationHandler, NavigationUiController, TransferHandler) -> Unit =
        { navigationHandler, _, transferHandler ->
            shares(navigationHandler, transferHandler)
        }

    override val icon: ImageVector = IconPack.Medium.Thin.Outline.FolderUsers
    override val selectedIcon: ImageVector? = null
    override val badge: Flow<MainNavItemBadge?>? = null

    @StringRes
    override val label: Int = sharedR.string.video_section_videos_location_option_shared_items
    override val preferredSlot: PreferredSlot = PreferredSlot.None
    override val availableOffline: Boolean = false
    override val analyticsEventIdentifier: NavigationEventIdentifier =
        SharesNavigationIdentifier
}

/**
 * Analytics identifier for the Shared items bottom navigation item.
 */
object SharesNavigationIdentifier : NavigationEventIdentifier {
    override val navigationElementType: String?
        get() = "Bottom"
    override val destination: String?
        get() = "Shared Items"
    override val eventName: String
        get() = "SharedItemsBottomNavigationItem"
    override val uniqueIdentifier: Int
        get() = -1
}
