package com.example.paymentgateway.imagepick

import android.net.Uri
import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.example.paymentgateway.R


import com.example.paymentgateway.databinding.ItemUploadServerBinding

@EpoxyModelClass
abstract class UploadImagePickerModel : EpoxyModelWithHolder<UploadImagePickerModel.Holder>() {


    @EpoxyAttribute
    lateinit var imageUrl : String

    @EpoxyAttribute
    var onDeleteClick: ((View) -> Unit)? = null

    override fun getDefaultLayout() = R.layout.item_upload_server

    override fun bind(holder: Holder) {

        Glide.with(holder.binding.root.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.imageView)

        holder.binding.deletePic.setOnClickListener {
            onDeleteClick?.invoke(it)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemUploadServerBinding
        override fun bindView(itemView: View) {
            binding= ItemUploadServerBinding.bind(itemView)

        }

    }
}


