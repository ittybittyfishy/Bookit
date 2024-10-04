package com.example.booknook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// This activity handles user login, registration redirection, and password reset.
class Login : AppCompatActivity() {

    // Declare variables for UI elements and Firebase services.
    lateinit var email: EditText  // Input field for the user's email.
    lateinit var password: EditText  // Input field for the user's password.
    lateinit var login: Button  // Button for triggering the login process.
    lateinit var registerButton: Button  // Button to navigate to the registration page.
    lateinit var auth: FirebaseAuth  // FirebaseAuth instance to handle authentication.
    lateinit var db: FirebaseFirestore  // Firestore instance to access the database.
    lateinit var forgotPass: TextView  // TextView to navigate to password reset (Changed from Button to TextView).

    // The onCreate method is called when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)  // Set the layout for this activity.

        // Initialize the UI components by linking them to their corresponding views in the layout.
        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        login = findViewById(R.id.LoginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPass = findViewById(R.id.ForgotPass)  // Initialize the TextView for "Forgot Password".

        // Initialize Firebase services for authentication and Firestore database access.
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Handle the registration button click event.
        // This navigates the user to the Register activity when clicked.
        registerButton.setOnClickListener {
            Log.d("TAG", "Register Button clicked")  // Log message for debugging.
            val intent = Intent(this@Login, Register::class.java)  // Create an intent to open Register activity.
            startActivity(intent)  // Start the Register activity.
            finish()  // Close the current activity.
        }

        // Handle the login button click event.
        // This triggers the login process using the provided email and password.
        login.setOnClickListener {
            Log.d("TAG", "Login Button clicked")  // Log message for debugging.
            val txtEmail = email.text.toString()  // Get the text from the email input field.
            val txtPassword = password.text.toString()  // Get the text from the password input field.

            // Check if either the email or password fields are empty.
            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast.makeText(this@Login, "Empty credentials!", Toast.LENGTH_SHORT).show()  // Show an error if any field is empty.
            } else {
                // If both fields are filled, proceed with the login process.
                loginUser(txtEmail, txtPassword)
            }
        }

        // Handle the "Forgot Password" TextView click event.
        // This navigates the user to the ForgotPasswordActivity.
        forgotPass.setOnClickListener {
            Log.d("TAG", "Forgot Password clicked")  // Log message for debugging.
            val intent = Intent(this@Login, ForgotPasswordActivity::class.java)  // Create an intent to open ForgotPasswordActivity.
            startActivity(intent)  // Start the ForgotPasswordActivity.
        }
    }

    // This method handles the login process using Firebase Authentication.
    private fun loginUser(email: String, password: String) {
        // Attempt to sign in with email and password using FirebaseAuth.
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Check if the login was successful.
                if (task.isSuccessful) {
                    // Get the current authenticated user.
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid  // Get the user's unique ID.

                        // Update the user's status to "online" in the Firestore database.
                        db.collection("users").document(userId)
                            .update("isOnline", true)  // Set the user's "isOnline" status to true.
                            .addOnSuccessListener {
                                // After successfully updating, fetch additional user info from Firestore.
                                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                                    if (document != null) {
                                        // Get the "isFirstLogin" flag to see if it's the user's first login.
                                        val isFirstLogin = document.getBoolean("isFirstLogin") ?: true
                                        // Retrieve the user's email and username for debugging purposes.
                                        val userEmail = document.getString("email")
                                        Log.d("TAG", "User Email: $userEmail")  // Log the user's email.
                                        val userName = document.getString("username")
                                        Log.d("TAG", "Username: $userName")  // Log the user's username.

                                        // Show a success message to the user.
                                        Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()

                                        // Navigate to the main activity after login.
                                        val intent = Intent(this@Login, MainActivity::class.java)
                                        intent.putExtra("isFirstLogin", isFirstLogin)  // Pass the "isFirstLogin" flag to MainActivity.
                                        startActivity(intent)  // Start the MainActivity.
                                        finish()  // Close the Login activity.
                                    }
                                }
                            }
                    }
                } else {
                    // If the login fails, show an error message to the user.
                    Toast.makeText(this@Login, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                // If there is an error during the login process, show the error message.
                Toast.makeText(this@Login, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
