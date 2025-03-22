package com.example.paymentgateway.graphqlimp

import android.content.Intent
import android.net.Uri
import com.example.paymentgateway.TripsPage
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi

import androidx.lifecycle.lifecycleScope
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional


import com.apollographql.apollo3.exception.ApolloException

import com.example.paymentgateway.GetAllReservationQuery

import com.example.paymentgateway.databinding.FragmentTripListBinding
import com.example.paymentgateway.databinding.ItemReservationBinding
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


class TripListFragment : Fragment() {
    private var _binding: FragmentTripListBinding? = null

    private lateinit var reservationBinding: ItemReservationBinding
    private val binding get() = _binding!!
    private lateinit var apolloClient: ApolloClient
    private lateinit var epoxyController: TripEpoxyController
    private var dateFilter: String?= null
 //   private var dateFilter = "upcomming"
    private var currentPage  = 1
    private var isLastPage = false
    private var isLoading = false

    companion object {
        private const val ARG_DATE_FILTER = "date_filter"

        fun newInstance(dateFilter: String): TripListFragment {
            return TripListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_DATE_FILTER, dateFilter)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTripListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get date filter from arguments
        dateFilter = ((arguments?.getString(ARG_DATE_FILTER) ?: "upcomming"))
      //  dateFilter = "upcomming"

        // Get Apollo client from parent fragment
        apolloClient = (parentFragment as TripsPage).getApolloClient()


        setupEpoxyRecyclerView()

        // Load initial data
        fetchTrips()

        // Set up retry button
        binding.retryButton.setOnClickListener {
            binding.errorView.visibility = View.GONE
            fetchTrips()
        }





    }



    private fun setupEpoxyRecyclerView() {
        epoxyController = TripEpoxyController { trip ->
            redirectEmail(trip)
        }

        binding.recyclerView.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            setController(epoxyController)
        }

            // Add pagination scroll listener
           binding.recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val layoutManager = recyclerView.layoutManager as androidx.recyclerview.widget.LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!isLoading && !isLastPage) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 10) {
                            loadMoreTrips()
                        }
                    }
                }
            })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchTrips() {
        if (isLoading) return

        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val query = GetAllReservationQuery(
                    userType = Optional.present("guest"),
                    currentPage = Optional.present(currentPage),
                    dateFilter = Optional.present(dateFilter)
                )




                val tes = apolloClient.query(GetAllReservationQuery()).execute().data
                Log.d("response2", "Response ${tes}")


                val response = apolloClient.query(query).execute().data?.getAllReservation
                Log.d("response1", "Response ${response}")

                handleResponse(response)
            } catch (e: ApolloException) {
                showError("Network error: ${e.message}")
            } catch (e: Exception) {
                showError("Something went wrong: ${e.message}")
            } finally {
                isLoading = false
                binding.progressBar.visibility = View.GONE
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadMoreTrips() {
        currentPage++
        fetchTrips()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleResponse(response: GetAllReservationQuery.GetAllReservation?) {
        val data = response


        if (data != null) {
            val reservations = data.result
            Log.d("Inside","$reservations")

            if (reservations != null) {
                if (reservations.isNotEmpty()) {
                    val trips = reservations.map { reservation ->
                        Trip(
                            id = reservation?.id.toString(),
                            title = reservation?.listData?.title.toString(),
                            displayName = reservation?.hostData?.displayName.toString(),
                            location = "${reservation?.listData?.street}, ${reservation?.listData?.city}, ${reservation?.listData?.state}, ${reservation?.listData?.country}, ${reservation?.listData?.zipcode}",
                            dateRange = "${reservation?.checkIn?.let { formatDate(it) }} - ${reservation?.checkOut?.let {
                                formatDate(
                                    it
                                )
                            }}",
                            price = "${reservation?.currency} ${reservation?.total}",
                            phone = reservation?.hostData?.phoneNumber.toString(),
                            email = reservation?.hostUser?.email.toString(),
                            reservationState = reservation?.reservationState.toString(),
                            imageUrl = "https://staging1.flutterapps.io/images/avatar/"+reservation?.hostData?.picture

                        )
                    }

                    if (currentPage == 1) {
                        epoxyController.setData(trips)
                    } else {
                        epoxyController.addData(trips)
                    }


                    isLastPage = trips.size < 10




                    // Show empty state if necessary
                    showEmptyState(trips.isEmpty())

                } else {
                    isLastPage = true

                    if (currentPage == 1) {
                        // Show empty state if first page has no results
                        showEmptyState(true)
                    }
                }
            }
        } else {
            showError("No data received")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun formatDate(dateString: String): String {
        try {
            val inputFormatter = Instant.ofEpochMilli(dateString.toLong())
            val dateTime = LocalDateTime.ofInstant(inputFormatter, ZoneId.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm", Locale.getDefault())
            return dateTime.format(formatter)
        } catch (e: Exception) {
            return dateString
        }
    }




    private fun redirectEmail(trip: Trip){
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(trip.email))
        intent.type = "message/rfc822"
        requireContext().startActivity(intent)
    }





    private fun showEmptyState(show: Boolean) {
        binding.emptyStateView.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

    private fun showError(message: String) {
        binding.errorView.visibility = View.VISIBLE
        binding.errorTextView.text = message
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
