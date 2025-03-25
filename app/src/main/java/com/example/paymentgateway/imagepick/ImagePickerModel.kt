package com.example.paymentgateway.imagepick

import android.net.Uri
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.example.paymentgateway.R

import com.example.paymentgateway.databinding.ItemPhotoBinding

@EpoxyModelClass
abstract class ImagePickerModel : EpoxyModelWithHolder<ImagePickerModel.Holder>() {
    @EpoxyAttribute
    lateinit var imageUri: Uri

    @EpoxyAttribute
    var onDeleteClick: ((View) -> Unit)? = null

    override fun getDefaultLayout() = R.layout.item_photo

    override fun bind(holder: Holder) {
        Glide.with(holder.binding.root.context)
            .load(imageUri)
            .into(holder.binding.imageView)

        holder.binding.deletePic.setOnClickListener {
            onDeleteClick?.invoke(it)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemPhotoBinding

        override fun bindView(itemView: View) {
            binding=ItemPhotoBinding.bind(itemView)

        }

    }
}


