package com.example.booknook.fragments

import GroupUpdateAdapter
import android.app.AlertDialog
import android.health.connect.datatypes.units.Length
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.GroupMemberUpdate
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MyGroupsHomepageFragment : Fragment() {
    private lateinit var leaveGroupButton: Button
    private var groupId: String? = null
    private var groupCreatorId: String? = null
    private lateinit var bannerImg: ImageView
    private lateinit var tagsChipGroup: ChipGroup
    private lateinit var numMembers: TextView
    private lateinit var membersOnline: TextView
    private lateinit var numRecommendations: TextView
    private lateinit var expandButton: ImageButton
    private lateinit var membersSection: LinearLayout
    private lateinit var recommendationsSection: LinearLayout
    private lateinit var memberUpdatesRecyclerView: RecyclerView
    private lateinit var memberUpdatesAdapter: GroupUpdateAdapter
    private lateinit var memberUpdatesList: MutableList<GroupMemberUpdate>
    private var isExpanded = false  // Tracks if chips are expanded or collapsed

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
        tagsChipGroup = view.findViewById(R.id.tagsChipGroup)
        expandButton = view.findViewById(R.id.expandButton)
        membersSection = view.findViewById(R.id.membersSection)
        recommendationsSection = view.findViewById(R.id.recommendationsSection)
        memberUpdatesRecyclerView = view.findViewById(R.id.memberUpdatesRecyclerView)

        // Sets up adapter and recycler view for the group updates
        memberUpdatesList = mutableListOf()
        memberUpdatesAdapter = GroupUpdateAdapter(memberUpdatesList)

        memberUpdatesRecyclerView.layoutManager = LinearLayoutManager(context)
        memberUpdatesRecyclerView.adapter = memberUpdatesAdapter

        if (groupId != null) {
            // Calls function to load the group's information
            loadGroupData(groupId!!)
            fetchMemberUpdates(groupId!!)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load in buttons
        leaveGroupButton = view.findViewById(R.id.leaveGroupButton)

        // Sets up snapshot listener to see if document has changed
        groupId?.let { groupId ->
            FirebaseFirestore.getInstance().collection("groups").document(groupId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("MyGroupsHomepageFragment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // Re-fetch group data and update the UI immediately if the group doc has changed
                        loadGroupData(groupId)
                    }
                }
        }

        // Allows user to leave the group
        // Olivia Fishbough
        leaveGroupButton.setOnClickListener(){
            if (leaveGroupButton.text == "Edit Group") {
                groupId?.let { it1 -> editGroup(it1) }
            }
            else if (leaveGroupButton.text == "Join Group"){
                groupId?.let { it1 -> joinGroup(it1) }
            }
            else {
                groupId?.let { it1 -> leaveGroup(it1) }
            }
        }

        // Veronica Nguyen
        // Handles click of expand button for chips
        expandButton.setOnClickListener {
            // Toggles the expand/collapse state when button is clicked
            isExpanded = !isExpanded
            displayChips(isExpanded)
            // Swithces the button image when clicked on
            expandButton.setImageResource(
                if (isExpanded) {
                    R.drawable.collapse_button
                } else {
                    R.drawable.expand_button
                }
            )
        }

        // Veronica Nguyen
        // Handles click of Members section
        membersSection.setOnClickListener {
            val groupMembersFragment = GroupMembersFragment()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            groupMembersFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(groupMembersFragment, "Members", showBackButton = true)
        }

        // Veronica Nguyen
        // Handles click of Recommendations section
        recommendationsSection.setOnClickListener {
            val recommendationsFragment = GroupRecommendationsFragment()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            recommendationsFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(recommendationsFragment, "Recommendations", showBackButton = true)
        }

    }

    // Veronica Nguyen
    // Function to edit a group
    private fun editGroup(groupId: String) {
        // Creates a popup that opens up edit screen
        val editFragment = EditGroupFragment.newInstance(groupId)
        childFragmentManager.let {
            editFragment.show(it, "EditGroupDialog")
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
                    if (!bannerImgUrl.isNullOrEmpty() && isAdded) {
                        Glide.with(this)
                            .load(bannerImgUrl)
                            .into(bannerImg)
                    }

                    // Check if the current user is the creator of the group
                    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                    groupCreatorId = document.getString("createdBy")
                    // Change button text to "Edit Group" if current user is the creator
                    if (currentUserId == groupCreatorId) {
                        leaveGroupButton.text = "Edit Group"
                    } else {
                        leaveGroupButton.text = "Leave Group"  // Else, keep it as "Leave Group"
                    }

                    // Calls function to display group tags
                    getTags(groupId)

                    // Displays the number of members
                    val members = document.get("members") as? List<*>
                    val numOfMembers = members?.size ?: 0
                    numMembers.text = "$numOfMembers"

                    // Calls function to get number of members online
                    getNumMembersOnline(groupId)

                    // Displays number of recommendations
                    groupsDocRef.collection("recommendations")
                        .get()
                        .addOnSuccessListener { recommendationSnapshot ->
                            val numOfRecommendations = recommendationSnapshot.size()
                            numRecommendations.text = "$numOfRecommendations"

                        }
                }
            }
    }

    // Veronica Nguyen
    // Function to get the group member updates
    private fun fetchMemberUpdates(groupId: String) {
        val db = FirebaseFirestore.getInstance()

        // Fetch the memberUpdates collection for the given group
        db.collection("groups").document(groupId)
            .collection("memberUpdates")
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    // Clear the list before adding new data
                    memberUpdatesList.clear()

                    // Iterate through the snapshot and create MemberUpdate objects
                    for (document in querySnapshot.documents) {
                        val memberUpdate = document.toObject(GroupMemberUpdate::class.java)
                        memberUpdate?.let { memberUpdatesList.add(it) }
                    }

                    // Notify the adapter that the data has been updated
                    memberUpdatesAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error fetching member updates", e)
            }
    }


    // Veronica Nguyen
    // Get group tags
    private fun getTags(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        val groupsDocRef = db.collection("groups").document(groupId)
        groupsDocRef.get()
            .addOnSuccessListener { document ->
                val tags = document.get("tags") as? List<String> ?: listOf()
                tagsChipGroup.removeAllViews() // Clear any existing chips

                val currentContext = context ?: return@addOnSuccessListener

                // Loops through each tag and displays it in the chips
                for (tag in tags) {
                    val chip = Chip(currentContext)
                    chip.text = tag
                    chip.isCloseIconVisible = false

                    // Customize size
                    chip.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    // Adjusts text size
                    chip.textSize = 12f  // Set the desired text size in sp
                    // Sets padding
                    chip.setPadding(10, 10, 10, 10)
                    tagsChipGroup.addView(chip)
                }
                // Only show expand button if there's more than 3 chips
                expandButton.visibility = if (tags.size > 3) View.VISIBLE else View.GONE
                displayChips(isExpanded)  // Initially shows only first row of tags
            }
    }

    // Veronica Nguyen
    // Displays the chips and initially shows only the first row
    private fun displayChips(expand: Boolean) {
        // Gets all the chips
        val chips = (0 until tagsChipGroup.childCount).map { tagsChipGroup.getChildAt(it) }
        chips.forEachIndexed { index, view ->
            view.visibility =
                // Shows all chips if isExpanded is true
                if (expand || index < 4) {
                    View.VISIBLE
                // Else shows only the first row
                } else {
                    View.GONE
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
                    var processedCount = 0 // Tracks how many members have been processed

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
                                processedCount++  // Increment the processed counter

                                // Updates TextView after all members have been checked
                                if (processedCount == memberIds.size) {
                                    membersOnline.text = "$onlineCount"
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(activity, "Failed to check online statuses", Toast.LENGTH_SHORT).show()
                                processedCount++
                                // Still need to check if all members were processed in case of failure
                                if (processedCount == memberIds.size) {
                                    membersOnline.text = "$onlineCount"
                                }
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Failed to retrieve members", Toast.LENGTH_SHORT).show()
            }
    }

    // Olivia Fishbough
    // Function that allows a user to leave a group
    private fun leaveGroup(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Log.w("LeaveGroup", "User not authenticated")
            return
        }

        // Step 1: Remove user from group members
        db.collection("groups").document(groupId).update("members", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                // Step 2: Remove group from user's joined groups and decrement numGroups
                db.collection("users").document(userId)
                    .update(
                        "joinedGroups", FieldValue.arrayRemove(groupId),
                        "numGroups", FieldValue.increment(-1)
                    )
                    .addOnSuccessListener {
                        Toast.makeText(activity, "User left group", Toast.LENGTH_SHORT).show()
                        Log.d("LeaveGroup", "Successfully removed user $userId from group $groupId and updated user data.")
                        // Update button text based on current group status
                        checkJoinedGroupStatus(groupId)
                    }
                    .addOnFailureListener { e ->
                        Log.w("LeaveGroup", "Failed to update user data after removing from group: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.w("LeaveGroup", "Failed to remove user from group members: ${e.message}")
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
                loadGroupData(groupId)
                checkJoinedGroupStatus(groupId) // Update button text based on current group status
                // Notify the user that the user has been added
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
    // Checks to see if the user has already joined the group
    private fun checkJoinedGroupStatus(groupId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        val groupsDocRef = FirebaseFirestore.getInstance().collection("groups").document(groupId)
        groupsDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val members = document.get("members") as? List<String>
                    val groupCreatorId = document.getString("createdBy")
                    if (members != null) {
                        // Loops through each member to check if they're already in the group
                        val alreadyJoined = members.any { membersItem -> membersItem == userId}
                        // If the current user is the creator of the group
                        if (userId == groupCreatorId) {
                            // Change button to "Edit Group"
                            leaveGroupButton.text = "Edit Group"
                            // If user is already a member
                        } else {
                            // Not joined, button displays "Join Group"
                            leaveGroupButton.text = "Join Group"
                            Toast.makeText(activity, "Not joined", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        leaveGroupButton.text = "Join Group"
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