// Register.kt
package com.example.booknook

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
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        register = findViewById(R.id.registerButton)

        auth = FirebaseAuth.getInstance()

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

