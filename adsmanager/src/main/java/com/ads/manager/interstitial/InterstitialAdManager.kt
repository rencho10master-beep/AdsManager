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
        // If interstitialId is empty, safely proceed immediately without crash
        if (config.interstitialId.isBlank()) {
            onComplete()
            return
        }

        // ૧. જો ક્લિક કાઉન્ટ પ્રમાણે Ad બતાવવાની ન હોય
        if (!clickTracker.shouldShowAd(count)) {
            onComplete()
            return
        }

        // ૨. જો Ad હજુ લોડ નથી થઈ (Fail થઈ હોય) તો નેક્સ્ટ ટાઇમ માટે લોડ કરી તરત onComplete() કોલ કરો
        val ad = interstitialAd
        if (ad == null) {
            loadAd(activity)
            onComplete()
            return
        }

        // ૩. Loading Dialog બતાવો
        AdLoadingDialog.show(activity, config)

        Handler(Looper.getMainLooper()).postDelayed({
            if (activity.isFinishing || activity.isDestroyed) {
                AdLoadingDialog.dismiss()
                onComplete()
                return@postDelayed
            }

            ad.adEventCallback = object : InterstitialAdEventCallback {
                // ૪. યુઝર Ad ક્લોઝ કરે ત્યારે:
                override fun onAdDismissedFullScreenContent() {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        interstitialAd = null
                        loadAd(activity)
                        onComplete()
                    }
                }

                // ૫. જો Ad બતાવવામાં ફેલ થાય ત્યારે:
                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
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

            ad.show(activity)
        }, 600)
    }

    fun clearAd() {
        interstitialAd = null
        isAdLoading = false
    }
}
