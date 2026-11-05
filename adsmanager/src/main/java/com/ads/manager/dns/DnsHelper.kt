package com.ads.manager.dns

import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings

object DnsHelper {

    /**
     * Returns true if Private DNS (AdBlock DNS, AdGuard, NextDNS, etc.) is enabled on the device.
     * Returns false if Private DNS is off.
     */
    fun isPrivateDnsEnabled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                // 1. Settings.Global check
                val privateDnsMode = Settings.Global.getString(context.contentResolver, "private_dns_mode")
                if (privateDnsMode != null && privateDnsMode != "off") {
                    return true
                }

                // 2. ConnectivityManager LinkProperties check
                val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                val activeNetwork = connectivityManager?.activeNetwork
                val linkProperties = connectivityManager?.getLinkProperties(activeNetwork)
                val serverName = linkProperties?.privateDnsServerName
                if (!serverName.isNullOrEmpty()) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }
}
