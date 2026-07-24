package mega.privacy.android.data.gateway

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import java.util.TimeZone

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class AndroidDeviceGatewayTest {

    private val underTest = AndroidDeviceGateway(
        context = mock(),
        appScope = TestScope(StandardTestDispatcher()),
    )

    private val originalTimeZone = TimeZone.getDefault()

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `test that getCurrentTimezoneOffset returns positive offset with sign for east of UTC`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+09:00"))

        assertThat(underTest.getCurrentTimezoneOffset()).isEqualTo("+09:00")
    }

    @Test
    fun `test that getCurrentTimezoneOffset returns negative offset with sign for west of UTC`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT-05:30"))

        assertThat(underTest.getCurrentTimezoneOffset()).isEqualTo("-05:30")
    }

    @Test
    fun `test that getCurrentTimezoneOffset returns plus zero zero for UTC`() {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+00:00"))

        assertThat(underTest.getCurrentTimezoneOffset()).isEqualTo("+00:00")
    }
}
