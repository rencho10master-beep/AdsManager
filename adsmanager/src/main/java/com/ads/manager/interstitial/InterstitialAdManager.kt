package com.ads.manager.interstitial

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.ads.manager.AdsConfig
import com.ads.manager.callback.InterstitialAdCallback
import com.ads.manager.dialog.AdLoadingDialog
import com.ads.manager.init.AdsInitializer
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback

class InterstitialAdManager(
    private val config: AdsConfig,
    private val clickTracker: AdClickTracker
) {
    private var interstitialAd: InterstitialAd? = null
    private var isAdLoading = false

    fun loadAd(activity: Activity, onLoaded: (() -> Unit)? = null) {
        AdsInitializer.safeInitialize(activity, config.appId)

        if (!MobileAds.isInitialized || isAdLoading || interstitialAd != null) {
            return
        }

        isAdLoading = true
        val adRequest = AdRequest.Builder(config.interstitialId).build()
        InterstitialAd.load(
            adRequest,
            object : AdLoadCallback<InterstitialAd> {
                override fun onAdLoaded(ad: InterstitialAd) {
                    activity.runOnUiThread {
                        interstitialAd = ad
                        isAdLoading = false
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        interstitialAd = null
                        isAdLoading = false
                    }
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        count: Int = config.interstitialInterval,
        callback: InterstitialAdCallback? = null,
        onComplete: () -> Unit
    ) {
        if (!clickTracker.shouldShowAd(count)) {
            callback?.onInterstitialAdDismissed()
            onComplete()
            return
        }

        val ad = interstitialAd
        if (ad == null) {
            loadAd(activity)
            callback?.onInterstitialAdFailed()
            onComplete()
            return
        }

        AdLoadingDialog.show(activity, config)

        Handler(Looper.getMainLooper()).postDelayed({
            if (activity.isFinishing || activity.isDestroyed) {
                AdLoadingDialog.dismiss()
                callback?.onInterstitialAdFailed()
                onComplete()
                return@postDelayed
            }

            ad.adEventCallback = object : InterstitialAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        interstitialAd = null
                        loadAd(activity)
                        callback?.onInterstitialAdDismissed()
                        onComplete()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        interstitialAd = null
                        loadAd(activity)
                        callback?.onInterstitialAdFailed()
                        onComplete()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        callback?.onInterstitialAdShown()
                    }
                }

                override fun onAdClicked() {
                    activity.runOnUiThread {
                        callback?.onInterstitialAdClicked()
                    }
                }
            }

            ad.show(activity)
        }, 600)
    }

    fun clearAd() {
        interstitialAd = null
        isAdLoading = false
    }
}
