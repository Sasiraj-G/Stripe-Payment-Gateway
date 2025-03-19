package com.example.paymentgateway

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.paymentgateway.databinding.ActivityCrashBinding

class CrashActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCrashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.restartButton.setOnClickListener {
            restartApp()
        }

    }
    private fun restartApp() {
        val intent = Intent(this, LinkedInLoginPage::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }
}