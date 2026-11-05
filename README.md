# AdsManager Android Library

A clean, standalone, zero-bloat Android Ads Library powered by **Google Mobile Ads (Next-Gen SDK v1.6.0)** with **compileSdk = 37**.

### Features:
- **Zero Warnings & Zero Deprecation**: Completely clean compile output on Android API 37.
- **Two Distinct App Open Ads**:
  1. **Splash App Open Ad**: With `onComplete` callback and configurable timeout (prevents stuck splash screens).
  2. **Resume App Open Ad**: Displays full-screen loading dialog when returning from background / recent apps, then shows the App Open Ad.
- **Strict `isPremium` / `isSubscribed` Mode**:
  - Instantly kills all ad requests.
  - **No Shimmer**: Native / Banner containers are immediately made `GONE` and views removed so user never sees empty gray boxes or loading animations.
  - Interstitial & Splash callbacks fire immediately without delay.
- **Custom Canvas Shimmer**: Zero external libraries (no Facebook Shimmer).
- **Click Frequency Tracker**: Interstitial shows on 1st click, then follows interval (e.g. every 2nd or 3rd click).
- **Notch / Cutout Friendly Loading Dialog**: Translucent fullscreen overlay before interstitial / app open ads.

---

## 🚀 Usage Guide

### 1. Initialize in Application Class
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = AdsConfig(
            appId = "ca-app-pub-xxxxxxxx~yyyyyyyy",
            bannerId = "ca-app-pub-xxxxxxxx/yyyyyyyy",
            interstitialId = "ca-app-pub-xxxxxxxx/yyyyyyyy",
            nativeId = "ca-app-pub-xxxxxxxx/yyyyyyyy",
            rewardedId = "ca-app-pub-xxxxxxxx/yyyyyyyy",
            splashAppOpenId = "ca-app-pub-xxxxxxxx/yyyyyyyy",
            resumeAppOpenId = "ca-app-pub-xxxxxxxx/yyyyyyyy",
            interstitialInterval = 2
        )

        // enableResumeAppOpenAd = true automatically handles background-to-foreground ads
        AdsManager.init(this, config, enableResumeAppOpenAd = true)
    }
}
```

---

### 2. Splash Screen Implementation (with Callback)
```kotlin
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Automatically preloads & shows splash ad with 5s timeout safety
        AdsManager.loadAndShowSplashAppOpenAd(this, timeoutMs = 5000L) {
            // Callback fires after ad closes, fails, or if isPremium == true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
```

---

### 3. Native & Banner Ads
```kotlin
// Native Ad (Shows Shimmer while loading; Hides completely if isPremium = true)
AdsManager.showNative(this, binding.nativeAdContainer)

// Banner Ad (Adaptive size)
AdsManager.showBanner(this, binding.bannerContainer)
```

---

### 4. Interstitial & Rewarded Ads
```kotlin
// Interstitial (Frequency checked + Loading Dialog shown)
AdsManager.showInterstitial(this) {
    // Navigate or perform action
}

// Rewarded Ad
AdsManager.showRewarded(
    activity = this,
    onEarned = { reward ->
        // reward.amount, reward.type
    },
    onClosed = {
        // user closed ad
    }
)
```

---

### 5. Premium / VIP Mode
```kotlin
AdsManager.isPremium = true // Or AdsManager.isSubscribed = true
// Shimmer and ads will never be shown anywhere in the app!
```
