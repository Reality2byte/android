package mega.privacy.android.feature.contact.info.view.dialog

import androidx.compose.runtime.Composable
import com.android.tools.screenshot.PreviewTest
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews

class NicknameDialogScreenshotTest {

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun NicknameDialogSetMode() {
        AndroidThemeForPreviews {
            NicknameDialog(
                nickname = null,
                onConfirm = {},
                onRemove = {},
                onDismiss = {},
            )
        }
    }

    @PreviewTest
    @CombinedThemePreviews
    @Composable
    fun NicknameDialogEditMode() {
        AndroidThemeForPreviews {
            NicknameDialog(
                nickname = "Ally",
                onConfirm = {},
                onRemove = {},
                onDismiss = {},
            )
        }
    }
}
