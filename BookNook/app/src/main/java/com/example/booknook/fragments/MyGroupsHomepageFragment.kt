package com.example.booknook.fragments

import android.health.connect.datatypes.units.Length
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MyGroupsHomepageFragment : Fragment() {
    private lateinit var leaveGroupButton: Button
    private var groupId: String? = null
    private var groupCreatorId: String? = null
    private lateinit var bannerImg: ImageView
    private lateinit var numMembers: TextView
    private lateinit var membersOnline: TextView
    private lateinit var numRecommendations: TextView

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
        val view = inflater.inflate(R.layout.fragment_my_groups_homepage, container, false)
        bannerImg = view.findViewById(R.id.bannerImage)
        numMembers = view.findViewById(R.id.numMembers)
        membersOnline = view.findViewById(R.id.membersOnline)
        numRecommendations = view.findViewById(R.id.numRecommendations)

        if (groupId != null) {
            // Calls function to load the group's information
            loadGroupData(groupId!!)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load in buttons
        leaveGroupButton = view.findViewById(R.id.leaveGroupButton)

        // Set on click listener to allow user to join group
        // Olivia Fishbough
        leaveGroupButton.setOnClickListener(){
            //
            // leaveGroup(groupId) function goes here
            //
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

                    // Displays the number of members
                    val members = document.get("members") as? List<*>
                    val numOfMembers = members?.size ?: 0
                    numMembers.text = "$numOfMembers"

                    // Calls function to get number of members online
                    getNumMembersOnline(groupId)
                }
            }
    }

    // Veronica Nguyen
    // Gets the number of members online
    private fun getNumMembersOnline(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        // Gets the group's document
        val groupsDocRef = db.collection("groups").document(groupId)

        groupsDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Gets the members in the group
                    val memberIds = document.get("members") as? List<String> ?: listOf()
                    var onlineCount = 0  // Stores the number of members online

                    // Loops through each member to check their online status
                    for (memberId in memberIds) {
                        // Accesses the user's document
                        db.collection("users").document(memberId).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc != null && userDoc.exists()) {
                                    // Checks if they're online
                                    val isOnline = userDoc.getBoolean("isOnline") ?: false
                                    if (isOnline) {
                                        onlineCount++
                                    }
                                }

                                // Updates TextView after last member is checked
                                if (memberId == memberIds.last()) {
                                    membersOnline.text = "$onlineCount"
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(activity, "Failed to check online statuses", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Failed to retrieve members", Toast.LENGTH_SHORT).show()
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