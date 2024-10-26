package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class GroupHomepageFragment : Fragment() {

    private lateinit var joinGroupButton: Button
    private var groupId: String? = null

    // Get bundled input from group item
    // Olivia Fishbough
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the groupId from arguments
        groupId = arguments?.getString("GROUP_ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_homepage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load in buttons
        joinGroupButton = view.findViewById(R.id.joinGroupButton)

        // Set on click listener to allow user to join group
        // Olivia Fishbough
        joinGroupButton.setOnClickListener(){
            // Call joinGroup only if groupId is not null
            groupId?.let { joinGroup(it) }

        }
    }

    // Function that allows user to join public group
    // Olivia Fishbough
    private fun joinGroup(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Ensure userId is not null
        if (userId == null) {
            Toast.makeText(requireContext(), "Error: User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Add user to group members
        db.collection("groups").document(groupId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                // Notify the owner that the user has been added
                Toast.makeText(requireContext(), "You have joined the group.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("GroupHomepageFragment", "Error adding user to group: ${e.message}")
            }

        // Update user's groups
        val userRef = db.collection("users").document(userId)
        userRef.update("joinedGroups", FieldValue.arrayUnion(groupId))
            .addOnSuccessListener {
                userRef.update("numGroups", FieldValue.increment(1))
                Toast.makeText(
                    requireContext(),
                    "Member has been added to group",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnFailureListener { e ->
                Log.w("GroupHomepageFragment", "Error adding group to user: ${e.message}")
            }
    }

    // Olivia Fishbough
    companion object {
        // Use this method to create a new instance of the fragment with the groupId
        fun newInstance(groupId: String): GroupHomepageFragment {
            val fragment = GroupHomepageFragment()
            val args = Bundle()
            args.putString("GROUP_ID", groupId)
            fragment.arguments = args
            return fragment
        }
    }
}