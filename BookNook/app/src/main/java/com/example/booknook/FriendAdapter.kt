package com.example.booknook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder

class FriendAdapter(private val friends: List<Friend>,
    private val onFriendClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(){

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.friend_username)  // Initialize username view
    }

    // Inflates layout for each friend  item and returns new FriendViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        // Gets view of individual friend item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)  // Returns the view holder with the view
    }

    // Binds data to view holder
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friends[position]
        holder.username.text = friend.friendUsername  // Sets username text

        // Allows user to click on each of their friends
        holder.itemView.setOnClickListener {
            onFriendClick(friend)
        }
    }

    override fun getItemCount(): Int = friends.size  // returns number of items in friends list
}