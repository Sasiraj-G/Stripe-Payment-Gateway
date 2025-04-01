package com.example.paymentgateway

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.paymentgateway.databinding.FragmentInboxPageBinding
import com.example.paymentgateway.imagepick.MultipleImagePicker
import com.example.paymentgateway.veriff.Veriff


class InboxPage : Fragment() {
    private lateinit var binding: FragmentInboxPageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentInboxPageBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imagePicker.setOnClickListener {

            val intent = Intent(requireContext(), MultipleImagePicker::class.java)
            startActivity(intent)

        }
        binding.locationSearch.setOnClickListener {
            val intent = Intent(requireContext(), LocationSearch::class.java)
            startActivity(intent)
        }
        binding.veriff.setOnClickListener {
            val intent = Intent(requireContext(), Veriff::class.java)
            startActivity(intent)
        }


    }

}