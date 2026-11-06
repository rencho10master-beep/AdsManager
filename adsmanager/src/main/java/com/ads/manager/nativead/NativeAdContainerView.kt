package com.ads.manager.nativead

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ads.manager.AdsManager
import com.ads.manager.R

open class NativeAdContainerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    open var nativeType: Int = 0 // 0 = large, 1 = small

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.NativeAdContainerView)
            nativeType = typedArray.getInt(R.styleable.NativeAdContainerView_nativeType, nativeType)
            typedArray.recycle()
        }

        if (isInEditMode) {
            setupEditModePreview()
        }
    }

    protected open fun setupEditModePreview() {
        val layoutRes = if (nativeType == 1) {
            R.layout.layout_small_native_ad
        } else {
            R.layout.layout_large_native_ad
        }
        LayoutInflater.from(context).inflate(layoutRes, this, true)
    }

    /**
     * Load native ad automatically based on XML nativeType.
     */
    fun load(activity: Activity) {
        if (nativeType == 1) {
            AdsManager.showNativeSmall(activity, this)
        } else {
            AdsManager.showNative(activity, this)
        }
    }

    fun loadLarge(activity: Activity) {
        AdsManager.showNative(activity, this)
    }

    fun loadSmall(activity: Activity) {
        AdsManager.showNativeSmall(activity, this)
    }

    fun loadCustom(activity: Activity, customAdLayoutResId: Int) {
        AdsManager.showNative(activity, this, customAdLayoutResId = customAdLayoutResId)
    }
}
