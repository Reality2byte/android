package mega.privacy.android.feature.mediaplayer.presentation

import com.google.common.truth.Truth.assertThat
import kotlin.time.Duration.Companion.milliseconds
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SleepTimerBottomSheetTest {

    @ParameterizedTest(name = "formatCountdown({0}ms) == \"{1}\"")
    @CsvSource(
        "0,         0:00",  // zero clamps to 0:00
        "-1000,     0:00",  // negative clamps to 0:00
        "1000,      0:01",  // 1 second
        "59000,     0:59",  // 59 seconds
        "60000,     1:00",  // exactly 1 minute
        "90000,     1:30",  // 1 min 30 sec
        "300000,    5:00",  // 5 minutes (Minutes5 option)
        "900000,    15:00", // 15 minutes (Minutes15 option)
        "1800000,   30:00", // 30 minutes (Minutes30 option)
        "3600000,   60:00", // 60 minutes (Minutes60 option)
        "3599000,   59:59", // one second before an hour
        "61000,     1:01",  // leading zero on seconds
    )
    fun `test that formatCountdown formats duration as M_SS`(
        inputMs: Long,
        expected: String,
    ) {
        assertThat(formatCountdown(inputMs.milliseconds)).isEqualTo(expected)
    }
}
