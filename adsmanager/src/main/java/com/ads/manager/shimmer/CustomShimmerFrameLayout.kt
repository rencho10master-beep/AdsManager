package com.ads.manager.shimmer

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Shader
import android.os.Looper
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.toColorInt
import com.ads.manager.R

class CustomShimmerFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val shimmerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }
    private var animator: ValueAnimator? = null
    private var shimmerOffset = 0f

    @ColorInt
    var shimmerColor: Int = "#E0E0E0".toColorInt()
        set(value) {
            field = value
            baseColor = value
            highlightColor = androidx.core.graphics.ColorUtils.blendARGB(value, android.graphics.Color.WHITE, 0.45f)
        }

    @ColorInt
    var baseColor: Int = "#E0E0E0".toColorInt()
        set(value) {
            field = value
            runOnMainThread {
                applyColorToChildren(this, value)
                updateGradient()
                invalidate()
            }
        }

    private fun applyColorToChildren(viewGroup: android.view.ViewGroup, color: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is android.view.ViewGroup) {
                if (child is androidx.cardview.widget.CardView) {
                    child.setCardBackgroundColor(color)
                }
                applyColorToChildren(child, color)
            } else if (child !is CustomShimmerFrameLayout) {
                child.setBackgroundColor(color)
            }
        }
    }

    @ColorInt
    var highlightColor: Int = "#F5F5F5".toColorInt()
        set(value) {
            field = value
            runOnMainThread {
                updateGradient()
                invalidate()
            }
        }

    var duration: Long = 1200L
        set(value) {
            field = value
            runOnMainThread {
                animator?.duration = value
            }
        }

    init {
        setWillNotDraw(false)

        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.ShimmerAdView)
            if (typedArray.hasValue(R.styleable.ShimmerAdView_shimmerColor)) {
                shimmerColor = typedArray.getColor(R.styleable.ShimmerAdView_shimmerColor, shimmerColor)
            } else {
                baseColor = typedArray.getColor(R.styleable.ShimmerAdView_shimmerBaseColor, baseColor)
                highlightColor = typedArray.getColor(R.styleable.ShimmerAdView_shimmerHighlightColor, highlightColor)
            }
            duration = typedArray.getInt(R.styleable.ShimmerAdView_shimmerDuration, 1200).toLong()
            typedArray.recycle()
        }

        setupAnimator()
    }

    private fun setupAnimator() {
        runOnMainThread {
            animator?.cancel()
            animator = ValueAnimator.ofFloat(-1f, 2f).apply {
                duration = this@CustomShimmerFrameLayout.duration
                repeatCount = ValueAnimator.INFINITE
                addUpdateListener {
                    shimmerOffset = it.animatedValue as Float
                    updateGradient()
                    invalidate()
                }
            }
        }
    }

    private fun updateGradient() {
        val width = width.toFloat()
        if (width <= 0) return

        val shimmerWidth = width * 0.5f
        val startX = width * shimmerOffset
        val endX = startX + shimmerWidth

        shimmerPaint.shader = LinearGradient(
            startX,
            0f,
            endX,
            0f,
            intArrayOf(
                Color.TRANSPARENT,
                Color.argb(130, 255, 255, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
    }

    fun startShimmer() {
        runOnMainThread {
            if (animator?.isStarted != true) {
                animator?.start()
            }
        }
    }

    fun stopShimmer() {
        runOnMainThread {
            animator?.cancel()
            invalidate()
        }
    }

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            post(action)
        }
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        applyColorToChildren(this, baseColor)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateGradient()
        startShimmer()
    }

    override fun dispatchDraw(canvas: Canvas) {
        if (animator?.isRunning == true && width > 0 && height > 0) {
            val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
            super.dispatchDraw(canvas)
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), shimmerPaint)
            canvas.restoreToCount(saveCount)
        } else {
            super.dispatchDraw(canvas)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }
}
