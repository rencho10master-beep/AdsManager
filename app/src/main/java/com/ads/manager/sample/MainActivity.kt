package com.ads.manager.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ads.manager.AdsManager
import com.ads.manager.sample.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdsManager.preloadAds(this)
        AdsManager.showBanner(this, binding.bannerContainer)

        // Voice style Small Native & Large Native
        AdsManager.showSmallNative(this, binding.smallNativeContainer)
        AdsManager.showLargeNative(this, binding.largeNativeContainer)

        binding.btnSplashAppOpen.setOnClickListener {
            AdsManager.loadAndShowSplashAppOpenAd(this, timeoutMs = 5000L) {
                Toast.makeText(this, "Splash App Open Ad Finished / Dismissed", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnInterstitial.setOnClickListener {
            AdsManager.showInterstitial(this) {
                Toast.makeText(this, "Action performed after Interstitial", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRewarded.setOnClickListener {
            AdsManager.showRewarded(
                activity = this,
                onEarned = { reward ->
                    Toast.makeText(this, "Reward earned: ${reward.amount} ${reward.type}", Toast.LENGTH_SHORT).show()
                },
                onClosed = {
                    Toast.makeText(this, "Rewarded Ad closed", Toast.LENGTH_SHORT).show()
                }
            )
        }

        binding.btnTogglePremium.setOnClickListener {
            AdsManager.isPremium = !AdsManager.isPremium
            val status = if (AdsManager.isPremium) "Premium (No Ads/Shimmer)" else "Free"
            binding.btnTogglePremium.text = "Toggle Premium (Current: $status)"
            Toast.makeText(this, "Status: $status", Toast.LENGTH_SHORT).show()

            if (AdsManager.isPremium) {
                AdsManager.showBanner(this, binding.bannerContainer)
                AdsManager.showSmallNative(this, binding.smallNativeContainer)
                AdsManager.showLargeNative(this, binding.largeNativeContainer)
            }
        }
    }
}
