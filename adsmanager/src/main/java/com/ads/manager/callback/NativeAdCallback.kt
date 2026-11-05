package com.ads.manager.callback

interface NativeAdCallback {
    fun onNativeAdLoaded() {}
    fun onNativeAdFailed() {}
    fun onNativeAdClicked() {}
}
