package com.ads.manager.banner

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.ads.manager.AdsManager
import com.ads.manager.R

class BannerAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var bannerType = 0 // 0 = adaptive, 1 = collapsible, 2 = inline, 3 = mrec

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.BannerAdView)
            bannerType = typedArray.getInt(R.styleable.BannerAdView_bannerType, 0)
            typedArray.recycle()
        }

        if (isInEditMode) {
            setupEditModePreview()
        }
    }

    private fun setupEditModePreview() {
        val density = context.resources.displayMetrics.density
        val heightDp = when (bannerType) {
            3 -> 250 // MREC
            2 -> 100 // Inline
            else -> 60 // Adaptive / Collapsible
        }

        val container = LinearLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, (heightDp * density).toInt())
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }

        val adBadge = TextView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                marginEnd = (8 * density).toInt()
            }
            text = "Ad"
            textSize = 10f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.parseColor("#FFCC00"))
            setPadding((4 * density).toInt(), (2 * density).toInt(), (4 * density).toInt(), (2 * density).toInt())
        }

        val bannerTitle = TextView(context).apply {
            text = when (bannerType) {
                1 -> "AdMob Collapsible Banner"
                2 -> "AdMob Inline Adaptive Banner"
                3 -> "AdMob Medium Rectangle (300x250)"
                else -> "AdMob Adaptive Banner"
            }
            textSize = 13f
            setTextColor(Color.DKGRAY)
            setTypeface(null, Typeface.BOLD)
        }

        container.addView(adBadge)
        container.addView(bannerTitle)
        addView(container)
    }

    /**
     * Load banner based on XML bannerType or standard adaptive.
     */
    fun load(activity: Activity) {
        when (bannerType) {
            1 -> AdsManager.showCollapsibleBanner(activity, this)
            2 -> AdsManager.showInlineBanner(activity, this)
            3 -> AdsManager.showMediumRectangleBanner(activity, this)
            else -> AdsManager.showBanner(activity, this)
        }
    }

    fun loadCollapsible(activity: Activity, position: String = "bottom") {
        AdsManager.showCollapsibleBanner(activity, this, position)
    }

    fun loadInline(activity: Activity, maxHeightDp: Int = 100) {
        AdsManager.showInlineBanner(activity, this, maxHeightDp)
    }

    fun loadMediumRectangle(activity: Activity) {
        AdsManager.showMediumRectangleBanner(activity, this)
    }
}
