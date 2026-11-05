package com.ads.manager.appopen

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.ads.manager.AdsConfig
import com.ads.manager.AdsManager
import com.ads.manager.dialog.AdLoadingDialog
import com.ads.manager.init.AdsInitializer
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAd
import com.google.android.libraries.ads.mobile.sdk.appopen.AppOpenAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError

class ResumeAppOpenManager(
    private val application: Application,
    private val config: AdsConfig
) : DefaultLifecycleObserver, Application.ActivityLifecycleCallbacks {

    private var resumeAd: AppOpenAd? = null
    private var isAdLoading = false
    private var currentActivity: Activity? = null
    private var isShowingAd = false
    private var loadTime: Long = 0

    init {
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        showAdIfAvailable()
    }

    fun loadAd() {
        if (AdsManager.isPremium()) return
        if (isAdAvailable() || isAdLoading) return

        AdsInitializer.safeInitialize(application, config.appId)

        if (!MobileAds.isInitialized) return

        isAdLoading = true
        val adRequest = AdRequest.Builder(config.resumeAppOpenId).build()
        AppOpenAd.load(
            adRequest,
            object : AdLoadCallback<AppOpenAd> {
                override fun onAdLoaded(ad: AppOpenAd) {
                    resumeAd = ad
                    isAdLoading = false
                    loadTime = SystemClock.elapsedRealtime()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    resumeAd = null
                    isAdLoading = false
                }
            }
        )
    }

    private fun isAdAvailable(): Boolean {
        val wasLoadedRecently = (SystemClock.elapsedRealtime() - loadTime) < 4 * 3600 * 1000
        return resumeAd != null && wasLoadedRecently
    }

    private fun showAdIfAvailable() {
        if (AdsManager.isPremium() || isShowingAd) return

        val activity = currentActivity ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val ad = resumeAd
        if (ad == null) {
            loadAd()
            return
        }

        isShowingAd = true
        AdLoadingDialog.show(activity, config)

        Handler(Looper.getMainLooper()).postDelayed({
            if (activity.isFinishing || activity.isDestroyed) {
                AdLoadingDialog.dismiss()
                isShowingAd = false
                return@postDelayed
            }

            ad.adEventCallback = object : AppOpenAdEventCallback {
                override fun onAdDismissedFullScreenContent() {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        resumeAd = null
                        isShowingAd = false
                        loadAd()
                    }
                }

                override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                    activity.runOnUiThread {
                        AdLoadingDialog.dismiss()
                        resumeAd = null
                        isShowingAd = false
                        loadAd()
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

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}
