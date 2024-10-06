package com.example.booknook.fragments

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.booknook.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AccountFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var nameEdit: EditText
    private lateinit var genderEdit: EditText
    private lateinit var birthdayEdit: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize UI elements
        currentPassword = view.findViewById(R.id.currentPassword)
        newPassword = view.findViewById(R.id.newPassword)
        confirmPassword = view.findViewById(R.id.confirmedPassword)
        nameEdit = view.findViewById(R.id.name_edit)
        genderEdit = view.findViewById(R.id.genderEdit)
        birthdayEdit = view.findViewById(R.id.birthdayEditText)
        val saveButton: Button = view.findViewById(R.id.saveButton)

        // Initialize Firebase authentication and Firestore
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Set a click listener on the save button to perform actions based on input fields
        saveButton.setOnClickListener{
            // Check if password fields are filled and trigger password change if so
            if (newPassword.text.isNotEmpty()
                && confirmPassword.text.isNotEmpty()
                && currentPassword.text.isNotEmpty())
            {
                changePassword()
            }

            saveUserDataIfChanged()
        }

        // allows user to select the date instead of inputting it manually
        birthdayEdit.setOnClickListener {
            val calendar = Calendar.getInstance()

            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Set the selected date to the birthday EditText in MM/dd/yyyy format
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, dayOfMonth)
                    birthdayEdit.setText(SimpleDateFormat("MM/dd/yyyy", Locale.US).format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePicker.show()
        }

        autofillUserData()
    }

    private fun autofillUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid

            // Fetch user data from Firestore
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val name = document.getString("name") ?: ""
                        val birthday = document.getTimestamp("birthday")?.toDate() ?: Date()
                        val gender = document.getString("gender") ?: ""

                        // Populate the text fields with the retrieved data
                        nameEdit.setText(name)
                        birthdayEdit.setText(SimpleDateFormat("MM/dd/yyyy", Locale.US).format(birthday))
                        genderEdit.setText(gender)
                    } else {
                        Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Settings", "Error fetching user data: ${e.message}")
                }
        }
    }

    // Function to handle password change
    private fun changePassword() {
        // Get new and confirmed passwords from the input fields
        val newPass = newPassword.text.toString()
        val confirmPass = confirmPassword.text.toString()

        // Check if new password and confirm password match
        if (newPass != confirmPass) {
            // Show an error message or toast indicating passwords don't match
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        // Ensure the new password meets the minimum length requirement
        else if (newPass.length < 6)
        {
            Toast.makeText(requireContext(), "New password too short", Toast.LENGTH_SHORT).show()
            return
        }
        // Ensure the new password is not the same as the current password
        else if (currentPassword.text.toString() == newPass)
        {
            Toast.makeText(requireContext(), "New password cannot be the same password", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user
        val user = auth.currentUser

        user?.let {
            // Reauthenticate the user before changing the password
            val credential =
                EmailAuthProvider.getCredential(user.email!!, currentPassword.text.toString())
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful)
                    {
                        // Reauthentication successful, now update the password
                        user.updatePassword(newPass)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful)
                                {
                                    // Password updated successfully
                                    Toast.makeText(
                                        requireContext(),
                                        "Password updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else
                                {
                                    // Password update failed, show error message
                                    Toast.makeText(
                                        requireContext(),
                                        "Password update failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                    else
                    {
                        // Reauthentication failed, show error message
                        Toast.makeText(
                            requireContext(),
                            "Reauthentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun saveUserDataIfChanged() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid

            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val storedName = document.getString("name") ?: ""
                        val storedGender = document.getString("gender") ?: ""
                        val storedBirthday = document.getTimestamp("birthday")?.toDate()

                        val newName = nameEdit.text.toString()
                        val newGender = genderEdit.text.toString()

                        // Safely get the birthday string using try-catch
                        val newBirthday: Date? = try {
                            SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(birthdayEdit.text.toString())
                        } catch (e: ParseException) {
                            null // In case parsing fails
                        }

                        // Only update fields that have changed
                        val updatedData = mutableMapOf<String, Any>()
                        if (newName != storedName) updatedData["name"] = newName
                        if (newGender != storedGender) updatedData["gender"] = newGender
                        if (newBirthday != storedBirthday && newBirthday != null) updatedData["birthday"] = newBirthday

                        if (updatedData.isNotEmpty()) {
                            // Update Firestore only if there is new data to save
                            db.collection("users").document(userId)
                                .update(updatedData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(context, "No changes made.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }
    }

}