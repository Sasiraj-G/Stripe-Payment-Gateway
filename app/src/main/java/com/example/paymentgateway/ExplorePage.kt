package com.example.paymentgateway

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.paymentgateway.databinding.FragmentExplorePageBinding


class ExplorePage : Fragment() {

    private lateinit var binding: FragmentExplorePageBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentExplorePageBinding.inflate(inflater,container,false)

        binding.explopreDetails.setOnClickListener {
            val intent = Intent(requireContext(),ExploreViewDetails::class.java)
            startActivity(intent)
        }
       return binding.root

    }




}