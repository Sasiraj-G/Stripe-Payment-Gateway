package com.example.paymentgateway.graphqlimp

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.replace
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.paymentgateway.R
import com.example.paymentgateway.TripsPage
import com.example.paymentgateway.databinding.FragmentImageTransitionBinding
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

class ImageTransitionFragment : Fragment() {

    private var _binding: FragmentImageTransitionBinding? = null
    private val binding get() = _binding!!
    private var tripId: String? = null
    private var imageUrl: String? = null
    companion object {
        private const val ARG_TRIP_ID = "trip_id"
        private const val ARG_IMAGE_URL = "image_url"
        fun newInstance(tripId: String, imageUrl: String): ImageTransitionFragment {
            return ImageTransitionFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TRIP_ID, tripId)
                    putString(ARG_IMAGE_URL, imageUrl)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentImageTransitionBinding.inflate(inflater, container, false)
        tripId = arguments?.getString(ARG_TRIP_ID)
        imageUrl = arguments?.getString(ARG_IMAGE_URL)

        imageUrl?.let { Log.d("InsideFrag", it) }

        val animation = TransitionInflater.from(requireContext()).inflateTransition(
            android.R.transition.move
        )

        sharedElementEnterTransition=animation
        postponeEnterTransition(2000, TimeUnit.MILLISECONDS)
        startPostponedEnterTransition()

        ViewCompat.setTransitionName(binding.profileImage, "$tripId")
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(this)
            .load(imageUrl)
            .listener(object : com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    startPostponedEnterTransition()
                    return false
                }
            })
            .into(binding.profileImage)


        binding.backButton.setOnClickListener {
            val animation = TransitionInflater.from(requireContext()).inflateTransition(
                android.R.transition.move
            )
           sharedElementReturnTransition=animation
            activity?.supportFragmentManager?.beginTransaction()?.add(R.id.fragment_containerView,
                TripsPage()
            )?.commit()
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}