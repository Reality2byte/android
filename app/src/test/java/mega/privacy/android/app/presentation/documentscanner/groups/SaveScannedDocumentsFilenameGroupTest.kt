package mega.privacy.android.app.presentation.documentscanner.groups

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.text.TextRange
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import mega.privacy.android.app.presentation.documentscanner.model.ScanFileType
import mega.privacy.android.domain.entity.documentscanner.ScanFilenameValidationStatus
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test class for [SaveScannedDocumentsFilenameGroup]
 */
@RunWith(AndroidJUnit4::class)
internal class SaveScannedDocumentsFilenameGroupTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test that the ui components are displayed`() {
        composeTestRule.setContent {
            SaveScannedDocumentsFilenameGroup(
                filename = "Filename.pdf",
                filenameValidationStatus = ScanFilenameValidationStatus.ValidFilename,
                scanFileType = ScanFileType.Pdf,
                onFilenameChanged = {},
                onFilenameConfirmed = {},
            )
        }

        composeTestRule.onNodeWithTag(SAVE_SCANNED_DOCUMENTS_FILENAME_GROUP_HEADER)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(SAVE_SCANNED_DOCUMENTS_FILENAME_GROUP_FILE_TYPE_IMAGE)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(SAVE_SCANNED_DOCUMENTS_FILENAME_GROUP_FILENAME_TEXT_FIELD)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(SAVE_SCANNED_DOCUMENTS_FILENAME_GROUP_EDIT_FILENAME_IMAGE)
            .assertIsDisplayed()
    }

    @Test
    fun `test that tapping the edit button selects the filename without its extension`() {
        composeTestRule.setContent {
            SaveScannedDocumentsFilenameGroup(
                filename = "Filename.pdf",
                filenameValidationStatus = ScanFilenameValidationStatus.ValidFilename,
                scanFileType = ScanFileType.Pdf,
                onFilenameChanged = {},
                onFilenameConfirmed = {},
            )
        }

        composeTestRule.onNodeWithTag(SAVE_SCANNED_DOCUMENTS_FILENAME_GROUP_EDIT_FILENAME_IMAGE)
            .performClick()

        assertThat(currentSelection()).isEqualTo(TextRange(0, "Filename".length))
    }

    @Test
    fun `test that tapping the field selects the filename without its extension`() {
        composeTestRule.setContent {
            SaveScannedDocumentsFilenameGroup(
                filename = "Filename.pdf",
                filenameValidationStatus = ScanFilenameValidationStatus.ValidFilename,
                scanFileType = ScanFileType.Pdf,
                onFilenameChanged = {},
                onFilenameConfirmed = {},
            )
        }

        composeTestRule.onNode(hasSetTextAction()).performClick()
        composeTestRule.waitForIdle()

        assertThat(currentSelection()).isEqualTo(TextRange(0, "Filename".length))
    }

    private fun currentSelection(): TextRange? =
        composeTestRule.onNode(hasSetTextAction())
            .fetchSemanticsNode()
            .config.getOrNull(SemanticsProperties.TextSelectionRange)
}
