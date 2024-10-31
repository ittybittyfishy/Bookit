package com.example.booknook.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
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
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.booknook.GroupItem
import com.example.booknook.R
import com.google.android.gms.tasks.Tasks
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditGroupFragment : DialogFragment() {

    private lateinit var groupNameEditText: EditText
    private lateinit var privateToggleCheckBox: CheckBox
    private lateinit var chipGroup: ChipGroup
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button
    private lateinit var deleteButton: Button
    private lateinit var transferOwnership: Button
    private lateinit var bannerImg: ImageView
    private var selectedBannerUri: Uri? = null // To hold the new selected banner image URI

    companion object {
        private const val ARG_GROUP_ID = "group_id"
        private const val IMAGE_PICKER_REQUEST_CODE = 1001

        fun newInstance(groupId: String): EditGroupFragment {
            val fragment = EditGroupFragment()
            val args = Bundle().apply {
                putString(ARG_GROUP_ID, groupId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_group, container, false)

        groupNameEditText = view.findViewById(R.id.groupName)
        privateToggleCheckBox = view.findViewById(R.id.privateToggle)
        chipGroup = view.findViewById(R.id.chip_group_groups)
        confirmButton = view.findViewById(R.id.confirmGroupButton)
        cancelButton = view.findViewById(R.id.cancelGroupButton)
        bannerImg = view.findViewById(R.id.imagePreview)
        deleteButton = view.findViewById(R.id.deleteGroup)
        transferOwnership = view.findViewById(R.id.transferOwnerGroup)
        val uploadImageButton: Button = view.findViewById(R.id.uploadImageButton)

        // Fetch group details using groupId
        arguments?.getString(ARG_GROUP_ID)?.let { groupId ->
            fetchGroupDetails(groupId)
        }

        confirmButton.setOnClickListener {
            val updatedGroupName = groupNameEditText.text.toString().trim()
            val isPrivate = privateToggleCheckBox.isChecked
            val selectedTags = getSelectedTags(chipGroup)

            if (updatedGroupName.isEmpty()) {
                Toast.makeText(context, "Please enter a group name", Toast.LENGTH_SHORT).show()
            } else {
                arguments?.getString(ARG_GROUP_ID)?.let { groupId ->
                    updateGroupDetails(groupId)
                }
            }
        }

        // Handle cancel button click
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Handle upload image button click to select a new banner
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        // handle transfering ownership
        transferOwnership.setOnClickListener{
            arguments?.getString(ARG_GROUP_ID)?.let { groupId ->
            transferOwner(groupId)}
        }

        // Handle deleting a group
        deleteButton.setOnClickListener{
            arguments?.getString(ARG_GROUP_ID)?.let { groupId ->
                deleteGroup(groupId)
            }

        }


        return view
    }

    private fun fetchGroupDetails(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val groupName = document.getString("groupName")
                    val isPrivate = document.getBoolean("private") ?: false
                    val bannerImgUrl = document.getString("bannerImg") ?: ""
                    val existingGenres = document.get("tags") as? List<String> ?: emptyList()

                    // Update UI with the fetched details
                    groupNameEditText.setText(groupName)
                    privateToggleCheckBox.isChecked = isPrivate
                    loadBannerImage(bannerImgUrl)
                    loadExistingGenrePreferences(chipGroup, existingGenres)
                }
            }
            .addOnFailureListener { e ->
                Log.e("EditGroupFragment", "Error fetching group details", e)
            }
    }

    private fun loadBannerImage(url: String) {
        if (url.isNotEmpty()) {
            Glide.with(this) // or Picasso
                .load(url)
                .into(bannerImg)
        }
    }

    private fun loadExistingGenrePreferences(chipGroup: ChipGroup, existingGenres: List<String>) {
        for (i in 0 until chipGroup.childCount) {
            val chip = chipGroup.getChildAt(i) as? Chip
            if (chip != null && existingGenres.contains(chip.text.toString())) {
                chip.isChecked = true
            }
        }
    }

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
                bannerImg.setImageURI(selectedImageUri)
                selectedBannerUri = selectedImageUri
            }
        }
    }

    private fun updateGroupDetails(groupId: String) {
        val groupNameStr = groupNameEditText.text.toString().trim()
        val isPrivate = privateToggleCheckBox.isChecked
        val selectedTags = getSelectedTags(chipGroup)

        val db = FirebaseFirestore.getInstance()
        val groupRef = db.collection("groups").document(groupId)

        if (selectedBannerUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
            val bannerRef = storageRef.child("groupBannerImgs/$groupNameStr-${System.currentTimeMillis()}.jpg")

            bannerRef.putFile(selectedBannerUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    bannerRef.downloadUrl.addOnSuccessListener { uri ->
                        val bannerImgUrl = uri.toString()
                        saveGroupToFirestore(groupRef, groupNameStr, isPrivate, selectedTags, bannerImgUrl)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("EditGroupFragment", "Error uploading banner image: ${e.message}")
                }
        } else {
            saveGroupToFirestore(groupRef, groupNameStr, isPrivate, selectedTags, null)
        }
    }

    private fun saveGroupToFirestore(
        groupRef: DocumentReference,
        groupName: String,
        isPrivate: Boolean,
        tags: List<String>,
        bannerImgUrl: String?
    ) {
        val updatedData = mapOf(
            "groupName" to groupName,
            "private" to isPrivate,
            "tags" to tags,
            "bannerImg" to bannerImgUrl
        )

        groupRef.update(updatedData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Group updated successfully", Toast.LENGTH_SHORT).show()

                // Notify the GroupsFragment to refresh its data
                val result = Bundle().apply {
                    putBoolean("refresh", true)
                }
                parentFragmentManager.setFragmentResult("groupUpdated", result)

                dismiss()
            }
            .addOnFailureListener { e ->
                Log.e("EditGroupFragment", "Error updating group: ${e.message}")
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

    private fun deleteGroup(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance().reference

        // Step 1: Fetch the group's data
        db.collection("groups").document(groupId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Fetch the list of members and banner image URL
                    val members = document.get("members") as? List<String> ?: emptyList()
                    val bannerImgUrl = document.getString("bannerImg")

                    // Step 2: Delete the group from Firestore
                    db.collection("groups").document(groupId).delete()
                        .addOnSuccessListener {
                            Log.d("DeleteGroup", "Group successfully deleted from Firestore")

                            // Step 3: Remove the group ID from each user's joinedGroups list
                            for (userId in members) {
                                db.collection("users").document(userId)
                                    .update("joinedGroups", FieldValue.arrayRemove(groupId), "numGroups", FieldValue.increment(-1))
                                    .addOnSuccessListener {
                                        Log.d("DeleteGroup", "Removed group from user: $userId")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("DeleteGroup", "Error removing group from user: ${e.message}")
                                    }
                            }

                            // Step 4: Delete the group's banner image from Storage (if it exists)
                            if (!bannerImgUrl.isNullOrEmpty()) {
                                val bannerRef = storage.storage.getReferenceFromUrl(bannerImgUrl)
                                bannerRef.delete()
                                    .addOnSuccessListener {
                                        Log.d("DeleteGroup", "Banner image successfully deleted")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("DeleteGroup", "Error deleting banner image: ${e.message}")
                                    }
                            }

                            Toast.makeText(requireContext(), "Group deleted successfully", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.w("DeleteGroup", "Error deleting group from Firestore: ${e.message}")
                        }
                } else {
                    Log.w("DeleteGroup", "Group not found in Firestore")
                }
            }
            .addOnFailureListener { e ->
                Log.w("DeleteGroup", "Error fetching group data: ${e.message}")
            }
    }

    private fun transferOwner(groupId: String) {
        val db = FirebaseFirestore.getInstance()
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle("Transfer Group Ownership")

        // Step 1: Fetch the members list from Firestore
        db.collection("groups").document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Get the members list
                    val memberIds = documentSnapshot.get("members") as? List<String> ?: emptyList()

                    // Step 2: Create a map to store user IDs and usernames
                    val userIdToUsername = mutableMapOf<String, String>()

                    // Step 3: Fetch each member's username from Firestore
                    val tasks = memberIds.map { memberId ->
                        db.collection("users").document(memberId).get().addOnSuccessListener { userDoc ->
                            if (userDoc.exists()) {
                                val username = userDoc.getString("username") ?: "Unknown"
                                userIdToUsername[memberId] = username
                            }
                        }
                    }

                    // Step 4: Wait for all username fetch tasks to complete
                    Tasks.whenAll(tasks).addOnSuccessListener {
                        // Prepare list of usernames for the dialog
                        val usernames = memberIds.map { userIdToUsername[it] ?: "Unknown" }.toTypedArray()

                        // Step 5: Show the dialog with usernames for selection
                        dialog.setItems(usernames) { _, which ->
                            val newOwnerId = memberIds[which]

                            // Step 6: Update Firestore with the new owner ID
                            db.collection("groups").document(groupId)
                                .update("createdBy", newOwnerId)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Ownership transferred successfully", Toast.LENGTH_SHORT).show()
                                    dismiss()  // Close dialog on success
                                }
                        }
                        dialog.show()
                    }
                }
            }
    }

}