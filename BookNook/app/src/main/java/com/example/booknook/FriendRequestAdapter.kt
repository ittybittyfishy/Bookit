package com.example.booknook
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.booknook.fragments.FriendProfileFragment
import com.example.booknook.fragments.UserProfileFragment
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class FriendRequestAdapter(private val friendRequests: List<FriendRequest>,
    private val onAcceptClick: (FriendRequest) -> Unit,
    private val onRejectClick: (FriendRequest) -> Unit
) : RecyclerView.Adapter<FriendRequestAdapter.FriendRequestViewHolder>(){

    // References views in each item of the recycler view
    class FriendRequestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val username: TextView = itemView.findViewById(R.id.friend_req_username)
        val acceptButton: ImageButton = itemView.findViewById(R.id.accept_button)
        val rejectButton: ImageButton = itemView.findViewById(R.id.reject_button)
        val profileImage: CircleImageView = itemView.findViewById(R.id.profileImage)
    }

    // Inflates layout for each friend request item and returns new FriendRequestViewHolder instance
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendRequestViewHolder {
        // Gets view of individual friend request item
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_friend_request, parent, false)
        return FriendRequestViewHolder(view)
    }

    // Binds data to view holder
    override fun onBindViewHolder(holder: FriendRequestViewHolder, position: Int) {
        val friendRequest = friendRequests[position]
        loadProfileImage(friendRequest.senderId, holder.profileImage)
        holder.username.text = friendRequest.username  // Sets username text

        // Allows user to click on each of their requests
        holder.itemView.setOnClickListener {
            val userProfileFragment = UserProfileFragment()
            val bundle = Bundle().apply {
                putString("receiverId", friendRequest.receiverId)  // Pass the receiver's id into the bundle
            }
            userProfileFragment.arguments = bundle  // Set the arguments to the bundle
            (holder.itemView.context as MainActivity).replaceFragment(userProfileFragment, "${friendRequest.username}")
        }

        // Setting up click listeners for accept and reject buttons
        holder.acceptButton.setOnClickListener {
            onAcceptClick(friendRequest)
        }

        holder.rejectButton.setOnClickListener {
            onRejectClick(friendRequest)
        }
    }

    override fun getItemCount(): Int = friendRequests.size  // returns number of items in friend requests list

    // Loads the user's profile picture
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
