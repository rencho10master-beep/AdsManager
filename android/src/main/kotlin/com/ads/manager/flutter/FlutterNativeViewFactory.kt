package com.ads.manager.flutter

import android.app.Activity
import android.content.Context
import android.view.View
import com.ads.manager.nativead.LargeNativeAdView
import com.ads.manager.nativead.NativeAdContainerView
import com.ads.manager.nativead.SmallNativeAdView
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class FlutterNativeViewFactory(private val activityProvider: () -> Activity?) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val params = args as? Map<*, *> ?: emptyMap<String, Any>()
        return FlutterNativeView(context, activityProvider(), params)
    }
}

class FlutterNativeView(
    context: Context,
    private val activity: Activity?,
    params: Map<*, *>
) : PlatformView {

    private val containerView: NativeAdContainerView

    init {
        val type = (params["type"] as? Number)?.toInt() ?: 0 // 0 = Large, 1 = Small
        containerView = if (type == 1) {
            SmallNativeAdView(context)
        } else {
            LargeNativeAdView(context)
        }

        activity?.let { act ->
            containerView.load(act)
        }
    }

    override fun getView(): View {
        return containerView
    }

    override fun dispose() {
        containerView.removeAllViews()
    }
}
