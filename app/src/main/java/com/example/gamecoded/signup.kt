package com.example.gamecoded

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.content.Intent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class signup : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var nextButton: Button
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var firestore: FirebaseFirestore

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_signup)
        userName = findViewById(R.id.name)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        confirmPassword = findViewById(R.id.confirm_password)

        nextButton = findViewById(R.id.next_signup)
        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nextButton.setOnClickListener {
            if (validateInputs()) {
                registerUser()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun validateInputs(): Boolean {
        val name = userName.text.toString().trim()
        val email = email.text.toString().trim()
        val password = password.text.toString().trim()
        val confirmPassword = confirmPassword.text.toString().trim()

        return when {
            name.isEmpty() -> {
                showToast("Please enter your name")
                false
            }
            email.isEmpty() -> {
                showToast("Please enter your email")
                false
            }
            password.isEmpty() -> {
                showToast("Please enter your password")
                false
            }
            confirmPassword.isEmpty() -> {
                showToast("Please confirm your password")
                false
            }
            password != confirmPassword -> {
                showToast("Passwords do not match")
                false
            }
            password.length < 6 -> {
                showToast("Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }

    private fun registerUser() {
        val email = email.text.toString().trim()
        val password = password.text.toString().trim()
        val name = userName.text.toString().trim()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserInfoToFirestore(name, email)
                    showToast("Registration successful")
                    startActivity(Intent(this, explorer::class.java))
                } else {
                    showToast("Registration failed: ${task.exception?.message}")
                }
            }
    }

    private fun saveUserInfoToFirestore(name: String, email: String) {
        val userId = firebaseAuth.currentUser?.uid
        val user = hashMapOf(
            "userId" to userId,
            "name" to name,
            "email" to email
        )

        if (userId != null) {
            firestore.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener {
                    showToast("Registration successful")
                    startActivity(Intent(this, explorer::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    showToast("Error saving user info: ${e.message}")
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}