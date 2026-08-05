package mega.privacy.android.feature.contact.requests.model

import androidx.compose.runtime.Stable
import mega.privacy.android.shared.contact.model.ContactItemUiState

/**
 * UI row model for a single contact request.
 *
 * @property handle Stable identifier for the request; usable as a `LazyColumn` key and to
 * dispatch actions back to the caller.
 * @property isOutgoing Whether this is a sent (outgoing) request; `false` for a received
 * (incoming) one. Determines which set of actions the bottom sheet offers.
 * @property contact Pre-resolved presentational data (display name, email, avatar) for the row.
 * @property createdTime Pre-formatted creation time rendered as the row subtitle. Formatting is
 * done by the mapper/ViewModel; this screen only renders the string.
 */
@Stable
data class ContactRequestUiItem(
    val handle: Long,
    val isOutgoing: Boolean,
    val contact: ContactItemUiState,
    val createdTime: String,
)
