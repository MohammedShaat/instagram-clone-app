package com.example.instagramcloneapp

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.instagramcloneapp.databinding.ActivityAddStoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding

    private lateinit var firebaseUser: FirebaseUser
    private var myUri: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        openImagePicker()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri

            uploadStory()
        }
    }

    private fun openImagePicker() {
        CropImage.activity()
            .setAspectRatio(9, 16)
            .start(this)
    }

    private fun uploadStory() = lifecycleScope.launch {
        if (imageUri == null) {
            Toast.makeText(applicationContext, "Upload a picture, please.", Toast.LENGTH_SHORT)
                .show()
            return@launch
        }

        val progressDialog = ProgressDialog(this@AddStoryActivity).apply {
            setCancelable(false)
            setTitle("Uploading story")
            setMessage("Your story is in progress. Please wait")
            show()
        }

        myUri = suspendCoroutine { continuation ->
            FirebaseStorage.getInstance().reference
                .child("Story Pictures")
                .child("${firebaseUser.uid}_${Calendar.getInstance().timeInMillis}")
                .putFile(imageUri!!).addOnCompleteListener { task ->
                    if (task.isSuccessful)
                        task.result.storage.downloadUrl.addOnSuccessListener { uri ->
                            continuation.resume(uri.toString())
                        }
                    else
                        continuation.resume(null)
                }
        }

        if (myUri == null) {
            progressDialog.dismiss()
            Toast.makeText(
                applicationContext,
                "Uploading failed, please try again.",
                Toast.LENGTH_SHORT
            )
                .show()
            return@launch
        }

        val storyRef = FirebaseDatabase.getInstance().reference
            .child("Stories")
            .child(firebaseUser.uid)
            .push()

        val story = hashMapOf<String, Any>()
        story["id"] = storyRef.key!!
        story["user_id"] = firebaseUser.uid
        story["image_url"] = myUri!!
        story["time_start"] = Calendar.getInstance().timeInMillis
        story["time_end"] =
            Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis

        storyRef.setValue(story).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                finish()
                Toast.makeText(
                    applicationContext,
                    "Your story has been uploaded successfully.",
                    Toast.LENGTH_SHORT
                )
                    .show()
                progressDialog.dismiss()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Failed to upload your story. Please try again",
                    Toast.LENGTH_SHORT
                )
                    .show()
                progressDialog.dismiss()
            }
        }
    }
}