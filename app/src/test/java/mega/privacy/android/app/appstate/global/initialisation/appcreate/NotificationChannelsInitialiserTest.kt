package mega.privacy.android.app.appstate.global.initialisation.appcreate

import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class NotificationChannelsInitialiserTest {

    private val notificationManager = mock<NotificationManagerCompat>()
    private val channels = setOf(
        mock<NotificationChannelCompat>(),
        mock<NotificationChannelCompat>(),
    )
    private val underTest = NotificationChannelsInitialiser(
        notificationManager = notificationManager,
        channels = channels,
    )

    @Test
    fun `test that invoke creates all injected notification channels`() {
        underTest()

        verify(notificationManager).createNotificationChannelsCompat(channels.toList())
    }
}
