package com.example.instagramcloneapp

import android.R.attr
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.instagramcloneapp.databinding.ActivityAccountSettingsBinding
import com.example.instagramcloneapp.module.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


private const val TAG = "AccountSettingsActivity"

class AccountSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSettingsBinding
    private lateinit var firebaseUser: FirebaseUser
    private var myUri: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        getUserInfo()

        val logoutBtn = binding.logoutBtn
        logoutBtn.setOnClickListener {
            logout()
        }

        binding.closeProfileBtn.setOnClickListener {
            finish()
        }

        binding.saveInfoProfileBtn.setOnClickListener {
            save()
        }

        binding.changeImageTextBtn.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this)
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val result = CropImage.getActivityResult(data)
            imageUri = result.uri

            Picasso.get().load(imageUri).placeholder(R.drawable.profile)
                .into(binding.profileImageView)
        }
    }


    private fun logout() {
        val auth = FirebaseAuth.getInstance()
        auth.signOut()

        val intent = Intent(this, SignInActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }

    private fun getUserInfo() {
        val followersRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(firebaseUser.uid)

        followersRef.addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) return
                    val user = snapshot.getValue(User::class.java) ?: return
                    binding.apply {
                        fullNameProfileFrag.setText(user.fullname)
                        usernameProfileFrag.setText(user.username)
                        bioProfileFrag.setText(user.bio)
                        Picasso.get().load(user.image).placeholder(R.drawable.profile)
                            .into(profileImageView)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

    private fun save() = lifecycleScope.launch {
        val progressDialog = ProgressDialog(this@AccountSettingsActivity).apply {
            setCancelable(false)
            setTitle("Update profile")
            setMessage("Profile is updating. Please wait")
            show()
        }

        val errorMessage = binding.errorMessageProfileFrag.apply { isVisible = false }
        val fullName = binding.fullNameProfileFrag.text.toString()
        val username = binding.usernameProfileFrag.text.toString()
        val bio = binding.bioProfileFrag.text.toString()

        // check if any field is empty
        if (fullName.isBlank() || username.isBlank() || bio.isBlank()) {
            errorMessage.isVisible = true
            progressDialog.dismiss()
            return@launch
        }

        // upload new image
        myUri = suspendCoroutine { continuation ->
            if (imageUri == null) {
                continuation.resume(null)
                return@suspendCoroutine
            }
            Log.i(TAG, "uploading image")
            FirebaseStorage.getInstance().reference
                .child("Profile Pictures")
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


        Log.i(TAG, "updating info")
        // Update user info
        val userRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(firebaseUser.uid)

        val updatedData = mutableMapOf<String, String>()
        updatedData["fullname"] = fullName
        updatedData["username"] = username
        updatedData["bio"] = bio
        myUri?.let {
            updatedData["image"] = it
        }

        userRef.updateChildren(updatedData.toMap()).addOnCompleteListener {
            finish()
            progressDialog.dismiss()
        }
    }
}