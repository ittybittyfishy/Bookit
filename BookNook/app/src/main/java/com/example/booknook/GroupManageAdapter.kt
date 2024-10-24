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

        // get group id
        var groupId = groupItem.groupId

        // Set group name
        holder.groupName.text = groupItem.groupName

        // allow user to edit groups
        // Allow user to edit groups
        holder.editButton.setOnClickListener {
            val editFragment = EditGroupFragment.newInstance(groupId)
            editFragment.show(
                fragmentManager, // Use the passed FragmentManager
                "EditGroupDialog"
            )

            // Setup the inner RecyclerView for displaying group requests
            holder.groupRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.groupRecyclerView.adapter = GroupRequestAdapter(
                groupItem.requests, // Provide the list of requests for this group
                onAcceptClick = { requestItem -> // Handle accept click for individual requests
                    onAcceptClick(groupItem.groupId, requestItem)
                },
                onRejectClick = { requestItem -> // Handle reject click for individual requests
                    onRejectClick(groupItem.groupId, requestItem)
                }
            )
        }
    }

    // Method to get the total number of items in the adapter
    override fun getItemCount(): Int = requests.size
}