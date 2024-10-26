package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.GroupAdapter
import com.example.booknook.GroupItem
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupsFragment : Fragment() {

    lateinit var findGroups: Button
    lateinit var manageGroups: Button

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private val groupList = mutableListOf<GroupItem>()

    private lateinit var sortGroups: Spinner

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
            openGroupHomepage(groupItem)
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

        sortGroups = view.findViewById(R.id.sortGroups)
        setupSortSpinner()

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

    // Setup the spinner for sorting options and handle the selection event
    private fun setupSortSpinner() {
        // Create an ArrayAdapter using the string array and custom spinner item layout
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.groups_sort_options,
            R.layout.item_collections_spinner_layout  // Custom layout for the spinner display
        )
        // Apply the adapter to the spinner
        adapter.setDropDownViewResource(R.layout.item_collections_spinner_dropdown) // The layout for dropdown items
        sortGroups.adapter = adapter

        // Handle selection changes as before
        sortGroups.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedSortOption = parent.getItemAtPosition(position).toString()
                sortGroups(selectedSortOption)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No action if nothing is selected
            }
        }
    }

    // Function to sort books within each custom collection based on the selected option
    private fun sortGroups(sortOption: String) {
        when (sortOption) {
            "Name A-Z" -> {
                // Sort groupList alphabetically by group name
                groupList.sortBy { it.groupName }
            }
            "Name Z-A" -> {
                // Sort groupList in reverse alphabetical order by group name
                groupList.sortByDescending { it.groupName }
            }
            "Members ↑" -> {
                // Sort groupList alphabetically by creater name
                groupList.sortBy {it.members.size}
            }
            "Members ↓" -> {
                groupList.sortByDescending {it.members.size}
            }
        }

        // Notify the adapter about the updated data
        groupAdapter.notifyDataSetChanged()
    }

    // Veronica Nguyen
    // Function to open the group homepage
    private fun openGroupHomepage(groupItem: GroupItem) {
        val myGroupsHomepageFragment = MyGroupsHomepageFragment()
        val bundle = Bundle()
        // Passes the group id and group creator id to the fragment
        bundle.putString("GROUP_ID", groupItem.id)
        bundle.putString("GROUP_CREATOR_ID", groupItem.createdBy)
        myGroupsHomepageFragment.arguments = bundle
        // Navigates to the homepage of the group
        (activity as MainActivity).replaceFragment(myGroupsHomepageFragment, "${groupItem.groupName}")
    }


}