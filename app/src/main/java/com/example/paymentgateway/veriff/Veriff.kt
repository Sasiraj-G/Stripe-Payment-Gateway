package com.example.paymentgateway.veriff

import android.content.Intent
import android.os.Bundle
import android.provider.Telephony.Mms.Inbox
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentgateway.databinding.ActivityVeriffBinding
import com.veriff.Result
import com.veriff.Sdk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import java.util.concurrent.TimeUnit
import kotlin.math.E

class Veriff : AppCompatActivity() {

    private lateinit var binding : ActivityVeriffBinding

    private val authToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6ImZlODA5YzYwLTBmMDUtMTFmMC1hNjFiLTE1ZWRjMjlhZGY3MiIsImVtYWlsIjoibW9oYW5AZ21haWwuY29tIiwiaWF0IjoxNzQzNTE3ODg2LCJleHAiOjE3NTkwNjk4ODZ9.9xEXgrKRoxnTlbzFz8STcGAmnIlZu8XKb-LgjCdcMC4"
    private val userId = "fe809c60-0f05-11f0-a61b-15edc29adf72"
    private val baseUrl = "https://rent.ventucr.com/api/create-verify-session"
    private val SiteBaseUrl="https://rent.ventucr.com/api/"
    private lateinit var sessionId: String

    private val REQUEST_CODE_VERIFF = 12


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityVeriffBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.veriffBtn.setOnClickListener {
            validateAndVerify()
        }


    }



    private fun validateAndVerify() {

        val firstName = binding.firstName.text.toString().trim()
        val lastName = binding.lastName.text.toString().trim()
        val country = binding.country.text.toString().trim()


        if (firstName.isEmpty() || lastName.isEmpty() || country.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        binding.progressbar.visibility = View.VISIBLE

        getVeriffSessionToken(firstName, lastName, country)
    }

    private fun getVeriffSessionToken(firstName: String, lastName: String, country: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create JSON payload for session creation
                val jsonObject = JSONObject().apply {
                    put("firstName", firstName)
                    put("lastName", lastName)
                    put("country", country)
                    put("userId", userId)
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val mediaType = "application/json".toMediaTypeOrNull()
                val body = jsonObject.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url(baseUrl)
                    .post(body)
                    .addHeader("auth", "$authToken")
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                Log.d("responseData",responseData.toString())

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && responseData != null) {
                        val jsonResponse = JSONObject(responseData)
                        val data = jsonResponse.getJSONObject("data")
                        val sessionToken= data.getString("url")
                        sessionId = data.getString("sessionId")
                        Log.d("responseData"," url ${sessionToken}")
                        Log.d("responseData"," url ${sessionId}")
                        if (sessionToken.isNotEmpty()) {
                            launchVeriffSdk(sessionToken)
                            Log.d("Verffi",response.body.toString())
                        } else {
                            showError("Failed to get session token")
                        }
                    } else {
                        showError("Error: ${response.code}")
                        Log.d("responseData", responseData.toString())
                        Log.d("responseData",response.body.toString())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showError("Error: ${e.message}")
                }
            }
        }
    }

    private fun launchVeriffSdk(sessionToken: String) {
        binding.progressbar.visibility = View.GONE

        val sessionUrl = sessionToken
        Log.d("responseData"," Indside lanuchveriffsdk${sessionUrl})")
        val intent = Sdk.createLaunchIntent(this, sessionUrl)
        startActivityForResult(intent, REQUEST_CODE_VERIFF)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_VERIFF) {
            val result = Result.fromResultIntent(data)
            if (result != null) {
                handleResult(result)
            }
            return
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }


    private fun handleResult(result: Result) {
        when (result.status) {
            Result.Status.DONE -> {
            if(sessionId.isNotEmpty()){
                Log.d("responseData",sessionId)
                Toast.makeText(this@Veriff,"Inside When ${sessionId}",Toast.LENGTH_LONG).show()
                sendSuccessVerification(sessionId)
            }else{
                showError("Failed to update session ID")
            }

            }
            Result.Status.CANCELED -> {

                Log.i("UserData", "Verification canceled")
            }
            Result.Status.ERROR ->

                Log.w("UserData", "Verification error occurred: " + result.error)
        }
    }
    private fun sendSuccessVerification(sessionId: String) {
        binding.progressbar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build()

                val jsonBody = JSONObject().apply {
                    put("sessionId", sessionId)
                }
                val body = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())

                val mediaType = "application/json".toMediaTypeOrNull()


                val request = Request.Builder()
                    .url("${SiteBaseUrl}success-veriff?sessionId=${sessionId}")
                    .addHeader("auth", authToken)
                    .addHeader("Content-Type", "application/json")
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()
                Log.d("responseData",responseData.toString())

                withContext(Dispatchers.Main) {
                    binding.progressbar.visibility = View.GONE
                    Log.d("responseData",responseData.toString())
                    Log.d("responseData",sessionId)
                    if (responseData != null) {
                        val jsonResponse = JSONObject(responseData)
                        val status = jsonResponse.optInt("status")
                        if (status == 200) {
                            Toast.makeText(this@Veriff, "Verification successfully recorded", Toast.LENGTH_LONG).show()
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            showError("Error status code")
                        }
                    } else {
                        showError("Error recording verification: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressbar.visibility = View.GONE
                    showError("Error recording verification: ${e.message}")
                }
            }
        }
    }



    private fun showError(message: String) {
        binding.progressbar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}


