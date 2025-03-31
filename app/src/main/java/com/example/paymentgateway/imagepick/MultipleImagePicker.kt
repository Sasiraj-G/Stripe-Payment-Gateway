package com.example.paymentgateway.imagepick


import android.animation.AnimatorSet
import android.animation.ObjectAnimator

import android.app.Activity
import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View

import android.view.animation.AccelerateDecelerateInterpolator


import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts

import androidx.appcompat.app.AppCompatActivity

import androidx.core.animation.doOnEnd
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.DOWN

import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyControllerAdapter
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyRecyclerView
import com.airbnb.epoxy.EpoxyTouchHelper
import com.airbnb.epoxy.preload.addEpoxyPreloader

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import com.example.paymentgateway.Constants
import com.example.paymentgateway.R
import com.example.paymentgateway.RemoveListPhotosMutation
import com.example.paymentgateway.Step2ListDetailsQuery
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import com.example.paymentgateway.databinding.ActivityMultipleImagePickerBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor


import java.io.File
import java.io.IOException
import java.util.Collections
import kotlin.math.abs


class MultipleImagePicker : AppCompatActivity() {
    private lateinit var binding: ActivityMultipleImagePickerBinding
    private lateinit var apolloClient: ApolloClient
    private val selectedImages = mutableListOf<Uri>()
    private val uploadedImages = mutableListOf<String>()
    private var isLoading = false

    var COVER_PHOTO_INDEX =0

    private val okHttpClient by lazy {
        OkHttpClient.Builder().build()
    }


    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleImageSelection(result.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultipleImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor =
            Interceptor { chain ->
                val original = chain.request()
                val builder = original.newBuilder()
                    .addHeader("auth", Constants.AUTH_TOKEN)
                    .method(original.method, original.body)
                chain.proceed(builder.build())
            }
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        apolloClient = ApolloClient.Builder()
            .serverUrl("https://staging1.flutterapps.io/api/graphql")
            .okHttpClient(okHttpClient)
            .build()

        setupRecyclerView()
        setupListeners()
        fetchExistingPhotos()

    }


    private fun fetchExistingPhotos() {
        Log.d("TEST","fff")
        if (isLoading) return

        isLoading = true
        binding.progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                try {
                    val response = apolloClient.query(Step2ListDetailsQuery(listId = "1973", listIdInt = 1973, preview = Optional.present(false))).execute()
                    response.data?.showListPhotos?.results?.let { photos ->
                        // Process existing photos
                        val existingImageUrls = photos.mapNotNull { it?.name }
                        Log.d("TEST", "Existing image URLs: $existingImageUrls")
                        uploadedImages.addAll(existingImageUrls)
                        setupRecyclerView()
//                        epoxyController.requestModelBuild()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MultipleImagePicker,
                        "Error fetching existing photos",
                        Toast.LENGTH_SHORT).show()
                    Log.d("TEST", "Error fetching existing photos: ${e.message}")
                }finally {
                    isLoading = false
                    binding.progressBar.visibility = View.GONE
                }
            }

    }

    private fun handleImageSelection(data: Intent?){
        val newImages = mutableListOf<Uri>()
        val singleImage = mutableListOf<Uri>()

        data?.clipData?.let { clipData ->
            val itemCount = minOf(clipData.itemCount, Constants.MAX_IMAGES)

            for (i in 0 until itemCount) {    //    for (i in 0 until clipData.itemCount)
                val imageUri = clipData.getItemAt(i).uri
                Log.d("MultipleImagePicker", "Selected image URI: $imageUri")
                newImages.add(imageUri)
            }
        }

        data?.data?.let { singleImageUri ->
            singleImage.add(singleImageUri)
        }

        // Add all selected images
        uploadImagesToServer()
        selectedImages.addAll(newImages)
        selectedImages.addAll(singleImage)
        setupRecyclerView()
        // Rebuild models
//        epoxyController.requestModelBuild()

    }


    private fun setupListeners() {
        binding.nextButton.setOnClickListener {
            if (selectedImages.isNotEmpty()) {
              uploadImagesToServer()

            } else {
                Toast.makeText(this, "Please select at least one image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadImagesToServer() {

     //   binding.progressBar.visibility = View.VISIBLE
        binding.nextButton.isEnabled = false

        lifecycleScope.launch {
            try {
                val listId = 1973
                val uploadResults = uploadImagesMultipart(listId, selectedImages)

                withContext(Dispatchers.Main) {
                    if (uploadResults) {
                        Toast.makeText(this@MultipleImagePicker, "Images uploaded successfully", Toast.LENGTH_SHORT).show()

                    } else {
                        Toast.makeText(this@MultipleImagePicker, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MultipleImagePicker, "Error uploading images: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                withContext(Dispatchers.Main) {
                  //  binding.progressBar.visibility = View.GONE
                    binding.nextButton.isEnabled = true
                }
            }
        }
    }

    private suspend fun uploadImagesMultipart(listId: Int, imageUris: List<Uri>): Boolean = withContext(Dispatchers.IO) {

        try{
        val multipartBodyBuilder = MultipartBody.Builder().setType(MultipartBody.FORM)
        // Add list ID to the request
        multipartBodyBuilder.addFormDataPart("listId", listId.toString())

        // Add each image as a part of the multipart request
        imageUris.forEachIndexed { index, uri ->
            val file = createTempFileFromUri(uri)
            val requestBody = file.asRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull() ?: "image/*".toMediaTypeOrNull())
            multipartBodyBuilder.addFormDataPart(
                "file",
                file.name,
                requestBody
            )
            Log.d("ImageUpload",file.name)
        }

        // Build the request
        val request = Request.Builder()
            .url(Constants.UPLOAD_IMAGE_URL)
            .post(multipartBodyBuilder.build())
            .addHeader("auth", "${Constants.AUTH_TOKEN}")
            .addHeader("Content-Type", "multipart/form-data")
            .build()


            val response = okHttpClient.newCall(request).execute()

            // Log full response details
            Log.d("ImageUpload", "Response Code: ${response.code}")
            Log.d("ImageUpload", "Response Message: ${response.message}")

            // Read response body
            val responseBody = response.body?.string()
            Log.d("ImageUpload", "Response Body: $responseBody")
            //response.isSuccessful
            when (response.code) {
                 200 -> {
                    Log.d("ImageUpload", "Upload successful")
                    true
                }
                else -> {
                    Log.e("ImageUpload", "Unexpected response: ${response.code}")
                    false
                }
            }


        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun createTempFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)

        val tempFile = File(cacheDir, "temp_upload_image_${System.currentTimeMillis()}.jpg")
        tempFile.deleteOnExit()

        inputStream?.use { input ->
            java.io.FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }

        return tempFile
    }

    private fun deleteImage(imageUrl: String) {

        lifecycleScope.launch {
            try {
                val response = apolloClient.mutation(
                    RemoveListPhotosMutation(
                        listId = 1973,
                        name = Optional.present(imageUrl)
                    )
                ).execute()
                val mutationResult = response.data?.removeListPhotos
                Log.d("TESTING", "mutationResult: $mutationResult")
                if (mutationResult?.status == 200) {
                    uploadedImages.remove(imageUrl)
                    setupRecyclerView()
//                    epoxyController.requestModelBuild()
                    Toast.makeText(this@MultipleImagePicker, "Image deleted successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MultipleImagePicker, mutationResult?.errorMessage ?: "Failed to delete image", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MultipleImagePicker, "Error deleting image: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }



    private fun setupRecyclerView() {

        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.imageRecyclerView.layoutManager = gridLayoutManager

        (binding.imageRecyclerView as? EpoxyRecyclerView)?.let { recyclerView ->
            recyclerView.withModels {

                imagePlaceholder {
                    id("placeholder")
                    onPlaceholderClick { _ -> openImagePicker() }
                }
                selectedImages.forEachIndexed { index, imageUri ->
                    imagePicker {
                        id("upload"+index)
                        imageUri(imageUri)


                        onClick {
                            val intent = Intent(this@MultipleImagePicker, ImageViewer::class.java)
                            val imageUriStrings = uploadedImages.map { it.toString() }
                            intent.putStringArrayListExtra(
                                ImageViewer.EXTRA_IMAGE_URIS,
                                ArrayList(imageUriStrings)
                            )
                            intent.putExtra(ImageViewer.EXTRA_INITIAL_POSITION, index)

                            startActivity(intent)

                        }
                        onDeleteClick { _ ->
                            selectedImages.removeAt(index)
                            setupRecyclerView()
                        }

                    }
                }
                uploadedImages.forEachIndexed { index, imageUrl ->
                    uploadImagePicker {
                        id( index)
                        imageUrl("https://staging1.flutterapps.io/images/upload/" + imageUrl)
                        onImageClick {
                            val intent = Intent(this@MultipleImagePicker, ImageViewer::class.java)
                            val imageUriStrings = uploadedImages.map { it.toString() }
                            intent.putStringArrayListExtra(
                                ImageViewer.EXTRA_IMAGE_URIS,
                                ArrayList(imageUriStrings)
                            )
                            intent.putExtra(ImageViewer.EXTRA_INITIAL_POSITION, index)

                            startActivity(intent)
                        }
                        onDeleteClick { _ ->
                            uploadedImages.removeAt(index)
                            deleteImage(imageUrl)
                            setupRecyclerView()
                        }

                    }

                }

              //  setupDragAndDrop(recyclerView)

//            enableFlexibleDragDrop(
//                recyclerView = binding.imageRecyclerView,
//                selectedImages = uploadedImages,
//                onDragComplete = { fromPosition, toPosition ->

//                    Log.d("DragDrop", "Moved from $fromPosition to $toPosition")
//                    setupRecyclerView()
//                }
//            )

//            enableDragDrop(
//                recyclerView = binding.imageRecyclerView,
//                selectedImages = uploadedImages,
//                onDragComplete = { fromPosition, toPosition ->
//                    Log.d("DragDrop", "Moved from $fromPosition to $toPosition")
//
//                    setupRecyclerView()
//                }
//            )
                setupDragAndDrop(recyclerView)

                // Setup drag and drop with EpoxyTouchHelper


                // Smooth scroll transformation
//            binding.imageRecyclerView.doOnPreDraw {
//                binding.imageRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        transformScrollItems(recyclerView)
//                    }
//                })
//            }
            }
        }



    }

    fun EpoxyController.enableDragDrop(
        recyclerView: RecyclerView,
        selectedImages: MutableList<String>,
        onDragComplete: ((fromPosition: Int, toPosition: Int) -> Unit)? = null
    ) {
        val dragDropCallback = object : ItemTouchHelper.Callback() {
            private var draggedItem: RecyclerView.ViewHolder? = null

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Only allow dragging for image picker models
                return if (viewHolder.itemView.tag == "draggable") {
                    val dragFlags = UP or
                            DOWN or
                            ItemTouchHelper.LEFT or
                            ItemTouchHelper.RIGHT
                    makeMovementFlags(dragFlags, 0)
                } else {
                    makeMovementFlags(0, 0)
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewHolder.itemView.tag != "draggable" ||
                    target.itemView.tag != "draggable") {
                    return false
                }

                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                if (fromPosition < 0 || fromPosition >= selectedImages.size ||
                    toPosition < 0 || toPosition >= selectedImages.size) {
                    return false
                }
                try {
                    Collections.swap(selectedImages, fromPosition, toPosition)
                    requestModelBuild()

                } catch (e: IndexOutOfBoundsException) {
                    return false
                }
              //  recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                requestModelBuild()
                onDragComplete?.invoke(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swiping implementation
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                viewHolder?.let { holder ->
                    when (actionState) {
                        ItemTouchHelper.ACTION_STATE_DRAG -> {

                            holder.itemView.animate()
                                .scaleX(0.9f)
                                .scaleY(0.9f)
                                .translationZ(16f)
                                .setDuration(200)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .start()
                        }
                        ItemTouchHelper.ACTION_STATE_IDLE -> {
                            // Reset view after drag
                            holder.itemView.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .translationZ(0f)
                                .setDuration(200)
                                .setInterpolator(AccelerateDecelerateInterpolator())
                                .start()
                        }
                    }
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                // Restore original state with animation
                val scaleXAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.1f, 1f)
                val scaleYAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.1f, 1f)
                val elevationAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "elevation", 16f, 0f)

                val animatorSet = AnimatorSet().apply {
                    playTogether(scaleXAnimator, scaleYAnimator, elevationAnimator)
                    duration = 200
                    interpolator = AccelerateDecelerateInterpolator()
                    doOnEnd {
                        // Ensure we reset the view completely
                        viewHolder.itemView.scaleX = 1f
                        viewHolder.itemView.scaleY = 1f
                        viewHolder.itemView.elevation = 0f
                    }
                }
                animatorSet.start()
            }
            override fun onMoved(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                fromPos: Int,
                target: RecyclerView.ViewHolder,
                toPos: Int,
                x: Int,
                y: Int
            ) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }

        val touchHelper = ItemTouchHelper(dragDropCallback)
        touchHelper.attachToRecyclerView(recyclerView)
    }

    fun EpoxyController.enableFlexibleDragDrop(
        recyclerView: RecyclerView,
        selectedImages: MutableList<String>,
        onDragComplete: ((fromPosition: Int, toPosition: Int) -> Unit)? = null
    ) {
        val dragDropCallback = object : ItemTouchHelper.Callback() {
            // Track the original position to ensure correct repositioning
            private var draggedItemOriginalPosition: Int = RecyclerView.NO_POSITION
            private var targetPosition: Int = RecyclerView.NO_POSITION

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return if (viewHolder.itemView.tag == "draggable") {
                    val dragFlags = UP or
                            DOWN or
                            ItemTouchHelper.LEFT or
                            ItemTouchHelper.RIGHT
                    makeMovementFlags(dragFlags, 0)
                } else {
                    makeMovementFlags(0, 0)
                }
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                viewHolder?.let {
                    if (actionState == ACTION_STATE_DRAG) {

                        draggedItemOriginalPosition = it.adapterPosition

                        // Apply scaling and elevation animation
                        val scaleXAnimator = ObjectAnimator.ofFloat(it.itemView, "scaleX", 1f, 1.1f)
                        val scaleYAnimator = ObjectAnimator.ofFloat(it.itemView, "scaleY", 1f, 1.1f)
                        val elevationAnimator = ObjectAnimator.ofFloat(it.itemView, "elevation", 0f, 16f)

                        val animatorSet = AnimatorSet().apply {
                            playTogether(scaleXAnimator, scaleYAnimator, elevationAnimator)
                            duration = 200
                            interpolator = AccelerateDecelerateInterpolator()
                        }
                        animatorSet.start()
                    }
                }
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // Ensure we're only moving draggable items
                if (viewHolder.itemView.tag != "draggable" ||
                    target.itemView.tag != "draggable") {
                    return false
                }

                // Get the positions
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition

                // Update the target position
                targetPosition = toPosition

                if (fromPosition < 0 || fromPosition >= selectedImages.size ||
                    toPosition < 0 || toPosition >= selectedImages.size) {
                    return false
                }

                try {
                   // Collections.swap(selectedImages, fromPosition, toPosition)
                    val removedItem = selectedImages.removeAt(fromPosition)
                    selectedImages.add(toPosition, removedItem)
                    requestModelBuild()

                } catch (e: IndexOutOfBoundsException) {
                    return false
                }

                requestModelBuild()


                onDragComplete?.invoke(fromPosition, toPosition)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)


                val scaleXAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "scaleX", 1.1f, 1f)
                val scaleYAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "scaleY", 1.1f, 1f)
                val elevationAnimator = ObjectAnimator.ofFloat(viewHolder.itemView, "elevation", 16f, 0f)

                val animatorSet = AnimatorSet().apply {
                    playTogether(scaleXAnimator, scaleYAnimator, elevationAnimator)
                    duration = 200
                    interpolator = AccelerateDecelerateInterpolator()
                    doOnEnd {

                        viewHolder.itemView.scaleX = 1f
                        viewHolder.itemView.scaleY = 1f
                        viewHolder.itemView.elevation = 0f
                    }
                }
                animatorSet.start()
            }

            override fun onMoved(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                fromPos: Int,
                target: RecyclerView.ViewHolder,
                toPos: Int,
                x: Int,
                y: Int
            ) {
                super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)


                viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }


        }

        // Attach the touch helper to the RecyclerView
        val touchHelper = ItemTouchHelper(dragDropCallback)
        touchHelper.attachToRecyclerView(recyclerView)
    }


    private fun transformScrollItems(recyclerView: RecyclerView) {
        val centerX = recyclerView.width / 2f
        val centerY = recyclerView.height / 2f

        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val childCenterX = child.x + child.width / 2f
            val childCenterY = child.y + child.height / 2f

            // Calculate distance from center
            val distanceX = abs(centerX - childCenterX)
            val distanceY = abs(centerY - childCenterY)
            val maxDistance = recyclerView.width.coerceAtLeast(recyclerView.height) / 2f

            // Calculate scale and rotation based on distance
            val scale = 1f - (maxDistance.coerceAtMost(distanceX.coerceAtLeast(distanceY)) / maxDistance) * 0.2f
            val rotation = (centerX - childCenterX) * 0.1f

            child.scaleX = scale
            child.scaleY = scale
            child.rotation = rotation
        }
    }

    private fun setupDragAndDrop(recyclerView: EpoxyRecyclerView) {
        val touchCallback = object : ItemTouchHelper.Callback() {
            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Enable drag only for non-first items
                val dragFlags = if (viewHolder.adapterPosition > 0) {
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN
                } else {
                    0
                }
                return makeMovementFlags(dragFlags, 0)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                if (fromPosition == 0 || toPosition == 0) return false


                val adjustedFromPosition = fromPosition - 1
                val adjustedToPosition = toPosition - 1

                try {
                    Collections.swap(uploadedImages, fromPosition, toPosition)

                } catch (e: IndexOutOfBoundsException) {
                    Toast.makeText(this@MultipleImagePicker, "Invalid move", Toast.LENGTH_SHORT).show()
                    return false
                }
                val movedImage = uploadedImages.removeAt(adjustedFromPosition)
                uploadedImages.add(adjustedToPosition, movedImage)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

            }

            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun canDropOver(
                recyclerView: RecyclerView,
                current: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return current.bindingAdapterPosition > 0 && target.bindingAdapterPosition > 0
            }
        }

        // Create and attach the ItemTouchHelper
        val itemTouchHelper = ItemTouchHelper(touchCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }








    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        imagePickerLauncher.launch(intent)
    }

}

