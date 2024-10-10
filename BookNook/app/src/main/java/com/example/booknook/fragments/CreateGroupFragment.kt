package com.example.booknook.fragments

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.booknook.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.jar.Manifest

class CreateGroupFragment : DialogFragment() {

    private var selectedBannerUri: Uri? = null  // To hold the selected banner image
    private lateinit var imagePreview: ImageView

    override fun onStart() {
        super.onStart()
        // Set the dialog's width and height programmatically
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,  // Set the width
            ViewGroup.LayoutParams.WRAP_CONTENT   // Set the height
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the UI elements
        val chipGroup: ChipGroup = view.findViewById(R.id.chip_group_groups)
        val confirmGroupButton: Button = view.findViewById(R.id.confirmGroupButton)
        val cancelGroupButton: Button = view.findViewById(R.id.cancelGroupButton)
        val groupName : EditText = view.findViewById(R.id.groupName)
        val privateToggle : CheckBox = view.findViewById(R.id.privateToggle)
        val uploadImageButton : Button = view.findViewById(R.id.uploadImageButton)
        imagePreview = view.findViewById(R.id.imagePreview)

        // Set up button listeners
        confirmGroupButton.setOnClickListener {

            val groupNameStr = groupName.text.toString().trim()
            val isPrivate = privateToggle.isChecked
            val selectedTags = getSelectedTags(chipGroup)

            if (groupNameStr.isEmpty()) {
                Toast.makeText(context, "Please enter a group name", Toast.LENGTH_SHORT).show()
            } else {
                if (selectedBannerUri != null) {
                    createGroup(groupNameStr, isPrivate, selectedTags, selectedBannerUri)
                    dismiss()
                } else {
                    // If no banner image is selected, create the group without it
                    createGroup(groupNameStr, isPrivate, selectedTags)
                    dismiss()
                }
            }
        }

        cancelGroupButton.setOnClickListener {
            dismiss()  // Close the dialog
        }

        uploadImageButton.setOnClickListener {
            // Trigger the image selection process
            openImagePicker()
        }

    }

    private fun createGroup(groupName: String, isPrivate: Boolean, tags: List<String>, bannerImg: Uri? = null) {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            val userId = user.uid

            // Check if bannerImg is provided
            if (bannerImg != null) {
                // Upload the banner image
                val storageRef = FirebaseStorage.getInstance().reference
                val bannerRef = storageRef.child("groupBannerImgs/$groupName-${System.currentTimeMillis()}.jpg")

                bannerRef.putFile(bannerImg)
                    .addOnSuccessListener { taskSnapshot ->
                        bannerRef.downloadUrl.addOnSuccessListener { uri ->
                            val bannerImgUrl = uri.toString()
                            createGroupInFirestore(groupName, isPrivate, userId, tags, bannerImgUrl, db)
                        }.addOnFailureListener { e ->
                            Log.w("CreateGroup", "Error getting banner image URL: ${e.message}")
                        }
                    }.addOnFailureListener { e ->
                        Log.w("CreateGroup", "Error uploading banner image: ${e.message}")
                    }
            } else {
                // If no banner image is provided, create the group without it
                createGroupInFirestore(groupName, isPrivate, userId, tags, null, db)
            }
        }
    }

    // Helper function to create the group in Firestore
    private fun createGroupInFirestore(groupName: String, isPrivate: Boolean, userId: String, tags: List<String>, bannerImgUrl: String?, db: FirebaseFirestore) {
        val groupData = hashMapOf(
            "groupName" to groupName,
            "private" to isPrivate,
            "members" to mutableListOf(userId),  // Initialize with the creator as the first member
            "books" to emptyList<Map<String, Any>>(),  // Empty book list initially
            "createdBy" to userId,
            "tags" to tags,
            "bannerImg" to bannerImgUrl,  // Store the URL of the uploaded banner image (or null)
            "messages" to emptyMap<String, Any>()
        )

        // Add the new group to Firestore
        db.collection("groups")
            .add(groupData)
            .addOnSuccessListener { documentReference ->
                val groupId = documentReference.id
                Log.d("CreateGroup", "Group created with ID: $groupId")

                // Update the user's document to include the new group ID in the 'joinedGroups' list
                db.collection("users").document(userId)
                    .update("joinedGroups", FieldValue.arrayUnion(groupId))
                    .addOnSuccessListener {
                        Toast.makeText(context, "Group created and added to your account", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Log.w("CreateGroup", "Error adding group to user: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                Log.w("CreateGroup", "Error creating group: ${e.message}")
            }
    }

    private fun getSelectedTags(chipGroup: ChipGroup): List<String> {
        val selectedTags = mutableListOf<String>()
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip?.isChecked == true) {
                selectedTags.add(chip.text.toString())
            }
        }
        return selectedTags
    }

    // To open the image picker
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICKER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri = data?.data
            if (selectedImageUri != null) {
                // Display the selected image
                imagePreview.setImageURI(selectedImageUri)
                // Update selectedBannerUri for later use
                selectedBannerUri = selectedImageUri
            } else {
                imagePreview.setImageResource(R.drawable.mr_blobby)  // Fallback image if URI is null
            }
        }
    }

    companion object {
        private const val IMAGE_PICKER_REQUEST_CODE = 1001
    }

}