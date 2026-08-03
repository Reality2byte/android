package mega.privacy.android.app.di.mediaplayer

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import mega.privacy.android.app.mediaplayer.gateway.AudioMediaControllerFacade
import mega.privacy.android.feature.mediaplayer.data.gateway.AudioMediaControllerGateway

/**
 * Hilt module that binds [AudioMediaControllerGateway] to [AudioMediaControllerFacade].
 *
 * No scope annotation — a new [AudioMediaControllerFacade] is created per injection site.
 * [mega.privacy.android.feature.mediaplayer.presentation.AudioPlayerViewModel] is
 * activity-scoped, so one instance is shared for the Activity's lifetime;
 * [AudioMediaControllerGateway.release] is called when the Activity is destroyed.
 */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class AudioMediaControllerModule {

    @Binds
    abstract fun bindAudioMediaControllerGateway(
        impl: AudioMediaControllerFacade,
    ): AudioMediaControllerGateway
}
