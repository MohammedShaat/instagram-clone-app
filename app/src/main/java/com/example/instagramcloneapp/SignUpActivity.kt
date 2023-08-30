package com.example.instagramcloneapp

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.instagramcloneapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val signinLinkBtn = binding.signinLinkBtn
        signinLinkBtn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }

        val signupBtn = binding.signupBtn
        signupBtn.setOnClickListener {
            createAccount()
        }
    }

    private fun createAccount() {
        val fullName = binding.fullNameSignup.text.toString()
        val username = binding.usernameSignup.text.toString()
        val email = binding.emailSignup.text.toString()
        val password = binding.passwordSignup.text.toString()

        when {
            fullName.isBlank() -> {
                Toast.makeText(this, "Full name is required", Toast.LENGTH_LONG).show()
            }

            username.isBlank() -> {
                Toast.makeText(this, "Username is required", Toast.LENGTH_LONG).show()
            }

            email.isBlank() -> {
                Toast.makeText(this, "Email is required", Toast.LENGTH_LONG).show()
            }

            password.isBlank() -> {
                Toast.makeText(this, "Password is required", Toast.LENGTH_LONG).show()
            }

            else -> {
                val mAuth = FirebaseAuth.getInstance()
                val progressDialog = ProgressDialog(this@SignUpActivity).apply {
                    setTitle("Signup")
                    setMessage("Please wait")
                    setCancelable(false)
                    setCanceledOnTouchOutside(false)
                }
                progressDialog.show()

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            saveUserInfo(fullName, username, email, progressDialog)
                        } else {
                            val message = task.exception.toString()
                            Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(
        fullName: String,
        username: String,
        email: String,
        progressDialog: ProgressDialog
    ) {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef = FirebaseDatabase.getInstance().reference.child("users")

        val userMap = HashMap<String, Any>()
        userMap["uid"] = currentUserId
        userMap["fullname"] = fullName.lowercase()
        userMap["username"] = username
        userMap["email"] = email
        userMap["bio"] = "Hey, I'm using Instagram Clone App"
        userMap["image"] =
            "https://firebasestorage.googleapis.com/v0/b/instagram-clone-app-7d979.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=c779c821-11f2-410a-be80-4c32fdf04864"

        usersRef.child(currentUserId).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressDialog.dismiss()
                    Toast.makeText(this, "Account has been created successfully", Toast.LENGTH_LONG)

                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val message = task.exception.toString()
                    Toast.makeText(this, "Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}