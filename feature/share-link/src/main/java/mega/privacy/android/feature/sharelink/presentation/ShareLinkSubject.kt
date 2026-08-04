package mega.privacy.android.feature.sharelink.presentation

/**
 * What is being shared: one or more nodes, or an album.
 *
 * The Share link and Link settings screens serve both, so the subject is explicit rather than a
 * nullable album id alongside the handles — the branches then stay exhaustive.
 */
sealed interface ShareLinkSubject {

    /**
     * The id this subject's client-side link options are cached under — the password and
     * separate-key preferences, which are choices about how to share rather than properties of
     * what is shared. Null when there is nothing to key on.
     */
    val cacheKey: Long?

    /**
     * One or more nodes.
     *
     * @property handles Node handles, in selection order.
     */
    data class Nodes(val handles: List<Long>) : ShareLinkSubject {
        override val cacheKey: Long? get() = handles.firstOrNull()
    }

    /**
     * A user album.
     *
     * @property albumId Id of the album being shared.
     */
    data class Album(val albumId: Long) : ShareLinkSubject {
        override val cacheKey: Long get() = albumId
    }
}
