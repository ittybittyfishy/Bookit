package com.example.booknook

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder
import de.hdodenhof.circleimageview.CircleImageView

class GroupRequestAdapter(
    private val requestList: List<GroupRequestItem>,
    private val onAcceptClick: (GroupRequestItem) -> Unit,
    private val onRejectClick: (GroupRequestItem) -> Unit
) : RecyclerView.Adapter<GroupRequestAdapter.GroupRequestViewHolder>() {

    // ViewHolder class to hold the views for each item
    class GroupRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: CircleImageView = itemView.findViewById(R.id.sendersProfileImage)
        val usernameTextView: TextView = itemView.findViewById(R.id.group_req_username)
        val acceptButton: ImageButton = itemView.findViewById(R.id.accept_button)
        val rejectButton: ImageButton = itemView.findViewById(R.id.reject_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_request, parent, false)
        return GroupRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupRequestViewHolder, position: Int) {
        val requestItem = requestList[position]

        // Set the username
        holder.usernameTextView.text = requestItem.senderUsername

        // Placeholder for setting profile image (if you have image URL, you can use Glide/Picasso)
        holder.profileImage.setImageResource(R.drawable.profile_picture_placeholder)

        // Handle accept button click
        holder.acceptButton.setOnClickListener {
            onAcceptClick(requestItem)
        }

        // Handle reject button click
        holder.rejectButton.setOnClickListener {
            onRejectClick(requestItem)
        }
    }

    override fun getItemCount(): Int {
        return requestList.size
    }
}