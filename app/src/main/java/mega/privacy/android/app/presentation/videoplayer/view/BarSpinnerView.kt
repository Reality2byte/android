package mega.privacy.android.app.presentation.videoplayer.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator

/**
 * Displays a rotating bar-style spinner using opacity cycling instead of geometric rotation.
 * 8 radial bars are drawn with decreasing alpha from the current leading bar, creating
 * the illusion of rotation without any canvas rotation.
 */
internal class BarSpinnerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFFFFF.toInt()
        style = Paint.Style.FILL
    }
    private var animFraction = 0f
    private val barRect = RectF()

    private var cx = 0f
    private var cy = 0f
    private var outerRadius = 0f
    private var innerRadius = 0f
    private var barWidth = 0f
    private var cornerRadius = 0f

    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 1000L
        interpolator = LinearInterpolator()
        repeatCount = ValueAnimator.INFINITE
        addUpdateListener {
            animFraction = it.animatedValue as Float
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
        outerRadius = minOf(w, h) / 2f * 0.72f
        innerRadius = outerRadius * 0.55f
        barWidth = outerRadius * 0.22f
        cornerRadius = barWidth / 2f
    }

    override fun onDraw(canvas: Canvas) {
        val currentBar = (animFraction * BAR_COUNT).toInt()
        repeat(BAR_COUNT) { i ->
            val age = (i - currentBar + BAR_COUNT) % BAR_COUNT
            paint.alpha = ((BAR_COUNT - age).toFloat() / BAR_COUNT * 255).toInt()
            canvas.save()
            canvas.rotate(360f / BAR_COUNT * i, cx, cy)
            barRect.set(cx - barWidth / 2, cy - outerRadius, cx + barWidth / 2, cy - innerRadius)
            canvas.drawRoundRect(barRect, cornerRadius, cornerRadius, paint)
            canvas.restore()
        }
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            if (!animator.isRunning) animator.start()
        } else {
            animator.cancel()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (isShown && !animator.isRunning) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    companion object {
        private const val BAR_COUNT = 8
    }
}
