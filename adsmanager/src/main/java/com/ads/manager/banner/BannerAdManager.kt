package com.ads.manager.banner

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowMetrics
import com.ads.manager.AdsConfig
import com.ads.manager.callback.BannerAdCallback
import com.ads.manager.init.AdsInitializer
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

class BannerAdManager(private val config: AdsConfig) {

    fun showBannerAd(
        activity: Activity,
        container: ViewGroup,
        callback: BannerAdCallback? = null,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        AdsInitializer.safeInitialize(activity, config.appId)

        if (!MobileAds.isInitialized) {
            container.visibility = View.GONE
            callback?.onBannerAdFailed()
            onFailed?.invoke()
            return
        }

        val adView = AdView(activity)
        container.removeAllViews()
        container.addView(adView)

        val bannerSize = getAdaptiveBannerSize(activity)
        val bannerRequest = BannerAdRequest.Builder(config.bannerId, bannerSize).build()

        adView.loadAd(
            bannerRequest,
            object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    activity.runOnUiThread {
                        if (activity.isFinishing || activity.isDestroyed) {
                            return@runOnUiThread
                        }
                        ad.adEventCallback = object : BannerAdEventCallback {
                            override fun onAdClicked() {
                                activity.runOnUiThread {
                                    callback?.onBannerAdClicked()
                                }
                            }
                        }
                        container.visibility = View.VISIBLE
                        callback?.onBannerAdLoaded()
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        container.visibility = View.GONE
                        callback?.onBannerAdFailed()
                        onFailed?.invoke()
                    }
                }
            }
        )
    }

    private fun getAdaptiveBannerSize(activity: Activity): AdSize {
        val displayMetrics = activity.resources.displayMetrics
        val adWidthPixels = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics: WindowMetrics = activity.windowManager.currentWindowMetrics
            windowMetrics.bounds.width()
        } else {
            displayMetrics.widthPixels
        }
        val density = displayMetrics.density
        val adWidth = (adWidthPixels / density).toInt()
        return AdSize.getLargeAnchoredAdaptiveBannerAdSize(activity, adWidth)
    }
}
