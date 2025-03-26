package com.example.paymentgateway.imagepick

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

import com.example.paymentgateway.databinding.ActivityMultipleImagePickerBinding
import kotlin.math.max


class MultipleImagePicker : AppCompatActivity() {
    private lateinit var binding: ActivityMultipleImagePickerBinding
    private val selectedImages = mutableListOf<Uri>()

    private val MAX_IMAGES = 10



    private val imagePickerLauncher = registerForActivityResult(

        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newImages = mutableListOf<Uri>()
            val singleImage = mutableListOf<Uri>()

            result.data?.clipData?.let { clipData ->
                val itemCount = minOf(clipData.itemCount, MAX_IMAGES)

                for (i in 0 until itemCount) {    //    for (i in 0 until clipData.itemCount)
                    val imageUri = clipData.getItemAt(i).uri
                    Log.d("MultipleImagePicker", "Selected image URI: $imageUri")
                    newImages.add(imageUri)

                }
            }

            result.data?.data?.let { singleImageUri ->
                singleImage.add(singleImageUri)
            }


            // Add all selected images
            selectedImages.addAll(newImages)
            selectedImages.addAll(singleImage)


            setupRecyclerView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultipleImagePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
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

