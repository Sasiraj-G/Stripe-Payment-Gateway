package com.example.paymentgateway

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.paymentgateway.databinding.FragmentProfilePageBinding
import com.example.paymentgateway.databinding.FragmentTripsPageBinding


class ProfilePage : Fragment() {

    private lateinit var binding: FragmentProfilePageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.crashTest.setOnClickListener {
            throw RuntimeException("Test Crash")
        }

        binding.gotoPayment.setOnClickListener{
            val intent = Intent(requireContext(),MainActivity::class.java)
            startActivity(intent)
        }
        binding.goToSplashScreen.setOnClickListener {
            val intent = Intent(requireContext(),SplashScreen::class.java)
            startActivity(intent)
        }
    }




}