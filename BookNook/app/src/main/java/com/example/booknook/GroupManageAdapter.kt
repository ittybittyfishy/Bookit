package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.CollectionAdapter.CollectionViewHolder

class GroupManageAdapter(private val requests: MutableList<GroupRequestHolderItem>) : RecyclerView.Adapter<GroupManageAdapter.GroupManageViewHolder>() {

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
        val groupRequestHolder = requests[position]
        holder.groupName.text = groupRequestHolder.groupName

        // Setup inner RecyclerView with onClick listeners for accept/reject
        holder.groupRecyclerView.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.groupRecyclerView.adapter = GroupRequestAdapter(
            groupRequestHolder.requests,
            onAcceptClick = { requestItem ->
                // Handle accept click here
                Toast.makeText(holder.itemView.context, "Accepted: ${requestItem.senderId}", Toast.LENGTH_SHORT).show()
            },
            onRejectClick = { requestItem ->
                // Handle reject click here
                Toast.makeText(holder.itemView.context, "Rejected: ${requestItem.senderId}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun getItemCount(): Int = requests.size


}