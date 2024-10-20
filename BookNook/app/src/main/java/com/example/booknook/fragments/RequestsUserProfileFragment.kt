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
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class RequestsUserProfileFragment : Fragment() {
    // Declare variables for UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var userUsername: TextView
    private lateinit var quoteEditText: EditText
    private lateinit var characterEditText: EditText

    // Declare TextView for displaying the number of collections

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

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_friend_request_profile, container, false)

        val senderId = arguments?.getString("senderId")  // Receives receiverId from friends fragment
        val receiverUsername = arguments?.getString("receiverUsername")


        // Initialize UI elements
        bannerImage = view.findViewById(R.id.bannerImage)
        profileImage = view.findViewById(R.id.profileImage)
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

        //block user menu
        threeDotsButton = view.findViewById(R.id.threeDotsButton)

        threeDotsButton.setOnClickListener { view ->
            showPopupMenu(view)
        }

        // Disable editing on EditTexts
        quoteEditText.isEnabled = false
        characterEditText.isEnabled = false

        // Load friend's data
        if (senderId != null) {
            loadUserData(senderId)
        } else {
            Toast.makeText(activity, "Friend user ID not provided", Toast.LENGTH_SHORT).show()
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

    private fun loadUserData(friendId: String) {
        val userDocRef = FirebaseFirestore.getInstance().collection("users").document(friendId)
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
                } else {
                    Toast.makeText(activity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
