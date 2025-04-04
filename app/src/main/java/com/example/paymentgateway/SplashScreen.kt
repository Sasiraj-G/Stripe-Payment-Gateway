package com.example.paymentgateway

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager.OnActivityDestroyListener
import android.view.View
import android.view.WindowManager
import android.widget.MediaController
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.paymentgateway.databinding.ActivitySplashScreenBinding

class SplashScreen : AppCompatActivity() {
    private lateinit var binding : ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)
        WindowCompat.setDecorFitsSystemWindows(
            window,
            false
        )
        this.window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        val videoPath = "android.resource://$packageName/${R.raw.car_splash_screen}"
        val videoUri = Uri.parse(videoPath)
        binding.videoView.setVideoURI(videoUri)
        binding.videoView.start()

        binding.videoView.setOnCompletionListener { _ ->
            navigateToMainActivity()
        }
        binding.skipButton.setOnClickListener {
            binding.videoView.stopPlayback()
            navigateToMainActivity()
        }
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, LinkedInLoginPage::class.java)
        startActivity(intent)
        finish()
    }


}