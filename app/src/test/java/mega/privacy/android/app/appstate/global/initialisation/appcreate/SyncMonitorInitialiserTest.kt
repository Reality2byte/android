package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.content.Context
import android.content.pm.PackageManager
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.feature.sync.domain.entity.NotificationDetails
import mega.privacy.android.feature.sync.domain.entity.SyncNotificationMessage
import mega.privacy.android.feature.sync.domain.entity.SyncNotificationType
import mega.privacy.android.feature.sync.domain.usecase.notifcation.MonitorSyncNotificationsUseCase
import mega.privacy.android.feature.sync.domain.usecase.notifcation.SetSyncNotificationShownUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.PauseResumeSyncsBasedOnBatteryAndWiFiUseCase
import mega.privacy.android.feature.sync.domain.usecase.sync.option.MonitorShouldSyncUseCase
import mega.privacy.android.feature.sync.ui.notification.SyncNotificationManager
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SyncMonitorInitialiserTest {
    private lateinit var underTest: SyncMonitorInitialiser

    private val context = mock<Context>()
    private val monitorShouldSyncUseCase = mock<MonitorShouldSyncUseCase>()
    private val monitorSyncNotificationsUseCase = mock<MonitorSyncNotificationsUseCase>()
    private val pauseResumeSyncsBasedOnBatteryAndWiFiUseCase =
        mock<PauseResumeSyncsBasedOnBatteryAndWiFiUseCase>()
    private val setSyncNotificationShownUseCase = mock<SetSyncNotificationShownUseCase>()
    private val syncNotificationManager = mock<SyncNotificationManager>()

    private val notificationMessage = SyncNotificationMessage(
        title = 1,
        text = 2,
        syncNotificationType = SyncNotificationType.STALLED_ISSUE,
        notificationDetails = NotificationDetails(path = null, errorCode = null),
    )

    @BeforeAll
    fun setUp() {
        underTest = SyncMonitorInitialiser(
            context = context,
            monitorShouldSyncUseCase = monitorShouldSyncUseCase,
            monitorSyncNotificationsUseCase = monitorSyncNotificationsUseCase,
            pauseResumeSyncsBasedOnBatteryAndWiFiUseCase = pauseResumeSyncsBasedOnBatteryAndWiFiUseCase,
            setSyncNotificationShownUseCase = setSyncNotificationShownUseCase,
            syncNotificationManager = syncNotificationManager,
        )
    }

    @BeforeEach
    fun resetMocks() {
        reset(
            context,
            monitorShouldSyncUseCase,
            monitorSyncNotificationsUseCase,
            pauseResumeSyncsBasedOnBatteryAndWiFiUseCase,
            setSyncNotificationShownUseCase,
            syncNotificationManager,
        )
        monitorShouldSyncUseCase.stub { on { invoke() }.thenReturn(emptyFlow()) }
        monitorSyncNotificationsUseCase.stub { on { invoke() }.thenReturn(emptyFlow()) }
        context.stub {
            on { checkPermission(any(), any(), any()) }
                .thenReturn(PackageManager.PERMISSION_GRANTED)
        }
    }

    @Test
    fun `test that invoke pauses or resumes syncs for each distinct should sync value`() =
        runTest {
            monitorShouldSyncUseCase.stub { on { invoke() }.thenReturn(flowOf(false, true)) }

            underTest()

            verify(pauseResumeSyncsBasedOnBatteryAndWiFiUseCase).invoke(false)
            verify(pauseResumeSyncsBasedOnBatteryAndWiFiUseCase).invoke(true)
        }

    @Test
    fun `test that invoke shows the sync notification when it is not already displayed`() =
        runTest {
            monitorSyncNotificationsUseCase.stub {
                on { invoke() }.thenReturn(flowOf(notificationMessage))
            }
            syncNotificationManager.stub {
                on { isSyncNotificationDisplayed() }.thenReturn(false)
                onBlocking { show(context, notificationMessage) }.thenReturn(1234)
            }

            underTest()

            verify(syncNotificationManager).show(context, notificationMessage)
            verify(setSyncNotificationShownUseCase).invoke(
                syncNotificationMessage = notificationMessage,
                notificationId = 1234,
            )
        }

    @Test
    fun `test that invoke does not show the sync notification when it is already displayed`() =
        runTest {
            monitorSyncNotificationsUseCase.stub {
                on { invoke() }.thenReturn(flowOf(notificationMessage))
            }
            syncNotificationManager.stub {
                on { isSyncNotificationDisplayed() }.thenReturn(true)
            }

            underTest()

            verify(syncNotificationManager, never()).show(any(), any())
            verifyNoInteractions(setSyncNotificationShownUseCase)
        }

    @Test
    fun `test that invoke does not show the sync notification when the permission is not granted`() =
        runTest {
            monitorSyncNotificationsUseCase.stub {
                on { invoke() }.thenReturn(flowOf(notificationMessage))
            }
            context.stub {
                on { checkPermission(any(), any(), any()) }
                    .thenReturn(PackageManager.PERMISSION_DENIED)
            }

            underTest()

            verifyNoInteractions(syncNotificationManager)
        }
}
