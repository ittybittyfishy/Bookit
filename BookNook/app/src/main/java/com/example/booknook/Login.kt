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

class Login : AppCompatActivity() {
    lateinit var email: EditText
    lateinit var password: EditText
    lateinit var login: Button
    lateinit var registerButton: Button
    lateinit var auth: FirebaseAuth
    lateinit var db: FirebaseFirestore
    lateinit var forgotPass: TextView  // Change from Button to TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.emailEditText)
        password = findViewById(R.id.passwordEditText)
        login = findViewById(R.id.LoginButton)
        registerButton = findViewById(R.id.registerButton)
        forgotPass = findViewById(R.id.ForgotPass)  // Initialize the TextView

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        registerButton.setOnClickListener {
            Log.d("TAG", "Register Button clicked")
            val intent = Intent(this@Login, Register::class.java)
            startActivity(intent)
            finish()
        }

        login.setOnClickListener {
            Log.d("TAG", "Login Button clicked")
            val txtEmail = email.text.toString()
            val txtPassword = password.text.toString()

            if (txtEmail.isEmpty() || txtPassword.isEmpty()) {
                Toast.makeText(this@Login, "Empty credentials!", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(txtEmail, txtPassword)
            }
        }

        forgotPass.setOnClickListener {
            Log.d("TAG", "Forgot Password clicked")
            val intent = Intent(this@Login, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        val userId = firebaseUser.uid
                        db.collection("users").document(userId).get().addOnSuccessListener { document ->
                            if (document != null) {
                                val isFirstLogin = document.getBoolean("isFirstLogin") ?: true
                                val userEmail = document.getString("email")
                                Log.d("TAG", "User Email: $userEmail")
                                val userName = document.getString("username")
                                Log.d("TAG", "Username: $userName")

                                Toast.makeText(this@Login, "Login successful", Toast.LENGTH_SHORT).show()

                                val intent = Intent(this@Login, MainActivity::class.java)
                                intent.putExtra("isFirstLogin", isFirstLogin)
                                startActivity(intent)
                                finish()
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
}
