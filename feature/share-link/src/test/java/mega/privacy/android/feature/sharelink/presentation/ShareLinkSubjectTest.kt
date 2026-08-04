package mega.privacy.android.feature.sharelink.presentation

import com.google.common.truth.Truth.assertThat
import mega.privacy.android.navigation.destination.LinkSettingsNavKey
import mega.privacy.android.navigation.destination.ShareLinkNavKey
import org.junit.jupiter.api.Test

/**
 * Covers the seam where the flat, serializable nav keys become the sealed subject both screens
 * work in terms of.
 */
class ShareLinkSubjectTest {

    @Test
    fun `test that a share link key with handles becomes a nodes subject`() {
        val subject = ShareLinkNavKey(handles = listOf(1L, 2L)).subject()

        assertThat(subject).isEqualTo(ShareLinkSubject.Nodes(listOf(1L, 2L)))
    }

    @Test
    fun `test that a share link key with an album id becomes an album subject`() {
        val subject = ShareLinkNavKey(albumId = ALBUM_ID).subject()

        assertThat(subject).isEqualTo(ShareLinkSubject.Album(ALBUM_ID))
    }

    @Test
    fun `test that a link settings key with an album id becomes an album subject`() {
        val subject = LinkSettingsNavKey(albumId = ALBUM_ID).subject()

        assertThat(subject).isEqualTo(ShareLinkSubject.Album(ALBUM_ID))
    }

    @Test
    fun `test that a link settings key with handles becomes a nodes subject`() {
        val subject = LinkSettingsNavKey(handles = listOf(1L)).subject()

        assertThat(subject).isEqualTo(ShareLinkSubject.Nodes(listOf(1L)))
    }

    @Test
    fun `test that an empty share link key becomes an empty nodes subject`() {
        val subject = ShareLinkNavKey().subject()

        assertThat(subject).isEqualTo(ShareLinkSubject.Nodes(emptyList()))
        assertThat(subject.cacheKey).isNull()
    }

    @Test
    fun `test that the cache key is the album id for an album and the first handle for nodes`() {
        assertThat(ShareLinkSubject.Album(ALBUM_ID).cacheKey).isEqualTo(ALBUM_ID)
        assertThat(ShareLinkSubject.Nodes(listOf(7L, 8L)).cacheKey).isEqualTo(7L)
    }

    private companion object {
        const val ALBUM_ID = 987L
    }
}
