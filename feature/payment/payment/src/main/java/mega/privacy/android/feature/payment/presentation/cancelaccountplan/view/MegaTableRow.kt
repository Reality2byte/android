package mega.privacy.android.feature.payment.presentation.cancelaccountplan.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mega.privacy.android.shared.original.core.ui.preview.CombinedThemePreviews
import mega.privacy.android.shared.original.core.ui.theme.OriginalTheme

@Composable
internal fun MegaTableRow(
    rowCells: List<TableCell>,
    minRowHeight: Dp,
    rowPadding: Dp,
    modifier: Modifier = Modifier,
) {
    val totalColumns = rowCells.size
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minRowHeight)
            .padding(horizontal = rowPadding, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        rowCells.forEachIndexed { index, cell ->
            val widthWeight = if (index == 0) FIRST_COLUMN_WEIGHT else
                (1f - FIRST_COLUMN_WEIGHT) / (totalColumns - 1).coerceAtLeast(1)

            MegaTableCell(
                cell = cell,
                modifier = Modifier
                    .weight(widthWeight)
                    .testTag(TABLE_CELL_TEST_TAG)
            )
        }
    }
}

@Composable
@CombinedThemePreviews
private fun TableRowPreview() {
    OriginalTheme(isDark = isSystemInDarkTheme()) {
        MegaTableRow(
            rowCells = listOf(
                TableCell.TextCell(
                    "Feature",
                    TableCell.TextCellStyle.Header,
                    TableCell.CellAlignment.Start
                ),
                TableCell.TextCell(
                    "Free Plan",
                    TableCell.TextCellStyle.Header,
                    TableCell.CellAlignment.Center
                ),
                TableCell.TextCell(
                    "Pro Plan",
                    TableCell.TextCellStyle.Header,
                    TableCell.CellAlignment.Center
                ),
            ),
            minRowHeight = 50.dp,
            rowPadding = 8.dp
        )
    }
}

private const val FIRST_COLUMN_WEIGHT = 0.4f

internal const val TABLE_CELL_TEST_TAG = "table_row:cell"