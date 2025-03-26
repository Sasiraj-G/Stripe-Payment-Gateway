package com.example.paymentgateway.imagepick

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
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
import com.example.paymentgateway.uploadServer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor


import java.io.File
import java.io.IOException


class MultipleImagePicker : AppCompatActivity() {
    private lateinit var binding: ActivityMultipleImagePickerBinding
    private lateinit var apolloClient: ApolloClient


    private val selectedImages = mutableListOf<Uri>()
    private val uploadedImages = mutableListOf<String>()



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

            lifecycleScope.launch {
                try {
                    val response = apolloClient.query(Step2ListDetailsQuery(listId = "1973", listIdInt = 1973, preview = Optional.present(false))).execute()
                    response.data?.showListPhotos?.results?.let { photos ->
                        // Process existing photos
                        val existingImageUrls = photos.mapNotNull { it?.name }
                        Log.d("TEST", "Existing image URLs: $existingImageUrls")
                        uploadedImages.addAll(existingImageUrls)
                        setupRecyclerView()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MultipleImagePicker,
                        "Error fetching existing photos",
                        Toast.LENGTH_SHORT).show()
                    Log.d("TEST", "Error fetching existing photos: ${e.message}")
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

    }


    private fun setupListeners() {
        binding.nextButton.setOnClickListener {
            if (selectedImages.isNotEmpty()) {
              uploadImagesToServer()
           //     uploadSelectedImages()
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

        // Execute the request
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
        //val tempFile = File.createTempFile("image", ".jpg", cacheDir)
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
        binding.imageRecyclerView.withModels {

                imagePlaceholder {
                    id("placeholder")
                    onPlaceholderClick { _ -> openImagePicker() }
                }
            selectedImages.forEachIndexed { index, imageUri ->
                imagePicker {
                    id(index)
                    imageUri(imageUri)

                    onDeleteClick { _ ->
                        selectedImages.removeAt(index)
                        setupRecyclerView()
                    }
                }
            }
            uploadedImages.forEachIndexed { index, imageUrl ->
                uploadImagePicker {
                    id("upload"+index)
                    imageUrl("https://staging1.flutterapps.io/images/upload/"+imageUrl)
                    onDeleteClick { _ ->
                        uploadedImages.removeAt(index)
                        deleteImage(imageUrl)
                        setupRecyclerView()
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

