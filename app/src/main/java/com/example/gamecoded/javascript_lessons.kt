package com.example.gamecoded

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class JavascriptLessons : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_javascript_lessons)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.javascript_lessons)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val javascript_Lesson1 = findViewById<Button>(R.id.lesson1)
        val javascript_Lesson2 = findViewById<Button>(R.id.lesson2)
        val javascript_Lesson3 = findViewById<Button>(R.id.lesson3)
        val javascript_Lesson4 = findViewById<Button>(R.id.lesson4)
        val javascript_Lesson5 = findViewById<Button>(R.id.lesson5)

//        javascript_Lesson1.setOnClickListener {
//            startActivity(Intent(this, javascript_lesson1::class.java))
//        }
//        javascript_Lesson2.setOnClickListener {
//            startActivity(Intent(this, javascript_lesson2::class.java))
//        }
//        javascript_Lesson3.setOnClickListener {
//            startActivity(Intent(this, javascript_lesson3::class.java))
//        }
//        javascript_Lesson4.setOnClickListener {
//            startActivity(Intent(this, javascript_lesson4::class.java))
//        }
//        javascript_Lesson5.setOnClickListener {
//            startActivity(Intent(this, javascript_lesson5::class.java))
//        }
    }
}