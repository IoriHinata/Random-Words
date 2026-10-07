package ru.randomwords

import android.app.Activity
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

object Ads {
    const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/9214589741"
    private var initialized = false

    fun requestConsentAndInitialize(activity: Activity, onReady: () -> Unit) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        val params = ConsentRequestParameters.Builder().build()
        consentInformation.requestConsentInfoUpdate(
            activity, params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    initializeIfAllowed(activity, consentInformation, onReady)
                }
            },
            {
                initializeIfAllowed(activity, consentInformation, onReady)
            }
        )
    }

    private fun initializeIfAllowed(activity: Activity, consentInformation: ConsentInformation, onReady: () -> Unit) {
        if (!consentInformation.canRequestAds()) return
        if (initialized) { onReady(); return }
        initialized = true
        MobileAds.initialize(activity) { activity.runOnUiThread { onReady() } }
    }

    fun showPrivacyOptions(activity: Activity) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { }
    }
}
