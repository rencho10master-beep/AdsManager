package com.ads.manager.nativead

import android.content.Context
import android.util.AttributeSet

class SmallNativeAdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : NativeAdContainerView(context, attrs, defStyleAttr) {
    override var nativeType: Int = 1

    init {
        if (isInEditMode) {
            removeAllViews()
            setupEditModePreview()
        }
    }
}
