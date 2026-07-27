package mega.privacy.android.feature.contact.info.view.dialog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews

class RemoveContactConfirmationDialogScreenshotTest {

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun RemoveContactConfirmation() {
        AndroidThemeForPreviews {
            RemoveContactConfirmationDialog(
                onConfirm = {},
                onDismiss = {},
            )
        }
    }
}
