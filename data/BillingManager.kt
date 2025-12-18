// data/BillingManager.kt (NEW)

package com.neon.peggame.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsManager: SettingsManager
) : PurchasesUpdatedListener, BillingClientStateListener {

    private val TAG = "BillingManager"
    private val SKU_PREMIUM = "premium_unlock"
    
    private lateinit var billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.IO)

    // Expose product details for UI display (price)
    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails.asStateFlow()

    init {
        initializeBillingClient()
    }

    private fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        connectToGooglePlay()
    }

    private fun connectToGooglePlay() {
        if (!billingClient.isReady) {
            billingClient.startConnection(this)
        }
    }

    // --- BillingClientStateListener Overrides ---

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d(TAG, "Billing setup successful.")
            queryProductDetails()
            queryExistingPurchases()
        } else {
            Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.w(TAG, "Billing service disconnected. Retrying connection...")
        // Try to restart the connection
        scope.launch { connectToGooglePlay() }
    }

    // --- Query Product Details ---

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(SKU_PREMIUM)
                .setProductType(ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params,
            ProductDetailsResponseListener { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    _productDetails.value = productDetailsList.first()
                    Log.d(TAG, "Product details found for ${SKU_PREMIUM}")
                } else {
                    Log.e(TAG, "Query product details failed: ${billingResult.debugMessage}")
                }
            }
        )
    }

    // --- Purchase Flow ---

    fun purchasePremium(activity: Activity) {
        val details = _productDetails.value
        val offerDetails = details?.oneTimePurchaseOfferDetails
        
        if (details == null || offerDetails == null) {
            Log.e(TAG, "Product details or offer not available. Cannot launch purchase flow.")
            connectToGooglePlay() 
            return
        }

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }

    // --- PurchasesUpdatedListener Overrides (Handles result of purchase) ---

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else {
            Log.e(TAG, "Purchase error or cancelled: ${billingResult.debugMessage}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && purchase.products.contains(SKU_PREMIUM)) {
            if (!purchase.isAcknowledged) {
                // Acknowledge the purchase to finalize it (required for non-consumables)
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.i(TAG, "Premium Purchase acknowledged. Status granted.")
                        scope.launch { settingsManager.setPremiumStatus(true) }
                    } else {
                        Log.e(TAG, "Purchase acknowledgment failed: ${billingResult.debugMessage}")
                    }
                }
            } else {
                 Log.i(TAG, "Premium Purchase already acknowledged.")
                 scope.launch { settingsManager.setPremiumStatus(true) }
            }
        }
    }

    // --- Query Existing Purchases (Restore purchases) ---

    private fun queryExistingPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params, PurchasesResponseListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumFound = purchases.any { 
                    it.products.contains(SKU_PREMIUM) && 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                scope.launch {
                    settingsManager.setPremiumStatus(premiumFound)
                }
                
                // Ensure unacknowledged purchases are acknowledged
                purchases.filter { !it.isAcknowledged }.forEach { handlePurchase(it) }
            } else {
                Log.e(TAG, "Query purchases failed: ${billingResult.debugMessage}")
            }
        })
    }
}
