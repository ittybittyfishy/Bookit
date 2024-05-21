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


class Login : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var login: Button
    lateinit var registerButton: Button
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        login = findViewById(R.id.LoginButton)
        registerButton = findViewById(R.id.registerButton)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        registerButton.setOnClickListener {
            Log.d("TAG", "Button clicked") // Add this line
            val intent = Intent(this@Login, Register::class.java)
            // Start the HomePageActivity
            startActivity(intent)
            // Finish the current activity to prevent the user from navigating back to it
            finish()
        }

        login.setOnClickListener {
            Log.d("TAG", "Button clicked") // Add this line
            val txtEmail = email.text.toString()
            val txtPassword = password.text.toString()

            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast.makeText(this@Login, "Empty credentials!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(txtEmail, txtPassword)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword( email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null)
                    {
                        val userId = firebaseUser.uid
                        db.collection("users").document(userId).get().addOnSuccessListener { document ->
                            if (document != null)
                            {
                                val userEmail = document.getString("email")
                                Log.d("TAG", "User Email: $userEmail")
                                val userName = document.getString("username")
                                Log.d("TAG", "Username: $userName")

                                Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()

                                // Create an Intent to start the HomePageActivity
                                val intent = Intent(this@Login, HomePage::class.java)
                                // Pass the username as an extra to the intent
                                intent.putExtra("username", userEmail)
                                // Start the HomePageActivity
                                startActivity(intent)
                                // Finish the current activity to prevent the user from navigating back to it
                                finish()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@Login, "Login failed", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener(this) { exception ->
                // Handle registration failure
                Toast.makeText(this@Login, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

