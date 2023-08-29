package com.example.instagramcloneapp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.instagramcloneapp.databinding.ActivityMainBinding
import com.example.instagramcloneapp.fragments.HomeFragment
import com.example.instagramcloneapp.fragments.NotificationsFragment
import com.example.instagramcloneapp.fragments.ProfileFragment
import com.example.instagramcloneapp.fragments.SearchFragment
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    internal var selectedFragment: Fragment? = null

    private val onItemSelectedListener = NavigationBarView.OnItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                selectedFragment = HomeFragment()
            }

            R.id.nav_search -> {
                selectedFragment = SearchFragment()
            }

            R.id.nav_add_post -> {
                selectedFragment = HomeFragment()
            }

            R.id.nav_notifications -> {
                selectedFragment = NotificationsFragment()
            }

            R.id.nav_profile -> {
                selectedFragment = ProfileFragment()
            }
        }
        if (selectedFragment != null) {
            supportFragmentManager.beginTransaction().replace(
                R.id.fragment_container,
                selectedFragment!!
            ).commit()
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navView.setOnItemSelectedListener(onItemSelectedListener)

        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            HomeFragment()
        ).commit()
    }
}