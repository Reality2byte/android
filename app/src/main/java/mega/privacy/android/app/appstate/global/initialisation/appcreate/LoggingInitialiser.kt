package mega.privacy.android.app.appstate.global.initialisation.appcreate

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import mega.privacy.android.app.BuildConfig
import mega.privacy.android.domain.qualifier.ApplicationScope
import mega.privacy.android.domain.usecase.EnableLogAllToConsole
import mega.privacy.android.domain.usecase.InitialiseLoggingUseCase
import mega.privacy.android.navigation.contract.initialisation.SynchronousAppCreateInitialiser
import javax.inject.Inject

/**
 * Enables console logging and kicks off the file logging subsystem initialisation.
 *
 * Synchronous and first in the list: every later initialiser should boot with logging
 * configured. The file logging initialisation itself remains fire-and-forget in the
 * application scope, exactly as it ran at androidx.startup provider time.
 */
internal class LoggingInitialiser @Inject constructor(
    private val enableLogAllToConsole: EnableLogAllToConsole,
    private val initialiseLoggingUseCase: InitialiseLoggingUseCase,
    @ApplicationScope private val appScope: CoroutineScope,
) : SynchronousAppCreateInitialiser {
    override val name = "LoggingInitialiser"

    override operator fun invoke() {
        enableLogAllToConsole(BuildConfig.DEBUG)
        appScope.launch {
            initialiseLoggingUseCase()
        }
    }
}
