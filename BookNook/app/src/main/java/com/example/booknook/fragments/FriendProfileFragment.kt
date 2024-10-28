package com.example.booknook.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import androidx.appcompat.widget.PopupMenu
import com.example.booknook.MainActivity
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import org.w3c.dom.Text


class FriendProfileFragment : Fragment() {

    // UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var addFriendButton: Button
    private lateinit var userUsername: TextView
    private lateinit var quoteEditText: EditText
    private lateinit var characterEditText: EditText

    // Firebase instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // Friend's user ID (to be passed as an argument)
    private var friendUserId: String? = null
    private var friendUsername: String? = null

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
    private lateinit var numGroupsTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the friend's user ID from the arguments
        friendUserId = arguments?.getString("receiverId")
        friendUsername = arguments?.getString("receiverUsername")
        if (friendUserId == null) {
            Toast.makeText(activity, "Friend user ID not provided", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile_friend, container, false)

        // Initialize UI elements
        bannerImage = view.findViewById(R.id.bannerImage)
        profileImage = view.findViewById(R.id.profileImage)
        addFriendButton = view.findViewById(R.id.addFriendButton)
        userUsername = view.findViewById(R.id.userUsername)
        quoteEditText = view.findViewById(R.id.rectangle4)
        characterEditText = view.findViewById(R.id.rectangle5)

        //load in stats UI
        numCollectionsTextView = view.findViewById(R.id.numCollectionsTextView)
        numBooksReadTextView = view.findViewById(R.id.numBooksReadTextView)
        topGenresTextView = view.findViewById(R.id.topGenresTextView)
        favoriteTagTextView = view.findViewById(R.id.favoriteTagTextView)
        averageRatingTextView = view.findViewById(R.id.averageRatingTextView)
        numReviewsTextView = view.findViewById(R.id.numReviewsTextView)
        numFriendsTextView = view.findViewById(R.id.numFriendsTextView)
        numGroupsTextView = view.findViewById(R.id.numGroupsTextView)

        //block user menu
        threeDotsButton = view.findViewById(R.id.threeDotsButton)

        threeDotsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Disable editing on EditTexts
        quoteEditText.isEnabled = false
        characterEditText.isEnabled = false

        // Load friend's data
        if (friendUserId != null) {
            loadFriendData(friendUserId!!)
        } else {
            Toast.makeText(activity, "Friend user ID not provided", Toast.LENGTH_SHORT).show()
        }


        // Load initial friend status
        friendUserId?.let { friendId ->
            checkFriendshipStatus(friendId)  // Check the initial status
        }

        // Set up Add Friend button
        addFriendButton.setOnClickListener {
            friendUserId?.let { friendId ->
                if (addFriendButton.text == "Unfriend") {
                    removeFriend(friendId)
                } else {
                    sendFriendRequest(friendId)
                }
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user
        val receiverId = arguments?.getString("receiverId")  // Retrieves the receiver's id from friends fragment arguments
        addFriendButton = view.findViewById(R.id.addFriendButton)  // Calls view for the Add Friend Button

    }

    // Function to send a friend request
    private fun sendFriendRequest(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser  // Gets the current user

        if (currentUser != null) {
            val senderId = currentUser.uid  // senderId is the current user
            val senderRef = db.collection("users").document(senderId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    // creates a map of friend request details
                    val friendRequest = hashMapOf(
                        "senderId" to senderId,
                        "senderUsername" to senderUsername,
                        "receiverId" to receiverId,
                        "status" to "pending"
                    )

                    // Update receiver's friend requests array in database
                    db.collection("users").document(receiverId)
                        .update("friendRequests", FieldValue.arrayUnion(friendRequest))
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Friend request sent", Toast.LENGTH_SHORT).show()
                            addFriendButton.text = "Pending"
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(activity, "Failed to send friend request: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "Sender username not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Function to remove a friend
    private fun removeFriend(friendId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUserRef = currentUserId?.let { db.collection("users").document(it) }

        if (currentUserRef != null) {
            // Fetch the current user's username
            currentUserRef.get().addOnSuccessListener { userDoc ->
                val currentUsername = userDoc.getString("username") ?: return@addOnSuccessListener

                // Create a map for the friend to be removed
                val friendToRemove = hashMapOf("friendId" to friendId, "friendUsername" to friendUsername)

                // Remove the friend from the current user's friends list
                currentUserRef.update("friends", FieldValue.arrayRemove(friendToRemove))
                    .addOnSuccessListener {
                        decrementNumFriends(currentUserRef)

                        // Now remove the current user from the friend's friends list
                        val friendRef = db.collection("users").document(friendId)
                        val currentUserAsFriend = hashMapOf("friendId" to currentUserId, "friendUsername" to currentUsername)

                        friendRef.update("friends", FieldValue.arrayRemove(currentUserAsFriend))
                            .addOnSuccessListener {
                                decrementNumFriends(friendRef)
                                context?.let { Toast.makeText(activity, "Friend removed from both users", Toast.LENGTH_SHORT).show() }
                                addFriendButton.text = "Add Friend"
                            }
                            .addOnFailureListener { e ->
                                context?.let{ Toast.makeText(activity, "Failed to remove friend from their list: ${e.message}", Toast.LENGTH_SHORT).show() }
                            }
                    }
                    .addOnFailureListener { e ->
                        context?.let { Toast.makeText(activity, "Failed to remove friend from your list: ${e.message}", Toast.LENGTH_SHORT).show() }
                    }
            }.addOnFailureListener {
                context?.let { Toast.makeText(activity, "Failed to fetch current user data", Toast.LENGTH_SHORT).show() }
            }
        } else {
            context?.let { Toast.makeText(activity, "Current user is not logged in", Toast.LENGTH_SHORT).show() }
        }
    }



    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_block_user, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_block_user -> {
                    //block user function goes here
                    friendUserId?.let { blockUser(it) }
                    friendUserId?.let { removeFriend(it) }

                    val blockedUserProfileFragment = BlockedUserProfileFragment()
                    val bundle = Bundle().apply {
                        putString("blockedUserId", friendUserId)  // Pass the receiver's id into the bundle
                        putString("blockedUsername", friendUsername)
                    }
                    blockedUserProfileFragment.arguments = bundle
                    (activity as MainActivity).replaceFragment(blockedUserProfileFragment, "Profile")
                    true
                }
                else -> false
            }
        }
        popup.show()
    }



    private fun loadFriendData(friendId: String) {
        val userDocRef = firestore.collection("users").document(friendId)
        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val favoriteQuote = document.getString("favoriteQuote") ?: ""
                    val favoriteCharacter = document.getString("favoriteCharacter") ?: ""
                    val profileImageUrl = document.getString("profileImageUrl")
                    val bannerImageUrl = document.getString("bannerImageUrl")
                    val numCollections = document.getLong("numCollections") ?: 0
                    val numBooksRead = document.getLong("numBooksRead") ?: 0
                    val favoriteTag = document.getString("favoriteTag") ?: "N/A"
                    val topGenres = document.get("topGenres") as? List<String> ?: listOf()
                    val averageRating = document.getDouble("averageRating") ?: 0.0
                    val numReviews = document.getLong("numReviews") ?: 0
                    val numFriends = document.getLong("numFriends") ?: 0
                    val numGroups = document.getLong("numGroups") ?: 0

                    // Set the username and other data to the views
                    userUsername.text = username ?: "No Username"
                    quoteEditText.setText(favoriteQuote)
                    characterEditText.setText(favoriteCharacter)

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

                    // Update stats in the corresponding TextViews
                    numCollectionsTextView.text = "$numCollections"
                    numBooksReadTextView.text = "$numBooksRead"
                    favoriteTagTextView.text = favoriteTag
                    topGenresTextView.text = topGenres.joinToString(", ")
                    averageRatingTextView.text = String.format("%.2f", averageRating)
                    numReviewsTextView.text = "$numReviews"
                    numFriendsTextView.text = "$numFriends"
                    numGroupsTextView.text = "$numGroups"
                } else {
                    Toast.makeText(activity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun checkFriendshipStatus(friendId: String) {
        if (currentUserId == null) return

        val currentUserDocRef = firestore.collection("users").document(currentUserId)
        currentUserDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friends = document.get("friends") as? List<Map<String, String>>
                    if (friends != null) {
                        // Loops through each friend to check if they're already friends by comparing friendIds
                        val isFriend = friends.any { friendMap -> friendMap["friendId"] == friendId}
                        if (isFriend) {
                            // Already friends
                            addFriendButton.text = "Unfriend"
                            Toast.makeText(activity, "Already friends", Toast.LENGTH_SHORT).show()
                        } else {
                            // Not friends
                            addFriendButton.text = "Add Friend"
                            Toast.makeText(activity, "Not friends", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        addFriendButton.text = "Add friend"
                        Toast.makeText(activity, "No friends found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error checking friendship status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to block a user
    private fun blockUser(receiverId: String) {
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
                                .update("blockedUsers", FieldValue.arrayUnion(blockedUser))
                                .addOnSuccessListener {
                                    context?.let { Toast.makeText(it, "User blocked", Toast.LENGTH_SHORT).show() }
                                }
                                .addOnFailureListener { e ->
                                    context?.let { Toast.makeText(it, "Failed to block user: ${e.message}", Toast.LENGTH_SHORT).show() }
                                }
                        } else {
                            context?.let { Toast.makeText(it, "Blocked user's username not found", Toast.LENGTH_SHORT).show() }
                        }
                    }
                } else {
                    context?.let { Toast.makeText(it, "Sender username not found", Toast.LENGTH_SHORT).show() }
                }
            }.addOnFailureListener {
                context?.let { Toast.makeText(it, "Failed to block user", Toast.LENGTH_SHORT).show() }
            }
        } else {
            context?.let { Toast.makeText(it, "User not authenticated", Toast.LENGTH_SHORT).show() }
        }
    }

    // Function to decrement numFriends in database
    private fun decrementNumFriends(userRef: DocumentReference) {
        userRef.update("numFriends", FieldValue.increment(-1))
    }


    companion object {
        private const val ARG_FRIEND_USER_ID = "friendUserId"

        fun newInstance(friendUserId: String): FriendProfileFragment {
            val fragment = FriendProfileFragment()
            val args = Bundle()
            args.putString(ARG_FRIEND_USER_ID, friendUserId)
            fragment.arguments = args
            return fragment
        }
    }
}
