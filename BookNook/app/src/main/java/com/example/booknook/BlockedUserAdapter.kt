package com.example.booknook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder

class BlockedUserAdapter(private val blockedUsers: List<BlockedUser>
) : RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder>(){

    class BlockedUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.blocked_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockedUserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blocked_user, parent, false)
        return BlockedUserViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlockedUserViewHolder, position: Int) {
        val blockedUser = blockedUsers[position]
        holder.username.text = blockedUser.blockedUsername  // Sets username text
    }

    override fun getItemCount(): Int = blockedUsers.size  // returns number of items in blocked users list
}