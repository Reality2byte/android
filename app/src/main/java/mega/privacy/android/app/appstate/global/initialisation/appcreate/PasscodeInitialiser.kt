package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import mega.privacy.android.core.passcode.PasscodeLifeCycleObserver
import mega.privacy.android.core.passcode.PasscodeLifecycleDispatcher
import mega.privacy.android.core.passcode.PasscodeProcessLifecycleOwner
import mega.privacy.android.navigation.contract.initialisation.SynchronousAppCreateInitialiser
import javax.inject.Inject

/**
 * Hooks the passcode lifecycle machinery into the application's activity callbacks.
 *
 * Synchronous: the dispatcher and process lifecycle owner must be registered before the first
 * activity is created, otherwise passcode locking would miss lifecycle events.
 */
internal class PasscodeInitialiser @Inject constructor(
    @ApplicationContext private val context: Context,
    private val passcodeLifecycleObserver: PasscodeLifeCycleObserver,
) : SynchronousAppCreateInitialiser {
    override val name = "PasscodeInitialiser"

    override operator fun invoke() {
        PasscodeLifecycleDispatcher.init(context)
        PasscodeProcessLifecycleOwner.init(context)
        PasscodeProcessLifecycleOwner.get().observer = passcodeLifecycleObserver
    }
}
