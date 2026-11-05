package com.ads.manager.sample

import android.app.Application
import android.graphics.Color
import com.ads.manager.AdsConfig
import com.ads.manager.AdsManager

class SampleApp : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = AdsConfig(
            appId = "ca-app-pub-3940256099942544~3347511713",
            interstitialId = "ca-app-pub-3940256099942544/1033173712",
            shimmerBaseColor = Color.parseColor("#E0E0E0"),
            shimmerHighlightColor = Color.parseColor("#F5F5F5"),
            dialogBackgroundColor = Color.BLACK,
            dialogTextColor = Color.WHITE,
            dialogLoadingText = "Ad is loading…"
        )

        AdsManager.init(this, config)
    }
}
