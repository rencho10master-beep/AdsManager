package com.ads.manager

import android.app.Activity
import android.app.Application
import android.view.View
import android.view.ViewGroup
import com.ads.manager.appopen.ResumeAppOpenManager
import com.ads.manager.appopen.SplashAppOpenManager
import com.ads.manager.banner.BannerAdManager
import com.ads.manager.init.AdsInitializer
import com.ads.manager.interstitial.AdClickTracker
import com.ads.manager.interstitial.InterstitialAdManager
import com.ads.manager.nativead.NativeAdManager
import com.ads.manager.rewarded.AdReward
import com.ads.manager.rewarded.RewardedAdManager

object AdsManager {
    private var config: AdsConfig = AdsConfig()
    private var clickTracker: AdClickTracker = AdClickTracker(2)

    private var interstitialManager: InterstitialAdManager? = null
    private var nativeManager: NativeAdManager? = null
    private var bannerManager: BannerAdManager? = null
    private var rewardedManager: RewardedAdManager? = null
    private var splashAppOpenManager: SplashAppOpenManager? = null
    private var resumeAppOpenManager: ResumeAppOpenManager? = null

    @Volatile
    var isPremium: Boolean = false

    var isSubscribed: Boolean
        get() = isPremium
        set(value) {
            isPremium = value
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

    fun showInterstitial(activity: Activity, count: Int = 1, onComplete: () -> Unit) {
        if (isPremium) {
            onComplete()
            return
        }
        interstitialManager?.showAd(activity, count, onComplete) ?: onComplete()
    }

    fun showLargeNative(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        nativeManager?.showLargeNativeAd(activity, container, onLoaded, onFailed)
    }

    fun showNativeLarge(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showLargeNative(activity, container, onLoaded, onFailed)
    }

    fun showSmallNative(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        nativeManager?.showSmallNativeAd(activity, container, onLoaded, onFailed)
    }

    fun showNativeSmall(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showSmallNative(activity, container, onLoaded, onFailed)
    }

    fun showNative(
        activity: Activity,
        container: ViewGroup,
        customShimmerResId: Int? = null,
        customAdLayoutResId: Int? = null,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        nativeManager?.showNativeAd(
            activity,
            container,
            customShimmerResId,
            customAdLayoutResId,
            onLoaded,
            onFailed
        )
    }

    fun showBanner(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        bannerManager?.showBannerAd(activity, container, onLoaded, onFailed)
    }

    fun showCollapsibleBanner(
        activity: Activity,
        container: ViewGroup,
        position: String = "bottom",
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        bannerManager?.showCollapsibleBannerAd(activity, container, position, onLoaded, onFailed)
    }

    fun showInlineBanner(
        activity: Activity,
        container: ViewGroup,
        maxHeightDp: Int = 100,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        bannerManager?.showInlineBannerAd(activity, container, maxHeightDp, onLoaded, onFailed)
    }

    fun showMediumRectangleBanner(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (isPremium) {
            container.removeAllViews()
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }
        bannerManager?.showMediumRectangleBannerAd(activity, container, onLoaded, onFailed)
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
