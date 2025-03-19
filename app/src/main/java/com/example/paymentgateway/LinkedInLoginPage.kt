package com.example.paymentgateway

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Website.URL
import android.util.Log
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat

import androidx.lifecycle.ViewModelProvider
import com.example.paymentgateway.databinding.ActivityLinkedInLoginPageBinding
import com.example.paymentgateway.utils.LinkedInConstants
import com.example.paymentgateway.viewmodel.LinkedInViewModel
import com.google.android.material.snackbar.Snackbar


class LinkedInLoginPage : AppCompatActivity() {
    private lateinit var binding: ActivityLinkedInLoginPageBinding
    private lateinit var viewModel: LinkedInViewModel
    private lateinit var webView: WebView
    private lateinit var progressBar: View
    private val number = "+917603978806"

    private lateinit var rootView: View
    private lateinit var checkNetworkConnection: CheckNetworkConnection


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLinkedInLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LinkedInViewModel::class.java)

        progressBar = findViewById(R.id.progressBar)


        binding.crashTest.setOnClickListener {
            throw RuntimeException("Test Crash")
        }


        binding.btnLinkedInLogin.setOnClickListener {
            initializeWebView()
        }
        binding.gotoPayment.setOnClickListener{
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        binding.skype.setOnClickListener {

            binding.skype.openSkypeApp(number)

        }

        binding.rentAllBtn.setOnClickListener {
            val intent=Intent(this,RentAllMainAactivity::class.java)
            startActivity(intent)
        }

        binding.whatsapp.setOnClickListener {
             binding.whatsapp.openWhatsAppChat(number)

        }
        viewModel.linkedInUser.observe(this) { user ->

            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra("USER_ID", user.id)
                putExtra("USER_FIRST_NAME", user.firstName)
                putExtra("USER_LAST_NAME", user.lastName)
                putExtra("USER_EMAIL", user.email)
                putExtra("USER_PROFILE_PICTURE", user.profilePictureUrl)
            }
            startActivity(intent)
            finish()
        }

        viewModel.isLoading.observe(this) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }

//        //network connection
        callNetworkConnection()
        rootView = findViewById(android.R.id.content)

    }



    fun View.openWhatsAppChat(toNumber: String) {
        val url = "https://api.whatsapp.com/send?phone=$toNumber"
        try {
            context.packageManager.getPackageInfo("com.whatsapp", PackageManager.GET_ACTIVITIES)
            context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
        } catch (e: PackageManager.NameNotFoundException) {
           // context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            toastMessage("Whatsapp Not install in your device")
        }
    }
 fun View.openSkypeApp(toNumber:String){
     val url = "skype:$toNumber"
//     val sky = Intent("android.intent.action.CALL_PRIVILEGED");
     try{
         context.packageManager.getPackageInfo("com.skype.raider.Main",PackageManager.GET_ACTIVITIES)
         context.startActivity(Intent(Intent.ACTION_VIEW).apply { data = Uri.parse(url) })
     }catch (e: PackageManager.NameNotFoundException){
        // context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))
         toastMessage("Skype Not install in your device")
     }
 }

    @SuppressLint("SetTextI18n")
     fun callNetworkConnection() {
        checkNetworkConnection = CheckNetworkConnection(application)
        checkNetworkConnection.observe(this) { state ->
            when (state) {
                NetworkState.CONNECTED -> {
                    //showSnackbar("Network Connected")
                    toastMessage("Network Connected")
                }
                NetworkState.DISCONNECTED -> {

                    //showSnackbar("No Internet Connection")
                    toastMessage("No Internet Connection")
                }
                NetworkState.NO_INTERNET-> {

                    //showSnackbar("Wi-fi Connected But No Intenet Access")
                    toastMessage("Wi-fi Connected But No Intenet Access")

                }
            }
        }
    }
     fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(rootView, message, Snackbar.ANIMATION_MODE_SLIDE)
        snackbar.show()
    }

    fun toastMessage(msg : String){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }




    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                Log.d("urlLogin", url)

                if (url.startsWith(LinkedInConstants.REDIRECT_URI)) {

                    val uri = Uri.parse(url)
                    val code = uri.getQueryParameter("code")

                    if (code != null) {
                        // Handle authorization response
                        viewModel.handleAuthorizationResponse(code)
                        return true
                    }
                }

                return false
            }
        }
        // Load the LinkedIn authorization URL
        webView.loadUrl(viewModel.getAuthorizationUrl())

        // Add WebView to the activity
        setContentView(webView)
    }





}