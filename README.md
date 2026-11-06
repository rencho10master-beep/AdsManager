# AdsManager (Android & Flutter Unified Library) 🚀

[![](https://jitpack.io/v/rencho10master-beep/AdsManager.svg)](https://jitpack.io/#rencho10master-beep/AdsManager)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Flutter](https://img.shields.io/badge/Flutter-3.0%2B-02569B.svg?style=flat&logo=flutter)](https://flutter.dev)
[![CompileSdk](https://img.shields.io/badge/compileSdk-35-blue.svg)](https://developer.android.com)

A high-performance AdMob Ads Library designed to work seamlessly across both **Native Android** and **Flutter**.

---

## 📑 Select Your Platform:
- [🤖 SECTION 1: NATIVE ANDROID COMPLETE GUIDE (Kotlin/Java & XML)](#-section-1-native-android-complete-guide)
- [🐦 SECTION 2: FLUTTER COMPLETE GUIDE (Dart & Pure Widgets)](#-section-2-flutter-complete-guide)

---
---

# 🤖 SECTION 1: NATIVE ANDROID COMPLETE GUIDE

Complete guide for Native Android projects using Kotlin/Java and dedicated XML custom views.

> [!NOTE]
> **Broad SDK Compatibility:** Supports **API 24 to API 37** (Android 7.0 Nougat to Android 16). 
> The library defines `aarMetadata.minCompileSdk = 24`, meaning any app compiling against API 24, 28, 30, 32, 33, 34, 35, 36, or 37 can use it without metadata rejection.

---

### 1. Add JitPack in `settings.gradle`
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

### 2. Add Dependency in app `build.gradle`
In your app's `build.gradle` (or `build.gradle.kts`):

```groovy
dependencies {
    implementation 'com.github.rencho10master-beep:AdsManager:1.0.3'
}
```

### 3. Add AdMob App ID in `AndroidManifest.xml`
```xml
<manifest>
    <application>
        <!-- Your AdMob App ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />
    </application>
</manifest>
```

---

### 4. 🇪🇺 Google UMP GDPR Consent (European Union & UK)
Call this in your `SplashActivity` before requesting ads. It automatically presents Google's official consent form for users in EEA & UK, and skips automatically for users in other countries (India, US, etc.):

```kotlin
// In SplashActivity.onCreate()
AdsManager.requestConsent(this) { canRequestAds ->
    // Consent finished or skipped -> safely load & show splash ad!
    AdsManager.loadAndShowSplashAppOpenAd(this, timeoutMs = 6000L) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
```

If you have a "Privacy Settings" button inside your app's Settings screen:
```kotlin
AdsManager.showPrivacyOptions(this) {
    // User updated their consent preferences
}
```

---

### 5. 🛡️ Private DNS (AdBlock) Detection
Detect if the user is using an AdBlock Private DNS (like AdGuard, NextDNS) that blocks all ads:

```kotlin
// Returns true if AdBlock Private DNS is active, false otherwise
if (AdsManager.isDNS(this)) {
    // Show alert / dialog to ask user to turn off Private DNS
}
```

---

### 6. Initialize in `Application` Class
Configure only the Ad IDs you need in your `Application` class. Omitted IDs safely default to blank without crashes:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val config = AdsConfig(
            appId = "ca-app-pub-3940256099942544~3347511713",
            bannerId = "ca-app-pub-3940256099942544/6300978111",
            interstitialId = "ca-app-pub-3940256099942544/1033173712",
            nativeId = "ca-app-pub-3940256099942544/2247696110",
            rewardedId = "ca-app-pub-3940256099942544/5224354917",
            splashAppOpenId = "ca-app-pub-3940256099942544/9257395921",
            resumeAppOpenId = "ca-app-pub-3940256099942544/9257395921",

            // 🎨 Shimmer Color (Single Color - defaults to clean light grey)
            shimmerColor = Color.parseColor("#E0E0E0"),

            // ⏳ Ad Loading Dialog Styling (Optional)
            dialogBackgroundColor = Color.parseColor("#CC000000"),
            dialogTextColor = Color.WHITE,
            dialogLoadingText = "Ad is loading…",

            // 🎨 Native Ad Complete Styling (Optional)
            nativeBackgroundColor = Color.WHITE,                   // Native Ad Background
            nativeHeadlineColor = Color.BLACK,                     // Headline / Title text color
            nativeBodyColor = Color.parseColor("#666666"),         // Body description text color
            nativeAdBadgeBgColor = Color.parseColor("#FFCC00"),    // "Ad" badge background
            nativeAdBadgeTextColor = Color.BLACK,                  // "Ad" badge text color
            nativeCtaBgColor = Color.parseColor("#1A73E8"),        // Install button background color
            nativeCtaTextColor = Color.WHITE,                      // Install button text color

            // 🔤 Custom Font from res/font/ folder (Optional)
            nativeFontResId = R.font.my_font                       // Directly pass your R.font!
        )

        AdsManager.init(this, config)
    }
}
```

---

### 7. Native Ads & Shimmer Layouts

You can display Native Ads either via **Kotlin Functions** (with dedicated Shimmer) or via **XML Custom Views**:

#### Method 1: Using Kotlin Functions (Voice-style Shimmer Included)
```kotlin
// 🔹 Large Native Ad (Voice-style Large Shimmer + Full MediaView Ad)
AdsManager.showLargeNative(this, binding.largeNativeContainer)

// 🔹 Small Native Ad (Voice-style Small Compact Shimmer + Compact Card Ad)
AdsManager.showSmallNative(this, binding.smallNativeContainer)

// 🔹 Generic Native with Custom Layout (Optional)
AdsManager.showNative(
    activity = this,
    container = binding.customNativeContainer,
    customShimmerResId = R.layout.layout_large_native_shimmer,
    customAdLayoutResId = R.layout.layout_large_native_ad
)
```

> [!TIP]
> **Dynamic Shimmer Colors:** Both `SmallNative` and `LargeNative` shimmers automatically inherit the `shimmerBaseColor` and `shimmerHighlightColor` defined in your `AdsConfig`. If `isPremium = true`, the shimmer is instantly hidden and container visibility set to `GONE`.

---

### 8. XML Custom Views (With Live Mockup in Android Studio Preview!)

#### A. Large Native Ad View (Full MediaView + Icon + Headline + Body + Install button)
In your XML layout:
```xml
<com.ads.manager.nativead.LargeNativeAdView
    android:id="@+id/largeNativeAdView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
Load in Kotlin (`Activity` or `Fragment`):
```kotlin
binding.largeNativeAdView.load(this)
```

#### B. Small Native Ad View (Compact layout ideal for lists & bottom cards)
In your XML layout:
```xml
<com.ads.manager.nativead.SmallNativeAdView
    android:id="@+id/smallNativeAdView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
Load in Kotlin:
```kotlin
binding.smallNativeAdView.load(this)
```

#### C. Dedicated Banner View (Adaptive & Collapsible)
In your XML layout:
```xml
<!-- Standard Adaptive Banner -->
<com.ads.manager.banner.BannerAdView
    android:id="@+id/bannerAdView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:bannerType="adaptive" />

<!-- Collapsible Banner -->
<com.ads.manager.banner.BannerAdView
    android:id="@+id/collapsibleBannerView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:bannerType="collapsible" />
```
Load in Kotlin:
```kotlin
binding.bannerAdView.load(this)
```

---

### 8. Android Ad Actions in Kotlin

#### 🎬 Show Interstitial Ad (Always calls callback on close, skip, or fail)
```kotlin
// count = 1: Shows every click
// count = 2: Shows every 2nd click
AdsManager.showInterstitial(this, count = 1) {
    // Action / Next Screen
    startActivity(Intent(this, NextActivity::class.java))
}
```

#### 📢 Load Banner Programmatically (Optional without XML custom view)
```kotlin
// 1. Adaptive Banner
AdsManager.showBanner(this, binding.bannerContainer)

// 2. Collapsible Banner (bottom or top)
AdsManager.showCollapsibleBanner(this, binding.bannerContainer, position = "bottom")

// 3. Inline Banner (for RecyclerView)
AdsManager.showInlineBanner(this, binding.bannerContainer, maxHeightDp = 100)

// 4. Medium Rectangle (MREC 300x250)
AdsManager.showMediumRectangleBanner(this, binding.bannerContainer)
```

#### 🎁 Show Rewarded Ad
```kotlin
AdsManager.showRewarded(
    this,
    onEarned = { reward ->
        Toast.makeText(this, "Reward earned: ${reward.amount}", Toast.LENGTH_SHORT).show()
    },
    onClosed = {
        // Continue game / next action
    }
)
```

#### 🚀 Splash App Open Ad
```kotlin
AdsManager.loadAndShowSplashAppOpenAd(this, timeoutMs = 6000L) {
    // Navigate from Splash to MainActivity
    startActivity(Intent(this, MainActivity::class.java))
    finish()
}
```

#### 💎 Instant Premium Mode (Turns OFF all ads & shimmers)
```kotlin
// Enable Premium
AdsManager.isPremium(true)

// Check status
if (AdsManager.isPremium()) {
    // User is Premium
}
```

---
---

# 🐦 SECTION 2: FLUTTER COMPLETE GUIDE

Complete guide for Flutter projects. **ZERO XML REQUIRED!** Pure Dart code and native Flutter widgets.

---

### 1. Add Dependency in Flutter `pubspec.yaml`
```yaml
dependencies:
  flutter:
    sdk: flutter
  ads_manager:
    git:
      url: https://github.com/rencho10master-beep/AdsManager.git
```

### 2. Add AdMob App ID in `android/app/src/main/AndroidManifest.xml`
```xml
<manifest>
    <application>
        <!-- Your AdMob App ID -->
        <meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-3940256099942544~3347511713" />
    </application>
</manifest>
```

---

### 3. 🇪🇺 Google UMP GDPR Consent in Flutter
```dart
// Check & show Google Consent dialog on app launch:
bool canRequestAds = await AdsManager.requestConsent();

// If you have a Privacy Settings button in settings screen:
await AdsManager.showPrivacyOptions();
```

---

### 4. 🛡️ Private DNS (AdBlocker) Check in Flutter
```dart
// Returns true if AdBlock DNS is active, false otherwise
bool isDnsOn = await AdsManager.isDNS();
if (isDnsOn) {
  // Alert user to turn off AdBlock DNS
}
```

---

### 5. Initialize in `main.dart`
```dart
import 'package:flutter/material.dart';
import 'package:ads_manager/ads_manager.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize AdsManager
  await AdsManager.init(
    const AdsConfig(
      appId: "ca-app-pub-3940256099942544~3347511713",
      bannerId: "ca-app-pub-3940256099942544/6300978111",
      interstitialId: "ca-app-pub-3940256099942544/1033173712",
      nativeId: "ca-app-pub-3940256099942544/2247696110",
      rewardedId: "ca-app-pub-3940256099942544/5224354917",
      splashAppOpenId: "ca-app-pub-3940256099942544/9257395921",
      resumeAppOpenId: "ca-app-pub-3940256099942544/9257395921",

      // 🎨 Custom Shimmer Color (Optional - defaults to "#E0E0E0")
      shimmerColor: "#E0E0E0",

      // ⏳ Custom Loading Dialog (Optional)
      dialogBackgroundColor: "#CC000000",
      dialogTextColor: "#FFFFFF",
      dialogLoadingText: "Ad is loading...",

      // 🎨 Native Ad Complete Styling (Optional)
      nativeBackgroundColor: "#FFFFFF",     // Native card background
      nativeHeadlineColor: "#000000",        // Title text color
      nativeBodyColor: "#666666",            // Description text color
      nativeAdBadgeBgColor: "#FFCC00",       // "Ad" badge background
      nativeAdBadgeTextColor: "#000000",     // "Ad" badge text color
      nativeCtaBgColor: "#1A73E8",           // Install button background color
      nativeCtaTextColor: "#FFFFFF",         // Install button text color
      nativeFontFamilyName: "my_font",       // Font from res/font/ or system
    ),
  );

  runApp(const MyApp());
}
```

---

### 6. Flutter Widgets (Pure Dart Widgets - ZERO XML!)

#### 🖼️ Native Ad Widget (With Custom Shimmer & Auto Ad Binding)
```dart
// 1. Large Native Ad (Full MediaView + Icon + Headline + Body + Install button)
const NativeAdWidget(type: NativeType.large)

// 2. Small Native Ad (Compact card for ListView items)
const NativeAdWidget(type: NativeType.small)
```

#### 📢 Banner Ad Widget
```dart
// 1. Standard Adaptive Banner
const BannerAdWidget(type: BannerType.adaptive)

// 2. Collapsible Banner (opens expanded initially and collapses)
const BannerAdWidget(
  type: BannerType.collapsible,
  collapsiblePosition: "bottom", // or "top"
)

// 3. Inline Adaptive Banner (for ListView/ScrollView)
const BannerAdWidget(
  type: BannerType.inline,
  maxHeightDp: 100,
)

// 4. Medium Rectangle Banner (MREC 300x250)
const BannerAdWidget(type: BannerType.mrec)
```

---

### 7. Flutter Ad Actions in Dart

#### 🎬 Show Interstitial Ad (Always calls callback on close, skip, or fail)
```dart
AdsManager.showInterstitial(
  count: 1, // 1 = Every single click, 2 = Every 2nd click
  onComplete: () {
    // Action / Navigate to next screen
    Navigator.push(context, MaterialPageRoute(builder: (_) => const NextScreen()));
  },
);
```

#### 🎁 Show Rewarded Ad
```dart
AdsManager.showRewarded(
  onEarned: (amount, type) {
    print("User earned reward: $amount $type");
  },
  onClosed: () {
    // Ad closed
  },
);
```

#### 🚀 Splash App Open Ad
```dart
AdsManager.loadAndShowSplashAppOpenAd(
  timeoutMs: 6000,
  onComplete: () {
    // Navigate from Splash to HomeScreen
    Navigator.pushReplacement(context, MaterialPageRoute(builder: (_) => const HomeScreen()));
  },
);
```

#### 💎 Set Premium Mode in Flutter
```dart
// Instantly kills all ads and shimmers
await AdsManager.isPremium(true);

// Check premium status
bool isVip = await AdsManager.isPremium();
```

---

## ⚡ License
MIT License. Free to use in commercial and personal projects.
