package mega.privacy.mobile.home.presentation.home.widget.chips

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.entity.preference.NavigationItemsPreference
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.domain.usecase.featureflag.GetFeatureFlagValueUseCase
import mega.privacy.android.domain.usecase.preference.MonitorNavigationItemsPreferenceUseCase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub

@ExtendWith(CoroutineMainDispatcherExtension::class)
class HomeChipsWidgetViewModelTest {
    private lateinit var underTest: HomeChipsWidgetViewModel

    private val getFeatureFlagValueUseCase = mock<GetFeatureFlagValueUseCase>()
    private val monitorNavigationItemsPreferenceUseCase =
        mock<MonitorNavigationItemsPreferenceUseCase>()

    @BeforeEach
    fun setUp() {
        underTest = HomeChipsWidgetViewModel(
            getFeatureFlagValueUseCase = getFeatureFlagValueUseCase,
            monitorNavigationItemsPreferenceUseCase = monitorNavigationItemsPreferenceUseCase,
        )
    }

    @AfterEach
    fun tearDown() {
        reset(
            getFeatureFlagValueUseCase,
            monitorNavigationItemsPreferenceUseCase,
        )
    }

    @Test
    fun `test that initial state hides no chips`() = runTest {
        stubFeatureFlag(enabled = true)
        monitorNavigationItemsPreferenceUseCase.stub {
            on { invoke() } doReturn flow { awaitCancellation() }
        }

        assertThat(underTest.uiState.value.hiddenSectionIds).isEmpty()
    }

    @Test
    fun `test that no chips are hidden when the feature flag is off`() = runTest {
        stubFeatureFlag(enabled = false)
        stubPreference(NavigationItemsPreference(listOf("favourites", "offline", "chat")))

        underTest.uiState.test {
            assertThat(expectMostRecentItem().hiddenSectionIds).isEmpty()
        }
    }

    @Test
    fun `test that no chips are hidden when the flag is on but no preference is saved`() = runTest {
        stubFeatureFlag(enabled = true)
        stubPreference(null)

        underTest.uiState.test {
            assertThat(expectMostRecentItem().hiddenSectionIds).isEmpty()
        }
    }

    @Test
    fun `test that preference ids are hidden when the flag is on`() = runTest {
        stubFeatureFlag(enabled = true)
        stubPreference(NavigationItemsPreference(listOf("favourites", "drive", "media")))

        underTest.uiState.test {
            assertThat(expectMostRecentItem().hiddenSectionIds)
                .containsExactly("favourites", "drive", "media")
        }
    }

    @Test
    fun `test that the chat id is hidden when the flag is on and the preference contains chat`() =
        runTest {
            stubFeatureFlag(enabled = true)
            stubPreference(NavigationItemsPreference(listOf("chat", "drive")))

            underTest.uiState.test {
                assertThat(expectMostRecentItem().hiddenSectionIds).contains("chat")
            }
        }

    private fun stubFeatureFlag(enabled: Boolean) {
        getFeatureFlagValueUseCase.stub {
            onBlocking { invoke(ApiFeatures.CustomisableBottomNavigation) } doReturn enabled
        }
    }

    private fun stubPreference(preference: NavigationItemsPreference?) {
        monitorNavigationItemsPreferenceUseCase.stub {
            on { invoke() } doReturn flow {
                emit(preference)
                awaitCancellation()
            }
        }
    }
}
