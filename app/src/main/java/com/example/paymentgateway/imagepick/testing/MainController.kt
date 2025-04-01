package com.example.paymentgateway.imagepick.testing

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.example.paymentgateway.imagepick.ImageViewer
import com.example.paymentgateway.imagepick.MultipleImagePicker
import com.example.paymentgateway.imagepick.imagePicker
import com.example.paymentgateway.imagepick.imagePlaceholder
import com.example.paymentgateway.imagepick.uploadImagePicker

class MainController(
    private val activity: MultipleImagePicker,
    private val onPlaceholderClick: () -> Unit
) : EpoxyController() {
    private var uploadedImages: List<String> = emptyList()
    private var selectedImages: List<Uri> = emptyList()
    private var itemTouchHelper: ItemTouchHelper? = null
    fun setData(uploadedImages: List<String>, selectedImages: List<Uri>) {
        this.uploadedImages = uploadedImages
        this.selectedImages = selectedImages
        requestModelBuild()
    }
    fun setItemTouchHelper(itemTouchHelper: ItemTouchHelper) {
        this.itemTouchHelper = itemTouchHelper
    }
    @SuppressLint("ClickableViewAccessibility")
    override fun buildModels() {
        imagePlaceholder {
            id("placeholder")
            onPlaceholderClick { _ -> onPlaceholderClick() }
        }
        selectedImages.forEachIndexed { index, imageUri ->
            imagePicker {
                id("upload" + index)
                imageUri(imageUri)
                onClick { _ ->
                    val intent = Intent(activity, ImageViewer::class.java)
                    val imageUriStrings = uploadedImages.map { it.toString() }
                    intent.putStringArrayListExtra(
                        ImageViewer.EXTRA_IMAGE_URIS,
                        ArrayList(imageUriStrings)
                    )
                    intent.putExtra(ImageViewer.EXTRA_INITIAL_POSITION, index)
                    activity.startActivity(intent)
                }
                onDeleteClick { _ ->
                    activity.selectedImages.removeAt(index)
                }


            }
        }
        uploadedImages.forEachIndexed { index, imageUrl ->
            uploadImagePicker {
                id("uploaded" + index)
                imageUrl("https://staging1.flutterapps.io/images/upload/" + imageUrl)
                rowId(imageUrl)
                gridId("main_grid")
                title("Image ${index + 1}")
                onImageClick { _ ->
                    val intent = Intent(activity, ImageViewer::class.java)
                    val imageUriStrings = uploadedImages.map { it.toString() }
                    intent.putStringArrayListExtra(ImageViewer.EXTRA_IMAGE_URIS, ArrayList(imageUriStrings))
                    intent.putExtra(ImageViewer.EXTRA_INITIAL_POSITION, index)
                    activity.startActivity(intent)
                }
                onDeleteClick { _ ->
                    activity.uploadedImages.removeAt(index)
                    activity.deleteImage(imageUrl)
                }

                onDragHandleTouchListener { view, event ->
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        val holder = activity.binding.imageRecyclerView.findContainingViewHolder(view)
                        if (holder != null) {
                            itemTouchHelper?.startDrag(holder)
                        }
                    }
                    false
                }


            }
        }

    }
    fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        itemTouchHelper?.startDrag(viewHolder)
        activity.callbackAdapter.onDragStart()
    }
    fun onDragStarted() {
        activity.callbackAdapter.onDragStart()
    }
    fun onDragEnded() {
        activity.callbackAdapter.onDragEnd()
    }


    }

