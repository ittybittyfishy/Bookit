package com.example.booknook.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.booknook.MainActivity
import com.example.booknook.R


class ManageGroupsFragment : Fragment() {

    // navigation buttons
    private lateinit var myGroups: Button
    private lateinit var findGroups: Button

    // page UI elements
    private lateinit var createGroup: Button

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

    }
}