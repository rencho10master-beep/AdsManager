package com.ads.manager.nativead

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.ads.manager.AdsConfig
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

    fun showNativeAd(
        activity: Activity,
        container: ViewGroup,
        customShimmerResId: Int? = null,
        customAdLayoutResId: Int? = null,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        AdsInitializer.safeInitialize(activity, config.appId)

        if (!MobileAds.isInitialized) {
            container.visibility = View.GONE
            onFailed?.invoke()
            return
        }

        container.visibility = View.VISIBLE
        container.removeAllViews()

        val inflater = LayoutInflater.from(activity)
        val shimmerLayout = inflater.inflate(
            customShimmerResId ?: R.layout.layout_shimmer_native,
            container,
            false
        )
        container.addView(shimmerLayout)

        val shimmerContainer = shimmerLayout.findViewById<CustomShimmerFrameLayout>(R.id.shimmerViewContainer)
            ?: (shimmerLayout as? CustomShimmerFrameLayout)

        shimmerContainer?.let { shimmer ->
            shimmer.baseColor = config.shimmerBaseColor
            shimmer.highlightColor = config.shimmerHighlightColor
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
                        if (activity.isFinishing || activity.isDestroyed) {
                            nativeAd.destroy()
                            return@runOnUiThread
                        }

                        shimmerContainer?.stopShimmer()
                        container.removeAllViews()

                        val adView = inflater.inflate(
                            customAdLayoutResId ?: R.layout.layout_default_native_ad,
                            container,
                            false
                        ) as NativeAdView

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
        val headlineView = adView.findViewById<TextView>(R.id.adHeadline)
        val bodyView = adView.findViewById<TextView>(R.id.adBody)
        val callToActionView = adView.findViewById<Button>(R.id.adCallToAction)
        val iconView = adView.findViewById<ImageView>(R.id.adAppIcon)
        val mediaView = adView.findViewById<MediaView>(R.id.adMedia)

        headlineView?.let {
            it.text = nativeAd.headline
            adView.headlineView = it
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

        iconView?.let {
            val icon = nativeAd.icon
            if (icon?.drawable != null) {
                it.setImageDrawable(icon.drawable)
                it.visibility = View.VISIBLE
                adView.iconView = it
            } else {
                it.visibility = View.GONE
            }
        }

        val mediaContent = nativeAd.mediaContent
        if (mediaView != null && (mediaContent.hasVideoContent || mediaContent.mainImage != null)) {
            mediaView.visibility = View.VISIBLE
            mediaView.mediaContent = mediaContent
        } else {
            mediaView?.visibility = View.GONE
        }

        adView.registerNativeAd(nativeAd, mediaView)
    }
}
