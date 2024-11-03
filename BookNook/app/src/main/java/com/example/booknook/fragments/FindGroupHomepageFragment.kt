package com.example.booknook.fragments

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
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class FindGroupHomepageFragment : Fragment() {

    private lateinit var joinGroupButton: Button
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
        val view = inflater.inflate(R.layout.fragment_find_group_homepage, container, false)
        bannerImg = view.findViewById(R.id.bannerImage)
        numMembers = view.findViewById(R.id.numMembers)
        membersOnline = view.findViewById(R.id.membersOnline)
        numRecommendations = view.findViewById(R.id.numRecommendations)
        tagsChipGroup = view.findViewById(R.id.tagsChipGroup)
        expandButton = view.findViewById(R.id.expandButton)
        membersSection = view.findViewById(R.id.membersSection)
        recommendationsSection = view.findViewById(R.id.recommendationsSection)

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

        // Sets up snapshot listener to see if document has changed
        groupId?.let { groupId ->
            // Load initial joined status
            checkJoinedGroupStatus(groupId)  // Check the initial status
            FirebaseFirestore.getInstance().collection("groups").document(groupId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w("FindGroupHomepageFragment", "Listen failed.", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        // Re-fetch group data and update the UI immediately if the group doc has changed
                        loadGroupData(groupId)
                    }
                }
        }

        // Set on click listener to allow user to join group
        // Olivia Fishbough
        joinGroupButton.setOnClickListener(){
            // Leave a group
            if (joinGroupButton.text == "Leave Group") {
                groupId?.let { it1 -> leaveGroup(it1) { checkJoinedGroupStatus(it1) } } // Pass the callback
            // Edit group if they're the creator
            } else if (joinGroupButton.text == "Edit Group"){
                groupId?.let { it1 -> editGroup(it1) }
            // Join group
            } else {
                // Call joinGroup only if groupId is not null
                groupId?.let { joinGroup(it) }
            }
        }

        // Veronica Nguyen
        // Handles click of expand button for chips
        expandButton.setOnClickListener {
            // Toggles the expand/collapse state when button is clicked
            isExpanded = !isExpanded
            displayChips(isExpanded)
            // Switches the button image when clicked on
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
            // Opens Members fragment
            (activity as MainActivity).replaceFragment(groupMembersFragment, "Members")
        }

        // Veronica Nguyen
        // Handles click of Recommendations section
        recommendationsSection.setOnClickListener {
            val recommendationsFragment = GroupRecommendationsFragment()
            val bundle = Bundle()
            bundle.putString("groupId", groupId)
            recommendationsFragment.arguments = bundle
            // Opens Recommendations fragment
            (activity as MainActivity).replaceFragment(recommendationsFragment, "Recommendations")
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
                    if (!bannerImgUrl.isNullOrEmpty() && isAdded) {
                        Glide.with(this)
                            .load(bannerImgUrl)
                            .into(bannerImg)
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
                    val recommendations = document.get("recommendations") as? List<Map<String, Any>>
                    val numofRecommendations = recommendations?.size ?: 0
                    numRecommendations.text = "$numofRecommendations"
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
                    val groupCreatorId = document.getString("createdBy")
                    if (members != null) {
                        // Loops through each member to check if they're already in the group
                        val alreadyJoined = members.any { membersItem -> membersItem == userId}
                        // If the current user is the creator of the group
                        if (userId == groupCreatorId) {
                            // Change button to "Edit Group"
                            joinGroupButton.text = "Edit Group"
                        // If user is already a member
                        } else if (alreadyJoined) {
                            // Already joined group, change button to "Leave Group"
                            joinGroupButton.text = "Leave Group"
                            Toast.makeText(activity, "Already joined", Toast.LENGTH_SHORT).show()
                        // User is not a creator or member of group
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
    private fun leaveGroup(groupId: String, onSuccess: () -> Unit) { // Added onSuccess parameter
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Toast.makeText(requireContext(), "Error: User is not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        // Remove user from the group members array
        db.collection("groups").document(groupId)
            .update("members", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                // Update user's group list and decrement group count
                val userRef = db.collection("users").document(userId)
                userRef.update("joinedGroups", FieldValue.arrayRemove(groupId))
                    .addOnSuccessListener {
                        userRef.update("numGroups", FieldValue.increment(-1))
                        onSuccess() // Call the callback function to update UI
                        Toast.makeText(
                            requireContext(),
                            "You have left the group.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("FindGroupHomepageFragment", "Error updating user data: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.w("FindGroupHomepageFragment", "Error removing user from group: ${e.message}")
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