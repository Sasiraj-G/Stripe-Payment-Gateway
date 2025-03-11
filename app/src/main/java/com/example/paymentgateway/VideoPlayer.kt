package com.example.paymentgateway


import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentgateway.databinding.ActivityVideoPlayerBinding

class VideoPlayer : AppCompatActivity() {

    private lateinit var binding: ActivityVideoPlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoUriString = intent.getStringExtra("VIDEO_URI")
        if (videoUriString != null) {
            val videoUri = Uri.parse(videoUriString)
            setupVideoPlayer(videoUri)
        } else {
            finish()
        }

        // Setup close button
        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun setupVideoPlayer(uri: Uri) {
        try {

            binding.videoView.setVideoURI(uri)


            binding.videoView.setOnPreparedListener { mp ->
                binding.videoView.start()
                binding.videoView.setMediaController(android.widget.MediaController(this).apply {
                    setAnchorView(binding.videoView)
                })
            }

            // Handle errors
            binding.videoView.setOnErrorListener { _, _, _ ->
                binding.videoPlayer.text = "Error playing video"
                true
            }

        } catch (e: Exception) {
            binding.videoPlayer.text = "Error: ${e.message}"
        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.videoView.stopPlayback()
    }
}
