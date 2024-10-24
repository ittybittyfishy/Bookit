package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.GroupAdapter
import com.example.booknook.GroupItem
import com.example.booknook.GroupManageAdapter
import com.example.booknook.GroupRequestAdapter
import com.example.booknook.GroupRequestHolderItem
import com.example.booknook.GroupRequestItem
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration


class ManageGroupsFragment : Fragment() {

    // Navigation buttons for navigating to "My Groups" and "Find Groups" sections
    private lateinit var myGroups: Button
    private lateinit var findGroups: Button

    // Page UI elements: button to create a new group and a RecyclerView to display groups
    private lateinit var createGroup: Button
    private lateinit var recyclerView: RecyclerView

    // Adapter for managing the list of groups and join requests
    private lateinit var groupManageAdapter: GroupManageAdapter
    private val groupList = mutableListOf<GroupRequestHolderItem>()

    // Listner to update the groups in live time
    private var groupListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and set up the "Create Group" button
        createGroup = view.findViewById(R.id.createGroupButton)

        createGroup.setOnClickListener {
            // Show a dialog fragment to create a new group
            val createGroupDialog = CreateGroupFragment()
            createGroupDialog.show(childFragmentManager, "CreateGroupDialog")
        }

        // Initialize navigation buttons and set click listeners
        myGroups = view.findViewById(R.id.myGroups)
        findGroups = view.findViewById(R.id.findGroups)

        myGroups.setOnClickListener {
            // Navigate to "My Groups" fragment
            val groupsFragment = GroupsFragment()
            (activity as MainActivity).replaceFragment(groupsFragment, "My Groups")
        }

        findGroups.setOnClickListener {
            // Navigate to "Find Groups" fragment
            val findGroupsFragment = FindGroupFragment()
            (activity as MainActivity).replaceFragment(findGroupsFragment, "Find Groups")
        }

        // Set up the RecyclerView to display groups
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter and set click listeners for accepting/rejecting join requests
        groupManageAdapter = GroupManageAdapter(
            groupList,
            onAcceptClick = { groupId, requestItem -> handleAcceptRequest(groupId, requestItem) },
            onRejectClick = { groupId, requestItem -> handleRejectRequest(groupId, requestItem) },
            fragmentManager = parentFragmentManager
        )
        recyclerView.adapter = groupManageAdapter

        // Load data from Firestore
        loadGroupsFromFirestore()
    }

    // Method to fetch groups from Firestore where the user is the owner
    private fun loadGroupsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Listen to changes in the "groups" collection
        groupListener = db.collection("groups")
            .whereEqualTo("createdBy", userId) // Only listen to groups created by the current user
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("ManageGroupsFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    groupList.clear() // Clear existing data

                    for (document in snapshot.documents) {
                        val groupName = document.getString("groupName") ?: "Unknown Group"
                        val groupId = document.id

                        // Create a GroupRequestHolderItem for each group (requests not handled)
                        val groupRequestHolderItem = GroupRequestHolderItem(
                            groupId = groupId,
                            groupName = groupName,
                            requests = mutableListOf() // Empty list for simplicity
                        )
                        groupList.add(groupRequestHolderItem)
                    }

                    // Notify adapter about data changes
                    groupManageAdapter.notifyDataSetChanged()
                }
            }
    }

    // Handle accepting a join request by adding the user to the group
    private fun handleAcceptRequest(groupId: String, requestItem: GroupRequestItem) {
        val db = FirebaseFirestore.getInstance()
        val userId = requestItem.senderId // The ID of the user who sent the join request

        // Add user to group members
        db.collection("groups").document(groupId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                // Remove the join request after adding the user to the group
                removeJoinRequest(groupId, requestItem)

                // Notify the owner that the user has been added
                Toast.makeText(requireContext(), "User added to group", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("ManageGroupsFragment", "Error adding user to group: ${e.message}")
            }

        // Update users's groups
        val userRef = db.collection("users").document(userId)
        db.collection("users").document(userId)
            .update("joinedGroups", FieldValue.arrayUnion(groupId))
            .addOnSuccessListener {
                userRef.update("numGroups", FieldValue.increment(1))
                Toast.makeText(
                    requireContext(),
                    "Memeber has been added to group",
                    Toast.LENGTH_SHORT
                )
            }
            .addOnFailureListener { e ->
                Log.w("CreateGroup", "Error adding group to user: ${e.message}")
            }
    }

    // Handle rejecting a join request by simply removing the request
    private fun handleRejectRequest(groupId: String, requestItem: GroupRequestItem) {
        // Just remove the join request without adding the user
        removeJoinRequest(groupId, requestItem)
        Toast.makeText(requireContext(), "Join request rejected", Toast.LENGTH_SHORT).show()
    }

    // Remove a join request from Firestore
    private fun removeJoinRequest(groupId: String, requestItem: GroupRequestItem) {
        val db = FirebaseFirestore.getInstance()

        // Look for the document matching the join request to be removed
        db.collection("groups").document(groupId)
            .collection("requests")
            .whereEqualTo("senderId", requestItem.senderId) // Find by sender's ID
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    // Delete the request document
                    document.reference.delete()
                }

                // Refresh the list of groups after removal
                loadGroupsFromFirestore()
            }
            .addOnFailureListener { e ->
                Log.w("ManageGroupsFragment", "Error removing join request: ${e.message}")
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove Firestore listener when fragment view is destroyed
        groupListener?.remove()
    }


}