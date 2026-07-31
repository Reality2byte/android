package mega.privacy.android.app.presentation.transfers.navigation

import com.google.common.truth.Truth.assertThat
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.destination.TransfersNavKey
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TransfersNavItemTest {

    private val underTest = TransfersNavItem()

    @Test
    fun `test that id is transfers`() {
        assertThat(underTest.id).isEqualTo("transfers")
    }

    @Test
    fun `test that destination is the default transfers nav key`() {
        assertThat(underTest.destination).isEqualTo(TransfersNavKey())
    }

    @Test
    fun `test that preferred slot is none`() {
        assertThat(underTest.preferredSlot).isEqualTo(PreferredSlot.None)
    }

    @Test
    fun `test that item is available offline`() {
        assertThat(underTest.availableOffline).isTrue()
    }

    @Test
    fun `test that badge is null`() {
        assertThat(underTest.badge).isNull()
    }

    @Test
    fun `test that icon matches the menu transfers item icon`() {
        assertThat(underTest.icon).isEqualTo(IconPack.Medium.Thin.Outline.ArrowsUpDownCircle)
    }

    @Test
    fun `test that selected icon is null`() {
        assertThat(underTest.selectedIcon).isNull()
    }

    @Test
    fun `test that label matches the menu transfers item label`() {
        assertThat(underTest.label).isEqualTo(sharedR.string.general_section_transfers)
    }

    @Test
    fun `test that item is flagged with the customisable bottom navigation feature`() {
        assertThat(underTest.feature).isEqualTo(ApiFeatures.CustomisableBottomNavigation)
    }

    @Test
    fun `test that analytics event identifier is the transfers navigation identifier`() {
        assertThat(underTest.analyticsEventIdentifier)
            .isEqualTo(TransfersNavigationIdentifier)
    }
}
