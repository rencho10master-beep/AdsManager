package com.ads.manager.init

import android.content.Context
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig

object AdsInitializer {
    fun safeInitialize(context: Context, appId: String, onComplete: (() -> Unit)? = null) {
        if (!MobileAds.isInitialized) {
            try {
                MobileAds.initialize(context, InitializationConfig.Builder(appId).build())
            } catch (_: Exception) {}
        }
        onComplete?.invoke()
    }
}
