package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FindGroupHomepageFragment : Fragment() {

    private lateinit var joinGroupButton: Button
    private var groupId: String? = null
    private var groupCreatorId: String? = null
    private lateinit var bannerImg: ImageView

    // Get bundled input from group item
    // Olivia Fishbough
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the groupId from arguments
        groupId = arguments?.getString("GROUP_ID")
        groupCreatorId = arguments?.getString("GROUP_CREATOR_ID")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_find_group_homepage, container, false)
        bannerImg = view.findViewById(R.id.bannerImage)

        if (groupId != null) {
            // Calls function to load the group's information
            loadGroupData(groupId!!)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load in buttons
        joinGroupButton = view.findViewById(R.id.joinGroupButton)

        // Load initial joined status
        groupId?.let { groupId ->
            checkJoinedGroupStatus(groupId)  // Check the initial status
        }

        // Set on click listener to allow user to join group
        // Olivia Fishbough
        joinGroupButton.setOnClickListener(){
            if (joinGroupButton.text == "Leave Group") {
                //
                // leaveGroup(groupId) function goes here
                //
            } else {
                // Call joinGroup only if groupId is not null
                groupId?.let { joinGroup(it) }
            }

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
                checkJoinedGroupStatus(groupId)
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

    // Veronica Nguyen
    // Loads the group data
    private fun loadGroupData(groupId: String) {
        val groupsDocRef = FirebaseFirestore.getInstance().collection("groups").document(groupId)
        groupsDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val bannerImgUrl = document.getString("bannerImg")

                    // Load banner image using Glide
                    if (!bannerImgUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(bannerImgUrl)
                            .into(bannerImg)
                    }
                }
            }
    }

    // Veronica Nguyen
    // Checks to see if the user has already joined the group
    private fun checkJoinedGroupStatus(groupId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val groupsDocRef = FirebaseFirestore.getInstance().collection("groups").document(groupId)
        groupsDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val members = document.get("members") as? List<String>
                    if (members != null) {
                        // Loops through each member to check if they're already in the group
                        val alreadyJoined = members.any { membersItem -> membersItem == userId}
                        if (alreadyJoined) {
                            // Already joined group, change button to "Leave Group"
                            joinGroupButton.text = "Leave Group"
                            Toast.makeText(activity, "Already joined", Toast.LENGTH_SHORT).show()
                        } else {
                            // Not joined, button displays "Join Group"
                            joinGroupButton.text = "Join Group"
                            Toast.makeText(activity, "Not joined", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        joinGroupButton.text = "Join Group"
                        Toast.makeText(activity, "No members found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error checking joined status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Olivia Fishbough
    companion object {
        // Use this method to create a new instance of the fragment with the groupId
        fun newInstance(groupId: String): FindGroupHomepageFragment {
            val fragment = FindGroupHomepageFragment()
            val args = Bundle()
            args.putString("GROUP_ID", groupId)
            fragment.arguments = args
            return fragment
        }
    }
}