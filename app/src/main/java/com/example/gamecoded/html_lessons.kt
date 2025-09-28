package com.example.gamecoded

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HtmlLessons : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_html_lessons)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.html_lessons)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val html_Lesson1 = findViewById<Button>(R.id.lesson1)
        val html_Lesson2 = findViewById<Button>(R.id.lesson2)
        val html_Lesson3 = findViewById<Button>(R.id.lesson3)
        val html_Lesson4 = findViewById<Button>(R.id.lesson4)
        val html_Lesson5 = findViewById<Button>(R.id.lesson5)

        html_Lesson1.setOnClickListener {
            startActivity(Intent(this, HtmlLesson1::class.java))
        }
        html_Lesson2.setOnClickListener {
            startActivity(Intent(this, HtmlLesson2::class.java))
        }
        html_Lesson3.setOnClickListener {
            startActivity(Intent(this, HtmlLesson3::class.java))
        }
        html_Lesson4.setOnClickListener {
            startActivity(Intent(this, HtmlLesson4::class.java))
        }
        html_Lesson5.setOnClickListener {
            startActivity(Intent(this, HtmlLesson5::class.java))
        }
    }
}