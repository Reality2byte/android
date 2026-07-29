package mega.privacy.android.app.appstate.content.navigation

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.Serializable
import mega.privacy.android.domain.entity.preference.NavigationItemsPreference
import mega.privacy.android.navigation.contract.MainNavItem
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.contract.navkey.MainNavItemNavKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainNavigationBarReconcilerTest {

    private val underTest = MainNavigationBarReconciler()

    private val homeItem = navItem(id = "home", preferredSlot = PreferredSlot.Ordered(0))
    private val driveItem = navItem(id = "drive", preferredSlot = PreferredSlot.Ordered(1))
    private val mediaItem = navItem(id = "media", preferredSlot = PreferredSlot.Ordered(2))
    private val menuItem = navItem(id = "menu", preferredSlot = PreferredSlot.Last)
    private val offlineItem = navItem(id = "offline", preferredSlot = PreferredSlot.None)

    private val enabledItems = setOf(mediaItem, menuItem, offlineItem, homeItem, driveItem)

    @Test
    fun `test that invoke returns the default bar when the flag is disabled`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = NavigationItemsPreference(listOf("media", "offline", "home")),
            isCustomisationEnabled = false,
        )

        assertThat(actual.items)
            .containsExactly(homeItem, driveItem, mediaItem, menuItem)
            .inOrder()
        assertThat(actual.startDestination).isNull()
    }

    @Test
    fun `test that invoke returns the default bar when the preference is null`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = null,
            isCustomisationEnabled = true,
        )

        assertThat(actual.items)
            .containsExactly(homeItem, driveItem, mediaItem, menuItem)
            .inOrder()
        assertThat(actual.startDestination).isNull()
    }

    @Test
    fun `test that invoke excludes items with no default slot from the default bar when the flag is disabled`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = null,
            isCustomisationEnabled = false,
        )

        assertThat(actual.items).doesNotContain(offlineItem)
    }

    @Test
    fun `test that invoke returns items in preference order with the last item pinned when the flag is enabled`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = NavigationItemsPreference(listOf("media", "offline", "home")),
            isCustomisationEnabled = true,
        )

        assertThat(actual.items)
            .containsExactly(mediaItem, offlineItem, homeItem, menuItem)
            .inOrder()
    }

    @Test
    fun `test that invoke hides enabled items not listed in the preference when the flag is enabled`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = NavigationItemsPreference(listOf("media", "home")),
            isCustomisationEnabled = true,
        )

        assertThat(actual.items).containsExactly(mediaItem, homeItem, menuItem).inOrder()
    }

    @Test
    fun `test that invoke skips preferred ids without a matching enabled item when the flag is enabled`() {
        val actual = underTest(
            enabledItems = setOf(homeItem, driveItem, menuItem),
            preference = NavigationItemsPreference(listOf("media", "drive", "home")),
            isCustomisationEnabled = true,
        )

        assertThat(actual.items).containsExactly(driveItem, homeItem, menuItem).inOrder()
    }

    @Test
    fun `test that invoke returns the start destination of the first preferred item when the flag is enabled`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = NavigationItemsPreference(listOf("media", "home")),
            isCustomisationEnabled = true,
        )

        assertThat(actual.startDestination).isEqualTo(mediaItem.destination)
    }

    @Test
    fun `test that invoke returns the default bar when no preferred id matches an enabled item`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = NavigationItemsPreference(listOf("unknown", "alsoUnknown")),
            isCustomisationEnabled = true,
        )

        assertThat(actual.items)
            .containsExactly(homeItem, driveItem, mediaItem, menuItem)
            .inOrder()
        assertThat(actual.startDestination).isNull()
    }

    @Test
    fun `test that invoke pins the last item to the end when its id is in the preference`() {
        val actual = underTest(
            enabledItems = enabledItems,
            preference = NavigationItemsPreference(listOf("menu", "media", "home")),
            isCustomisationEnabled = true,
        )

        assertThat(actual.items).containsExactly(mediaItem, homeItem, menuItem).inOrder()
        assertThat(actual.startDestination).isEqualTo(mediaItem.destination)
    }

    private fun navItem(id: String, preferredSlot: PreferredSlot) = mock<MainNavItem> {
        on { this.id } doReturn id
        on { this.preferredSlot } doReturn preferredSlot
        on { this.destination } doReturn TestNavKey(id)
    }

    @Serializable
    private data class TestNavKey(val id: String) : MainNavItemNavKey
}
