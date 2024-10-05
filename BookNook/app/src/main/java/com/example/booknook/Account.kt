package com.example.booknook

import android.content.Intent
import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity

// This is the Account class, which is an Activity.
// An Activity in Android is a single screen in an app where users can interact with UI elements.
// AppCompatActivity is a base class for activities, providing compatibility support for older versions of Android.
class Account : AppCompatActivity() {

    // Declaring FirebaseAuth and FirebaseFirestore instances that will be used for authentication and database operations.
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Declaring UI elements (EditText fields) to handle user input for password, name, and gender changes.
    private lateinit var currentPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var name: EditText
    private lateinit var gender: EditText

    // The onCreate method is the entry point of an activity.
    // It is called when the activity is created, and it's where you set up the UI and initial behavior.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account) // Setting the layout for this activity using a predefined XML file.

        // Binding the UI elements to the corresponding views in the layout (activity_account.xml).
        val savedButton: Button = findViewById(R.id.saveButton)
        newPassword = findViewById(R.id.newPassword)
        confirmPassword = findViewById(R.id.confirmedPassword)
        currentPassword = findViewById(R.id.currentPassword)
        name = findViewById(R.id.name_edit)
        gender = findViewById(R.id.genderEdit)

        // Initializing FirebaseAuth and FirebaseFirestore instances.
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Setting a click listener for the save button.
        // When the button is clicked, the app will check if any of the fields (password, name, gender) are filled and act accordingly.
        savedButton.setOnClickListener {
            // If any of the password fields are filled, it calls changePassword().
            if (newPassword.text.isNotEmpty() || currentPassword.text.isNotEmpty() || confirmPassword.text.isNotEmpty()) {
                changePassword()
            }
            // If the name field is filled, it calls changeName().
            if (name.text.isNotEmpty()) {
                changeName()
            }
            // If the gender field is filled, it calls changeGender().
            if (gender.text.isNotEmpty()) {
                changeGender()
            }

            // After saving changes, the app navigates back to the main activity (home page).
            val intent = Intent(this@Account, MainActivity::class.java)
            startActivity(intent) // Starting the main activity.
            finish() // Finishing the current activity to prevent users from going back to it.
        }
    }

    // Method to handle changing the user's password.
    private fun changePassword() {
        val newPassword = newPassword.text.toString()
        val confirmPassword = confirmPassword.text.toString()

        // Check if new password and confirm password match.
        if (newPassword != confirmPassword) {
            // Show an error message if the passwords do not match.
            Toast.makeText(this@Account, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        } else if (newPassword.length < 6) {
            // Show an error if the new password is too short (Firebase requires at least 6 characters).
            Toast.makeText(this@Account, "New password too short", Toast.LENGTH_SHORT).show()
            return
        } else if (currentPassword.text.toString() == newPassword) {
            // Show an error if the new password is the same as the current password.
            Toast.makeText(this@Account, "New password cannot be the same password", Toast.LENGTH_SHORT).show()
            return
        }

        // Getting the current user from FirebaseAuth.
        val user = auth.currentUser

        user?.let {
            // Reauthenticate the user by asking for the current password.
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword.text.toString())
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful) {
                        // If reauthentication is successful, proceed with updating the password.
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful) {
                                    // Notify the user that the password was updated successfully.
                                    Toast.makeText(
                                        this@Account,
                                        "Password updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    // Notify the user that the password update failed.
                                    Toast.makeText(
                                        this@Account,
                                        "Password update failed",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        // Notify the user that reauthentication failed (e.g., wrong current password).
                        Toast.makeText(
                            this@Account,
                            "Reauthentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    // Method to handle changing the user's name.
    private fun changeName() {
        val newName = name.text.toString()
        // Get the current user's ID from FirebaseAuth.
        val userId = auth.currentUser?.uid

        // If the user is logged in, update the name field in the Firestore database.
        userId?.let { uid ->
            db.collection("users").document(uid).update("name", newName).addOnSuccessListener {
                // Notify the user that the name was updated successfully.
                Toast.makeText(
                    this@Account,
                    "Name updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                // Notify the user of an error in updating the name.
                Toast.makeText(
                    this@Account,
                    "Error updating name: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Method to handle changing the user's gender.
    private fun changeGender() {
        val newGender = gender.text.toString()
        // Get the current user's ID from FirebaseAuth.
        val userId = auth.currentUser?.uid

        // If the user is logged in, update the gender field in the Firestore database.
        userId?.let { uid ->
            db.collection("users").document(uid).update("gender", newGender).addOnSuccessListener {
                // Notify the user that the gender was updated successfully.
                Toast.makeText(
                    this@Account,
                    "Gender updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                // Notify the user of an error in updating the gender.
                Toast.makeText(
                    this@Account,
                    "Error updating Gender: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
