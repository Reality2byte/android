package mega.privacy.android.feature.clouddrive.navigation

import com.google.common.truth.Truth.assertThat
import mega.privacy.android.domain.featuretoggle.ApiFeatures
import mega.privacy.android.navigation.contract.PreferredSlot
import mega.privacy.android.navigation.destination.SharesNavKey
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SharesNavItemTest {
    private val underTest = SharesNavItem()

    @Test
    fun `test that id is stable`() {
        assertThat(underTest.id).isEqualTo("shares")
    }

    @Test
    fun `test that preferred slot is None`() {
        assertThat(underTest.preferredSlot).isEqualTo(PreferredSlot.None)
    }

    @Test
    fun `test that item is flagged with the customisable bottom navigation feature`() {
        assertThat(underTest.feature).isEqualTo(ApiFeatures.CustomisableBottomNavigation)
    }

    @Test
    fun `test that destination is the shares nav key`() {
        assertThat(underTest.destination).isEqualTo(SharesNavKey)
    }

    @Test
    fun `test that item is not available offline`() {
        assertThat(underTest.availableOffline).isFalse()
    }

    @Test
    fun `test that badge is null`() {
        assertThat(underTest.badge).isNull()
    }

    @Test
    fun `test that label is the shared items string`() {
        assertThat(underTest.label)
            .isEqualTo(sharedR.string.video_section_videos_location_option_shared_items)
    }
}
