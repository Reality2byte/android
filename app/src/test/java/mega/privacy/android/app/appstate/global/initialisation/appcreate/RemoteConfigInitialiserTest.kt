package mega.privacy.android.app.appstate.global.initialisation.appcreate

import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.BuildConfig
import mega.privacy.android.domain.usecase.featureflag.FetchAndActivateRemoteConfigUseCase
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RemoteConfigInitialiserTest {
    private lateinit var underTest: RemoteConfigInitialiser

    private val fetchAndActivateRemoteConfigUseCase = mock<FetchAndActivateRemoteConfigUseCase>()

    @BeforeAll
    fun setUp() {
        underTest = RemoteConfigInitialiser(
            fetchAndActivateRemoteConfigUseCase = fetchAndActivateRemoteConfigUseCase,
        )
    }

    @BeforeEach
    fun resetMocks() {
        reset(fetchAndActivateRemoteConfigUseCase)
    }

    @Test
    fun `test that invoke calls fetchAndActivateRemoteConfigUseCase with the build type fetch interval`() =
        runTest {
            underTest()

            verify(fetchAndActivateRemoteConfigUseCase).invoke(useMinimalFetchInterval = BuildConfig.DEBUG)
        }
}
