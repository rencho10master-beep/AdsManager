package com.ads.manager.interstitial

class AdClickTracker(private val interval: Int = 2) {
    private var clickCount = 0

    fun shouldShowAd(): Boolean {
        clickCount++
        return if (clickCount == 1) {
            true
        } else {
            (clickCount - 1) % interval == 0
        }
    }

    fun reset() {
        clickCount = 0
    }
}
