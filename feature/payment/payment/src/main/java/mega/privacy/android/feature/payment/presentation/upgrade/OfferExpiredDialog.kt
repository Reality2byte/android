package mega.privacy.android.feature.payment.presentation.upgrade

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import mega.android.core.ui.components.dialogs.BasicDialog
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Dialog shown when the discount campaign ends while the user is on the subscription page. Its only
 * action reloads the page so the plans are re-fetched without the expired discount.
 *
 * @param onDismiss called for the action button and for any other dismissal
 * @param modifier
 */
@Composable
internal fun OfferExpiredDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicDialog(
        modifier = modifier.testTag(TEST_TAG_OFFER_EXPIRED_DIALOG),
        title = stringResource(sharedR.string.subscription_offer_expired_dialog_title),
        description = stringResource(sharedR.string.subscription_offer_expired_dialog_description),
        positiveButtonText = stringResource(sharedR.string.subscription_offer_expired_dialog_button),
        onPositiveButtonClicked = onDismiss,
        onDismiss = onDismiss,
        dismissOnClickOutside = false,
    )
}

@CombinedThemePreviews
@Composable
private fun OfferExpiredDialogPreview() {
    AndroidThemeForPreviews {
        OfferExpiredDialog(onDismiss = {})
    }
}

/**
 * Test tag for the expired-offer dialog
 */
internal const val TEST_TAG_OFFER_EXPIRED_DIALOG = "subscription_offer:expired_dialog"
