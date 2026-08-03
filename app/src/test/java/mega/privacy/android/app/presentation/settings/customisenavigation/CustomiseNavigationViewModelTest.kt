package mega.privacy.android.app.presentation.settings.customisenavigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.StateEvent
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mega.privacy.android.app.presentation.settings.customisenavigation.model.CustomiseNavigationUiState
import mega.privacy.android.core.test.extension.CoroutineMainDispatcherExtension
import mega.privacy.android.domain.entity.preference.NavigationItemsPreference
import mega.privacy.android.domain.usecase.featureflag.GetEnabledFlaggedItemsUseCase
import mega.privacy.android.domain.usecase.preference.MonitorNavigationItemsPreferenceUseCase
import mega.privacy.android.domain.usecase.preference.SetNavigationItemsPreferenceUseCase
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.PreferredSlot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify

@ExtendWith(CoroutineMainDispatcherExtension::class)
class CustomiseNavigationViewModelTest {
    private lateinit var underTest: CustomiseNavigationViewModel

    private val getEnabledFlaggedItemsUseCase = mock<GetEnabledFlaggedItemsUseCase>()
    private val monitorNavigationItemsPreferenceUseCase =
        mock<MonitorNavigationItemsPreferenceUseCase>()
    private val setNavigationItemsPreferenceUseCase = mock<SetNavigationItemsPreferenceUseCase>()

    private val homeItem = navItem(id = "home", preferredSlot = PreferredSlot.Ordered(0))
    private val driveItem = navItem(id = "drive", preferredSlot = PreferredSlot.Ordered(1))
    private val mediaItem = navItem(id = "media", preferredSlot = PreferredSlot.Ordered(2))
    private val offlineItem = navItem(id = "offline", preferredSlot = PreferredSlot.None)
    private val menuItem = navItem(id = "menu", preferredSlot = PreferredSlot.Last)
    private val allItems = setOf(homeItem, driveItem, mediaItem, offlineItem, menuItem)

    @BeforeEach
    fun setUp() {
        stubEnabledItems(allItems)
        stubPreference(preference = null)
        initUnderTest()
    }

    @AfterEach
    fun tearDown() {
        reset(
            getEnabledFlaggedItemsUseCase,
            monitorNavigationItemsPreferenceUseCase,
            setNavigationItemsPreferenceUseCase,
        )
    }

    @Test
    fun `test that initial state is Loading`() = runTest {
        assertThat(underTest.uiState.value).isEqualTo(CustomiseNavigationUiState.Loading)
    }

    @Test
    fun `test that base arrangement is the default slot order when no preference is saved`() =
        runTest {
            underTest.uiState.test {
                val actual = awaitDataState()

                assertThat(actual.baseArrangement.map { it.id })
                    .containsExactly("home", "drive", "media")
                    .inOrder()
            }
        }

    @Test
    fun `test that base arrangement follows the preference order when a preference is saved`() =
        runTest {
            stubPreference(NavigationItemsPreference(listOf("media", "home", "drive")))

            underTest.uiState.test {
                val actual = awaitDataState()

                assertThat(actual.baseArrangement.map { it.id })
                    .containsExactly("media", "home", "drive")
                    .inOrder()
            }
        }

    @Test
    fun `test that persisted ids without a matching enabled item are skipped`() = runTest {
        stubPreference(NavigationItemsPreference(listOf("media", "ghost", "home")))

        underTest.uiState.test {
            val actual = awaitDataState()

            assertThat(actual.baseArrangement.map { it.id })
                .containsExactly("media", "home")
                .inOrder()
        }
    }

    @Test
    fun `test that base arrangement falls back to the default bar when no preferred id matches`() =
        runTest {
            stubPreference(NavigationItemsPreference(listOf("ghost")))

            underTest.uiState.test {
                val actual = awaitDataState()

                assertThat(actual.baseArrangement.map { it.id })
                    .containsExactly("home", "drive", "media")
                    .inOrder()
            }
        }

    @Test
    fun `test that available items are the enabled items not in the base arrangement`() = runTest {
        stubPreference(NavigationItemsPreference(listOf("media", "home")))

        underTest.uiState.test {
            val actual = awaitDataState()

            assertThat(actual.availableItems.map { it.id })
                .containsExactly("drive", "offline")
                .inOrder()
        }
    }

    @Test
    fun `test that the menu item is exposed and excluded from base and available items`() =
        runTest {
            underTest.uiState.test {
                val actual = awaitDataState()

                assertThat(actual.menuItem.id).isEqualTo("menu")
                assertThat(actual.baseArrangement.map { it.id }).doesNotContain("menu")
                assertThat(actual.availableItems.map { it.id }).doesNotContain("menu")
            }
        }

    @Test
    fun `test that default arrangement ids are the default slot order`() = runTest {
        stubPreference(NavigationItemsPreference(listOf("media", "home")))

        underTest.uiState.test {
            val actual = awaitDataState()

            assertThat(actual.defaultArrangementIds)
                .containsExactly("home", "drive", "media")
                .inOrder()
        }
    }

    @Test
    fun `test that state remains Loading when no pinned last item exists`() = runTest {
        stubEnabledItems(setOf(homeItem, driveItem, mediaItem))
        initUnderTest(mainNavItems = setOf(homeItem, driveItem, mediaItem))

        underTest.uiState.test {
            assertThat(awaitItem()).isEqualTo(CustomiseNavigationUiState.Loading)
            expectNoEvents()
        }
    }

    @Test
    fun `test that save persists the given ordered ids`() = runTest {
        underTest.save(listOf("media", "home", "drive"))

        verify(setNavigationItemsPreferenceUseCase)
            .invoke(NavigationItemsPreference(listOf("media", "home", "drive")))
    }

    @Test
    fun `test that save excludes the menu item id`() = runTest {
        underTest.save(listOf("media", "menu", "home"))

        verify(setNavigationItemsPreferenceUseCase)
            .invoke(NavigationItemsPreference(listOf("media", "home")))
    }

    @Test
    fun `test that save triggers the saved event when persisting succeeds`() = runTest {
        underTest.uiState.test {
            assertThat(awaitDataState().savedEvent).isEqualTo(StateEvent.Consumed)

            underTest.save(listOf("home", "drive", "media"))

            assertThat(awaitDataState().savedEvent).isEqualTo(StateEvent.Triggered)
        }
    }

    @Test
    fun `test that save does not trigger the saved event when persisting fails`() = runTest {
        setNavigationItemsPreferenceUseCase.stub {
            onBlocking { invoke(any()) }.thenThrow(RuntimeException("failed"))
        }

        underTest.uiState.test {
            assertThat(awaitDataState().savedEvent).isEqualTo(StateEvent.Consumed)

            underTest.save(listOf("home", "drive", "media"))

            expectNoEvents()
        }
    }

    private fun initUnderTest(
        mainNavItems: Set<MainNavItem> = allItems,
    ) {
        underTest = CustomiseNavigationViewModel(
            mainNavItems = mainNavItems,
            getEnabledFlaggedItemsUseCase = getEnabledFlaggedItemsUseCase,
            monitorNavigationItemsPreferenceUseCase = monitorNavigationItemsPreferenceUseCase,
            setNavigationItemsPreferenceUseCase = setNavigationItemsPreferenceUseCase,
        )
    }

    private fun stubEnabledItems(items: Set<MainNavItem>) {
        getEnabledFlaggedItemsUseCase.stub {
            on { invoke(any<Set<MainNavItem>>()) }.thenReturn(
                flow {
                    emit(items)
                    awaitCancellation()
                }
            )
        }
    }

    private fun stubPreference(preference: NavigationItemsPreference?) {
        monitorNavigationItemsPreferenceUseCase.stub {
            on { invoke() }.thenReturn(flowOf(preference))
        }
    }

    private fun navItem(id: String, preferredSlot: PreferredSlot) = mock<MainNavItem> {
        on { this.id }.thenReturn(id)
        on { this.preferredSlot }.thenReturn(preferredSlot)
        on { this.label }.thenReturn(android.R.string.ok)
        on { this.icon }.thenReturn(Icons.Default.Home)
    }

    private suspend fun ReceiveTurbine<CustomiseNavigationUiState>.awaitDataState(): CustomiseNavigationUiState.Data {
        var item = awaitItem()
        while (item !is CustomiseNavigationUiState.Data) {
            item = awaitItem()
        }
        return item
    }
}
