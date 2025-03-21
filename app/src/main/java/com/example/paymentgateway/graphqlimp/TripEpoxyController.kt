package com.example.paymentgateway.graphqlimp

import com.airbnb.epoxy.EpoxyController

class TripEpoxyController(
    private var onHostEmailClick: (Trip) -> Unit
) : EpoxyController() {
    private val trips = mutableListOf<Trip>()

    fun setData(newTrips: List<Trip>) {
        trips.clear()
        trips.addAll(newTrips)
        requestModelBuild()
    }

    fun addData(newTrips: List<Trip>) {
        trips.addAll(newTrips)
        requestModelBuild()
    }

    override fun buildModels() {
        trips.forEach { trip ->
            tripEpoxyItem {
                id(trip.id)
                trip(trip)
//                onHostEmailClick(@TripListFragment.onHostEmailClick)
//                onHostEmailClick(this@TripEpoxyController.onHostEmailClick)

            }


        }
    }
}