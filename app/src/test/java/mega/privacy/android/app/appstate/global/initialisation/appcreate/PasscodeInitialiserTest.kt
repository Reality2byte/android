package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.app.Application
import android.content.Context
import com.google.common.truth.Truth.assertThat
import mega.privacy.android.core.passcode.PasscodeLifeCycleObserver
import mega.privacy.android.core.passcode.PasscodeProcessLifecycleOwner
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class PasscodeInitialiserTest {

    private val application = mock<Application>()
    private val context = mock<Context> {
        on { applicationContext } doReturn application
    }
    private val passcodeLifecycleObserver = mock<PasscodeLifeCycleObserver>()
    private val underTest = PasscodeInitialiser(
        context = context,
        passcodeLifecycleObserver = passcodeLifecycleObserver,
    )

    @Test
    fun `test that invoke registers the observer on the passcode process lifecycle owner`() {
        underTest()

        assertThat(PasscodeProcessLifecycleOwner.get().observer)
            .isSameInstanceAs(passcodeLifecycleObserver)
    }

    @Test
    fun `test that invoke attaches activity lifecycle callbacks to the application`() {
        underTest()

        verify(application, atLeastOnce()).registerActivityLifecycleCallbacks(any())
    }
}
