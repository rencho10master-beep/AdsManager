package com.ads.manager.interstitial

class AdClickTracker(private val defaultInterval: Int = 2) {
    private val clickCounters = mutableMapOf<String, Int>()

    @Synchronized
    fun shouldShowAd(interval: Int = defaultInterval, key: String = "interval_$interval"): Boolean {
        if (interval <= 1) {
            // Har vakhte show (every time on every single click)
            return true
        }

        val currentCount = (clickCounters[key] ?: 0) + 1
        clickCounters[key] = currentCount

        return if (currentCount == 1) {
            true // First click par show thase
        } else {
            (currentCount - 1) % interval == 0
        }
    }

    @Synchronized
    fun reset(key: String? = null) {
        if (key != null) {
            clickCounters.remove(key)
        } else {
            clickCounters.clear()
        }
    }

    @Synchronized
    fun getClickCount(key: String = "default"): Int {
        return clickCounters[key] ?: 0
    }
}
