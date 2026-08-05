package mega.privacy.android.feature.contact.requests.view

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import mega.android.core.ui.components.sheets.MegaModalBottomSheet
import mega.android.core.ui.components.sheets.MegaModalBottomSheetBackground
import mega.privacy.android.core.nodecomponents.list.NodeActionListTile
import mega.privacy.android.domain.entity.contacts.ContactRequestAction
import mega.privacy.android.feature.contact.requests.model.ContactRequestUiItem
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Per-request actions bottom sheet. Received requests offer Accept / Decline / Ignore; sent
 * requests offer Reinvite / Remove. The sheet only invokes [onAction]; it performs no domain work.
 *
 * @param request The request the actions apply to.
 * @param onDismiss Invoked when the sheet is dismissed without picking an action.
 * @param onAction Invoked with the picked [ContactRequestAction] for [request].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContactRequestActionsBottomSheet(
    request: ContactRequestUiItem,
    onDismiss: () -> Unit,
    onAction: (ContactRequestUiItem, ContactRequestAction) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val close = { action: ContactRequestAction ->
        coroutineScope
            .launch { sheetState.hide() }
            .invokeOnCompletion {
                onAction(request, action)
            }
    }

    MegaModalBottomSheet(
        modifier = Modifier.testTag(CONTACT_REQUEST_ACTIONS_SHEET_TAG),
        bottomSheetBackground = MegaModalBottomSheetBackground.Surface1,
        sheetState = sheetState,
        onDismissRequest = onDismiss,
    ) {
        ContactRequestActionsSheetContent(
            isOutgoing = request.isOutgoing,
            onAction = { close(it) },
        )
    }
}

/**
 * Stateless list of request actions rendered inside [ContactRequestActionsBottomSheet]. Extracted so
 * it can be previewed and screenshot-tested without the modal-sheet window.
 *
 * @param isOutgoing Whether to show the sent (Reinvite / Remove) or received
 * (Accept / Ignore / Decline) actions.
 * @param onAction Invoked with the picked action.
 * @param modifier
 */
@Composable
internal fun ContactRequestActionsSheetContent(
    isOutgoing: Boolean,
    onAction: (ContactRequestAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        if (isOutgoing) {
            NodeActionListTile(
                text = stringResource(sharedR.string.contact_reinvite),
                icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.RotateCw),
                onActionClicked = { onAction(ContactRequestAction.Remind) },
                modifier = Modifier.testTag(CONTACT_REQUEST_ACTION_REINVITE_TAG),
            )
            NodeActionListTile(
                text = stringResource(sharedR.string.general_remove),
                icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.X),
                isDestructive = true,
                onActionClicked = { onAction(ContactRequestAction.Delete) },
                modifier = Modifier.testTag(CONTACT_REQUEST_ACTION_REMOVE_TAG),
            )
        } else {
            NodeActionListTile(
                text = stringResource(sharedR.string.contact_accept),
                icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.CheckCircle),
                onActionClicked = { onAction(ContactRequestAction.Accept) },
                modifier = Modifier.testTag(CONTACT_REQUEST_ACTION_ACCEPT_TAG),
            )
            NodeActionListTile(
                text = stringResource(sharedR.string.contact_ignore),
                icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.SlashCircle),
                onActionClicked = { onAction(ContactRequestAction.Ignore) },
                modifier = Modifier.testTag(CONTACT_REQUEST_ACTION_IGNORE_TAG),
            )
            NodeActionListTile(
                text = stringResource(sharedR.string.contact_decline),
                icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.XCircle),
                isDestructive = true,
                onActionClicked = { onAction(ContactRequestAction.Deny) },
                modifier = Modifier.testTag(CONTACT_REQUEST_ACTION_DECLINE_TAG),
            )
        }
    }
}
