package mega.privacy.android.navigation.contract

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class MainNavItemOrderingTest {

    private val homeItem = navItem(id = "home", preferredSlot = PreferredSlot.Ordered(0))
    private val driveItem = navItem(id = "drive", preferredSlot = PreferredSlot.Ordered(1))
    private val mediaItem = navItem(id = "media", preferredSlot = PreferredSlot.Ordered(2))
    private val menuItem = navItem(id = "menu", preferredSlot = PreferredSlot.Last)

    private val allItems = listOf(mediaItem, menuItem, homeItem, driveItem)

    @Test
    fun `test that orderedByUserPreference returns items in user order when all ids are present`() {
        val actual = allItems.orderedByUserPreference(listOf("media", "home", "drive"))

        assertThat(actual).containsExactly(mediaItem, homeItem, driveItem, menuItem).inOrder()
    }

    @Test
    fun `test that orderedByUserPreference falls back to preferred slot order when preference is empty`() {
        val actual = allItems.orderedByUserPreference(emptyList())

        assertThat(actual).containsExactly(homeItem, driveItem, mediaItem, menuItem).inOrder()
    }

    @Test
    fun `test that orderedByUserPreference appends unmatched items in preferred slot order when preference is partial`() {
        val actual = allItems.orderedByUserPreference(listOf("media", "unknown"))

        assertThat(actual).containsExactly(mediaItem, homeItem, driveItem, menuItem).inOrder()
    }

    @Test
    fun `test that orderedByUserPreference pins the last slot item to the end when its id is in the preference`() {
        val actual = allItems.orderedByUserPreference(listOf("menu", "drive", "home", "media"))

        assertThat(actual).containsExactly(driveItem, homeItem, mediaItem, menuItem).inOrder()
    }

    private fun navItem(id: String, preferredSlot: PreferredSlot) = mock<MainNavItem> {
        on { this.id } doReturn id
        on { this.preferredSlot } doReturn preferredSlot
    }
}
