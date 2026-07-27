package mega.privacy.android.feature.sharelink.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import mega.android.core.ui.components.MegaScaffoldWithTopAppBarScrollBehavior
import mega.android.core.ui.components.button.AnchoredButtonGroup
import mega.android.core.ui.components.toolbar.AppBarNavigationType
import mega.android.core.ui.components.toolbar.MegaTopAppBar
import mega.android.core.ui.model.Button
import mega.android.core.ui.model.menu.MenuActionWithClick
import mega.android.core.ui.model.menu.MenuActionWithIcon
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Revamped Share link result screen.
 *
 * The scaffold and its bars live here; the per-state body composables and the preview live in
 * `ShareLinkStateContent.kt` and `ShareLinkScreenPreview.kt` respectively.
 *
 * @param uiState The current [ShareLinkUiState].
 * @param onBack Invoked when the Close action is tapped.
 * @param onOpenSettings Invoked when the settings (gear) action is tapped.
 * @param onShareLink Invoked with the shareable link text when the bottom "Share link" button is
 * tapped: the single link (the key-less link when the key is shared separately) or, for multiple
 * nodes, every link joined by newlines.
 * @param onCopyLink Invoked when the copy icon on a link is tapped.
 * @param onCopyKey Invoked when the copy icon on the separate key card is tapped.
 * @param onCopyPassword Invoked when the copy icon on the password card is tapped.
 * @param onLinksCopied Invoked once when the multi-node screen opens and all links have been
 * copied to the clipboard automatically.
 * @param onSensitiveWarningConfirmed Invoked when the user confirms the hidden-items warning.
 * @param onSensitiveWarningDismissed Invoked when the user cancels the hidden-items warning.
 * @param onCopyrightAgreed Invoked when the user agrees to the first-time copyright consent.
 * @param onCopyrightDisagreed Invoked when the user declines the first-time copyright consent.
 * @param modifier Modifier for the scaffold.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareLinkScreen(
    uiState: ShareLinkUiState,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit,
    onShareLink: (String) -> Unit,
    onCopyLink: () -> Unit,
    onCopyKey: () -> Unit,
    modifier: Modifier = Modifier,
    onCopyPassword: () -> Unit = {},
    onLinksCopied: () -> Unit = {},
    onSensitiveWarningConfirmed: () -> Unit = {},
    onSensitiveWarningDismissed: () -> Unit = {},
    onCopyrightAgreed: () -> Unit = {},
    onCopyrightDisagreed: () -> Unit = {},
) {
    val linkCount = (uiState as? ShareLinkUiState.Data)?.handles?.size ?: 1

    MegaScaffoldWithTopAppBarScrollBehavior(
        modifier = modifier,
        topBar = {
            MegaTopAppBar(
                modifier = Modifier.testTag(SHARE_LINK_APP_BAR_TAG),
                title = if (uiState is ShareLinkUiState.CopyrightConsent) {
                    ""
                } else {
                    pluralStringResource(sharedR.plurals.label_share_links, linkCount)
                },
                subtitle = null,
                navigationType = AppBarNavigationType.Close(onBack),
                actions = buildList {
                    if (uiState is ShareLinkUiState.Data && !uiState.isMultiNode) {
                        add(MenuActionWithClick(ShareLinkSettingsAction, onOpenSettings))
                    }
                },
            )
        },
        bottomBar = {
            when (uiState) {
                is ShareLinkUiState.Data -> {
                    val shareText =
                        pluralStringResource(sharedR.plurals.label_share_links, linkCount)
                    AnchoredButtonGroup(
                        modifier = Modifier.fillMaxWidth(),
                        buttonGroup = listOf(
                            {
                                Button.PrimaryButton(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag(SHARE_LINK_SHARE_BUTTON_TAG),
                                    text = shareText,
                                    onClick = { onShareLink(uiState.shareableLinksText()) },
                                )
                            },
                        ),
                    )
                }

                ShareLinkUiState.CopyrightConsent -> AnchoredButtonGroup(
                    modifier = Modifier.fillMaxWidth(),
                    buttonGroup = listOf(
                        {
                            Button.PrimaryButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(SHARE_LINK_COPYRIGHT_AGREE_TAG),
                                text = stringResource(sharedR.string.copyright_action_agree),
                                onClick = onCopyrightAgreed,
                            )
                        },
                        {
                            Button.SecondaryButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag(SHARE_LINK_COPYRIGHT_DISAGREE_TAG),
                                text = stringResource(sharedR.string.general_dialog_cancel_button),
                                onClick = onCopyrightDisagreed,
                            )
                        },
                    ),
                )

                else -> Unit
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when (uiState) {
                ShareLinkUiState.Loading -> ShareLinkLoading()
                ShareLinkUiState.Error -> ShareLinkError()
                ShareLinkUiState.CopyrightConsent -> CopyrightConsent()
                is ShareLinkUiState.SensitiveWarning -> {
                    ShareLinkLoading()
                    SensitiveItemsWarningDialog(
                        type = uiState.type,
                        nodeCount = uiState.nodeCount,
                        onConfirm = onSensitiveWarningConfirmed,
                        onDismiss = onSensitiveWarningDismissed,
                    )
                }

                is ShareLinkUiState.Data -> if (uiState.isMultiNode) {
                    MultiNodeContent(
                        uiState = uiState,
                        onCopyLink = onCopyLink,
                        onLinksCopied = onLinksCopied,
                    )
                } else {
                    ShareLinkContent(
                        uiState = uiState,
                        onCopyLink = onCopyLink,
                        onCopyKey = onCopyKey,
                        onCopyPassword = onCopyPassword,
                    )
                }
            }
        }
    }
}

/**
 * Toolbar action that opens the Link settings editor from the Share link screen.
 */
internal data object ShareLinkSettingsAction : MenuActionWithIcon {
    @Composable
    override fun getIconPainter() =
        rememberVectorPainter(IconPack.Medium.Thin.Outline.GearSix)

    override val testTag = "share_link_screen:action_settings"

    @Composable
    override fun getDescription() = stringResource(sharedR.string.general_settings)
}

internal const val SHARE_LINK_APP_BAR_TAG = "share_link_screen:app_bar"
internal const val SHARE_LINK_SHARE_BUTTON_TAG = "share_link_screen:button_share"
internal const val SHARE_LINK_NODE_HEADER_TAG = "share_link_screen:node_header"
internal const val SHARE_LINK_MULTI_NODE_LIST_TAG = "share_link_screen:multi_node_list"
internal const val SHARE_LINK_SENSITIVE_WARNING_TAG = "share_link_screen:sensitive_warning"
internal const val SHARE_LINK_ACCESS_BANNER_TAG = "share_link_screen:access_banner"
internal const val SHARE_LINK_LOADING_TAG = "share_link_screen:loading"
internal const val SHARE_LINK_ERROR_TAG = "share_link_screen:error"
internal const val SHARE_LINK_COPYRIGHT_TAG = "share_link_screen:copyright"
internal const val SHARE_LINK_COPYRIGHT_AGREE_TAG = "share_link_screen:copyright_agree"
internal const val SHARE_LINK_COPYRIGHT_DISAGREE_TAG = "share_link_screen:copyright_disagree"
