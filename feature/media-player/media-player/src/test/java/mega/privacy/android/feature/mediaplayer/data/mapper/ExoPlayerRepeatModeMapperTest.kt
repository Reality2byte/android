package mega.privacy.android.feature.mediaplayer.data.mapper

import androidx.media3.common.Player
import com.google.common.truth.Truth
import mega.privacy.android.feature.mediaplayer.data.mapper.ExoPlayerRepeatModeMapper
import mega.privacy.android.domain.entity.mediaplayer.RepeatToggleMode
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class ExoPlayerRepeatModeMapperTest {
    private val underTest = ExoPlayerRepeatModeMapper()

    @TestFactory
    fun `test that repeat toggle mode can be mapped correctly`() =
        listOf(
            RepeatToggleMode.REPEAT_ONE to Player.REPEAT_MODE_ONE,
            RepeatToggleMode.REPEAT_NONE to Player.REPEAT_MODE_OFF,
            RepeatToggleMode.REPEAT_ALL to Player.REPEAT_MODE_ALL,
        ).map { (input, expected) ->
            DynamicTest.dynamicTest("test that $input is mapped to $expected") {
                Truth.assertThat(underTest(input)).isEqualTo(expected)
            }
        }
}