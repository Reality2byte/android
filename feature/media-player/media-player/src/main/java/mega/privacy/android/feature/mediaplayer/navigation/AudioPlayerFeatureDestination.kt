package mega.privacy.android.feature.mediaplayer.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import mega.privacy.android.navigation.contract.FeatureDestination
import mega.privacy.android.navigation.contract.NavigationHandler
import mega.privacy.android.navigation.contract.TransferHandler

/**
 * [FeatureDestination] that contributes the Compose audio player route
 * ([AudioPlayerScreenNavKey]) to the single-activity navigation graph.
 */
class AudioPlayerFeatureDestination(
    private val launchSourceHolder: AudioPlayerLaunchSourceHolder,
) : FeatureDestination {
    override val navigationGraph: EntryProviderScope<NavKey>.(NavigationHandler, TransferHandler) -> Unit =
        { navigationHandler, transferHandler ->
            audioPlayerScreen(
                navigationHandler = navigationHandler,
                launchSourceHolder = launchSourceHolder,
                onTransfer = transferHandler::setTransferEvent,
            )
        }
}
