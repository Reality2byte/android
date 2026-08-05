package mega.privacy.android.feature.contact.requests.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import mega.android.core.ui.components.state.EmptyStateView
import mega.privacy.android.feature.contact.requests.model.ContactRequestTab
import mega.privacy.android.icon.pack.R as iconPackR
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Empty state shown when a contact requests tab has no entries.
 *
 * @param tab The tab whose empty state is being rendered; selects the received vs sent copy/icon.
 * @param modifier
 */
@Composable
internal fun ContactRequestsEmptyView(
    tab: ContactRequestTab,
    modifier: Modifier = Modifier,
) {
    val illustration = when (tab) {
        ContactRequestTab.Received -> iconPackR.drawable.ic_user_arrow_in_glass
        ContactRequestTab.Sent -> iconPackR.drawable.ic_user_arrow_out_glass
    }
    val title = when (tab) {
        ContactRequestTab.Received -> stringResource(sharedR.string.received_requests_empty)
        ContactRequestTab.Sent -> stringResource(sharedR.string.sent_requests_empty)
    }
    EmptyStateView(
        modifier = modifier.fillMaxSize(),
        imagePainter = painterResource(illustration),
        title = title,
    )
}
