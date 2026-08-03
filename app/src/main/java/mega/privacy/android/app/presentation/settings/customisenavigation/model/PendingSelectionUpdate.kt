package mega.privacy.android.app.presentation.settings.customisenavigation.model

/**
 * Maximum number of user-selectable navigation bar items, excluding the pinned Menu item.
 */
const val MaxSelectableNavigationItems = 4

/**
 * Minimum number of user-selectable navigation bar items, excluding the pinned Menu item.
 */
const val MinSelectableNavigationItems = 3

/**
 * Result of applying a change to the pending navigation selection.
 */
sealed interface PendingSelectionUpdate {

    /**
     * The change was applied.
     *
     * @property orderedIds the resulting ordered selection
     */
    data class Applied(val orderedIds: List<String>) : PendingSelectionUpdate

    /**
     * The item cannot be added because the selection already holds
     * [MaxSelectableNavigationItems] items.
     */
    data object MaxItemsReached : PendingSelectionUpdate

    /**
     * The item cannot be removed because the selection only holds
     * [MinSelectableNavigationItems] items.
     */
    data object MinItemsRequired : PendingSelectionUpdate
}

/**
 * Adds [id] to the end of the pending selection.
 *
 * @return [PendingSelectionUpdate.MaxItemsReached] when the selection is full, otherwise the
 * applied selection
 */
fun List<String>.addNavigationItem(id: String): PendingSelectionUpdate = when {
    id in this -> PendingSelectionUpdate.Applied(this)
    size >= MaxSelectableNavigationItems -> PendingSelectionUpdate.MaxItemsReached
    else -> PendingSelectionUpdate.Applied(this + id)
}

/**
 * Removes [id] from the pending selection.
 *
 * @return [PendingSelectionUpdate.MinItemsRequired] when the selection is at the minimum,
 * otherwise the applied selection
 */
fun List<String>.removeNavigationItem(id: String): PendingSelectionUpdate = when {
    id !in this -> PendingSelectionUpdate.Applied(this)
    size <= MinSelectableNavigationItems -> PendingSelectionUpdate.MinItemsRequired
    else -> PendingSelectionUpdate.Applied(this - id)
}

/**
 * Moves the item at [fromIndex] to [toIndex], returning the receiver when either index is
 * out of bounds.
 */
fun List<String>.moveNavigationItem(fromIndex: Int, toIndex: Int): List<String> = when {
    fromIndex == toIndex || fromIndex !in indices || toIndex !in indices -> this
    else -> toMutableList().apply { add(toIndex, removeAt(fromIndex)) }
}
