package com.ads.manager.callback

interface InterstitialAdCallback {
    fun onInterstitialAdShown() {}
    fun onInterstitialAdDismissed() {}
    fun onInterstitialAdFailed() {}
    fun onInterstitialAdClicked() {}
}
