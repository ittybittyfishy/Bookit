package com.example.booknook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder

class BlockedUserAdapter(private val blockedUsers: List<BlockedUser>
) : RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder>(){

    // References view of username in recycler view
    class BlockedUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.blocked_username)
    }

    // Inflates layout for each blocked user item and returns new FriendRequestViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        // Gets view of individual blocked user item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blocked_user, parent, false)
        return BlockedUserViewHolder(view)
    }

    // Binds data to view holder
    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) {
        val blockedUser = blockedUsers[position]
        holder.username.text = blockedUser.blockedUsername  // Sets username text
    }

    override fun getItemCount(): Int = blockedUsers.size  // returns number of items in blocked users list
}