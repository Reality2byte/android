package mega.privacy.android.feature.payment.presentation.offer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import mega.privacy.android.domain.entity.Subscription
import mega.privacy.android.feature.payment.components.SubscriptionOfferScreenContent
import mega.privacy.android.feature.payment.components.SubscriptionOfferScreenSkeleton
import mega.privacy.android.feature.payment.model.extensions.toUIAccountType
import mega.privacy.android.feature.payment.presentation.upgrade.billedDescription
import mega.privacy.android.feature.payment.presentation.upgrade.getCampaignName
import mega.privacy.android.shared.resources.R as sharedR
import java.text.DateFormat
import java.util.Date

/**
 * Full-screen promo for the recommended discounted plan (DSN-3130 offer landing screen). Shows the
 * campaign artwork, the offer countdown, the discounted plan card and a pinned buy CTA.
 *
 * Renders a full-screen empty placeholder while [SubscriptionOfferState.offerSubscription] is null.
 *
 * @param uiState the offer to promote
 * @param onBuyClick called with the promoted [Subscription] when the buy CTA is tapped
 * @param onDismiss called when the dismiss (X) icon is tapped
 * @param modifier
 */
@Composable
internal fun SubscriptionOfferScreen(
    uiState: SubscriptionOfferState,
    onBuyClick: (Subscription) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isMonthly = uiState.isMonthly
    val offerSubscription = uiState.offerSubscription
    val subscription = offerSubscription?.getSubscription(isMonthly)
    if (offerSubscription == null || subscription == null) {
        // Full-height skeleton while loading: the slide-up enter transition derives its travel
        // distance from the entering content height, so an empty screen here would slide nowhere.
        SubscriptionOfferScreenSkeleton(modifier = modifier.fillMaxSize())
        return
    }
    val context = LocalContext.current
    val locale = LocalLocale.current.platformLocale
    val planName = stringResource(offerSubscription.accountType.toUIAccountType().textValue)

    val storageFormatted = offerSubscription.formatStorageSize()
    val transferFormatted = offerSubscription.formatTransferSize(isMonthly)
    val storageText = stringResource(
        sharedR.string.choose_account_screen_storage_label,
        stringResource(storageFormatted.unit, storageFormatted.size)
    )
    val transferText = stringResource(
        sharedR.string.choose_account_screen_transfer_quota_label,
        stringResource(transferFormatted.unit, transferFormatted.size)
    )

    val discountedMonthly = offerSubscription.localiseDiscountedPriceMonthlyCurrencyCode(
        locale,
        isMonthly,
    )?.price.orEmpty()
    val discountedYearly = offerSubscription.localiseDiscountedPriceYearlyCurrencyCode(
        locale,
        isMonthly,
    )?.price.orEmpty()
    val originalPrice = offerSubscription.localisePriceCurrencyCode(locale, isMonthly).price

    val priceText: String
    val monthlyPriceText: String?
    val billedDiscountedPrice: String
    val billedOriginalPrice: String
    if (isMonthly) {
        priceText =
            stringResource(sharedR.string.subscription_revamp_price_per_month, discountedMonthly)
        monthlyPriceText = null
        billedDiscountedPrice = priceText
        billedOriginalPrice =
            stringResource(sharedR.string.subscription_revamp_price_per_month, originalPrice)
    } else {
        priceText =
            stringResource(sharedR.string.subscription_revamp_price_per_year, discountedYearly)
        monthlyPriceText =
            stringResource(sharedR.string.subscription_revamp_price_per_month, discountedMonthly)
        billedDiscountedPrice = discountedYearly
        billedOriginalPrice = originalPrice
    }

    SubscriptionOfferScreenContent(
        campaignText = getCampaignName(
            context = context,
            discountName = subscription.discountName,
            discountPercentage = subscription.discountedPercentage ?: 0,
        ),
        validUntil = uiState.offerValidUntil,
        validUntilText = uiState.offerValidUntil?.let {
            stringResource(
                sharedR.string.subscription_offer_countdown_valid_until,
                DateFormat.getDateInstance(DateFormat.LONG, locale).format(Date(it * 1000L)),
            )
        },
        planName = planName,
        priceText = priceText,
        originalPriceText = originalPrice,
        discountDescriptionText = billedDescription(
            offerPeriod = subscription.offerPeriod,
            isMonthly = isMonthly,
            discountedPrice = billedDiscountedPrice,
            originalPrice = billedOriginalPrice,
        ),
        storageText = storageText,
        transferText = transferText,
        buyButtonText = stringResource(
            sharedR.string.subscription_revamp_get_plan_button,
            planName,
        ),
        onBuyClick = { onBuyClick(subscription) },
        onDismissClick = onDismiss,
        modifier = modifier,
        monthlyPriceText = monthlyPriceText,
    )
}
