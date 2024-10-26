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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class GroupsFragment : Fragment() {

    lateinit var findGroups: Button
    lateinit var manageGroups: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private val groupList = mutableListOf<GroupItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // initalize buttons
        findGroups = view.findViewById(R.id.findGroups)
        manageGroups = view.findViewById(R.id.manageGroups)

        // Recycler View
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        groupAdapter = GroupAdapter(groupList) { groupItem ->
            // Handle group item click
            Toast.makeText(context, "Clicked: ${groupItem.groupName}", Toast.LENGTH_SHORT).show()
            val groupHomepageFragment = GroupHomepageFragment()
            (activity as MainActivity).replaceFragment(groupHomepageFragment, "${groupItem.groupName}")
        }
        recyclerView.adapter = groupAdapter


        // set listener
        findGroups.setOnClickListener()
        {
            val findGroupFragment = FindGroupFragment()
            (activity as MainActivity).replaceFragment(findGroupFragment, "Find Groups")
        }

        manageGroups.setOnClickListener()
        {
            val manageGroupFragment = ManageGroupsFragment()
            (activity as MainActivity).replaceFragment(manageGroupFragment, "Manage Groups")
        }

        // Listen for group update result
        parentFragmentManager.setFragmentResultListener("groupUpdated", this) { _, bundle ->
            val shouldRefresh = bundle.getBoolean("refresh")
            if (shouldRefresh) {
                loadGroupsFromFirestore() // Refresh the dataset when group info is updated
            }
        }

        // Load groups
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
                    val members = document.get("members") as? List<String>

                    // Check if members is not null and contains the userId
                    if (members != null && members.contains(userId)) {
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