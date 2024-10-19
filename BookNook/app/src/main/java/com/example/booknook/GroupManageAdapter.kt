package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.CollectionAdapter.CollectionViewHolder

class GroupManageAdapter(
private val requests: MutableList<GroupRequestHolderItem>,
private val onAcceptClick: (String, GroupRequestItem) -> Unit,
private val onRejectClick: (String, GroupRequestItem) -> Unit
) : RecyclerView.Adapter<GroupManageAdapter.GroupManageViewHolder>() {

    class GroupManageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.groupName)
        val groupRecyclerView: RecyclerView = itemView.findViewById(R.id.groupsRecyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupManageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group_management, parent, false)
        return GroupManageViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupManageViewHolder, position: Int) {
        val groupItem = requests[position]

        // Set group name
        holder.groupName.text = groupItem.groupName

        // Setup inner RecyclerView
        holder.groupRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.groupRecyclerView.adapter = GroupRequestAdapter(
            groupItem.requests,
            onAcceptClick = { requestItem ->
                onAcceptClick(groupItem.groupId, requestItem)
            },
            onRejectClick = { requestItem ->
                onRejectClick(groupItem.groupId, requestItem)
            }
        )
    }

    override fun getItemCount(): Int = requests.size
}