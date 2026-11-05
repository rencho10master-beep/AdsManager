package com.ads.manager

import android.app.Activity
import android.app.Application
import android.view.View
import android.view.ViewGroup
import com.ads.manager.appopen.ResumeAppOpenManager
import com.ads.manager.appopen.SplashAppOpenManager
import com.ads.manager.banner.BannerAdManager
import com.ads.manager.dialog.AdLoadingDialog
import com.ads.manager.init.AdsInitializer
import com.ads.manager.interstitial.AdClickTracker
import com.ads.manager.interstitial.InterstitialAdManager
import com.ads.manager.nativead.NativeAdManager
import com.ads.manager.rewarded.AdReward
import com.ads.manager.rewarded.RewardedAdManager
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

object AdsManager {
    private var config: AdsConfig = AdsConfig()
    private var clickTracker: AdClickTracker = AdClickTracker(1)

    private var interstitialManager: InterstitialAdManager? = null
    private var nativeManager: NativeAdManager? = null
    private var bannerManager: BannerAdManager? = null
    private var rewardedManager: RewardedAdManager? = null
    private var splashAppOpenManager: SplashAppOpenManager? = null
    private var resumeAppOpenManager: ResumeAppOpenManager? = null

    @JvmStatic
    @Volatile
    var isPremium: Boolean = false
        set(value) {
            field = value
            if (value) {
                // Instantly dismiss any loading dialogs and clear ads
                AdLoadingDialog.dismiss()
                interstitialManager?.clearAd()
                rewardedManager?.clearAd()
            }
        }

    @JvmStatic
    var isSubscribed: Boolean
        get() = isPremium
        set(value) {
            isPremium = value
        }

    /**
     * Helper function to update premium status.
     * When set to true, all ads and shimmers are immediately killed and disabled.
     */
    @JvmStatic
    fun updatePremiumStatus(premium: Boolean) {
        isPremium = premium
    }

    /**
     * Alternative helper: setAdsEnabled(false) turns OFF all ads.
     */
    @JvmStatic
    fun setAdsEnabled(enabled: Boolean) {
        isPremium = !enabled
    }

    @JvmStatic
    fun isAdsEnabled(): Boolean {
        return !isPremium
    }

    fun init(
        application: Application,
        customConfig: AdsConfig = AdsConfig(),
        enableResumeAppOpenAd: Boolean = true
    ) {
        config = customConfig
        clickTracker = AdClickTracker(customConfig.interstitialInterval)

        interstitialManager = InterstitialAdManager(config, clickTracker)
        nativeManager = NativeAdManager(config)
        bannerManager = BannerAdManager(config)
        rewardedManager = RewardedAdManager(config)
        splashAppOpenManager = SplashAppOpenManager(config)

        if (enableResumeAppOpenAd) {
            resumeAppOpenManager = ResumeAppOpenManager(application, config)
            resumeAppOpenManager?.loadAd()
        }

        AdsInitializer.safeInitialize(application.applicationContext, config.appId)
    }

    fun preloadAds(activity: Activity) {
        if (isPremium) return
        interstitialManager?.loadAd(activity)
        rewardedManager?.loadAd(activity)
        resumeAppOpenManager?.loadAd()
    }

    fun loadSplashAppOpenAd(activity: Activity, onLoaded: (() -> Unit)? = null) {
        if (isPremium) {
            onLoaded?.invoke()
            return
        }
        splashAppOpenManager?.loadSplashAd(activity, onLoaded) ?: onLoaded?.invoke()
    }

    fun showSplashAppOpenAd(activity: Activity, onComplete: () -> Unit) {
        if (isPremium) {
            onComplete()
            return
        }
        splashAppOpenManager?.showSplashAd(activity, onComplete) ?: onComplete()
    }

    fun loadAndShowSplashAppOpenAd(
        activity: Activity,
        timeoutMs: Long = 6000L,
        onComplete: () -> Unit
    ) {
        if (isPremium) {
            onComplete()
            return
        }
        splashAppOpenManager?.loadAndShowSplashAd(activity, timeoutMs, onComplete) ?: onComplete()
    }

    /**
     * Show Interstitial Ad.
     * Ads fail thaye ya close kare atyare onComplete() call thase.
     * @param count 1 = Har vakhte show thase (Every single click)
     *              2 = Dar 2ji var show thase
     *              3 = Dar 3ji var show thase
     */
    @JvmOverloads
    fun showInterstitial(
        activity: Activity,
        count: Int = config.interstitialInterval,
        onComplete: () -> Unit
    ) {
        if (isPremium) {
            onComplete()
            return
        }
        interstitialManager?.showAd(activity, count, onComplete) ?: onComplete()
    }

    /**
     * Show Native Ad inside any ViewGroup (CardView, LinearLayout, FrameLayout, etc.)
     * @param customAdLayoutResId Optional custom designed XML layout (e.g. R.layout.my_native_ad)
     */
    @JvmOverloads
    fun showNative(
        activity: Activity,
        container: ViewGroup,
        customAdLayoutResId: Int? = null,
        customShimmerResId: Int? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        nativeManager?.showNativeAd(
            activity = activity,
            container = container,
            customAdLayoutResId = customAdLayoutResId,
            customShimmerResId = customShimmerResId
        )
    }

    /**
     * Show Small (Compact) Native Ad (ideal for lists or bottom banner area).
     */
    fun showNativeSmall(
        activity: Activity,
        container: ViewGroup
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        nativeManager?.showNativeSmall(activity, container)
    }

    /**
     * Directly bind Native Ad to an existing NativeAdView defined in your XML layout!
     */
    fun bindNative(
        activity: Activity,
        nativeAdView: NativeAdView
    ) {
        if (isPremium) {
            nativeAdView.visibility = View.GONE
            return
        }
        nativeManager?.bindNativeAd(activity, nativeAdView)
    }

    fun showBanner(
        activity: Activity,
        container: ViewGroup
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        bannerManager?.showBannerAd(activity, container)
    }

    fun showRewarded(
        activity: Activity,
        onEarned: (AdReward) -> Unit,
        onClosed: () -> Unit
    ) {
        if (isPremium) {
            onClosed()
            return
        }
        rewardedManager?.showAd(activity, onEarned, onClosed) ?: onClosed()
    }
}
