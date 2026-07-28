package mega.privacy.android.app.appstate.global.initialisation.appcreate

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.notifications.CameraUploadsNotificationManager
import mega.privacy.android.domain.entity.camerauploads.CameraUploadsStatusInfo
import mega.privacy.android.domain.usecase.camerauploads.MonitorCameraUploadsStatusInfoUseCase
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CameraUploadsWorkerNotificationInitialiserTest {
    private lateinit var underTest: CameraUploadsWorkerNotificationInitialiser

    private val monitorCameraUploadsStatusInfoUseCase =
        mock<MonitorCameraUploadsStatusInfoUseCase>()
    private val cameraUploadsNotificationManager = mock<CameraUploadsNotificationManager>()

    @BeforeAll
    fun setUp() {
        underTest = CameraUploadsWorkerNotificationInitialiser(
            monitorCameraUploadsStatusInfoUseCase = monitorCameraUploadsStatusInfoUseCase,
            cameraUploadsNotificationManager = cameraUploadsNotificationManager,
        )
    }

    @BeforeEach
    fun resetMocks() {
        reset(monitorCameraUploadsStatusInfoUseCase, cameraUploadsNotificationManager)
    }

    @Test
    fun `test that invoke shows a notification for each camera uploads status info`() = runTest {
        monitorCameraUploadsStatusInfoUseCase.stub {
            on { invoke() }.thenReturn(
                flowOf(
                    CameraUploadsStatusInfo.Started,
                    CameraUploadsStatusInfo.CheckFilesForUpload,
                )
            )
        }

        underTest()

        verify(cameraUploadsNotificationManager)
            .showNotification(CameraUploadsStatusInfo.Started)
        verify(cameraUploadsNotificationManager)
            .showNotification(CameraUploadsStatusInfo.CheckFilesForUpload)
    }

    @Test
    fun `test that invoke does not show a notification when no status info is emitted`() =
        runTest {
            monitorCameraUploadsStatusInfoUseCase.stub {
                on { invoke() }.thenReturn(flowOf())
            }

            underTest()

            verifyNoInteractions(cameraUploadsNotificationManager)
        }
}
