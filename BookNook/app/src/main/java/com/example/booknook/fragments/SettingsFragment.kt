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
import android.widget.Toast
import com.example.booknook.Account
import com.example.booknook.Register

class SettingsFragment : Fragment() {

    lateinit var accountButton: Button
    lateinit var notificationButton: Button
    lateinit var signOutButton: Button
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
        auth = FirebaseAuth.getInstance()

        // Set listeners
        accountButton.setOnClickListener {
            // Handle account button click
            val intent = Intent(requireContext(), Account::class.java)
            requireContext().startActivity(intent)
        }

        signOutButton.setOnClickListener {
            // Handle sign out button click
            auth.signOut()
            val intent = Intent(activity, Login::class.java)
            startActivity(intent)
            Toast.makeText(requireContext(), "You have been logged out", Toast.LENGTH_SHORT).show()
            activity?.finish()
        }
    }
}