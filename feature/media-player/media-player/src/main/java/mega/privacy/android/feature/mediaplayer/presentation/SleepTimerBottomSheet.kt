package mega.privacy.android.feature.mediaplayer.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import kotlin.time.Duration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import mega.android.core.ui.components.MegaText
import mega.android.core.ui.components.sheets.MegaModalBottomSheet
import mega.android.core.ui.components.sheets.MegaModalBottomSheetBackground
import mega.android.core.ui.theme.values.TextColor
import mega.privacy.android.feature.mediaplayer.presentation.model.SleepTimerOption
import mega.privacy.android.feature.mediaplayer.presentation.model.SleepTimerState
import mega.privacy.android.shared.original.core.ui.theme.OriginalTheme
import mega.privacy.android.shared.resources.R as sharedR

/**
 * Bottom sheet for configuring the audio player's sleep timer.
 *
 * When no timer is active, the title is "Sleep timer". When a countdown or end-of-track
 * mode is active the title reflects the current state, the active option is highlighted,
 * and a "Turn off timer" row is appended at the bottom.
 *
 * @param sleepTimerState Current sleep timer state.
 * @param onOptionSelected Invoked with the chosen [SleepTimerOption]; the caller is
 *   responsible for dismissing the sheet.
 * @param onTurnOff Invoked when the user taps "Turn off timer".
 * @param onDismiss Invoked when the sheet should be dismissed without changes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepTimerBottomSheet(
    sleepTimerState: SleepTimerState,
    onOptionSelected: (SleepTimerOption) -> Unit,
    onTurnOff: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val title = when (sleepTimerState) {
        is SleepTimerState.CountingDown ->
            stringResource(
                sharedR.string.audio_player_sleep_timer_active_title,
                formatCountdown(sleepTimerState.remaining),
            )

        SleepTimerState.EndOfTrack, SleepTimerState.Inactive ->
            stringResource(sharedR.string.audio_player_sleep_timer_title)
    }

    val isTimerActive = sleepTimerState !is SleepTimerState.Inactive

    OriginalTheme(isDark = true) {
        MegaModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            bottomSheetBackground = MegaModalBottomSheetBackground.Surface1,
        ) {
            MegaText(
                text = title,
                textColor = TextColor.Primary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(SleepTimerOption.entries) { option ->
                    SleepTimerOptionRow(
                        label = option.label(),
                        onClick = { onOptionSelected(option) },
                    )
                }

                if (isTimerActive) {
                    item {
                        SleepTimerOptionRow(
                            label = stringResource(sharedR.string.audio_player_sleep_timer_turn_off),
                            onClick = onTurnOff,
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SleepTimerOptionRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MegaText(
        text = label,
        textColor = TextColor.Primary,
        style = MaterialTheme.typography.bodyLarge,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}

@Composable
private fun SleepTimerOption.label(): String = when (this) {
    SleepTimerOption.EndOfTrack -> stringResource(sharedR.string.audio_player_sleep_timer_end_of_track)
    else -> {
        val minutes = duration.inWholeMinutes.toInt()
        pluralStringResource(sharedR.plurals.audio_player_sleep_timer_minutes, minutes, minutes)
    }
}

internal fun formatCountdown(duration: Duration): String {
    if (duration <= Duration.ZERO) return "0:00"
    val totalSeconds = duration.inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
