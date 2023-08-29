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
                moveToFragment(HomeFragment())
                return@OnItemSelectedListener true
            }

            R.id.nav_search -> {
                moveToFragment(SearchFragment())
                return@OnItemSelectedListener true
            }

            R.id.nav_add_post -> {
                return@OnItemSelectedListener true
            }

            R.id.nav_notifications -> {
                moveToFragment(NotificationsFragment())
                return@OnItemSelectedListener true
            }

            R.id.nav_profile -> {
                moveToFragment(ProfileFragment())
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

        moveToFragment(HomeFragment())
    }

    private fun moveToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_container,
            fragment
        ).commit()
    }
}