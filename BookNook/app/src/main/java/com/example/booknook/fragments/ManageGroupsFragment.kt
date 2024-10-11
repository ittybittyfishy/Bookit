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
    private lateinit var groupAdapter: GroupAdapter
    private val groupList = mutableListOf<GroupItem>()

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

        createGroup.setOnClickListener()
        {
            val createGroupDialog = CreateGroupFragment()
            createGroupDialog.show(childFragmentManager, "CreateGroupDialog")
        }

        // navigation buttons
        myGroups = view.findViewById(R.id.myGroups)
        findGroups = view.findViewById(R.id.findGroups)

        myGroups.setOnClickListener()
        {
            val groupsFragment = GroupsFragment()
            (activity as MainActivity).replaceFragment(groupsFragment, "My Groups")
        }

        findGroups.setOnClickListener()
        {
            val findGroupsFragment = FindGroupFragment()
            (activity as MainActivity).replaceFragment(findGroupsFragment, "Find Groups")
        }

        // Recycler View
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        groupAdapter = GroupAdapter(groupList) { groupItem ->
            // Handle group item click
            Toast.makeText(context, "Clicked: ${groupItem.groupName}", Toast.LENGTH_SHORT).show()
        }
        recyclerView.adapter = groupAdapter

        loadGroupsFromFirestore()

    }

    private fun loadGroupsFromFirestore() {
        val db = FirebaseFirestore.getInstance()

        val userId = FirebaseAuth.getInstance().currentUser?.uid

        db.collection("groups").get()
            .addOnSuccessListener { documents ->
                groupList.clear()  // Clear any existing data

                for (document in documents) {
                    // Retrieve the members list from the document
                    val owner = document.get("createdBy") as? String

                    // Check if user is the owner
                    if (owner == userId) {
                        val group = document.toObject(GroupItem::class.java).copy(id = document.id)
                        groupList.add(group)
                    }
                }

                groupAdapter.notifyDataSetChanged()  // Update RecyclerView with new data
            }
            .addOnFailureListener { e ->
                Log.w("GroupsFragment", "Error loading groups: ${e.message}")
            }
    }
}