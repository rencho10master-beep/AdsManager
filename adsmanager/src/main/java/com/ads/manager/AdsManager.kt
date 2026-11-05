package com.ads.manager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.ads.manager.appopen.ResumeAppOpenManager
import com.ads.manager.appopen.SplashAppOpenManager
import com.ads.manager.banner.BannerAdManager
import com.ads.manager.consent.ConsentManager
import com.ads.manager.dialog.AdLoadingDialog
import com.ads.manager.dns.DnsHelper
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

    @Volatile
    private var _isPremium: Boolean = false

    /**
     * Function to set Premium status:
     * AdsManager.isPremium(true) -> Instantly kills all ads and shimmers!
     * AdsManager.isPremium(false) -> Normal ads mode.
     */
    @JvmStatic
    fun isPremium(premium: Boolean) {
        _isPremium = premium
        if (premium) {
            // Instantly dismiss any loading dialogs and clear ads
            AdLoadingDialog.dismiss()
            interstitialManager?.clearAd()
            rewardedManager?.clearAd()
        }
    }

    /**
     * Function to check if user is Premium:
     * if (AdsManager.isPremium()) { ... }
     */
    @JvmStatic
    fun isPremium(): Boolean {
        return _isPremium
    }

    /**
     * Alternative helper: setAdsEnabled(false) turns OFF all ads.
     */
    @JvmStatic
    fun setAdsEnabled(enabled: Boolean) {
        isPremium(!enabled)
    }

    @JvmStatic
    fun isAdsEnabled(): Boolean {
        return !_isPremium
    }

    private var applicationContext: Context? = null

    /**
     * Variable to check if Private DNS is enabled (ON).
     * If Private DNS is ON -> returns true
     * If Private DNS is OFF -> returns false
     */
    @JvmStatic
    val isDNS: Boolean
        get() = applicationContext?.let { DnsHelper.isPrivateDnsEnabled(it) } ?: false

    /**
     * Check if Private DNS is enabled by passing Context:
     * e.g. AdsManager.isDNS(this) or AdsManager.checkDNS(this)
     */
    @JvmStatic
    fun isDNS(context: Context): Boolean {
        return DnsHelper.isPrivateDnsEnabled(context)
    }

    @JvmStatic
    fun checkDNS(context: Context): Boolean {
        return DnsHelper.isPrivateDnsEnabled(context)
    }

    /**
     * Request Google UMP GDPR Consent (Required for European Union & UK users).
     * Automatically displays Google Consent form if user is in EU/EEA/UK.
     * Skips automatically for other countries (e.g. India, US).
     * @param onComplete Callback with boolean indicating if ads can be requested.
     */
    @JvmStatic
    fun requestConsent(activity: Activity, onComplete: (canRequestAds: Boolean) -> Unit) {
        ConsentManager.requestConsent(activity, onComplete)
    }

    /**
     * Show Privacy Options Form (allows user to change their consent from Settings screen).
     */
    @JvmStatic
    fun showPrivacyOptions(activity: Activity, onDismissed: (() -> Unit)? = null) {
        ConsentManager.showPrivacyOptionsForm(activity, onDismissed)
    }

    @JvmStatic
    fun canRequestAds(activity: Activity): Boolean {
        return ConsentManager.canRequestAds(activity)
    }

    fun init(
        application: Application,
        customConfig: AdsConfig = AdsConfig(),
        enableResumeAppOpenAd: Boolean = true
    ) {
        applicationContext = application.applicationContext
        config = customConfig
        clickTracker = AdClickTracker(1)

        interstitialManager = InterstitialAdManager(config, clickTracker)
        nativeManager = NativeAdManager(config)
        bannerManager = BannerAdManager(config)
        rewardedManager = RewardedAdManager(config)
        splashAppOpenManager = SplashAppOpenManager(config)

        if (enableResumeAppOpenAd && config.resumeAppOpenId.isNotBlank()) {
            resumeAppOpenManager = ResumeAppOpenManager(application, config)
            resumeAppOpenManager?.loadAd()
        }

        AdsInitializer.safeInitialize(application.applicationContext, config.appId)
    }

    fun preloadAds(activity: Activity) {
        if (isPremium()) return
        interstitialManager?.loadAd(activity)
        rewardedManager?.loadAd(activity)
        resumeAppOpenManager?.loadAd()
    }

    fun loadSplashAppOpenAd(activity: Activity, onLoaded: (() -> Unit)? = null) {
        if (isPremium()) {
            onLoaded?.invoke()
            return
        }
        splashAppOpenManager?.loadSplashAd(activity, onLoaded) ?: onLoaded?.invoke()
    }

    fun showSplashAppOpenAd(activity: Activity, onComplete: () -> Unit) {
        if (isPremium()) {
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
        if (isPremium()) {
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
        count: Int = 1,
        onComplete: () -> Unit
    ) {
        if (isPremium()) {
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
        if (isPremium()) {
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
        if (isPremium()) {
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
        if (isPremium()) {
            nativeAdView.visibility = View.GONE
            return
        }
        nativeManager?.bindNativeAd(activity, nativeAdView)
    }

    /**
     * Standard Anchored Adaptive Banner
     */
    fun showBanner(
        activity: Activity,
        container: ViewGroup
    ) {
        if (isPremium()) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        bannerManager?.showBannerAd(activity, container)
    }

    /**
     * Collapsible Banner Ad (Expands initially and collapses).
     * @param position "bottom" (default) or "top"
     */
    @JvmOverloads
    fun showCollapsibleBanner(
        activity: Activity,
        container: ViewGroup,
        position: String = "bottom"
    ) {
        if (isPremium()) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        bannerManager?.showCollapsibleBannerAd(activity, container, position)
    }

    /**
     * Inline Adaptive Banner Ad (for RecyclerView or Scrollable lists).
     * @param maxHeightDp Maximum allowed banner height in dp (default: 100)
     */
    @JvmOverloads
    fun showInlineBanner(
        activity: Activity,
        container: ViewGroup,
        maxHeightDp: Int = 100
    ) {
        if (isPremium()) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        bannerManager?.showInlineBannerAd(activity, container, maxHeightDp)
    }

    /**
     * Medium Rectangle (MREC) Banner Ad (300x250).
     */
    fun showMediumRectangleBanner(
        activity: Activity,
        container: ViewGroup
    ) {
        if (isPremium()) {
            container.removeAllViews()
            container.visibility = View.GONE
            return
        }
        bannerManager?.showMediumRectangleBannerAd(activity, container)
    }

    fun showRewarded(
        activity: Activity,
        onEarned: (AdReward) -> Unit,
        onClosed: () -> Unit
    ) {
        if (isPremium()) {
            onClosed()
            return
        }
        rewardedManager?.showAd(activity, onEarned, onClosed) ?: onClosed()
    }
}
