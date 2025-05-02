package com.ecotrack.Content.Dashbord

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ecotrack.Data.ConnectionFragment
import com.ecotrack.MainActivity
import com.ecotrack.R
import com.ecotrack.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        loadUserData()
    }

    private fun setupListeners() {

        binding.logoutButton.setOnClickListener {
            signOut()
        }

        binding.notificationsContainer.setOnClickListener {
            binding.notificationsSwitch.toggle()
        }

        binding.languageContainer.setOnClickListener {
            Toast.makeText(context, "Language settings", Toast.LENGTH_SHORT).show()
        }

        binding.privacyContainer.setOnClickListener {
            Toast.makeText(context, "Privacy policy", Toast.LENGTH_SHORT).show()
        }

        binding.termsContainer.setOnClickListener {
            Toast.makeText(context, "Terms of service", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }

        // Load user profile data
        loadProfileData(currentUser.uid)
    }

    private fun loadProfileData(uid: String) {
        val user = auth.currentUser
        user?.let {
            // Set user name and email
            binding.profileName.text = user.displayName ?: "User"
            binding.profileEmail.text = user.email ?: ""

            // Load profile image
            context?.let { ctx ->
                Glide.with(ctx)
                    .load(user.photoUrl ?: R.drawable.user)
                    .placeholder(R.drawable.user)
                    .error(R.drawable.user)
                    .into(binding.profileImage)
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        // Navigate to login screen or main activity
        (activity as? MainActivity)?.showFragment(ConnectionFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Nullify binding reference to avoid memory leaks
    }
}
