package mega.privacy.android.navigation.contract

/**
 * Defines the preferred slot for a navigation item
 */
sealed interface PreferredSlot {
    /**
     * Specifies a specific slot number (0-based)
     */
    data class Ordered(val slot: Int) : PreferredSlot

    /**
     * Specifies that this item is pinned to the last slot.
     *
     * The last slot is non-removable and reserved for the Menu item; only one item may use it.
     */
    data object Last : PreferredSlot

    /**
     * Specifies that this item has no default slot.
     *
     * It never appears in the navigation bar unless the user explicitly adds it via the
     * navigation items preference.
     */
    data object None : PreferredSlot
}