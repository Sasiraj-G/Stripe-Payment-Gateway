package com.example.paymentgateway.imagepick

import android.view.View
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyHolder
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.EpoxyModelWithHolder
import com.bumptech.glide.Glide
import com.example.paymentgateway.R
import com.example.paymentgateway.databinding.ItemImageViewerBinding
import com.github.chrisbanes.photoview.PhotoView

@EpoxyModelClass
abstract class ImageViewerModels : EpoxyModelWithHolder<ImageViewerModels.Holder>() {

    @EpoxyAttribute
     var imageUri: String? = null
    @EpoxyAttribute
     var onImageLoadedListener: ((PhotoView) -> Unit)? = null

    override fun getDefaultLayout() = R.layout.item_image_viewer

    override fun createNewHolder(): Holder = Holder()

    override fun bind(holder: Holder) {
        super.bind(holder)
        imageUri?.let { uri ->
            Glide.with(holder.binding.photoView.context)
                .load(uri)
                .placeholder(R.drawable.progress_animation)
                .into(holder.binding.photoView)

            onImageLoadedListener?.invoke(holder.binding.photoView)
        }
    }

    class Holder : EpoxyHolder() {
        lateinit var binding: ItemImageViewerBinding

        override fun bindView(itemView: View) {
            binding= ItemImageViewerBinding.bind(itemView)

        }
    }
}