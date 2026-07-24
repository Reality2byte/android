package mega.privacy.android.feature.transfers.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import mega.privacy.android.icon.pack.R as iconPackR
import mega.privacy.android.shared.resources.R as sharedR
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions

@RunWith(AndroidJUnit4::class)
class FailedTransferItemTest {

    @get:Rule
    var composeRule = createComposeRule()

    private val name = "File name.pdf"
    private val error = "Failed"
    private val onRetry = mock<() -> Unit>()
    private val onClear = mock<() -> Unit>()

    @Test
    fun `test that failed download shows correctly`() {
        initComposeRuleContent(
            CompletedTransferUI(
                isDownload = true,
                fileTypeResId = iconPackR.drawable.ic_pdf_medium_solid,
                previewUri = null,
                fileName = name,
                location = null,
                error = error,
                sizeString = "10 MB",
                date = "10 Aug 2024 19:09",
            )
        )
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_IMAGE).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_NAME).assertIsDisplayed()
            onNodeWithText(name).assertIsDisplayed()
            onNodeWithText(error).assertIsDisplayed()
        }
    }

    @Test
    fun `test that cancelled download shows correctly`() {
        initComposeRuleContent(
            CompletedTransferUI(
                isDownload = true,
                fileTypeResId = iconPackR.drawable.ic_pdf_medium_solid,
                previewUri = null,
                fileName = name,
                location = null,
                error = null,
                sizeString = "10 MB",
                date = "10 Aug 2024 19:09",
            )
        )
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_IMAGE).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_NAME).assertIsDisplayed()
            onNodeWithText(name).assertIsDisplayed()
            onNodeWithText(
                InstrumentationRegistry.getInstrumentation().targetContext.getString(sharedR.string.transfers_section_cancelled)
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `test that failed upload shows correctly`() {
        initComposeRuleContent(
            CompletedTransferUI(
                isDownload = false,
                fileTypeResId = iconPackR.drawable.ic_pdf_medium_solid,
                previewUri = null,
                fileName = name,
                location = null,
                error = error,
                sizeString = "10 MB",
                date = "10 Aug 2024 19:09",
            )
        )
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_IMAGE).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_NAME).assertIsDisplayed()
            onNodeWithText(name).assertIsDisplayed()
            onNodeWithText(error).assertIsDisplayed()
        }
    }

    @Test
    fun `test that cancelled upload shows correctly`() {
        initComposeRuleContent(
            CompletedTransferUI(
                isDownload = false,
                fileTypeResId = iconPackR.drawable.ic_pdf_medium_solid,
                previewUri = null,
                fileName = name,
                location = null,
                error = null,
                sizeString = "10 MB",
                date = "10 Aug 2024 19:09",
            )
        )
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_IMAGE).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_NAME).assertIsDisplayed()
            onNodeWithText(name).assertIsDisplayed()
            onNodeWithText(
                InstrumentationRegistry.getInstrumentation().targetContext.getString(sharedR.string.transfers_section_cancelled)
            ).assertIsDisplayed()
        }
    }

    @Test
    fun `test that selected active transfer shows correctly`() {
        initComposeRuleContent(
            CompletedTransferUI(
                isDownload = false,
                fileTypeResId = iconPackR.drawable.ic_pdf_medium_solid,
                previewUri = null,
                fileName = name,
                location = null,
                error = null,
                sizeString = "10 MB",
                date = "10 Aug 2024 19:09",
                isSelected = true,
            )
        )
        with(composeRule) {
            onNodeWithTag(TEST_TAG_TRANSFER_SELECTED).assertIsDisplayed()
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_IMAGE).assertIsDisplayed()
        }
    }

    @Test
    fun `test that short swipe to left does not invoke onClear`() {
        initComposeRuleContent(failedTransfer())
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).performTouchInput {
                swipeLeft(startX = width.toFloat(), endX = width * 0.8f)
            }
            waitForIdle()
        }
        verifyNoInteractions(onClear, onRetry)
    }

    @Test
    fun `test that swipe to left invokes onClear when passing the dismiss threshold`() {
        initComposeRuleContent(failedTransfer())
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).performTouchInput {
                swipeLeft()
            }
            waitForIdle()
        }
        verify(onClear).invoke()
        verifyNoInteractions(onRetry)
    }

    @Test
    fun `test that short swipe to right does not invoke onRetry`() {
        initComposeRuleContent(failedTransfer())
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).performTouchInput {
                swipeRight(startX = 0f, endX = width * 0.2f)
            }
            waitForIdle()
        }
        verifyNoInteractions(onRetry, onClear)
    }

    @Test
    fun `test that swipe to right invokes onRetry when passing the dismiss threshold`() {
        initComposeRuleContent(failedTransfer())
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).performTouchInput {
                swipeRight()
            }
            waitForIdle()
        }
        verify(onRetry).invoke()
        verifyNoInteractions(onClear)
    }

    @Test
    fun `test that swipe reversing direction with a short offset does not invoke any action`() {
        initComposeRuleContent(failedTransfer())
        with(composeRule) {
            onNodeWithTag(TEST_TAG_COMPLETED_TRANSFER_ITEM).performTouchInput {
                down(Offset(width * 0.6f, centerY))
                moveTo(Offset(width * 0.3f, centerY), delayMillis = 100)
                moveTo(Offset(width * 0.7f, centerY), delayMillis = 100)
                up()
            }
            waitForIdle()
        }
        verifyNoInteractions(onRetry, onClear)
    }

    private fun failedTransfer() = CompletedTransferUI(
        isDownload = true,
        fileTypeResId = iconPackR.drawable.ic_pdf_medium_solid,
        previewUri = null,
        fileName = name,
        location = null,
        error = error,
        sizeString = "10 MB",
        date = "10 Aug 2024 19:09",
    )

    private fun initComposeRuleContent(completedTransferUI: CompletedTransferUI) =
        with(completedTransferUI) {
            composeRule.setContent {
                FailedTransferItem(
                    isDownload = isDownload,
                    fileTypeResId = fileTypeResId,
                    previewUri = previewUri,
                    fileName = fileName,
                    error = error,
                    isSelected = isSelected,
                    enableSwipeToDismiss = true,
                    onMoreClicked = mock(),
                    onRetry = onRetry,
                    onClear = onClear,
                )
            }
        }
}