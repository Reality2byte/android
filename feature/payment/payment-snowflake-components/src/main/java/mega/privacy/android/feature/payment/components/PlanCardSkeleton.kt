package mega.privacy.android.feature.payment.components

import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mega.android.core.ui.modifiers.shimmerEffect
import mega.android.core.ui.preview.CombinedThemePreviews
import mega.android.core.ui.theme.AndroidTheme
import mega.android.core.ui.tokens.theme.DSTokens

/**
 * Shimmer placeholder for a bordered plan card, shared by the quota-warning and subscription offer
 * skeletons since both designs reuse the same plan card placeholder.
 */
@Composable
internal fun PlanCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = DSTokens.colors.border.strong,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SkeletonBar(Modifier.width(48.dp).height(16.dp))
        SkeletonBar(Modifier.width(90.dp).height(16.dp))
        SkeletonBar(Modifier.width(230.dp).height(16.dp))
        SkeletonBar(Modifier.width(230.dp).height(16.dp))
        SkeletonBar(Modifier.fillMaxWidth().height(32.dp), shape = RoundedCornerShape(8.dp))
    }
}

/**
 * A single shimmering placeholder bar. Size is supplied through [modifier].
 */
@Composable
internal fun SkeletonBar(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
) {
    Spacer(modifier = modifier.shimmerEffect(shape))
}

@CombinedThemePreviews
@Composable
private fun PlanCardSkeletonPreview() {
    AndroidTheme(isSystemInDarkTheme()) {
        PlanCardSkeleton(modifier = Modifier.padding(16.dp))
    }
}
