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

    // Initialize Firestore
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

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

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid
                        db.collection("users").document(userId)
                            .update("isOnline", true)
                            .addOnSuccessListener {
                                db.collection("users").document(userId).get().addOnSuccessListener { document ->
                                    if (document != null) {
                                        val isFirstLogin = document.getBoolean("isFirstLogin") ?: true
                                        val userEmail = document.getString("email")
                                        val userName = document.getString("username")

                                        // Check if the user has the Book Nooker achievement
                                        checkAndAwardBookNookerAchievement(userId)

                                        Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()
                                        val intent = Intent(this@Login, MainActivity::class.java)
                                        intent.putExtra("isFirstLogin", isFirstLogin)
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                    }
                } else {
                    Toast.makeText(this@Login, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                Toast.makeText(this@Login, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    fun signInUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    checkAndAwardBookNookerAchievement(userId)
                }
            }
        }
    }

    private fun awardBookNookerAchievement(userId: String) {
        // Reference the user's document in Firestore
        val userDocRef = firestore.collection("users").document(userId)

        // Use a mutable map instead of HashMap
        val updates: MutableMap<String, Any> = mutableMapOf(
            "bookNookerAchieved" to true,
            "xp" to 50  // Assuming 50 XP for getting this achievement; adjust as necessary
        )

        // Apply the updates to Firestore
        userDocRef.update(updates)
            .addOnSuccessListener {
                // Achievement awarded successfully, inform the user
                Toast.makeText(this, "Congrats! You've unlocked the 'Book Nooker' achievement!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                // Handle any errors
                Toast.makeText(this, "Error awarding achievement: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    // Check if the user already has the "Book Nooker" achievement
    fun checkAndAwardBookNookerAchievement(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val bookNookerAchieved = document.getBoolean("bookNookerAchieved") ?: false
                if (!bookNookerAchieved) {
                    // Award the achievement if not yet awarded
                    awardBookNookerAchievement(userId)
                }
            }
        }
    }


}
