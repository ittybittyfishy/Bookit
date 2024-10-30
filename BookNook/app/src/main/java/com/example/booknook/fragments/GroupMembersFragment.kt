package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.Friend
import com.example.booknook.FriendAdapter
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupMembersFragment : Fragment() {

    private lateinit var collapseOnlineButton: ImageButton
    private lateinit var collapseOfflineButton: ImageButton
    private lateinit var db: FirebaseFirestore
    private lateinit var onlineMembersRecyclerView: RecyclerView
    private lateinit var offlineMembersRecyclerView: RecyclerView
    private var groupId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_members, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupId = arguments?.getString("groupId")

        db = FirebaseFirestore.getInstance()
        // Initialize buttons and views
        collapseOnlineButton = view.findViewById(R.id.collapse_online_button)
        collapseOfflineButton = view.findViewById(R.id.collapse_offline_button)
        onlineMembersRecyclerView = view.findViewById(R.id.members_recycler_view)
        offlineMembersRecyclerView = view.findViewById(R.id.offline_members_recycler_view)

        onlineMembersRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays online members in 2 columns
        offlineMembersRecyclerView.layoutManager = GridLayoutManager(context, 2)  // Displays offline friends in 2 columns

        // Handles when collapse/expand button for members is clicked
        collapseOnlineButton.setOnClickListener {
            if (onlineMembersRecyclerView.visibility == View.GONE) {  // If the view is currently collapsed
                onlineMembersRecyclerView.visibility = View.VISIBLE  // Make the online members visible
                collapseOnlineButton.setImageResource(R.drawable.collapse_button)  // Show collapse button
            } else {  // If the view is currently expanded
                onlineMembersRecyclerView.visibility = View.GONE  // Sets view to invisible to collapse members
                collapseOnlineButton.setImageResource(R.drawable.expand_button)  // Show expand button
            }
        }

        // Handles when collapse/expand button for offline friends is clicked
        collapseOfflineButton.setOnClickListener {
            if (offlineMembersRecyclerView.visibility == View.GONE) {  // If the view is currently collapsed
                offlineMembersRecyclerView.visibility = View.VISIBLE  // Make the offline members visible
                collapseOfflineButton.setImageResource(R.drawable.collapse_button)  // Show collapse button
            } else {  // If the view is currently expanded
                offlineMembersRecyclerView.visibility = View.GONE  // Sets view to invisible to collapse members
                collapseOfflineButton.setImageResource(R.drawable.expand_button)  // Show expand button
            }
        }
        groupId?.let { loadMembers(it) }  // Loads the group members
    }

    // Function to load the group members
    private fun loadMembers(groupId: String) {
        db.collection("groups").document(groupId)
            .addSnapshotListener { documentSnapshot, e ->  // Lists for changes in group document
                if (e != null) {
                    activity?.let { context ->
                        Toast.makeText(context, "Error loading group members", Toast.LENGTH_SHORT).show()
                    }
                    return@addSnapshotListener  // Returns early if there is an error
                }

                if (documentSnapshot != null && documentSnapshot.exists()) {
                    // Gets the members list
                    val members = documentSnapshot.get("members") as? List<String>
                    // Creates list for online and offline member
                    if (members != null) {
                        val onlineMembers = mutableListOf<Friend>()
                        val offlineMembers = mutableListOf<Friend>()

                        // Loops through each member's Id
                        members.forEach { memberId ->
                            // Checks each member's document
                            db.collection("users").document(memberId).get()
                                .addOnSuccessListener { memberDocument ->
                                    // Gets online status and their username
                                    val isOnline = memberDocument.getBoolean("isOnline") ?: false
                                    val memberUsername = memberDocument.getString("username") ?: "Unknown"

                                    // Member information
                                    val memberInfo = Friend(
                                        friendId = memberId,
                                        friendUsername = memberUsername
                                    )

                                    // Add friend to the appropriate list based on isOnline status
                                    if (isOnline) {
                                        onlineMembers.add(memberInfo)
                                    } else {
                                        offlineMembers.add(memberInfo)
                                    }

                                    // Update recycler views of each list and allows navigation to member's profile
                                    onlineMembersRecyclerView.adapter = FriendAdapter(onlineMembers) { selectedMember ->
                                        openMemberProfile(selectedMember)
                                    }
                                    offlineMembersRecyclerView.adapter = FriendAdapter(offlineMembers) { selectedMember ->
                                        openMemberProfile(selectedMember)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(activity, "Error loading member data", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                }
            }
    }

    // Function to navigate to the friend's profile when clicking on them
    private fun openMemberProfile(selectedMember: Friend) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // If the group member is the current user, open their own profile
        if (currentUserId == selectedMember.friendId) {
            val profileFragment = ProfileFragment()
            (activity as MainActivity).replaceFragment(profileFragment, "Profile")
        // Otherwise open the member's  profile as another user's profile
        } else {
            val friendProfileFragment = FriendProfileFragment()
            val bundle = Bundle()
            bundle.putString("receiverId", selectedMember.friendId)
            bundle.putString("receiverUsername", selectedMember.friendUsername)
            friendProfileFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(friendProfileFragment, "Profile")
        }
    }
}