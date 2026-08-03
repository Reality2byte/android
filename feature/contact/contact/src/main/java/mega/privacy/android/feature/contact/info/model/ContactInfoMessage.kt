package mega.privacy.android.feature.contact.info.model

/**
 * One-shot feedback messages shown by the contact info screen in a snackbar.
 */
enum class ContactInfoMessage {

    /**
     * The nickname was set or updated successfully.
     */
    NicknameAdded,

    /**
     * The nickname was removed successfully.
     */
    NicknameRemoved,

    /**
     * Creating the 1:1 chat room with the contact failed.
     */
    ChatCreationError,

    /**
     * The microphone permission required to start a call was denied.
     */
    MicrophonePermissionDenied,
}
