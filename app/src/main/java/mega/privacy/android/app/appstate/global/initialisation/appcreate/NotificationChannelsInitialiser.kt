package mega.privacy.android.app.appstate.global.initialisation.appcreate

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import mega.privacy.android.navigation.contract.initialisation.SynchronousAppCreateInitialiser
import javax.inject.Inject

/**
 * Creates all the app's notification channels.
 *
 * Synchronous: channels must exist before any code posts a notification. Recreating an existing
 * notification channel with its original values performs no operation, so it's safe to call this
 * code when starting an app.
 * Source: https://developer.android.com/develop/ui/views/notifications/channels#CreateChannel
 */
internal class NotificationChannelsInitialiser @Inject constructor(
    private val notificationManager: NotificationManagerCompat,
    private val channels: Set<@JvmSuppressWildcards NotificationChannelCompat>,
) : SynchronousAppCreateInitialiser {
    override val name = "NotificationChannelsInitialiser"

    override operator fun invoke() {
        notificationManager.createNotificationChannelsCompat(channels.toList())
    }
}
