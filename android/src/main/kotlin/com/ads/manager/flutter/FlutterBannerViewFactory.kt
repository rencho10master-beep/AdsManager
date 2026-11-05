package com.ads.manager.flutter

import android.app.Activity
import android.content.Context
import android.view.View
import com.ads.manager.banner.BannerAdView
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FlutterBannerViewFactory(private val activityProvider: () -> Activity?) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val params = args as? Map<*, *> ?: emptyMap<String, Any>()
        return FlutterBannerView(context, activityProvider(), params)
    }
}

class FlutterBannerView(
    context: Context,
    private val activity: Activity?,
    params: Map<*, *>
) : PlatformView {

    private val bannerAdView: BannerAdView = BannerAdView(context)

    init {
        val type = (params["type"] as? Number)?.toInt() ?: 0
        val collapsiblePos = (params["collapsiblePosition"] as? String) ?: "bottom"
        val maxHeightDp = (params["maxHeightDp"] as? Number)?.toInt() ?: 100

        activity?.let { act ->
            when (type) {
                1 -> bannerAdView.loadCollapsible(act, collapsiblePos)
                2 -> bannerAdView.loadInline(act, maxHeightDp)
                3 -> bannerAdView.load(act) // MREC or handled via bannerType
                else -> bannerAdView.load(act)
            }
        }
    }

    override fun getView(): View {
        return bannerAdView
    }

    override fun dispose() {
        bannerAdView.removeAllViews()
    }
}
