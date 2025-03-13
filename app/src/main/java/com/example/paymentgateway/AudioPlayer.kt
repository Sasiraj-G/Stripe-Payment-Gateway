package com.example.paymentgateway

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.example.paymentgateway.databinding.ActivityAudioPlayerBinding

class AudioPlayer : AppCompatActivity() {

    private lateinit var binding: ActivityAudioPlayerBinding
    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private var isPlaying = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val audioUriString = intent.getStringExtra("AUDIO_URI")
        if (audioUriString != null) {
            setupMediaPlayer(Uri.parse(audioUriString))
        } else {
            finish()
        }
        // network connection


        setupControls()
    }

    private fun setupMediaPlayer(uri: Uri) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(applicationContext, uri)
                prepare()
                binding.seekBar.max = duration
            }
            updateSeekBar()

        } catch (e: Exception) {
            binding.audio.text = "Error: ${e.message}"
        }
    }

    private fun setupControls() {
        // Play Pause button
        binding.btnPlayPause.setOnClickListener {
            if (isPlaying) {
                mediaPlayer?.pause()
                binding.btnPlayPause.text = "Play"
            } else {
                mediaPlayer?.start()
                binding.btnPlayPause.text = "Pause"
            }
            isPlaying = !isPlaying



        }

        // Seekbar listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })


        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun updateSeekBar() {
        mediaPlayer?.let { player ->
            if (player.isPlaying) {
                binding.seekBar.progress = player.currentPosition
                binding.textCurrentTime.text = formatTime(player.currentPosition)
                binding.textTotalTime.text = formatTime(player.duration)
            }
        }

        handler.postDelayed({ updateSeekBar() }, 1000)
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        handler.removeCallbacksAndMessages(null)
    }
}