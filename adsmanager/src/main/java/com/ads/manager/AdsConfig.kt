package com.ads.manager

import android.graphics.Color
import androidx.annotation.ColorInt

data class AdsConfig(
    val appId: String = "",
    val bannerId: String = "",
    val interstitialId: String = "",
    val nativeId: String = "",
    val rewardedId: String = "",
    val splashAppOpenId: String = "",
    val resumeAppOpenId: String = "",

    @param:ColorInt val shimmerBaseColor: Int = Color.parseColor("#E0E0E0"),
    @param:ColorInt val shimmerHighlightColor: Int = Color.parseColor("#F5F5F5"),
    @param:ColorInt val dialogBackgroundColor: Int = Color.BLACK,
    @param:ColorInt val dialogTextColor: Int = Color.WHITE,
    val dialogLoadingText: String = "Ad is loading…"
)
