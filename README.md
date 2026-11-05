# AdsManager Android Library

A clean, standalone, zero-bloat Android Ads Library powered by **Google Mobile Ads (Next-Gen SDK v1.6.0)** with **compileSdk = 37**.

### Features:
- **Zero Warnings & Zero Deprecation**: Completely clean compile output on Android API 37.
- **Two Distinct App Open Ads**:
  1. **Splash App Open Ad**: With `onComplete` callback and configurable timeout (prevents stuck splash screens).
  2. **Resume App Open Ad**: Displays full-screen loading dialog when returning from background / recent apps, then shows the App Open Ad.
- **Strict `isPremium` / Functions**:
  - `AdsManager.setPremium(true)` or `AdsManager.setAdsEnabled(false)`.
  - Instantly kills all ad requests and dismisses any loading dialogs.
  - **No Shimmer**: Native / Banner containers are immediately made `GONE` and views removed so user never sees empty gray boxes or loading animations.
  - Interstitial & Splash callbacks fire immediately without delay.
- **Flexible Interstitial Ad Frequency**:
  - Show every time (`interval = 1`).
  - Show every 2 clicks (`interval = 2`).
  - Show every 3 clicks (`interval = 3`).
  - Custom `key` per screen/button so click counters never conflict!
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
            interstitialInterval = 2
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

### 3. Interstitial Ads (Custom Click Count per Screen/Button)
```kotlin
// Case A: Show EVERY time (Every single click)
AdsManager.showInterstitial(this, interval = 1) {
    // Navigate or perform action
}

// Case B: Show every 2nd time on Screen B
AdsManager.showInterstitial(this, interval = 2, key = "screen_b") {
    // Navigate or perform action
}

// Case C: Show every 3rd time on Screen C
AdsManager.showInterstitial(this, interval = 3, key = "screen_c") {
    // Navigate or perform action
}
```

---

### 4. Native Ads (Default, Small, or Custom XML Layout)
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

// 4. Directly Bind to a NativeAdView in your layout
AdsManager.bindNative(this, binding.myNativeAdView)
```

---

### 5. Premium / VIP Mode Functions
When user buys premium inside PremiumActivity:
```kotlin
// Option 1: Function call
AdsManager.setPremium(true)

// Option 2: Turn ads off function
AdsManager.setAdsEnabled(false)

// Option 3: Property assignment
AdsManager.isPremium = true
```
All ads, loading dialogs, and shimmers are instantly dismissed and hidden!
