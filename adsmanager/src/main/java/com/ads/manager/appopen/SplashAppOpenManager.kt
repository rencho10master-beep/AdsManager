package com.ads.manager.appopen

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.ads.manager.AdsConfig
import com.ads.manager.AdsManager
import com.ads.manager.init.AdsInitializer
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

class SplashAppOpenManager(private val config: AdsConfig) {
    private var splashAd: AppOpenAd? = null
    private var isAdLoading = false

    fun loadSplashAd(activity: Activity, onLoaded: (() -> Unit)? = null) {
        if (AdsManager.isPremium || config.splashAppOpenId.isBlank()) {
            onLoaded?.invoke()
            return
        }

        AdsInitializer.safeInitialize(activity, config.appId)

        if (!MobileAds.isInitialized || isAdLoading || splashAd != null) {
            return
        }

        isAdLoading = true
        val adRequest = AdRequest.Builder(config.splashAppOpenId).build()
        AppOpenAd.load(
            adRequest,
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    activity.runOnUiThread {
                        splashAd = ad
                        isAdLoading = false
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        splashAd = null
                        isAdLoading = false
                    }
                }
            }
        )
    }

    fun showSplashAd(activity: Activity, onComplete: () -> Unit) {
        if (activity.isFinishing || activity.isDestroyed || AdsManager.isPremium || config.splashAppOpenId.isBlank()) {
            onComplete()
            return
        }

        val ad = splashAd
        if (ad == null) {
            onComplete()
            return
        }

        ad.adEventCallback = object : AppOpenAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                activity.runOnUiThread {
                    splashAd = null
                    onComplete()
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                activity.runOnUiThread {
                    splashAd = null
                    onComplete()
                }
            }

            override fun onAdShowedFullScreenContent() {
                splashAd = null
            }
        }

        try {
            ad.show(activity)
        } catch (_: Exception) {
            splashAd = null
            onComplete()
        }
    }

    fun loadAndShowSplashAd(
        activity: Activity,
        timeoutMs: Long = 6000L,
        onComplete: () -> Unit
    ) {
        if (AdsManager.isPremium || config.splashAppOpenId.isBlank()) {
            onComplete()
            return
        }

        if (splashAd != null) {
            showSplashAd(activity, onComplete)
            return
        }

        var isCompleted = false
        val timeoutHandler = Handler(Looper.getMainLooper())
        val timeoutRunnable = Runnable {
            if (!isCompleted) {
                isCompleted = true
                onComplete()
            }
        }
        timeoutHandler.postDelayed(timeoutRunnable, timeoutMs)

        loadSplashAd(activity) {
            if (!isCompleted) {
                timeoutHandler.removeCallbacks(timeoutRunnable)
                isCompleted = true
                showSplashAd(activity, onComplete)
            }
        }
    }
}
