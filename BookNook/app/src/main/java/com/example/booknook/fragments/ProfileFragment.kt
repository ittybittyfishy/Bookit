package com.example.booknook.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide  // Add this import for image loading
import com.example.booknook.R
import com.google.api.Distribution.BucketOptions.Linear
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage  // Import Firebase Storage
import de.hdodenhof.circleimageview.CircleImageView
import org.w3c.dom.Text
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import com.example.booknook.MainActivity

//working version
// Define a Fragment class for the Profile section
class ProfileFragment : Fragment() {

    // Declare variables for UI elements
    private lateinit var bannerImage: ImageView
    private lateinit var profileImage: CircleImageView
    private lateinit var uploadBannerButton: Button
    private lateinit var uploadProfileButton: Button

    // Declare ActivityResultLauncher variables for handling image picking results
    private lateinit var pickBannerImageLauncher: ActivityResultLauncher<Intent>
    private lateinit var pickProfileImageLauncher: ActivityResultLauncher<Intent>

    // Declare TextView for displaying the user stats
    private lateinit var numCollectionsTextView: TextView
    private lateinit var numBooksReadTextView: TextView
    private lateinit var topGenresTextView: TextView
    private lateinit var favoriteTagTextView: TextView
    private lateinit var averageRatingTextView: TextView
    private lateinit var numReviewsTextView: TextView
    private lateinit var numFriendsTextView: TextView
    private lateinit var numGroupsTextView: TextView

    // Main user username
    private lateinit var userUsername: TextView

    // Declare variables for the EditText fields and ImageButtons
    private lateinit var quoteEditText: EditText
    private lateinit var characterEditText: EditText
    private lateinit var pencilButton1: ImageButton
    private lateinit var pencilButton2: ImageButton

    // Variables to store original hint texts
    private var originalQuoteHint: CharSequence? = null
    private var originalCharacterHint: CharSequence? = null

    // Firebase instances
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUser = auth.currentUser
    private val userId = currentUser?.uid  // Retrieves ID of the current user

    //make icons clickable
    // Initialize the UI elements for sections
    private lateinit var collectionsSection: LinearLayout
    private lateinit var groupsSection: LinearLayout
    private lateinit var friendsSection: LinearLayout
    private lateinit var achievementsSection: LinearLayout

    //Work review 4
    private lateinit var userLevelTextView: TextView
    private lateinit var numAchievementsTextView: TextView


    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        //work review 4
        userLevelTextView = view.findViewById(R.id.rectangle1)

        // Initialize the UI elements
        bannerImage = view.findViewById(R.id.bannerImage)
        profileImage = view.findViewById(R.id.profileImage)
        uploadBannerButton = view.findViewById(R.id.uploadBannerButton)
        uploadProfileButton = view.findViewById(R.id.uploadProfileButton)

        // Initialize the TextView for the stats
        numCollectionsTextView = view.findViewById(R.id.numCollectionsTextView)
        numBooksReadTextView = view.findViewById(R.id.numBooksReadTextView)
        topGenresTextView = view.findViewById(R.id.topGenresTextView)
        favoriteTagTextView = view.findViewById(R.id.favoriteTagTextView)
        averageRatingTextView = view.findViewById(R.id.averageRatingTextView)
        numReviewsTextView = view.findViewById(R.id.numReviewsTextView)
        numFriendsTextView = view.findViewById(R.id.numFriendsTextView)
        numGroupsTextView = view.findViewById(R.id.numGroupsTextView)


        //make icons clickable
        collectionsSection = view.findViewById(R.id.collections_section)
        groupsSection = view.findViewById(R.id.groups_section)
        friendsSection = view.findViewById(R.id.friends_section)
        achievementsSection = view.findViewById(R.id.achievements_section)

        //work review 4
        numAchievementsTextView = view.findViewById(R.id.numAchievementsTextView)

        // Set click listeners to navigate to the respective fragments
        collectionsSection.setOnClickListener {
            replaceFragment(CollectionFragment(), "Collections")
        }

        groupsSection.setOnClickListener {
            replaceFragment(GroupsFragment(), "Groups")
        }

        friendsSection.setOnClickListener {
            replaceFragment(FriendsFragment(), "Friends")
        }

        achievementsSection.setOnClickListener {
            replaceFragment(AchievementsFragment(), "Achievements")

        }



        // Initialize text view for the main user username
        userUsername = view.findViewById(R.id.userUsername)

        // Initialize EditText fields and ImageButtons for favorite quote/character
        quoteEditText = view.findViewById(R.id.rectangle4)
        characterEditText = view.findViewById(R.id.rectangle5)
        pencilButton1 = view.findViewById(R.id.pencil1)
        pencilButton2 = view.findViewById(R.id.pencil2)

        // Store original hint texts
        originalQuoteHint = quoteEditText.hint
        originalCharacterHint = characterEditText.hint

        // Disable editing on the EditText fields initially
        disableEditing(quoteEditText, pencilButton1)
        disableEditing(characterEditText, pencilButton2)

        // Register ActivityResultLauncher for picking banner image
        pickBannerImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        handleImageResult(data, PICK_BANNER_IMAGE)
                    }
                }
            }

        // Register ActivityResultLauncher for picking profile image
        pickProfileImageLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        handleImageResult(data, PICK_PROFILE_IMAGE)
                    }
                }
            }

        // Set click listeners to handle button clicks
        uploadBannerButton.setOnClickListener {
            // Call method to pick an image for the banner
            pickImageFromGallery(PICK_BANNER_IMAGE)
        }

        uploadProfileButton.setOnClickListener {
            // Call method to pick an image for the profile
            pickImageFromGallery(PICK_PROFILE_IMAGE)
        }

        // Set click listeners for the pencil icons to enable editing
        // Handle click on the first pencil icon (Quote)
        pencilButton1.setOnClickListener {
            if (!quoteEditText.isEnabled) {
                // Enable editing and change icon to save
                enableEditing(quoteEditText, pencilButton1)
            } else {
                // Disable editing, save data, and change icon back to pencil
                disableEditing(quoteEditText, pencilButton1)
                saveQuoteToFirestore(quoteEditText.text.toString())
            }
        }

        // Handle click on the second pencil icon (Character)
        pencilButton2.setOnClickListener {
            if (!characterEditText.isEnabled) {
                enableEditing(characterEditText, pencilButton2)
            } else {
                disableEditing(characterEditText, pencilButton2)
                saveCharacterToFirestore(characterEditText.text.toString())
            }
        }

        // Set OnEditorActionListener for the EditTexts to handle 'Done' action
        quoteEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                disableEditing(quoteEditText, pencilButton1)
                saveQuoteToFirestore(quoteEditText.text.toString())
                true
            } else {
                false
            }
        }

        characterEditText.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                disableEditing(characterEditText, pencilButton2)
                saveCharacterToFirestore(characterEditText.text.toString())
                true
            } else {
                false
            }
        }



        // If user is authenticated, update their stats
        if (userId != null) {
            updateTopGenres(userId)
            updateNumBooksRead(userId)
            updateFavoriteTag(userId)
            updateNumGroups(userId)
            updateNumCollections(userId)
            updateAverageRating(userId)
            updateNumReviews(userId)
            updateNumFriends(userId)
            updateUserLevel(userId)
            loadUnlockedAchievements(userId) // Sets up and populates the Spinner

                // Other data fetching methods...

            // Retrieve the username and other data from Firestore
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username")
                        val favoriteQuote = document.getString("favoriteQuote") ?: ""
                        val favoriteCharacter = document.getString("favoriteCharacter") ?: ""
                        val profileImageUrl = document.getString("profileImageUrl")
                        val bannerImageUrl = document.getString("bannerImageUrl")

                        // Set the username and other data to the TextViews
                        userUsername.text = username ?: "No Username"
                        quoteEditText.setText(favoriteQuote)
                        characterEditText.setText(favoriteCharacter)

                        // Load images from Firebase Storage using Glide
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
                        Toast.makeText(activity, "User document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error fetching user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        //work review 4
        userId?.let {
            loadUnlockedAchievements(it)
            displayNumAchievementsUnlocked(it)
        }

        // Return the created view
        return view
    }

    // Function to replace the current fragment
    fun replaceFragment(fragment: Fragment, title: String) {
        val transaction = parentFragmentManager.beginTransaction()  // Use parentFragmentManager
        transaction.replace(R.id.menu_container, fragment)  // Replace the fragment in the specified container
        transaction.commit()  // Commit the transaction

        // Use view?.findViewById or (activity as? MainActivity) to access views in the parent activity
        val bannerTextView = activity?.findViewById<TextView>(R.id.bannerTextView)
        bannerTextView?.text = title  // Update the banner text in the parent activity
    }


    // Function to enable editing on an EditText and change icon to save
    private fun enableEditing(editText: EditText, imageButton: ImageButton) {
        // Enable editing
        editText.isEnabled = true
        editText.isFocusable = true
        editText.isFocusableInTouchMode = true
        editText.isCursorVisible = true
        editText.requestFocus()

        // Remove hint text
        editText.hint = null

        // Show the keyboard
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)

        // Change pencil icon to save icon
        imageButton.setImageResource(R.drawable.save) // Ensure you have 'save' icon in drawable
    }

    // Function to disable editing on an EditText and change icon back to pencil
    private fun disableEditing(editText: EditText, imageButton: ImageButton) {
        // Disable editing
        editText.isEnabled = false
        editText.isFocusable = false
        editText.isFocusableInTouchMode = false
        editText.isCursorVisible = false

        // Restore hint text if field is empty
        if (editText.text.isEmpty()) {
            if (editText == quoteEditText) {
                editText.hint = originalQuoteHint
            } else if (editText == characterEditText) {
                editText.hint = originalCharacterHint
            }
        }

        // Hide the keyboard
        val imm = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)

        // Change save icon back to pencil icon
        imageButton.setImageResource(R.drawable.pencil) // Ensure you have 'pencil' icon in drawable
    }

    // Function to save favorite quote to Firestore
    private fun saveQuoteToFirestore(quote: String) {
        if (userId != null) {
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.update("favoriteQuote", quote)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Favorite quote saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error saving favorite quote: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    // Function to save favorite character to Firestore
    private fun saveCharacterToFirestore(character: String) {
        if (userId != null) {
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.update("favoriteCharacter", character)
                .addOnSuccessListener {
                    Toast.makeText(activity, "Favorite character saved", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(activity, "Error saving favorite character: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateTopGenres(userId: String) {
        val db = FirebaseFirestore.getInstance()

        // Fetch the document for the user
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Assuming "topGenres" is a field in the Firestore document and is a List of Strings
                    val topGenres = document.get("topGenres") as? List<String>

                    if (topGenres != null && topGenres.isNotEmpty()) {
                        // Join the list into a string to display it in the TextView
                        val topGenresString = topGenres.joinToString(", ")
                        Log.d("TAG", "Top Genres: $topGenresString") // Log the topGenres string
                        // Update the TextView with the retrieved genres
                        topGenresTextView.text = "$topGenresString"
                    } else {
                        // Handle case where topGenres is missing or empty
                        Log.d("TAG", "Top genres field is empty or null")
                        topGenresTextView.text = "N/A"
                    }
                } else {
                    // Handle case where the document doesn't exist
                    Log.d("TAG", "No such document for userId: $userId")
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                // Handle the failure to fetch data
                Log.d("TAG", "Error fetching user data", exception)
                Toast.makeText(context, "Failed to retrieve top genres: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    // Function updates the number of books the user has read
    private fun updateNumBooksRead(userId: String) {
        // References document of current user
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            // Retrieves the standardCollections map in database
            val standardCollections = document.get("standardCollections") as? Map<String, Any>
            // Retrieves the "Finished" array under the map
            val finishedBooks = standardCollections?.get("Finished") as? List<*>
            // Finds the size of the array to determine number of books read
            val numBooksRead = finishedBooks?.size ?: 0

            // Updates the numBooksRead field in database
            userDocRef.update("numBooksRead", numBooksRead)
                .addOnSuccessListener {
                    // Update text view here (if applicable)
                    numBooksReadTextView.text = "$numBooksRead"

                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error updating number of books read", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(activity, "Error getting number of books read: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Veronica Nguyen
    // Function to get the user's favorite tag
    private fun updateFavoriteTag(userId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(userId).get()  // Gets users collection
            .addOnSuccessListener { document ->
                val tagCount = mutableMapOf<String, Int>() // Map to count number of times a tag appears

                // Gets user's standard collections
                val standardCollections = document.get("standardCollections") as? Map<String, List<Map<String, Any>>>
                // Loops through each standard collection
                standardCollections?.values?.forEach { bookList ->
                    // Loops through each book in a collection
                    bookList.forEach { book ->
                        val tags = book["tags"] as? List<String> ?: listOf()  // Retrieves tags from a book
                        // Loops through each tag of a book
                        tags.forEach { tag ->
                            // Increments the count of that tag by 1
                            tagCount[tag] = tagCount.getOrDefault(tag, 0) + 1
                        }
                    }
                }

                // Gets user's custom collections
                val customCollections = document.get("customCollections") as? Map<String, Map<String, Any>>
                // Loops through each custom collection
                customCollections?.forEach { (_, collectionData) ->  //  Ignores key parameter of lambda expression
                    val books = collectionData["books"] as? List<Map<String, Any>>  // Gets the books in collection
                    // Loops through each book in a collection
                    books?.forEach { book ->
                        val tags = book["tags"] as? List<String> ?: listOf()  // Retrieves tags from a book
                        // Loops through each tag of a book
                        tags.forEach { tag ->
                            // Increments the count of that tag by 1
                            tagCount[tag] = tagCount.getOrDefault(tag, 0) + 1
                        }
                    }
                }

                // Sort tags by count in descending order and take the most frequent one
                val favoriteTag = tagCount.entries.maxByOrNull { it.value }?.key
                favoriteTag?.let { tag ->
                    db.collection("users").document(userId).update("favoriteTag", tag)
                        .addOnSuccessListener {
                            // Safely display the tag
                            favoriteTagTextView.text = tag.toString()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ProfileFragment", "Failed to update favorite tag: ${e.message}")
                            Toast.makeText(context, "Failed to update favorite tag: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } ?: run {
                    // Handle case where favoriteTag is null
                    Log.d("ProfileFragment", "No favorite tag found to update.")
                    favoriteTagTextView.text = "N/A"  // Display a placeholder or handle it appropriately
                }
            }
    }

    // Veronica Nguyen
    // Function updates the number of groups the user is in
    private fun updateNumGroups(userId: String) {
        Log.d("ProfileFragment", "updateNumGroups called with userId: $userId")

        // References document of current user
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            Log.d("ProfileFragment", "Document fetched: ${document.data}")

            // Retrieves the joinedGroups array from the database
            val groups = document.get("joinedGroups") as? List<*>
            Log.d("ProfileFragment", "Groups fetched: $groups")

            // Finds the size of the array to determine number of groups
            val numGroups = groups?.size ?: 0

            // Update the TextView to display the number of groups
            numGroupsTextView.text = "$numGroups"
            Log.d("ProfileFragment", "Number of groups: $numGroups")
        }.addOnFailureListener { e ->
            Log.e("ProfileFragment", "Error getting number of groups: ${e.message}")
            Toast.makeText(activity, "Error getting number of groups: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Veronica Nguyen
    // Function updates the number of custom collections a user has
    private fun updateNumCollections(userId: String) {
        // References document of current user
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            // Retrieves the customCollections map in database
            val customCollections = document.get("customCollections") as? Map<String, Any>
            // Finds the size of the map to determine number of collections
            val numCollections = customCollections?.size ?: 0

            // Updates the numCollections field in database
            userDocRef.update("numCollections", numCollections)
                .addOnSuccessListener {
                    // Update numCollectionsTextView with the value
                    numCollectionsTextView.text = "$numCollections"
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error updating number of collections", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(activity, "Error getting number of collections: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Veronica Nguyen
    // Function updates the average rating of the user
    fun updateAverageRating(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocRef = db.collection("users").document(userId)

        // Access all collections named "reviews" in the database
        db.collectionGroup("reviews")
            .whereEqualTo("userId", userId)  // Finds all documents with the user's id (current user)
            .get()
            .addOnSuccessListener { documents ->
                // Gets all of the user's ratings under reviews
                val userRatings = documents.mapNotNull { it.getDouble("rating") }
                if (userRatings.isNotEmpty()) {
                    // Calculate the user's average rating
                    val ratingsTotalSum = userRatings.sum()
                    val averageRating = ratingsTotalSum / userRatings.size

                    // Update the user's averageRating in Firestore
                    userDocRef.update("averageRating", averageRating)
                        .addOnFailureListener {
                            context?.let {
                                Toast.makeText(it, "Error updating average rating", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    context?.let {
                        Toast.makeText(it, "No ratings found", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                context?.let {
                    Toast.makeText(it, "Error getting user ratings", Toast.LENGTH_SHORT).show()
                }
            }
    }


    // Veronica Nguyen
    // Function updates the number of reviews the user has
    private fun updateNumReviews(userId: String) {
        // References document of current user
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            // Retrieves the numReviews field in database
            val numReviews = document.getLong("numReviews") ?: 0

            // Updates the numReviews field in database
            userDocRef.update("numReviews", numReviews)
                .addOnSuccessListener {
                    // Update text view here (if applicable)
                    numReviewsTextView.text = "$numReviews"
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error updating number of reviews", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(activity, "Error getting number of reviews: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Veronica Nguyen
    // Function updates the number of friends the user has
    private fun updateNumFriends(userId: String) {
        // References document of current user
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            // Retrieves the friends array in database
            val friends = document.get("friends") as? List<*>
            // Finds the size of the array to determine number of friends
            val numFriends = friends?.size ?: 0

            // Updates the numFriends field in database
            userDocRef.update("numFriends", numFriends)
                .addOnSuccessListener {
                    // Update text view here (if applicable)
                    numFriendsTextView.text = "$numFriends"
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Error updating number of friends", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e ->
            Toast.makeText(activity, "Error getting number of friends: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Method to open the gallery and pick an image
    private fun pickImageFromGallery(requestCode: Int) {
        // Create an Intent to open the image picker
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // Launch the appropriate ActivityResultLauncher based on the request code
        when (requestCode) {
            PICK_BANNER_IMAGE -> pickBannerImageLauncher.launch(intent)
            PICK_PROFILE_IMAGE -> pickProfileImageLauncher.launch(intent)
        }
    }

    // Method to handle the result of the image picking
    private fun handleImageResult(data: Intent, requestCode: Int) {
        // Get the selected image URI from the data
        val selectedImageUri: Uri? = data.data
        try {
            selectedImageUri?.let { uri ->
                // Upload the image to Firebase Storage
                uploadImageToFirebaseStorage(uri, requestCode)
            }
        } catch (e: Exception) {
            // Handle exceptions and show a toast message if loading the image fails
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to upload image to Firebase Storage
    private fun uploadImageToFirebaseStorage(imageUri: Uri, requestCode: Int) {
        val storageRef = storage.reference

        // Create a reference to the file you want to upload
        val imageRef = storageRef.child("profileImages/${userId}_${if (requestCode == PICK_PROFILE_IMAGE) "profile" else "banner"}.jpg")

        // Get the bitmap from the URI
        val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, imageUri))
        } else {
            MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, imageUri)
        }

        // Compress the bitmap and convert it to bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        // Upload the bytes to Firebase Storage
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            // Get the download URL
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Save the download URL to Firestore
                saveImageUrlToFirestore(uri.toString(), requestCode)

                // Load the image into the appropriate ImageView
                if (requestCode == PICK_PROFILE_IMAGE) {
                    Glide.with(this)
                        .load(uri.toString())
                        .into(profileImage)
                } else if (requestCode == PICK_BANNER_IMAGE) {
                    Glide.with(this)
                        .load(uri.toString())
                        .into(bannerImage)
                }

                Toast.makeText(requireContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to upload image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to save image URL to Firestore
    private fun saveImageUrlToFirestore(imageUrl: String, requestCode: Int) {
        if (userId != null) {
            val userDocRef = firestore.collection("users").document(userId)
            val field = if (requestCode == PICK_PROFILE_IMAGE) "profileImageUrl" else "bannerImageUrl"
            val data = mapOf(field to imageUrl)
            userDocRef.update(data)
                .addOnSuccessListener {
                    // Image URL saved successfully
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to save image URL: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    //work review 4
    private fun updateUserLevel(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                try {
                    // Check if the level is stored as a number
                    val userLevel = document.get("level") as? Number
                    if (userLevel != null) {
                        userLevelTextView.text = "Level ${userLevel.toLong()}"
                    } else {
                        // Handle if the level is null or not a number
                        Log.d("ProfileFragment", "Level is null or not a valid number")
                        userLevelTextView.text = "Level 0" // Default value
                    }
                } catch (e: ClassCastException) {
                    Log.e("ProfileFragment", "Field 'level' is not a valid Number: ${e.message}")
                    userLevelTextView.text = "Level 0" // Default to Level 0 in case of type mismatch
                }
            } else {
                Log.d("ProfileFragment", "No such document for user: $userId")
                userLevelTextView.text = "Level 0" // Default if document does not exist
            }
        }.addOnFailureListener { e ->
            Log.e("ProfileFragment", "Error fetching user level: ${e.message}")
            Toast.makeText(activity, "Failed to retrieve user level", Toast.LENGTH_SHORT).show()
        }
    }


    // Updated loadUnlockedAchievements to call loadSavedTitle after setup
    private fun loadUnlockedAchievements(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Initialize the list for unlocked achievements
                    val unlockedAchievements = mutableListOf<String>()

                    // Check for each achievement and add it to the list if unlocked
                    val achievementFields = mapOf(
                        "firstChapterAchieved" to "First Chapter",
                        "readingRookieAchieved" to "Reading Rookie",
                        "storySeekerAchieved" to "Story Seeker",
                        "novelNavigatorAchieved" to "Novel Navigator",
                        "bookEnthusiastAchieved" to "Book Enthusiast",
                        "legendaryLibrarianAchieved" to "Legendary Librarian",
                        "bookGodAchieved" to "Book God",
                        "fantasyExplorerAchieved" to "Fantasy Explorer",
                        "historyAchieved" to "Historian",
                        "mysterySolverAchieved" to "Mystery Solver",
                        "psychAchieved" to "Psych Expert"
                    )

                    // Loop through the map and add achievements that are true
                    for ((field, name) in achievementFields) {
                        if (document.getBoolean(field) == true) {
                            unlockedAchievements.add(name)
                            Log.d("ProfileFragment", "Unlocked Achievement: $name")
                        }
                    }

                    Log.d("ProfileFragment", "Total Unlocked Achievements: $unlockedAchievements")

                    // Populate the Spinner with all unlocked achievements
                    setupSpinner(unlockedAchievements) {
                        // Callback after spinner is set up
                        loadSavedTitle(userId)
                    }

                } else {
                    Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "User document does not exist.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProfileFragment", "Error fetching unlocked achievements: ${e.message}")
                Toast.makeText(context, "Failed to retrieve achievements: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Modified setupSpinner to accept a callback
    private fun setupSpinner(unlockedAchievements: List<String>, onSetupComplete: () -> Unit) {
        val spinner: Spinner = view?.findViewById(R.id.rectangle3) ?: return

        if (unlockedAchievements.isNotEmpty()) {
            // Create an ArrayAdapter with the unlocked achievements
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                unlockedAchievements
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            Log.d("ProfileFragment", "Spinner Adapter Set with: $unlockedAchievements")

            // Set an OnItemSelectedListener to handle item selection
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                var isInitialSelection = true

                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedTitle = parent.getItemAtPosition(position).toString()
                    Log.d("ProfileFragment", "Spinner selected: $selectedTitle")

                    if (isInitialSelection) {
                        // Avoid saving the title when initially setting the selection
                        isInitialSelection = false
                        Log.d("ProfileFragment", "Initial selection, not saving.")
                        return
                    }

                    // Call the function to save the selected title to Firestore
                    saveSelectedTitleToUserProfile(selectedTitle)
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    Log.d("ProfileFragment", "Spinner no selection")
                }
            }

            // Ensure that the spinner's adapter is set before calling onSetupComplete
            onSetupComplete()
        } else {
            // Handle the case where no achievements are unlocked
            Toast.makeText(context, "No achievements unlocked yet.", Toast.LENGTH_SHORT).show()
            Log.d("ProfileFragment", "No achievements unlocked to populate spinner.")
        }
    }

    private fun saveSelectedTitleToUserProfile(title: String) {
        Log.d("ProfileFragment", "Saving selected title: $title")
        val userId = auth.currentUser?.uid ?: run {
            Log.e("ProfileFragment", "User ID is null. Cannot save title.")
            Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
            return
        }
        val userDocRef = firestore.collection("users").document(userId)

        // Update the user's profile with the selected achievement title
        userDocRef.update("profileExperienceTitle", title)
            .addOnSuccessListener {
                Log.d("ProfileFragment", "Profile title updated to: $title")
                Toast.makeText(context, "Profile title updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileFragment", "Error updating profile title: ", exception)
                Toast.makeText(context, "Failed to update profile title. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadSavedTitle(userId: String) {
        Log.d("ProfileFragment", "Loading saved title for user: $userId")
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val savedTitle = document.getString("profileExperienceTitle")
                Log.d("ProfileFragment", "Loaded savedTitle: $savedTitle")
                if (savedTitle != null && view != null) {
                    val spinner: Spinner = view!!.findViewById(R.id.rectangle3)
                    val adapter = spinner.adapter as? ArrayAdapter<String>
                    if (adapter != null) {
                        // Set the saved title as the selected item if it exists in the adapter
                        val position = adapter.getPosition(savedTitle)
                        Log.d("ProfileFragment", "Spinner position for savedTitle: $position")
                        if (position != -1) {
                            spinner.setSelection(position)
                            Log.d("ProfileFragment", "Spinner set to position: $position")
                        } else {
                            Log.w("ProfileFragment", "Saved title '$savedTitle' not found in spinner adapter")
                            // Optionally set a default selection or notify the user
                            // spinner.setSelection(0)
                            // Toast.makeText(context, "Saved title not found. Please select a new title.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Log.e("ProfileFragment", "Spinner adapter is null")
                    }
                } else {
                    Log.w("ProfileFragment", "Saved title is null or view is null")
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileFragment", "Error loading saved profile title: ", exception)
        }
    }


    //work review 4
    private fun displayNumAchievementsUnlocked(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // List of achievement fields to check
                val achievementFields = listOf(
                    "bookNookerAchieved",
                    "firstChapterAchieved",
                    "readingRookieAchieved",
                    "storySeekerAchieved",
                    "novelNavigatorAchieved",
                    "bookEnthusiastAchieved",
                    "legendaryLibrarianAchieved",
                    "bookGodAchieved",
                    "fantasyExplorerAchieved",
                    "historyAchieved",
                    "mysterySolverAchieved",
                    "psychAchieved"
                    // Add more achievement fields as needed
                )

                // Count achievements that are true (unlocked)
                val unlockedCount = achievementFields.count { field ->
                    document.getBoolean(field) == true
                }

                // Update the TextView
                numAchievementsTextView.text = "$unlockedCount"
            } else {
                numAchievementsTextView.text = "0"
            }
        }.addOnFailureListener { exception ->
            Log.e("ProfileFragment", "Error fetching user achievements: ", exception)
            Toast.makeText(context, "Failed to load achievements. Try again.", Toast.LENGTH_SHORT).show()
        }
    }


    // Companion object to define constants
    companion object {
        // Constants for identifying the type of image picked
        private const val PICK_BANNER_IMAGE = 1
        private const val PICK_PROFILE_IMAGE = 2
    }
}
