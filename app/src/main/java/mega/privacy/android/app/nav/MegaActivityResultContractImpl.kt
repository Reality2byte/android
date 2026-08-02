package mega.privacy.android.app.nav

import android.content.Context
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import dagger.hilt.android.qualifiers.ApplicationContext
import mega.privacy.android.app.activities.contract.AddToAlbumActivityContract
import mega.privacy.android.app.activities.contract.HiddenNodeOnboardingActivityContract
import mega.privacy.android.app.activities.contract.NameCollisionActivityContract
import mega.privacy.android.app.activities.contract.SelectFolderToCopyActivityContract
import mega.privacy.android.app.activities.contract.SelectFolderToMoveActivityContract
import mega.privacy.android.app.activities.contract.SendToChatActivityContract
import mega.privacy.android.app.activities.contract.VersionsFileActivityContract
import mega.privacy.android.app.activities.contract.VideoToPlaylistActivityContract
import mega.privacy.android.app.camera.InAppCameraLauncher
import mega.privacy.android.app.nav.contract.OpenMultipleDocumentsPersistable
import mega.privacy.android.domain.entity.node.NameCollision
import mega.privacy.android.navigation.MegaActivityResultContract
import mega.privacy.android.navigation.camera.CameraArg
import javax.inject.Inject

/**
 * Implementation of MegaActivityResultContract that provides all the necessary
 * ActivityResultContract instances for node-related operations.
 */
class MegaActivityResultContractImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : MegaActivityResultContract {

    override val versionsFileActivityResultContract: VersionsFileActivityContract =
        VersionsFileActivityContract()

    override val selectFolderToMoveActivityResultContract: SelectFolderToMoveActivityContract =
        SelectFolderToMoveActivityContract()

    override val selectFolderToCopyActivityResultContract: SelectFolderToCopyActivityContract =
        SelectFolderToCopyActivityContract()

    override val sendToChatActivityResultContract: SendToChatActivityContract =
        SendToChatActivityContract()

    override val hiddenNodeOnboardingActivityResultContract: HiddenNodeOnboardingActivityContract =
        HiddenNodeOnboardingActivityContract()

    override val inAppCameraResultContract: ActivityResultContract<CameraArg, Uri?>
        get() = InAppCameraLauncher()

    override val nameCollisionActivityContract: ActivityResultContract<ArrayList<NameCollision>, String?>
        get() = NameCollisionActivityContract()

    override val openMultipleDocumentsPersistable: ActivityResultContract<Array<String>, List<@JvmSuppressWildcards Uri>>
        get() = OpenMultipleDocumentsPersistable(context.contentResolver)

    override val addToAlbumResultContract: ActivityResultContract<Pair<Array<Long>, Int>, String?>
        get() = AddToAlbumActivityContract()

    override val videoToPlaylistActivityContract: VideoToPlaylistActivityContract =
        VideoToPlaylistActivityContract()
}