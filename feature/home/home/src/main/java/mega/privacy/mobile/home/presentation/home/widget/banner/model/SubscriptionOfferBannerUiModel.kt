package mega.privacy.mobile.home.presentation.home.widget.banner.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import mega.android.core.ui.model.LocalizedText

/**
 * Display data for the locally-built subscription offer banner shown on the Home carousel.
 *
 * Holds unresolved values ([LocalizedText] and a [StringRes]) rather than resolved strings, so the
 * banner text is localised in the UI layer instead of at mapping time. This keeps the mapper a pure
 * Kotlin function and lets the copy follow a locale change.
 *
 * @property campaignName Campaign label — the discount name, or a "Special offer" fallback.
 * @property discountPercentage Discount percentage shown in the headline (e.g. 50 for "50% off").
 * @property formattedPrice Locale-formatted discounted monthly price (e.g. "€4.99").
 * @property planNameRes Plan name string resource (e.g. "Pro I").
 */
@Immutable
data class SubscriptionOfferBannerUiModel(
    val campaignName: LocalizedText,
    val discountPercentage: Int,
    val formattedPrice: String,
    @StringRes val planNameRes: Int,
)
