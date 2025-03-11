package com.example.paymentgateway

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.paymentgateway.databinding.ActivityMediaOptionsBinding
import android.content.Intent
import android.Manifest


class MediaOptions : AppCompatActivity() {
    private lateinit var binding: ActivityMediaOptionsBinding
    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMediaOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPickImage.setOnClickListener {
            openImagePicker()
        }
        binding.btnPickAudio.setOnClickListener {
            openAudioPicker()
        }

        binding.btnPickVideo.setOnClickListener {
            openVideoPicker()
        }
        checkPermissions()

    }
    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_AUDIO)
            permissionsToRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11-12
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        } else {
            // Android 10 and below
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val needToRequest = permissionsToRequest.any {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needToRequest) {
            requestPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    // Image picker
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {

            binding.imageView.setImageURI(it)
            binding.textStatus.text = "Image loaded successfully"
        }
    }
    // Audio picker
    private val pickAudio = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val intent = Intent(this, AudioPlayer::class.java)
            intent.putExtra("AUDIO_URI", it.toString())
            startActivity(intent)
        }
    }

    // Video picker
    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val intent = Intent(this, VideoPlayer::class.java)
            intent.putExtra("VIDEO_URI", it.toString())
            startActivity(intent)
        }
    }

    private fun openImagePicker() {
        pickImage.launch("image/*")
    }

    private fun openAudioPicker() {
        pickAudio.launch("audio/*")
    }

    private fun openVideoPicker() {
        pickVideo.launch("video/*")
    }

}