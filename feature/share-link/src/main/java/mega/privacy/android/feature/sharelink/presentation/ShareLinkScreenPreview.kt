package mega.privacy.android.feature.sharelink.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.icon.pack.R as iconPackR

/**
 * Renders every [ShareLinkUiState] the screen can show, in light and dark, from a single preview
 * driven by [ShareLinkUiStateProvider]. Add a state to the provider to get its preview.
 */
@CombinedThemePreviews
@Composable
private fun ShareLinkScreenPreview(
    @PreviewParameter(ShareLinkUiStateProvider::class) uiState: ShareLinkUiState,
) {
    AndroidThemeForPreviews {
        ShareLinkScreen(
            uiState = uiState,
            onBack = {},
            onOpenSettings = {},
            onShareLink = {},
            onCopyLink = {},
            onCopyKey = {},
        )
    }
}

/**
 * Supplies one instance of each [ShareLinkUiState] variant for [ShareLinkScreenPreview].
 */
internal class ShareLinkUiStateProvider : PreviewParameterProvider<ShareLinkUiState> {
    override val values: Sequence<ShareLinkUiState> = sequenceOf(
        ShareLinkUiState.Loading,
        ShareLinkUiState.Error,
        ShareLinkUiState.CopyrightConsent,
        ShareLinkUiState.SensitiveWarning(SensitiveWarningType.Items, nodeCount = 1),
        singleNode,
        singleNode.copy(isKeySeparate = true),
        singleNode.copy(
            isPasswordSet = true,
            password = "s3cretPass",
            linkWithPassword = "https://mega.nz/#P!encryptedLink",
        ),
        multiNode,
        album,
        album.copy(isKeySeparate = true),
    )

    private companion object {
        val singleNode = ShareLinkUiState.Data(
            nodeLinks = listOf(
                ShareLinkNodeItem(
                    handle = 1L,
                    name = "Presentation.pdf",
                    isFolder = false,
                    iconRes = iconPackR.drawable.ic_pdf_medium_solid,
                    sizeInBytes = 10L * 1024 * 1024,
                    modificationTime = 1_749_000_000L,
                    childFolderCount = null,
                    childFileCount = null,
                    link = "https://mega.nz/file/abc123#decryptionKey",
                    linkWithoutKey = "https://mega.nz/file/abc123",
                    key = "decryptionKey",
                ),
            ),
            accountType = null,
        )

        val multiNode = ShareLinkUiState.Data(
            nodeLinks = listOf(
                ShareLinkNodeItem(
                    handle = 1L,
                    name = "Documents",
                    isFolder = true,
                    iconRes = iconPackR.drawable.ic_folder_medium_solid,
                    sizeInBytes = null,
                    modificationTime = null,
                    childFolderCount = 6,
                    childFileCount = 12,
                    link = "https://mega.nz/folder/abc123#folderKey",
                    linkWithoutKey = "https://mega.nz/folder/abc123",
                    key = "folderKey",
                ),
                ShareLinkNodeItem(
                    handle = 2L,
                    name = "Presentation.pdf",
                    isFolder = false,
                    iconRes = iconPackR.drawable.ic_pdf_medium_solid,
                    sizeInBytes = 10L * 1024 * 1024,
                    modificationTime = 1_749_000_000L,
                    childFolderCount = null,
                    childFileCount = null,
                    link = "https://mega.nz/file/def456#fileKey",
                    linkWithoutKey = "https://mega.nz/file/def456",
                    key = "fileKey",
                ),
            ),
            accountType = null,
        )

        // No cover path, so the header renders its placeholder — a real thumbnail cannot be
        // loaded from disk in a preview.
        val album = ShareLinkUiState.Data(
            nodeLinks = listOf(
                ShareLinkNodeItem(
                    handle = 99L,
                    name = "Lisbon",
                    isFolder = false,
                    iconRes = null,
                    sizeInBytes = null,
                    modificationTime = null,
                    childFolderCount = null,
                    childFileCount = null,
                    link = "https://mega.nz/collection/xyz789#albumKey",
                    linkWithoutKey = "https://mega.nz/collection/xyz789",
                    key = "albumKey",
                ),
            ),
            accountType = null,
            album = ShareLinkAlbumInfo(photoCount = 6, coverThumbnailPath = null),
        )
    }
}
