package com.ads.manager.callback

interface BannerAdCallback {
    fun onBannerAdLoaded() {}
    fun onBannerAdFailed() {}
    fun onBannerAdClicked() {}
}
