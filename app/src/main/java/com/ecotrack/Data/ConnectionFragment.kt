package com.ecotrack.Data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.ecotrack.MainActivity
import com.ecotrack.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ConnectionFragment : Fragment() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var signInButton: MaterialButton
    private lateinit var progressBar: CircularProgressIndicator
    private val TAG = "EnergyTrackingFragment"

    private val signInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (!isAdded) return@registerForActivityResult

        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(it)
                    handleSignInResult(task)
                } ?: run {
                    showLoading(false)
                    Log.w(TAG, "Sign-in result data is null")
                }
            }
            else -> {
                showLoading(false)
                Log.w(TAG, "Sign-in canceled or failed with code: ${result.resultCode}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connection, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signInButton = view.findViewById(R.id.google_sign_in_button)
        progressBar = view.findViewById(R.id.progress_bar)

        // Initialize Firebase services
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()

        configureGoogleSignIn()

        signInButton.setOnClickListener {
            when {
                !isNetworkAvailable() -> showToast("No internet connection")
                else -> signIn()
            }
        }
    }

    private fun configureGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            context?.let {
                googleSignInClient = GoogleSignIn.getClient(it, gso)
            } ?: run {
                Log.e(TAG, "Context is null during Google Sign-In configuration")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google SignIn configuration failed", e)
            showToast("Authentication service unavailable")
        }
    }

    private fun signIn() {
        if (!::googleSignInClient.isInitialized) {
            Log.e(TAG, "GoogleSignInClient not initialized")
            showToast("Authentication service unavailable")
            return
        }

        showLoading(true)
        try {
            val signInIntent = googleSignInClient.signInIntent
            signInLauncher.launch(signInIntent)
        } catch (e: Exception) {
            showLoading(false)
            Log.e(TAG, "Sign-in failed", e)
            showToast("Sign-in error occurred")
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        if (!isAdded) return

        try {
            val account = completedTask.getResult(ApiException::class.java)
            account?.idToken?.let { token ->
                firebaseAuthWithGoogle(token)
            } ?: run {
                showLoading(false)
                Log.e(TAG, "ID token is null")
                showToast("Authentication failed")
            }
        } catch (e: ApiException) {
            showLoading(false)
            Log.e(TAG, "Google SignIn failed: ${e.message}, status code: ${e.statusCode}", e)
            showToast("Authentication failed")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        if (!isAdded) return

        showLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(credential).await()

                if (isAdded) {
                    authResult.user?.let { user ->
                        saveUserData(user)
                        checkIfUserIsNew(user.uid)
                    } ?: run {
                        Log.e(TAG, "Firebase auth successful but user is null")
                        showToast("Authentication error")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase authentication failed", e)
                showToast("Authentication failed")
            } finally {
                if (isAdded) {
                    showLoading(false)
                }
            }
        }
    }

    private suspend fun checkIfUserIsNew(uid: String) {
        try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("energyInfo")
                .document("usageData")
                .get(Source.SERVER).await()

            if (snapshot.exists()) {
                navigateToHome()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to check if user is new", e)
            navigateToHome()
        }
    }


    private suspend fun saveUserData(user: FirebaseUser) {
        if (!isAdded) return

        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: ""),
            "email" to (user.email ?: ""),
            "photoUrl" to (user.photoUrl?.toString() ?: "")
        )

        try {
            withContext(Dispatchers.IO) {
                firestore.collection("users").document(user.uid).set(userData).await()
            }
            Log.d(TAG, "User data saved successfully to Firestore")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user data to Firestore", e)
        }
    }

    private fun navigateToHome() {
        if (!isAdded) return

        try {
            (activity as? MainActivity)?.onAuthenticationSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "Navigation to home failed", e)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val context = context ?: return false
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.activeNetworkInfo?.isConnected == true
        }
    }

    private fun showLoading(show: Boolean) {
        if (!isAdded) return
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
        signInButton.isEnabled = !show
    }

    private fun showToast(message: String) {
        if (!isAdded) return

        try {
            context?.let {
                Toast.makeText(it, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show toast", e)
        }
    }
}