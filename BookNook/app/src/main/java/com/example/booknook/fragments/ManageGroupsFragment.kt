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
import com.google.firebase.firestore.FirebaseFirestore


class ManageGroupsFragment : Fragment() {

    // navigation buttons
    private lateinit var myGroups: Button
    private lateinit var findGroups: Button

    // page UI elements
    private lateinit var createGroup: Button
    private lateinit var recyclerView: RecyclerView

    private lateinit var groupManageAdapter: GroupManageAdapter
    private val groupList = mutableListOf<GroupRequestHolderItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_manage_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Page UI elements
        createGroup = view.findViewById(R.id.createGroupButton)

        createGroup.setOnClickListener {
            val createGroupDialog = CreateGroupFragment()
            createGroupDialog.show(childFragmentManager, "CreateGroupDialog")
        }

        // Navigation buttons
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

        // Recycler View setup
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize adapter
        groupManageAdapter = GroupManageAdapter(groupList)
        recyclerView.adapter = groupManageAdapter

        // Load data from Firestore
        loadGroupsFromFirestore()
    }

    private fun loadGroupsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("groups").get()
            .addOnSuccessListener { documents ->
                groupList.clear()

                for (document in documents) {
                    val owner = document.getString("createdBy")

                    if (owner == userId) {
                        val groupName = document.getString("groupName") ?: "Unknown Group"
                        val groupId = document.id

                        // Fetch requests for this group
                        db.collection("groups").document(groupId).collection("requests")
                            .get()
                            .addOnSuccessListener { requestDocs ->
                                val requests = requestDocs.map { requestDoc ->
                                    requestDoc.toObject(GroupRequestItem::class.java)
                                }.toMutableList() // Convert to MutableList

                                // Create GroupRequestHolderItem containing group name and requests
                                val groupRequestHolderItem = GroupRequestHolderItem(
                                    groupName = groupName,
                                    requests = requests // Now it should be mutable
                                )

                                groupList.add(groupRequestHolderItem)
                                groupManageAdapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener { e ->
                                Log.w(
                                    "ManageGroupsFragment",
                                    "Error loading requests: ${e.message}"
                                )
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.w("ManageGroupsFragment", "Error loading groups: ${e.message}")
            }
    }

}