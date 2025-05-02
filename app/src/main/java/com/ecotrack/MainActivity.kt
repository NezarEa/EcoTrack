package com.ecotrack

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.ecotrack.Content.OnBoardingFragment
import com.ecotrack.Data.ConnectionFragment
import com.ecotrack.content.dashboard.HomeFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPref: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private val handler = Handler(Looper.getMainLooper())
    private var keepSplashOnScreen = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // For edge-to-edge UI support
        setContentView(R.layout.activity_main)

        // Set up window insets to handle system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()

        // Load shared preferences
        sharedPref = getSharedPreferences("app_prefs", MODE_PRIVATE)

        // Delay fragment transaction slightly to ensure UI is ready
        handler.postDelayed({
            keepSplashOnScreen = false
            checkUserState()
        }, 300) // You can adjust the delay time here
    }

    // Check user state and navigate to appropriate fragment
    private fun checkUserState() {
        val fragment = when {
            !sharedPref.getBoolean("onboarding_complete", false) -> {
                OnBoardingFragment() // Show onboarding if not completed
            }
            auth.currentUser == null -> {
                ConnectionFragment() // Show connection if user is not authenticated
            }
            else -> {
                HomeFragment() // Show home fragment if user is authenticated
            }
        }
        // Show fragment only if it's not already the current one
        if (fragment::class.java != supportFragmentManager.findFragmentById(R.id.nav_host_fragment)?.javaClass) {
            showFragment(fragment)
        }
    }

    // Method to show a fragment
    fun showFragment(fragment: Fragment, addToBackStack: Boolean = false) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.nav_host_fragment, fragment) // Replace current fragment
        if (addToBackStack) {
            transaction.addToBackStack(null) // Add to back stack if needed
        }
        transaction.commit()
    }

    // Handle onboarding completion
    fun completeOnboarding() {
        sharedPref.edit().putBoolean("onboarding_complete", true).apply()
        showFragment(ConnectionFragment(), addToBackStack = false)
    }

    // Handle successful authentication
    fun onAuthenticationSuccess() {
        showFragment(HomeFragment(), addToBackStack = false)
    }

    // Handle back button press to manage navigation
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            super.onBackPressed() // Go back in fragment stack
        } else {
            finishAffinity() // Close app if no fragments in back stack
        }
    }

    // Cleanup handler on destroy
    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
