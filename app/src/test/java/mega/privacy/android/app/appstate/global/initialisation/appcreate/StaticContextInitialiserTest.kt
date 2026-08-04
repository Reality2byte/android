package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.content.Context
import com.google.common.truth.Truth.assertThat
import mega.privacy.android.app.utils.AvatarUtil
import mega.privacy.android.app.utils.VideoCaptureUtils
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class StaticContextInitialiserTest {

    private val context = mock<Context>()
    private val underTest = StaticContextInitialiser(context)

    @Test
    fun `test that invoke sets the application context on AvatarUtil`() {
        underTest()

        assertThat(AvatarUtil.applicationContext).isSameInstanceAs(context)
    }

    @Test
    fun `test that invoke sets the application context on VideoCaptureUtils`() {
        underTest()

        assertThat(VideoCaptureUtils.getApplicationContextForTesting()).isSameInstanceAs(context)
    }
}
