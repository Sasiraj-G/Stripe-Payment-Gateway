package com.example.paymentgateway.imagepick

import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.carousel
import com.github.chrisbanes.photoview.PhotoView

class ImagePagerController(
    private val imageUris: List<String>,
    private val onImageLoadedCallback: ((PhotoView) -> Unit)? = null
) : EpoxyController() {

    override fun buildModels() {
        imageUris.forEachIndexed { index, imageUri ->
            imageViewerModels {
                id("image_$index")
                imageUri("https://staging1.flutterapps.io/images/upload/"+imageUri)
                onImageLoadedCallback?.let { callback ->
                    onImageLoadedCallback
                }
            }
        }

    }
}