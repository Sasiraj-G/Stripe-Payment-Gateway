package com.example.paymentgateway

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.paymentgateway.databinding.FragmentWishListsPageBinding


class WishListsPage : Fragment() {

    private lateinit var binding: FragmentWishListsPageBinding


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentWishListsPageBinding.inflate(inflater, container, false)
        binding.btnGo.setOnClickListener {
            val intent = Intent(this@WishListsPage.requireContext(), ExploreViewDetails::class.java)
            startActivity(intent)
        }

        return binding.root
    }



}