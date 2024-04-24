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


class Register : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var register: Button
    lateinit var gotoLogin: Button
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        register = findViewById(R.id.registerButton)
        gotoLogin = findViewById(R.id.toLogin)

        auth = FirebaseAuth.getInstance()

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

            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast.makeText(this@Register, "Empty credentials!", Toast.LENGTH_SHORT).show()
            } else if (txtPassword.length < 6) {
                Toast.makeText(this@Register, "Password too short", Toast.LENGTH_SHORT).show()
            } else {
                registerUser(txtEmail, txtPassword)
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@Register, "Registration successful", Toast.LENGTH_SHORT).show()
                    val username = auth.currentUser?.email
                    // Create an Intent to start the Login
                    val intent = Intent(this@Register, Login::class.java)
                    // Pass the username as an extra to the intent
                    intent.putExtra("username", username)
                    // Start the HomePageActivity
                    startActivity(intent)
                    // Finish the current activity to prevent the user from navigating back to it
                    finish()
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

