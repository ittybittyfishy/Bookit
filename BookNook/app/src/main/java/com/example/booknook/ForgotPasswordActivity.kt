package com.example.booknook

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// This activity handles the "Forgot Password" functionality where users can request a password reset via email.
class ForgotPasswordActivity : AppCompatActivity() {

    // Declare variables for UI elements and Firebase authentication.
    private lateinit var emailEditText: EditText // EditText where users will input their email.
    private lateinit var resetPasswordButton: Button // Button to trigger the password reset process.
    private lateinit var auth: FirebaseAuth // FirebaseAuth instance to manage authentication.

    // The onCreate method is called when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password) // Set the layout for this activity using XML.

        // Initialize the UI elements by finding them in the layout.
        emailEditText = findViewById(R.id.emailEditText)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        // Initialize the FirebaseAuth instance to access Firebase's authentication methods.
        auth = FirebaseAuth.getInstance()

        // Set a click listener on the reset password button.
        // When the user clicks the button, it triggers the password reset process.
        resetPasswordButton.setOnClickListener {
            // Get the email entered by the user and trim any leading/trailing spaces.
            val email = emailEditText.text.toString().trim()

            // Check if the email field is empty.
            if (email.isEmpty()) {
                // If the email field is empty, show a Toast message to prompt the user to enter an email.
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
            } else {
                // If an email is entered, call the resetPassword() method to initiate the password reset process.
                resetPassword(email)
            }
        }
    }

    // Method to handle sending a password reset email.
    private fun resetPassword(email: String) {
        // FirebaseAuth method to send a password reset email to the given email address.
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task -> // Add a listener to check if the operation was successful.
                if (task.isSuccessful) {
                    // If the email is successfully sent, show a success message.
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                } else {
                    // If the email sending fails, show an error message.
                    Toast.makeText(this, "Failed to send reset email", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
