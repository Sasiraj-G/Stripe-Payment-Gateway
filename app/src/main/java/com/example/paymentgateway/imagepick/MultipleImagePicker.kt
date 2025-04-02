package com.example.paymentgateway.imagepick


import com.example.paymentgateway.imagepick.testing.MainController
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_DRAG
import androidx.recyclerview.widget.ItemTouchHelper.DOWN

import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyRecyclerView

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.network.okHttpClient
import com.example.paymentgateway.Constants
import com.example.paymentgateway.RemoveListPhotosMutation
import com.example.paymentgateway.Step2ListDetailsQuery
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import com.example.paymentgateway.databinding.ActivityMultipleImagePickerBinding
import com.example.paymentgateway.imagepick.testing.MainCallbackAdapter
import com.example.paymentgateway.imagepick.testing.MainEpoxyTouchCallback
import com.example.paymentgateway.imagepick.testing.MainSimpleOnItemTouchListener
import com.example.paymentgateway.imagepick.testing.MainViewModel
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


class MultipleImagePicker : AppCompatActivity(),MainCallbackAdapter,MainSimpleOnItemTouchListener.OnInterceptTouchEventListener {
    lateinit var binding: ActivityMultipleImagePickerBinding
    private lateinit var apolloClient: ApolloClient
    val selectedImages = mutableListOf<Uri>()
     val uploadedImages = mutableListOf<String>()
    private var isLoading = false
    lateinit var callbackAdapter: MainCallbackAdapter

    private lateinit var controller: MainController
    private lateinit var itemTouchHelper: ItemTouchHelper

    private lateinit var viewModel: MainViewModel

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

        // setupRecyclerView()
        setupRecyclerViews() //new
        setupListeners()
        fetchExistingPhotos()

        // Initialize ViewModel
        viewModel = MainViewModel()
        loadExistingImages()

        // Initialize controller and setup RecyclerView
        setupRecyclerViews()

        // Observe ViewModel data
        observeViewModelData()


    }

    private fun observeViewModelData() {
        viewModel.firstRowsData.observe(this) { rows ->
            val firstSectionImages = rows.map { it.image }
                .filter { it.isNotEmpty() }
            updateImageSections(firstSectionImages, viewModel.secondRowsData.value?.map { it.image } ?: emptyList())
        }

        viewModel.secondRowsData.observe(this) { rows ->
            val secondSectionImages = rows.map { it.image }
                .filter { it.isNotEmpty() } // Only include non-empty image URLs
            updateImageSections(viewModel.firstRowsData.value?.map { it.image } ?: emptyList(), secondSectionImages)
        }
    }
    private fun updateImageSections(firstSectionImages: List<String>, secondSectionImages: List<String>) {

        uploadedImages.clear()
        uploadedImages.addAll(firstSectionImages.filter { it.isNotEmpty() })
        uploadedImages.addAll(secondSectionImages.filter { it.isNotEmpty() })

        controller.setData(uploadedImages, selectedImages)
    }
    private fun loadExistingImages() {
        uploadedImages.clear()
        uploadedImages.addAll(uploadedImages)
        updateViewModelWithImages(uploadedImages)
        controller.setData(uploadedImages, selectedImages)
    }



    private fun updateViewModelWithImages(images: List<String>) {

        val rows = images.mapIndexed { index, imageUrl ->
            MainViewModel.Row(
                id = imageUrl,
                title = "Image ${index + 1}",
                image = imageUrl
            )
        }

        viewModel.updateRows(rows)
    }


    private fun setupRecyclerViews() {
        val gridLayoutManager = GridLayoutManager(this, 2)
        binding.imageRecyclerView.layoutManager = gridLayoutManager

        // Initialize the controller
        controller = MainController(this, ::openImagePicker)

        binding.imageRecyclerView.adapter = controller.adapter //new line add
        binding.imageRecyclerView.setController(controller)

        val touchCallback = MainEpoxyTouchCallback(controller, object : MainEpoxyTouchCallback.OnRowMoveListener {
            override fun onMoved(movingRowId: String, shiftingRowId: String) {

                viewModel.moveRow(movingRowId, shiftingRowId)

                // Handle the move operation in local data too
                val movingIndex = uploadedImages.indexOfFirst { it == movingRowId }
                val shiftingIndex = uploadedImages.indexOfFirst { it == shiftingRowId }

                if (movingIndex != -1 && shiftingIndex != -1) {
                    val temp = uploadedImages[movingIndex]
                    uploadedImages.removeAt(movingIndex)
                    uploadedImages.add(shiftingIndex, temp)
                    controller.setData(uploadedImages, selectedImages)
                    controller.requestModelBuild()
                }
            }
        })


        itemTouchHelper = ItemTouchHelper(touchCallback)
        itemTouchHelper.attachToRecyclerView(binding.imageRecyclerView)
        binding.imageRecyclerView.addOnItemTouchListener(MainSimpleOnItemTouchListener(this))
        controller.setItemTouchHelper(itemTouchHelper)
        controller.setData(uploadedImages, selectedImages)

        controller.requestModelBuild() //new

    }


    override fun onDragStart() {
        binding.imageRecyclerView.isNestedScrollingEnabled = false
    }

    override fun onDragEnd() {
       binding.imageRecyclerView.isNestedScrollingEnabled = true
    }


    override fun onInterceptTouchEvent(touchedPosition: Int) {

        if (touchedPosition >= 0) {

        }
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
                       // setupRecyclerView()
                        setupRecyclerViews()
                        controller.requestModelBuild()  //new
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
        controller.setData(uploadedImages, selectedImages)
        controller.requestModelBuild()  //new
        setupRecyclerViews()
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

    fun deleteImage(imageUrl: String) {

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
//                    setupRecyclerView()
                    setupRecyclerViews()
                    controller.requestModelBuild()  //new
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

            }
        }



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

