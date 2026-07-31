package mega.privacy.android.app.presentation.transfers.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow
import mega.privacy.android.app.presentation.transfers.view.navigation.transfersScreen3
import mega.privacy.android.domain.entity.Feature
import mega.privacy.android.domain.entity.navigation.Flagged
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.MainNavItemBadge
import mega.privacy.android.navigation.contract.NavigationHandler
import mega.privacy.android.navigation.contract.NavigationUiController
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.contract.TransferHandler
import mega.privacy.android.navigation.contract.navkey.MainNavItemNavKey
import mega.privacy.android.navigation.destination.TransfersNavKey
import mega.privacy.android.navigation.destination.UpgradeAccountNavKey
import mega.privacy.android.shared.resources.R as sharedR
import mega.privacy.mobile.analytics.core.event.identifier.NavigationEventIdentifier

/**
 * Main navigation item for the Transfers section.
 *
 * Has no default slot; it only appears in the navigation bar when the user adds it via the
 * navigation items preference. The Transfers screen also stays registered in the outer
 * navigation graph by [TransfersFeatureDestination], keeping it reachable when this item is
 * disabled.
 */
class TransfersNavItem : MainNavItem, Flagged {
    override val id: String = "transfers"
    override val destination: MainNavItemNavKey = TransfersNavKey()
    override val screen: EntryProviderScope<NavKey>.(NavigationHandler, NavigationUiController, TransferHandler) -> Unit =
        { navigationHandler, _, _ ->
            transfersScreen3(
                onBackPress = navigationHandler::back,
                onNavigateToUpgradeAccount = { navigationHandler.navigate(UpgradeAccountNavKey()) },
                navigationHandler = navigationHandler,
            )
        }
    override val icon: ImageVector = IconPack.Medium.Thin.Outline.ArrowsUpDownCircle
    override val selectedIcon: ImageVector? = null
    override val badge: Flow<MainNavItemBadge?>? = null
    override val label: Int = sharedR.string.general_section_transfers
    override val preferredSlot: PreferredSlot = PreferredSlot.None
    override val availableOffline: Boolean = true
    override val analyticsEventIdentifier: NavigationEventIdentifier =
        TransfersNavigationIdentifier
    override val feature: Feature = ApiFeatures.CustomisableBottomNavigation
}

object TransfersNavigationIdentifier : NavigationEventIdentifier {
    override val navigationElementType: String?
        get() = "Bottom"
    override val destination: String?
        get() = "Transfers"
    override val eventName: String
        get() = "TransfersBottomNavigationItem"
    override val uniqueIdentifier: Int
        get() = -1
}
