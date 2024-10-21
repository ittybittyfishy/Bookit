package com.example.booknook.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.MainActivity
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class BlockedUserProfileFragment : Fragment() {
    // Declare variables for UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var userUsername: TextView
    private lateinit var quoteEditText: EditText
    private lateinit var characterEditText: EditText

    // Friend's user ID (to be passed as an argument)
    private var blockedUserId: String? = null
    private var blockedUsername: String? = null

    //block user menu
    private lateinit var threeDotsButton: ImageButton

    //load in stats
    private lateinit var numCollectionsTextView: TextView
    private lateinit var numBooksReadTextView: TextView
    private lateinit var topGenresTextView: TextView
    private lateinit var favoriteTagTextView: TextView
    private lateinit var averageRatingTextView: TextView
    private lateinit var numReviewsTextView: TextView
    private lateinit var numFriendsTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        blockedUserId = arguments?.getString("blockedUserId")  // Receives blockedUserId from blocked fragment
        blockedUsername = arguments?.getString("blockedUsername")

        if (blockedUserId == null) {
            Toast.makeText(activity, "Blocked user ID not provided", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
    }

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_blocked_user_profile, container, false)


        // Initialize UI elements
        bannerImage = view.findViewById(R.id.bannerImage)
        profileImage = view.findViewById(R.id.profileImage)
        userUsername = view.findViewById(R.id.userUsername)

        //block user menu
        threeDotsButton = view.findViewById(R.id.threeDotsButton)

        threeDotsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Load blocked user's data
        if (blockedUserId != null) {
            loadUserData(blockedUserId!!)
        } else {
            Toast.makeText(activity, "Blocked user ID not provided", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user
        val receiverId = arguments?.getString("receiverId")  // Retrieves the receiver's id from friends fragment arguments
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_unblock_user, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_unblock_user -> {
                    //unblock user function goes here
                    blockedUserId?.let { unblockUser(it) }

                    val friendProfileFragment = FriendProfileFragment()
                    val bundle = Bundle().apply {
                        putString("receiverId", blockedUserId)  // Pass the receiver's id into the bundle
                        putString("receiverUsername", blockedUsername)
                    }
                    friendProfileFragment.arguments = bundle
                    (activity as MainActivity).replaceFragment(friendProfileFragment, "Profile")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun loadUserData(friendId: String) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(friendId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val profileImageUrl = document.getString("profileImageUrl")
                    val bannerImageUrl = document.getString("bannerImageUrl")

                    // Set the username and other data to the views
                    userUsername.text = username ?: "No Username"

                    // Ensure the fragment is attached before loading images with Glide
                    if (isAdded && activity != null) {
                        // Load images using Glide
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profileImageUrl)
                                .into(profileImage)
                        }

                        if (!bannerImageUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(bannerImageUrl)
                                .into(bannerImage)
                        }
                    } else {
                        context?.let { Toast.makeText(activity, "Fragment is not attached", Toast.LENGTH_SHORT).show() }
                    }
                } else {
                    context?.let { Toast.makeText(activity, "User does not exist", Toast.LENGTH_SHORT).show() }
                }
            }
            .addOnFailureListener { e ->
                context?.let { Toast.makeText(activity, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show() }
            }
    }


    // Function to unblock a user
    private fun unblockUser(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user

        if (currentUser != null) {
            val senderId = currentUser.uid  // senderId is the current user
            val senderRef = db.collection("users").document(senderId)  // gets the sender's document
            val blockedUserRef = db.collection("users").document(receiverId)  // gets the blocked user's document

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")  // gets the sender's username

                if (senderUsername != null) {
                    blockedUserRef.get().addOnSuccessListener { blockedUserDoc ->
                        val blockedUsername = blockedUserDoc?.getString("username")  // gets the blocked user's username

                        if (blockedUsername != null) {
                            // creates a map of blocked user's details
                            val blockedUser = hashMapOf(
                                "blockedUserId" to receiverId,
                                "blockedUsername" to blockedUsername
                            )

                            // Update current user's blocked users array in database
                            db.collection("users").document(senderId)
                                .update("blockedUsers", FieldValue.arrayRemove(blockedUser))
                                .addOnSuccessListener {
                                    context?.let { Toast.makeText(activity, "User unblocked", Toast.LENGTH_SHORT).show() }
                                }
                                .addOnFailureListener { e -> Toast.makeText(activity, "Failed to unblock user: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            context?.let { Toast.makeText(activity, "Blocked user's username not found", Toast.LENGTH_SHORT).show() }
                        }
                    }
                } else {
                    context?.let { Toast.makeText(activity, "Sender username not found", Toast.LENGTH_SHORT).show() }
                }
            }.addOnFailureListener {
                context?.let { Toast.makeText(activity, "Failed to unblock user", Toast.LENGTH_SHORT).show() }
            }
        } else {
            context?.let { Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show() }
        }
    }
}
