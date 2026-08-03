package mega.privacy.android.feature.mediaplayer.presentation.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * State of the audio player's sleep timer.
 */
sealed interface SleepTimerState {

    /** No sleep timer is currently active. */
    data object Inactive : SleepTimerState

    /**
     * A countdown timer is running.
     *
     * @property remaining Duration remaining until the player pauses.
     * @property option The option that was originally selected to start this countdown.
     */
    data class CountingDown(
        val remaining: Duration,
        val option: SleepTimerOption,
    ) : SleepTimerState

    /** The player will pause automatically when the current track finishes. */
    data object EndOfTrack : SleepTimerState
}

/**
 * Options available in the sleep timer bottom sheet.
 *
 * @property duration Duration for timed options; [Duration.ZERO] for [EndOfTrack].
 */
enum class SleepTimerOption(val duration: Duration) {
    Minutes5(5.minutes),
    Minutes15(15.minutes),
    Minutes30(30.minutes),
    Minutes60(60.minutes),
    EndOfTrack(Duration.ZERO),
}
