package mega.privacy.android.feature.sharelink.presentation

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.text.DateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

/**
 * The default timezone is pinned per test so these exercise real offsets rather than whatever the
 * build machine happens to be set to — the UTC-vs-local confusion this code exists to resolve is
 * invisible when both are the same.
 */
class ExpiryDateTest {

    @AfterEach
    fun tearDown() {
        TimeZone.setDefault(null)
    }

    private fun useTimeZone(id: String) = TimeZone.setDefault(TimeZone.getTimeZone(id))

    private fun utcMidnight(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance(UTC).apply {
            clear()
            set(year, month, day, 0, 0, 0)
        }.timeInMillis

    private fun Long.localFields(): List<Int> =
        Calendar.getInstance().apply { timeInMillis = this@localFields }.let {
            listOf(
                it.get(Calendar.YEAR),
                it.get(Calendar.MONTH),
                it.get(Calendar.DAY_OF_MONTH),
                it.get(Calendar.HOUR_OF_DAY),
                it.get(Calendar.MINUTE),
                it.get(Calendar.SECOND),
            )
        }

    @ParameterizedTest
    @ValueSource(strings = [AUCKLAND, LOS_ANGELES, "UTC"])
    fun `test that endOfLocalDay returns the last second of the chosen day in local time`(zone: String) {
        useTimeZone(zone)

        val endOfDay = endOfLocalDay(utcMidnight(2026, Calendar.JULY, 29))

        assertThat(endOfDay.localFields())
            .isEqualTo(listOf(2026, Calendar.JULY, 29, 23, 59, 59))
    }

    @ParameterizedTest
    @ValueSource(strings = [AUCKLAND, LOS_ANGELES, "UTC"])
    fun `test that endOfLocalDay returns an instant in the future when today is chosen`(zone: String) {
        useTimeZone(zone)

        val endOfToday = endOfLocalDay(todayStartUtcMillis())

        assertThat(endOfToday).isGreaterThan(System.currentTimeMillis())
    }

    @ParameterizedTest
    @ValueSource(strings = [AUCKLAND, LOS_ANGELES, "UTC"])
    fun `test that utcMidnightOfLocalDay round-trips the day chosen in the picker`(zone: String) {
        useTimeZone(zone)
        val picked = utcMidnight(2026, Calendar.JULY, 29)

        val roundTripped = utcMidnightOfLocalDay(endOfLocalDay(picked))

        assertThat(roundTripped).isEqualTo(picked)
    }

    @ParameterizedTest
    @ValueSource(strings = [AUCKLAND, LOS_ANGELES, "UTC"])
    fun `test that todayStartUtcMillis makes the local today selectable`(zone: String) {
        useTimeZone(zone)

        val floor = todayStartUtcMillis()

        assertThat(floor).isEqualTo(utcMidnightOfLocalDay(System.currentTimeMillis()))
        assertThat(endOfLocalDay(floor).localFields().take(3))
            .isEqualTo(System.currentTimeMillis().localFields().take(3))
    }

    @Test
    fun `test that formatExpiryDate renders the local day when the day ends on the next UTC day`() {
        // In Los Angeles the last second of 29 July is already 30 July in UTC, so formatting in UTC
        // would show the user a day they did not pick.
        useTimeZone(LOS_ANGELES)
        val endOfDay = endOfLocalDay(utcMidnight(2026, Calendar.JULY, 29))
        val utcDay = Calendar.getInstance(UTC)
            .apply { timeInMillis = endOfDay }
            .get(Calendar.DAY_OF_MONTH)
        assertThat(utcDay).isEqualTo(30)

        val formatted = formatExpiryDate(endOfDay)

        assertThat(formatted)
            .isEqualTo(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(endOfDay)))
        assertThat(formatted).isNotEqualTo(
            DateFormat.getDateInstance(DateFormat.MEDIUM)
                .apply { timeZone = UTC }
                .format(Date(endOfDay))
        )
    }

    private companion object {
        // A large positive offset and a negative one, where UTC-midnight storage breaks differently.
        const val AUCKLAND = "Pacific/Auckland"
        const val LOS_ANGELES = "America/Los_Angeles"
    }
}
