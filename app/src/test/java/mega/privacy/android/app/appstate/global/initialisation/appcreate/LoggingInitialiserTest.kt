package mega.privacy.android.app.appstate.global.initialisation.appcreate

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.BuildConfig
import mega.privacy.android.domain.usecase.EnableLogAllToConsole
import mega.privacy.android.domain.usecase.InitialiseLoggingUseCase
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@OptIn(ExperimentalCoroutinesApi::class)
class LoggingInitialiserTest {

    private val enableLogAllToConsole = mock<EnableLogAllToConsole>()
    private val initialiseLoggingUseCase = mock<InitialiseLoggingUseCase>()

    @Test
    fun `test that invoke enables console logging synchronously`() = runTest {
        val underTest = LoggingInitialiser(
            enableLogAllToConsole = enableLogAllToConsole,
            initialiseLoggingUseCase = initialiseLoggingUseCase,
            appScope = this,
        )

        underTest()

        verify(enableLogAllToConsole).invoke(BuildConfig.DEBUG)
        verifyNoInteractions(initialiseLoggingUseCase)
    }

    @Test
    fun `test that invoke launches logging initialisation in the application scope`() = runTest {
        val underTest = LoggingInitialiser(
            enableLogAllToConsole = enableLogAllToConsole,
            initialiseLoggingUseCase = initialiseLoggingUseCase,
            appScope = this,
        )

        underTest()
        advanceUntilIdle()

        verify(initialiseLoggingUseCase).invoke()
    }
}
