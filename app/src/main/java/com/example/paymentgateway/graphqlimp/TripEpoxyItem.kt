package com.example.paymentgateway.graphqlimp

import android.view.View
import android.widget.TextView
import androidx.databinding.ViewDataBinding
import coil.load
import coil.transform.RoundedCornersTransformation
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.example.paymentgateway.R
import com.example.paymentgateway.databinding.ItemReservationBinding

@EpoxyModelClass
abstract class TripEpoxyItem : DataBindingEpoxyModel() {
    @EpoxyAttribute
    lateinit var trip: Trip

    @EpoxyAttribute
    lateinit var onHostDetailsClick: (Trip) -> Unit

    override fun setDataBindingVariables(binding: ViewDataBinding) {
        if (binding is ItemReservationBinding) {
            binding.trip = trip
            binding.approveBtn.setOnClickListener {
                onHostDetailsClick(trip)
            }

            // Load image using Coil
            binding.profileImage.load(trip.imageUrl) {
                crossfade(true)
                transformations(RoundedCornersTransformation(8f))
            }

            binding.executePendingBindings()
        }
    }
    override fun getDefaultLayout(): Int {
        return R.layout.item_reservation
    }
}
