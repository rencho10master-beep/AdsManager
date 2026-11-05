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
        customAdLayoutResId: Int? = null,
        customShimmerResId: Int? = null,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onFailed?.invoke()
            return
        }

        AdsInitializer.safeInitialize(activity, config.appId)

        if (config.nativeId.isBlank() || !MobileAds.isInitialized) {
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

                        val inflatedLayout = inflater.inflate(
                            customAdLayoutResId ?: R.layout.layout_default_native_ad,
                            container,
                            false
                        )

                        val nativeAdView = resolveNativeAdView(activity, inflatedLayout)
                        bindNativeAdView(nativeAd, nativeAdView)

                        val viewToAdd = if (nativeAdView.parent != null) {
                            inflatedLayout
                        } else {
                            nativeAdView
                        }
                        container.addView(viewToAdd)
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

    fun showNativeSmall(
        activity: Activity,
        container: ViewGroup,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        showNativeAd(
            activity = activity,
            container = container,
            customAdLayoutResId = R.layout.layout_small_native_ad,
            customShimmerResId = R.layout.layout_shimmer_small_native,
            onLoaded = onLoaded,
            onFailed = onFailed
        )
    }

    fun bindNativeAd(
        activity: Activity,
        nativeAdView: NativeAdView,
        onLoaded: (() -> Unit)? = null,
        onFailed: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onFailed?.invoke()
            return
        }

        AdsInitializer.safeInitialize(activity, config.appId)

        if (config.nativeId.isBlank() || !MobileAds.isInitialized) {
            nativeAdView.visibility = View.GONE
            onFailed?.invoke()
            return
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
                        bindNativeAdView(nativeAd, nativeAdView)
                        nativeAdView.visibility = View.VISIBLE
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        nativeAdView.visibility = View.GONE
                        onFailed?.invoke()
                    }
                }
            }
        )
    }

    private fun resolveNativeAdView(activity: Activity, view: View): NativeAdView {
        if (view is NativeAdView) {
            return view
        }

        val found = findNativeAdView(view)
        if (found != null) {
            return found
        }

        val wrapper = NativeAdView(activity)
        wrapper.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        (view.parent as? ViewGroup)?.removeView(view)
        wrapper.addView(view)
        return wrapper
    }

    private fun findNativeAdView(view: View): NativeAdView? {
        if (view is NativeAdView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child = view.getChildAt(i)
                val found = findNativeAdView(child)
                if (found != null) return found
            }
        }
        return null
    }

    private fun bindNativeAdView(nativeAd: NativeAd, adView: NativeAdView) {
        val headlineView = adView.findViewById<TextView>(R.id.adHeadline) ?: adView.findViewWithTag("adHeadline")
        val bodyView = adView.findViewById<TextView>(R.id.adBody) ?: adView.findViewWithTag("adBody")
        val callToActionView = adView.findViewById<Button>(R.id.adCallToAction) ?: adView.findViewWithTag("adCallToAction")
        val iconView = adView.findViewById<ImageView>(R.id.adAppIcon) ?: adView.findViewWithTag("adAppIcon")
        val mediaView = adView.findViewById<MediaView>(R.id.adMedia) ?: adView.findViewWithTag("adMedia")
        val attributionView = adView.findViewById<TextView>(R.id.adAttribution) ?: adView.findViewWithTag("adAttribution")

        // 1. Root Native Ad Background Color
        config.nativeBackgroundColor?.let { bgColor ->
            adView.setBackgroundColor(bgColor)
        }

        // 2. Typeface / Font Family (from res/font/, Typeface object, or font name)
        val resolvedTypeface: android.graphics.Typeface? = config.nativeFontFamily
            ?: config.nativeFontResId?.let { fontRes ->
                try {
                    androidx.core.content.res.ResourcesCompat.getFont(adView.context, fontRes)
                } catch (_: Exception) {
                    null
                }
            }
            ?: config.nativeFontFamilyName?.let { fontName ->
                try {
                    val fontResId = adView.context.resources.getIdentifier(fontName, "font", adView.context.packageName)
                    if (fontResId != 0) {
                        androidx.core.content.res.ResourcesCompat.getFont(adView.context, fontResId)
                    } else {
                        android.graphics.Typeface.create(fontName, android.graphics.Typeface.NORMAL)
                    }
                } catch (_: Exception) {
                    null
                }
            }

        // 3. Headline Styling
        headlineView?.let {
            it.text = nativeAd.headline
            config.nativeHeadlineColor?.let { color -> it.setTextColor(color) }
            resolvedTypeface?.let { tf -> it.typeface = tf }
            adView.headlineView = it
        }

        // 4. Body Styling
        bodyView?.let {
            if (nativeAd.body.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.text = nativeAd.body
                config.nativeBodyColor?.let { color -> it.setTextColor(color) }
                resolvedTypeface?.let { tf -> it.typeface = tf }
                it.visibility = View.VISIBLE
            }
            adView.bodyView = it
        }

        // 5. "Ad" Badge Attribution Styling
        attributionView?.let {
            config.nativeAdBadgeBgColor?.let { color -> it.setBackgroundColor(color) }
            config.nativeAdBadgeTextColor?.let { color -> it.setTextColor(color) }
            resolvedTypeface?.let { tf -> it.typeface = tf }
        }

        // 6. Call To Action (Install/Open) Button Styling
        callToActionView?.let {
            if (nativeAd.callToAction.isNullOrEmpty()) {
                it.visibility = View.GONE
            } else {
                it.text = nativeAd.callToAction
                config.nativeCtaBgColor?.let { color ->
                    it.backgroundTintList = android.content.res.ColorStateList.valueOf(color)
                }
                config.nativeCtaTextColor?.let { color -> it.setTextColor(color) }
                resolvedTypeface?.let { tf -> it.typeface = tf }
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

        mediaView?.let { mv ->
            mv.visibility = View.VISIBLE
            mv.mediaContent = nativeAd.mediaContent
        }

        adView.registerNativeAd(nativeAd, mediaView)
    }
}
