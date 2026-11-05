package com.ads.manager.interstitial

class AdClickTracker(private val defaultInterval: Int = 1) {
    private val countMap = mutableMapOf<Int, Int>()

    @Synchronized
    fun shouldShowAd(count: Int = defaultInterval): Boolean {
        if (count <= 1) {
            // Har vakhte show (Every single click)
            return true
        }

        val current = (countMap[count] ?: 0) + 1
        countMap[count] = current

        return if (current == 1) {
            true // First time show
        } else {
            (current - 1) % count == 0
        }
    }

    @Synchronized
    fun reset() {
        countMap.clear()
    }
}
