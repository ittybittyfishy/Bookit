package com.example.booknook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder

class FriendAdapter(private val friends: List<Friend>
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(){

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.friend_username)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.username.text = friend.friendUsername  // Sets username text
    }

    override fun getItemCount(): Int = friends.size  // returns number of items in friends list
}