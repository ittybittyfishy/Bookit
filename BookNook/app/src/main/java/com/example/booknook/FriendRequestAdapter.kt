package com.example.booknook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class FriendRequestAdapter(private val friendRequests: List<FriendRequest>,
    private val onAcceptClick: (FriendRequest) -> Unit,
    private val onRejectClick: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>(){

    class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val username: TextView = itemView.findViewById(R.id.friend_req_username)
        val acceptButton: Button = itemView.findViewById(R.id.accept_button)
        val rejectButton: Button = itemView.findViewById(R.id.reject_button)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        holder.username.text = friendRequest.username

        holder.acceptButton.setOnClickListener {
            onAcceptClick(friendRequest)
        }

        holder.rejectButton.setOnClickListener {
            onRejectClick(friendRequest)
        }
    }

    override fun getItemCount(): Int = friendRequests.size
}
