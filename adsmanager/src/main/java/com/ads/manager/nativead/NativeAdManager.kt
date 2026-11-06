package com.ads.manager.nativead

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ads.manager.AdsConfig
import com.ads.manager.AdsManager
import com.ads.manager.R
import com.ads.manager.init.AdsInitializer
import com.ads.manager.shimmer.CustomShimmerFrameLayout
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdChoicesPlacement
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.MediaView
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView

class NativeAdManager(private val config: AdsConfig) {

    fun showLargeNativeAd(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showNativeAd(
            activity = activity,
            container = container,
            customShimmerResId = R.layout.layout_large_native_shimmer,
            customAdLayoutResId = R.layout.layout_large_native_ad,
            onLoaded = onLoaded,
            onFailed = onFailed
        )
    }

    fun showSmallNativeAd(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showNativeAd(
            activity = activity,
            container = container,
            customShimmerResId = R.layout.layout_small_native_shimmer,
            customAdLayoutResId = R.layout.layout_small_native_ad,
            onLoaded = onLoaded,
            onFailed = onFailed
        )
    }

    fun showNativeAd(
        activity: Activity,
        container: ViewGroup,
        customShimmerResId: Int? = null,
        customAdLayoutResId: Int? = null,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (AdsManager.isPremium) {
            container.removeAllViews()
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

        container.visibility = View.VISIBLE
        container.removeAllViews()

        val inflater = LayoutInflater.from(activity)
        val defaultShimmer = customShimmerResId ?: R.layout.layout_large_native_shimmer
        val shimmerLayout = inflater.inflate(defaultShimmer, container, false)
        container.addView(shimmerLayout)

        val shimmerContainer = shimmerLayout.findViewById<CustomShimmerFrameLayout>(R.id.shimmerViewContainer)
            ?: (shimmerLayout as? CustomShimmerFrameLayout)

        shimmerContainer?.let { shimmer ->
            shimmer.shimmerColor = config.shimmerColor
            shimmer.startShimmer()
        }

        val adRequest = NativeAdRequest.Builder(
            config.nativeId,
            listOf(NativeAd.NativeAdType.NATIVE)
        ).setAdChoicesPlacement(AdChoicesPlacement.TOP_RIGHT)
         .build()

        NativeAdLoader.load(
            adRequest,
            object : NativeAdLoaderCallback {
                override fun onNativeAdLoaded(nativeAd: NativeAd) {
                    activity.runOnUiThread {
                        if (activity.isFinishing || activity.isDestroyed || AdsManager.isPremium) {
                            shimmerContainer?.stopShimmer()
                            container.removeAllViews()
                            if (AdsManager.isPremium) container.visibility = View.GONE
                            nativeAd.destroy()
                            return@runOnUiThread
                        }

                        shimmerContainer?.stopShimmer()
                        container.removeAllViews()

                        val defaultLayout = customAdLayoutResId ?: R.layout.layout_large_native_ad
                        val adView = inflater.inflate(defaultLayout, container, false) as NativeAdView

                        bindNativeAdView(nativeAd, adView)
                        container.addView(adView)
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        shimmerContainer?.stopShimmer()
                        container.visibility = View.GONE
                        onFailed?.invoke()
                    }
                }
            }
        )
    }

    private fun bindNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.ad_headline)
        val advertiserView = adView.findViewById<TextView>(R.id.ad_advertiser)
        val bodyView = adView.findViewById<TextView>(R.id.ad_body)
        val callToActionView = adView.findViewById<Button>(R.id.ad_call_to_action)
        val iconView = adView.findViewById<ImageView>(R.id.ad_app_icon)
        val centerIconView = adView.findViewById<ImageView>(R.id.ad_center_app_icon)
        val mediaView = adView.findViewById<MediaView>(R.id.ad_media)

        headlineView?.let {
            it.text = nativeAd.headline
            adView.headlineView = it
        }

        advertiserView?.let {
            if (nativeAd.advertiser.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.text = nativeAd.advertiser
                it.visibility = View.VISIBLE
            }
            adView.advertiserView = it
        }

        bodyView?.let {
            if (nativeAd.body.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.text = nativeAd.body
                it.visibility = View.VISIBLE
            }
            adView.bodyView = it
        }

        callToActionView?.let {
            if (nativeAd.callToAction.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.text = nativeAd.callToAction
                it.visibility = View.VISIBLE
            }
            adView.callToActionView = it
        }

        val mediaContent = nativeAd.mediaContent
        val hasMedia = mediaContent.hasVideoContent || mediaContent.mainImage != null

        if (hasMedia) {
            mediaView?.visibility = View.VISIBLE
            mediaView?.mediaContent = mediaContent
            centerIconView?.visibility = View.GONE

            if (nativeAd.icon == null) {
                iconView?.visibility = View.GONE
            } else {
                iconView?.setImageDrawable(nativeAd.icon?.drawable)
                iconView?.visibility = View.VISIBLE
            }
            adView.iconView = iconView
        } else {
            mediaView?.visibility = View.GONE
            iconView?.visibility = View.GONE

            if (nativeAd.icon == null) {
                centerIconView?.visibility = View.GONE
            } else {
                centerIconView?.setImageDrawable(nativeAd.icon?.drawable)
                centerIconView?.visibility = View.VISIBLE
            }
            adView.iconView = centerIconView
        }

        adView.registerNativeAd(nativeAd, mediaView)
    }
}
