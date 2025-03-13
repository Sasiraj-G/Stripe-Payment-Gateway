package com.example.paymentgateway

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.paymentgateway.databinding.ActivityCropImageBinding
import com.soundcloud.android.crop.Crop
import java.io.File

class CropImage : AppCompatActivity() {
    private lateinit var binding: ActivityCropImageBinding
    private var resultHandler: ActivityResultLauncher<Intent>? = null

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 101
        private var REQUEST_CODE_PICK_IMAGE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCropImageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val permissions = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                REQUEST_CODE_PERMISSIONS
            )
        }

        val imageContract = registerForActivityResult(ActivityResultContracts.GetContent()){
            binding.imageView.setImageURI(it)

        }


       binding.btnSelectImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)

//           startActivityForResults(intent, REQUEST_CODE_PICK_IMAGE)

//           imageContract.launch("image/*")


        }


    }

    fun startActivityForResults(intent: Intent, requestCode: Int)
    {
        resultHandler?.launch(intent)
        REQUEST_CODE_PICK_IMAGE = requestCode

    }

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
            }
        }
    }




    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                Crop.of(uri, Uri.fromFile(File(cacheDir, "cropped")))
                    .asSquare()
                    .start(this)
            }
        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            val croppedUri = Crop.getOutput(data)
            if (croppedUri != null) {
//                val bitmaps = MediaStore.Images.Media.getBitmap(this.contentResolver, croppedUri)
                val bitmap =  ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.contentResolver, croppedUri))
                binding.imageView.setImageBitmap(bitmap)
                saveImageToGallery(bitmap)
            }
        }
    }




    private fun saveImageToGallery(bitmap: Bitmap) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "Image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MyApp")
        }
        val contentResolver = contentResolver
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri).use { outputStream ->
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to save image to gallery", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}


