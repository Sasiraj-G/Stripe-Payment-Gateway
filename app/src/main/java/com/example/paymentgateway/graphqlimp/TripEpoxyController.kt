package com.example.paymentgateway.graphqlimp

import android.widget.ImageView
import com.airbnb.epoxy.EpoxyController

class TripEpoxyController(
    private val onProfileImageClick: (Trip, ImageView) -> Unit
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
                profileImageUrl(trip.imageUrl)
                onProfileImageClickListener { view ->
                    onProfileImageClick(trip, view as ImageView)
                }



            }


        }
    }
}