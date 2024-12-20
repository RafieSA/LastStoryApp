package com.dicoding.picodiploma.loginwithanimation.view.addstory

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import com.dicoding.picodiploma.loginwithanimation.R
import com.dicoding.picodiploma.loginwithanimation.data.Result
import com.dicoding.picodiploma.loginwithanimation.data.pref.UserPreference
import com.dicoding.picodiploma.loginwithanimation.data.pref.dataStore
import com.dicoding.picodiploma.loginwithanimation.databinding.ActivityAddStoryBinding
import com.dicoding.picodiploma.loginwithanimation.view.ViewModelFactory
import com.dicoding.picodiploma.loginwithanimation.view.main.MainViewModel
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private lateinit var userPreference: UserPreference
    private var currentImageUri: Uri? = null
    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        userPreference = UserPreference.getInstance(dataStore)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.buttonGallery.setOnClickListener { startGallery() }
        binding.buttonAdd.setOnClickListener { uploadStory() }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun uploadStory() {
        currentImageUri?.let { uri ->
            val description = binding.edAddDescription.text.toString()
            if (description.isEmpty()) {
                Toast.makeText(this, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                return@let
            }

            val file = uriToFile(uri, this)
            val compressedFile = reduceFileImage(file)
            val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaType())
            val imageMultipart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "photo",
                compressedFile.name,
                requestImageFile
            )
            val descriptionRequestBody = description.toRequestBody("text/plain".toMediaType())

            lifecycleScope.launch {
                userPreference.getSession().collect { user ->
                    mainViewModel.uploadStory(user.token, imageMultipart, descriptionRequestBody)
                    mainViewModel.uploadResult.observe(this@AddStoryActivity) { result ->
                        if (result != null) {
                            when (result) {
                                is Result.Loading -> {}
                                is Result.Success -> {
                                    Toast.makeText(this@AddStoryActivity, "Story uploaded successfully", Toast.LENGTH_SHORT).show()
                                    val intent = Intent()
                                    setResult(RESULT_OK, intent)
                                    finish()
                                }
                                is Result.Error -> {
                                    val errorMessage = when (result.error) {
                                        "java.net.SocketTimeoutException: timeout" -> getString(R.string.timeout_error_message)
                                        else -> result.error
                                    }
                                    Toast.makeText(this@AddStoryActivity, errorMessage, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
            }
        } ?: run {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToFile(selectedImg: Uri, context: Context): File {
        val contentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
        val outputStream = FileOutputStream(myFile)
        val buf = ByteArray(1024)
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
        outputStream.close()
        inputStream.close()

        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile(timeStamp, ".jpg", storageDir)
    }


    private fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)?.getRotatedBitmap(file)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPicByteArray = bmpStream.toByteArray()
            streamLength = bmpPicByteArray.size
            compressQuality -= 5
        } while (streamLength > MAXIMAL_SIZE)
        bitmap?.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    private fun Bitmap?.getRotatedBitmap(file: File): Bitmap? {
        val orientation = ExifInterface(file).getAttributeInt(
            ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(this, 90F)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(this, 180F)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(this, 270F)
            ExifInterface.ORIENTATION_NORMAL -> this
            else -> this
        }
    }

    private fun rotateImage(source: Bitmap?, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return if (source != null) {
            Bitmap.createBitmap(
                source, 0, 0, source.width, source.height, matrix, true
            )
        } else {
            null
        }
    }

    private val timeStamp: String = System.currentTimeMillis().toString()

    companion object {
        private const val MAXIMAL_SIZE = 1000000
    }
}