package com.example.paymentgateway

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

import coil.load
import coil.transform.CircleCropTransformation
import com.example.paymentgateway.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {
    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_profile)

        val firstName = intent.getStringExtra("USER_FIRST_NAME") ?: ""
        val lastName = intent.getStringExtra("USER_LAST_NAME") ?: ""
        val email = intent.getStringExtra("USER_EMAIL") ?: ""
        val profilePictureUrl = intent.getStringExtra("USER_PROFILE_PICTURE")

        binding.userName.text="$firstName $lastName"
        binding.userEmail.text="$email"


        if (!profilePictureUrl.isNullOrEmpty()) {
            binding.profileImage.load(profilePictureUrl) {
                crossfade(true)
                transformations(CircleCropTransformation())
                placeholder(R.drawable.placeholder_profile)

            }
        } else {
            binding.profileImage.load(R.drawable.placeholder_profile)
        }

       binding.goToPaymentScreen.setOnClickListener {
           val intent = Intent(this@ProfileActivity,MainActivity::class.java)
           startActivity(intent)
        }

    }
}