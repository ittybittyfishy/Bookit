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

class FriendProfileFragment : Fragment() {

    // UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var addFriendButton: Button
    private lateinit var userUsername: TextView
    private lateinit var quoteEditText: EditText
    private lateinit var characterEditText: EditText

    // UI elements for Experience Title and Achievements
    private lateinit var experienceTitleTextView: TextView
    private lateinit var numAchievementsTextView: TextView
    private lateinit var achievementsDetailsTextView: TextView // Optional for listing achievements
    private lateinit var levelTextView: TextView

    // Firebase instances
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid

    // Friend's user ID (to be passed as an argument)
    private var friendUserId: String? = null
    private var friendUsername: String? = null

    // Block user menu
    private lateinit var threeDotsButton: ImageButton

    // Load in stats
    private lateinit var numCollectionsTextView: TextView
    private lateinit var numBooksReadTextView: TextView
    private lateinit var topGenresTextView: TextView
    private lateinit var favoriteTagTextView: TextView
    private lateinit var averageRatingTextView: TextView
    private lateinit var numReviewsTextView: TextView
    private lateinit var numFriendsTextView: TextView
    private lateinit var numGroupsTextView: TextView

    // Define all achievement fields
    private val achievementFields = listOf(
        "bookGodAchieved",
        "firstChapterAchieved",
        "readingRookieAchieved",
        "storySeekerAchieved",
        "novelNavigatorAchieved",
        "bookEnthusiastAchieved",
        "legendaryLibrarianAchieved",
        "fantasyExplorerAchieved",
        "historianAchieved",
        "mysterySolverAchieved",
        "psychExpertAchieved"
        // Add any additional achievement fields here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the friend's user ID and username from the arguments
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

        // Initialize new TextViews
        experienceTitleTextView = view.findViewById(R.id.experienceTitleTextView)
        numAchievementsTextView = view.findViewById(R.id.numAchievementsTextView)
        levelTextView = view.findViewById(R.id.levelTextView)

        // Initialize stats UI elements
        numCollectionsTextView = view.findViewById(R.id.numCollectionsTextView)
        numBooksReadTextView = view.findViewById(R.id.numBooksReadTextView)
        topGenresTextView = view.findViewById(R.id.topGenresTextView)
        favoriteTagTextView = view.findViewById(R.id.favoriteTagTextView)
        averageRatingTextView = view.findViewById(R.id.averageRatingTextView)
        numReviewsTextView = view.findViewById(R.id.numReviewsTextView)
        numFriendsTextView = view.findViewById(R.id.numFriendsTextView)
        numGroupsTextView = view.findViewById(R.id.numGroupsTextView)

        // Initialize block user menu
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
            checkFriendshipStatus(friendId)
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

        // Optional: Additional setup if needed
    }

    // Function to send a friend request
    private fun sendFriendRequest(receiverId: String) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser // Yunjong Noh

        if (currentUser != null) {
            val senderId = currentUser.uid // Yunjong Noh
            val senderRef = db.collection("users").document(senderId)

            // Fetch sender's username
            senderRef.get().addOnSuccessListener { senderDoc ->
                val senderUsername = senderDoc?.getString("username")

                if (senderUsername != null) {
                    // Create a map of friend request details
                    val friendRequest = hashMapOf(
                        "senderId" to senderId,
                        "senderUsername" to senderUsername,
                        "receiverId" to receiverId,
                        "status" to "pending"
                    )

                    // Update receiver's friend requests array in Firestore
                    db.collection("users").document(receiverId)
                        .update("friendRequests", FieldValue.arrayUnion(friendRequest))
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Friend request sent", Toast.LENGTH_SHORT).show()
                            addFriendButton.text = "Pending"
                            // Yunjong Noh
                            // Send a notification to the receiver
                            sendFriendRequestNotification(receiverId, senderId, senderUsername)
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(activity, "Failed to send friend request: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("FriendProfileFragment", "Failed to send friend request", e)
                        }
                } else {
                    Toast.makeText(activity, "Sender username not found", Toast.LENGTH_SHORT).show()
                    Log.e("FriendProfileFragment", "Sender username not found for ID: $senderId")
                }
            }
        }
    }
    // Yunjong Noh
    // Function to send a friend request notification
    private fun sendFriendRequestNotification(receiverId: String, senderId: String, senderUsername: String) {
        val db = FirebaseFirestore.getInstance()
        val currentTime = System.currentTimeMillis()
        val expirationTime = currentTime + 10 * 24 * 60 * 60 * 1000 // Notification expires in 10 days

        // Create the notification item
        val notification = NotificationItem(
            userId = receiverId, // Receiver's ID
            senderId = senderId,  // Sender's ID
            receiverId = receiverId,  // Receiver's ID
            message = "$senderUsername has sent you a friend request.",
            timestamp = currentTime,
            type = NotificationType.FRIEND_REQUEST,
            dismissed = false,
            expirationTime = expirationTime,
            profileImageUrl = "", // You may fetch and add the profile image URL if needed
            username = senderUsername // Use sender's username
        )

        // Add the notification to Firestore
        db.collection("notifications").add(notification)
            .addOnSuccessListener { documentReference ->
                val notificationId = documentReference.id
                db.collection("notifications").document(notificationId)
                    .update("notificationId", notificationId)
                    .addOnSuccessListener {
                        Log.d("FriendProfileFragment", "Notification sent successfully with ID: $notificationId")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FriendProfileFragment", "Failed to update notification ID: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FriendProfileFragment", "Failed to send notification: ${e.message}", e)
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
                                context?.let { Toast.makeText(activity, "Failed to remove friend from their list: ${e.message}", Toast.LENGTH_SHORT).show() }
                                Log.e("FriendProfileFragment", "Failed to remove friend from their list", e)
                            }
                    }
                    .addOnFailureListener { e ->
                        context?.let { Toast.makeText(activity, "Failed to remove friend from your list: ${e.message}", Toast.LENGTH_SHORT).show() }
                        Log.e("FriendProfileFragment", "Failed to remove friend from your list", e)
                    }
            }.addOnFailureListener {
                context?.let { Toast.makeText(activity, "Failed to fetch current user data", Toast.LENGTH_SHORT).show() }
                Log.e("FriendProfileFragment", "Failed to fetch current user data")
            }
        } else {
            context?.let { Toast.makeText(activity, "Current user is not logged in", Toast.LENGTH_SHORT).show() }
            Log.e("FriendProfileFragment", "Current user is not logged in")
        }
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_block_user, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_block_user -> {
                    // Block user function goes here
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

                    // Initialize achievement count
                    var numAchievements = 0
                    val achievedAchievements = mutableListOf<String>()

                    // Iterate through each achievement field and count the number achieved
                    for (field in achievementFields) {
                        val achieved = document.getBoolean(field) ?: false
                        if (achieved) {
                            numAchievements++
                            // Convert field names to readable achievement names
                            val achievementName = when (field) {
                                "bookGodAchieved" -> "Book God"
                                "firstChapterAchieved" -> "First Chapter"
                                "readingRookieAchieved" -> "Reading Rookie"
                                "storySeekerAchieved" -> "Story Seeker"
                                "novelNavigatorAchieved" -> "Novel Navigator"
                                "bookEnthusiastAchieved" -> "Book Enthusiast"
                                "legendaryLibrarianAchieved" -> "Legendary Librarian"
                                "fantasyExplorerAchieved" -> "Fantasy Explorer"
                                "historianAchieved" -> "Historian"
                                "mysterySolverAchieved" -> "Mystery Solver"
                                "psychExpertAchieved" -> "Psych Expert"
                                else -> field // Fallback to the field name
                            }
                            achievedAchievements.add(achievementName)
                            Log.d("FriendProfileFragment", "Achievement $achievementName achieved.")
                        } else {
                            Log.d("FriendProfileFragment", "Achievement $field not achieved.")
                        }
                    }

                    // Fetch profileExperienceTitle
                    val experienceTitle = document.getString("profileExperienceTitle") ?: "N/A"

                    // Log fetched data for debugging
                    Log.d("FriendProfileFragment", "Fetched profileExperienceTitle: $experienceTitle")
                    Log.d("FriendProfileFragment", "Number of Achievements: $numAchievements")
                    Log.d("FriendProfileFragment", "Top Genres: $topGenres")
                    Log.d("FriendProfileFragment", "Favorite Tag: $favoriteTag")

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

                    // Update achievements count
                    numAchievementsTextView.text = "$numAchievements"

                    // Update achievements list
                    if (::achievementsDetailsTextView.isInitialized) {
                        if (achievedAchievements.isNotEmpty()) {
                            achievementsDetailsTextView.text = achievedAchievements.joinToString("\n")
                        } else {
                            achievementsDetailsTextView.text = "None"
                        }
                    }

                    // Update experience title
                    experienceTitleTextView.text = "$experienceTitle"

                    // **Fetch and Update Level**
                    val level = document.getLong("level") ?: 0
                    levelTextView.text = "Lvl. $level"
                    Log.d("FriendProfileFragment", "User Level: $level")
                } else {
                    Toast.makeText(activity, "User does not exist", Toast.LENGTH_SHORT).show()
                    Log.e("FriendProfileFragment", "User document does not exist for ID: $friendId")
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FriendProfileFragment", "Error fetching user data", e)
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
                        val isFriend = friends.any { friendMap -> friendMap["friendId"] == friendId }
                        if (isFriend) {
                            // Already friends
                            addFriendButton.text = "Unfriend"
                            // Optionally, remove the Toast to avoid multiple messages
                            // Toast.makeText(activity, "Already friends", Toast.LENGTH_SHORT).show()
                        } else {
                            // Not friends
                            addFriendButton.text = "Add Friend"
                            // Toast.makeText(activity, "Not friends", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        addFriendButton.text = "Add Friend"
                        // Toast.makeText(activity, "No friends found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error checking friendship status: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("FriendProfileFragment", "Error checking friendship status", e)
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
                            // Creates a map of blocked user's details
                            val blockedUser = hashMapOf(
                                "blockedUserId" to receiverId,
                                "blockedUsername" to blockedUsername
                            )

                            // Update current user's blocked users array in database
                            db.collection("users").document(senderId)
                                .update("blockedUsers", FieldValue.arrayUnion(blockedUser))
                                .addOnSuccessListener {
                                    context?.let { Toast.makeText(it, "User blocked", Toast.LENGTH_SHORT).show() }
                                    Log.d("FriendProfileFragment", "User blocked successfully")
                                }
                                .addOnFailureListener { e ->
                                    context?.let { Toast.makeText(it, "Failed to block user: ${e.message}", Toast.LENGTH_SHORT).show() }
                                    Log.e("FriendProfileFragment", "Failed to block user", e)
                                }
                        } else {
                            context?.let { Toast.makeText(it, "Blocked user's username not found", Toast.LENGTH_SHORT).show() }
                            Log.e("FriendProfileFragment", "Blocked user's username not found for ID: $receiverId")
                        }
                    }
                } else {
                    context?.let { Toast.makeText(it, "Sender username not found", Toast.LENGTH_SHORT).show() }
                    Log.e("FriendProfileFragment", "Sender username not found for ID: $senderId")
                }
            }.addOnFailureListener {
                context?.let { Toast.makeText(it, "Failed to block user", Toast.LENGTH_SHORT).show() }
                Log.e("FriendProfileFragment", "Failed to fetch sender's username")
            }
        } else {
            context?.let { Toast.makeText(it, "User not authenticated", Toast.LENGTH_SHORT).show() }
            Log.e("FriendProfileFragment", "User not authenticated")
        }
    }

    // Function to decrement numFriends in database
    private fun decrementNumFriends(userRef: DocumentReference) {
        userRef.update("numFriends", FieldValue.increment(-1))
            .addOnSuccessListener {
                Log.d("FriendProfileFragment", "numFriends decremented successfully")
            }
            .addOnFailureListener { e ->
                Log.e("FriendProfileFragment", "Failed to decrement numFriends", e)
            }
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
