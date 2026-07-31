package mega.privacy.android.feature.contact.picker

import androidx.compose.runtime.saveable.SaverScope
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class ContactSelectionStateTest {

    @Test
    fun `test that toggling an unselected handle selects it`() {
        val underTest = ContactSelectionState()

        underTest.toggleSelection(1L)

        assertThat(underTest.selectedHandles).containsExactly(1L)
        assertThat(underTest.selectedItemsCount).isEqualTo(1)
    }

    @Test
    fun `test that toggling a selected handle deselects it`() {
        val underTest = ContactSelectionState(initialSelectedHandles = setOf(1L, 2L))

        underTest.toggleSelection(1L)

        assertThat(underTest.selectedHandles).containsExactly(2L)
    }

    @Test
    fun `test that toggling an unselected phone email selects it`() {
        val underTest = ContactSelectionState()

        underTest.togglePhoneSelection("a@test.com")

        assertThat(underTest.selectedPhoneEmails).containsExactly("a@test.com")
        assertThat(underTest.selectedItemsCount).isEqualTo(1)
    }

    @Test
    fun `test that toggling a selected phone email deselects it`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneEmails = setOf("a@test.com", "b@test.com"),
        )

        underTest.togglePhoneSelection("a@test.com")

        assertThat(underTest.selectedPhoneEmails).containsExactly("b@test.com")
    }

    @Test
    fun `test that selectedItemsCount counts mega handles and phone and manual emails and phone numbers`() {
        val underTest = ContactSelectionState(
            initialSelectedHandles = setOf(1L, 2L),
            initialSelectedPhoneEmails = setOf("a@test.com"),
            initialSelectedManualEmails = setOf("m@test.com"),
            initialSelectedPhoneNumbers = setOf("+123"),
        )

        assertThat(underTest.selectedItemsCount).isEqualTo(5)
    }

    @Test
    fun `test that toggling an unselected phone number selects it`() {
        val underTest = ContactSelectionState()

        underTest.togglePhoneNumber("+123")

        assertThat(underTest.selectedPhoneNumbers).containsExactly("+123")
        assertThat(underTest.selectedItemsCount).isEqualTo(1)
    }

    @Test
    fun `test that toggling a selected phone number deselects it`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneNumbers = setOf("+123", "+456"),
        )

        underTest.togglePhoneNumber("+123")

        assertThat(underTest.selectedPhoneNumbers).containsExactly("+456")
    }

    @Test
    fun `test that selectPhoneNumbers adds without deselecting existing selection`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneNumbers = setOf("+123"),
        )

        underTest.selectPhoneNumbers(listOf("+456", "+789"))

        assertThat(underTest.selectedPhoneNumbers)
            .containsExactly("+123", "+456", "+789")
    }

    @Test
    fun `test that removePhoneNumber removes only the given number`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneNumbers = setOf("+123", "+456"),
        )

        underTest.removePhoneNumber("+123")

        assertThat(underTest.selectedPhoneNumbers).containsExactly("+456")
    }

    @Test
    fun `test that isPhoneNumberSelected returns true when the number is selected`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneNumbers = setOf("+123"),
        )

        assertThat(underTest.isPhoneNumberSelected("+123")).isTrue()
    }

    @Test
    fun `test that isPhoneNumberSelected returns false when the number is not selected`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneNumbers = setOf("+123"),
        )

        assertThat(underTest.isPhoneNumberSelected("+456")).isFalse()
    }

    @Test
    fun `test that selectManualEmail adds without deselecting existing selection`() {
        val underTest = ContactSelectionState(
            initialSelectedManualEmails = setOf("a@test.com"),
        )

        underTest.selectManualEmail("b@test.com")

        assertThat(underTest.selectedManualEmails)
            .containsExactly("a@test.com", "b@test.com")
    }

    @Test
    fun `test that removeManualEmail removes only the given email`() {
        val underTest = ContactSelectionState(
            initialSelectedManualEmails = setOf("a@test.com", "b@test.com"),
        )

        underTest.removeManualEmail("a@test.com")

        assertThat(underTest.selectedManualEmails).containsExactly("b@test.com")
    }

    @Test
    fun `test that isEmailSelected returns true when a manual email matches case-insensitively`() {
        val underTest = ContactSelectionState(
            initialSelectedManualEmails = setOf("Guest@Test.com"),
        )

        assertThat(underTest.isEmailSelected("guest@test.com")).isTrue()
    }

    @Test
    fun `test that isEmailSelected returns true when a phone email matches case-insensitively`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneEmails = setOf("phone@test.com"),
        )

        assertThat(underTest.isEmailSelected("PHONE@test.com")).isTrue()
    }

    @Test
    fun `test that isEmailSelected returns false when the email is not selected`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneEmails = setOf("phone@test.com"),
            initialSelectedManualEmails = setOf("manual@test.com"),
        )

        assertThat(underTest.isEmailSelected("other@test.com")).isFalse()
    }

    @Test
    fun `test that selectPhoneEmails adds without deselecting existing selection`() {
        val underTest = ContactSelectionState(
            initialSelectedPhoneEmails = setOf("a@test.com"),
        )

        underTest.selectPhoneEmails(listOf("b@test.com", "c@test.com"))

        assertThat(underTest.selectedPhoneEmails)
            .containsExactly("a@test.com", "b@test.com", "c@test.com")
    }

    @Test
    fun `test that deselectAll clears mega and phone and manual and phone number selection`() {
        val underTest = ContactSelectionState(
            initialSelectedHandles = setOf(1L, 2L, 3L),
            initialSelectedPhoneEmails = setOf("a@test.com"),
            initialSelectedManualEmails = setOf("m@test.com"),
            initialSelectedPhoneNumbers = setOf("+123"),
        )

        underTest.deselectAll()

        assertThat(underTest.selectedHandles).isEmpty()
        assertThat(underTest.selectedPhoneEmails).isEmpty()
        assertThat(underTest.selectedManualEmails).isEmpty()
        assertThat(underTest.selectedPhoneNumbers).isEmpty()
        assertThat(underTest.selectedItemsCount).isEqualTo(0)
    }

    @Test
    fun `test that the saver round trips mega handles and phone and manual emails and phone numbers`() {
        val original = ContactSelectionState(
            initialSelectedHandles = setOf(1L, 2L, 3L),
            initialSelectedPhoneEmails = setOf("a@test.com", "b@test.com"),
            initialSelectedManualEmails = setOf("m@test.com"),
            initialSelectedPhoneNumbers = setOf("+123", "+456"),
        )
        val saver = ContactSelectionState.Saver

        val saved = with(saver) { SaverScope { true }.save(original) }
        val restored = saved?.let { saver.restore(it) }

        assertThat(restored?.selectedHandles).isEqualTo(setOf(1L, 2L, 3L))
        assertThat(restored?.selectedPhoneEmails).isEqualTo(setOf("a@test.com", "b@test.com"))
        assertThat(restored?.selectedManualEmails).isEqualTo(setOf("m@test.com"))
        assertThat(restored?.selectedPhoneNumbers).isEqualTo(setOf("+123", "+456"))
    }

    @Test
    fun `test that the saver restores a legacy three list save with no phone numbers`() {
        val legacySave = listOf(
            listOf("1", "2"),
            listOf("a@test.com"),
            listOf("m@test.com"),
        )

        val restored = ContactSelectionState.Saver.restore(legacySave)

        assertThat(restored?.selectedHandles).isEqualTo(setOf(1L, 2L))
        assertThat(restored?.selectedPhoneEmails).isEqualTo(setOf("a@test.com"))
        assertThat(restored?.selectedManualEmails).isEqualTo(setOf("m@test.com"))
        assertThat(restored?.selectedPhoneNumbers).isEmpty()
    }
}
