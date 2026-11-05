package com.ads.manager

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FontRes

data class AdsConfig(
    val appId: String = "",
    val bannerId: String = "",
    val interstitialId: String = "",
    val nativeId: String = "",
    val rewardedId: String = "",
    val splashAppOpenId: String = "",
    val resumeAppOpenId: String = "",

    // Shimmer Colors Customization
    @param:ColorInt val shimmerBaseColor: Int = Color.parseColor("#E0E0E0"),
    @param:ColorInt val shimmerHighlightColor: Int = Color.parseColor("#F5F5F5"),

    // Ad Loading Dialog Customization
    @param:ColorInt val dialogBackgroundColor: Int = Color.parseColor("#CC000000"), // Translucent dark overlay
    @param:ColorInt val dialogTextColor: Int = Color.WHITE,
    @param:ColorInt val dialogProgressColor: Int = Color.WHITE,
    val dialogLoadingText: String = "Ad is loading…",
    val showDialogProgressBar: Boolean = true,

    // Native Ad Styling Customization
    @param:ColorInt val nativeBackgroundColor: Int? = null,
    @param:ColorInt val nativeHeadlineColor: Int? = null,
    @param:ColorInt val nativeBodyColor: Int? = null,
    @param:ColorInt val nativeAdBadgeBgColor: Int? = null,
    @param:ColorInt val nativeAdBadgeTextColor: Int? = null,
    @param:ColorInt val nativeCtaBgColor: Int? = null,
    @param:ColorInt val nativeCtaTextColor: Int? = null,
    @param:FontRes val nativeFontResId: Int? = null,
    val nativeFontFamily: android.graphics.Typeface? = null,
    val nativeFontFamilyName: String? = null
)
