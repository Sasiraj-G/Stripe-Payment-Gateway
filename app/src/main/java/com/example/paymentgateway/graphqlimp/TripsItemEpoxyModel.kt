package com.example.paymentgateway.graphqlimp

import androidx.databinding.ViewDataBinding

import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.example.paymentgateway.databinding.ItemReservationBinding

abstract class TripItemEpoxyModel : DataBindingEpoxyModel() {
    @EpoxyAttribute
    lateinit var trip: Trip

//    @EpoxyAttribute
//    lateinit var onHostDetailsClick: (Trip) -> Unit

    override fun setDataBindingVariables(binding: ViewDataBinding) {
        if (binding is ItemReservationBinding) {
            binding.trip = trip
            binding.executePendingBindings()

//            binding.approveBtn.setOnClickListener {
//                onHostDetailsClick(trip)
//            }
        }
    }
}

