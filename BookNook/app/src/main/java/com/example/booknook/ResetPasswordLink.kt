package com.example.booknook

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

// This activity allows the user to reset their password after verifying their identity.
class ResetPasswordActivity : AppCompatActivity() {

    // Declare UI components
    private lateinit var newPasswordEditText: EditText  // Input field for the new password.
    private lateinit var confirmPasswordEditText: EditText  // Input field to confirm the new password.
    private lateinit var submitPasswordButton: Button  // Button to submit the new password.

    // The onCreate method is called when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)  // Set the layout for this activity.

        // Initialize the UI components by linking them to their corresponding views in the layout.
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        submitPasswordButton = findViewById(R.id.submitPasswordButton)

        // Set a listener for the "Submit Password" button click event.
        // This will handle the password reset process when clicked.
        submitPasswordButton.setOnClickListener {
            val newPassword = newPasswordEditText.text.toString()  // Get the new password entered by the user.
            val confirmPassword = confirmPasswordEditText.text.toString()  // Get the confirmation password entered by the user.

            // Check if the new password and confirm password match.
            if (newPassword == confirmPassword) {
                // Get the current authenticated user from FirebaseAuth.
                val auth = FirebaseAuth.getInstance()

                // Update the user's password using Firebase Authentication.
                auth.currentUser?.updatePassword(newPassword)
                    ?.addOnCompleteListener { task ->
                        // Check if the password update was successful.
                        if (task.isSuccessful) {
                            // If successful, show a success message.
                            Toast.makeText(this, "Password has been reset", Toast.LENGTH_SHORT).show()
                            // You can add code here to redirect the user to the login or home screen after a successful reset.
                        } else {
                            // If the reset fails, show an error message.
                            Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                // If the passwords don't match, show an error message.
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
