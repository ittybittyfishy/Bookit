package com.example.booknook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// This activity handles user registration, where users can create a new account.
class Register : AppCompatActivity() {

    // Declare variables for UI elements and Firebase services.
    lateinit var email: EditText  // Input field for the user's email.
    lateinit var username: EditText  // Input field for the user's desired username.
    lateinit var password: EditText  // Input field for the user's password.
    lateinit var confirmPass: EditText  // Input field to confirm the password.
    lateinit var register: Button  // Button to trigger the registration process.
    lateinit var gotoLogin: Button  // Button to navigate to the login page.
    lateinit var auth: FirebaseAuth  // FirebaseAuth instance to handle authentication.
    lateinit var db: FirebaseFirestore  // Firestore instance to access the database.

    // The onCreate method is called when the activity is created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)  // Set the layout for this activity.

        // Initialize the UI components by linking them to their corresponding views in the layout.
        email = findViewById(R.id.emailEditText)
        username = findViewById(R.id.username)
        password = findViewById(R.id.passwordEditText)
        confirmPass = findViewById(R.id.confirmPassword)
        register = findViewById(R.id.registerButton)
        gotoLogin = findViewById(R.id.toLogin)

        // Initialize Firebase services for authentication and Firestore database access.
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Handle the "Go to Login" button click event.
        // This navigates the user to the Login activity when clicked.
        gotoLogin.setOnClickListener {
            Log.d("TAG", "Button clicked")  // Log message for debugging.
            val intent = Intent(this@Register, Login::class.java)  // Create an intent to open Login activity.
            startActivity(intent)  // Start the Login activity.
            finish()  // Close the current activity to prevent the user from navigating back to it.
        }

        // Handle the "Register" button click event.
        // This triggers the registration process when clicked.
        register.setOnClickListener {
            Log.d("TAG", "Button clicked")  // Log message for debugging.
            val txtEmail = email.text.toString()  // Get the text from the email input field.
            val txtPassword = password.text.toString()  // Get the text from the password input field.
            val txtConfirmPass = confirmPass.text.toString()  // Get the text from the confirm password input field.
            val txtUser = username.text.toString()  // Get the text from the username input field.

            // Check if any fields are empty.
            if (txtEmail.isEmpty() || txtPassword.isEmpty() || txtConfirmPass.isEmpty() || txtUser.isEmpty()) {
                Toast.makeText(this@Register, "Empty credentials!", Toast.LENGTH_SHORT).show()  // Show an error if any field is empty.
            } else if (txtPassword.length < 6) {
                // Show an error if the password is too short.
                Toast.makeText(this@Register, "Password too short", Toast.LENGTH_SHORT).show()
            } else if (txtPassword != txtConfirmPass) {
                // Show an error if the password and confirm password fields don't match.
                Toast.makeText(this@Register, "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                // Check if the username already exists.
                checkIfUsernameExists(txtUser) { exists ->
                    if (exists) {
                        // Show an error if the username is already taken.
                        Toast.makeText(this@Register, "Username already exists", Toast.LENGTH_SHORT).show()
                    } else {
                        // If the username is available, proceed to register the user.
                        registerUser(txtEmail, txtPassword)
                    }
                }
            }
        }
    }

    // Method to check if the username already exists in the Firestore database.
    private fun checkIfUsernameExists(username: String, callback: (Boolean) -> Unit) {
        // Query the "users" collection in Firestore to check if any user has the same username.
        db.collection("users").whereEqualTo("username", username).get()
            .addOnSuccessListener { documents -> callback(!documents.isEmpty) }  // If documents are found, the username exists.
            .addOnFailureListener { exception ->  // Handle query failure.
                Toast.makeText(this@Register, "Username check failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    // Method to register a new user using Firebase Authentication and Firestore.
    private fun registerUser(email: String, password: String) {
        val txtUser = username.text.toString()  // Get the username from the input field.

        // Register the user with Firebase using email and password.
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid  // Get the newly created user's ID.
                        val userEmail = firebaseUser.email  // Get the user's email.

                        // Initialize the user's standard collections (default book categories).
                        val standardCollections = hashMapOf(
                            "Want to Read" to mutableListOf<Map<String, Any>>(),
                            "Reading" to mutableListOf<Map<String, Any>>(),
                            "Finished" to mutableListOf<Map<String, Any>>(),
                            "Dropped" to mutableListOf<Map<String, Any>>()  // Default collections for books.
                        )

                        // Create a user object with the user's details.
                        val user = hashMapOf(
                            "email" to userEmail,
                            "username" to txtUser,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),  // Automatically set the account creation timestamp.
                            "name" to "",  // User's full name (empty for now).
                            "birthday" to null,  // User's birthday (not set yet).
                            "gender" to "",  // User's gender (empty for now).
                            "joinedGroups" to mutableListOf<String>(),
                            "isFirstLogin" to true,  // Flag to indicate if this is the user's first login.
                            "standardCollections" to standardCollections,  // Add the default collections to the user.
                            "customCollections" to emptyMap<String, Any>() // Initialize custom collections as empty.
                        )

                        // Save the user object in Firestore under the "users" collection.
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                // Show a success message once the user data is saved in Firestore.
                                Toast.makeText(this@Register, "Registration successful", Toast.LENGTH_SHORT).show()

                                // Navigate to the login page after successful registration.
                                val intent = Intent(this@Register, Login::class.java)
                                intent.putExtra("username", userEmail)  // Pass the registered email to the login page.
                                startActivity(intent)  // Start the Login activity.
                                finish()  // Close the Register activity.
                            }
                            .addOnFailureListener { e ->
                                // Show an error message if saving user data to Firestore fails.
                                Toast.makeText(this@Register, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    // If registration fails, show an error message.
                    Toast.makeText(this@Register, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                // Handle registration failure by showing an error message.
                Toast.makeText(this@Register, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
