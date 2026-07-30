package mega.privacy.android.feature.contact.picker

/**
 * Outcome of inviting a scanned contact, surfaced to the user as one-shot feedback.
 */
enum class ScannedContactInviteFeedback {
    /**
     * The invitation was sent (or resent) successfully.
     */
    Sent,

    /**
     * The invitation could not be sent.
     */
    Failed,
}
