package mega.privacy.android.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.VisibleForTesting
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mega.privacy.android.app.MegaApplication
import mega.privacy.android.domain.qualifier.ApplicationScope
import mega.privacy.android.domain.usecase.workers.StartCameraUploadUseCase
import timber.log.Timber

/**
 * Broadcast receiver triggered when the device has finished booting
 *
 * Dependencies are resolved lazily through an entry point instead of @AndroidEntryPoint field
 * injection, because the system can deliver BOOT_COMPLETED to an instrumented test process
 * before the Hilt test component exists.
 */
class BootEventReceiver : BroadcastReceiver() {

    /**
     * Receive an Intent broadcast
     *
     * Beginning Android 15, [Intent.ACTION_BOOT_COMPLETED] Receivers are not allowed to start
     * Foreground Services using dataSync Foreground Service Types
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (!context.canResolveHiltEntryPoints()) return
        val entryPoint = EntryPointAccessors.fromApplication(
            context,
            BootEventReceiverEntryPoint::class.java,
        )
        handleIntent(
            intent = intent,
            applicationScope = entryPoint.applicationScope(),
            startCameraUploadUseCase = entryPoint.startCameraUploadUseCase(),
        )
    }

    /**
     * True when the receiver may resolve Hilt entry points.
     *
     * This is the one architecturally honest guard: the OS times the BOOT_COMPLETED broadcast,
     * so it can arrive in an instrumented test process before the Hilt test component exists.
     * There the application is a Hilt test application rather than [MegaApplication], and entry
     * point resolution would crash, so the receiver must no-op; tests exercise [handleIntent]
     * directly instead.
     */
    private fun Context.canResolveHiltEntryPoints(): Boolean =
        applicationContext is MegaApplication

    @VisibleForTesting
    internal fun handleIntent(
        intent: Intent,
        applicationScope: CoroutineScope,
        startCameraUploadUseCase: StartCameraUploadUseCase,
    ) {
        if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED) &&
            Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM
        ) {
            Timber.d("BOOT_COMPLETED received. Starting Camera Uploads")
            applicationScope.launch {
                runCatching {
                    startCameraUploadUseCase()
                }.onFailure { Timber.e(it) }
            }
        }
    }

    /**
     * Entry point for [BootEventReceiver] dependencies
     */
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEventReceiverEntryPoint {
        /**
         * Application scope to run coroutines
         */
        @ApplicationScope
        fun applicationScope(): CoroutineScope

        /**
         * Schedule camera upload
         */
        fun startCameraUploadUseCase(): StartCameraUploadUseCase
    }
}
