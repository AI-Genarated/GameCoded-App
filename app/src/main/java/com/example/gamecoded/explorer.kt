package com.example.gamecoded

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class explorer : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_explorer)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.explorer)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val home_button = findViewById<ImageButton>(R.id.home_button)
        home_button.setOnClickListener {
            startActivity(Intent(this, explorer::class.java))
            }

        val lessons_button = findViewById<ImageButton>(R.id.lessons_button)
        home_button.setOnClickListener {
            startActivity(Intent(this, python_lessons::class.java))
        }

        }
    }