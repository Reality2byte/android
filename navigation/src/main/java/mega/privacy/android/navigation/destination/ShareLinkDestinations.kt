package mega.privacy.android.navigation.destination

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Navigation key for the revamped Share link screen (feature/share-link).
 *
 * Gated behind the `ShareLinkRevamp` flag: when the flag is disabled the destination
 * redirects to the legacy [GetLinkNavKey] for nodes, or to [AlbumGetLinkNavKey] for an album.
 *
 * Exactly one of [handles] and [albumId] is populated. The destination turns the pair into a
 * sealed subject immediately; the flat fields exist only because the key is a serialization
 * boundary.
 *
 * @param handles List of node handles to share. A single handle is the single-node
 * "Share link" screen; multiple handles is the "Share links" multi-node screen.
 * @param albumId Id of the album to share, or null when sharing nodes.
 */
@Serializable
data class ShareLinkNavKey(
    val handles: List<Long> = emptyList(),
    val albumId: Long? = null,
) : NavKey

/**
 * Navigation key for the revamped Link settings editor screen (feature/share-link),
 * opened from the gear action on the Share link screen.
 *
 * Carries the same subject as the [ShareLinkNavKey] it was opened from.
 *
 * @param handles List of node handles whose link settings are being edited.
 * @param albumId Id of the album whose link settings are being edited, or null for nodes.
 */
@Serializable
data class LinkSettingsNavKey(
    val handles: List<Long> = emptyList(),
    val albumId: Long? = null,
) : NavKey
