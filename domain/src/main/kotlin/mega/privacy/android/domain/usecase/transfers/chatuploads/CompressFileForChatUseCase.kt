package mega.privacy.android.domain.usecase.transfers.chatuploads

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import mega.privacy.android.domain.entity.uri.UriPath
import mega.privacy.android.domain.usecase.chat.ChatUploadCompressionState
import mega.privacy.android.domain.usecase.chat.ChatUploadNotCompressedReason
import mega.privacy.android.domain.usecase.file.IsImageFileUseCase
import mega.privacy.android.domain.usecase.file.IsVideoFileUseCase
import java.io.File
import javax.inject.Inject

/**
 * Compress a file before uploading it to the chat and return the state of the compression.
 */
class CompressFileForChatUseCase @Inject constructor(
    private val chatAttachmentNeedsCompressionUseCase: ChatAttachmentNeedsCompressionUseCase,
    private val isImageFileUseCase: IsImageFileUseCase,
    private val isVideoFileUseCase: IsVideoFileUseCase,
    private val downscaleImageForChatUseCase: DownscaleImageForChatUseCase,
    private val compressVideoForChatUseCase: CompressVideoForChatUseCase,
) {
    /**
     * Invoke
     */
    suspend operator fun invoke(uriPath: UriPath): Flow<ChatUploadCompressionState> {
        return when {
            !chatAttachmentNeedsCompressionUseCase(uriPath) -> null
            isImageFileUseCase(uriPath) -> {
                downscaleImageForChatUseCase(uriPath)
            }

            isVideoFileUseCase(uriPath) -> {
                compressVideoForChatUseCase(uriPath)
            }

            else -> null
        } ?: flowOf(
            ChatUploadCompressionState.NotCompressed(ChatUploadNotCompressedReason.CompressionNotNeeded)
        )
    }
}