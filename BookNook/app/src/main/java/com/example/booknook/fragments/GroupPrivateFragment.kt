package com.example.booknook.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupPrivateFragment : DialogFragment() {

    // Declare properties for groupId and groupCreatorId
    private lateinit var groupId: String
    private lateinit var groupCreatorId: String

    // UI elements
    private lateinit var sendRequest: Button
    private lateinit var cancel: Button

    // Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getString("GROUP_ID") ?: ""
            groupCreatorId = it.getString("GROUP_CREATOR_ID") ?: ""
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_private, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI elements
        sendRequest = view.findViewById(R.id.sendRequest)
        cancel = view.findViewById(R.id.cancel)

        auth = FirebaseAuth.getInstance()

        sendRequest.setOnClickListener {
            // Send a join request
            sendJoinRequest(groupId, groupCreatorId)
            dismiss()
        }

        cancel.setOnClickListener {
            dismiss()
        }
    }

    // Function to send a join request for a private group
    private fun sendJoinRequest(groupId: String, groupCreatorId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user

        if (currentUser != null) {
            val senderId = currentUser.uid  // senderId is the current user
            val senderRef = db.collection("users").document(senderId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    // Create a map of join request details
                    val joinRequest = hashMapOf(
                        "senderId" to senderId,
                        "senderUsername" to senderUsername,
                        "groupId" to groupId,
                        "status" to "pending"
                    )

                    // Update the group's requests collection in the database
                    db.collection("groups").document(groupId)
                        .collection("requests")
                        .add(joinRequest)
                        .addOnSuccessListener {
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Join request sent", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            if (isAdded) {
                                Toast.makeText(requireContext(), "Failed to send join request: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    // Yunjong Noh
                    // Notify the owner of the group request
                    sendGroupJoinNotification(groupCreatorId, senderId, senderUsername)
                } else {
                    if (isAdded) {
                        Toast.makeText(requireContext(), "Sender username not found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
    // Yunjong Noh
    // Function to send a group join request notification to the group creator
    private fun sendGroupJoinNotification(receiverId: String, senderId: String, senderUsername: String) {
        val db = FirebaseFirestore.getInstance()
        val currentTime = System.currentTimeMillis()
        val expirationTime = currentTime + 10 * 24 * 60 * 60 * 1000 // Notification expires in 10 days

        // Create the notification item
        val notification = NotificationItem(
            userId = receiverId, // The ID of the user who will receive the notification (group creator)
            senderId = senderId, // The ID of the user who sent the join request
            receiverId = receiverId, // The ID of the receiver (same as userId)
            message = "$senderUsername has requested to join your group.",
            timestamp = currentTime,
            type = NotificationType.GROUP_JOIN_REQUEST, // Notification type for group join request
            dismissed = false,
            expirationTime = expirationTime,
            profileImageUrl = "", // Optional: Fetch and add the profile image URL if needed
            username = senderUsername // Sender's username
        )

        // Add the notification to Firestore
        db.collection("notifications").add(notification)
            .addOnSuccessListener { documentReference ->
                val notificationId = documentReference.id
                db.collection("notifications").document(notificationId)
                    .update("notificationId", notificationId)
                    .addOnSuccessListener {
                        Log.d("GroupPrivateFragment", "Notification sent successfully with ID: $notificationId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("GroupPrivateFragment", "Failed to update notification ID: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("GroupPrivateFragment", "Failed to send notification: ${e.message}", e)
            }
    }
}
