package mega.privacy.android.feature.payment.presentation.quotawarning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mega.privacy.android.domain.entity.AccountSubscriptionCycle
import mega.privacy.android.domain.entity.AccountType
import mega.privacy.android.domain.entity.account.AccountDetail
import mega.privacy.android.domain.entity.account.AccountLevelDetail
import mega.privacy.android.domain.entity.payment.Subscriptions
import mega.privacy.android.domain.usecase.account.GetSpecificAccountDetailUseCase
import mega.privacy.android.domain.usecase.account.MonitorAccountDetailUseCase
import mega.privacy.android.domain.usecase.account.MonitorStorageStateUseCase
import mega.privacy.android.domain.usecase.billing.GetSubscriptionsUseCase
import mega.privacy.android.domain.usecase.contact.GetCurrentUserEmail
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.domain.usecase.transfers.overquota.MonitorTransferOverQuotaUseCase
import mega.privacy.android.feature.payment.model.LocalisedSubscription
import mega.privacy.android.feature.payment.model.mapper.LocalisedSubscriptionMapper
import timber.log.Timber
import javax.inject.Inject

/**
 * ViewModel for the quota-warning upsell screen. Exposes the current plan, storage/transfer usage,
 * and the recommended plan (the smallest subscription whose storage covers current usage).
 */
@HiltViewModel
class QuotaWarningUpgradeViewModel @Inject constructor(
    private val monitorAccountDetailUseCase: MonitorAccountDetailUseCase,
    private val monitorStorageStateUseCase: MonitorStorageStateUseCase,
    private val monitorTransferOverQuotaUseCase: MonitorTransferOverQuotaUseCase,
    private val getSubscriptionsUseCase: GetSubscriptionsUseCase,
    private val getCurrentUserEmail: GetCurrentUserEmail,
    private val monitorConnectivityUseCase: MonitorConnectivityUseCase,
    private val localisedSubscriptionMapper: LocalisedSubscriptionMapper,
    private val getSpecificAccountDetailUseCase: GetSpecificAccountDetailUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(QuotaWarningUpgradeState())

    /**
     * The current UI state.
     */
    val state = _state.asStateFlow()

    /** Plans fetch outcome, null until the first attempt finishes. */
    private val subscriptionsResult = MutableStateFlow<Result<Subscriptions>?>(null)

    /** In-flight retry, so a second tap on "Try again" is ignored rather than starting a fetch. */
    private var retryJob: Job? = null

    init {
        monitorConnectivity()
        fetchEmail()
        monitorAccountDetail()
        monitorQuotaState()
        fetchLatestUsedInfo()
    }

    private fun fetchLatestUsedInfo() {
        viewModelScope.launch {
            runCatching {
                getSpecificAccountDetailUseCase(
                    storage = true,
                    transfer = true,
                    pro = false
                )
            }.onFailure { Timber.e(it) }
        }
    }

    /**
     * Refetches the screen's data, for the error state's "Try again" action. Ignored while an
     * earlier retry is still running, so repeated taps do not start a fetch each.
     */
    fun onRetry() {
        if (retryJob?.isActive == true) return
        retryJob = viewModelScope.launch {
            fetchSubscriptions()
            if (_state.value.email == null) {
                loadEmail()
            }
        }
    }

    private fun monitorConnectivity() {
        viewModelScope.launch {
            monitorConnectivityUseCase()
                .distinctUntilChanged()
                .catch { Timber.e(it) }
                .collect { isConnected ->
                    _state.update { it.copy(isConnected = isConnected) }
                }
        }
    }

    private fun fetchEmail() {
        viewModelScope.launch { loadEmail() }
    }

    private suspend fun loadEmail() {
        val email = runCatching { getCurrentUserEmail() }.getOrElse {
            Timber.e(it)
            null
        }
        _state.update { it.copy(email = email) }
    }

    private fun monitorQuotaState() {
        viewModelScope.launch {
            combine(
                monitorStorageStateUseCase(),
                monitorTransferOverQuotaUseCase(),
            ) { storageState, isTransferOverQuota -> storageState to isTransferOverQuota }
                .catch { Timber.e(it) }
                .collect { (storageState, isTransferOverQuota) ->
                    _state.update {
                        it.copy(
                            storageState = storageState,
                            isTransferOverQuota = isTransferOverQuota,
                        )
                    }
                }
        }
    }

    private fun monitorAccountDetail() {
        viewModelScope.launch {
            fetchSubscriptions()
            combine(
                monitorAccountDetailUseCase(),
                subscriptionsResult,
            ) { detail, result -> detail to result }
                .catch { Timber.e(it) }
                .collect { (detail, result) -> updateAccountDetail(detail, result?.getOrNull()) }
        }
    }

    private suspend fun fetchSubscriptions() {
        val result = runCatching { getSubscriptionsUseCase() }.onFailure { Timber.e(it) }
        subscriptionsResult.update { result }
        _state.update { it.copy(hasLoadError = result.isFailure) }
    }

    private fun updateAccountDetail(detail: AccountDetail, subscriptions: Subscriptions?) {
        val levelDetail = detail.levelDetail
        val storageDetail = detail.storageDetail
        val transferDetail = detail.transferDetail
        val storageUsed = storageDetail?.usedStorage
        val transferUsed = transferDetail?.usedTransfer
        val cycle = levelDetail?.let(::resolveCurrentPlanCycle) ?: AccountSubscriptionCycle.UNKNOWN
        val candidates = upgradeCandidates(
            currentPlan = levelDetail?.accountType,
            totalStorage = storageDetail?.totalStorage,
            totalTransfer = transferDetail?.totalTransfer,
            cycle = cycle,
            subscriptions = subscriptions,
        )
        val isHighestPlan = levelDetail?.accountType?.isPaid == true &&
                subscriptions.hasPlans() &&
                candidates.isEmpty()
        _state.update {
            it.copy(
                currentPlan = levelDetail?.accountType,
                subscriptionCycle = cycle,
                storageUsed = storageUsed,
                storageTotal = storageDetail?.totalStorage,
                storageUsedPercentage = storageDetail?.usedPercentage ?: 0,
                transferUsed = transferUsed,
                transferTotal = transferDetail?.totalTransfer,
                transferUsedPercentage = transferDetail?.usedTransferPercentage ?: 0,
                recommendedSubscription = recommendedSubscription(
                    storageUsed = storageUsed,
                    transferUsed = transferUsed,
                    cycle = cycle,
                    candidates = candidates,
                ),
                isHighestPlan = isHighestPlan,
                isLoading = it.isLoading && storageDetail == null,
            )
        }
    }

    /**
     * Cycle of the current plan's own subscription (matched by id, then by level), falling back to
     * the account-level cycle, which can be wrong when the account holds multiple subscriptions.
     */
    private fun resolveCurrentPlanCycle(levelDetail: AccountLevelDetail): AccountSubscriptionCycle {
        val subscriptions = levelDetail.accountSubscriptionDetailList
        val planSubscriptionId = levelDetail.accountPlanDetail?.subscriptionId
        val matchingSubscription = planSubscriptionId?.let { id ->
            subscriptions.firstOrNull { it.subscriptionId == id }
        } ?: subscriptions.firstOrNull { it.subscriptionLevel == levelDetail.accountType }
        return matchingSubscription?.subscriptionCycle
            ?.takeIf { it != AccountSubscriptionCycle.UNKNOWN }
            ?: levelDetail.accountSubscriptionCycle
    }

    private fun Subscriptions?.hasPlans(): Boolean =
        this != null && (monthlySubscriptions.isNotEmpty() || yearlySubscriptions.isNotEmpty())

    /**
     * The plans worth upgrading to, sorted by storage: those that raise at least one quota of the
     * current plan and lower neither. Every plan qualifies while the account is not on a paid plan.
     */
    private fun upgradeCandidates(
        currentPlan: AccountType?,
        totalStorage: Long?,
        totalTransfer: Long?,
        cycle: AccountSubscriptionCycle,
        subscriptions: Subscriptions?,
    ): List<LocalisedSubscription> {
        if (subscriptions == null) return emptyList()
        val plans = subscriptions.toLocalisedPlans()
        if (currentPlan?.isPaid != true) return plans
        // subscription quotas are expressed in GB, so compare against the account quotas in GB
        val currentStorageGb = (totalStorage ?: 0L) / BYTES_IN_GB
        val currentTransferGb = (totalTransfer ?: 0L) / BYTES_IN_GB
        return plans.filter { plan ->
            val storageGb = plan.storage.toLong()
            val transferGb = plan.transferGbFor(cycle) ?: currentTransferGb
            plan.accountType != currentPlan &&
                    storageGb >= currentStorageGb && transferGb >= currentTransferGb &&
                    (storageGb > currentStorageGb || transferGb > currentTransferGb)
        }
    }

    private fun Subscriptions.toLocalisedPlans(): List<LocalisedSubscription> =
        (monthlySubscriptions + yearlySubscriptions)
            .map { it.accountType }
            .distinct()
            .map { accountType ->
                localisedSubscriptionMapper(
                    monthlySubscription = monthlySubscriptions
                        .firstOrNull { it.accountType == accountType },
                    yearlySubscription = yearlySubscriptions
                        .firstOrNull { it.accountType == accountType },
                )
            }
            .sortedBy { it.storage }

    /**
     * Smallest candidate whose storage and transfer both cover current usage (largest if none does),
     * so upgrading clears the over-quota state whichever quota triggered the warning.
     *
     * Special case: when a discounted plan also covers current usage and its post-offer price
     * undercuts that default recommendation, the discounted plan is recommended instead (the
     * cheapest such offer wins), so the user is shown the better-value deal.
     */
    private fun recommendedSubscription(
        storageUsed: Long?,
        transferUsed: Long?,
        cycle: AccountSubscriptionCycle,
        candidates: List<LocalisedSubscription>,
    ): LocalisedSubscription? {
        val storageBytes = storageUsed ?: 0L
        val default = candidates.firstOrNull { it.coversUsage(storageBytes, transferUsed, cycle) }
            ?: candidates.lastOrNull()
            ?: return null
        return cheaperDiscountedAlternative(candidates, storageBytes, transferUsed, cycle, default)
            ?: default
    }

    /**
     * The cheapest discounted plan that also covers current usage and whose post-offer price
     * undercuts [default], or null when no such better-value offer exists.
     */
    private fun cheaperDiscountedAlternative(
        candidates: List<LocalisedSubscription>,
        storageUsed: Long,
        transferUsed: Long?,
        cycle: AccountSubscriptionCycle,
        default: LocalisedSubscription,
    ): LocalisedSubscription? {
        val defaultPrice = default.effectiveMonthlyPrice() ?: return null
        return candidates
            .filter { it.hasDiscount && it.coversUsage(storageUsed, transferUsed, cycle) }
            .mapNotNull { plan -> plan.effectiveMonthlyPrice()?.let { plan to it } }
            .filter { (_, price) -> price < defaultPrice }
            .minByOrNull { (_, price) -> price }
            ?.first
    }

    /**
     * Whether this plan's quotas exceed current usage in both dimensions, so upgrading to it clears
     * the over-quota state. Usage is in bytes; plan quotas are in GB. A dimension without data —
     * transfer usage not loaded, or a plan with no transfer quota — does not rule the plan out.
     */
    private fun LocalisedSubscription.coversUsage(
        storageUsed: Long,
        transferUsed: Long?,
        cycle: AccountSubscriptionCycle,
    ): Boolean = storage.toLong() * BYTES_IN_GB > storageUsed &&
            coversTransferUsage(transferUsed, cycle)

    private fun LocalisedSubscription.coversTransferUsage(
        transferUsed: Long?,
        cycle: AccountSubscriptionCycle,
    ): Boolean {
        val used = transferUsed ?: return true
        val quotaGb = transferGbFor(cycle) ?: return true
        return quotaGb * BYTES_IN_GB > used
    }

    /**
     * Transfer quota in GB of the billing cycle this plan will be offered in, or null when the SDK
     * reports none. A yearly option carries the whole year of transfer, as does the account quota of
     * a yearly plan, so the cycle is picked the way the recommended plan card picks it.
     */
    private fun LocalisedSubscription.transferGbFor(cycle: AccountSubscriptionCycle): Long? {
        val preferMonthly = cycle == AccountSubscriptionCycle.MONTHLY
        val subscription = getSubscription(isMonthly = preferMonthly)
            ?: getSubscription(isMonthly = !preferMonthly)
        return subscription?.transfer?.toLong()?.takeIf { it > 0 }
    }

    /**
     * The lowest monthly-equivalent price of the plan across its available billing cycles, using the
     * discounted amount where present. Null when no price is available. Amounts share the account
     * currency and plan prices differ by whole currency units, so the raw Float value is safe to compare.
     */
    private fun LocalisedSubscription.effectiveMonthlyPrice(): Float? = listOfNotNull(
        monthlySubscription?.let { (it.discountedAmountMonthly ?: it.amount).value },
        yearlySubscription?.let { it.discountedAmountMonthly?.value ?: (it.amount.value / 12) },
    ).minOrNull()

    private companion object {
        private const val BYTES_IN_GB = 1024L * 1024L * 1024L
    }
}
