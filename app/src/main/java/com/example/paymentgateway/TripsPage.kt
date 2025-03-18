package com.example.paymentgateway

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.example.paymentgateway.databinding.FragmentTripsPageBinding
import com.example.paymentgateway.graphqlimp.TripListFragment
import com.google.android.material.tabs.TabLayoutMediator
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class TripsPage : Fragment() {
    private var _binding: FragmentTripsPageBinding? = null
    private val binding get() = _binding!!
    private lateinit var apolloClient: ApolloClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTripsPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Apollo Client
        initApolloClient()

        // Setup ViewPager with Tabs
        setupViewPager()
    }

    private fun initApolloClient() {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor =
            Interceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                    .addHeader("auth", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6Ijk3N2JjNTUwLTUwNjktMTFlOS1hMTRlLTYzNWUwZmQzYmZhNiIsImVtYWlsIjoicWFAcmFkaWNhbHN0YXJ0LmNvbSIsImlhdCI6MTc0MTkzNjAyMCwiZXhwIjoxNzU3NDg4MDIwfQ.iwOTUeK0X2S1MyQ36uj3buFJWvk_pH59HMh3eNkeH8k")
                    .method(original.method, original.body)
                chain.proceed(builder.build())
            }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        apolloClient = ApolloClient.Builder()
            .serverUrl("https://staging1.flutterapps.io/api/graphql")
            .okHttpClient(okHttpClient)
            .build()

    }

    private fun setupViewPager() {
        val viewPager = binding.viewPager
        viewPager.adapter = TripPagerAdapter(this)

        // Connect TabLayout with ViewPager
        TabLayoutMediator(binding.tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "Upcoming trips" else "Previous trips"
        }.attach()
    }


    fun getApolloClient(): ApolloClient {
        return apolloClient
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ViewPager adapter
    private inner class TripPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> TripListFragment.newInstance("upcomming")
                else -> TripListFragment.newInstance("previous")
            }
        }
    }

}
