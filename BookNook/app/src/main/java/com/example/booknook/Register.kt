// Register.kt
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


class Register : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var username: EditText
    lateinit var password: EditText
    lateinit var confirmPass: EditText
    lateinit var register: Button
    lateinit var gotoLogin: Button
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email = findViewById(R.id.emailEditText)
        username = findViewById(R.id.username)
        password = findViewById(R.id.passwordEditText)
        confirmPass = findViewById(R.id.confirmPassword)
        register = findViewById(R.id.registerButton)
        gotoLogin = findViewById(R.id.toLogin)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        gotoLogin.setOnClickListener {
            Log.d("TAG", "Button clicked") // Add this line
            val intent = Intent(this@Register, Login::class.java)
            // Start the Login
            startActivity(intent)
            // Finish the current activity to prevent the user from navigating back to it
            finish()
        }
        register.setOnClickListener {
            Log.d("TAG", "Button clicked") // Add this line
            val txtEmail = email.text.toString()
            val txtPassword = password.text.toString()
            val txtConfirmPass = confirmPass.text.toString()
            val txtUser = username.text.toString()

            if (txtEmail.isEmpty() || txtPassword.isEmpty() || txtConfirmPass.isEmpty() || txtUser.isEmpty()) {
                Toast.makeText(this@Register, "Empty credentials!", Toast.LENGTH_SHORT).show()
            } else if (txtPassword.length < 6) {
                Toast.makeText(this@Register, "Password too short", Toast.LENGTH_SHORT).show()
            } else if (txtPassword != txtConfirmPass) {
                Toast.makeText(this@Register, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            else {
                registerUser(txtEmail, txtPassword)
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        val txtUser = username.text.toString()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid
                        val userEmail = firebaseUser.email

                        // Predefined collections
                        val standardCollections = hashMapOf(
                            "Want to Read" to emptyMap<String, Any>(),
                            "Reading" to emptyMap<String, Any>(),
                            "Finished" to emptyMap<String, Any>(),
                            "Dropped" to emptyMap<String, Any>()
                        )

                        // create new user with email and timestamp
                        val user = hashMapOf(
                            "email" to userEmail,
                            "username" to txtUser,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "name" to "",
                            "birthday" to null,
                            "gender" to "",
                            "standardCollections" to standardCollections,
                            "customCollections" to emptyMap<String, Any>() // Initialize custom collections as empty
                        )

                        // Add a new document with a generated ID
                        db.collection("users").document(userId)
                            .set(user)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this@Register,
                                    "Registration successful",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@Register, Login::class.java)
                                intent.putExtra("username", userEmail)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this@Register,
                                    "Error saving user data: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                } else {
                    Toast.makeText(this@Register, "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                // Handle registration failure
                Toast.makeText(this@Register, "Registration failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

