# AdsManager Android Library

A clean, standalone, zero-bloat Android Ads Library powered by **Google Mobile Ads (Next-Gen SDK v1.6.0)** with **compileSdk = 37**.

### Features:
- **Zero Warnings & Zero Deprecation**: Completely clean compile output on Android API 37.
- **Two Distinct App Open Ads**:
  1. **Splash App Open Ad**: With `onComplete` callback and configurable timeout (prevents stuck splash screens).
  2. **Resume App Open Ad**: Displays full-screen loading dialog when returning from background / recent apps, then shows the App Open Ad.
- **Strict `isPremium` Mode**:
  - `AdsManager.isPremium = true` or `AdsManager.updatePremiumStatus(true)` or `AdsManager.setAdsEnabled(false)`.
  - Instantly kills all ad requests and dismisses any loading dialogs.
  - **No Shimmer**: Native / Banner containers are immediately made `GONE` and views removed so user never sees empty gray boxes or loading animations.
  - Interstitial & Splash callbacks fire immediately without delay.
- **Flexible Interstitial Ad Frequency**:
  - `AdsManager.showInterstitial(this, 1) { ... }` -> Show every single click.
  - `AdsManager.showInterstitial(this, 2) { ... }` -> Show on 1st click, then every 2nd click.
  - `AdsManager.showInterstitial(this, 3) { ... }` -> Show on 1st click, then every 3rd click.
  - When ad fails, closes, or skips, `{ ... }` callback fires immediately!
- **Custom XML Layouts for Native Ads**:
  - Works with any `ViewGroup` (`FrameLayout`, `LinearLayout`, `CardView`, etc.).
  - Supports custom designed XML layouts (`customAdLayoutResId = R.layout.my_layout`).
  - Supports direct binding to `NativeAdView` in XML (`bindNative(activity, view)`).
  - Built-in Medium and Small Native ad layouts with Android Studio preview (`tools:` attributes).
- **Custom Canvas Shimmer**: Zero external libraries (no Facebook Shimmer).
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
            interstitialInterval = 1
        )

        // enableResumeAppOpenAd = true automatically handles background-to-foreground ads
        AdsManager.init(this, config, enableResumeAppOpenAd = true)
    }
}
```

---

### 2. Splash Screen Implementation (with Callback & Timeout)
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

### 3. Interstitial Ads (Simple Count & Instant Callback)
```kotlin
// Har vakhte show karva mate (Count 1):
AdsManager.showInterstitial(this, 1) {
    // Ad close thay ya fail thay atyare aa run thase:
    startActivity(Intent(this, DetailActivity::class.java))
}

// Dar 2ji var show karva mate (Count 2):
AdsManager.showInterstitial(this, 2) {
    startActivity(Intent(this, DetailActivity::class.java))
}

// Dar 3ji var show karva mate (Count 3):
AdsManager.showInterstitial(this, 3) {
    startActivity(Intent(this, DetailActivity::class.java))
}
```

---

### 4. Native & Banner Ads (Simple - No Callbacks Needed)
```kotlin
// 1. Medium Native Ad (with MediaView & Shimmer)
AdsManager.showNative(this, binding.nativeAdContainer)

// 2. Small Native Ad (Compact - no MediaView, great for bottom/lists)
AdsManager.showNativeSmall(this, binding.smallNativeContainer)

// 3. Your Own Custom XML Layout!
AdsManager.showNative(
    activity = this,
    container = binding.customAdContainer, // Any ViewGroup (CardView, LinearLayout, etc.)
    customAdLayoutResId = R.layout.my_custom_native_ad
)

// 4. Banner Ad (Adaptive size)
AdsManager.showBanner(this, binding.bannerContainer)
```

---

### 5. Premium / VIP Mode Functions
When user buys premium inside PremiumActivity:
```kotlin
AdsManager.isPremium = true
// or
AdsManager.updatePremiumStatus(true)
// or
AdsManager.setAdsEnabled(false)
```
All ads, loading dialogs, and shimmers are instantly dismissed and hidden!
