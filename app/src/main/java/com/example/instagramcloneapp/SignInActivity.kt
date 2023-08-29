package com.example.instagramcloneapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.instagramcloneapp.databinding.ActivitySignInBinding

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val signupLinkBtn = binding.signupLinkBtn
        signupLinkBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}