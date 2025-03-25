package com.example.paymentgateway.imagepick

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.example.paymentgateway.R
import com.example.paymentgateway.databinding.ItemAddImageBinding

@EpoxyModelClass
abstract class ImagePlaceholderModel : EpoxyModelWithHolder<ImagePlaceholderModel.Holder>() {
    @EpoxyAttribute
    var onPlaceholderClick: ((View) -> Unit)? = null

    override fun getDefaultLayout() = R.layout.item_add_image

    override fun bind(holder: Holder) {

        holder.binding.placeholderContainer.setOnClickListener {
            onPlaceholderClick?.invoke(it)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemAddImageBinding
        override fun bindView(itemView: View) {
            binding=ItemAddImageBinding.bind(itemView)

        }

    }
}