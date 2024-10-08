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

class FriendProfileFragment : Fragment() {

    // Declare variables for UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var uploadBannerButton: Button
    private lateinit var uploadProfileButton: Button
    private lateinit var addFriendButton: Button

    // Declare TextView for displaying the number of collections
    private lateinit var numCollectionsTextView: TextView

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.friend_profile, container, false)

        val receiverId = arguments?.getString("receiverId")  // Receives receiverId from friends fragment

        addFriendButton = view.findViewById(R.id.addFriendButton)

        // Handles click of add friend button
        addFriendButton.setOnClickListener {
            if (receiverId != null) {
                addFriendButton.text = "Pending"
                addFriendButton.isClickable = false
                sendFriendRequest(receiverId)  // Calls function to send a friend request
            }
        }
        return view
    }

    // Function to send a friend request
    private fun sendFriendRequest(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user

        if (currentUser != null) {
            val senderId = currentUser.uid  // senderId is the current user
            val senderRef = db.collection("users").document(senderId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    // creates a map of friend request details
                    val friendRequest = hashMapOf(
                        "senderId" to senderId,
                        "senderUsername" to senderUsername,
                        "receiverId" to receiverId,
                        "status" to "pending"
                    )

                    // Update receiver's friend requests array in database
                    db.collection("users").document(receiverId)
                        .update("friendRequests", FieldValue.arrayUnion(friendRequest))
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Friend request sent", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e -> Toast.makeText(activity, "Failed to send friend request: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "Sender username not found", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(activity, "Failed to send friend request", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
}