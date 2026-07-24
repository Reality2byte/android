package mega.privacy.android.app.appstate.global.initialisation.appcreate

import mega.privacy.android.app.BuildConfig
import mega.privacy.android.domain.usecase.featureflag.FetchAndActivateRemoteConfigUseCase
import mega.privacy.android.navigation.contract.initialisation.AsyncAppCreateInitialiser
import javax.inject.Inject

/**
 * Fetches and activates Firebase Remote Config values at app create so that
 * A/B test experiment values are available before the first screen renders.
 */
class RemoteConfigInitialiser @Inject constructor(
    private val fetchAndActivateRemoteConfigUseCase: FetchAndActivateRemoteConfigUseCase,
) : AsyncAppCreateInitialiser {
    override val name = "RemoteConfigInitialiser"

    override suspend operator fun invoke() {
        fetchAndActivateRemoteConfigUseCase(useMinimalFetchInterval = BuildConfig.DEBUG)
    }
}
