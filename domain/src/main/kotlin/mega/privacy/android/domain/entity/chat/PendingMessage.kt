package mega.privacy.android.domain.entity.chat

import mega.privacy.android.domain.entity.uri.UriPath

/**
 * Pending message
 *
 * @property id
 * @property chatId
 * @property transferUniqueId
 * @property type
 * @property uploadTimestamp
 * @property state
 * @property tempIdKarere
 * @property videoDownSampled
 * @property uriPath
 * @property nodeHandle
 * @property fingerprint
 * @property name
 * @property originalUriPath original location of the file to be attached, useful in case of media compression, where [uriPath] is changed to a temporary file that will be deleted once uploaded
 * @constructor Create empty Pending message
 */
data class PendingMessage(
    var id: Long = -1,
    var chatId: Long = -1,
    val transferUniqueId: Long = UNKNOWN_TRANSFER_ID,
    var type: Int = -1,
    var uploadTimestamp: Long = -1,
    var state: Int = PendingMessageState.PREPARING.value,
    var tempIdKarere: Long = -1,
    var videoDownSampled: String? = null,
    var uriPath: UriPath = UriPath(""),
    var nodeHandle: Long = -1,
    var fingerprint: String? = null,
    var name: String? = null,
    val originalUriPath: UriPath = UriPath("")
) {
    /**
     * True if it's a voice clip, false otherwise
     */
    val isVoiceClip: Boolean = type == TYPE_VOICE_CLIP

    constructor(
        chatId: Long,
        uploadTimestamp: Long,
        uriPath: UriPath,
        fingerprint: String?,
        name: String?,
    ) : this() {
        this.chatId = chatId
        this.uploadTimestamp = uploadTimestamp
        this.uriPath = uriPath
        this.fingerprint = fingerprint
        this.name = name
    }

    constructor(
        id: Long,
        chatId: Long,
        uploadTimestamp: Long,
        tempIdKarere: Long,
        uriPath: UriPath,
        fingerprint: String?,
        name: String?,
        nodeHandle: Long,
        state: Int,
    ) : this() {
        this.id = id
        this.chatId = chatId
        this.uploadTimestamp = uploadTimestamp
        this.tempIdKarere = tempIdKarere
        this.uriPath = uriPath
        this.fingerprint = fingerprint
        this.name = name
        this.nodeHandle = nodeHandle
        this.state = state
    }

    companion object {
        /**
         * Type for voice clip pending message
         */
        const val TYPE_VOICE_CLIP = 3

        /**
         * Id for pending messages where transfer is still unknown or not started
         */
        const val UNKNOWN_TRANSFER_ID = -1L
    }
}