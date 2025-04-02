package com.example.paymentgateway

import android.os.Bundle
import android.view.View
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone

import com.example.paymentgateway.databinding.ActivityRentAllMainActivityBinding
import com.example.paymentgateway.graphqlimp.ImageTransitionFragment

class RentAllMainAactivity : AppCompatActivity() {

    private lateinit var binding: ActivityRentAllMainActivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRentAllMainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //load default fragment

        supportFragmentManager.beginTransaction().replace(R.id.fragment_containerView,ExplorePage()).commit()

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val selectedFragment = when(item.itemId){
                R.id.expore -> ExplorePage()
                R.id.wishlists -> WishListsPage()
                R.id.trips -> TripsPage()
                R.id.inbox -> InboxPage()
                R.id.profile -> ProfilePage()
                else -> ExplorePage()

            }
            supportFragmentManager.beginTransaction().replace(R.id.fragment_containerView,selectedFragment).commit()
            true


        }

        binding.fab.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fragment_containerView,TripsPage()).commit()

        }


    }

    override fun onBackPressed() {
        // Get the current fragment

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStackImmediate()
        } else {
            super.onBackPressed()
        }
    }
}