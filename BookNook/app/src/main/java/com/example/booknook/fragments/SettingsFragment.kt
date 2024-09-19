package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.booknook.R
import android.widget.Button
import com.example.booknook.Login
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.example.booknook.Account
import com.example.booknook.MainActivity
import com.example.booknook.Register
import android.content.res.Configuration
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    lateinit var accountButton: Button
    lateinit var notificationButton: Button
    lateinit var signOutButton: Button
    lateinit var darkModeSwitch: Switch
    lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize buttons
        accountButton = view.findViewById(R.id.account_button)
        notificationButton = view.findViewById(R.id.notification_button)
        signOutButton = view.findViewById(R.id.sign_out)
        darkModeSwitch = view.findViewById(R.id.darkModeSwitch)
        auth = FirebaseAuth.getInstance()

        // Set listeners
        accountButton.setOnClickListener {
            // Handle account button click
            val accountFragment = AccountFragment()
            (activity as MainActivity).replaceFragment(accountFragment, "Account")
        }

        notificationButton.setOnClickListener {
            // Handle account button click
            val notificationFragment = NotificationFragment()
            (activity as MainActivity).replaceFragment(notificationFragment, "Notifications")
        }

        signOutButton.setOnClickListener {
            // Handle sign out button click
            val user = auth.currentUser
            if (user != null) {
                val userId = user.uid
                FirebaseFirestore.getInstance().collection("users").document(userId)
                    .update("isOnline", false)  // Updates the user's status to offline when they sign out
                    .addOnSuccessListener {
                        auth.signOut()
                        val intent = Intent(activity, Login::class.java)
                        startActivity(intent)
                        Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()
                        activity?.finish()
                    }
            }
        }

        darkModeSwitch.isChecked = isDarkThemeOn()

        // Handle the switch toggle action
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

    }

    private fun isDarkThemeOn(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}