# AdsManager Android Library

[![](https://jitpack.io/v/rencho10master-beep/AdsManager.svg)](https://jitpack.io/#rencho10master-beep/AdsManager)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![CompileSdk](https://img.shields.io/badge/compileSdk-37-blue.svg)](https://developer.android.com)

A clean, standalone, zero-bloat Android Ads Library powered by **Google Mobile Ads (Next-Gen SDK v1.6.0)** with **compileSdk = 37**.

---

## 📦 1. Installation (Add Dependency)

### Step 1: Add JitPack repository
In your root `settings.gradle` (or `settings.gradle.kts`):

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' } // <-- Add this line
    }
}
```

### Step 2: Add the dependency
In your app's `build.gradle` (or `build.gradle.kts`):

```groovy
dependencies {
    implementation 'com.github.rencho10master-beep:AdsManager:1.0.0'
}
```

---

## ⚙️ 2. AndroidManifest.xml Setup

Add your AdMob App ID inside the `<application>` tag:

```xml
<manifest>
    <application>
        <!-- AdMob App ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy" />
    </application>
</manifest>
```

---

## 🚀 3. Initialize in Application Class

In your `Application` class, configure only the Ad IDs your app needs (all other IDs are optional and will safely never crash):

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Example 1: Full Configuration with Custom Colors & Dialog Styling
        val config = AdsConfig(
            appId = "ca-app-pub-3940256099942544~3347511713",
            bannerId = "ca-app-pub-3940256099942544/6300978111",
            interstitialId = "ca-app-pub-3940256099942544/1033173712",
            nativeId = "ca-app-pub-3940256099942544/2247696110",
            rewardedId = "ca-app-pub-3940256099942544/5224354917",
            splashAppOpenId = "ca-app-pub-3940256099942544/9257395921",
            resumeAppOpenId = "ca-app-pub-3940256099942544/9257395921",

            // 🎨 Custom Shimmer Colors
            shimmerBaseColor = Color.parseColor("#E0E0E0"),
            shimmerHighlightColor = Color.parseColor("#F5F5F5"),

            // ⏳ Custom Ad Loading Dialog (shown before Interstitial & Resume App Open)
            dialogBackgroundColor = Color.parseColor("#CC000000"), // Background overlay color
            dialogTextColor = Color.WHITE,                         // Loading text color
            dialogProgressColor = Color.WHITE,                     // Spinner/Progress color
            dialogLoadingText = "Ad is loading…",                  // Message
            showDialogProgressBar = true                           // Enable/disable spinner
        )

        // Example 2: If your app ONLY uses Interstitial Ad (zero crash guarantee for unadded IDs)
        /*
        val config = AdsConfig(
            appId = "ca-app-pub-3940256099942544~3347511713",
            interstitialId = "ca-app-pub-3940256099942544/1033173712"
        )
        */

        // enableResumeAppOpenAd = true automatically handles background-to-foreground ads
        AdsManager.init(this, config, enableResumeAppOpenAd = true)
    }
}
```

---

## 🛡️ 4. Private DNS Detection (`isDNS`)

Detect if the user has Private DNS (AdBlock DNS, AdGuard, NextDNS) turned ON on their device:

```kotlin
// Check anywhere using Context:
if (AdsManager.isDNS(this)) {
    // Private DNS is ON (Blocking Ads)
    // Show dialog to ask user to turn off Private DNS or buy Premium!
} else {
    // Private DNS is OFF (Normal flow)
}

// Or using property (after AdsManager.init):
if (AdsManager.isDNS) {
    // DNS is ON
}
```

---

## ⏱️ 5. Splash Screen App Open Ad

Loads and shows App Open Ad with timeout safety. If ad fails or takes too long, the `{ ... }` callback fires automatically so splash screen never freezes:

```kotlin
class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // 5-second timeout safety
        AdsManager.loadAndShowSplashAppOpenAd(this, timeoutMs = 5000L) {
            // Callback fires after ad closes, fails, or if user is Premium:
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
```

---

## 🎯 6. Interstitial Ads (with Click Count)

Shows Interstitial Ad with frequency count. The callback `{ ... }` **always executes** when ad closes, fails to load, or if user is premium:

```kotlin
// Har vakhte show karva mate (Count = 1):
AdsManager.showInterstitial(this, 1) {
    // Navigate or perform action
    startActivity(Intent(this, DetailActivity::class.java))
}

// Dar 2ji var show karva mate (Count = 2):
AdsManager.showInterstitial(this, 2) {
    startActivity(Intent(this, DetailActivity::class.java))
}

// Dar 3ji var show karva mate (Count = 3):
AdsManager.showInterstitial(this, 3) {
    startActivity(Intent(this, DetailActivity::class.java))
}
```

---

## 🖼️ 7. Native Ads (Medium, Small & Custom XML Layouts)

### A. Medium Native Ad (with MediaView & Shimmer)
```kotlin
AdsManager.showNative(this, binding.nativeAdContainer)
```

### B. Small Native Ad (Compact - no MediaView, great for lists / bottom)
```kotlin
AdsManager.showNativeSmall(this, binding.smallNativeContainer)
```

### C. Your Own Custom Designed XML Layout!
Create your own XML layout (e.g. `res/layout/my_custom_native_ad.xml`) with any CardView, background, or colors, and pass it:
```kotlin
AdsManager.showNative(
    activity = this,
    container = binding.myContainer, // Any ViewGroup (CardView, LinearLayout, FrameLayout, etc.)
    customAdLayoutResId = R.layout.my_custom_native_ad
)
```

---

## 📱 8. Adaptive Banner Ads

```kotlin
AdsManager.showBanner(this, binding.bannerContainer)
```

---

## 🎁 9. Rewarded Ads

```kotlin
AdsManager.showRewarded(
    activity = this,
    onEarned = { reward ->
        Toast.makeText(this, "Reward: ${reward.amount} ${reward.type}", Toast.LENGTH_SHORT).show()
    },
    onClosed = {
        // User closed ad
    }
)
```

---

## 👑 10. Premium / VIP User Mode (Disable All Ads)

When user purchases subscription in PremiumActivity:

```kotlin
// Option 1: Function (Set Premium status)
AdsManager.isPremium(true)  // Instantly disables and removes all ads & shimmers!
AdsManager.isPremium(false) // Normal free mode

// Check Premium status anywhere:
if (AdsManager.isPremium()) {
    // User is Premium
}

// Option 2: Disable Ads function
AdsManager.setAdsEnabled(false) // Turns off all ads
```

// Option 3: Disable Ads function
AdsManager.setAdsEnabled(false)
```

### What happens when Premium is enabled:
- Instantly dismisses any open loading dialogs.
- Kills all pending ad requests.
- Native & Banner containers immediately become `GONE` (no blank spaces or shimmer).
- Interstitial & Splash callbacks fire immediately without any delay.
