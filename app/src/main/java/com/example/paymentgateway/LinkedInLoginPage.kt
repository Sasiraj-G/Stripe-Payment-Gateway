package com.example.paymentgateway

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Dialog
import android.content.Intent
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


class LinkedInLoginPage : AppCompatActivity() {
    private lateinit var binding: ActivityLinkedInLoginPageBinding
    private lateinit var viewModel: LinkedInViewModel
    private lateinit var webView: WebView
    private lateinit var progressBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLinkedInLoginPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LinkedInViewModel::class.java)

        progressBar = findViewById(R.id.progressBar)

        binding.btnLinkedInLogin.setOnClickListener {
            initializeWebView()
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

    }
    @SuppressLint("SetJavaScriptEnabled")
    private fun initializeWebView() {
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()

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