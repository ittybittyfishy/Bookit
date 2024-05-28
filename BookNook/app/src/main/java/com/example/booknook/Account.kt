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

class Account : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var currentPassword: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var name: EditText
    private lateinit var gender: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        val savedButton : Button = findViewById(R.id.saveButton)
        newPassword = findViewById(R.id.newPassword)
        confirmPassword = findViewById(R.id.confirmedPassword)
        currentPassword = findViewById(R.id.currentPassword)
        name = findViewById(R.id.name_edit)
        gender = findViewById(R.id.genderEdit)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()


        savedButton.setOnClickListener{
            if (newPassword.text.isNotEmpty() || currentPassword.text.isNotEmpty() || confirmPassword.text.isNotEmpty())
            {
                changePassword()
            }
            if (name.text.isNotEmpty())
            {
                changeName()
            }
            if (gender.text.isNotEmpty())
            {
                changeGender()
            }

            // Create an Intent to start the HomePageActivity
            val intent = Intent(this@Account, MainActivity::class.java)
            // Start the HomePageActivity
            startActivity(intent)
            // Finish the current activity to prevent the user from navigating back to it
            finish()
        }
    }

    private fun changePassword() {
        val newPassword = newPassword.text.toString()
        val confirmPassword = confirmPassword.text.toString()

        // Check if new password and confirm password match
        if (newPassword != confirmPassword) {
            // Show an error message or toast indicating passwords don't match
            Toast.makeText(this@Account, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        else if (newPassword.length < 6)
        {
            Toast.makeText(this@Account, "New password too short", Toast.LENGTH_SHORT).show()
            return
        }

        else if (currentPassword.text.toString() == newPassword)
        {
            Toast.makeText(this@Account, "New password cannot be the same password", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user
        val user = auth.currentUser

        user?.let {
            // Call reauthenticate method to reauthenticate the user
            val credential =
                EmailAuthProvider.getCredential(user.email!!, currentPassword.text.toString())
            user.reauthenticate(credential)
                .addOnCompleteListener { reauthTask ->
                    if (reauthTask.isSuccessful)
                    {
                        // Reauthentication successful, now update the password
                        user.updatePassword(newPassword)
                            .addOnCompleteListener { updatePasswordTask ->
                                if (updatePasswordTask.isSuccessful)
                                {
                                    // Password updated successfully
                                    Toast.makeText(
                                        this@Account,
                                        "Password updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                else
                                {
                                    // Password update failed, show error message
                                    Toast.makeText(
                                        this@Account,
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
                            this@Account,
                            "Reauthentication failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun changeName() {
        val newName = name.text.toString()
        // Get the current user
        val userId = auth.currentUser?.uid

        // If user is logged in, fetch username from Firebase
        userId?.let { uid ->
            db.collection("users").document(uid).update("name",newName).addOnSuccessListener {
                Toast.makeText(
                    this@Account,
                    "Name updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@Account,
                    "Error updating name: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun changeGender()
    {
        val newGender = gender.text.toString()
        // Get the current user
        val userId = auth.currentUser?.uid

        // If user is logged in, fetch username from Firebase
        userId?.let { uid ->
            db.collection("users").document(uid).update("gender",newGender).addOnSuccessListener {
                Toast.makeText(
                    this@Account,
                    "Gender updated successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener { e ->
                Toast.makeText(
                    this@Account,
                    "Error updating Gender: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}