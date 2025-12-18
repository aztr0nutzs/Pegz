// data/AdManager.kt (NEW)

package com.neon.peggame.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(@ApplicationContext private val context: Context) {

    private val TAG = "AdManager"
    // Test Ad Unit ID for Interstitials
    private val AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" 
    
    private var interstitialAd: InterstitialAd? = null

    init {
        // Initialize Mobile Ads SDK (ensure this runs on app startup)
        MobileAds.initialize(context)
    }

    fun loadAd() {
        if (interstitialAd != null) {
            // Ad already loaded or loading
            return
        }

        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            AD_UNIT_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Ad failed to load: ${adError.toString()}")
                    interstitialAd = null
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Ad was loaded.")
                    interstitialAd = ad
                }
            }
        )
    }

    /**
     * Shows the loaded interstitial ad.
     * @param activity The current Activity required for showing the full-screen ad.
     * @param onAdDismissed A callback to run after the user closes the ad or if the ad fails to show.
     */
    fun showAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    interstitialAd = null
                    loadAd() // Pre-load the next ad
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    onAdDismissed() // Ensure game flow continues
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed on screen.")
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Interstitial ad is not ready yet. Continuing game flow.")
            loadAd() // Try loading immediately
            onAdDismissed() // Continue flow immediately
        }
    }
}
