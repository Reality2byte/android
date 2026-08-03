package mega.privacy.android.app.presentation.settings.customisenavigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.palm.composestateevents.StateEvent
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import mega.privacy.android.app.presentation.settings.customisenavigation.model.CustomiseNavigationUiState
import mega.privacy.android.app.presentation.settings.customisenavigation.model.NavigationItemUiModel
import mega.privacy.android.core.coroutine.asUiStateFlow
import mega.privacy.android.domain.entity.preference.NavigationItemsPreference
import mega.privacy.android.domain.usecase.featureflag.GetEnabledFlaggedItemsUseCase
import mega.privacy.android.domain.usecase.preference.MonitorNavigationItemsPreferenceUseCase
import mega.privacy.android.domain.usecase.preference.SetNavigationItemsPreferenceUseCase
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.contract.sortedByPreferredSlot
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the Customise navigation settings screen.
 *
 * Provides the persisted arrangement and the items available to add, and persists the
 * selection on save. The pending (unsaved) arrangement is owned by the UI.
 */
@HiltViewModel
internal class CustomiseNavigationViewModel @Inject constructor(
    mainNavItems: Set<@JvmSuppressWildcards MainNavItem>,
    getEnabledFlaggedItemsUseCase: GetEnabledFlaggedItemsUseCase,
    monitorNavigationItemsPreferenceUseCase: MonitorNavigationItemsPreferenceUseCase,
    private val setNavigationItemsPreferenceUseCase: SetNavigationItemsPreferenceUseCase,
) : ViewModel() {

    private val menuItemIds = mainNavItems
        .filter { it.preferredSlot is PreferredSlot.Last }
        .map { it.id }
        .toSet()

    private val savedEventChannel = Channel<StateEvent>(Channel.BUFFERED)

    val uiState: StateFlow<CustomiseNavigationUiState> by lazy(LazyThreadSafetyMode.NONE) {
        combine(
            getEnabledFlaggedItemsUseCase(mainNavItems),
            monitorNavigationItemsPreferenceUseCase(),
            savedEventChannel.receiveAsFlow()
                .onStart { emit(consumed) },
        ) { enabledItems, preference, savedEvent ->
            buildDataState(
                enabledItems = enabledItems,
                preference = preference,
                savedEvent = savedEvent,
            )
        }.filterNotNull()
            .catch { Timber.e(it, "Error building customise navigation state") }
            .asUiStateFlow(
                scope = viewModelScope,
                initialValue = CustomiseNavigationUiState.Loading,
            )
    }

    private fun buildDataState(
        enabledItems: Set<MainNavItem>,
        preference: NavigationItemsPreference?,
        savedEvent: StateEvent,
    ): CustomiseNavigationUiState.Data? {
        val (lastItems, orderableItems) = enabledItems
            .partition { it.preferredSlot is PreferredSlot.Last }
        val menuItem = lastItems.firstOrNull() ?: run {
            Timber.w("No pinned last navigation item found")
            return null
        }
        val defaultItems = orderableItems.sortedByPreferredSlot()
        val itemsById = orderableItems.associateBy { it.id }
        val baseItems = preference?.orderedVisibleItemIds
            ?.mapNotNull { itemsById[it] }
            .orEmpty()
            .ifEmpty { defaultItems }
        val availableItems = (orderableItems - baseItems.toSet())
            .sortedWith(
                compareBy(
                    { (it.preferredSlot as? PreferredSlot.Ordered)?.slot ?: Int.MAX_VALUE },
                    { it.id },
                )
            )
        return CustomiseNavigationUiState.Data(
            baseArrangement = baseItems.map { it.toUiModel() },
            availableItems = availableItems.map { it.toUiModel() },
            menuItem = menuItem.toUiModel(),
            defaultArrangementIds = defaultItems.map { it.id },
            savedEvent = savedEvent,
        )
    }

    /**
     * Persists [orderedIds] as the navigation items preference.
     *
     * The pinned Menu item is never persisted; the first id is the start screen.
     */
    fun save(orderedIds: List<String>) {
        viewModelScope.launch {
            runCatching {
                setNavigationItemsPreferenceUseCase(
                    NavigationItemsPreference(orderedIds.filterNot { it in menuItemIds })
                )
            }.onSuccess {
                savedEventChannel.send(triggered)
            }.onFailure {
                Timber.e(it, "Failed to save navigation items preference")
            }
        }
    }

    /**
     * Marks the saved event as consumed.
     */
    fun onSavedEventConsumed() {
        savedEventChannel.trySend(consumed)
    }

    private fun MainNavItem.toUiModel() = NavigationItemUiModel(
        id = id,
        label = label,
        icon = icon,
    )
}
