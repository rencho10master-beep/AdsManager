package com.ads.manager.rewarded

import android.app.Activity
import com.ads.manager.AdsConfig
import com.ads.manager.dialog.AdLoadingDialog
import com.ads.manager.init.AdsInitializer
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback

class RewardedAdManager(private val config: AdsConfig) {
    private var rewardedAd: RewardedAd? = null
    private var isAdLoading = false

    fun loadAd(activity: Activity, onLoaded: (() -> Unit)? = null) {
        AdsInitializer.safeInitialize(activity, config.appId)

        if (!MobileAds.isInitialized || isAdLoading || rewardedAd != null) return

        isAdLoading = true
        val adRequest = AdRequest.Builder(config.rewardedId).build()
        RewardedAd.load(
            adRequest,
            object : AdLoadCallback<RewardedAd> {
                override fun onAdLoaded(ad: RewardedAd) {
                    activity.runOnUiThread {
                        rewardedAd = ad
                        isAdLoading = false
                        onLoaded?.invoke()
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    activity.runOnUiThread {
                        rewardedAd = null
                        isAdLoading = false
                    }
                }
            }
        )
    }

    fun showAd(
        activity: Activity,
        onUserEarnedReward: (AdReward) -> Unit,
        onClosed: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            loadAd(activity)
            onClosed()
            return
        }

        AdLoadingDialog.show(activity, config)

        var earnedReward: AdReward? = null

        ad.adEventCallback = object : RewardedAdEventCallback {
            override fun onAdDismissedFullScreenContent() {
                activity.runOnUiThread {
                    AdLoadingDialog.dismiss()
                    rewardedAd = null
                    loadAd(activity)
                    earnedReward?.let { onUserEarnedReward(it) }
                    onClosed()
                }
            }

            override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
                activity.runOnUiThread {
                    AdLoadingDialog.dismiss()
                    rewardedAd = null
                    loadAd(activity)
                    onClosed()
                }
            }

            override fun onAdShowedFullScreenContent() {
                activity.runOnUiThread {
                    AdLoadingDialog.dismiss()
                }
            }
        }

        ad.show(
            activity,
            object : OnUserEarnedRewardListener {
                override fun onUserEarnedReward(reward: RewardItem) {
                    earnedReward = AdReward(reward.amount, reward.type)
                }
            }
        )
    }

    fun clearAd() {
        rewardedAd = null
        isAdLoading = false
    }
}
