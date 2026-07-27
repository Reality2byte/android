package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.content.Context
import androidx.emoji2.text.EmojiCompat
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import mega.privacy.android.thirdpartylib.twemoji.EmojiManager
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class EmojiInitialiserTest {

    private val context = mock<Context>()
    private val underTest = EmojiInitialiser(
        context = context,
    )

    @Test
    fun `test that invoke initialises EmojiCompat`() = runTest {
        underTest()

        assertThat(EmojiCompat.get()).isNotNull()
    }

    @Test
    fun `test that invoke installs the twemoji provider`() = runTest {
        underTest()

        assertThat(EmojiManager.getInstance().getNumEmojis("😀")).isGreaterThan(0)
    }
}
