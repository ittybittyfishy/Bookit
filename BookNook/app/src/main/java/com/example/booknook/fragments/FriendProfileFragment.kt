package com.example.booknook.fragments

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.firestore.FieldValue


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

    //block user menu
    private lateinit var threeDotsButton: ImageButton




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the friend's user ID from the arguments
        friendUserId = arguments?.getString(ARG_FRIEND_USER_ID)
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

        // Set up Add Friend button
        addFriendButton.setOnClickListener {
            friendUserId?.let { friendId ->
                toggleFriendshipStatus(friendId)
            }
        }

        return view
    }

    private fun showPopupMenu(view: View) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(R.menu.menu_block_user, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_block_user -> {
                    //block user function goes here
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

                    // Check if the current user is already friends with this user
                    checkFriendshipStatus(friendId)
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
                    val friends = document.get("friends") as? List<*>
                    if (friends != null && friends.contains(friendId)) {
                        // Already friends
                        addFriendButton.text = "Remove Friend"
                    } else {
                        // Not friends
                        addFriendButton.text = "Add Friend"
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error checking friendship status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun toggleFriendshipStatus(friendId: String) {
        if (currentUserId == null) return

        val currentUserDocRef = firestore.collection("users").document(currentUserId)

        currentUserDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val friends = document.get("friends") as? MutableList<String> ?: mutableListOf()

                    if (friends.contains(friendId)) {
                        // Remove friend
                        friends.remove(friendId)
                        currentUserDocRef.update("friends", friends)
                            .addOnSuccessListener {
                                addFriendButton.text = "Add Friend"
                                Toast.makeText(activity, "Friend removed", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Add friend
                        friends.add(friendId)
                        currentUserDocRef.update("friends", friends)
                            .addOnSuccessListener {
                                addFriendButton.text = "Remove Friend"
                                Toast.makeText(activity, "Friend added", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error updating friendship status: ${e.message}", Toast.LENGTH_SHORT).show()
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
