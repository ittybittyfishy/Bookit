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
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide  // Add this import for image loading
import com.example.booknook.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage  // Import Firebase Storage
import de.hdodenhof.circleimageview.CircleImageView
import java.io.ByteArrayOutputStream
import java.io.IOException

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

    // Declare TextView for displaying the number of collections
    private lateinit var numCollectionsTextView: TextView

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
            updateNumBooksRead(userId)
            updateNumCollections(userId)
            updateNumFriends(userId)
            updateNumReviews(userId)

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

    // Companion object to define constants
    companion object {
        // Constants for identifying the type of image picked
        private const val PICK_BANNER_IMAGE = 1
        private const val PICK_PROFILE_IMAGE = 2
    }
}
