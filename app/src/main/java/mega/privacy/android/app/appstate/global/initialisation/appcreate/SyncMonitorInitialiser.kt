package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.launch
import mega.privacy.android.feature.sync.domain.usecase.notifcation.MonitorSyncNotificationsUseCase
import mega.privacy.android.feature.sync.domain.usecase.notifcation.SetSyncNotificationShownUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.PauseResumeSyncsBasedOnBatteryAndWiFiUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.option.MonitorShouldSyncUseCase
import mega.privacy.android.feature.sync.ui.notification.SyncNotificationManager
import mega.privacy.android.navigation.contract.initialisation.AsyncAppCreateInitialiser
import timber.log.Timber
import javax.inject.Inject

/**
 * Monitors sync state to pause or resume syncs and to surface sync notifications.
 *
 * Async: both collectors already ran fire-and-forget in the application scope at androidx.startup
 * provider time; nothing at boot waits on them.
 */
internal class SyncMonitorInitialiser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val monitorShouldSyncUseCase: MonitorShouldSyncUseCase,
    private val monitorSyncNotificationsUseCase: MonitorSyncNotificationsUseCase,
    private val pauseResumeSyncsBasedOnBatteryAndWiFiUseCase: PauseResumeSyncsBasedOnBatteryAndWiFiUseCase,
    private val setSyncNotificationShownUseCase: SetSyncNotificationShownUseCase,
    private val syncNotificationManager: SyncNotificationManager,
) : AsyncAppCreateInitialiser {
    override val name = "SyncMonitorInitialiser"

    override suspend operator fun invoke(): Unit = coroutineScope {
        launch {
            monitorShouldSyncUseCase()
                .distinctUntilChanged()
                .retry {
                    Timber.e("SyncMonitorInitialiser: Error monitoring sync state: $it")
                    true
                }
                .collect { shouldSync ->
                    Timber.d("SyncMonitorInitialiser: Should sync: $shouldSync")
                    pauseResumeSyncsBasedOnBatteryAndWiFiUseCase(shouldSync)
                }
        }

        launch {
            monitorSyncNotificationsUseCase()
                .retry {
                    Timber.e("SyncMonitorInitialiser: Error monitoring notifications: $it")
                    true
                }
                .collect { notification ->
                    notification?.let {
                        runCatching {
                            if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                if (!syncNotificationManager.isSyncNotificationDisplayed()) {
                                    val notificationId =
                                        syncNotificationManager.show(context, notification)
                                    setSyncNotificationShownUseCase(
                                        syncNotificationMessage = notification,
                                        notificationId = notificationId,
                                    )
                                }
                            }
                        }
                    }
                }
        }
        Timber.d("SyncMonitorInitialiser: Started monitoring")
    }
}
