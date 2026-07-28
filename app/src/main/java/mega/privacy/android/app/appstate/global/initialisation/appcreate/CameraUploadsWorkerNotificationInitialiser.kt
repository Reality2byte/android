package mega.privacy.android.app.appstate.global.initialisation.appcreate

import mega.privacy.android.app.notifications.CameraUploadsNotificationManager
import mega.privacy.android.domain.usecase.camerauploads.MonitorCameraUploadsStatusInfoUseCase
import mega.privacy.android.navigation.contract.initialisation.AsyncAppCreateInitialiser
import javax.inject.Inject

/**
 * Collects the camera uploads status info and surfaces each update as a notification.
 *
 * Async: the collector already ran fire-and-forget in the application scope at androidx.startup
 * provider time; nothing at boot waits on it.
 */
internal class CameraUploadsWorkerNotificationInitialiser @Inject constructor(
    private val monitorCameraUploadsStatusInfoUseCase: MonitorCameraUploadsStatusInfoUseCase,
    private val cameraUploadsNotificationManager: CameraUploadsNotificationManager,
) : AsyncAppCreateInitialiser {
    override val name = "CameraUploadsWorkerNotificationInitialiser"

    override suspend operator fun invoke() {
        monitorCameraUploadsStatusInfoUseCase().collect {
            cameraUploadsNotificationManager.showNotification(it)
        }
    }
}
