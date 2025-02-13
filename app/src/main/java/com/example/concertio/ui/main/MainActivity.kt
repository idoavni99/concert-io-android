package com.example.concertio.ui.main

import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.concertio.R
import com.example.concertio.extensions.loadImage
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {
    private val bottomNav by lazy { findViewById<BottomNavigationView>(R.id.bottom_nav) }
    private val navHostFragment by lazy { supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
    private val userProfileViewModel by viewModels<UserProfileViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainActivity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initBottomNav()

        userProfileViewModel.observeMyProfile().observe(this) {
            it?.profilePicture?.let {
                bottomNav.menu.findItem(R.id.userProfileFragment).loadImage(
                    applicationContext,
                    Uri.parse(it),
                    R.drawable.empty_profile_picture,
                    lifecycleScope
                )
            }
        }
    }

    private fun initBottomNav() {
        bottomNav.itemIconTintList = null
        NavigationUI.setupWithNavController(
            bottomNav,
            navHostFragment.navController,
        )
    }
}