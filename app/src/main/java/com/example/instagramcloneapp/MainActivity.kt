package com.example.instagramcloneapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.instagramcloneapp.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                return@OnItemSelectedListener true
            }

            R.id.nav_search -> {
                return@OnItemSelectedListener true
            }

            R.id.nav_add_post -> {
                return@OnItemSelectedListener true
            }

            R.id.nav_notifications -> {
                return@OnItemSelectedListener true
            }

            R.id.nav_profile -> {
                return@OnItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navView.setOnItemSelectedListener(onItemSelectedListener)
    }
}