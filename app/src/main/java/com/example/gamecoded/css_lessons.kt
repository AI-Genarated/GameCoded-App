package com.example.gamecoded

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CssLessons : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_css_lessons)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.css_lessons)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val css_Lesson1 = findViewById<Button>(R.id.lesson1)
        val css_Lesson2 = findViewById<Button>(R.id.lesson2)
        val css_Lesson3 = findViewById<Button>(R.id.lesson3)
        val css_Lesson4 = findViewById<Button>(R.id.lesson4)
        val css_Lesson5 = findViewById<Button>(R.id.lesson5)

//        css_Lesson1.setOnClickListener {
//            startActivity(Intent(this, css_lesson1::class.java))
//        }
//        css_Lesson2.setOnClickListener {
//            startActivity(Intent(this, css_lesson2::class.java))
//        }
//        css_Lesson3.setOnClickListener {
//            startActivity(Intent(this, css_lesson3::class.java))
//        }
//        css_Lesson4.setOnClickListener {
//            startActivity(Intent(this, css_lesson4::class.java))
//        }
//        css_Lesson5.setOnClickListener {
//            startActivity(Intent(this, css_lesson5::class.java))
//        }
    }
}