package com.ads.manager.banner

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import com.ads.manager.AdsConfig
import com.ads.manager.init.AdsInitializer
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

class BannerAdManager(private val config: AdsConfig) {

    /**
     * Standard Anchored Adaptive Banner (Sticky top/bottom banner).
     */
    fun showBannerAd(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        val bannerSize = getAdaptiveBannerSize(activity)
        loadBannerInternal(activity, container, bannerSize, null, onLoaded, onFailed)
    }

    /**
     * Collapsible Banner Ad (Expands when shown and collapses to normal banner).
     * @param position "bottom" (default) or "top"
     */
    fun showCollapsibleBannerAd(
        activity: Activity,
        container: ViewGroup,
        position: String = "bottom",
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        val bannerSize = getAdaptiveBannerSize(activity)
        val extras = Bundle().apply {
            putString("collapsible", position)
        }
        loadBannerInternal(activity, container, bannerSize, extras, onLoaded, onFailed)
    }

    /**
     * Inline Adaptive Banner Ad (Ideal for RecyclerView items or scrollable content).
     * @param maxHeightDp Maximum allowable height in dp (default: 100)
     */
    fun showInlineBannerAd(
        activity: Activity,
        container: ViewGroup,
        maxHeightDp: Int = 100,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        val adWidth = getAdWidthInDp(activity)
        val bannerSize = AdSize.getInlineAdaptiveBannerAdSize(adWidth, maxHeightDp)
        loadBannerInternal(activity, container, bannerSize, null, onLoaded, onFailed)
    }

    /**
     * Medium Rectangle (MREC) Banner Ad (300x250).
     */
    fun showMediumRectangleBannerAd(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        loadBannerInternal(activity, container, AdSize.MEDIUM_RECTANGLE, null, onLoaded, onFailed)
    }

    private fun loadBannerInternal(
        activity: Activity,
        container: ViewGroup,
        adSize: AdSize,
        extras: Bundle?,
        onLoaded: (() -> Unit)?,
        onFailed: (() -> Unit)?
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onFailed?.invoke()
            return
        }

        if (config.bannerId.isBlank()) {
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }

        AdsInitializer.safeInitialize(activity, config.appId)

        if (!MobileAds.isInitialized) {
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }

        // Clean up previous AdView before removing
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? AdView)?.destroy()
        }
        container.removeAllViews()

        val adView = AdView(activity)
        container.addView(adView)

        val requestBuilder = BannerAdRequest.Builder(config.bannerId, adSize)
        if (extras != null) {
            requestBuilder.setGoogleExtrasBundle(extras)
        }
        val bannerRequest = requestBuilder.build()

        adView.loadAd(
            bannerRequest,
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    activity.runOnUiThread {
                        if (activity.isFinishing || activity.isDestroyed) {
                            adView.destroy()
                            return@runOnUiThread
                        }
                        container.visibility = View.VISIBLE
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        if (activity.isFinishing || activity.isDestroyed) {
                            return@runOnUiThread
                        }
                        container.visibility = View.GONE
                        onFailed?.invoke()
                    }
                }
            }
        )
    }

    private fun getAdaptiveBannerSize(activity: Activity): AdSize {
        val adWidth = getAdWidthInDp(activity)
        return AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }

    private fun getAdWidthInDp(activity: Activity): Int {
        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }
        val density = displayMetrics.density
        return (adWidthPixels / density).toInt()
    }
}
