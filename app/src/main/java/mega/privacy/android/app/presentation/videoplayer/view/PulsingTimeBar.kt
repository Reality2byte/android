package mega.privacy.android.app.presentation.videoplayer.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.OptIn
import androidx.core.content.withStyledAttributes
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTimeBar
import androidx.media3.ui.R as Media3R

/**
 * A [DefaultTimeBar] that pulses the played-bar alpha while the player is buffering.
 * The scrubber and unplayed bar remain at full opacity; only the played (coloured)
 * portion fades from 100 % → 30 % → 100 % in a continuous cycle.
 *
 * Animation spec:
 *  - Alpha: 255 → 77 (100 % → 30 %) and back
 *  - Duration: 600 ms each direction (1 200 ms full cycle)
 *  - RepeatMode: REVERSE / INFINITE
 *  - Interpolator: [AccelerateDecelerateInterpolator]
 *  - Start delay: 400 ms so transient buffers don't cause a flash
 */
@OptIn(UnstableApi::class)
internal class PulsingTimeBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : DefaultTimeBar(context, attrs) {

    private var isSeekBuffering = false
    private val originalPlayedColor: Int

    init {
        var color = Color.WHITE
        context.withStyledAttributes(attrs, Media3R.styleable.DefaultTimeBar) {
            color = getColor(Media3R.styleable.DefaultTimeBar_played_color, Color.WHITE)
        }
        originalPlayedColor = color
    }

    private val pulseAnimator = ValueAnimator.ofInt(255, 77).apply {
        duration = 600L
        repeatCount = ValueAnimator.INFINITE
        repeatMode = ValueAnimator.REVERSE
        startDelay = 400L
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener { animator ->
            val alpha = animator.animatedValue as Int
            setPlayedColor(
                Color.argb(
                    alpha,
                    Color.red(originalPlayedColor),
                    Color.green(originalPlayedColor),
                    Color.blue(originalPlayedColor),
                )
            )
        }
    }

    /**
     * Starts or stops the played-bar pulsing animation.
     * Pass `true` while buffering, `false` when playback resumes.
     */
    fun setSeekBuffering(buffering: Boolean) {
        if (isSeekBuffering == buffering) return
        isSeekBuffering = buffering
        if (buffering) {
            pulseAnimator.start()
        } else {
            pulseAnimator.cancel()
            setPlayedColor(originalPlayedColor)
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility != VISIBLE) {
            pulseAnimator.cancel()
            isSeekBuffering = false
        }
    }

    override fun onDetachedFromWindow() {
        pulseAnimator.cancel()
        super.onDetachedFromWindow()
    }
}
