package com.ads.manager.interstitial

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.ads.manager.AdsConfig
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
    private var isShowingAd = false
    private var lastClickTime: Long = 0

    fun loadAd(activity: Activity, onLoaded: (() -> Unit)? = null) {
        if (config.interstitialId.isBlank()) return

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
        count: Int = 1,
        onComplete: () -> Unit
    ) {
        // Activity safety check
        if (activity.isFinishing || activity.isDestroyed) {
            onComplete()
            return
        }

        // Prevent rapid duplicate clicks (debounce 600ms)
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 600) {
            return
        }
        lastClickTime = currentTime

        // Prevent overlapping ad shows
        if (isShowingAd) {
            return
        }

        // If interstitialId is empty, safely proceed immediately without crash
        if (config.interstitialId.isBlank()) {
            onComplete()
            return
        }

        // 1. If not matched click count, proceed without ad
        if (!clickTracker.shouldShowAd(count)) {
            onComplete()
            return
        }

        // 2. If ad is not loaded, reload for next time and proceed
        val ad = interstitialAd
        if (ad == null) {
            loadAd(activity)
            onComplete()
            return
        }

        // 3. Show Loading Dialog
        isShowingAd = true
        AdLoadingDialog.show(activity, config)

        Handler(Looper.getMainLooper()).postDelayed({
            if (activity.isFinishing || activity.isDestroyed) {
                AdLoadingDialog.dismiss()
                isShowingAd = false
                onComplete()
                return@postDelayed
            }

            ad.adEventCallback = object : InterstitialAdEventCallback {
                // 4. Ad Dismissed
                override fun onAdDismissedFullScreenContent() {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        isShowingAd = false
                        interstitialAd = null
                        loadAd(activity)
                        onComplete()
                    }
                }

                // 5. Ad Failed To Show
                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        isShowingAd = false
                        interstitialAd = null
                        loadAd(activity)
                        onComplete()
                    }
                }

                override fun onAdShowedFullScreenContent() {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                    }
                }
            }

            try {
                ad.show(activity)
            } catch (_: Exception) {
                AdLoadingDialog.dismiss()
                isShowingAd = false
                interstitialAd = null
                loadAd(activity)
                onComplete()
            }
        }, 600)
    }

    fun clearAd() {
        interstitialAd = null
        isAdLoading = false
        isShowingAd = false
    }
}
