package com.ads.manager

import android.graphics.Color
import androidx.annotation.ColorInt

data class AdsConfig(
    val appId: String = "ca-app-pub-3940256099942544~3347511713",
    val bannerId: String = "ca-app-pub-3940256099942544/6300978111",
    val interstitialId: String = "ca-app-pub-3940256099942544/1033173712",
    val nativeId: String = "ca-app-pub-3940256099942544/2247696110",
    val rewardedId: String = "ca-app-pub-3940256099942544/5224354917",
    val splashAppOpenId: String = "ca-app-pub-3940256099942544/9257395921",
    val resumeAppOpenId: String = "ca-app-pub-3940256099942544/9257395921",

    val interstitialInterval: Int = 2,

    @param:ColorInt val shimmerBaseColor: Int = Color.parseColor("#E0E0E0"),
    @param:ColorInt val shimmerHighlightColor: Int = Color.parseColor("#F5F5F5"),
    @param:ColorInt val dialogBackgroundColor: Int = Color.BLACK,
    @param:ColorInt val dialogTextColor: Int = Color.WHITE,
    val dialogLoadingText: String = "Ad is loading…"
)
