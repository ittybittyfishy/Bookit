package com.example.booknook
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.FriendRequestAdapter.FriendRequestViewHolder
import com.example.booknook.fragments.UserProfileFragment
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class BlockedUserAdapter(private val blockedUsers: List<BlockedUser>
) : RecyclerView.Adapter<BlockedUserAdapter.BlockedUserViewHolder>(){

    // References view of username in recycler view
    class BlockedUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.blocked_username)
        val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
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
        loadProfileImage(blockedUser.blockedUserId, holder.profileImage)

        // Allows user to click on each of their blocked users
        holder.itemView.setOnClickListener {
            val userProfileFragment = UserProfileFragment()
            val bundle = Bundle().apply {
                putString("receiverId", blockedUser.blockedUserId)  // Pass the blocked user's id into the bundle
            }
            userProfileFragment.arguments = bundle  // Set the arguments to the bundle
            (holder.itemView.context as MainActivity).replaceFragment(userProfileFragment, "${blockedUser.blockedUsername}")
        }
    }

    override fun getItemCount(): Int = blockedUsers.size  // returns number of items in blocked users list

    // Loads the blocked user's profile picture
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