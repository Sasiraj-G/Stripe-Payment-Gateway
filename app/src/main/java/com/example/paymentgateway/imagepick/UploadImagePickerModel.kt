package com.example.paymentgateway.imagepick

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.example.paymentgateway.R


import com.example.paymentgateway.databinding.ItemUploadServerBinding
import com.example.paymentgateway.imagepick.ImagePickerModel.Holder

@EpoxyModelClass
abstract class UploadImagePickerModel : EpoxyModelWithHolder<UploadImagePickerModel.Holder>() {


    @EpoxyAttribute
    lateinit var imageUrl : String

    @EpoxyAttribute
    var onDeleteClick: ((View) -> Unit)? = null

    @EpoxyAttribute
    lateinit var rowId: String   //new
    @EpoxyAttribute
    lateinit var title: String   //new
    @EpoxyAttribute
    var onDragHandleTouchListener: View.OnTouchListener? = null  //new

    @EpoxyAttribute
    lateinit var gridId: String //new

    @EpoxyAttribute
    var itemId: String? = null

    @EpoxyAttribute
    var onImageClick: ((View) -> Unit)? = null

    override fun getDefaultLayout() = R.layout.item_upload_server


    @SuppressLint("ClickableViewAccessibility")
    override fun bind(holder: Holder) {
        Glide.with(holder.binding.root.context)
            .load(imageUrl)
            .placeholder(android.R.drawable.progress_indeterminate_horizontal)
            .error(android.R.drawable.stat_notify_error)
            .into(holder.binding.imageView)

        holder.binding.deleteImage.setOnClickListener {
            onDeleteClick?.invoke(it)
        }
        holder.binding.cardImageContainer.setOnClickListener {
            onImageClick?.invoke(it)

        }
        holder.binding.cardImageContainer.setOnTouchListener(onDragHandleTouchListener)   //new
    }



    @SuppressLint("ClickableViewAccessibility")
    override fun unbind(holder: Holder) {
        with(holder) {
            binding.cardImageContainer.setOnTouchListener(null)
        }
    }





    class Holder : EpoxyHolder() {
        lateinit var binding: ItemUploadServerBinding
        override fun bindView(itemView: View) {
            binding= ItemUploadServerBinding.bind(itemView)
           itemView.tag=  "draggable"

        }

    }
}


