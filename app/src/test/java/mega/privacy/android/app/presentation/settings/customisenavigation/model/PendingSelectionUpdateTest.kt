package mega.privacy.android.app.presentation.settings.customisenavigation.model

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class PendingSelectionUpdateTest {

    @Test
    fun `test that addNavigationItem appends the id when below the maximum`() {
        val actual = listOf("home", "drive", "media").addNavigationItem("chat")

        assertThat(actual).isEqualTo(
            PendingSelectionUpdate.Applied(listOf("home", "drive", "media", "chat"))
        )
    }

    @Test
    fun `test that addNavigationItem returns MaxItemsReached when the selection is full`() {
        val actual = listOf("home", "drive", "media", "chat").addNavigationItem("offline")

        assertThat(actual).isEqualTo(PendingSelectionUpdate.MaxItemsReached)
    }

    @Test
    fun `test that addNavigationItem returns the same selection when the id is already selected`() {
        val selection = listOf("home", "drive", "media")

        val actual = selection.addNavigationItem("drive")

        assertThat(actual).isEqualTo(PendingSelectionUpdate.Applied(selection))
    }

    @Test
    fun `test that removeNavigationItem removes the id when above the minimum`() {
        val actual = listOf("home", "drive", "media", "chat").removeNavigationItem("drive")

        assertThat(actual).isEqualTo(
            PendingSelectionUpdate.Applied(listOf("home", "media", "chat"))
        )
    }

    @Test
    fun `test that removeNavigationItem returns MinItemsRequired when at the minimum`() {
        val actual = listOf("home", "drive", "media").removeNavigationItem("drive")

        assertThat(actual).isEqualTo(PendingSelectionUpdate.MinItemsRequired)
    }

    @Test
    fun `test that removeNavigationItem returns the same selection when the id is not selected`() {
        val selection = listOf("home", "drive", "media", "chat")

        val actual = selection.removeNavigationItem("offline")

        assertThat(actual).isEqualTo(PendingSelectionUpdate.Applied(selection))
    }

    @Test
    fun `test that moveNavigationItem moves the id to the target index`() {
        val actual = listOf("home", "drive", "media", "chat").moveNavigationItem(3, 0)

        assertThat(actual).isEqualTo(listOf("chat", "home", "drive", "media"))
    }

    @Test
    fun `test that moveNavigationItem returns the same list when an index is out of bounds`() {
        val selection = listOf("home", "drive", "media")

        assertThat(selection.moveNavigationItem(0, 3)).isEqualTo(selection)
        assertThat(selection.moveNavigationItem(-1, 0)).isEqualTo(selection)
    }
}
