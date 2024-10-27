package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.CollectionAdapter.CollectionViewHolder
import com.example.booknook.fragments.EditGroupFragment

class GroupManageAdapter(
private val requests: MutableList<GroupRequestHolderItem>,
private val onAcceptClick: (String, GroupRequestItem) -> Unit, // Callback for accepting a request
private val onRejectClick: (String, GroupRequestItem) -> Unit, // Callback for rejecting a request
private val fragmentManager: FragmentManager
) : RecyclerView.Adapter<GroupManageAdapter.GroupManageViewHolder>() {

    // ViewHolder class to hold references to the views for each item
    class GroupManageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.groupName)
        val groupRecyclerView: RecyclerView = itemView.findViewById(R.id.groupsRecyclerView)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupManageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_management, parent, false)
        return GroupManageViewHolder(view)
    }

    // Method to bind data to the ViewHolder
    override fun onBindViewHolder(holder: GroupManageViewHolder, position: Int) {
        val groupItem = requests[position]

        // Set group name
        holder.groupName.text = groupItem.groupName

        // Initialize inner RecyclerView with horizontal layout for group requests
        holder.groupRecyclerView.layoutManager = LinearLayoutManager(
            holder.itemView.context,
            LinearLayoutManager.HORIZONTAL, // Set to horizontal orientation
            false
        )
        holder.groupRecyclerView.adapter = GroupRequestAdapter(
            groupItem.requests,
            onAcceptClick = { requestItem ->
                onAcceptClick(groupItem.groupId, requestItem)
            },
            onRejectClick = { requestItem ->
                onRejectClick(groupItem.groupId, requestItem)
            }
        )

        // Edit button click to show edit fragment
        holder.editButton.setOnClickListener {
            val editFragment = EditGroupFragment.newInstance(groupItem.groupId)
            editFragment.show(
                fragmentManager,
                "EditGroupDialog"
            )
        }
    }

    // Method to get the total number of items in the adapter
    override fun getItemCount(): Int = requests.size
}