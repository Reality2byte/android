package mega.privacy.android.app.appstate.global.initialisation.appcreate

import android.content.Context
import androidx.core.provider.FontRequest
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.text.FontRequestEmojiCompatConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import mega.privacy.android.app.R
import mega.privacy.android.navigation.contract.initialisation.AsyncAppCreateInitialiser
import mega.privacy.android.thirdpartylib.twemoji.EmojiManager
import mega.privacy.android.thirdpartylib.twemoji.EmojiManagerShortcodes
import mega.privacy.android.thirdpartylib.twemoji.TwitterEmojiProvider
import timber.log.Timber
import javax.inject.Inject

/**
 * Loads the emoji shortcode data and registers the EmojiCompat and twemoji configurations.
 *
 * Async: the whole body already ran fire-and-forget in the application scope at androidx.startup
 * provider time; EmojiCompat uses [EmojiCompat.LOAD_STRATEGY_MANUAL], so this only registers the
 * configuration and nothing waits on it at boot.
 */
internal class EmojiInitialiser @Inject constructor(
    @ApplicationContext private val context: Context,
) : AsyncAppCreateInitialiser {
    override val name = "EmojiInitialiser"

    override suspend operator fun invoke() {
        EmojiManagerShortcodes.initEmojiData(context)

        Timber.d("Use downloadable font for EmojiCompat")

        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )

        val config = FontRequestEmojiCompatConfig(context, fontRequest)
            .setReplaceAll(false)
            .setMetadataLoadStrategy(EmojiCompat.LOAD_STRATEGY_MANUAL)
            .registerInitCallback(object : EmojiCompat.InitCallback() {
                override fun onInitialized() {
                    Timber.d("EmojiCompat initialized")
                }

                override fun onFailed(throwable: Throwable?) {
                    Timber.w("EmojiCompat initialization failed")
                }
            })

        EmojiCompat.init(config)
        EmojiManager.install(TwitterEmojiProvider())
    }
}
