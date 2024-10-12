package com.example.booknook.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class UserProfileFragment : Fragment() {
    // Declare variables for UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView

    // Declare TextView for displaying the number of collections
    private lateinit var numCollectionsTextView: TextView

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        val receiverId = arguments?.getString("receiverId")  // Receives receiverId from friends fragment

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user
        val receiverId = arguments?.getString("receiverId")  // Retrieves the receiver's id from friends fragment arguments
    }
}
