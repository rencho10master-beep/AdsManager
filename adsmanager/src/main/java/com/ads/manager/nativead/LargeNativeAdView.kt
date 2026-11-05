package com.ads.manager.nativead

import android.content.Context
import android.util.AttributeSet

class LargeNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NativeAdContainerView(context, attrs, defStyleAttr) {
    init {
        nativeType = 0
    }
}
