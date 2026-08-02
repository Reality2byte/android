package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.button.SecondaryNavigationIconButton
import mega.privacy.android.icon.pack.IconPack
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Transparent top bar holding only the dismiss (X) affordance, used by the subscription offer
 * screen's skeleton and error states where there is no banner artwork to overlay the icon on.
 */
@Composable
internal fun SubscriptionOfferTopBar(
    onDismissClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(TOP_BAR_HEIGHT)
            .padding(horizontal = 16.dp),
    ) {
        SecondaryNavigationIconButton(
            icon = rememberVectorPainter(IconPack.Medium.Thin.Outline.X),
            onClick = onDismissClick,
            contentDescription = stringResource(sharedR.string.general_dismiss_dialog),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .testTag(TEST_TAG_SUBSCRIPTION_OFFER_SCREEN_DISMISS),
        )
    }
}

private val TOP_BAR_HEIGHT = 56.dp
