package com.example.booknook.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.booknook.Login
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ConfirmSignoutFragment : DialogFragment() {

    lateinit var confirmSignOut: ImageButton
    lateinit var cancelSignOut:ImageButton

    lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirm_signout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI elements
        confirmSignOut = view.findViewById(R.id.confirmSignOut)
        cancelSignOut = view.findViewById(R.id.cancelSignOut)

        auth = FirebaseAuth.getInstance()

        confirmSignOut.setOnClickListener {
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
                    .addOnFailureListener { e ->
                        Log.e("SignOut", "Error updating Firestore: ${e.message}")
                        Toast.makeText(requireContext(), "Error logging out. Please try again.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Log.e("SignOut", "User is null, unable to sign out")
            }
        }

        cancelSignOut.setOnClickListener()
        {
            dismiss()
        }
    }
}