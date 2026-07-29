package mega.privacy.android.app.appstate.content.navigation

import mega.privacy.android.domain.entity.preference.NavigationItemsPreference
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.contract.navkey.MainNavItemNavKey
import mega.privacy.android.navigation.contract.sortedByPreferredSlot
import javax.inject.Inject

/**
 * Resolves the navigation bar items and start destination from the enabled [MainNavItem]s,
 * the user's [NavigationItemsPreference] and the customisation feature flag.
 */
class MainNavigationBarReconciler @Inject constructor() {

    /**
     * Reconciles the navigation bar.
     *
     * When customisation is disabled or no preference is saved, the bar shows the default items:
     * [PreferredSlot.Ordered] items by slot followed by the pinned [PreferredSlot.Last] item,
     * excluding [PreferredSlot.None] items.
     *
     * When customisation is enabled and a preference is saved, the bar shows the preferred ids
     * that match an enabled item, in preference order, followed by the pinned
     * [PreferredSlot.Last] item. Persisted ids without a matching enabled item are skipped.
     * If no preferred id matches an enabled item, the default bar is used instead.
     *
     * @param enabledItems the enabled [MainNavItem]s
     * @param preference the saved navigation items preference, null if not set
     * @param isCustomisationEnabled the customisable bottom navigation feature flag value
     * @return the resolved [MainNavigationBar]
     */
    operator fun invoke(
        enabledItems: Set<MainNavItem>,
        preference: NavigationItemsPreference?,
        isCustomisationEnabled: Boolean,
    ): MainNavigationBar {
        if (!isCustomisationEnabled || preference == null) return defaultBar(enabledItems)
        val (lastItems, orderableItems) = enabledItems
            .partition { it.preferredSlot is PreferredSlot.Last }
        val itemsById = orderableItems.associateBy { it.id }
        val visibleItems = preference.orderedVisibleItemIds.mapNotNull { itemsById[it] }
        if (visibleItems.isEmpty()) return defaultBar(enabledItems)
        return MainNavigationBar(
            items = visibleItems + lastItems,
            startDestination = visibleItems.first().destination,
        )
    }

    private fun defaultBar(enabledItems: Set<MainNavItem>) = MainNavigationBar(
        items = enabledItems.sortedByPreferredSlot(),
        startDestination = null,
    )
}

/**
 * The resolved navigation bar.
 *
 * @property items the items shown in the navigation bar, in display order
 * @property startDestination the destination of the user's preferred start item, null when the
 * start screen is not driven by the navigation items preference
 */
data class MainNavigationBar(
    val items: List<MainNavItem>,
    val startDestination: MainNavItemNavKey?,
)
