# AdsManager (Android & Flutter Dual Library) 🚀

[![](https://jitpack.io/v/rencho10master-beep/AdsManager.svg)](https://jitpack.io/#rencho10master-beep/AdsManager)
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Flutter](https://img.shields.io/badge/Flutter-3.0%2B-02569B.svg?style=flat&logo=flutter)](https://flutter.dev)
[![CompileSdk](https://img.shields.io/badge/compileSdk-37-blue.svg)](https://developer.android.com)

A single unified AdMob Ads library that works seamlessly for **Native Android (Kotlin/Java & Custom XML Views)** and **Flutter (Dart & Pure Widgets - ZERO XML needed!)**.

---

## 📑 Quick Navigation
- [🐦 Flutter Integration Guide (Dart & Widgets)](#-flutter-integration-guide)
- [🤖 Native Android Integration Guide (Kotlin & XML)](#-native-android-integration-guide)
- [🛡️ Private DNS (AdBlock) Detection](#-private-dns-adblock-detection)
- [💎 Instant Premium Mode](#-instant-premium-mode)

---

# 🐦 Flutter Integration Guide

### 1. Add Dependency in Flutter `pubspec.yaml`
```yaml
dependencies:
  flutter:
    sdk: flutter
  ads_manager:
    git:
      url: https://github.com/rencho10master-beep/AdsManager.git
```

### 2. Configure `android/app/src/main/AndroidManifest.xml`
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

### 3. Initialize in `main.dart`
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

      // Optional: Shimmer colors & Dialog styling
      shimmerBaseColor: "#E0E0E0",
      shimmerHighlightColor: "#F5F5F5",
      dialogLoadingText: "Ad is loading...",
    ),
  );

  runApp(const MyApp());
}
```

### 4. Flutter Widgets (Zero XML! Pure Dart Widgets)

#### 🖼️ Native Ad Widget (Large & Small)
```dart
// 1. Large Native Ad (with MediaView, App Icon, Headline, Body, Call-To-Action)
const NativeAdWidget(type: NativeType.large)

// 2. Small Native Ad (compact view for lists)
const NativeAdWidget(type: NativeType.small)
```

#### 📢 Banner Ad Widget
```dart
// 1. Standard Adaptive Banner
const BannerAdWidget(type: BannerType.adaptive)

// 2. Collapsible Banner (opens expanded and collapses)
const BannerAdWidget(
  type: BannerType.collapsible,
  collapsiblePosition: "bottom", // or "top"
)

// 3. Inline Adaptive Banner (for ListView/RecyclerView)
const BannerAdWidget(
  type: BannerType.inline,
  maxHeightDp: 100,
)

// 4. Medium Rectangle Banner (MREC 300x250)
const BannerAdWidget(type: BannerType.mrec)
```

### 5. Flutter Ad Actions

#### 🎬 Show Interstitial Ad (with Frequency Count)
```dart
AdsManager.showInterstitial(
  count: 1, // 1 = Every click, 2 = Every 2nd click, etc.
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
    print("Ad closed");
  },
);
```

#### 🚀 Splash App Open Ad
```dart
AdsManager.loadAndShowSplashAppOpenAd(
  timeoutMs: 6000,
  onComplete: () {
    // Navigate from Splash to HomeScreen
  },
);
```

#### 🛡️ Private DNS (AdBlocker) Check in Flutter
```dart
bool isDnsOn = await AdsManager.isDNS();
if (isDnsOn) {
  // Private DNS (AdGuard / NextDNS / AdBlock) is ON!
}
```

#### 💎 Set Premium Mode in Flutter
```dart
// Turn ON Premium -> Instantly removes all ads & shimmers
await AdsManager.isPremium(true);

// Check if user is premium
bool isPremium = await AdsManager.isPremium();
```

---

# 🤖 Native Android Integration Guide

### 1. Add JitPack in `settings.gradle`
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### 2. Add Dependency in app `build.gradle`
```groovy
dependencies {
    implementation 'com.github.rencho10master-beep:AdsManager:1.0.0'
}
```

### 3. Initialize in `Application` class
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
            shimmerBaseColor = Color.parseColor("#E0E0E0"),
            shimmerHighlightColor = Color.parseColor("#F5F5F5")
        )

        AdsManager.init(this, config)
    }
}
```

### 4. Dedicated Custom Views in XML (Shows Live Mockups in Android Studio Preview!)

#### 🖼️ Large Native Ad View
```xml
<com.ads.manager.nativead.LargeNativeAdView
    android:id="@+id/largeNativeAdView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```
Load in Kotlin:
```kotlin
binding.largeNativeAdView.load(this)
```

#### 🖼️ Small Native Ad View
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

#### 📢 Dedicated Banner View
```xml
<!-- Adaptive Banner -->
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

### 5. Native Android Ad Actions

#### 🎬 Show Interstitial Ad (Always calls callback on close, fail, or skip)
```kotlin
AdsManager.showInterstitial(this, count = 1) {
    // Next Screen / Action
    startActivity(Intent(this, DetailActivity::class.java))
}
```

#### 🛡️ Private DNS Detection
```kotlin
if (AdsManager.isDNS(this)) {
    // User has AdBlock Private DNS active
}
```

#### 💎 Instant Premium Toggle
```kotlin
// Instantly kills all ads and dismisses dialogs
AdsManager.isPremium(true)

// Check status
val isVip = AdsManager.isPremium()
```

---

## ⚡ License
MIT License. Free to use in commercial and personal projects.
