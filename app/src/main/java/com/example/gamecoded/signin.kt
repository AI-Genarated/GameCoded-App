package com.example.gamecoded

import android.os.Bundle
import android.content.Intent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class SignIn : AppCompatActivity() {

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signin)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signin)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize FirebaseAuth instance
        firebaseAuth = FirebaseAuth.getInstance()

        // Initialize EditText fields
        email = findViewById(R.id.email)
        password = findViewById(R.id.password_toggle)

        // Set up button click listener
        val nextBtn = findViewById<Button>(R.id.next_signin)
        nextBtn.setOnClickListener {
            if (validateInput()) {
                loginUser()
            }
        }
    }

    private fun validateInput(): Boolean {
        val email = email.text.toString().trim()
        val password = password.text.toString().trim()

        return when {
            email.isEmpty() -> {
                showToast("Please enter your email")
                false
            }
            password.isEmpty() -> {
                showToast("Please enter your password")
                false
            }
            else -> true
        }
    }

    private fun loginUser() {
        val email = email.text.toString().trim()
        val password = password.text.toString().trim()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Navigate to ExplorePage upon successful login
                    startActivity(Intent(this, Explorer::class.java))
                    finish()
                } else {
                    showToast("Login failed: ${task.exception?.message}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}