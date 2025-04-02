package com.example.paymentgateway.graphqlimp


import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.databinding.ViewDataBinding
import coil.load
import coil.transform.RoundedCornersTransformation
import com.airbnb.epoxy.DataBindingEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.example.paymentgateway.R
import com.example.paymentgateway.databinding.ItemReservationBinding
import kotlin.coroutines.coroutineContext

@EpoxyModelClass
abstract class TripEpoxyItem : DataBindingEpoxyModel() {
    @EpoxyAttribute
    lateinit var trip: Trip

    @EpoxyAttribute
    var profileImageUrl: String? = null

    @EpoxyAttribute(EpoxyAttribute.Option.DoNotHash)
    var onProfileImageClicked: ((ImageView) -> Unit)? = null

    @EpoxyAttribute
    lateinit var onProfileImageClickListener: View.OnClickListener

//    @EpoxyAttribute
//    lateinit var onHostEmailClick: (Trip) -> Unit
//
//    @EpoxyAttribute
//    lateinit var onHostPhoneClick: (Trip) -> Unit



    override fun setDataBindingVariables(binding: ViewDataBinding) {

        if (binding is ItemReservationBinding) {
            binding.trip = trip
            binding.phoneNumber.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:${trip.phone}")
                }
                binding.root.context.startActivity(intent)
            }
            binding.gmailAddress.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:${trip.email}")
                }
                binding.root.context.startActivity(intent)

            }

            //image click listener
            profileImageUrl?.let {
                Glide.with(binding.profileImage.context)
                    .load(it)
                    .circleCrop()
                    .into(binding.profileImage)
            }

            // Set transition name for shared element
            ViewCompat.setTransitionName(binding.profileImage, trip.id)

            binding.profileImage.setOnClickListener(onProfileImageClickListener)




            // Load image using Coil

            binding.profileImage.load(trip.imageUrl) {
                crossfade(true)
                transformations(RoundedCornersTransformation())
            }
            if(trip.title.contains("null")){
                binding.title.visibility= View.GONE
            }else{
                binding.title.visibility=View.VISIBLE
            }
            if(trip.location.contains("null")){
                binding.location.visibility= View.GONE
            }else{
                binding.location.visibility=View.VISIBLE
            }

            if(trip.phone.contains("null")){
                binding.phoneNumber.visibility= View.GONE
                binding.phoneIcon.visibility=View.GONE
            }else{
                binding.phoneNumber.visibility=View.VISIBLE
                binding.phoneIcon.visibility=View.VISIBLE
            }
            binding.executePendingBindings()
        }

    }


    override fun getDefaultLayout(): Int {
        return R.layout.item_reservation
    }
}
