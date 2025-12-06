package com.example.studentnotes

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Use 'as?' to prevent a crash if the fragment is not found immediately
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as? NavHostFragment

        if (navHostFragment != null) {
            navController = navHostFragment.navController
            // UNCOMMENTED: This enables the back arrow and titles in the top bar
            setupActionBarWithNavController(navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // This handles the click on the back arrow
        return if (::navController.isInitialized) {
            navController.navigateUp() || super.onSupportNavigateUp()
        } else {
            super.onSupportNavigateUp()
        }
    }
}
