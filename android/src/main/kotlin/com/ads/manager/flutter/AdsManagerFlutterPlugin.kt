package com.ads.manager.flutter

import android.app.Activity
import android.graphics.Color
import com.ads.manager.AdsConfig
import com.ads.manager.AdsManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

class AdsManagerFlutterPlugin : FlutterPlugin, MethodCallHandler, ActivityAware {

    private lateinit var channel: MethodChannel
    private var activity: Activity? = null

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, "com.ads.manager/methods")
        channel.setMethodCallHandler(this)

        binding.platformViewRegistry.registerViewFactory(
            "com.ads.manager/banner",
            FlutterBannerViewFactory { activity }
        )

        binding.platformViewRegistry.registerViewFactory(
            "com.ads.manager/native",
            FlutterNativeViewFactory { activity }
        )
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "init" -> {
                val act = activity
                if (act == null) {
                    result.error("NO_ACTIVITY", "Cannot initialize without active activity", null)
                    return
                }

                val configMap = call.argument<Map<String, Any>>("config") ?: emptyMap()
                val enableResumeAppOpenAd = call.argument<Boolean>("enableResumeAppOpenAd") ?: true

                val config = AdsConfig(
                    appId = (configMap["appId"] as? String) ?: "",
                    bannerId = (configMap["bannerId"] as? String) ?: "",
                    interstitialId = (configMap["interstitialId"] as? String) ?: "",
                    nativeId = (configMap["nativeId"] as? String) ?: "",
                    rewardedId = (configMap["rewardedId"] as? String) ?: "",
                    splashAppOpenId = (configMap["splashAppOpenId"] as? String) ?: "",
                    resumeAppOpenId = (configMap["resumeAppOpenId"] as? String) ?: "",
                    shimmerColor = parseColor(
                        (configMap["shimmerColor"] as? String) ?: (configMap["shimmerBaseColor"] as? String),
                        "#E0E0E0"
                    ),
                    dialogBackgroundColor = parseColor(configMap["dialogBackgroundColor"] as? String, "#B3000000"),
                    dialogProgressColor = parseColor(configMap["dialogProgressColor"] as? String, "#FFFFFF"),
                    dialogTextColor = parseColor(configMap["dialogTextColor"] as? String, "#FFFFFF"),
                    dialogLoadingText = (configMap["dialogLoadingText"] as? String) ?: "Loading ad...",
                    showDialogProgressBar = (configMap["showDialogProgressBar"] as? Boolean) ?: true,

                    // Native Ad Styling Customization
                    nativeBackgroundColor = parseOptionalColor(configMap["nativeBackgroundColor"] as? String),
                    nativeHeadlineColor = parseOptionalColor(configMap["nativeHeadlineColor"] as? String),
                    nativeBodyColor = parseOptionalColor(configMap["nativeBodyColor"] as? String),
                    nativeAdBadgeBgColor = parseOptionalColor(configMap["nativeAdBadgeBgColor"] as? String),
                    nativeAdBadgeTextColor = parseOptionalColor(configMap["nativeAdBadgeTextColor"] as? String),
                    nativeCtaBgColor = parseOptionalColor(configMap["nativeCtaBgColor"] as? String),
                    nativeCtaTextColor = parseOptionalColor(configMap["nativeCtaTextColor"] as? String),
                    nativeFontFamilyName = configMap["nativeFontFamilyName"] as? String
                )

                AdsManager.init(act.application, config, enableResumeAppOpenAd)
                result.success(true)
            }

            "setPremium" -> {
                val premium = call.argument<Boolean>("premium") ?: false
                AdsManager.isPremium(premium)
                result.success(null)
            }

            "isPremium" -> {
                result.success(AdsManager.isPremium())
            }

            "isDNS" -> {
                val act = activity
                val isDns = if (act != null) AdsManager.isDNS(act) else AdsManager.isDNS
                result.success(isDns)
            }

            "requestConsent" -> {
                val act = activity
                if (act == null) {
                    result.success(true)
                    return
                }
                AdsManager.requestConsent(act) { canRequestAds ->
                    result.success(canRequestAds)
                }
            }

            "showPrivacyOptions" -> {
                val act = activity
                if (act == null) {
                    result.success(true)
                    return
                }
                AdsManager.showPrivacyOptions(act) {
                    result.success(true)
                }
            }

            "showInterstitial" -> {
                val act = activity
                val count = call.argument<Int>("count") ?: 1
                if (act == null) {
                    result.success(true)
                    return
                }
                AdsManager.showInterstitial(act, count) {
                    result.success(true)
                }
            }

            "preloadAds" -> {
                activity?.let { AdsManager.preloadAds(it) }
                result.success(null)
            }

            "showRewarded" -> {
                val act = activity
                if (act == null) {
                    result.success(mapOf("earned" to false))
                    return
                }
                var earned = false
                var amount = 1
                var type = ""
                AdsManager.showRewarded(
                    act,
                    onEarned = { reward ->
                        earned = true
                        amount = reward.amount
                        type = reward.type
                    },
                    onClosed = {
                        result.success(mapOf("earned" to earned, "amount" to amount, "type" to type))
                    }
                )
            }

            "showSplashAppOpenAd" -> {
                val act = activity
                val timeoutMs = (call.argument<Number>("timeoutMs"))?.toLong() ?: 6000L
                if (act == null) {
                    result.success(true)
                    return
                }
                AdsManager.loadAndShowSplashAppOpenAd(act, timeoutMs) {
                    result.success(true)
                }
            }

            else -> result.notImplemented()
        }
    }

    private fun parseColor(colorStr: String?, defaultColorHex: String): Int {
        return try {
            Color.parseColor(colorStr ?: defaultColorHex)
        } catch (_: Exception) {
            Color.parseColor(defaultColorHex)
        }
    }

    private fun parseOptionalColor(colorStr: String?): Int? {
        if (colorStr.isNullOrBlank()) return null
        return try {
            Color.parseColor(colorStr)
        } catch (_: Exception) {
            null
        }
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
