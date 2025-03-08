package com.example.paymentgateway

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.paymentgateway.databinding.ActivityMainBinding
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var paymentSheet: PaymentSheet
    private lateinit var customerId: String
    private lateinit var ephemeralKey: String
    private lateinit var clientSecret: String
    private var apiInterface = ApiUtilities.getApiInterface()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PaymentConfiguration.init(this, Utils.PUBLISHABLE_KEY)
        getCustomerId()

        binding.btn.setOnClickListener {
            paymentFlow()
        }

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)

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
}

