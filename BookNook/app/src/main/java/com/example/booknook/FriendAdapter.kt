package com.example.booknook
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class FriendAdapter(private val friends: List<Friend>,
    private val onFriendClick: (Friend) -> Unit
) : RecyclerView.Adapter<FriendAdapter.FriendViewHolder>(){

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.friend_username)  // Initialize username view
        val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
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
        loadProfileImage(friend.friendId, holder.profileImage)

        // Allows user to click on each of their friends
        holder.itemView.setOnClickListener {
            onFriendClick(friend)
        }
    }

    override fun getItemCount(): Int = friends.size  // returns number of items in friends list

    // Loads the friend's profile picture
    private fun loadProfileImage(senderId: String, imageView: CircleImageView) {
        val db = FirebaseFirestore.getInstance()

        // Fetch the profile image URL from Firestore
        db.collection("users").document(senderId).get().addOnSuccessListener { document ->
            val profileImageUrl = document.getString("profileImageUrl")
            if (!profileImageUrl.isNullOrEmpty()) {
                // Use Glide to load the image
                Glide.with(imageView.context)
                    .load(profileImageUrl)
                    .placeholder(R.drawable.profile_picture_placeholder)  // Placeholder while loading
                    .error(R.drawable.profile_picture_placeholder)       // Fallback in case of error
                    .into(imageView)
            } else {
                // Set a placeholder if no profile image URL is found
                imageView.setImageResource(R.drawable.profile_picture_placeholder)
            }
        }.addOnFailureListener {
            // Handle errors and set a placeholder if the request fails
            imageView.setImageResource(R.drawable.profile_picture_placeholder)
        }
    }
}