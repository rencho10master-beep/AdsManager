package com.ads.manager.consent

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object ConsentManager {

    /**
     * Request Consent Info Update and show Consent Form if required (GDPR / EEA / UK users).
     * Automatically skips for non-EEA users (like in India, USA, etc.).
     * Always calls [onComplete] with a boolean indicating whether ads can be requested.
     */
    fun requestConsent(
        activity: Activity,
        onComplete: (canRequestAds: Boolean) -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onComplete(true)
            return
        }

        try {
            val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
            val params = ConsentRequestParameters.Builder().build()

            consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                {
                    UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { _ ->
                        activity.runOnUiThread {
                            onComplete(consentInformation.canRequestAds())
                        }
                    }
                },
                { _ ->
                    // Network failure or error -> proceed safely without crashing
                    activity.runOnUiThread {
                        onComplete(consentInformation.canRequestAds())
                    }
                }
            )
        } catch (_: Exception) {
            onComplete(true)
        }
    }

    /**
     * Show Privacy Options Form (allows users in Settings to modify their consent choices).
     */
    fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onDismissed?.invoke()
            return
        }

        try {
            UserMessagingPlatform.showPrivacyOptionsForm(activity) { _ ->
                activity.runOnUiThread {
                    onDismissed?.invoke()
                }
            }
        } catch (_: Exception) {
            onDismissed?.invoke()
        }
    }

    /**
     * Check if consent permits requesting ads.
     */
    fun canRequestAds(activity: Activity): Boolean {
        return try {
            UserMessagingPlatform.getConsentInformation(activity).canRequestAds()
        } catch (_: Exception) {
            true
        }
    }

    /**
     * Check if user needs privacy options form shown (to display a "Privacy Settings" button).
     */
    fun isPrivacyOptionsRequired(activity: Activity): Boolean {
        return try {
            UserMessagingPlatform.getConsentInformation(activity).privacyOptionsRequirementStatus ==
                    ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Reset consent state (for debugging / testing).
     */
    fun resetConsent(activity: Activity) {
        try {
            UserMessagingPlatform.getConsentInformation(activity).reset()
        } catch (_: Exception) {}
    }
}
