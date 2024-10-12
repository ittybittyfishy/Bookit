package com.example.booknook.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.io.IOException

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

    // Declare TextView for displaying the number of collections
    private lateinit var numCollectionsTextView: TextView

    // Method called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Initialize the UI elements
        bannerImage = view.findViewById(R.id.bannerImage)
        profileImage = view.findViewById(R.id.profileImage)
        uploadBannerButton = view.findViewById(R.id.uploadBannerButton)
        uploadProfileButton = view.findViewById(R.id.uploadProfileButton)

        // Initialize the TextView for number of collections
        numCollectionsTextView = view.findViewById(R.id.numCollectionsTextView)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val userId: String? = currentUser?.uid  // Retrieves id of the current user

        // Register ActivityResultLauncher for picking banner image
        pickBannerImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    handleImageResult(data, PICK_BANNER_IMAGE)
                }
            }
        }

        // Register ActivityResultLauncher for picking profile image
        pickProfileImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

        // Veronica Nguyen
        // Function updates the number of books the user has read
        fun updateNumBooksRead(userId: String) {
            // References document of current user
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

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
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Error updating number of books read", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Error getting number of books read: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Veronica Nguyen
        // Function updates the number of custom collections a user has
        fun updateNumCollections(userId: String) {
            // References document of current user
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

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
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Error updating number of collections", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Error getting number of collections: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Veronica Nguyen
        // Function updates the number of friends the user has
        fun updateNumFriends(userId: String) {
            // References document of current user
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                // Retrieves the friends array in database
                val friends = document.get("friends") as? List<*>
                // Finds the size of the array to determine number of friends
                val numFriends = friends?.size ?: 0

                // Updates the numFriends field in database
                userDocRef.update("numFriends", numFriends)
                    .addOnSuccessListener {
                        // Update text view here (if applicable)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Error updating number of friends", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Error getting number of friends: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Veronica Nguyen
        // Function updates the number of reviews the user has
        fun updateNumReviews(userId: String) {
            // References document of current user
            val userDocRef = FirebaseFirestore.getInstance().collection("users").document(userId)

            userDocRef.get().addOnSuccessListener { document ->
                // Retrieves the numReviews field in database
                val numReviews = document.getLong("numReviews") ?: 0

                // Updates the numReviews field in database
                userDocRef.update("numReviews", numReviews)
                    .addOnSuccessListener {
                        // Update text view here (if applicable)
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(activity, "Error updating number of reviews", Toast.LENGTH_SHORT).show()
                    }
            }.addOnFailureListener { e ->
                Toast.makeText(activity, "Error getting number of reviews: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // If user is authenticated, update their stats
        if (userId != null) {
            updateNumBooksRead(userId)
            updateNumCollections(userId)
            updateNumFriends(userId)
            updateNumReviews(userId)
        } else {
            Toast.makeText(activity, "User not authenticated", Toast.LENGTH_SHORT).show()
        }

        // Return the created view
        return view
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
        // Update view here using "topGenres" field in database
    }

    // Veronica Nguyen
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

                // Update user's favoriteTag field in Firestore
                db.collection("users").document(userId).update("favoriteTag", favoriteTag)
                    .addOnSuccessListener {
                        // Update text view here

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to update favorite tag: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Failed to retrieve collections: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
    }

    // Veronica Nguyen
    // Function updates the number of groups the user is in
    private fun updateNumGroups(userId: String) {
        // To-do
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

        // Accesses all collections named "reviews" in database
        db.collectionGroup("reviews")
            .whereEqualTo("userId", userId)  // Finds all documents with the user's id (current user)
            .get()
            .addOnSuccessListener { documents ->
                // Gets all of the user's ratings under reviews
                val userRatings = documents.mapNotNull { it.getDouble("rating") }
                if (userRatings.isNotEmpty()) {
                    // Gets the sum of all of the ratings
                    val ratingsTotalSum = userRatings.sum()
                    // Calculates the user's average rating
                    val averageRating = ratingsTotalSum / userRatings.size
                    // Rounds the average rating to two decimal places
                    val roundedAverageRating = BigDecimal(averageRating).setScale(2, RoundingMode.HALF_UP).toDouble()

                    // Updates the averageRating field in database
                    userDocRef.update("averageRating", roundedAverageRating)
                        .addOnSuccessListener {
                            // Update text view here (if applicable)
                        }
                        .addOnFailureListener {
                            Toast.makeText(activity, "Error updating average rating", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(activity, "No ratings found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(activity, "Error getting user ratings", Toast.LENGTH_SHORT).show()
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
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    // Use ImageDecoder for API 28 and above
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireActivity().contentResolver, uri))
                } else {
                    // Fallback to method for older API levels
                    requireActivity().contentResolver.openInputStream(uri)?.use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    } ?: throw IOException("Unable to open input stream for URI")
                }
                // Set the bitmap to the appropriate ImageView based on the request code
                when (requestCode) {
                    PICK_BANNER_IMAGE -> bannerImage.setImageBitmap(bitmap)
                    PICK_PROFILE_IMAGE -> profileImage.setImageBitmap(bitmap)
                }
            }
        } catch (e: Exception) {
            // Handle exceptions and show a toast message if loading the image fails
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    // Companion object to define constants
    companion object {
        // Constants for identifying the type of image picked
        private const val PICK_BANNER_IMAGE = 1
        private const val PICK_PROFILE_IMAGE = 2
    }
}
