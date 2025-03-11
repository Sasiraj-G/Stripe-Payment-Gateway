package com.example.paymentgateway

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.androidnetworking.AndroidNetworking
import com.androidnetworking.common.Priority
import com.androidnetworking.error.ANError
import com.androidnetworking.interfaces.JSONObjectRequestListener
import com.example.paymentgateway.databinding.ActivityMainBinding
import com.paypal.android.corepayments.CoreConfig
import com.paypal.android.corepayments.Environment
import com.paypal.android.corepayments.PayPalSDKError
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutClient
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutFundingSource
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutListener
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutRequest
import com.paypal.android.paypalwebpayments.PayPalWebCheckoutResult
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerId: String
    private lateinit var ephemeralKey: String
    private lateinit var clientSecret: String
    private var apiInterface = ApiUtilities.getApiInterface()


    //pay pal
    private val clientID = "AapUYh-hiVCclsRVQuN1F2JSq11QL8I0Cjaz-NHVeGR0x1tpdqh3zUn4avLXdD-NCfy4o5sZntFwVe4_"
    private val secretID = "EA6DCIxmRiQTR8YiRskDwag8RJPrUp7bKZCoe5tX-G-48dDrxKDRDiiK6B6UFXHN9cx4723a0uDVz1KA"
    private val returnUrl = "com.example.paymentgateway://god"
    var accessToken = ""
    private lateinit var uniqueId: String
    private var orderid = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //paypal
        AndroidNetworking.initialize(applicationContext)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PaymentConfiguration.init(this, Utils.PUBLISHABLE_KEY)
        getCustomerId()

        binding.btn.setOnClickListener {
            paymentFlow()
        }


        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)


        //paypal

        binding.paypalBtn.visibility = View.GONE

        fetchAccessToken()

        binding.paypalBtn.setOnClickListener {
            startOrder()
        }

        //meadia
        binding.mediaOptions.setOnClickListener {
            val intent = Intent(this@MainActivity,MediaOptions::class.java)
            startActivity(intent)
        }
    }

    private fun paymentFlow() {

        if (!::clientSecret.isInitialized || !::customerId.isInitialized || !::ephemeralKey.isInitialized) {
            Toast.makeText(this, "Payment data not ready yet. Please wait.", Toast.LENGTH_SHORT)
                .show()
            return
        }
        paymentSheet.presentWithPaymentIntent(
            clientSecret,
            PaymentSheet.Configuration(
                merchantDisplayName = "PaymentGateWay",
                customer = PaymentSheet.CustomerConfiguration(
                    id = customerId,
                    ephemeralKeySecret = ephemeralKey
                )
            )
        )
    }


    private fun getCustomerId() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = apiInterface.getCustomer()
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful && res.body() != null) {  // Fixed typo
                        customerId = res.body()!!.id
                        getEphemeralKey(customerId)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to get customer ID",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    fun getEphemeralKey(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = apiInterface.getEphemeralKey(customerId)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful && res.body() != null) {
                        ephemeralKey = res.body()!!.secret
                        getPaymentIntent(customerId)
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to get ephemeral key",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {

                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun getPaymentIntent(customerId: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val res = apiInterface.getPaymentIntent(customerId)
                withContext(Dispatchers.Main) {
                    if (res.isSuccessful && res.body() != null) {
                        clientSecret = res.body()!!.client_secret
                        Toast.makeText(this@MainActivity, "Ready for payment", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        Toast.makeText(
                            this@MainActivity,
                            "Failed to get payment intent",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun onPaymentSheetResult(paymentSheetResult: PaymentSheetResult) {
        when (paymentSheetResult) {
            is PaymentSheetResult.Completed -> {
                Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
            }

            is PaymentSheetResult.Canceled -> {
                Toast.makeText(this, "Payment Canceled", Toast.LENGTH_SHORT).show()
            }

            is PaymentSheetResult.Failed -> {
                Toast.makeText(
                    this,
                    "Payment Failed: ${paymentSheetResult.error.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    //paypal integration methods

    private fun handlerOrderID(orderID: String) {
        Log.d(TAG, orderID)
        val config = CoreConfig(clientID, environment = Environment.SANDBOX)
        Log.d(TAG, config.toString())
        val payPalWebCheckoutClient = PayPalWebCheckoutClient(this@MainActivity, config, returnUrl)
        Log.d(TAG,payPalWebCheckoutClient.toString())
        payPalWebCheckoutClient.listener = object : PayPalWebCheckoutListener {



                override fun onPayPalWebSuccess(result: PayPalWebCheckoutResult) {
                        Log.d(TAG, "onPayPalWebSuccess: $result")
                        Log.d(TAG, "${result.toString()}")

                }


            override fun onPayPalWebFailure(error: PayPalSDKError) {
                Log.d(TAG, "onPayPalWebFailure: $error")
            }

            override fun onPayPalWebCanceled() {
                Log.d(TAG, "onPayPalWebCanceled: ")
            }
        }

        orderid = orderID
        val payPalWebCheckoutRequest =
            PayPalWebCheckoutRequest(orderID, fundingSource = PayPalWebCheckoutFundingSource.PAYPAL)
        payPalWebCheckoutClient.start(payPalWebCheckoutRequest)

    }

    private fun startOrder() {
        uniqueId = UUID.randomUUID().toString()

        val orderRequestJson = JSONObject().apply {
            put("intent", "CAPTURE")
            put("purchase_units", JSONArray().apply {
                put(JSONObject().apply {
                    put("reference_id", uniqueId)
                    put("amount", JSONObject().apply {
                        put("currency_code", "USD")
                        put("value", "5.00")
                    })
                })
            })
            put("payment_source", JSONObject().apply {
                put("paypal", JSONObject().apply {
                    put("experience_context", JSONObject().apply {
                        put("payment_method_preference", "IMMEDIATE_PAYMENT_REQUIRED")
                        put("brand_name", "SH Developer")
                        put("locale", "en-US")
                        put("landing_page", "LOGIN")
                        put("shipping_preference", "NO_SHIPPING")
                        put("user_action", "PAY_NOW")
                        put("return_url", returnUrl)
                        put("cancel_url", "https://example.com/cancelUrl")
                    })
                })
            })
        }

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addHeaders("PayPal-Request-Id", uniqueId)
            .addJSONObjectBody(orderRequestJson)
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Order Response : $response")
                    handlerOrderID(response.getString("id"))
                }

                override fun onError(error: ANError) {
                    Log.d(
                        TAG,
                        "Order Error : ${error.message} || ${error.errorBody} || ${error.response}"
                    )
                }
            })
    }
    private fun fetchAccessToken() {
        val authString = "$clientID:$secretID"
        val encodedAuthString = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)

        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v1/oauth2/token")
            .addHeaders("Authorization", "Basic $encodedAuthString")
            .addHeaders("Content-Type", "application/x-www-form-urlencoded")
            .addBodyParameter("grant_type", "client_credentials")
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    accessToken = response.getString("access_token")
                    Log.d(TAG, accessToken)

                    Toast.makeText(this@MainActivity, "Access Token Fetched!", Toast.LENGTH_SHORT)
                        .show()

                    binding.paypalBtn.visibility = View.VISIBLE
                }

                override fun onError(error: ANError) {
                    Log.d(TAG, error.errorBody)
                    Toast.makeText(this@MainActivity, "Error Occurred!", Toast.LENGTH_SHORT).show()
                }
            })
    }



    private fun captureOrder(orderID: String) {
        AndroidNetworking.post("https://api-m.sandbox.paypal.com/v2/checkout/orders/$orderID/capture")
            .addHeaders("Authorization", "Bearer $accessToken")
            .addHeaders("Content-Type", "application/json")
            .addJSONObjectBody(JSONObject()) // Empty body
            .setPriority(Priority.HIGH)
            .build()
            .getAsJSONObject(object : JSONObjectRequestListener {
                override fun onResponse(response: JSONObject) {
                    Log.d(TAG, "Capture Response : $response")
                    Toast.makeText(this@MainActivity, "Payment Successful", Toast.LENGTH_SHORT).show()
                }

                override fun onError(error: ANError) {
                    // Handle the error
                    Log.e(TAG, "Capture Error : " + error.errorDetail)
                }
            })
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "onNewIntent: $intent")
        if (intent?.data!!.getQueryParameter("opType") == "payment") {
           captureOrder(orderid)
            Toast.makeText(this, "Payment Succssfully", Toast.LENGTH_SHORT).show()
        } else if (intent.data!!.getQueryParameter("opType") == "cancel") {
            Toast.makeText(this, "Payment Cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val TAG = "MyTag"
    }








}

