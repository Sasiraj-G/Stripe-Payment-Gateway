package com.example.paymentgateway.imagepick


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ScaleGestureDetector

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

import com.example.paymentgateway.databinding.ActivityImageViewerBinding


import com.github.chrisbanes.photoview.PhotoView


class ImageViewer : AppCompatActivity() {
    private lateinit var binding: ActivityImageViewerBinding
    private lateinit var imageUris: List<String>
    private var currentPosition = 0

    companion object {
        const val EXTRA_IMAGE_URIS = "extra_image_uris"
        const val EXTRA_INITIAL_POSITION = "extra_initial_position"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImageViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)


        imageUris = intent.getStringArrayListExtra(EXTRA_IMAGE_URIS) ?: emptyList()
        currentPosition = intent.getIntExtra(EXTRA_INITIAL_POSITION, 0)
            .coerceIn(0, imageUris.size - 1)

        setupViews()
    }

    private fun setupViews() {
        // Setup Epoxy Pager Controller
        val controller = ImagePagerController(imageUris) { photoView ->
            setupZoomListener(photoView)
        }
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.imageRecyclerView.layoutManager = layoutManager
        binding.imageRecyclerView.setController(controller)
        val pagerSnapHelper = PagerSnapHelper()
        pagerSnapHelper.attachToRecyclerView(binding.imageRecyclerView)

        binding.imageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION && position != currentPosition) {
                        currentPosition = position
                        updateIndexIndicator()
                    }
                }
            }
        })
        binding.imageRecyclerView.scrollToPosition(currentPosition)

        updateIndexIndicator()
        controller.requestModelBuild()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupZoomListener(photoView: PhotoView) {
        val scaleGestureDetector = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    val scaleFactor = detector.scaleFactor
                    val currentScale = photoView.scale

                    // Set new scale with bounds
                    val newScale = currentScale * scaleFactor
                    photoView.scale = newScale.coerceIn(1f, 4f)

                    return true
                }
            }
        )

        photoView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun updateIndexIndicator() {
        binding.imageIndexIndicator.apply {
            text = "${currentPosition + 1}/${imageUris.size}"
        }
    }
}

