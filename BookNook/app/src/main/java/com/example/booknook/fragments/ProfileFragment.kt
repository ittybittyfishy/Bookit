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
