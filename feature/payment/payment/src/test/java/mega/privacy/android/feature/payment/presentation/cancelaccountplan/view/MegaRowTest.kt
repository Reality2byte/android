package mega.privacy.android.feature.payment.presentation.cancelaccountplan.view

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w720dp-h1280dp-xhdpi")
class MegaRowTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `test that all row cells are displayed correctly`() {

        val cells = listOf(
            TableCell.TextCell(
                "Feature",
                style = TableCell.TextCellStyle.Header,
                cellAlignment = TableCell.CellAlignment.Start,
            ),
            TableCell.TextCell(
                "Free",
                style = TableCell.TextCellStyle.Header,
                cellAlignment = TableCell.CellAlignment.Center,
            ),
            TableCell.TextCell(
                text = "Pro Plan", style = TableCell.TextCellStyle.Header,
                cellAlignment = TableCell.CellAlignment.Center,
            ),
        )

        composeTestRule.setContent {
            MegaTableRow(
                rowCells = cells,
                minRowHeight = 50.dp,
                rowPadding = 8.dp
            )
        }
        composeTestRule.onAllNodesWithTag(TABLE_CELL_TEST_TAG).assertCountEquals(cells.size)
    }

    @Test
    @Config(qualifiers = "w320dp-h640dp-xhdpi")
    fun `test that row cells share the whole row width`() {
        val cells = listOf(
            TableCell.TextCell(
                "Password-protected links",
                style = TableCell.TextCellStyle.SubHeader,
                cellAlignment = TableCell.CellAlignment.Start,
            ),
            TableCell.TextCell(
                "Limited",
                style = TableCell.TextCellStyle.Normal,
                cellAlignment = TableCell.CellAlignment.Center,
            ),
            TableCell.TextCell(
                text = "Unlimited", style = TableCell.TextCellStyle.Normal,
                cellAlignment = TableCell.CellAlignment.Center,
            ),
        )

        composeTestRule.setContent {
            MegaTableRow(
                rowCells = cells,
                minRowHeight = 50.dp,
                rowPadding = 8.dp,
                modifier = Modifier.testTag(TEST_TAG_ROW),
            )
        }

        val cellNodes = composeTestRule.onAllNodesWithTag(TABLE_CELL_TEST_TAG)
        cellNodes[0].assertWidthIsAtLeast(100.dp)
        cellNodes[1].assertWidthIsAtLeast(80.dp)
        cellNodes[2].assertWidthIsAtLeast(80.dp)
        composeTestRule.onNodeWithTag(TEST_TAG_ROW).assertHeightIsAtLeast(50.dp)
    }
}

private const val TEST_TAG_ROW = "mega_row_test:row"
