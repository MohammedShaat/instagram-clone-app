package com.example.instagramcloneapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.instagramcloneapp.databinding.ActivityAddPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val TAG = "AddPostActivity"

class AddPostActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPostBinding
    private lateinit var firebaseUser: FirebaseUser
    private var myUri: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        binding.closeAddPostBtn.setOnClickListener {
            finish()
        }

        binding.saveInfoAddPostBtn.setOnClickListener {
            post()
        }

        binding.uploadImageTextBtn.setOnClickListener {
            openImagePicker()
        }

        openImagePicker()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri

            binding.imageAddPost.setImageURI(imageUri)
        }
    }

    private fun openImagePicker() {
        CropImage.activity()
            .start(this)
    }

    private fun post() = lifecycleScope.launch {
        if (imageUri == null) {
            Toast.makeText(applicationContext, "Upload a picture, please.", Toast.LENGTH_SHORT)
                .show()
            return@launch
        }

        val progressDialog = ProgressDialog(this@AddPostActivity).apply {
            setCancelable(false)
            setTitle("Adding new post")
            setMessage("Your post is in progress. Please wait")
            show()
        }

        myUri = suspendCoroutine { continuation ->
            Log.i(TAG, "Uploading image: $imageUri")
            FirebaseStorage.getInstance().reference
                .child("Post Pictures")
                .child("${firebaseUser.uid}_${Calendar.getInstance().timeInMillis}")
                .putFile(imageUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        task.result.storage.downloadUrl.addOnSuccessListener { uri ->
                            continuation.resume(uri.toString())
                        }
                    else
                        continuation.resume(null)
                }
            Log.i(TAG, "Finished Uploading image")
        } ?: return@launch
        Log.i(TAG, "myUri=$myUri")

        val post = hashMapOf<String, String>()
        post["image"] = myUri!!
        post["caption"] = binding.captionAddPost.text.toString()
        post["uid"] = firebaseUser.uid

        val postsRef = FirebaseDatabase.getInstance().reference
            .child("Posts")
            .push()
            .setValue(post)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    finish()
                    Toast.makeText(
                        applicationContext,
                        "Your post has been published.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    progressDialog.dismiss()
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Failed to publish your post. Please try again",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    progressDialog.dismiss()
                }
            }
    }
}