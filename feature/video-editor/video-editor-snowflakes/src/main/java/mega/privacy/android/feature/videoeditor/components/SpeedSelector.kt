package mega.privacy.android.feature.videoeditor.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.theme.AndroidThemeForPreviews
import mega.android.core.ui.theme.AppTheme
import mega.android.core.ui.theme.values.TextColor
import mega.android.core.ui.tokens.theme.DSTokens


/**
 * Speed-multiplier selector for the Speed tool, styled as an M3 expressive
 * connected button group (single-select). Decoupled — the caller supplies the
 * formatted labels and the selection callback.
 *
 * @param options formatted labels, one per selectable speed
 * @param selectedIndex index of the selected option, or -1 for none
 * @param onSelect called with the tapped option's index
 * @param modifier applied to the button group row
 */
@Composable
fun SpeedSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BetweenSpace),
    ) {
        options.forEachIndexed { index, label ->
            ConnectedToggleButton(
                label = label,
                selected = index == selectedIndex,
                isFirst = index == 0,
                isLast = index == options.lastIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun RowScope.ConnectedToggleButton(
    label: String,
    selected: Boolean,
    isFirst: Boolean,
    isLast: Boolean,
    onClick: () -> Unit,
) {
    val innerCorner by animateDpAsState(
        targetValue = if (selected) FullCorner else InnerCorner,
        label = "innerCorner",
    )
    val shape = RoundedCornerShape(
        topStart = if (isFirst) FullCorner else innerCorner,
        bottomStart = if (isFirst) FullCorner else innerCorner,
        topEnd = if (isLast) FullCorner else innerCorner,
        bottomEnd = if (isLast) FullCorner else innerCorner,
    )
    val background =
        if (selected) DSTokens.colors.brand.containerDefault else DSTokens.colors.neutral.containerDefault
    val borderColor = if (selected) DSTokens.colors.border.brand else Color.Transparent

    Box(
        modifier = Modifier
            .weight(1f)
            .height(GroupHeight)
            .clip(shape)
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MegaText(
                text = label,
                style = AppTheme.typography.labelLarge,
                textColor = if (selected) TextColor.Primary else TextColor.Secondary,
            )
        }
    }
}

// M3 expressive connected button group metrics
private val GroupHeight = 56.dp
private val BetweenSpace = 2.dp
private val InnerCorner = 8.dp
private val FullCorner = GroupHeight / 2


@Preview(showBackground = true)
@Composable
private fun SpeedSelectorPreview() {
    AndroidThemeForPreviews {
        SpeedSelector(
            options = listOf("0.25x", "0.5x", "1x", "1.5x", "2x", "4x"),
            selectedIndex = 2,
            onSelect = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
