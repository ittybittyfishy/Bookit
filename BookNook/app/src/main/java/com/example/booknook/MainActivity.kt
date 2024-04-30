// MainActivity.kt
package com.example.booknook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


//testing comment
class MainActivity : AppCompatActivity() {
    lateinit var register: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register) // Set activity_register.xml as the content view

        register = findViewById(R.id.registerButton)

        register.setOnClickListener {
            startActivity(Intent(this@MainActivity, Register::class.java))
        }
    }
}


