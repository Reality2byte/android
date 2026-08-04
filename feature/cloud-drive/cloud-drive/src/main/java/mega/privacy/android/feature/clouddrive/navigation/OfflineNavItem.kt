package mega.privacy.android.feature.clouddrive.navigation

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.Flow
import mega.privacy.android.domain.entity.Feature
import mega.privacy.android.domain.entity.navigation.Flagged
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.feature.clouddrive.presentation.offline.offlineScreen
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.MainNavItemBadge
import mega.privacy.android.navigation.contract.NavigationHandler
import mega.privacy.android.navigation.contract.NavigationUiController
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.contract.TransferHandler
import mega.privacy.android.navigation.contract.navkey.MainNavItemNavKey
import mega.privacy.android.navigation.destination.OfflineInfoNavKey
import mega.privacy.android.navigation.destination.OfflineNavKey
import mega.privacy.android.navigation.destination.TransfersNavKey
import mega.privacy.android.shared.resources.R as sharedR
import mega.privacy.mobile.analytics.core.event.identifier.NavigationEventIdentifier
import mega.privacy.mobile.analytics.event.OfflineFilesBottomNavigationItemEvent

/**
 * Main navigation item for the Offline files section.
 *
 * Has no default slot; it only appears in the navigation bar when the user adds it via the
 * navigation items preference.
 */
class OfflineNavItem : MainNavItem, Flagged {
    override val id: String = "offline"
    override val feature: Feature = ApiFeatures.CustomisableBottomNavigation
    override val destination: MainNavItemNavKey = OfflineNavKey()
    override val screen: EntryProviderScope<NavKey>.(NavigationHandler, NavigationUiController, TransferHandler) -> Unit =
        { navigationHandler, _, transferHandler ->
            offlineScreen(
                navigationHandler = navigationHandler,
                onBack = navigationHandler::back,
                onNavigateToFolder = { parentId, name ->
                    navigationHandler.navigate(
                        OfflineNavKey(
                            nodeId = parentId,
                            title = name,
                        )
                    )
                },
                onNavigateToTransfers = {
                    navigationHandler.navigate(TransfersNavKey())
                },
                onTransfer = transferHandler::setTransferEvent,
                openFileInformation = { handle ->
                    navigationHandler.navigate(OfflineInfoNavKey(handle = handle))
                },
            )
        }

    override val icon: ImageVector = IconPack.Medium.Thin.Outline.ArrowDownCircle
    override val selectedIcon: ImageVector? = null
    override val badge: Flow<MainNavItemBadge?>? = null

    @StringRes
    override val label: Int = sharedR.string.general_section_offline_files
    override val preferredSlot: PreferredSlot = PreferredSlot.None
    override val availableOffline: Boolean = true
    override val analyticsEventIdentifier: NavigationEventIdentifier =
        OfflineFilesBottomNavigationItemEvent
}
