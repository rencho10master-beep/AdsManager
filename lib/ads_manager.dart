import 'dart:async';
import 'package:flutter/services.dart';
import 'src/ads_config.dart';

export 'src/ads_config.dart';
export 'src/banner_ad_widget.dart';
export 'src/native_ad_widget.dart';

/// AdsManager for Flutter.
/// Provides single-line Interstitial, Rewarded, Splash App Open, Private DNS Detection,
/// and Premium toggling.
class AdsManager {
  static const MethodChannel _channel = MethodChannel('com.ads.manager/methods');

  /// Initialize AdsManager with configuration.
  static Future<bool> init(AdsConfig config, {bool enableResumeAppOpenAd = true}) async {
    try {
      final res = await _channel.invokeMethod<bool>('init', {
        'config': config.toMap(),
        'enableResumeAppOpenAd': enableResumeAppOpenAd,
      });
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Check or set Premium mode.
  /// AdsManager.isPremium(true) -> Instantly kills all ads and shimmers!
  /// AdsManager.isPremium() -> Returns current status.
  static Future<bool> isPremium([bool? premium]) async {
    try {
      if (premium != null) {
        await _channel.invokeMethod('setPremium', {'premium': premium});
        return premium;
      } else {
        final res = await _channel.invokeMethod<bool>('isPremium');
        return res ?? false;
      }
    } catch (_) {
      return false;
    }
  }

  /// Check if Private DNS (AdBlock DNS, AdGuard, NextDNS) is enabled on device.
  /// Returns true if AdBlock DNS is active, false otherwise.
  static Future<bool> isDNS() async {
    try {
      final res = await _channel.invokeMethod<bool>('isDNS');
      return res ?? false;
    } catch (_) {
      return false;
    }
  }

  /// Show Interstitial Ad with frequency count.
  /// [count] = 1: Shows every time.
  /// [count] = 2: Shows every 2nd click.
  /// [onComplete]: Called immediately when ad closes, fails, or is skipped.
  static Future<void> showInterstitial({
    int count = 1,
    required void Function() onComplete,
  }) async {
    try {
      await _channel.invokeMethod('showInterstitial', {'count': count});
    } catch (_) {}
    onComplete();
  }

  /// Preload Interstitial and Rewarded ads.
  static Future<void> preloadAds() async {
    try {
      await _channel.invokeMethod('preloadAds');
    } catch (_) {}
  }

  /// Show Rewarded Ad.
  static Future<void> showRewarded({
    required void Function(num amount, String type) onEarned,
    required void Function() onClosed,
  }) async {
    try {
      final res = await _channel.invokeMapMethod<String, dynamic>('showRewarded');
      if (res != null && res['earned'] == true) {
        final num amount = res['amount'] ?? 1;
        final String type = res['type'] ?? "";
        onEarned(amount, type);
      }
    } catch (_) {}
    onClosed();
  }

  /// Load and Show Splash App Open Ad with timeout.
  static Future<void> loadAndShowSplashAppOpenAd({
    int timeoutMs = 6000,
    required void Function() onComplete,
  }) async {
    try {
      await _channel.invokeMethod('showSplashAppOpenAd', {'timeoutMs': timeoutMs});
    } catch (_) {}
    onComplete();
  }
}
