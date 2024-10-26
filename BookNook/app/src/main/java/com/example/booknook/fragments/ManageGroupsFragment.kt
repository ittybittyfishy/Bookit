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

    // Listener to update the groups in real time
    private var groupListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_manage_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize and set up the "Create Group" button
        createGroup = view.findViewById(R.id.createGroupButton)
        createGroup.setOnClickListener {
            val createGroupDialog = CreateGroupFragment()
            createGroupDialog.show(childFragmentManager, "CreateGroupDialog")
        }

        // Initialize navigation buttons and set click listeners
        myGroups = view.findViewById(R.id.myGroups)
        findGroups = view.findViewById(R.id.findGroups)

        myGroups.setOnClickListener {
            val groupsFragment = GroupsFragment()
            (activity as MainActivity).replaceFragment(groupsFragment, "My Groups")
        }

        findGroups.setOnClickListener {
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

    private fun loadGroupsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Listen to changes in the "groups" collection for groups created by the current user
        groupListener = db.collection("groups")
            .whereEqualTo("createdBy", userId)
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

                        // Fetch join requests for the group
                        db.collection("groups").document(groupId).collection("requests")
                            .get()
                            .addOnSuccessListener { requestDocs ->
                                val requests = requestDocs.map { requestDoc ->
                                    requestDoc.toObject(GroupRequestItem::class.java)
                                }.toMutableList()

                                // Create a holder item containing group information and requests
                                val groupRequestHolderItem = GroupRequestHolderItem(
                                    groupId = groupId,
                                    groupName = groupName,
                                    requests = requests
                                )

                                // Add to the list and update the adapter
                                groupList.add(groupRequestHolderItem)
                                groupManageAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.w("ManageGroupsFragment", "Error loading requests: ${e.message}")
                            }
                    }

                    groupManageAdapter.notifyDataSetChanged() // Refresh the adapter after processing all groups
                }
            }
    }

    private fun handleAcceptRequest(groupId: String, requestItem: GroupRequestItem) {
        val db = FirebaseFirestore.getInstance()
        val userId = requestItem.senderId

        // Add user to group members
        db.collection("groups").document(groupId)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                removeJoinRequest(groupId, requestItem) // Remove the join request
                Toast.makeText(requireContext(), "User added to group", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("ManageGroupsFragment", "Error adding user to group: ${e.message}")
            }

        // Update user's groups
        val userRef = db.collection("users").document(userId)
        userRef.update("joinedGroups", FieldValue.arrayUnion(groupId))
            .addOnSuccessListener {
                userRef.update("numGroups", FieldValue.increment(1))
                Toast.makeText(requireContext(), "Member has been added to group", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("ManageGroupsFragment", "Error adding group to user: ${e.message}")
            }
    }

    private fun handleRejectRequest(groupId: String, requestItem: GroupRequestItem) {
        removeJoinRequest(groupId, requestItem)
        Toast.makeText(requireContext(), "Join request rejected", Toast.LENGTH_SHORT).show()
    }

    private fun removeJoinRequest(groupId: String, requestItem: GroupRequestItem) {
        val db = FirebaseFirestore.getInstance()

        db.collection("groups").document(groupId).collection("requests")
            .whereEqualTo("senderId", requestItem.senderId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete()
                }

                loadGroupsFromFirestore() // Refresh the list of groups
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