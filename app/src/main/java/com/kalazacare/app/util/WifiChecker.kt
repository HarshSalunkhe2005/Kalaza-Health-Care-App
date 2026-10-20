package com.kalazacare.app.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.kalazacare.app.BuildConfig

/**
 * The Wi-Fi gate is on exactly when at least one facility gateway IP is configured (WIFI_GATEWAY_IPS in
 * local.properties). Leave it empty for development or demos and anyone can log in from any network.
 */
val WIFI_GATE_ENABLED: Boolean get() = ALLOWED_GATEWAY_IPS.isNotEmpty()

/**
 * The facility Wi-Fi router(s)' gateway/local IP address (e.g. "192.0.2.3") -- the ONLY way
 * this app tells "am I on the right network". SSID matching was dropped entirely: several
 * Android 13+ devices (a test Vivo included) kept the SSID redacted as "<unknown ssid>" no
 * matter what -- every documented permission granted (ACCESS_FINE_LOCATION, NEARBY_WIFI_DEVICES,
 * Location Services, the network location provider), still unresolved. Gateway/routing info was
 * never part of that same privacy carve-out, since it's not considered location-identifying the
 * way a Wi-Fi network name is, so it's proven to actually work where SSID didn't.
 *
 * To find it on a phone already connected to the target network: Settings -> Wi-Fi -> tap the
 * connected network -> look for "Gateway" or "Router" under its IP details (may be under an
 * "Advanced" section depending on the phone). Put that value in WIFI_GATEWAY_IPS (comma-separated).
 */
val ALLOWED_GATEWAY_IPS: Set<String> =
    BuildConfig.WIFI_GATEWAY_IPS.split(',').map { it.trim() }.filter { it.isNotEmpty() }.toSet()

/**
 * The connected Wi-Fi network's gateway/router IP address, or null if not connected to Wi-Fi or
 * it couldn't be determined. Unlike SSID, this isn't gated behind location permission -- it's
 * ordinary IP routing info, available with just ACCESS_NETWORK_STATE.
 */
fun currentWifiGatewayIp(context: Context): String? {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val wifiNetwork = connectivityManager.allNetworks.firstOrNull { network ->
        connectivityManager.getNetworkCapabilities(network)?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    } ?: return null
    val linkProperties = connectivityManager.getLinkProperties(wifiNetwork) ?: return null
    return linkProperties.routes.firstOrNull { it.isDefaultRoute }?.gateway?.hostAddress
}
