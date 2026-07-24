package mega.privacy.android.feature.sharelink.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.image.MegaIcon
import mega.android.core.ui.components.surface.BoxSurface
import mega.android.core.ui.components.surface.SurfaceColor
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.AppTheme
import mega.android.core.ui.theme.values.IconColor
import mega.android.core.ui.theme.values.TextColor
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

/**
 * The read-only "Share link details" card group.
 *
 * Shows the link card and, depending on the sharing options:
 * - when [key] is non-null (the link and key are shared separately), a second card with the
 *   decryption key and its own copy action;
 * - when [passwordProtected] is true, a lock + "Password protected" helper under the link, and
 *   when [maskedPassword] is non-null a separate card with the (masked) password and a copy action
 *   that copies the real password.
 *
 * @param link The public share link shown to the user.
 * @param onCopyLink Invoked when the link's copy icon is tapped.
 * @param modifier Modifier for the card group.
 * @param key The decryption key shown in a separate card, or null when the key is part of the link.
 * @param onCopyKey Invoked when the key's copy icon is tapped.
 * @param passwordProtected Whether to show the "Password protected" helper under the link.
 * @param maskedPassword The password shown (already masked) in a separate card, or null when the
 * link is not password protected.
 * @param onCopyPassword Invoked when the password's copy icon is tapped; copies the real password.
 */
@Composable
fun ShareLinkDetails(
    link: String,
    onCopyLink: () -> Unit,
    modifier: Modifier = Modifier,
    key: String? = null,
    onCopyKey: () -> Unit = {},
    passwordProtected: Boolean = false,
    maskedPassword: String? = null,
    onCopyPassword: () -> Unit = {},
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        BoxSurface(
            surfaceColor = SurfaceColor.Surface1,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .testTag(SHARE_LINK_DETAILS_TAG),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ShareLinkDetailRow(
                    label = stringResource(sharedR.string.album_get_link_link_section_title),
                    value = link,
                    onCopy = onCopyLink,
                )
                if (passwordProtected) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.testTag(SHARE_LINK_PASSWORD_PROTECTED_TAG),
                    ) {
                        MegaIcon(
                            modifier = Modifier.size(16.dp),
                            painter = rememberVectorPainter(IconPack.Medium.Thin.Outline.Lock),
                            tint = IconColor.Secondary,
                            contentDescription = null,
                        )
                        MegaText(
                            text = stringResource(sharedR.string.share_link_password_protected_label),
                            textColor = TextColor.Secondary,
                            style = AppTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
        if (key != null) {
            BoxSurface(
                surfaceColor = SurfaceColor.Surface1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(SHARE_LINK_KEY_DETAILS_TAG),
            ) {
                ShareLinkDetailRow(
                    modifier = Modifier.padding(16.dp),
                    label = stringResource(sharedR.string.album_get_link_decryption_key_section_title),
                    value = key,
                    onCopy = onCopyKey,
                    copyTestTag = SHARE_LINK_KEY_COPY_TAG,
                )
            }
        }
        if (maskedPassword != null) {
            BoxSurface(
                surfaceColor = SurfaceColor.Surface1,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(SHARE_LINK_PASSWORD_DETAILS_TAG),
            ) {
                ShareLinkDetailRow(
                    modifier = Modifier.padding(16.dp),
                    label = stringResource(sharedR.string.password_text),
                    value = maskedPassword,
                    onCopy = onCopyPassword,
                    copyTestTag = SHARE_LINK_PASSWORD_COPY_TAG,
                )
            }
        }
    }
}

@CombinedThemePreviews
@Composable
private fun ShareLinkDetailsPreview() {
    AndroidThemeForPreviews {
        ShareLinkDetails(
            link = "https://mega.nz/file/abc123#decryptionKey",
            onCopyLink = {},
        )
    }
}

@CombinedThemePreviews
@Composable
private fun ShareLinkDetailsSeparateKeyPreview() {
    AndroidThemeForPreviews {
        ShareLinkDetails(
            link = "https://mega.nz/file/abc123",
            onCopyLink = {},
            key = "decryptionKey",
            onCopyKey = {},
        )
    }
}

@CombinedThemePreviews
@Composable
private fun ShareLinkDetailsPasswordPreview() {
    AndroidThemeForPreviews {
        ShareLinkDetails(
            link = "https://mega.nz/file/abc123#passwordEncrypted",
            onCopyLink = {},
            passwordProtected = true,
            maskedPassword = "••••••••••••",
            onCopyPassword = {},
        )
    }
}

internal const val SHARE_LINK_DETAILS_TAG = "share_link_details:card"
internal const val SHARE_LINK_KEY_DETAILS_TAG = "share_link_details:key_card"
internal const val SHARE_LINK_KEY_COPY_TAG = "share_link_details:key_copy"
internal const val SHARE_LINK_PASSWORD_PROTECTED_TAG = "share_link_details:password_protected"
internal const val SHARE_LINK_PASSWORD_DETAILS_TAG = "share_link_details:password_card"
internal const val SHARE_LINK_PASSWORD_COPY_TAG = "share_link_details:password_copy"
