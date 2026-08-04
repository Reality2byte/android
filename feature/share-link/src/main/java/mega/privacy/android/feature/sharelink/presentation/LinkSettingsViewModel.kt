package mega.privacy.android.feature.sharelink.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.domain.entity.node.FolderNode
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.usecase.GetNodeByIdUseCase
import mega.privacy.android.domain.usecase.GetPasswordStrengthUseCase
import mega.privacy.android.domain.usecase.account.MonitorAccountDetailUseCase
import mega.privacy.android.domain.usecase.filelink.EncryptLinkWithPasswordUseCase
import mega.privacy.android.domain.usecase.node.ExportNodeUseCase
import mega.privacy.android.feature.sharelink.session.LinkPassword
import mega.privacy.android.feature.sharelink.session.ShareLinkPasswordCache
import mega.privacy.android.feature.sharelink.session.ShareLinkSeparateKeyCache
import timber.log.Timber
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for the revamped Link settings editor screen.
 *
 * Holds the editable security-option selection (separate key, expiry, password), tracks whether
 * it differs from the initial state so Save can enable, and on [onSave] applies the changes to the
 * node's public link via [ExportNodeUseCase] (expiry) and [EncryptLinkWithPasswordUseCase]
 * (password). The account type drives Pro gating of the expiry and password rows.
 */
@HiltViewModel(assistedFactory = LinkSettingsViewModel.Factory::class)
class LinkSettingsViewModel @AssistedInject constructor(
    @Assisted private val args: Args,
    private val getNodeByIdUseCase: GetNodeByIdUseCase,
    private val exportNodeUseCase: ExportNodeUseCase,
    private val encryptLinkWithPasswordUseCase: EncryptLinkWithPasswordUseCase,
    private val getPasswordStrengthUseCase: GetPasswordStrengthUseCase,
    private val monitorAccountDetailUseCase: MonitorAccountDetailUseCase,
    private val passwordCache: ShareLinkPasswordCache,
    private val separateKeyCache: ShareLinkSeparateKeyCache,
) : ViewModel() {

    private val handle: Long? = args.subject.cacheKey
    private val isAlbum: Boolean = args.subject is ShareLinkSubject.Album
    private val cachedPassword: LinkPassword? =
        handle?.takeUnless { isAlbum }?.let(passwordCache::get)
    private val cachedSeparateKey: Boolean = handle?.let(separateKeyCache::get) ?: false

    private val _uiState = MutableStateFlow(
        LinkSettingsUiState(
            isAlbum = isAlbum,
            isSeparateKeyEnabled = cachedSeparateKey,
            initialSeparateKeyEnabled = cachedSeparateKey,
            isPasswordEnabled = cachedPassword != null,
            isPasswordAlreadySet = cachedPassword != null,
            initialPassword = cachedPassword?.password,
            password = cachedPassword?.password,
        )
    )
    val uiState: StateFlow<LinkSettingsUiState> = _uiState.asStateFlow()

    private var publicLink: String? = null

    init {
        loadNode()
        monitorAccountDetail()
        cachedPassword?.password?.let(::computeStrength)
    }

    /**
     * Separate key and password protection are mutually exclusive: a password-protected link
     * already encrypts the key, so enabling one clears the other. At most one of
     * [LinkSettingsUiState.isSeparateKeyEnabled] and [LinkSettingsUiState.isPasswordEnabled] is
     * ever true.
     */
    fun onSeparateKeyEnabled(enabled: Boolean) = update {
        if (enabled) {
            it.copy(
                isSeparateKeyEnabled = true,
                isPasswordEnabled = false,
                password = null,
                passwordStrength = null,
            )
        } else {
            it.copy(isSeparateKeyEnabled = false)
        }
    }

    fun onExpiryEnabled(enabled: Boolean) = updateUnlessAlbum {
        it.copy(isExpiryEnabled = enabled, expiryDate = if (enabled) it.expiryDate else null)
    }

    fun onExpiryDateChanged(expiryDate: Long) = updateUnlessAlbum {
        it.copy(expiryDate = expiryDate)
    }

    /** @see onSeparateKeyEnabled for the invariant this upholds from the other side. */
    fun onPasswordEnabled(enabled: Boolean) = updateUnlessAlbum {
        if (enabled) {
            it.copy(
                isSeparateKeyEnabled = false,
                isPasswordEnabled = true,
            )
        } else {
            it.copy(
                isPasswordEnabled = false,
                password = null,
                passwordStrength = null,
            )
        }
    }

    fun onPasswordChanged(password: String) {
        if (isAlbum) return
        update { it.copy(password = password) }
        computeStrength(password)
    }

    private fun computeStrength(password: String) {
        viewModelScope.launch {
            val strength = password.takeIf(String::isNotEmpty)
                ?.let { runCatching { getPasswordStrengthUseCase(it) }.getOrNull() }
            update { it.copy(passwordStrength = strength) }
        }
    }

    fun onSave() {
        val handle = handle ?: return
        val current = _uiState.value
        if (current.isSaving || !current.isDirty || !current.isValid) return

        update { it.copy(isSaving = true) }
        viewModelScope.launch {
            runCatching { applyChanges(handle, current) }
                .onSuccess { update { it.copy(isSaving = false, savedEvent = triggered) } }
                .onFailure { throwable ->
                    Timber.e(throwable, "Failed to save link settings")
                    update { it.copy(isSaving = false, errorEvent = triggered) }
                }
        }
    }

    fun onSavedEventConsumed() = update { it.copy(savedEvent = consumed) }

    fun onErrorEventConsumed() = update { it.copy(errorEvent = consumed) }

    /**
     * Applies the pending changes, writing any password change/removal to the shared
     * [ShareLinkPasswordCache] so the Share link screen reflects it.
     */
    private suspend fun applyChanges(
        handle: Long,
        state: LinkSettingsUiState,
    ) {
        if (state.isSeparateKeyDirty) {
            separateKeyCache.set(handle, state.isSeparateKeyEnabled)
        }
        if (state.isExpiryDirty) {
            val expireTimeSeconds = state.expiryDate
                ?.takeIf { state.isExpiryEnabled }
                ?.milliseconds?.inWholeSeconds
            exportNodeUseCase(
                nodeToExport = NodeId(handle),
                expireTime = expireTimeSeconds,
                callerName = CALLER_NAME,
            )
        }
        val password = state.password
        when {
            state.isPasswordEnabled && !password.isNullOrBlank() -> {
                val encrypted = publicLink?.takeIf(String::isNotEmpty)
                    ?.let { encryptLinkWithPasswordUseCase(it, password) }
                passwordCache.set(handle, LinkPassword(password = password, linkWithPassword = encrypted))
            }

            state.isPasswordAlreadySet && !state.isPasswordEnabled ->
                passwordCache.set(handle, null)
        }
    }

    /** No-op for an album: it is not a node, and only the separate-key option applies to it. */
    private fun loadNode() {
        val handle = handle?.takeUnless { isAlbum } ?: return
        viewModelScope.launch {
            val node = runCatching { getNodeByIdUseCase(NodeId(handle)) }
                .onFailure { Timber.e(it, "Failed to load node for link settings") }
                .getOrNull()
            val exportedData = node?.exportedData
            publicLink = exportedData?.publicLink
            val expiryMillis = exportedData?.expirationTime?.seconds?.inWholeMilliseconds
            update {
                if (expiryMillis == null) {
                    it.copy(isFolder = node is FolderNode)
                } else {
                    it.copy(
                        isFolder = node is FolderNode,
                        isExpiryEnabled = true,
                        isExpiryAlreadySet = true,
                        initialExpiryDate = expiryMillis,
                        expiryDate = expiryMillis,
                    )
                }
            }
        }
    }

    private fun monitorAccountDetail() {
        viewModelScope.launch {
            monitorAccountDetailUseCase()
                .catch { Timber.e(it) }
                .collect { accountDetail ->
                    update { it.copy(isLoading = false, accountType = accountDetail.levelDetail?.accountType) }
                }
        }
    }

    private fun update(transform: (LinkSettingsUiState) -> LinkSettingsUiState) =
        _uiState.update { transform(it).withComputedFlags() }

    /**
     * Applies [transform] only for a node subject.
     *
     * An album link supports neither an expiry nor a password, so the state must never be able to
     * describe one — the rows are absent from the screen, but the invariant belongs here rather
     * than resting on the UI. Ignoring the change keeps [LinkSettingsUiState.isSaveEnabled] false
     * too, so Save can never offer to apply something that would be dropped.
     */
    private fun updateUnlessAlbum(transform: (LinkSettingsUiState) -> LinkSettingsUiState) {
        if (isAlbum) return
        update(transform)
    }

    private fun LinkSettingsUiState.withComputedFlags() =
        copy(hasUnsavedChanges = isDirty, isSaveEnabled = isDirty && isValid && !isSaving)

    private val LinkSettingsUiState.isDirty: Boolean
        get() = isSeparateKeyDirty || isExpiryDirty || isPasswordDirty

    private val LinkSettingsUiState.isSeparateKeyDirty: Boolean
        get() = isSeparateKeyEnabled != initialSeparateKeyEnabled

    private val LinkSettingsUiState.isExpiryDirty: Boolean
        get() = if (isExpiryAlreadySet) {
            !isExpiryEnabled || expiryDate != initialExpiryDate
        } else {
            isExpiryEnabled || expiryDate != null
        }

    private val LinkSettingsUiState.isPasswordDirty: Boolean
        get() = if (isPasswordAlreadySet) {
            !isPasswordEnabled || password != initialPassword
        } else {
            isPasswordEnabled || !password.isNullOrEmpty()
        }

    private val LinkSettingsUiState.isValid: Boolean
        get() = when {
            isExpiryEnabled && expiryDate == null -> false
            isPasswordEnabled && password.isNullOrBlank() -> false
            else -> true
        }

    /**
     * Assisted factory arguments.
     *
     * @property subject Whose link settings are being edited: node handles or an album.
     */
    data class Args(val subject: ShareLinkSubject)

    @AssistedFactory
    interface Factory {
        fun create(args: Args): LinkSettingsViewModel
    }

    private companion object {
        const val CALLER_NAME = "LinkSettingsViewModel"
    }
}
